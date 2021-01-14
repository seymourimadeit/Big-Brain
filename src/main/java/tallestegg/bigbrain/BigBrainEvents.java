package tallestegg.bigbrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.entity.ai.goals.PressureEntityWithMultishotCrossbowGoal;
import tallestegg.bigbrain.entity.ai.goals.RunWhileChargingGoal;
import tallestegg.bigbrain.networking.PlayerCriticalPacket;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof PigEntity && event.getParentB() instanceof PigEntity && BigBrainConfig.PigBreeding) {
            PigEntity pig = (PigEntity) event.getParentA();
            for (int i = 0; i < 2 + pig.world.rand.nextInt(3); ++i) {
                AgeableEntity baby = EntityType.PIG.create(event.getChild().world);
                baby.copyLocationAndAnglesFrom(pig);
                baby.setChild(true);
                pig.world.addEntity(baby);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onMouseKeyPressed(ClickInputEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (((IOneCriticalAfterCharge) player).isCritical() && event.isAttack()) {
            BigBrainPackets.INSTANCE.sendToServer(new PlayerCriticalPacket(player.getEntityId()));
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof IBucklerUser) {
            if (((IBucklerUser) event.getEntity()).isCharging()) {
                event.getEntity().setMotion(event.getEntity().getMotion().getX(), 0, event.getEntity().getMotion().getZ());
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onMovementKeyPressed(InputUpdateEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (((IBucklerUser) player).isCharging()) {
            //event.getMovementInput().jump = false;
            event.getMovementInput().moveStrafe = 0;
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().contains("minecraft:chests/bastion")) {
            LootPool pool = LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(BigBrainItems.BUCKLER.get()).weight(10)).addEntry(ItemLootEntry.builder(Items.AIR).weight(90)).build(); // This is hacky, but this will make do for now.
            event.getTable().addPool(pool);
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
            MobEntity mob = (MobEntity)event.getEntity();
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, AbstractVillagerEntity.class, true));
        }
        
        if (event.getEntity() instanceof AbstractPiglinEntity) {
            AbstractPiglinEntity piglin = (AbstractPiglinEntity)event.getEntity();
            piglin.func_242340_t(true);
        }
    }
}
