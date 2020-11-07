package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;

public class LookGoalTest extends LookAtGoal {

    public LookGoalTest(MobEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance, float chanceIn) {
        super(entityIn, watchTargetClass, maxDistance, chanceIn);
    }
    
    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && this.entity.getAttackTarget() == null;
    }

}
