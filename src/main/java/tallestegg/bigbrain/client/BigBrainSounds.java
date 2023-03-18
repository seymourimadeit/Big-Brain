package tallestegg.bigbrain.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestegg.bigbrain.BigBrain;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BigBrain.MODID);
    public static final RegistryObject<SoundEvent> PIGLIN_BRUTE_CHARGE = SOUNDS.register("entity.piglin_brute.charge", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "entity.piglin_brute.charge")));
    public static final RegistryObject<SoundEvent> SHIELD_BASH = SOUNDS.register("item.buckler.bash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "item.buckler.bash")));
}
