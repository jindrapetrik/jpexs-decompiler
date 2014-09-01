package com.jpexs.proxy;

import java.util.*;

public class ThreadPool implements Cleanable {

    private String name;
    private Vector pool = new Vector();

    public ThreadPool(String name) {
        this.name = name;
    }

    public synchronized ReusableThread get() {
        ReusableThread rt = null;

        if (pool.size() > 0) {
            rt = (ReusableThread) pool.firstElement();
            pool.removeElement(rt);
        }

        if (rt == null) {
            rt = new ReusableThread(this);
            rt.start();
        }

        return rt;
    }

    public synchronized void put(ReusableThread rt) {
        pool.addElement(rt);
    }

    public synchronized void clean() {
        long now = System.currentTimeMillis();

        for (Enumeration e = pool.elements(); e.hasMoreElements();) {
            ReusableThread rt = (ReusableThread) e.nextElement();
            if (now - rt.getLastRunTime() >= 30000) {
                rt.terminate();
                pool.removeElement(rt);
            }
        }
    }
}
