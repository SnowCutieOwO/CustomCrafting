package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.Conditions;
import me.wolfyscript.customcrafting.recipes.types.RecipeType;
import me.wolfyscript.customcrafting.recipes.types.smithing.CustomSmithingRecipe;
import me.wolfyscript.customcrafting.recipes.types.smithing.SmithingData;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.utils.NamespacedKey;
import me.wolfyscript.utilities.api.utils.RandomCollection;
import me.wolfyscript.utilities.api.utils.inventory.InventoryUtils;
import me.wolfyscript.utilities.api.utils.inventory.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SmithingListener implements Listener {

    private final HashMap<UUID, SmithingData> preCraftedRecipes = new HashMap<>();
    private final HashMap<UUID, HashMap<NamespacedKey, CustomItem>> precraftedItems = new HashMap<>();

    private final CustomCrafting customCrafting;

    public SmithingListener(CustomCrafting customCrafting) {
        this.customCrafting = customCrafting;
    }

    @EventHandler
    public void onPrepare(PrepareSmithingEvent event) {
        SmithingInventory inv = event.getInventory();
        Player player = (Player) event.getView().getPlayer();
        ItemStack base = inv.getItem(0);
        ItemStack addition = inv.getItem(1);

        preCraftedRecipes.put(player.getUniqueId(), null);
        for (CustomSmithingRecipe recipe : customCrafting.getRecipeHandler().getAvailableRecipes(RecipeType.SMITHING, player)) {
            if (!recipe.getConditions().checkConditions(recipe, new Conditions.Data(player, event.getInventory().getLocation() != null ? event.getInventory().getLocation().getBlock() : null, event.getView()))) {
                continue;
            }
            Optional<CustomItem> optionalBase = recipe.getBase().stream().filter(customItem -> customItem.isSimilar(base, recipe.isExactMeta())).findFirst();
            if (!optionalBase.isPresent()) {
                continue;
            }
            Optional<CustomItem> optionalAddition = recipe.getAddition().stream().filter(customItem -> customItem.isSimilar(addition, recipe.isExactMeta())).findFirst();
            if (!optionalAddition.isPresent()) {
                continue;
            }
            //Recipe is valid
            preCraftedRecipes.put(player.getUniqueId(), new SmithingData(recipe, optionalBase.get(), optionalAddition.get()));
            assert base != null;
            assert addition != null;

            CustomItem result = null;

            RandomCollection<CustomItem> items = new RandomCollection<>();
            recipe.getResults().stream().filter(cI -> !cI.hasPermission() || player.hasPermission(cI.getPermission())).forEach(cI -> items.add(cI.getRarityPercentage(), cI.clone()));
            HashMap<NamespacedKey, CustomItem> precraftedItem = precraftedItems.getOrDefault(player.getUniqueId(), new HashMap<>());
            if (precraftedItem.get(recipe.getNamespacedKey()) == null) {
                if (!items.isEmpty()) {
                    result = items.next();
                    precraftedItem.put(recipe.getNamespacedKey(), result);
                    precraftedItems.put(player.getUniqueId(), precraftedItem);
                }
            } else {
                result = precraftedItem.get(recipe.getNamespacedKey());
            }

            if (result != null) {
                //Progress result
                ItemStack endResult = result.create();
                endResult.addUnsafeEnchantments(base.getEnchantments());

                event.setResult(endResult);
            }
            break;
        }
    }

    @EventHandler
    public void onTakeOutItem(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().getType().equals(InventoryType.SMITHING)) return;
        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();
        Inventory inventory = event.getClickedInventory();
        if (event.getSlot() == 2 && !ItemUtils.isAirOrNull(inventory.getItem(2)) && action.equals(InventoryAction.NOTHING)) {
            //Take out item!
            if (preCraftedRecipes.get(player.getUniqueId()) == null) {
                //Vanilla Recipe
                return;
            }

            ItemStack resultStack = event.getCurrentItem().clone();
            if (event.isShiftClick()) {
                if (InventoryUtils.hasInventorySpace(player, resultStack)) {
                    player.getInventory().addItem(resultStack);
                }
            } else if (!ItemUtils.isAirOrNull(event.getCursor())) {
                event.setCancelled(true);
                return;
            }else{
                event.getView().setCursor(resultStack);
            }

            final ItemStack baseItem = Objects.requireNonNull(inventory.getItem(0)).clone();
            final ItemStack additionItem = Objects.requireNonNull(inventory.getItem(1)).clone();

            Bukkit.getScheduler().runTask(customCrafting, () -> {
                SmithingData smithingData = preCraftedRecipes.get(player.getUniqueId());
                CustomItem base = smithingData.getBase();
                CustomItem addition = smithingData.getAddition();

                base.consumeItem(baseItem, 1, inventory);
                inventory.setItem(0, baseItem);

                addition.consumeItem(additionItem, 1, inventory);
                inventory.setItem(1, additionItem);

                preCraftedRecipes.remove(player.getUniqueId());
            });
        }


    }

}