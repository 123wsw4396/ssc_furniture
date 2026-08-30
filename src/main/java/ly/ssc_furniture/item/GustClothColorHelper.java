package ly.ssc_furniture.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

/**
 * 轻快缠布颜色映射: 把 ItemStack 的染色 int RGB 映射到最近的 vanilla DyeColor,
 * 并返回对应贴图 ResourceLocation.
 */
public final class GustClothColorHelper {

    private static final ResourceLocation BASE_TEX =
            new ResourceLocation("ssc_furniture", "textures/entity/gust_cloth_for_foot_binding.png");

    private GustClothColorHelper() {}

    /** vanilla 16 色的贴图纹理颜色 (DyeColor.getTextureDiffuseColor 返回 float[3], 转 int RGB). */
    public static DyeColor closestDyeColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        DyeColor best = DyeColor.WHITE;
        double bestDist = Double.MAX_VALUE;
        for (DyeColor c : DyeColor.values()) {
            float[] rgbf = c.getTextureDiffuseColors();
            int cr = (int) (rgbf[0] * 255);
            int cg = (int) (rgbf[1] * 255);
            int cb = (int) (rgbf[2] * 255);
            double d = (cr - r) * (cr - r) + (cg - g) * (cg - g) + (cb - b) * (cb - b);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        return best;
    }

    /**
     * 根据 stack 染色状态返回缠布 3D 模型贴图 ResourceLocation.
     * 未染色 → 基础贴图; 已染色 → 最近 vanilla 色对应贴图.
     */
    public static ResourceLocation getTexture(ItemStack stack) {
        if (!(stack.getItem() instanceof DyeableLeatherItem dyeable) || !dyeable.hasCustomColor(stack)) {
            return BASE_TEX;
        }
        DyeColor color = closestDyeColor(dyeable.getColor(stack));
        return new ResourceLocation("ssc_furniture",
                "textures/entity/gust_cloth_for_foot_binding_" + color.getName() + ".png");
    }

    /** predicate 值: 未染色返回 0, 已染色返回 (index+1)/17 (0..16 档). */
    public static float getModelPredicateValue(ItemStack stack) {
        if (!(stack.getItem() instanceof DyeableLeatherItem dyeable) || !dyeable.hasCustomColor(stack)) {
            return 0.0F;
        }
        DyeColor color = closestDyeColor(dyeable.getColor(stack));
        return (color.getId() + 1) / 17.0F;
    }
}
