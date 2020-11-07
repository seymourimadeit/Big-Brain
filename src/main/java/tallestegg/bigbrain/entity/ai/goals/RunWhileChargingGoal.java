package tallestegg.bigbrain.entity.ai.goals;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class RunWhileChargingGoal extends RandomWalkingGoal {

    public RunWhileChargingGoal(CreatureEntity creatureIn, double speedIn) {
        super(creatureIn, speedIn);
    }

    @Override
    public boolean shouldExecute() {
        return ((PillagerEntity) creature).isCharging() && creature.getAttackTarget() != null && this.findPosition();
    }

    public boolean findPosition() {
        Vector3d vector3d = this.getPosition();
        if (vector3d == null) {
            return false;
        } else {
            this.x = vector3d.x;
            this.y = vector3d.y;
            this.z = vector3d.z;
            return true;
        }
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        if (creature.getAttackTarget() != null) {
            creature.faceEntity(creature.getAttackTarget(), 30.0F, 30.0F);
            creature.getLookController().setLookPositionWithEntity(creature.getAttackTarget(), 30.0F, 30.0F);
        }
    }

    @Override
    @Nullable
    protected Vector3d getPosition() {
        return RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, this.creature.getAttackTarget().getPositionVec());
    }

    @Nullable
    protected BlockPos getRandPos(IBlockReader worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
        BlockPos blockpos = entityIn.getPosition();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
        float f = (float) (horizontalRange * horizontalRange * verticalRange * 2);
        BlockPos blockpos1 = null;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int l = i - horizontalRange; l <= i + horizontalRange; ++l) {
            for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1) {
                for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1) {
                    blockpos$mutable.setPos(l, i1, j1);
                    if (worldIn.getFluidState(blockpos$mutable).isTagged(FluidTags.WATER)) {
                        float f1 = (float) ((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));
                        if (f1 < f) {
                            f = f1;
                            blockpos1 = new BlockPos(blockpos$mutable);
                        }
                    }
                }
            }
        }

        return blockpos1;
    }
}