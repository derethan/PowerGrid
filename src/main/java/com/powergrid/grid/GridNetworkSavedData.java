package com.powergrid.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class GridNetworkSavedData extends SavedData {

    public enum NodeType {
        CONNECTOR,
        RELAY,
        BREAKER,
        POLE_LIGHT;

        public static final Codec<NodeType> CODEC = Codec.STRING.xmap(
                s -> valueOf(s.toUpperCase(Locale.ROOT)),
                NodeType::name
        );
    }

    public static class SavedConnection {
        public static final Codec<SavedConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("targetPos").forGetter(c -> c.targetPos),
                Codec.STRING.fieldOf("wireType").forGetter(c -> c.wireType.getSerializedName()),
                Codec.DOUBLE.fieldOf("distance").forGetter(c -> c.distance)
        ).apply(instance, (targetPos, wireTypeStr, distance) -> new SavedConnection(targetPos, WireType.fromString(wireTypeStr), distance)));

        public final BlockPos targetPos;
        public final WireType wireType;
        public final double distance;

        public SavedConnection(BlockPos targetPos, WireType wireType, double distance) {
            this.targetPos = targetPos;
            this.wireType = wireType;
            this.distance = distance;
        }

        public WireConnection toWireConnection() {
            return new WireConnection(targetPos, wireType, distance);
        }
    }

    public static class SavedNode {
        public static final Codec<SavedNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(n -> n.pos),
                NodeType.CODEC.fieldOf("nodeType").forGetter(n -> n.nodeType),
                Codec.STRING.fieldOf("wireType").forGetter(n -> n.wireType.getSerializedName()),
                Codec.STRING.optionalFieldOf("mode", "auto").forGetter(n -> n.mode.getSerializedName()),
                Codec.BOOL.optionalFieldOf("circuitClosed", true).forGetter(n -> n.circuitClosed),
                BlockPos.CODEC.optionalFieldOf("attachedTransformerPos").forGetter(n -> Optional.ofNullable(n.attachedTransformerPos)),
                Codec.INT.optionalFieldOf("cachedSupplyRate", 0).forGetter(n -> n.cachedSupplyRate),
                Codec.BOOL.optionalFieldOf("solarSource", false).forGetter(n -> n.solarSource),
                SavedConnection.CODEC.listOf().optionalFieldOf("connections", List.of()).forGetter(n -> n.connections)
        ).apply(instance, (pos, nodeType, wireTypeStr, modeStr, circuitClosed, optTrans, supplyRate, solar, conns) -> {
            SavedNode node = new SavedNode(pos, nodeType, WireType.fromString(wireTypeStr));
            node.mode = modeStr.equalsIgnoreCase("input") ? ConnectorMode.INPUT :
                        modeStr.equalsIgnoreCase("output") ? ConnectorMode.OUTPUT : ConnectorMode.AUTO;
            node.circuitClosed = circuitClosed;
            node.attachedTransformerPos = optTrans.orElse(null);
            node.cachedSupplyRate = supplyRate;
            node.solarSource = solar;
            node.connections = new ArrayList<>(conns);
            return node;
        }));

        public final BlockPos pos;
        public final NodeType nodeType;
        public final WireType wireType;
        public ConnectorMode mode = ConnectorMode.AUTO;
        public boolean circuitClosed = true;
        @Nullable
        public BlockPos attachedTransformerPos = null;
        public int cachedSupplyRate = 0;
        public boolean solarSource = false;
        public List<SavedConnection> connections = new ArrayList<>();

        public SavedNode(BlockPos pos, NodeType nodeType, WireType wireType) {
            this.pos = pos;
            this.nodeType = nodeType;
            this.wireType = wireType;
        }

        public boolean isCircuitClosed() {
            return this.circuitClosed;
        }

        public void addConnection(BlockPos targetPos, WireType type, double distance) {
            connections.removeIf(c -> c.targetPos.equals(targetPos));
            connections.add(new SavedConnection(targetPos, type, distance));
        }

        public void removeConnection(BlockPos targetPos) {
            connections.removeIf(c -> c.targetPos.equals(targetPos));
        }
    }

    private final Map<BlockPos, SavedNode> nodes = new HashMap<>();
    // Tracks pending connection removals for nodes that were unloaded when a severed connection was removed from the other end
    private final Map<BlockPos, Set<BlockPos>> pendingRemovals = new HashMap<>();

    public static final Codec<GridNetworkSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SavedNode.CODEC.listOf().fieldOf("nodes").forGetter(GridNetworkSavedData::getNodeList)
    ).apply(instance, GridNetworkSavedData::fromNodeList));

    public static final SavedDataType<GridNetworkSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("powergrid", "grid_network"),
            GridNetworkSavedData::new,
            CODEC,
            null
    );

    public GridNetworkSavedData() {
    }

    private List<SavedNode> getNodeList() {
        return new ArrayList<>(this.nodes.values());
    }

    private static GridNetworkSavedData fromNodeList(List<SavedNode> list) {
        GridNetworkSavedData data = new GridNetworkSavedData();
        for (SavedNode node : list) {
            data.nodes.put(node.pos, node);
        }
        return data;
    }

    public static GridNetworkSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<BlockPos, SavedNode> getNodes() {
        return Collections.unmodifiableMap(this.nodes);
    }

    @Nullable
    public SavedNode getNode(BlockPos pos) {
        return this.nodes.get(pos);
    }

    /**
     * Registers or syncs a loaded node with SavedData, and applies any pending severed connections.
     * Returns true if any pending connection removal was applied (so the caller should update its BlockEntity).
     */
    public boolean syncNode(BlockPos pos, NodeType type, WireType wireType, List<WireConnection> liveConnections,
                            ConnectorMode mode, boolean circuitClosed, @Nullable BlockPos attachedTransformerPos) {
        SavedNode node = this.nodes.computeIfAbsent(pos, p -> new SavedNode(p, type, wireType));
        node.mode = mode;
        node.circuitClosed = circuitClosed;
        node.attachedTransformerPos = attachedTransformerPos;

        boolean changedLive = false;
        Set<BlockPos> removals = this.pendingRemovals.remove(pos);
        if (removals != null && !removals.isEmpty()) {
            for (BlockPos targetPos : removals) {
                node.removeConnection(targetPos);
            }
            changedLive = true;
        }

        // Reconcile saved connections with live connections (excluding removed ones)
        node.connections.clear();
        for (WireConnection conn : liveConnections) {
            if (removals == null || !removals.contains(conn.getTargetPos())) {
                node.addConnection(conn.getTargetPos(), conn.getWireType(), conn.getDistance());
            }
        }

        setDirty();
        return changedLive;
    }

    public Set<BlockPos> getAndClearPendingRemovals(BlockPos pos) {
        Set<BlockPos> rem = this.pendingRemovals.remove(pos);
        if (rem != null) {
            setDirty();
            return rem;
        }
        return Collections.emptySet();
    }

    public void addWireConnection(BlockPos posA, BlockPos posB, WireType type, double distance) {
        SavedNode nodeA = this.nodes.get(posA);
        if (nodeA != null) {
            nodeA.addConnection(posB, type, distance);
        }
        SavedNode nodeB = this.nodes.get(posB);
        if (nodeB != null) {
            nodeB.addConnection(posA, type, distance);
        }
        setDirty();
    }

    public void removeWireConnection(BlockPos posA, BlockPos posB, boolean isALoaded, boolean isBLoaded) {
        SavedNode nodeA = this.nodes.get(posA);
        if (nodeA != null) {
            nodeA.removeConnection(posB);
        }
        SavedNode nodeB = this.nodes.get(posB);
        if (nodeB != null) {
            nodeB.removeConnection(posA);
        }

        if (!isALoaded) {
            this.pendingRemovals.computeIfAbsent(posA, p -> new HashSet<>()).add(posB);
        }
        if (!isBLoaded) {
            this.pendingRemovals.computeIfAbsent(posB, p -> new HashSet<>()).add(posA);
        }

        setDirty();
    }

    public void removeNode(BlockPos pos) {
        SavedNode removed = this.nodes.remove(pos);
        if (removed != null) {
            for (SavedConnection conn : removed.connections) {
                SavedNode target = this.nodes.get(conn.targetPos);
                if (target != null) {
                    target.removeConnection(pos);
                }
                this.pendingRemovals.computeIfAbsent(conn.targetPos, p -> new HashSet<>()).add(pos);
            }
            setDirty();
        }
    }

    public void setBreakerClosed(BlockPos pos, boolean closed) {
        SavedNode node = this.nodes.get(pos);
        if (node != null) {
            node.circuitClosed = closed;
            setDirty();
        }
    }

    public void setConnectorMode(BlockPos pos, ConnectorMode mode) {
        SavedNode node = this.nodes.get(pos);
        if (node != null) {
            node.mode = mode;
            setDirty();
        }
    }

    public void updateConnectorSupply(BlockPos pos, int supplyRate, boolean solarSource, @Nullable BlockPos transformerPos) {
        SavedNode node = this.nodes.get(pos);
        if (node != null) {
            if (node.cachedSupplyRate != supplyRate || node.solarSource != solarSource || !Objects.equals(node.attachedTransformerPos, transformerPos)) {
                node.cachedSupplyRate = supplyRate;
                node.solarSource = solarSource;
                node.attachedTransformerPos = transformerPos;
                setDirty();
            }
        }
    }
}
