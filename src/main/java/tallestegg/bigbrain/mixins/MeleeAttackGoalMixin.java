package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.common.entity.IBucklerUser;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
    @Shadow
    public int ticksUntilNextAttack;

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;ticksUntilNextPathRecalculation:I"), cancellable = true, method = "start")
    public void start(CallbackInfo info) {
        if (BigBrainConfig.meleeFix)
            info.cancel();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;ticksUntilNextAttack:I"), cancellable = true, method = "resetAttackCooldown", remap = false)
    public void resetAttackCooldown(CallbackInfo info) {
        if (BigBrainConfig.meleeFix) {
            if (this.ticksUntilNextAttack <= 0) {
                this.ticksUntilNextAttack = 20;
            }
            info.cancel();
        }
    }
    
    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "canUse")
    public void canUse(CallbackInfoReturnable<Boolean> info) {
        if (((IBucklerUser)mob).isBucklerDashing()) 
            info.setReturnValue(false);
    }
}
