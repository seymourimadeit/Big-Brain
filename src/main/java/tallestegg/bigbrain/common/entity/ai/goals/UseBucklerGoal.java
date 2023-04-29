package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;

import java.util.EnumSet;

//So Guards can use the buckler if the player puts it on their offhand.
public class UseBucklerGoal<T extends PathfinderMob> extends Goal {
    private final T owner;
    private int strafeTicks;
    private long nextOkStartTime;
    private ChargePhases chargePhase = ChargePhases.NONE;

    public UseBucklerGoal(T owner) {
        this.owner = owner;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return (owner.getLevel().getGameTime() - nextOkStartTime > (long)BigBrainConfig.COMMON.BucklerCooldown.get())&& owner.getOffhandItem().getItem() instanceof BucklerItem && owner.getTarget() != null && owner.hasLineOfSight(owner.getTarget()) && owner.getTarget().distanceTo(owner) >= 4.0D
                && !owner.isInWaterRainOrBubble();
    }

    @Override
    public boolean canContinueToUse() {
        return owner.getOffhandItem().getItem() instanceof BucklerItem && owner.getTarget() != null && owner.hasLineOfSight(owner.getTarget()) && !owner.isInWaterRainOrBubble() && chargePhase != ChargePhases.FINISH;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = owner.getTarget();
        if (livingEntity == null)
            return;
        if (BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(owner)) > 0 && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), owner) > 0 || BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(owner)) <= 0)
            owner.lookAt(livingEntity, 30.0F, 30.0F);
        if (owner.distanceTo(livingEntity) >= 10.0D) {
            owner.getNavigation().moveTo(livingEntity, 1.0D);
        } else {
            owner.getNavigation().stop();
        }
        if (chargePhase == ChargePhases.STRAFE && strafeTicks > 0 && owner.distanceTo(livingEntity) >= 4.0D && owner.distanceTo(livingEntity) <= 10.0D) {
            owner.getMoveControl().strafe(-2.0F, 0.0F);
            strafeTicks--;
            if (strafeTicks == 0)
                chargePhase = ChargePhases.CHARGE;
        } else if (chargePhase == ChargePhases.CHARGE) {
            if (!owner.isUsingItem()) {
                owner.startUsingItem(InteractionHand.OFF_HAND);
                chargePhase = ChargePhases.CHARGING;
            }
        } else if (chargePhase == ChargePhases.CHARGING) {
            if (owner.getTicksUsingItem() >= owner.getUseItem().getUseDuration())
                chargePhase = ChargePhases.FINISH;
        }
    }

    @Override
    public void start() {
        owner.setAggressive(true);
        chargePhase = ChargePhases.STRAFE;
        strafeTicks = 20;
    }

    @Override
    public void stop() {
        owner.stopUsingItem();
        owner.setAggressive(false);
        owner.setTarget(null);
        nextOkStartTime = owner.getLevel().getGameTime();
    }

    public enum ChargePhases {
        NONE, STRAFE, CHARGE, CHARGING, FINISH;
    }
}
