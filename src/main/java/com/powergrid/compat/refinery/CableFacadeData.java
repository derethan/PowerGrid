package com.powergrid.compat.refinery;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record CableFacadeData(Block block, boolean hollow) {
    public Identifier getBlockId() {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public boolean isValid() {
        return block != null && block != Blocks.AIR;
    }
}
