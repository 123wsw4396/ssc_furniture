package ly.ssc_furniture.block;

import ly.ssc_furniture.menu.DigestionWebBoxMenu;
import ly.ssc_furniture.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("removal")
 public class DigestionWebBoxBlockEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity, WorldlyContainer {

    public static final int CONTAINER_SIZE = 15;
    public static final int DIGEST_TICKS = 200;

    private static final String CONTROLLER_NAME = "controller";
    private static final String ANIM_OPEN = "open";
    private static final String ANIM_CLOSE = "close";

    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlayAndHold(ANIM_OPEN);
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlayAndHold(ANIM_CLOSE);

    private static final TagKey<Item> ORIGINS_MEAT_TAG =
            TagKey.create(Registries.ITEM, new ResourceLocation("origins", "meat"));
    private static final ResourceLocation SPIDER_FLUID_COCOON_ID =
            new ResourceLocation("shape-shifter-curse", "spider_fluid_cocoon");

    private static Item spiderFluidCocoonItem = null;
    private static boolean spiderFluidCocoonResolved = false;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private final int[] digestProgress = new int[CONTAINER_SIZE];

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, ModSounds.DIGESTION_WEB_BOX_OPEN_CLOSE, SoundSource.BLOCKS,
                    0.6F, level.getRandom().nextFloat() * 0.1F + 0.95F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, ModSounds.DIGESTION_WEB_BOX_OPEN_CLOSE, SoundSource.BLOCKS,
                    0.6F, level.getRandom().nextFloat() * 0.1F + 0.95F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof DigestionWebBoxMenu menu) {
                return menu.getContainer() == DigestionWebBoxBlockEntity.this;
            }
            return false;
        }
    };

    public DigestionWebBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DIGESTION_WEB_BOX_BE, pos, state);
    }

    public Container getContainer() {
        return this;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.items = list;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.ssc_furniture.digestion_web_box");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new DigestionWebBoxMenu(id, inv, this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isAcceptedInput(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[CONTAINER_SIZE];
        for (int i = 0; i < CONTAINER_SIZE; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return isAcceptedInput(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    public static boolean isAcceptedInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (isSpiderFluidCocoon(stack)) return true;
        return isMeat(stack);
    }

    private static boolean isMeat(ItemStack stack) {
        if (stack.is(ORIGINS_MEAT_TAG)) return true;
        var food = stack.getItem().getFoodProperties();
        return food != null && food.isMeat();
    }

    private static Item getSpiderFluidCocoonItem() {
        if (!spiderFluidCocoonResolved) {
            spiderFluidCocoonResolved = true;
            spiderFluidCocoonItem = BuiltInRegistries.ITEM.get(SPIDER_FLUID_COCOON_ID);
            if (spiderFluidCocoonItem == Items.AIR) spiderFluidCocoonItem = null;
        }
        return spiderFluidCocoonItem;
    }

    private static boolean isSpiderFluidCocoon(ItemStack stack) {
        Item item = getSpiderFluidCocoonItem();
        return item != null && stack.is(item);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            net.minecraft.world.ContainerHelper.loadAllItems(tag, this.items);
        }
        java.util.Arrays.fill(digestProgress, 0);
        if (tag.contains("DigestProgress", Tag.TAG_INT_ARRAY)) {
            int[] saved = tag.getIntArray("DigestProgress");
            for (int i = 0; i < CONTAINER_SIZE && i < saved.length; i++) {
                digestProgress[i] = saved[i];
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag)) {
            net.minecraft.world.ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.put("DigestProgress", new IntArrayTag(digestProgress.clone()));
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            if (param > 0) {
                triggerAnim(CONTROLLER_NAME, ANIM_OPEN);
            } else {
                triggerAnim(CONTROLLER_NAME, ANIM_CLOSE);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, 0, state -> PlayState.STOP)
                .triggerableAnim(ANIM_OPEN, OPEN_ANIM)
                .triggerableAnim(ANIM_CLOSE, CLOSE_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DigestionWebBoxBlockEntity be) {
        if (level.isClientSide) return;
        boolean changed = false;
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = be.items.get(i);
            if (stack.isEmpty()) {
                if (be.digestProgress[i] != 0) { be.digestProgress[i] = 0; changed = true; }
                continue;
            }
            Item cocoon = getSpiderFluidCocoonItem();
            if (cocoon != null && stack.is(cocoon)) {
                if (be.digestProgress[i] != 0) { be.digestProgress[i] = 0; changed = true; }
                continue;
            }
            boolean isRotten = stack.is(Items.ROTTEN_FLESH);
            boolean isMeat = isMeat(stack);
            if (!isMeat && !isRotten) {
                if (be.digestProgress[i] != 0) { be.digestProgress[i] = 0; changed = true; }
                continue;
            }
            be.digestProgress[i]++;
            if (be.digestProgress[i] >= DIGEST_TICKS) {
                int count = stack.getCount();
                if (isRotten) {
                    Item target = cocoon;
                    if (target == null) {
                        // SSC not present; leave as rotten flesh, stop progress
                        be.digestProgress[i] = 0;
                    } else {
                        be.items.set(i, new ItemStack(target, count));
                        be.digestProgress[i] = 0;
                    }
                } else {
                    be.items.set(i, new ItemStack(Items.ROTTEN_FLESH, count));
                    be.digestProgress[i] = 0;
                }
                changed = true;
            }
        }
        if (changed) {
            be.setChanged();
        }
    }
}
