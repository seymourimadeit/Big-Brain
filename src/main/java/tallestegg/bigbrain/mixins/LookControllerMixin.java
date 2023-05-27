package tallestegg.bigbrain.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;

@Mixin(LookControl.class)
public class LookControllerMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (mob != null && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), mob) >= 0 && BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(mob)) > 0)
            info.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "setLookAt(DDDFF)V", cancellable = true)
    public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch, CallbackInfo info) {
        if (mob != null && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), mob) >= 0 && BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(mob)) > 0)
            info.cancel();
    }
}
