# TARUMT Resorts - Hotel Management System

BMCS2063 combined prototype (Walk-In booking + Housekeeping) in one ECB-style project.

## How to run

### Method 1 — Double-click

Double-click **`run.bat`**

### Method 2 — Terminal

```powershell
cd "C:\Users\vinsx\Desktop\HotelManagementSystem"
.\run.bat
```

Or:

```powershell
.\run.ps1
```

### Hotel menu

| Choice | Opens |
|---|---|
| 1 | GUI |
| 2 | Walk-In (console) |
| 3 | Housekeeping (console) |
| 0 | Quit |

In the GUI, switch tabs between **Walk-In** and **Housekeeping**. Both share the same rooms.

## Project layout

```
HotelManagementSystem/
  src/
    adt/         Circular queue (team ADT)
    app/         HotelMain (entry point)
    boundary/    Console + GUI screens
    control/     Business logic
    dao/         Save / load reservations
    entity/      Guest, Reservation, Room, ...
    utility/     Shared messages
    tests/       Simple tests
  run.bat / run.ps1
  README.md
```

`bin/` and `data/` are created automatically when you run.

## Modules inside this project

- **Walk-In:** `WalkInBookingControl`, `WalkInBookingUI`, `WalkInBookingGUI`, `CircularQueue`
- **Housekeeping:** `HousekeepingController`, `ConsoleUI`, `HousekeepingGUI`, `Stack`

Both share the same `Room` list via `HotelBootstrap`.
