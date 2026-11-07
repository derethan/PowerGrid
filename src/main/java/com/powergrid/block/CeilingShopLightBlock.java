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
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CeilingShopLightBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE_NS = Block.box(4.0, 11.0, 0.0, 12.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_EW = Block.box(0.0, 11.0, 4.0, 16.0, 16.0, 12.0);

    public CeilingShopLightBlock(BlockBehaviour.Properties properties) {
        super(properties
                .sound(SoundType.LANTERN)
                .strength(1.5f, 3.0f)
                .noOcclusion()
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EW : SHAPE_NS;
    }

    public static Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    public static boolean isValidCeiling(LevelReader level, BlockPos ceilingPos) {
        BlockState state = level.getBlockState(ceilingPos);
        return !state.isAir() && (
                state.isFaceSturdy(level, ceilingPos, Direction.DOWN)
                || Block.canSupportCenter(level, ceilingPos, Direction.DOWN)
                || state.isSolid()
                || state.isCollisionShapeFullBlock(level, ceilingPos)
        );
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidCeiling(level, pos.above());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (!isValidCeiling(level, pos.above())) {
            return null;
        }

        Direction primary = context.getHorizontalDirection();
        Direction[] candidates = new Direction[]{
                primary,
                primary.getOpposite(),
                primary.getClockWise(),
                primary.getCounterClockWise()
        };

        for (Direction dir : candidates) {
            BlockPos headPos = pos.relative(dir);
            if (isValidCeiling(level, headPos.above()) && level.getBlockState(headPos).canBeReplaced(context)) {
                return this.defaultBlockState()
                        .setValue(FACING, dir)
                        .setValue(PART, BedPart.FOOT)
                        .setValue(LIT, true);
            }
        }

        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            BedPart part = state.getValue(PART);
            BlockPos otherPos = pos.relative(getNeighbourDirection(part, facing));
            BedPart otherPart = part == BedPart.FOOT ? BedPart.HEAD : BedPart.FOOT;
            level.setBlock(otherPos, state.setValue(PART, otherPart), Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        Direction facing = state.getValue(FACING);
        BedPart part = state.getValue(PART);
        if (direction == getNeighbourDirection(part, facing)) {
            if (!neighborState.is(this) || neighborState.getValue(PART) == part || neighborState.getValue(FACING) != facing) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            BedPart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            BlockPos otherPos = pos.relative(getNeighbourDirection(part, facing));
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(PART) != part) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean newLit = !state.getValue(LIT);
        BedPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos otherPos = pos.relative(getNeighbourDirection(part, facing));

        level.setBlock(pos, state.setValue(LIT, newLit), Block.UPDATE_ALL);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this)) {
            level.setBlock(otherPos, otherState.setValue(LIT, newLit), Block.UPDATE_ALL);
        }

        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, newLit ? 0.8f : 0.6f);
        return InteractionResult.SUCCESS;
    }
}
