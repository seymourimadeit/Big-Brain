package tallestegg.bigbrain.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BurrowingCapabilityPacket {
    private final int entityId;
    private final boolean burrow;

    public BurrowingCapabilityPacket(int entityId, boolean burrow) {
        this.entityId = entityId;
        this.burrow = burrow;
    }

    public static BurrowingCapabilityPacket decode(FriendlyByteBuf buf) {
        return new BurrowingCapabilityPacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(BurrowingCapabilityPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.burrow);
    }

    public static void handle(BurrowingCapabilityPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            BigBrainNetworking.syncBurrow(msg);
        });
        context.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean getBurrow() {
        return this.burrow;
    }
}
