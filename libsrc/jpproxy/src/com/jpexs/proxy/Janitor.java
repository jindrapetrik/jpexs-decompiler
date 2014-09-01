package com.jpexs.proxy;

import java.util.Enumeration;
import java.util.Vector;

class Janitor implements Runnable {

    private Vector cleanable = new Vector();

    public void add(Cleanable c) {
        cleanable.addElement(c);
    }

    public void run() {
        Thread.currentThread().setName("Janitor");

        for (;;) {
            try {
                Thread.sleep(30 * 1000); /* 30 seconds */

            } catch (Exception e) {

            }

            for (Enumeration e = cleanable.elements();
                    e.hasMoreElements();) {
                ((Cleanable) e.nextElement()).clean();
            }

            Http.clean();

        }
    }
}
