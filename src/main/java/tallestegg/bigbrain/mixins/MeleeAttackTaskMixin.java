package tallestegg.bigbrain.mixins;

import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(MeleeAttack.class)
public class MeleeAttackTaskMixin {
    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "lambda$create$0")
    private static void checkExtraStartConditions(BehaviorBuilder.Instance p_258533_, MemoryAccessor p_258534_, MemoryAccessor p_258535_, MemoryAccessor p_258536_, MemoryAccessor p_258537_, int p_258538_, ServerLevel p_258539_, Mob p_258540_, long p_258541_, CallbackInfoReturnable<Boolean> cir) {
        if (((IBucklerUser)p_258540_).isBucklerDashing())
            cir.setReturnValue(false);
    }
}
