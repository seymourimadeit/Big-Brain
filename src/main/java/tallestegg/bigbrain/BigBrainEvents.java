package tallestegg.bigbrain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.entity.ai.goals.FindShelterGoal;
import tallestegg.bigbrain.entity.ai.goals.PressureEntityWithMultishotCrossbowGoal;
import tallestegg.bigbrain.entity.ai.goals.RestrictSunAnimalGoal;
import tallestegg.bigbrain.entity.ai.goals.RunWhileChargingGoal;
import tallestegg.bigbrain.entity.ai.goals.UseBucklerGoal;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    private static final Method setTargetPiglin = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34624_",
            AbstractPiglin.class, LivingEntity.class);

    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA().getType() == EntityType.PIG && event.getParentB().getType() == EntityType.PIG) {
            Pig pig = (Pig) event.getParentA();
            for (int i = 0; i < BigBrainConfig.minPigBabiesBred
                    + pig.level.random.nextInt(BigBrainConfig.maxPigBabiesBred + 1); ++i) {
                Pig baby = EntityType.PIG.create(event.getChild().level);
                baby.copyPosition(pig);
                baby.setBaby(true);
                pig.getCommandSenderWorld().addFreshEntity(baby);
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof IBucklerUser) {
            if (((IBucklerUser) event.getEntity()).isBucklerDashing()) {
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().x(), 0.0D,
                        event.getEntity().getDeltaMovement().z());
            }
        }
    }

    @SubscribeEvent
    public static void modifiyVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            if (event.getLookingEntity() instanceof LivingEntity
                    && ((LivingEntity) event.getLookingEntity()).hasEffect(MobEffects.BLINDNESS))
                event.modifyVisibility(BigBrainConfig.mobBlindnessVision);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Snowball && BigBrainConfig.snowGolemSlow) {
            if (event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) event.getRayTraceResult()).getEntity();
                if (entity instanceof LivingEntity && event.getProjectile().getOwner() instanceof SnowGolem) {
                    LivingEntity living = (LivingEntity) entity;
                    if (living.canFreeze())
                        living.setTicksFrozen(living.getTicksFrozen() + 100);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingUpdateEvent event) {
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
                if (turningLevel == 0)
                    BigBrainEvents.shieldBash(entity);
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
                if (((IBucklerUser) entity).getCooldown() <= 0) {
                    ((IBucklerUser) entity).setCooldown(0);
                }
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
        if (((IOneCriticalAfterCharge) event.getPlayer()).isCritical()) {
            event.setResult(Result.ALLOW);
            event.setDamageModifier(1.5F);
            event.getPlayer().level.playSound((Player) null, event.getPlayer().getX(), event.getPlayer().getY(),
                    event.getPlayer().getZ(), SoundEvents.PLAYER_ATTACK_CRIT, event.getPlayer().getSoundSource(), 1.0F,
                    1.0F);
            ((IOneCriticalAfterCharge) event.getPlayer()).setCritical(false);
        }
    }

    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin) {
            try {
                setTargetPiglin.invoke(PiglinAi.class, event.getEntity(), event.getTarget());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                new RuntimeException("Big Brain has failed to invoke maybeRetaliate");
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Pillager) {
            Pillager pillager = (Pillager) entity;
            if (BigBrainConfig.PillagerMultishot)
                pillager.goalSelector.addGoal(2, new PressureEntityWithMultishotCrossbowGoal<>(pillager, 1.0D, 3.0F));
            if (BigBrainConfig.PillagerCover)
                pillager.goalSelector.addGoal(1, new RunWhileChargingGoal(pillager, 0.9D));
        }

        if (entity instanceof Enemy && BigBrainConfig.MobsAttackAllVillagers
                && !BigBrainConfig.MobBlackList.contains(entity.getEncodeId())) {
            Mob mob = (Mob) entity;
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, AbstractVillager.class, true));
        }

        if (entity instanceof AbstractVillager && BigBrainConfig.MobsAttackAllVillagers) {
            AbstractVillager villager = (AbstractVillager) entity;
            villager.goalSelector.addGoal(2,
                    new AvoidEntityGoal<>(villager, Mob.class, 8.0F, 1.0D, 0.5D, (p_213469_1_) -> {
                        return !BigBrainConfig.MobBlackList.contains(p_213469_1_.getEncodeId());
                    }));
        }

        if (BigBrainConfig.EntitiesThatCanAlsoUseTheBuckler.contains(entity.getEncodeId())) {
            PathfinderMob creature = (PathfinderMob) entity;
            creature.goalSelector.addGoal(0, new UseBucklerGoal<>(creature));
        }

        if (entity instanceof PolarBear) {
            PolarBear polar = (PolarBear) entity;
            if (BigBrainConfig.PolarBearFish)
                polar.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(polar, AbstractFish.class, 10, true,
                        true, (Predicate<LivingEntity>) null));
        }

        if (BigBrainConfig.animalShelter && entity instanceof Animal
                && !BigBrainConfig.AnimalBlackList.contains(entity.getEncodeId())
                && !(entity instanceof FlyingAnimal)) {
            Animal animal = (Animal) entity;
            animal.goalSelector.addGoal(2, new RestrictSunAnimalGoal(animal));
            animal.goalSelector.addGoal(3, new FindShelterGoal(animal));
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

    public static void shieldBash(LivingEntity entity) {
        double maxValue = Double.MAX_VALUE;
        Entity entityHit = null;
        for (Entity entityThatIsNear : entity.level.getEntities(entity,
                entity.getBoundingBox().expandTowards(entity.getDeltaMovement()), EntitySelector.pushableBy(entity))) {
            AABB axisalignedbb = entityThatIsNear.getBoundingBox().inflate((double) 0.3F);
            Optional<Vec3> optional = axisalignedbb.clip(entity.position(),
                    entity.position().add(entity.getDeltaMovement()));
            if (optional.isPresent()) {
                double distance = entity.position().distanceToSqr(optional.get());
                if (distance < maxValue) {
                    maxValue = distance;
                    entityHit = entityThatIsNear;
                    entityHit.push(entity);
                    int bangLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(),
                            entity);
                    float f = 6.0F + ((float) entity.getRandom().nextInt(3));
                    float f1 = 3.0F;
                    if (f1 > 0.0F && entity instanceof LivingEntity) {
                        for (int duration = 0; duration < 10; ++duration) {
                            double d0 = entity.getRandom().nextGaussian() * 0.02D;
                            double d1 = entity.getRandom().nextGaussian() * 0.02D;
                            double d2 = entity.getRandom().nextGaussian() * 0.02D;
                            SimpleParticleType type = entityHit instanceof WitherBoss
                                    || entityHit instanceof WitherSkeleton ? ParticleTypes.SMOKE : ParticleTypes.CLOUD;
                            // Collision is done on the server side, so a server side method must be used.
                            ((ServerLevel) entity.level).sendParticles(type, entity.getRandomX(1.0D),
                                    entity.getRandomY() + 1.0D, entity.getRandomZ(1.0D), 1, d0, d1, d2, 1.0D);
                        }
                        if (bangLevel == 0) {
                            if (entityHit.hurt(DamageSource.mobAttack(entity), f)) {
                                if (entityHit instanceof LivingEntity) {
                                    ((LivingEntity) entityHit).knockback((double) (f1),
                                            (double) Mth.sin(entity.getYRot() * ((float) Math.PI / 180F)),
                                            (double) (-Mth.cos(entity.getYRot() * ((float) Math.PI / 180F))));
                                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                                }
                                if (!entity.isSilent())
                                    ((ServerLevel) entity.level).playSound((Player) null, entity.getX(), entity.getY(),
                                            entity.getZ(), BigBrainSounds.SHIELD_BASH.get(), entity.getSoundSource(),
                                            0.5F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                                if (entityHit instanceof Player
                                        && ((Player) entityHit).getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK))
                                    ((Player) entityHit).disableShield(true);
                            }
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
                            Explosion.BlockInteraction mode = BigBrainConfig.BangBlockDestruction
                                    ? Explosion.BlockInteraction.BREAK
                                    : Explosion.BlockInteraction.NONE;
                            entity.level.explode((Entity) null, entity.getX(), entity.getY(), entity.getZ(),
                                    (float) bangLevel * 1.0F, mode);
                            ((IBucklerUser) entity).setBucklerDashing(false);
                        }
                        entity.setLastHurtMob(entityHit);
                        if (entity instanceof IOneCriticalAfterCharge)
                            ((IOneCriticalAfterCharge) entity).setCritical(BigBrainEnchantments
                                    .getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), entity) == 0);
                    }
                }
            }
        }
    }
}
