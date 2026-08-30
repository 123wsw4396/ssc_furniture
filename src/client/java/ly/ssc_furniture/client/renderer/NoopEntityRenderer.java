package ly.ssc_furniture.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NoopEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private static final ResourceLocation EMPTY = new ResourceLocation("minecraft", "textures/misc/white.png");

    public NoopEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return EMPTY;
    }

    @Override
    public boolean shouldRender(T entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return false;
    }
}
