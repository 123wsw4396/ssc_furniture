package ly.ssc_furniture.net;

import io.netty.buffer.Unpooled;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import ly.ssc_furniture.server.HookRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** C2S: 客户端每 tick 状态变化时发送 W/S 输入 (-1 = 后退, 0 = 无, 1 = 前进). */
public final class GrappleInputPacket {

    public static final ResourceLocation ID = new ResourceLocation(SSCFurniture.MOD_ID, "grapple_input");

    private GrappleInputPacket() {}

    public static FriendlyByteBuf create(byte state) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(state);
        return buf;
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            byte state = buf.readByte();
            server.execute(() -> {
                for (GrapplingHookEntity hook : HookRegistry.getStuckHooks(player)) {
                    if (hook.isModeB()) hook.setGrappleInput(state);
                }
            });
        });
    }
}
