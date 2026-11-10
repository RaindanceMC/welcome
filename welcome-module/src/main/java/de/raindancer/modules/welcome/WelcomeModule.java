package de.raindancer.modules.welcome;

import de.raindancer.core.content.music.MusicPack;
import de.raindancer.core.content.music.Track;
import de.raindancer.core.data.settings.SettingsStore;
import de.raindancer.core.ui.camera.Cutscenes;
import de.raindancer.core.platform.proxy.ProxyTransfer;
import de.raindancer.modules.api.FlexModule;
import de.raindancer.modules.api.ModuleCommand;
import de.raindancer.modules.api.ModuleContext;
import de.raindancer.modules.api.ModuleInfo;
import de.raindancer.modules.welcome.listener.TutorialListener;
import de.raindancer.modules.welcome.listener.WelcomeListener;
import de.raindancer.modules.welcome.service.TutorialService;
import de.raindancer.modules.welcome.store.TutorialPlaces;
import de.raindancer.modules.welcome.tutorial.IslandBuilder;
import de.raindancer.modules.welcome.rules.Role;
import de.raindancer.modules.welcome.service.IntroService;
import de.raindancer.modules.welcome.store.IntroShots;
import de.raindancer.modules.welcome.service.WelcomeService;
import de.raindancer.modules.welcome.util.PermissionNodes;

import java.io.IOException;
import java.util.List;

/**
 * The welcome server: newcomers — sent here by the proxy because the network has never seen them — watch the intro
 * (a camera flight through the shots staff recorded, with the welcome music), are greeted and sent on to the
 * network when they are done, or at once with /skip. Runs on a database of its own, so
 * whatever they try here stays here.
 */
public final class WelcomeModule implements FlexModule {

    private static final ModuleInfo INFO = ModuleInfo.of("welcome", "Welcome", "0.3.0")
            .describedAs("Greets players new to the network and sends them on")
            .by("Raindancer118");

    @Override
    public ModuleInfo info() {
        return INFO;
    }

    @Override
    public void enable(ModuleContext context) {
        SettingsStore<WelcomeSettings> settings = context.settings(WelcomeSettings.class, WelcomeSettings.DEFAULTS);
        context.core().messages().defineFrom(WelcomeModule.class.getResourceAsStream("messages.yml"),
                context.chat().brand()::chatPrefix);
        context.core().messages().seriousFrom(WelcomeModule.class.getResourceAsStream("messages-serious.yml"));
        PermissionNodes.register(context.plugin().getServer());
        ProxyTransfer.register(context.plugin());
        java.util.function.Consumer<org.bukkit.entity.Player> toWelcomeServer = player -> ProxyTransfer.send(
                context.plugin(), player, settings.current().welcomeServer().strip());
        if (Role.of(settings.current().role()) == Role.NETWORK) {
            WelcomeCommands.ready(new WelcomeServices(context.core().messages(), null, null, null, null, toWelcomeServer));
            context.log().info("Welcome on a network server: /trailer sends players to {}.",
                    settings.current().welcomeServer());
            return;
        }

        WelcomeService welcome = new WelcomeService(context.plugin(), settings::current, context.core().messages(),
                System::currentTimeMillis);
        List<Track> tracks = music(context);
        Cutscenes cutscenes = new Cutscenes(context.plugin());
        IntroService intro = new IntroService(context.plugin(), cutscenes,
                IntroShots.at(context.plugin().getDataFolder().toPath().resolve("intro.yml")), settings::current,
                context.core().messages(), context.core().actionBars(), context.core().resourcePacks(), () -> tracks,
                System::currentTimeMillis);
        context.closeWith(intro::close);
        welcome.intro(intro);
        IslandBuilder island = new IslandBuilder(context.plugin(), player -> player.hasPermission(PermissionNodes.STAY));
        island.loadIfThere();
        TutorialService tutorial = new TutorialService(context.plugin(),
                TutorialPlaces.at(context.plugin().getDataFolder().toPath().resolve("tutorial.yml")), settings::current,
                context.core().messages(), context.core().actionBars(), welcome);
        context.closeWith(tutorial::close);
        welcome.tutorial(tutorial);
        context.listener(new WelcomeListener(welcome));
        context.listener(new TutorialListener(tutorial));
        WelcomeCommands.ready(new WelcomeServices(context.core().messages(), welcome, intro, tutorial, island,
                toWelcomeServer));
        WelcomeSettings now = settings.current();
        if (!now.enabled()) {
            context.log().info("Welcome is up but switched off: arrivals just stand here.");
        } else {
            context.log().info("Welcome is up: newcomers are greeted and sent on to {}.", now.nextServer());
            context.log().info(intro.wouldPlay() ? "Intro: {} shot(s), {} track(s) of music."
                    : "Intro: {} shot(s) — record at least two with /intro shot add; {} track(s) of music.",
                    intro.shots().all().size(), tracks.size());
            context.log().info(tutorial.ready() ? "Tutorial: {} station(s) placed."
                    : "Tutorial: {} station(s) placed, not ready — /tutorial island generate, or /tutorial start set "
                    + "and /tutorial station set <id>.", tutorial.places().stations().size());
        }
    }

    /** plugins/RainsWelcome/music, offered to the server's resource pack. */
    private static List<Track> music(ModuleContext context) {
        try {
            MusicPack pack = MusicPack.offer(context.plugin(), context.core().resourcePacks(), "Welcome", "rainswelcome",
                    "Rain's Welcome — music");
            context.closeWith(pack::withdraw);
            return pack.tracks();
        } catch (IOException failure) {
            context.log().warn("The welcome music could not be read ({}); the intro plays the game's menu music.",
                    failure.getMessage());
            return List.of();
        }
    }

    @Override
    public List<ModuleCommand> commands() {
        return WelcomeCommands.declared();
    }

    @Override
    public void disable() {
        WelcomeCommands.stopped();
    }
}
