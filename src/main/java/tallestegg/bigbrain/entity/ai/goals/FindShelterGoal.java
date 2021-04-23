package tallestegg.bigbrain.entity.ai.goals;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.bigbrain.BigBrainConfig;

public class FindShelterGoal extends FleeSunGoal {
    protected double shelterX;
    protected double shelterY;
    protected double shelterZ;

    public FindShelterGoal(CreatureEntity entity) {
        super(entity, 1.35D);
    }

    @Override
    public boolean shouldExecute() {
        boolean raining = creature.getEntityWorld().isNightTime() && !BigBrainConfig.NightAnimalBlackList.contains(creature.getEntityString()) || !BigBrainConfig.RainAnimalBlackList.contains(creature.getEntityString()) && creature.getEntityWorld().isRainingAt(creature.getPosition());
        boolean isTamed = creature instanceof TameableEntity && ((TameableEntity) creature).isTamed() || creature instanceof AbstractHorseEntity && ((AbstractHorseEntity) creature).getOwnerUniqueId() != null;
        return !this.creature.world.isRemote() && raining && !isTamed && !creature.isBeingRidden() && creature.getAttackTarget() == null && this.creature.getEntityWorld().canSeeSky(creature.getPosition()) && this.isPossibleShelter();
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, 1.35D);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.hasPath();
    }

    @Override
    public void tick() {
    }

    protected boolean isPossibleShelter() {
        Vector3d vector3d = this.findPossibleShelter();
        if (vector3d == null) {
            return false;
        } else {
            this.shelterX = vector3d.x;
            this.shelterY = vector3d.y;
            this.shelterZ = vector3d.z;
            return true;
        }
    }

    @Override
    @Nullable
    protected Vector3d findPossibleShelter() {
        Random random = this.creature.getRNG();
        BlockPos blockpos = this.creature.getPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.creature.world.canSeeSky(blockpos1)) {
                return Vector3d.copyCenteredHorizontally(blockpos1);
            }
        }
        return null;
    }
}