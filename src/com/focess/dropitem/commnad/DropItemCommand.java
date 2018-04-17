package com.focess.dropitem.commnad;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffectType;

import com.focess.dropitem.Debug;
import com.focess.dropitem.DropItem;
import com.focess.dropitem.event.DropItemDeathEvent;
import com.focess.dropitem.item.CraftDropItem;
import com.focess.dropitem.item.EntityDropItem;
import com.focess.dropitem.util.AnxiCode;
import com.focess.dropitem.util.Array;

public class DropItemCommand extends Command {
	private static YamlConfiguration yaml;
	private int anxiCode;
	private DropItem drop;
	private final Array<String> messages = new Array<>(true);

	private boolean flag = true;

	public DropItemCommand(final String description, final String usageMessage, final int anxiCode,
			final DropItem dropItem) {
		super("DropItem", description, usageMessage, new Array<>(new String[] { "di" }).toArrayList());
		try {
			this.drop = dropItem;
			this.anxiCode = AnxiCode.getCode(DropItemCommand.class, dropItem);
			if (anxiCode == this.anxiCode) {
				final File messFile = new File(this.drop.getDataFolder(), "message.yml");
				DropItemCommand.yaml = YamlConfiguration.loadConfiguration(messFile);
				this.loadConfig();
			} else
				AnxiCode.shut(DropItemCommand.class);
		} catch (final Exception e) {
			Debug.debug(e, "Something wrong in registing Command DropItem.");
		}
	}

	@Override
	public boolean execute(final CommandSender commandSender, final String a, final String[] args) {
		try {
			if (!this.flag) {
				commandSender.sendMessage(this.getMessage("CommandUnregister"));
				return true;
			}
			if (!commandSender.hasPermission("dropitem.command"))
				commandSender.sendMessage(this.getMessage("HaveNoPermission"));
			else if (args.length == 0)
				this.usage(commandSender);
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("clean")) {
					final List<EntityDropItem> dropItems = CraftDropItem.getDropItems(this.anxiCode).toArrayList();
					for (final EntityDropItem dropItem : dropItems)
						CraftDropItem.remove(dropItem, DropItemDeathEvent.DeathCause.SYSTEM_CLEAN);
					final File drops = new File(this.drop.getDataFolder(), "drops");
					for (final File file : drops.listFiles())
						file.delete();
					this.drop.getCraftAIListener(this.anxiCode).clear(this.anxiCode);
					commandSender.sendMessage(this.getMessage("AfterClean"));
				} else if (args[0].equalsIgnoreCase("disable")) {
					commandSender.sendMessage(this.getMessage("Disabling"));
					this.drop.getPluginLoader().disablePlugin(this.drop);
					final List<EntityDropItem> dropItems = CraftDropItem.getDropItems(this.anxiCode).toArrayList();
					for (final EntityDropItem dropItem : dropItems) {
						dropItem.getLocation().getWorld().dropItem(dropItem.getLocation(), dropItem.getItemInHand());
						CraftDropItem.remove(dropItem, false);
					}
					this.unregister(this.drop.commandMap);
					this.flag = false;
					FileUtils.forceDelete(this.drop.getDataFolder());
				} else if (args[0].equalsIgnoreCase("reload")) {
					commandSender.sendMessage(this.getMessage("Reloading"));
					this.drop.loadConfig();
				} else if (args[0].equalsIgnoreCase("cleanall")) {
					final List<EntityDropItem> dropItems = CraftDropItem.getDropItems(this.anxiCode).toArrayList();
					for (final EntityDropItem dropItem : dropItems)
						CraftDropItem.remove(dropItem, DropItemDeathEvent.DeathCause.SYSTEM_CLEAN);
					final File drops = new File(this.drop.getDataFolder(), "drops");
					for (final File file : drops.listFiles())
						file.delete();
					this.drop.getCraftAIListener(this.anxiCode).clear(this.anxiCode);
					final List<World> worlds = Bukkit.getWorlds();
					for (final World world : worlds)
						for (final Entity entity : world.getEntities())
							if (!this.drop.islower) {
								if ((entity instanceof ArmorStand) && !((ArmorStand) entity).isVisible())
									entity.remove();
							} else if ((entity instanceof Zombie)
									&& ((Zombie) entity).hasPotionEffect(PotionEffectType.INVISIBILITY))
								entity.remove();
					commandSender.sendMessage(this.getMessage("AfterCleanAll"));
				} else {
					commandSender.sendMessage(this.getMessage("InvaildArg"));
					this.usage(commandSender);
				}
			} else {
				commandSender.sendMessage(this.getMessage("InvaildArg"));
				this.usage(commandSender);
			}
			// if (args[0].equalsIgnoreCase("test"))
			// new Test((Player)commandSender);
			return true;
		} catch (final Exception e) {
			final StringBuilder sargs = new StringBuilder();
			for (final String s : args)
				sargs.append(s);
			Debug.debug(e, "Something wrong in executing Command DropItem(CommandSender = " + commandSender.getName()
					+ ", Args = " + sargs.toString() + ").");
			return true;
		}
	}

	private String getMessage(final String message) {
		try {
			final Object[] messages = this.messages.getValue(message);
			if (messages == null)
				return "";
			if (messages.length == 1)
				return (String) messages[0];
			return "";
		} catch (final Exception e) {
			Debug.debug(e, "Something wrong in getting File Message(Path = \"" + DropItemCommand.yaml.getCurrentPath()
					+ "\").");
			return "";
		}
	}

	private void loadConfig() {
		try {
			final Set<String> keys = DropItemCommand.yaml.getKeys(false);
			for (final String key : keys)
				this.messages.setValue(this.messages.add(key), DropItemCommand.yaml.getString(key));
		} catch (final Exception e) {
			Debug.debug(e, "Something wrong in loading File Message(Path = \"" + DropItemCommand.yaml.getCurrentPath()
					+ "\").");
		}
	}

	@Override
	public List<String> tabComplete(final CommandSender commandSender, final String alias, final String[] args) {
		try {
			final Array<String> defaults = new Array<>(new String[] { "clean", "cleanall", "disable", "reload" });
			if (args == null)
				return defaults.toArrayList();
			else if (args.length == 1) {
				final Array<String> temp = new Array<>();
				for (final String arg : defaults)
					if (arg.startsWith(args[0]))
						temp.add(arg);
				return temp.toArrayList();
			}
			return new Array<String>().toArrayList();
		} catch (final Exception e) {
			final StringBuilder sargs = new StringBuilder();
			for (final String s : args)
				sargs.append(s + " ");
			Debug.debug(e, "Something wrong in showing infomation about Command DropItem(CommandSender = "
					+ commandSender.getName() + ", Args = " + sargs.toString() + ").");
			return new Array<String>().toArrayList();
		}
	}

	private void usage(final CommandSender commandSender) {
		try {
			commandSender.sendMessage(this.getMessage("Welcome"));
			commandSender.sendMessage(this.getMessage("CommandClean"));
			commandSender.sendMessage(this.getMessage("CommandCleanAll"));
			commandSender.sendMessage(this.getMessage("CommandDisable"));
			commandSender.sendMessage(this.getMessage("CommandReload"));
		} catch (final Exception e) {
			Debug.debug(e, "Something wrong in usaging to " + commandSender.getName() + ".");
		}
	}
}