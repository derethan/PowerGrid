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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PoleCrossarmBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty NEG_POLE = BooleanProperty.create("neg_pole");
    public static final BooleanProperty POS_POLE = BooleanProperty.create("pos_pole");
    public static final BooleanProperty NEG_END = BooleanProperty.create("neg_end");
    public static final BooleanProperty POS_END = BooleanProperty.create("pos_end");

    // Horizontal rounded beam shape along X and Z at upper portion of block (Y: 10.0 - 16.0)
    private static final VoxelShape SHAPE_X = Block.box(0.0, 10.0, 5.0, 16.0, 16.0, 11.0);
    private static final VoxelShape SHAPE_Z = Block.box(5.0, 10.0, 0.0, 11.0, 16.0, 16.0);

    public PoleCrossarmBlock(Properties properties) {
        super(properties.strength(2.0f, 3.0f).sound(SoundType.WOOD).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(NEG_POLE, false)
                .setValue(POS_POLE, false)
                .setValue(NEG_END, true)
                .setValue(POS_END, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, NEG_POLE, POS_POLE, NEG_END, POS_END);
    }

    public static Direction getNegativeDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
    }

    public static Direction getPositiveDirection(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private boolean isPole(BlockState state) {
        return state.getBlock() instanceof TreatedWoodPoleBlock;
    }

    private boolean isCrossarm(BlockState state) {
        return state.getBlock() instanceof PoleCrossarmBlock;
    }

    public BlockState calculateState(LevelReader level, BlockPos pos, Direction.Axis preferredAxis) {
        boolean westPole = isPole(level.getBlockState(pos.west()));
        boolean eastPole = isPole(level.getBlockState(pos.east()));
        boolean northPole = isPole(level.getBlockState(pos.north()));
        boolean southPole = isPole(level.getBlockState(pos.south()));

        boolean westCrossarm = isCrossarm(level.getBlockState(pos.west()));
        boolean eastCrossarm = isCrossarm(level.getBlockState(pos.east()));
        boolean northCrossarm = isCrossarm(level.getBlockState(pos.north()));
        boolean southCrossarm = isCrossarm(level.getBlockState(pos.south()));

        Direction.Axis chosenAxis = preferredAxis;
        if (westPole || eastPole || westCrossarm || eastCrossarm) {
            if (!(northPole || southPole || northCrossarm || southCrossarm) || preferredAxis == Direction.Axis.X) {
                chosenAxis = Direction.Axis.X;
            }
        } else if (northPole || southPole || northCrossarm || southCrossarm) {
            chosenAxis = Direction.Axis.Z;
        }

        Direction negDir = getNegativeDirection(chosenAxis);
        Direction posDir = getPositiveDirection(chosenAxis);

        BlockState negState = level.getBlockState(pos.relative(negDir));
        BlockState posState = level.getBlockState(pos.relative(posDir));

        boolean negIsPole = isPole(negState);
        boolean posIsPole = isPole(posState);

        boolean negIsCrossarm = isCrossarm(negState);
        boolean posIsCrossarm = isCrossarm(posState);

        boolean negEnd = !negIsPole && !negIsCrossarm;
        boolean posEnd = !posIsPole && !posIsCrossarm;

        return this.defaultBlockState()
                .setValue(AXIS, chosenAxis)
                .setValue(NEG_POLE, negIsPole)
                .setValue(POS_POLE, posIsPole)
                .setValue(NEG_END, negEnd)
                .setValue(POS_END, posEnd);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        Direction.Axis axis;
        if (clickedFace.getAxis().isHorizontal()) {
            axis = clickedFace.getAxis();
        } else {
            Direction playerHorizontal = context.getHorizontalDirection();
            axis = (playerHorizontal.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
        }

        return calculateState(level, pos, axis);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        Direction.Axis currentAxis = state.getValue(AXIS);
        if (direction.getAxis().isHorizontal()) {
            return calculateState(level, pos, currentAxis);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem())) {
            Direction.Axis axis = state.getValue(AXIS);
            Direction targetDir = null;

            // Expand towards the clicked side if open, or the first open end
            Direction hitDirection = hitResult.getDirection();
            if (hitDirection.getAxis() == axis) {
                if (hitDirection == getPositiveDirection(axis) && state.getValue(POS_END)) {
                    targetDir = hitDirection;
                } else if (hitDirection == getNegativeDirection(axis) && state.getValue(NEG_END)) {
                    targetDir = hitDirection;
                }
            }

            if (targetDir == null) {
                if (state.getValue(POS_END)) {
                    targetDir = getPositiveDirection(axis);
                } else if (state.getValue(NEG_END)) {
                    targetDir = getNegativeDirection(axis);
                }
            }

            if (targetDir != null) {
                BlockPos targetPos = pos.relative(targetDir);
                if (level.getBlockState(targetPos).canBeReplaced() && !level.isOutsideBuildHeight(targetPos)) {
                    if (!level.isClientSide()) {
                        BlockState newState = calculateState(level, targetPos, axis);
                        level.setBlock(targetPos, newState, 3);
                        level.playSound(null, targetPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.85f);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
