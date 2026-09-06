package ly.ssc_furniture.client.render.bathtub;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.PlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

public final class FormCheck {

    private FormCheck() {}

    private static IForm currentForm(Player player) {
        if (player == null) return null;
        return FormUtils.getPlayerForm(player);
    }

    private static boolean isAxolotl(IForm form) {
        return form != null && form.getFormGroup() == RegPlayerForms.AXOLOTL_FORM;
    }

    public static boolean isAxolotlSleepPoseEnabled(Player player) {
        IForm form = currentForm(player);
        return isAxolotl(form) && form.getFormTier() >= 4;
    }

    public static boolean isAxolotl2SleepPoseEnabled(Player player) {
        IForm form = currentForm(player);
        return isAxolotl(form) && form.getFormTier() == 3;
    }

    public static boolean isAnyAxolotlSleepPoseEnabled(Player player) {
        return isAxolotlSleepPoseEnabled(player) || isAxolotl2SleepPoseEnabled(player);
    }

    public static boolean shouldKeepOriginalSkin(Player player) {
        if (player == null) return false;
        PlayerSkinComponent c = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        return c != null && c.shouldKeepOriginalSkin();
    }

    public static boolean isFormColorEnabled(Player player) {
        if (player == null) return false;
        PlayerSkinComponent c = RegPlayerSkinComponent.SKIN_SETTINGS.get(player);
        return c != null && c.isEnableFormColor();
    }
}
