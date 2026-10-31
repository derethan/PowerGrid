package com.powergrid.item;

import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class WireCoilItem extends Item {
    private final WireType wireType;
    private static final Map<UUID, BlockPos> PENDING_CONNECTIONS = new HashMap<>();

    public WireCoilItem(WireType wireType, Properties properties) {
        super(properties.stacksTo(64));
        this.wireType = wireType;
    }

    public WireType getWireType() {
        return wireType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IWireNode targetNode)) {
            if (player.isShiftKeyDown() && PENDING_CONNECTIONS.containsKey(player.getUUID())) {
                PENDING_CONNECTIONS.remove(player.getUUID());
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_cancelled"));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        UUID playerId = player.getUUID();
        if (!PENDING_CONNECTIONS.containsKey(playerId)) {
            // First click: select starting node
            if (!targetNode.isWireTypeSupported(this.wireType)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect_tier", this.wireType.getDisplayName()));
                return InteractionResult.FAIL;
            }
            if (!targetNode.canTakeMoreConnections()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect"));
                return InteractionResult.FAIL;
            }
            PENDING_CONNECTIONS.put(playerId, pos);
            level.playSound(null, pos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.8f, 1.2f);
            player.sendSystemMessage(Component.translatable("message.powergrid.wire_start", pos.getX(), pos.getY(), pos.getZ()));
            return InteractionResult.SUCCESS;
        } else {
            // Second click: complete connection
            BlockPos startPos = PENDING_CONNECTIONS.remove(playerId);
            if (startPos.equals(pos)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_cancelled"));
                return InteractionResult.SUCCESS;
            }

            BlockEntity startBE = level.getBlockEntity(startPos);
            if (!(startBE instanceof IWireNode startNode)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_invalid_start"));
                return InteractionResult.FAIL;
            }

            if (!startNode.isWireTypeSupported(this.wireType) || !targetNode.isWireTypeSupported(this.wireType)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect_tier", this.wireType.getDisplayName()));
                return InteractionResult.FAIL;
            }

            double dist = Math.sqrt(startPos.distSqr(pos));
            if (dist > this.wireType.getMaxDistance()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_too_long", (int) dist, this.wireType.getMaxDistance()));
                return InteractionResult.FAIL;
            }

            if (!startNode.canConnectWire(this.wireType, pos) || !targetNode.canConnectWire(this.wireType, startPos)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect"));
                return InteractionResult.FAIL;
            }

            // Establish connection on both ends
            startNode.addConnection(pos, this.wireType, dist);
            targetNode.addConnection(startPos, this.wireType, dist);

            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.5f);
            player.sendSystemMessage(Component.translatable("message.powergrid.wire_connected", (int) dist, this.wireType.getDisplayName()));

            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.wire_tier", this.wireType.getDisplayName()));
        builder.accept(Component.translatable("tooltip.powergrid.wire_max_rate", this.wireType.getTransferRate()));
        builder.accept(Component.translatable("tooltip.powergrid.wire_max_dist", this.wireType.getMaxDistance()));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
