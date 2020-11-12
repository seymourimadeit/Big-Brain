package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.CrossbowItem;

@Mixin(RangedCrossbowAttackGoal.class)
public class RangedCrossbowAttackGoalMixin<T extends MonsterEntity & IRangedAttackMob & ICrossbowUser> extends Goal {

    @Shadow
    @Final
    private T field_220748_a;

    @Shadow
    private RangedCrossbowAttackGoal.CrossbowState field_220749_b = RangedCrossbowAttackGoal.CrossbowState.UNCHARGED;

    @Shadow
    private int field_220753_f;

    @Inject(at = @At(value = "HEAD"), method = "tick")
    public void tick(CallbackInfo info) {
        if (CrossbowItem.isCharged(field_220748_a.getActiveItemStack()) && this.field_220749_b == RangedCrossbowAttackGoal.CrossbowState.CHARGING && this.field_220749_b != RangedCrossbowAttackGoal.CrossbowState.CHARGED) {
            this.field_220748_a.stopActiveHand();
            this.field_220749_b = RangedCrossbowAttackGoal.CrossbowState.CHARGED;
            this.field_220753_f = 20 + this.field_220748_a.getRNG().nextInt(20);
            this.field_220748_a.setCharging(false);
        }
    }

    @Shadow
    public boolean shouldExecute() {
        return false;
    }
}
