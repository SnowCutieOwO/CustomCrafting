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
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShapedCraftRecipe extends ShapedRecipe implements CraftingRecipe {

    private boolean permission;
    private boolean advancedWorkbench;
    private boolean exactMeta;

    private CraftConfig config;
    private String id;
    private String group;
    private CustomItem result;
    private HashMap<Character, ArrayList<CustomItem>> ingredients;
    private String[] shape;
    private String shapeLine;
    private RecipePriority priority;
    private WolfyUtilities api;

    public ShapedCraftRecipe(CraftConfig config) {
        super(new NamespacedKey(config.getFolder(), config.getName()), config.getResult());
        this.result = config.getResult();
        this.id = config.getId();
        this.config = config;
        this.shape = WolfyUtilities.formatShape(config.getShape()).toArray(new String[0]);
        this.ingredients = config.getIngredients();
        this.group = config.getGroup();
        this.permission = config.needPerm();
        this.advancedWorkbench = config.needWorkbench();
        this.priority = config.getPriority();
        this.api = CustomCrafting.getApi();
        this.exactMeta = config.isExactMeta();
    }

    @Override
    public void load() {
        this.shape(shape);
        StringBuilder stringBuilder = new StringBuilder();
        for (String row : shape) {
            for (Character letter : row.toCharArray()) {
                if (!letter.equals(' ')) {
                    stringBuilder.append(letter);
                }
            }
        }
        shapeLine = stringBuilder.toString();

        for (Character itemKey : shapeLine.toCharArray()) {
            if (itemKey != ' ') {
                List<CustomItem> items = ingredients.get(itemKey);
                List<Material> materials = new ArrayList<>();
                items.forEach(itemStack -> materials.add(itemStack.getType()));
                setIngredient(itemKey, new RecipeChoice.MaterialChoice(materials));
            }
        }
        if (!this.group.isEmpty()) {
            setGroup(group);
        }
    }

    @Override
    public void save() {

    }

    @Override
    public boolean check(List<List<ItemStack>> matrix) {
        List<Character> containedKeys = new ArrayList<>();
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(i).size(); j++) {
                if ((matrix.get(i).get(j) != null && getShape()[i].charAt(j) != ' ')) {
                    if (checkIngredient(matrix.get(i).get(j), getIngredients().get(getShape()[i].charAt(j))) == null) {
                        return false;
                    } else {
                        containedKeys.add(getShape()[i].charAt(j));
                    }
                } else if (!(matrix.get(i).get(j) == null && getShape()[i].charAt(j) == ' ')) {
                    return false;
                }
            }
        }
        return containedKeys.containsAll(getIngredients().keySet());
    }

    public CustomItem checkIngredient(ItemStack input, List<CustomItem> ingredients) {
        for (CustomItem ingredient : ingredients) {
            if (input.getType().equals(ingredient.getType()) && input.getAmount() >= ingredient.getAmount() && !(exactMeta || ingredient.hasItemMeta()) || ingredient.isSimilar(input)) {
                return ingredient.clone();
            }
        }
        return null;
    }

    @Override
    public void removeMatrix(List<List<ItemStack>> matrix, CraftingInventory inventory, boolean small, int totalAmount) {
        int startIndex = 0;
        for (int i = 0; i < inventory.getMatrix().length; i++) {
            if (inventory.getMatrix()[i] != null) {
                startIndex = i;
                break;
            }
        }
        if (matrix.get(0).size() > 1) {
            for (int i = 0; i < matrix.get(0).size(); i++) {
                if(matrix.get(0).get(i) != null){
                    startIndex = startIndex - i;
                    break;
                }
            }
        }
        int r = 0;
        int c = 0;
        for (int x = startIndex; x < inventory.getMatrix().length; x++) {
            if (r < matrix.size() && c < matrix.get(r).size()) {
                if ((matrix.get(r).get(c) != null && getShape()[r].charAt(c) != ' ')) {
                    ItemStack input = matrix.get(r).get(c);
                    ItemStack item = checkIngredient(input, getIngredients().get(getShape()[r].charAt(c)));
                    if (item != null) {
                        if (item.getMaxStackSize() > 1) {
                            int amount = input.getAmount() - item.getAmount() * totalAmount;
                            input.setAmount(amount);
                        }
                        //TEST FOR BUCKETS AND OTHER ITEMS!?
                        if (input.getAmount() > 0) {
                            inventory.setItem(x, input);
                        }else{
                            inventory.setItem(x, new ItemStack(Material.AIR));
                        }
                    }
                }
            }
            if (r >= matrix.size()) {
                break;
            }
            c++;
            if (c >= matrix.get(r).size()) {
                c = 0;
                r++;
            }
        }
    }

    @Override
    public int getAmountCraftable(List<List<ItemStack>> matrix) {
        int totalAmount = -1;
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(i).size(); j++) {
                if ((matrix.get(i).get(j) != null && getShape()[i].charAt(j) != ' ')) {
                    ItemStack item = checkIngredient(matrix.get(i).get(j), getIngredients().get(getShape()[i].charAt(j)));
                    if (item != null) {
                        int possible = matrix.get(i).get(j).getAmount() / item.getAmount();
                        if (possible < totalAmount || totalAmount == -1)
                            totalAmount = possible;
                    }
                }
            }
        }
        return totalAmount;
    }

    @Override
    public void setPermission(boolean perm) {
        this.permission = perm;
    }

    @Override
    public void setAdvancedWorkbench(boolean workbench) {
        this.advancedWorkbench = workbench;
    }

    @Override
    public void setIngredients(HashMap<Character, ArrayList<CustomItem>> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public HashMap<Character, ArrayList<CustomItem>> getIngredients() {
        return ingredients;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public void setResult(ItemStack result) {
        this.result = new CustomItem(result);
    }

    @Override
    public CustomItem getCustomResult() {
        return result;
    }

    public CraftConfig getConfig() {
        return config;
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
        return false;
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