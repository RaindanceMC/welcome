package de.raindancer.modules.welcome.service;

import de.raindancer.modules.welcome.rules.WaitingSky;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Players the proxy parks here while they queue for a full server: a spectator's view (no hotbar, no hearts) high
 * over the island at midnight, alone, free to look around but not to move. The proxy draws their place in line over
 * it as a title, and moves them on by itself.
 */
final class QueueSky {

    private record Parked(GameMode before, Location spot, Location ground) {
    }

    private final Plugin plugin;
    private final Map<UUID, Parked> parked = new ConcurrentHashMap<>();

    QueueSky(Plugin plugin) {
        this.plugin = plugin;
    }

    /** On the player's own thread. */
    void park(Player player) {
        World world = player.getWorld();
        Location ground = world.getSpawnLocation();
        Location spot = new Location(world, ground.getX(), WaitingSky.height(ground.getBlockY(), world.getMaxHeight()),
                ground.getZ(), ground.getYaw(), WaitingSky.PITCH);
        parked.putIfAbsent(player.getUniqueId(), new Parked(player.getGameMode(), spot, ground));
        player.setGameMode(GameMode.SPECTATOR);
        player.setPlayerTime(WaitingSky.TIME, false);
        player.setPlayerWeather(WeatherType.CLEAR);
        player.teleportAsync(spot);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player);
            }
        }
    }

    /** Whoever arrives does not see the waiting. */
    void hideFrom(Player arriving) {
        for (UUID waiting : parked.keySet()) {
            Player other = Bukkit.getPlayer(waiting);
            if (other != null && !other.equals(arriving)) {
                arriving.hidePlayer(plugin, other);
            }
        }
    }

    Optional<Location> spot(Player player) {
        return Optional.ofNullable(parked.get(player.getUniqueId())).map(Parked::spot);
    }

    /** Back to normal — leaving, or starting the tour while they wait. */
    void release(Player player) {
        Parked was = parked.remove(player.getUniqueId());
        if (was == null) {
            return;
        }
        player.setGameMode(was.before() == GameMode.SPECTATOR ? Bukkit.getDefaultGameMode() : was.before());
        player.resetPlayerTime();
        player.resetPlayerWeather();
        // Saved where they stand: without this their next visit would start in the sky.
        player.teleportAsync(was.ground());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            }
        }
    }
}
