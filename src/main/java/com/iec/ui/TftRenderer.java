package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import com.iec.model.SideRoadStub;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class TftRenderer {

    private static final int SCREEN_SIZE = 480;
    private static final int BUFFER_SIZE = 600;

    private static final double ROAD_WIDTH = 15.0;
    private static final double ROAD_CENTER_OFFSET = ROAD_WIDTH / 2.0;

    private static final double VEHICLE_X = BUFFER_SIZE / 2.0;
    private static final double VEHICLE_Y = BUFFER_SIZE * 0.62 + ROAD_CENTER_OFFSET;

    private static final double FORWARD_METERS = 100.0;
    private static final double BACKWARD_METERS = 50.0;
    private static final double TOTAL_METERS = FORWARD_METERS + BACKWARD_METERS;

    private static final double SIDE_ROAD_LENGTH_M = 20.0;

    private final Canvas offscreen = new Canvas(BUFFER_SIZE, BUFFER_SIZE);
    private final GraphicsContext ogc = offscreen.getGraphicsContext2D();

    private NavigationSnapshot snapshot;

    public void setSnapshot(NavigationSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void renderWorld(double vehicleLat, double vehicleLon) {

        ogc.setFill(Color.BLACK);
        ogc.fillRect(0, 0, BUFFER_SIZE, BUFFER_SIZE);

        if (snapshot == null || snapshot.pathPoints.size() < 2) return;

        double metersToPixels = BUFFER_SIZE / TOTAL_METERS;

        drawSideRoads(vehicleLat, vehicleLon, metersToPixels);
//        drawMainRoute(vehicleLat, vehicleLon, metersToPixels);
        drawCurvyRoute(vehicleLat, vehicleLon, metersToPixels);
    }

    private void drawSideRoads(double vLat, double vLon, double m2p) {
        if (snapshot.sideRoads == null) return;
        for (SideRoadStub sr : snapshot.sideRoads) {
            double dy = metersNorth(vLat, vLon, sr.lat, sr.lon);
            if (Math.abs(dy) > FORWARD_METERS) continue;
            double dx = metersEast(vLat, vLon, sr.lat, sr.lon);
            double x1 = VEHICLE_X + dx * m2p;
            double y1 = VEHICLE_Y - dy * m2p;
            double rad = Math.toRadians(sr.bearingDeg);
            double x2 = x1 + Math.sin(rad) * SIDE_ROAD_LENGTH_M * m2p;
            double y2 = y1 - Math.cos(rad) * SIDE_ROAD_LENGTH_M * m2p;
            ogc.setStroke(new LinearGradient(
                    x1, y1, x2, y2,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(200, 200, 200,  0.5)),
                    new Stop(1, Color.rgb(0, 0, 0,  0))
            ));
            ogc.setLineWidth(ROAD_WIDTH);
            ogc.strokeLine(x1, y1, x2, y2);
            ogc.setStroke(new LinearGradient(
                    x1, y1, x2, y2,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(0, 0, 0,  1)),
                    new Stop(1, Color.rgb(0, 0, 0,  1))
            ));
            ogc.setLineWidth(6);
            ogc.strokeLine(x1, y1, x2, y2);
        }
    }

    private void drawMainRoute(double vLat, double vLon, double m2p) {

        ogc.setStroke(Color.rgb(255,255,255));
        ogc.setLineWidth(ROAD_WIDTH);

        var pts = snapshot.pathPoints;

        for (int i = 0; i < pts.size() - 1; i++) {

            double dy1 = metersNorth(vLat, vLon, pts.getLat(i), pts.getLon(i));
            double dy2 = metersNorth(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1));

            if (dy1 > FORWARD_METERS && dy2 > FORWARD_METERS) continue;
            if (dy1 < -BACKWARD_METERS && dy2 < -BACKWARD_METERS) continue;

            double dx1 = metersEast(vLat, vLon, pts.getLat(i), pts.getLon(i));
            double dx2 = metersEast(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1));

            ogc.strokeLine(
                    VEHICLE_X + dx1 * m2p,
                    VEHICLE_Y - dy1 * m2p,
                    VEHICLE_X + dx2 * m2p,
                    VEHICLE_Y - dy2 * m2p
            );
        }
    }

//    private void drawCurvyRoute(double vLat, double vLon, double m2p) {
//
//        var pts = snapshot.pathPoints;
//
//        ogc.setStroke(Color.WHITE);
//        ogc.setLineWidth(ROAD_WIDTH);
//        ogc.setLineCap(StrokeLineCap.ROUND);
//        ogc.setLineJoin(StrokeLineJoin.ROUND);
//
//        // Radius of the turn curve (in meters)
//        final double TURN_RADIUS_M = 8.0;
//        final double TURN_RADIUS_PX = TURN_RADIUS_M * m2p;
//
//        for (int i = 1; i < pts.size() - 1; i++) {
//
//            // Previous, current, next points in meters
//            double ax = metersEast(vLat, vLon, pts.getLat(i - 1), pts.getLon(i - 1)) * m2p;
//            double ay = -metersNorth(vLat, vLon, pts.getLat(i - 1), pts.getLon(i - 1)) * m2p;
//
//            double bx = metersEast(vLat, vLon, pts.getLat(i), pts.getLon(i)) * m2p;
//            double by = -metersNorth(vLat, vLon, pts.getLat(i), pts.getLon(i)) * m2p;
//
//            double cx = metersEast(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1)) * m2p;
//            double cy = -metersNorth(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1)) * m2p;
//
//            ax += VEHICLE_X;
//            ay += VEHICLE_Y;
//            bx += VEHICLE_X;
//            by += VEHICLE_Y;
//            cx += VEHICLE_X;
//            cy += VEHICLE_Y;
//
//            // Direction vectors
//            double v1x = bx - ax;
//            double v1y = by - ay;
//            double v2x = cx - bx;
//            double v2y = cy - by;
//
//            double len1 = Math.hypot(v1x, v1y);
//            double len2 = Math.hypot(v2x, v2y);
//
//            if (len1 < 1 || len2 < 1) continue;
//
//            // Normalize
//            v1x /= len1;
//            v1y /= len1;
//            v2x /= len2;
//            v2y /= len2;
//
//            // Angle between segments
//            double dot = v1x * v2x + v1y * v2y;
//            double angleDeg = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));
//
//            // If almost straight, draw normally
//            if (angleDeg < 20) {
//                ogc.strokeLine(ax, ay, bx, by);
//                continue;
//            }
//
//            // Points before and after the corner
//            double p1x = bx - v1x * TURN_RADIUS_PX;
//            double p1y = by - v1y * TURN_RADIUS_PX;
//
//            double p2x = bx + v2x * TURN_RADIUS_PX;
//            double p2y = by + v2y * TURN_RADIUS_PX;
//
//            // Draw incoming straight
//            ogc.strokeLine(ax, ay, p1x, p1y);
//
//            // Draw rounded corner (quadratic curve)
//            ogc.beginPath();
//            ogc.moveTo(p1x, p1y);
//            ogc.quadraticCurveTo(bx, by, p2x, p2y);
//            ogc.stroke();
//
//            // Draw outgoing straight
//            ogc.strokeLine(p2x, p2y, cx, cy);
//        }
//    }

    private void drawCurvyRoute(double vLat, double vLon, double m2p) {
        var pts = snapshot.pathPoints;
        if (pts.size() < 2) return;

        ogc.setStroke(Color.WHITE);
        ogc.setLineWidth(ROAD_WIDTH);
        ogc.setLineCap(StrokeLineCap.ROUND);
        ogc.setLineJoin(StrokeLineJoin.ROUND);

        final double TURN_RADIUS_PX = 8.0 * m2p;

        ogc.beginPath();

        // 1. Calculate and move to the first point
        double prevX = VEHICLE_X + metersEast(vLat, vLon, pts.getLat(0), pts.getLon(0)) * m2p;
        double prevY = VEHICLE_Y - metersNorth(vLat, vLon, pts.getLat(0), pts.getLon(0)) * m2p;
        ogc.moveTo(prevX, prevY);

        for (int i = 1; i < pts.size() - 1; i++) {
            // Current corner (B)
            double bx = VEHICLE_X + metersEast(vLat, vLon, pts.getLat(i), pts.getLon(i)) * m2p;
            double by = VEHICLE_Y - metersNorth(vLat, vLon, pts.getLat(i), pts.getLon(i)) * m2p;

            // Next point (C)
            double cx = VEHICLE_X + metersEast(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1)) * m2p;
            double cy = VEHICLE_Y - metersNorth(vLat, vLon, pts.getLat(i + 1), pts.getLon(i + 1)) * m2p;

            // Vector B -> Prev (A)
            double v1x = prevX - bx;
            double v1y = prevY - by;
            // Vector B -> Next (C)
            double v2x = cx - bx;
            double v2y = cy - by;

            double len1 = Math.hypot(v1x, v1y);
            double len2 = Math.hypot(v2x, v2y);

            // Clamp radius to half the segment length to prevent overlapping curves
            double dynamicRadius = Math.min(TURN_RADIUS_PX, Math.min(len1 / 2.0, len2 / 2.0));

            if (len1 < 0.1 || len2 < 0.1 || dynamicRadius < 1.0) {
                ogc.lineTo(bx, by);
                prevX = bx;
                prevY = by;
            } else {
                // Point where curve starts (p1)
                double p1x = bx + (v1x / len1) * dynamicRadius;
                double p1y = by + (v1y / len1) * dynamicRadius;

                // Point where curve ends (p2)
                double p2x = bx + (v2x / len2) * dynamicRadius;
                double p2y = by + (v2y / len2) * dynamicRadius;

                ogc.lineTo(p1x, p1y);              // Line to start of curve
                ogc.quadraticCurveTo(bx, by, p2x, p2y); // Curve through corner

                // Update prevX/Y to the end of this curve for the next iteration
                prevX = p2x;
                prevY = p2y;
            }
        }

        // 2. Finish at the very last point
        double finalX = VEHICLE_X + metersEast(vLat, vLon, pts.getLat(pts.size() - 1), pts.getLon(pts.size() - 1)) * m2p;
        double finalY = VEHICLE_Y - metersNorth(vLat, vLon, pts.getLat(pts.size() - 1), pts.getLon(pts.size() - 1)) * m2p;
        ogc.lineTo(finalX, finalY);

        ogc.stroke();
    }


    public void drawToScreen(GraphicsContext gc) {
        int view = (BUFFER_SIZE - SCREEN_SIZE) / 2;
        gc.drawImage(offscreen.snapshot(null, null),
                view, view, SCREEN_SIZE, SCREEN_SIZE,
                0, 0, SCREEN_SIZE, SCREEN_SIZE);
    }

    public void drawHud(GraphicsContext gc, double remainingKm, double etaMin) {
    gc.setFill(Color.WHITE);
    gc.fillText(
            String.format("Remaining %.2f km", remainingKm),
            20, 30
    );
    gc.fillText(
            String.format("ETA %.1f min", etaMin),
            20, 50
    );
}


    public double getVehicleScreenX() {
        return SCREEN_SIZE / 2.0;
    }

    public double getVehicleScreenY() {
        return SCREEN_SIZE * 0.65 + ROAD_CENTER_OFFSET;
    }

    private static double metersNorth(double lat0, double lon0, double lat, double lon) {
        return (lat - lat0) * 111320.0;
    }

    private static double metersEast(double lat0, double lon0, double lat, double lon) {
        return (lon - lon0) * 111320.0 * Math.cos(Math.toRadians(lat0));
    }
}
