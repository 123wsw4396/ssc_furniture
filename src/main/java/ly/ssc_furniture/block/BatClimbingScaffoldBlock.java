package ly.ssc_furniture.block;

import ly.ssc_furniture.util.FormRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BatClimbingScaffoldBlock extends BaseEntityBlock {

    // 只允许贴在方块底面 (天花板下表面) -> 无 FACING 属性, 单一模型
    // 从 Bat_ClimbingScaffold.txt 转换而来 (单位 1/16)
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7, 8, 0, 8, 9, 16),
            Block.box(7, 14, 0, 10, 16, 16),
            Block.box(7, 9, 15, 8, 14, 16),
            Block.box(7, 9, 0, 8, 14, 1),
            Block.box(7, 12, 1, 8, 13, 15)
    );

    public BatClimbingScaffoldBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // 只允许玩家点击方块底面 (clickedFace = DOWN) 来放置
        if (ctx.getClickedFace() != Direction.DOWN) return null;
        return this.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 需要头顶方块支撑
        BlockPos supportPos = pos.above();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!FormRequirement.isBatTier3OrAbove(player)) {
            player.displayClientMessage(
                    Component.translatable("block.ssc_furniture.bat_climbing_scaffold.not_bat"), true);
            return InteractionResult.FAIL;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        var result = player.startSleepInBed(pos);
        result.ifLeft(reason -> {
            Component msg = reason.getMessage();
            if (msg != null) {
                player.displayClientMessage(msg, true);
            } else {
                player.displayClientMessage(
                        Component.translatable("block.ssc_furniture.bat_climbing_scaffold.cannot_sleep"), true);
            }
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatClimbingScaffoldBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
