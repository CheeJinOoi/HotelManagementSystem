package adt;

import java.util.Iterator;

/**
 * QueueInterface.java
 * A linear FIFO collection ADT. Positions are 1-based from the front of the queue.
 *
 * Adapted from the course sample ListInterface style (Frank M. Carrano / ECBDemo).
 * Circular-array queue operations and add-on methods implemented for this assignment.
 *
 * @author vinsx
 * @version 1.0
 */
public interface QueueInterface<T> {

  /**
   * Task: Adds a new entry at the back of the queue.
   *
   * @param newEntry the object to be added
   * @return true if the addition is successful, or false if not
   */
  public boolean enqueue(T newEntry);

  /**
   * Task: Removes and returns the entry at the front of the queue.
   *
   * @return the front entry, or null if the queue is empty
   */
  public T dequeue();

  /**
   * Task: Retrieves the entry at the front of the queue without removing it.
   *
   * @return the front entry, or null if the queue is empty
   */
  public T getFront();

  /**
   * Task: Removes all entries from the queue.
   */
  public void clear();

  /**
   * Task: Sees whether the queue is empty.
   *
   * @return true if the queue is empty
   */
  public boolean isEmpty();

  /**
   * Task: Sees whether the queue is full.
   *
   * @return true if the queue is full
   */
  public boolean isFull();

  /**
   * Task: Gets the number of entries in the queue.
   *
   * @return the number of entries currently in the queue
   */
  public int getNumberOfEntries();

  /**
   * Task: Retrieves the entry at a given position from the front (1 = front).
   *
   * @param givenPosition a 1-based position from the front
   * @return the indicated entry, or null if the position is invalid
   */
  public T getEntry(int givenPosition);

  /**
   * Task: Sees whether the queue contains a given entry.
   *
   * @param anEntry the desired entry
   * @return true if the queue contains anEntry
   */
  public boolean contains(T anEntry);

  /**
   * Task: Gets the 1-based position of a given entry from the front.
   *
   * @param anEntry the desired entry
   * @return the 1-based position, or -1 if not found
   */
  public int indexOf(T anEntry);

  /**
   * Task: Removes the entry at a given 1-based position from the front.
   *
   * @param givenPosition a 1-based position from the front
   * @return the removed entry, or null if the position is invalid
   */
  public T remove(int givenPosition);

  /**
   * Task: Creates an iterator over this queue from front to back.
   *
   * @return an iterator for the client
   */
  public Iterator<T> getIterator();
}
