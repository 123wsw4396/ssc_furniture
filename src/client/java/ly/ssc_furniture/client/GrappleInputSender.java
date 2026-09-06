package ly.ssc_furniture.client;

import ly.ssc_furniture.client.anim.HangState;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import ly.ssc_furniture.net.GrappleInputPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/** 客户端: 只在钩爪 B 模式激活且 W/S 状态变化时向服务端发包. */
public final class GrappleInputSender {

    private static byte lastSent = 0;
    private static boolean lastHadModeB = false;

    private GrappleInputSender() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(GrappleInputSender::tick);
    }

    private static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            resetIfNeeded();
            return;
        }

        boolean hasModeB = hasLocalModeBHook(player);
        if (!hasModeB) {
            resetIfNeeded();
            lastHadModeB = false;
            return;
        }

        byte state = 0;
        float f = player.input.forwardImpulse;
        if (HangState.isHanging(player)) {
            // 倒立: W 屏蔽 (防止继续钉死), S 保留 (才能放长绳退出倒立)
            if (f < -0.5F) state = -1;
        } else {
            if (f > 0.5F) state = 1;
            else if (f < -0.5F) state = -1;
        }

        if (state != lastSent || !lastHadModeB) {
            if (ClientPlayNetworking.canSend(GrappleInputPacket.ID)) {
                ClientPlayNetworking.send(GrappleInputPacket.ID, GrappleInputPacket.create(state));
                lastSent = state;
                lastHadModeB = true;
            }
        }
    }

    private static void resetIfNeeded() {
        if (lastSent != 0 && lastHadModeB) {
            if (ClientPlayNetworking.canSend(GrappleInputPacket.ID)) {
                ClientPlayNetworking.send(GrappleInputPacket.ID, GrappleInputPacket.create((byte) 0));
            }
            lastSent = 0;
        }
    }

    private static boolean hasLocalModeBHook(LocalPlayer player) {
        if (!(player.level() instanceof ClientLevel level)) return false;
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof GrapplingHookEntity hook
                    && hook.isStuck()
                    && hook.isModeB()
                    && hook.getOwner() == player) {
                return true;
            }
        }
        return false;
    }
}
