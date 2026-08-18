/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.EnumSet;
import java.util.Set;

/** Stable physical panel occupancy and restocker type for one factory gauge. */
public final class FactoryGaugeBlockEntityData extends MCABlockEntity {

    @NBTName("top_left")
    private PanelData topLeft;

    @NBTName("top_right")
    private PanelData topRight;

    @NBTName("bottom_left")
    private PanelData bottomLeft;

    @NBTName("bottom_right")
    private PanelData bottomRight;

    @NBTName("Restocker")
    private boolean restocker;

    public FactoryGaugeBlockEntityData() {
    }

    Set<FactoryGaugeRenderPlan.Slot> activeSlots() {
        EnumSet<FactoryGaugeRenderPlan.Slot> active =
                EnumSet.noneOf(FactoryGaugeRenderPlan.Slot.class);
        add(active, FactoryGaugeRenderPlan.Slot.TOP_LEFT, topLeft);
        add(active, FactoryGaugeRenderPlan.Slot.TOP_RIGHT, topRight);
        add(active, FactoryGaugeRenderPlan.Slot.BOTTOM_LEFT, bottomLeft);
        add(active, FactoryGaugeRenderPlan.Slot.BOTTOM_RIGHT, bottomRight);
        return active;
    }

    boolean restocker() {
        return restocker;
    }

    private static void add(
            Set<FactoryGaugeRenderPlan.Slot> target,
            FactoryGaugeRenderPlan.Slot slot,
            PanelData data
    ) {
        if (data != null && data.active()) {
            target.add(slot);
        }
    }

    /** Presence marker for Create's behaviour-owned per-slot compound. */
    public static final class PanelData {

        @NBTName("RecipeAddress")
        private String recipeAddress;

        public PanelData() {
        }

        boolean active() {
            // Every non-empty panel written by exact Create includes this key,
            // even when its value is the empty string.
            return recipeAddress != null;
        }
    }
}
