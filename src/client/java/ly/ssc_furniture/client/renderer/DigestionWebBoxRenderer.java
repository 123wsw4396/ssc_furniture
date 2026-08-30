package ly.ssc_furniture.client.renderer;

import ly.ssc_furniture.block.DigestionWebBoxBlockEntity;
import ly.ssc_furniture.client.model.DigestionWebBoxModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DigestionWebBoxRenderer extends GeoBlockRenderer<DigestionWebBoxBlockEntity> {

    public DigestionWebBoxRenderer(BlockEntityRendererProvider.Context context) {
        super(new DigestionWebBoxModel());
    }
}
