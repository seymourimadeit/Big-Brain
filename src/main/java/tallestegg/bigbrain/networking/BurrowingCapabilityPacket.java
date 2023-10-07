package tallestegg.bigbrain.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

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

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            BigBrainNetworking.syncBurrow(this);
        });
        context.setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean getBurrow() {
        return this.burrow;
    }
}
