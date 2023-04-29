package tallestegg.bigbrain.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;

@Mixin(MoveControl.class)
public abstract class MovementControllerMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), mob) >= 0 && BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(mob)) > 0)
            info.cancel();
    }
}
