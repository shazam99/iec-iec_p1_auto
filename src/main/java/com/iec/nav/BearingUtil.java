package com.iec.nav;

public class BearingUtil {

    public static double bearingDegrees(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(phi2);
        double x =
                Math.cos(phi1) * Math.sin(phi2) -
                        Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
}
