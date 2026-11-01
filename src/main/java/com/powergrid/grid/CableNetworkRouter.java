package com.powergrid.grid;

import com.powergrid.block.entity.HeavyAccumulatorBlockEntity;
import com.powergrid.block.entity.PowerCableBlockEntity;
import com.powergrid.block.entity.TransformerBlockEntity;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CableNetworkRouter {

    public record DeviceTarget(BlockPos cablePos, Direction facing, BlockPos devicePos, EnergyHandler handler, BlockEntity blockEntity) {}

    public static class FoundDevice {
        public final DeviceTarget target;
        public final int bottleneck;

        public FoundDevice(DeviceTarget target, int bottleneck) {
            this.target = target;
            this.bottleneck = bottleneck;
        }
    }

    /**
     * Finds reachable energy sinks connected to the cable network starting from originCable.
     * Respects disabled sides, cable per-tick capacities, and prevents feedback loops.
     */
    public static List<FoundDevice> findSinks(Level level, PowerCableBlockEntity originCable, @Nullable Direction originSide) {
        if (level == null || level.isClientSide()) return Collections.emptyList();

        List<FoundDevice> sinks = new ArrayList<>();
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> visitedSinks = new HashSet<>();
        Queue<CablePathNode> queue = new ArrayDeque<>();

        BlockPos startPos = originCable.getBlockPos();
        visitedCables.add(startPos);

        // Never loop back into the source device that injected energy into originCable
        if (originSide != null) {
            visitedSinks.add(startPos.relative(originSide));
        }

        int startRemaining = originCable.getRemainingThroughput();
        queue.add(new CablePathNode(startPos, originCable, startRemaining));

        while (!queue.isEmpty()) {
            CablePathNode current = queue.poll();
            if (current.bottleneck <= 0) continue;

            for (Direction dir : Direction.values()) {
                if (current.be.isSideDisabled(dir)) continue;

                BlockPos neighborPos = current.pos.relative(dir);
                Direction opposite = dir.getOpposite();

                // If neighbor is an already visited cable, skip
                if (visitedCables.contains(neighborPos)) continue;

                if (!level.isLoaded(neighborPos)) continue;

                BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                if (neighborBE instanceof PowerCableBlockEntity neighborCable) {
                    if (!neighborCable.isSideDisabled(opposite)) {
                        visitedCables.add(neighborPos);
                        int pathBottleneck = Math.min(current.bottleneck, neighborCable.getRemainingThroughput());
                        queue.add(new CablePathNode(neighborPos, neighborCable, pathBottleneck));
                    }
                } else {
                    // Check if neighborBE is valid sink
                    if (neighborBE != null && !isExcludedSink(neighborBE, opposite)) {
                        if (!visitedSinks.add(neighborPos)) {
                            continue;
                        }
                        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, neighborPos, opposite);
                        if (handler != null) {
                            sinks.add(new FoundDevice(new DeviceTarget(current.pos, dir, neighborPos, handler, neighborBE), current.bottleneck));
                        }
                    }
                }
            }
        }

        return sinks;
    }

    private static boolean isExcludedSink(BlockEntity be, Direction face) {
        if (be instanceof WireConnectorBlockEntity connector) {
            // Only output to connector if connector is in INPUT mode (drawing from grid/cable to push to wire)
            return connector.getMode() == ConnectorMode.OUTPUT;
        }
        return false;
    }

    /**
     * Attempt to route energy inserted at originCable across the network towards valid sinks.
     * Uses parentTx (or opens a root transaction if null) to guarantee transaction atomicity and safety.
     */
    public static int routeEnergy(Level level, PowerCableBlockEntity originCable, @Nullable Direction fromSide, int amount, @Nullable TransactionContext parentTx) {
        if (amount <= 0 || level == null || level.isClientSide()) return 0;

        int allowedAtOrigin = Math.min(amount, originCable.getRemainingThroughput());
        if (allowedAtOrigin <= 0) return 0;

        List<FoundDevice> sinks = findSinks(level, originCable, fromSide);
        if (sinks.isEmpty()) return 0;

        int totalTransferred = 0;
        int remainingToPush = allowedAtOrigin;

        for (FoundDevice sink : sinks) {
            if (remainingToPush <= 0) break;

            int maxForSink = Math.min(remainingToPush, sink.bottleneck);
            if (maxForSink <= 0) continue;

            int accepted;
            try (Transaction subTx = Transaction.open(parentTx)) {
                accepted = sink.target.handler().insert(maxForSink, subTx);
                if (accepted > 0) {
                    subTx.commit();
                    recordThroughputAlongPath(level, originCable.getBlockPos(), sink.target.cablePos(), accepted, parentTx);
                    sink.target.blockEntity().setChanged();
                    remainingToPush -= accepted;
                    totalTransferred += accepted;
                }
            }
        }

        return totalTransferred;
    }

    /**
     * BFS backwards or shortest-path to record throughput across cables between start and end.
     */
    public static void recordThroughputAlongPath(Level level, BlockPos start, BlockPos end, int amount, @Nullable TransactionContext tx) {
        if (start.equals(end)) {
            BlockEntity be = level.getBlockEntity(start);
            if (be instanceof PowerCableBlockEntity cable) {
                cable.recordTransferred(amount, tx);
            }
            return;
        }

        // BFS path finding
        Map<BlockPos, BlockPos> parentMap = new HashMap<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        boolean found = false;

        while (!queue.isEmpty()) {
            BlockPos curr = queue.poll();
            if (curr.equals(end)) {
                found = true;
                break;
            }

            BlockEntity be = level.getBlockEntity(curr);
            if (!(be instanceof PowerCableBlockEntity cable)) continue;

            for (Direction dir : Direction.values()) {
                if (cable.isSideDisabled(dir)) continue;
                BlockPos next = curr.relative(dir);
                if (visited.contains(next) || !level.isLoaded(next)) continue;

                BlockEntity nextBe = level.getBlockEntity(next);
                if (nextBe instanceof PowerCableBlockEntity nextCable) {
                    if (!nextCable.isSideDisabled(dir.getOpposite())) {
                        visited.add(next);
                        parentMap.put(next, curr);
                        queue.add(next);
                    }
                }
            }
        }

        if (found) {
            BlockPos step = end;
            while (step != null) {
                BlockEntity be = level.getBlockEntity(step);
                if (be instanceof PowerCableBlockEntity cable) {
                    cable.recordTransferred(amount, tx);
                }
                step = parentMap.get(step);
            }
        }
    }

    /**
     * Routine called during cable serverTick to push internal buffered energy or pull from adjacent extractable sources.
     */
    public static void distributeFromCable(PowerCableBlockEntity cable) {
        Level level = cable.getLevel();
        if (level == null || level.isClientSide()) return;

        // 1. If cable has buffered energy, try pushing it into connected sinks
        int stored = cable.getEnergyStorage().getAmountAsInt();
        if (stored > 0) {
            try (Transaction tx = Transaction.openRoot()) {
                int routed = routeEnergy(level, cable, null, stored, tx);
                if (routed > 0) {
                    cable.getEnergyStorage().extract(routed, tx);
                    tx.commit();
                    cable.setChanged();
                }
            }
        }

        // 2. If cable still has remaining throughput capacity, check adjacent passive blocks to pull energy
        int remaining = cable.getRemainingThroughput();
        if (remaining <= 0) return;

        BlockPos pos = cable.getBlockPos();
        for (Direction dir : Direction.values()) {
            if (cable.isSideDisabled(dir)) continue;

            BlockPos neighborPos = pos.relative(dir);
            if (!level.isLoaded(neighborPos)) continue;

            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            // Don't pull from other cables here (they handle themselves)
            if (neighborBE == null || neighborBE instanceof PowerCableBlockEntity) continue;

            // Don't pull from Wire Connectors in OUTPUT mode
            if (neighborBE instanceof WireConnectorBlockEntity connector && connector.getMode() == ConnectorMode.OUTPUT) {
                continue;
            }

            // Don't pull from self-ticking energy pushers like Heavy Accumulators or Transformers
            if (neighborBE instanceof HeavyAccumulatorBlockEntity || neighborBE instanceof TransformerBlockEntity) {
                continue;
            }

            Direction opp = dir.getOpposite();
            EnergyHandler neighborHandler = level.getCapability(Capabilities.Energy.BLOCK, neighborPos, opp);
            if (neighborHandler != null) {
                List<FoundDevice> sinks = findSinks(level, cable, dir);
                if (sinks.isEmpty()) continue;

                int maxPull = Math.min(remaining, cable.getGauge().getTransferRate());
                try (Transaction pullTx = Transaction.openRoot()) {
                    int extracted = neighborHandler.extract(maxPull, pullTx);
                    if (extracted > 0) {
                        int routed = routeEnergy(level, cable, dir, extracted, pullTx);
                        if (routed > 0) {
                            if (routed < extracted) {
                                cable.getEnergyStorage().insert(extracted - routed, pullTx);
                            }
                            pullTx.commit();
                            neighborBE.setChanged();
                            cable.setChanged();
                            remaining -= extracted;
                            if (remaining <= 0) break;
                        }
                    }
                }
            }
        }
    }

    private record CablePathNode(BlockPos pos, PowerCableBlockEntity be, int bottleneck) {}
}
