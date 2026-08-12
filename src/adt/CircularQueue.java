package adt;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * CircularQueue.java
 * Team Linear ADT for this assignment: a FIFO queue stored in a circular array.
 *
 * Why circular array?
 * - enqueue at the back, dequeue at the front
 * - front/back indexes wrap with % capacity so empty slots at the start can be reused
 * - when the array is full, capacity is doubled (same idea as ECBDemo ArrayList)
 *
 * Positions for getEntry/remove/indexOf are 1-based from the front (1 = next to serve).
 *
 * Adapted from the course sample ArrayList resizing approach (Frank M. Carrano / ECBDemo).
 *
 * @author vinsx
 * @version 1.0
 */
public class CircularQueue<T> implements QueueInterface<T>, Serializable {

  /** Underlying circular storage. */
  private T[] array;
  /** Index of the front element (next to dequeue). */
  private int front;
  /** Index of the next free slot at the back (next enqueue position). */
  private int back;
  /** How many elements are currently stored. */
  private int numberOfEntries;
  private static final int DEFAULT_CAPACITY = 5;

  public CircularQueue() {
    this(DEFAULT_CAPACITY);
  }

  @SuppressWarnings("unchecked")
  public CircularQueue(int initialCapacity) {
    numberOfEntries = 0;
    front = 0;
    back = 0;
    array = (T[]) new Object[initialCapacity];
  }

  @Override
  public boolean enqueue(T newEntry) {
    // If full, grow the array first (queue is never permanently full)
    if (isArrayFull()) {
      doubleCapacity();
    }
    // Place at back, then move back forward with wrap-around
    array[back] = newEntry;
    back = (back + 1) % array.length;
    numberOfEntries++;
    return true;
  }

  @Override
  public T dequeue() {
    T frontEntry = null;
    if (!isEmpty()) {
      // Take front item, clear the slot, move front forward with wrap-around
      frontEntry = array[front];
      array[front] = null;
      front = (front + 1) % array.length;
      numberOfEntries--;
    }
    return frontEntry;
  }

  @Override
  public T getFront() {
    T frontEntry = null;
    if (!isEmpty()) {
      frontEntry = array[front];
    }
    return frontEntry;
  }

  @Override
  public void clear() {
    while (!isEmpty()) {
      dequeue();
    }
    front = 0;
    back = 0;
  }

  @Override
  public boolean isEmpty() {
    return numberOfEntries == 0;
  }

  @Override
  public boolean isFull() {
    return false;
  }

  @Override
  public int getNumberOfEntries() {
    return numberOfEntries;
  }

  @Override
  public T getEntry(int givenPosition) {
    T result = null;
    if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
      int index = (front + givenPosition - 1) % array.length;
      result = array[index];
    }
    return result;
  }

  @Override
  public boolean contains(T anEntry) {
    return indexOf(anEntry) != -1;
  }

  @Override
  public int indexOf(T anEntry) {
    int position = -1;
    boolean found = false;
    for (int i = 1; !found && i <= numberOfEntries; i++) {
      T current = getEntry(i);
      if (anEntry == null) {
        if (current == null) {
          found = true;
          position = i;
        }
      } else if (anEntry.equals(current)) {
        found = true;
        position = i;
      }
    }
    return position;
  }

  @Override
  public T remove(int givenPosition) {
    T result = null;
    if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
      int removeIndex = (front + givenPosition - 1) % array.length;
      result = array[removeIndex];
      shiftForward(givenPosition);
      numberOfEntries--;
      back = (front + numberOfEntries) % array.length;
    }
    return result;
  }

  @Override
  public Iterator<T> getIterator() {
    return new QueueIterator();
  }

  @Override
  public String toString() {
    String outputStr = "";
    for (int i = 1; i <= numberOfEntries; i++) {
      outputStr += getEntry(i) + "\n";
    }
    return outputStr;
  }

  private boolean isArrayFull() {
    return numberOfEntries == array.length;
  }

  @SuppressWarnings("unchecked")
  private void doubleCapacity() {
    T[] oldArray = array;
    int oldCapacity = oldArray.length;
    array = (T[]) new Object[oldCapacity * 2];
    for (int i = 0; i < numberOfEntries; i++) {
      array[i] = oldArray[(front + i) % oldCapacity];
    }
    front = 0;
    back = numberOfEntries;
  }

  /**
   * Task: Shifts entries after the removed position one slot toward the front,
   * wrapping around the circular array.
   */
  private void shiftForward(int givenPosition) {
    int lastIndex = numberOfEntries - 1;
    for (int offset = givenPosition - 1; offset < lastIndex; offset++) {
      int currentIndex = (front + offset) % array.length;
      int nextIndex = (front + offset + 1) % array.length;
      array[currentIndex] = array[nextIndex];
    }
    int lastCircularIndex = (front + lastIndex) % array.length;
    array[lastCircularIndex] = null;
  }

  private class QueueIterator implements Iterator<T> {
    private int nextPosition = 1;

    @Override
    public boolean hasNext() {
      return nextPosition <= numberOfEntries;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more entries in the queue.");
      }
      T nextEntry = getEntry(nextPosition);
      nextPosition++;
      return nextEntry;
    }
  }
}
