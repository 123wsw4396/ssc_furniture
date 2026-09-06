package ly.ssc_furniture.client.mixin;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.WaterbedBlock;
import ly.ssc_furniture.client.render.bathtub.FormCheck;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void ssc_furniture$hideWhenBathtubSleep(AbstractClientPlayer player, float entityYaw, float partialTicks,
                                                    com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                    net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                    int packedLight, CallbackInfo ci) {
        if (!player.isSleeping()) return;
        if (!FormCheck.isAnyAxolotlSleepPoseEnabled(player)) return;
        Optional<BlockPos> posOpt = player.getSleepingPos();
        if (posOpt.isEmpty()) return;
        Level level = player.level();
        BlockState bs = level.getBlockState(posOpt.get());
        boolean isBathtub = bs.getBlock() instanceof BathtubBedBlock;
        boolean isWaterbed = bs.getBlock() instanceof WaterbedBlock;
        if (!isBathtub && !isWaterbed) return;
        // tier 4 睡姿只在浴缸床上隐藏玩家; tier 3 睡姿在浴缸床和水床上都隐藏
        if (!isBathtub && !FormCheck.isAxolotl2SleepPoseEnabled(player)) return;
        ci.cancel();
    }
}
