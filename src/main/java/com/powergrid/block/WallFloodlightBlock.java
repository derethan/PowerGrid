package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WallFloodlightBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 4.0, 10.0, 14.0, 13.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 4.0, 0.0, 14.0, 13.0, 6.0);
    private static final VoxelShape SHAPE_WEST = Block.box(10.0, 4.0, 2.0, 16.0, 13.0, 14.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 4.0, 2.0, 6.0, 13.0, 14.0);

    public WallFloodlightBlock(BlockBehaviour.Properties properties) {
        super(properties
                .sound(SoundType.LANTERN)
                .strength(1.2f, 2.5f)
                .noOcclusion()
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    public static boolean isValidWall(LevelReader level, BlockPos wallPos, Direction face) {
        BlockState state = level.getBlockState(wallPos);
        return !state.isAir() && (state.isFaceSturdy(level, wallPos, face) || Block.canSupportCenter(level, wallPos, face) || state.isSolid());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wallPos = pos.relative(facing.getOpposite());
        return isValidWall(level, wallPos, facing);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockPos wallPos = pos.relative(clickedFace.getOpposite());
            if (isValidWall(level, wallPos, clickedFace)) {
                return this.defaultBlockState()
                        .setValue(FACING, clickedFace)
                        .setValue(LIT, true);
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean newLit = !state.getValue(LIT);
        level.setBlock(pos, state.setValue(LIT, newLit), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, newLit ? 0.8f : 0.6f);
        return InteractionResult.SUCCESS;
    }
}
