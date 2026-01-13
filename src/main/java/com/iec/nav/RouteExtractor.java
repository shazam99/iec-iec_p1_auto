package com.iec.nav;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts raw latitude/longitude points from a GraphHopper route.
 * Output format is ESP32-friendly: double[]{lat, lon}
 */
public class RouteExtractor {

    public List<double[]> extract(ResponsePath path) {

        List<double[]> points = new ArrayList<>();

        PointList geometry = path.getPoints();

        for (int i = 0; i < geometry.size(); i++) {
            double lat = geometry.getLat(i);
            double lon = geometry.getLon(i);
            points.add(new double[]{lat, lon});
        }

        return points;
    }
}
