package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.passive.TameableEntity;

public class FindShelterGoal extends FleeSunGoal {
    private final CreatureEntity entity;

    public FindShelterGoal(CreatureEntity entity) {
        super(entity, 1.35D);
        this.entity = entity;
    }

    @Override
    public boolean shouldExecute() {
        boolean raining = entity.getEntityWorld().isNightTime() || entity.getEntityWorld().isRainingAt(entity.getPosition());
        boolean isTamed = entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
        return raining && !isTamed && entity.isServerWorld() && this.isPossibleShelter() && this.entity.getEntityWorld().canSeeSky(entity.getPosition());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.entity.getNavigator().noPath() && this.shouldExecute() && this.entity.getEntityWorld().canSeeSky(entity.getPosition());
    }
}
