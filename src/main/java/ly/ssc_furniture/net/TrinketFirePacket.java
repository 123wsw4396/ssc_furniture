package ly.ssc_furniture.net;

import io.netty.buffer.Unpooled;
import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.compat.TrinketsCompat;
import ly.ssc_furniture.server.HookController;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S: 客户端在按下 SSC 主/备能力键 且 装备了塑形发射器 时, 通知服务端发射.
 * 服务端二次校验 (装备 + 蜘蛛形态), 再走 HookController.fireFromTrinket (自带 10 tick 节流).
 */
public final class TrinketFirePacket {

    public static final ResourceLocation ID = new ResourceLocation(SSCFurniture.MOD_ID, "trinket_fire");

    private TrinketFirePacket() {}

    public static FriendlyByteBuf create(boolean modeB) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(modeB);
        return buf;
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            boolean modeB = buf.readBoolean();
            server.execute(() -> {
                if (!TrinketsCompat.hasShapingHook(player)) return;
                HookController.fireFromTrinket(player, modeB);
            });
        });
    }
}
