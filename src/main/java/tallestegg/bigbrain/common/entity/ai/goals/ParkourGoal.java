package tallestegg.bigbrain.common.entity.ai.goals;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class ParkourGoal extends Goal {
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocity;
    private final Mob mob;
    private final BiPredicate<Mob, BlockPos> acceptableLandingSpot = ParkourGoal::defaultAcceptableLandingSpot;
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected BlockPos posToJump;
    protected int findJumpTries;

    public ParkourGoal(Mob mob) {
        this.mob = mob;
        this.maxLongJumpHeight = 1;
        this.maxLongJumpWidth = 1;
        this.maxJumpVelocity = 1.5F;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E p_251540_, BlockPos p_248879_) {
        Level level = p_251540_.level;
        BlockPos blockpos = p_248879_.below();
        return level.getBlockState(blockpos).isSolidRender(level, blockpos) && p_251540_.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(level, p_248879_.mutable())) == 0.0F;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getNavigation() != null && this.mob.isOnGround()) {
            Path path = this.mob.getNavigation().getPath();
            return this.mob.getNavigation().isInProgress() && path != null && !path.canReach();
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && !mob.isInWaterOrBubble() && (this.chosenJump != null);
        return flag;
    }

    @Override
    public void start() {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(mob.position());
        if (this.mob.getNavigation() == null)
            return;
    }

    @Override
    public void tick() {
        if (this.chosenJump != null) {
            if (this.posToJump != null) {
                this.mob.getNavigation().moveTo(this.posToJump.getX(), this.posToJump.getY(), this.posToJump.getZ(), 1.25D);
            }
            this.leapTowards(mob, this.mob.position().add(this.chosenJump), this.chosenJump.length(), 0.0F);
            this.mob.getJumpControl().jump();
        } else {
            --this.findJumpTries;
            this.pickCandidate(mob, this.mob.getNavigation().getTargetPos());
        }
    }

    @Override
    public void stop() {
        this.mob.setDiscardFriction(false);
        this.mob.getNavigation().stop();
    }

    protected void pickCandidate(Mob pEntity, BlockPos block) {
        for (BlockPos pos : BlockPos.betweenClosed(pEntity.blockPosition(), block)) {
            BlockPos jumpPos = pEntity.blockPosition().closerThan(pos, 3.0D) ? pos : block;
            if (this.isAcceptableLandingPosition(pEntity, jumpPos)) {
                Vec3 vec3 = Vec3.atCenterOf(jumpPos);
                Vec3 vec31 = this.calculateOptimalJumpVector(pEntity, vec3);
                if (vec31 != null) {
                    this.mob.setYRot(this.mob.yBodyRot);
                    this.mob.getLookControl().setLookAt(Vec3.atCenterOf(block).x(), this.mob.getEyeY(), Vec3.atCenterOf(block).z());
                    this.posToJump = jumpPos;
                    this.chosenJump = vec31;
                }
            }
        }
    }

    private boolean isAcceptableLandingPosition(Mob pEntity, BlockPos pPos) {
        BlockPos blockpos = pEntity.blockPosition();
        int i = blockpos.getX();
        int j = blockpos.getZ();
        return i == pPos.getX() && j == pPos.getZ() ? false : this.acceptableLandingSpot.test(pEntity, pPos);
    }

    @Nullable
    protected Vec3 calculateOptimalJumpVector(Mob pMob, Vec3 pTarget) {
        List<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle(list);

        for (int i : list) {
            Vec3 vec3 = this.calculateJumpVectorForAngle(pMob, pTarget, i);
            if (vec3 != null) {
                return vec3;
            }
        }

        return null;
    }

    @Nullable
    private Vec3 calculateJumpVectorForAngle(Mob pMob, Vec3 pTarget, int pAngle) {
        Vec3 vec3 = pMob.position();
        Vec3 vec31 = (new Vec3(pTarget.x - vec3.x, 0.0D, pTarget.z - vec3.z)).normalize().scale(0.5D);
        pTarget = pTarget.subtract(vec31);
        Vec3 vec32 = pTarget.subtract(vec3);
        float f = (float) pAngle * (float) Math.PI / 180.0F;
        double d0 = Math.atan2(vec32.z, vec32.x);
        double d1 = vec32.subtract(0.0D, vec32.y, 0.0D).lengthSqr();
        double d2 = Math.sqrt(d1);
        double d3 = vec32.y;
        double d4 = Math.sin((double) (2.0F * f));
        double d5 = 0.08D;
        double d6 = Math.pow(Math.cos((double) f), 2.0D);
        double d7 = Math.sin((double) f);
        double d8 = Math.cos((double) f);
        double d9 = Math.sin(d0);
        double d10 = Math.cos(d0);
        double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
        if (d11 < 0.0D) {
            return null;
        } else {
            double d12 = Math.sqrt(d11);
            if (d12 > (double) this.maxJumpVelocity) {
                return null;
            } else {
                double d13 = d12 * d8;
                double d14 = d12 * d7;
                int i = Mth.ceil(d2 / d13) * 2;
                double d15 = 0.0D;
                Vec3 vec33 = null;
                EntityDimensions entitydimensions = pMob.getDimensions(Pose.STANDING);

                for (int j = 0; j < i - 1; ++j) {
                    d15 += d2 / (double) i;
                    double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
                    double d17 = d15 * d10;
                    double d18 = d15 * d9;
                    Vec3 vec34 = new Vec3(vec3.x + d17, vec3.y + d16, vec3.z + d18);
                    if (vec33 != null && !this.isClearTransition(pMob, entitydimensions, vec33, vec34)) {
                        return null;
                    }

                    vec33 = vec34;
                }

                return (new Vec3(d13 * d10, d14, d13 * d9)).scale((double) 0.95F);
            }
        }
    }

    private boolean isClearTransition(Mob pMob, EntityDimensions pDimensions, Vec3 pStart, Vec3 pEnd) {
        Vec3 vec3 = pEnd.subtract(pStart);
        double d0 = (double) Math.min(pDimensions.width, pDimensions.height);
        int i = Mth.ceil(vec3.length() / d0);
        Vec3 vec31 = vec3.normalize();
        Vec3 vec32 = pStart;

        for (int j = 0; j < i; ++j) {
            vec32 = j == i - 1 ? pEnd : vec32.add(vec31.scale(d0 * (double) 0.9F));
            if (!pMob.level.noCollision(pMob, pDimensions.makeBoundingBox(vec32))) {
                return false;
            }
        }

        return true;
    }

    private double getJumpVelocity(Level level, LivingEntity entity) {
        double baseVelocity = 0.42D * getJumpVelocityMultiplier(level, entity);
        if (entity.hasEffect(MobEffects.JUMP)) {
            baseVelocity += 0.1D * (entity.getEffect(MobEffects.JUMP).getAmplifier() + 1);
        }
        return baseVelocity;
    }

    private double getJumpVelocityMultiplier(Level level, LivingEntity entity) {
        float f1 = level.getBlockState(entity.blockPosition()).getBlock().getJumpFactor();
        float f2 = level.getBlockState(getVelocityAffectingPos(entity)).getBlock().getJumpFactor();
        return (f1 == 1.0) ? (double) f2 : (double) f1;
    }

    private BlockPos getVelocityAffectingPos(LivingEntity entity) {
        return floorBlockPos(entity.blockPosition().getX(), entity.getBoundingBox().minY - 0.5000001, entity.blockPosition().getZ());
    }

    private BlockPos floorBlockPos(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private BlockPos floorBlockPos(Vec3 pos) {
        return floorBlockPos(pos.x(), pos.y(), pos.z());
    }

    private void leapTowards(LivingEntity entity, Vec3 target, double horzVel, double yVel) {
        Vec3 dir = target.subtract(entity.position()).normalize();
        Vec3 leap = new Vec3(dir.x, 0.0, dir.z).normalize().scale(horzVel).yRot((float) yVel);
        float clampedYVelocity = (float) (entity.getDeltaMovement().y() < 0.1D ? leap.y : 0.0D);

        // Normalize to make sure the velocity doesn't go beyond what we expect
        Vec3 horzVelocity = entity.getDeltaMovement().add(leap.x, 0.0, leap.z);
        double scale = horzVel / horzVelocity.length();
        if (scale < 1.0D) {
            horzVelocity = horzVelocity.scale(scale);
        }
        ((Mob) entity).getLookControl().setLookAt(target);
        entity.setDeltaMovement(horzVelocity.yRot(clampedYVelocity));
    }
}
