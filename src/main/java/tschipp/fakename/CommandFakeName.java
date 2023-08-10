package tschipp.fakename;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

public class CommandFakeName extends CommandBase implements ICommand {

    private final List<String> names = new ArrayList<>();

    public CommandFakeName() {
        names.add("fakename");
        names.add("fn");
        names.add("fname");
    }

    @Override
    public int compareTo(ICommand o) {
        return this.getName().compareTo(o.getName());
    }

    public String getCommandUsageSet() {
        return "/fakename <mode> [player] [fakename]";
    }

    private int handleSetname(ICommandSender sender, Collection<Entity> players, String fakename) {
        fakename = fakename.replace("&", "\u00a7") + "\u00a7r";
        fakename = fakename.replace("/-", " ");

        for(Entity player : players) {
            NBTTagCompound tag = player.getEntityData();
            tag.setString(FakeName.KEY, fakename);
            sender.sendMessage(new TextComponentString(player.getName() + "'s name is now " + fakename));
            FakeName.getInstance().getNetwork().sendToAll(new FakeNamePacket(fakename, player.getEntityId(), 0));
            player.setCustomNameTag(fakename);
            ((EntityPlayer) player).refreshDisplayName();
        }

        return 1;
    }

    private int handleClear(ICommandSender sender, Collection<Entity> players) {
        for(Entity player : players) {
            NBTTagCompound tag = player.getEntityData();
            tag.removeTag(FakeName.KEY);
            sender.sendMessage(new TextComponentString(player.getName() + "'s fake name was cleared!"));
            FakeName.getInstance().getNetwork().sendToAll(new FakeNamePacket("something", player.getEntityId(), 1));
            player.setCustomNameTag(player.getName());
            ((EntityPlayer) player).refreshDisplayName();
        }

        return 1;
    }

    private int handleRealname(ICommandSender sender, String fakename) {
        String copy = fakename;
        fakename = fakename.replace("&", "\u00a7") + "\u00a7r";
        fakename = fakename.replace("/-", " ");
        PlayerList players = sender.getServer().getPlayerList();
        boolean success = false;
        for(EntityPlayerMP player : players.getPlayers()) {
            if (player.getEntityData().hasKey(FakeName.KEY)) {
                if (player.getEntityData().getString(FakeName.KEY).equalsIgnoreCase(fakename)) {
                    sender.sendMessage(new TextComponentString(copy + "'s real name is " + player.getGameProfile().getName()));
                    success = true;
                }
            }
        }

        if (success) {
            return 1;
        }

        sender.sendMessage(new TextComponentString("No player with that name was found!"));
        return 0;
    }

    @Override
    public List<String> getAliases() {
        return this.names;
    }

    @Override
    public String getName() {
        return "fakename";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/fakename <mode> <args...>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        if(args[0].equalsIgnoreCase("set")) {
            // /fakename set <playername> <fakename>
            if (args.length == 3) {
                String fakeName = args[2];
                handleSetname(sender, CommandBase.getEntityList(server, sender, args[1]), fakeName);
            } else if (args.length == 2) {
                String fakeName = args[1];
                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
                    handleSetname(sender, Collections.singletonList((EntityPlayerMP) sender.getCommandSenderEntity()), fakeName);
                } else {
                    throw new WrongUsageException(this.getCommandUsageSet());
                }
            } else {
                throw new WrongUsageException(this.getCommandUsageSet());
            }
        } else if(args[0].equalsIgnoreCase("clear")) {
            // /fakename clear <playername>
            if (args.length == 2) {
                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
                    handleClear(sender, CommandBase.getEntityList(server, sender, args[1]));
                } else {
                    throw new WrongUsageException(this.getCommandUsageSet());
                }
            } else if (args.length == 1) {
                if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
                    handleClear(sender, Collections.singletonList((EntityPlayerMP) sender.getCommandSenderEntity()));
                } else {
                    throw new WrongUsageException(this.getCommandUsageSet());
                }
            } else {
                throw new WrongUsageException(this.getCommandUsageSet());
            }
        } else if(args[0].equalsIgnoreCase("real")) {
            // /fakename real <name>
            if(args.length == 2) {
                handleRealname(sender, args[1]);
            }else{
                throw new WrongUsageException(this.getCommandUsageSet());
            }
        } else {
            throw new WrongUsageException(this.getUsage(sender));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if(args.length < 1) {
            return Collections.emptyList(); // Return an empty list if the length of the args is less than 1
        }

        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "set", "clear", "real");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("real"))) {
            return CommandBase.getListOfStringsMatchingLastWord(args, server.getPlayerList().getPlayers().stream().map(EntityPlayerMP::getName).collect(Collectors.toList()));
        }

        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

}
