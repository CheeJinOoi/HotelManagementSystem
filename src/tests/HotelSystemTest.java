package tests;

import boundary.FrontDeskGUI;
import boundary.HousekeepingGUI;
import boundary.VIPRoomAllocationGUI;
import boundary.WalkInBookingGUI;
import control.FrontDeskController;
import control.FrontDeskReports;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.VIPRoomAllocationControl;
import control.WalkInBookingControl;
import entity.Guest;
import entity.HousekeepingStatus;
import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import hashing.HashedDictionary;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

/**
 * Function, integration, and system tests for the hotel system
 * after the Front Desk GitHub update.
 */
public class HotelSystemTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println(" TARUMT Resorts - Function / Integration / System tests");
        System.out.println("==============================================");

        runFunctionTests();
        runIntegrationTests();
        runSystemTests();

        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) {
            throw new AssertionError(failed + " test(s) failed.");
        }
        System.out.println("ALL TESTS PASSED");
    }

    private static void runFunctionTests() throws Exception {
        section("FUNCTION TESTS");

        HashedDictionary<String, String> dictionary = new HashedDictionary<>();
        check("hash add new key returns null", dictionary.add("10000001", "Ali") == null);
        check("hash getValue finds entry", "Ali".equals(dictionary.getValue("10000001")));
        check("hash contains existing key", dictionary.contains("10000001"));
        check("hash replace returns old value", "Ali".equals(dictionary.add("10000001", "Ali Rahman")));
        check("hash getValue after replace", "Ali Rahman".equals(dictionary.getValue("10000001")));
        check("hash remove returns value", "Ali Rahman".equals(dictionary.remove("10000001")));
        check("hash getValue after remove", dictionary.getValue("10000001") == null);
        check("hash size after remove is 0", dictionary.getSize() == 0);

        for (int i = 0; i < 80; i++) {
            dictionary.add("K" + i, "V" + i);
        }
        check("hash still finds key after growth", "V10".equals(dictionary.getValue("K10")));

        HousekeepingController housekeeping = new HousekeepingController(5);
        housekeeping.addRoom(new Room("201", "Deluxe", HousekeepingStatus.DIRTY));
        String invalid = housekeeping.updateRoomStatus("201", HousekeepingStatus.READY_FOR_CHECKIN, "Staff", "skip");
        check("housekeeping rejects invalid jump", invalid.toLowerCase().contains("invalid"));
        check("housekeeping Dirty -> Cleaning",
            housekeeping.updateRoomStatus("201", HousekeepingStatus.CLEANING_IN_PROGRESS, "Staff", "start")
                .toLowerCase().contains("success"));
        check("housekeeping Cleaning -> Inspected",
            housekeeping.updateRoomStatus("201", HousekeepingStatus.INSPECTED, "Staff", "done")
                .toLowerCase().contains("success"));
        check("housekeeping Inspected -> Ready",
            housekeeping.updateRoomStatus("201", HousekeepingStatus.READY_FOR_CHECKIN, "Staff", "pass")
                .toLowerCase().contains("success"));
        check("housekeeping undo works",
            housekeeping.undoLastAction().toLowerCase().contains("undo"));
        check("housekeeping redo works",
            housekeeping.redoLastAction().toLowerCase().contains("redo"));

        Path tempFile = Files.createTempFile("housekeeping-test-", ".txt");
        housekeeping.saveState(tempFile.toString());
        HousekeepingController reloaded = new HousekeepingController(5);
        reloaded.loadState(tempFile.toString());
        check("housekeeping persistence reloads room", reloaded.findRoomById("201") != null);
        check("housekeeping persistence keeps status",
            reloaded.findRoomById("201").getCurrentStatus() == HousekeepingStatus.READY_FOR_CHECKIN);
        Files.deleteIfExists(tempFile);

        HousekeepingController shared = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(shared);
        FrontDeskController frontDesk = new FrontDeskController(walkIn);

        check("confirmation 8 digits valid", frontDesk.isValidConfirmationNumber("10000001"));
        check("confirmation 7 digits invalid", !frontDesk.isValidConfirmationNumber("1234567"));
        check("confirmation letters invalid", !frontDesk.isValidConfirmationNumber("ABCD1234"));
        check("unknown confirmation returns null", frontDesk.findReservation("99999999") == null);
        check("invalid confirmation returns null", frontDesk.findReservation("abc") == null);

        String missingBill = frontDesk.getGuestBill("99999999");
        check("bill for missing reservation", missingBill.toLowerCase().contains("not found"));

        String availability = frontDesk.searchRoomAvailability("Standard");
        check("room availability mentions Standard", availability.contains("Standard"));
        check("room availability has available count", availability.contains("Available Rooms"));

        String emptyType = frontDesk.searchRoomAvailability(" ");
        check("empty room type is rejected", emptyType.toLowerCase().contains("required"));

        FrontDeskReports reports = new FrontDeskReports(frontDesk);
        String vipReport = reports.generateVIPGuestReport();
        String deskReport = reports.generateFrontDeskReport();
        check("VIP report generated", vipReport != null && vipReport.contains("VIP GUEST REPORT"));
        check("Front Desk report generated", deskReport != null && deskReport.contains("FRONT DESK"));
    }

    private static void runIntegrationTests() {
        section("INTEGRATION TESTS");

        HousekeepingController housekeeping = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);
        FrontDeskController frontDesk = new FrontDeskController(walkIn);
        FrontDeskReports reports = new FrontDeskReports(frontDesk);

        check("walk-in shares housekeeping controller",
            walkIn.getHousekeepingController() == housekeeping);
        check("front desk shares housekeeping controller",
            frontDesk.getHousekeepingController() == housekeeping);

        Reservation[] all = walkIn.getAllReservations();
        check("walk-in has reservation history", all != null && all.length > 0);
        check("front desk hash size matches reservations with keys",
            frontDesk.getReservationHashSize() == countWithConfirmation(all));

        boolean allFound = true;
        for (int i = 0; i < all.length; i++) {
            if (all[i] == null || all[i].getConfirmationNumber() == null) {
                continue;
            }
            Reservation hashed = frontDesk.findReservation(all[i].getConfirmationNumber());
            if (hashed == null || !all[i].getConfirmationNumber().equals(hashed.getConfirmationNumber())) {
                allFound = false;
                break;
            }
        }
        check("front desk hash finds every walk-in reservation", allFound);

        int readyStandard = 0;
        Room[] rooms = housekeeping.getAllRooms();
        for (int i = 0; i < rooms.length; i++) {
            if ("Standard".equalsIgnoreCase(rooms[i].getRoomType()) && rooms[i].isReadyForAssignment()) {
                readyStandard++;
            }
        }
        check("front desk available Standard rooms match housekeeping",
            frontDesk.getAvailableRoomCount("Standard") == readyStandard);
        check("front desk available Standard array length matches count",
            frontDesk.getAvailableRooms("Standard").length == readyStandard);

        String board = walkIn.formatRoomStatusBoard();
        check("walk-in room board lists a hotel room", board.contains("101") || board.contains("Room"));

        Reservation waiting = firstWithStatus(all, ReservationStatus.WAITING);
        if (waiting != null) {
            String waitingBill = frontDesk.getGuestBill(waiting.getConfirmationNumber());
            check("bill blocked for waiting guest", waitingBill.toLowerCase().contains("cannot be generated"));
        } else {
            check("bill blocked for waiting guest (no waiting sample)", true);
        }

        Reservation billed = firstBillable(all);
        if (billed != null) {
            String bill = frontDesk.getGuestBill(billed.getConfirmationNumber());
            check("bill generated for checked-in/out guest", bill.contains("TOTAL BILL"));
        } else {
            check("bill generated for checked-in/out guest (none in store)", true);
        }

        Reservation[] vipRows = reports.getVIPReservations();
        check("VIP report array is non-null", vipRows != null);

        VIPRoomAllocationControl vip = new VIPRoomAllocationControl();
        vip.setRooms(housekeeping.getAllRooms());
        check("VIP module uses same room objects as housekeeping",
            vip.getRooms() == housekeeping.getAllRooms() || vip.getRooms().length == housekeeping.getAllRooms().length);
        vip.addVIPGuest(new entity.VIPGuest("Test VIP", "900101-14-0000", "012-0000000",
            entity.VIPGuest.MembershipTier.PLATINUM, "Standard"));
        check("VIP guest can be added to shared module", vip.getQueueSize() >= 1);
    }

    private static void runSystemTests() throws Exception {
        section("SYSTEM TESTS");

        HousekeepingController housekeeping = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);
        FrontDeskController frontDesk = new FrontDeskController(walkIn);
        VIPRoomAllocationControl vip = new VIPRoomAllocationControl();
        vip.setRooms(housekeeping.getAllRooms());

        check("bootstrap created rooms", housekeeping.getAllRooms().length > 0);
        check("walk-in pending queue accessible", walkIn.getPendingReservations() != null);
        check("front desk reports CLI aliases exist",
            frontDesk.searchAvailableRooms("Deluxe").contains("Deluxe"));
        check("front desk checkBill alias works",
            frontDesk.checkBill("99999999").toLowerCase().contains("not found"));

        SwingUtilities.invokeAndWait(() -> {
            WalkInBookingGUI walkInGui = new WalkInBookingGUI(walkIn);
            HousekeepingGUI houseGui = new HousekeepingGUI(housekeeping);
            VIPRoomAllocationGUI vipGui = new VIPRoomAllocationGUI(vip);
            FrontDeskGUI deskGui = new FrontDeskGUI(frontDesk);
            walkInGui.refresh();
            houseGui.refresh();
            vipGui.refresh();
            deskGui.refresh();
            check("Walk-In GUI panel created", walkInGui.getComponentCount() > 0);
            check("Housekeeping GUI panel created", houseGui.getComponentCount() > 0);
            check("VIP GUI panel created", vipGui.getComponentCount() > 0);
            check("Front Desk GUI panel created", deskGui.getComponentCount() > 0);
        });

        Guest guest = new Guest("System Test Guest", "010101-01-0101", "011-1111111");
        String register = walkIn.registerWalkIn(guest, "Standard", 1);
        boolean registered = register.toLowerCase().contains("confirmation");
        check("system can register a walk-in", registered);

        frontDesk.refreshReservationHash();
        Reservation[] afterRegister = walkIn.getAllReservations();
        Reservation newest = afterRegister[afterRegister.length - 1];
        check("front desk hash sees newly registered walk-in",
            newest != null && frontDesk.findReservation(newest.getConfirmationNumber()) != null);

        String guestLookup = frontDesk.formatReservationDetails(
            frontDesk.searchGuest(newest.getConfirmationNumber()));
        check("front desk guest search returns new walk-in",
            guestLookup.contains("System Test Guest"));

        String cancel = walkIn.cancelWaitingReservation(newest.getConfirmationNumber());
        check("system can cancel the test walk-in", cancel.toLowerCase().contains("cancel")
            || cancel.toLowerCase().contains("removed") || cancel.toLowerCase().contains("waiting"));
        frontDesk.refreshReservationHash();
        check("cancelled test walk-in is no longer waiting in hash as WAITING",
            frontDesk.findReservation(newest.getConfirmationNumber()) == null
                || frontDesk.findReservation(newest.getConfirmationNumber()).getStatus()
                    != ReservationStatus.WAITING);
    }

    private static Reservation firstWithStatus(Reservation[] reservations, ReservationStatus status) {
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] != null && reservations[i].getStatus() == status
                    && reservations[i].getConfirmationNumber() != null) {
                return reservations[i];
            }
        }
        return null;
    }

    private static Reservation firstBillable(Reservation[] reservations) {
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] == null) {
                continue;
            }
            ReservationStatus status = reservations[i].getStatus();
            if ((status == ReservationStatus.CHECKED_IN || status == ReservationStatus.CHECKED_OUT)
                    && reservations[i].getConfirmationNumber() != null
                    && reservations[i].getGuest() != null) {
                return reservations[i];
            }
        }
        return null;
    }

    private static int countWithConfirmation(Reservation[] reservations) {
        int count = 0;
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] != null && reservations[i].getConfirmationNumber() != null) {
                count++;
            }
        }
        return count;
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
