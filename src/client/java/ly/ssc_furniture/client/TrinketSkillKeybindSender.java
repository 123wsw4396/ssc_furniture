package ly.ssc_furniture.client;

import ly.ssc_furniture.compat.TrinketsCompat;
import ly.ssc_furniture.net.TrinketFirePacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.onixary.shapeShifterCurseFabric.client.ShapeShifterCurseFabricClient;

/**
 * 客户端: 直接读 SSC 公开的 `useActiveSkill1PowerKeybind` / `useActiveSkill2PowerKeybind`,
 * 按住任一 + 装备塑形发射器 + 蜘蛛形态时, 每 tick 发 TrinketFirePacket 到服务端.
 * 服务端 HookController.fireFromTrinket 有 10 tick 节流, 匹配 SSC 原 continuous 行为.
 *
 * 目的: SSC 只在 spider_3 挂了 active_skill_2 power, tier 0/2 客户端 apoli 遍历时找不到 power,
 * 永远不会发 apoli:use_active_powers, 服务端 mixin 拦截不到. 这里绕过 apoli, 直接监听 keybind.
 *
 * SSC 是本 mod 硬依赖, 故直接类型访问, 不再反射.
 */
public final class TrinketSkillKeybindSender {

    private TrinketSkillKeybindSender() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(TrinketSkillKeybindSender::tick);
    }

    private static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (!TrinketsCompat.hasShapingHook(player)) return;

        KeyMapping kb1 = ShapeShifterCurseFabricClient.useActiveSkill1PowerKeybind;
        KeyMapping kb2 = ShapeShifterCurseFabricClient.useActiveSkill2PowerKeybind;
        if (kb1 == null && kb2 == null) return;

        if (!ClientPlayNetworking.canSend(TrinketFirePacket.ID)) return;

        if (kb1 != null && kb1.isDown()) {
            ClientPlayNetworking.send(TrinketFirePacket.ID, TrinketFirePacket.create(false));
        }
        if (kb2 != null && kb2.isDown()) {
            ClientPlayNetworking.send(TrinketFirePacket.ID, TrinketFirePacket.create(true));
        }
    }
}
