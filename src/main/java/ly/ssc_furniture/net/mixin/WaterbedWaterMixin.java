package ly.textmod.net.mixin;

import ly.textmod.net.block.WaterbedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class WaterbedWaterMixin {

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract BlockPos blockPosition();

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void onIsInWater(CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = blockPosition();
        BlockState state = level().getBlockState(pos);
        if (state.getBlock() instanceof WaterbedBlock) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
    private void onIsEyeInFluid(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if (fluidTag == FluidTags.WATER) {
            BlockPos pos = blockPosition();
            BlockState state = level().getBlockState(pos);
            if (state.getBlock() instanceof WaterbedBlock) {
                cir.setReturnValue(true);
            }
        }
    }
}