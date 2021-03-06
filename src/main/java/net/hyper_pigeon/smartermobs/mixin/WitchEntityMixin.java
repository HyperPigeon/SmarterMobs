package net.hyper_pigeon.smartermobs.mixin;

import net.hyper_pigeon.smartermobs.goals.BuffTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin extends MobEntity {

    @Shadow
    public abstract boolean isDrinking();

    private BuffTargetGoal<HostileEntity> buffGoal;


    protected WitchEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "initGoals")
    protected void addGoals(CallbackInfo ci){
        buffGoal = new BuffTargetGoal<HostileEntity>(this,HostileEntity.class,true,entity -> entity != null && entity.getType() != EntityType.WITCH
        && entity.getType() != EntityType.ENDER_DRAGON && entity.getType() != EntityType.WITHER && (entity.getAttacking() != null ? (entity.getAttacking() != this) : true)
                && (this.getAttacking() != null ? (this.getAttacking()!=entity) : true));
        this.targetSelector.add(2,buffGoal);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/ai/goal/RaidGoal.decreaseCooldown ()V", shift = At.Shift.BEFORE), method = "tickMovement")
    public void tickBuffGoal(CallbackInfo ci){
        buffGoal.decreaseCooldown();
        if (this.buffGoal.getCooldown() <= 0) {
            this.buffGoal.setEnabled(true);
        } else {
            this.buffGoal.setEnabled(false);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.getVelocity ()Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.BEFORE), method = "attack", cancellable = true)
    public void buffAttack(LivingEntity target, float pullProgress, CallbackInfo ci){
        if(buffGoal.getTarget() != null) {

            Vec3d vec3d = target.getVelocity();
            double d = target.getX() + vec3d.x - this.getX();
            double e = target.getEyeY() - (double)1.1f - this.getY();
            double f = target.getZ() + vec3d.z - this.getZ();
            double g = Math.sqrt(d * d + f * f);
            Potion potion = Potions.SWIFTNESS;

            if(target.isOnFire()){
                potion = Potions.FIRE_RESISTANCE;
                this.setTarget(null);
            }
            else if(target instanceof ZombieEntity){
                potion = target.hasStatusEffect(StatusEffects.STRENGTH) ? Potions.SWIFTNESS : Potions.STRENGTH;
                this.setTarget(null);
            }
            else if(target instanceof SkeletonEntity){
                potion = target.hasStatusEffect(StatusEffects.SPEED) ? Potions.INVISIBILITY : Potions.SWIFTNESS;
                this.setTarget(null);
            }
            else if(target instanceof CreeperEntity){
                potion = target.hasStatusEffect(StatusEffects.SPEED) ? Potions.INVISIBILITY : Potions.SWIFTNESS;
                this.setTarget(null);
            }
            else if(target instanceof SpiderEntity){
                potion = target.hasStatusEffect(StatusEffects.STRENGTH) ? Potions.SWIFTNESS : Potions.STRENGTH;
                this.setTarget(null);
            }
            else if(target instanceof EndermanEntity){
                potion = target.hasStatusEffect(StatusEffects.INVISIBILITY) ? Potions.STRENGTH : Potions.INVISIBILITY;
                this.setTarget(null);
            }
            else if(target instanceof RaiderEntity && !(target instanceof RangedAttackMob)){
                potion = target.hasStatusEffect(StatusEffects.STRENGTH) ? Potions.SWIFTNESS : Potions.STRENGTH;
                this.setTarget(null);
            }
            else {
                this.setTarget(null);
            }

            PotionEntity potionEntity = new PotionEntity(this.world, this);
            potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
            potionEntity.setPitch(potionEntity.getPitch() - -20.0f);
            potionEntity.setVelocity(d, e + g * 0.2, f, 0.75f, 8.0f);
            if (!this.isSilent()) {
                this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0f, 0.8f + this.random.nextFloat() * 0.4f);
            }
            this.world.spawnEntity(potionEntity);

            ci.cancel();

        }
    }









}
