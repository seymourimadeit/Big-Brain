package tallestegg.bigbrain;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
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
    public static void onEntityAttacked(LivingDamageEvent event) {
        if (event.getEntity() instanceof IBucklerUser && ((IBucklerUser)event.getEntity()).isBucklerDashing()) {
            event.setAmount(event.getAmount() / 2.0F);
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
                List<Entity> list = entity.world.getEntitiesInAABBexcluding(entity, entity.getBoundingBox().expand(0.5D, 0.0D, 0.5D), EntityPredicates.pushableBy(entity));
                if (!list.isEmpty()) {
                    int i = entity.world.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
                    if (i > 0 && list.size() > i - 1 && entity.getRNG().nextInt(4) == 0) {
                        int j = 0;

                        for (int k = 0; k < list.size(); ++k) {
                            if (!list.get(k).isPassenger()) {
                                ++j;
                            }
                        }

                        if (j > i - 1) {
                            entity.attackEntityFrom(DamageSource.CRAMMING, 6.0F);
                        }
                    }

                    for (int l = 0; l < list.size(); ++l) {
                        Entity entity2 = list.get(l);
                        entity2.applyEntityCollision(entity);
                        if (turningLevel == 0) {
                            int bangLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), entity);
                            float f = 6.0F + ((float) entity.getRNG().nextInt(3));
                            float f1 = 2.0F;
                            if (f1 > 0.0F && entity instanceof LivingEntity) {
                                for (int duration = 0; duration < 10; ++duration) {
                                    double d0 = entity.getRNG().nextGaussian() * 0.02D;
                                    double d1 = entity.getRNG().nextGaussian() * 0.02D;
                                    double d2 = entity.getRNG().nextGaussian() * 0.02D;
                                    BasicParticleType type = entity2 instanceof WitherEntity || entity2 instanceof WitherSkeletonEntity ? ParticleTypes.SMOKE : ParticleTypes.CLOUD;
                                    if (entity.world instanceof ServerWorld) {
                                        // Collision is done on the server side, so a server side method must be used.
                                        ((ServerWorld) entity.world).spawnParticle(type, entity.getPosXRandom(1.0D), entity.getPosYRandom() + 1.0D, entity.getPosZRandom(1.0D), 1, d0, d1, d2, 1.0D);
                                        if (!entity.isSilent())
                                            ((ServerWorld) entity.world).playSound((PlayerEntity) null, (double) entity.getPosition().getX(), (double) entity.getPosition().getY(), (double) entity.getPosition().getZ(), BigBrainSounds.SHIELD_BASH.get(), entity.getSoundCategory(), 0.12F,
                                                    0.8F + entity.getRNG().nextFloat() * 0.4F);
                                    }
                                }
                                if (bangLevel == 0) {
                                    entity2.attackEntityFrom(DamageSource.causeMobDamage(entity), f);
                                    ((LivingEntity) entity2).applyKnockback(f1 * 0.8F, (double) MathHelper.sin(entity.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(entity.rotationYaw * ((float) Math.PI / 180F))));
                                    if (entity2 instanceof PlayerEntity && ((PlayerEntity) entity2).getActiveItemStack().isShield(((PlayerEntity) entity2)))
                                        ((PlayerEntity) entity2).disableShield(true);
                                } else {
                                    Hand hand = entity.getHeldItemMainhand().getItem() instanceof BucklerItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
                                    ItemStack stack = entity.getHeldItem(hand);
                                    stack.damageItem(10 * bangLevel, entity, (player1) -> { // We will need feedback on this.
                                        player1.sendBreakAnimation(hand);
                                        if (entity instanceof PlayerEntity)
                                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem((PlayerEntity) (Object) entity, entity.getActiveItemStack(), hand);
                                    });
                                    Explosion.Mode mode = BigBrainConfig.BangBlockDestruction ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
                                    entity.world.createExplosion((Entity) null, DamageSource.causeExplosionDamage(entity), (ExplosionContext) null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), (float) bangLevel * 1.0F, false, mode);
                                    ((IBucklerUser) entity).setBucklerDashing(false);
                                }
                                entity.setLastAttackedEntity(entity2);
                                if (entity instanceof IOneCriticalAfterCharge)
                                    ((IOneCriticalAfterCharge) entity).setCritical(BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), (LivingEntity) (Object) entity) == 0);
                            }
                        }
                    }
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
}
