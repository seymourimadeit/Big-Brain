package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.BigBrainConfig;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FindShelterGoal extends Goal {
    protected final PathfinderMob mob;
    private final Level level;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private long canUseCheck;

    public FindShelterGoal(PathfinderMob entity) {
        this.mob = entity;
        this.level = entity.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.level.getGameTime();
        boolean raining = !this.level.isDay() && !BigBrainConfig.NightAnimalBlackList.contains(this.mob.getEncodeId()) || !BigBrainConfig.RainAnimalBlackList.contains(mob.getEncodeId()) && mob.level().isRainingAt(mob.blockPosition());
        boolean isTamed = this.mob instanceof TamableAnimal && ((TamableAnimal) mob).isTame() || mob instanceof AbstractHorse && ((AbstractHorse) mob).getOwnerUUID() != null;
        if (this.setWantedPos())
            return raining && !isTamed && !mob.isVehicle() && mob.getTarget() == null && this.mob.level().canSeeSky(mob.blockPosition());
        return (gameTime - this.canUseCheck) > 2400L;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, 1.35D);
        this.mob.getLookControl().setLookAt(this.wantedX, this.wantedY, this.wantedZ);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.canUseCheck = this.mob.level().getGameTime();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() || ( this.mob.getNavigation().getPath() != null && this.mob.getNavigation().getPath().canReach());
    }

    protected boolean setWantedPos() {
        Vec3 vec3 = this.getHidePos();
        if (vec3 == null) {
            return false;
        } else {
            this.wantedX = vec3.x;
            this.wantedY = vec3.y;
            this.wantedZ = vec3.z;
            return true;
        }
    }

    @Nullable
    protected Vec3 getHidePos() {
        RandomSource randomsource = this.mob.getRandom();
        BlockPos blockpos = this.mob.blockPosition();
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos1 = blockpos.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);
            BlockPos position = new BlockPos(blockpos1.getX(), blockpos1.getY() + 2, blockpos1.getZ());
            if (!this.level.canSeeSky(position) && position.closerThan(mob.blockPosition(), 20.0D)) {
                return Vec3.atBottomCenterOf(position);
            } else {
                return null;
            }
        }
        return null;
    }
}