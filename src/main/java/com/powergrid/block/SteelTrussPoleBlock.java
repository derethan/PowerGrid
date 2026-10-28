package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SteelTrussPoleBlock extends Block {
    private static final VoxelShape TRUSS_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public SteelTrussPoleBlock(Properties properties) {
        super(properties.strength(4.5f, 8.0f).sound(SoundType.NETHERITE_BLOCK).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TRUSS_SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem())) {
            BlockPos.MutableBlockPos target = pos.mutable();
            while (level.getBlockState(target).getBlock() instanceof SteelTrussPoleBlock) {
                target.move(Direction.UP);
            }

            if (level.getBlockState(target).canBeReplaced() && !level.isOutsideBuildHeight(target)) {
                if (!level.isClientSide()) {
                    level.setBlock(target, this.defaultBlockState(), 3);
                    level.playSound(null, target, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0f, 0.85f);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
