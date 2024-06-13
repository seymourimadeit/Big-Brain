package tallestegg.bigbrain.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tallestegg.bigbrain.BigBrain;

public record ShellHealthPacket(int entityId, int shellHealth) implements CustomPacketPayload {
    public static final Type<ShellHealthPacket> TYPE = new Type<>(new ResourceLocation(BigBrain.MODID, "shell_health"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShellHealthPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ShellHealthPacket::entityId, ByteBufCodecs.INT, ShellHealthPacket::shellHealth, ShellHealthPacket::new);

    public static void handle(ShellHealthPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> BigBrainNetworking.syncShellHealth(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
