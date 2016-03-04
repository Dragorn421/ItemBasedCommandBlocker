package fr.dragorn421.itembasedcommandblocker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class IBCBPlugin extends JavaPlugin implements Listener
{

	static private IBCBPlugin instance;

	private YAMLConfigHandler yamlConfigHandler;

	private Map<String, List<ItemFilter>> equals;
	private Map<String, List<ItemFilter>> contains;
	private Map<String, List<ItemFilter>> startsWith;
	private Map<Material, String[]> names;

	@Override
	public void onEnable()
	{
		IBCBPlugin.instance = this;
		try {
			this.yamlConfigHandler = new YAMLConfigHandler(this);
		} catch (final IOException e) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				@Override
				public void run()
				{
					Bukkit.getPluginManager().disablePlugin(IBCBPlugin.instance);
				}
			}, 0L);
			throw new IllegalStateException("Unable to load configuration.", e);
		}
		this.loadConfig();
		this.names = this.getNames();
		Bukkit.getPluginManager().registerEvents(this, this);
		super.getLogger().info(super.getName() + " enabled!");
	}

	@Override
	public void onDisable()
	{
		super.getLogger().info(super.getName() + " disabled!");
	}

	public void loadConfig()
	{
		boolean modified = false;
		if(!this.getConfig().isString("default-comparison") || getMapForComparison(this.getConfig().getString("default-comparison")) == null)
		{
			this.getConfig().set("default-comparison", "equals");
			modified = true;
		}
		final String defaultComparison = this.getConfig().getString("default-comparison").toLowerCase();
		if(!this.getConfig().isConfigurationSection("filters"))
		{
			this.getConfig().createSection("filters");
			modified = true;
		}
		final ConfigurationSection filtersCs = this.getConfig().getConfigurationSection("filters");
		this.equals = new HashMap<>();
		this.contains = new HashMap<>();
		this.startsWith = new HashMap<>();
		for(final String key : filtersCs.getKeys(false))
		{
			if(!filtersCs.isConfigurationSection(key))
				continue;
			final ConfigurationSection cs = filtersCs.getConfigurationSection(key);
			Map<String, List<ItemFilter>> map = this.getMapForComparison(cs.getString("comparison-type", defaultComparison));
			if(map == null)
				map = this.getMapForComparison(defaultComparison);
			String cmd = cs.getString("command");
			if(cmd == null || cmd.length() == 0)
			{
				cs.set("command", "");
				modified = true;
				continue;
			}
			cmd = cmd.toLowerCase();
			List<ItemFilter> filterList = map.get(cmd);
			if(filterList == null)
				map.put(cmd, filterList = new ArrayList<>(1));
			filterList.add(ItemFilter.fromConfig(cs));
		}
		//TODO add permissions to bypass
		if(modified)
			this.saveConfig();
	}

	public Map<String, List<ItemFilter>> getMapForComparison(final String comparison)
	{
		switch(comparison.toLowerCase())
		{
		case "equals":
			return this.equals;
		case "contains":
			return this.contains;
		case "startswith":
			return this.startsWith;
		default:
			return null;
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent e)
	{
		//long start = System.currentTimeMillis();
		final String cmd = e.getMessage().toLowerCase();
		List<ItemFilter> filters = this.equals.get(cmd);
		ItemStack inventory[] = null;
		ItemStack blockingItem = null;
		if(filters != null)
		{
			inventory = this.getFullInventory(e.getPlayer());
			for(int i=filters.size()-1;i>=0;i--)
				if((blockingItem = filters.get(i).matchesItems(inventory)) != null)
					break;
		}
		if(blockingItem == null)
		{
			mainloop:
			for(final Entry<String, List<ItemFilter>> en : this.contains.entrySet())
				if(cmd.contains(en.getKey()))
				{
					filters = en.getValue();
					if(inventory == null)
						inventory = this.getFullInventory(e.getPlayer());
					for(int i=filters.size()-1;i>=0;i--)
						if((blockingItem = filters.get(i).matchesItems(inventory)) != null)
							break mainloop;
				}
		}
		if(blockingItem == null)
		{
			mainloop:
			for(final Entry<String, List<ItemFilter>> en : this.startsWith.entrySet())
				if(cmd.startsWith(en.getKey()))
				{
					filters = en.getValue();
					if(inventory == null)
						inventory = this.getFullInventory(e.getPlayer());
					for(int i=filters.size()-1;i>=0;i--)
						if((blockingItem = filters.get(i).matchesItems(inventory)) != null)
							break mainloop;
				}
		}
		if(blockingItem != null)
		{
			e.setCancelled(true);
			e.getPlayer().sendMessage("Vous ne pouvez pas exécuter cette commande car vous possédez dans votre inventaire l'objet :");
			this.sendName(e.getPlayer(), blockingItem);
		}
		//System.out.println("time for cmd parse: " + (System.currentTimeMillis() - start) + " ms");
	}

	public String getName(final Material m, final short damage)
	{
		final String names[] = this.names.get(m);
		if(damage < names.length)
			return names[damage];
		return names[0];
	}

	public void sendName(final Player p, final ItemStack is)
	{
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " {\"translate\":\"" + this.getName(is.getType(), is.getDurability()) + "\"}");
	}

	private Map<Material, String[]> getNames()
	{//inspired by http://rubukkit.org/threads/iz-material-stone-v-tile-stone-stone-name.111035/
		final Map<Material, String[]> names = new HashMap<>();
		final ItemStack is = new ItemStack(Material.STONE);
		final World w = Bukkit.getWorlds().get(0);
		final Location loc = w.getSpawnLocation();
		final Item item = w.dropItem(loc, is);
		final String expectedError = "Item entity " + item.getEntityId() + " has no item?!";
		super.getLogger().info("These errors (" + expectedError + ") are not intended but are normal and cannot be avoided, don't panic!");//TODO
		item.setCustomName(null);
		for(final Material m : Material.values())
		{
			is.setType(m);
			is.setDurability((short) 0);
			item.setItemStack(is);
			String name0 = item.getName();
			is.setDurability(Short.MAX_VALUE);
			if(is.getDurability() == Short.MAX_VALUE)
			{
				final List<String> nameList = new ArrayList<>();
				nameList.add(name0.substring(5)+".name");
				for(short i=1;i<=Short.MAX_VALUE;i++)
				{
					is.setDurability(i);
					item.setItemStack(is);
					final String name;
					if((name = item.getName()).equals(name0))
						break;
					nameList.add(name.substring(5)+".name");
				}
				names.put(m, nameList.toArray(new String[0]));
			}
			else
				names.put(m, new String[]{name0.substring(5)+".name"});
		}
		item.remove();
		getLogger().info("End of expected errors");//TODO
		return names;
	}

	private ItemStack[] getFullInventory(final Player p)
	{
		final ItemStack contents[] = p.getInventory().getContents();
		final ItemStack armor[] = p.getInventory().getArmorContents();
		final ItemStack full[] = new ItemStack[contents.length+armor.length];
		System.arraycopy(contents, 0, full, 0, contents.length);
		System.arraycopy(armor, 0, full, contents.length, armor.length);
/*		System.out.println("contents: " + contents.length);
		for(final ItemStack is : contents)
			if(is!=null)System.out.println(is.getType() + ":" + is.getDurability());
		System.out.println("armor: " + armor.length);
		for(final ItemStack is : armor)
			if(is!=null)System.out.println(is.getType() + ":" + is.getDurability());
		System.out.println("full: " + full.length);
		for(final ItemStack is : full)
			if(is!=null)System.out.println(is.getType() + ":" + is.getDurability());//*/
		return full;
	}

	@Override
	public FileConfiguration getConfig()
	{
		return this.yamlConfigHandler.getConfig();
	}

	@Override
	public void reloadConfig()
	{
		this.yamlConfigHandler.reloadConfigSilent();
	}

	@Override
	public void saveConfig()
	{
		this.yamlConfigHandler.save();
	}

	static public IBCBPlugin get()
	{
		return IBCBPlugin.instance;
	}

}
