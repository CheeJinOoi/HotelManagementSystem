package adt;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ArrayStack.java
 * Array implementation of StackInterface (LIFO).
 *
 * Implementation approach: linear array with dynamic resizing.
 * Adapted from the course sample ArrayList resizing approach (Frank M. Carrano / ECBDemo).
 *
 * @author vinsx
 */
public class ArrayStack<T> implements StackInterface<T> {

  private static final int INITIAL_CAPACITY = 10;
  private Object[] elements;
  private int top;

  public ArrayStack() {
    this.elements = new Object[INITIAL_CAPACITY];
    this.top = 0;
  }

  @Override
  public boolean push(T newEntry) {
    if (newEntry == null) {
      return false;
    }
    ensureCapacity();
    elements[top++] = newEntry;
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T pop() {
    if (isEmpty()) {
      return null;
    }
    T item = (T) elements[--top];
    elements[top] = null;
    return item;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T peek() {
    if (isEmpty()) {
      return null;
    }
    return (T) elements[top - 1];
  }

  @Override
  public void clear() {
    for (int i = 0; i < top; i++) {
      elements[i] = null;
    }
    top = 0;
  }

  @Override
  public boolean isEmpty() {
    return top == 0;
  }

  @Override
  public boolean isFull() {
    return false;
  }

  @Override
  public int getNumberOfEntries() {
    return top;
  }

  @Override
  public boolean contains(T anEntry) {
    if (anEntry == null) {
      return false;
    }
    for (int i = 0; i < top; i++) {
      if (anEntry.equals(elements[i])) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<T> getIterator() {
    return new StackIterator();
  }

  private void ensureCapacity() {
    if (top >= elements.length) {
      int newCapacity = elements.length * 2;
      Object[] newElements = new Object[newCapacity];
      for (int i = 0; i < elements.length; i++) {
        newElements[i] = elements[i];
      }
      elements = newElements;
    }
  }

  private class StackIterator implements Iterator<T> {
    private int nextIndex = top - 1;

    @Override
    public boolean hasNext() {
      return nextIndex >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return (T) elements[nextIndex--];
    }
  }
}
