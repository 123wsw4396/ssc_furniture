package ly.ssc_furniture.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * SSC 玩家形态反射访问入口. 唯一职责: 从 Player 拿到当前 form 对象和它的 formId.
 * 供 SscFormCompat / FormMatcher 等共享.
 * SSC 未加载时全部方法返回 null (静默 fallback).
 */
public final class SscFormAccess {

    private SscFormAccess() {}

    private static boolean initTried = false;
    private static Object componentKey;
    private static Method getComponentMethod;

    private static void lazyInit() {
        if (initTried) return;
        initTried = true;
        try {
            Class<?> regCompClass = Class.forName(
                "net.onixary.shapeShifterCurseFabric.player_form.utils.RegPlayerFormComponent");
            Field playerFormField = regCompClass.getField("PLAYER_FORM");
            componentKey = playerFormField.get(null);
            for (Method m : componentKey.getClass().getMethods()) {
                if (m.getName().equals("get") && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(Player.class)) {
                    getComponentMethod = m;
                    break;
                }
            }
            if (getComponentMethod == null) {
                for (Method m : componentKey.getClass().getMethods()) {
                    if (m.getName().equals("get") && m.getParameterCount() == 1) {
                        getComponentMethod = m;
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
            componentKey = null;
            getComponentMethod = null;
        }
    }

    public static @Nullable Object getCurrentFormObject(Player player) {
        if (player == null) return null;
        lazyInit();
        if (componentKey == null || getComponentMethod == null) return null;
        try {
            Object component = getComponentMethod.invoke(componentKey, player);
            if (component == null) return null;
            Field nowFormField = component.getClass().getField("nowForm");
            return nowFormField.get(component);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static @Nullable ResourceLocation getFormId(Object form) {
        if (form == null) return null;
        try {
            Method getFormID = form.getClass().getMethod("getFormID");
            Object v = getFormID.invoke(form);
            return v instanceof ResourceLocation rl ? rl : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static @Nullable ResourceLocation getCurrentFormId(Player player) {
        return getFormId(getCurrentFormObject(player));
    }
}
