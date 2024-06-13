package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

@Mixin(RangedCrossbowAttackGoal.class)
public class RangedCrossbowAttackGoalMixin<T extends net.minecraft.world.entity.Mob & RangedAttackMob & CrossbowAttackMob> extends Goal {
    @Shadow
    @Final
    private T mob;

    @Shadow
    private RangedCrossbowAttackGoal.CrossbowState crossbowState = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;

    @Shadow
    private int attackDelay;

    @Final
    @Shadow
    private float attackRadiusSqr;

    @Shadow
    private int seeTime;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;getChargeDuration(Lnet/minecraft/world/item/ItemStack;)I"), method = "tick")
    public void tick(CallbackInfo info) {
        int i = this.mob.getTicksUsingItem();
        ItemStack itemstack = this.mob.getUseItem();
        if (i >= CrossbowItem.getChargeDuration(itemstack) || CrossbowItem.isCharged(itemstack)) {
            this.mob.releaseUsingItem();
            this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGED;
            this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
            this.mob.setChargingCrossbow(false);
        }
    }

    @Inject(at = @At(value = "HEAD", target = "Lnet/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal;CrossbowState$UNCHARGED:Ljava/lang/Enum"), method = "tick")
    public void tick2(CallbackInfo info) {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            double d0 = this.mob.distanceToSqr(livingentity);
            boolean flag2 = (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (this.crossbowState == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED
                    && !CrossbowItem.isCharged(this.mob.getUseItem())) {
                if (!flag2) {
                    this.mob.startUsingItem(
                            ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                    this.crossbowState = RangedCrossbowAttackGoal.CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            }
        }
    }

    @Shadow
    public boolean canUse() {
        return false;
    }
}