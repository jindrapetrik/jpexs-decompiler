package com.jpexs.proxy;

import java.util.*;

public class WorkerThread extends Thread {

    private Runnable task;
    private String name;

    private static int nactive = 0;
    private static Vector list = new Vector();
    private static int max = 10;
    private static long lifetime = 900 * 1000; /* 15 minute default */


    private
            WorkerThread() {
        setDaemon(true);
    }

    /**
     * Sets the lifetime of an idle WorkerThread (in ms). A WorkerThread will
     * remain on the idle list for this much time before exiting. This does not
     * affect WorkerThreads currently idling.
     */
    synchronized static void
            setLifetime(long time) {
        lifetime = time;
    }

    /**
     * Sets the maximum number of WorkerThreads that can exist at any given
     * time. If this value is decreased below the current number of
     * WorkerThreads, this will not take effect immediately.
     */
    synchronized static void
            setMaxThreads(int maxThreads) {
        max = maxThreads;
    }

    /**
     * Obtains a WorkerThread to which a task can be assigned. If an idle
     * WorkerThread is present, it is removed from the idle list and returned.
     * If not, and the maximum number of WorkerThreads has not been reached, a
     * new WorkerThread is created. If the maximum number has been reached, this
     * blocks until a WorkerThread is free.
     */
    static WorkerThread
            getThread() {
        WorkerThread t;
        synchronized (list) {
            if (list.size() > 0) {
                t = (WorkerThread) list.firstElement();
                list.removeElement(t);
            } else if (nactive >= max) {
                while (true) {
                    try {
                        list.wait();
                    } catch (InterruptedException e) {
                    }
                    if (list.size() == 0) {
                        continue;
                    }
                    t = (WorkerThread) list.firstElement();
                    list.removeElement(t);
                    break;
                }
            } else {
                t = new WorkerThread();
            }
            nactive++;
        }
        return t;
    }

    /**
     * Assigns a task to a WorkerThread
     *
     * @param task The task to be run
     * @param name The name of the task
     */
    public static void
            assignThread(Runnable task, String name) {
        while (true) {
            try {
                WorkerThread t = getThread();
                synchronized (t) {
                    t.task = task;
                    t.name = name;
                    if (!t.isAlive()) {
                        t.start();
                    } else {
                        t.notify();
                    }
                }
                return;
            } catch (IllegalThreadStateException e) {
            }
        }
    }

    /**
     * Performs the task
     */
    synchronized public void
            run() {
        while (true) {
            setName(name);
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println(t);
            }
            setName("idle thread");
            synchronized (list) {
                list.addElement(this);
                if (nactive >= max) {
                    list.notify();
                }
                nactive--;
            }
            task = null;
            try {
                wait(lifetime);
            } catch (InterruptedException e) {
            }
            if (task == null) {
                list.removeElement(this);
                return;
            }
        }
    }

}
