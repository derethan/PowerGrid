package com.powergrid.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class AlternatorCoilBlock extends Block {
    public AlternatorCoilBlock(Properties properties) {
        super(properties.strength(2.5f, 5.0f).sound(SoundType.COPPER));
    }
}
