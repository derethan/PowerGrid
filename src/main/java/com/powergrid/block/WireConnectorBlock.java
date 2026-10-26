package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.WireConnectorBlockEntity;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WireConnectorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_DOWN = Block.box(5.0, 0.0, 5.0, 11.0, 8.0, 11.0);
    private static final VoxelShape SHAPE_UP = Block.box(5.0, 8.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 8.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 5.0, 8.0, 11.0, 11.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0, 5.0, 5.0, 8.0, 11.0, 11.0);
    private static final VoxelShape SHAPE_EAST = Block.box(8.0, 5.0, 5.0, 16.0, 11.0, 11.0);

    private final WireType wireType;

    public WireConnectorBlock(WireType wireType, Properties properties) {
        super(properties.strength(1.5f, 3.0f).sound(SoundType.METAL).noOcclusion());
        this.wireType = wireType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public WireType getWireType() {
        return wireType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
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
