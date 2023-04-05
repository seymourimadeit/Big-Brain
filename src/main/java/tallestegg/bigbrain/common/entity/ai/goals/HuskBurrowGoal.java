package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

import java.util.EnumSet;

public class HuskBurrowGoal extends Goal {
    protected Husk husk;
    private int waitUntilDigTime;
    private BurrowPhases phase;

    public HuskBurrowGoal(Husk husk) {
        this.husk = husk;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.husk.getTarget() != null && this.husk.getTarget().distanceTo(this.husk) >= 5.0D && this.husk.getTarget() instanceof Player;
    }

    @Override
    public boolean canContinueToUse() {
        return this.husk.getTarget() != null && this.phase != BurrowPhases.STOP;
    }

    @Override
    public void start() {
        super.start();
        this.waitUntilDigTime = 20;
        BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(true);
        this.phase = BurrowPhases.START;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.husk.getTarget();
        if (target == null)
            return;
        this.phase = BurrowPhases.BURROW;
        if (this.phase == BurrowPhases.BURROW) {
            this.husk.getNavigation().moveTo(target, 2.0D);
            if (husk.isWithinMeleeAttackRange(target)) {
                BigBrainCapabilities.getBurrowing(this.husk).setBurrowing(false);
                this.phase = BurrowPhases.GRAB;
            }
        }
        if (this.phase == BurrowPhases.GRAB) {
            target.startRiding(this.husk, true);
            husk.lookAt(target, 30.F, 30.0F);
            husk.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.phase = BurrowPhases.SINK;
        }
        if (this.phase == BurrowPhases.SINK) {
            husk.lookAt(target, 30.F, 30.0F);
            husk.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.waitUntilDigTime--;
            if (this.waitUntilDigTime <= 0) {
                target.stopRiding();
                target.setPos(new Vec3(target.getX(), target.getY() - 4.0D, target.getZ()));
                this.phase = BurrowPhases.END;
            }
        }
        if (this.phase == BurrowPhases.END) {
            this.phase = BurrowPhases.STOP;
        }
    }

    @Override
    public void stop() {
        super.stop();
      //  this.husk.setTarget(null);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public enum BurrowPhases {
        START, BURROW, GRAB, SINK, END, STOP;
    }
}
