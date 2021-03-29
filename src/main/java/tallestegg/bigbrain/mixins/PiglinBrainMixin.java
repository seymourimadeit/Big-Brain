package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;

@Mixin(PiglinTasks.class)
public class PiglinBrainMixin {
    // Mojang forgot to make sure this method doesn't continue if it detects an entity is an instance of AbstractPiglinEntity
    @Inject(at = @At(value = "HEAD"), cancellable = true, method = "func_234468_a_")
    private static void func_234468_a_(PiglinEntity piglin, LivingEntity entity, CallbackInfo info) {
        if (entity instanceof AbstractPiglinEntity)
            info.cancel();
    }
}
