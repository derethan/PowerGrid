package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.StreetLightBlockEntity;
import com.powergrid.item.EngineerHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class StreetLightBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<StreetLightPart> PART = EnumProperty.create("part", StreetLightPart.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public enum StreetLightPart implements StringRepresentable {
        BASE("base"),
        POLE_LOWER("pole_lower"),
        POLE_UPPER("pole_upper"),
        HEAD("head");

        private final String name;

        StreetLightPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getHeightOffset() {
            return switch (this) {
                case BASE -> 0;
                case POLE_LOWER -> 1;
                case POLE_UPPER -> 2;
                case HEAD -> 3;
            };
        }
    }

    // Concrete Footing Base Shape (octagonal/square chamfered pedestal)
    private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    // Metal Pole Shaft Shape (slender centered 4x4 post)
    private static final VoxelShape POLE_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    // North Head: Post + Arm curving to -Z + Cobrahead luminaire
    private static final VoxelShape HEAD_NORTH = Shapes.or(
            Block.box(6.0, 0.0, 6.0, 10.0, 14.0, 10.0),
            Block.box(6.0, 10.0, -4.0, 10.0, 16.0, 8.0),
            Block.box(4.0, 9.0, -8.0, 12.0, 14.0, 0.0)
    );

    // South Head: Post + Arm curving to +Z + Cobrahead luminaire
    private static final VoxelShape HEAD_SOUTH = Shapes.or(
            Block.box(6.0, 0.0, 6.0, 10.0, 14.0, 10.0),
            Block.box(6.0, 10.0, 8.0, 10.0, 16.0, 20.0),
            Block.box(4.0, 9.0, 16.0, 12.0, 14.0, 24.0)
    );

    // West Head: Post + Arm curving to -X + Cobrahead luminaire
    private static final VoxelShape HEAD_WEST = Shapes.or(
            Block.box(6.0, 0.0, 6.0, 10.0, 14.0, 10.0),
            Block.box(-4.0, 10.0, 6.0, 8.0, 16.0, 10.0),
            Block.box(-8.0, 9.0, 4.0, 0.0, 14.0, 12.0)
    );

    // East Head: Post + Arm curving to +X + Cobrahead luminaire
    private static final VoxelShape HEAD_EAST = Shapes.or(
            Block.box(6.0, 0.0, 6.0, 10.0, 14.0, 10.0),
            Block.box(8.0, 10.0, 6.0, 20.0, 16.0, 10.0),
            Block.box(16.0, 9.0, 4.0, 24.0, 14.0, 12.0)
    );

    public StreetLightBlock(Properties properties) {
        super(properties.strength(2.5f, 6.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> {
                    if (state.getValue(PART) == StreetLightPart.HEAD) {
                        return state.getValue(POWERED) ? 15 : 7;
                    }
                    return 0;
                }));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, StreetLightPart.BASE)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        StreetLightPart part = state.getValue(PART);
        if (part == StreetLightPart.BASE) {
            return BASE_SHAPE;
        } else if (part == StreetLightPart.POLE_LOWER || part == StreetLightPart.POLE_UPPER) {
            return POLE_SHAPE;
        }

        return switch (state.getValue(FACING)) {
            case NORTH -> HEAD_NORTH;
            case SOUTH -> HEAD_SOUTH;
            case WEST -> HEAD_WEST;
            case EAST -> HEAD_EAST;
            default -> HEAD_NORTH;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Needs 3 blocks of clearance above for the 4-block total street light assembly
        if (pos.getY() + 3 >= level.getMaxY()) {
            return null;
        }

        for (int i = 1; i <= 3; i++) {
            BlockPos abovePos = pos.above(i);
            if (!level.getBlockState(abovePos).canBeReplaced(context)) {
                return null;
            }
        }

        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, StreetLightPart.BASE)
                .setValue(POWERED, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.above(1), this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, StreetLightPart.POLE_LOWER)
                    .setValue(POWERED, false), 3);
            level.setBlock(pos.above(2), this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, StreetLightPart.POLE_UPPER)
                    .setValue(POWERED, false), 3);
            level.setBlock(pos.above(3), this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, StreetLightPart.HEAD)
                    .setValue(POWERED, false), 3);
        }
    }

    public static BlockPos getBasePos(BlockPos currentPos, StreetLightPart part) {
        return currentPos.below(part.getHeightOffset());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            StreetLightPart part = state.getValue(PART);
            BlockPos basePos = getBasePos(pos, part);

            // Break all other parts cleanly without duplicate item drops
            for (int i = 0; i <= 3; i++) {
                BlockPos targetPos = basePos.above(i);
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
        StreetLightPart part = state.getValue(PART);
        BlockPos basePos = getBasePos(pos, part);

        for (int i = 0; i <= 3; i++) {
            BlockPos targetPos = basePos.above(i);
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
        if (stack.getItem() instanceof EngineerHammerItem) {
            StreetLightPart part = state.getValue(PART);
            BlockPos basePos = getBasePos(pos, part);

            if (player.isShiftKeyDown()) {
                // Shift-right click: Toggle Photocell Dusk-to-Dawn mode
                if (!level.isClientSide()) {
                    BlockEntity be = level.getBlockEntity(basePos);
                    if (be instanceof StreetLightBlockEntity streetLightBE) {
                        streetLightBE.togglePhotocell(player);
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                // Normal right-click: Rotate street light orientation
                if (!level.isClientSide()) {
                    Direction nextFacing = state.getValue(FACING).getClockWise();
                    for (int i = 0; i <= 3; i++) {
                        BlockPos targetPos = basePos.above(i);
                        BlockState targetState = level.getBlockState(targetPos);
                        if (targetState.is(this)) {
                            level.setBlock(targetPos, targetState.setValue(FACING, nextFacing), 3);
                        }
                    }
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.4f, 1.8f);
                    player.sendSystemMessage(Component.literal("§e[Street Light] §7Rotated facing to §6" + nextFacing.getName().toUpperCase() + "§r"));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == StreetLightPart.BASE) {
            return new StreetLightBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == StreetLightPart.BASE && !level.isClientSide()) {
            return (lvl, p, st, be) -> {
                if (be instanceof StreetLightBlockEntity streetLightBE) {
                    streetLightBE.serverTick(lvl, p, st);
                }
            };
        }
        return null;
    }
}
