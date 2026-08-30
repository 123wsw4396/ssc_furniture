package ly.ssc_furniture.client.render.bathtub;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.skin.RegPlayerSkinComponent;
import net.onixary.shapeShifterCurseFabric.player_form.utils.PlayerFormComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FormCheck {

    private FormCheck() {}

    private static boolean triedInit = false;
    private static Object componentKey;
    private static Method getMethod;
    private static Field nowFormField;

    private static boolean triedInitSkin = false;
    private static Object skinComponentKey;
    private static Method skinGetMethod;
    private static Method isEnableFormColorMethod;
    private static Method shouldKeepOriginalSkinMethod;

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

    private static void initSkin() {
        if (triedInitSkin) return;
        triedInitSkin = true;
        try {
            Field f = RegPlayerSkinComponent.class.getDeclaredField("SKIN_SETTINGS");
            f.setAccessible(true);
            skinComponentKey = f.get(null);
            skinGetMethod = skinComponentKey.getClass().getMethod("get", Object.class);
            Class<?> skinCompCls = Class.forName(
                    "net.onixary.shapeShifterCurseFabric.player_form.skin.PlayerSkinComponent");
            isEnableFormColorMethod = skinCompCls.getMethod("isEnableFormColor");
            shouldKeepOriginalSkinMethod = skinCompCls.getMethod("shouldKeepOriginalSkin");
        } catch (Throwable t) {
            skinComponentKey = null;
        }
    }

    public static boolean isAxolotlSleepPoseEnabled(Player player) {
        init();
        if (componentKey == null) return false;
        try {
            Object comp = getMethod.invoke(componentKey, player);
            if (comp == null) return false;
            Object form = nowFormField.get(comp);
            if (!(form instanceof IForm iform)) return false;
            if (iform.getFormGroup() != RegPlayerForms.AXOLOTL_FORM) return false;
            return iform.getFormTier() >= 4;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isAxolotl2SleepPoseEnabled(Player player) {
        init();
        if (componentKey == null) return false;
        try {
            Object comp = getMethod.invoke(componentKey, player);
            if (comp == null) return false;
            Object form = nowFormField.get(comp);
            if (!(form instanceof IForm iform)) return false;
            if (iform.getFormGroup() != RegPlayerForms.AXOLOTL_FORM) return false;
            return iform.getFormTier() == 3;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isAnyAxolotlSleepPoseEnabled(Player player) {
        return isAxolotlSleepPoseEnabled(player) || isAxolotl2SleepPoseEnabled(player);
    }

    public static boolean shouldKeepOriginalSkin(Player player) {
        initSkin();
        if (skinComponentKey == null || shouldKeepOriginalSkinMethod == null) return false;
        try {
            Object comp = skinGetMethod.invoke(skinComponentKey, player);
            if (comp == null) return false;
            Object result = shouldKeepOriginalSkinMethod.invoke(comp);
            return result instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isFormColorEnabled(Player player) {
        initSkin();
        if (skinComponentKey == null || isEnableFormColorMethod == null) return false;
        try {
            Object comp = skinGetMethod.invoke(skinComponentKey, player);
            if (comp == null) return false;
            Object result = isEnableFormColorMethod.invoke(comp);
            return result instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }
}
