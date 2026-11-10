package de.raindancer.modules.welcome.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("a shot's caption on screen: title and subtitle")
class CaptionTest {

    @Test
    @DisplayName("\"Title | subtitle\": both")
    void both() {
        assertThat(Caption.of("Guild halls | Found one with friends")).isEqualTo(new Caption("Guild halls", "Found one with friends"));
    }

    @Test
    @DisplayName("short: the title alone")
    void shortOne() {
        assertThat(Caption.of("The market")).isEqualTo(new Caption("The market", ""));
    }

    @Test
    @DisplayName("too long for a title without a |: the subtitle line, which fits it, rather than cut off at the edges")
    void longOne() {
        assertThat(Caption.of("Your adventure starts here")).isEqualTo(new Caption("", "Your adventure starts here"));
    }

    @Test
    @DisplayName("blank: nothing")
    void blank() {
        assertThat(Caption.of("  ").isEmpty()).isTrue();
        assertThat(Caption.of(null).isEmpty()).isTrue();
    }
}
