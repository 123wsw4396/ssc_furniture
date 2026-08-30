package ly.ssc_furniture.compat;

import ly.ssc_furniture.item.ModItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Trinkets 软依赖入口. Trinkets 未加载时静默返回 EMPTY/false.
 * 优先走 SSC AccessoryUtils (客户端 LocalPlayer 可靠), fallback 到 trinkets API.
 */
public final class TrinketsCompat {

    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    private static final boolean SSC_LOADED = FabricLoader.getInstance().isModLoaded("shape-shifter-curse");

    private TrinketsCompat() {}

    public static boolean isLoaded() { return LOADED; }

    public static boolean isShapingHook(Item item) {
        return item == ModItems.SHAPING_GRAPPLING_HOOK_IRON
                || item == ModItems.SHAPING_GRAPPLING_HOOK_DIAMOND;
    }

    /** 玩家饰品栏里是否装备了塑形发射器. */
    public static boolean hasShapingHook(Player player) {
        if (!LOADED) return false;
        return !findShapingHook(player).isEmpty();
    }

    /** 返回饰品栏里装备的塑形发射器; 无则 ItemStack.EMPTY. */
    public static ItemStack findShapingHook(Player player) {
        if (!LOADED) return ItemStack.EMPTY;
        if (SSC_LOADED) {
            ItemStack st = SscAccessoryImpl.findShapingHook(player);
            if (st != null && !st.isEmpty()) return st;
        }
        return TrinketsApiImpl.findShapingHook(player);
    }

    /** SSC AccessoryUtils 路径, 已验证客户端 LocalPlayer 可用 (与 SpiderTPEHRHideMixin 同路径). */
    private static final class SscAccessoryImpl {
        static ItemStack findShapingHook(Player player) {
            try {
                ItemStack st = net.onixary.shapeShifterCurseFabric.util.Accessory.AccessoryUtils
                        .getEntitySlot(player, "auto", "hand", "extra_hand", 0);
                if (st != null && !st.isEmpty() && isShapingHook(st.getItem())) return st;
            } catch (Throwable ignored) {}
            return ItemStack.EMPTY;
        }
    }

    /** Trinkets API 路径, fallback (客户端可能拿不到, 服务端可靠). */
    private static final class TrinketsApiImpl {
        static ItemStack findShapingHook(Player player) {
            try {
                java.util.Optional<dev.emi.trinkets.api.TrinketComponent> comp =
                        dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player);
                if (comp.isEmpty()) return ItemStack.EMPTY;
                for (Object pair : comp.get().getAllEquipped()) {
                    ItemStack st = extractStack(pair);
                    if (st != null && !st.isEmpty() && isShapingHook(st.getItem())) return st;
                }
            } catch (Throwable ignored) {}
            return ItemStack.EMPTY;
        }

        private static ItemStack extractStack(Object pair) {
            if (pair == null) return null;
            try {
                java.lang.reflect.Method m;
                try {
                    m = pair.getClass().getMethod("getSecond");
                } catch (NoSuchMethodException e) {
                    m = pair.getClass().getMethod("getB");
                }
                Object second = m.invoke(pair);
                return second instanceof ItemStack st ? st : null;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}
