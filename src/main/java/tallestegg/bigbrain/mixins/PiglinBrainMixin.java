package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

@Mixin(PiglinAi.class)
public class PiglinBrainMixin {
    // Mojang forgot to make sure this method doesn't continue if it detects an entity is an instance of AbstractPiglinEntity
    @Inject(at = @At(value = "HEAD"), cancellable = true, method = "wasHurtBy")
    private static void wasHurtBy(Piglin piglin, LivingEntity entity, CallbackInfo info) {
        if (entity instanceof AbstractPiglin)
            info.cancel();
    }
}
