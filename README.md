# RaindanceMC/welcome

- **`welcome-module`** — Rain's Flexible Modules — Welcome: the welcome server. The proxy (RainsNetworkProxy `newcomers.server`) sends players the network has never seen here first; they are greeted (title + welcome window) and sent on to the network when they choose, or at once with `/skip`. Newcomers take no damage and get no hungrier here. The server runs on a database of its own, so money, homes or anything else tried here never reaches the network.
- Players RainsNetworkProxy parks here while they queue for a full server (note `queue`) skip the intro and wait as spectators high over the island under the midnight sky, alone, free to look around (0.6.0); `/tutorial` takes them out of it onto the tour.
- **`welcome-standalone`** — Rain's Welcome: the module and the standard wrapper, shaded together.

Sending on is Core's `ProxyTransfer` (the proxy's plugin-message channel; Velocity needs `bungee-plugin-message-channel = true`), so it needs no shared database. `/skip` (`/continue`) for everyone; `/welcome preview | status` for staff (`rainswelcome.admin`). Staff and builders (`rainswelcome.stay`) are never welcomed or sent on. Settings page "Welcome server".

Intro: newcomers first watch a camera flight through shots staff record (`/intro shot add "Title | subtitle"`), with music from `plugins/RainsWelcome/music` (Core's MusicPack), sneak skips; `/trailer` shows it again — on a network server (setting `role: network`, e.g. the SMP) it sends players over to the welcome server.

Tutorial: `/tutorial` (or "Show me around first" in the welcome window) — a guide (Core's `Guide`, only that player sees it) walks the player round nine stations on the tutorial island (money, cosmetics, claims, homes, travel, jobs, skills, guilds, rules), explains each in a native dialog and waits while they try it (`/balance`, `/cosmetics`, `/claim`, …). `/tutorial next` skips a stop, `/tutorial stop`. Staff: `/tutorial island generate` builds a ready island in its own world (`tutorial`, locked, void below — falling off puts you back at the start), or `/tutorial start set` + `/tutorial station set <id>` on a build of your own.

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework).

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `welcome-standalone/target/RainsWelcome-0.6.0.jar` — needs RainsCore 2.85.0 or newer

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.

End to end: `WelcomeScenarioTest` in [e2e](https://github.com/RaindanceMC/e2e).
