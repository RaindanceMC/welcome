package de.raindancer.modules.welcome.tutorial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The generated tutorial island: a round, gently rising island with a wobbling coast, and where its stations stand
 * in a ring around the middle. Pure arithmetic; {@code IslandBuilder} lays the blocks.
 */
public final class IslandShape {

    private static final int WAVES = 5;

    private final int radius;
    private final double[] amplitude = new double[WAVES];
    private final double[] phase = new double[WAVES];

    public IslandShape(int radius, long seed) {
        this.radius = radius;
        Random random = new Random(seed);
        for (int i = 0; i < WAVES; i++) {
            amplitude[i] = 0.06 / (i + 1) + random.nextDouble() * 0.02;
            phase[i] = random.nextDouble() * Math.PI * 2;
        }
    }

    public int radius() {
        return radius;
    }

    /** Blocks above the sea at x, z (relative to the middle); empty in the sea. */
    public Optional<Integer> height(int x, int z) {
        double angle = Math.atan2(z, x);
        double coast = radius;
        for (int i = 0; i < WAVES; i++) {
            coast += radius * amplitude[i] * Math.sin((i + 2) * angle + phase[i]);
        }
        double distance = Math.hypot(x, z);
        if (distance > coast) {
            return Optional.empty();
        }
        double inland = 1 - distance / coast;
        // Flat near the coast (a beach), rising slowly towards a low hill in the middle.
        return Optional.of(1 + (int) Math.round(4 * inland * inland));
    }

    /** {@code count} spots evenly round a ring at three fifths of the radius, x and z relative to the middle. */
    public List<int[]> ring(int count) {
        List<int[]> spots = new ArrayList<>();
        double ring = radius * 0.6;
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count - Math.PI / 2;
            spots.add(new int[]{(int) Math.round(Math.cos(angle) * ring), (int) Math.round(Math.sin(angle) * ring)});
        }
        return spots;
    }
}
