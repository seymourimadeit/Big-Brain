package tallestegg.bigbrain.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.Tags.Items;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.BigBrainSounds;
import tallestegg.bigbrain.client.renderers.BucklerRenderer;
import tallestegg.bigbrain.entity.IBucklerUser;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class BucklerItem extends ShieldItem {
    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag tooltip) {
        list.add((Component.translatable("item.bigbrain.buckler.desc.charge")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.while")).withStyle(ChatFormatting.GRAY));
        list.add((Component.translatable("item.bigbrain.buckler.desc.forward")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.speed")).withStyle(ChatFormatting.BLUE));
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) == 0
                && EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), stack) == 0)
            list.add((Component.translatable("item.bigbrain.buckler.desc.bash")).withStyle(ChatFormatting.BLUE));
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) > 0)
            list.add(
                    (Component.translatable("item.bigbrain.buckler.desc.explosion")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.knockback")).withStyle(ChatFormatting.BLUE));
        if (EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.BANG.get(), stack) == 0
                && EnchantmentHelper.getItemEnchantmentLevel(BigBrainEnchantments.TURNING.get(), stack) == 0) {
            list.add((Component.translatable("item.bigbrain.buckler.desc.critical")).withStyle(ChatFormatting.BLUE));
            list.add((Component.translatable("item.bigbrain.buckler.desc.critSwing")).withStyle(ChatFormatting.RED));
            list.add((Component.translatable("item.bigbrain.buckler.desc.turnSpeed")).withStyle(ChatFormatting.RED));
        }
        list.add((Component.translatable("item.bigbrain.buckler.desc.noJumping")).withStyle(ChatFormatting.RED));
        list.add((Component.translatable("item.bigbrain.buckler.desc.water")).withStyle(ChatFormatting.RED));
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return new BucklerRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        });
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        ItemStack itemstack = super.finishUsingItem(stack, worldIn, entityLiving);
        if (entityLiving instanceof IBucklerUser) {
            ((IBucklerUser) entityLiving).setBucklerDashing(true);
            BucklerItem.setReady(stack, true);
            stack.hurtAndBreak(1, entityLiving, (entityLiving1) -> {
                entityLiving1.broadcastBreakEvent(EquipmentSlot.OFFHAND);
            });
            if (entityLiving instanceof Player)
                ((Player) entityLiving).getCooldowns().addCooldown(this, BigBrainConfig.BucklerCooldown);
            entityLiving.stopUsingItem();
            if (entityLiving instanceof AbstractPiglin)
                entityLiving.playSound(BigBrainSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F, entityLiving.isBaby()
                        ? (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.5F
                        : (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
        return itemstack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        return !playerIn.isInWaterRainOrBubble() ? super.use(worldIn, playerIn, handIn)
                : InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vec3 look = entity.getViewVector(1.0F);
            Vec3 motion = entity.getDeltaMovement();
            if (entity instanceof Player) {
                entity.setDeltaMovement(look.x * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), motion.y,
                        look.z * entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                // This is the only way to make the piglin brute go faster without having it
                // spazz out.
                entity.setDeltaMovement(look.x * 1.0D, motion.y, look.z * 1.0D);
            }
        }
    }
    
    public static boolean isReady(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.getBoolean("Ready");
    }

    public static void setReady(ItemStack stack, boolean ready) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("Ready", ready);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.INGOTS_GOLD);
    }


    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
       return net.minecraftforge.common.ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }
}