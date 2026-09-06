package ly.ssc_furniture.mixin;

import ly.ssc_furniture.util.NightSkipState;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class BatScaffoldNightSkipMixin {
	@Inject(method = "wakeUpAllPlayers", at = @At("HEAD"))
	private void ssc_furniture$onNightSkipHead(CallbackInfo ci) {
		NightSkipState.SKIPPING_NIGHT.set(Boolean.TRUE);
	}

	@Inject(method = "wakeUpAllPlayers", at = @At("RETURN"))
	private void ssc_furniture$onNightSkipReturn(CallbackInfo ci) {
		NightSkipState.SKIPPING_NIGHT.set(Boolean.FALSE);
	}
}
