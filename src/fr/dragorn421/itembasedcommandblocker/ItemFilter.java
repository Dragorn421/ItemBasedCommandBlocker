package fr.dragorn421.itembasedcommandblocker;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemFilter
{

	final private Material type;
	final private short data;
	final private Set<Enchantment> neededEnchantments;
	final private Set<Enchantment> forbiddenEnchantments;

	public ItemFilter(final Material type, final short data, final Set<Enchantment> neededEnchantments, final Set<Enchantment> forbiddenEnchantments)
	{
		this.type = type;
		this.data = data<0?-1:data;
		this.neededEnchantments = neededEnchantments;
		this.forbiddenEnchantments = forbiddenEnchantments;
		//System.out.println(this.toString());
	}

	public boolean matchesItem(final ItemStack is)
	{
/*		if(is != null)
		{
			System.out.println(is);
			System.out.println("=type "+(this.type == null || this.type == is.getType()));
			System.out.println("=data "+(this.data == -1 || this.data == is.getDurability()));
			for(final Enchantment ench : is.getEnchantments().keySet())
				if((!this.neededEnchantments.isEmpty() && !this.neededEnchantments.contains(ench)) || this.forbiddenEnchantments.contains(ench))
					System.out.println("ench "+ench+" "+true);
		}//*/
		if(	is != null
			&& (this.type == null || this.type == is.getType())
			&& (this.data == -1 || this.data == is.getDurability()))
		{
			final Set<Enchantment> enchantments = is.getEnchantments().keySet();
			if(!enchantments.containsAll(this.neededEnchantments))
				return false;
			if(!Collections.disjoint(enchantments, this.forbiddenEnchantments))
				return false;
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
		final Set<Enchantment> neededEnchantments = ItemFilter.convertEnchantments(cs.getStringList("with-enchantments"));
		final Set<Enchantment> forbiddenEnchantments = ItemFilter.convertEnchantments(cs.getStringList("without-enchantments"));
		return new ItemFilter(type, data, neededEnchantments, forbiddenEnchantments);
	}

	static public Set<Enchantment> convertEnchantments(final List<String> enchantmentsStr)
	{
		final Set<Enchantment> enchantments = new HashSet<>();
		if(enchantmentsStr == null)
			return enchantments;
		for(final String enchStr : enchantmentsStr)
		{
			final Enchantment ench = Enchantment.getByName(enchStr);
			if(ench == null)
				{}//TODO ench not found/mistype
			else
				enchantments.add(ench);
		}
		return enchantments;
	}

}
