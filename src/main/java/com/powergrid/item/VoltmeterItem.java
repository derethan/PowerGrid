package com.powergrid.item;

import com.powergrid.block.StreetLightBlock;
import com.powergrid.block.WoodenWaterWheelBlock;
import com.powergrid.block.entity.PowerCableBlockEntity;
import com.powergrid.block.entity.StreetLightBlockEntity;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import com.powergrid.block.entity.WoodenWaterWheelBlockEntity;
import com.powergrid.config.PowerGridConfig;
import com.powergrid.grid.CableGauge;
import com.powergrid.grid.CableNetworkRouter;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.List;
import java.util.function.Consumer;

public class VoltmeterItem extends Item {
    public VoltmeterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.4f);

        BlockEntity be = level.getBlockEntity(pos);
        boolean foundInfo = false;

        // Check if Indoor Power Cable
        if (be instanceof PowerCableBlockEntity cable) {
            foundInfo = true;
            CableGauge gauge = cable.getGauge();
            player.sendSystemMessage(Component.literal("§6===== [Power Grid Cable Diagnostic] =====§r"));
            player.sendSystemMessage(Component.literal("§7Cable Gauge: §e" + gauge.getDisplayName()));
            player.sendSystemMessage(Component.literal("§7Max Throughput: §6" + String.format("%,d", gauge.getTransferRate()) + " FE/t§r"));
            player.sendSystemMessage(Component.literal("§7Stored Buffer: §b" + cable.getEnergyStorage().getAmountAsInt() + " / " + gauge.getBufferCapacity() + " FE§r"));
            player.sendSystemMessage(Component.literal("§7Transferred this Tick: §a" + cable.getTransferredThisTick() + " FE§r (Remaining: §a" + cable.getRemainingThroughput() + " FE§r)"));

            StringBuilder conns = new StringBuilder();
            for (Direction dir : Direction.values()) {
                if (cable.isSideDisabled(dir)) {
                    conns.append(" ").append(dir.getName().toUpperCase()).append("(§cDIS§r)");
                } else {
                    BlockPos np = pos.relative(dir);
                    if (level.isLoaded(np)) {
                        BlockEntity nbe = level.getBlockEntity(np);
                        if (nbe instanceof PowerCableBlockEntity) {
                            conns.append(" ").append(dir.getName().toUpperCase()).append("(§aCABLE§r)");
                        } else if (level.getCapability(Capabilities.Energy.BLOCK, np, dir.getOpposite()) != null) {
                            conns.append(" ").append(dir.getName().toUpperCase()).append("(§bDEV§r)");
                        }
                    }
                }
            }
            if (conns.length() > 0) {
                player.sendSystemMessage(Component.literal("§7Connections:" + conns));
            }

            List<CableNetworkRouter.FoundDevice> sinks = CableNetworkRouter.findSinks(level, cable, null);
            player.sendSystemMessage(Component.literal("§7Reachable Sinks: §f" + sinks.size()));
            for (CableNetworkRouter.FoundDevice sink : sinks) {
                player.sendSystemMessage(Component.literal(" §8-§r Target: §b" + sink.target.devicePos().toShortString() + "§r (Path limit: §6" + sink.bottleneck + " FE/t§r)"));
            }
        }

        // Check if Street Light
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof StreetLightBlock) {
            foundInfo = true;
            StreetLightBlock.StreetLightPart part = state.getValue(StreetLightBlock.PART);
            BlockPos basePos = StreetLightBlock.getBasePos(pos, part);
            BlockEntity baseBE = level.getBlockEntity(basePos);
            if (baseBE instanceof StreetLightBlockEntity streetLight) {
                player.sendSystemMessage(Component.literal("§6===== [Power Grid Street Light Diagnostic] =====§r"));
                player.sendSystemMessage(Component.literal("§7Status: " + (streetLight.isPowered() ? "§aEnergized (High Brightness - 15)§r" : "§eStandby Glow (Default - 7)§r")));
                player.sendSystemMessage(Component.literal("§7Internal Buffer: §b" + streetLight.energyStorage.getAmountAsInt() + " / " + StreetLightBlockEntity.BUFFER_CAPACITY + " FE§r"));
                player.sendSystemMessage(Component.literal("§7Power Draw: §6" + StreetLightBlockEntity.POWER_DRAW_PER_TICK + " FE/t (when energized)§r"));
                player.sendSystemMessage(Component.literal("§7Photocell Mode: " + (streetLight.isPhotocellEnabled() ? "§aAutomatic (Dusk-to-Dawn)§r" : "§6Always On (24/7)§r")));
                player.sendSystemMessage(Component.literal("§8(Tip: Sneak-Right-Click with Engineer's Hammer to toggle Photocell mode)§r"));
            }
        }

        // Check if Wooden Water Wheel
        if (state.getBlock() instanceof WoodenWaterWheelBlock) {
            foundInfo = true;
            BlockPos centerPos = WoodenWaterWheelBlock.getCenterPos(pos, state);
            BlockEntity centerBE = level.getBlockEntity(centerPos);
            if (centerBE instanceof WoodenWaterWheelBlockEntity wheel) {
                player.sendSystemMessage(Component.literal("§6===== [Power Grid Water Wheel Diagnostic] =====§r"));
                player.sendSystemMessage(Component.literal("§7Status: " + (wheel.isSpinning() ? "§aSpinning (Flowing Water Detected)§r" : "§cStopped (Requires Flowing Water)§r")));
                player.sendSystemMessage(Component.literal("§7Water Drop Height: §b" + wheel.getWaterDropHeight() + " block(s)§r"));
                player.sendSystemMessage(Component.literal("§7Generation Rate: §a" + wheel.getFeGeneratedLastTick() + " FE/t§r §8(10 FE/t per drop level)§r"));
                player.sendSystemMessage(Component.literal("§7Stored Energy: §b" + wheel.energyStorage.getAmountAsInt() + " / " + PowerGridConfig.CONFIG.waterWheelCapacity.get() + " FE§r"));
                player.sendSystemMessage(Component.literal("§8(Tip: Stack wheels along axle to transfer accumulated energy to transformers or batteries)§r"));
            }
        }

        // Check if wire node
        if (be instanceof IWireNode node) {
            foundInfo = true;
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_header"));
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_node_tier", node.getSupportedWireType().getDisplayName()));
            if (be instanceof WireConnectorBlockEntity connector) {
                player.sendSystemMessage(Component.literal("§7Mode: §r").append(connector.getMode().getFormattedName()));
            }
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_conn_count", node.getConnections().size()));
            for (WireConnection conn : node.getConnections()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_conn_line",
                        conn.getTargetPos().toShortString(), (int) conn.getDistance(), conn.getWireType().getDisplayName()));
            }
        }

        // Check energy capability
        if (!(be instanceof PowerCableBlockEntity)) {
            EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, pos, face);
            if (energy != null) {
                foundInfo = true;
                player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_energy",
                        energy.getAmountAsInt(), energy.getCapacityAsInt()));
            }
        }

        if (!foundInfo) {
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_no_data"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.voltmeter.desc"));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
