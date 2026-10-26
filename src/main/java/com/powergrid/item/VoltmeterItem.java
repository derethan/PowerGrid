package com.powergrid.item;

import com.powergrid.block.entity.PowerPlantControllerBlockEntity;
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

        // Check if wire node
        if (be instanceof IWireNode node) {
            foundInfo = true;
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_header"));
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_node_tier", node.getSupportedWireType().getDisplayName()));
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_conn_count", node.getConnections().size()));
            for (WireConnection conn : node.getConnections()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_conn_line",
                        conn.getTargetPos().toShortString(), (int) conn.getDistance(), conn.getWireType().getDisplayName()));
            }
        }

        // Check if multiblock power plant
        if (be instanceof PowerPlantControllerBlockEntity plant) {
            foundInfo = true;
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_plant_status",
                    plant.isAssembled() ? "§aASSEMBLED & OPERATIONAL§r" : "§cINCOMPLETE§r"));
        }

        // Check energy capability
        EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, pos, face);
        if (energy != null) {
            foundInfo = true;
            player.sendSystemMessage(Component.translatable("message.powergrid.voltmeter_energy",
                    energy.getAmountAsInt(), energy.getCapacityAsInt()));
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
