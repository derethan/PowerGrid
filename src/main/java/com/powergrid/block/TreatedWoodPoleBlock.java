package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TreatedWoodPoleBlock extends Block {
    private static final VoxelShape POLE_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public TreatedWoodPoleBlock(Properties properties) {
        super(properties.strength(2.0f, 3.0f).sound(SoundType.WOOD));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return POLE_SHAPE;
    }
}
