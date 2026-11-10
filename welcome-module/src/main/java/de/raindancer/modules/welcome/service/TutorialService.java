package de.raindancer.modules.welcome.service;

import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.ui.actionbar.ActionBarPriority;
import de.raindancer.core.ui.actionbar.ActionBars;
import de.raindancer.core.ui.dialog.DialogPage;
import de.raindancer.core.ui.messages.Messages;
import de.raindancer.core.world.npc.Attendants;
import de.raindancer.core.world.npc.Guide;
import de.raindancer.modules.welcome.WelcomeSettings;
import de.raindancer.modules.welcome.store.TutorialPlaces;
import de.raindancer.modules.welcome.tutorial.Station;
import de.raindancer.modules.welcome.tutorial.Stations;
import de.raindancer.modules.welcome.tutorial.TutorialRun;
import de.raindancer.modules.welcome.tutorial.TutorialRun.Phase;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The tutorial island: a newcomer's own guide walks them from station to station, says what each is about in a
 * speech bubble and in chat, and waits while they try it — /sethome at the homes station, /cosmetics at the
 * cosmetics one. Skippable step by step (/tutorial next) or all of it (/skip). Nothing they do here follows them:
 * the welcome server keeps its own records.
 */
public final class TutorialService {

    private static final String OWNER = "tutorial";
    private static final int SAY_SECONDS = 5;
    private static final double NEAR = 6;
    /** A dialog closed without a choice: how long until the tour goes on by itself. */
    private static final int CLOSED_SECONDS = 25;

    private final Plugin plugin;
    private final TutorialPlaces places;
    private final Supplier<WelcomeSettings> settings;
    private final Messages messages;
    private final ActionBars bars;
    private final WelcomeService welcome;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private static final class Session {
        final TutorialRun run;
        final Guide guide;
        int generation;

        Session(TutorialRun run, Guide guide) {
            this.run = run;
            this.guide = guide;
        }
    }

    public TutorialService(Plugin plugin, TutorialPlaces places, Supplier<WelcomeSettings> settings, Messages messages,
                           ActionBars bars, WelcomeService welcome) {
        this.plugin = plugin;
        this.places = places;
        this.settings = settings;
        this.messages = messages;
        this.bars = bars;
        this.welcome = welcome;
    }

    public Plugin plugin() {
        return plugin;
    }

    public TutorialPlaces places() {
        return places;
    }

    public boolean ready() {
        return settings.get().tutorialEnabled() && places.ready()
                && places.start().flatMap(place -> place.in(plugin.getServer())).isPresent();
    }

    public boolean running(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void start(Player player) {
        if (!ready()) {
            messages.send(player, "welcome.tutorial-not-ready");
            return;
        }
        stop(player, false);
        Location start = places.start().flatMap(place -> place.in(plugin.getServer())).orElseThrow();
        player.teleportAsync(start).thenRun(() -> Scheduling.entity(plugin, player, () -> {
            Location beside = start.clone().add(start.getDirection().setY(0).normalize().multiply(2.5));
            beside.setYaw(start.getYaw() + 180);
            Guide guide = Guide.spawn(plugin, player, beside, Attendants.look(settings.get().guideSkin()),
                    messages.get("welcome.tutorial-guide-name", "name", settings.get().guideName()));
            Session session = new Session(new TutorialRun(Stations.placed(places.stations().keySet())), guide);
            sessions.put(player.getUniqueId(), session);
            guide.say(messages.get("welcome.tutorial-hello-bubble"), SAY_SECONDS);
            hello(player, session);
        }));
    }

    /** Back to the start, off the void below the island. */
    public void rescue(Player player) {
        places.start().flatMap(place -> place.in(plugin.getServer())).ifPresent(start -> {
            player.setFallDistance(0);
            player.teleportAsync(start);
            messages.send(player, "welcome.tutorial-rescued");
        });
    }

    /** /tutorial next: on to the next station. */
    public void skipStep(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            messages.send(player, "welcome.tutorial-none");
            return;
        }
        session.run.skip();
        proceed(player, session);
    }

    /** A command they typed, maybe the one the station asks for. */
    public void typed(Player player, String line) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        Station station = session.run.station().orElse(null);
        if (station != null && session.run.typed(line)) {
            player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.MASTER, 0.6f,
                    1.4f));
            messages.send(player, "welcome.tutorial-done-step", "station", plain(station.key("title")));
            // Whatever the guide was about to say for that station is stale now.
            session.generation++;
            // After the command has run, so its own answer comes first.
            later(player, session, 40, () -> proceed(player, session));
        }
    }

    /** Ends theirs: {@code quietly} when they leave or are sent on. */
    public void stop(Player player, boolean tell) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.generation++;
        session.guide.remove();
        bars.clear(player.getUniqueId(), OWNER);
        if (tell) {
            messages.send(player, "welcome.tutorial-stopped");
        }
    }

    public void close() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            stop(player, false);
        }
    }

    private void proceed(Player player, Session session) {
        if (sessions.get(player.getUniqueId()) != session || !player.isOnline()) {
            return;
        }
        int generation = ++session.generation;
        TutorialRun run = session.run;
        if (run.phase() == Phase.FINISHED) {
            finish(player, session);
            return;
        }
        Station station = run.station().orElseThrow();
        Location place = places.stations().get(station.id()).in(plugin.getServer()).orElse(null);
        if (place == null) {
            run.skip();
            proceed(player, session);
            return;
        }
        bars.show(player.getUniqueId(), OWNER, messages.get("welcome.tutorial-bar", "number", run.number(),
                "total", run.total(), "station", plain(station.key("title"))), ActionBars.UNTIL_CLEARED,
                ActionBarPriority.LOW);
        switch (run.phase()) {
            case WALK -> {
                session.guide.say(messages.get("welcome.tutorial-follow", "station", plain(station.key("title"))),
                        SAY_SECONDS);
                session.guide.walkTo(place, () -> waitForPlayer(player, session, generation, place));
            }
            case EXPLAIN -> explain(player, session, generation, station);
            case TRY -> {
                session.guide.say(messages.get(station.key("hint")), 60);
                bars.show(player.getUniqueId(), OWNER, messages.get("welcome.tutorial-try", "hint",
                        plain(station.key("hint"))), ActionBars.UNTIL_CLEARED, ActionBarPriority.LOW);
            }
            default -> {
            }
        }
    }

    /** The guide is there; it starts talking once the player has caught up. */
    private void waitForPlayer(Player player, Session session, int generation, Location place) {
        Scheduling.entityTimer(plugin, player, 5, 10, task -> {
            if (session.generation != generation || sessions.get(player.getUniqueId()) != session) {
                task.cancel();
                return;
            }
            if (player.getWorld() == place.getWorld() && player.getLocation().distance(place) <= NEAR) {
                task.cancel();
                session.run.arrived();
                proceed(player, session);
            }
        }, () -> {
        });
    }

    /** The guide introduces themself in a dialog; "Let's go" (or closing it and a moment) starts the walk. */
    private void hello(Player player, Session session) {
        String name = settings.get().guideName();
        DialogPage.titled(plain("welcome.tutorial-hello-title", "name", name))
                .text(messages.raw("welcome.tutorial-hello-text").replace("<name>", name))
                .button(plain("welcome.tutorial-button-go"), plain("welcome.tutorial-button-go-hint"),
                        answers -> Scheduling.entity(plugin, player, () -> proceed(player, session)))
                .button(plain("welcome.tutorial-button-end"), plain("welcome.tutorial-button-end-hint"),
                        answers -> Scheduling.entity(plugin, player, () -> stop(player, true)))
                .show(player);
        int generation = session.generation;
        later(player, session, CLOSED_SECONDS * 20, () -> {
            if (session.generation == generation) {
                proceed(player, session);
            }
        });
    }

    /**
     * What the station is about, in the game's own dialog: its icon, what the guide says, and what to try.
     * Closed without a choice, they get a moment to read and then the task anyway.
     */
    private void explain(Player player, Session session, int generation, Station station) {
        session.guide.say(messages.get("welcome.tutorial-here-bubble"), SAY_SECONDS);
        String name = settings.get().guideName();
        DialogPage.titled(plain(station.key("title")), plain("welcome.tutorial-stop-trail", "number",
                        session.run.number(), "total", session.run.total()))
                .item(() -> new ItemStack(station.icon()), messages.raw("welcome.tutorial-guide-line")
                        .replace("<name>", name))
                .text(messages.raw(station.key("say1")) + "\n\n" + messages.raw(station.key("say2")))
                .button(plain("welcome.tutorial-button-try"), plain(station.key("hint")),
                        answers -> Scheduling.entity(plugin, player, () -> explained(player, session, generation)))
                .button(plain("welcome.tutorial-button-next"), plain("welcome.tutorial-button-next-hint"),
                        answers -> Scheduling.entity(plugin, player, () -> {
                            if (session.generation == generation) {
                                skipStep(player);
                            }
                        }))
                .button(plain("welcome.tutorial-button-end"), plain("welcome.tutorial-button-end-hint"),
                        answers -> Scheduling.entity(plugin, player, () -> stop(player, true)))
                .show(player);
        later(player, session, CLOSED_SECONDS * 20, () -> explained(player, session, generation));
    }

    private void explained(Player player, Session session, int generation) {
        if (session.generation == generation && session.run.phase() == Phase.EXPLAIN
                && sessions.get(player.getUniqueId()) == session) {
            session.run.explained();
            proceed(player, session);
        }
    }

    private void finish(Player player, Session session) {
        session.guide.say(messages.get("welcome.tutorial-finished", "name", settings.get().guideName()), 30);
        bars.clear(player.getUniqueId(), OWNER);
        DialogPage.titled(plain("welcome.tutorial-finished-title"))
                .text(messages.raw("welcome.tutorial-finished-text"))
                .button(plain("welcome.button-go"), plain("welcome.button-go-hint"), answers -> {
                    stop(player, false);
                    welcome.sendOn(player);
                })
                .show(player);
    }

    private void later(Player player, Session session, long ticks, Runnable then) {
        int generation = session.generation;
        Scheduling.entityLater(plugin, player, ticks, () -> {
            if (sessions.get(player.getUniqueId()) == session && session.generation == generation) {
                then.run();
            }
        });
    }

    private String plain(String key, Object... values) {
        return PlainTextComponentSerializer.plainText().serialize(messages.get(key, values));
    }
}
