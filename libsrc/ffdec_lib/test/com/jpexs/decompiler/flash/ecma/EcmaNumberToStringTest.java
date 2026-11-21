/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.ecma;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EcmaNumberToStringTest {

    @DataProvider(name = "numbers")
    public Object[][] numbers() {
        return new Object[][]{
            {Double.MIN_VALUE, "4.9e-324"}, {-Double.MIN_VALUE, "-4.9e-324"},
            {Double.MIN_NORMAL, "2.2250738585072014e-308"},
            {Math.nextDown(Double.MIN_NORMAL), "2.225073858507201e-308"},
            {Double.MAX_VALUE, "1.7976931348623157e+308"},
            {-Double.MAX_VALUE, "-1.7976931348623157e+308"},
            {1e-200, "1e-200"}, {1e200, "1.0e+200"},
            {1e-7, "1e-7"}, {1e-6, "0.000001"},
            {1e20, "100000000000000000000"}, {1e21, "1e+21"},
            {0.0, "0.0"}, {-0.0, "-0.0"},
            {Double.NaN, "NaN"}, {Double.POSITIVE_INFINITY, "Infinity"},
            {Double.NEGATIVE_INFINITY, "-Infinity"}
        };
    }

    @Test(dataProvider = "numbers")
    public void testNumberFormatting(double value, String expected) {
        Assert.assertEquals(EcmaNumberToString.stringFor(value), expected);
        Assert.assertEquals(EcmaScript.toString(value), value == 0.0 ? "0" : expected);
    }

    @Test
    public void testConcurrentColdCacheGrowth() throws Exception {
        int threads = 16;
        int rounds = 1000;
        Field cache = EcmaNumberToString.class.getDeclaredField("powerOf5Cache");
        cache.setAccessible(true);
        Object previousCache;
        synchronized (EcmaNumberToString.class) {
            previousCache = cache.get(null);
        }
        // Exercise initialization and competing resizes, rather than just
        // reading the already warmed cache left by the formatting tests.
        CyclicBarrier start = new CyclicBarrier(threads, () -> {
            synchronized (EcmaNumberToString.class) {
                try {
                    cache.set(null, null);
                } catch (IllegalAccessException ex) {
                    throw new AssertionError(ex);
                }
            }
        });
        CyclicBarrier end = new CyclicBarrier(threads);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            Object[][] numbers = numbers();
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int worker = 0; worker < threads; worker++) {
                final int offset = worker;
                tasks.add(() -> {
                    for (int round = 0; round < rounds; round++) {
                        start.await(10, TimeUnit.SECONDS);
                        if (failure.get() == null) {
                            try {
                                for (int i = 0; i < numbers.length; i++) {
                                    Object[] number = numbers[(i + offset + round) % numbers.length];
                                    double value = (Double) number[0];
                                    Assert.assertEquals(EcmaScript.toString(value), value == 0.0 ? "0" : number[1]);
                                }
                            } catch (RuntimeException | AssertionError ex) {
                                failure.compareAndSet(null, ex);
                            }
                        }
                        end.await(10, TimeUnit.SECONDS);
                    }
                    return null;
                });
            }
            for (Future<Void> task : executor.invokeAll(tasks, 30, TimeUnit.SECONDS)) {
                task.get();
            }
            if (failure.get() != null) {
                Assert.fail("Concurrent number formatting failed", failure.get());
            }
        } finally {
            executor.shutdownNow();
            Assert.assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            synchronized (EcmaNumberToString.class) {
                cache.set(null, previousCache);
            }
        }
    }
}
