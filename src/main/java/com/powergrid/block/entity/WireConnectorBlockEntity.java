package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.WireConnectorBlock;
import com.powergrid.grid.ConnectorMode;
import com.powergrid.grid.EnergyGridManager;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class WireConnectorBlockEntity extends BlockEntity implements IWireNode {
    private WireType wireType;
    private final List<WireConnection> connections = new ArrayList<>();
    public final SimpleEnergyHandler energyStorage;
    private ConnectorMode mode = ConnectorMode.AUTO;

    public WireConnectorBlockEntity(BlockPos pos, BlockState state, WireType wireType) {
        super(PowerGridMod.WIRE_CONNECTOR_BE.get(), pos, state);
        this.wireType = wireType != null ? wireType : getWireTypeFromState(state);
        int rate = this.wireType.getTransferRate();
        this.energyStorage = new SimpleEnergyHandler(rate * 4, rate, rate);
    }

    public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, getWireTypeFromState(state));
    }

    public static WireType getWireTypeFromState(BlockState state) {
        if (state != null && state.getBlock() instanceof WireConnectorBlock wcb) {
            return wcb.getWireType();
        }
        return WireType.LV;
    }

    public ConnectorMode getMode() {
        return this.mode;
    }

    public void setMode(ConnectorMode mode) {
        this.mode = mode;
        setChanged();
        syncToClient();
    }

    public void cycleMode(Player player) {
        this.mode = this.mode.next();
        setChanged();
        syncToClient();
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.8f);
        }
        player.sendSystemMessage(Component.translatable("message.powergrid.connector_mode", this.mode.getFormattedName()));
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public WireType getSupportedWireType() {
        if (this.wireType == null) {
            this.wireType = getWireTypeFromState(getBlockState());
        }
        return this.wireType;
    }

    @Override
    public int getMaxConnections() {
        return 4;
    }

    @Override
    public boolean canConnectWire(WireType type, BlockPos otherPos) {
        if (type != this.wireType) return false;
        if (otherPos != null && !otherPos.equals(this.worldPosition)) {
            double distSq = this.worldPosition.distSqr(otherPos);
            if (distSq > (double) (type.getMaxDistance() * type.getMaxDistance())) return false;
            for (WireConnection conn : connections) {
                if (conn.getTargetPos().equals(otherPos)) return false;
            }
        }
        return connections.size() < getMaxConnections();
    }

    @Override
    public void addConnection(BlockPos targetPos, WireType type, double distance) {
        this.connections.add(new WireConnection(targetPos, type, distance));
        setChanged();
        syncToClient();
    }

    @Override
    public void removeConnection(BlockPos targetPos) {
        this.connections.removeIf(conn -> conn.getTargetPos().equals(targetPos));
        setChanged();
        syncToClient();
    }

    @Override
    public void clearAllConnections() {
        this.connections.clear();
        setChanged();
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

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        // Distribute energy across wire network using the EnergyGridManager BFS router
        EnergyGridManager.distributePower(this);

        // If connector has leftover energy in buffer and mode allows outputting, deliver to attached block
        if (this.mode != ConnectorMode.INPUT && this.energyStorage.getAmountAsInt() > 0) {
            Direction facing = state.getValue(WireConnectorBlock.FACING);
            Direction attachedFace = facing.getOpposite();
            BlockPos attachedPos = pos.relative(attachedFace);

            EnergyHandler neighborEnergy = level.getCapability(Capabilities.Energy.BLOCK, attachedPos, facing);
            if (neighborEnergy != null) {
                int stored = this.energyStorage.getAmountAsInt();
                int toPush = Math.min(stored, this.wireType.getTransferRate());
                int acceptedSim;
                try (var simTx = Transaction.openRoot()) {
                    acceptedSim = neighborEnergy.insert(toPush, simTx);
                }
                if (acceptedSim > 0) {
                    try (var tx = Transaction.openRoot()) {
                        int extracted = this.energyStorage.extract(acceptedSim, tx);
                        if (extracted > 0) {
                            int accepted = neighborEnergy.insert(extracted, tx);
                            if (accepted == extracted) {
                                tx.commit();
                                setChanged();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null) return;

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

            // Drop wire coils in survival
            if (this.level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos, new ItemStack(conn.getWireType().getCoilItem()));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("wireType", getSupportedWireType().getSerializedName());
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        output.putString("connectorMode", this.mode.getSerializedName());
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
        String wt = input.getStringOr("wireType", "");
        if (!wt.isEmpty()) {
            this.wireType = WireType.fromString(wt);
        } else {
            this.wireType = getWireTypeFromState(getBlockState());
        }

        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);

        String modeStr = input.getStringOr("connectorMode", "auto");
        this.mode = modeStr.equals("input") ? ConnectorMode.INPUT : modeStr.equals("output") ? ConnectorMode.OUTPUT : ConnectorMode.AUTO;

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
}
