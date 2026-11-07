package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.WindmillRotorBlockEntity;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WindmillRotorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<WindmillRotorPart> PART = EnumProperty.create("part", WindmillRotorPart.class);
    public static final BooleanProperty OPERATING = BooleanProperty.create("operating");

    // Axle shapes for center block (8x8 box spanning 16 units through axle axis)
    private static final VoxelShape AXLE_SHAPE_Z = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0);
    private static final VoxelShape AXLE_SHAPE_X = Block.box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0);

    // Blade selection shape for member blocks
    private static final VoxelShape BLADE_SHAPE_Z = Block.box(1.0, 1.0, 2.0, 15.0, 15.0, 14.0);
    private static final VoxelShape BLADE_SHAPE_X = Block.box(2.0, 1.0, 1.0, 14.0, 15.0, 15.0);

    public WindmillRotorBlock(Properties properties) {
        super(properties
                .strength(2.5f, 5.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, WindmillRotorPart.CENTER)
                .setValue(OPERATING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OPERATING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        WindmillRotorPart part = state.getValue(PART);
        boolean isZAxis = (facing.getAxis() == Direction.Axis.Z);

        if (part == WindmillRotorPart.CENTER) {
            return isZAxis ? AXLE_SHAPE_Z : AXLE_SHAPE_X;
        }
        return isZAxis ? BLADE_SHAPE_Z : BLADE_SHAPE_X;
    }

    public static Direction getHorizontalDiameterDirection(Direction facing) {
        return WindmillRotorPart.getHorizontalDiameterDirection(facing);
    }

    public static BlockPos getCenterPos(BlockPos currentPos, BlockState state) {
        if (!state.hasProperty(PART) || !state.hasProperty(FACING)) return currentPos;
        return WindmillRotorPart.getCenterPos(currentPos, state.getValue(FACING), state.getValue(PART));
    }

    public static BlockPos getPartPos(BlockPos centerPos, Direction facing, WindmillRotorPart part) {
        return WindmillRotorPart.getPartPos(centerPos, facing, part);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // Determine orientation
        Direction facing;
        BlockState clickedState = level.getBlockState(clickedPos.relative(clickedFace.getOpposite()));
        if (clickedState.getBlock() instanceof WindmillRotorBlock) {
            facing = clickedState.getValue(FACING);
        } else if (clickedFace.getAxis().isHorizontal()) {
            facing = clickedFace.getAxis() == Direction.Axis.Z ? Direction.NORTH : Direction.EAST;
        } else {
            facing = context.getHorizontalDirection().getAxis() == Direction.Axis.Z ? Direction.NORTH : Direction.EAST;
        }

        // Validate clearance for all 9 blocks of the 3x3 plane
        for (WindmillRotorPart part : WindmillRotorPart.values()) {
            if (part == WindmillRotorPart.CENTER) continue;
            BlockPos partPos = getPartPos(clickedPos, facing, part);
            BlockState stateAt = level.getBlockState(partPos);
            if (!stateAt.canBeReplaced(context)) {
                return null;
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, WindmillRotorPart.CENTER)
                .setValue(OPERATING, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);

            for (WindmillRotorPart part : WindmillRotorPart.values()) {
                if (part == WindmillRotorPart.CENTER) continue;
                BlockPos partPos = getPartPos(pos, facing, part);

                level.setBlock(partPos, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(PART, part)
                        .setValue(OPERATING, false), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos centerPos = getCenterPos(pos, state);
            Direction facing = state.getValue(FACING);

            for (WindmillRotorPart part : WindmillRotorPart.values()) {
                BlockPos targetPos = getPartPos(centerPos, facing, part);
                if (!targetPos.equals(pos)) {
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(this)) {
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 35);
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

        for (WindmillRotorPart part : WindmillRotorPart.values()) {
            BlockPos targetPos = getPartPos(centerPos, facing, part);
            if (!targetPos.equals(pos)) {
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(this)) {
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 35);
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
                for (WindmillRotorPart part : WindmillRotorPart.values()) {
                    BlockPos targetPos = getPartPos(centerPos, nextFacing, part);
                    BlockState atState = level.getBlockState(targetPos);
                    if (!atState.is(this) && !atState.canBeReplaced()) {
                        canRotate = false;
                        break;
                    }
                }

                if (canRotate) {
                    // Clear old positions
                    for (WindmillRotorPart part : WindmillRotorPart.values()) {
                        BlockPos oldPos = getPartPos(centerPos, currentFacing, part);
                        level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 35);
                    }

                    // Place in new orientation
                    for (WindmillRotorPart part : WindmillRotorPart.values()) {
                        BlockPos newPos = getPartPos(centerPos, nextFacing, part);
                        level.setBlock(newPos, this.defaultBlockState()
                                .setValue(FACING, nextFacing)
                                .setValue(PART, part)
                                .setValue(OPERATING, operating), 3);
                    }

                    level.playSound(null, centerPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.2f);
                    player.sendSystemMessage(Component.literal("§6[Power Grid]§r Windmill Rotor axle re-aligned to " + nextFacing.getName().toUpperCase() + "."));
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal("§c[Power Grid]§r Cannot rotate: insufficient clearance in the 3x3 plane."));
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos centerPos = getCenterPos(pos, state);
            BlockEntity be = level.getBlockEntity(centerPos);
            if (be instanceof WindmillRotorBlockEntity rotor) {
                int clearance = rotor.getGroundClearance();
                boolean spinning = rotor.isSpinning();
                int gen = rotor.getFeGeneratedLastTick();
                int energy = rotor.energyStorage.getAmountAsInt();

                if (clearance < 3) {
                    player.sendSystemMessage(Component.literal("§6[Windmill Rotor]§r §cStopped (Requires at least 3 air blocks of ground clearance! Current: " + clearance + " blocks)§r"));
                } else {
                    player.sendSystemMessage(Component.literal("§6[Windmill Rotor]§r §aOperating: " + gen + " FE/t §7(" + clearance + " blocks above ground) | Buffer: " + energy + " FE§r"));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == WindmillRotorPart.CENTER) {
            return new WindmillRotorBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) != WindmillRotorPart.CENTER) {
            return null;
        }
        return (lvl, p, st, be) -> {
            if (be instanceof WindmillRotorBlockEntity rotor) {
                if (lvl.isClientSide()) {
                    rotor.clientTick();
                } else {
                    rotor.serverTick(lvl, p, st);
                }
            }
        };
    }
}
