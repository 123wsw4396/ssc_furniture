package ly.ssc_furniture;

import ly.ssc_furniture.block.BathtubBedBlock;
import ly.ssc_furniture.block.ModBlocks;
import ly.ssc_furniture.entity.ModEntities;
import ly.ssc_furniture.item.ModItems;
import ly.ssc_furniture.itemgroup.ModItemGroup;
import ly.ssc_furniture.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SSCFurniture implements ModInitializer {
	public static final String MOD_ID = "ssc_furniture";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Class<?> formSpider0Class;
	private static Class<?> formSpider1Class;
	private static Class<?> formSpider2Class;
	private static Class<?> formSpider3Class;

	private static Object getCurrentFormObject(Player player) {
		try {
			Class<?> regCompClass = Class.forName(
				"net.onixary.shapeShifterCurseFabric.player_form.ability.RegPlayerFormComponent");
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

			Method getCurrentForm = component.getClass().getMethod("getCurrentForm");
			return getCurrentForm.invoke(component);
		} catch (Exception e) {
			return null;
		}
	}

	private static ResourceLocation getFormIdFromObject(Object form) {
		try {
			Field formIdField = form.getClass().getField("FormID");
			return (ResourceLocation) formIdField.get(form);
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
			formSpider0Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider0");
			formSpider1Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider1");
			formSpider2Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider2");
			formSpider3Class = Class.forName("net.onixary.shapeShifterCurseFabric.player_form.forms.Form_Spider3");
		} catch (ClassNotFoundException e) {
			LOGGER.warn("SSC spider form classes not found, grappling hook will use form_id fallback");
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
		if (formSpider0Class != null && formSpider0Class.isInstance(form)) {
			return new SpiderFormInfo(inheritanceDepth(form.getClass(), formSpider0Class));
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
		ModSounds.register();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModEntities.register();
		ModItemGroup.register();
		registerBathtubSleepHooks();
		LOGGER.info("Hello Fabric world!");
	}

	private static void registerBathtubSleepHooks() {
		EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> {
			if (player.level().getBlockState(sleepingPos).getBlock() instanceof BathtubBedBlock) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}
}
