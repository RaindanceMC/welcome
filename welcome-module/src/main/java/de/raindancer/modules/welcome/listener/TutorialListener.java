package de.raindancer.modules.welcome.listener;

import de.raindancer.modules.welcome.service.TutorialService;
import de.raindancer.modules.welcome.tutorial.IslandBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** The commands a newcomer tries at the stations, and their guide gone when they leave. */
public final class TutorialListener implements Listener {

    private final TutorialService tutorial;

    public TutorialListener(TutorialService tutorial) {
        this.tutorial = tutorial;
    }

    /** Seen whether or not the command exists here: trying it is what counts. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        tutorial.typed(event.getPlayer(), event.getMessage());
    }

    /**
     * The island's world is empty past the sea round it: whoever swims out or falls off is put back at the start
     * rather than falling for ever — newcomers take no damage here, so the void would never end it.
     */
    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getY() < IslandBuilder.VOID_BELOW
                && event.getTo().getWorld().getName().equals(IslandBuilder.WORLD)) {
            tutorial.rescue(event.getPlayer());
        }
    }

    /** Sent there, too: a command or a plugin putting somebody below the island. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo().getY() < IslandBuilder.VOID_BELOW
                && event.getTo().getWorld().getName().equals(IslandBuilder.WORLD)) {
            de.raindancer.core.platform.util.Scheduling.entityLater(tutorial.plugin(), event.getPlayer(), 2,
                    () -> tutorial.rescue(event.getPlayer()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tutorial.stop(event.getPlayer(), false);
    }
}
