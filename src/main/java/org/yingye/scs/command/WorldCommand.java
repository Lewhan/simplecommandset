package org.yingye.scs.command;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.yingye.scs.core.Core;
import org.yingye.scs.util.Auxiliary;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class WorldCommand implements CommandExecutor {

    private static final HashMap<String, World.Environment> WORLD_TYPE = new HashMap(Map.of("normal", World.Environment.NORMAL, "nether", World.Environment.NETHER, "end", World.Environment.THE_END));
    private static final HashMap<String, String> WORLD_TYPE_NAME = new HashMap(Map.of("normal", "正常", "nether", "下界", "end", "末地"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        label = label.toLowerCase();
        if (label.equalsIgnoreCase("world") || label.equalsIgnoreCase("simplecommandset:world")) {
            if (args.length > 0) {
                world(sender, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "参数不足");
            }
        }
        return true;
    }

    private void world(CommandSender sender, String command, String[] args) {
        String label = args[0].toLowerCase();
        if (label.equals("create")) {
            createWorld(sender, args);
        } else if (label.equals("delete")) {
            deleteWorld(sender, args);
        } else if (label.equals("tp")) {
            if (sender instanceof Player) {
                if(args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "请输入要前往的世界名");
                } else {
                    tpWorld((Player) sender, args[1]);
                }
            } else {
                sender.sendMessage("改命令只能由玩家使用");
            }
        }
    }

    private void createWorld(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "参数不足");
        } else if (args.length == 2) {
            createWorld(sender, args[1]);
        } else {
            createWorld(sender, args[1], args[2]);
        }
    }

    private void createWorld(CommandSender sender, String worldName) {
        String type = "normal";
        createWorld(sender, worldName, type);
    }

    /**
     * 创建一个世界
     *
     * @param sender    操作者
     * @param worldName 世界名
     * @param type      世界类型
     */
    private void createWorld(CommandSender sender, String worldName, String type) {
        WorldCreator creator = new WorldCreator(worldName);
        World.Environment environment = WORLD_TYPE.get(type);
        if (environment == null) {
            creator.environment(World.Environment.NORMAL);
        } else {
            creator.environment(environment);
        }
        sender.getServer().createWorld(creator);
        sender.sendMessage(ChatColor.GREEN + "创建成功");
        if (sender instanceof Player) {
            Core.printWarn(Auxiliary.getFormatDate() + " --- 管理员: " + sender.getName() + ",创建了世界: " + worldName + ",世界类型为: " + WORLD_TYPE_NAME.get(type));
        }
    }

    private void deleteWorld(CommandSender sender, String[] args) {
        String des = "";
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "参数不足");
            return;
        }

        if (args.length > 2) {
            des = args[2];
        } else {
            des = "true";
        }

        String why = des;
        new BukkitRunnable() {
            @Override
            public void run() {
                deleteWorld(sender, args[1], why);
            }
        }.runTaskLater(Core.getPlugin(), 20);
    }

    /**
     * @param sender    命令触发者
     * @param worldName 要删除的世界名
     * @param destroy   是否真的删除，如果为false，则会在下次创建同名世界的时候直接挂载上
     */
    private void deleteWorld(CommandSender sender, String worldName, String destroy) {
        World world = sender.getServer().getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "没有找到该世界");
            return;
        }
        sender.getServer().unloadWorld(world, true);
        if (destroy.equals("true")) {
            File file = world.getWorldFolder();
            removeDir(file);
        }
        sender.sendMessage(ChatColor.GREEN + "删除成功");
        if (sender instanceof Player) {
            Core.printWarn(Auxiliary.getFormatDate() + " --- 管理员: " + sender.getName() + ",删除了世界: " + worldName);
        }
    }

    /**
     * 删除世界对应的文件夹
     *
     * @param file 世界对应的资源文件夹
     */
    private void removeDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                file.delete();
            } else {
                for (File f : files) {
                    removeDir(f);
                }
            }
        }
        file.delete();
    }

    /**
     * 世界传送
     *
     * @param player    触发命令的玩家
     * @param worldName 要传送到哪个世界
     */
    private void tpWorld(Player player, String worldName) {
        World world = player.getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "没有找到这个世界");
        } else {
            player.teleport(world.getSpawnLocation());
        }
    }

}