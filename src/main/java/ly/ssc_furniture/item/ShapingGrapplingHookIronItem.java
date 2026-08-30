package ly.ssc_furniture.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.items.accessory.AccessoryItem;

/**
 * 塑形蛛丝发射器 (铁). 装备到手/额外手饰品槽后, 蜘蛛形态主/备能力键触发发射.
 * 装备无耐久; 消耗蛛丝更省 (11/9/7).
 */
public class ShapingGrapplingHookIronItem extends AccessoryItem {

    public ShapingGrapplingHookIronItem(Properties settings) {
        super(settings);
    }

    @Override
    public void onEquip(ItemStack stack, LivingEntity owner, SlotData slotData) {
        if (owner.level().isClientSide) return;
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ssc_furniture.shaping_grappling_hook.tooltip"));
        tooltip.add(Component.translatable("item.ssc_furniture.shaping_grappling_hook.tooltip.usage"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
