package com.iec.nav;

import com.graphhopper.util.PointList;

/**
 * Simulates vehicle movement along a route.
 * This logic will be reused on ESP32 (Phase 6).
 */
public class NavigationState {

    private final PointList path;
    private int index = 0;

    private static final int STEP = 1; // move 1 point per tick

    public NavigationState(PointList pathPoints) {
        this.path = pathPoints;
    }

    /** Advance vehicle position */
    public void tick() {
        if (index < path.size() - 1) {
            index += STEP;
        }
    }

    /** Current latitude */
    public double getCurrentLat() {
        return path.getLat(index);
    }

    /** Current longitude */
    public double getCurrentLon() {
        return path.getLon(index);
    }

    /** Remaining distance in KM (approximate) */
    public double getRemainingDistanceKm() {
        int remainingPoints = path.size() - index - 1;
        return remainingPoints * 0.03; // approx 30m per point
    }

    /** True when destination reached */
    public boolean isFinished() {
        return index >= path.size() - 1;
    }
}
