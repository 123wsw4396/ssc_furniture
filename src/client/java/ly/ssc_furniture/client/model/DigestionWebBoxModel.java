package ly.ssc_furniture.client.model;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.block.DigestionWebBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

 public class DigestionWebBoxModel extends GeoModel<DigestionWebBoxBlockEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(SSCFurniture.MOD_ID, "geo/digestion_web_box.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SSCFurniture.MOD_ID, "textures/block/digestion_web_box.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(SSCFurniture.MOD_ID, "animations/digestion_web_box.animation.json");

    @Override
    public ResourceLocation getModelResource(DigestionWebBoxBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DigestionWebBoxBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DigestionWebBoxBlockEntity animatable) {
        return ANIMATION;
    }
}
