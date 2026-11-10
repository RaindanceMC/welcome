package de.raindancer.modules.welcome.listener;

import de.raindancer.modules.welcome.service.WelcomeService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Arriving, leaving, and newcomers kept safe while they are here. */
public final class WelcomeListener implements Listener {

    private final WelcomeService welcome;

    public WelcomeListener(WelcomeService welcome) {
        this.welcome = welcome;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        welcome.arrived(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        welcome.left(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && welcome.protects(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && welcome.protects(player)
                && event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }
}
