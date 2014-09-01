package com.jpexs.proxy;

public class ReusableThread extends Thread {

    private ThreadPool pool = null;
    private Runnable runnable = null;
    private boolean alive = true;
    private long lastrun = 0;
    private int used = 0;

    public ReusableThread(ThreadPool pool) {
        this.pool = pool;
    }

    public synchronized void setRunnable(Runnable runnable) {
        this.runnable = runnable;
        notify();
    }

    public synchronized void terminate() {
        alive = false;
        notify();
    }

    public long getLastRunTime() {
        return lastrun;
    }

    public int useCount() {
        return used;
    }

    public void run() {
        while (alive) {
            setName("ReusableThread: idle");

            while (runnable == null && alive) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

            if (alive) {
                setName("ReusableThread: busy");
                setPriority(Thread.NORM_PRIORITY);
                lastrun = System.currentTimeMillis();
                used++;
                runnable.run();
                runnable = null;
                pool.put(this);
            }
        }
    }
}
