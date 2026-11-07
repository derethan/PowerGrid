package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.SolarPanelBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SolarPanelBlock extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);

    private final SolarPanelTier tier;

    public SolarPanelBlock(SolarPanelTier tier, BlockBehaviour.Properties properties) {
        super(properties
                .sound(SoundType.METAL)
                .strength(2.0f, 4.0f)
                .noOcclusion());
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(ACTIVE, false));
    }

    public SolarPanelTier getTier() {
        return this.tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean north = level.getBlockState(pos.north()).is(this);
        boolean south = level.getBlockState(pos.south()).is(this);
        boolean east = level.getBlockState(pos.east()).is(this);
        boolean west = level.getBlockState(pos.west()).is(this);
        boolean active = SolarPanelBlockEntity.isExposedToDaylight(level, pos);

        return this.defaultBlockState()
                .setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(EAST, east)
                .setValue(WEST, west)
                .setValue(ACTIVE, active);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction.getAxis().isHorizontal()) {
            boolean connected = neighborState.is(this);
            state = switch (direction) {
                case NORTH -> state.setValue(NORTH, connected);
                case SOUTH -> state.setValue(SOUTH, connected);
                case EAST -> state.setValue(EAST, connected);
                case WEST -> state.setValue(WEST, connected);
                default -> state;
            };
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SolarPanelBlockEntity be) {
            boolean generating = SolarPanelBlockEntity.isExposedToDaylight(level, pos);
            int energy = be.getEnergyStored();
            int cap = this.tier.getCapacity();
            int genRate = this.tier.getGenerationRate();
            Component status = generating
                    ? Component.literal("Generating (" + genRate + " FE/t)").withStyle(ChatFormatting.GREEN)
                    : Component.literal("Idle (No Daylight)").withStyle(ChatFormatting.YELLOW);
            Component msg = Component.literal("[" + this.tier.getDisplayName() + "] ").withStyle(ChatFormatting.GOLD)
                    .append(status)
                    .append(Component.literal(" | Buffer: " + energy + " / " + cap + " FE").withStyle(ChatFormatting.WHITE));
            player.sendSystemMessage(msg);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return blockEntityType == PowerGridMod.SOLAR_PANEL_BE.get()
                ? (lvl, pos, st, be) -> SolarPanelBlockEntity.serverTick(lvl, pos, st, (SolarPanelBlockEntity) be)
                : null;
    }
}
