package me.tuskdev.ban.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Easily create ItemStack, without messing your hands.
 * <i>Note that if you do use this in one of your projects, leave this notice.</i>
 * <i>Please do credit me if you do use this in one of your projects.</i>
 *
 * @author NonameSL
 */
public class ItemBuilder {

    private final ItemStack itemStack;

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param material The material to create the ItemBuilder with.
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item.
     */
    public ItemBuilder(Material material, int amount) {
        itemStack = new ItemStack(material, amount);
    }

    /**
     * Create a new ItemBuilder from scratch.
     *
     * @param material   The material of the item.
     * @param amount     The amount of the item.
     * @param durability The durability of the item.
     */
    public ItemBuilder(Material material, int amount, byte durability) {
        itemStack = new ItemStack(material, amount, durability);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Clone the ItemBuilder into a new one.
     *
     * @return The cloned instance.
     */
    public ItemBuilder clone() {
        return new ItemBuilder(itemStack.clone());
    }

    /**
     * Change the amount of the item.
     *
     * @param amount The durability to set it to.
     */
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Change the durability of the item.
     *
     * @param durability The durability to set it to.
     */
    public ItemBuilder setDurability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    /**
     * Get the displayName of the item.
     */
    public String getName() {
        return itemStack.getItemMeta().getDisplayName();
    }

    /**
     * Set the displayName of the item.
     *
     * @param name The name to change it to.
     */
    public ItemBuilder setName(String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Add an unsafe enchantment.
     *
     * @param enchantment The enchantment to add.
     * @param level The level to put the enchantment on.
     */
    public ItemBuilder addUnsafeEnchantment(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Remove a certain enchant from the item.
     *
     * @param enchantment The enchantment to remove
     */
    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        itemStack.removeEnchantment(enchantment);
        return this;
    }

    /**
     * Set the skull owner for the item. Works on skulls only.
     *
     * @param owner The name of the skull's owner.
     */
    public ItemBuilder setSkullOwner(String owner) {
        if (itemStack.getType() != Material.SKULL && itemStack.getType() != Material.SKULL_ITEM) return this;

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwner(owner);
        itemStack.setItemMeta(skullMeta);
        return this;
    }

    /**
     * Add an enchantment to the item.
     *
     * @param enchantment The enchantment to add
     * @param level       The level
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Add multiple enchants at once.
     *
     * @param enchantments The enchants to add.
     */
    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        itemStack.addEnchantments(enchantments);
        return this;
    }

    /**
     * Sets infinity durability on the item by setting the durability to Short.MAX_VALUE.
     */
    public ItemBuilder setInfinityDurability() {
        itemStack.setDurability(Short.MAX_VALUE);
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(String... lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param line The lore to remove.
     */
    public ItemBuilder removeLoreLine(String line) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(itemMeta.getLore());
        if (!lore.contains(line)) return this;
        lore.remove(line);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param index The index of the lore line to remove.
     */
    public ItemBuilder removeLoreLine(int index) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(itemMeta.getLore());
        if (index < 0 || index > lore.size()) return this;
        lore.remove(index);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     */
    public ItemBuilder addLoreLine(String line) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = (itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>());
        lore.add(line);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line   The lore line to add.
     * @param index  The index of where to put it.
     */
    public ItemBuilder addLoreLine(String line, int index) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(itemMeta.getLore());
        lore.set(index, line);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     *
     * @param color The color to set it to.
     */
    public ItemBuilder setLeatherArmorColor(Color color) {
        try {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(color);
            itemStack.setItemMeta(leatherArmorMeta);
        } catch (ClassCastException ignored) {
        }
        return this;
    }

    /**
     * Retrieves the ItemStack from the ItemBuilder.
     *
     * @return The ItemStack created/modified by the ItemBuilder instance.
     */
    public ItemStack toItemStack() {
        return itemStack;
    }

}