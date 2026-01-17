package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationState;
import com.iec.ui.marker.VehicleMarker;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RoundTftApp extends Application {

    private static NavigationState navState;
    private static NavigationSnapshot snapshot;

    private static final double SPEED_FACTOR = 0.05; // x real speed

    public static void setNavigation(NavigationState state, NavigationSnapshot snap) {
        navState = state;
        snapshot = snap;
    }

    @Override
    public void start(Stage stage) {

        Canvas screen = new Canvas(480, 480);
        GraphicsContext gc = screen.getGraphicsContext2D();

        TftRenderer renderer = new TftRenderer();
        renderer.setSnapshot(snapshot);

        VehicleMarker vehicle = new VehicleMarker.TriangleVehicleMarker(25, Color.WHITE);

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                double lat = navState.getCurrentLat();
                double lon = navState.getCurrentLon();

                // Render world offscreen
                renderer.renderWorld(lat, lon);

                // Draw viewport
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, 480, 480);
                renderer.drawToScreen(gc);

                // Draw vehicle (fixed)
                double heading = navState.getHeadingRadians();

                vehicle.draw(
                        gc,
                        renderer.getVehicleScreenX(),
                        renderer.getVehicleScreenY(),
                        heading
                );

                renderer.drawHud(
                        gc,
                        navState.getRemainingDistanceKm(),
                        snapshot.etaMinutes
                );

                navState.tick(SPEED_FACTOR);
            }
        }.start();

        stage.setTitle("IEC Navigation Simulator");
        stage.setScene(new Scene(new StackPane(screen)));
        stage.show();
    }
}
