package ly.ssc_furniture.server;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 服务端: 玩家 -> stuck 状态的 hook entity id 列表 (按插入顺序). */
public final class HookRegistry {

    private static final int MAX_STUCK_HOOKS_B_MODE_PERMANENT = 2;
    private static final int MAX_STUCK_HOOKS_B_MODE_LOW_TIER = 1;
    private static final int MAX_STUCK_HOOKS_A_MODE = 1;
    private static final Map<UUID, LinkedList<Integer>> HOOKS = new ConcurrentHashMap<>();

    private HookRegistry() {}

    /** 命中瞬间调用: A 模式最多 1 根; B 模式: 蜘蛛永久形态(tier>=3)最多 2 根, 否则最多 1 根; 超出时移除最老. */
    public static int addStuck(Player owner, GrapplingHookEntity hook) {
        if (owner == null) return -1;
        LinkedList<Integer> list = HOOKS.computeIfAbsent(owner.getUUID(), k -> new LinkedList<>());
        int cap;
        if (hook.isModeB()) {
            SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(owner);
            int tier = (info != null) ? info.tier : 0;
            cap = (tier >= 3) ? MAX_STUCK_HOOKS_B_MODE_PERMANENT : MAX_STUCK_HOOKS_B_MODE_LOW_TIER;
        } else {
            cap = MAX_STUCK_HOOKS_A_MODE;
        }
        int removed = -1;
        List<Integer> toDiscard = new ArrayList<>();
        while (list.size() >= cap) {
            Integer oldest = list.pollFirst();
            if (oldest != null) {
                removed = oldest;
                toDiscard.add(oldest);
            }
        }
        list.addLast(hook.getId());
        for (Integer oldest : toDiscard) {
            Entity e = hook.level().getEntity(oldest);
            if (e instanceof GrapplingHookEntity oldHook && !oldHook.isRemoved()) {
                oldHook.discard();
            }
        }
        return removed;
    }

    public static void remove(Player owner, int entityId) {
        if (owner == null) return;
        LinkedList<Integer> list = HOOKS.get(owner.getUUID());
        if (list == null) return;
        list.removeFirstOccurrence(entityId);
    }

    public static List<GrapplingHookEntity> getStuckHooks(ServerPlayer player) {
        LinkedList<Integer> list = HOOKS.get(player.getUUID());
        if (list == null || list.isEmpty()) return List.of();
        ServerLevel level = player.serverLevel();
        List<GrapplingHookEntity> out = new ArrayList<>(list.size());
        list.removeIf(id -> {
            Entity e = level.getEntity(id);
            if (e instanceof GrapplingHookEntity hook && !hook.isRemoved()) {
                out.add(hook);
                return false;
            }
            return true;
        });
        return out;
    }

    public static void clearPlayer(Player owner) {
        if (owner == null) return;
        HOOKS.remove(owner.getUUID());
    }

    /** 服务端: 玩家当前是否已有任意 B 模式 stuck 钩存在. */
    public static boolean hasAnyStuckModeB(Player owner) {
        if (owner == null) return false;
        LinkedList<Integer> list = HOOKS.get(owner.getUUID());
        if (list == null || list.isEmpty()) return false;
        for (Integer id : list) {
            Entity e = owner.level().getEntity(id);
            if (e instanceof GrapplingHookEntity hook && !hook.isRemoved() && hook.isModeB()) {
                return true;
            }
        }
        return false;
    }

    /** 服务端: 玩家当前是否已有任意 A 模式 stuck 钩存在. */
    public static boolean hasAnyStuckModeA(Player owner) {
        if (owner == null) return false;
        LinkedList<Integer> list = HOOKS.get(owner.getUUID());
        if (list == null || list.isEmpty()) return false;
        for (Integer id : list) {
            Entity e = owner.level().getEntity(id);
            if (e instanceof GrapplingHookEntity hook && !hook.isRemoved() && !hook.isModeB()) {
                return true;
            }
        }
        return false;
    }
}
