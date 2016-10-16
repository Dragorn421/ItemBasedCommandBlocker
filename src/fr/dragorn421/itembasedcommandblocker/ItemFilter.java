package fr.dragorn421.itembasedcommandblocker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemFilter
{

	final private Material type;
	final private short data;
	final private Map<Enchantment, Integer> neededEnchantments;
	final private Map<Enchantment, Integer> forbiddenEnchantments;

	public ItemFilter(final Material type, final short data, final Map<Enchantment, Integer> neededEnchantments, final Map<Enchantment, Integer> forbiddenEnchantments)
	{
		this.type = type;
		this.data = data<0?-1:data;
		this.neededEnchantments = neededEnchantments;
		this.forbiddenEnchantments = forbiddenEnchantments;
		//System.out.println(this.toString());
	}

	public boolean matchesItem(final ItemStack is)
	{
		if(	is != null
			&& (this.type == null || this.type == is.getType())
			&& (this.data == -1 || this.data == is.getDurability()))
		{
			final Map<Enchantment, Integer> enchantments = is.getEnchantments();
			for(final Entry<Enchantment, Integer> needed : this.neededEnchantments.entrySet())
			{
				final Integer level = enchantments.get(needed.getKey());
				// if the item does not have the enchantment he needs
				if(level == null)
					return false;
				// if the enchantment needs to be a certain level and not matched
				if(needed.getValue() != 0 && level != needed.getValue())
					return false;
			}
			for(final Entry<Enchantment, Integer> ench : enchantments.entrySet())
			{
				final Integer forbiddenLevel = this.forbiddenEnchantments.get(ench.getKey());
				// if not forbidden at all
				if(forbiddenLevel == null)
					continue;
				// if forbidden for all levels
				if(forbiddenLevel == 0)
					return false;
				// if level is forbidden
				if(forbiddenLevel == ench.getValue())
					return false;
			}
			return true;
		}
		return false;
	}

	public ItemStack matchesItems(final ItemStack items[])
	{
		for(final ItemStack is : items)
			if(matchesItem(is))
				return is;
		return null;
	}

	@Override
	public String toString()
	{
		return "ItemFilter{type="+this.type+",data="+this.data+",neededEnchantments="+this.neededEnchantments.toString()+",forbiddenEnchantments="+this.forbiddenEnchantments.toString()+"}";
	}

	static public ItemFilter fromConfig(final ConfigurationSection cs)
	{
		final String typeStr = cs.getString("type", null);
		final Material type;
		if(typeStr == null)
			type = null;
		else
			type = Material.matchMaterial(typeStr);
		final short data = (short) cs.getInt("data", -1);
		final Map<Enchantment, Integer> neededEnchantments = ItemFilter.convertEnchantments(cs.getStringList("with-enchantments"));
		final Map<Enchantment, Integer> forbiddenEnchantments = ItemFilter.convertEnchantments(cs.getStringList("without-enchantments"));
		return new ItemFilter(type, data, neededEnchantments, forbiddenEnchantments);
	}

	static public Map<Enchantment, Integer> convertEnchantments(final List<String> enchantmentsStr)
	{
		final Map<Enchantment, Integer> enchantments = new HashMap<>();
		if(enchantmentsStr == null)
			return enchantments;
		for(final String enchStr : enchantmentsStr)
		{
			final String[] args = enchStr.split(":", 2);
			final Enchantment ench = Enchantment.getByName(args[0]);
			if(ench == null)
				{}//TODO ench not found/mistype
			else
			{
				Integer level = 0;
				if(args.length != 1)
				{
					try {
						level = Integer.parseInt(args[1]);
						if(level <= 0)
							level = 0;
					} catch(final NumberFormatException e) {
						//TODO invalid ench level number
					}
				}
				enchantments.put(ench, level);
			}
		}
		return enchantments;
	}

}
