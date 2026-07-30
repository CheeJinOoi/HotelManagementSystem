package entity;

public class TaskLog {
    private static final int INITIAL_CAPACITY = 10;
    private StatusEntry[] entries;
    private int size;

    public TaskLog() {
        this.entries = new StatusEntry[INITIAL_CAPACITY];
        this.size = 0;
    }

    public void add(StatusEntry entry) {
        ensureCapacity();
        entries[size++] = entry;
    }

    public StatusEntry removeLast() {
        if (isEmpty()) {
            return null;
        }
        StatusEntry removed = entries[--size];
        entries[size] = null; // help GC
        return removed;
    }

    public StatusEntry peekLast() {
        if (isEmpty()) {
            return null;
        }
        return entries[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public StatusEntry get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return entries[index];
    }

    public StatusEntry[] toArray() {
        StatusEntry[] result = new StatusEntry[size];
        for (int i = 0; i < size; i++) {
            result[i] = entries[i];
        }
        return result;
    }

    private void ensureCapacity() {
        if (size >= entries.length) {
            int newCapacity = entries.length * 2;
            StatusEntry[] newEntries = new StatusEntry[newCapacity];
            for (int i = 0; i < entries.length; i++) {
                newEntries[i] = entries[i];
            }
            entries = newEntries;
        }
    }
}
