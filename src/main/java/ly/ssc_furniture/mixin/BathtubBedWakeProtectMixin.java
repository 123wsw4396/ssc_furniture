package ly.ssc_furniture.mixin;

import ly.ssc_furniture.block.BathtubBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public class BathtubBedWakeProtectMixin {
	@Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
	private void ssc_furniture$protectBathtubDaySleep(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
		if (!wakeImmediately) return;
		ServerPlayer self = (ServerPlayer)(Object)this;
		if (self.level().isClientSide) return;
		Optional<BlockPos> spOpt = self.getSleepingPos();
		if (spOpt.isEmpty()) return;
		BlockPos sp = spOpt.get();
		BlockState st = self.level().getBlockState(sp);
		if (!(st.getBlock() instanceof BathtubBedBlock)) return;
		if (!self.level().isDay()) return;
		ci.cancel();
	}
}
