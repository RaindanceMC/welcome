package de.raindancer.modules.welcome.tutorial;

import de.raindancer.core.platform.util.Scheduling;
import de.raindancer.core.world.manage.VoidGenerator;
import de.raindancer.core.world.protection.LockedWorlds;
import de.raindancer.modules.welcome.store.TutorialPlaces;
import de.raindancer.modules.welcome.store.TutorialPlaces.Place;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * The tutorial's own world: a void world with a generated island ({@link IslandShape}) — a beach, grass, a few
 * trees, paths from the middle out to a ring of stations with a lantern each — until staff build a nicer one.
 * Locked: newcomers cannot break or place anything there; staff ({@code builder}) can.
 */
public final class IslandBuilder {

    public static final String WORLD = "tutorial";
    private static final int SEA = 62;
    /** Below this in the island's world nothing is left to stand on: put back at the start. */
    public static final int VOID_BELOW = SEA - 20;
    private static final int RADIUS = 40;

    private final Plugin plugin;
    private final Predicate<org.bukkit.entity.Player> builder;

    public IslandBuilder(Plugin plugin, Predicate<org.bukkit.entity.Player> builder) {
        this.plugin = plugin;
        this.builder = builder;
    }

    /** The world, if it was made before — loaded with its void generator, locked. */
    public World loadIfThere() {
        if (!new File(plugin.getServer().getWorldContainer(), WORLD).isDirectory()) {
            return null;
        }
        return create();
    }

    private World create() {
        World world = new WorldCreator(WORLD).generator(VoidGenerator.empty()).type(WorldType.FLAT)
                .generateStructures(false).createWorld();
        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setTime(6000);
            LockedWorlds.lock(plugin, WORLD, builder);
        }
        return world;
    }

    /** Builds the island and puts the start and every station on it; {@code done} runs once all chunks are laid. */
    public void build(TutorialPlaces places, Runnable done) {
        World world = create();
        if (world == null) {
            return;
        }
        IslandShape shape = new IslandShape(RADIUS, 1118L);
        List<int[]> ring = shape.ring(Stations.ALL.size());
        int reach = (RADIUS + 12) >> 4;
        AtomicInteger left = new AtomicInteger((2 * reach + 1) * (2 * reach + 1));
        for (int cx = -reach; cx <= reach; cx++) {
            for (int cz = -reach; cz <= reach; cz++) {
                int chunkX = cx;
                int chunkZ = cz;
                Scheduling.region(plugin, new Location(world, chunkX * 16 + 8, SEA, chunkZ * 16 + 8), () -> {
                    layChunk(world, shape, ring, chunkX, chunkZ);
                    if (left.decrementAndGet() == 0) {
                        Scheduling.region(plugin, new Location(world, 0, SEA, 0), () -> {
                            finish(world, shape, ring, places);
                            done.run();
                        });
                    }
                });
            }
        }
    }

    private static void layChunk(World world, IslandShape shape, List<int[]> ring, int chunkX, int chunkZ) {
        world.getChunkAt(chunkX, chunkZ).load(true);
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkX * 16 + dx;
                int z = chunkZ * 16 + dz;
                if (Math.hypot(x, z) > RADIUS + 12) {
                    continue;
                }
                var height = shape.height(x, z);
                if (height.isEmpty()) {
                    world.getBlockAt(x, SEA - 3, z).setType(Material.SAND, false);
                    for (int y = SEA - 2; y <= SEA; y++) {
                        world.getBlockAt(x, y, z).setType(Material.WATER, false);
                    }
                    continue;
                }
                int top = SEA + height.get();
                for (int y = SEA - 4; y < top - 2; y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE, false);
                }
                boolean beach = height.get() <= 1;
                world.getBlockAt(x, top - 2, z).setType(beach ? Material.SAND : Material.DIRT, false);
                world.getBlockAt(x, top - 1, z).setType(beach ? Material.SAND : Material.DIRT, false);
                world.getBlockAt(x, top, z).setType(path(x, z, ring) ? Material.DIRT_PATH
                        : beach ? Material.SAND : Material.GRASS_BLOCK, false);
            }
        }
    }

    /** On the way from the middle out to a station, a block or so wide. */
    private static boolean path(int x, int z, List<int[]> ring) {
        for (int[] spot : ring) {
            double length = Math.hypot(spot[0], spot[1]);
            double along = (x * spot[0] + z * spot[1]) / length;
            if (along < 2 || along > length) {
                continue;
            }
            double off = Math.abs(x * spot[1] - z * spot[0]) / length;
            if (off <= 1.0) {
                return true;
            }
        }
        return Math.hypot(x, z) <= 3.5;
    }

    private void finish(World world, IslandShape shape, List<int[]> ring, TutorialPlaces places) {
        Random random = new Random(1118L);
        for (int tree = 0; tree < 40; tree++) {
            int x = random.nextInt(RADIUS * 2) - RADIUS;
            int z = random.nextInt(RADIUS * 2) - RADIUS;
            var height = shape.height(x, z);
            if (height.isEmpty() || height.get() < 2 || path(x, z, ring) || Math.hypot(x, z) < 8 || nearStation(x, z, ring)) {
                continue;
            }
            Location at = new Location(world, x, SEA + height.get() + 1, z);
            world.generateTree(at, random.nextBoolean() ? TreeType.TREE : TreeType.BIRCH);
        }
        int startY = SEA + shape.height(0, 0).orElse(1) + 1;
        places.start(new Place(WORLD, 0.5, startY, 0.5, 180, 0));
        world.setSpawnLocation(0, startY, 0);
        for (int i = 0; i < Stations.ALL.size(); i++) {
            int[] spot = ring.get(i);
            int y = SEA + shape.height(spot[0], spot[1]).orElse(1) + 1;
            // A lantern on a post a few steps behind where the guide stands, clear of its speech bubble.
            double out = Math.hypot(spot[0], spot[1]);
            int px = spot[0] + (int) Math.round(3 * spot[0] / out);
            int pz = spot[1] + (int) Math.round(3 * spot[1] / out);
            int postY = SEA + shape.height(px, pz).orElse(1) + 1;
            Block post = world.getBlockAt(px, postY, pz);
            post.setType(Material.SPRUCE_FENCE, false);
            post.getRelative(0, 1, 0).setType(Material.SPRUCE_FENCE, false);
            post.getRelative(0, 2, 0).setType(Material.LANTERN, false);
            float yaw = (float) Math.toDegrees(Math.atan2(spot[0], -spot[1]));
            places.station(Stations.ALL.get(i).id(), new Place(WORLD, spot[0] + 0.5, y, spot[1] + 0.5, yaw, 0));
        }
    }

    private static boolean nearStation(int x, int z, List<int[]> ring) {
        for (int[] spot : ring) {
            if (Math.hypot(x - spot[0], z - spot[1]) < 5) {
                return true;
            }
        }
        return false;
    }
}
