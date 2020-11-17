package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(RangedCrossbowAttackGoal.class)
public class RangedCrossbowAttackGoalMixin<T extends MonsterEntity & IRangedAttackMob & ICrossbowUser> extends Goal {

    @Shadow
    @Final
    private T field_220748_a;

    @Shadow
    private RangedCrossbowAttackGoal.CrossbowState field_220749_b = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;

    @Shadow
    private int field_220753_f;

    @Final
    @Shadow
    private double field_220750_c;

    @Final
    @Shadow
    private float field_220751_d;

    @Shadow
    private int field_220752_e;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;getChargeTime(Lnet/minecraft/item/ItemStack;)I"), method = "tick()V")
    public void tick(CallbackInfo info) {
        int i = this.field_220748_a.getItemInUseMaxCount();
        ItemStack itemstack = this.field_220748_a.getActiveItemStack();
        if (i >= CrossbowItem.getChargeTime(itemstack) || CrossbowItem.isCharged(itemstack)) {
            this.field_220748_a.stopActiveHand();
            this.field_220749_b = RangedCrossbowAttackGoal.CrossbowState.CHARGED;
            this.field_220753_f = 20 + this.field_220748_a.getRNG().nextInt(20);
            this.field_220748_a.setCharging(false);
        }
    }

    @Inject(at = @At(value = "HEAD", target = "Lnet/minecraft/entity/ai/goal/RangedCrossbowAttackGoal;CrossbowState$UNCHARGED:Ljava/lang/Enum"), method = "tick()V")
    public void test(CallbackInfo info) {
        LivingEntity livingentity = this.field_220748_a.getAttackTarget();
        if (livingentity != null) {
            double d0 = this.field_220748_a.getDistanceSq(livingentity);
            boolean flag2 = (d0 > (double) this.field_220751_d || this.field_220752_e < 5) && this.field_220753_f == 0;
            if (this.field_220749_b == RangedCrossbowAttackGoal.CrossbowState.UNCHARGED && !CrossbowItem.isCharged(this.field_220748_a.getActiveItemStack())) {
                if (!flag2) {
                    this.field_220748_a.setActiveHand(ProjectileHelper.getHandWith(this.field_220748_a, Items.CROSSBOW));
                    this.field_220749_b = RangedCrossbowAttackGoal.CrossbowState.CHARGING;
                    this.field_220748_a.setCharging(true);
                }
            }
        }
    }

    @Shadow
    public boolean shouldExecute() {
        return false;
    }
}