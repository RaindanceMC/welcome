package de.raindancer.modules.welcome;

import de.raindancer.core.ui.help.HelpLint;
import de.raindancer.modules.api.ModuleCommand;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GuideTest {

    @Test
    void theGuideIsComplete() {
        assertThat(HelpLint.of(Path.of("src/main/resources/rains-help"))
                .module("welcome")
                .settings(WelcomeSettings.class, WelcomeSettings.DEFAULTS)
                .commands(new WelcomeModule().commands().stream().map(ModuleCommand::name).toList())
                .problems()).isEmpty();
    }
}
