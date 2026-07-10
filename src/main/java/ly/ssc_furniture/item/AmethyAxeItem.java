package ly.ssc_furniture.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


public class AmethyAxeItem extends AxeItem {
    public AmethyAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!target.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) target.level();
            BlockParticleOption particle = new BlockParticleOption(
                    ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState());
            serverLevel.sendParticles(particle,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    15, 0.3, 0.5, 0.3, 0.0);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockParticleOption particle = new BlockParticleOption(
                    ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState());
            serverLevel.sendParticles(particle,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.3, 0.3, 0.3, 0.0);
        }

        return super.mineBlock(stack, level, state, pos, miner);
    }
}
