package dev.zenfyr.andromeda.modules.items.infinite_totem.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class KnockoffTotemParticle extends SimpleAnimatedParticle {

  KnockoffTotemParticle(
      ClientLevel world,
      double x,
      double y,
      double z,
      double velocityX,
      double velocityY,
      double velocityZ,
      SpriteSet spriteProvider) {
    super(world, x, y, z, spriteProvider, 1.25F);
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

  @Environment(EnvType.CLIENT)
  public record Factory(SpriteSet spriteProvider) implements ParticleProvider<SimpleParticleType> {

    @Override
    public Particle createParticle(
        SimpleParticleType defaultParticleType,
        ClientLevel clientWorld,
        double d,
        double e,
        double f,
        double g,
        double h,
        double i) {
      return new KnockoffTotemParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
    }
  }
}
