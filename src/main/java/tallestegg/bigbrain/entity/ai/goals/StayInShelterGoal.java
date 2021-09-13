package tallestegg.bigbrain.entity.ai.goals;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.BigBrainConfig;

public class StayInShelterGoal extends RandomStrollGoal {
    public StayInShelterGoal(PathfinderMob creatureIn, double speedIn) {
        super(creatureIn, 1.35D);
    }

    @Override
    public boolean canUse() {
        boolean raining = mob.getCommandSenderWorld().isNight()
                && !BigBrainConfig.NightAnimalBlackList.contains(mob.getEncodeId())
                || !BigBrainConfig.RainAnimalBlackList.contains(mob.getEncodeId())
                        && mob.getCommandSenderWorld().isRainingAt(mob.blockPosition());
        boolean isTamed = mob instanceof TamableAnimal && ((TamableAnimal) mob).isTame()
                || mob instanceof AbstractHorse && ((AbstractHorse) mob).getOwnerUUID() != null;
        return raining && !isTamed && !mob.isVehicle() && mob.getTarget() == null
                && !this.mob.getCommandSenderWorld().canSeeSky(mob.blockPosition()) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.targetPosCanSeeSky();
    }

    public boolean targetPosCanSeeSky() {
        return mob.getNavigation().getTargetPos() != null
                && !this.mob.getCommandSenderWorld().canSeeSky(mob.getNavigation().getTargetPos());
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        Random random = this.mob.getRandom();
        BlockPos blockpos = this.mob.blockPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.offset(random.nextInt(10) - 5, random.nextInt(4) - 2, random.nextInt(10) - 5);
            if (!this.mob.level.canSeeSky(blockpos1))
                return Vec3.atBottomCenterOf(blockpos1);

        }
        return null;
    }
}
