package ly.ssc_furniture.server;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import ly.ssc_furniture.item.GrapplingHookItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 每 tick 遍历玩家统一求解 grappling hook 物理.
 * A 模式: 直线牵引 (不做摆物理), 到达 STOP_DISTANCE 后静止
 * B 模式: XPBD 位置层投影 + 反推速度 -> 稳定摆动物理; W/S 调节绳长
 * 双钩: 极限拉紧 (锚点距离 >= 绳长和) 时钉住玩家
 */
public final class HookController {

    private static final int SOLVER_ITERATIONS = 5;
    private static final double EPS = 1e-6;
    private static final double EXTREME_TOLERANCE = 0.02;
    private static final double DAMPING = 0.995;
    private static final double MAX_SPEED = 3.0;
    private static final double PUMP_GAIN = 8.0;
    private static final double PUMP_MAX_PER_TICK = 0.10;
    private static final double ARRIVED_TOLERANCE = 0.05;
    private static final double NOVICE_INITIAL_SHRINK = 0.5;
    private static final double NOVICE_TANGENT_KICK = 1.0;

    private HookController() {}

    /** 饰品版发射节流: 上次发射时间. */
    private static final Map<UUID, Long> LAST_TRINKET_FIRE = new HashMap<>();
    private static final long TRINKET_COOLDOWN_TICKS = 10L;

    /**
     * 由 apoli power keybind mixin 调用. 检查 spider form + silk, 触发发射.
     * 节流: 每 10 ticks 最多一次 (power 是 continuous, 每 tick 都会调用).
     */
    public static void fireFromTrinket(ServerPlayer player, boolean modeB) {
        long now = player.serverLevel().getGameTime();
        Long last = LAST_TRINKET_FIRE.get(player.getUUID());
        if (last != null && now - last < TRINKET_COOLDOWN_TICKS) return;

        SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(player);
        if (info == null) return;

        int tier = Math.min(info.tier, 3);
        double cost = GrapplingHookItem.getTrinketSilkCostForTier(tier);
        double silk = SSCFurniture.getPlayerSilk(player);
        if (silk < cost) {
            player.displayClientMessage(
                    Component.translatable("item.ssc_furniture.grappling_hook.no_silk", (int) cost), true);
            return;
        }

        boolean actualModeB;
        if (HookRegistry.hasAnyStuckModeA(player)) actualModeB = false;
        else if (HookRegistry.hasAnyStuckModeB(player)) actualModeB = true;
        else actualModeB = modeB;

        GrapplingHookItem.fireHook(player, player.level(), info, actualModeB, cost);
        LAST_TRINKET_FIRE.put(player.getUUID(), now);
    }

    public static void clearTrinketCooldown(UUID uuid) {
        LAST_TRINKET_FIRE.remove(uuid);
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                tickPlayer(player);
            }
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        List<GrapplingHookEntity> hooks = HookRegistry.getStuckHooks(player);
        if (hooks.isEmpty()) return;

        Vec3 ownerCenter = player.position().add(0, player.getBbHeight() * 0.5, 0);
        List<HookData> data = new ArrayList<>(hooks.size());
        boolean anyAMode = false;

        for (GrapplingHookEntity hook : hooks) {
            Vec3 pivot = hook.getPivot();
            if (pivot == null) continue;

            Vec3 pivotToOwner = ownerCenter.subtract(pivot);
            double L = pivotToOwner.length();

            if (hook.getInitialLength() < 0) {
                hook.setInitialLength(L);
                double initialTarget = L;
                if (hook.isNovice() && hook.isModeB()) {
                    initialTarget = Math.max(GrapplingHookEntity.STOP_DISTANCE, L - NOVICE_INITIAL_SHRINK);
                }
                hook.setCurrentLength(initialTarget);
            }

            double target = computeTargetLength(hook, L);
            hook.setCurrentLength(target);

            Vec3 n = L > EPS ? pivotToOwner.scale(1.0 / L) : new Vec3(0, 1, 0);
            data.add(new HookData(hook, pivot, n, L, target));
            if (!hook.isModeB()) anyAMode = true;
        }

        if (data.isEmpty()) return;

        // A 模式: 收紧过程直线牵引 (无重力), 到达 STOP_DISTANCE 后切换为重力悬挂
        if (anyAMode) {
            HookData d = data.get(0);
            boolean arrived = d.targetLength <= GrapplingHookEntity.STOP_DISTANCE + EPS
                    && d.L <= GrapplingHookEntity.STOP_DISTANCE + ARRIVED_TOLERANCE;

            if (!arrived) {
                // 收紧: 直线牵引, 无重力, 沿 -n 拉向 pivot
                double gap = d.L - d.targetLength;
                double speed = Math.min(gap, d.hook.getPullSpeed());
                Vec3 v = d.n.scale(-speed);
                player.setDeltaMovement(v);
                player.fallDistance = 0;
                player.hurtMarked = true;
                return;
            }

            // 已到达: 重力悬挂, 用 XPBD 单钩位置投影
            Vec3 vGrav = player.getDeltaMovement();
            Vec3 xPred = ownerCenter.add(vGrav);
            for (int iter = 0; iter < SOLVER_ITERATIONS; iter++) {
                Vec3 delta = xPred.subtract(d.pivot);
                double pl = delta.length();
                if (pl > d.targetLength + EPS && pl > EPS) {
                    xPred = d.pivot.add(delta.scale(d.targetLength / pl));
                } else {
                    break;
                }
            }
            Vec3 vNew = xPred.subtract(ownerCenter);
            vNew = vNew.scale(DAMPING);
            double vLen = vNew.length();
            if (vLen > MAX_SPEED) vNew = vNew.scale(MAX_SPEED / vLen);
            player.setDeltaMovement(vNew);
            player.fallDistance = 0;
            player.hurtMarked = true;
            return;
        }

        // B 模式双钩极限拉紧: 钉在几何解位置
        if (data.size() >= 2 && player.hurtTime == 0) {
            HookData d1 = data.get(0);
            HookData d2 = data.get(1);
            Vec3 delta = d2.pivot.subtract(d1.pivot);
            double D = delta.length();
            double sum = d1.targetLength + d2.targetLength;
            double tol = Math.max(EXTREME_TOLERANCE, sum * 0.005);
            if (D >= sum - tol && D > EPS) {
                Vec3 anchorPos = d1.pivot.add(delta.scale(d1.targetLength / D));
                double footY = anchorPos.y - player.getBbHeight() * 0.5;
                player.setPos(anchorPos.x, footY, anchorPos.z);
                player.setDeltaMovement(0, 0, 0);
                player.fallDistance = 0;
                player.hurtMarked = true;
                return;
            }
        }

        // B 模式 novice 首 tick kick: 手动径向抬升 + 水平切向踢出, 跳过 XPBD 与泵浦
        for (HookData d : data) {
            if (!d.hook.isModeB()) continue;
            if (!d.hook.consumeNeedsNoviceKick()) continue;

            Vec3 vRadial = d.n.scale(-NOVICE_INITIAL_SHRINK);

            Vec3 look = player.getLookAngle();
            Vec3 lookTangent = look.subtract(d.n.scale(look.dot(d.n)));
            double tLen = lookTangent.length();
            Vec3 tangent;
            if (tLen > EPS) {
                tangent = lookTangent.scale(1.0 / tLen);
            } else {
                double nhx = d.n.x, nhz = d.n.z;
                double nhLen = Math.sqrt(nhx * nhx + nhz * nhz);
                if (nhLen > EPS) {
                    tangent = new Vec3(-nhz / nhLen, 0.0, nhx / nhLen);
                } else {
                    float yawRad = player.yBodyRot * ((float) Math.PI / 180F);
                    tangent = new Vec3(-Math.cos(yawRad), 0.0, -Math.sin(yawRad));
                }
            }
            Vec3 vTangent = tangent.scale(NOVICE_TANGENT_KICK);

            Vec3 vKick = vRadial.add(vTangent);
            player.setDeltaMovement(vKick);
            player.fallDistance = 0;
            player.hurtMarked = true;
            return;
        }

        // B 模式 XPBD 摆物理:
        // 1. 用 vanilla 已算好的 deltaMovement (已含重力 -0.08 + 玩家输入)
        Vec3 v = player.getDeltaMovement();
        // 2. 位置预测 (dt = 1 tick)
        Vec3 xPred = ownerCenter.add(v);

        // 3. 位置层球面约束: 迭代把 xPred 投影回所有球面内
        for (int iter = 0; iter < SOLVER_ITERATIONS; iter++) {
            boolean corrected = false;
            for (HookData d : data) {
                Vec3 delta = xPred.subtract(d.pivot);
                double len = delta.length();
                if (len > d.targetLength + EPS && len > EPS) {
                    xPred = d.pivot.add(delta.scale(d.targetLength / len));
                    corrected = true;
                }
            }
            if (!corrected) break;
        }

        // 4. 从位置修正反推速度 (量纲正确, 无径向内推残留)
        Vec3 vNew = xPred.subtract(ownerCenter);

        // 5. 收紧泵浦: W 输入且真实收缩时, 沿朝向切平面投影加速 (自然收敛)
        // novice (tier 0) 屏蔽泵浦: 只能靠命中初动能, 按 W 无效
        for (HookData d : data) {
            if (!d.hook.isModeB()) continue;
            if (d.hook.isNovice()) continue;
            if (d.hook.getGrappleInput() <= 0) continue;
            double shrink = d.L - d.targetLength;
            if (shrink <= EPS) continue;

            Vec3 look = player.getLookAngle();
            Vec3 tangent = look.subtract(d.n.scale(look.dot(d.n)));
            double tLen = tangent.length();
            if (tLen <= EPS) continue;
            tangent = tangent.scale(1.0 / tLen);

            double vTangent = vNew.dot(tangent);
            double convergence = Math.max(0.0, 1.0 - vTangent / MAX_SPEED);
            double boost = shrink * PUMP_GAIN * convergence;
            if (boost > PUMP_MAX_PER_TICK) boost = PUMP_MAX_PER_TICK;
            vNew = vNew.add(tangent.scale(boost));
        }

        // 6. 阻尼
        vNew = vNew.scale(DAMPING);

        // 7. 总速限
        double vLen = vNew.length();
        if (vLen > MAX_SPEED) {
            vNew = vNew.scale(MAX_SPEED / vLen);
        }

        player.setDeltaMovement(vNew);
        player.fallDistance = 0;
        player.hurtMarked = true;
    }

    /** A 模式: 匀速收紧到 STOP_DISTANCE. B 模式: W/S 输入调整. */
    private static double computeTargetLength(GrapplingHookEntity hook, double L) {
        double current = hook.getCurrentLength();
        if (current < 0) current = L;

        if (hook.isModeB()) {
            byte input = hook.getGrappleInput();
            double step = hook.getBModeSpeed();
            double delta = 0;
            if (input > 0) delta = -step;
            else if (input < 0) delta = step;
            double newLen = current + delta;
            newLen = Math.max(GrapplingHookEntity.STOP_DISTANCE, newLen);
            newLen = Math.min(hook.getInitialLength(), newLen);
            return newLen;
        }
        return Math.max(GrapplingHookEntity.STOP_DISTANCE, current - hook.getPullSpeed());
    }

    private record HookData(GrapplingHookEntity hook, Vec3 pivot, Vec3 n, double L, double targetLength) {}
}
