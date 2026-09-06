package ly.ssc_furniture.compat;

/**
 * Per-form 渲染微调: 三轴像素平移 + 均匀缩放.
 * 单位: model-pixel (poseStack 已在骨骼局部空间的像素尺度下, 不用除以 16).
 */
public record FormAdjustment(float dx, float dy, float dz, float scale) {
    public static final FormAdjustment IDENTITY = new FormAdjustment(0f, 0f, 0f, 1f);

    public boolean hasTranslation() {
        return dx != 0f || dy != 0f || dz != 0f;
    }

    public boolean hasScale() {
        return scale != 1f;
    }

    public boolean isIdentity() {
        return !hasTranslation() && !hasScale();
    }
}
