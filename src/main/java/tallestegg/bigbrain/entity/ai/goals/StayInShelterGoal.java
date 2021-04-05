package tallestegg.bigbrain.entity.ai.goals;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class StayInShelterGoal extends RandomWalkingGoal {

    public StayInShelterGoal(CreatureEntity creatureIn, double speedIn) {
        super(creatureIn, 1.35D);
    }

    @Override
    public boolean shouldExecute() {
        boolean raining = creature.getEntityWorld().isNightTime() || creature.getEntityWorld().isRainingAt(creature.getPosition());
        boolean isTamed = creature instanceof TameableEntity && ((TameableEntity) creature).isTamed();
        return raining && !isTamed && creature.getAttackTarget() == null && !this.creature.getEntityWorld().canSeeSky(creature.getPosition()) && super.shouldExecute();
    }

    @Override
    @Nullable
    protected Vector3d getPosition() {
        Random random = this.creature.getRNG();
        BlockPos blockpos = this.creature.getPosition();
        for (int i = 0; i < 2; ++i) {
            BlockPos blockpos1 = blockpos.add(random.nextInt(10) - 5, random.nextInt(4) - 2, random.nextInt(10) - 5);
            if (!this.creature.world.canSeeSky(blockpos1))
                return Vector3d.copyCenteredHorizontally(blockpos1);

        }
        return null;
    }
}
