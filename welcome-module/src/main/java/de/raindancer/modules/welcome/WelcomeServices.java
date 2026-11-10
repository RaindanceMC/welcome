package de.raindancer.modules.welcome;

import de.raindancer.core.ui.messages.Messages;
import de.raindancer.modules.welcome.service.WelcomeService;

/** What this module built, handed to its commands. */
public record WelcomeServices(Messages messages, WelcomeService welcome) {
}
