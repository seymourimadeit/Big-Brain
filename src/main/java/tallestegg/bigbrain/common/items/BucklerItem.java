package tallestegg.bigbrain.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags.Items;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.client.renderers.BucklerRenderer;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BucklerItem extends ShieldItem {
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);

    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vec3 look = entity.getViewVector(1.0F);
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(look.x * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), motion.y,
                    look.z * entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
        }
    }

    public static boolean isReady(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.getBoolean("Ready");
    }

    public static int getChargeTicks(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTag();
        if (compoundnbt != null)
            return compoundnbt.getInt("ChargeTicks");
        else
            return 0;
    }

    public static void setChargeTicks(ItemStack stack, int chargeTicks) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        compoundnbt.putInt("ChargeTicks", chargeTicks);
    }

    public static void setReady(ItemStack stack, boolean ready) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("Ready", ready);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag tooltip) {
        list.add((Component.translatable("item.bigbrain.buckler.desc.charge")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.while")).withStyle(ChatFormatting.GRAY));
        list.add((Component.translatable("item.bigbrain.buckler.desc.forward")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.speed")).withStyle(ChatFormatting.BLUE));
        if (stack.getEnchantmentLevel(BigBrainEnchantments.BANG.get()) == 0
                && stack.getEnchantmentLevel(BigBrainEnchantments.TURNING.get()) == 0)
            list.add((Component.translatable("item.bigbrain.buckler.desc.bash")).withStyle(ChatFormatting.BLUE));
        if (stack.getEnchantmentLevel(BigBrainEnchantments.BANG.get()) > 0)
            list.add(
                    (Component.translatable("item.bigbrain.buckler.desc.explosion")).withStyle(ChatFormatting.BLUE));
        list.add((Component.translatable("item.bigbrain.buckler.desc.knockback")).withStyle(ChatFormatting.BLUE));
        if (stack.getEnchantmentLevel(BigBrainEnchantments.BANG.get()) == 0
                && stack.getEnchantmentLevel(BigBrainEnchantments.TURNING.get()) == 0) {
            list.add((Component.translatable("item.bigbrain.buckler.desc.critical")).withStyle(ChatFormatting.BLUE));
            list.add((Component.translatable("item.bigbrain.buckler.desc.critSwing")).withStyle(ChatFormatting.RED));
            list.add((Component.translatable("item.bigbrain.buckler.desc.turnSpeed")).withStyle(ChatFormatting.RED));
        }
        list.add((Component.translatable("item.bigbrain.buckler.desc.noJumping")).withStyle(ChatFormatting.RED));
        list.add((Component.translatable("item.bigbrain.buckler.desc.water")).withStyle(ChatFormatting.RED));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new BucklerRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        });
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        ItemStack itemstack = super.finishUsingItem(stack, worldIn, entityLiving);
        int turningLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), entityLiving);
        int configValue = turningLevel == 0 ? BigBrainConfig.BucklerRunTime : BigBrainConfig.BucklerTurningRunTime;
        BucklerItem.setReady(stack, true);
        BucklerItem.setChargeTicks(stack, configValue);
        AttributeInstance speed = entityLiving.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance knockback = entityLiving.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        knockback.removeModifier(KNOCKBACK_RESISTANCE);
        knockback.addTransientModifier(KNOCKBACK_RESISTANCE);
        speed.removeModifier(CHARGE_SPEED_BOOST);
        speed.addTransientModifier(CHARGE_SPEED_BOOST);
        stack.hurtAndBreak(1, entityLiving, (entityLiving1) -> entityLiving1.broadcastBreakEvent(EquipmentSlot.OFFHAND));
        if (entityLiving instanceof Player)
            ((Player) entityLiving).getCooldowns().addCooldown(this, BigBrainConfig.BucklerCooldown);
        entityLiving.stopUsingItem();
        if (entityLiving instanceof AbstractPiglin)
            entityLiving.playSound(BigBrainSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F, entityLiving.isBaby()
                    ? (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.5F
                    : (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.0F);
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

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.INGOTS_GOLD);
    }


    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }
}