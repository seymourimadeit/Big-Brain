package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(MouseHelper.class)
public class MouseHelperMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private double lastLookTime = Double.MIN_VALUE;

    @Final
    @Shadow
    private MouseSmoother xSmoother = new MouseSmoother();

    @Final
    @Shadow
    private MouseSmoother ySmoother = new MouseSmoother();

    @Shadow
    private double xVelocity;

    @Shadow
    private double yVelocity;

    // This is the worst implemetation of a feature I have ever done.
    // Please, @ me on discord for a better implementation.
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/entity/player/ClientPlayerEntity;", shift = At.Shift.AFTER), cancellable = true, method = "updatePlayerLook()V")
    public void updatePlayerLook(CallbackInfo info) {
        info.cancel();
    }

    @Inject(at = @At(value = "HEAD"), cancellable = true, method = "updatePlayerLook()V")
    public void updatePlayerLook2(CallbackInfo info) {
        if (minecraft.player == null) {
            return;
        }
        double d0 = NativeUtil.getTime();
        double d1 = d0 - this.lastLookTime;
        double d4 = ((IBucklerUser) minecraft.player).isBucklerDashing() && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), minecraft.player) == 0 ? 0.2F : this.minecraft.gameSettings.mouseSensitivity * (double) 0.6F + (double) 0.2F;
        double d5 = d4 * d4 * d4 * 8.0D;
        double d2;
        double d3;
        if (this.minecraft.gameSettings.smoothCamera) {
            double d6 = this.xSmoother.smooth(this.xVelocity * d5, d1 * d5);
            double d7 = this.ySmoother.smooth(this.yVelocity * d5, d1 * d5);
            d2 = d6;
            d3 = d7;
        } else {
            this.xSmoother.reset();
            this.ySmoother.reset();
            d2 = this.xVelocity * d5;
            d3 = this.yVelocity * d5;
        }
        int i = 1;
        if (this.minecraft.gameSettings.invertMouse) {
            i = -1;
        }
        if (this.minecraft.player != null) {
            this.minecraft.player.rotateTowards(d2, d3 * (double) i);
        }
    }

}
