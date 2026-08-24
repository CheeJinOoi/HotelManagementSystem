# Housekeeping and Task Log Module

## Presentation Script

Good morning/afternoon. My individual module is the **Housekeeping and Task Log** module. Its purpose is to track the cleaning progress of hotel rooms, keep a history of housekeeping actions, support undo and redo, and produce management reports.

My main control class is `HousekeepingController.java`. The module follows the Entity-Control-Boundary architecture:

- **Entity:** `Room`, `HousekeepingStatus`, `StatusEntry`, `UndoRecord`, and `TaskLog` store the data.
- **Control:** `HousekeepingController` contains the business rules and algorithms.
- **Boundary:** `ConsoleUI`, `HousekeepingGUI`, and `Reporter` handle user interaction and display.

## 1. Housekeeping Status Workflow

Each room follows a controlled sequence:

```text
DIRTY -> CLEANING_IN_PROGRESS -> INSPECTED -> READY_FOR_CHECKIN
```

`HousekeepingStatus` controls the valid next and previous states. The method `canTransitionTo()` prevents invalid jumps, such as moving directly from Dirty to Ready.

When a valid update is made, the controller:

1. Finds the room by room ID.
2. Checks whether the status transition is valid.
3. Creates a `StatusEntry` containing the status, time, staff member, and note.
4. Adds the entry to the room's `TaskLog`.
5. Stores an `UndoRecord` for rollback.

## 2. Linear ADTs and Undo/Redo

The task history is stored in `TaskLog`, which is a custom array-based linear structure. It supports adding entries, removing the last entry, viewing the last entry, and converting the history to an array.

Undo and redo use the custom `ArrayStack` ADT:

- `undoStack` stores completed actions.
- `redoStack` stores actions that have been undone.
- The latest action is removed first, so the behavior is LIFO: Last In, First Out.

For example, if the room changes from Dirty to Cleaning and then from Cleaning to Inspected, undo first changes it back from Inspected to Cleaning.

When a new action is performed after an undo, the redo stack is cleared because the old redo history is no longer valid.

## 3. Searching Algorithm

The method `searchRooms()` performs an explicit linear search through the room array.

It can filter using multiple criteria:

- Housekeeping status
- Room type
- Occupancy

The algorithm checks every room and counts matching rooms first. It then creates an appropriately sized array and stores the matching rooms in a second pass. No `java.util` collection is used.

The simpler `findRoomById()` method is also a linear search used for room updates and lookups.

## 4. Sorting Algorithm

The method `insertionSortByTaskCount()` implements insertion sort manually.

The reports are sorted by the number of task-log entries, with the rooms having the highest number of task entries shown first. Insertion sort works by taking one room at a time and shifting earlier rooms until the correct position is found.

Its time complexity is:

- Best case: O(n)
- Average case: O(n^2)
- Worst case: O(n^2)

This is suitable for this module because the room list is small and the assignment requires an explicit sorting algorithm.

## 5. Management Reports

### Report 1: Status Workload Report

The method is `generateStatusWorkloadReport()`.

It combines:

- Linear searching with status, room type, and occupancy filters
- Insertion sorting by task-log size
- Analytical output showing room status, occupancy, and task count

This report helps a supervisor identify rooms requiring more housekeeping attention.

### Report 2: Task History Report

The method is `generateTaskHistoryReport()`.

It combines:

- Linear searching by room type
- Filtering by minimum task-log entries
- Insertion sorting by task-log size
- Output showing current status, task count, and the last staff member who updated the room

This report helps identify rooms with frequent or significant housekeeping activity.

Both reports can be accessed through the console housekeeping menu and the Swing housekeeping GUI.

## 6. Data Persistence

`saveState()` writes room information and task-log entries to a text file. `loadState()` reads the file line by line and reconstructs the rooms and their histories.

The implementation uses arrays and file streams instead of `List` or other Java collection classes, following the project restriction.

## 7. Demonstration Flow

I would demonstrate the module in this order:

1. Open the Housekeeping module.
2. Select a Dirty room.
3. Change it to Cleaning In Progress.
4. Change it to Inspected.
5. Change it to Ready for Check-In.
6. Use Undo and show that it returns to the previous status.
7. Use Redo and show that the status is restored.
8. Open the Status Workload Report and apply filters.
9. Open the Task History Report and set a minimum task count.
10. Show that the results are sorted by task-log activity.

## 8. Important Files

- `src/control/HousekeepingController.java`: Main control logic, search, sorting, reports, and persistence
- `src/entity/HousekeepingStatus.java`: Valid status workflow
- `src/entity/TaskLog.java`: Custom task-history linear structure
- `src/adt/ArrayStack.java`: Custom stack used for undo and redo
- `src/entity/UndoRecord.java`: Stores rollback information
- `src/entity/StatusEntry.java`: Stores each task-log event
- `src/boundary/ConsoleUI.java`: Console menu and report access
- `src/boundary/HousekeepingGUI.java`: Graphical interface and report dialogs

## 9. Testing Evidence

The project test suite verifies:

- Invalid status transitions are rejected.
- All valid status transitions work.
- Undo and redo work.
- Room searching with criteria works.
- Both housekeeping reports are generated.
- Room and task history persistence works.
- The GUI panels can be created.

Latest result:

```text
57 tests passed, 0 failed
```

## Possible Lecturer Questions

### Why did you use a stack for undo?

Undo follows LIFO behavior. The most recent action must be reversed first, which is exactly the behavior of a stack.

### Why did you use arrays instead of ArrayList?

The assignment restricts Java collection classes. Arrays and custom ADTs also make the data structure and algorithm logic explicit.

### Why is this a linear search?

The algorithm checks rooms one by one from the beginning of the array until all rooms have been evaluated. Therefore, its worst-case time complexity is O(n).

### Why did you choose insertion sort?

The room list is small, and insertion sort is simple to implement and clearly demonstrates the sorting algorithm without using `Collections.sort()`.

### How do the reports combine search and sort?

Each report first obtains matching rooms through `searchRooms()` and then sorts the matching array using `insertionSortByTaskCount()` before formatting the output.

### What happens if an invalid status transition is requested?

`HousekeepingStatus.canTransitionTo()` rejects the change, so no task-log entry or undo record is created.

## Closing Statement

In conclusion, my module demonstrates a complete housekeeping workflow using custom linear ADTs, explicit searching and sorting algorithms, undo and redo functionality, persistence, and two analytical management reports while following the ECB architecture and avoiding Java collection classes.
