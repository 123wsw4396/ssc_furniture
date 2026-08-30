package ly.ssc_furniture.mixin;

import ly.ssc_furniture.block.BatClimbingScaffoldBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class BatScaffoldObstructBypassMixin {
	@Inject(method = "bedBlocked", at = @At("HEAD"), cancellable = true)
	private void ssc_furniture$bypassScaffoldObstruct(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer self = (ServerPlayer)(Object)this;
		if (self.level().getBlockState(pos).getBlock() instanceof BatClimbingScaffoldBlock) {
			cir.setReturnValue(false);
		}
	}
}
