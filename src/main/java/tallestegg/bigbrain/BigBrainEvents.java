package tallestegg.bigbrain;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.common.capabilities.implementations.BurrowCapability;
import tallestegg.bigbrain.common.capabilities.implementations.IOneCriticalAfterCharge;
import tallestegg.bigbrain.common.capabilities.providers.BurrowingProvider;
import tallestegg.bigbrain.common.capabilities.providers.GuranteedCritProvider;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.entity.ai.goals.*;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;
import tallestegg.bigbrain.networking.BigBrainNetworking;
import tallestegg.bigbrain.networking.BurrowingCapabilityPacket;
import tallestegg.bigbrain.networking.CriticalCapabilityPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    private static final Method setTargetPiglin = ObfuscationReflectionHelper.findMethod(PiglinAi.class, "m_34826_", AbstractPiglin.class, LivingEntity.class);
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA().getType() == EntityType.PIG && event.getParentB().getType() == EntityType.PIG) {
            Pig pig = (Pig) event.getParentA();
            Level level = pig.getLevel();
            RandomSource randomSource = level.getRandom();
            for (int i = 0; i < BigBrainConfig.minPigBabiesBred + randomSource.nextInt(BigBrainConfig.maxPigBabiesBred + 1); ++i) {
                Pig baby = EntityType.PIG.create(event.getChild().level);
                baby.copyPosition(pig);
                baby.setPersistenceRequired();
                if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
                    level.addFreshEntity(new ExperienceOrb(level, pig.getX(), pig.getY(), pig.getZ(), pig.getRandom().nextInt(7) + 1));
                baby.setBaby(true);
                baby.setPersistenceRequired();
                pig.getCommandSenderWorld().addFreshEntity(baby);
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (BucklerItem.getChargeTicks(BigBrainItems.checkEachHandForBuckler(event.getEntity())) > 0) {
            event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().x(), 0.0D, event.getEntity().getDeltaMovement().z());
        }
    }

    @SubscribeEvent
    public static void modifiyVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() instanceof LivingEntity living) {
            if (living.hasEffect(MobEffects.BLINDNESS)) event.modifyVisibility(BigBrainConfig.mobBlindnessVision);
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
                    if (living.canFreeze()) living.setTicksFrozen(living.getTicksFrozen() + 100);
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
    public static void entityHitbox(EntityEvent.Size event) {
        if (event.getEntity() instanceof Husk husk) {
            if (husk.hasPose(Pose.SWIMMING)) {
                event.setNewSize(EntityDimensions.scalable(1.0F, 1.5F), true);
                event.setNewEyeHeight(0.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getEntityBeingMounted() instanceof Husk husk && husk.isAlive() && BigBrainCapabilities.getBurrowing(husk).isCarrying() && (!player.isSpectator() || !player.isCreative()) && player.isAlive() && event.isDismounting()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Husk husk) {
            BurrowCapability burrow = BigBrainCapabilities.getBurrowing(husk);
            if (burrow != null) {
                if (!husk.level.isClientSide)
                    BigBrainNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> husk), new BurrowingCapabilityPacket(husk.getId(), burrow.isBurrowing()));
                if (burrow.isBurrowing()) {
                    BucklerItem.spawnRunningEffectsWhileCharging(entity);
                    if (entity.getRandom().nextInt(10) == 0) {
                        BlockState onState = husk.getBlockStateOn();
                        husk.playSound(onState.getSoundType(husk.level, husk.blockPosition(), husk).getBreakSound());
                    }
                }
            }
        }
        if (entity instanceof Dolphin dolphin) {
            if (dolphin.touchingUnloadedChunk())
                dolphin.setAirSupply(300);
        }
        int turningLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), entity);
        ItemStack bucklerItemStack = BigBrainItems.checkEachHandForBuckler(entity);
        boolean bucklerReadyToCharge = BucklerItem.isReady(bucklerItemStack);
        int bucklerChargeTicks = BucklerItem.getChargeTicks(bucklerItemStack);
        if (bucklerReadyToCharge) {
            BucklerItem.setChargeTicks(bucklerItemStack, bucklerChargeTicks - 1);
            if (bucklerChargeTicks > 0) {
                BucklerItem.moveFowards(entity);
                BucklerItem.spawnRunningEffectsWhileCharging(entity);
                if (turningLevel == 0 && !entity.level.isClientSide()) BucklerItem.bucklerBash(entity);
            }
            if (bucklerChargeTicks <= 0) {
                AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                AttributeInstance knockback = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                if (speed == null || knockback == null) {
                    return;
                }
                knockback.removeModifier(KNOCKBACK_RESISTANCE);
                speed.removeModifier(CHARGE_SPEED_BOOST);
                entity.stopUsingItem();
                BucklerItem.setChargeTicks(bucklerItemStack, 0);
                BucklerItem.setReady(bucklerItemStack, false);
            }
        }
        IOneCriticalAfterCharge criticalAfterCharge = BigBrainCapabilities.getGuaranteedCritical(entity);
        if (criticalAfterCharge != null) {
            if (criticalAfterCharge.isCritical()) {
                if (entity.swingTime > 0) {
                    entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BigBrainSounds.CRITICAL_DEACTIVATE.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                    criticalAfterCharge.setCritical(false);
                }
                for (int i = 0; i < 2; ++i) {
                    entity.level.addParticle(ParticleTypes.CRIT, entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
                }
            }
            if (event.getEntity() instanceof ServerPlayer player)
                BigBrainNetworking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CriticalCapabilityPacket(player.getId(), criticalAfterCharge.isCritical()));
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().contains("minecraft:chests/bastion")) {
            ResourceLocation bucklerBastionLoot = new ResourceLocation(BigBrain.MODID, "chests/buckler_loot_table");
            event.getTable().addPool(LootPool.lootPool().name("buckler_bastion_chests").add(LootTableReference.lootTableReference(bucklerBastionLoot)).build());
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        IOneCriticalAfterCharge criticalAfterCharge = BigBrainCapabilities.getGuaranteedCritical(player);
        if (criticalAfterCharge.isCritical()) {
            event.setResult(Result.ALLOW);
            event.setDamageModifier(1.5F);
            event.getEntity().level.playSound(null, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), SoundEvents.PLAYER_ATTACK_CRIT, event.getEntity().getSoundSource(), 1.0F, 1.0F);
            criticalAfterCharge.setCritical(false);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Husk husk) {
            if (!event.getTarget().level.isClientSide) {
                BurrowCapability burrow = BigBrainCapabilities.getBurrowing(husk);
                if (burrow != null)
                    BigBrainNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> husk), new BurrowingCapabilityPacket(husk.getId(), burrow.isBurrowing()));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Husk husk && BigBrainConfig.COMMON.huskBurrowing.get())
            husk.goalSelector.addGoal(1, new HuskBurrowGoal(husk));
        if (entity instanceof Pillager pillager) {
            if (BigBrainConfig.PillagerMultishot)
                pillager.goalSelector.addGoal(2, new PressureEntityWithMultishotCrossbowGoal<>(pillager, 1.0D, 3.0F));
            if (BigBrainConfig.PillagerCover)
                pillager.goalSelector.addGoal(1, new RunWhileChargingGoal(pillager, 0.9D));
            pillager.goalSelector.addGoal(3, new ZoomInAtRandomGoal(pillager));
        }

        if (entity instanceof Enemy && entity instanceof Mob mob) {
            if (BigBrainConfig.MobsAttackAllVillagers && !BigBrainConfig.MobBlackList.contains(entity.getEncodeId())) {
                mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, AbstractVillager.class, true));
            }
        }

        if (entity instanceof AbstractVillager villager && BigBrainConfig.MobsAttackAllVillagers) {
            villager.goalSelector.addGoal(2, new AvoidEntityGoal<>(villager, Mob.class, 8.0F, 1.0D, 0.5D, (avoidTarget) -> !BigBrainConfig.MobBlackList.contains(avoidTarget.getEncodeId()) && avoidTarget instanceof Enemy));
        }

        if (entity instanceof PathfinderMob creature) {
            if (BigBrainConfig.COMMON.jumpAi.get() && !BigBrainConfig.COMMON.jumpBlackList.get().contains(creature.getEncodeId()) && (creature instanceof Zombie || creature instanceof AbstractIllager || creature instanceof AbstractPiglin
                    || creature instanceof AbstractSkeleton || creature instanceof Creeper || creature instanceof AbstractVillager || BigBrainConfig.COMMON.jumpWhiteList.get().contains(creature.getEncodeId())))
                creature.goalSelector.addGoal(1, new ParkourGoal(creature));
            if (GoalUtils.hasGroundPathNavigation(creature) && creature.getNavigation().getNodeEvaluator().canOpenDoors() && BigBrainConfig.openFenceGate && !BigBrainConfig.cantOpenFenceGates.contains(creature.getEncodeId())) {
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
            if (BigBrainConfig.COMMON.bowAiNew.get()) {
                if (!BigBrainConfig.COMMON.bowAiBlackList.get().contains(entity.getEncodeId()) && creature.goalSelector.availableGoals.stream().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof RangedBowAttackGoal<?>)) {
                    creature.goalSelector.availableGoals.removeIf((p_25367_) -> p_25367_.getGoal() instanceof RangedBowAttackGoal<?>);
                    creature.goalSelector.addGoal(3, new NewBowAttackGoal(creature, 1.55D, 20, 15.0F));
                }
            }

            if (entity instanceof PolarBear polar) {
                if (BigBrainConfig.PolarBearFish)
                    polar.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(polar, AbstractFish.class, 10, true, true, (Predicate<LivingEntity>) null));
            }

            if (BigBrainConfig.animalShelter && entity instanceof Animal animal && !BigBrainConfig.AnimalBlackList.contains(entity.getEncodeId()) && !(entity instanceof FlyingAnimal)) {
                animal.goalSelector.addGoal(7, new RestrictSunAnimalGoal(animal));
                animal.goalSelector.addGoal(8, new FindShelterGoal(animal));
            }

            if (entity instanceof Sheep sheep) {
                if (BigBrainConfig.sheepRunAway)
                    sheep.goalSelector.addGoal(2, new AvoidEntityGoal<>(sheep, Wolf.class, 8.0F, 1.0D, 1.6D));
            }

            if (entity instanceof Ocelot ocelot) {
                if (BigBrainConfig.ocelotPhantom)
                    ocelot.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ocelot, Phantom.class, 10, true, true, (Predicate<LivingEntity>) null));
                if (BigBrainConfig.ocelotCreeper)
                    ocelot.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(ocelot, Creeper.class, 10, true, true, (Predicate<LivingEntity>) null));
                if (BigBrainConfig.ocelotParrot)
                    ocelot.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(ocelot, Parrot.class, 10, true, true, (Predicate<LivingEntity>) null));
            }

            if (entity instanceof Parrot parrot)
                if (BigBrainConfig.ocelotParrot)
                    parrot.goalSelector.addGoal(2, new AvoidEntityGoal<>(parrot, Ocelot.class, 8.0F, 1.0D, 5.0D));
        }
    }

    @SubscribeEvent
    public static void onHit(LivingHurtEvent event) {
        if (event.getEntity() instanceof Animal animal && BigBrainConfig.COMMON.animalPanic.get()) {
            for (Animal nearbyEntities : animal.getLevel().getEntitiesOfClass(animal.getClass(), animal.getBoundingBox().inflate(5.0D))) {
                if (event.getSource().getEntity() instanceof LivingEntity && !event.getSource().is(DamageTypes.MOB_ATTACK_NO_AGGRO))
                    nearbyEntities.setLastHurtByMob((LivingEntity) event.getSource().getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof Husk && event.getSource().is(DamageTypes.IN_WALL))
            event.setCanceled(true);

    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        final GuranteedCritProvider critProvider = new GuranteedCritProvider();
        final BurrowingProvider burrowingProvider = new BurrowingProvider();
        if (event.getObject() instanceof Player) {
            event.addCapability(GuranteedCritProvider.IDENTIFIER, critProvider);
            event.addListener(critProvider::invalidate);
        }
        if (event.getObject() instanceof Husk) {
            event.addCapability(BurrowingProvider.IDENTIFIER, burrowingProvider);
            event.addListener(burrowingProvider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity().getUseItem().getItem() instanceof BucklerItem)
            event.setCanceled(true);
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
        if (event.getEntity() instanceof Creeper creeper && event.getOriginalTarget() instanceof Ocelot && event.getOriginalTarget() != null)
            creeper.setTarget(null);
        if (event.getEntity() instanceof Pillager pillager) {
            if (pillager.getUseItem().getItem() instanceof SpyglassItem && pillager.isPatrolling()) {
                pillager.setAggressive(true); // This needs to be done as pillagers patrolling stare at the player from
                // afar when spotted, and we want pillagers with spyglasses to immediately
                // target the player, the patrol goal is executed with a pillager isn't
                // aggressive.
                if (pillager.getNavigation().isDone())
                    pillager.getNavigation().moveTo(event.getOriginalTarget(), 1.0D);
                for (Raider raider : pillager.level.getNearbyEntities(Raider.class, TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight().ignoreInvisibilityTesting(), pillager, pillager.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
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

    @SubscribeEvent
    public static void finalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        MobSpawnType spawnType = event.getSpawnType();
        RandomSource rSource = event.getLevel().getRandom();
        if (event.getEntity() instanceof Pillager pillager) {
            if (spawnType == MobSpawnType.PATROL) {
                float chance = BigBrainConfig.spyGlassPillagerChance;
                if (pillager.isPatrolLeader() && rSource.nextFloat() < chance)
                    pillager.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SPYGLASS));
            }
        }
        if (event.getEntity() instanceof PiglinBrute piglinBrute) {
            if (!BigBrainConfig.BruteSpawningWithBuckler) return;
            piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BigBrainItems.BUCKLER.get()));
            ItemStack itemstack = piglinBrute.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                if (rSource.nextInt(300) == 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                    map.putIfAbsent(BigBrainEnchantments.TURNING.get(), 1);
                    EnchantmentHelper.setEnchantments(map, itemstack);
                    piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
                }
                if (rSource.nextInt(500) == 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                    map.putIfAbsent(BigBrainEnchantments.BANG.get(), 1);
                    EnchantmentHelper.setEnchantments(map, itemstack);
                    piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void dropLoot(LivingDropsEvent event) {
        if (event.getEntity() instanceof PiglinBrute brute) {
            ItemStack itemstack = brute.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                float f = 0.10F;
                boolean flag = f > 1.0F;
                if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (event.isRecentlyHit() || flag) && Math.max(brute.getRandom().nextFloat() - (float) event.getLootingLevel() * 0.01F, 0.0F) < f) {
                    if (itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(brute.getRandom().nextInt(brute.getRandom().nextInt(itemstack.getMaxDamage() / 2)));
                    }
                    brute.spawnAtLocation(itemstack);
                    brute.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
        }
    }
}
