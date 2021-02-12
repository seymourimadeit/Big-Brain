package tallestegg.bigbrain.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//This has to be created because the FishingBobberEntity is hardcoded to be used fo the player only, and we want the drowned to be able to use
//fishing rods.
/*public class DrownedFishingBobberEntity extends FishingBobberEntity {

    private DrownedFishingBobberEntity(World p_i50219_1_, PlayerEntity p_i50219_2_, int p_i50219_3_, int p_i50219_4_) {
        super(EntityType.FISHING_BOBBER, p_i50219_1_);
        this.ignoreFrustumCheck = true;
        this.setShooter(p_i50219_2_);
        p_i50219_2_.fishingBobber = this;
        this.luck = Math.max(0, p_i50219_3_);
        this.lureSpeed = Math.max(0, p_i50219_4_);
    }

    @OnlyIn(Dist.CLIENT)
    public DrownedFishingBobberEntity(World worldIn, PlayerEntity p_i47290_2_, double x, double y, double z) {
        this(worldIn, p_i47290_2_, 0, 0);
        this.setPosition(x, y, z);
        this.prevPosX = this.getPosX();
        this.prevPosY = this.getPosY();
        this.prevPosZ = this.getPosZ();
    }

    public DrownedFishingBobberEntity(PlayerEntity p_i50220_1_, World p_i50220_2_, int p_i50220_3_, int p_i50220_4_) {
        this(p_i50220_2_, p_i50220_1_, p_i50220_3_, p_i50220_4_);
        float f = p_i50220_1_.rotationPitch;
        float f1 = p_i50220_1_.rotationYaw;
        float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
        double d0 = p_i50220_1_.getPosX() - (double) f3 * 0.3D;
        double d1 = p_i50220_1_.getPosYEye();
        double d2 = p_i50220_1_.getPosZ() - (double) f2 * 0.3D;
        this.setLocationAndAngles(d0, d1, d2, f1, f);
        Vector3d vector3d = new Vector3d((double) (-f3), (double) MathHelper.clamp(-(f5 / f4), -5.0F, 5.0F), (double) (-f2));
        double d3 = vector3d.length();
        vector3d = vector3d.mul(0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D);
        this.setMotion(vector3d);
        this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, (double) MathHelper.sqrt(horizontalMag(vector3d))) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }
}*/
