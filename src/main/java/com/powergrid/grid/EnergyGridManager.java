package com.powergrid.grid;

import com.powergrid.block.HeavyAccumulatorBlock;
import com.powergrid.block.SolarPanelBlock;
import com.powergrid.block.TransformerBlock;
import com.powergrid.block.WireConnectorBlock;
import com.powergrid.block.entity.BreakerSwitchBlockEntity;
import com.powergrid.block.entity.PoleStreetLightBlockEntity;
import com.powergrid.block.entity.PowerCableBlockEntity;
import com.powergrid.block.entity.SolarPanelBlockEntity;
import com.powergrid.block.entity.TransformerBlockEntity;
import com.powergrid.block.entity.WindmillRotorBlockEntity;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import com.powergrid.block.entity.WireRelayBlockEntity;
import com.powergrid.block.entity.WoodenWaterWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class EnergyGridManager {

    private record PathNode(BlockPos pos, WireType incomingWireType, double totalDistance) {}

    private static class SinkCandidate {
        final BlockEntity targetBE;
        final EnergyHandler sinkEnergy;
        final double distance;
        final float efficiency;
        final int acceptedSim;
        final int requestedFromSource;

        SinkCandidate(BlockEntity targetBE, EnergyHandler sinkEnergy, double distance, float efficiency, int acceptedSim, int requestedFromSource) {
            this.targetBE = targetBE;
            this.sinkEnergy = sinkEnergy;
            this.distance = distance;
            this.efficiency = efficiency;
            this.acceptedSim = acceptedSim;
            this.requestedFromSource = requestedFromSource;
        }
    }

    private static final Map<ServerLevel, Long> LAST_UNLOADED_TICK = new WeakHashMap<>();

    /**
     * Executes energy distribution from a source WireConnectorBlockEntity across the wire network.
     * Uses GridNetworkSavedData to traverse through loaded or unloaded relays, switches, and connectors.
     */
    public static int distributePower(WireConnectorBlockEntity source) {
        Level level = source.getLevel();
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return 0;

        // An OUTPUT-only connector never supplies power to the grid
        if (source.getMode() == ConnectorMode.OUTPUT) return 0;

        List<WireConnection> initialConns = source.getConnections();
        if (initialConns.isEmpty()) return 0;

        BlockPos sourcePos = source.getBlockPos();
        BlockState sourceState = source.getBlockState();
        if (!sourceState.hasProperty(WireConnectorBlock.FACING)) return 0;

        Direction facing = sourceState.getValue(WireConnectorBlock.FACING);
        Direction attachedFace = facing.getOpposite();
        BlockPos attachedPos = sourcePos.relative(attachedFace);
        EnergyHandler sourceNeighbor = level.getCapability(Capabilities.Energy.BLOCK, attachedPos, facing);

        WireType wireType = source.getSupportedWireType();
        int rate = wireType.getTransferRate();

        // 1. Measure sustainable supply rate for unloaded telemetry caching in SavedData
        int sustainableSupply = measureSustainableSupply(serverLevel, source, attachedPos, sourceNeighbor, rate);
        boolean isSolar = isSolarAttached(serverLevel, attachedPos);
        BlockPos transPos = getAttachedTransformerPos(serverLevel, attachedPos);

        GridNetworkSavedData savedData = GridNetworkSavedData.get(serverLevel);
        savedData.updateConnectorSupply(sourcePos, sustainableSupply, isSolar, transPos);

        // 2. Traverse network via BFS across loaded AND unloaded nodes using GridNetworkSavedData
        List<SinkCandidate> sinks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<PathNode> queue = new ArrayDeque<>();

        visited.add(sourcePos);
        queue.add(new PathNode(sourcePos, wireType, 0.0));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            BlockPos currentPos = current.pos();

            List<WireConnection> outgoingConns = getConnectionsForNode(serverLevel, savedData, currentPos);

            for (WireConnection conn : outgoingConns) {
                BlockPos targetPos = conn.getTargetPos();
                if (visited.contains(targetPos)) continue;

                boolean chunkLoaded = serverLevel.hasChunkAt(targetPos);
                GridNetworkSavedData.SavedNode savedNode = savedData.getNode(targetPos);

                // If breaker switch, check if closed
                if (chunkLoaded) {
                    BlockEntity be = serverLevel.getBlockEntity(targetPos);
                    if (be instanceof BreakerSwitchBlockEntity breaker) {
                        if (!breaker.isCircuitClosed()) continue;
                    }
                } else if (savedNode != null && savedNode.nodeType == GridNetworkSavedData.NodeType.BREAKER) {
                    if (!savedNode.isCircuitClosed()) continue;
                }

                visited.add(targetPos);
                double dist = current.totalDistance() + conn.getDistance();

                // If loaded, evaluate live BlockEntity sinks
                if (chunkLoaded) {
                    BlockEntity targetBE = serverLevel.getBlockEntity(targetPos);

                    if (targetBE instanceof WireConnectorBlockEntity targetConnector) {
                        if (targetConnector.getMode() != ConnectorMode.INPUT) {
                            BlockState targetState = targetConnector.getBlockState();
                            if (targetState.hasProperty(WireConnectorBlock.FACING)) {
                                Direction targetFacing = targetState.getValue(WireConnectorBlock.FACING);
                                BlockPos targetAttached = targetPos.relative(targetFacing.getOpposite());
                                EnergyHandler sinkEnergy = serverLevel.getCapability(Capabilities.Energy.BLOCK, targetAttached, targetFacing);

                                if (sinkEnergy != null) {
                                    if (source.getMode() == ConnectorMode.AUTO && targetConnector.getMode() == ConnectorMode.AUTO && sourceNeighbor != null) {
                                        BlockState sinkAttachedState = serverLevel.getBlockState(targetAttached);
                                        BlockState srcAttachedState = serverLevel.getBlockState(attachedPos);

                                        boolean srcIsStorage = srcAttachedState.getBlock() instanceof HeavyAccumulatorBlock || srcAttachedState.getBlock() instanceof TransformerBlock;
                                        boolean sinkIsStorage = sinkAttachedState.getBlock() instanceof HeavyAccumulatorBlock || sinkAttachedState.getBlock() instanceof TransformerBlock;

                                        if (srcIsStorage && sinkIsStorage) {
                                            continue;
                                        }
                                    }

                                    int acceptedSim;
                                    try (var tx = Transaction.openRoot()) {
                                        acceptedSim = sinkEnergy.insert(rate, tx);
                                    }

                                    if (acceptedSim > 0) {
                                        double lossFreeDist = wireType.getLossFreeDistance();
                                        double effectiveLossDist = Math.max(0.0, dist - lossFreeDist);
                                        float loss = Math.min(0.95f, (float) (effectiveLossDist * wireType.getLossPerBlock()));
                                        float eff = Math.max(0.05f, 1.0f - loss);
                                        int req = (int) Math.ceil((double) acceptedSim / eff);
                                        sinks.add(new SinkCandidate(targetConnector, sinkEnergy, dist, eff, acceptedSim, req));
                                    }
                                }
                            }
                        }
                        queue.add(new PathNode(targetPos, targetConnector.getSupportedWireType(), dist));
                    } else if (targetBE instanceof PoleStreetLightBlockEntity poleLight) {
                        int needed = PoleStreetLightBlockEntity.BUFFER_CAPACITY - poleLight.energyStorage.getAmountAsInt();
                        if (needed > 0) {
                            int acceptedSim;
                            try (var tx = Transaction.openRoot()) {
                                acceptedSim = poleLight.energyStorage.insert(Math.min(rate, needed), tx);
                            }
                            if (acceptedSim > 0) {
                                double lossFreeDist = wireType.getLossFreeDistance();
                                double effectiveLossDist = Math.max(0.0, dist - lossFreeDist);
                                float loss = Math.min(0.95f, (float) (effectiveLossDist * wireType.getLossPerBlock()));
                                float eff = Math.max(0.05f, 1.0f - loss);
                                int req = (int) Math.ceil((double) acceptedSim / eff);
                                sinks.add(new SinkCandidate(poleLight, poleLight.energyStorage, dist, eff, acceptedSim, req));
                            }
                        }
                        queue.add(new PathNode(targetPos, wireType, dist));
                    } else if (targetBE instanceof WireRelayBlockEntity || targetBE instanceof BreakerSwitchBlockEntity) {
                        queue.add(new PathNode(targetPos, wireType, dist));
                    }
                } else if (savedNode != null) {
                    // Node is currently in an unloaded chunk; traverse through it using SavedData edges
                    queue.add(new PathNode(targetPos, savedNode.wireType, dist));
                }
            }
        }

        if (sinks.isEmpty()) return 0;

        // 3. Determine how much energy is needed and available
        int totalRequested = 0;
        for (SinkCandidate sink : sinks) {
            totalRequested += sink.requestedFromSource;
        }

        int maxToPull = Math.min(rate, totalRequested);
        int available = source.energyStorage.getAmountAsInt();

        if (available < maxToPull && sourceNeighbor != null) {
            try (var simTx = Transaction.openRoot()) {
                int simExtracted = sourceNeighbor.extract(maxToPull - available, simTx);
                available += simExtracted;
            }
        }

        if (available <= 0) return 0;

        int toTransmit = Math.min(maxToPull, available);

        // 4. Deliver to sinks and extract ONLY what was actually accepted (+ line loss)
        int totalTransferred = 0;
        try (var tx = Transaction.openRoot()) {
            int budgetRemaining = toTransmit;

            for (SinkCandidate sink : sinks) {
                if (budgetRemaining <= 0) break;

                int alloc = (int) Math.round((double) toTransmit * (sink.requestedFromSource / (double) totalRequested));
                alloc = Math.min(alloc, budgetRemaining);
                if (alloc <= 0) continue;

                int netForSink = Math.max(1, Math.round(alloc * sink.efficiency));
                int inserted = sink.sinkEnergy.insert(netForSink, tx);
                if (inserted > 0) {
                    int sourceConsumed = (int) Math.ceil((double) inserted / sink.efficiency);
                    sourceConsumed = Math.min(sourceConsumed, alloc);

                    int extractedForThisSink = 0;
                    if (source.energyStorage.getAmountAsInt() > 0) {
                        extractedForThisSink += source.energyStorage.extract(sourceConsumed, tx);
                    }
                    if (extractedForThisSink < sourceConsumed && sourceNeighbor != null) {
                        extractedForThisSink += sourceNeighbor.extract(sourceConsumed - extractedForThisSink, tx);
                    }

                    budgetRemaining -= sourceConsumed;
                    totalTransferred += inserted;
                    sink.targetBE.setChanged();
                    if (sink.targetBE instanceof WireConnectorBlockEntity wc) {
                        BlockState targetSt = wc.getBlockState();
                        if (targetSt.hasProperty(WireConnectorBlock.FACING)) {
                            BlockPos sinkBEPos = wc.getBlockPos().relative(targetSt.getValue(WireConnectorBlock.FACING).getOpposite());
                            BlockEntity sinkBE = serverLevel.getBlockEntity(sinkBEPos);
                            if (sinkBE != null) {
                                sinkBE.setChanged();
                            }
                        }
                    }
                }
            }

            if (totalTransferred > 0) {
                tx.commit();
                source.setChanged();
                BlockEntity srcBE = serverLevel.getBlockEntity(attachedPos);
                if (srcBE != null) {
                    srcBE.setChanged();
                }
            }
        }

        return totalTransferred;
    }

    /**
     * Ticks unloaded power grid sources once per server tick per level.
     * Allows loaded factories to draw power from remote power plants even when the generator chunk is unloaded.
     */
    public static void tickUnloadedGridSources(ServerLevel level) {
        long currentTick = level.getGameTime();
        Long lastTick = LAST_UNLOADED_TICK.get(level);
        if (lastTick != null && lastTick == currentTick) {
            return;
        }
        LAST_UNLOADED_TICK.put(level, currentTick);

        GridNetworkSavedData savedData = GridNetworkSavedData.get(level);
        Map<BlockPos, GridNetworkSavedData.SavedNode> allNodes = savedData.getNodes();
        if (allNodes.isEmpty()) return;

        boolean isDark = level.isDarkOutside() || level.isRaining() || level.isThundering();

        for (GridNetworkSavedData.SavedNode sourceNode : allNodes.values()) {
            if (sourceNode.nodeType != GridNetworkSavedData.NodeType.CONNECTOR) continue;
            if (sourceNode.mode == ConnectorMode.OUTPUT) continue;
            if (sourceNode.cachedSupplyRate <= 0) continue;

            // If the source node's chunk is currently loaded, its normal serverTick handles distribution
            if (level.hasChunkAt(sourceNode.pos)) continue;

            // If power is pure solar and it is nighttime or stormy, solar generation is 0
            if (sourceNode.solarSource && isDark) continue;

            distributeFromUnloadedSource(level, savedData, sourceNode);
        }
    }

    private static void distributeFromUnloadedSource(ServerLevel level, GridNetworkSavedData savedData, GridNetworkSavedData.SavedNode sourceNode) {
        WireType wireType = sourceNode.wireType;
        int rate = Math.min(wireType.getTransferRate(), sourceNode.cachedSupplyRate);
        if (rate <= 0) return;

        List<SinkCandidate> sinks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<PathNode> queue = new ArrayDeque<>();

        visited.add(sourceNode.pos);
        queue.add(new PathNode(sourceNode.pos, wireType, 0.0));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            BlockPos currentPos = current.pos();

            List<WireConnection> outgoingConns = getConnectionsForNode(level, savedData, currentPos);

            for (WireConnection conn : outgoingConns) {
                BlockPos targetPos = conn.getTargetPos();
                if (visited.contains(targetPos)) continue;

                boolean chunkLoaded = level.hasChunkAt(targetPos);
                GridNetworkSavedData.SavedNode targetSaved = savedData.getNode(targetPos);

                if (chunkLoaded) {
                    BlockEntity be = level.getBlockEntity(targetPos);
                    if (be instanceof BreakerSwitchBlockEntity breaker) {
                        if (!breaker.isCircuitClosed()) continue;
                    }
                } else if (targetSaved != null && targetSaved.nodeType == GridNetworkSavedData.NodeType.BREAKER) {
                    if (!targetSaved.isCircuitClosed()) continue;
                }

                visited.add(targetPos);
                double dist = current.totalDistance() + conn.getDistance();

                if (chunkLoaded) {
                    BlockEntity targetBE = level.getBlockEntity(targetPos);
                    if (targetBE instanceof WireConnectorBlockEntity targetConnector) {
                        if (targetConnector.getMode() != ConnectorMode.INPUT) {
                            BlockState targetState = targetConnector.getBlockState();
                            if (targetState.hasProperty(WireConnectorBlock.FACING)) {
                                Direction targetFacing = targetState.getValue(WireConnectorBlock.FACING);
                                BlockPos targetAttached = targetPos.relative(targetFacing.getOpposite());
                                EnergyHandler sinkEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetAttached, targetFacing);

                                if (sinkEnergy != null) {
                                    int acceptedSim;
                                    try (var tx = Transaction.openRoot()) {
                                        acceptedSim = sinkEnergy.insert(rate, tx);
                                    }

                                    if (acceptedSim > 0) {
                                        double lossFreeDist = wireType.getLossFreeDistance();
                                        double effectiveLossDist = Math.max(0.0, dist - lossFreeDist);
                                        float loss = Math.min(0.95f, (float) (effectiveLossDist * wireType.getLossPerBlock()));
                                        float eff = Math.max(0.05f, 1.0f - loss);
                                        int req = (int) Math.ceil((double) acceptedSim / eff);
                                        sinks.add(new SinkCandidate(targetConnector, sinkEnergy, dist, eff, acceptedSim, req));
                                    }
                                }
                            }
                        }
                        queue.add(new PathNode(targetPos, targetConnector.getSupportedWireType(), dist));
                    } else if (targetBE instanceof PoleStreetLightBlockEntity poleLight) {
                        int needed = PoleStreetLightBlockEntity.BUFFER_CAPACITY - poleLight.energyStorage.getAmountAsInt();
                        if (needed > 0) {
                            int acceptedSim;
                            try (var tx = Transaction.openRoot()) {
                                acceptedSim = poleLight.energyStorage.insert(Math.min(rate, needed), tx);
                            }
                            if (acceptedSim > 0) {
                                double lossFreeDist = wireType.getLossFreeDistance();
                                double effectiveLossDist = Math.max(0.0, dist - lossFreeDist);
                                float loss = Math.min(0.95f, (float) (effectiveLossDist * wireType.getLossPerBlock()));
                                float eff = Math.max(0.05f, 1.0f - loss);
                                int req = (int) Math.ceil((double) acceptedSim / eff);
                                sinks.add(new SinkCandidate(poleLight, poleLight.energyStorage, dist, eff, acceptedSim, req));
                            }
                        }
                        queue.add(new PathNode(targetPos, wireType, dist));
                    } else if (targetBE instanceof WireRelayBlockEntity || targetBE instanceof BreakerSwitchBlockEntity) {
                        queue.add(new PathNode(targetPos, wireType, dist));
                    }
                } else if (targetSaved != null) {
                    queue.add(new PathNode(targetPos, targetSaved.wireType, dist));
                }
            }
        }

        if (sinks.isEmpty()) return;

        int totalRequested = 0;
        for (SinkCandidate sink : sinks) {
            totalRequested += sink.requestedFromSource;
        }

        int toTransmit = Math.min(rate, totalRequested);
        if (toTransmit <= 0) return;

        try (var tx = Transaction.openRoot()) {
            int budgetRemaining = toTransmit;

            for (SinkCandidate sink : sinks) {
                if (budgetRemaining <= 0) break;

                int alloc = (int) Math.round((double) toTransmit * (sink.requestedFromSource / (double) totalRequested));
                alloc = Math.min(alloc, budgetRemaining);
                if (alloc <= 0) continue;

                int netForSink = Math.max(1, Math.round(alloc * sink.efficiency));
                int inserted = sink.sinkEnergy.insert(netForSink, tx);
                if (inserted > 0) {
                    int sourceConsumed = (int) Math.ceil((double) inserted / sink.efficiency);
                    budgetRemaining -= Math.min(sourceConsumed, alloc);
                    sink.targetBE.setChanged();
                    if (sink.targetBE instanceof WireConnectorBlockEntity wc) {
                        BlockState targetSt = wc.getBlockState();
                        if (targetSt.hasProperty(WireConnectorBlock.FACING)) {
                            BlockPos sinkBEPos = wc.getBlockPos().relative(targetSt.getValue(WireConnectorBlock.FACING).getOpposite());
                            BlockEntity sinkBE = level.getBlockEntity(sinkBEPos);
                            if (sinkBE != null) {
                                sinkBE.setChanged();
                            }
                        }
                    }
                }
            }
            tx.commit();
        }
    }

    private static List<WireConnection> getConnectionsForNode(ServerLevel level, GridNetworkSavedData savedData, BlockPos pos) {
        if (level.hasChunkAt(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IWireNode wireNode) {
                return wireNode.getConnections();
            }
        }
        GridNetworkSavedData.SavedNode node = savedData.getNode(pos);
        if (node != null && !node.connections.isEmpty()) {
            List<WireConnection> list = new ArrayList<>(node.connections.size());
            for (GridNetworkSavedData.SavedConnection sc : node.connections) {
                list.add(sc.toWireConnection());
            }
            return list;
        }
        return Collections.emptyList();
    }

    private static int measureSustainableSupply(ServerLevel level, WireConnectorBlockEntity source, BlockPos attachedPos, EnergyHandler sourceNeighbor, int maxRate) {
        BlockState attachedState = level.getBlockState(attachedPos);
        BlockEntity attachedBE = level.getBlockEntity(attachedPos);

        if (attachedBE instanceof WoodenWaterWheelBlockEntity wheel) {
            return Math.min(maxRate, Math.max(wheel.getFeGeneratedLastTick(), wheel.energyStorage.getAmountAsInt()));
        }
        if (attachedBE instanceof WindmillRotorBlockEntity rotor) {
            return Math.min(maxRate, Math.max(rotor.getFeGeneratedLastTick(), rotor.energyStorage.getAmountAsInt()));
        }
        if (attachedBE instanceof SolarPanelBlockEntity solar) {
            return Math.min(maxRate, Math.max(solar.getTier().getGenerationRate(), solar.getEnergyStored()));
        }
        if (attachedBE instanceof TransformerBlockEntity transformer) {
            return Math.min(maxRate, transformer.energyStorage.getAmountAsInt());
        }
        if (attachedBE instanceof PowerCableBlockEntity cable) {
            return Math.min(maxRate, cable.getGauge().getTransferRate());
        }

        if (sourceNeighbor != null) {
            int currentBuffer = source.energyStorage.getAmountAsInt();
            try (var simTx = Transaction.openRoot()) {
                int extracted = sourceNeighbor.extract(maxRate, simTx);
                return Math.min(maxRate, Math.max(currentBuffer, extracted));
            }
        }

        return source.energyStorage.getAmountAsInt();
    }

    private static boolean isSolarAttached(ServerLevel level, BlockPos attachedPos) {
        BlockState state = level.getBlockState(attachedPos);
        return state.getBlock() instanceof SolarPanelBlock;
    }

    @Nullable
    private static BlockPos getAttachedTransformerPos(ServerLevel level, BlockPos attachedPos) {
        BlockState state = level.getBlockState(attachedPos);
        return state.getBlock() instanceof TransformerBlock ? attachedPos : null;
    }
}
