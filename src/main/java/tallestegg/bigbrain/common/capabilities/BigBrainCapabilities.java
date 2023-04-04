package tallestegg.bigbrain.common.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.BigBrain;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainCapabilities {
    public static final Capability<IOneCriticalAfterCharge> GUARANTEED_CRIT_TRACKER = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IOneCriticalAfterCharge.class);
    }

    public static IOneCriticalAfterCharge getGuaranteedCritical(LivingEntity entity) {
        LazyOptional<IOneCriticalAfterCharge> listener = entity.getCapability(GUARANTEED_CRIT_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the Big Brain github!"));
        return null;
    }
}
