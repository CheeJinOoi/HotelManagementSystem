package app;

import boundary.ConsoleUI;
import boundary.HotelGUI;
import boundary.HotelUI;
import boundary.VIPConsoleUI;
import control.FrontDeskController;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.VIPRoomAllocationControl;
import control.WalkInBookingControl;
import utility.MessageUI;

public class HotelMain {

    public static void main(String[] args) {
        // 1. Create shared rooms (managed by Housekeeping)
        HousekeepingController housekeeping = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);

        // 2. Create VIP module
        VIPRoomAllocationControl vipControl = new VIPRoomAllocationControl();

        // 3. Share the same room list with VIP module
        vipControl.setRooms(housekeeping.getAllRoomsList());

        // 4. Add test VIP data
        vipControl.addTestData();

        HotelUI hotelUI = new HotelUI();

        //Front Desk uses SAME Walk-In data
        FrontDeskController frontDesk = new FrontDeskController(walkIn);

        // Save housekeeping state on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> HotelBootstrap.save(housekeeping)));

        int choice = 0;
        do {
            choice = hotelUI.getMenuChoice();
            switch (choice) {
                case 0:
                    HotelBootstrap.save(housekeeping);
                    MessageUI.displayHotelExitMessage();
                    break;
                case 1:
                    HotelGUI.open(walkIn, housekeeping, vipControl,frontDesk);
                    System.out.println("GUI opened. Close the window when finished.");
                    break;
                case 2:
                    walkIn.runWalkInBooking();
                    break;
                case 3:
                    new ConsoleUI(housekeeping).start();
                    break;
                case 4:
                    new VIPConsoleUI(vipControl).start();
                    break;
                case 5:
                    frontDesk.runFrontDesk();
                    break;
                default:
                    MessageUI.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }
}