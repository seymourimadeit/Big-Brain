package tallestegg.bigbrain.common.entity.ai.goals;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.BigBrainConfig;

public class FindShelterGoal extends FleeSunGoal {
    public FindShelterGoal(PathfinderMob entity) {
        super(entity, 1.35D);
    }

    @Override
    public boolean canUse() {
        boolean raining = mob.getCommandSenderWorld().isNight() && !BigBrainConfig.NightAnimalBlackList.contains(mob.getEncodeId()) || !BigBrainConfig.RainAnimalBlackList.contains(mob.getEncodeId()) && mob.getCommandSenderWorld().isRainingAt(mob.blockPosition());
        boolean isTamed = mob instanceof TamableAnimal && ((TamableAnimal) mob).isTame() || mob instanceof AbstractHorse && ((AbstractHorse) mob).getOwnerUUID() != null;
        return raining && !isTamed && !mob.isVehicle() && mob.getTarget() == null && this.mob.getCommandSenderWorld().canSeeSky(mob.blockPosition()) && this.setWantedPos();
    }

    @Override
    @Nullable
    protected Vec3 getHidePos() {
        RandomSource random = this.mob.getRandom();
        BlockPos blockpos = this.mob.blockPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.mob.level.canSeeSky(blockpos1) && this.mob.level.getFluidState(blockpos1).isEmpty())
                return Vec3.atBottomCenterOf(blockpos1);

        }
        return null;
    }
}