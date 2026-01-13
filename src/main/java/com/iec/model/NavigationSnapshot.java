package com.iec.model;

import java.util.List;

/**
 * Immutable navigation state snapshot.
 */
public class NavigationSnapshot {

    // Static route data
    public final double distanceKm;
    public final double etaMinutes;
    public final int action;
    public final double nextTurnDistanceMeters;
    public final List<double[]> path;

    // Dynamic navigation state
    public final double lat;
    public final double lon;
    public final double remainingDistanceKm;
    public final double remainingTimeMin;

    public NavigationSnapshot(
            double distanceKm,
            double etaMinutes,
            int action,
            double nextTurnDistanceMeters,
            List<double[]> path,
            double lat,
            double lon,
            double remainingDistanceKm,
            double remainingTimeMin
    ) {
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
        this.action = action;
        this.nextTurnDistanceMeters = nextTurnDistanceMeters;
        this.path = path;
        this.lat = lat;
        this.lon = lon;
        this.remainingDistanceKm = remainingDistanceKm;
        this.remainingTimeMin = remainingTimeMin;
    }
}
