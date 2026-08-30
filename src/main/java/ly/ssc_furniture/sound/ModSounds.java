package ly.ssc_furniture.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent GRAPPLING_HOOK_SHOOT =
            SoundEvent.createVariableRangeEvent(new ResourceLocation("ssc_furniture", "item.grappling_hook.shoot"));

    public static final SoundEvent DIGESTION_WEB_BOX_OPEN_CLOSE =
            SoundEvent.createVariableRangeEvent(new ResourceLocation("ssc_furniture", "block.digestion_web_box.open_close"));

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                new ResourceLocation("ssc_furniture", "item.grappling_hook.shoot"),
                GRAPPLING_HOOK_SHOOT);
        Registry.register(BuiltInRegistries.SOUND_EVENT,
                new ResourceLocation("ssc_furniture", "block.digestion_web_box.open_close"),
                DIGESTION_WEB_BOX_OPEN_CLOSE);
    }
}
