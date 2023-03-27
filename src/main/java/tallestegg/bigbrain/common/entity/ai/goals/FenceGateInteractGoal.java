package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class FenceGateInteractGoal extends Goal {
    protected Mob mob;
    protected BlockPos gatePos = BlockPos.ZERO;
    protected boolean hasGate;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public FenceGateInteractGoal(Mob p_25193_) {
        this.mob = p_25193_;
        if (!GoalUtils.hasGroundPathNavigation(p_25193_)) {
            throw new IllegalArgumentException("Unsupported mob type for FenceGateInteractGoal");
        }
    }

    protected void setOpen(boolean open) {
        if (this.hasGate) {
            BlockState blockstate = this.mob.level.getBlockState(this.gatePos);
            if (blockstate.getBlock() instanceof FenceGateBlock) {
                this.mob.level.setBlock(this.gatePos, blockstate.setValue(FenceGateBlock.OPEN, Boolean.valueOf(open)),
                        10);
                this.mob.level.playSound((Player)null, this.gatePos, open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, this.mob.level.getRandom().nextFloat() * 0.1F + 0.9F);
                this.mob.level.gameEvent(mob, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, gatePos);
            }
        }

    }

    @Override
    public boolean canUse() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else {
            GroundPathNavigation groundpathnavigation = (GroundPathNavigation) this.mob.getNavigation();
            Path path = groundpathnavigation.getPath();
            if (path != null && this.mob.getNavigation().getNodeEvaluator().canOpenDoors()) {
                for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); ++i) {
                        Node node = path.getNode(i);
                    this.gatePos = new BlockPos(node.x + mob.getRandom().nextInt(4) - 2, node.y, node.z + mob.getRandom().nextInt(4) - 2); //needed as normally pathfinding ignores closed fence gates
                    // and we need the mobs to recognize that a fence gate exists so it can be opened/closed
                    if (this.mob.distanceToSqr(this.gatePos.getX(), this.gatePos.getY(), this.gatePos.getZ()) < 2.25D) {
                        this.hasGate = this.mob.level.getBlockState(this.gatePos).getBlock() instanceof FenceGateBlock;
                        return this.hasGate;
                    }
                }
                return this.hasGate;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.passed;
    }

    @Override
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float) ((double) this.gatePos.getX() + 0.5D - this.mob.getX());
        this.doorOpenDirZ = (float) ((double) this.gatePos.getZ() + 0.5D - this.mob.getZ());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float f = (float) ((double) this.gatePos.getX() + 0.5D - this.mob.getX());
        float f1 = (float) ((double) this.gatePos.getZ() + 0.5D - this.mob.getZ());
        float f2 = this.doorOpenDirX * f + this.doorOpenDirZ * f1;
        if (f2 < 0.0F)
            this.passed = true;
    }
}
