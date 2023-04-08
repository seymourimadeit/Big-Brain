package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

import java.util.EnumSet;

public class HuskBurrowGoal extends Goal {
    protected Husk husk;
    private int burrowTime;
    private int waitUntilDigTime;
    private BurrowPhases phase;
    private long canUseCheck;
    private int seeTime;

    public HuskBurrowGoal(Husk husk) {
        this.husk = husk;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.husk.level.getGameTime();
        LivingEntity target = this.husk.getTarget();
        return (gameTime - canUseCheck > 100L) && !this.husk.isBaby() && target != null && !target.isPassenger() && target.distanceTo(this.husk) >= 5.0D && target instanceof Player && this.husk.getBlockStateOn().is(BlockTags.SAND);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.husk.getTarget();
        return target != null && this.phase != BurrowPhases.STOP || this.entityInWall(this.husk) && this.phase == BurrowPhases.DIG_OUT && this.phase != BurrowPhases.STOP;
    }

    @Override
    public void start() {
        this.husk.setAggressive(true);
        this.burrowTime = 40;
        this.waitUntilDigTime = 40;
        this.phase = BurrowPhases.START;
    }

    @Override
    public void tick() {
        LivingEntity target = this.husk.getTarget();
        Path path = this.husk.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Node node = path.getPreviousNode();
            Node nextNode = path.getNextNode();
            if (node != null) {
                if (nextNode != null)
                    if (!this.husk.level.getBlockState(nextNode.asBlockPos()).is(BlockTags.SAND) || !this.husk.level.getBlockState(node.asBlockPos()).is(BlockTags.SAND))
                        return;
            }
        }
        if (this.phase == BurrowPhases.BURROW)
            --this.burrowTime;
        if (target != null) {
            boolean canSee = this.husk.getSensing().hasLineOfSight(target);
            if (canSee) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }
            if (canSee != this.seeTime > 0)
                this.seeTime = 0;
            if (this.phase == BurrowPhases.START) {
                this.husk.setPose(Pose.SWIMMING);
                BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(true);
                this.phase = BurrowPhases.BURROW;
            } else if (this.phase == BurrowPhases.BURROW) {
                this.husk.getNavigation().moveTo(target, 1.8D);
                if (this.husk.isWithinMeleeAttackRange(target)) {
                    this.husk.getNavigation().stop();
                    this.husk.setPose(Pose.STANDING);
                    this.phase = BurrowPhases.GRAB;
                }
            }
            if (this.phase == BurrowPhases.GRAB) {
                target.startRiding(this.husk, true);
                this.phase = BurrowPhases.SINK;
            }
        }
        if (this.phase == BurrowPhases.SINK) {
            this.waitUntilDigTime--;
            if (this.waitUntilDigTime <= 0) {
                BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(false);
                this.husk.noPhysics = true;
                this.husk.setDeltaMovement(0.0D, -4.0D, 0.0D);
                this.phase = BurrowPhases.DIG_OUT;
            }
        } else if (this.phase == BurrowPhases.DIG_OUT) {
            if (this.entityInWall(this.husk)) {
                this.husk.ejectPassengers();
                this.husk.setDeltaMovement(0.0, 0.5D, 0.0D);
            } else {
                this.phase = BurrowPhases.END;
            }
        }
        if (this.phase == BurrowPhases.END || this.husk.getLastDamageSource() != null && this.husk.lastHurt >= (this.husk.getMaxHealth() / 2.0F) || this.burrowTime <= 0 && this.phase == BurrowPhases.BURROW || target != null && !this.husk.getSensing().hasLineOfSight(target) && this.seeTime < -60) {
            BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(false);
            this.phase = BurrowPhases.STOP;
        }
    }

    @Override
    public void stop() {
        BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(false);
        this.husk.setPose(Pose.STANDING);
        this.husk.noPhysics = false;
        this.husk.ejectPassengers();
        this.canUseCheck = this.husk.level.getGameTime();
        this.burrowTime = 0;
        this.waitUntilDigTime = 0;
        this.husk.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean entityInWall(Husk husk) {
        float f = husk.getDimensions(husk.getPose()).width * 0.8F;
        AABB aabb = AABB.ofSize(husk.getEyePosition(), (double) f, 1.0E-6D, (double) f);
        return BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = husk.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(husk.level, p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(husk.level, p_201942_).move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        });
    }

    public enum BurrowPhases {
        START, BURROW, GRAB, SINK, DIG_OUT, END, STOP
    }
}
