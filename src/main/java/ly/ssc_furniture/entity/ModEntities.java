package ly.ssc_furniture.entity;

import ly.ssc_furniture.SSCFurniture;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<GrapplingHookEntity> GRAPPLING_HOOK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(SSCFurniture.MOD_ID, "grappling_hook"),
            FabricEntityTypeBuilder.<GrapplingHookEntity>create(MobCategory.MISC,
                            GrapplingHookEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static final EntityType<SeatEntity> SEAT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(SSCFurniture.MOD_ID, "seat"),
            FabricEntityTypeBuilder.<SeatEntity>create(MobCategory.MISC, SeatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.01F, 0.01F))
                    .trackRangeBlocks(16)
                    .trackedUpdateRate(20)
                    .build()
    );

    public static void register() {
        SSCFurniture.LOGGER.info("Registering entities for " + SSCFurniture.MOD_ID);
    }
}
