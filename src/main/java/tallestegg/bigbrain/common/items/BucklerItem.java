package tallestegg.bigbrain.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.client.renderers.BucklerRenderer;
import tallestegg.bigbrain.common.capabilities.implementations.IOneCriticalAfterCharge;
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

    public static void bucklerBash(LivingEntity entity) {
        List<LivingEntity> list = entity.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), entity, entity.getBoundingBox().inflate(1.5D));
        if (!list.isEmpty()) {
            LivingEntity entityHit = list.get(0);
            entityHit.push(entity);
            int bangLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), entity);
            float damage = 6.0F + ((float) entity.getRandom().nextInt(3));
            float knockbackStrength = 3.0F;
            for (int duration = 0; duration < 10; ++duration) {
                double d0 = entity.getRandom().nextGaussian() * 0.02D;
                double d1 = entity.getRandom().nextGaussian() * 0.02D;
                double d2 = entity.getRandom().nextGaussian() * 0.02D;
                SimpleParticleType type = entityHit instanceof WitherBoss || entityHit instanceof WitherSkeleton ? ParticleTypes.SMOKE : ParticleTypes.CLOUD;
                // Collision is done on the server side, so a server side method must be used.
                ((ServerLevel) entity.level).sendParticles(type, entity.getRandomX(1.0D), entity.getRandomY() + 1.0D, entity.getRandomZ(1.0D), 1, d0, d1, d2, 1.0D);
            }
            if (bangLevel == 0) {
                if (entityHit.hurt(entity.damageSources().mobAttack(entity), damage)) {
                    entityHit.knockback(knockbackStrength, (double) Mth.sin(entity.getYRot() * ((float) Math.PI / 180F)), (double) (-Mth.cos(entity.getYRot() * ((float) Math.PI / 180F))));
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }
                if (!entity.isSilent())
                    entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BigBrainSounds.SHIELD_BASH.get(), entity.getSoundSource(), 0.5F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                if (entityHit instanceof Player && entityHit.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK))
                    ((Player) entityHit).disableShield(true);
            } else {
                InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = entity.getItemInHand(hand);
                stack.hurtAndBreak(5 * bangLevel, entity, (player1) -> {
                    player1.broadcastBreakEvent(hand);
                    if (entity instanceof Player)
                        ForgeEventFactory.onPlayerDestroyItem((Player) entity, entity.getUseItem(), hand);
                });
                Level.ExplosionInteraction mode = BigBrainConfig.BangBlockDestruction ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
                entity.level.explode(null, entity.getX(), entity.getY(), entity.getZ(), (float) bangLevel * 1.0F, mode);
                setChargeTicks(stack, 0);
            }
            entity.setLastHurtMob(entityHit);
            if (entity instanceof Player player) {
                IOneCriticalAfterCharge criticalAfterCharge = BigBrainCapabilities.getGuaranteedCritical(player);
                player.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BigBrainSounds.CRITICAL_ACTIVATE.get(), entity.getSoundSource(), 1.0F, 1.0F);
                criticalAfterCharge.setCritical(BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), player) == 0);
            }
        }
    }

    public static void spawnRunningEffectsWhileCharging(LivingEntity entity) {
        int i = Mth.floor(entity.getX());
        int j = Mth.floor(entity.getY() - (double) 0.2F);
        int k = Mth.floor(entity.getZ());
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = entity.level.getBlockState(blockpos);
        if (!blockstate.addRunningEffects(entity.level, blockpos, entity))
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vec3 = entity.getDeltaMovement();
                entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).height, entity.getY() + 0.1D, entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).width, vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
            }
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