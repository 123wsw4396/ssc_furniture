package ly.ssc_furniture;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.BatClimbingScaffoldBlock;
import ly.ssc_furniture.block.ModBlocks;
import ly.ssc_furniture.compat.FormOffsetDataLoader;
import ly.ssc_furniture.compat.FormOffsetRegistry;
import ly.ssc_furniture.compat.LegTypeDataLoader;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SSCFurniture implements ModInitializer {
	public static final String MOD_ID = "ssc_furniture";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Class<?> formSpider1Class;
	private static Class<?> formSpider2Class;
	private static Class<?> formSpider3Class;
	private static Class<?> formSnowFox2Class;
	private static Class<?> formSnowFox3Class;

	private static Object getCurrentFormObject(Player player) {
		try {
			Class<?> regCompClass = Class.forName(
				"net.onixary.shapeShifterCurseFabric.player_form.utils.RegPlayerFormComponent");
			Field playerFormField = regCompClass.getField("PLAYER_FORM");
			Object componentKey = playerFormField.get(null);

			Object component = null;
			for (Method m : componentKey.getClass().getMethods()) {
				if (m.getName().equals("get") && m.getParameterCount() == 1) {
					if (m.getParameterTypes()[0].isInstance(player)) {
						component = m.invoke(componentKey, player);
						break;
					}
				}
			}
			if (component == null) return null;

			Field nowFormField = component.getClass().getField("nowForm");
			return nowFormField.get(component);
		} catch (Exception e) {
			return null;
		}
	}

	private static ResourceLocation getFormIdFromObject(Object form) {
		try {
			Method getFormID = form.getClass().getMethod("getFormID");
			return (ResourceLocation) getFormID.invoke(form);
		} catch (Exception e) {
			return null;
		}
	}

	public static ResourceLocation getPlayerFormId(Player player) {
		Object form = getCurrentFormObject(player);
		if (form == null) return null;
		return getFormIdFromObject(form);
	}

	public static class SpiderFormInfo {
		public final int tier;
		public SpiderFormInfo(int tier) { this.tier = tier; }
	}

	private static void loadSpiderFormClasses() {
		try {
			formSpider1Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider1");
			formSpider2Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider2");
			formSpider3Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider3");
		} catch (ClassNotFoundException e) {
			LOGGER.warn("SSC spider form classes not found, grappling hook will use form_id fallback");
		}
		try {
			formSnowFox2Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_SnowFox2");
			formSnowFox3Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_SnowFox3");
		} catch (ClassNotFoundException e) {
			LOGGER.warn("SSC snow_fox form classes not found, foot binding will use form_id fallback");
		}
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

	public static SpiderFormInfo getSpiderFormInfo(Player player) {
		Object form = getCurrentFormObject(player);
		if (form == null) return null;

		if (formSpider3Class != null && formSpider3Class.isInstance(form)) {
			return new SpiderFormInfo(3 + inheritanceDepth(form.getClass(), formSpider3Class));
		}
		if (formSpider2Class != null && formSpider2Class.isInstance(form)) {
			return new SpiderFormInfo(2 + inheritanceDepth(form.getClass(), formSpider2Class));
		}
		if (formSpider1Class != null && formSpider1Class.isInstance(form)) {
			return new SpiderFormInfo(1 + inheritanceDepth(form.getClass(), formSpider1Class));
		}

		ResourceLocation id = getFormIdFromObject(form);
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

	/**
	 * 判定玩家是否处于雪狐形态, 返回 tier (仅识别 tier 2/3, SSC 未提供 tier 0/1 类).
	 * 优先按类反射, fallback 按 formId 路径 "snow_fox_N" 解析.
	 */
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
		Object form = getCurrentFormObject(player);
		if (form == null) return null;
		ResourceLocation id = getFormIdFromObject(form);
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
		try {
			Class<?> cls = Class.forName("net.onixary.shapeShifterCurseFabric.mana.ManaUtils");
			Method getMana = cls.getMethod("getPlayerMana", Player.class);
			return (Double) getMana.invoke(null, player);
		} catch (Exception e) {
			return -1;
		}
	}

	public static boolean tryConsumeSilk(Player player, double amount) {
		try {
			Class<?> cls = Class.forName("net.onixary.shapeShifterCurseFabric.mana.ManaUtils");
			Method consume = cls.getMethod("consumePlayerMana", Player.class, double.class);
			consume.invoke(null, player, amount);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void onInitialize() {
		loadSpiderFormClasses();
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
