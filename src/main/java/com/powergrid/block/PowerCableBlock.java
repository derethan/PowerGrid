package com.powergrid.block;

import com.powergrid.block.entity.PowerCableBlockEntity;
import com.powergrid.grid.CableConnectionType;
import com.powergrid.grid.CableGauge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class PowerCableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final EnumProperty<CableConnectionType> NORTH = EnumProperty.create("north", CableConnectionType.class);
    public static final EnumProperty<CableConnectionType> SOUTH = EnumProperty.create("south", CableConnectionType.class);
    public static final EnumProperty<CableConnectionType> EAST = EnumProperty.create("east", CableConnectionType.class);
    public static final EnumProperty<CableConnectionType> WEST = EnumProperty.create("west", CableConnectionType.class);
    public static final EnumProperty<CableConnectionType> UP = EnumProperty.create("up", CableConnectionType.class);
    public static final EnumProperty<CableConnectionType> DOWN = EnumProperty.create("down", CableConnectionType.class);

    public static final Map<Direction, EnumProperty<CableConnectionType>> FACING_PROPERTIES = Map.of(
            Direction.NORTH, NORTH,
            Direction.SOUTH, SOUTH,
            Direction.EAST, EAST,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
    );

    private static final VoxelShape CORE_SHAPE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    private static final VoxelShape UP_SHAPE = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);

    private final CableGauge gauge;

    public PowerCableBlock(CableGauge gauge, Properties properties) {
        super(properties.strength(0.8f, 1.0f).sound(SoundType.WOOL).noOcclusion());
        this.gauge = gauge;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(NORTH, CableConnectionType.NONE)
                .setValue(SOUTH, CableConnectionType.NONE)
                .setValue(EAST, CableConnectionType.NONE)
                .setValue(WEST, CableConnectionType.NONE)
                .setValue(UP, CableConnectionType.NONE)
                .setValue(DOWN, CableConnectionType.NONE));
    }

    public CableGauge getGauge() {
        return gauge;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerCableBlockEntity(pos, state, this.gauge);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof PowerCableBlockEntity cable) {
                cable.serverTick(lvl, pos, st);
            }
        };
    }

    public CableConnectionType getConnectionType(Level level, BlockPos pos, Direction dir, @Nullable PowerCableBlockEntity cableBE) {
        if (cableBE != null && cableBE.isSideDisabled(dir)) {
            return CableConnectionType.NONE;
        }

        BlockPos neighborPos = pos.relative(dir);
        if (!level.isLoaded(neighborPos)) {
            return CableConnectionType.NONE;
        }

        BlockEntity neighborBE = level.getBlockEntity(neighborPos);
        if (neighborBE instanceof PowerCableBlockEntity neighborCable) {
            if (neighborCable.isSideDisabled(dir.getOpposite())) {
                return CableConnectionType.NONE;
            }
            return CableConnectionType.CABLE;
        }

        if (level.getCapability(Capabilities.Energy.BLOCK, neighborPos, dir.getOpposite()) != null) {
            return CableConnectionType.DEVICE;
        }

        return CableConnectionType.NONE;
    }

    public BlockState updateConnections(Level level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        PowerCableBlockEntity cableBE = (be instanceof PowerCableBlockEntity c) ? c : null;

        BlockState result = state;
        for (Direction dir : Direction.values()) {
            EnumProperty<CableConnectionType> prop = FACING_PROPERTIES.get(dir);
            CableConnectionType conn = getConnectionType(level, pos, dir, cableBE);
            result = result.setValue(prop, conn);
        }
        return result;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluid = level.getFluidState(pos);

        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
        return updateConnections(level, pos, state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        if (levelReader instanceof Level level) {
            return updateConnections(level, pos, state);
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;
        if (state.getValue(NORTH).isConnected()) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH).isConnected()) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST).isConnected()) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST).isConnected()) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP).isConnected()) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN).isConnected()) shape = Shapes.or(shape, DOWN_SHAPE);
        return shape;
    }

    /**
     * Finds which direction arm or face was clicked on the cable block.
     */
    public static Direction getTargetedSide(Vec3 clickLocation, Direction clickedFace, BlockPos pos) {
        Vec3 hit = clickLocation.subtract(pos.getX(), pos.getY(), pos.getZ());
        double dx = hit.x - 0.5;
        double dy = hit.y - 0.5;
        double dz = hit.z - 0.5;

        // If clicked outside the 4x4 center core, select the arm direction
        double absX = Math.abs(dx);
        double absY = Math.abs(dy);
        double absZ = Math.abs(dz);

        if (absX > absY && absX > absZ && absX > 0.15) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY > absX && absY > absZ && absY > 0.15) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else if (absZ > absX && absZ > absY && absZ > 0.15) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        return clickedFace;
    }
}
