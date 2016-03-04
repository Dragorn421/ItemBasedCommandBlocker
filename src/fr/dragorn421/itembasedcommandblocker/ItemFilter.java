package fr.dragorn421.itembasedcommandblocker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemFilter
{

	final private Material type;
	final private short data;
	final private FilterList<Enchantment> enchantments;

	public ItemFilter(final Material type, final short data, final FilterList<Enchantment> enchantments)
	{
		this.type = type;
		this.data = data<0?-1:data;
		this.enchantments = enchantments;
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
				if(!this.enchantments.isAllowed(ench))
					System.out.println("ench "+true);
		}//*/
		if(	is != null
			&& (this.type == null || this.type == is.getType())
			&& (this.data == -1 || this.data == is.getDurability()))
		{
			for(final Enchantment ench : is.getEnchantments().keySet())
				if(!this.enchantments.isAllowed(ench))
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
		return "ItemFilter{type="+this.type+",data="+this.data+",enchantments="+this.enchantments.toString()+"}";
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
		final List<String> enchantmentsStr = cs.getStringList("enchantments");
		final Collection<Enchantment> enchantments = new HashSet<>();
		for(final String enchStr : enchantmentsStr)
		{
			final Enchantment ench = Enchantment.getByName(enchStr);
			if(ench == null)
				{}//TODO ench not found/mistype
			else
				enchantments.add(ench);
		}
		final boolean whitelist = cs.getBoolean("ench-are-whitelist", true);
		return new ItemFilter(type, data, new FilterList<>(enchantments, whitelist));
	}

}
