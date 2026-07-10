package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public class GrapplingHookRenderer extends EntityRenderer<GrapplingHookEntity> {

    private static final ResourceLocation ROPE_TEXTURE =
            new ResourceLocation("ssc_furniture", "textures/entity/grappling_rope.png");

    private static final float HALF = 0.05F;
    private static final float UV_TILE = 0.25F;

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

        poseStack.pushPose();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        float sx = 0.0F, sy = 0.0F, sz = 0.0F;
        float ex = (float) delta.x, ey = (float) delta.y, ez = (float) delta.z;

        float rxf = (float) (rx * HALF), ryf = (float) (ry * HALF), rzf = (float) (rz * HALF);
        float uxf = (float) (ux * HALF), uyf = (float) (uy * HALF), uzf = (float) (uz * HALF);

        float v1 = (float) (len / UV_TILE);

        // +right face: normal = +right, corners: s+r+u, s+r-u, e+r-u, e+r+u
        emitQuad(buffer, matrix, normalMat, packedLight,
                sx + rxf + uxf, sy + ryf + uyf, sz + rzf + uzf,
                sx + rxf - uxf, sy + ryf - uyf, sz + rzf - uzf,
                ex + rxf - uxf, ey + ryf - uyf, ez + rzf - uzf,
                ex + rxf + uxf, ey + ryf + uyf, ez + rzf + uzf,
                (float) rx, (float) ry, (float) rz, v1);

        // -right face: normal = -right, corners: s-r-u, s-r+u, e-r+u, e-r-u
        emitQuad(buffer, matrix, normalMat, packedLight,
                sx - rxf - uxf, sy - ryf - uyf, sz - rzf - uzf,
                sx - rxf + uxf, sy - ryf + uyf, sz - rzf + uzf,
                ex - rxf + uxf, ey - ryf + uyf, ez - rzf + uzf,
                ex - rxf - uxf, ey - ryf - uyf, ez - rzf - uzf,
                (float) -rx, (float) -ry, (float) -rz, v1);

        // +up face: normal = +up, corners: s-r+u, s+r+u, e+r+u, e-r+u
        emitQuad(buffer, matrix, normalMat, packedLight,
                sx - rxf + uxf, sy - ryf + uyf, sz - rzf + uzf,
                sx + rxf + uxf, sy + ryf + uyf, sz + rzf + uzf,
                ex + rxf + uxf, ey + ryf + uyf, ez + rzf + uzf,
                ex - rxf + uxf, ey - ryf + uyf, ez - rzf + uzf,
                (float) ux, (float) uy, (float) uz, v1);

        // -up face: normal = -up, corners: s+r-u, s-r-u, e-r-u, e+r-u
        emitQuad(buffer, matrix, normalMat, packedLight,
                sx + rxf - uxf, sy + ryf - uyf, sz + rzf - uzf,
                sx - rxf - uxf, sy - ryf - uyf, sz - rzf - uzf,
                ex - rxf - uxf, ey - ryf - uyf, ez - rzf - uzf,
                ex + rxf - uxf, ey + ryf - uyf, ez + rzf - uzf,
                (float) -ux, (float) -uy, (float) -uz, v1);

        poseStack.popPose();
    }

    private static void emitQuad(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMat, int packedLight,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float nx, float ny, float nz, float v1) {
        buffer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(255, 255, 255, 255).uv(1.0F, v1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMat, nx, ny, nz).endVertex();
        buffer.vertex(matrix, x4, y4, z4).color(255, 255, 255, 255).uv(0.0F, v1).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMat, nx, ny, nz).endVertex();
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

        return new Vec3(handX, handY, handZ);
    }

    @Override
    public ResourceLocation getTextureLocation(GrapplingHookEntity entity) {
        return ROPE_TEXTURE;
    }
}
