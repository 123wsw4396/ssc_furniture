package ly.ssc_furniture.client.anim;

import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HangState {

    private static final Set<UUID> hangingPlayers = new HashSet<>();

    public static void setHanging(Player player, boolean hanging) {
        if (player == null) return;
        UUID id = player.getUUID();
        if (hanging) hangingPlayers.add(id);
        else hangingPlayers.remove(id);
    }

    public static boolean isHanging(Player player) {
        if (player == null) return false;
        return hangingPlayers.contains(player.getUUID());
    }
}
