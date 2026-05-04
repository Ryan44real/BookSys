package com.tem.booksys.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class BookLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(String bookId) {
        return locks.computeIfAbsent(bookId, k -> new ReentrantLock());
    }

    public void removeLock(String bookId) {
        ReentrantLock lock = locks.get(bookId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        // Only remove if not currently in use
        if (lock != null && !lock.isLocked()) {
            locks.remove(bookId);
        }
    }
}
