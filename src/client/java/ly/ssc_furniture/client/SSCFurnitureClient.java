package ly.ssc_furniture.client;

import ly.ssc_furniture.client.anim.HangAnimation;
import ly.ssc_furniture.client.anim.BatScaffoldSleepAnimation;
import ly.ssc_furniture.client.command.HangDebugCommand;
import ly.ssc_furniture.client.config.SSCFurnitureConfig;
import ly.ssc_furniture.client.render.bathtub.BathtubSleepAxolotl2FormRenderer;
import ly.ssc_furniture.client.render.bathtub.BathtubSleepAxolotl2HumanRenderer;
import ly.ssc_furniture.client.render.bathtub.BathtubSleepRenderer;
import ly.ssc_furniture.client.renderer.DigestionWebBoxItemRenderer;
import ly.ssc_furniture.client.renderer.DigestionWebBoxRenderer;
import ly.ssc_furniture.client.renderer.FootBindingAttachmentLayer;
import ly.ssc_furniture.client.renderer.GrapplingHookAttachmentLayer;
import ly.ssc_furniture.client.renderer.GrapplingHookRenderer;
import ly.ssc_furniture.client.renderer.NoopEntityRenderer;
import ly.ssc_furniture.client.screen.DigestionWebBoxScreen;
import ly.ssc_furniture.entity.ModEntities;
import ly.ssc_furniture.block.ModBlocks;
import ly.ssc_furniture.item.GustClothColorHelper;
import ly.ssc_furniture.item.ModItems;
import ly.ssc_furniture.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistry;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

public class SSCFurnitureClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SSCFurnitureConfig.load();
		EntityRendererRegistry.register(ModEntities.GRAPPLING_HOOK, GrapplingHookRenderer::new);
		EntityRendererRegistry.register(ModEntities.SEAT, NoopEntityRenderer::new);
		BlockEntityRenderers.register(ModBlocks.DIGESTION_WEB_BOX_BE, DigestionWebBoxRenderer::new);
		BuiltinItemRendererRegistry.INSTANCE.register(
				ModBlocks.DIGESTION_WEB_BOX.asItem(),
				new DigestionWebBoxItemRenderer());
		MenuScreens.register(ModMenus.DIGESTION_WEB_BOX, DigestionWebBoxScreen::new);

		AnimRegistry.registerPowerDefaultAnim(
				HangAnimation.ANIM_ID,
				new AnimRegistry.PowerDefaultAnim(
						new AnimUtils.AnimationHolderData(HangAnimation.ANIM_ID)
				)
		);

		AnimRegistry.registerPowerDefaultAnim(
				BatScaffoldSleepAnimation.ANIM_ID,
				new AnimRegistry.PowerDefaultAnim(
						new AnimUtils.AnimationHolderData(BatScaffoldSleepAnimation.ANIM_ID)
				)
		);

		HangAnimation.register();
		BatScaffoldSleepAnimation.register();
		HangDebugCommand.register();
		GrappleInputSender.register();
		TrinketSkillKeybindSender.register();
		BathtubSleepRenderer.register();
		BathtubSleepAxolotl2FormRenderer.register();
		BathtubSleepAxolotl2HumanRenderer.register();

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, ctx) -> {
			if (entityRenderer instanceof PlayerRenderer playerRenderer) {
				helper.register(new GrapplingHookAttachmentLayer(playerRenderer));
				helper.register(new FootBindingAttachmentLayer(playerRenderer));
			}
		});

		// 空 ArmorRenderer 抑制 vanilla HumanoidArmorLayer 对缠布靴的紫黑色未贴图输出.
		// 缠布实际渲染在 FootBindingAttachmentLayer 完成.
		ArmorRenderer.register(
				(matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {},
				ModItems.GUST_CLOTH_FOR_FOOT_BINDING);

		// 物品栏图标按 NBT 染色切换 model. predicate "dye_color": 未染色 0.0, 已染色 (colorId+1)/17.
		ItemProperties.register(
				ModItems.GUST_CLOTH_FOR_FOOT_BINDING,
				new ResourceLocation("ssc_furniture", "dye_color"),
				(stack, level, entity, seed) -> GustClothColorHelper.getModelPredicateValue(stack));
	}
}
