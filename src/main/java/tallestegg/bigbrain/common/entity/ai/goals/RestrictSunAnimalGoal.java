package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import tallestegg.bigbrain.BigBrainConfig;

public class RestrictSunAnimalGoal extends RestrictSunGoal {
    private final PathfinderMob mob;

    public RestrictSunAnimalGoal(PathfinderMob mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        boolean raining = mob.getCommandSenderWorld().isNight()
                && !BigBrainConfig.NightAnimalBlackList.contains(mob.getEncodeId())
                || !BigBrainConfig.RainAnimalBlackList.contains(mob.getEncodeId())
                        && mob.getCommandSenderWorld().isRainingAt(mob.blockPosition());
        boolean isTamed = mob instanceof TamableAnimal && ((TamableAnimal) mob).isTame()
                || mob instanceof AbstractHorse && ((AbstractHorse) mob).getOwnerUUID() != null;
        return GoalUtils.hasGroundPathNavigation(this.mob) && raining && !isTamed && !mob.isVehicle() && mob.getTarget() == null;
    }
}
