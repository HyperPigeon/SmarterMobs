package net.hyper_pigeon.smartermobs.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class BuffTargetGoal <T extends LivingEntity> extends ActiveTargetGoal<T> {

    private static final int MAX_COOLDOWN = 600;
    private int cooldown = 0;
    private boolean enabled = false;

    public BuffTargetGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
        super(mob, targetClass, checkVisibility);
    }

    public BuffTargetGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, checkVisibility, targetPredicate);
    }

    public BuffTargetGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility, boolean checkCanNavigate) {
        super(mob, targetClass, checkVisibility, checkCanNavigate);
    }

    public BuffTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void decreaseCooldown() {
        --this.cooldown;
    }

    public void setEnabled(boolean isEnabled){
        enabled = isEnabled;
    }

    @Override
    public boolean canStart() {
        if (!enabled || this.cooldown > 0 || !this.mob.getRandom().nextBoolean()) {
            return false;
        }
        this.findClosestTarget();
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.cooldown = BuffTargetGoal.toGoalTicks(MAX_COOLDOWN);
        super.start();
    }

    public LivingEntity getTarget(){
        return this.targetEntity;
    }
}
