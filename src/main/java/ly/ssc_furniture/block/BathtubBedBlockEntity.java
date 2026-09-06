package ly.ssc_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BathtubBedBlockEntity extends BlockEntity {
    public BathtubBedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BATHTUB_BED_BLOCK_ENTITY, pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlocks.BATHTUB_BED_BLOCK_ENTITY;
    }
}
