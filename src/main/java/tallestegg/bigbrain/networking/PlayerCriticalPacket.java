package tallestegg.bigbrain.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;

public class PlayerCriticalPacket {
    private final int entityId;

    public PlayerCriticalPacket(int entityId) {
        this.entityId = entityId;
    }

    public static PlayerCriticalPacket decode(PacketBuffer buf) {
        return new PlayerCriticalPacket(buf.readInt());
    }

    public static void encode(PlayerCriticalPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(PlayerCriticalPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet != null) {
            ((NetworkEvent.Context) context.get()).enqueueWork(new Runnable() {
                @Override
                public void run() {
                    ServerPlayerEntity player = ((NetworkEvent.Context) context.get()).getSender();
                    ((IOneCriticalAfterCharge)player).setCritical(false);
                }
            });
        }
        context.get().setPacketHandled(true);
    }
}