package tallestegg.bigbrain.entity.ai.goals;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.bigbrain.BigBrainConfig;

public class FindShelterGoal extends FleeSunGoal {
    protected final CreatureEntity entity;

    public FindShelterGoal(CreatureEntity entity) {
        super(entity, 1.35D);
        this.entity = entity;
    }

    @Override
    public boolean shouldExecute() {
        boolean raining = entity.getEntityWorld().isNightTime() && !BigBrainConfig.NightAnimalBlackList.contains(entity.getEntityString()) || !BigBrainConfig.RainAnimalBlackList.contains(entity.getEntityString()) && entity.getEntityWorld().isRainingAt(entity.getPosition());
        boolean isTamed = entity instanceof TameableEntity && ((TameableEntity) entity).isTamed() || entity instanceof AbstractHorseEntity && ((AbstractHorseEntity) entity).getOwnerUniqueId() != null;
        return raining && !isTamed && !entity.isBeingRidden() && entity.getAttackTarget() == null && this.isPossibleShelter() && this.entity.getEntityWorld().canSeeSky(entity.getPosition());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return super.shouldContinueExecuting() && this.shouldExecute() && this.entity.getEntityWorld().canSeeSky(entity.getPosition());
    }

    @Override
    @Nullable
    protected Vector3d findPossibleShelter() {
        Random random = this.creature.getRNG();
        BlockPos blockpos = this.creature.getPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.entity.world.canSeeSky(blockpos1) && this.creature.getBlockPathWeight(blockpos1) < 0.0F && this.creature.world.getBlockState(blockpos1) != Blocks.WATER.getDefaultState())
                return Vector3d.copyCenteredHorizontally(blockpos1);

        }
        return null;
    }
}
