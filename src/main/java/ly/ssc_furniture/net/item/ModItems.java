package ly.textmod.net.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import ly.textmod.net.TextMod;
public class ModItems {

    public static final Item AMETHYST_SWORD = registerItem("amethyst_sword",
            new AmethystSwordItem(Tiers.DIAMOND, 3, -2.4F, new FabricItemSettings()));
    public static final Item
            AMETHYST_AXE =registerItem("amethyst_axe",new AmethyAxeItem(Tiers.DIAMOND,6.0f,-3.0f,new FabricItemSettings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(TextMod.MOD_ID, name), item);

    }

    public static void registerModItems() {
        TextMod.LOGGER.info("Registering items for " + TextMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> {
                    entries.accept(AMETHYST_SWORD);
                    entries.accept(AMETHYST_AXE);
                });

    }
}
