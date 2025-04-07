package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.BigBrainConfig;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class NewBowAttackGoal<T extends PathfinderMob & RangedAttackMob> extends Goal {
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final T mob;
    private int attackIntervalMin;
    private Path path;
    private int attackTime = -1;
    private int seeTime;
    private int avoidTime;
    private int arrowsShot = 0;

    public NewBowAttackGoal(T pMob, double pSpeedModifier, int pAttackIntervalMin, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && mob.getMainHandItem().getItem() instanceof BowItem;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target != null) {
            double distanceSquared = mob.distanceToSqr(target);
            boolean canSee = mob.getSensing().hasLineOfSight(target);
            boolean seeTimeGreaterThanZero = this.seeTime > 0;
            this.mob.getLookControl().setLookAt(target);
            this.mob.lookAt(target, 30.0F, 30.0F);
            if (mob.isUsingItem()) {
                if (!canSee && this.seeTime < -60) {
                    mob.stopUsingItem();
                } else if (canSee) {
                    int i = mob.getTicksUsingItem();
                    int timeToShoot = distanceSquared <= 40.0D && BigBrainConfig.COMMON.bowAiCloseRange.get() ? Mth.floor(Mth.lerp(distanceSquared / (double) this.attackRadiusSqr, 5.0D, 20.0D)) : 20;
                    if (i >= timeToShoot) {
                        mob.stopUsingItem();
                        mob.performRangedAttack(target, BowItem.getPowerForTime(i));
                        this.arrowsShot++;
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(mob, item -> item instanceof BowItem));
            }
            if (distanceSquared > (double) this.attackRadiusSqr && this.seeTime >= 20) {
                this.path = mob.getNavigation().createPath(target, 0);
                this.mob.getNavigation().moveTo(this.path, this.speedModifier);
            } else if (distanceSquared < (double) this.attackRadiusSqr && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
            }
            if (canSee != seeTimeGreaterThanZero)
                this.seeTime = 0;
            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }
            if (distanceSquared <= 6.0D || arrowsShot >= 3) {
                if (this.avoidTime <= 0)
                    this.avoidTime = 60;
                else
                    this.avoidTime -= 10;
                this.arrowsShot = 0;
            }
            if (this.avoidTime <= 0)
                this.mob.getNavigation().stop();
            if (this.avoidTime > 60)
                this.avoidTime = 60;
            if (--this.avoidTime > 0) {
                Vec3 vec3 = this.getPosition(this.mob);
                if (distanceSquared <= this.attackRadiusSqr) {
                    if (vec3 != null && mob.getNavigation().isDone()) {
                        this.path = mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                        this.mob.getLookControl().setLookAt(vec3.x, mob.getEyeY(), vec3.z);
                        if (this.path != null && this.path.canReach()) {
                            this.mob.getNavigation().moveTo(this.path, this.speedModifier);
                            this.attackTime = -1;
                            this.mob.stopUsingItem();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        mob.stopUsingItem();
    }

    @Nullable
    protected Vec3 getPosition(T mob) {
        if (mob.getTarget() != null)
            return LandRandomPos.getPosAway(mob, 5, 7, mob.getTarget().position());
        else
            return LandRandomPos.getPos(mob, 5, 7);
    }
}
