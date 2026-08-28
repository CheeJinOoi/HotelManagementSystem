package app;

import boundary.ConsoleStyle;
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
        HousekeepingController housekeeping = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);

        VIPRoomAllocationControl vipControl = new VIPRoomAllocationControl();
        vipControl.setRooms(housekeeping.getAllRooms());


        HotelUI hotelUI = new HotelUI();

        FrontDeskController frontDesk = new FrontDeskController(walkIn);

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
                    HotelGUI.open(walkIn, housekeeping, vipControl, frontDesk);
                    ConsoleStyle.info("GUI opening... close the window when finished.");
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