package ly.ssc_furniture.block;

import ly.ssc_furniture.SSCFurniture;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block WATERBED = registerBlock("waterbed",
            new WaterbedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).noOcclusion()));

    public static final Block BATHTUB_BED = registerBlock("bathtub_bed",
            new BathtubBedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).noOcclusion()));

    public static final Block FOUR_LEGGED_CUSHION = registerBlockWithTooltip("four_legged_cushion",
            new FourLeggedCushionBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion()),
            "block.ssc_furniture.four_legged_cushion.tooltip");

    public static final Block DIGESTION_WEB_BOX = registerBlockWithTooltip("digestion_web_box",
            new DigestionWebBoxBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion()),
            "block.ssc_furniture.digestion_web_box.tooltip");

    public static final Block BAT_CLIMBING_SCAFFOLD = registerBlockWithTooltip("bat_climbing_scaffold",
            new BatClimbingScaffoldBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion().strength(0.4f)),
            "block.ssc_furniture.bat_climbing_scaffold.tooltip");

    public static final BlockEntityType<WaterbedBlockEntity> WATERBED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "waterbed"),
                    FabricBlockEntityTypeBuilder.create(WaterbedBlockEntity::new, WATERBED).build());

    public static final BlockEntityType<BathtubBedBlockEntity> BATHTUB_BED_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "bathtub_bed"),
                    FabricBlockEntityTypeBuilder.create(BathtubBedBlockEntity::new, BATHTUB_BED).build());

    public static final BlockEntityType<DigestionWebBoxBlockEntity> DIGESTION_WEB_BOX_BE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "digestion_web_box"),
                    FabricBlockEntityTypeBuilder.create(DigestionWebBoxBlockEntity::new, DIGESTION_WEB_BOX).build());

    public static final BlockEntityType<BatClimbingScaffoldBlockEntity> BAT_CLIMBING_SCAFFOLD_BE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(SSCFurniture.MOD_ID, "bat_climbing_scaffold"),
                    FabricBlockEntityTypeBuilder.create(BatClimbingScaffoldBlockEntity::new, BAT_CLIMBING_SCAFFOLD).build());

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

    private static Block registerBlockWithTooltip(String name, Block block, String tooltipKey) {
        Item item = new BlockItem(block, new FabricItemSettings()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(tooltipKey));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        };
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(SSCFurniture.MOD_ID, name), item);
        return Registry.register(BuiltInRegistries.BLOCK,
                new ResourceLocation(SSCFurniture.MOD_ID, name), block);
    }

    public static void registerModBlocks() {
        SSCFurniture.LOGGER.info("Registering blocks for " + SSCFurniture.MOD_ID);
    }
}
