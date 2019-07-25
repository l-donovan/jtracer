package jtracer.tracelib.helper;

import java.util.EmptyStackException;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class PreloadedQueue<T> {
    private Vector<T> items;
    private final AtomicInteger ticketNumber = new AtomicInteger(0);
    private final AtomicInteger currentTicket = new AtomicInteger(0);

    public PreloadedQueue() {
        this.items = new Vector<>();
    }

    private int takeATicket() {
        return this.ticketNumber.getAndIncrement();
    }

    public void push(T item) {
        this.items.add(item);
    }

    public T pop() throws EmptyStackException, TimeoutException {
        int ticket = this.takeATicket();
        long startTime = System.currentTimeMillis();

        try {
            while (this.currentTicket.intValue() != ticket) {
                if ((System.currentTimeMillis() - startTime) > 1e3) {
                    throw new TimeoutException("Operation timed out while waiting for lock");
                }
            }

            if (this.items.isEmpty()) {
                throw new EmptyStackException();
            }

            return this.items.remove(0);
        } finally {
            this.currentTicket.getAndIncrement();
        }
    }
}
