package com.iec.ui;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX simulator for 480x480 round TFT.
 * Phase 4: animated navigation simulation.
 */
public class RoundTftApp extends Application {

    // -------- Static handover from Main --------
    private static NavigationSnapshot initialSnapshot;

    public static void setInitialSnapshot(NavigationSnapshot snapshot) {
        initialSnapshot = snapshot;
    }

    // -------- Runtime --------
    private NavigationState navState;
    private TftRenderer renderer;

    @Override
    public void start(Stage stage) {

        if (initialSnapshot == null) {
            throw new IllegalStateException("NavigationSnapshot not set before launch()");
        }

        Canvas canvas = new Canvas(480, 480);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        renderer = new TftRenderer(gc);
        navState = new NavigationState(initialSnapshot.path);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 480, 480);

        stage.setTitle("IEC Round TFT Simulator");
        stage.setScene(scene);
        stage.show();

        // -------- Animation loop (navigation tick) --------
        AnimationTimer timer = new AnimationTimer() {

            private long last = 0;

            @Override
            public void handle(long now) {
                if (now - last < 33_000_000) return; // ~30 FPS
                last = now;

                navState.tick(); // move vehicle forward
                renderer.render(navState, initialSnapshot);
            }
        };

        timer.start();
    }
}
