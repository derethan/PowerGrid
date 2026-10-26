package com.powergrid.item;

import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WireCuttersItem extends Item {
    public WireCuttersItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IWireNode node)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        List<WireConnection> conns = new ArrayList<>(node.getConnections());
        if (conns.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.powergrid.no_wires_to_cut"));
            return InteractionResult.SUCCESS;
        }

        for (WireConnection conn : conns) {
            BlockEntity otherBE = level.getBlockEntity(conn.getTargetPos());
            if (otherBE instanceof IWireNode otherNode) {
                otherNode.removeConnection(pos);
            }
        }
        node.clearAllConnections();

        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.sendSystemMessage(Component.translatable("message.powergrid.wires_severed", conns.size()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.cutters.desc"));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
