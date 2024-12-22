package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

import java.util.EnumSet;
import java.util.List;

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
        long gameTime = this.husk.level().getGameTime();
        LivingEntity target = this.husk.getTarget();
        return (gameTime - canUseCheck > 100L) && !this.husk.isBaby() && target != null && target.getVehicle() == null && target.distanceTo(this.husk) >= 5.0D && target instanceof Player && this.husk.getBlockStateOn().is(BlockTags.SAND);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.husk.getTarget();
        for (Husk nearbyEntities : husk.level().getEntitiesOfClass(husk.getClass(), husk.getBoundingBox().inflate(5.0D))) {
            boolean burrowing = nearbyEntities.getData(BigBrainCapabilities.BURROWING);
            return !burrowing && nearbyEntities != husk;
        }
        return target != null && burrowTime > 0 &&
                this.phase != BurrowPhases.STOP
                || this.entityInWall(this.husk) && this.phase == BurrowPhases.DIG_OUT
                && this.phase != BurrowPhases.STOP || this.husk.getLastDamageSource() != null && this.husk.lastHurt >= (this.husk.getMaxHealth() / 2.0F) || target != null && !this.husk.getSensing().hasLineOfSight(target) && this.seeTime > -60 || !this.husk.isEyeInFluidType(this.husk.getEyeInFluidType());
    }

    @Override
    public void start() {
        this.husk.setAggressive(true);
        this.burrowTime = 100;
        this.waitUntilDigTime = 40;
        this.phase = BurrowPhases.START;
    }

    @Override
    public void tick() {
        boolean burrowing = husk.getData(BigBrainCapabilities.BURROWING.get());
        if (burrowing) {
            --this.burrowTime;
        }
        LivingEntity target = this.husk.getTarget();
        Path path = this.husk.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Node node = path.getPreviousNode();
            Node nextNode = path.getNextNode();
            if (node != null) {
                if (nextNode != null)
                    if (!this.husk.level().getBlockState(nextNode.asBlockPos()).is(BlockTags.SAND) || !this.husk.level().getBlockState(node.asBlockPos()).is(BlockTags.SAND))
                        return;
            }
        }
        if (this.phase == BurrowPhases.END || this.husk.getLastDamageSource() != null && this.husk.lastHurt >= (this.husk.getMaxHealth() / 2.0F) || target != null && !this.husk.getSensing().hasLineOfSight(target) && this.seeTime < -60 || this.husk.isInFluidType()) {
            this.phase = BurrowPhases.STOP;
        }
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
                this.husk.setData(BigBrainCapabilities.BURROWING.get(), true);
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
                if (!target.isPassenger()) {
                    target.startRiding(this.husk, true);
                } else {
                    this.phase = BurrowPhases.END;
                }
                this.husk.setData(BigBrainCapabilities.CARRYING.get(), true);
                this.phase = BurrowPhases.SINK;
            }
            if (this.phase == BurrowPhases.SINK) {
                this.waitUntilDigTime--;
                if (this.waitUntilDigTime <= 0) {
                    this.husk.setData(BigBrainCapabilities.BURROWING.get(), false);
                    this.husk.setData(BigBrainCapabilities.DIGGING.get(), true);
                    this.husk.noPhysics = true;
                    this.husk.setDeltaMovement(0.0D, -4.0D, 0.0D);
                    this.phase = BurrowPhases.DIG_OUT;
                }
            } else if (this.phase == BurrowPhases.DIG_OUT) {
                if (this.entityInWall(this.husk)) {
                    if (!this.husk.getPassengers().isEmpty())
                        this.husk.ejectPassengers();
                    this.husk.setData(BigBrainCapabilities.CARRYING.get(), false);
                    this.husk.setDeltaMovement(0.0, 1.0D, 0.0D);
                } else {
                    this.husk.setData(BigBrainCapabilities.DIGGING.get(), false);
                    this.phase = BurrowPhases.END;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.husk.setData(BigBrainCapabilities.BURROWING.get(), false);
        this.husk.setData(BigBrainCapabilities.CARRYING.get(), false);
        this.husk.setData(BigBrainCapabilities.DIGGING.get(), false);
        this.husk.setPose(Pose.STANDING);
        this.husk.noPhysics = false;
        if (!this.husk.getPassengers().isEmpty())
            this.husk.ejectPassengers();
        this.canUseCheck = this.husk.level().getGameTime();
        this.burrowTime = 0;
        this.waitUntilDigTime = 0;
        this.husk.setAggressive(false);
        this.husk.setTarget(null);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean entityInWall(Husk husk) {
        float f = husk.getDimensions(husk.getPose()).width() * 0.8F;
        AABB aabb = AABB.ofSize(husk.getEyePosition(), f, 1.0E-6D, (double) f);
        return BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = husk.level().getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(husk.level(), p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(husk.level(), p_201942_).move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        });
    }

    public enum BurrowPhases {
        START, BURROW, GRAB, SINK, DIG_OUT, END, STOP
    }
}
