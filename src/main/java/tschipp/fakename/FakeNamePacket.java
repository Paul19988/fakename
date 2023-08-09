package tschipp.fakename;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class FakeNamePacket {
    public final String fakename;
    public final int entityId;
    public final int deleteFakename;

    public FakeNamePacket(FriendlyByteBuf buf) {
        this.fakename = buf.readUtf();
        this.entityId = buf.readInt();
        this.deleteFakename = buf.readInt();
    }

    public FakeNamePacket(String fakename, int entityID, int delete) {
        this.fakename = fakename;
        this.entityId = entityID;
        this.deleteFakename = delete;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(fakename);
        buf.writeInt(entityId);
        buf.writeInt(deleteFakename);
    }

    public void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.level == null) {
                return;
            }

            Player toSync = (Player) mc.level.getEntity(entityId);

            if (toSync != null) {
                ctx.get().setPacketHandled(true);

                FakeName.performFakenameOperation(toSync, fakename, deleteFakename);

                if (mc.player == null) {
                    return;
                }

                PlayerInfo playerInfo = mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId());

                if (playerInfo == null) {
                    return;
                }

                playerInfo.setTabListDisplayName(deleteFakename == 0 ? new TextComponent(fakename) : new TextComponent(toSync.getGameProfile().getName()));
            }

        });
    }

}
