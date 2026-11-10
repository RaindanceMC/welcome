package de.raindancer.modules.welcome.rules;

import de.raindancer.core.content.pack.PackStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("when a newcomer's intro plays")
class IntroRuleTest {

    @Test
    @DisplayName("on, and at least two shots recorded: it plays")
    void plays() {
        assertThat(IntroRule.plays(true, 2)).isTrue();
        assertThat(IntroRule.plays(true, 7)).isTrue();
    }

    @Test
    @DisplayName("off, or fewer than two shots — nothing to fly between: straight to the welcome")
    void skipped() {
        assertThat(IntroRule.plays(false, 5)).isFalse();
        assertThat(IntroRule.plays(true, 1)).isFalse();
        assertThat(IntroRule.plays(true, 0)).isFalse();
    }

    @Test
    @DisplayName("it waits for the resource pack, which carries the music: loaded, already had, declined or failed")
    void packSettled() {
        assertThat(IntroRule.ready(PackStatus.LOADED, true, 1)).isTrue();
        assertThat(IntroRule.ready(PackStatus.ALREADY_HAD_IT, true, 1)).isTrue();
        assertThat(IntroRule.ready(PackStatus.DECLINED, true, 1)).isTrue();
        assertThat(IntroRule.ready(PackStatus.FAILED, true, 1)).isTrue();
        assertThat(IntroRule.ready(PackStatus.SENT, true, 1)).isFalse();
        assertThat(IntroRule.ready(PackStatus.NOT_SENT, true, 1)).isFalse();
    }

    @Test
    @DisplayName("no pack on this server at all, or waited long enough: it starts anyway")
    void noPackOrPatience() {
        assertThat(IntroRule.ready(PackStatus.NOT_SENT, false, 1)).isTrue();
        assertThat(IntroRule.ready(PackStatus.SENT, true, IntroRule.PACK_PATIENCE_SECONDS)).isTrue();
    }
}
