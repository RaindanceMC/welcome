package de.raindancer.modules.welcome.command;

import de.raindancer.core.platform.proxy.Handoff;
import de.raindancer.modules.welcome.WelcomeServices;
import de.raindancer.modules.welcome.rules.ArrivalRule;
import de.raindancer.modules.welcome.service.WelcomeService;
import de.raindancer.modules.welcome.store.TutorialPlaces.Place;
import de.raindancer.modules.welcome.tutorial.Station;
import de.raindancer.modules.welcome.tutorial.Stations;
import de.raindancer.modules.welcome.util.PermissionNodes;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * {@code /tutorial}: starts it — on a network server it sends the player to the welcome server, straight into the tour; {@code next} skips a station; {@code stop}. Staff: {@code start set},
 * {@code station set|remove <id>}, {@code station list}, {@code island generate}.
 */
public final class TutorialCommand implements BasicCommand {

    private final Supplier<WelcomeServices> services;

    public TutorialCommand(Supplier<WelcomeServices> services) {
        this.services = services;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        WelcomeServices live = services.get();
        if (!(source.getSender() instanceof Player player)) {
            // The island needs nobody standing anywhere, so the console can build it before anyone joins.
            if (live.onWelcomeServer() && args.length == 2 && args[0].equalsIgnoreCase("island")
                    && args[1].equalsIgnoreCase("generate")) {
                var console = source.getSender();
                live.messages().send(console, "welcome.tutorial-island-building");
                live.island().build(live.tutorial().places(),
                        () -> live.messages().send(console, "welcome.tutorial-island-built"));
            } else {
                live.messages().send(source.getSender(), "welcome.only-a-player");
            }
            return;
        }
        if (!live.onWelcomeServer()) {
            if (args.length == 0) {
                Handoff.leave(player, WelcomeService.CAME_FOR, ArrivalRule.CAME_FOR_THE_TOUR);
                live.messages().send(player, "welcome.tutorial-going");
                live.toWelcomeServer().accept(player);
            } else {
                live.messages().send(player, "welcome.only-on-welcome");
            }
            return;
        }
        String first = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (first) {
            case "" -> live.tutorial().start(player);
            case "next" -> live.tutorial().skipStep(player);
            case "stop" -> live.tutorial().stop(player, true);
            case "start", "station", "island" -> staff(live, player, args);
            default -> live.messages().send(player, "welcome.tutorial-usage");
        }
    }

    private static void staff(WelcomeServices live, Player player, String[] args) {
        if (!player.hasPermission(PermissionNodes.ADMIN)) {
            live.messages().send(player, "welcome.tutorial-usage");
            return;
        }
        var places = live.tutorial().places();
        String what = args[0].toLowerCase(Locale.ROOT);
        String how = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "";
        if (what.equals("start") && how.equals("set")) {
            places.start(Place.of(player.getLocation()));
            live.messages().send(player, "welcome.tutorial-start-set");
        } else if (what.equals("island") && how.equals("generate")) {
            live.messages().send(player, "welcome.tutorial-island-building");
            live.island().build(places, () -> live.messages().send(player, "welcome.tutorial-island-built"));
        } else if (what.equals("station") && how.equals("list")) {
            for (Station station : Stations.ALL) {
                Place place = places.stations().get(station.id());
                live.messages().send(player, place == null ? "welcome.tutorial-station-unset" : "welcome.tutorial-station-line",
                        "id", station.id(), "x", place == null ? 0 : Math.round(place.x()),
                        "y", place == null ? 0 : Math.round(place.y()), "z", place == null ? 0 : Math.round(place.z()));
            }
        } else if (what.equals("station") && (how.equals("set") || how.equals("remove")) && args.length > 2) {
            var station = Stations.byId(args[2]);
            if (station.isEmpty()) {
                live.messages().send(player, "welcome.tutorial-no-such", "ids",
                        String.join(", ", Stations.ALL.stream().map(Station::id).toList()));
            } else if (how.equals("set")) {
                places.station(station.get().id(), Place.of(player.getLocation()));
                live.messages().send(player, "welcome.tutorial-station-set", "id", station.get().id());
            } else {
                live.messages().send(player, places.removeStation(station.get().id())
                        ? "welcome.tutorial-station-removed" : "welcome.tutorial-no-such",
                        "id", station.get().id(), "ids", "");
            }
        } else {
            live.messages().send(player, "welcome.tutorial-usage");
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("next", "stop", "start", "station", "island");
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "start" -> List.of("set");
                case "station" -> List.of("set", "remove", "list");
                case "island" -> List.of("generate");
                default -> List.of();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("station")) {
            return Stations.ALL.stream().map(Station::id).toList();
        }
        return List.of();
    }
}
