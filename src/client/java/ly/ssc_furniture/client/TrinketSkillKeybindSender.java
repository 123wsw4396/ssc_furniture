package ly.ssc_furniture.client;

import ly.ssc_furniture.compat.TrinketsCompat;
import ly.ssc_furniture.net.TrinketFirePacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.lang.reflect.Field;

/**
 * 客户端: 反射拿 SSC 的 `useActiveSkill1PowerKeybind` / `useActiveSkill2PowerKeybind`,
 * 按住任一 + 装备塑形发射器 + 蜘蛛形态时, 每 tick 发 TrinketFirePacket 到服务端.
 * 服务端 HookController.fireFromTrinket 有 10 tick 节流, 匹配 SSC 原 continuous 行为.
 *
 * 目的: SSC 只在 spider_3 挂了 active_skill_2 power, tier 0/2 客户端 apoli 遍历时找不到 power,
 * 永远不会发 apoli:use_active_powers, 服务端 mixin 拦截不到. 这里绕过 apoli, 直接监听 keybind.
 */
public final class TrinketSkillKeybindSender {

    private static KeyMapping kb1;
    private static KeyMapping kb2;
    private static boolean resolved = false;
    private static boolean resolveFailed = false;

    private TrinketSkillKeybindSender() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(TrinketSkillKeybindSender::tick);
    }

    private static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (!TrinketsCompat.hasShapingHook(player)) return;

        resolveKeybindsOnce();
        if (kb1 == null && kb2 == null) return;

        if (!ClientPlayNetworking.canSend(TrinketFirePacket.ID)) return;

        if (kb1 != null && kb1.isDown()) {
            ClientPlayNetworking.send(TrinketFirePacket.ID, TrinketFirePacket.create(false));
        }
        if (kb2 != null && kb2.isDown()) {
            ClientPlayNetworking.send(TrinketFirePacket.ID, TrinketFirePacket.create(true));
        }
    }

    private static void resolveKeybindsOnce() {
        if (resolved || resolveFailed) return;
        try {
            Class<?> cls = Class.forName("net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient");
            Field f1 = cls.getField("useActiveSkill1PowerKeybind");
            Field f2 = cls.getField("useActiveSkill2PowerKeybind");
            Object o1 = f1.get(null);
            Object o2 = f2.get(null);
            if (o1 instanceof KeyMapping k1) kb1 = k1;
            if (o2 instanceof KeyMapping k2) kb2 = k2;
            resolved = kb1 != null || kb2 != null;
            if (!resolved) resolveFailed = true;
        } catch (Throwable ignored) {
            resolveFailed = true;
        }
    }
}
