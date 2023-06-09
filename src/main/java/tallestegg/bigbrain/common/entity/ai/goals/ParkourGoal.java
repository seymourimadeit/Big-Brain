package tallestegg.bigbrain.common.entity.ai.goals;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class ParkourGoal extends Goal {
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
    protected final float maxJumpVelocity;
    private final Mob mob;
    private final BiPredicate<Mob, BlockPos> acceptableLandingSpot = ParkourGoal::defaultAcceptableLandingSpot;
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected BlockPos posToJump;
    protected int findJumpTries;
    protected int failedToFindJumpCounter;
    protected long tryAgainTime;

    public ParkourGoal(Mob mob) {
        this.mob = mob;
        this.maxJumpVelocity = 1.5F;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E mob, BlockPos pos) {
        Level level = mob.level;
        BlockPos blockpos = pos.below();
        return level.getBlockState(blockpos).isSolidRender(level, blockpos);
    }

    @Override
    public boolean canUse() {
        if (this.mob.getNavigation() != null && this.mob.isOnGround()) {
            Path path = this.mob.getNavigation().getPath();
            return this.mob.getNavigation().isInProgress() && path != null && !path.canReach() &&  (this.mob.getLevel().getGameTime() - tryAgainTime > 100L);
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && !mob.isInWaterOrBubble() && this.chosenJump != null && this.failedToFindJumpCounter >= 2;
        return flag;
    }

    @Override
    public void start() {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(mob.position());
        if (this.mob.getNavigation() == null)
            return;
        Vec3 pos = Vec3.atCenterOf(this.mob.getNavigation().getTargetPos());
        this.mob.getLookControl().setLookAt(pos.x, pos.y, pos.z, 90.0F, 90.0F);
        this.mob.setYRot(this.mob.getYHeadRot());
        this.pickCandidate(mob, this.mob.getNavigation().getTargetPos());
    }

    @Override
    public void tick() {
        Vec3 pos = Vec3.atCenterOf(this.mob.getNavigation().getTargetPos());
        this.mob.getLookControl().setLookAt(pos.x, pos.y, pos.z, 90.0F, 90.0F);
        this.mob.setYRot(this.mob.getYHeadRot());
        if (this.chosenJump != null) {
            this.leapTowards(mob, this.mob.position().add(this.chosenJump), this.chosenJump.length(), 0.0F);
            this.mob.getJumpControl().jump();
        } else {
            --this.findJumpTries;
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        if (this.failedToFindJumpCounter >= 2) {
            this.tryAgainTime = this.mob.getLevel().getGameTime();
            this.failedToFindJumpCounter = 0;
        }
    }

    protected void pickCandidate(Mob pEntity, BlockPos block) {
        for (BlockPos pos : BlockPos.betweenClosed(pEntity.blockPosition(), block)) {
            BlockPos jumpPos = pEntity.blockPosition().closerThan(pos, 3.0D) ? pos : block;
            if (this.isAcceptableLandingPosition(pEntity, jumpPos)) {
                Vec3 vec3 = Vec3.atCenterOf(jumpPos);
                Vec3 vec31 = this.calculateOptimalJumpVector(pEntity, vec3);
                if (vec31 == null)
                    return;
                PathNavigation pathnavigation = pEntity.getNavigation();
                Path path = pathnavigation.createPath(jumpPos, 0, 8);
                if (path != null && !path.canReach()) {
                    if (vec31 != null) {
                        this.posToJump = jumpPos;
                        this.chosenJump = vec31;
                    }
                } else {
                    this.failedToFindJumpCounter++;
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
                return (new Vec3(d13 * d10, d14, d13 * d9)).scale(0.95F);
            }
        }
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

    /*public BlockType testForBlock(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        FluidState fluidState = level.getFluidState(pos);

        if (blockState.is(Blocks.SWEET_BERRY_BUSH) ||
                blockState.is(BlockTags.FIRE) ||
                CampfireBlock.isLitCampfire(blockState) ||
                fluidState.is(FluidTags.WATER))
            return BlockType.PASSABLE_OBSTACLE;
        if (fluidState.is(FluidTags.LAVA) ||
                blockState.is(Blocks.CACTUS) ||
                blockState.is(Blocks.HONEY_BLOCK) ||
                blockState.is(Blocks.MAGMA_BLOCK))
            return BlockType.SOLID_OBSTACLE;
        if (block instanceof LeavesBlock ||
                blockState.is(BlockTags.FENCES) ||
                blockState.is(BlockTags.WALLS) ||
                (block instanceof FenceGateBlock && !blockState.getValue(FenceGateBlock.OPEN) ||
                        (DoorBlock.isWoodenDoor(blockState) && !blockState.getValue(DoorBlock.OPEN)) ||
                        (block instanceof DoorBlock && material == Material.METAL && !blockState.getValue(DoorBlock.OPEN)) ||
                        (block instanceof DoorBlock && blockState.getValue(DoorBlock.OPEN)) ||
                        !blockState.isPathfindable(level, pos, PathComputationType.LAND))) {
            return BlockType.BLOCKED;
        }
        return null;
    }

    public enum BlockType {
        BLOCKED,
        SOLID_OBSTACLE,
        PASSABLE_OBSTACLE,
    }*/
}
