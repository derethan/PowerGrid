package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.SolarPanelBlock;
import com.powergrid.block.SolarPanelTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SolarPanelBlockEntity extends BlockEntity {
    private final SolarPanelTier tier;
    private int energy = 0;
    private boolean generating = false;

    public final SimpleEnergyHandler energyStorage;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.SOLAR_PANEL_BE.get(), pos, state);
        if (state.getBlock() instanceof SolarPanelBlock spb) {
            this.tier = spb.getTier();
        } else {
            this.tier = SolarPanelTier.STANDARD;
        }

        int cap = this.tier.getCapacity();
        int maxRate = this.tier.getMaxExportRate();
        this.energyStorage = new SimpleEnergyHandler(cap, maxRate, maxRate) {
            @Override
            public void set(int amount) {
                super.set(amount);
                SolarPanelBlockEntity.this.energy = amount;
                setChanged();
            }

            @Override
            public int insert(int amount, TransactionContext transaction) {
                int inserted = super.insert(amount, transaction);
                SolarPanelBlockEntity.this.energy = this.energy;
                return inserted;
            }

            @Override
            public int extract(int amount, TransactionContext transaction) {
                int extracted = super.extract(amount, transaction);
                SolarPanelBlockEntity.this.energy = this.energy;
                return extracted;
            }
        };
    }

    public SolarPanelTier getTier() {
        return this.tier != null ? this.tier : SolarPanelTier.STANDARD;
    }

    public static boolean isExposedToDaylight(Level level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight()) {
            return false;
        }
        if (!level.canSeeSky(pos) && !level.canSeeSky(pos.above())) {
            return false;
        }
        int skyBrightness = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        return skyBrightness > 0 && !level.isDarkOutside();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity be) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SolarPanelTier tier = be.getTier();
        boolean canGenerate = isExposedToDaylight(serverLevel, pos);
        be.generating = canGenerate;

        int cap = tier.getCapacity();
        int rate = tier.getGenerationRate();

        if (canGenerate && be.energy < cap) {
            be.addEnergy(rate);
        }

        // Periodically sync ACTIVE blockstate property for visual indicator LEDs
        if (serverLevel.getGameTime() % 10 == 0) {
            if (state.hasProperty(SolarPanelBlock.ACTIVE) && state.getValue(SolarPanelBlock.ACTIVE) != canGenerate) {
                serverLevel.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, canGenerate), Block.UPDATE_CLIENTS);
            }
        }

        if (be.energy > 0) {
            be.pushEnergyToReceivers(serverLevel, pos);
            if (be.energy > 0) {
                be.balanceWithConnectedPanels(serverLevel, pos);
            }
        }
    }

    private void pushEnergyToReceivers(ServerLevel level, BlockPos pos) {
        int maxExport = this.getTier().getMaxExportRate();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue; // Top is photovoltaic glass surface
            BlockPos targetPos = pos.relative(dir);
            if (level.getBlockState(targetPos).getBlock() instanceof SolarPanelBlock) {
                continue; // Handled by array balancing
            }
            EnergyHandler targetHandler = level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
            if (targetHandler != null) {
                int toPush = Math.min(this.energy, maxExport);
                try (Transaction tx = Transaction.openRoot()) {
                    int inserted = targetHandler.insert(toPush, tx);
                    if (inserted > 0) {
                        tx.commit();
                        this.energy -= inserted;
                        this.energyStorage.set(this.energy);
                        setChanged();
                        if (this.energy <= 0) break;
                    }
                }
            }
        }
    }

    private void balanceWithConnectedPanels(ServerLevel level, BlockPos pos) {
        int maxExport = this.getTier().getMaxExportRate();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockEntity(neighborPos) instanceof SolarPanelBlockEntity neighborPanel) {
                int diff = this.energy - neighborPanel.energy;
                if (diff > 1) {
                    int transfer = Math.min(diff / 2, maxExport);
                    int space = neighborPanel.getTier().getCapacity() - neighborPanel.energy;
                    int actual = Math.min(transfer, space);
                    if (actual > 0) {
                        this.energy -= actual;
                        this.energyStorage.set(this.energy);
                        neighborPanel.addEnergy(actual);
                        setChanged();
                        if (this.energy <= 0) break;
                    }
                }
            }
        }
    }

    public void addEnergy(int amount) {
        int cap = this.getTier().getCapacity();
        int space = cap - this.energy;
        int toAdd = Math.min(amount, space);
        if (toAdd > 0) {
            this.energy += toAdd;
            this.energyStorage.set(this.energy);
            setChanged();
        }
    }

    public int getEnergyStored() {
        return this.energy;
    }

    public boolean isGenerating() {
        return this.generating;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", this.energy);
        output.putBoolean("generating", this.generating);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy = input.getIntOr("energy", 0);
        this.energyStorage.set(this.energy);
        this.generating = input.getBooleanOr("generating", false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
