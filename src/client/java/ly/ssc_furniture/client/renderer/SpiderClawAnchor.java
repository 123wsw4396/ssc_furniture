package ly.ssc_furniture.client.renderer;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SpiderClawAnchor {

    private static final long TTL_NANOS = 100_000_000L;

    private static final ConcurrentMap<UUID, Vec3> ANCHORS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, Long> LAST_UPDATE = new ConcurrentHashMap<>();

    private SpiderClawAnchor() {}

    public static void set(UUID playerId, Vec3 worldPos) {
        if (playerId == null || worldPos == null) return;
        ANCHORS.put(playerId, worldPos);
        LAST_UPDATE.put(playerId, System.nanoTime());
    }

    public static Vec3 get(UUID playerId) {
        if (playerId == null) return null;
        Long t = LAST_UPDATE.get(playerId);
        if (t == null) return null;
        if ((System.nanoTime() - t) > TTL_NANOS) return null;
        return ANCHORS.get(playerId);
    }
}
