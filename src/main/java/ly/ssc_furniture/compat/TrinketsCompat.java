package ly.ssc_furniture.compat;

import ly.ssc_furniture.item.ModItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils;

import java.util.Optional;

/**
 * 饰品软依赖入口. Trinkets 未加载时静默返回 EMPTY/false.
 * 优先走 SSC AccessoryUtils (客户端 LocalPlayer 可靠), fallback 到 trinkets API.
 */
public final class TrinketsCompat {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    private TrinketsCompat() {}

    public static boolean isLoaded() { return LOADED; }

    public static boolean isShapingHook(Item item) {
        return item == ModItems.SHAPING_GRAPPLING_HOOK_IRON
                || item == ModItems.SHAPING_GRAPPLING_HOOK_DIAMOND;
    }

    /** 玩家饰品栏里是否装备了塑形发射器. */
    public static boolean hasShapingHook(Player player) {
        return !findShapingHook(player).isEmpty();
    }

    /** 返回饰品栏里装备的塑形发射器; 无则 ItemStack.EMPTY. */
    public static ItemStack findShapingHook(Player player) {
        if (player == null) return ItemStack.EMPTY;

        // SSC 自带 accessories 系统 (trinkets/curios 之一), 与 SpiderTPEHRHideMixin 同路径.
        try {
            ItemStack st = AccessoryUtils.getEntitySlot(player, "auto", "hand", "extra_hand", 0);
            if (st != null && !st.isEmpty() && isShapingHook(st.getItem())) return st;
        } catch (Throwable ignored) {}

        if (!LOADED) return ItemStack.EMPTY;
        try {
            Optional<dev.emi.trinkets.api.TrinketComponent> comp =
                    dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player);
            if (comp.isEmpty()) return ItemStack.EMPTY;
            ItemStack[] found = { ItemStack.EMPTY };
            comp.get().forEach((slot, stack) -> {
                if (found[0].isEmpty() && stack != null && isShapingHook(stack.getItem())) {
                    found[0] = stack;
                }
            });
            return found[0];
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }
}
