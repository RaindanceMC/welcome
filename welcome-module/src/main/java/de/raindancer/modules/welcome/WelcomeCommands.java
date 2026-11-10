package de.raindancer.modules.welcome;

import de.raindancer.modules.api.ModuleCommand;
import de.raindancer.modules.welcome.command.IntroCommand;
import de.raindancer.modules.welcome.command.SkipCommand;
import de.raindancer.modules.welcome.command.TrailerCommand;
import de.raindancer.modules.welcome.command.TutorialCommand;
import de.raindancer.modules.welcome.command.WelcomeCommand;
import de.raindancer.modules.welcome.util.PermissionNodes;

import java.util.List;

/**
 * Declared at bootstrap, filled in when the module starts — Paper registers commands before any
 * plugin is enabled, so the commands point at {@link #require} until then.
 */
public final class WelcomeCommands {

    private static volatile WelcomeServices services;

    private WelcomeCommands() {
    }

    public static List<ModuleCommand> declared() {
        return List.of(
                ModuleCommand.of("trailer", "Shows the Hyades trailer — the intro newcomers see",
                        new TrailerCommand(WelcomeCommands::require)),
                ModuleCommand.of("tutorial", "Starts the tour of the island with your guide",
                        new TutorialCommand(WelcomeCommands::require))
                        .taking("(nothing) — start it", "next — the next station", "stop",
                                "start set | station set|remove|list <id> | island generate — staff"),
                ModuleCommand.of("skip", "Skips the welcome and takes you on to the network",
                        new SkipCommand(WelcomeCommands::require)).aliased("continue"),
                ModuleCommand.of("welcome", "Shows the welcome as a newcomer gets it",
                        new WelcomeCommand(WelcomeCommands::require))
                        .taking("preview — the welcome as a newcomer gets it", "status — what happens to you here")
                        .needing(PermissionNodes.ADMIN),
                ModuleCommand.of("intro", "Records the intro's shots, or plays it",
                        new IntroCommand(WelcomeCommands::require))
                        .taking("shot add <caption> — where you stand and look, \"Title | subtitle\"",
                                "shot list | remove <n> | move <from> <to>", "play — watch it")
                        .needing(PermissionNodes.ADMIN));
    }

    static void ready(WelcomeServices live) {
        services = live;
    }

    static void stopped() {
        services = null;
    }

    private static WelcomeServices require() {
        WelcomeServices live = services;
        if (live == null) {
            throw new IllegalStateException("the welcome module is not running");
        }
        return live;
    }
}
