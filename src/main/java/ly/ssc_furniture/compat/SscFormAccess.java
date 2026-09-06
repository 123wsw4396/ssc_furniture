package ly.ssc_furniture.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import org.jetbrains.annotations.Nullable;

/**
 * SSC 玩家形态访问入口. 唯一职责: 从 Player 拿到当前 form 对象和它的 formId.
 * 供 SscFormCompat / FormMatcher / SSCFurniture 等共享.
 *
 * SSC 是本 mod 的硬依赖 (fabric.mod.json depends), 因此这里直接走 SSC 官方提供的
 * {@link FormUtils#getPlayerForm(Player)} 类型安全 API, 不再使用反射.
 */
public final class SscFormAccess {

    private SscFormAccess() {}

    public static @Nullable IForm getCurrentForm(Player player) {
        if (player == null) return null;
        return FormUtils.getPlayerForm(player);
    }

    public static @Nullable ResourceLocation getFormId(@Nullable IForm form) {
        return form == null ? null : form.getFormID();
    }

    public static @Nullable ResourceLocation getCurrentFormId(Player player) {
        return getFormId(getCurrentForm(player));
    }
}
