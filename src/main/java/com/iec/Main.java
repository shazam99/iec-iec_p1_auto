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
//                28.410115,77.362332,
//                28.387889,77.365063 // dentist u turn

//                28.387815,77.364403,
//                28.384858,77.362705 // chandila chowk

//                28.462666,77.507872, // pari chowk
//                28.462880, 77.510202

                29.025818602887757, 77.07152169492728,
                29.028351524559234, 77.07358163136662

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
