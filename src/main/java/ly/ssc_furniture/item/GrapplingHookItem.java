package ly.ssc_furniture.item;

import ly.ssc_furniture.SSCFurniture;
import ly.ssc_furniture.entity.GrapplingHookEntity;
import ly.ssc_furniture.server.HookRegistry;
import ly.ssc_furniture.sound.ModSounds;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GrapplingHookItem extends Item {

    private static final double BASE_SILK_COST = 12.0;
    private static final double BASE_MAX_DISTANCE = 30.0;
    private static final double BASE_PULL_SPEED = 0.5;
    private static final float BASE_SHOOT_VELOCITY = 1.5F;
    private static final double DISTANCE_BONUS = 11.0;
    private static final double SPEED_MULT = 0.25;

    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ssc_furniture.grappling_hook.tooltip"));
        tooltip.add(Component.translatable("item.ssc_furniture.grappling_hook.tooltip.mode_b"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static double getSilkCostForTier(int tier) {
        switch (tier) {
            case 3: return 8.0;
            case 2: return 10.0;
            default: return BASE_SILK_COST;
        }
    }

    public static double getTrinketSilkCostForTier(int tier) {
        switch (tier) {
            case 3: return 7.0;
            case 2: return 9.0;
            default: return 11.0;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        SSCFurniture.SpiderFormInfo spiderInfo = SSCFurniture.getSpiderFormInfo(player);
        if (spiderInfo == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.ssc_furniture.grappling_hook.not_spider"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        int tier = Math.min(spiderInfo.tier, 3);
        double cost = getSilkCostForTier(tier);

        double silk = SSCFurniture.getPlayerSilk(player);
        if (silk < cost) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.ssc_furniture.grappling_hook.no_silk", (int) cost), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        boolean modeB;
        if (HookRegistry.hasAnyStuckModeA(player)) {
            modeB = false;
        } else if (HookRegistry.hasAnyStuckModeB(player)) {
            modeB = true;
        } else {
            modeB = player.isShiftKeyDown();
        }

        if (!level.isClientSide) {
            fireHook(player, level, spiderInfo, modeB, cost);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.success(stack);
    }

    /**
     * 发射钩爪. 由手持 use() 与饰品 Mixin 共享.
     * 调用前需自行检查 silk 是否足够. 不会 hurtAndBreak (由 caller 处理).
     */
    public static void fireHook(Player player, Level level,
                                SSCFurniture.SpiderFormInfo spiderInfo,
                                boolean modeB, double silkCost) {
        if (level.isClientSide) return;

        SSCFurniture.tryConsumeSilk(player, silkCost);

        int tier = Math.min(spiderInfo.tier, 3);
        double mult = 1.0 + SPEED_MULT * tier / 3.0;
        double maxDist = BASE_MAX_DISTANCE + DISTANCE_BONUS * tier / 3.0;
        double pullSpd = BASE_PULL_SPEED * mult;
        float shootVel = (float) (BASE_SHOOT_VELOCITY * mult);

        GrapplingHookEntity hook = new GrapplingHookEntity(level, player);
        hook.setMaxDistance(maxDist);
        hook.setPullSpeed(pullSpd);
        hook.setBModeSpeed(0.022 * spiderInfo.tier);
        hook.setModeB(modeB);
        hook.setNovice(spiderInfo.tier == 0);
        Vec3 look = player.getLookAngle();
        float bodyYawRad = player.yBodyRot * ((float) Math.PI / 180F);
        double backX = -Math.sin(bodyYawRad);
        double backZ = Math.cos(bodyYawRad);
        double backOffset = spiderInfo.tier >= 3 ? 0.75 : 0.5;
        double spawnX = player.getX() - backX * backOffset;
        double spawnZ = player.getZ() - backZ * backOffset;
        double spawnY = player.getY() + (spiderInfo.tier >= 3 ? 0.7 : 0.9);
        hook.setPos(spawnX, spawnY, spawnZ);
        hook.shoot(look.x, look.y, look.z, shootVel, 0.5F);
        level.addFreshEntity(hook);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GRAPPLING_HOOK_SHOOT, SoundSource.PLAYERS, 0.5F, 0.4F);
    }
}
