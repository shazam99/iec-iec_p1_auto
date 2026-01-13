package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Renders navigation UI to a 480x480 round TFT-like canvas.
 * Phase 4 renderer – logic will map 1:1 to ESP32 TFT.
 */
public class TftRenderer {

    private final GraphicsContext g;

    private static final double WIDTH = 480;
    private static final double HEIGHT = 480;
    private static final double CENTER = 240;

    public TftRenderer(GraphicsContext graphicsContext) {
        this.g = graphicsContext;
    }

    public void render(
            NavigationState navState,
            NavigationSnapshot snapshot
    ) {
        clear();
        drawRoute(snapshot);
        drawVehicle(navState);
        drawHud(navState, snapshot);
    }

    // ---------------- Drawing ----------------

    private void clear() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawRoute(NavigationSnapshot snapshot) {
        if (snapshot.path == null || snapshot.path.size() < 2) return;

        g.setStroke(Color.DARKCYAN);
        g.setLineWidth(3);

        for (int i = 0; i < snapshot.path.size() - 1; i++) {
            double[] p1 = snapshot.path.get(i);
            double[] p2 = snapshot.path.get(i + 1);

            double x1 = mapLonToX(p1[1]);
            double y1 = mapLatToY(p1[0]);
            double x2 = mapLonToX(p2[1]);
            double y2 = mapLatToY(p2[0]);

            g.strokeLine(x1, y1, x2, y2);
        }
    }

    private void drawVehicle(NavigationState navState) {
        double[] pos = navState.getCurrentPosition();

        double x = mapLonToX(pos[1]);
        double y = mapLatToY(pos[0]);

        g.setFill(Color.ORANGE);
        g.fillOval(x - 6, y - 6, 12, 12);
    }

    private void drawHud(
            NavigationState navState,
            NavigationSnapshot snapshot
    ) {
        g.setFill(Color.WHITE);

        double remainingKm =
                navState.getRemainingDistanceMeters() / 1000.0;

        g.fillText(
                String.format("Remaining: %.2f km", remainingKm),
                140,
                30
        );

        g.fillText(
                String.format("ETA: %.1f min", snapshot.etaMinutes),
                140,
                50
        );
    }

    // ---------------- Coordinate mapping ----------------
    // Simple linear mapping (simulation only)

    private double mapLatToY(double lat) {
        return CENTER - ((lat * 1000) % 200);
    }

    private double mapLonToX(double lon) {
        return CENTER + ((lon * 1000) % 200);
    }
}
