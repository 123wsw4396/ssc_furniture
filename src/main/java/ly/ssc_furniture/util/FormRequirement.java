package ly.ssc_furniture.util;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FormRequirement {

    private FormRequirement() {}

    private static boolean triedInit = false;
    private static Object componentKey;
    private static Method getMethod;
    private static Field nowFormField;

    private static void init() {
        if (triedInit) return;
        triedInit = true;
        try {
            Field f = PlayerFormComponent.class.getDeclaredField("COMPONENT");
            f.setAccessible(true);
            componentKey = f.get(null);
            getMethod = componentKey.getClass().getMethod("get", Object.class);
            nowFormField = PlayerFormComponent.class.getDeclaredField("nowForm");
            nowFormField.setAccessible(true);
        } catch (Throwable t) {
            componentKey = null;
        }
    }

    public static boolean isAxolotlTier1OrAbove(Player player) {
        init();
        if (componentKey == null) return false;
        try {
            Object comp = getMethod.invoke(componentKey, player);
            if (comp == null) return false;
            Object form = nowFormField.get(comp);
            if (!(form instanceof IForm iform)) return false;
            if (iform.getFormGroup() != RegPlayerForms.AXOLOTL_FORM) return false;
            return iform.getFormTier() > 1;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isBatTier3OrAbove(Player player) {
        init();
        if (componentKey == null) return false;
        try {
            Object comp = getMethod.invoke(componentKey, player);
            if (comp == null) return false;
            Object form = nowFormField.get(comp);
            if (!(form instanceof IForm iform)) return false;
            if (iform.getFormGroup() != RegPlayerForms.BAT_FORM) return false;
            return iform.getFormTier() >= 4;
        } catch (Throwable t) {
            return false;
        }
    }
}
