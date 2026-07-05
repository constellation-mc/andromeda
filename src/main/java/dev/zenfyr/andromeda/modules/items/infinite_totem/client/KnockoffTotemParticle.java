package dev.zenfyr.andromeda.modules.items.infinite_totem.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class KnockoffTotemParticle extends SimpleAnimatedParticle {

  KnockoffTotemParticle(
      ClientLevel level,
      double x,
      double y,
      double z,
      double velocityX,
      double velocityY,
      double velocityZ,
      SpriteSet spriteProvider) {
    super(level, x, y, z, spriteProvider, 1.25F);
    this.friction = 0.6F;
    this.xd = velocityX;
    this.yd = velocityY;
    this.zd = velocityZ;
    this.quadSize *= 0.75F;
    this.lifetime = 60 + random.nextInt(12);
    this.setSpriteFromAge(spriteProvider);

    if (random.nextInt(2) == 0) {
      this.setColor(-1121831);
    } else {
      this.setColor(-329741);
    }
  }

  public record Factory(SpriteSet spriteProvider) implements ParticleProvider<SimpleParticleType> {

    @Override
    public @Nullable Particle createParticle(
        SimpleParticleType particleOptions,
        ClientLevel clientLevel,
        double d,
        double e,
        double f,
        double g,
        double h,
        double i,
        RandomSource randomSource) {
      return new KnockoffTotemParticle(clientLevel, d, e, f, g, h, i, this.spriteProvider);
    }
  }
}
