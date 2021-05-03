package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
    @Shadow
    public int field_234037_i_;

    @Shadow
    @Final
    protected CreatureEntity attacker;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;delayCounter:I"), cancellable = true, method = "startExecuting")
    public void a(CallbackInfo info) {
        info.cancel();
    }
    
    
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;field_234037_i_:I"), cancellable = true, method = "func_234039_g_", remap = false)
    public void func_234039_g_(CallbackInfo info) {
        if (this.field_234037_i_ <= 0) {
            this.field_234037_i_ = 20;
        }
        info.cancel();
    }
}
