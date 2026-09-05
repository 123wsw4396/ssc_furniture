package ly.ssc_furniture.util;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;

public final class FormRequirement {

    private FormRequirement() {}

    private static IForm currentForm(Player player) {
        if (player == null) return null;
        return FormUtils.getPlayerForm(player);
    }

    public static boolean isAxolotlTier1OrAbove(Player player) {
        IForm form = currentForm(player);
        if (form == null) return false;
        return form.getFormGroup() == RegPlayerForms.AXOLOTL_FORM && form.getFormTier() > 1;
    }

    public static boolean isBatTier3OrAbove(Player player) {
        IForm form = currentForm(player);
        if (form == null) return false;
        return form.getFormGroup() == RegPlayerForms.BAT_FORM && form.getFormTier() >= 4;
    }
}
