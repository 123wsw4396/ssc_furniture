package ly.ssc_furniture.item;

import ly.ssc_furniture.SSCFurniture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 轻快缠布运行时属性管理:
 * 装备到 FEET 槽 && 处于雪狐 tier 2/3 时, 挂 3 个 modifier:
 *   - MOVEMENT_SPEED +11% (MULTIPLY_TOTAL)
 *   - ARMOR +2 (ADDITION)
 *   - ARMOR_TOUGHNESS +1 (ADDITION)
 * 条件不满足时立即移除.
 */
public final class GustClothEffects {

    private static final UUID SPEED_UUID = UUID.fromString("a7f3c9e2-4b6d-4e51-9c8a-1f2b3d4e5a6b");
    private static final UUID ARMOR_UUID = UUID.fromString("b8e4dab3-5c7e-4f62-8d9b-2a3c4d5e6f7c");
    private static final UUID TOUGH_UUID = UUID.fromString("c9f5ebc4-6d8f-4073-9e0c-3b4d5e6f708d");

    private static final AttributeModifier SPEED_MOD = new AttributeModifier(
            SPEED_UUID, "ssc_furniture:gust_cloth_speed", 0.11D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier ARMOR_MOD = new AttributeModifier(
            ARMOR_UUID, "ssc_furniture:gust_cloth_armor", 2.0D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier TOUGH_MOD = new AttributeModifier(
            TOUGH_UUID, "ssc_furniture:gust_cloth_toughness", 1.0D, AttributeModifier.Operation.ADDITION);

    private GustClothEffects() {}

    public static void tick(Player player) {
        boolean shouldApply = shouldApply(player);
        applyOrRemove(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MOD, shouldApply);
        applyOrRemove(player.getAttribute(Attributes.ARMOR), ARMOR_MOD, shouldApply);
        applyOrRemove(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGH_MOD, shouldApply);
    }

    private static boolean shouldApply(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.is(ModItems.GUST_CLOTH_FOR_FOOT_BINDING)) return false;
        return SSCFurniture.isGustClothActive(player);
    }

    private static void applyOrRemove(AttributeInstance inst, AttributeModifier mod, boolean apply) {
        if (inst == null) return;
        boolean has = inst.hasModifier(mod);
        if (apply && !has) {
            inst.addPermanentModifier(mod);
        } else if (!apply && has) {
            inst.removeModifier(mod);
        }
    }
}
