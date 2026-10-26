package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.PowerPlantControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class PowerPlantControllerBlockEntity extends BlockEntity {
    public static final int CAPACITY = 2_000_000;
    public static final int GENERATION_RATE = 4_096; // 4096 FE/t heavy generation
    public static final int MAX_OUTPUT = 16_384;

    private boolean assembled = false;
    private int burnTime = 0;
    private int maxBurnTime = 0;
    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(CAPACITY, 0, MAX_OUTPUT);

    public PowerPlantControllerBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.POWER_PLANT_CONTROLLER_BE.get(), pos, state);
    }

    public boolean isAssembled() {
        return assembled;
    }

    public boolean checkStructure() {
        Level lvl = getLevel();
        if (lvl == null) return false;

        Direction facing = getBlockState().getValue(PowerPlantControllerBlock.FACING);
        BlockPos centerPos = this.worldPosition.relative(facing.getOpposite());

        // Validate 3x3x3 footprint surrounding centerPos
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = centerPos.offset(x, y, z);
                    if (p.equals(this.worldPosition)) continue; // Controller block itself

                    BlockState st = lvl.getBlockState(p);
                    // Base must be Generator Casing
                    if (y == -1 && !st.is(PowerGridMod.GENERATOR_CASING.get())) {
                        return false;
                    }
                    // Center core must be Alternator Coil or Turbine Rotor
                    if (y == 0 && x == 0 && z == 0 && !st.is(PowerGridMod.ALTERNATOR_COIL.get()) && !st.is(PowerGridMod.TURBINE_ROTOR.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean toggleAssembly(Player player) {
        Level lvl = getLevel();
        if (lvl == null || lvl.isClientSide()) return false;

        if (this.assembled) {
            this.assembled = false;
            lvl.setBlock(worldPosition, getBlockState().setValue(PowerPlantControllerBlock.ASSEMBLED, false).setValue(PowerPlantControllerBlock.ACTIVE, false), 3);
            lvl.playSound(null, worldPosition, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);
            player.sendSystemMessage(Component.translatable("message.powergrid.plant_disassembled"));
            setChanged();
            return true;
        } else {
            if (checkStructure()) {
                this.assembled = true;
                lvl.setBlock(worldPosition, getBlockState().setValue(PowerPlantControllerBlock.ASSEMBLED, true), 3);
                lvl.playSound(null, worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.9f, 1.2f);
                player.sendSystemMessage(Component.translatable("message.powergrid.plant_assembled"));
                setChanged();
                return true;
            } else {
                player.sendSystemMessage(Component.translatable("message.powergrid.plant_incomplete"));
                return false;
            }
        }
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!assembled) return;

        // Internal generator simulation
        if (burnTime > 0) {
            burnTime--;
            int room = this.energyStorage.getCapacityAsInt() - this.energyStorage.getAmountAsInt();
            if (room > 0) {
                try (var tx = Transaction.openRoot()) {
                    int inserted = this.energyStorage.insert(Math.min(room, GENERATION_RATE), tx);
                    if (inserted > 0) {
                        tx.commit();
                        setChanged();
                    }
                }
            }

            // Spawn industrial smoke particles from top exhaust
            if (level instanceof ServerLevel serverLevel && level.getRandom().nextInt(5) == 0) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 2.2, pos.getZ() + 0.5,
                        2, 0.1, 0.3, 0.1, 0.02);
            }
        } else {
            // Auto recharge burn cycle
            this.burnTime = 200;
            this.maxBurnTime = 200;
        }

        // Push generated power to adjacent blocks
        int stored = this.energyStorage.getAmountAsInt();
        if (stored > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos targetPos = pos.relative(dir);
                EnergyHandler targetEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
                if (targetEnergy != null) {
                    int toPush = Math.min(this.energyStorage.getAmountAsInt(), MAX_OUTPUT);
                    try (var tx = Transaction.openRoot()) {
                        int extracted = this.energyStorage.extract(toPush, tx);
                        if (extracted > 0) {
                            int accepted = targetEnergy.insert(extracted, tx);
                            if (accepted == extracted) {
                                tx.commit();
                                setChanged();
                                if (this.energyStorage.getAmountAsInt() <= 0) break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("assembled", this.assembled);
        output.putInt("burnTime", this.burnTime);
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.assembled = input.getBooleanOr("assembled", false);
        this.burnTime = input.getIntOr("burnTime", 0);
        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);
    }
}
