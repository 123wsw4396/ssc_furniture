package ly.ssc_furniture.compat.matchers;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.compat.FormMatcher;
import ly.ssc_furniture.compat.SscFormAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 把"胡狼四足"归为一个大类:
 *   - 玩家当前 form 是 Form_AnubisWolf3 的实例 (含附属继承) → 命中
 *   - 或者 formId == shape-shifter-curse:anubis_wolf_3 → 命中
 * 逻辑 key: "ssc:anubis_wolf_3".
 */
public final class AnubisWolf3Matcher implements FormMatcher {

    public static final String KEY = "ssc:anubis_wolf_3";
    private static final ResourceLocation OFFICIAL_ID =
            new ResourceLocation("shape-shifter-curse", "anubis_wolf_3");
    private static final String FQCN = "net.onixary.shapeShifterCurseFabric.player_form.forms.Form_AnubisWolf3";

    private static Class<?> cachedClass;
    private static boolean triedLoad = false;

    private static Class<?> loadClass() {
        if (triedLoad) return cachedClass;
        triedLoad = true;
        try {
            cachedClass = Class.forName(FQCN);
        } catch (ClassNotFoundException e) {
            SSCFurniture.LOGGER.warn("[FormOffset] SSC class not found: {}", FQCN);
            cachedClass = null;
        }
        return cachedClass;
    }

    @Override
    public @Nullable String match(Player player) {
        Object form = SscFormAccess.getCurrentFormObject(player);
        if (form == null) return null;

        Class<?> cls = loadClass();
        if (cls != null && cls.isInstance(form)) return KEY;

        ResourceLocation id = SscFormAccess.getFormId(form);
        if (OFFICIAL_ID.equals(id)) return KEY;

        return null;
    }
}
