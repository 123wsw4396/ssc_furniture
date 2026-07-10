package ly.ssc_furniture.itemgroup;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.block.ModBlocks;
import ly.ssc_furniture.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroup {

    public static final CreativeModeTab SSC_FURNITURE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(SSCFurniture.MOD_ID, "ssc_furniture"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.ssc_furniture"))
                    .icon(() -> new ItemStack(ModBlocks.BATHTUB_BED))
                    .displayItems((context, entries) -> {
                        entries.accept(ModBlocks.WATERBED);
                        entries.accept(ModBlocks.BATHTUB_BED);
                        entries.accept(ModItems.AMETHYST_SWORD);
                        entries.accept(ModItems.AMETHYST_AXE);
                        entries.accept(ModItems.IRON_GRAPPLING_HOOK);
                        entries.accept(ModItems.DIAMOND_GRAPPLING_HOOK);
                    })
                    .build()
    );

    public static void register() {
        SSCFurniture.LOGGER.info("Registering creative tab for " + SSCFurniture.MOD_ID);
    }
}
