package de.raindancer.modules.welcome.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaitingSkyTest {

    @Test
    @DisplayName("above the clouds, so the night sky is all they see")
    void aboveTheClouds() {
        assertThat(WaitingSky.height(64, 320)).isGreaterThanOrEqualTo(WaitingSky.CLOUDS + 20);
    }

    @Test
    @DisplayName("never above the build limit, however high the island")
    void underTheLimit() {
        assertThat(WaitingSky.height(300, 320)).isLessThanOrEqualTo(310);
        assertThat(WaitingSky.height(64, 200)).isLessThanOrEqualTo(190);
    }

    @Test
    @DisplayName("midnight, looking up a little: stars and the moon")
    void midnight() {
        assertThat(WaitingSky.TIME).isEqualTo(18000L);
        assertThat(WaitingSky.PITCH).isNegative();
    }
}
