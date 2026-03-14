package com.enchantmod;

import com.enchantmod.enchantments.BlastShotEnchantment;
import com.enchantmod.enchantments.BloodLeechEnchantment;
import com.enchantmod.enchantments.BladeFuryEnchantment;
import com.enchantmod.enchantments.InfernumEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
        DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EnchantMod.MOD_ID);

    public static final RegistryObject<Enchantment> BLAST_SHOT =
        ENCHANTMENTS.register("blast_shot", BlastShotEnchantment::new);

    public static final RegistryObject<Enchantment> VAMPIRISM =
        ENCHANTMENTS.register("vampirism", BloodLeechEnchantment::new);

    public static final RegistryObject<Enchantment> BLADE_FURY =
        ENCHANTMENTS.register("blade_fury", BladeFuryEnchantment::new);

    public static final RegistryObject<Enchantment> INFERNUM =
        ENCHANTMENTS.register("infernum", InfernumEnchantment::new);
}
