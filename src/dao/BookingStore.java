package dao;

import adt.QueueInterface;
import entity.Reservation;
import java.io.Serializable;

/**
 * BookingStore.java
 * Simple serializable box that holds BOTH queues so they can be written
 * to one file in a single ObjectOutputStream.writeObject(...) call.
 *
 * pendingQueue  = waiting guests (FIFO)
 * historyQueue  = all bookings for reports
 *
 * @author vinsx
 */
public class BookingStore implements Serializable {

  private QueueInterface<Reservation> pendingQueue;
  private QueueInterface<Reservation> historyQueue;

  public BookingStore() {
  }

  public BookingStore(QueueInterface<Reservation> pendingQueue,
      QueueInterface<Reservation> historyQueue) {
    this.pendingQueue = pendingQueue;
    this.historyQueue = historyQueue;
  }

  public QueueInterface<Reservation> getPendingQueue() {
    return pendingQueue;
  }

  public void setPendingQueue(QueueInterface<Reservation> pendingQueue) {
    this.pendingQueue = pendingQueue;
  }

  public QueueInterface<Reservation> getHistoryQueue() {
    return historyQueue;
  }

  public void setHistoryQueue(QueueInterface<Reservation> historyQueue) {
    this.historyQueue = historyQueue;
  }
}
