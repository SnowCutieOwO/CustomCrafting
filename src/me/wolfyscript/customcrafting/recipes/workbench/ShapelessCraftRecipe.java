package me.wolfyscript.customcrafting.recipes.workbench;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.configs.custom_configs.workbench.CraftConfig;
import me.wolfyscript.customcrafting.items.CustomItem;
import me.wolfyscript.customcrafting.recipes.RecipePriority;
import me.wolfyscript.utilities.api.WolfyUtilities;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

public class ShapelessCraftRecipe extends ShapelessRecipe implements CraftingRecipe {

    private boolean permission;
    private boolean advancedWorkbench;

    private RecipePriority priority;

    private CraftConfig config;
    private String id;
    private CustomItem result;
    private String group;
    private HashMap<Character, ArrayList<CustomItem>> ingredients;
    private WolfyUtilities api;
    private boolean exactMeta;

    public ShapelessCraftRecipe(CraftConfig config) {
        super(new NamespacedKey(config.getFolder(), config.getName()), config.getResult());
        this.result = config.getResult();
        this.id = config.getId();
        this.config = config;
        this.ingredients = config.getIngredients();
        this.permission = config.needPerm();
        this.advancedWorkbench = config.needWorkbench();
        this.group = config.getGroup();
        this.priority = config.getPriority();
        this.api = CustomCrafting.getApi();
        this.exactMeta = config.isExactMeta();
    }

    @Override
    public void load() {
        for (Character itemKey : getIngredients().keySet()) {
            List<CustomItem> items = getIngredients().get(itemKey);
            List<Material> materials = new ArrayList<>();
            items.forEach(itemStack -> materials.add(itemStack.getType()));
            addIngredient(new RecipeChoice.MaterialChoice(materials));
        }
    }

    @Override
    public void save() {

    }

    @Override
    public boolean check(List<List<ItemStack>> matrix) {
        List<Character> allKeys = new ArrayList<>(getIngredients().keySet());
        List<Character> usedKeys = new ArrayList<>();
        for (List<ItemStack> items : matrix) {
            for (ItemStack itemStack : items) {
                if (itemStack == null)
                    continue;
                checkIngredient(allKeys, usedKeys, itemStack);
            }
        }
        return usedKeys.containsAll(getIngredients().keySet());
    }

    private CustomItem checkIngredient(List<Character> allKeys, List<Character> usedKeys, ItemStack item) {
        for (Character key : allKeys) {
            if (!usedKeys.contains(key)) {
                for (CustomItem ingredient : ingredients.get(key)) {
                    if (item.getType().equals(ingredient.getType()) && item.getAmount() >= ingredient.getAmount() && !(exactMeta || ingredient.hasItemMeta()) || ingredient.isSimilar(item)) {
                        usedKeys.add(key);
                        return ingredient.clone();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void removeMatrix(List<List<ItemStack>> matrix, CraftingInventory inventory, boolean small, int totalAmount) {
        List<Character> allKeys = new ArrayList<>(getIngredients().keySet());
        List<Character> usedKeys = new ArrayList<>();
        int passes = small ? 2 : 3;
        int index = 0;
        for (int i = 0; i < inventory.getMatrix().length; i++) {
            if (inventory.getMatrix()[i] != null) {
                index = i;
                break;
            }
        }
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < passes; j++) {
                if (i < matrix.size() && j < matrix.get(i).size()) {
                    ItemStack input = matrix.get(i).get(j);
                    if (input == null)
                        continue;
                    ItemStack result = checkIngredient(allKeys, usedKeys, input);
                    if (result == null)
                        continue;
                    if (result.getMaxStackSize() > 1) {
                        int amount = input.getAmount() - result.getAmount() * totalAmount;
                        input.setAmount(amount);
                    }
                    //TEST FOR BUCKETS AND OTHER ITEMS!?
                    if (input.getAmount() > 0){
                        inventory.setItem(index, input);
                    }else{
                        inventory.setItem(index, new ItemStack(Material.AIR));
                    }
                }
                index++;
                if (index >= inventory.getMatrix().length) {
                    break;
                }
            }
            if (index >= inventory.getMatrix().length) {
                break;
            }
        }
    }

    @Override
    public int getAmountCraftable(List<List<ItemStack>> matrix) {
        List<Character> allKeys = new ArrayList<>(getIngredients().keySet());
        List<Character> usedKeys = new ArrayList<>();
        int totalAmount = -1;
        for (List<ItemStack> items : matrix) {
            for (ItemStack itemStack : items) {
                if (itemStack != null) {
                    ItemStack result = checkIngredient(allKeys, usedKeys, itemStack);
                    if (result != null) {
                        int possible = itemStack.getAmount() / result.getAmount();
                        if (possible < totalAmount || totalAmount == -1)
                            totalAmount = possible;
                    }
                }
            }
        }
        return totalAmount;
    }

    public void setIngredients(HashMap<Character, ArrayList<CustomItem>> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public HashMap<Character, ArrayList<CustomItem>> getIngredients() {
        return ingredients;
    }

    public void setResult(ItemStack result) {
        this.result = new CustomItem(result);
    }

    @Override
    public void setAdvancedWorkbench(boolean advancedWorkbench) {
        this.advancedWorkbench = advancedWorkbench;
    }

    @Override
    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    public CustomItem getCustomResult() {
        return result;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean needsPermission() {
        return permission;
    }

    @Override
    public boolean needsAdvancedWorkbench() {
        return advancedWorkbench;
    }

    @Override
    public boolean isShapeless() {
        return true;
    }

    public CraftConfig getConfig() {
        return config;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public RecipePriority getPriority() {
        return priority;
    }

    @Override
    public boolean isExactMeta() {
        return exactMeta;
    }
}