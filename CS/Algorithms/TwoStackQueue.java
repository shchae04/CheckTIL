package CS.Algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * A Queue implementation using two stacks with amortized O(1) operations.
 *
 * @param <E> element type
 */
public class TwoStackQueue<E> {
    private final Deque<E> inStack = new ArrayDeque<>();
    private final Deque<E> outStack = new ArrayDeque<>();

    /**
     * Enqueue an element to the tail of the queue.
     */
    public void offer(E e) {
        inStack.push(e);
    }

    /**
     * Returns, but does not remove, the head of the queue.
     * @throws NoSuchElementException if the queue is empty
     */
    public E peek() {
        moveIfNeeded();
        if (outStack.isEmpty()) throw new NoSuchElementException("Queue is empty");
        return outStack.peek();
    }

    /**
     * Dequeue and return the head of the queue.
     * @throws NoSuchElementException if the queue is empty
     */
    public E poll() {
        moveIfNeeded();
        if (outStack.isEmpty()) throw new NoSuchElementException("Queue is empty");
        return outStack.pop();
    }

    /**
     * @return true if no elements are present
     */
    public boolean isEmpty() {
        return inStack.isEmpty() && outStack.isEmpty();
    }

    /**
     * @return current number of elements in the queue
     */
    public int size() {
        return inStack.size() + outStack.size();
    }

    /**
     * Removes all elements from the queue.
     */
    public void clear() {
        inStack.clear();
        outStack.clear();
    }

    private void moveIfNeeded() {
        if (outStack.isEmpty()) {
            while (!inStack.isEmpty()) {
                outStack.push(inStack.pop());
            }
        }
    }

    /**
     * Simple demonstration.
     */
    public static void main(String[] args) {
        TwoStackQueue<Integer> q = new TwoStackQueue<>();
        q.offer(1);
        q.offer(2);
        q.offer(3);
        System.out.println(q.poll()); // 1
        System.out.println(q.peek()); // 2
        System.out.println(q.poll()); // 2
        q.offer(4);
        System.out.println(q.poll()); // 3
        System.out.println(q.poll()); // 4
        System.out.println(q.isEmpty()); // true
    }
}
