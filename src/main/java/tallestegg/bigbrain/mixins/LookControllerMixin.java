package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(LookControl.class)
public class LookControllerMixin {
    @Shadow
    @Final
    protected Mob mob;

    // TODO PR a event to forge that allows us to cancel look control ticking, and
    // also pray that they don't ignore it.
    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getOffhandItem()) == 0 && ((IBucklerUser) mob).isBucklerDashing())
            info.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "setLookAt(DDDFF)V", cancellable = true)
    public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch, CallbackInfo info) {
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getOffhandItem()) == 0 && ((IBucklerUser) mob).isBucklerDashing())
            info.cancel();
    }
}
