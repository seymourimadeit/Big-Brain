package tallestegg.bigbrain.entity.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

public class ChargeTask<T extends PiglinBruteEntity> extends Task<T> {

    private ChargePhases chargePhase = ChargePhases.NONE;
    private int strafeTicks;

    public ChargeTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, T owner) {
        LivingEntity livingentity = this.getAttackTarget(owner);
        return livingentity.getDistance(owner) > 4.0D;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        return entityIn.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET) && this.shouldExecute(worldIn, entityIn) && chargePhase != ChargePhases.FINISH;
    }

    private LivingEntity getAttackTarget(MobEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected void updateTask(ServerWorld worldIn, T entityIn, long gameTime) {
        entityIn.faceEntity(this.getAttackTarget(entityIn), 30.0F, 30.0F);
        if (chargePhase == ChargePhases.STRAFE && strafeTicks > 0) {
            entityIn.getMoveHelper().strafe(-2.0F, 0.0F);
            strafeTicks--;
            if (strafeTicks == 0)
                chargePhase = ChargePhases.CHARGE;
        } else if (chargePhase == ChargePhases.CHARGE) {
            if (!entityIn.isHandActive()) {
                entityIn.setActiveHand(Hand.OFF_HAND);
                chargePhase = ChargePhases.FINISH;
            }
        }
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        chargePhase = ChargePhases.STRAFE;
        strafeTicks = 15;
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
