package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.PowerCableBlock;
import com.powergrid.compat.refinery.CableFacadeData;
import com.powergrid.compat.refinery.RefineryFacadeCompat;
import com.powergrid.grid.CableGauge;
import com.powergrid.grid.CableNetworkRouter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PowerCableBlockEntity extends BlockEntity {
    private CableGauge gauge;
    private final SimpleEnergyHandler energyStorage;
    private int disabledSidesMask = 0; // 6-bit mask (1 << Direction.get3DDataValue())
    private final Map<Direction, CableFacadeData> facades = new EnumMap<>(Direction.class);

    private long lastTickTime = -1;
    private int transferredThisTick = 0;

    private final ThroughputJournal throughputJournal = new ThroughputJournal();

    private class ThroughputJournal extends SnapshotJournal<Integer> {
        @Override
        protected Integer createSnapshot() {
            return transferredThisTick;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            transferredThisTick = snapshot;
        }
    }

    public PowerCableBlockEntity(BlockPos pos, BlockState state, CableGauge gauge) {
        super(PowerGridMod.POWER_CABLE_BE.get(), pos, state);
        this.gauge = gauge != null ? gauge : getGaugeFromState(state);
        int cap = this.gauge.getBufferCapacity();
        int rate = this.gauge.getTransferRate();
        this.energyStorage = new SimpleEnergyHandler(cap, rate, rate);
    }

    public PowerCableBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, getGaugeFromState(state));
    }

    public static CableGauge getGaugeFromState(BlockState state) {
        if (state != null && state.getBlock() instanceof PowerCableBlock pcb) {
            return pcb.getGauge();
        }
        return CableGauge.GAUGE_18;
    }

    public CableGauge getGauge() {
        if (this.gauge == null) {
            this.gauge = getGaugeFromState(getBlockState());
        }
        return this.gauge;
    }

    public SimpleEnergyHandler getEnergyStorage() {
        return this.energyStorage;
    }

    public boolean isSideDisabled(Direction dir) {
        return (this.disabledSidesMask & (1 << dir.get3DDataValue())) != 0;
    }

    public void setSideDisabled(Direction dir, boolean disabled) {
        if (disabled) {
            this.disabledSidesMask |= (1 << dir.get3DDataValue());
        } else {
            this.disabledSidesMask &= ~(1 << dir.get3DDataValue());
        }
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState current = getBlockState();
            if (current.getBlock() instanceof PowerCableBlock pcb) {
                BlockState updated = pcb.updateConnections(this.level, this.worldPosition, current);
                this.level.setBlock(this.worldPosition, updated, 3);
            }
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void toggleSide(Direction dir, Player player) {
        boolean nowDisabled = !isSideDisabled(dir);
        setSideDisabled(dir, nowDisabled);
        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.8f);
        }
        if (player != null) {
            String status = nowDisabled ? "§cDisconnected" : "§aConnected";
            player.sendSystemMessage(Component.literal("§6[Power Grid]§r Cable face " + dir.getName().toUpperCase() + " set to: " + status));
        }
    }

    public Map<Direction, CableFacadeData> getFacades() {
        return Collections.unmodifiableMap(this.facades);
    }

    @Nullable
    public CableFacadeData getFacade(Direction dir) {
        return dir != null ? this.facades.get(dir) : null;
    }

    public boolean hasFacade(Direction dir) {
        return dir != null && this.facades.containsKey(dir);
    }

    public boolean hasFacades() {
        return !this.facades.isEmpty();
    }

    public void setFacade(Direction dir, @Nullable CableFacadeData data) {
        if (dir == null) return;
        if (data == null || !data.isValid()) {
            this.facades.remove(dir);
        } else {
            this.facades.put(dir, data);
        }
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            this.level.updateNeighborsAt(this.worldPosition, getBlockState().getBlock());
        }
    }

    public void dropAllFacades() {
        if (this.level == null || this.level.isClientSide() || this.facades.isEmpty()) return;
        for (CableFacadeData data : this.facades.values()) {
            if (data != null && data.isValid()) {
                ItemStack drop = RefineryFacadeCompat.createFacadeStack(data.block(), data.hollow(), 1);
                if (!drop.isEmpty()) {
                    Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, drop);
                }
            }
        }
        this.facades.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            dropAllFacades();
        }
    }

    public int getRemainingThroughput() {
        if (this.level != null) {
            long now = this.level.getGameTime();
            if (now != this.lastTickTime) {
                this.lastTickTime = now;
                this.transferredThisTick = 0;
            }
        }
        return Math.max(0, getGauge().getTransferRate() - this.transferredThisTick);
    }

    public void recordTransferred(int amount) {
        recordTransferred(amount, null);
    }

    public void recordTransferred(int amount, @Nullable TransactionContext tx) {
        if (this.level != null) {
            long now = this.level.getGameTime();
            if (now != this.lastTickTime) {
                this.lastTickTime = now;
                this.transferredThisTick = 0;
            }
        }
        if (tx != null) {
            this.throughputJournal.updateSnapshots(tx);
        }
        this.transferredThisTick += amount;
    }

    public int getTransferredThisTick() {
        if (this.level != null && this.level.getGameTime() != this.lastTickTime) {
            return 0;
        }
        return this.transferredThisTick;
    }

    public EnergyHandler getSideEnergyHandler(Direction side) {
        if (side != null && isSideDisabled(side)) {
            return null;
        }
        return new CableSideEnergyHandler(side);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        CableNetworkRouter.distributeFromCable(this);
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("gauge", getGauge().getSerializedName());
        output.putInt("storedEnergy", 0);
        output.putInt("disabledSides", this.disabledSidesMask);
        if (!this.facades.isEmpty()) {
            var facList = output.childrenList("Facades");
            for (Map.Entry<Direction, CableFacadeData> entry : this.facades.entrySet()) {
                CableFacadeData fd = entry.getValue();
                if (fd != null && fd.isValid()) {
                    var facOut = facList.addChild();
                    facOut.putInt("Side", entry.getKey().get3DDataValue());
                    facOut.putString("Block", fd.getBlockId().toString());
                    facOut.putBoolean("Hollow", fd.hollow());
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String gStr = input.getStringOr("gauge", "");
        if (!gStr.isEmpty()) {
            this.gauge = CableGauge.fromString(gStr);
        } else {
            this.gauge = getGaugeFromState(getBlockState());
        }
        this.energyStorage.set(0);
        this.disabledSidesMask = input.getIntOr("disabledSides", 0);

        this.facades.clear();
        for (ValueInput facIn : input.childrenListOrEmpty("Facades")) {
            int sideIdx = facIn.getIntOr("Side", -1);
            String blockIdStr = facIn.getStringOr("Block", "");
            boolean hollow = facIn.getBooleanOr("Hollow", false);
            if (sideIdx >= 0 && sideIdx < 6 && !blockIdStr.isEmpty()) {
                Identifier id = Identifier.tryParse(blockIdStr);
                if (id != null) {
                    Block blk = BuiltInRegistries.BLOCK.getValue(id);
                    if (blk != null && blk != Blocks.AIR) {
                        this.facades.put(Direction.from3DDataValue(sideIdx), new CableFacadeData(blk, hollow));
                    }
                }
            }
        }
    }

    private class CableSideEnergyHandler implements EnergyHandler {
        private final Direction side;

        public CableSideEnergyHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int insert(int maxAmount, TransactionContext tx) {
            if (maxAmount <= 0) return 0;
            if (level == null || level.isClientSide()) return 0;

            int remaining = getRemainingThroughput();
            if (remaining <= 0) return 0;

            int toInsert = Math.min(maxAmount, remaining);

            // Directly route across connected sinks using caller's transaction context
            int routed = CableNetworkRouter.routeEnergy(level, PowerCableBlockEntity.this, side, toInsert, tx);
            if (routed > 0) {
                setChanged();
            }
            return routed;
        }

        @Override
        public int extract(int maxAmount, TransactionContext tx) {
            return 0;
        }

        @Override
        public long getAmountAsLong() {
            return 0;
        }

        @Override
        public int getAmountAsInt() {
            return 0;
        }

        @Override
        public long getCapacityAsLong() {
            return energyStorage.getCapacityAsLong();
        }

        @Override
        public int getCapacityAsInt() {
            return energyStorage.getCapacityAsInt();
        }
    }
}
