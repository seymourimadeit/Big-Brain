package tallestegg.bigbrain.entity.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

public class ChargeTask<T extends PiglinBruteEntity> extends Task<T> {

    private ChargePhases chargePhase = ChargePhases.NONE;
    private int strafeTicks;

    public ChargeTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, T owner) {
        LivingEntity livingentity = this.getAttackTarget(owner);
        return livingentity != null && livingentity.getDistance(owner) >= 4.0D && BrainUtil.isMobVisible(owner, livingentity) && ((IBucklerUser) owner).getCooldown() == BigBrainConfig.BucklerCooldown && owner.getHeldItemOffhand().getItem() instanceof BucklerItem && !owner.isInWaterRainOrBubbleColumn();
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        LivingEntity livingentity = this.getAttackTarget(entityIn);
        return livingentity != null && entityIn.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET) && ((IBucklerUser) entityIn).getCooldown() == BigBrainConfig.BucklerCooldown && entityIn.getHeldItemOffhand().getItem() instanceof BucklerItem
                && !entityIn.isInWaterRainOrBubbleColumn() && chargePhase != ChargePhases.FINISH;
    }

    private LivingEntity getAttackTarget(T mob) {
        return mob.getAttackTarget();
    }

    @Override
    protected void updateTask(ServerWorld worldIn, T entityIn, long gameTime) {
        LivingEntity livingEntity = this.getAttackTarget(entityIn);
        if (((IBucklerUser) entityIn).isBucklerDashing() && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), entityIn.getHeldItemOffhand()) > 0 || !((IBucklerUser) entityIn).isBucklerDashing()) {
            entityIn.faceEntity(livingEntity, 30.0F, 30.0F);
        }
        if (chargePhase == ChargePhases.STRAFE && strafeTicks > 0 && entityIn.getDistance(livingEntity) >= 4.0D && entityIn.getDistance(livingEntity) <= 10.0D) {
            entityIn.getMoveHelper().strafe(-2.0F, 0.0F);
            strafeTicks--;
            if (strafeTicks == 0)
                chargePhase = ChargePhases.CHARGE;
        } else if (chargePhase == ChargePhases.CHARGE) {
            if (!entityIn.isHandActive()) {
                entityIn.setActiveHand(Hand.OFF_HAND);
            }
            if (entityIn.getItemInUseMaxCount() >= entityIn.getActiveItemStack().getUseDuration())
                chargePhase = ChargePhases.FINISH;
        }
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        chargePhase = ChargePhases.STRAFE;
        strafeTicks = 20;
    }

    @Override
    protected void resetTask(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        if (entityIn.isHandActive())
            entityIn.resetActiveHand();
    }

    public enum ChargePhases {
        NONE, STRAFE, CHARGE, FINISH;
    }
}
