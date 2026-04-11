package com.javamastery.threading;

import java.util.concurrent.atomic.*;

/**
 * Demonstrates atomic classes for lock-free thread-safe operations.
 * Uses CAS (Compare-And-Swap) instructions under the hood.
 */
public class AtomicDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Atomic Operations Demo ===\n");

        // --- 1. AtomicInteger ---
        System.out.println("--- AtomicInteger ---");
        atomicIntegerDemo();

        // --- 2. AtomicReference ---
        System.out.println("\n--- AtomicReference ---");
        atomicReferenceDemo();

        // --- 3. LongAdder (high contention) ---
        System.out.println("\n--- LongAdder ---");
        longAdderDemo();

        // --- 4. AtomicStampedReference (ABA problem) ---
        System.out.println("\n--- AtomicStampedReference (solves ABA) ---");
        atomicStampedDemo();
    }

    static void atomicIntegerDemo() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        // incrementAndGet is atomic — no synchronization needed
        Runnable task = () -> {
            for (int i = 0; i < 10_000; i++) {
                counter.incrementAndGet();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("  Counter: " + counter.get() + " (expected 20000)");

        // CAS loop pattern — retry until successful
        int oldVal, newVal;
        do {
            oldVal = counter.get();
            newVal = oldVal * 2;
        } while (!counter.compareAndSet(oldVal, newVal));
        System.out.println("  After CAS doubling: " + counter.get());

        // Other useful methods
        System.out.println("  getAndAdd(5): " + counter.getAndAdd(5));
        System.out.println("  updateAndGet(x -> x + 10): " + counter.updateAndGet(x -> x + 10));
        System.out.println("  accumulateAndGet(3, Math::max): " + counter.accumulateAndGet(3, Math::max));
    }

    static void atomicReferenceDemo() {
        record Account(String owner, double balance) {}
        AtomicReference<Account> ref = new AtomicReference<>(new Account("Alice", 100.0));

        // Atomic update with CAS
        Account oldAccount = ref.get();
        Account newAccount = new Account(oldAccount.owner(), oldAccount.balance() + 50.0);
        boolean updated = ref.compareAndSet(oldAccount, newAccount);
        System.out.println("  Updated: " + updated + ", Balance: " + ref.get().balance());
    }

    static void longAdderDemo() throws InterruptedException {
        // LongAdder is better than AtomicLong under high contention
        // It uses cell striping — each thread writes to its own cell
        LongAdder adder = new LongAdder();

        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    adder.increment();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.println("  LongAdder sum: " + adder.sum() + " (expected 400000)");
        System.out.println("  Use LongAdder when: high contention, frequent writes, infrequent reads");
        System.out.println("  Use AtomicLong when: low contention, need exact get() value");
    }

    static void atomicStampedDemo() {
        // ABA Problem: Thread 1 sees A, Thread 2 changes A->B->A,
        // Thread 1's CAS succeeds but the value was modified!
        // AtomicStampedReference attaches a version stamp to detect this.

        AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

        int[] stampHolder = new int[1];
        String val = ref.get(stampHolder);
        System.out.println("  Initial: value=" + val + ", stamp=" + stampHolder[0]);

        // This CAS checks BOTH value AND stamp
        boolean success = ref.compareAndSet("A", "B", 0, 1);
        System.out.println("  CAS A->B: " + success + ", stamp now=" + ref.getStamp());

        success = ref.compareAndSet("B", "A", 1, 2);
        System.out.println("  CAS B->A: " + success + ", stamp now=" + ref.getStamp());

        // Stale stamp=0 fails even though value is "A" again
        success = ref.compareAndSet("A", "C", 0, 3);
        System.out.println("  CAS with stale stamp(0): " + success + " (detects ABA!)");
    }
}
