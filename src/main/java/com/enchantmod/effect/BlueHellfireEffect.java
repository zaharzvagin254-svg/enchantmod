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
        entity.hurt(entity.level().damageSources().magic(), 1.0f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
