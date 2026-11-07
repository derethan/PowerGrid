package com.powergrid.compat.refinery;

import com.powergrid.item.EngineerHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class RefineryFacadeCompat {
    public static final String REFINERY_MODID = "refinery";

    public static final Identifier FACADE_BLOCK_COMP_ID = Identifier.fromNamespaceAndPath(REFINERY_MODID, "facade_block");
    public static final Identifier PIPE_FACADE_ID = Identifier.fromNamespaceAndPath(REFINERY_MODID, "pipe_facade");
    public static final Identifier PIPE_FACADE_HOLE_ID = Identifier.fromNamespaceAndPath(REFINERY_MODID, "pipe_facade_hole");
    public static final Identifier CORRUGATED_STEEL_PANEL_ID = Identifier.fromNamespaceAndPath(REFINERY_MODID, "corrugated_steel_panel");

    public static final Map<Direction, VoxelShape> FACADE_SHAPES = new EnumMap<>(Direction.class);

    static {
        FACADE_SHAPES.put(Direction.DOWN,  Block.box(0.0, 0.0,  0.0, 16.0, 2.0,  16.0));
        FACADE_SHAPES.put(Direction.UP,    Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0));
        FACADE_SHAPES.put(Direction.NORTH, Block.box(0.0, 0.0,  0.0, 16.0, 16.0, 2.0));
        FACADE_SHAPES.put(Direction.SOUTH, Block.box(0.0, 0.0,  14.0, 16.0, 16.0, 16.0));
        FACADE_SHAPES.put(Direction.WEST,  Block.box(0.0, 0.0,  0.0, 2.0,  16.0, 16.0));
        FACADE_SHAPES.put(Direction.EAST,  Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    }

    private RefineryFacadeCompat() {}

    public static boolean isRefineryLoaded() {
        return ModList.get() != null && ModList.get().isLoaded(REFINERY_MODID);
    }

    public static boolean isFacadeItem(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        return REFINERY_MODID.equals(id.getNamespace()) &&
                (PIPE_FACADE_ID.getPath().equals(id.getPath()) || PIPE_FACADE_HOLE_ID.getPath().equals(id.getPath()));
    }

    public static boolean isHollowFacade(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && REFINERY_MODID.equals(id.getNamespace()) && PIPE_FACADE_HOLE_ID.getPath().equals(id.getPath());
    }

    public static Block getFacadeBlock(ItemStack stack) {
        if (stack.isEmpty()) return Blocks.STONE;

        DataComponentType<?> compType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(FACADE_BLOCK_COMP_ID);
        if (compType != null) {
            Object val = stack.get(compType);
            if (val instanceof String idStr && !idStr.isEmpty()) {
                Identifier id = Identifier.tryParse(idStr);
                if (id != null) {
                    Block block = BuiltInRegistries.BLOCK.getValue(id);
                    if (block != null && block != Blocks.AIR) {
                        return block;
                    }
                }
            }
        }

        Block fallback = BuiltInRegistries.BLOCK.getValue(CORRUGATED_STEEL_PANEL_ID);
        if (fallback != null && fallback != Blocks.AIR) {
            return fallback;
        }
        return Blocks.STONE;
    }

    public static ItemStack createFacadeStack(Block targetBlock, boolean hollow, int count) {
        Identifier itemId = hollow ? PIPE_FACADE_HOLE_ID : PIPE_FACADE_ID;
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, count);
        DataComponentType<?> compType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(FACADE_BLOCK_COMP_ID);
        if (compType != null && targetBlock != null) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(targetBlock);
            if (id != null) {
                setComponent(stack, compType, id.toString());
            }
        }
        return stack;
    }

    @SuppressWarnings("unchecked")
    private static <T> void setComponent(ItemStack stack, DataComponentType<?> type, Object value) {
        stack.set((DataComponentType<T>) type, (T) value);
    }

    public static boolean isWrenchOrHammer(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof EngineerHammerItem) return true;
        if (stack.is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench")))) return true;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().toLowerCase().contains("wrench");
    }

    public static boolean isWrench(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(ItemTags.create(Identifier.fromNamespaceAndPath("c", "tools/wrench")))) return true;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().toLowerCase().contains("wrench");
    }

    /**
     * Resolves which face of a cable block a player is targeting when placing or removing a facade.
     * Near the outer boundary (> 0.34 from center), picks the outer face. Otherwise falls back to hit normal.
     */
    public static Direction resolveTargetFace(BlockPos pos, BlockHitResult hitResult) {
        double dx = hitResult.getLocation().x - (pos.getX() + 0.5);
        double dy = hitResult.getLocation().y - (pos.getY() + 0.5);
        double dz = hitResult.getLocation().z - (pos.getZ() + 0.5);

        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);

        double max = Math.max(ax, Math.max(ay, az));
        if (max > 0.34) {
            if (max == ax) return dx > 0 ? Direction.EAST : Direction.WEST;
            if (max == ay) return dy > 0 ? Direction.UP : Direction.DOWN;
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return hitResult.getDirection();
    }
}
