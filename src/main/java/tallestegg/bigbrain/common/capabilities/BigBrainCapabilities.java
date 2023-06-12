package tallestegg.bigbrain.common.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.common.capabilities.implementations.BurrowCapability;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainCapabilities {
    public static final Capability<BurrowCapability> BURROW_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(BurrowCapability.class);
    }

    public static BurrowCapability getBurrowing(LivingEntity entity) {
        @NotNull LazyOptional<BurrowCapability> listener = entity.getCapability(BURROW_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the Big Brain github!"));
        return null;
    }
}
