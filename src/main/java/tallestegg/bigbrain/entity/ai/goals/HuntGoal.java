package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;

public class HuntGoal <T extends LivingEntity> extends NearestAttackableTargetGoal<T>{

    public HuntGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
        super(goalOwnerIn, targetClassIn, checkSight);
    }
    
    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && this.goalOwner.getRNG().nextFloat() < 15.0F;
    }
}
