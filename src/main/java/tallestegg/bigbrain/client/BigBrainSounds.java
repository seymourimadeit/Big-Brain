package tallestegg.bigbrain.client;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.bigbrain.BigBrain;

public class BigBrainSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, BigBrain.MODID);
    public static final Holder<SoundEvent> PIGLIN_BRUTE_CHARGE = SOUNDS.register("entity.piglin_brute.charge", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "entity.piglin_brute.charge")));
    public static final Holder<SoundEvent> SHIELD_BASH = SOUNDS.register("item.buckler.bash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "item.buckler.bash")));
    public static final Holder<SoundEvent> CRITICAL_ACTIVATE = SOUNDS.register("entity.criticalcharge.activate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "entity.criticalcharge.activate")));
    public static final Holder<SoundEvent> CRITICAL_DEACTIVATE = SOUNDS.register("entity.criticalcharge.deactivate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BigBrain.MODID, "entity.criticalcharge.deactivate")));
}
