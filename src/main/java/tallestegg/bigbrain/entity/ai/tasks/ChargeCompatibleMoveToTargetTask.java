package tallestegg.bigbrain.entity.ai.tasks;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.entity.IBucklerUser;

public class ChargeCompatibleMoveToTargetTask extends MoveToTargetTask {

    public ChargeCompatibleMoveToTargetTask(float speed) {
        super(speed);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
        if (!((IBucklerUser) entityIn).isCharging()) {
            super.startExecuting(worldIn, entityIn, gameTimeIn);
        }
    }
}
