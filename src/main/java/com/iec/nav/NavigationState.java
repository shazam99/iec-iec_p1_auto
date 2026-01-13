package com.iec.nav;

import java.util.List;

/**
 * Simulates vehicle movement along a route.
 * Phase 4: JavaFX simulation
 * Phase 5: Logic will port 1:1 to ESP32.
 */
public class NavigationState {

    private final List<double[]> path;
    private int index = 0;

    private double remainingDistanceMeters;

    // meters advanced per tick (~1.5 m @ 30 FPS ≈ 54 km/h)
    private static final double STEP_METERS = 1.5;

    public NavigationState(List<double[]> path) {
        this.path = path;
        this.remainingDistanceMeters = estimateTotalDistance();
    }

    /**
     * Advances the vehicle along the path.
     * Called every frame.
     */
    public void tick() {
        if (index >= path.size() - 1) return;

        remainingDistanceMeters -= STEP_METERS;
        if (remainingDistanceMeters < 0) remainingDistanceMeters = 0;

        index++;
    }

    /**
     * Current vehicle GPS position.
     */
    public double[] getCurrentPosition() {
        if (index >= path.size()) {
            return path.get(path.size() - 1);
        }
        return path.get(index);
    }

    public double getRemainingDistanceMeters() {
        return remainingDistanceMeters;
    }

    // ---------------- Utility ----------------

    private double estimateTotalDistance() {
        double sum = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            sum += haversine(
                    path.get(i)[0], path.get(i)[1],
                    path.get(i + 1)[0], path.get(i + 1)[1]
            );
        }
        return sum;
    }

    // Distance in meters
    private double haversine(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        final double R = 6371000; // Earth radius (m)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
