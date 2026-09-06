package ly.ssc_furniture.block;

import ly.ssc_furniture.SSCFurniture;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class DigestionWebBoxBlock extends BaseEntityBlock {

    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = buildShapeNorth();
    private static final EnumMap<Direction, VoxelShape> SHAPES_BY_DIR = new EnumMap<>(Direction.class);

    static {
        SHAPES_BY_DIR.put(Direction.NORTH, SHAPE_NORTH);
        SHAPES_BY_DIR.put(Direction.SOUTH, rotateShape(SHAPE_NORTH, Rotation.CLOCKWISE_180));
        SHAPES_BY_DIR.put(Direction.EAST, rotateShape(SHAPE_NORTH, Rotation.CLOCKWISE_90));
        SHAPES_BY_DIR.put(Direction.WEST, rotateShape(SHAPE_NORTH, Rotation.COUNTERCLOCKWISE_90));
    }

    public DigestionWebBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES_BY_DIR.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES_BY_DIR.getOrDefault(state.getValue(FACING), SHAPE_NORTH);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DigestionWebBoxBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        SSCFurniture.SpiderFormInfo info = SSCFurniture.getSpiderFormInfo(player);
        if (info == null || info.tier < 2) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("block.ssc_furniture.digestion_web_box.too_dirty")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider != null) {
            player.openMenu(provider);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlocks.DIGESTION_WEB_BOX_BE,
                (l, p, s, be) -> DigestionWebBoxBlockEntity.serverTick(l, p, s, be));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof DigestionWebBoxBlockEntity box ? box : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DigestionWebBoxBlockEntity box) {
                net.minecraft.world.Containers.dropContents(level, pos, box);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DigestionWebBoxBlockEntity box) {
            return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(box);
        }
        return 0;
    }

    private static VoxelShape buildShapeNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.join(s, Shapes.box(0.0625, 0, 0.0625, 0.5, 0.125, 0.1875), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.5, 0, 0.0625, 0.9375, 0.125, 0.1875), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.0625, 0, 0.1875, 0.5, 0.125, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.5, 0, 0.1875, 0.9375, 0.125, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.0625, 0.125, 0.0625, 0.1875, 0.25, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.1875, 0.125, 0.0625, 0.5, 0.625, 0.1875), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.5, 0.125, 0.0625, 0.8125, 0.625, 0.1875), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.0625, 0.4375, 0.0625, 0.125, 0.5, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.0625, 0.3125, 0.0625, 0.125, 0.375, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.8125, 0.375, 0.0625, 0.875, 0.5, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.875, 0.4375, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.1875, 0.125, 0.8125, 0.5, 0.625, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.5, 0.125, 0.8125, 0.8125, 0.625, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.07103562499999999, 0.625, 0.04632124999999998, 0.508535625, 0.6875, 0.92132125), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.508535625, 0.625, 0.04632124999999998, 0.9460356249999999, 0.6875, 0.92132125), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.8125, 0.5, 0.0625, 0.875, 0.625, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.8125, 0.125, 0.0625, 0.9375, 0.375, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.125, 0.25, 0.0625, 0.1875, 0.375, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.875, 0.5625, 0.0625, 0.9375, 0.625, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.0625, 0.5625, 0.0625, 0.1875, 0.625, 0.9375), BooleanOp.OR);
        s = Shapes.join(s, Shapes.box(0.125, 0.375, 0.0625, 0.1875, 0.5625, 0.9375), BooleanOp.OR);
        return s;
    }

    private static VoxelShape rotateShape(VoxelShape base, Rotation rot) {
        VoxelShape[] buffer = new VoxelShape[]{ base, Shapes.empty() };
        int times = switch (rot) {
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
            default -> 0;
        };
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
}
