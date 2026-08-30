package ly.ssc_furniture.client.anim;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

public class HangAnimation {

    public static final ResourceLocation ANIM_ID = new ResourceLocation("ssc_furniture", "spider_3_hang");

    private static final double ARRIVAL_DISTANCE = 2.0;

    private static boolean currentlyHanging = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(HangAnimation::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            if (currentlyHanging) currentlyHanging = false;
            return;
        }

        boolean shouldHang = shouldHang(player);
        if (shouldHang == currentlyHanging) return;

        if (shouldHang) {
            AnimUtils.playPowerAnimLoop(player, ANIM_ID, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
            HangState.setHanging(player, true);
            CameraRollState.setHanging(true);
            currentlyHanging = true;
        } else {
            AnimUtils.stopPowerAnimWithIDs(player, AnimUtils.AnimationSendSideType.ONLY_CLIENT, ANIM_ID);
            HangState.setHanging(player, false);
            CameraRollState.setHanging(false);
            currentlyHanging = false;
        }
    }

    private static boolean shouldHang(LocalPlayer player) {
        if (player.isShiftKeyDown()) return false;

        SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(player);
        if (info == null || info.tier < 3) return false;

        return findArrivedCeilingHook(player) != null;
    }

    private static GrapplingHookEntity findArrivedCeilingHook(LocalPlayer player) {
        AABB search = player.getBoundingBox().inflate(8.0);
        for (Entity e : player.level().getEntities(player, search, ent -> ent instanceof GrapplingHookEntity)) {
            GrapplingHookEntity hook = (GrapplingHookEntity) e;
            if (hook.getOwner() != player) continue;
            if (!hook.isNoGravity()) continue;

            // 仅命中方块底面 (= 天花板挂点) 才允许倒立; 命中侧面/实体不倒立
            if (hook.getHitFace() != Direction.DOWN) continue;

            double dx = hook.getX() - player.getX();
            double dy = hook.getY() - player.getEyeY();
            double dz = hook.getZ() - player.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > ARRIVAL_DISTANCE) continue;

            return hook;
        }
        return null;
    }
}
