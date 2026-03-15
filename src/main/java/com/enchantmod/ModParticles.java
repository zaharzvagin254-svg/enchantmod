package com.enchantmod;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, EnchantMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> BLUE_SPARK =
        PARTICLE_TYPES.register("blue_spark", () -> new SimpleParticleType(false));
}
