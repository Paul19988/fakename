package tschipp.fakename;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FakeNamePacketHandler implements IMessageHandler<FakeNamePacket, IMessage> {

    @Override
    public IMessage onMessage(final FakeNamePacket message, final MessageContext ctx) {
        IThreadListener mainThread = Minecraft.getMinecraft();

        mainThread.addScheduledTask(() -> {
            EntityPlayer toSync = (EntityPlayer) FakeName.getInstance().getProxy().getClientWorld().getEntityByID(message.entityId);

            if (toSync != null) {
                System.out.println("Syncing " + toSync.getName() + " with " + message.fakename);
                if (Minecraft.getMinecraft().getConnection() == null) {
                    return;
                }

                if (message.deleteFakename == 0) {
                    NBTTagCompound tag = toSync.getEntityData();
                    tag.setString(FakeName.KEY, message.fakename);
                    Minecraft.getMinecraft().getConnection().getPlayerInfo(toSync.getUniqueID()).setDisplayName(new TextComponentString(message.fakename));
                } else {
                    NBTTagCompound tag = toSync.getEntityData();
                    tag.removeTag(FakeName.KEY);
                    Minecraft.getMinecraft().getConnection().getPlayerInfo(toSync.getUniqueID()).setDisplayName(new TextComponentString(toSync.getGameProfile().getName()));
                }
                toSync.refreshDisplayName();
            }
        });
        return null;
    }
}