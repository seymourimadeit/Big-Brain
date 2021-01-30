package tallestegg.bigbrain.items;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags.Items;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
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
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.charge")).mergeStyle(TextFormatting.BLUE));
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.while")).mergeStyle(TextFormatting.GRAY));
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.forward")).mergeStyle(TextFormatting.BLUE));
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.speed")).mergeStyle(TextFormatting.BLUE));
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) == 0 && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), stack) == 0)
            tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.bash")).mergeStyle(TextFormatting.BLUE));
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) > 0)
            tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.explosion")).mergeStyle(TextFormatting.BLUE));
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.knockback")).mergeStyle(TextFormatting.BLUE));
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) == 0 && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), stack) == 0) {
            tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.critical")).mergeStyle(TextFormatting.BLUE));
            tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.critSwing")).mergeStyle(TextFormatting.RED));
            tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.turnSpeed")).mergeStyle(TextFormatting.RED));
        }
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.noJumping")).mergeStyle(TextFormatting.RED));
        tooltip.add((new TranslationTextComponent("item.bigbrain.buckler.desc.water")).mergeStyle(TextFormatting.RED));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        ItemStack itemstack = super.onItemUseFinish(stack, worldIn, entityLiving);
        if (isReady(stack))
            setReady(stack, false);
        if (entityLiving instanceof IBucklerUser) {
            if (((IBucklerUser) entityLiving).getCooldown() > 0) {
                ((IBucklerUser) entityLiving).setCharging(true);
                BucklerItem.setReady(stack, true);
                stack.damageItem(1, entityLiving, (entityLiving1) -> {
                    entityLiving1.sendBreakAnimation(EquipmentSlotType.OFFHAND);
                });
                if (entityLiving instanceof PlayerEntity)
                    ((PlayerEntity) entityLiving).getCooldownTracker().setCooldown(this, BigBrainConfig.BucklerCooldown);
                entityLiving.resetActiveHand();
                if (entityLiving instanceof AbstractPiglinEntity)
                    entityLiving.playSound(BigBrainSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F, entityLiving.isChild() ? (entityLiving.getRNG().nextFloat() - entityLiving.getRNG().nextFloat()) * 0.2F + 1.5F : (entityLiving.getRNG().nextFloat() - entityLiving.getRNG().nextFloat()) * 0.2F + 1.0F);
            }
        } else {
            BucklerItem.setReady(stack, true);
            stack.damageItem(1, entityLiving, (entityLiving1) -> {
                entityLiving1.sendBreakAnimation(EquipmentSlotType.OFFHAND);
            });
        }
        return itemstack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
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

    public static boolean isReady(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.getBoolean("Ready");
    }

    public static void setReady(ItemStack stack, boolean ready) {
        CompoundNBT compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("Ready", ready);
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