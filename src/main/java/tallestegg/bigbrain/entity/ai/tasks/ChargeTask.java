package tallestegg.bigbrain.entity.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

public class ChargeTask<T extends PiglinBrute> extends Behavior<T> {
    private ChargePhases chargePhase = ChargePhases.NONE;
    private int strafeTicks;

    public ChargeTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, T owner) {
        LivingEntity livingentity = this.getAttackTarget(owner);
        return livingentity != null && livingentity.distanceTo(owner) >= 4.0D && BehaviorUtils.canSee(owner, livingentity) && ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getOffhandItem().getItem() instanceof BucklerItem && !owner.isInWaterRainOrBubble();
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        LivingEntity livingentity = this.getAttackTarget(entityIn);
        return livingentity != null && entityIn.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && ((IBucklerUser) entityIn).getCooldown() == BigBrainConfig.BucklerCooldown && entityIn.getOffhandItem().getItem() instanceof BucklerItem
                && !entityIn.isInWaterRainOrBubble() && chargePhase != ChargePhases.FINISH;
    }

    private LivingEntity getAttackTarget(T mob) {
        return mob.getTarget();
    }

    @Override
    protected void tick(ServerLevel worldIn, T entityIn, long gameTime) {
        LivingEntity livingEntity = this.getAttackTarget(entityIn);
        if (((IBucklerUser) entityIn).isBucklerDashing() && EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), entityIn.getOffhandItem()) > 0 || !((IBucklerUser) entityIn).isBucklerDashing()) {
            entityIn.lookAt(livingEntity, 30.0F, 30.0F);
        }
        if (chargePhase == ChargePhases.STRAFE && strafeTicks > 0 && entityIn.distanceTo(livingEntity) >= 4.0D && entityIn.distanceTo(livingEntity) <= 10.0D) {
            entityIn.getMoveControl().strafe(-2.0F, 0.0F);
            strafeTicks--;
            if (strafeTicks == 0)
                chargePhase = ChargePhases.CHARGE;
        } else if (chargePhase == ChargePhases.CHARGE) {
            if (!entityIn.isUsingItem()) {
                entityIn.startUsingItem(InteractionHand.OFF_HAND);
            }
            if (entityIn.getTicksUsingItem() >= entityIn.getUseItem().getUseDuration())
                chargePhase = ChargePhases.FINISH;
        }
    }

    @Override
    protected void start(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        chargePhase = ChargePhases.STRAFE;
        strafeTicks = 20;
    }

    @Override
    protected void stop(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        if (entityIn.isUsingItem())
            entityIn.stopUsingItem();
    }

    public enum ChargePhases {
        NONE, STRAFE, CHARGE, FINISH;
    }
}
