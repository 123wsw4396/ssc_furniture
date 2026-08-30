package ly.ssc_furniture.compat;

import ly.ssc_furniture.SSCFurniture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SSC 形态-腿型 兼容层.
 *
 * 用途: 让"轻快缠布"这类跨形态兽腿装备既支持 SSC 官方形态, 也支持第三方附属基于 SSC Form 类扩展或
 * 动态数据包注册的形态. 判定优先级:
 *   1) Java API 显式注册 (registerLegType)
 *   2) 数据包 JSON 显式注册 (LegTypeDataLoader 写入 dataRegistry)
 *   3) 反射 instanceof 探测已知 SSC Form 类 (tier-2 直立趾行 / tier-3 四足)
 *   4) formID 正则兜底 (匹配 SSC 官方裸 NormalForm 的 anubis_wolf_2 等)
 *
 * 语义: DIGITIGRADE / QUADRUPED 都算兽腿, isBeastLeg 返回 true.
 * HUMANOID / OTHER / null 都不算.
 *
 * 附属集成方式:
 *   代码: SscFormCompat.registerLegType(new ResourceLocation("hex_mod","hex_fox_2"), LegType.DIGITIGRADE);
 *   数据包: data/&lt;ns&gt;/ssc_furniture/compat/leg_types/xxx.json
 *          { "hex_mod:hex_fox_2": "digitigrade", "hex_mod:hex_fox_3": "quadruped" }
 */
public final class SscFormCompat {

    public enum LegType {
        HUMANOID,
        DIGITIGRADE,
        QUADRUPED,
        OTHER;

        public static LegType parse(String s) {
            if (s == null) return OTHER;
            switch (s.toLowerCase()) {
                case "humanoid":    return HUMANOID;
                case "digitigrade": return DIGITIGRADE;
                case "quadruped":   return QUADRUPED;
                default:            return OTHER;
            }
        }
    }

    private SscFormCompat() {}

    private static final Map<ResourceLocation, LegType> apiRegistry = new HashMap<>();
    private static final Map<ResourceLocation, LegType> dataRegistry = new HashMap<>();
    private static final Map<ResourceLocation, LegType> builtinRegistry = new LinkedHashMap<>();

    private static final Map<Class<?>, LegType> classProbeTable = new LinkedHashMap<>();

    private static final Pattern OFFICIAL_ID_PATTERN =
        Pattern.compile("^(snow_fox|anubis_wolf|familiar_fox|ocelot|bat)_(\\d+).*$");

    private static boolean initialized = false;

    /** 幂等. 在 mod 主 initializer 里调一次即可. */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        loadClassProbeTable();
        loadBuiltinRegistry();
    }

    private static void loadClassProbeTable() {
        // 沿用 SSCFurniture 中已使用的 5 家: SNOW_FOX / ANUBIS_WOLF / FAMILIAR_FOX / OCELOT / BAT.
        // ANUBIS_WOLF_2 在 SSC 里是裸 NormalForm 没专属类 — 靠正则兜底, 附属基于它扩展需手动登记.
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_SnowFox2",     LegType.DIGITIGRADE);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_FamiliarFox2", LegType.DIGITIGRADE);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Ocelot2",      LegType.DIGITIGRADE);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Bat2",         LegType.DIGITIGRADE);

        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_SnowFox3",     LegType.QUADRUPED);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_FamiliarFox3", LegType.QUADRUPED);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Ocelot3",      LegType.QUADRUPED);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Bat3",         LegType.QUADRUPED);
        putProbe("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_AnubisWolf3",  LegType.QUADRUPED);
    }

    private static void putProbe(String fqcn, LegType type) {
        try {
            Class<?> cls = Class.forName(fqcn);
            classProbeTable.put(cls, type);
        } catch (ClassNotFoundException e) {
            SSCFurniture.LOGGER.warn("[SscFormCompat] SSC form class not found, skip probe: {}", fqcn);
        }
    }

    private static void loadBuiltinRegistry() {
        // 覆盖 SSC 官方 5 家的 tier 2/3 全部 formID (含裸 NormalForm 的 anubis_wolf_2), 作为正则之外的显式兜底.
        String[] families = { "snow_fox", "anubis_wolf", "familiar_fox", "ocelot", "bat" };
        for (String fam : families) {
            builtinRegistry.put(new ResourceLocation("shape-shifter-curse", fam + "_2"), LegType.DIGITIGRADE);
            builtinRegistry.put(new ResourceLocation("shape-shifter-curse", fam + "_3"), LegType.QUADRUPED);
        }
    }

    /** 供其它 mod 在自己的 onInitialize 里注册. 覆盖同名旧值. */
    public static void registerLegType(ResourceLocation formId, LegType type) {
        if (formId == null || type == null) return;
        apiRegistry.put(formId, type);
    }

    /** 供 LegTypeDataLoader 使用: 每次 reload 前清空数据包分区. */
    static void clearDataRegistry() {
        dataRegistry.clear();
    }

    /** 供 LegTypeDataLoader 使用: 追加数据包条目. */
    static void putDataRegistry(ResourceLocation formId, LegType type) {
        if (formId == null || type == null) return;
        dataRegistry.put(formId, type);
    }

    /** 主入口: 优先级链判定. 无法识别返回 null. */
    public static LegType getLegType(Player player) {
        if (player == null) return null;
        Object form = getCurrentFormObject(player);
        if (form == null) return null;
        ResourceLocation id = getFormIdFromObject(form);

        // 1) Java API
        if (id != null) {
            LegType t = apiRegistry.get(id);
            if (t != null) return t;
            // 2) 数据包 JSON
            t = dataRegistry.get(id);
            if (t != null) return t;
        }

        // 3) 反射 instanceof (自动覆盖附属基于 Form_xxx 类扩展的形态, 如 hex_fox_2 = new Form_FamiliarFox2(...))
        for (Map.Entry<Class<?>, LegType> e : classProbeTable.entrySet()) {
            if (e.getKey().isInstance(form)) return e.getValue();
        }

        // 4) 内置官方 formID 兜底 (主要为 anubis_wolf_2 这种裸 NormalForm)
        if (id != null) {
            LegType t = builtinRegistry.get(id);
            if (t != null) return t;
            // 5) formID 正则兜底 (仅 SSC 官方 namespace)
            if ("shape-shifter-curse".equals(id.getNamespace())) {
                Matcher m = OFFICIAL_ID_PATTERN.matcher(id.getPath());
                if (m.matches()) {
                    int tier;
                    try { tier = Integer.parseInt(m.group(2)); } catch (NumberFormatException nfe) { tier = -1; }
                    if (tier == 2) return LegType.DIGITIGRADE;
                    if (tier == 3) return LegType.QUADRUPED;
                }
            }
        }
        return null;
    }

    public static boolean isBeastLeg(Player player) {
        LegType t = getLegType(player);
        return t == LegType.DIGITIGRADE || t == LegType.QUADRUPED;
    }

    // ---------------------------------------------------------------
    // SSC 反射工具 (与 SSCFurniture.getCurrentFormObject 同路径, 独立一份避免循环依赖)
    // ---------------------------------------------------------------

    private static Object getCurrentFormObject(Player player) {
        try {
            Class<?> regCompClass = Class.forName(
                "net.onixary.shapeShifterCurseFabric.player_form.utils.RegPlayerFormComponent");
            Field playerFormField = regCompClass.getField("PLAYER_FORM");
            Object componentKey = playerFormField.get(null);
            Object component = null;
            for (Method m : componentKey.getClass().getMethods()) {
                if (m.getName().equals("get") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].isInstance(player)) {
                    component = m.invoke(componentKey, player);
                    break;
                }
            }
            if (component == null) return null;
            Field nowFormField = component.getClass().getField("nowForm");
            return nowFormField.get(component);
        } catch (Throwable e) {
            return null;
        }
    }

    private static ResourceLocation getFormIdFromObject(Object form) {
        try {
            Method getFormID = form.getClass().getMethod("getFormID");
            Object v = getFormID.invoke(form);
            return v instanceof ResourceLocation rl ? rl : null;
        } catch (Throwable e) {
            return null;
        }
    }

    /** Debug 用: 快照当前所有注册来源, 便于 /command 或调试. */
    public static Map<String, Map<ResourceLocation, LegType>> dumpAll() {
        Map<String, Map<ResourceLocation, LegType>> out = new LinkedHashMap<>();
        out.put("api",     Collections.unmodifiableMap(new HashMap<>(apiRegistry)));
        out.put("data",    Collections.unmodifiableMap(new HashMap<>(dataRegistry)));
        out.put("builtin", Collections.unmodifiableMap(new HashMap<>(builtinRegistry)));
        return out;
    }
}
