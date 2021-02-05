package tallestegg.bigbrain.entity.ai.goals;

import java.util.EnumSet;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

//So Guards can use the buckler if the player puts it on their offhand.
public class UseBucklerGoal<T extends CreatureEntity> extends Goal {
    private final T owner;
    private int strafeTicks;
    private ChargePhases chargePhase = ChargePhases.NONE;

    public UseBucklerGoal(T owner) {
        this.owner = owner;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        return ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getHeldItemOffhand().getItem() instanceof BucklerItem && owner.getAttackTarget() != null && owner.canEntityBeSeen(owner.getAttackTarget()) && owner.getAttackTarget().getDistance(owner) >= 4.0D
                && !owner.isInWaterRainOrBubbleColumn();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getHeldItemOffhand().getItem() instanceof BucklerItem && owner.getAttackTarget() != null && owner.canEntityBeSeen(owner.getAttackTarget()) && !owner.isInWaterRainOrBubbleColumn();
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = owner.getAttackTarget();
        if (livingEntity == null)
            return;
        if (((IBucklerUser) owner).isBucklerDashing() && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), owner.getHeldItemOffhand()) > 0 || !((IBucklerUser) owner).isBucklerDashing()) {
            owner.faceEntity(livingEntity, 30.0F, 30.0F);
        }
        if (chargePhase == ChargePhases.STRAFE && strafeTicks > 0 && owner.getDistance(livingEntity) >= 4.0D && owner.getDistance(livingEntity) <= 10.0D) {
            owner.getMoveHelper().strafe(-2.0F, 0.0F);
            strafeTicks--;
            if (strafeTicks == 0)
                chargePhase = ChargePhases.CHARGE;
        } else if (chargePhase == ChargePhases.CHARGE) {
            if (!owner.isHandActive())
                owner.setActiveHand(Hand.OFF_HAND);
            if (owner.getItemInUseMaxCount() >= owner.getActiveItemStack().getUseDuration())
                chargePhase = ChargePhases.FINISH;
        }
    }

    @Override
    public void startExecuting() {
        owner.setAggroed(true);
        chargePhase = ChargePhases.STRAFE;
        strafeTicks = 20;
    }

    @Override
    public void resetTask() {
        owner.resetActiveHand();
        owner.setAggroed(false);
    }

    public enum ChargePhases {
        NONE, STRAFE, CHARGE, FINISH;
    }
}
