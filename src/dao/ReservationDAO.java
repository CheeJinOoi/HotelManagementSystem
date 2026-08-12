package dao;

import adt.CircularQueue;
import adt.QueueInterface;
import entity.Reservation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * ReservationDAO.java
 * DAO = Data Access Object: save / load reservation queues to a binary file.
 *
 * Same idea as ECBDemo ProductDAO (products.dat).
 * File used: data/reservations.dat
 *
 * @author vinsx
 */
public class ReservationDAO {

  private String fileName = "data/reservations.dat";

  /**
   * Write pending + history queues together using BookingStore.
   */
  public void saveToFile(QueueInterface<Reservation> pendingQueue,
      QueueInterface<Reservation> historyQueue) {
    File file = new File(fileName);
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }
    try {
      ObjectOutputStream ooStream = new ObjectOutputStream(new FileOutputStream(file));
      ooStream.writeObject(new BookingStore(pendingQueue, historyQueue));
      ooStream.close();
    } catch (FileNotFoundException ex) {
      System.out.println("\nFile not found");
    } catch (IOException ex) {
      System.out.println("\nCannot save to file");
    }
  }

  /**
   * Read saved queues. If the file does not exist yet, return empty queues
   * so the control class can seed sample demo data.
   */
  public BookingStore retrieveFromFile() {
    File file = new File(fileName);
    BookingStore store = new BookingStore(new CircularQueue<Reservation>(),
        new CircularQueue<Reservation>());
    try {
      ObjectInputStream oiStream = new ObjectInputStream(new FileInputStream(file));
      store = (BookingStore) oiStream.readObject();
      oiStream.close();
    } catch (FileNotFoundException ex) {
      System.out.println("\nNo saved reservations file. Starting with sample data.");
    } catch (IOException ex) {
      System.out.println("\nCannot read from file.");
    } catch (ClassNotFoundException ex) {
      System.out.println("\nClass not found.");
    }
    return store;
  }
}
