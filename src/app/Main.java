package app;

import boundary.HotelGUI;
import control.FrontDeskController;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.VIPRoomAllocationControl;
import control.WalkInBookingControl;

public class Main {
    public static void main(String[] args) {
        HousekeepingController housekeeping = HotelBootstrap.create();
        WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);

        VIPRoomAllocationControl vipControl = new VIPRoomAllocationControl();
        vipControl.setRooms(housekeeping.getAllRooms());


        Runtime.getRuntime().addShutdownHook(new Thread(() -> HotelBootstrap.save(housekeeping)));

        FrontDeskController frontDesk = new FrontDeskController(walkIn);

        HotelGUI.open(walkIn, housekeeping, vipControl, frontDesk);
    }
}