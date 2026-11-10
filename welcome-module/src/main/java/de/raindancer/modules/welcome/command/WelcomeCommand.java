package de.raindancer.modules.welcome.command;

import de.raindancer.modules.welcome.WelcomeServices;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** {@code /welcome preview}: the welcome as a newcomer gets it; {@code status}: what this server does with arrivals. */
public final class WelcomeCommand implements BasicCommand {

    private static final List<String> OPTIONS = List.of("preview", "status");

    private final Supplier<WelcomeServices> services;

    public WelcomeCommand(Supplier<WelcomeServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        WelcomeServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            live.messages().send(source.getSender(), "welcome.only-a-player");
            return;
        }
        String option = args.length == 0 ? "preview" : args[0].toLowerCase(Locale.ROOT);
        switch (option) {
            case "preview" -> live.welcome().greet(player);
            case "status" -> live.messages().send(player, switch (live.welcome().verdict(player)) {
                case WELCOME -> "welcome.status-welcome";
                case STAY -> "welcome.status-stay";
                case OFF -> "welcome.status-off";
            });
            default -> live.messages().send(player, "welcome.usage");
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return args.length <= 1 ? OPTIONS.stream().filter(option -> option.startsWith(typed)).toList() : List.of();
    }
}
