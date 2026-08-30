package ly.ssc_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BatClimbingScaffoldBlockEntity extends BlockEntity {
    public BatClimbingScaffoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BAT_CLIMBING_SCAFFOLD_BE, pos, state);
    }
}
