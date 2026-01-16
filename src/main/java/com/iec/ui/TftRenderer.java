package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TftRenderer {

    private static final int SCREEN_SIZE = 480;
    private static final int BUFFER_SIZE = 600;

    private static final double ROAD_WIDTH = 8.0;
    private static final double ROAD_CENTER_OFFSET = ROAD_WIDTH / 2.0;

    // Vehicle anchor (aligned to visual road center)
    private static final double VEHICLE_X = BUFFER_SIZE / 2;
    private static final double VEHICLE_Y = BUFFER_SIZE * 0.62 + ROAD_CENTER_OFFSET;

    private static final double FORWARD_METERS = 500.0;
    private static final double BACKWARD_METERS = 200.0;
    private static final double TOTAL_METERS = FORWARD_METERS + BACKWARD_METERS;

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

        ogc.setStroke(Color.WHITE);
        ogc.setLineWidth(ROAD_WIDTH);

        double metersToPixels = BUFFER_SIZE / TOTAL_METERS;
        var pts = snapshot.pathPoints;

        for (int i = 0; i < pts.size() - 1; i++) {

            double dy1 = metersNorth(vehicleLat, vehicleLon,
                    pts.getLat(i), pts.getLon(i));
            double dy2 = metersNorth(vehicleLat, vehicleLon,
                    pts.getLat(i + 1), pts.getLon(i + 1));

            if (dy1 > FORWARD_METERS && dy2 > FORWARD_METERS) continue;
            if (dy1 < -BACKWARD_METERS && dy2 < -BACKWARD_METERS) continue;

            double dx1 = metersEast(vehicleLat, vehicleLon,
                    pts.getLat(i), pts.getLon(i));
            double dx2 = metersEast(vehicleLat, vehicleLon,
                    pts.getLat(i + 1), pts.getLon(i + 1));

            double x1 = VEHICLE_X + dx1 * metersToPixels;
            double y1 = VEHICLE_Y - dy1 * metersToPixels;
            double x2 = VEHICLE_X + dx2 * metersToPixels;
            double y2 = VEHICLE_Y - dy2 * metersToPixels;

            ogc.strokeLine(x1, y1, x2, y2);
        }
    }

    public double getVehicleScreenX() {
        return SCREEN_SIZE / 2.0;
    }

    public double getVehicleScreenY() {
        return SCREEN_SIZE * 0.65 + ROAD_CENTER_OFFSET;
    }

    public void drawHud(GraphicsContext gc, double remainingKm, double etaMin) {
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("Remaining %.2f km", remainingKm), 150, 30);
        gc.fillText(String.format("ETA %.1f min", etaMin), 150, 50);
    }

    private static double metersNorth(double lat0, double lon0, double lat, double lon) {
        return (lat - lat0) * 111320.0;
    }

    private static double metersEast(double lat0, double lon0, double lat, double lon) {
        return (lon - lon0) * 111320.0 * Math.cos(Math.toRadians(lat0));
    }
    /** Copy viewport from buffer to screen */
    public void drawToScreen(GraphicsContext gc) {
        int viewX = (BUFFER_SIZE - SCREEN_SIZE) / 2;
        int viewY = (BUFFER_SIZE - SCREEN_SIZE) / 2;

        gc.drawImage(
                offscreen.snapshot(null, null),
                viewX, viewY,
                SCREEN_SIZE, SCREEN_SIZE,
                0, 0,
                SCREEN_SIZE, SCREEN_SIZE
        );
    }
}

