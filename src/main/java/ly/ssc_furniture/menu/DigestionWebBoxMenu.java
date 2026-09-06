package ly.ssc_furniture.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DigestionWebBoxMenu extends AbstractContainerMenu {

    public static final int CONTAINER_SIZE = 15;
    public static final int GRID_COLS = 5;
    public static final int GRID_ROWS = 3;

    private final Container container;

    public DigestionWebBoxMenu(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(CONTAINER_SIZE));
    }

    public DigestionWebBoxMenu(int syncId, Inventory playerInv, Container container) {
        super(ModMenus.DIGESTION_WEB_BOX, syncId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInv.player);

        // 3x5 grid, centered: x = 44, y = 18
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = col + row * GRID_COLS;
                this.addSlot(new FilteredSlot(container, index, 44 + col * 18, 18 + row * 18));
            }
        }

        // player inventory 3x9 at x = 8, y = 84
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // hotbar at y = 142
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CONTAINER_SIZE) {
                // container -> player inv
                if (!this.moveItemStackTo(stack, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // player -> container (respect filter)
                if (!this.container.canPlaceItem(0, stack)) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(stack, 0, CONTAINER_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private static class FilteredSlot extends Slot {
        public FilteredSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.container.canPlaceItem(this.getContainerSlot(), stack);
        }
    }
}
