package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtils;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class SnowFoxLegBoneCache {

    private static final ResourceLocation REFERENCE_GEO =
            new ResourceLocation("ssc_furniture", "geo/entity/snow_fox_leg_reference.geo.json");

    private static Matrix4f leftPad2Matrix;
    private static Matrix4f rightPad2Matrix;
    private static boolean triedLoad = false;
    private static boolean loaded = false;

    private SnowFoxLegBoneCache() {}

    private static GeoBone findBone(BakedGeoModel model, String name) {
        for (GeoBone top : model.topLevelBones()) {
            GeoBone found = findBoneRec(top, name);
            if (found != null) return found;
        }
        return null;
    }

    private static GeoBone findBoneRec(GeoBone bone, String name) {
        if (name.equals(bone.getName())) return bone;
        for (GeoBone child : bone.getChildBones()) {
            GeoBone found = findBoneRec(child, name);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * 浠?rootParentName 鐨勫眬閮ㄧ┖闂?prep 瀹屾瘯鍚?, 绱Н璧板埌 targetName 楠ㄩ鐨?prep 瀹屾瘯鐘舵€?
     * 杩斿洖璇ョ疮绉潤鎬佺煩闃? rootParentName 鏈韩鐨?prep 涓嶅寘鍚湪缁撴灉閲?
     */
    private static Matrix4f computeChainMatrix(BakedGeoModel model, String rootParentName, String targetName) {
        GeoBone target = findBone(model, targetName);
        if (target == null) return null;

        java.util.List<GeoBone> chain = new java.util.ArrayList<>();
        GeoBone cur = target;
        while (cur != null && !rootParentName.equals(cur.getName())) {
            chain.add(0, cur);
            cur = cur.getParent();
        }
        if (cur == null) return null;

        PoseStack ps = new PoseStack();
        for (GeoBone b : chain) {
            RenderUtils.prepMatrixForBone(ps, b);
        }
        return new Matrix4f(ps.last().pose());
    }

    private static void tryLoad() {
        if (triedLoad) return;
        triedLoad = true;
        try {
            BakedGeoModel model = GeckoLibCache.getBakedModels().get(REFERENCE_GEO);
            if (model == null) {
                triedLoad = false;
                return;
            }
            leftPad2Matrix = computeChainMatrix(model, "bipedRightLeg", "LeftPad2");
            rightPad2Matrix = computeChainMatrix(model, "bipedLeftLeg", "RightPad2");
            loaded = leftPad2Matrix != null && rightPad2Matrix != null;
        } catch (Throwable t) {
            loaded = false;
        }
    }

    public static Matrix4f getLeftPad2Matrix() {
        if (!loaded) tryLoad();
        return leftPad2Matrix;
    }

    public static Matrix4f getRightPad2Matrix() {
        if (!loaded) tryLoad();
        return rightPad2Matrix;
    }
}
