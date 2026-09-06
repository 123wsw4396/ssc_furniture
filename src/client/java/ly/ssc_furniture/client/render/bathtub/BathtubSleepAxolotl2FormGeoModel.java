package ly.ssc_furniture.client.render.bathtub;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class BathtubSleepAxolotl2FormGeoModel extends GeoModel<BathtubSleepAxolotl2FormAnimatable> {

    private static final ResourceLocation MODEL =
            new ResourceLocation("ssc_furniture", "geo/form_axolotl_2_bathtub_sleep_form.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("ssc_furniture", "textures/entity/form_axolotl_2_bathtub_sleep.png");

    @Override
    public ResourceLocation getModelResource(BathtubSleepAxolotl2FormAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BathtubSleepAxolotl2FormAnimatable animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BathtubSleepAxolotl2FormAnimatable animatable) {
        return null;
    }
}
