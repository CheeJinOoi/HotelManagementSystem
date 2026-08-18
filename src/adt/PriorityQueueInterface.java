package adt;

/**
 * PriorityQueueInterface.java
 * A priority queue ADT. Highest-priority entry is served first.
 *
 * Adapted from the course sample collection-ADT style (Frank M. Carrano / ECBDemo).
 */
public interface PriorityQueueInterface<T extends Comparable<T>> {
    void enqueue(T element);
    T dequeue();
    T peek();
    boolean isEmpty();
    boolean isFull();
    int size();
    void clear();
}