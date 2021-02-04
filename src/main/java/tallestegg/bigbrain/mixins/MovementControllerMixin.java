package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mixin(MovementController.class)
public abstract class MovementControllerMixin {

    @Shadow
    @Final
    protected MobEntity mob;

    @Shadow
    protected double posX;

    @Shadow
    protected double posY;

    @Shadow
    protected double posZ;

    @Shadow
    protected double speed;

    @Shadow
    protected float moveForward;

    @Shadow
    protected float moveStrafe;

    @Shadow
    protected MovementController.Action action = MovementController.Action.WAIT;

    /**
     * @author TallestRed
     * TODO : add a pr for this in forge so we cancel it instead of just doing this.
     */
    @Overwrite
    public void tick() {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 && ((IBucklerUser) mob).isCharging() || !((IBucklerUser) mob).isCharging()) {
            if (this.action == MovementController.Action.STRAFE) {
                float f = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
                float f1 = (float) this.speed * f;
                float f2 = this.moveForward;
                float f3 = this.moveStrafe;
                float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 = f2 * f4;
                f3 = f3 * f4;
                float f5 = MathHelper.sin(this.mob.rotationYaw * ((float) Math.PI / 180F));
                float f6 = MathHelper.cos(this.mob.rotationYaw * ((float) Math.PI / 180F));
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                if (!this.func_234024_b_(f7, f8)) {
                    this.moveForward = 1.0F;
                    this.moveStrafe = 0.0F;
                }

                this.mob.setAIMoveSpeed(f1);
                this.mob.setMoveForward(this.moveForward);
                this.mob.setMoveStrafing(this.moveStrafe);
                this.action = MovementController.Action.WAIT;
            } else if (this.action == MovementController.Action.MOVE_TO) {
                this.action = MovementController.Action.WAIT;
                double d0 = this.posX - this.mob.getPosX();
                double d1 = this.posZ - this.mob.getPosZ();
                double d2 = this.posY - this.mob.getPosY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;
                if (d3 < (double) 2.5000003E-7F) {
                    this.mob.setMoveForward(0.0F);
                    return;
                }

                float f9 = (float) (MathHelper.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
                this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                BlockPos blockpos = this.mob.getPosition();
                BlockState blockstate = this.mob.world.getBlockState(blockpos);
                Block block = blockstate.getBlock();
                VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.world, blockpos);
                if (d2 > (double) this.mob.stepHeight && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getWidth())
                        || !voxelshape.isEmpty() && this.mob.getPosY() < voxelshape.getEnd(Direction.Axis.Y) + (double) blockpos.getY() && !block.isIn(BlockTags.DOORS) && !block.isIn(BlockTags.FENCES)) {
                    this.mob.getJumpController().setJumping();
                    this.action = MovementController.Action.JUMPING;
                }
            } else if (this.action == MovementController.Action.JUMPING) {
                this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.mob.isOnGround()) {
                    this.action = MovementController.Action.WAIT;
                }
            } else {
                this.mob.setMoveForward(0.0F);
            }
        }
    }

    @Shadow
    abstract float limitAngle(float rotationYaw, float f9, float f);

    @Shadow
    abstract boolean func_234024_b_(float f7, float f8);
}
