package ly.ssc_furniture.client.render.bathtub;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class BathtubSleepAxolotl2HumanGeoModel extends GeoModel<BathtubSleepAxolotl2HumanAnimatable> {

    private static final ResourceLocation MODEL =
            new ResourceLocation("ssc_furniture", "geo/form_axolotl_2_bathtub_sleep_human.geo.json");
    // texture 浼氬湪娓叉煋鏃堕€氳繃 RenderType 鎸囧畾 (鐜╁ skin 鎴?ssc_base_skin), 杩欓噷杩斿洖鍗犱綅
    private static final ResourceLocation FALLBACK_TEXTURE =
            new ResourceLocation("shape-shifter-curse",
                    "textures/entity/base_player/ssc_base_skin.png");

    @Override
    public ResourceLocation getModelResource(BathtubSleepAxolotl2HumanAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BathtubSleepAxolotl2HumanAnimatable animatable) {
        return FALLBACK_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BathtubSleepAxolotl2HumanAnimatable animatable) {
        return null;
    }
}
