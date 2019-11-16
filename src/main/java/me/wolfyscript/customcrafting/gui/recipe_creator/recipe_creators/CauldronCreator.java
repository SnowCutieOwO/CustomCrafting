package me.wolfyscript.customcrafting.gui.recipe_creator.recipe_creators;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.PlayerCache;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.recipe_creator.buttons.CauldronContainerButton;
import me.wolfyscript.customcrafting.recipes.types.cauldron.CauldronConfig;
import me.wolfyscript.customcrafting.recipes.types.cauldron.CauldronRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.inventory.GuiHandler;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonActionRender;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CauldronCreator extends ExtendedGuiWindow {

    public CauldronCreator(InventoryAPI inventoryAPI) {
        super("cauldron", inventoryAPI, 45);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton("back", new ButtonState("none", "back", WolfyUtilities.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0="), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.openCluster("none");
            return true;
        })));
        registerButton(new ActionButton("save", new ButtonState("recipe_creator","save", Material.WRITABLE_BOOK, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            PlayerCache cache = CustomCrafting.getPlayerCache(player);
            if (validToSave(cache)) {
                openChat(guiHandler, "$msg.gui.none.recipe_creator.save.input$", (guiHandler1, player1, s, args) -> {
                    PlayerCache cache1 = CustomCrafting.getPlayerCache(player1);
                    CauldronConfig config = cache1.getCauldronConfig();
                    if (args.length > 1) {
                        if (!config.saveConfig(args[0], args[1], player1)) {
                            return true;
                        }
                        try {
                            Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> {
                                CustomCrafting.getRecipeHandler().injectRecipe(new CauldronRecipe(config));
                                api.sendPlayerMessage(player, "$msg.gui.none.recipe_creator.loading.success$");
                            }, 1);
                        } catch (Exception ex) {
                            api.sendPlayerMessage(player, "$msg.gui.none.recipe_creator.error_loading$", new String[]{"%REC%", config.getId()});
                            ex.printStackTrace();
                            return false;
                        }
                        Bukkit.getScheduler().runTaskLater(CustomCrafting.getInst(), () -> guiHandler.changeToInv("main_menu"), 1);
                    }
                    return false;
                });
            } else {
                api.sendPlayerMessage(player, "$msg.gui.none.recipe_creator.save.empty$");
            }
            return false;
        })));

        registerButton(new DummyButton("cauldron", new ButtonState("cauldron", Material.CAULDRON)));

        registerButton(new CauldronContainerButton(0));
        registerButton(new CauldronContainerButton(1));
        registerButton(new ItemInputButton("handItem_container", new ButtonState("handItem_container", Material.AIR, new ButtonActionRender() {
            @Override
            public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int slot, InventoryClickEvent event) {
                if (event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                    Bukkit.getScheduler().runTask(CustomCrafting.getInst(), () -> {
                        if (inventory.getItem(slot) != null && !inventory.getItem(slot).getType().equals(Material.AIR)) {
                            CustomCrafting.getPlayerCache(player).getItems().setItem("single", CustomItem.getByItemStack(inventory.getItem(slot)));
                            guiHandler.changeToInv("none","item_editor");
                        }
                    });
                    return true;
                }
                Bukkit.getScheduler().runTask(CustomCrafting.getInst(), () -> CustomCrafting.getPlayerCache(player).getCauldronConfig().setHandItem(inventory.getItem(slot) != null && !inventory.getItem(slot).getType().equals(Material.AIR) ? CustomItem.getByItemStack(inventory.getItem(slot)) : new CustomItem(Material.AIR)));
                return false;
            }

            @Override
            public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int i, boolean b) {
                CustomItem customItem = CustomCrafting.getPlayerCache(player).getCauldronConfig().getHandItem();
                if(customItem != null){
                    return customItem.getItemStack();
                }
                return new ItemStack(Material.AIR);
            }
        })));


        registerButton(new ToggleButton("dropItems", new ButtonState("dropItems.enabled", Material.DROPPER, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setDropItems(false);
            return true;
        }), new ButtonState("dropItems.disabled", Material.CHEST, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setDropItems(true);
            return true;
        })));
        registerButton(new ToggleButton("fire", new ButtonState("fire.enabled", Material.FLINT_AND_STEEL, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setFire(false);
            return true;
        }), new ButtonState("fire.disabled", Material.FLINT, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setFire(true);
            return true;
        })));
        registerButton(new ToggleButton("water", new ButtonState("water.enabled", Material.WATER_BUCKET, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setWater(false);
            return true;
        }), new ButtonState("water.disabled", Material.BUCKET, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setWater(true);
            return true;
        })));
        registerButton(new ChatInputButton("xp", new ButtonState("xp", Material.EXPERIENCE_BOTTLE, (hashMap, guiHandler, player, itemStack, slot, help) -> {
            hashMap.put("%xp%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getXP());
            return itemStack;
        }), "$msg.gui.none.recipe_creator.cauldron.xp$", (guiHandler, player, s, args) -> {
            float xp;
            try {
                xp = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                api.sendPlayerMessage(player, "$msg.gui.recipe_creator.valid_number$");
                return true;
            }
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setXP(xp);
            return false;
        }));
        registerButton(new ChatInputButton("cookingTime", new ButtonState("cookingTime", Material.CLOCK, (hashMap, guiHandler, player, itemStack, slot, help) -> {
            hashMap.put("%time%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getCookingTime());
            return itemStack;
        }), "$msg.gui.none.recipe_creator.cauldron.cookingTime$", (guiHandler, player, s, args) -> {
            int time;
            try {
                time = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                api.sendPlayerMessage(player, "$msg.gui.recipe_creator.valid_number$");
                return true;
            }
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setCookingTime(time);
            return false;
        }));
        registerButton(new ChatInputButton("waterLevel", new ButtonState("waterLevel", Material.GLASS_BOTTLE, (hashMap, guiHandler, player, itemStack, slot, help) -> {
            hashMap.put("%level%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getWaterLevel());
            return itemStack;
        }), "$msg.gui.none.recipe_creator.cauldron.waterLevel", (guiHandler, player, s, args) -> {
            int waterLvl;
            try {
                waterLvl = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                api.sendPlayerMessage(player, "$msg.gui.recipe_creator.valid_number$");
                return true;
            }
            if(waterLvl > 3){
                waterLvl = 3;
            }
            CustomCrafting.getPlayerCache(player).getCauldronConfig().setWaterLevel(waterLvl);
            return false;
        }));

        if (WolfyUtilities.hasMythicMobs()) {
            registerButton(new ActionButton("mythicMob", new ButtonState("mythicMob", Material.WITHER_SKELETON_SKULL, new ButtonActionRender() {
                @Override
                public boolean run(GuiHandler guiHandler, Player player, Inventory inventory, int i, InventoryClickEvent event) {
                    if (event.getClick().isLeftClick()) {
                        openChat(guiHandler, "$msg.gui.none.recipe_creator.cauldron.mythicMob", (guiHandler1, player1, s, args) -> {
                            if (args.length > 1) {
                                CauldronConfig config = CustomCrafting.getPlayerCache(player).getCauldronConfig();
                                MythicMob mythicMob = MythicMobs.inst().getMobManager().getMythicMob(args[0]);
                                if (mythicMob != null) {
                                    int level = 1;
                                    try {
                                        level = Integer.parseInt(args[1]);
                                    } catch (NumberFormatException e) {
                                        api.sendPlayerMessage(player, "$msg.gui.recipe_creator.valid_number$");
                                        return true;
                                    }
                                    double modX = config.getMythicMobMod().getX();
                                    double modY = config.getMythicMobMod().getY();
                                    double modZ = config.getMythicMobMod().getZ();
                                    if (args.length >= 5) {
                                        try {
                                            modX = Double.parseDouble(args[2]);
                                            modY = Double.parseDouble(args[3]);
                                            modZ = Double.parseDouble(args[4]);
                                        } catch (NumberFormatException e) {
                                            api.sendPlayerMessage(player, "$msg.gui.recipe_creator.valid_number$");
                                            return true;
                                        }
                                    }
                                    config.setMythicMob(args[0], level, modX, modY, modZ);
                                    return false;
                                }
                            }
                            return true;
                        });
                    } else {
                        CustomCrafting.getPlayerCache(player).getCauldronConfig().setMythicMob("<none>", 0, 0, 0.5, 0);
                    }
                    return true;
                }

                @Override
                public ItemStack render(HashMap<String, Object> hashMap, GuiHandler guiHandler, Player player, ItemStack itemStack, int i, boolean b) {
                    hashMap.put("%mob.name%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getMythicMobName());
                    hashMap.put("%mob.level%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getMythicMobLevel());
                    hashMap.put("%mob.modX%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getMythicMobMod().getX());
                    hashMap.put("%mob.modY%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getMythicMobMod().getY());
                    hashMap.put("%mob.modZ%", CustomCrafting.getPlayerCache(player).getCauldronConfig().getMythicMobMod().getZ());
                    return itemStack;
                }
            })));
        }
    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            event.setButton(0, "back");
            PlayerCache cache = CustomCrafting.getPlayerCache(event.getPlayer());
            CauldronConfig cauldronConfig = cache.getCauldronConfig();
            ((ToggleButton) event.getGuiWindow().getButton("fire")).setState(event.getGuiHandler(), cauldronConfig.needsFire());
            ((ToggleButton) event.getGuiWindow().getButton("water")).setState(event.getGuiHandler(), cauldronConfig.isWater());
            ((ToggleButton) event.getGuiWindow().getButton("dropItems")).setState(event.getGuiHandler(), cauldronConfig.dropItems());

            event.setButton(11, "cauldron.container_0");

            event.setButton(13, "cookingTime");

            event.setButton(19, "water");
            event.setButton(20, "cauldron");
            event.setButton(21, "waterLevel");

            event.setButton(23, "xp");
            event.setButton(25, "cauldron.container_1");

            event.setButton(29, "fire");
            event.setButton(34, "dropItems");

            if(!cache.getCauldronConfig().dropItems()){
                event.setButton(35, "handItem_container");
            }
            if (WolfyUtilities.hasMythicMobs()){
                event.setButton(13, "mythicMob");
            }

            event.setButton(44, "save");
        }
    }

    private boolean validToSave(PlayerCache cache) {
        CauldronConfig config = cache.getCauldronConfig();
        return config.getIngredients() != null && !config.getIngredients().isEmpty() && ((config.getResult() != null && !config.getResult().isEmpty()) || (WolfyUtilities.hasMythicMobs() && !config.getMythicMobName().equals("<none>")));
    }
}