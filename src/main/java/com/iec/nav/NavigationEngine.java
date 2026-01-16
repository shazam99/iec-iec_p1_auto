package com.iec.nav;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.PointList;
import com.iec.model.NavigationSnapshot;
import com.iec.model.SideRoadStub;

import java.util.ArrayList;
import java.util.List;

public class NavigationEngine {

    private final GraphHopper hopper;
    private final LocationIndex locationIndex;

    public NavigationEngine(String osmFile) {
        hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest"));
        hopper.importOrLoad();

        locationIndex = hopper.getLocationIndex();
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

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (int i = 0; i < pts.size(); i++) {
            minLat = Math.min(minLat, pts.getLat(i));
            maxLat = Math.max(maxLat, pts.getLat(i));
            minLon = Math.min(minLon, pts.getLon(i));
            maxLon = Math.max(maxLon, pts.getLon(i));
        }

        List<SideRoadStub> sideRoads = extractSideRoads(pts);

        return new NavigationSnapshot(
                pts,
                path.getDistance() / 1000.0,
                path.getTime() / 60000.0,
                minLat,
                maxLat,
                minLon,
                maxLon,
                sideRoads
        );
    }

    /**
     * Extract real side roads using graph snapping.
     */
    private List<SideRoadStub> extractSideRoads(PointList routePoints) {

        BaseGraph graph = hopper.getBaseGraph();
        List<SideRoadStub> result = new ArrayList<>();

        for (int i = 1; i < routePoints.size() - 1; i++) {

            double lat = routePoints.getLat(i);
            double lon = routePoints.getLon(i);

            Snap qr = locationIndex.findClosest(lat, lon, edge -> true);
            if (!qr.isValid()) continue;

            int nodeId = qr.getClosestNode();

            EdgeIterator iter = graph.createEdgeExplorer().setBaseNode(nodeId);
            while (iter.next()) {

                int adj = iter.getAdjNode();

                double adjLat = graph.getNodeAccess().getLat(adj);
                double adjLon = graph.getNodeAccess().getLon(adj);

                double bearing = bearingDeg(lat, lon, adjLat, adjLon);

                result.add(new SideRoadStub(lat, lon, bearing));
            }
        }

        return result;
    }

    private static double bearingDeg(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        double x =
                Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                        Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }
}
