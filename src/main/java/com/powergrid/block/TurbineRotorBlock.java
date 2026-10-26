package com.powergrid.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class TurbineRotorBlock extends Block {
    public TurbineRotorBlock(Properties properties) {
        super(properties.strength(3.0f, 6.0f).sound(SoundType.NETHERITE_BLOCK));
    }
}
