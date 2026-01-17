package com.iec.nav;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
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
     * Extract side roads using real graph geometry,
     * excluding the main route direction.
     */
    private List<SideRoadStub> extractSideRoads(PointList routePoints) {

        BaseGraph graph = hopper.getBaseGraph();
        List<SideRoadStub> result = new ArrayList<>();

        for (int i = 1; i < routePoints.size() - 1; i++) {

            double lat = routePoints.getLat(i);
            double lon = routePoints.getLon(i);

            Snap snap = locationIndex.findClosest(lat, lon, edge -> true);
            if (!snap.isValid()) continue;

            int nodeId = snap.getClosestNode();

            // Previous and next route points
            double prevLat = routePoints.getLat(i - 1);
            double prevLon = routePoints.getLon(i - 1);
            double nextLat = routePoints.getLat(i + 1);
            double nextLon = routePoints.getLon(i + 1);

            EdgeIterator iter = graph.createEdgeExplorer().setBaseNode(nodeId);
            while (iter.next()) {

                int adj = iter.getAdjNode();
                double adjLat = graph.getNodeAccess().getLat(adj);
                double adjLon = graph.getNodeAccess().getLon(adj);

                // Skip main route edges
                if (isSameLocation(adjLat, adjLon, prevLat, prevLon) ||
                        isSameLocation(adjLat, adjLon, nextLat, nextLon)) {
                    continue;
                }

                EdgeIteratorState edge = iter.detach(false);
                PointList geom = edge.fetchWayGeometry(FetchMode.ALL);
                if (geom.size() < 2) continue;

                double lat0 = geom.getLat(0);
                double lon0 = geom.getLon(0);
                double lat1 = geom.getLat(1);
                double lon1 = geom.getLon(1);

                result.add(new SideRoadStub(lat0, lon0, lat1, lon1));
            }
        }

        return result;
    }

    private static boolean isSameLocation(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        return Math.abs(lat1 - lat2) < 1e-5 &&
                Math.abs(lon1 - lon2) < 1e-5;
    }
}
