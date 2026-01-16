package com.iec.model;

import com.graphhopper.util.PointList;
import java.util.List;

public class NavigationSnapshot {

    public final PointList pathPoints;
    public final double distanceKm;
    public final double etaMinutes;

    public final double minLat, maxLat, minLon, maxLon;

    // 🔹 NEW
    public final List<SideRoadStub> sideRoads;

    public NavigationSnapshot(
            PointList pathPoints,
            double distanceKm,
            double etaMinutes,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon,
            List<SideRoadStub> sideRoads
    ) {
        this.pathPoints = pathPoints;
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.sideRoads = sideRoads;
    }
}
