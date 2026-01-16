package com.iec.model;

/**
 * Short visual representation of a real road
 * branching from the active route.
 */
public class SideRoadStub {

    public final double lat;
    public final double lon;
    public final double bearingDeg;

    public SideRoadStub(double lat, double lon, double bearingDeg) {
        this.lat = lat;
        this.lon = lon;
        this.bearingDeg = bearingDeg;
    }
}
