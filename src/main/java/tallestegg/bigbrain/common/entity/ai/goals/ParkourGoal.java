package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.BigBrainConfig;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiPredicate;

public class ParkourGoal extends Goal {
    private static final int[] JUMP_ANGLES = {65, 70, 75, 80};
    protected final float maxJumpVelocity;
    private final Mob mob;
    private final BiPredicate<Mob, BlockPos> acceptableLandingSpot = ParkourGoal::defaultAcceptableLandingSpot;
    public JumpPhases phase;
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected BlockPos posToJump;
    protected int findJumpTries;
    protected long tryAgainTime;
    private int currentSearchDistance = 1;
    private Direction searchDirection;
    private BlockPos cachedMobPos;

    public ParkourGoal(Mob mob) {
        this.mob = mob;
        this.maxJumpVelocity = 1.5F;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E mob, BlockPos pos) {
        return mob instanceof PathfinderMob && GoalUtils.isSolid((PathfinderMob) mob, pos.below());
    }

    public boolean canJump() {
        long time = this.mob.level().getGameTime();
        if (time - this.tryAgainTime <= 100L) return false;

        Path path = this.mob.getNavigation().getPath();
        return path != null && !path.canReach();
    }

    @Override
    public boolean canUse() {
        if (this.mob.onGround() && this.mob.getNavigation().isInProgress()) {
            if (!this.canJump())
                return false;
            if (BigBrainConfig.COMMON.jumpOnlyIfTargeting.get())
                return this.mob.getTarget() != null;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.phase == JumpPhases.SEARCHING || this.chosenJump != null)
                && this.findJumpTries > 0
                && !mob.isInWaterOrBubble();
    }

    @Override
    public void start() {
        this.phase = JumpPhases.SEARCHING;
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.mob.setYRot(this.mob.getYHeadRot());
        this.currentSearchDistance = 1;
        this.searchDirection = this.mob.getNearestViewDirection();
        BlockPos currentPos = this.mob.blockPosition();
        this.cachedMobPos = currentPos;
        this.initialPosition = Optional.of(Vec3.atLowerCornerOf(currentPos));
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.phase == JumpPhases.SEARCHING) {
            pickNextCandidateTick();
            --this.findJumpTries;
            if (this.currentSearchDistance >= 8 && this.chosenJump == null) {
                this.phase = JumpPhases.END;
            }
            return;
        }

        if (this.phase == JumpPhases.JUMP) {
            Vec3 jump = this.chosenJump;
            if (jump != null) {
                this.leapTowards(mob, this.mob.position().add(jump), jump.length(), 0.0F);
                this.mob.getJumpControl().jump();
                this.chosenJump = null;
                this.phase = JumpPhases.END;
            }
        }
    }

    @Override
    public void stop() {
        this.phase = JumpPhases.END;
        this.tryAgainTime = this.mob.level().getGameTime();
        this.chosenJump = null;
    }

    protected void pickNextCandidateTick() {
        BlockPos jumpPos = this.cachedMobPos.relative(this.searchDirection, this.currentSearchDistance++);
        Level level = this.mob.level();

        if (!level.getBlockState(jumpPos).isAir() || !this.isAcceptableLandingPosition(this.mob, jumpPos)) {
            return;
        }

        Vec3 targetVec = Vec3.atCenterOf(jumpPos);
        Vec3 jumpVec = this.calculateOptimalJumpVector(this.mob, targetVec);

        if (jumpVec != null) {
            this.posToJump = jumpPos;
            this.chosenJump = jumpVec;
            this.phase = JumpPhases.JUMP;
        }
    }

    private boolean isAcceptableLandingPosition(Mob pEntity, BlockPos pPos) {
        BlockPos blockpos = this.cachedMobPos;
        if (blockpos.getX() == pPos.getX() && blockpos.getZ() == pPos.getZ()) {
            return false;
        }
        return this.acceptableLandingSpot.test(pEntity, pPos);
    }

    @Nullable
    protected Vec3 calculateOptimalJumpVector(Mob pMob, Vec3 pTarget) {
        int len = JUMP_ANGLES.length;
        int startOffset = pMob.getRandom().nextInt(len);
        float maxVel = this.maxJumpVelocity;

        for (int i = 0; i < len; i++) {
            int angle = JUMP_ANGLES[(startOffset + i) % len];
            Optional<Vec3> vec3Opt = LongJumpUtil.calculateJumpVectorForAngle(pMob, pTarget, maxVel, angle, false);
            if (vec3Opt.isPresent()) {
                return vec3Opt.get();
            }
        }
        return null;
    }

    private void leapTowards(LivingEntity entity, Vec3 target, double horzVel, double yVel) {
        Vec3 currentMovement = entity.getDeltaMovement();
        Vec3 entityPos = entity.position();

        double targetX = target.x - entityPos.x;
        double targetZ = target.z - entityPos.z;
        double distanceSq = targetX * targetX + targetZ * targetZ;

        if (distanceSq < 1.0E-7D) return;

        double invLength = 1.0D / Math.sqrt(distanceSq);
        double dirX = targetX * invLength;
        double dirZ = targetZ * invLength;

        double leapX = dirX * horzVel;
        double leapZ = dirZ * horzVel;

        if (yVel != 0.0F) {
            float rad = (float) (yVel * (Math.PI / 180.0D));
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            double rx = leapX * cos + leapZ * sin;
            double rz = leapZ * cos - leapX * sin;
            leapX = rx;
            leapZ = rz;
        }

        double curY = currentMovement.y;
        float clampedYVelocity = (float) (curY < 0.1D ? 0.0D : 0.0D);
        double horzVelocityX = currentMovement.x + leapX;
        double horzVelocityZ = currentMovement.z + leapZ;
        double horzLen = Math.sqrt(horzVelocityX * horzVelocityX + horzVelocityZ * horzVelocityZ);
        double scale = horzVel / horzLen;
        if (scale < 1.0D) {
            horzVelocityX *= scale;
            horzVelocityZ *= scale;
        }
        entity.setDeltaMovement(new Vec3(horzVelocityX, clampedYVelocity, horzVelocityZ));
    }

    public enum JumpPhases {
        NONE,
        SEARCHING,
        JUMP,
        END
    }
}