package ly.ssc_furniture.client;

import ly.ssc_furniture.client.anim.HangAnimation;
import ly.ssc_furniture.client.command.HangDebugCommand;
import ly.ssc_furniture.client.renderer.GrapplingHookRenderer;
import ly.ssc_furniture.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimRegistry;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimUtils;

public class SSCFurnitureClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.GRAPPLING_HOOK, GrapplingHookRenderer::new);

		AnimRegistry.registerPowerDefaultAnim(
				HangAnimation.ANIM_ID,
				new AnimRegistry.PowerDefaultAnim(
						new AnimUtils.AnimationHolderData(HangAnimation.ANIM_ID)
				)
		);

		HangAnimation.register();
		HangDebugCommand.register();
	}
}
