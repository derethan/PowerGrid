package com.powergrid.grid;

import com.powergrid.block.WireConnectorBlock;
import com.powergrid.block.entity.BreakerSwitchBlockEntity;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import com.powergrid.block.entity.WireRelayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;

public class EnergyGridManager {

    private record PathNode(IWireNode node, double totalDistance) {}

    private static class SinkCandidate {
        final WireConnectorBlockEntity connector;
        final EnergyHandler sinkEnergy;
        final double distance;
        final float efficiency;
        final int acceptedSim;
        final int requestedFromSource;

        SinkCandidate(WireConnectorBlockEntity connector, EnergyHandler sinkEnergy, double distance, float efficiency, int acceptedSim, int requestedFromSource) {
            this.connector = connector;
            this.sinkEnergy = sinkEnergy;
            this.distance = distance;
            this.efficiency = efficiency;
            this.acceptedSim = acceptedSim;
            this.requestedFromSource = requestedFromSource;
        }
    }

    /**
     * Executes energy distribution from a source WireConnectorBlockEntity across the wire network.
     * Traverses through relays, closed switches, and connectors to find hungry sinks.
     */
    public static int distributePower(WireConnectorBlockEntity source) {
        Level level = source.getLevel();
        if (level == null || level.isClientSide()) return 0;

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

        // 1. Traverse network via BFS to locate all valid sinks
        List<SinkCandidate> sinks = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<PathNode> queue = new ArrayDeque<>();

        visited.add(sourcePos);
        queue.add(new PathNode(source, 0.0));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();

            for (WireConnection conn : current.node().getConnections()) {
                BlockPos targetPos = conn.getTargetPos();
                if (visited.contains(targetPos)) continue;
                if (!level.hasChunkAt(targetPos)) continue;

                BlockEntity targetBE = level.getBlockEntity(targetPos);
                if (!(targetBE instanceof IWireNode targetNode)) {
                    continue;
                }

                // If breaker switch, check if closed
                if (targetNode instanceof BreakerSwitchBlockEntity breaker) {
                    if (!breaker.isCircuitClosed()) {
                        continue; // Open switch cuts circuit
                    }
                }

                visited.add(targetPos);
                double dist = current.totalDistance() + conn.getDistance();

                // If target is a WireConnector, evaluate as a candidate sink
                if (targetNode instanceof WireConnectorBlockEntity targetConnector) {
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
                                    float loss = Math.min(0.95f, (float) dist * wireType.getLossPerBlock());
                                    float eff = Math.max(0.05f, 1.0f - loss);
                                    int req = (int) Math.ceil((double) acceptedSim / eff);
                                    sinks.add(new SinkCandidate(targetConnector, sinkEnergy, dist, eff, acceptedSim, req));
                                }
                            }
                        }
                    }
                    // Can still branch forward through this connector
                    queue.add(new PathNode(targetNode, dist));
                } else if (targetNode instanceof WireRelayBlockEntity || targetNode instanceof BreakerSwitchBlockEntity) {
                    queue.add(new PathNode(targetNode, dist));
                }
            }
        }

        if (sinks.isEmpty()) return 0;

        // 2. Determine how much energy is needed and available
        int totalRequested = 0;
        for (SinkCandidate sink : sinks) {
            totalRequested += sink.requestedFromSource;
        }

        int maxToPull = Math.min(rate, totalRequested);
        int available = source.energyStorage.getAmountAsInt();

        // If connector buffer is not full, check neighbor energy
        if (available < maxToPull && sourceNeighbor != null) {
            try (var tx = Transaction.openRoot()) {
                int extracted = sourceNeighbor.extract(maxToPull - available, tx);
                available += extracted;
            }
        }

        if (available <= 0) return 0;

        int toTransmit = Math.min(maxToPull, available);

        // 3. Extract and deliver in a unified transaction
        int totalTransferred = 0;
        try (var tx = Transaction.openRoot()) {
            int extracted = 0;
            if (source.energyStorage.getAmountAsInt() > 0) {
                extracted += source.energyStorage.extract(toTransmit, tx);
            }
            if (extracted < toTransmit && sourceNeighbor != null) {
                extracted += sourceNeighbor.extract(toTransmit - extracted, tx);
            }

            if (extracted > 0) {
                int remaining = extracted;
                for (SinkCandidate sink : sinks) {
                    if (remaining <= 0) break;

                    int alloc = (int) Math.round((double) extracted * (sink.requestedFromSource / (double) totalRequested));
                    alloc = Math.min(alloc, remaining);
                    if (alloc <= 0) continue;

                    int netForSink = Math.max(1, Math.round(alloc * sink.efficiency));
                    int inserted = sink.sinkEnergy.insert(netForSink, tx);
                    if (inserted > 0) {
                        remaining -= alloc;
                        totalTransferred += inserted;
                        sink.connector.setChanged();
                    }
                }

                tx.commit();
                source.setChanged();
            }
        }

        return totalTransferred;
    }
}
