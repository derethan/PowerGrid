package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.WoodenWaterWheelBlockEntity;
import com.powergrid.item.EngineerHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WoodenWaterWheelBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<WaterWheelPart> PART = EnumProperty.create("part", WaterWheelPart.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty OPERATING = BooleanProperty.create("operating");

    // Axle shapes for center block (8x8 box spanning 16 units through axle axis)
    private static final VoxelShape AXLE_SHAPE_Z = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0);
    private static final VoxelShape AXLE_SHAPE_X = Block.box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0);

    // Rim/paddle selection shape for member blocks (centered 12-unit thick slab)
    private static final VoxelShape MEMBER_SHAPE_Z = Block.box(1.0, 1.0, 2.0, 15.0, 15.0, 14.0);
    private static final VoxelShape MEMBER_SHAPE_X = Block.box(2.0, 1.0, 1.0, 14.0, 15.0, 15.0);

    public WoodenWaterWheelBlock(Properties properties) {
        super(properties
                .strength(2.5f, 5.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, WaterWheelPart.CENTER)
                .setValue(WATERLOGGED, false)
                .setValue(OPERATING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WATERLOGGED, OPERATING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Monolithic BER renders the entire rotating 3D water wheel; block models are invisible to prevent z-fighting
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        // Empty occlusion shape prevents neighbor chunk face culling
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        WaterWheelPart part = state.getValue(PART);
        boolean isZAxis = (facing.getAxis() == Direction.Axis.Z);

        if (part == WaterWheelPart.CENTER) {
            return isZAxis ? AXLE_SHAPE_Z : AXLE_SHAPE_X;
        }
        return isZAxis ? MEMBER_SHAPE_Z : MEMBER_SHAPE_X;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Members allow fluid to pass and items/players to step through smoothly
        if (state.getValue(PART) == WaterWheelPart.CENTER) {
            return getShape(state, level, pos, context);
        }
        return Shapes.empty();
    }

    public static Direction getHorizontalDiameterDirection(Direction facing) {
        return WaterWheelPart.getHorizontalDiameterDirection(facing);
    }

    public static BlockPos getCenterPos(BlockPos currentPos, BlockState state) {
        return WaterWheelPart.getCenterPos(currentPos, state.getValue(FACING), state.getValue(PART));
    }

    public static BlockPos getPartPos(BlockPos centerPos, Direction facing, WaterWheelPart part) {
        return WaterWheelPart.getPartPos(centerPos, facing, part);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        // Check if placed against an existing water wheel to match axle orientation for stacking
        BlockState clickedState = level.getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        Direction facing;
        if (clickedState.getBlock() instanceof WoodenWaterWheelBlock) {
            facing = clickedState.getValue(FACING);
        } else if (context.getClickedFace().getAxis().isHorizontal()) {
            facing = (context.getClickedFace().getAxis() == Direction.Axis.X) ? Direction.EAST : Direction.NORTH;
        } else {
            Direction playerFacing = context.getHorizontalDirection();
            facing = (playerFacing.getAxis() == Direction.Axis.X) ? Direction.EAST : Direction.NORTH;
        }

        // Verify clearance for all 9 parts in the 3x3x1 vertical plane
        for (WaterWheelPart part : WaterWheelPart.values()) {
            BlockPos pos = getPartPos(clickedPos, facing, part);
            if (level.isOutsideBuildHeight(pos)) {
                return null;
            }
            if (part != WaterWheelPart.CENTER) {
                BlockState stateAt = level.getBlockState(pos);
                if (!stateAt.canBeReplaced(context) && !stateAt.getFluidState().is(Fluids.WATER)) {
                    return null;
                }
            }
        }

        FluidState fluid = level.getFluidState(clickedPos);
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, WaterWheelPart.CENTER)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER)
                .setValue(OPERATING, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);

            for (WaterWheelPart part : WaterWheelPart.values()) {
                if (part == WaterWheelPart.CENTER) continue;
                BlockPos partPos = getPartPos(pos, facing, part);
                boolean wasWater = level.getFluidState(partPos).getType() == Fluids.WATER;

                level.setBlock(partPos, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(PART, part)
                        .setValue(WATERLOGGED, wasWater)
                        .setValue(OPERATING, false), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos centerPos = getCenterPos(pos, state);
            Direction facing = state.getValue(FACING);

            for (WaterWheelPart part : WaterWheelPart.values()) {
                BlockPos targetPos = getPartPos(centerPos, facing, part);
                if (!targetPos.equals(pos)) {
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(this)) {
                        BlockState replacement = targetState.getValue(WATERLOGGED)
                                ? Blocks.WATER.defaultBlockState()
                                : Blocks.AIR.defaultBlockState();
                        level.setBlock(targetPos, replacement, 35);
                        level.levelEvent(player, 2001, targetPos, Block.getId(targetState));
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        BlockPos centerPos = getCenterPos(pos, state);
        Direction facing = state.getValue(FACING);

        for (WaterWheelPart part : WaterWheelPart.values()) {
            BlockPos targetPos = getPartPos(centerPos, facing, part);
            if (!targetPos.equals(pos)) {
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(this)) {
                    BlockState replacement = targetState.getValue(WATERLOGGED)
                            ? Blocks.WATER.defaultBlockState()
                            : Blocks.AIR.defaultBlockState();
                    level.setBlock(targetPos, replacement, 35);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos centerPos = getCenterPos(pos, state);

        if (stack.getItem() instanceof EngineerHammerItem) {
            if (!level.isClientSide()) {
                Direction currentFacing = state.getValue(FACING);
                Direction nextFacing = (currentFacing.getAxis() == Direction.Axis.Z) ? Direction.EAST : Direction.NORTH;
                boolean operating = state.getValue(OPERATING);

                // Test if 9-block clearance exists in the new orientation
                boolean canRotate = true;
                for (WaterWheelPart part : WaterWheelPart.values()) {
                    BlockPos targetPos = getPartPos(centerPos, nextFacing, part);
                    BlockState atState = level.getBlockState(targetPos);
                    if (!atState.is(this) && !atState.canBeReplaced() && !atState.getFluidState().is(Fluids.WATER)) {
                        canRotate = false;
                        break;
                    }
                }

                if (canRotate) {
                    // Clear old positions
                    for (WaterWheelPart part : WaterWheelPart.values()) {
                        BlockPos oldPos = getPartPos(centerPos, currentFacing, part);
                        BlockState oldState = level.getBlockState(oldPos);
                        boolean water = oldState.hasProperty(WATERLOGGED) && oldState.getValue(WATERLOGGED);
                        level.setBlock(oldPos, water ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 35);
                    }

                    // Place new positions
                    for (WaterWheelPart part : WaterWheelPart.values()) {
                        BlockPos newPos = getPartPos(centerPos, nextFacing, part);
                        boolean water = level.getFluidState(newPos).getType() == Fluids.WATER;
                        level.setBlock(newPos, this.defaultBlockState()
                                .setValue(FACING, nextFacing)
                                .setValue(PART, part)
                                .setValue(WATERLOGGED, water)
                                .setValue(OPERATING, operating), 3);
                    }

                    level.playSound(null, centerPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.8f);
                    player.sendSystemMessage(Component.literal("§e[Water Wheel] §7Rotated axle axis to §6" + nextFacing.getAxis().getName().toUpperCase() + "§r"));
                } else {
                    player.sendSystemMessage(Component.literal("§c[Water Wheel] §7Cannot rotate: obstruction in target area.§r"));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == WaterWheelPart.CENTER) {
            return new WoodenWaterWheelBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == WaterWheelPart.CENTER) {
            return level.isClientSide()
                    ? (lvl, p, st, be) -> {
                        if (be instanceof WoodenWaterWheelBlockEntity waterWheel) {
                            waterWheel.clientTick();
                        }
                    }
                    : (lvl, p, st, be) -> {
                        if (be instanceof WoodenWaterWheelBlockEntity waterWheel) {
                            waterWheel.serverTick(lvl, p, st);
                        }
                    };
        }
        return null;
    }
}
