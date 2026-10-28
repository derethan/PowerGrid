package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TreatedWoodPoleBlock extends Block {
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    // 7x16x7 centered octagonal column bounding shape
    private static final VoxelShape POLE_SHAPE = Block.box(4.5, 0.0, 4.5, 11.5, 16.0, 11.5);

    public TreatedWoodPoleBlock(Properties properties) {
        super(properties.strength(2.0f, 3.0f).sound(SoundType.WOOD).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return POLE_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean hasUp = isPole(level.getBlockState(pos.above()));
        boolean hasDown = isPole(level.getBlockState(pos.below())) || level.getBlockState(pos.below()).isSolid();
        return this.defaultBlockState().setValue(UP, hasUp).setValue(DOWN, hasDown);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (direction == Direction.UP) {
            return state.setValue(UP, isPole(neighborState));
        } else if (direction == Direction.DOWN) {
            return state.setValue(DOWN, isPole(neighborState) || neighborState.isSolid());
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    private boolean isPole(BlockState state) {
        return state.getBlock() instanceof TreatedWoodPoleBlock;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Right-clicking any section of a pole with a pole in hand extends the pole upwards at the top
        if (stack.is(this.asItem())) {
            BlockPos.MutableBlockPos target = pos.mutable();
            // Traverse upwards to the top of the pole column
            while (level.getBlockState(target).getBlock() instanceof TreatedWoodPoleBlock) {
                target.move(Direction.UP);
            }

            if (level.getBlockState(target).canBeReplaced() && !level.isOutsideBuildHeight(target)) {
                if (!level.isClientSide()) {
                    boolean hasUp = isPole(level.getBlockState(target.above()));
                    boolean hasDown = isPole(level.getBlockState(target.below())) || level.getBlockState(target.below()).isSolid();
                    BlockState newState = this.defaultBlockState().setValue(UP, hasUp).setValue(DOWN, hasDown);
                    level.setBlock(target, newState, 3);
                    level.playSound(null, target, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.85f);
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
