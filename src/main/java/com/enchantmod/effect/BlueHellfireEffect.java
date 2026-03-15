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
        // x2 damage compared to vanilla fire (vanilla = 1, ours = 2)
        entity.hurt(entity.level().damageSources().inFire(), 2.0f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
