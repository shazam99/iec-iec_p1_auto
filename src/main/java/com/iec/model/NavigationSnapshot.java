package com.iec.model;

import com.graphhopper.util.PointList;

public class NavigationSnapshot {

    public final PointList pathPoints;
    public final double distanceKm;
    public final double etaMinutes;

    // ✅ NEW: bounding box
    public final double minLat, maxLat, minLon, maxLon;

    public NavigationSnapshot(
            PointList pathPoints,
            double distanceKm,
            double etaMinutes,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon
    ) {
        this.pathPoints = pathPoints;
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }
}
