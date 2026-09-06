package ly.ssc_furniture.item;

import ly.ssc_furniture.SSCFurniture;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

public class ModItems {

    public static final Item AMETHYST_SWORD = registerItem("amethyst_sword",
            new AmethystSwordItem(Tiers.DIAMOND, 3, -2.4F, new FabricItemSettings()));
    public static final Item
            AMETHYST_AXE =registerItem("amethyst_axe",new AmethyAxeItem(Tiers.DIAMOND,6.0f,-3.0f,new FabricItemSettings()));

    public static final Item TAB_ICON = registerItem("tab_icon",
            new Item(new FabricItemSettings()));

    public static final Item IRON_GRAPPLING_HOOK = registerItem("iron_grappling_hook",
            new GrapplingHookItem(new FabricItemSettings().durability(100)));

    public static final Item DIAMOND_GRAPPLING_HOOK = registerItem("diamond_grappling_hook",
            new GrapplingHookItem(new FabricItemSettings().durability(1561)));

    public static final Item SHAPING_GRAPPLING_HOOK_IRON = registerItem("shaping_grappling_hook_iron",
            new ShapingGrapplingHookIronItem(new FabricItemSettings().stacksTo(1)));

    public static final Item SHAPING_GRAPPLING_HOOK_DIAMOND = registerItem("shaping_grappling_hook_diamond",
            new ShapingGrapplingHookDiamondItem(new FabricItemSettings().stacksTo(1)));

    public static final Item GUST_CLOTH_FOR_FOOT_BINDING = registerItem("gust_cloth_for_foot_binding",
            new GustClothItem(GustClothArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS,
                    new FabricItemSettings().stacksTo(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(SSCFurniture.MOD_ID, name), item);

    }

    public static void registerModItems() {
        SSCFurniture.LOGGER.info("Registering items for " + SSCFurniture.MOD_ID);
    }
}
