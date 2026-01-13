package com.iec.nav;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class NavJsonBuilder {

    public static String build(
            int action,
            int distance,
            int eta,
            List<double[]> path) {

        JsonObject root = new JsonObject();

        JsonObject n = new JsonObject();
        n.addProperty("a", action);
        n.addProperty("d", distance);
        n.addProperty("e", eta);
        root.add("n", n);

        JsonArray p = new JsonArray();
        for (double[] pt : path) {
            JsonArray c = new JsonArray();
            c.add(round(pt[0]));
            c.add(round(pt[1]));
            p.add(c);
        }
        root.add("p", p);

        return root.toString();
    }

    private static double round(double v) {
        return Math.round(v * 1e5) / 1e5;
    }
}
