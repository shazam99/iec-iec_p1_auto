package com.iec;

import com.iec.model.NavigationSnapshot;
import com.iec.nav.NavigationEngine;
import com.iec.ui.RoundTftApp;

/**
 * Entry point.
 * Phase 4: build route, then hand off to UI simulation.
 */
public class Main {

    public static void main(String[] args) {

        // 1️⃣ Build route using GraphHopper
        NavigationEngine engine = new NavigationEngine();

        NavigationSnapshot snapshot = engine.buildRoute(
                "maps/india-260111.osm.pbf",
                28.368210, 77.372246,   // Faridabad
                28.456225, 77.030099  // India Gate
        );

        System.out.println("✅ Graph loaded");
        System.out.println("📍 Route calculated");
        System.out.println("➡️ Distance: " + snapshot.distanceKm + " km");
        System.out.println("⏱️ Time: " + snapshot.etaMinutes + " minutes");

        // 2️⃣ Hand snapshot to JavaFX app
        RoundTftApp.setInitialSnapshot(snapshot);

        // 3️⃣ Launch round TFT simulator
        RoundTftApp.launch(RoundTftApp.class);
    }
}
