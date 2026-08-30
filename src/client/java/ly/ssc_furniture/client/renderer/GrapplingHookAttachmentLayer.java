package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.item.ModItems;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormModel;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderer;
import net.onixary.shapeShifterCurseFabric.render.form_render.FormRenderUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GrapplingHookAttachmentLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final Logger LOGGER = LoggerFactory.getLogger("ssc_furniture/HookLayer");
    private static long lastLogTick = 0L;
    private static long tickCounter = 0L;

    private static final ResourceLocation GEO_ID_TIER0 =
            new ResourceLocation("ssc_furniture", "geo/entity/spider_claw_tier0.geo.json");
    private static final ResourceLocation GEO_ID_TIER2 =
            new ResourceLocation("ssc_furniture", "geo/entity/spider_claw_tier2.geo.json");
    private static final ResourceLocation GEO_ID_TIER3 =
            new ResourceLocation("ssc_furniture", "geo/entity/spider_claw_tier3.geo.json");
    private static final ResourceLocation TEX_IRON =
            new ResourceLocation("ssc_furniture", "textures/entity/spider_claw_iron.png");
    private static final ResourceLocation TEX_DIAMOND =
            new ResourceLocation("ssc_furniture", "textures/entity/spider_claw_diamond.png");

    public GrapplingHookAttachmentLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        tickCounter++;
        boolean shouldLog = (tickCounter - lastLogTick) >= 100;

        ItemStack found = findGrapplingHook(player);
        if (found == null) return;

        SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(player);
        if (info == null) return;

        // tier 0 鐢?tier0 妯″瀷, tier 2 鐢?tier2, tier 3 鐢?tier3, tier 1 涓嶅姞杞?
                ResourceLocation geoId;
        switch (info.tier) {
            case 0 -> geoId = GEO_ID_TIER0;
            case 2 -> geoId = GEO_ID_TIER2;
            case 3 -> geoId = GEO_ID_TIER3;
            default -> { return; }
        }

        BakedGeoModel model = GeckoLibCache.getBakedModels().get(geoId);
        if (model == null) {
            if (shouldLog) {
                LOGGER.warn("[render] BakedGeoModel NULL for key={}, tier={}", geoId, info.tier);
                lastLogTick = tickCounter;
            }
            return;
        }

        GeoBone tailBone = findSpiderTailBone(player);

        ResourceLocation tex = (found.is(ModItems.DIAMOND_GRAPPLING_HOOK)
                || found.is(ModItems.SHAPING_GRAPPLING_HOOK_DIAMOND)) ? TEX_DIAMOND : TEX_IRON;
        RenderType renderType = RenderType.entityCutoutNoCull(tex);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        poseStack.pushPose();

        // SSC 涓?mod 鏍囧噯鍙樻崲 (鍙傝€?FormRenderFeature.render + GeoObjectRenderer.preRender):
        // 1. 缁?X 杞寸炕杞?180掳 (BlockBench Y-up 鍒?vanilla Y-down)
        poseStack.mulPose(new org.joml.Quaternionf().rotationX((float) Math.PI));
        // 2. FormRenderFeature 鐨勫钩绉?
                poseStack.translate(0.0F, -1.51F, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        // 3. AzureLib GeoObjectRenderer.preRender 鐨勫亸绉?(瀵归綈 modelRenderTranslations 鍧愭爣绯?
        poseStack.translate(0.5F, 0.51F, 0.5F);

        if (tailBone != null) {
            // 鏂规 B: 璺?tail_0 楠ㄩ鍚屾 (鍖呭惈 body 鏃嬭浆 + 灏惧反鎽嗗姩 + 韫蹭笅)
            // modelSpaceMatrix = "bipedBody 鍙樻崲鍚? tail_0 pivot 涔嬪墠"鐨勭┖闂?
                        Matrix4f tailMatrix = tailBone.getModelSpaceMatrix();
            poseStack.mulPoseMatrix(new Matrix4f(tailMatrix));
            // 鎵嬪姩搴旂敤 tail_0 鑷繁鐨?pivot + rot (璧拌矾 sway + drag + 鐜╁鏃嬭浆 drag)
            RenderUtils.prepMatrixForBone(poseStack, tailBone);

            // 鐜板湪澶勪簬 tail_0 灞€閮ㄥ潗鏍囩郴 (Bedrock 鍍忕礌鍗曚綅, 宸?/16 搴旂敤)
            // tail_0 灞€閮?+Y = 灏惧反鏈鏂瑰悜, -Z = 灏惧反鍚戝墠, +X = 鐜╁宸︿晶
            // 鎸?tier 娌垮熬宸村眬閮ㄥ潗鏍囪皟鏁村彂灏勫櫒浣嶇疆
            float localUp = 0.0F;
            float localForward = 0.0F;
            switch (info.tier) {
                case 0 -> { localUp = 0.3F; localForward = 0.1F; }
                case 1 -> { localUp = 0.35F; localForward = -0.2F; }
                case 2 -> { localUp = 0.4F; localForward = -0.4F; }
                case 3 -> { localUp = 0.0F; localForward = -0.8F; }
                default -> {}
            }
            poseStack.translate(0.0F, localUp, localForward);
        } else {
            // Fallback: 鏈壘鍒?tail_0, 浣跨敤 body 鐩稿闈欐€佷綅缃?
                        boolean isTier3 = info.tier >= 3;
            float backOffset = isTier3 ? 0.75F : 0.5F;
            float yOffset = isTier3 ? 0.7F : 0.9F;
            float extraUp = 0.0F;
            float extraForward = 0.0F;
            switch (info.tier) {
                case 0 -> { extraUp = 0.4F; extraForward = 0.1F; }
                case 2 -> { extraUp = 0.5F; extraForward = -0.5F; }
                case 3 -> { extraForward = -1.0F; }
                default -> {}
            }
            poseStack.translate(0.5F, yOffset + extraUp, -backOffset + 0.5F + extraForward);
        }

        int packedOverlay = OverlayTexture.NO_OVERLAY;
        boolean captureAnchor = info.tier == 0 || info.tier == 2 || info.tier == 3;
        for (GeoBone bone : model.topLevelBones()) {
            renderBone(poseStack, bone, buffer, packedLight, packedOverlay, captureAnchor, player);
        }

        poseStack.popPose();
    }

    /** 鎷垮埌鐜╁褰撳墠 spider FormModel 鐨?tail_0 楠ㄩ, 骞朵繚璇佸叾 tracking matrices 宸插紑鍚? */
    private static GeoBone findSpiderTailBone(AbstractClientPlayer player) {
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
            // 涓?mod 鍙兘鏈姞杞?spider form, 闈欓粯 fallback
        }
        return null;
    }

    private static ItemStack findGrapplingHook(AbstractClientPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (isGrapplingHook(main.getItem())) return main;
        ItemStack off = player.getOffhandItem();
        if (isGrapplingHook(off.getItem())) return off;
        ItemStack trinket = ly.ssc_furniture.compat.TrinketsCompat.findShapingHook(player);
        if (!trinket.isEmpty()) return trinket;
        return null;
    }

    private static boolean isGrapplingHook(Item item) {
        return item == ModItems.IRON_GRAPPLING_HOOK
                || item == ModItems.DIAMOND_GRAPPLING_HOOK
                || item == ModItems.SHAPING_GRAPPLING_HOOK_IRON
                || item == ModItems.SHAPING_GRAPPLING_HOOK_DIAMOND;
    }

    private static void renderBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
                                   int packedLight, int packedOverlay,
                                   boolean captureAnchor, AbstractClientPlayer player) {
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);

        boolean isMain = captureAnchor && "main".equals(bone.getName());

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                RenderUtils.translateToPivotPoint(poseStack, cube);
                RenderUtils.rotateMatrixAroundCube(poseStack, cube);
                RenderUtils.translateAwayFromPivotPoint(poseStack, cube);
                Matrix3f normalMat = new Matrix3f(poseStack.last().normal());
                Matrix4f poseMat = new Matrix4f(poseStack.last().pose());

                // 閫夐」 2: main bone 鐨勭涓€涓?cube 鐨勭涓€涓?vertex 涓栫晫鍧愭爣 鈫?anchor
                if (isMain && cube.quads().length > 0) {
                    GeoQuad firstQuad = cube.quads()[0];
                    if (firstQuad != null && firstQuad.vertices().length > 0) {
                        Vector3f p = firstQuad.vertices()[0].position();
                        Vector4f viewSpace = poseMat.transform(new Vector4f(p.x(), p.y(), p.z(), 1.0F));
                        net.minecraft.client.Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
                        Matrix4f viewRotInv = new Matrix4f()
                                .rotateX(camera.getXRot() * ((float) Math.PI / 180F))
                                .rotateY((camera.getYRot() + 180.0F) * ((float) Math.PI / 180F))
                                .invert();
                        Vector4f worldOffset = viewRotInv.transform(new Vector4f(viewSpace.x(), viewSpace.y(), viewSpace.z(), 1.0F));
                        Vec3 cam = camera.getPosition();
                        SpiderClawAnchor.set(player.getUUID(),
                                new Vec3(cam.x + worldOffset.x(), cam.y + worldOffset.y(), cam.z + worldOffset.z()));
                    }
                }

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
                renderBone(poseStack, child, buffer, packedLight, packedOverlay, captureAnchor, player);
            }
        }
        poseStack.popPose();
    }
}
