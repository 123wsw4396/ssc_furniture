package ly.ssc_furniture.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ly.ssc_furniture.client.anim.HangState;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

public class HangDebugCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            register(dispatcher);
        });
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("scfr_anim")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("play")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    LocalPlayer player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;
                                    String raw = StringArgumentType.getString(ctx, "id");
                                    ResourceLocation id = raw.contains(":") ? new ResourceLocation(raw) : new ResourceLocation("ssc_furniture", raw);
                                    boolean ok = AnimUtils.playPowerAnimLoop(player, id, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
                                    ctx.getSource().sendFeedback(Component.literal("play " + id + " -> " + ok));
                                    return 1;
                                })))
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("stop")
                        .executes(ctx -> {
                            LocalPlayer player = ctx.getSource().getPlayer();
                            if (player == null) return 0;
                            boolean ok = AnimUtils.stopPowerAnim(player, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
                            ctx.getSource().sendFeedback(Component.literal("stop -> " + ok));
                            return 1;
                        }))
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            try {
                                Class<?> regCls = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry");
                                Object map = regCls.getMethod("getAnimations").invoke(null);
                                java.util.Map<?, ?> m = (java.util.Map<?, ?>) map;
                                ctx.getSource().sendFeedback(Component.literal("registry size: " + m.size()));
                                int shown = 0;
                                for (Object k : m.keySet()) {
                                    String s = k.toString();
                                    if (s.startsWith("ssc_furniture:") || s.contains("hang") || s.contains("spider_3")) {
                                        ctx.getSource().sendFeedback(Component.literal("  " + s));
                                        shown++;
                                    }
                                }
                                ctx.getSource().sendFeedback(Component.literal("matched: " + shown));
                            } catch (Throwable t) {
                                ctx.getSource().sendFeedback(Component.literal("list failed: " + t));
                            }
                            return 1;
                        }))
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("hang")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("on")
                                .executes(ctx -> {
                                    LocalPlayer player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;
                                    ResourceLocation id = new ResourceLocation("ssc_furniture", "spider_3_hang");
                                    boolean ok = AnimUtils.playPowerAnimLoop(player, id, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
                                    HangState.setHanging(player, true);
                                    ctx.getSource().sendFeedback(Component.literal("hang on -> anim=" + ok + " mixin=on"));
                                    return 1;
                                }))
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("off")
                                .executes(ctx -> {
                                    LocalPlayer player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;
                                    boolean ok = AnimUtils.stopPowerAnim(player, AnimUtils.AnimationSendSideType.ONLY_CLIENT);
                                    HangState.setHanging(player, false);
                                    ctx.getSource().sendFeedback(Component.literal("hang off -> anim=" + ok + " mixin=off"));
                                    return 1;
                                })))
        );
    }
}
