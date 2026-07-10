package ly.textmod.net.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WaterbedBlockEntity extends BlockEntity {
    public WaterbedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WATERBED_BLOCK_ENTITY, pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlocks.WATERBED_BLOCK_ENTITY;
    }
}