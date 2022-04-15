package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(MeleeAttack.class)
public class MeleeAttackTaskMixin {
    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "checkExtraStartConditions")
    public void checkExtraStartConditions(ServerLevel level, Mob mob, CallbackInfoReturnable<Boolean> info) {
        if (((IBucklerUser)mob).isBucklerDashing()) 
            info.setReturnValue(false);
    }
}
