package com.iec.nav;

import com.graphhopper.util.PointList;

/**
 * Simulates vehicle movement along a route with speed control.
 */
public class NavigationState {

    private final PointList path;
    private int currentIndex = 0;          // current segment start index
    private double positionAlongSegment = 0; // meters moved along current segment

    public NavigationState(PointList pathPoints) {
        this.path = pathPoints;
    }

    /** Advance vehicle by one tick at normal speed */
    public void tick() {
        tick(1.0); // default speedFactor = 1
    }

    /**
     * Advance vehicle along path scaled by speedFactor
     * @param speedFactor multiplier, 1.0 = normal, <1.0 = slower
     */
    public void tick(double speedFactor) {
        if (currentIndex >= path.size() - 1) return;

        double distanceToNext = distanceMeters(
                path.getLat(currentIndex), path.getLon(currentIndex),
                path.getLat(currentIndex + 1), path.getLon(currentIndex + 1)
        );

        double moveDistance = 30 * speedFactor; // assume 30m per tick at normal speed
        positionAlongSegment += moveDistance;

        while (positionAlongSegment >= distanceToNext && currentIndex < path.size() - 2) {
            positionAlongSegment -= distanceToNext;
            currentIndex++;
            distanceToNext = distanceMeters(
                    path.getLat(currentIndex), path.getLon(currentIndex),
                    path.getLat(currentIndex + 1), path.getLon(currentIndex + 1)
            );
        }
    }

    /** Current latitude (interpolated along segment) */
    public double getCurrentLat() {
        if (currentIndex >= path.size() - 1) return path.getLat(path.size() - 1);
        double t = positionAlongSegment / distanceMeters(
                path.getLat(currentIndex), path.getLon(currentIndex),
                path.getLat(currentIndex + 1), path.getLon(currentIndex + 1)
        );
        return path.getLat(currentIndex) * (1 - t) + path.getLat(currentIndex + 1) * t;
    }

    /** Current longitude (interpolated along segment) */
    public double getCurrentLon() {
        if (currentIndex >= path.size() - 1) return path.getLon(path.size() - 1);
        double t = positionAlongSegment / distanceMeters(
                path.getLat(currentIndex), path.getLon(currentIndex),
                path.getLat(currentIndex + 1), path.getLon(currentIndex + 1)
        );
        return path.getLon(currentIndex) * (1 - t) + path.getLon(currentIndex + 1) * t;
    }

    /** Remaining distance in KM (approximate) */
    public double getRemainingDistanceKm() {
        double remaining = distanceMeters(
                getCurrentLat(), getCurrentLon(),
                path.getLat(path.size() - 1), path.getLon(path.size() - 1)
        );
        return remaining / 1000.0;
    }

    /** True when destination reached */
    public boolean isFinished() {
        return currentIndex >= path.size() - 1;
    }
    public double getHeadingRadians() {
        if (currentIndex >= path.size() - 1) return 0;

        double lat1 = Math.toRadians(path.getLat(currentIndex));
        double lon1 = Math.toRadians(path.getLon(currentIndex));
        double lat2 = Math.toRadians(path.getLat(currentIndex + 1));
        double lon2 = Math.toRadians(path.getLon(currentIndex + 1));

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        return Math.atan2(y, x); // radians
    }


    // ================== Helper ==================
    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
