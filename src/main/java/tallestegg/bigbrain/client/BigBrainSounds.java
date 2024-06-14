package tallestegg.bigbrain.client;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestegg.bigbrain.BigBrain;

public class BigBrainSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, BigBrain.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ARMADILLO_CRACK = createVariableRangeSound("entity.armadillo.crack");

    public static DeferredHolder<SoundEvent, SoundEvent> createVariableRangeSound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BigBrain.MODID, path)));
    }
}
