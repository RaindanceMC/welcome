package de.raindancer.modules.welcome.command;

import de.raindancer.modules.welcome.WelcomeServices;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/** {@code /trailer}: the intro again — here on the welcome server, or by going over to it from the network. */
public final class TrailerCommand implements BasicCommand {

    private final Supplier<WelcomeServices> services;

    public TrailerCommand(Supplier<WelcomeServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        WelcomeServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            live.messages().send(source.getSender(), "welcome.only-a-player");
            return;
        }
        if (!live.onWelcomeServer()) {
            live.messages().send(player, "welcome.trailer-going");
            live.toWelcomeServer().accept(player);
            return;
        }
        if (live.intro().playing(player)) {
            return;
        }
        if (!live.intro().wouldPlay()) {
            live.messages().send(player, "welcome.trailer-none");
            return;
        }
        live.intro().play(player, () -> live.messages().send(player, "welcome.trailer-over"));
    }
}
