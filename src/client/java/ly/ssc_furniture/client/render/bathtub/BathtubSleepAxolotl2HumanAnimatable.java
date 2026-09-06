package ly.ssc_furniture.client.render.bathtub;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

@SuppressWarnings("removal")
public class BathtubSleepAxolotl2HumanAnimatable implements GeoAnimatable {

    public static final BathtubSleepAxolotl2HumanAnimatable INSTANCE = new BathtubSleepAxolotl2HumanAnimatable();

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }
}
