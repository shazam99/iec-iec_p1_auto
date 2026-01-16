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

- The Android application allows the user to search for and select a destination.
- The application continuously determines the user’s current location using GPS.
- The application retrieves a navigation route from the routing engine/server based on the current location and destination.
- The application packages and transmits navigation data to the ESP32 over BLE in fixed segments of up to 500 meters.

#### ESP32 Display Goals

- The ESP32 displays only the next 500 meters of the route at any given time.
- The vehicle position is represented by a white arrow with an orange border.
- The active route is rendered as a solid white road.
- Non-required nearby roads are rendered as fading white lines within a 10–20 meter range to provide situational context.
- Major roads or highways are rendered in yellow.
- Upcoming turns are indicated using green directional arrows.

### Explicit Non‑Goals

These items are intentionally excluded to keep the system simple, reliable, and focused on low‑distraction navigation, and to avoid scope creep or hardware limitations reopening settled design decisions.

- Full map browsing
- Voice navigation
- Touch input on ESP32

---

## 2. System Architecture (Logical, Version-Agnostic)

### High-Level Architecture

Android App

- GPS Location Provider
- Routing Engine
- Navigation Engine
- BLE Transmitter

ESP32 Device

- BLE Receiver
- JSON Parser
- Display Renderer

### Data Flow

Android → BLE → ESP32

Update frequency: defined by implementation (bounded by BLE and device constraints)

---

## 3. Project Scope Boundaries

This document intentionally avoids implementation-stage or version-specific commitments. Detailed milestones, feature matrices, and versioned guarantees are defined in separate versioned specifications.

At this level, IEC guarantees:

- Turn-by-turn navigation concepts
- Segmented route visualization
- Clear division of responsibilities between Android and ESP32

Items such as dynamic zoom, traffic-aware routing, or re-routing behavior are explicitly out of scope unless defined in a versioned specification.

---

## 4. Data Contracts 📦

### BLE Payload Contract

TBD

---

## 5. Module Contracts 🧩

### Android – NavigationEngine

Responsibilities:

- Maintain active route
- Extract next maneuver
- Provide simplified geometry for ESP32

Public API:

- calculateRoute(start: LatLng, end: LatLng)
- getNavigationData(currentLocation: LatLng, speedMps: Float): NavigationData

No other methods are permitted.

---

## 6. ESP32 Constraints ⚡

- Available RAM: \~320 KB
- JSON buffer size: ≤ 2 KB
- Floating-point operations must be minimized
- BLE update rate: max 1 Hz

---

## 7. Error Handling Rules 🛡️

The system follows a fail-safe design philosophy: the ESP32 must never crash, block, or enter an undefined state due to missing data, communication issues, or malformed input.

- GPS unavailable → send last valid navigation packet
- BLE disconnected → ESP32 shows waiting screen
- JSON parse error → packet ignored, no crash



