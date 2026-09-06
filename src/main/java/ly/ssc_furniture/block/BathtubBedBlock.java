package ly.ssc_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import ly.ssc_furniture.util.FormRequirement;

public class BathtubBedBlock extends BedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape HEAD_SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 1, 16),
        Block.box(0, 0, 15, 3, 14, 16),
        Block.box(0, 0, 0, 1, 14, 15),
        Block.box(1, 0, 8, 15, 3, 15),
        Block.box(3, 0, 15, 13, 13, 16),
        Block.box(13, 0, 15, 16, 14, 16),
        Block.box(15, 0, 0, 16, 14, 15)
    );
    private static final VoxelShape FOOT_SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 1, 16),
        Block.box(0, 0, 1, 16, 14, 2),
        Block.box(0, 0, 0, 1, 14, 16),
        Block.box(10, 4, 0, 14, 6, 1),
        Block.box(10, 2, 0, 12, 5, 1),
        Block.box(9, 4, 0, 12, 7, 1),
        Block.box(5, 6, 0, 11, 9, 1),
        Block.box(4, 9, 0, 10, 11, 1),
        Block.box(4, 11, 0, 8, 12, 1),
        Block.box(6, 5, 0, 8, 6, 1),
        Block.box(12, 3, 0, 13, 4, 1),
        Block.box(15, 0, 0, 16, 14, 16)
    );

    public BathtubBedBlock(Properties properties) {
        super(DyeColor.WHITE, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false)
                .setValue(WATERLOGGED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.is(Items.BUCKET)) {
                itemStack.shrink(1);
                ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                if (itemStack.isEmpty()) {
                    player.setItemInHand(hand, waterBucket);
                } else if (!player.getInventory().add(waterBucket)) {
                    player.drop(waterBucket, false);
                }
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (state.getValue(PART) != BedPart.HEAD) {
                BlockPos headPos = pos.relative(state.getValue(FACING));
                BlockState headState = level.getBlockState(headPos);
                if (headState.is(this)) {
                    return use(headState, level, headPos, player, hand, hit);
                }
                player.displayClientMessage(
                    Component.translatable("block.ssc_furniture.bathtub_bed.missing"), true);
                return InteractionResult.FAIL;
            }

            if (state.getValue(OCCUPIED)) {
                player.displayClientMessage(
                    Component.translatable("block.minecraft.bed.occupied"), true);
                return InteractionResult.FAIL;
            }

            if (!FormRequirement.isAxolotlTier1OrAbove(player)) {
                player.displayClientMessage(
                    Component.translatable("block.ssc_furniture.water_form_required"), true);
                return InteractionResult.FAIL;
            }

            var result = player.startSleepInBed(pos);
            result.ifLeft(reason -> {
                if (reason.getMessage() != null) {
                    player.displayClientMessage(reason.getMessage(), true);
                }
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rotateShape(state.getValue(PART) == BedPart.FOOT ? FOOT_SHAPE : HEAD_SHAPE,
                          state.getValue(FACING).getOpposite());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rotateShape(state.getValue(PART) == BedPart.FOOT ? FOOT_SHAPE : HEAD_SHAPE,
                          state.getValue(FACING).getOpposite());
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) return shape;
        VoxelShape[] out = {Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            VoxelShape part = switch (facing) {
                case SOUTH -> Shapes.box(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
                case EAST  -> Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2);
                case WEST  -> Shapes.box(z1, y1, 1 - x2, z2, y2, 1 - x1);
                default -> Shapes.box(x1, y1, z1, x2, y2, z2);
            };
            out[0] = Shapes.or(out[0], part);
        });
        return out[0];
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BathtubBedBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
