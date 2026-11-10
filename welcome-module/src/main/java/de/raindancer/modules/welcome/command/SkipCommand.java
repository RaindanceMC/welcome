package de.raindancer.modules.welcome.command;

import de.raindancer.modules.welcome.WelcomeServices;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/** {@code /skip}: straight on to the network, from anywhere on the welcome server. */
public final class SkipCommand implements BasicCommand {

    private final Supplier<WelcomeServices> services;

    public SkipCommand(Supplier<WelcomeServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        WelcomeServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            live.messages().send(source.getSender(), "welcome.only-a-player");
            return;
        }
        live.welcome().sendOn(player);
    }
}
