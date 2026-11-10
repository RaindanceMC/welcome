package de.raindancer.modules.welcome.util;

import org.bukkit.Server;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/** What this module asks about somebody. Registered in code, since a hosted module has no descriptor. */
public final class PermissionNodes {

    /** Staff and builders: may stay on the welcome server, never sent on, never protected. */
    public static final String STAY = "rainswelcome.stay";
    /** /welcome preview | status. */
    public static final String ADMIN = "rainswelcome.admin";

    private PermissionNodes() {
    }

    public static List<Permission> declared() {
        return List.of(
                new Permission(STAY, "Stay on the welcome server to work on it, not welcomed or sent on",
                        PermissionDefault.OP),
                new Permission(ADMIN, "See the welcome as a newcomer does; /welcome", PermissionDefault.OP));
    }

    /** Registers whatever is not registered yet. @return how many were added */
    public static int register(Server server) {
        if (server == null) {
            return 0;
        }
        int added = 0;
        for (Permission permission : declared()) {
            if (server.getPluginManager().getPermission(permission.getName()) != null) {
                continue;
            }
            try {
                server.getPluginManager().addPermission(permission);
                added++;
            } catch (IllegalArgumentException alreadyThere) {
                // Registered by another copy between the check and the add. Nothing to do.
            }
        }
        return added;
    }
}
