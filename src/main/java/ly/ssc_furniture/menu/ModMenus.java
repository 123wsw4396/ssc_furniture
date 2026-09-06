package ly.ssc_furniture.menu;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {

    public static MenuType<DigestionWebBoxMenu> DIGESTION_WEB_BOX;

    public static void register() {
        DIGESTION_WEB_BOX = ScreenHandlerRegistry.registerSimple(
                new ResourceLocation("ssc_furniture", "digestion_web_box"),
                DigestionWebBoxMenu::new
        );
    }
}
