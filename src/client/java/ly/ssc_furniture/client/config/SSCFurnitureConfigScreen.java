package ly.ssc_furniture.client.config;

import ly.ssc_furniture.client.anim.CameraRollState;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SSCFurnitureConfigScreen extends Screen {

    private final Screen parent;
    private Button hangCameraButton;
    private Button shapingHandRenderButton;

    public SSCFurnitureConfigScreen(Screen parent) {
        super(Component.translatable("ssc_furniture.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 40;

        hangCameraButton = Button.builder(buildHangCameraLabel(), btn -> {
            SSCFurnitureConfig.INSTANCE.hangCameraInverted = !SSCFurnitureConfig.INSTANCE.hangCameraInverted;
            SSCFurnitureConfig.save();
            CameraRollState.updateTargetFromHanging();
            btn.setMessage(buildHangCameraLabel());
        }).bounds(centerX - 100, y, 200, 20).build();
        this.addRenderableWidget(hangCameraButton);

        shapingHandRenderButton = Button.builder(buildShapingHandRenderLabel(), btn -> {
            SSCFurnitureConfig.INSTANCE.showShapingHookInExtraHand =
                    !SSCFurnitureConfig.INSTANCE.showShapingHookInExtraHand;
            SSCFurnitureConfig.save();
            btn.setMessage(buildShapingHandRenderLabel());
        }).bounds(centerX - 100, y + 30, 200, 20)
                .tooltip(Tooltip.create(Component.translatable("ssc_furniture.config.shaping_hand_render.tooltip")))
                .build();
        this.addRenderableWidget(shapingHandRenderButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                btn -> this.minecraft.setScreen(parent))
                .bounds(centerX - 100, y + 60, 200, 20).build());
    }

    private static Component buildHangCameraLabel() {
        String stateKey = SSCFurnitureConfig.INSTANCE.hangCameraInverted
                ? "ssc_furniture.config.hang_camera.inverted"
                : "ssc_furniture.config.hang_camera.normal";
        return Component.translatable("ssc_furniture.config.hang_camera.label")
                .append(Component.literal(": "))
                .append(Component.translatable(stateKey));
    }

    private static Component buildShapingHandRenderLabel() {
        String stateKey = SSCFurnitureConfig.INSTANCE.showShapingHookInExtraHand
                ? "ssc_furniture.config.shaping_hand_render.on"
                : "ssc_furniture.config.shaping_hand_render.off";
        return Component.translatable("ssc_furniture.config.shaping_hand_render.label")
                .append(Component.literal(": "))
                .append(Component.translatable(stateKey));
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
