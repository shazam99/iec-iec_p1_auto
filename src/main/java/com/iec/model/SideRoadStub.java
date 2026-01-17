package com.iec.model;

/**
 * Short visual representation of a real road
 * branching from the active route.
 *
 * Contains two real geometry points:
 *  - junction point (lat0, lon0)
 *  - next point along the side road (lat1, lon1)
 */
public class SideRoadStub {

    public final double lat0;
    public final double lon0;
    public final double lat1;
    public final double lon1;

    public SideRoadStub(double lat0, double lon0, double lat1, double lon1) {
        this.lat0 = lat0;
        this.lon0 = lon0;
        this.lat1 = lat1;
        this.lon1 = lon1;
    }
}
