package tallestegg.bigbrain.entity.ai.tasks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.entity.IBucklerUser;

public class ChargeCompatibleAttackTargetTask extends AttackTargetTask {
    protected final int cooldown;

    public ChargeCompatibleAttackTargetTask(int cooldown) {
        super(cooldown);
        this.cooldown = cooldown;
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
        return (!((IBucklerUser) entityIn).isCharging());
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
        LivingEntity livingentity = this.getAttackTarget(entityIn);
        if (!((IBucklerUser) entityIn).isCharging())
            BrainUtil.lookAt(entityIn, livingentity);
        entityIn.swingArm(Hand.MAIN_HAND);
        entityIn.attackEntityAsMob(livingentity);
        entityIn.getBrain().replaceMemory(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long) this.cooldown);
    }

    protected LivingEntity getAttackTarget(MobEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }
}
