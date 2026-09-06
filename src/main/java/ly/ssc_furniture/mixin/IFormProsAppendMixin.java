package ly.ssc_furniture.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IForm.class)
public interface IFormProsAppendMixin {
	@Inject(method = "getContentText", at = @At("RETURN"), cancellable = true)
	default void ssc_furniture$appendSpiderPros(CodexData.ContentType type, CallbackInfoReturnable<Component> cir) {
		if (type != CodexData.ContentType.PROS) return;
		IForm self = (IForm) this;
		ResourceLocation id = self.getFormID();
		if (id == null) return;
		if (!"shape-shifter-curse".equals(id.getNamespace())) return;
		String appendKey;
		switch (id.getPath()) {
			case "spider_0" -> appendKey = "codex.append.ssc_furniture.spider_0.pros";
			case "spider_2" -> appendKey = "codex.append.ssc_furniture.spider_2.pros";
			case "spider_3" -> appendKey = "codex.append.ssc_furniture.spider_3.pros";
			default -> { return; }
		}
		Component original = cir.getReturnValue();
		if (original == null) return;
		MutableComponent appended = Component.empty()
				.append(original)
				.append(Component.translatable(appendKey));
		cir.setReturnValue(appended);
	}
}
