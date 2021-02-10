package tallestegg.bigbrain;

import java.util.function.Predicate;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.entity.ai.goals.PressureEntityWithMultishotCrossbowGoal;
import tallestegg.bigbrain.entity.ai.goals.RunWhileChargingGoal;
import tallestegg.bigbrain.entity.ai.goals.UseBucklerGoal;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof PigEntity && event.getParentB() instanceof PigEntity && BigBrainConfig.PigBreeding) {
            PigEntity pig = (PigEntity) event.getParentA();
            for (int i = 0; i < 2 + pig.world.rand.nextInt(4); ++i) {
                AgeableEntity baby = EntityType.PIG.create(event.getChild().world);
                baby.copyLocationAndAnglesFrom(pig);
                baby.setChild(true);
                pig.world.addEntity(baby);
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof IBucklerUser) {
            if (((IBucklerUser) event.getEntity()).isBucklerDashing()) {
                event.getEntity().setMotion(event.getEntity().getMotion().getX(), 0.0D, event.getEntity().getMotion().getZ());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingUpdateEvent event) {
        // event.setCanceled(true); //Only uncomment this if you're going to take a
        // screenshot of something.
        if (event.getEntity() instanceof IBucklerUser) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            int turningLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), entity);
            int coolDown = ((IBucklerUser) entity).getCooldown();
            int bucklerUseTimer = ((IBucklerUser) entity).getBucklerUseTimer();
            if (!((IBucklerUser) entity).isBucklerDashing()) {
                ++bucklerUseTimer;
                int configValue = turningLevel == 0 ? BigBrainConfig.BucklerRunTime : BigBrainConfig.BucklerTurningRunTime;
                if (bucklerUseTimer > configValue)
                    bucklerUseTimer = configValue;
                ++coolDown;
                if (coolDown > BigBrainConfig.BucklerCooldown)
                    coolDown = BigBrainConfig.BucklerCooldown;
                ((IBucklerUser) entity).setBucklerUseTimer(bucklerUseTimer);
                ((IBucklerUser) entity).setCooldown(coolDown);
            }

            if (((IBucklerUser) entity).isBucklerDashing()) {
                BucklerItem.moveFowards(entity);
                coolDown--;
                bucklerUseTimer--;
                ((IBucklerUser) entity).setBucklerUseTimer(bucklerUseTimer);
                ((IBucklerUser) entity).setCooldown(coolDown);
            }
            if (bucklerUseTimer <= 0) {
                ((IBucklerUser) entity).setBucklerDashing(false);
                ((IBucklerUser) entity).setCooldown(0);
                bucklerUseTimer = 0;
                entity.resetActiveHand();
            }
            if (coolDown <= 0) {
                ((IBucklerUser) entity).setCooldown(0);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().contains("minecraft:chests/bastion")) {
            ResourceLocation bucklerBastionLoot = new ResourceLocation(BigBrain.MODID, "chests/buckler_loot_table");
            // LootPool pool =
            // LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(BigBrainItems.BUCKLER.get()).weight(10)).addEntry(EmptyLootEntry.func_216167_a().weight(90)).build();
            event.getTable().addPool(LootPool.builder().name("buckler_bastion_chests").addEntry(TableLootEntry.builder(bucklerBastionLoot)).build());
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (((IOneCriticalAfterCharge) event.getPlayer()).isCritical()) {
            ((IOneCriticalAfterCharge) event.getPlayer()).setCritical(false);
            event.getPlayer().world.playSound((PlayerEntity) null, event.getPlayer().getPosX(), event.getPlayer().getPosY(), event.getPlayer().getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, event.getPlayer().getSoundCategory(), 1.0F, 1.0F);
            event.getPlayer().onCriticalHit(event.getTarget());
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PillagerEntity) {
            PillagerEntity pillager = (PillagerEntity) event.getEntity();
            if (BigBrainConfig.PillagerMultishot)
                pillager.goalSelector.addGoal(2, new PressureEntityWithMultishotCrossbowGoal<>(pillager, 1.0D, 3.0F));
            if (BigBrainConfig.PillagerCover)
                pillager.goalSelector.addGoal(1, new RunWhileChargingGoal(pillager, 0.9D));
        }

        if (event.getEntity() instanceof IMob && BigBrainConfig.MobsAttackAllVillagers && !BigBrainConfig.MobBlackList.contains(event.getEntity().getEntityString())) {
            MobEntity mob = (MobEntity) event.getEntity();
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, AbstractVillagerEntity.class, true));
        }

        if (event.getEntity() instanceof VillagerEntity && BigBrainConfig.MobsAttackAllVillagers) {
            VillagerEntity villager = (VillagerEntity) event.getEntity();
            villager.goalSelector.addGoal(2, new AvoidEntityGoal<>(villager, MobEntity.class, 8.0F, 1.0D, 0.5D, (p_213469_1_) -> {
                return !BigBrainConfig.MobBlackList.contains(p_213469_1_.getEntityString());
            }));
        }

        if (event.getEntity().getType().equals(ForgeRegistries.ENTITIES.getValue(new ResourceLocation("guardvillagers:guard")))) {
            CreatureEntity creature = (CreatureEntity) event.getEntity();
            creature.goalSelector.addGoal(0, new UseBucklerGoal<>(creature));
        }

        /*
         * if (event.getEntity() instanceof AbstractPiglinEntity) { AbstractPiglinEntity
         * piglin = (AbstractPiglinEntity) event.getEntity();
         * piglin.func_242340_t(true); }
         */

        if (event.getEntity() instanceof PolarBearEntity) {
            PolarBearEntity polar = (PolarBearEntity) event.getEntity();
            if (BigBrainConfig.PolarBearFish)
                polar.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(polar, AbstractFishEntity.class, 10, true, true, (Predicate<LivingEntity>) null));
        }
    }

    /*
     * public static void render(LivingEntity entityIn, LivingRenderer<LivingEntity,
     * ?> renderer, EntityModel<LivingEntity> entityModel, float entityYaw, float
     * partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int
     * packedLightIn, float opacity) { matrixStackIn.push();
     * entityModel.swingProgress = entityIn.getSwingProgress(partialTicks);
     * 
     * boolean shouldSit = entityIn.isPassenger() && (entityIn.getRidingEntity() !=
     * null && entityIn.getRidingEntity().shouldRiderSit()); entityModel.isSitting =
     * shouldSit; entityModel.isChild = entityIn.isChild(); float f =
     * MathHelper.interpolateAngle(partialTicks, entityIn.prevRenderYawOffset,
     * entityIn.renderYawOffset); float f1 =
     * MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYawHead,
     * entityIn.rotationYawHead); float f2 = f1 - f; if (shouldSit &&
     * entityIn.getRidingEntity() instanceof LivingEntity) { LivingEntity
     * livingentity = (LivingEntity) entityIn.getRidingEntity(); f =
     * MathHelper.interpolateAngle(partialTicks, livingentity.prevRenderYawOffset,
     * livingentity.renderYawOffset); f2 = f1 - f; float f3 =
     * MathHelper.wrapDegrees(f2); if (f3 < -85.0F) { f3 = -85.0F; }
     * 
     * if (f3 >= 85.0F) { f3 = 85.0F; }
     * 
     * f = f1 - f3; if (f3 * f3 > 2500.0F) { f += f3 * 0.2F; }
     * 
     * f2 = f1 - f; }
     * 
     * float f6 = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch,
     * entityIn.rotationPitch); if (entityIn.getPose() == Pose.SLEEPING) { Direction
     * direction = entityIn.getBedDirection(); if (direction != null) { float f4 =
     * entityIn.getEyeHeight(Pose.STANDING) - 0.1F; matrixStackIn.translate((double)
     * ((float) (-direction.getXOffset()) * f4), 0.0D, (double) ((float)
     * (-direction.getZOffset()) * f4)); } }
     * 
     * float f7 = (float) entityIn.ticksExisted + partialTicks;
     * matrixStackIn.scale(-1.0F, -1.0F, 1.0F); matrixStackIn.translate(0.0D,
     * (double) -1.501F, 0.0D); float f8 = 0.0F; float f5 = 0.0F; if (!shouldSit &&
     * entityIn.isAlive()) { f8 = MathHelper.lerp(partialTicks,
     * entityIn.prevLimbSwingAmount, entityIn.limbSwingAmount); f5 =
     * entityIn.limbSwing - entityIn.limbSwingAmount * (1.0F - partialTicks); if
     * (entityIn.isChild()) { f5 *= 3.0F; }
     * 
     * if (f8 > 1.0F) { f8 = 1.0F; } }
     * 
     * entityModel.setLivingAnimations(entityIn, f5, f8, partialTicks);
     * entityModel.setRotationAngles(entityIn, f5, f8, f7, f2, f6); Minecraft
     * minecraft = Minecraft.getInstance(); boolean flag = !entityIn.isInvisible();
     * boolean flag1 = !flag && !entityIn.isInvisibleToPlayer(minecraft.player);
     * boolean flag2 = minecraft.isEntityGlowing(entityIn); RenderType rendertype =
     * RenderType.getItemEntityTranslucentCull(renderer.getEntityTexture(entityIn));
     * if (rendertype != null) { IVertexBuilder ivertexbuilder =
     * bufferIn.getBuffer(rendertype); int i =
     * LivingRenderer.getPackedOverlay(entityIn, 0.0F);
     * entityModel.render(matrixStackIn, ivertexbuilder, packedLightIn, i, 1.0F,
     * 1.0F, 1.0F, opacity); }
     * 
     * matrixStackIn.pop(); }
     */
}
