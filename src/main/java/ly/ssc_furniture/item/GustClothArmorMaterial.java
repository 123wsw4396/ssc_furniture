package ly.ssc_furniture.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 轻快缠布装甲材质:
 * - 耐久: 铁靴 (195) 的 80% = 156
 * - 护甲: BOOTS +2 (只在 BOOTS 槽有值, 其它槽 0)
 * - 韧性: 1
 * - 附魔值: 15 (等同皮革, 可享受鞋子附魔)
 * 实际渲染在 FootBindingAttachmentLayer, vanilla ArmorRenderer 已注册为 no-op.
 */
public enum GustClothArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int BOOTS_DURABILITY = 156;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return type == ArmorItem.Type.BOOTS ? BOOTS_DURABILITY : 0;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() { return 15; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.STRING);
    }

    @Override
    public String getName() { return "ssc_furniture:gust_cloth"; }

    @Override
    public float getToughness() { return 0.0F; }

    @Override
    public float getKnockbackResistance() { return 0.0F; }
}
