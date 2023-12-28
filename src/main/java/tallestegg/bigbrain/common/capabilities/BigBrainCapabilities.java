package tallestegg.bigbrain.common.capabilities;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tallestegg.bigbrain.BigBrain;

import java.util.function.Supplier;


public class BigBrainCapabilities {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BigBrain.MODID);
    public static final Supplier<AttachmentType<Boolean>> BURROWING = ATTACHMENT_TYPES.register(
            "burrowing", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<Boolean>> CARRYING = ATTACHMENT_TYPES.register(
            "carrying", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<Boolean>> DIGGING = ATTACHMENT_TYPES.register(
            "digging", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
}
