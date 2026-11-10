package de.raindancer.modules.welcome.service;

import de.raindancer.core.content.music.MusicSession;
import de.raindancer.core.content.music.Track;
import de.raindancer.core.content.pack.ResourcePacks;
import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.ui.actionbar.ActionBarPriority;
import de.raindancer.core.ui.actionbar.ActionBars;
import de.raindancer.core.ui.camera.CameraPath;
import de.raindancer.core.ui.camera.Cutscenes;
import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.welcome.WelcomeSettings;
import de.raindancer.modules.welcome.model.Caption;
import de.raindancer.modules.welcome.model.Shot;
import de.raindancer.modules.welcome.rules.IntroRule;
import de.raindancer.modules.welcome.store.IntroShots;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The newcomer's intro: once their resource pack (which carries the music) has settled, the camera glides through
 * the recorded shots with each caption as a title and the welcome music playing, and they are back where they
 * stood when it ends or they skip it by sneaking.
 */
public final class IntroService {

    private static final String OWNER = "welcome";

    private final Plugin plugin;
    private final Cutscenes cutscenes;
    private final IntroShots shots;
    private final Supplier<WelcomeSettings> settings;
    private final Messages messages;
    private final ActionBars bars;
    private final ResourcePacks packs;
    private final Supplier<List<Track>> tracks;
    private final LongSupplier clock;

    public IntroService(Plugin plugin, Cutscenes cutscenes, IntroShots shots, Supplier<WelcomeSettings> settings,
                        Messages messages, ActionBars bars, ResourcePacks packs, Supplier<List<Track>> tracks,
                        LongSupplier clock) {
        this.plugin = plugin;
        this.cutscenes = cutscenes;
        this.shots = shots;
        this.settings = settings;
        this.messages = messages;
        this.bars = bars;
        this.packs = packs;
        this.tracks = tracks;
        this.clock = clock;
    }

    public IntroShots shots() {
        return shots;
    }

    public boolean wouldPlay() {
        return IntroRule.plays(settings.get().introEnabled(), Shot.usable(shots.all()));
    }

    public boolean playing(Player player) {
        return cutscenes.playing(player.getUniqueId());
    }

    /** Waits for their pack to settle, then plays; {@code after} runs when it is over (or at once if none plays). */
    public void await(Player player, Runnable after) {
        if (!wouldPlay()) {
            after.run();
            return;
        }
        long arrived = clock.getAsLong();
        Scheduling.entityTimer(plugin, player, 10, 10, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            double waited = (clock.getAsLong() - arrived) / 1000.0;
            if (IntroRule.ready(packs.statusOf(player.getUniqueId()), packs.current().isPresent(), waited)) {
                task.cancel();
                play(player, after);
            }
        }, () -> {
        });
    }

    /** At once, pack or not — staff previewing it. */
    public void play(Player player, Runnable after) {
        List<Shot> recorded = shots.all();
        World world = Shot.world(recorded).map(plugin.getServer()::getWorld).orElse(null);
        if (world == null || !IntroRule.plays(true, Shot.usable(recorded))) {
            messages.send(player, "welcome.intro-none");
            after.run();
            return;
        }
        CameraPath path = Shot.path(recorded, settings.get().secondsPerShot());
        Location from = player.getLocation();
        MusicSession music = new MusicSession(player, player, tracks.get(), packs.isWearing(player.getUniqueId()),
                settings.get().music().equalsIgnoreCase("custom") ? "minecraft:music.menu" : settings.get().music(),
                0.8f, clock);
        boolean started = cutscenes.play(player, world, path, this::caption, end -> {
            music.stop();
            bars.clear(player.getUniqueId(), OWNER);
            player.clearTitle();
            player.teleportAsync(from).thenRun(() -> Scheduling.entity(plugin, player, after));
        });
        if (!started) {
            music.stop();
            return;
        }
        String credit = settings.get().musicCredit() == null ? "" : settings.get().musicCredit().strip();
        bars.show(player.getUniqueId(), OWNER, credit.isEmpty() ? messages.get("welcome.intro-skip")
                        : messages.get("welcome.intro-skip-credit", "credit", credit),
                ActionBars.UNTIL_CLEARED, ActionBarPriority.LOW);
        Scheduling.entityTimer(plugin, player, 20, 20, task -> {
            if (!player.isOnline() || !cutscenes.playing(player.getUniqueId())) {
                task.cancel();
                return;
            }
            music.tick();
        }, () -> {
        });
    }

    /** A shot's caption as title and subtitle ({@link Caption}). */
    private void caption(Player player, CameraPath.Key key) {
        Caption caption = Caption.of(key.caption());
        Component title = caption.title().isEmpty() ? Component.empty()
                : MiniMessage.miniMessage().deserialize(messages.raw("welcome.intro-caption")
                        .replace("<caption>", MiniMessage.miniMessage().escapeTags(caption.title())));
        Component subtitle = caption.subtitle().isEmpty() ? Component.empty()
                : MiniMessage.miniMessage().deserialize(messages.raw("welcome.intro-subcaption")
                        .replace("<caption>", MiniMessage.miniMessage().escapeTags(caption.subtitle())));
        player.showTitle(Title.title(title, subtitle,
                Title.Times.times(Duration.ofMillis(700), Duration.ofSeconds(3), Duration.ofMillis(900))));
    }

    public void close() {
        cutscenes.close();
    }
}
