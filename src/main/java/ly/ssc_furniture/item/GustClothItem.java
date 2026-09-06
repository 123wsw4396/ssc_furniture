package ly.ssc_furniture.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 轻快缠布. 实现 DyeableLeatherItem 以复用 vanilla 皮革盔甲染色 (ArmorDyeRecipe / CauldronInteraction / tooltip).
 */
public class GustClothItem extends ArmorItem implements DyeableLeatherItem {
    public GustClothItem(ArmorMaterial material, Type type, Properties settings) {
        super(material, type, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ssc_furniture.gust_cloth_for_foot_binding.tooltip").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.ssc_furniture.gust_cloth_for_foot_binding.tooltip.effect").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.ssc_furniture.gust_cloth_for_foot_binding.tooltip.dyeable").withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
