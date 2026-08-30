package ly.ssc_furniture.client.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ly.ssc_furniture.client.config.SSCFurnitureConfigScreen;

public class SSCFurnitureModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SSCFurnitureConfigScreen::new;
    }
}
