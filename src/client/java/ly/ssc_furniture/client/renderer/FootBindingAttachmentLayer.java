package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.compat.FormAdjustment;
import ly.ssc_furniture.compat.FormOffsetRegistry;
import ly.ssc_furniture.item.GustClothColorHelper;
import ly.ssc_furniture.item.ModItems;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * 缂犲竷鎸傝浇: 瑁呭鍒?FEET 妲?涓?澶勪簬闆嫄 tier 3 鏃?
 * 缂犲竷 L 鎸傚埌 (bipedRightLeg 鍔ㄦ€佺煩闃? 脳 (鍙傝€冮鏋?LeftPad2 闈欐€佸瓙閾剧煩闃?;
 * 缂犲竷 R 鎸傚埌 (bipedLeftLeg 鍔ㄦ€佺煩闃? 脳 (鍙傝€冮鏋?RightPad2 闈欐€佸瓙閾剧煩闃?.
 *
 * 涔嬫墍浠ュ乏鍙冲弽鎸? 鏄洜涓?SSC 闆嫄瀹樻柟 geo 閲?bipedLeftLeg/bipedRightLeg 鍙槸绌虹殑
 * vanilla Steve 鍔ㄧ敾閿氱偣, 鑰?瑙嗚涓婄殑宸﹀悗鑵?瀹為檯鏄寕鍦?bipedRightLeg 涓嬬殑 LeftLeg 閾? * (瑙?snow_fox_leg_reference.geo.json).
 */
public class FootBindingAttachmentLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation GEO_L =
            new ResourceLocation("ssc_furniture", "geo/entity/gust_cloth_for_foot_binding_l.geo.json");
    private static final ResourceLocation GEO_R =
            new ResourceLocation("ssc_furniture", "geo/entity/gust_cloth_for_foot_binding_r.geo.json");

    public FootBindingAttachmentLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.GUST_CLOTH_FOR_FOOT_BINDING)) return;

        if (!SSCFurniture.isGustClothActive(player)) return;

        BakedGeoModel modelL = GeckoLibCache.getBakedModels().get(GEO_L);
        BakedGeoModel modelR = GeckoLibCache.getBakedModels().get(GEO_R);
        if (modelL == null || modelR == null) return;

        // 缂犲竷 L 璧?bipedRightLeg 鈫?LeftPad2 閾?
                GeoBone rightLegBone = findFormBone(player, "bipedRightLeg");
        Matrix4f leftPad2Static = SnowFoxLegBoneCache.getLeftPad2Matrix();
        // 缂犲竷 R 璧?bipedLeftLeg 鈫?RightPad2 閾?
                GeoBone leftLegBone = findFormBone(player, "bipedLeftLeg");
        Matrix4f rightPad2Static = SnowFoxLegBoneCache.getRightPad2Matrix();

        ResourceLocation tex = GustClothColorHelper.getTexture(boots);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
        int packedOverlay = OverlayTexture.NO_OVERLAY;

        // 鎸夊舰鎬佸井璋?(translate + scale). 璧?FormOffsetRegistry, 鏀寔 instanceof 闅愬紡鍏变韩 + 鏁版嵁鍖呮墿灞?
        FormAdjustment adj = FormOffsetRegistry.resolveAdjustment(FormOffsetRegistry.EQ_GUST_CLOTH, player);

        if (rightLegBone != null && leftPad2Static != null) {
            renderAttached(poseStack, rightLegBone, leftPad2Static, modelL, buffer, packedLight, packedOverlay, adj);
        }
        if (leftLegBone != null && rightPad2Static != null) {
            renderAttached(poseStack, leftLegBone, rightPad2Static, modelR, buffer, packedLight, packedOverlay, adj);
        }
    }

    private static void renderAttached(PoseStack poseStack, GeoBone rootBiped, Matrix4f staticChain,
                                       BakedGeoModel model, VertexConsumer buffer,
                                       int packedLight, int packedOverlay, FormAdjustment adj) {
        poseStack.pushPose();

        // SSC form 鏍囧噯鍙樻崲 (鍙傝€?GrapplingHookAttachmentLayer):
        poseStack.mulPose(new org.joml.Quaternionf().rotationX((float) Math.PI));
        poseStack.translate(0.0F, -1.51F, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        poseStack.translate(0.5F, 0.51F, 0.5F);

        // 1. 杩涘叆 SSC 鍔ㄦ€佺殑 bipedRightLeg / bipedLeftLeg 楠ㄩ绌洪棿 (鍚?vanilla 璧拌矾鎽嗗姩)
        Matrix4f bipedMatrix = rootBiped.getModelSpaceMatrix();
        poseStack.mulPoseMatrix(new Matrix4f(bipedMatrix));
        RenderUtils.prepMatrixForBone(poseStack, rootBiped);

        // 2. 搴旂敤鍙傝€冮鏋剁殑闈欐€佸瓙閾剧煩闃?(biped 灞€閮ㄧ┖闂?鈫?LeftPad2/RightPad2 灞€閮ㄧ┖闂?
        poseStack.mulPoseMatrix(new Matrix4f(staticChain));

        // 3. 褰㈡€佺浉鍏冲井璋?(translate + scale)
        if (adj != null && adj.hasTranslation()) {
            poseStack.translate(adj.dx(), adj.dy(), adj.dz());
        }
        if (adj != null && adj.hasScale()) {
            float s = adj.scale();
            poseStack.scale(s, s, s);
        }

        // 鐜板湪澶勪簬 LeftPad2 / RightPad2 楠ㄩ灞€閮ㄧ┖闂? 鎸傜紶甯冩ā鍨嬮《灞傞楠?
                for (GeoBone bone : model.topLevelBones()) {
            renderBone(poseStack, bone, buffer, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    /** 鍦ㄧ帺瀹舵墍鏈?FormRenderer 閲屾壘鎸囧畾鍚嶅瓧鐨勯楠? 骞朵繚璇?tracking matrices 宸插紑. */
    private static GeoBone findFormBone(AbstractClientPlayer player, String name) {
        try {
            List<FormRenderer> renderers = FormRenderUtils.getPlayerAllFormRenderer(player);
            if (renderers == null) return null;
            for (FormRenderer fr : renderers) {
                if (fr == null) continue;
                FormModel fm = fr.realModel;
                if (fm == null) continue;
                GeoBone bone = fm.getCachedGeoBone(name);
                if (bone == null) continue;
                if (!bone.isTrackingMatrices()) bone.setTrackingMatrices(true);
                return bone;
            }
        } catch (Throwable t) {
            // form 鏈姞杞?/ 涓?mod 缂哄け, 闈欓粯 fallback
        }
        return null;
    }

    private static void renderBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
                                   int packedLight, int packedOverlay) {
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                RenderUtils.translateToPivotPoint(poseStack, cube);
                RenderUtils.rotateMatrixAroundCube(poseStack, cube);
                RenderUtils.translateAwayFromPivotPoint(poseStack, cube);
                Matrix3f normalMat = new Matrix3f(poseStack.last().normal());
                Matrix4f poseMat = new Matrix4f(poseStack.last().pose());

                for (GeoQuad quad : cube.quads()) {
                    if (quad == null) continue;
                    Vector3f n = normalMat.transform(new Vector3f(quad.normal()));
                    RenderUtils.fixInvertedFlatCube(cube, n);
                    for (GeoVertex vertex : quad.vertices()) {
                        Vector3f p = vertex.position();
                        Vector4f transformed = poseMat.transform(new Vector4f(p.x(), p.y(), p.z(), 1.0F));
                        buffer.vertex(transformed.x(), transformed.y(), transformed.z())
                                .color(1.0F, 1.0F, 1.0F, 1.0F)
                                .uv(vertex.texU(), vertex.texV())
                                .overlayCoords(packedOverlay)
                                .uv2(packedLight)
                                .normal(n.x(), n.y(), n.z())
                                .endVertex();
                    }
                }
                poseStack.popPose();
            }
        }
        if (!bone.isHidingChildren()) {
            for (GeoBone child : bone.getChildBones()) {
                renderBone(poseStack, child, buffer, packedLight, packedOverlay);
            }
        }
        poseStack.popPose();
    }
}
