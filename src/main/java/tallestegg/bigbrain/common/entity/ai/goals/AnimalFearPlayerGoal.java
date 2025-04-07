package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

import java.util.EnumSet;

public class AnimalFearPlayerGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    public AnimalFearPlayerGoal(PathfinderMob pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
        super(pMob, pEntityClassToAvoid, p_25052_ -> true, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && mob.getData((BigBrainCapabilities.SAW_HUNT.get())) > 0;
    }

    @Override
    public void tick() {
        super.tick();
        mob.setData(BigBrainCapabilities.SAW_HUNT.get(), mob.getData((BigBrainCapabilities.SAW_HUNT.get())) - 1);
    }
}
