package adt;

public class HeapPriorityQueue<T extends Comparable<T>> 
        implements PriorityQueueInterface<T> {
    
    private T[] heap;
    private int size;
    private static final int DEFAULT_CAPACITY = 100;
    
    @SuppressWarnings("unchecked")
    public HeapPriorityQueue() {
        heap = (T[]) new Comparable[DEFAULT_CAPACITY + 1];
        size = 0;
    }
    
    @Override
    public void enqueue(T element) {
        if (size == heap.length - 1) {
            resize();
        }
        size++;
        heap[size] = element;
        heapifyUp(size);
    }
    
    @Override
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T root = heap[1];
        heap[1] = heap[size];
        heap[size] = null;
        size--;
        heapifyDown(1);
        return root;
    }
    
    @Override
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[1];
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        for (int i = 1; i <= size; i++) {
            heap[i] = null;
        }
        size = 0;
    }
    
    private void heapifyUp(int index) {
        while (index > 1) {
            int parent = index / 2;
            if (heap[index].compareTo(heap[parent]) > 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }
    
    private void heapifyDown(int index) {
        while (index * 2 <= size) {
            int left = index * 2;
            int right = left + 1;
            int max = left;
            
            if (right <= size && heap[right].compareTo(heap[left]) > 0) {
                max = right;
            }
            
            if (heap[index].compareTo(heap[max]) < 0) {
                swap(index, max);
                index = max;
            } else {
                break;
            }
        }
    }
    
    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        T[] newHeap = (T[]) new Comparable[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }
}