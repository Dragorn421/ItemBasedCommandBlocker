package fr.dragorn421.itembasedcommandblocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class YAMLConfigHandler
{

	final private File file;

	private YamlConfiguration config;

	public YAMLConfigHandler(final Plugin pl) throws IOException
	{
		this(pl, "config.yml");
	}

	public YAMLConfigHandler(final Plugin pl, final String fileName) throws IOException
	{
		this(new File(pl.getDataFolder(), fileName));
	}

	public YAMLConfigHandler(final File file) throws IOException
	{
		this.file = file;
		this.reloadConfig();
	}

	public FileConfiguration getConfig()
	{
		return this.config;
	}

	public boolean reloadConfigSilent()
	{
		try {
			if(!this.file.isFile())
			{
				this.file.mkdirs();
				this.file.delete();
				this.file.createNewFile();
			}
			try(final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"))){
				this.config = YamlConfiguration.loadConfiguration(in);
			}
			return true;
		} catch(final IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void reloadConfig() throws IOException
	{
		if(!this.file.isFile())
		{
			this.file.mkdirs();
			this.file.delete();
			this.file.createNewFile();
		}
		try(final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"))){
			this.config = YamlConfiguration.loadConfiguration(in);
		}
	}

	public boolean save()
	{
		this.file.delete();
		try {
			this.file.createNewFile();
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		try(final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), "UTF-8"))){
			out.write(this.config.saveToString());
			out.flush();
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void saveDefaultConfig(final Plugin plugin)
	{
		try(final BufferedReader in = new BufferedReader(new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8));
				final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8))) {
			String s;
			while((s = in.readLine()) != null)
			{
				out.write(s);
			}
		} catch(final IOException e) {
			e.printStackTrace();
		}
	}

}
