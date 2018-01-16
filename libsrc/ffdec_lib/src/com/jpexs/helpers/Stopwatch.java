/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.helpers;

import java.util.Date;

/**
 *
 * @author JPEXS
 */
public class Stopwatch {

    private long startTime, elapsedTime;

    private boolean running;

    public Date startDate, endDate;

    public static Stopwatch startNew() {
        Stopwatch sw = new Stopwatch();
        sw.start();
        return sw;
    }

    public void start() {
        running = true;
        startDate = new Date();
        startTime = System.nanoTime();
    }

    public void stop() {
        elapsedTime = System.nanoTime() - startTime;
        endDate = new Date();
        running = false;
    }

    public long getElapsedNanoseconds() {
        if (running) {
            return System.nanoTime() - startTime;
        }
        return elapsedTime;
    }

    public long getElapsedMilliseconds() {
        return getElapsedNanoseconds() / 1000000;
    }
}
