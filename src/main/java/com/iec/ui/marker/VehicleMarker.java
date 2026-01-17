package com.iec.ui.marker;


import com.iec.ui.TftRenderer;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.paint.Color;

public interface VehicleMarker {

    void draw(
            GraphicsContext gc,
            double x,
            double y,
            double headingRad
    );

    class CircleVehicleMarker implements VehicleMarker {
        private final double radius;
        private final Color color;
        public CircleVehicleMarker() {
            this.radius = 6;
            this.color = Color.ORANGE;
        }
        public CircleVehicleMarker(double radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void draw(GraphicsContext gc, double x, double y, double headingRad) {

        }
    }
    class TriangleVehicleMarker implements VehicleMarker {
        private final double size;
        private final Color color;
        public TriangleVehicleMarker() {
            this.size = 6;
            this.color = Color.BLUE;
        }
        public TriangleVehicleMarker(double size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void draw(GraphicsContext gc, double x, double y, double headingRad) {

            gc.save();

            gc.translate(x, y);
            gc.rotate(Math.toDegrees(headingRad));

            // Custom dimensions: wide base, short tip
            double halfWidth = size/1 ; // Increase this for "wider" sides
            double halfHeight = size*0.8; // Decrease this for "smaller" height

            double[] xPoints = {0, -halfWidth, halfWidth};
            double[] yPoints = {-halfHeight, halfHeight, halfHeight};

            // Fill
            gc.setFill(color);
            gc.fillPolygon(xPoints, yPoints, 3);

            // White Border
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(5.0);
            gc.strokePolygon(xPoints, yPoints, 3);

            gc.restore();
        }
    }

}


