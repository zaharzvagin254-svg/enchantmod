package com.enchantmod;

import com.enchantmod.effect.BlueHellfireEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EnchantMod.MOD_ID);

    public static final RegistryObject<MobEffect> BLUE_HELLFIRE =
        MOB_EFFECTS.register("blue_hellfire", BlueHellfireEffect::new);
}
