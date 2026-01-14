package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationState;
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

    public static void setNavigation(NavigationState state, NavigationSnapshot snap) {
        navState = state;
        snapshot = snap;
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(480, 480);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        TftRenderer renderer = new TftRenderer();
        renderer.setSnapshot(snapshot);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, 480, 480);

                renderer.drawRoute(gc);
                renderer.drawVehicle(
                        gc,
                        navState.getCurrentLat(),
                        navState.getCurrentLon()
                );

                renderer.drawHud(
                        gc,
                        navState.getRemainingDistanceKm(),
                        snapshot.etaMinutes
                );

                navState.tick();
            }
        }.start();

        stage.setTitle("ESP32 TFT Simulator");
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.show();
    }
}
