package ly.ssc_furniture.block;

import ly.ssc_furniture.SSCFurniture;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block WATERBED = registerBlock("waterbed",
            new WaterbedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).noOcclusion()));

    public static final Block BATHTUB_BED = registerBlock("bathtub_bed",
            new BathtubBedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).noOcclusion()));

    public static final BlockEntityType<WaterbedBlockEntity> WATERBED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "waterbed"),
                    FabricBlockEntityTypeBuilder.create(WaterbedBlockEntity::new, WATERBED).build());

    public static final BlockEntityType<BathtubBedBlockEntity> BATHTUB_BED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "bathtub_bed"),
                    FabricBlockEntityTypeBuilder.create(BathtubBedBlockEntity::new, BATHTUB_BED).build());

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(SSCFurniture.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        Item item = new BlockItem(block, new FabricItemSettings());
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(SSCFurniture.MOD_ID, name), item);
    }

    public static void registerModBlocks() {
        SSCFurniture.LOGGER.info("Registering blocks for " + SSCFurniture.MOD_ID);
    }
}
