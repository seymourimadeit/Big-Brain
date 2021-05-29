package tallestegg.bigbrain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BigBrain.MODID);
    public static final RegistryObject<SoundEvent> PIGLIN_BRUTE_CHARGE = SOUNDS.register("entity.piglin_brute.charge", () -> new SoundEvent(new ResourceLocation(BigBrain.MODID, "entity.piglin_brute.charge")));
    public static final RegistryObject<SoundEvent> SHIELD_BASH = SOUNDS.register("item.buckler.bash", () -> new SoundEvent(new ResourceLocation(BigBrain.MODID, "item.buckler.bash")));
    public static final RegistryObject<SoundEvent> BUCKLER_PING = SOUNDS.register("item.buckler.ping", () -> new SoundEvent(new ResourceLocation(BigBrain.MODID, "item.buckler.ping")));
}
