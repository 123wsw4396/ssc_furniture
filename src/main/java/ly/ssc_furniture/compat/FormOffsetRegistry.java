package ly.ssc_furniture.compat;

import ly.ssc_furniture.compat.matchers.AnubisWolf3Matcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-form 装备渲染微调注册表 (translate + scale, 单位 model-pixel).
 *
 * 三层分区 (优先级由高到低): api > data > builtin.
 * 每层内部两级 key: equipmentId → String key. String key 可以是:
 *   - formId 字符串, 例如 "hex_mod:hex_wolf_3" (精确)
 *   - matcher 逻辑 key, 例如 "ssc:anubis_wolf_3" (大类)
 *
 * 单次查询顺序:
 *   for src in [api, data, builtin]:
 *       for keyType in [formIdKey (精确), matcherKey (大类)]:
 *           if src[equipmentId][key] exists: return
 *   return IDENTITY
 * 精确覆盖大类, 附属可覆盖官方.
 *
 * 装备标识 (equipmentId) 约定为完整 mod:path, 例如 "ssc_furniture:gust_cloth".
 */
public final class FormOffsetRegistry {

    public static final ResourceLocation EQ_GUST_CLOTH =
            new ResourceLocation("ssc_furniture", "gust_cloth");

    private FormOffsetRegistry() {}

    private static final Map<ResourceLocation, Map<String, FormAdjustment>> apiRegistry = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, FormAdjustment>> dataRegistry = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, FormAdjustment>> builtinRegistry = new HashMap<>();

    private static final List<FormMatcher> matchers = new ArrayList<>();

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        matchers.add(new AnubisWolf3Matcher());

        // 内置默认: 胡狼四足 (含 instanceof Form_AnubisWolf3 的附属) 缠布向身后偏 2/16 方块 + 1.10x 缩放.
        // 当前 poseStack 空间已回到 block 单位, 所以 2/16 方块 = 0.125f.
        // 若实机发现方向偏错, 把 dz 改成负值.
        registerBuiltin(EQ_GUST_CLOTH, AnubisWolf3Matcher.KEY,
                new FormAdjustment(0f, 0f, 2f / 16f, 1.10f));

        // 胡狼双足 tier 2 (anubis_wolf_2): 无平移, 1.10x 缩放.
        // 因为 anubis_wolf_2 是裸 NormalForm 无专属类, 只能按 formId 精确匹配;
        // 基于它扩展的附属形态需自行通过 JSON / registerApi 登记同款缩放.
        registerBuiltin(EQ_GUST_CLOTH, "shape-shifter-curse:anubis_wolf_2",
                new FormAdjustment(0f, 0f, 0f, 1.10f));
    }

    // ---------------- 注册 API ----------------

    /** 供其它 mod 使用: 覆盖 api 层 (最高优先级). */
    public static void registerApi(ResourceLocation equipmentId, String key, FormAdjustment adj) {
        if (equipmentId == null || key == null || adj == null) return;
        apiRegistry.computeIfAbsent(equipmentId, k -> new LinkedHashMap<>()).put(key, adj);
    }

    /** 内部使用: 写入 data 层. LegTypeDataLoader-style loader 调用. */
    static void putData(ResourceLocation equipmentId, String key, FormAdjustment adj) {
        if (equipmentId == null || key == null || adj == null) return;
        dataRegistry.computeIfAbsent(equipmentId, k -> new LinkedHashMap<>()).put(key, adj);
    }

    /** 内部使用: 清空 data 层, 用于 reload 前. */
    static void clearData() {
        dataRegistry.clear();
    }

    /** 内部使用: 写入 builtin 层. init() 时调用. */
    private static void registerBuiltin(ResourceLocation equipmentId, String key, FormAdjustment adj) {
        builtinRegistry.computeIfAbsent(equipmentId, k -> new LinkedHashMap<>()).put(key, adj);
    }

    public static void registerMatcher(FormMatcher matcher) {
        if (matcher != null) matchers.add(matcher);
    }

    // ---------------- 查询 ----------------

    /** 主入口: 返回该玩家对该装备应有的微调. 未命中返回 IDENTITY. */
    public static FormAdjustment resolveAdjustment(ResourceLocation equipmentId, Player player) {
        if (equipmentId == null || player == null) return FormAdjustment.IDENTITY;

        ResourceLocation formId = SscFormAccess.getCurrentFormId(player);
        String formIdKey = formId != null ? formId.toString() : null;
        String matcherKey = matchOne(player);

        FormAdjustment adj = lookup(apiRegistry, equipmentId, formIdKey, matcherKey);
        if (adj != null) return adj;
        adj = lookup(dataRegistry, equipmentId, formIdKey, matcherKey);
        if (adj != null) return adj;
        adj = lookup(builtinRegistry, equipmentId, formIdKey, matcherKey);
        if (adj != null) return adj;

        return FormAdjustment.IDENTITY;
    }

    private static FormAdjustment lookup(Map<ResourceLocation, Map<String, FormAdjustment>> src,
                                         ResourceLocation equipmentId, String formIdKey, String matcherKey) {
        Map<String, FormAdjustment> table = src.get(equipmentId);
        if (table == null) return null;
        if (formIdKey != null) {
            FormAdjustment v = table.get(formIdKey);
            if (v != null) return v;
        }
        if (matcherKey != null) {
            FormAdjustment v = table.get(matcherKey);
            if (v != null) return v;
        }
        return null;
    }

    private static String matchOne(Player player) {
        for (FormMatcher m : matchers) {
            String k = m.match(player);
            if (k != null) return k;
        }
        return null;
    }

    // ---------------- Debug ----------------

    public static Map<String, Map<ResourceLocation, Map<String, FormAdjustment>>> dumpAll() {
        Map<String, Map<ResourceLocation, Map<String, FormAdjustment>>> out = new LinkedHashMap<>();
        out.put("api",     Collections.unmodifiableMap(new HashMap<>(apiRegistry)));
        out.put("data",    Collections.unmodifiableMap(new HashMap<>(dataRegistry)));
        out.put("builtin", Collections.unmodifiableMap(new HashMap<>(builtinRegistry)));
        return out;
    }
}
