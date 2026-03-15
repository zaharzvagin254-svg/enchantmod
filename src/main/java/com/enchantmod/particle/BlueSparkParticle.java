package com.enchantmod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlueSparkParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected BlueSparkParticle(ClientLevel level, double x, double y, double z,
                                 double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;

        this.lifetime = 12 + random.nextInt(8);
        this.quadSize = 0.12f + random.nextFloat() * 0.08f;

        this.xd = vx + (random.nextDouble() - 0.5) * 0.15;
        this.yd = vy + 0.05 + random.nextDouble() * 0.08;
        this.zd = vz + (random.nextDouble() - 0.5) * 0.15;

        this.rCol = 0.1f + random.nextFloat() * 0.15f;
        this.gCol = 0.3f + random.nextFloat() * 0.3f;
        this.bCol = 0.85f + random.nextFloat() * 0.15f;
        this.alpha = 0.9f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.xd *= 0.88;
        this.yd *= 0.92;
        this.zd *= 0.88;
        this.yd += 0.004;
        this.alpha = 1.0f - (float) this.age / (float) this.lifetime;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float life = ((float) this.age + partialTick) / (float) this.lifetime;
        return this.quadSize * (1.0f - life * 0.5f);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double vx, double vy, double vz) {
            return new BlueSparkParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
