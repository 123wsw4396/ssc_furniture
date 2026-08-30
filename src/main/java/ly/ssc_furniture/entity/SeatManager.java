package ly.ssc_furniture.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class SeatManager {

    private SeatManager() {}

    public static boolean isOccupied(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(0.5D);
        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, box,
                e -> pos.equals(e.getSeatPos()) && !e.getPassengers().isEmpty());
        return !seats.isEmpty();
    }

    public static boolean trySit(Level level, BlockPos pos, Player player, double yOffset, float yaw) {
        if (level.isClientSide) return true;
        if (player.isPassenger()) return false;
        if (isOccupied(level, pos)) return false;

        SeatEntity seat = new SeatEntity(level, pos, yOffset);
        seat.setYRot(yaw);
        seat.setYHeadRot(yaw);
        ((ServerLevel) level).addFreshEntity(seat);
        player.startRiding(seat, true);
        return true;
    }

    public static void ejectAt(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        AABB box = new AABB(pos).inflate(0.5D);
        for (SeatEntity seat : level.getEntitiesOfClass(SeatEntity.class, box,
                e -> pos.equals(e.getSeatPos()))) {
            seat.ejectPassengers();
            seat.discard();
        }
    }
}
