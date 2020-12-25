package tallestegg.bigbrain.entity.ai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.entity.IBucklerUser;

public class ChargeTask<T extends PiglinBruteEntity> extends Task<T> {

    public ChargeTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, T owner) {
        LivingEntity livingentity = this.getAttackTarget(owner);
        return livingentity.getDistance(owner) > 3.0D;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        return entityIn.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET) && this.shouldExecute(worldIn, entityIn);
    }

    private LivingEntity getAttackTarget(MobEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private boolean isRanged(MobEntity mob) {
        return mob.func_233634_a_((item) -> {
            return item instanceof ShootableItem && mob.func_230280_a_((ShootableItem) item);
        });
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        if (!entityIn.isHandActive()) {
            entityIn.setActiveHand(Hand.OFF_HAND);
        }
        entityIn.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_CONVRTED_TO_ZOMBIFIED, 1.0F, entityIn.isChild() ? (entityIn.getRNG().nextFloat() - entityIn.getRNG().nextFloat()) * 0.2F + 1.5F : (entityIn.getRNG().nextFloat() - entityIn.getRNG().nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    protected void resetTask(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        // entityIn.resetActiveHand();
    }
}
