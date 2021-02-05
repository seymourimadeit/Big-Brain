package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(LookController.class)
public class LookControllerMixin {
    @Shadow
    @Final
    protected MobEntity mob;

    // TODO PR a event to forge that allows us to cancel look control ticking, and
    // also pray that they don't ignore it.
    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) == 0 && ((IBucklerUser) mob).isBucklerDashing())
            info.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "setLookPosition(DDDFF)V", cancellable = true)
    public void setLookPosition(double x, double y, double z, float deltaYaw, float deltaPitch, CallbackInfo info) {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) == 0 && ((IBucklerUser) mob).isBucklerDashing())
            info.cancel();
    }
}
