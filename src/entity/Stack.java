package entity;

/**
 * Stack.java
 * Simple generic LIFO stack used by Housekeeping for undo / redo.
 *
 * Not from java.util — custom ADT required by the assignment style.
 * push = add on top, pop = remove from top, peek = look at top.
 */
public class Stack<T> {
    private static final int INITIAL_CAPACITY = 10;
    private Object[] elements;
    /** Next free index; also equals current size. */
    private int top;

    public Stack() {
        this.elements = new Object[INITIAL_CAPACITY];
        this.top = 0;
    }

    /** Place an item on top of the stack. */
    public void push(T item) {
        ensureCapacity();
        elements[top++] = item;
    }

    /** Remove and return the top item, or null if empty. */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T item = (T) elements[--top];
        elements[top] = null;
        return item;
    }

    /** Return the top item without removing it. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return (T) elements[top - 1];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    public int size() {
        return top;
    }

    /** Grow the array when full (dynamic resizing). */
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
}
