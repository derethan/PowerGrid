package com.powergrid.guide;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class GuideEntry {
    private final String id;
    private final String title;
    private final String subtitle;
    private final GuideCategory category;
    private final Supplier<ItemStack> iconSupplier;
    private final String description;
    private final String proTips;
    private final @Nullable List<GuideBlockRequirement> requirements;
    private final @Nullable List<GuideLayer> layers;
    private final List<GuideRecipe> recipes;

    public GuideEntry(
            String id,
            String title,
            String subtitle,
            GuideCategory category,
            Supplier<ItemStack> iconSupplier,
            String description,
            String proTips,
            @Nullable List<GuideBlockRequirement> requirements,
            @Nullable List<GuideLayer> layers,
            List<GuideRecipe> recipes
    ) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.category = category;
        this.iconSupplier = iconSupplier;
        this.description = description;
        this.proTips = proTips;
        this.requirements = requirements;
        this.layers = layers;
        this.recipes = recipes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public GuideCategory getCategory() {
        return category;
    }

    public ItemStack getIcon() {
        return iconSupplier != null ? iconSupplier.get() : ItemStack.EMPTY;
    }

    public String getDescription() {
        return description;
    }

    public String getProTips() {
        return proTips;
    }

    public boolean isMultiblock() {
        return (requirements != null && !requirements.isEmpty()) || (layers != null && !layers.isEmpty());
    }

    public @Nullable List<GuideBlockRequirement> getRequirements() {
        return requirements;
    }

    public int getTotalRequiredBlocks() {
        if (requirements == null) return 0;
        int sum = 0;
        for (GuideBlockRequirement req : requirements) {
            sum += req.count();
        }
        return sum;
    }

    public @Nullable List<GuideLayer> getLayers() {
        return layers;
    }

    public List<GuideRecipe> getRecipes() {
        return recipes;
    }
}
