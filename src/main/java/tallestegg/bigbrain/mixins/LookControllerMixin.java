package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.util.math.MathHelper;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(LookController.class)
public class LookControllerMixin {
    @Shadow
    @Final
    protected MobEntity mob;

    @Shadow
    protected float deltaLookYaw;

    @Shadow
    protected float deltaLookPitch;

    @Shadow
    protected boolean isLooking;

    @Shadow
    protected double posX;

    @Shadow
    protected double posY;

    @Shadow
    protected double posZ;

    // TODO PR a event to forge that allows us to cancel look control ticking, and also pray that they don't ignore it.
    @Overwrite
    public void tick() {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 && ((IBucklerUser) mob).isBucklerDashing() || !((IBucklerUser) mob).isBucklerDashing()) {
            if (this.shouldResetPitch()) {
                this.mob.rotationPitch = 0.0F;
            }

            if (this.isLooking) {
                this.isLooking = false;
                this.mob.rotationYawHead = this.clampedRotate(this.mob.rotationYawHead, this.getTargetYaw(), this.deltaLookYaw);
                this.mob.rotationPitch = this.clampedRotate(this.mob.rotationPitch, this.getTargetPitch(), this.deltaLookPitch);
            } else {
                this.mob.rotationYawHead = this.clampedRotate(this.mob.rotationYawHead, this.mob.renderYawOffset, 10.0F);
            }

            if (!this.mob.getNavigator().noPath()) {
                this.mob.rotationYawHead = MathHelper.func_219800_b(this.mob.rotationYawHead, this.mob.renderYawOffset, (float) this.mob.getHorizontalFaceSpeed());
            }
        }
    }

    @Shadow
    private float clampedRotate(float rotationYawHead, float targetYaw, float deltaLookYaw2) {
        return 0;
    }

    @Shadow
    private boolean shouldResetPitch() {
        return false;
    }

    @Overwrite
    public void setLookPosition(double x, double y, double z, float deltaYaw, float deltaPitch) {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 && ((IBucklerUser) mob).isBucklerDashing() || !((IBucklerUser) mob).isBucklerDashing()) {
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.deltaLookYaw = deltaYaw;
            this.deltaLookPitch = deltaPitch;
            this.isLooking = true;
        }
    }

    @Shadow
    protected float getTargetPitch() {
        double d0 = this.posX - this.mob.getPosX();
        double d1 = this.posY - this.mob.getPosYEye();
        double d2 = this.posZ - this.mob.getPosZ();
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        return (float) (-(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
    }

    @Shadow
    protected float getTargetYaw() {
        double d0 = this.posX - this.mob.getPosX();
        double d1 = this.posZ - this.mob.getPosZ();
        return (float) (MathHelper.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
    }

}
