package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.block.entity.HeavyAccumulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HeavyAccumulatorBlock extends Block implements EntityBlock {

    public HeavyAccumulatorBlock(Properties properties) {
        super(properties.strength(3.5f, 6.0f).sound(SoundType.NETHERITE_BLOCK));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeavyAccumulatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == PowerGridMod.HEAVY_ACCUMULATOR_BE.get()
                ? (lvl, p, st, be) -> ((HeavyAccumulatorBlockEntity) be).serverTick(lvl, p, st)
                : null;
    }
}
