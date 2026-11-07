package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.StreetLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class StreetLightBlockEntity extends BlockEntity {
    public static final int BUFFER_CAPACITY = 1_000;
    public static final int MAX_RECEIVE = 32;
    public static final int POWER_DRAW_PER_TICK = 5; // 5 FE/t per user specification

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(BUFFER_CAPACITY, MAX_RECEIVE, 0);
    private boolean photocellEnabled = true;
    private boolean isPowered = false;

    public StreetLightBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.STREET_LIGHT_BE.get(), pos, state);
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
        player.sendSystemMessage(Component.literal("§e[Street Light] §7Photocell Mode: " +
                (this.photocellEnabled ? "§aAutomatic (Dusk-to-Dawn)§r" : "§6Always Active (Continuous 24/7)§r")));
    }

    public boolean isPowered() {
        return this.isPowered;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean shouldAttemptDraw;

        if (this.photocellEnabled) {
            // Dusk-to-Dawn automatic sensor: active during dark hours or severe weather
            boolean isDark = level.isDarkOutside() || level.isRaining() || level.isThundering();
            shouldAttemptDraw = isDark;
        } else {
            // Always active mode
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

            // Update HEAD blockstate at pos.above(3) to change light emission (15 vs 7)
            BlockPos headPos = pos.above(3);
            BlockState headState = level.getBlockState(headPos);
            if (headState.is(PowerGridMod.STREET_LIGHT.get()) && headState.getValue(StreetLightBlock.PART) == StreetLightBlock.StreetLightPart.HEAD) {
                if (headState.getValue(StreetLightBlock.POWERED) != active) {
                    level.setBlock(headPos, headState.setValue(StreetLightBlock.POWERED, active), Block.UPDATE_ALL);
                }
            }

            // Also keep BASE blockstate synchronized for consistency
            if (state.getValue(StreetLightBlock.POWERED) != active) {
                level.setBlock(pos, state.setValue(StreetLightBlock.POWERED, active), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        output.putBoolean("photocell", this.photocellEnabled);
        output.putBoolean("isPowered", this.isPowered);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);
        this.photocellEnabled = input.getBooleanOr("photocell", true);
        this.isPowered = input.getBooleanOr("isPowered", false);
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
        return tag;
    }
}
