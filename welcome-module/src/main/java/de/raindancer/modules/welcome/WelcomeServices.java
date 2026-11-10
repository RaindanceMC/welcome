package de.raindancer.modules.welcome;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.welcome.service.IntroService;
import de.raindancer.modules.welcome.service.TutorialService;
import de.raindancer.modules.welcome.service.WelcomeService;
import de.raindancer.modules.welcome.tutorial.IslandBuilder;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * What this module built, handed to its commands. On a network server only /trailer works: everything else is null
 * there, and {@code toWelcomeServer} sends a player over.
 */
public record WelcomeServices(Messages messages, WelcomeService welcome, IntroService intro, TutorialService tutorial,
                              IslandBuilder island, Consumer<Player> toWelcomeServer) {

    public boolean onWelcomeServer() {
        return welcome != null;
    }
}
