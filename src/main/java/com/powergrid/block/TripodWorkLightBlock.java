package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TripodWorkLightBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape LOWER_SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5)
    );

    private static final VoxelShape UPPER_SHAPE_NS = Shapes.or(
            Block.box(6.5, 0.0, 6.5, 9.5, 9.0, 9.5),
            Block.box(1.0, 7.0, 4.0, 15.0, 16.0, 12.0)
    );

    private static final VoxelShape UPPER_SHAPE_EW = Shapes.or(
            Block.box(6.5, 0.0, 6.5, 9.5, 9.0, 9.5),
            Block.box(4.0, 7.0, 1.0, 12.0, 16.0, 15.0)
    );

    public TripodWorkLightBlock(BlockBehaviour.Properties properties) {
        super(properties
                .sound(SoundType.LANTERN)
                .strength(1.5f, 3.0f)
                .noOcclusion()
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_SHAPE;
        }
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.X ? UPPER_SHAPE_EW : UPPER_SHAPE_NS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxY() && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(LIT, true);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos below = pos.below();
            BlockState stateBelow = level.getBlockState(below);
            return !stateBelow.isAir() && (stateBelow.isFaceSturdy(level, below, Direction.UP) || Block.canSupportCenter(level, below, Direction.UP) || stateBelow.isSolid());
        } else {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) == half) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            DoubleBlockHalf half = state.getValue(HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos below = pos.below();
                BlockState stateBelow = level.getBlockState(below);
                if (stateBelow.is(state.getBlock()) && stateBelow.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean newLit = !state.getValue(LIT);
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

        level.setBlock(pos, state.setValue(LIT, newLit), Block.UPDATE_ALL);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this)) {
            level.setBlock(otherPos, otherState.setValue(LIT, newLit), Block.UPDATE_ALL);
        }

        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, newLit ? 0.8f : 0.6f);
        return InteractionResult.SUCCESS;
    }
}
