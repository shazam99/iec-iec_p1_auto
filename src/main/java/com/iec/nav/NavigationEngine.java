package com.iec.nav;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.iec.model.NavigationSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4 Navigation Engine
 * - Owns GraphHopper
 * - Builds route
 * - Produces NavigationSnapshot
 */
public class NavigationEngine {

    private GraphHopper hopper;
    private boolean initialized = false;

    public NavigationEngine() {
        // lazy init
    }

    /**
     * Build route between two points.
     */
    public NavigationSnapshot buildRoute(
            String osmFile,
            double fromLat, double fromLon,
            double toLat, double toLon
    ) {

        initIfNeeded(osmFile);

        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon)
                .setProfile("car")
                .setLocale("en");

        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            throw new RuntimeException(rsp.getErrors().toString());
        }

        ResponsePath path = rsp.getBest();

        // ---- Extract polyline ----
        List<double[]> points = new ArrayList<>();
        path.getPoints().forEach(p ->
                points.add(new double[]{p.lat, p.lon})
        );

        double distanceKm = path.getDistance() / 1000.0;
        double etaMinutes = path.getTime() / 60000.0;

        // Initial position = first point
        double startLat = points.get(0)[0];
        double startLon = points.get(0)[1];

        return new NavigationSnapshot(
                distanceKm,
                etaMinutes,
                /* action */ 0,
                /* nextTurnDistanceMeters */ 0,
                points,
                startLat,
                startLon,
                distanceKm,
                etaMinutes
        );
    }

    // ---------------- Internals ----------------

    private void initIfNeeded(String osmFile) {
        if (initialized) return;

        hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation("graph-cache");

        hopper.setProfiles(
                new Profile("car").setVehicle("car").setWeighting("fastest")
        );

        hopper.importOrLoad();

        initialized = true;
    }
}
