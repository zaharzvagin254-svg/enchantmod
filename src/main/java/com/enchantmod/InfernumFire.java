package com.enchantmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;

public class InfernumFire {

    // Поджигаем моба адским огнем (soul fire - синий)
    // В Minecraft soul fire хранится в NBT теге как "HasSoulFire" или через эффект горения в нижнем мире
    public static void setOnSoulFire(LivingEntity entity, int seconds) {
        // Поджигаем
        entity.setSecondsOnFire(seconds);

        // Устанавливаем NBT тег чтобы огонь был синим (soul fire)
        CompoundTag tag = entity.getPersistentData();
        tag.putBoolean("enchantmod_soul_fire", true);
        tag.putLong("enchantmod_soul_fire_until", entity.level().getGameTime() + (long)(seconds * 20));
    }

    public static boolean hasSoulFire(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        if (!tag.getBoolean("enchantmod_soul_fire")) return false;
        return entity.level().getGameTime() < tag.getLong("enchantmod_soul_fire_until");
    }
}
