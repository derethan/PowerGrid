package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.PoleStreetLightBlock;
import com.powergrid.grid.ConnectorMode;
import com.powergrid.grid.EnergyGridManager;
import com.powergrid.grid.GridNetworkSavedData;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PoleStreetLightBlockEntity extends BlockEntity implements IWireNode {
    public static final int BUFFER_CAPACITY = 1_000;
    public static final int MAX_RECEIVE = 32;
    public static final int POWER_DRAW_PER_TICK = 5;

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(BUFFER_CAPACITY, MAX_RECEIVE, 0);
    private final List<WireConnection> connections = new ArrayList<>();
    private boolean photocellEnabled = true;
    private boolean isPowered = false;

    public PoleStreetLightBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.POLE_STREET_LIGHT_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData savedData = GridNetworkSavedData.get(serverLevel);
            boolean changed = savedData.syncNode(this.worldPosition, GridNetworkSavedData.NodeType.POLE_LIGHT,
                    getSupportedWireType(), this.connections, ConnectorMode.OUTPUT, true, null);
            if (changed) {
                java.util.Set<BlockPos> removals = savedData.getAndClearPendingRemovals(this.worldPosition);
                this.connections.removeIf(c -> removals.contains(c.getTargetPos()));
                setChanged();
            }
        }
    }

    public boolean isPhotocellEnabled() {
        return this.photocellEnabled;
    }

    public void setPhotocellEnabled(boolean photocellEnabled) {
        this.photocellEnabled = photocellEnabled;
        setChanged();
    }

    public void togglePhotocell(Player player) {
        this.photocellEnabled = !this.photocellEnabled;
        setChanged();
        player.sendSystemMessage(Component.literal("§e[Pole Street Light] §7Photocell Mode: " +
                (this.photocellEnabled ? "§aAutomatic (Dusk-to-Dawn)§r" : "§6Always Active (Continuous 24/7)§r")));
    }

    public boolean isPowered() {
        return this.isPowered;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            EnergyGridManager.tickUnloadedGridSources(serverLevel);
        }

        boolean shouldAttemptDraw;

        if (this.photocellEnabled) {
            boolean isDark = level.isDarkOutside() || level.isRaining() || level.isThundering();
            shouldAttemptDraw = isDark;
        } else {
            shouldAttemptDraw = true;
        }

        boolean active = false;
        if (shouldAttemptDraw) {
            if (this.energyStorage.getAmountAsInt() >= POWER_DRAW_PER_TICK) {
                try (var tx = Transaction.openRoot()) {
                    int extracted = this.energyStorage.extract(POWER_DRAW_PER_TICK, tx);
                    if (extracted == POWER_DRAW_PER_TICK) {
                        tx.commit();
                        active = true;
                    }
                }
            }
        }

        if (this.isPowered != active) {
            this.isPowered = active;
            setChanged();
            if (state.getValue(PoleStreetLightBlock.POWERED) != active) {
                level.setBlock(pos, state.setValue(PoleStreetLightBlock.POWERED, active), Block.UPDATE_ALL);
            }
        }
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // === IWireNode Implementation ===

    @Override
    public WireType getSupportedWireType() {
        return WireType.LV;
    }

    @Override
    public boolean isWireTypeSupported(WireType type) {
        return true; // Supports LV, MV, and HV overhead wires from any pole line
    }

    @Override
    public int getMaxConnections() {
        return 2; // Inbound power lead + optional outbound daisy-chain
    }

    @Override
    public boolean canConnectWire(WireType type, BlockPos otherPos) {
        if (!isWireTypeSupported(type)) return false;
        if (!canTakeMoreConnections()) return false;
        for (WireConnection conn : this.connections) {
            if (conn.getTargetPos().equals(otherPos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addConnection(BlockPos targetPos, WireType type, double distance) {
        this.connections.removeIf(conn -> conn.getTargetPos().equals(targetPos));
        this.connections.add(new WireConnection(targetPos, type, distance));
        setChanged();
        syncToClient();
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData.get(serverLevel).addWireConnection(this.worldPosition, targetPos, type, distance);
        }
    }

    @Override
    public void removeConnection(BlockPos targetPos) {
        this.connections.removeIf(c -> c.getTargetPos().equals(targetPos));
        setChanged();
        syncToClient();
        if (this.level instanceof ServerLevel serverLevel) {
            boolean targetLoaded = serverLevel.hasChunkAt(targetPos);
            GridNetworkSavedData.get(serverLevel).removeWireConnection(this.worldPosition, targetPos, true, targetLoaded);
        }
    }

    @Override
    public void clearAllConnections() {
        if (this.level == null || this.level.isClientSide()) return;

        BlockPos pos = getBlockPos();
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData.get(serverLevel).removeNode(pos);
        }
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData savedData = GridNetworkSavedData.get(serverLevel);
            for (WireConnection conn : this.connections) {
                boolean targetLoaded = serverLevel.hasChunkAt(conn.getTargetPos());
                savedData.removeWireConnection(pos, conn.getTargetPos(), true, targetLoaded);
            }
        }

        List<WireConnection> toSever = new ArrayList<>(this.connections);
        this.connections.clear();
        setChanged();

        for (WireConnection conn : toSever) {
            BlockPos otherPos = conn.getTargetPos();
            if (this.level.isLoaded(otherPos)) {
                BlockEntity otherBE = this.level.getBlockEntity(otherPos);
                if (otherBE instanceof IWireNode otherNode) {
                    otherNode.removeConnection(pos);
                    this.level.sendBlockUpdated(otherPos, this.level.getBlockState(otherPos), this.level.getBlockState(otherPos), 3);
                }
            }

            if (this.level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos, new ItemStack(conn.getWireType().getCoilItem()));
            }
        }
        syncToClient();
    }

    @Override
    public List<WireConnection> getConnections() {
        return this.connections;
    }

    @Override
    public BlockPos getNodePos() {
        return this.worldPosition;
    }

    // === NBT Serialization ===

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        output.putBoolean("photocell", this.photocellEnabled);
        output.putBoolean("isPowered", this.isPowered);

        output.putInt("connCount", this.connections.size());
        for (int i = 0; i < this.connections.size(); i++) {
            WireConnection c = this.connections.get(i);
            output.putInt("c_x_" + i, c.getTargetPos().getX());
            output.putInt("c_y_" + i, c.getTargetPos().getY());
            output.putInt("c_z_" + i, c.getTargetPos().getZ());
            output.putString("c_type_" + i, c.getWireType().getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);
        this.photocellEnabled = input.getBooleanOr("photocell", true);
        this.isPowered = input.getBooleanOr("isPowered", false);

        this.connections.clear();
        int count = input.getIntOr("connCount", 0);
        for (int i = 0; i < count; i++) {
            int x = input.getIntOr("c_x_" + i, 0);
            int y = input.getIntOr("c_y_" + i, 0);
            int z = input.getIntOr("c_z_" + i, 0);
            String typeStr = input.getStringOr("c_type_" + i, "lv");
            WireType type = typeStr.equals("hv") ? WireType.HV : typeStr.equals("mv") ? WireType.MV : WireType.LV;
            BlockPos target = new BlockPos(x, y, z);
            this.connections.add(new WireConnection(target, type, Math.sqrt(this.worldPosition.distSqr(target))));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        tag.putBoolean("photocell", this.photocellEnabled);
        tag.putBoolean("isPowered", this.isPowered);
        tag.putInt("connCount", this.connections.size());
        for (int i = 0; i < this.connections.size(); i++) {
            WireConnection c = this.connections.get(i);
            tag.putInt("c_x_" + i, c.getTargetPos().getX());
            tag.putInt("c_y_" + i, c.getTargetPos().getY());
            tag.putInt("c_z_" + i, c.getTargetPos().getZ());
            tag.putString("c_type_" + i, c.getWireType().getSerializedName());
        }
        return tag;
    }
}
