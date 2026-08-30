package ly.ssc_furniture.entity;

import ly.ssc_furniture.SSCFurniture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SeatEntity extends Entity {

    private static final double BASE_OFFSET = -0.4D;
    private static final ResourceLocation ANUBIS_WOLF_3 =
            new ResourceLocation("shape-shifter-curse", "anubis_wolf_3");

    private BlockPos seatPos;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos, double yOffset) {
        this(ModEntities.SEAT, level);
        this.seatPos = pos;
        this.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
    }

    public BlockPos getSeatPos() {
        return seatPos;
    }

    @Override
    public double getPassengersRidingOffset() {
        List<Entity> passengers = getPassengers();
        if (!passengers.isEmpty() && passengers.get(0) instanceof Player player) {
            ResourceLocation formId = SSCFurniture.getPlayerFormId(player);
            if (formId != null && formId.equals(ANUBIS_WOLF_3)) {
                return BASE_OFFSET + 0.2D;
            }
        }
        return BASE_OFFSET;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (seatPos == null) {
            discard();
            return;
        }
        if (getPassengers().isEmpty()) {
            discard();
            return;
        }
        BlockState state = level().getBlockState(seatPos);
        if (!(state.getBlock() instanceof ly.ssc_furniture.block.FourLeggedCushionBlock)) {
            ejectPassengers();
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("SeatX")) {
            this.seatPos = new BlockPos(tag.getInt("SeatX"), tag.getInt("SeatY"), tag.getInt("SeatZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (seatPos != null) {
            tag.putInt("SeatX", seatPos.getX());
            tag.putInt("SeatY", seatPos.getY());
            tag.putInt("SeatZ", seatPos.getZ());
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
