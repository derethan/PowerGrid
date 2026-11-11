package com.powergrid.item;

import com.powergrid.grid.GridNetworkSavedData;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
    private static final Map<UUID, PendingWire> PENDING_CONNECTIONS = new HashMap<>();

    private record PendingWire(BlockPos pos, WireType wireType, long gameTime) {}

    public WireCoilItem(WireType wireType, Properties properties) {
        super(properties.stacksTo(64));
        this.wireType = wireType;
    }

    public WireType getWireType() {
        return wireType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (PENDING_CONNECTIONS.remove(player.getUUID()) != null) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_cancelled"));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
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
        PendingWire pending = PENDING_CONNECTIONS.get(playerId);

        // Cancel if mismatched wire type or stale (older than 30 seconds / 600 ticks)
        if (pending != null && (pending.wireType() != this.wireType || level.getGameTime() - pending.gameTime() > 600L)) {
            PENDING_CONNECTIONS.remove(playerId);
            pending = null;
        }

        if (pending == null) {
            // First click: select starting node
            if (!targetNode.isWireTypeSupported(this.wireType)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect_tier", this.wireType.getDisplayName()));
                return InteractionResult.FAIL;
            }
            if (!targetNode.canTakeMoreConnections()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect"));
                return InteractionResult.FAIL;
            }
            PENDING_CONNECTIONS.put(playerId, new PendingWire(pos, this.wireType, level.getGameTime()));
            level.playSound(null, pos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.8f, 1.2f);
            player.sendSystemMessage(Component.translatable("message.powergrid.wire_start", pos.getX(), pos.getY(), pos.getZ()));
            return InteractionResult.SUCCESS;
        } else {
            // Second click: complete connection
            PENDING_CONNECTIONS.remove(playerId);
            BlockPos startPos = pending.pos();
            if (startPos.equals(pos)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_cancelled"));
                return InteractionResult.SUCCESS;
            }

            BlockEntity startBE = level.getBlockEntity(startPos);
            IWireNode startNode = (startBE instanceof IWireNode in) ? in : null;
            GridNetworkSavedData.SavedNode savedStart = null;

            if (startNode == null && level instanceof ServerLevel serverLevel) {
                savedStart = GridNetworkSavedData.get(serverLevel).getNode(startPos);
            }

            if (startNode == null && savedStart == null) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_invalid_start"));
                return InteractionResult.FAIL;
            }

            boolean startSupports = startNode != null ? startNode.isWireTypeSupported(this.wireType) : (savedStart.wireType == this.wireType || savedStart.nodeType == GridNetworkSavedData.NodeType.BREAKER || savedStart.nodeType == GridNetworkSavedData.NodeType.POLE_LIGHT);
            if (!startSupports || !targetNode.isWireTypeSupported(this.wireType)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect_tier", this.wireType.getDisplayName()));
                return InteractionResult.FAIL;
            }

            double dist = Math.sqrt(startPos.distSqr(pos));
            if (dist > this.wireType.getMaxDistance()) {
                player.sendSystemMessage(Component.translatable("message.powergrid.wire_too_long", (int) dist, this.wireType.getMaxDistance()));
                return InteractionResult.FAIL;
            }

            if (!targetNode.canConnectWire(this.wireType, startPos)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect"));
                return InteractionResult.FAIL;
            }

            if (startNode != null && !startNode.canConnectWire(this.wireType, pos)) {
                player.sendSystemMessage(Component.translatable("message.powergrid.cannot_connect"));
                return InteractionResult.FAIL;
            }

            // Establish connection on both ends
            if (startNode != null) {
                startNode.addConnection(pos, this.wireType, dist);
            } else if (level instanceof ServerLevel serverLevel) {
                GridNetworkSavedData.get(serverLevel).addWireConnection(startPos, pos, this.wireType, dist);
            }
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
