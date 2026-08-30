package ly.ssc_furniture.client.anim;

import ly.ssc_furniture.block.BatClimbingScaffoldBlock;
import ly.ssc_furniture.util.FormRequirement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

import java.util.Optional;

public class BatScaffoldSleepAnimation {

    public static final ResourceLocation ANIM_ID =
            new ResourceLocation("ssc_furniture", "bat_3_climbing_scaffold_sleep");

    private static boolean currentlyPlaying = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BatScaffoldSleepAnimation::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            if (currentlyPlaying) currentlyPlaying = false;
            return;
        }

        boolean shouldPlay = shouldPlay(player);
        if (shouldPlay == currentlyPlaying) return;

        if (shouldPlay) {
            AnimUtils.playPowerAnimLoop(player, ANIM_ID, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
            currentlyPlaying = true;
        } else {
            AnimUtils.stopPowerAnimWithIDs(player, AnimUtils.AnimationSendSideType.ONLY_CLIENT, ANIM_ID);
            currentlyPlaying = false;
        }
    }

    private static boolean shouldPlay(LocalPlayer player) {
        if (!player.isSleeping()) return false;
        if (!FormRequirement.isBatTier3OrAbove(player)) return false;
        Optional<BlockPos> spOpt = player.getSleepingPos();
        if (spOpt.isEmpty()) return false;
        BlockState st = player.level().getBlockState(spOpt.get());
        return st.getBlock() instanceof BatClimbingScaffoldBlock;
    }
}
