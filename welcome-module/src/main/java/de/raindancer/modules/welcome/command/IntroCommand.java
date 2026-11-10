package de.raindancer.modules.welcome.command;

import de.raindancer.modules.welcome.WelcomeServices;
import de.raindancer.modules.welcome.model.Shot;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * {@code /intro shot add <caption>} records where you stand and look as the next shot ("Title | subtitle" for
 * both); {@code shot list}, {@code shot remove <n>}, {@code shot move <from> <to>}; {@code play} watches it.
 */
public final class IntroCommand implements BasicCommand {

    private final Supplier<WelcomeServices> services;

    public IntroCommand(Supplier<WelcomeServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        WelcomeServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            live.messages().send(source.getSender(), "welcome.only-a-player");
            return;
        }
        String first = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if (first.equals("play")) {
            live.intro().play(player, () -> live.messages().send(player, "welcome.intro-over"));
            return;
        }
        if (!first.equals("shot") || args.length < 2) {
            live.messages().send(player, "welcome.intro-usage");
            return;
        }
        var shots = live.intro().shots();
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "add" -> {
                Location at = player.getEyeLocation();
                String caption = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).strip();
                shots.add(new Shot(at.getWorld().getName(), at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch(),
                        caption));
                live.messages().send(player, "welcome.intro-added", "number", shots.all().size());
            }
            case "list" -> {
                List<Shot> all = shots.all();
                if (all.isEmpty()) {
                    live.messages().send(player, "welcome.intro-empty");
                }
                for (int i = 0; i < all.size(); i++) {
                    Shot shot = all.get(i);
                    live.messages().send(player, "welcome.intro-line", "number", i + 1, "world", shot.world(),
                            "x", Math.round(shot.x()), "y", Math.round(shot.y()), "z", Math.round(shot.z()),
                            "caption", shot.caption().isBlank() ? "—" : shot.caption());
                }
            }
            case "remove" -> live.messages().send(player,
                    args.length > 2 && shots.remove(number(args[2])) ? "welcome.intro-removed" : "welcome.intro-no-such");
            case "move" -> live.messages().send(player,
                    args.length > 3 && shots.move(number(args[2]), number(args[3])) ? "welcome.intro-moved"
                            : "welcome.intro-no-such");
            default -> live.messages().send(player, "welcome.intro-usage");
        }
    }

    private static int number(String text) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException notOne) {
            return -1;
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("shot", "play");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("shot")) {
            return List.of("add", "list", "remove", "move");
        }
        return List.of();
    }
}
