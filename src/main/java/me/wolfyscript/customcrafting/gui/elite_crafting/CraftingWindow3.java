package me.wolfyscript.customcrafting.gui.elite_crafting;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.CCCache;
import me.wolfyscript.customcrafting.data.cache.EliteWorkbench;
import me.wolfyscript.customcrafting.gui.EliteCraftingCluster;
import me.wolfyscript.customcrafting.gui.elite_crafting.buttons.CraftingSlotButton;
import me.wolfyscript.utilities.api.inventory.gui.GuiCluster;
import me.wolfyscript.utilities.api.inventory.gui.GuiUpdate;
import me.wolfyscript.utilities.api.inventory.gui.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.gui.button.buttons.DummyButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CraftingWindow3 extends CraftingWindow {

    public CraftingWindow3(GuiCluster<CCCache> cluster, CustomCrafting customCrafting) {
        super(cluster, "crafting_grid3", 27, customCrafting);
    }

    @Override
    public void onInit() {
        super.onInit();
        for (int i = 0; i < 9; i++) {
            registerButton(new CraftingSlotButton(i, customCrafting));
        }
        registerButton(new DummyButton<>("texture_dark", new ButtonState<>("none", "background", Material.BLACK_STAINED_GLASS_PANE)));
        registerButton(new DummyButton<>("texture_light", new ButtonState<>("none", "background", Material.BLACK_STAINED_GLASS_PANE)));
    }

    @Override
    public void onUpdateSync(GuiUpdate<CCCache> event) {
        super.onUpdateSync(event);

        CCCache cache = event.getGuiHandler().getCustomCache();
        EliteWorkbench eliteWorkbench = cache.getEliteWorkbench();
        if (eliteWorkbench.getContents() == null || eliteWorkbench.getCurrentGridSize() <= 0) {
            eliteWorkbench.setCurrentGridSize((byte) 3);
            eliteWorkbench.setContents(new ItemStack[9]);
        }

        event.setButton(9, EliteCraftingCluster.RECIPE_BOOK);
        int slot;
        for (int i = 0; i < 9; i++) {
            slot = getGridX() + i + (i / 3) * 6;
            event.setButton(slot, "crafting.slot_" + i);
        }
        event.setButton(16, RESULT);
    }

    @Override
    public int getGridX() {
        return 2;
    }

}
