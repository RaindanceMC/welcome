package de.raindancer.modules.welcome.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("what this server is to the welcome module")
class RoleTest {

    @Test
    @DisplayName("welcome: the welcome server itself; network: any other server, where /trailer sends you over")
    void parse() {
        assertThat(Role.of("welcome")).isEqualTo(Role.WELCOME);
        assertThat(Role.of(" Network ")).isEqualTo(Role.NETWORK);
    }

    @Test
    @DisplayName("anything else is the welcome server — what the module was installed for")
    void fallback() {
        assertThat(Role.of(null)).isEqualTo(Role.WELCOME);
        assertThat(Role.of("smp")).isEqualTo(Role.WELCOME);
    }
}
