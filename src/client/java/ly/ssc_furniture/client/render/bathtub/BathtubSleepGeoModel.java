package ly.ssc_furniture.client.render.bathtub;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class BathtubSleepGeoModel extends GeoModel<BathtubSleepAnimatable> {

    private static final ResourceLocation MODEL =
            new ResourceLocation("ssc_furniture", "geo/form_axolotl_3_bathtub_sleep.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("ssc_furniture", "textures/entity/form_axolotl_3_bathtub_sleep.png");

    @Override
    public ResourceLocation getModelResource(BathtubSleepAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BathtubSleepAnimatable animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BathtubSleepAnimatable animatable) {
        return null;
    }
}
