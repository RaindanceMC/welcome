package de.raindancer.modules.welcome.rules;

import de.raindancer.modules.welcome.rules.ArrivalRule.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("what somebody arriving on the welcome server gets")
class ArrivalRuleTest {

    @Test
    @DisplayName("a newcomer is welcomed and offered the way on")
    void welcomed() {
        assertThat(ArrivalRule.decide(true, "smp", false)).isEqualTo(Verdict.WELCOME);
    }

    @Test
    @DisplayName("staff and builders may stay without being sent on")
    void staff() {
        assertThat(ArrivalRule.decide(true, "smp", true)).isEqualTo(Verdict.STAY);
    }

    @Test
    @DisplayName("switched off, or nowhere to send them: nothing happens")
    void off() {
        assertThat(ArrivalRule.decide(false, "smp", false)).isEqualTo(Verdict.OFF);
        assertThat(ArrivalRule.decide(true, " ", false)).isEqualTo(Verdict.OFF);
        assertThat(ArrivalRule.decide(true, null, false)).isEqualTo(Verdict.OFF);
    }

    @Test
    @DisplayName("sent on and still here after a while: the network did not take them")
    void stuck() {
        assertThat(ArrivalRule.stuck(1_000, 6_999, 6)).isFalse();
        assertThat(ArrivalRule.stuck(1_000, 7_000, 6)).isTrue();
        assertThat(ArrivalRule.stuck(-1, 70_000, 6)).isFalse();
    }
}
