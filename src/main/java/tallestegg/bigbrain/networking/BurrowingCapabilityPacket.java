package tallestegg.bigbrain.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.bigbrain.BigBrain;

public record BurrowingCapabilityPacket(int entityId, boolean burrow) implements CustomPacketPayload {
    public static final Type<BurrowingCapabilityPacket> TYPE = new Type<>(new ResourceLocation(BigBrain.MODID, "burrow"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BurrowingCapabilityPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, BurrowingCapabilityPacket::entityId, ByteBufCodecs.BOOL, BurrowingCapabilityPacket::burrow, BurrowingCapabilityPacket::new);

    public static void handle(BurrowingCapabilityPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> BigBrainNetworking.syncBurrow(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
