package me.dessie.dessielib.enchantmentapi.properties;

import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.CEnchantmentLoader;
import me.dessie.dessielib.enchantmentapi.utils.AnvilGenerator;
import me.dessie.dessielib.enchantmentapi.utils.EnchantmentGenerator;
import me.dessie.dessielib.enchantmentapi.utils.GrindstoneGenerator;
import me.dessie.dessielib.enchantmentapi.utils.VillagerGenerator;
import me.dessie.dessielib.events.slot.SlotEventHelper;
import me.dessie.dessielib.events.slot.SlotUpdateEvent;
import me.dessie.dessielib.events.slot.UpdateType;
import net.minecraft.server.v1_16_R3.ContainerEnchantTable;
import net.minecraft.server.v1_16_R3.IRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertyListener implements Listener {

    @EventHandler
    public void onVillagerAcquire(VillagerAcquireTradeEvent event) {
        //Got enchanted book, we're gonna modify that :)
        if(event.getRecipe().getResult().getType() == Material.ENCHANTED_BOOK) {
            Enchantment enchantment;
            do {
                enchantment = VillagerGenerator.getRandomEnchantment();

            } while(CEnchantment.getByKey(enchantment.getKey()) != null && !CEnchantment.getByKey(enchantment.getKey()).getEnchantProperties().canBeVillagerTrade());

            int level = VillagerGenerator.getRandomLevel(enchantment);
            int cost = VillagerGenerator.getRandomCost(level, enchantment);

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);

            if(CEnchantment.getByKey(enchantment.getKey()) != null) {
                CEnchantment.getByKey(enchantment.getKey()).enchant(book, level);
            } else {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
                meta.addStoredEnchant(enchantment, level, false);
                book.setItemMeta(meta);
            }

            MerchantRecipe recipe = new MerchantRecipe(book, event.getRecipe().getMaxUses());
            recipe.setExperienceReward(event.getRecipe().hasExperienceReward());
            recipe.setMaxUses(event.getRecipe().getMaxUses());
            recipe.setVillagerExperience(event.getRecipe().getVillagerExperience());
            recipe.setPriceMultiplier(event.getRecipe().getPriceMultiplier());
            recipe.addIngredient(new ItemStack(Material.EMERALD, cost));

            event.setRecipe(recipe);
        }
    }

    @EventHandler
    public void onGrindstone(SlotUpdateEvent event) {
        //Ignore when the result is generated.
        if(event.getUpdateType() == UpdateType.RESULT_GENERATE) return;
        if(event.getInventory().getType() != InventoryType.GRINDSTONE) return;

        //Get the target
        ItemStack target = null;
        if(event.getSlot() == 0 || (event.getSlot() == 1 && SlotEventHelper.isNullOrAir(event.getInventory().getItem(0)))) {
            target = event.getNewItem();
        } else if(event.getSlot() == 1) {
            target = event.getInventory().getItem(0);
        }

        if(event.getSlot() != 2) {
            ItemStack finalTarget = target;
            //Run a tick later, since sometimes the result could be gotten as null.
            Bukkit.getScheduler().runTask(CEnchantmentLoader.getPlugin(), () -> {
                GrindstoneGenerator.doResultUpdate(event.getInventory().getItem(2), finalTarget);
            });
        } else {
            target = SlotEventHelper.isNullOrAir(event.getOldInventory().getItem(0))
                    ? event.getOldInventory().getItem(1)
                    : event.getOldInventory().getItem(0);

            ItemStack other = !SlotEventHelper.isNullOrAir(event.getOldInventory().getItem(0))
                    ? event.getOldInventory().getItem(1)
                    : null;

            //They removed the item, so drop the experience.
            ItemStack result = event.getOldItem();

            GrindstoneGenerator.dropExp(GrindstoneGenerator.getDropExp(target, other, result), event.getInventory().getLocation());
        }
    }

    @EventHandler
    public void onAnvil(SlotUpdateEvent event) {
        if(event.getInventory().getType() != InventoryType.ANVIL) return;
        //Ignore updating of the result slot
        if(event.getSlot() == 2) return;

        ItemStack result = event.getInventory().getItem(2);
        ItemStack target = event.getInventory().getItem(0);
        ItemStack sacrifice = event.getInventory().getItem(1);
        if(!SlotEventHelper.isNullOrAir(result)) {
            Set<CEnchantment> allEnchantments = AnvilGenerator.getEnchantmentsToAdd(event.getPlayer(), target, sacrifice);

            //Remove the conflicting vanilla enchantments
            AnvilGenerator.removeConflictingVanilla(target, sacrifice, result);

            //Clear all the current enchantments.
            for(CEnchantment enchantment : allEnchantments) {
                enchantment.removeEnchantment(result, false);
            }

            //Enchant the result item.
            int cost = AnvilGenerator.doAnvilEnchant(event.getPlayer(), allEnchantments, target, sacrifice, result);

            //Add the cost to the current cost.
            Bukkit.getScheduler().runTask(CEnchantmentLoader.getPlugin(), () -> {
                AnvilInventory inventory = (AnvilInventory) event.getInventory();
                inventory.setRepairCost(inventory.getRepairCost() + cost);
            });
        } else if(!SlotEventHelper.isNullOrAir(target) && !SlotEventHelper.isNullOrAir(sacrifice)) {
            if(target.getType() != sacrifice.getType() && sacrifice.getType() != Material.ENCHANTED_BOOK) return;

            //Set the result as a clone of the target, since there's not one in this case.
            result = target.clone();

            //Gets the CEnchantments to add to the target.
            Set<CEnchantment> allEnchantments = AnvilGenerator.getEnchantmentsToAdd(event.getPlayer(), target, sacrifice);

            //Remove the conflicting vanilla enchantments
            AnvilGenerator.removeConflictingVanilla(target, sacrifice, result);

            //Remove all current enchantments
            for(CEnchantment enchantment : allEnchantments) {
                enchantment.removeEnchantment(result, false);
            }

            //Enchant and set the result.
            int cost = AnvilGenerator.doAnvilEnchant(event.getPlayer(), allEnchantments, target, sacrifice, result);
            event.getInventory().setItem(2, result);

            //Add the cost to the current cost.
            Bukkit.getScheduler().runTask(CEnchantmentLoader.getPlugin(), () -> {
                AnvilInventory inventory = (AnvilInventory) event.getInventory();
                inventory.setRepairCost(inventory.getRepairCost() + cost);
            });
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {


        //Get which enchantment they clicked, we don't want to remove the enchantment that they got guaranteed.
        //This is just done by accessing the EnchantmentTable's offers through NMS and grabbing the enchantment ID.
        ContainerEnchantTable tableContainer = ((ContainerEnchantTable) ((CraftInventoryView) event.getView()).getHandle());
        Enchantment clicked = null;
        for(int i = 0; i < 3; i++) {
            if(tableContainer.costs[i] == event.getExpLevelCost()) {
                int id = tableContainer.enchantments[i];
                clicked = id >= 0 ? Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(IRegistry.ENCHANTMENT.getKey(IRegistry.ENCHANTMENT.fromId(id)))) : null;
            }
        }
        if(clicked == null) return;
        Enchantment finalClicked = clicked;

        //Remove all enchantments except the guaranteed one
        event.getEnchantsToAdd().keySet().removeIf(ench -> ench != finalClicked);

        //Get modified power.
        int modifiedPower = EnchantmentGenerator.getModifiedPower(event.getItem().getType(), event.getExpLevelCost());

        //Get all possible enchantments for this item
        Map<Enchantment, Integer> possibleEnchantments = EnchantmentGenerator.getPossibleEnchantments(event.getItem(), clicked, modifiedPower);

        //Choose enchantments and then apply them
        List<Enchantment> chosen = EnchantmentGenerator.chooseEnchantments(new ArrayList<>(possibleEnchantments.keySet()), clicked, modifiedPower);
        for(Enchantment enchantment : chosen) {
            if(enchantment instanceof CEnchantment) {
                ((CEnchantment) enchantment).enchant(event.getItem(), possibleEnchantments.get(enchantment));
            } else {
                int level = !possibleEnchantments.containsKey(enchantment) ? event.getEnchantsToAdd().get(enchantment) : possibleEnchantments.get(enchantment);
                event.getEnchantsToAdd().put(enchantment, level);
            }
        }
    }
}
