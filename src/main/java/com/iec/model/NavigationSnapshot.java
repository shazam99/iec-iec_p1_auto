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
    // ================== Window helpers ==================
    // Returns bounding box for 100m behind and 500m ahead of vehicle

    public double getWindowMinLat(double currentLat, double currentLon) {
        var bounds = computeWindow(currentLat, currentLon);
        return bounds.minLat;
    }

    public double getWindowMaxLat(double currentLat, double currentLon) {
        var bounds = computeWindow(currentLat, currentLon);
        return bounds.maxLat;
    }

    public double getWindowMinLon(double currentLat, double currentLon) {
        var bounds = computeWindow(currentLat, currentLon);
        return bounds.minLon;
    }

    public double getWindowMaxLon(double currentLat, double currentLon) {
        var bounds = computeWindow(currentLat, currentLon);
        return bounds.maxLon;
    }

    private static class Window {
        double minLat, maxLat, minLon, maxLon;
    }

    private Window computeWindow(double currentLat, double currentLon) {
        final double FORWARD_METERS = 500;
        final double BACKWARD_METERS = 100;

        Window w = new Window();
        if (pathPoints == null || pathPoints.size() < 2) {
            w.minLat = currentLat;
            w.maxLat = currentLat;
            w.minLon = currentLon;
            w.maxLon = currentLon;
            return w;
        }

        // Find closest point index
        int closestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < pathPoints.size(); i++) {
            double d = distanceMeters(currentLat, currentLon,
                    pathPoints.getLat(i), pathPoints.getLon(i));
            if (d < bestDist) {
                bestDist = d;
                closestIdx = i;
            }
        }

        // Forward window
        int forwardIdx = closestIdx;
        double distForward = 0;
        for (int i = closestIdx; i < pathPoints.size() - 1; i++) {
            double d = distanceMeters(
                    pathPoints.getLat(i), pathPoints.getLon(i),
                    pathPoints.getLat(i + 1), pathPoints.getLon(i + 1));
            distForward += d;
            if (distForward >= FORWARD_METERS) break;
            forwardIdx = i + 1;
        }

        // Backward window
        int backwardIdx = closestIdx;
        double distBackward = 0;
        for (int i = closestIdx; i > 0; i--) {
            double d = distanceMeters(
                    pathPoints.getLat(i), pathPoints.getLon(i),
                    pathPoints.getLat(i - 1), pathPoints.getLon(i - 1));
            distBackward += d;
            if (distBackward >= BACKWARD_METERS) break;
            backwardIdx = i - 1;
        }

        // Compute min/max
        w.minLat = pathPoints.get(backwardIdx).getLat();
        w.maxLat = pathPoints.get(forwardIdx).getLat();
        w.minLon = pathPoints.get(backwardIdx).getLon();
        w.maxLon = pathPoints.get(forwardIdx).getLon();

        return w;
    }

    private static double distanceMeters(double lat1, double lon1,
                                         double lat2, double lon2) {
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
