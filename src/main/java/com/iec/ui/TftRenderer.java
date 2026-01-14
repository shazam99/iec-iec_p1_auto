package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TftRenderer {

    private static final int SIZE = 480;
    private static final int PADDING = 20;

    private NavigationSnapshot snapshot;

    public void setSnapshot(NavigationSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    private double mapX(double lon) {
        return PADDING +
                (lon - snapshot.minLon)
                        / (snapshot.maxLon - snapshot.minLon)
                        * (SIZE - 2 * PADDING);
    }

    private double mapY(double lat) {
        // Screen Y-axis inverted
        return SIZE - PADDING -
                (lat - snapshot.minLat)
                        / (snapshot.maxLat - snapshot.minLat)
                        * (SIZE - 2 * PADDING);
    }

    public void drawRoute(GraphicsContext gc) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);

        var pts = snapshot.pathPoints;

        for (int i = 1; i < pts.size(); i++) {
            gc.strokeLine(
                    mapX(pts.getLon(i - 1)),
                    mapY(pts.getLat(i - 1)),
                    mapX(pts.getLon(i)),
                    mapY(pts.getLat(i))
            );
        }
    }

    public void drawVehicle(GraphicsContext gc, double lat, double lon) {
        gc.setFill(Color.ORANGE);
        gc.fillOval(
                mapX(lon) - 6,
                mapY(lat) - 6,
                12,
                12
        );
    }

    public void drawHud(GraphicsContext gc, double remainingKm, double etaMin) {
        gc.setFill(Color.WHITE);
        gc.fillText(String.format("Remaining %.2f km", remainingKm), 150, 30);
        gc.fillText(String.format("ETA %.1f min", etaMin), 150, 50);
    }
}
