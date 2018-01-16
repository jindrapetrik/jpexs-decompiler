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
package com.jpexs.helpers.stat;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.Stopwatch;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class Statistics implements AutoCloseable {

    private static final Map<String, StatisticData> map = new HashMap<>();

    private final String name;

    private final Stopwatch sw;

    private static synchronized void addTime(String name, long duration) {
        StatisticData s = map.get(name);
        if (s == null) {
            s = new StatisticData();
            map.put(name, s);
        }

        s.value += duration;
        if (duration > s.max) {
            s.max = duration;
        }

        if (duration < s.min) {
            s.min = duration;
        }

        s.count++;
    }

    public static synchronized void addToMap(Map<String, StatisticData> targetMap) {
        for (Map.Entry<String, StatisticData> e : map.entrySet()) {
            String name = e.getKey();
            StatisticData d = e.getValue();
            StatisticData d2 = targetMap.get(name);
            if (d2 == null) {
                d2 = new StatisticData();
                targetMap.put(name, d2);
            }

            d2.value += d.value;
            d2.count += d.count;
            if (d.max > d2.max) {
                d2.max = d.max;
            }

            if (d.min < d2.min) {
                d2.min = d.min;
            }
        }
    }

    public static synchronized void print() {
        print(map);
    }

    public static synchronized void print(Map<String, StatisticData> map) {
        for (Map.Entry<String, StatisticData> e : map.entrySet()) {
            String name = e.getKey();
            StatisticData d = e.getValue();
            System.out.println(name + ": count: " + d.count + " / total: " + (d.value / 1000000)
                    + "ms / min: " + (d.min / 1000)
                    + "us / max: " + (d.max / 1000)
                    + "us / avg: " + (d.value / d.count / 1000) + "us");
        }
    }

    public static synchronized void clear() {
        map.clear();
    }

    public Statistics(String name) {
        this.name = name;
        sw = Configuration.showStat ? Stopwatch.startNew() : null;
    }

    @Override
    public void close() {
        if (sw != null) {
            sw.stop();
            addTime(name, sw.getElapsedNanoseconds());
        }
    }
}
