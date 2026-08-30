package ly.ssc_furniture.client.render.bathtub;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.WaterbedBlock;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

public class BathtubSleepAxolotl2HumanRenderer {

    private static final ResourceLocation SSC_BASE_SKIN =
            new ResourceLocation("shape-shifter-curse",
                    "textures/entity/base_player/ssc_base_skin.png");

    private static GeoObjectRenderer<BathtubSleepAxolotl2HumanAnimatable> renderer;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BathtubSleepAxolotl2HumanRenderer::onRender);
    }

    private static GeoObjectRenderer<BathtubSleepAxolotl2HumanAnimatable> getRenderer() {
        if (renderer == null) {
            renderer = new GeoObjectRenderer<>(new BathtubSleepAxolotl2HumanGeoModel());
        }
        return renderer;
    }

    private static ResourceLocation getSkinTextureFor(Player player) {
        if (FormCheck.shouldKeepOriginalSkin(player)) {
            if (player instanceof AbstractClientPlayer acp) {
                try {
                    return acp.getSkinTextureLocation();
                } catch (Throwable t) {
                    // fall through
                }
            }
        }
        return SSC_BASE_SKIN;
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
            if (!FormCheck.isAxolotl2SleepPoseEnabled(p)) continue;
            Optional<BlockPos> sleepPosOpt = p.getSleepingPos();
            if (sleepPosOpt.isEmpty()) continue;
            BlockPos sleepPos = sleepPosOpt.get();
            BlockState bs = level.getBlockState(sleepPos);
            if (!(bs.getBlock() instanceof BathtubBedBlock) && !(bs.getBlock() instanceof WaterbedBlock)) continue;

            Direction facing = bs.getValue(BedBlock.FACING);
            BlockPos footPos;
            if (bs.getValue(BedBlock.PART) == BedPart.FOOT) {
                footPos = sleepPos;
            } else {
                footPos = sleepPos.relative(facing.getOpposite());
            }
            BlockState footState = level.getBlockState(footPos);
            if (!(footState.getBlock() instanceof BathtubBedBlock) && !(footState.getBlock() instanceof WaterbedBlock)) continue;
            if (footState.getValue(BedBlock.PART) != BedPart.FOOT) continue;

            Direction headDir = facing;
            Direction leftDir = facing.getCounterClockWise();
            double worldDx = headDir.getStepX() * 2 + leftDir.getStepX();
            double worldDz = headDir.getStepZ() * 2 + leftDir.getStepZ();

            double bx = (footPos.getX() + 0.5 + worldDx) - camPos.x;
            double by = footPos.getY() - camPos.y - 0.51;
            double bz = (footPos.getZ() + 0.5 + worldDz) - camPos.z;

            poseStack.pushPose();
            poseStack.translate(bx, by, bz);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-facing.toYRot() + 180.0f));
            poseStack.translate(0.5, 0.0, 0.5);

            int light = LevelRenderer.getLightColor(level, footPos);

            ResourceLocation tex = getSkinTextureFor(p);
            RenderType rt = RenderType.entityCutoutNoCull(tex);
            getRenderer().render(poseStack, BathtubSleepAxolotl2HumanAnimatable.INSTANCE, bufferSource, rt, null, light);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
