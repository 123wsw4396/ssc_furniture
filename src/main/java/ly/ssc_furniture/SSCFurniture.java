package ly.ssc_furniture;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.BatClimbingScaffoldBlock;
import ly.ssc_furniture.block.ModBlocks;
import ly.ssc_furniture.compat.FormOffsetDataLoader;
import ly.ssc_furniture.compat.FormOffsetRegistry;
import ly.ssc_furniture.compat.LegTypeDataLoader;
import ly.ssc_furniture.compat.SscFormAccess;
import ly.ssc_furniture.compat.SscFormCompat;
import ly.ssc_furniture.entity.ModEntities;
import ly.ssc_furniture.item.GustClothEffects;
import ly.ssc_furniture.item.ModItems;
import ly.ssc_furniture.itemgroup.ModItemGroup;
import ly.ssc_furniture.menu.ModMenus;
import ly.ssc_furniture.net.GrappleInputPacket;
import ly.ssc_furniture.net.TrinketFirePacket;
import ly.ssc_furniture.server.HookController;
import ly.ssc_furniture.server.HookRegistry;
import ly.ssc_furniture.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.mana.ManaUtils;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider1;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider2;
import net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSCFurniture implements ModInitializer {
	public static final String MOD_ID = "ssc_furniture";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation getPlayerFormId(Player player) {
		return SscFormAccess.getCurrentFormId(player);
	}

	public static class SpiderFormInfo {
		public final int tier;
		public SpiderFormInfo(int tier) { this.tier = tier; }
	}

	private static int inheritanceDepth(Class<?> cls, Class<?> baseClass) {
		int depth = 0;
		Class<?> c = cls;
		while (c != null && c != baseClass) {
			c = c.getSuperclass();
			depth++;
		}
		return c == null ? 0 : depth;
	}

	/**
	 * 判定玩家是否处于蜘蛛形态并返回 tier.
	 * 官方 tier 1/2/3 各自有专属类; 附属基于这些类继承的形态按继承深度累加 (tier 4+).
	 * 其余情况按 formId 路径 "spider_N" 解析兜底.
	 */
	public static SpiderFormInfo getSpiderFormInfo(Player player) {
		IForm form = SscFormAccess.getCurrentForm(player);
		if (form == null) return null;

		if (form instanceof Form_Spider3) {
			return new SpiderFormInfo(3 + inheritanceDepth(form.getClass(), Form_Spider3.class));
		}
		if (form instanceof Form_Spider2) {
			return new SpiderFormInfo(2 + inheritanceDepth(form.getClass(), Form_Spider2.class));
		}
		if (form instanceof Form_Spider1) {
			return new SpiderFormInfo(1 + inheritanceDepth(form.getClass(), Form_Spider1.class));
		}

		ResourceLocation id = form.getFormID();
		if (id != null && id.getPath().startsWith("spider_")) {
			try {
				int tier = Integer.parseInt(id.getPath().substring("spider_".length()));
				return new SpiderFormInfo(Math.max(0, tier));
			} catch (NumberFormatException e) {
				return new SpiderFormInfo(0);
			}
		}

		return null;
	}

	public static class SnowFoxFormInfo {
		public final int tier;
		public SnowFoxFormInfo(int tier) { this.tier = tier; }
	}

	/** 判定玩家是否处于雪狐形态, 返回 tier (SSC 未暴露 tier 0/1 专属类, 按 formId 解析). */
	public static SnowFoxFormInfo getSnowFoxFormInfo(Player player) {
		SscFormInfo info = getSscFormInfo(player);
		if (info == null || info.family != SscFormFamily.SNOW_FOX) return null;
		return new SnowFoxFormInfo(info.tier);
	}

	/** SSC 形态大类枚举, 用于统一识别系列 (无关 tier). */
	public enum SscFormFamily {
		SNOW_FOX, ANUBIS_WOLF, FAMILIAR_FOX, OCELOT, BAT, SPIDER, OTHER
	}

	public static class SscFormInfo {
		public final SscFormFamily family;
		public final int tier;
		public SscFormInfo(SscFormFamily family, int tier) {
			this.family = family;
			this.tier = tier;
		}
	}

	private static final java.util.regex.Pattern FORM_ID_PATTERN =
		java.util.regex.Pattern.compile("^(snow_fox|anubis_wolf|familiar_fox|ocelot|bat|spider)_(\\d+)(?:_.*)?$");

	/**
	 * 统一形态识别: 根据 form ResourceLocation path 匹配 family + tier.
	 * 亚种 (如 snow_fox_3_sub_white_weasel) 会被识别为 SNOW_FOX/tier=3.
	 * 未匹配返回 null.
	 */
	public static SscFormInfo getSscFormInfo(Player player) {
		ResourceLocation id = SscFormAccess.getCurrentFormId(player);
		if (id == null) return null;
		java.util.regex.Matcher m = FORM_ID_PATTERN.matcher(id.getPath());
		if (!m.matches()) return null;
		SscFormFamily family;
		switch (m.group(1)) {
			case "snow_fox":      family = SscFormFamily.SNOW_FOX; break;
			case "anubis_wolf":   family = SscFormFamily.ANUBIS_WOLF; break;
			case "familiar_fox":  family = SscFormFamily.FAMILIAR_FOX; break;
			case "ocelot":        family = SscFormFamily.OCELOT; break;
			case "bat":           family = SscFormFamily.BAT; break;
			case "spider":        family = SscFormFamily.SPIDER; break;
			default:              family = SscFormFamily.OTHER; break;
		}
		int tier;
		try {
			tier = Integer.parseInt(m.group(2));
		} catch (NumberFormatException e) {
			tier = -1;
		}
		return new SscFormInfo(family, tier);
	}

	/** 缠布 / 类似跨形态适配装备的启用条件: 委派给 SscFormCompat, 支持第三方附属 & 数据包扩展. */
	public static boolean isGustClothActive(Player player) {
		return SscFormCompat.isBeastLeg(player);
	}

	public static double getPlayerSilk(Player player) {
		return ManaUtils.getPlayerMana(player);
	}

	public static boolean tryConsumeSilk(Player player, double amount) {
		ManaUtils.consumePlayerMana(player, amount);
		return true;
	}

	@Override
	public void onInitialize() {
		SscFormCompat.init();
		FormOffsetRegistry.init();
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new LegTypeDataLoader());
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FormOffsetDataLoader());
		ModSounds.register();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModEntities.register();
		ModItemGroup.register();
		ModMenus.register();
		registerBathtubSleepHooks();
		GrappleInputPacket.registerServer();
		TrinketFirePacket.registerServer();
		ServerTickEvents.END_SERVER_TICK.register(HookController::onServerTick);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
				GustClothEffects.tick(p);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HookRegistry.clearPlayer(handler.getPlayer()));
		CauldronInteraction.WATER.put(ModItems.GUST_CLOTH_FOR_FOOT_BINDING, CauldronInteraction.DYED_ITEM);
		LOGGER.info("Hello Fabric world!");
	}

	private static void registerBathtubSleepHooks() {
		EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> {
			if (player.level().getBlockState(sleepingPos).getBlock() instanceof BathtubBedBlock) {
				return InteractionResult.SUCCESS;
			}
			if (player.level().getBlockState(sleepingPos).getBlock() instanceof BatClimbingScaffoldBlock) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
			if (entity.level().getBlockState(sleepingPos).getBlock() instanceof BatClimbingScaffoldBlock) {
				return Direction.NORTH;
			}
			return sleepingDirection;
		});

		EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register((entity, sleepingPos, bedState, wakeUpPos) -> {
			if (bedState.getBlock() instanceof BatClimbingScaffoldBlock) {
				return Vec3.atBottomCenterOf(sleepingPos);
			}
			return wakeUpPos;
		});

		EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, sleepingPos, vanillaResult) -> {
			if (player.level().getBlockState(sleepingPos).getBlock() instanceof BatClimbingScaffoldBlock) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}
}
