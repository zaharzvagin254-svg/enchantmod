package com.enchantmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BlueHellfireEffect extends MobEffect {

    public BlueHellfireEffect() {
        super(MobEffectCategory.HARMFUL, 0x0044FF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // x2 damage compared to vanilla fire (vanilla onFire = 1 per second)
        entity.hurt(entity.level().damageSources().onFire(), 2.0f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Every 20 ticks = 1 second
        return duration % 20 == 0;
    }
}
