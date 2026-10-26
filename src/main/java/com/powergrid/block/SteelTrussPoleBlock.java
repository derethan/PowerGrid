package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SteelTrussPoleBlock extends Block {
    private static final VoxelShape TRUSS_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public SteelTrussPoleBlock(Properties properties) {
        super(properties.strength(4.5f, 8.0f).sound(SoundType.NETHERITE_BLOCK));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TRUSS_SHAPE;
    }
}
