package com.powergrid.item;

import com.powergrid.block.entity.PowerPlantControllerBlockEntity;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class EngineerHammerItem extends Item {
    public EngineerHammerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PowerPlantControllerBlockEntity controller) {
            if (!level.isClientSide()) {
                controller.toggleAssembly(player);
            }
            return InteractionResult.SUCCESS;
        }

        // Rotate directional blocks on click
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            if (!level.isClientSide()) {
                Direction current = state.getValue(HorizontalDirectionalBlock.FACING);
                level.setBlock(pos, state.setValue(HorizontalDirectionalBlock.FACING, current.getClockWise()), 3);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.8f);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.hammer.desc_1"));
        builder.accept(Component.translatable("tooltip.powergrid.hammer.desc_2"));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
