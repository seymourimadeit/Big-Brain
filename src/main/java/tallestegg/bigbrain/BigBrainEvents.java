package tallestegg.bigbrain;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.entity.ai.goals.*;
import tallestegg.bigbrain.items.BucklerItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    private static final Method setTargetPiglin = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34826_",
            AbstractPiglin.class, LivingEntity.class);

    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA().getType() == EntityType.PIG && event.getParentB().getType() == EntityType.PIG) {
            Pig pig = (Pig) event.getParentA();
            Level level = pig.getLevel();
            RandomSource randomSource = level.getRandom();
            for (int i = 0; i < BigBrainConfig.minPigBabiesBred
                    + randomSource.nextInt(BigBrainConfig.maxPigBabiesBred + 1); ++i) {
                Pig baby = EntityType.PIG.create(event.getChild().level);
                baby.copyPosition(pig);
                baby.setPersistenceRequired();
                if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    level.addFreshEntity(new ExperienceOrb(level, pig.getX(), pig.getY(), pig.getZ(), pig.getRandom().nextInt(7) + 1));
                }
                baby.setBaby(true);
                pig.getCommandSenderWorld().addFreshEntity(baby);
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof IBucklerUser) {
            if (((IBucklerUser) event.getEntity()).isBucklerDashing()) {
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().x(), 0.0D, event.getEntity().getDeltaMovement().z());
            }
        }
    }

    @SubscribeEvent
    public static void modifiyVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() instanceof LivingEntity living) {
            if (living.hasEffect(MobEffects.BLINDNESS))
                event.modifyVisibility(BigBrainConfig.mobBlindnessVision);
            if (living.getUseItem().getItem() instanceof SpyglassItem && living.getAttribute(Attributes.FOLLOW_RANGE) != null)
                event.modifyVisibility(living.getAttributeValue(Attributes.FOLLOW_RANGE) * 2.0D);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Snowball && BigBrainConfig.snowGolemSlow) {
            if (event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) event.getRayTraceResult()).getEntity();
                if (entity instanceof LivingEntity living) {
                    if (living.canFreeze())
                        living.setTicksFrozen(living.getTicksFrozen() + 100);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        Player player = event.getEntity();
        if (BigBrainConfig.snowGolemSlow) {
            if (item == Items.SNOWBALL) {
                player.swing(event.getHand(), true);
                player.getCooldowns().addCooldown(item, 4);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof IBucklerUser) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            int turningLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(),
                    entity);
            if (!((IBucklerUser) entity).isBucklerDashing()) {
                ((IBucklerUser) entity).setBucklerUseTimer(((IBucklerUser) entity).getBucklerUseTimer() + 1);
                int configValue = turningLevel == 0 ? BigBrainConfig.BucklerRunTime
                        : BigBrainConfig.BucklerTurningRunTime;
                if (((IBucklerUser) entity).getBucklerUseTimer() > configValue)
                    ((IBucklerUser) entity).setBucklerUseTimer(configValue);
                ((IBucklerUser) entity).setCooldown(((IBucklerUser) entity).getCooldown() + 1);
                if (((IBucklerUser) entity).getCooldown() > BigBrainConfig.BucklerCooldown)
                    ((IBucklerUser) entity).setCooldown(BigBrainConfig.BucklerCooldown);
            }

            if (((IBucklerUser) entity).isBucklerDashing()) {
                BucklerItem.moveFowards(entity);
                ((IBucklerUser) entity).setBucklerUseTimer(((IBucklerUser) entity).getBucklerUseTimer() - 1);
                ((IBucklerUser) entity).setCooldown(((IBucklerUser) entity).getCooldown() - 1);
                BigBrainEvents.spawnRunningEffectsWhileCharging(entity);
                if (turningLevel == 0 && !entity.level.isClientSide())
                    BigBrainEvents.bucklerBash(entity);
                if (((IBucklerUser) entity).getBucklerUseTimer() <= 0) {
                    InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;
                    ItemStack stack = entity.getItemInHand(hand);
                    ((IBucklerUser) entity).setBucklerDashing(false);
                    ((IBucklerUser) entity).setBucklerUseTimer(0);
                    ((IBucklerUser) entity).setCooldown(0);
                    BucklerItem.setReady(stack, false);
                    entity.stopUsingItem();
                }
                if (((IBucklerUser) entity).getCooldown() <= 0)
                    ((IBucklerUser) entity).setCooldown(0);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().contains("minecraft:chests/bastion")) {
            ResourceLocation bucklerBastionLoot = new ResourceLocation(BigBrain.MODID, "chests/buckler_loot_table");
            event.getTable().addPool(LootPool.lootPool().name("buckler_bastion_chests")
                    .add(LootTableReference.lootTableReference(bucklerBastionLoot)).build());
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(CriticalHitEvent event) {
        if (((IOneCriticalAfterCharge) event.getEntity()).isCritical()) {
            event.setResult(Result.ALLOW);
            event.setDamageModifier(1.5F);
            event.getEntity().level.playSound((Player) null, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(), SoundEvents.PLAYER_ATTACK_CRIT, event.getEntity().getSoundSource(), 1.0F,
                    1.0F);
            ((IOneCriticalAfterCharge) event.getEntity()).setCritical(false);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Pillager pillager) {
            if (BigBrainConfig.PillagerMultishot)
                pillager.goalSelector.addGoal(2, new PressureEntityWithMultishotCrossbowGoal<>(pillager, 1.0D, 3.0F));
            if (BigBrainConfig.PillagerCover)
                pillager.goalSelector.addGoal(1, new RunWhileChargingGoal(pillager, 0.9D));
            pillager.goalSelector.addGoal(3, new ZoomInAtRandomGoal(pillager));
        }

        if (entity instanceof Enemy && BigBrainConfig.MobsAttackAllVillagers
                && !BigBrainConfig.MobBlackList.contains(entity.getEncodeId())) {
            Mob mob = (Mob) entity;
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, AbstractVillager.class, true));
        }

        if (entity instanceof AbstractVillager villager && BigBrainConfig.MobsAttackAllVillagers) {
            villager.goalSelector.addGoal(2,
                    new AvoidEntityGoal<>(villager, Mob.class, 8.0F, 1.0D, 0.5D, (avoidTarget) -> {
                        return !BigBrainConfig.MobBlackList.contains(avoidTarget.getEncodeId()) && avoidTarget instanceof Enemy;
                    }));
        }

        if (entity instanceof PathfinderMob creature) {
            if (GoalUtils.hasGroundPathNavigation(creature) && ((GroundPathNavigation) creature.getNavigation()).getNodeEvaluator().canOpenDoors() && BigBrainConfig.openFenceGate && !BigBrainConfig.cantOpenFenceGates.contains(creature.getEncodeId())) {
                if (creature instanceof Raider) {
                    creature.goalSelector.addGoal(2, new OpenFenceGateGoal(creature, false) {
                        @Override
                        public boolean canUse() {
                            return ((Raider) creature).hasActiveRaid() && super.canUse();
                        }
                    });
                } else {
                    creature.goalSelector.addGoal(2, new OpenFenceGateGoal(creature, true));
                }
            }
            if (BigBrainConfig.EntitiesThatCanAlsoUseTheBuckler.contains(entity.getEncodeId()))
                creature.goalSelector.addGoal(0, new UseBucklerGoal<>(creature));
        }

        if (entity instanceof PolarBear polar) {
            if (BigBrainConfig.PolarBearFish)
                polar.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(polar, AbstractFish.class, 10, true,
                        true, (Predicate<LivingEntity>) null));
        }

        if (BigBrainConfig.animalShelter && entity instanceof Animal animal
                && !BigBrainConfig.AnimalBlackList.contains(entity.getEncodeId())
                && !(entity instanceof FlyingAnimal)) {
            animal.goalSelector.addGoal(2, new RestrictSunAnimalGoal(animal));
            animal.goalSelector.addGoal(3, new FindShelterGoal(animal));
        }

        if (entity instanceof Sheep sheep) {
            if (BigBrainConfig.sheepRunAway)
                sheep.goalSelector.addGoal(2, new AvoidEntityGoal<>(sheep, Wolf.class, 8.0F, 1.0D, 1.6D));
        }

        if (entity instanceof Ocelot ocelot) {
            if (BigBrainConfig.ocelotPhantom)
                ocelot.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ocelot, Phantom.class, 10, true,
                        true, (Predicate<LivingEntity>) null));
            if (BigBrainConfig.ocelotCreeper)
                ocelot.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(ocelot, Creeper.class, 10, true,
                        true, (Predicate<LivingEntity>) null));
            if (BigBrainConfig.ocelotParrot)
                ocelot.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(ocelot, Parrot.class, 10, true, true,
                        (Predicate<LivingEntity>) null));
        }

        if (entity instanceof Parrot parrot)
            if (BigBrainConfig.ocelotParrot)
                parrot.goalSelector.addGoal(2, new AvoidEntityGoal<>(parrot, Ocelot.class, 8.0F, 1.0D, 5.0D));
    }

    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin) {
            try {
                setTargetPiglin.invoke(PiglinAi.class, event.getEntity(), event.getOriginalTarget());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                new RuntimeException("Big Brain has failed to invoke maybeRetaliate");
            }
        }
        if (event.getEntity() instanceof Creeper creeper && event.getOriginalTarget() instanceof Ocelot ocelot
                && event.getOriginalTarget() != null)
            creeper.setTarget(null);
        if (event.getEntity() instanceof Pillager pillager) {
            if (pillager.getUseItem().getItem() instanceof SpyglassItem && pillager.isPatrolling()) {
                pillager.setAggressive(true); // This needs to be done as pillagers patrolling stare at the player from
                // afar when spotted, and we want pillagers with spyglasses to immediately
                // target the player, the patrol goal is executed with a pillager isn't
                // aggressive.
                if (pillager.getNavigation().isDone())
                    pillager.getNavigation().moveTo(event.getOriginalTarget(), 1.0D);
                for (Raider raider : pillager.level.getNearbyEntities(Raider.class,
                        TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight().ignoreInvisibilityTesting(),
                        pillager, pillager.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
                    raider.setAggressive(true);
                    if (!(raider.getUseItem().getItem() instanceof SpyglassItem) && !raider.isPatrolling())
                        raider.setTarget(event.getOriginalTarget());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onToolTipLoad(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == Items.SNOWBALL) {
            event.getToolTip().add(Component.translatable("item.bigbrain.snowball.desc.hit").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.bigbrain.snowball.desc.freeze").withStyle(ChatFormatting.BLUE));
        }
    }

    public static void bucklerBash(LivingEntity entity) {
        List<LivingEntity> list = entity.level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(),
                entity, entity.getBoundingBox());
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
                SimpleParticleType type = entityHit instanceof WitherBoss || entityHit instanceof WitherSkeleton
                        ? ParticleTypes.SMOKE
                        : ParticleTypes.CLOUD;
                // Collision is done on the server side, so a server side method must be used.
                ((ServerLevel) entity.level).sendParticles(type, entity.getRandomX(1.0D), entity.getRandomY() + 1.0D,
                        entity.getRandomZ(1.0D), 1, d0, d1, d2, 1.0D);
            }
            if (bangLevel == 0) {
                if (entityHit.hurt(DamageSource.mobAttack(entity), damage)) {
                    entityHit.knockback((double) (knockbackStrength),
                            (double) Mth.sin(entity.getYRot() * ((float) Math.PI / 180F)),
                            (double) (-Mth.cos(entity.getYRot() * ((float) Math.PI / 180F))));
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }
                if (!entity.isSilent())
                    ((ServerLevel) entity.level).playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(),
                            BigBrainSounds.SHIELD_BASH.get(), entity.getSoundSource(), 0.5F,
                            0.8F + entity.getRandom().nextFloat() * 0.4F);
                if (entityHit instanceof Player
                        && ((Player) entityHit).getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK))
                    ((Player) entityHit).disableShield(true);
            } else {
                InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
                ItemStack stack = entity.getItemInHand(hand);
                stack.hurtAndBreak(5 * bangLevel, entity, (player1) -> {
                    player1.broadcastBreakEvent(hand);
                    if (entity instanceof Player)
                        ForgeEventFactory.onPlayerDestroyItem((Player) entity, entity.getUseItem(), hand);
                });
                Level.ExplosionInteraction mode = BigBrainConfig.BangBlockDestruction ? Level.ExplosionInteraction.TNT
                        : Level.ExplosionInteraction.NONE;
                entity.level.explode(null, entity.getX(), entity.getY(), entity.getZ(),
                        (float) bangLevel * 1.0F, mode);
                ((IBucklerUser) entity).setBucklerDashing(false);
            }
            entity.setLastHurtMob(entityHit);
            if (entity instanceof IOneCriticalAfterCharge)
                ((IOneCriticalAfterCharge) entity).setCritical(
                        BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), entity) == 0);
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
                entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos),
                        entity.getX() + (entity.getRandom().nextDouble() - 0.5D)
                                * (double) entity.getDimensions(entity.getPose()).height,
                        entity.getY() + 0.1D,
                        entity.getZ() + (entity.getRandom().nextDouble() - 0.5D)
                                * (double) entity.getDimensions(entity.getPose()).width,
                        vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
            }
    }
}
