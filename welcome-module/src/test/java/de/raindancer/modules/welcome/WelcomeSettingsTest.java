package de.raindancer.modules.welcome;

import de.raindancer.core.world.npc.Skins;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomeSettingsTest {

    @Test
    void theGuideWearsTheExplorerSkinUnlessStaffChooseAnother() {
        assertThat(Skins.mineSkinId(WelcomeSettings.DEFAULTS.guideSkin()))
                .contains("12f8a17d9bed4948ba766f9bac3624ee");
    }
}
