package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.world.entity.Mob;

public class OpenFenceGateGoal extends FenceGateInteractGoal {
    private final boolean closeFence;
    private int forgetTime;

    public OpenFenceGateGoal(Mob mob, boolean shouldCloseFence) {
        super(mob);
        this.mob = mob;
        this.closeFence = shouldCloseFence;
    }

    @Override
    public boolean canContinueToUse() {
        return this.closeFence && this.forgetTime > 0 && super.canContinueToUse();
    }

    @Override
    public boolean canUse() {
     //   System.out.println(super.canUse());
        return super.canUse();
    }

    @Override
    public void start() {
        this.forgetTime = 20;
        this.setOpen(true);
    }

    @Override
    public void stop() {
        this.setOpen(false);
    }

    @Override
    public void tick() {
        --this.forgetTime;
        super.tick();
    }
}
