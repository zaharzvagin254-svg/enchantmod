package com.enchantmod;

import com.enchantmod.enchantments.BlastShotEnchantment;
import com.enchantmod.enchantments.BloodLeechEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
        DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EnchantMod.MOD_ID);

    public static final RegistryObject<Enchantment> BLAST_SHOT =
        ENCHANTMENTS.register("blast_shot", BlastShotEnchantment::new);

    public static final RegistryObject<Enchantment> BLOOD_LEECH =
        ENCHANTMENTS.register("blood_leech", BloodLeechEnchantment::new);
}
