package tschipp.fakename;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Iterator;

@Mod.EventBusSubscriber(value = Side.SERVER, modid = "fakename")
public class FakenameEventsServer {

    @SubscribeEvent
    public void renderName(PlayerEvent.NameFormat event) {
        NBTTagCompound tag = event.getEntityPlayer().getEntityData();
        if (tag.hasKey(FakeName.KEY)) {
            event.setDisplayname(tag.getString(FakeName.KEY));
        } else {
            event.setDisplayname(event.getUsername());
        }
    }

    @SubscribeEvent
    public void onJoinWorld(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if(player.getServer() == null) {
            System.out.println("SERVER IS NULL: " + player.getName());
            return;
        }

        WorldServer serverWorld = (WorldServer) event.player.world;
        Iterator<? extends EntityPlayer> playersTracking = serverWorld.getEntityTracker().getTrackingPlayers(player).iterator();

        if(player.getEntityData().hasKey(FakeName.KEY)) {
            while(playersTracking.hasNext()) {
                FakeName.getInstance().getNetwork().sendTo(new FakeNamePacket(player.getEntityData().getString(FakeName.KEY), player.getEntityId(), 0), (EntityPlayerMP) playersTracking.next());
            }
        }
    }

    @SubscribeEvent
    public void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) event.getTarget();
            if (targetPlayer.getEntityData().hasKey(FakeName.KEY)) {
                EntityPlayerMP toRecieve = (EntityPlayerMP) event.getEntityPlayer();

                FakeName.getInstance().getNetwork().sendTo(new FakeNamePacket(targetPlayer.getEntityData().getString(FakeName.KEY), targetPlayer.getEntityId(), 0), toRecieve);
            }
        }
    }

    //Makes Sure that the Data persists on Death
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();

        if (oldPlayer.getEntityData().hasKey(FakeName.KEY)) {
            newPlayer.getEntityData().setString(FakeName.KEY, oldPlayer.getEntityData().getString(FakeName.KEY));
        }
    }

}
