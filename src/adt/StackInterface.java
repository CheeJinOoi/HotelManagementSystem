package adt;

import java.util.Iterator;

/**
 * StackInterface.java
 * A LIFO collection ADT. The top is the last item pushed.
 *
 * Basic stack operations are included for completeness and reuse,
 * even if a given module does not call every method.
 *
 * Adapted from the course sample collection-ADT style (Frank M. Carrano / ECBDemo).
 *
 * @author vinsx
 */
public interface StackInterface<T> {

  /**
   * Task: Adds a new entry to the top of the stack.
   *
   * @param newEntry the object to be added
   * @return true if the addition is successful
   */
  public boolean push(T newEntry);

  /**
   * Task: Removes and returns the stack's top entry.
   *
   * @return the top entry, or null if the stack is empty
   */
  public T pop();

  /**
   * Task: Retrieves the stack's top entry without removing it.
   *
   * @return the top entry, or null if the stack is empty
   */
  public T peek();

  /**
   * Task: Removes all entries from the stack.
   */
  public void clear();

  /**
   * Task: Sees whether the stack is empty.
   *
   * @return true if the stack is empty
   */
  public boolean isEmpty();

  /**
   * Task: Sees whether the stack is full.
   *
   * @return true if the stack cannot accept another entry without growing
   */
  public boolean isFull();

  /**
   * Task: Gets the number of entries in the stack.
   *
   * @return the number of entries currently in the stack
   */
  public int getNumberOfEntries();

  /**
   * Task: Sees whether the stack contains a given entry.
   *
   * @param anEntry the desired entry
   * @return true if the stack contains anEntry
   */
  public boolean contains(T anEntry);

  /**
   * Task: Creates an iterator over this stack from top to bottom.
   *
   * @return an iterator for the client
   */
  public Iterator<T> getIterator();
}
