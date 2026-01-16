# IEC Project – Single Source of Truth

> This document is the single source of truth for the IEC project.
> Any code, design, or discussion must conform to this document.
> If something is not defined here, it does not exist.

---

## 1. Project Overview

### Purpose

IEC solves the problem of riders and drivers needing clear, distraction-free navigation without relying on a full smartphone map UI while in motion.

IEC is a navigation system consisting of an Android application and an ESP32-based external display device. The system provides turn-by-turn navigation and route visualization on a dedicated display.

### Goals

#### Android Application Goals

* The Android application allows the user to search for and select a destination.
* The application continuously determines the user’s current location using GPS.
* The application retrieves a navigation route from the routing engine/server based on the current location and destination.
* The application packages and transmits navigation data to the ESP32 over BLE in fixed segments of up to 500 meters.

#### ESP32 Display Goals

* The ESP32 displays only the next 500 meters of the route at any given time.
* The vehicle position is represented by a white arrow with an orange border.
* The active route is rendered as a solid white road.
* Non-required nearby roads are rendered as fading white lines within a 10–20 meter range to provide situational context.
* Major roads or highways are rendered in yellow.
* Upcoming turns are indicated using green directional arrows.

### Explicit Non‑Goals

These items are intentionally excluded to keep the system simple, reliable, and focused on low‑distraction navigation, and to avoid scope creep or hardware limitations reopening settled design decisions.

* Full map browsing
* Voice navigation
* Touch input on ESP32

---

## 2. System Architecture (Logical, Version-Agnostic)

### High-Level Architecture

Android App

* GPS Location Provider
* Routing Engine
* Navigation Engine
* BLE Transmitter

ESP32 Device

* BLE Receiver
* JSON Parser
* Display Renderer

### Data Flow

Android → BLE → ESP32

Update frequency: defined by implementation (bounded by BLE and device constraints)

---

## 3. Project Scope Boundaries

This document intentionally avoids implementation-stage or version-specific commitments. Detailed milestones, feature matrices, and versioned guarantees are defined in separate versioned specifications.

At this level, IEC guarantees:

* Turn-by-turn navigation concepts
* Segmented route visualization
* Clear division of responsibilities between Android and ESP32

Items such as dynamic zoom, traffic-aware routing, or re-routing behavior are explicitly out of scope unless defined in a versioned specification.

---

## 4. Data Contracts 📦

### BLE Payload Contract

TBD

---

## 5. Module Contracts 🧩

### Android – NavigationEngine

Responsibilities:

* Maintain active route
* Extract next maneuver
* Provide simplified geometry for ESP32

Public API:

* calculateRoute(start: LatLng, end: LatLng)
* getNavigationData(currentLocation: LatLng, speedMps: Float): NavigationData

No other methods are permitted.

---

## 6. ESP32 Constraints ⚡

* Available RAM: ~320 KB
* JSON buffer size: ≤ 2 KB
* Floating-point operations must be minimized
* BLE update rate: max 1 Hz

---

## 7. Error Handling Rules 🛡️

The system follows a fail-safe design philosophy: the ESP32 must never crash, block, or enter an undefined state due to missing data, communication issues, or malformed input.

* GPS unavailable → send last valid navigation packet
* BLE disconnected → ESP32 shows waiting screen
* JSON parse error → packet ignored, no crash

---

## Phase v6 🧱

### Summary

Phase v6 represents the first end-to-end, working integration of navigation logic and display rendering. At this stage, the system has moved beyond conceptual architecture and now operates as a cohesive pipeline from navigation state generation to on-device visualization.

The primary achievement of this phase is the establishment of a clean internal data flow: navigation data is computed, normalized into a snapshot model, serialized for transport, and rendered deterministically on a constrained display. Core responsibilities are clearly separated, enabling future iteration without architectural churn.

**Current State (v6):**

* Navigation logic is functional and produces consistent maneuver and path data
* A snapshot-based model decouples navigation, serialization, and rendering
* JSON generation for BLE transmission is implemented
* TFT rendering reliably visualizes route, position, and maneuvers

**What Has Been Achieved So Far:**

* Stable internal navigation engine abstraction
* Deterministic route and maneuver extraction
* Defined boundary between domain logic and UI rendering
* A code structure suitable for incremental feature development and optimization

This section documents the current codebase structure and responsibilities of each file as uploaded. It reflects the actual implementation state and is descriptive, not prescriptive.

### Directory Structure

```
iec/
├── Main.java
├── model/
│   └── NavigationSnapshot.java
├── nav/
│   ├── NavigationEngine.java
│   ├── NavigationState.java
│   ├── RouteExtractor.java
│   ├── ManeuverExtractor.java
│   ├── NavJsonBuilder.java
│   ├── GeoUtil.java
│   └── BearingUtil.java
└── ui/
    ├── RoundTftApp.java
    ├── TftRenderer.java
    └── NavState.java
```

### File Descriptions

**Main.java**
Application entry point. Wires together the navigation engine, UI layer, and snapshot flow. Responsible for bootstrapping the system and driving update cycles.

**model/NavigationSnapshot.java**
Immutable data model representing a complete navigation state at a point in time. Acts as the boundary object between navigation logic and rendering/serialization layers.

**nav/NavigationEngine.java**
Core orchestration layer for navigation. Owns the routing engine integration, manages route lifecycle, and produces high-level navigation state used by downstream components.

**nav/NavigationState.java**
Mutable internal state used by the navigation engine to track progress along the route, current position, and maneuver context.

**nav/RouteExtractor.java**
Extracts and simplifies the forward-looking route geometry from the routing engine output. Responsible for limiting distance and point count.

**nav/ManeuverExtractor.java**
Derives the next maneuver (turn type, distance, bearing) from the active route and current position.

**nav/NavJsonBuilder.java**
Serializes a NavigationSnapshot into a compact JSON representation suitable for BLE transmission to the ESP32.

**nav/GeoUtil.java**
Geospatial utility functions including distance calculations, point interpolation, and coordinate math shared across navigation components.

**nav/BearingUtil.java**
Utility for bearing and heading calculations used for maneuver determination and orientation logic.

**ui/RoundTftApp.java**
High-level UI controller for the round TFT display. Converts navigation snapshots into renderable UI state and coordinates rendering updates.

**ui/TftRenderer.java**
Low-level rendering engine responsible for drawing roads, paths, arrows, and indicators onto the TFT display.

**ui/NavState.java**
UI-specific state model derived from NavigationSnapshot. Decouples rendering concerns from navigation-domain data.
