package com.iec;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationEngine;
import com.iec.nav.NavigationState;
import com.iec.ui.RoundTftApp;

public class Main {

    public static void main(String[] args) {

        // ✅ Engine needs OSM file path
        NavigationEngine engine =
                new NavigationEngine("maps/india-260111.osm.pbf");

        NavigationSnapshot snapshot = engine.buildRoute(
                28.373902,77.37149,   // Faridabad
//                28.61291, 77.22951    // India Gate
//                28.100542,77.336318 // palwal
                28.387845,77.353638 // omaxe
        );

        System.out.println("✅ Graph loaded");
        System.out.println("📍 Route calculated");
        System.out.println("➡️ Distance: " + snapshot.distanceKm + " km");
        System.out.println("⏱️ Time: " + snapshot.etaMinutes + " minutes");

        NavigationState navState =
                new NavigationState(snapshot.pathPoints);

        RoundTftApp.setNavigation(navState, snapshot);

        RoundTftApp.launch(RoundTftApp.class);
    }
}
