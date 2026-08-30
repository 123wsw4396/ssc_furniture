package ly.ssc_furniture.compat;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 把玩家当前形态归类到一个"逻辑 key". 用于 FormOffsetRegistry 的大类查询.
 * 命中返回逻辑 key (例如 "ssc:anubis_wolf_3"); 不命中返回 null.
 * 不同 matcher 之间按注册顺序尝试, 首个非 null 命中生效.
 */
@FunctionalInterface
public interface FormMatcher {
    @Nullable String match(Player player);
}
