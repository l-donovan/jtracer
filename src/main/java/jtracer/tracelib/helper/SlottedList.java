package jtracer.tracelib.helper;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlottedList<T> {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private T[] items;
    private boolean[] hasValue;

    @SuppressWarnings("unchecked")
    public SlottedList(int size) {
        this.items = (T[]) new Object[size];
        this.hasValue = new boolean[size];
    }

    public void set(int pos, T val) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (this.locked.get()) {
            if (System.currentTimeMillis() - startTime > 1e3) {
                throw new TimeoutException("Operation timed out while waiting for lock");
            }
        }
        this.locked.set(true);
        this.items[pos] = val;
        this.hasValue[pos] = true;
        this.locked.set(false);
    }

    public T get(int pos) {
        // `get` isn't locking or threadsafe. Designed to be run only after `isFull` is confirmed
        return this.items[pos];
    }

    public boolean isFull() throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (this.locked.get()) {
            if ((System.currentTimeMillis() - startTime) > 1e3) {
                throw new TimeoutException("Operation timed out while waiting for lock");
            }
        }
        this.locked.set(true);
        for (boolean b : this.hasValue) {
            if (!b) {
                this.locked.set(false);
                return false;
            }
        }
        this.locked.set(false);
        return true;
    }

    public void waitUntilFull() throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (true) {
            if ((System.currentTimeMillis() - startTime) >= 200) {
                if (this.isFull()) {
                    break;
                } else {
                    startTime = System.currentTimeMillis();
                }
            }
        }
    }
}
