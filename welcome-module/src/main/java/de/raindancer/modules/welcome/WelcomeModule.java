package de.raindancer.modules.welcome;

import de.raindancer.core.data.settings.SettingsStore;
import de.raindancer.core.platform.proxy.ProxyTransfer;
import de.raindancer.modules.api.FlexModule;
import de.raindancer.modules.api.ModuleCommand;
import de.raindancer.modules.api.ModuleContext;
import de.raindancer.modules.api.ModuleInfo;
import de.raindancer.modules.welcome.listener.WelcomeListener;
import de.raindancer.modules.welcome.service.WelcomeService;
import de.raindancer.modules.welcome.util.PermissionNodes;

import java.util.List;

/**
 * The welcome server: newcomers — sent here by the proxy because the network has never seen them — are greeted
 * and sent on to the network when they are done, or at once with /skip. Runs on a database of its own, so
 * whatever they try here stays here.
 */
public final class WelcomeModule implements FlexModule {

    private static final ModuleInfo INFO = ModuleInfo.of("welcome", "Welcome", "0.1.0")
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

        WelcomeService welcome = new WelcomeService(context.plugin(), settings::current, context.core().messages(),
                System::currentTimeMillis);
        context.listener(new WelcomeListener(welcome));
        WelcomeCommands.ready(new WelcomeServices(context.core().messages(), welcome));
        WelcomeSettings now = settings.current();
        if (!now.enabled()) {
            context.log().info("Welcome is up but switched off: arrivals just stand here.");
        } else {
            context.log().info("Welcome is up: newcomers are greeted and sent on to {}.", now.nextServer());
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
