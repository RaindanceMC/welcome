package de.raindancer.modules.welcome.service;

import de.raindancer.core.platform.proxy.ProxyTransfer;
import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.ui.dialog.DialogPage;
import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.welcome.WelcomeSettings;
import de.raindancer.modules.welcome.rules.ArrivalRule;
import de.raindancer.modules.welcome.rules.ArrivalRule.Verdict;
import de.raindancer.modules.welcome.util.PermissionNodes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The welcome server's side of a newcomer's first minutes: the intro, greeted, then sent on to the network
 * through the proxy when they choose to go — or at once with /skip. The proxy sends only players the network has
 * never seen here; this server runs on a database of its own, so nothing done here follows them.
 */
public final class WelcomeService {

    /** How long a send may take before they are told the network did not take them. */
    private static final int PATIENCE_SECONDS = 6;

    private final Plugin plugin;
    private final Supplier<WelcomeSettings> settings;
    private final Messages messages;
    private final LongSupplier clock;
    private final Map<UUID, Long> sentAt = new ConcurrentHashMap<>();
    private volatile IntroService intro;

    public WelcomeService(Plugin plugin, Supplier<WelcomeSettings> settings, Messages messages, LongSupplier clock) {
        this.plugin = plugin;
        this.settings = settings;
        this.messages = messages;
        this.clock = clock;
    }

    /** The intro newcomers watch first, if any. */
    public void intro(IntroService intro) {
        this.intro = intro;
    }

    public Verdict verdict(Player player) {
        WelcomeSettings s = settings.get();
        return ArrivalRule.decide(s.enabled(), s.nextServer(), player.hasPermission(PermissionNodes.STAY));
    }

    public boolean protects(Player player) {
        return settings.get().protect() && verdict(player) == Verdict.WELCOME;
    }

    public void arrived(Player player) {
        if (verdict(player) != Verdict.WELCOME) {
            return;
        }
        IntroService first = intro;
        if (first == null) {
            greet(player);
        } else {
            first.await(player, () -> greet(player));
        }
    }

    /** The title, then the welcome window once the world has loaded behind it. */
    public void greet(Player player) {
        player.showTitle(Title.title(messages.get("welcome.title"), messages.get("welcome.subtitle"),
                Title.Times.times(Duration.ofMillis(800), Duration.ofSeconds(3), Duration.ofMillis(800))));
        long delay = Math.max(1, settings.get().offerAfterSeconds() * 20L);
        Scheduling.entityLater(plugin, player, delay, () -> offer(player));
    }

    private void offer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        DialogPage.titled(plain("welcome.window-title"))
                .text(mini("welcome.window-text"))
                .button(plain("welcome.button-go"), plain("welcome.button-go-hint"), answers -> sendOn(player))
                .show(player);
        messages.send(player, "welcome.chat-hint");
    }

    /** To the network, through the proxy. Safe to call again; a second press while on the way does nothing new. */
    public void sendOn(Player player) {
        String next = settings.get().nextServer();
        if (next == null || next.isBlank()) {
            messages.send(player, "welcome.nowhere");
            return;
        }
        long now = clock.getAsLong();
        sentAt.put(player.getUniqueId(), now);
        messages.send(player, "welcome.going");
        ProxyTransfer.send(plugin, player, next.strip());
        Scheduling.entityLater(plugin, player, PATIENCE_SECONDS * 20L + 5, () -> {
            Long at = sentAt.get(player.getUniqueId());
            if (player.isOnline() && at != null && ArrivalRule.stuck(at, clock.getAsLong(), PATIENCE_SECONDS)) {
                sentAt.remove(player.getUniqueId());
                messages.send(player, "welcome.not-moved");
            }
        });
    }

    public void left(Player player) {
        sentAt.remove(player.getUniqueId());
    }

    private String plain(String key) {
        return PlainTextComponentSerializer.plainText().serialize(messages.get(key));
    }

    private String mini(String key) {
        return messages.raw(key);
    }
}
