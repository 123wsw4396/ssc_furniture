package ly.ssc_furniture.compat.matchers;

import ly.ssc_furniture.compat.FormMatcher;
import ly.ssc_furniture.compat.SscFormAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_AnubisWolf3;
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

    @Override
    public @Nullable String match(Player player) {
        IForm form = SscFormAccess.getCurrentForm(player);
        if (form == null) return null;

        if (form instanceof Form_AnubisWolf3) return KEY;

        if (OFFICIAL_ID.equals(SscFormAccess.getFormId(form))) return KEY;

        return null;
    }
}
