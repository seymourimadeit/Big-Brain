package tallestegg.bigbrain.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CriticalCapabilityPacket {
    private final int entityId;
    private final boolean crit;

    public CriticalCapabilityPacket(int entityId, boolean crit) {
        this.entityId = entityId;
        this.crit = crit;
    }

    public static CriticalCapabilityPacket decode(FriendlyByteBuf buf) {
        return new CriticalCapabilityPacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(CriticalCapabilityPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.crit);
    }

    public static void handle(CriticalCapabilityPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            BigBrainNetworking.syncCritical(msg);
        });
        context.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean getCrit() {
        return this.crit;
    }
}
