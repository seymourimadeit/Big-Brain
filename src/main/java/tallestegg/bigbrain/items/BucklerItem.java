package tallestegg.bigbrain.items;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags.Items;
import tallestegg.bigbrain.BigBrainSounds;
import tallestegg.bigbrain.client.renderers.BucklerRenderer;
import tallestegg.bigbrain.entity.IBucklerUser;

public class BucklerItem extends ShieldItem {
    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_.setISTER(BucklerItem::getISTER));
        DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    private static Callable<ItemStackTileEntityRenderer> getISTER() {
        return BucklerRenderer::new;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

    }

    @Override
    public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        if (!livingEntityIn.isInWaterRainOrBubbleColumn()) {
            if (((IBucklerUser) livingEntityIn).getCooldown() > 0) {
                ((IBucklerUser) livingEntityIn).setCharging(true);
                stack.damageItem(1, livingEntityIn, (player1) -> {
                    player1.sendBreakAnimation(EquipmentSlotType.OFFHAND);
                });
                if (livingEntityIn instanceof PlayerEntity) {
                    ((PlayerEntity) livingEntityIn).getCooldownTracker().setCooldown(this, 240);
                }
                livingEntityIn.resetActiveHand();
                if (livingEntityIn instanceof AbstractPiglinEntity)
                    livingEntityIn.playSound(BigBrainSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F,
                            livingEntityIn.isChild() ? (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F + 1.5F : (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return !playerIn.isInWaterRainOrBubbleColumn() ? super.onItemRightClick(worldIn, playerIn, handIn) : ActionResult.resultPass(playerIn.getHeldItem(handIn));
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vector3d d3 = entity.getLookVec();
            Vector3d d4 = entity.getLook(1.0F);
            Vector3d motion = entity.getMotion();
            if (entity instanceof PlayerEntity) {
                entity.setMotion(d3.x * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), motion.y, d3.z * entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                // This is the only way to make the piglin brute go faster without having it
                // spazz out.
                entity.setMotion(d4.x * 1.0D, motion.y, d4.z * 1.0D);
            }
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return Items.INGOTS_GOLD.contains(repair.getItem());
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
        return true;
    }
}
