package tallestegg.bigbrain.common.entity.ai.goals;

import java.util.EnumSet;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.InteractionHand;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.entity.IBucklerUser;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;

//So Guards can use the buckler if the player puts it on their offhand.
public class UseBucklerGoal<T extends PathfinderMob> extends Goal {
    private final T owner;
    private int strafeTicks;
    private ChargePhases chargePhase = ChargePhases.NONE;

    public UseBucklerGoal(T owner) {
        this.owner = owner;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getOffhandItem().getItem() instanceof BucklerItem && owner.getTarget() != null && owner.hasLineOfSight(owner.getTarget()) && owner.getTarget().distanceTo(owner) >= 4.0D
                && !owner.isInWaterRainOrBubble();
    }

    @Override
    public boolean canContinueToUse() {
        return ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getOffhandItem().getItem() instanceof BucklerItem && owner.getTarget() != null && owner.hasLineOfSight(owner.getTarget()) && !owner.isInWaterRainOrBubble() && chargePhase != ChargePhases.FINISH;
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
            if (!owner.isUsingItem())
                owner.startUsingItem(InteractionHand.OFF_HAND);
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
    }

    public enum ChargePhases {
        NONE, STRAFE, CHARGE, FINISH;
    }
}
