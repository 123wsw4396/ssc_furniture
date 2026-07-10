package ly.textmod.net.block;

import ly.textmod.net.TextMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class    ModBlocks {

    public static final Block WATERBED = registerBlock("waterbed",
            new WaterbedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).noOcclusion()));

    public static final BlockEntityType<WaterbedBlockEntity> WATERBED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(TextMod.MOD_ID, "waterbed"),
                    FabricBlockEntityTypeBuilder.create(WaterbedBlockEntity::new, WATERBED).build());

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(TextMod.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        Item item = new BlockItem(block, new FabricItemSettings());
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(TextMod.MOD_ID, name), item);
    }

    public static void registerModBlocks() {
        TextMod.LOGGER.info("Registering blocks for " + TextMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(WATERBED));
    }
}