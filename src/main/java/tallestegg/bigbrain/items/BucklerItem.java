package tallestegg.bigbrain.items;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import tallestegg.bigbrain.client.renderers.BucklerRenderer;
import tallestegg.bigbrain.entity.IBucklerUser;

public class BucklerItem extends ShieldItem {
    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_.setISTER(BucklerItem::getISTER));
    }

    private static Callable<ItemStackTileEntityRenderer> getISTER() {
        return BucklerRenderer::new;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

    }

    @Override
    public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        if (((IBucklerUser) livingEntityIn).getCooldown() > 0) {
            ((IBucklerUser) livingEntityIn).setCharging(true);
            stack.damageItem(1, livingEntityIn, (player1) -> {
                player1.sendBreakAnimation(EquipmentSlotType.OFFHAND);
            });
            if (livingEntityIn instanceof PlayerEntity) {
                ((PlayerEntity) livingEntityIn).getCooldownTracker().setCooldown(this, 1);
            }
            livingEntityIn.resetActiveHand();
            if (livingEntityIn instanceof AbstractPiglinEntity)
                livingEntityIn.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_CONVRTED_TO_ZOMBIFIED, 1.0F,
                        livingEntityIn.isChild() ? (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F + 1.5F : (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F + 1.0F);
        }

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vector3d d3 = entity.getLookVec();
            Vector3d motion = entity.getMotion();
            if (entity instanceof PlayerEntity) {
                entity.setMotion(d3.x * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), motion.y, d3.z * entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                // This is the only way to make the piglin brute go faster without having it
                // spazz out.
                entity.setMotion(d3.x * 1.0D, motion.y, d3.z * 1.0D);
            }
        }
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
        return true;
    }
}
