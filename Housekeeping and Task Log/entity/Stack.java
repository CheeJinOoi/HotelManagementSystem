package entity;

public class Stack<T> {
    private static final int INITIAL_CAPACITY = 10;
    private Object[] elements;
    private int top;

    public Stack() {
        this.elements = new Object[INITIAL_CAPACITY];
        this.top = 0;
    }

    public void push(T item) {
        ensureCapacity();
        elements[top++] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T item = (T) elements[--top];
        elements[top] = null;
        return item;
    }

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
