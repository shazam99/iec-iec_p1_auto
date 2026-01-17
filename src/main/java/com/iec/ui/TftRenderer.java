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
    private static final double BACKWARD_METERS = 10.0;
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

        ogc.setLineCap(StrokeLineCap.ROUND);
        ogc.setLineJoin(StrokeLineJoin.ROUND);

        for (SideRoadStub sr : snapshot.sideRoads) {

            // Junction point (start)
            double dy0 = metersNorth(vLat, vLon, sr.lat0, sr.lon0);
            if (dy0 < -BACKWARD_METERS || dy0 > FORWARD_METERS) continue;

            double dx0 = metersEast(vLat, vLon, sr.lat0, sr.lon0);

            double x0 = VEHICLE_X + dx0 * m2p;
            double y0 = VEHICLE_Y - dy0 * m2p;

            // Direction vector (real side road direction)
            double dy1 = metersNorth(vLat, vLon, sr.lat1, sr.lon1);
            double dx1 = metersEast(vLat, vLon, sr.lat1, sr.lon1);

            double vx = dx1 - dx0;
            double vy = dy1 - dy0;

            double len = Math.hypot(vx, vy);
            if (len < 0.1) continue;

            // Normalize
            vx /= len;
            vy /= len;

            // Clamp to fixed visual length
            double endDx = dx0 + vx * SIDE_ROAD_LENGTH_M;
            double endDy = dy0 + vy * SIDE_ROAD_LENGTH_M;

            double x1 = VEHICLE_X + endDx * m2p;
            double y1 = VEHICLE_Y - endDy * m2p;

            // Soft fade like screenshot
            ogc.setStroke(new LinearGradient(
                    x0, y0, x1, y1,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(220, 220, 220, 0.5)),
                    new Stop(1.0, Color.rgb(220, 220, 220, 0))
            ));

            ogc.setLineWidth(ROAD_WIDTH * 1.0);

            ogc.strokeLine(x0, y0, x1, y1);
            ogc.setStroke(new LinearGradient(
                    x0, y0, x1, y1,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(0, 0, 0, 1)),
                    new Stop(1.0, Color.rgb(0, 0, 0, 1))
            ));

            ogc.setLineWidth(ROAD_WIDTH * 0.3);
            ogc.strokeLine(x0, y0, x1, y1);
        }
    }

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
