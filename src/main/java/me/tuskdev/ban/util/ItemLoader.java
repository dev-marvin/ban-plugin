package me.tuskdev.ban.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ItemLoader {

    public static ItemStack load(ConfigurationSection configurationSection, Map<String, Object> variables) {
        ItemBuilder itemBuilder = new ItemBuilder(
                Material.getMaterial(configurationSection.getString("material", "STONE").toUpperCase()),
                configurationSection.getInt("amount", 1),
                (byte) configurationSection.getInt("data", 0)
        );

        if (configurationSection.contains("name")) {
            itemBuilder.setName(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("name")));
            variables.forEach((key, value) -> itemBuilder.setName(itemBuilder.getName().replace("{" + key + "}", value.toString())));
        }

        if (configurationSection.contains("lore")) {
            List<String> lore = configurationSection.getStringList("lore");

            lore.replaceAll(line -> {
                String output = ChatColor.translateAlternateColorCodes('&', line);

                for (Map.Entry<String, Object> stringObjectEntry : variables.entrySet()) {
                    output = output.replace("{" + stringObjectEntry.getKey() + "}", stringObjectEntry.getValue().toString());
                }

                return output;
            });

            itemBuilder.setLore(lore);
        }

        if (configurationSection.contains("enchants")) {
            ConfigurationSection enchants = configurationSection.getConfigurationSection("enchants");
            enchants.getKeys(false).forEach(key -> itemBuilder.addEnchant(Enchantment.getByName(key.toUpperCase()), enchants.getInt(key)));
        }

        return itemBuilder.toItemStack();
    }

}
