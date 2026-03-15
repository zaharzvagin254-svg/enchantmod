package com.enchantmod.render;

import com.enchantmod.EnchantMod;
import com.enchantmod.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnchantMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) {
            entity.getPersistentData().putInt("saved_fire_ticks", entity.getRemainingFireTicks());
            entity.setRemainingFireTicks(-1);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.BLUE_HELLFIRE.get())) {
            int ticks = entity.getPersistentData().getInt("saved_fire_ticks");
            entity.setRemainingFireTicks(ticks);
        }
    }
}
