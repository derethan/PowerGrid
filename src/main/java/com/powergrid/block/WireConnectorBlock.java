package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WireConnectorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POLE_MOUNT = BooleanProperty.create("pole_mount");
    public static final BooleanProperty CROSSARM_END = BooleanProperty.create("crossarm_end");

    // Standard Shapes
    private static final VoxelShape SHAPE_UP    = Block.box(3.5, 0.0, 3.5, 12.5, 11.0, 12.5);
    private static final VoxelShape SHAPE_DOWN  = Block.box(3.5, 5.0, 3.5, 12.5, 16.0, 12.5);
    private static final VoxelShape SHAPE_NORTH = Block.box(3.5, 3.5, 5.0, 12.5, 12.5, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(3.5, 3.5, 0.0, 12.5, 12.5, 11.0);
    private static final VoxelShape SHAPE_WEST  = Block.box(5.0, 3.5, 3.5, 16.0, 12.5, 12.5);
    private static final VoxelShape SHAPE_EAST  = Block.box(0.0, 3.5, 3.5, 11.0, 12.5, 12.5);

    // Crossarm End Shapes (centered vertically at Y=13.0)
    private static final VoxelShape SHAPE_NORTH_END = Block.box(3.5, 8.5, 5.0, 12.5, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH_END = Block.box(3.5, 8.5, 0.0, 12.5, 16.0, 11.0);
    private static final VoxelShape SHAPE_WEST_END  = Block.box(5.0, 8.5, 3.5, 16.0, 16.0, 12.5);
    private static final VoxelShape SHAPE_EAST_END  = Block.box(0.0, 8.5, 3.5, 11.0, 16.0, 12.5);

    // Pole Standoff Shapes (extended base)
    private static final VoxelShape SHAPE_NORTH_POLE = Block.box(3.5, 3.5, 0.0, 12.5, 12.5, 16.0);
    private static final VoxelShape SHAPE_SOUTH_POLE = Block.box(3.5, 3.5, 0.0, 12.5, 12.5, 16.0);
    private static final VoxelShape SHAPE_WEST_POLE  = Block.box(0.0, 3.5, 3.5, 16.0, 12.5, 12.5);
    private static final VoxelShape SHAPE_EAST_POLE  = Block.box(0.0, 3.5, 3.5, 16.0, 12.5, 12.5);

    private final WireType wireType;

    public WireConnectorBlock(WireType wireType, Properties properties) {
        super(properties.strength(1.5f, 3.0f).sound(SoundType.METAL).noOcclusion());
        this.wireType = wireType;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POLE_MOUNT, false)
                .setValue(CROSSARM_END, false));
    }

    public WireType getWireType() {
        return wireType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POLE_MOUNT, CROSSARM_END);
    }

    public static BlockState getCalculatedState(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
        BlockPos attachedPos = pos.relative(facing.getOpposite());
        BlockState attachedState = level.getBlockState(attachedPos);

        boolean crossarmEnd = false;
        boolean poleMount = false;

        if (facing.getAxis().isHorizontal()) {
            if (attachedState.getBlock() instanceof PoleCrossarmBlock) {
                Direction.Axis crossarmAxis = attachedState.getValue(PoleCrossarmBlock.AXIS);
                if (facing.getAxis() == crossarmAxis) {
                    crossarmEnd = true;
                } else {
                    poleMount = true;
                }
            } else if (attachedState.getBlock() instanceof TreatedWoodPoleBlock || attachedState.getBlock() instanceof SteelTrussPoleBlock) {
                poleMount = true;
            }
        }

        return state.setValue(FACING, facing)
                .setValue(CROSSARM_END, crossarmEnd)
                .setValue(POLE_MOUNT, poleMount);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        return getCalculatedState(this.defaultBlockState(), context.getLevel(), context.getClickedPos(), facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite()) {
            return getCalculatedState(state, level, pos, facing);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(CROSSARM_END)) {
            return switch (facing) {
                case NORTH -> SHAPE_NORTH_END;
                case SOUTH -> SHAPE_SOUTH_END;
                case WEST  -> SHAPE_WEST_END;
                case EAST  -> SHAPE_EAST_END;
                default    -> SHAPE_UP;
            };
        }
        if (state.getValue(POLE_MOUNT)) {
            return switch (facing) {
                case NORTH -> SHAPE_NORTH_POLE;
                case SOUTH -> SHAPE_SOUTH_POLE;
                case WEST  -> SHAPE_WEST_POLE;
                case EAST  -> SHAPE_EAST_POLE;
                default    -> SHAPE_UP;
            };
        }
        return switch (facing) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WireConnectorBlockEntity(pos, state, this.wireType);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == PowerGridMod.WIRE_CONNECTOR_BE.get()
                ? (lvl, p, st, be) -> ((WireConnectorBlockEntity) be).serverTick(lvl, p, st)
                : null;
    }
}
