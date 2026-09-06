package ly.ssc_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ly.ssc_furniture.block.DigestionWebBoxBlock;
import ly.ssc_furniture.block.DigestionWebBoxBlockEntity;
import ly.ssc_furniture.block.ModBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DigestionWebBoxItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private DigestionWebBoxBlockEntity dummy;

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (dummy == null) {
            dummy = new DigestionWebBoxBlockEntity(
                    BlockPos.ZERO,
                    ModBlocks.DIGESTION_WEB_BOX.defaultBlockState()
                            .setValue(DigestionWebBoxBlock.FACING, Direction.SOUTH));
        }
        Minecraft.getInstance().getBlockEntityRenderDispatcher()
                .renderItem(dummy, pose, buffers, light, overlay);
    }
}
