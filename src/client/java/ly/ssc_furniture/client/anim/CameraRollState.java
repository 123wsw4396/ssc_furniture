package ly.ssc_furniture.client.anim;

import ly.ssc_furniture.client.config.SSCFurnitureConfig;

public class CameraRollState {

    private static float currentRoll = 0f;
    private static float targetRoll = 0f;
    private static final float LERP = 0.15f;
    private static boolean hanging = false;

    public static void setHanging(boolean hangingNow) {
        hanging = hangingNow;
        updateTargetFromHanging();
    }

    public static void updateTargetFromHanging() {
        targetRoll = (hanging && SSCFurnitureConfig.INSTANCE.hangCameraInverted) ? 180f : 0f;
    }

    public static float getRoll() {
        if (currentRoll != targetRoll) {
            currentRoll += (targetRoll - currentRoll) * LERP;
            if (Math.abs(targetRoll - currentRoll) < 0.1f) currentRoll = targetRoll;
        }
        return currentRoll;
    }

    public static void reset() {
        currentRoll = 0f;
        targetRoll = 0f;
        hanging = false;
    }
}
