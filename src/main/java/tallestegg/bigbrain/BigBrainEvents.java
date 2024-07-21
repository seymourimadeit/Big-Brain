package tallestegg.bigbrain;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
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
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.codehaus.plexus.util.cli.shell.Shell;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.common.entity.ai.goals.*;
import tallestegg.bigbrain.networking.BurrowingCapabilityPacket;
import tallestegg.bigbrain.networking.ShellHealthPacket;

import java.util.function.Predicate;

@EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA().getType() == EntityType.PIG && event.getParentB().getType() == EntityType.PIG) {
            Pig pig = (Pig) event.getParentA();
            Level level = pig.level();
            RandomSource randomSource = level.getRandom();
            for (int i = 0; i < BigBrainConfig.minPigBabiesBred + randomSource.nextInt(BigBrainConfig.maxPigBabiesBred + 1); ++i) {
                Pig baby = EntityType.PIG.create(event.getChild().level());
                if (baby != null)
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
    public static void onMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof Husk husk) {
            boolean carrying = husk.getData(BigBrainCapabilities.CARRYING);
            if (event.getEntity() instanceof Player player && husk.isAlive() && carrying && (!player.isSpectator() || !player.isCreative()) && player.isAlive() && event.isDismounting()) {
                event.setCanceled(true);
            }
        }
    }

    public static void spawnRunningEffectsWhileCharging(LivingEntity entity) {
        int i = Mth.floor(entity.getX());
        int j = Mth.floor(entity.getY() - (double) 0.2F);
        int k = Mth.floor(entity.getZ());
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = entity.level().getBlockState(blockpos);
        if (!blockstate.addRunningEffects(entity.level(), blockpos, entity))
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vec3 = entity.getDeltaMovement();
                entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).height(), entity.getY() + 0.1D, entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).width(), vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
            }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof Husk husk) {
                boolean burrowing = husk.getData(BigBrainCapabilities.BURROWING.get());
                if (!husk.level().isClientSide)
                    PacketDistributor.sendToPlayersTrackingEntity(husk, new BurrowingCapabilityPacket(husk.getId(), burrowing));
                if (burrowing) {
                    spawnRunningEffectsWhileCharging(entity);
                    if (entity.getRandom().nextInt(10) == 0) {
                        BlockState onState = husk.getBlockStateOn();
                        husk.playSound(onState.getSoundType(husk.level(), husk.blockPosition(), husk).getBreakSound());
                    }
                }
            }
            if (entity instanceof Dolphin dolphin) {
                if (dolphin.touchingUnloadedChunk())
                    dolphin.setAirSupply(300);
            }
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Husk husk) {
            if (!event.getTarget().level().isClientSide) {
                boolean burrowing = husk.getData(BigBrainCapabilities.BURROWING.get());
                PacketDistributor.sendToPlayersTrackingEntity(husk, new BurrowingCapabilityPacket(husk.getId(), burrowing));
            }
        }
        if (event.getTarget() instanceof Armadillo armadillo) {
            int shellHealth = armadillo.getData(BigBrainCapabilities.SHELL_HEALTH.get());
            PacketDistributor.sendToPlayersTrackingEntity(armadillo, new ShellHealthPacket(armadillo.getId(), shellHealth));
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
                creature.goalSelector.addGoal(0, new ParkourGoal(creature));
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
            if (BigBrainConfig.COMMON.bowAiNew.get()) {
                if (!BigBrainConfig.COMMON.bowAiBlackList.get().contains(entity.getEncodeId()) && creature.goalSelector.getAvailableGoals().stream().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof RangedBowAttackGoal<?>)) {
                    creature.goalSelector.getAvailableGoals().removeIf((p_25367_) -> p_25367_.getGoal() instanceof RangedBowAttackGoal<?>);
                    creature.goalSelector.addGoal(3, new NewBowAttackGoal(creature, 1.55D, 20, 15.0F));
                }
            }

            if (entity instanceof PolarBear polar) {
                if (BigBrainConfig.PolarBearFish)
                    polar.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(polar, AbstractFish.class, 10, true, true, (Predicate<LivingEntity>) null));
            }

            if (BigBrainConfig.animalShelter && entity instanceof Animal animal && BigBrainConfig.AnimalWhiteList.contains(entity.getEncodeId()) && !(entity instanceof FlyingAnimal)) {
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
    public static void onHit(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Armadillo armadillo && BigBrainConfig.COMMON.armadilloShell.get()) {
            int shellHealth = armadillo.getData(BigBrainCapabilities.SHELL_HEALTH.get());
            if (shellHealth < 13) {
                armadillo.playSound(BigBrainSounds.ARMADILLO_CRACK.get());
                if (armadillo.level() instanceof ServerLevel serverlevel) {
                    serverlevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()),
                            armadillo.getX(),
                            armadillo.getY() + 1.0,
                            armadillo.getZ(),
                            20,
                            0.2,
                            0.1,
                            0.2,
                            0.1
                    );
                }
            }
            if (shellHealth > 0) {
                event.setNewDamage(0.0F);
                armadillo.setData(BigBrainCapabilities.SHELL_HEALTH.get(), shellHealth - (int) event.getContainer().getOriginalDamage());
            } else if (shellHealth <= 0) {
                event.getContainer().setNewDamage(event.getContainer().getOriginalDamage() * 2.0F);
            }
            PacketDistributor.sendToPlayersTrackingEntity(armadillo, new ShellHealthPacket(armadillo.getId(), shellHealth));
        }
        if (event.getEntity() instanceof Husk husk) {
            boolean carrying = husk.getData(BigBrainCapabilities.CARRYING);
            if (event.getContainer().getSource().is(DamageTypes.IN_WALL) && carrying)
                event.getContainer().setNewDamage(0.0F);
        }
    }

    @SubscribeEvent
    public static void onHitPost(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Animal animal && BigBrainConfig.COMMON.animalPanic.get()) {
            for (Animal nearbyEntities : animal.level().getEntitiesOfClass(animal.getClass(), animal.getBoundingBox().inflate(5.0D))) {
                if (event.getSource().getEntity() instanceof LivingEntity && !event.getSource().is(DamageTypes.MOB_ATTACK_NO_AGGRO)) {
                    Vec3 vec3 = DefaultRandomPos.getPos(nearbyEntities, 5, 4);
                    if (vec3 != null)
                        nearbyEntities.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 2.0D);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTargetSet(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            if (event.getOriginalAboutToBeSetTarget() != null)
                piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, event.getOriginalAboutToBeSetTarget().getUUID());
        }
        if (event.getEntity() instanceof Creeper creeper && event.getOriginalAboutToBeSetTarget() instanceof Ocelot && event.getOriginalAboutToBeSetTarget() != null)
            creeper.setTarget(null);
        if (event.getEntity() instanceof Pillager pillager) {
            if (pillager.getUseItem().getItem() instanceof SpyglassItem && pillager.isPatrolling()) {
                pillager.setAggressive(true); // This needs to be done as pillagers patrolling stare at the player from
                // afar when spotted, and we want pillagers with spyglasses to immediately
                // target the player, the patrol goal is executed with a pillager isn't
                // aggressive.
                if (pillager.getNavigation().isDone())
                    pillager.getNavigation().moveTo(event.getOriginalAboutToBeSetTarget(), 1.0D);
                for (Raider raider : pillager.level().getNearbyEntities(Raider.class, TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight().ignoreInvisibilityTesting(), pillager, pillager.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
                    raider.setAggressive(true);
                    if (!(raider.getUseItem().getItem() instanceof SpyglassItem) && !raider.isPatrolling())
                        raider.setTarget(event.getOriginalAboutToBeSetTarget());
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
    public static void finalizeSpawn(FinalizeSpawnEvent event) {
        MobSpawnType spawnType = event.getSpawnType();
        RandomSource rSource = event.getLevel().getRandom();
        if (event.getEntity() instanceof Armadillo armadillo && BigBrainConfig.COMMON.armadilloShell.get())
            armadillo.setData(BigBrainCapabilities.SHELL_HEALTH, 13);
        if (event.getEntity() instanceof Pillager pillager) {
            if (spawnType == MobSpawnType.PATROL) {
                float chance = BigBrainConfig.spyGlassPillagerChance;
                if (pillager.isPatrolLeader() && rSource.nextFloat() < chance)
                    pillager.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SPYGLASS));
            }
        }
    }
}
