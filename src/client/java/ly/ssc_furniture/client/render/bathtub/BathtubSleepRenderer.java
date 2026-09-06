package ly.ssc_furniture.client.render.bathtub;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import ly.ssc_furniture.block.BathtubBedBlock;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils.ColorSetting;

public class BathtubSleepRenderer {

    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation("ssc_furniture", "textures/entity/form_axolotl_3_bathtub_sleep.png");
    private static final ResourceLocation COLORMASK =
            new ResourceLocation("shape-shifter-curse",
                    "textures/form/form_axolotl_3/form_axolotl_3_colormask.png");

    private static final Map<ColorSetting, ResourceLocation> BAKED_CACHE = new HashMap<>();

    private static GeoObjectRenderer<BathtubSleepAnimatable> renderer;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BathtubSleepRenderer::onRender);
    }

    private static GeoObjectRenderer<BathtubSleepAnimatable> getRenderer() {
        if (renderer == null) {
            renderer = new GeoObjectRenderer<>(new BathtubSleepGeoModel());
        }
        return renderer;
    }

    private static ResourceLocation getTextureFor(Player player) {
        if (!FormCheck.isFormColorEnabled(player)) {
            return BASE_TEXTURE;
        }
        ColorSetting cs;
        try {
            cs = FormTextureUtils.getPlayerColorSetting(player);
        } catch (Throwable t) {
            return BASE_TEXTURE;
        }
        if (cs == null) return BASE_TEXTURE;
        ResourceLocation cached = BAKED_CACHE.get(cs);
        if (cached != null) return cached;
        try {
            ResourceLocation baked = FormTextureUtils.BakeTexture(BASE_TEXTURE, COLORMASK, cs, false);
            if (baked != null) {
                BAKED_CACHE.put(cs, baked);
                return baked;
            }
        } catch (Throwable t) {
            // fall through
        }
        return BASE_TEXTURE;
    }

    private static void onRender(WorldRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        Camera cam = ctx.camera();
        Vec3 camPos = cam.getPosition();
        PoseStack poseStack = ctx.matrixStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (Player p : level.players()) {
            if (!p.isSleeping()) continue;
            if (!FormCheck.isAxolotlSleepPoseEnabled(p)) continue;
            Optional<BlockPos> sleepPosOpt = p.getSleepingPos();
            if (sleepPosOpt.isEmpty()) continue;
            BlockPos sleepPos = sleepPosOpt.get();
            BlockState bs = level.getBlockState(sleepPos);
            if (!(bs.getBlock() instanceof BathtubBedBlock)) continue;

            // vanilla BedBlock: FACING 浠?FOOT 鎸囧悜 HEAD, 涓ゆ FACING 鐩稿悓
            Direction facing = bs.getValue(BathtubBedBlock.FACING);
            BlockPos footPos;
            if (bs.getValue(BathtubBedBlock.PART) == BedPart.FOOT) {
                footPos = sleepPos;
            } else {
                footPos = sleepPos.relative(facing.getOpposite());
            }
            BlockState footState = level.getBlockState(footPos);
            if (!(footState.getBlock() instanceof BathtubBedBlock)) continue;
            if (footState.getValue(BathtubBedBlock.PART) != BedPart.FOOT) continue;

            // 涓栫晫鍧愭爣琛ュ伩: 娌?(灏惧反鏂瑰悜 + 闈㈡湞搴婂ご鍙虫墜鏂瑰悜) 鍚?1 鏍?
                        Direction tailDir = facing.getOpposite();
            Direction rightDir = facing.getClockWise();
            double worldDx = tailDir.getStepX() + rightDir.getStepX();
            double worldDz = tailDir.getStepZ() + rightDir.getStepZ();

            double bx = (footPos.getX() + 0.5 + worldDx) - camPos.x;
            double by = footPos.getY() - camPos.y - 0.34;
            double bz = (footPos.getZ() + 0.5 + worldDz) - camPos.z;

            poseStack.pushPose();
            poseStack.translate(bx, by, bz);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(0.5, 0.0, 0.5);

            int light = LevelRenderer.getLightColor(level, footPos);

            ResourceLocation tex = getTextureFor(p);
            RenderType rt = RenderType.entityTranslucent(tex);
            getRenderer().render(poseStack, BathtubSleepAnimatable.INSTANCE, bufferSource, rt, null, light);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
