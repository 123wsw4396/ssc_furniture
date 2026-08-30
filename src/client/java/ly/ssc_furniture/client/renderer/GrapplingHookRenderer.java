package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

import java.util.List;

public class GrapplingHookRenderer extends EntityRenderer<GrapplingHookEntity> {

    private static final ResourceLocation ROPE_TEXTURE =
            new ResourceLocation("ssc_furniture", "textures/entity/grappling_rope.png");

    private static final float HALF = 0.05F;
    private static final float UV_TILE = 0.25F;
    private static final int SEGMENTS = 16;

    public GrapplingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GrapplingHookEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!(entity.getOwner() instanceof Player player)) return;

        Vec3 handPos = getHandPosition(player, partialTick);
        Vec3 hookPos = entity.getPosition(partialTick);
        Vec3 delta = handPos.subtract(hookPos);

        double len = delta.length();
        if (len < 1.0E-4) return;

        double dirX = delta.x / len;
        double dirY = delta.y / len;
        double dirZ = delta.z / len;

        double refX, refY, refZ;
        if (Math.abs(dirY) < 0.9) {
            refX = 0.0; refY = 1.0; refZ = 0.0;
        } else {
            refX = 1.0; refY = 0.0; refZ = 0.0;
        }

        double rx = dirY * refZ - dirZ * refY;
        double ry = dirZ * refX - dirX * refZ;
        double rz = dirX * refY - dirY * refX;
        double rLen = Math.sqrt(rx * rx + ry * ry + rz * rz);
        rx /= rLen; ry /= rLen; rz /= rLen;

        double ux = ry * dirZ - rz * dirY;
        double uy = rz * dirX - rx * dirZ;
        double uz = rx * dirY - ry * dirX;

        // 涓ょ鍏夌収閲囨牱: 鎵嬩綅缃?(璺熼殢鐜╁) 涓?閽╁瓙浣嶇疆 (鍙傛暟浼犲叆)
        Level level = entity.level();
        int handLight = LevelRenderer.getLightColor(level, BlockPos.containing(handPos));
        int hookLight = packedLight;
        int handBlock = handLight & 0xFFFF;
        int handSky = (handLight >> 16) & 0xFFFF;
        int hookBlock = hookLight & 0xFFFF;
        int hookSky = (hookLight >> 16) & 0xFFFF;

        poseStack.pushPose();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        // hookPos 鍒?handPos: 娈?s 浠?0..SEGMENTS-1, s=0 璧风偣=hookPos, s=SEGMENTS 缁堢偣=handPos
        // relative 鍧愭爣鍩哄噯鏄?entity 浣嶇疆 (hookPos), 鍥犳: p(t) = delta * t, t \in [0, 1]
        float rxf = (float) (rx * HALF), ryf = (float) (ry * HALF), rzf = (float) (rz * HALF);
        float uxf = (float) (ux * HALF), uyf = (float) (uy * HALF), uzf = (float) (uz * HALF);
        float dxT = (float) delta.x, dyT = (float) delta.y, dzT = (float) delta.z;
        float vTotal = (float) (len / UV_TILE);

        for (int s = 0; s < SEGMENTS; s++) {
            float t0 = s / (float) SEGMENTS;
            float t1 = (s + 1) / (float) SEGMENTS;

            float ax = dxT * t0, ay = dyT * t0, az = dzT * t0;
            float bx = dxT * t1, by = dyT * t1, bz = dzT * t1;

            int light0 = lerpLight(t0, handBlock, hookBlock, handSky, hookSky);
            int light1 = lerpLight(t1, handBlock, hookBlock, handSky, hookSky);

            float v0 = t0 * vTotal;
            float v1 = t1 * vTotal;

            // 4 quad 鏂瑰舰妯埅闈? +right / -right / +up / -up 鍥涗釜闈? 瑙嗚鏇寸矖
            // +right 闈?(娉曠嚎 +right, 娌?up 灞曞紑)
            emitQuad(buffer, matrix, normalMat,
                    ax + rxf - uxf, ay + ryf - uyf, az + rzf - uzf,
                    ax + rxf + uxf, ay + ryf + uyf, az + rzf + uzf,
                    bx + rxf + uxf, by + ryf + uyf, bz + rzf + uzf,
                    bx + rxf - uxf, by + ryf - uyf, bz + rzf - uzf,
                    (float) rx, (float) ry, (float) rz,
                    v0, v1, light0, light1);

            // -right 闈?(娉曠嚎 -right, 娌?up 灞曞紑)
            emitQuad(buffer, matrix, normalMat,
                    ax - rxf + uxf, ay - ryf + uyf, az - rzf + uzf,
                    ax - rxf - uxf, ay - ryf - uyf, az - rzf - uzf,
                    bx - rxf - uxf, by - ryf - uyf, bz - rzf - uzf,
                    bx - rxf + uxf, by - ryf + uyf, bz - rzf + uzf,
                    (float) -rx, (float) -ry, (float) -rz,
                    v0, v1, light0, light1);

            // +up 闈?(娉曠嚎 +up, 娌?right 灞曞紑)
            emitQuad(buffer, matrix, normalMat,
                    ax - rxf + uxf, ay - ryf + uyf, az - rzf + uzf,
                    ax + rxf + uxf, ay + ryf + uyf, az + rzf + uzf,
                    bx + rxf + uxf, by + ryf + uyf, bz + rzf + uzf,
                    bx - rxf + uxf, by - ryf + uyf, bz - rzf + uzf,
                    (float) ux, (float) uy, (float) uz,
                    v0, v1, light0, light1);

            // -up 闈?(娉曠嚎 -up, 娌?right 灞曞紑)
            emitQuad(buffer, matrix, normalMat,
                    ax + rxf - uxf, ay + ryf - uyf, az + rzf - uzf,
                    ax - rxf - uxf, ay - ryf - uyf, az - rzf - uzf,
                    bx - rxf - uxf, by - ryf - uyf, bz - rzf - uzf,
                    bx + rxf - uxf, by + ryf - uyf, bz + rzf - uzf,
                    (float) -ux, (float) -uy, (float) -uz,
                    v0, v1, light0, light1);
        }

        poseStack.popPose();
    }

    private static int lerpLight(float t, int handBlock, int hookBlock, int handSky, int hookSky) {
        // t=0 -> hook 绔? t=1 -> hand 绔?
                int block = Math.round(hookBlock + (handBlock - hookBlock) * t);
        int sky = Math.round(hookSky + (handSky - hookSky) * t);
        return (block & 0xFFFF) | ((sky & 0xFFFF) << 16);
    }

    private static void emitQuad(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMat,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float nx, float ny, float nz,
                                 float v0, float v1, int light0, int light1) {
        // 椤剁偣椤哄簭: 璧风偣娈典袱鐐?(light0), 缁堢偣娈典袱鐐?(light1)
        buffer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255).uv(0.0F, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light0).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255).uv(1.0F, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light0).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(255, 255, 255, 255).uv(1.0F, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light1).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x4, y4, z4).color(255, 255, 255, 255).uv(0.0F, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light1).normal(normalMat, nx, ny, nz).endVertex();
    }

    private Vec3 getHandPosition(Player player, float partialTick) {
        double px = Mth.lerp(partialTick, player.xo, player.getX());
        double py = Mth.lerp(partialTick, player.yo, player.getY());
        double pz = Mth.lerp(partialTick, player.zo, player.getZ());

        float yaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * 0.017453292F;
        float sinYaw = Mth.sin(yaw);
        float cosYaw = Mth.cos(yaw);

        SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(player);
        boolean isTier3 = info != null && info.tier >= 3;
        double backOffset = isTier3 ? 0.75 : 0.5;
        double yOffset = isTier3 ? 0.7 : 0.9;

        double backX = -(-sinYaw);
        double backZ = -(cosYaw);
        double handX = px + backX * backOffset;
        double handY = py + yOffset;
        double handZ = pz + backZ * backOffset;

        // tier 0/2/3: 浼樺厛璇?attachment layer 鎹曡幏鐨?main bone 涓栫晫閿氱偣
        if (info != null && (info.tier == 0 || info.tier == 2 || info.tier == 3)) {
            Vec3 anchor = SpiderClawAnchor.get(player.getUUID());
            if (anchor != null) {
                return anchor;
            }
        }

        // tier 0 fallback: 鏂规 A 鎵嬬畻 (绗竴浜虹О / attachment layer 鏈覆鏌撴椂)
        if (info != null && info.tier == 0) {
            double forwardX = -sinYaw;
            double forwardZ = cosYaw;
            handX += forwardX * 0.1;
            handY -= 0.1;
            handZ += forwardZ * 0.1;

            GeoBone tail = findSpiderTailBone(player);
            if (tail != null) {
                float rotZ = tail.getRotZ();
                float rotX = tail.getRotX();
                double swayArm = 0.2;
                double localDx = swayArm * Math.sin(rotZ);
                double localDy = swayArm * Math.sin(rotX);
                double rightX = cosYaw;
                double rightZ = sinYaw;
                handX += rightX * localDx;
                handY += localDy;
                handZ += rightZ * localDx;
            }
        }

        return new Vec3(handX, handY, handZ);
    }

    private static GeoBone findSpiderTailBone(Player player) {
        try {
            List<FormRenderer> renderers = FormRenderUtils.getPlayerAllFormRenderer(player);
            if (renderers == null) return null;
            for (FormRenderer fr : renderers) {
                if (fr == null) continue;
                FormModel fm = fr.realModel;
                if (fm == null) continue;
                GeoBone bone = fm.getCachedGeoBone("tail_0");
                if (bone == null) continue;
                if (!bone.isTrackingMatrices()) bone.setTrackingMatrices(true);
                return bone;
            }
        } catch (Throwable t) {
            // 闈欓粯 fallback
        }
        return null;
    }

    @Override
    public ResourceLocation getTextureLocation(GrapplingHookEntity entity) {
        return ROPE_TEXTURE;
    }
}
