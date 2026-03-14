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
        // Наносим урон огнём каждые 20 тиков (1 секунда)
        // x2 от обычного Fire Aspect (обычный 1 урон, наш 2)
        entity.hurt(entity.level().damageSources().inFire(), 2.0f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Каждые 20 тиков = 1 секунда
        return duration % 20 == 0;
    }
}
