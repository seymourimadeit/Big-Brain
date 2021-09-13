package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(MoveControl.class)
public abstract class MovementControllerMixin {

    @Shadow
    @Final
    protected Mob mob;

    /**
     * TODO PR a MovementController and LookController event so we don't have to mixin into these classes.
     */
    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getOffhandItem()) == 0 && ((IBucklerUser) mob).isBucklerDashing())
            info.cancel();
    }
}
