package ly.ssc_furniture.mixin;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.WaterbedBlock;
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
        Level level = level();
        BlockPos pos = blockPosition();
        if (!level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof WaterbedBlock || state.getBlock() instanceof BathtubBedBlock) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
    private void onIsEyeInFluid(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if (fluidTag == FluidTags.WATER) {
            Level level = level();
            BlockPos pos = blockPosition();
            if (!level.hasChunkAt(pos)) return;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof WaterbedBlock || state.getBlock() instanceof BathtubBedBlock) {
                cir.setReturnValue(true);
            }
        }
    }
}
