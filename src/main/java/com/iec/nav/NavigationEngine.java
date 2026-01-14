package com.iec.nav;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.util.PointList;
import com.iec.model.NavigationSnapshot;

public class NavigationEngine {

    private final GraphHopper hopper;

    public NavigationEngine(String osmFile) {
        hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.importOrLoad();
    }

    public NavigationSnapshot buildRoute(
            double fromLat,
            double fromLon,
            double toLat,
            double toLon
    ) {
        ResponsePath path = hopper.route(
                new com.graphhopper.GHRequest(fromLat, fromLon, toLat, toLon)
                        .setProfile("car")
        ).getBest();

        PointList pts = path.getPoints();

        // ---- Bounding box calculation ----
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (int i = 0; i < pts.size(); i++) {
            double lat = pts.getLat(i);
            double lon = pts.getLon(i);

            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
            minLon = Math.min(minLon, lon);
            maxLon = Math.max(maxLon, lon);
        }

        return new NavigationSnapshot(
                pts,
                path.getDistance() / 1000.0,
                path.getTime() / 60000.0,
                minLat,
                maxLat,
                minLon,
                maxLon
        );
    }
}
