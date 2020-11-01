package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
    @Shadow
    public int field_234037_i_;

    @Shadow
    @Final
    protected CreatureEntity attacker;

    @Inject(method = "startExecuting()V", at = @At(value = "FIELD", target = "net/minecraft/entity/ai/goal/MeleeAttackGoal.delayCounter"), cancellable = true)
    public void startExecuting(CallbackInfo info) {
        System.out.println("ttttt");
        if (this.field_234037_i_ <= 0) {
            this.field_234037_i_ = 0;
        }
        info.cancel();
    }
    
    @Overwrite
    public void func_234039_g_() {
        if (this.field_234037_i_ <= 0) {
            this.field_234037_i_ = 20;
        }
    }

    @Shadow
    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return 0;
    }
}
