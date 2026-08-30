package ly.ssc_furniture.client.mixin;

import ly.ssc_furniture.client.config.SSCFurnitureConfig;
import ly.ssc_furniture.compat.TrinketsCompat;
import net.minecraft.client.player.AbstractClientPlayer;
import net.onixary.shapeShifterCurseFabric.render.tech.SpiderTPEHR;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 装备了塑形蛛丝发射器 (在 extra_hand 槽) 时, 屏蔽 SSC 的第三人称手持 png 渲染.
 * 因为我们已经用 GrapplingHookAttachmentLayer 在尾部渲染了 3D 模型, 手部再显示 png 是重复.
 * 可通过配置 showShapingHookInExtraHand 强制显示 (默认 false = 隐藏).
 */
@Mixin(value = SpiderTPEHR.class, remap = false)
public class SpiderTPEHRHideMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void ssc_furniture$hideShapingHook(
            net.minecraft.client.renderer.ItemInHandRenderer heldItemRenderer,
            com.mojang.blaze3d.vertex.PoseStack matrices,
            net.minecraft.client.renderer.MultiBufferSource vertexConsumers,
            int light,
            AbstractClientPlayer player,
            float limbAngle, float limbDistance, float tickDelta,
            float animationProgress, float headYaw, float headPitch,
            CallbackInfo ci) {
        if (SSCFurnitureConfig.INSTANCE.showShapingHookInExtraHand) return;
        try {
            net.minecraft.world.item.ItemStack stack = AccessoryUtils.getEntitySlot(
                    player, "auto", "hand", "extra_hand", 0);
            if (stack != null && !stack.isEmpty() && TrinketsCompat.isShapingHook(stack.getItem())) {
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
