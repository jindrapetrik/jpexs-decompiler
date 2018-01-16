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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 * @param <T>
 */
public abstract class CancellableWorker<T> implements RunnableFuture<T> {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static List<CancellableWorker> workers = Collections.synchronizedList(new ArrayList<CancellableWorker>());

    private final FutureTask<T> future;

    public CancellableWorker() {
        super();
        Callable<T> callable = new Callable<T>() {
            @Override
            public T call() throws Exception {
                return doInBackground();
            }
        };

        future = new FutureTask<T>(callable) {
            @Override
            protected void done() {
                workerDone();
            }
        };
    }

    protected abstract T doInBackground() throws Exception;

    @Override
    public final void run() {
        workers.add(this);
        future.run();
    }

    protected void onStart() {
    }

    protected void done() {
    }

    public final void execute() {
        onStart();
        THREAD_POOL.execute(this);
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        boolean r = future.cancel(mayInterruptIfRunning);
        if (r) {
            workerCancelled();
        }
        return r;
    }

    public void workerCancelled() {

    }

    @Override
    public final boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public final boolean isDone() {
        return future.isDone();
    }

    @Override
    public final T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public final T get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    private void workerDone() {
        workers.remove(this);
        done();
    }

    public static <T> T call(final Callable<T> c, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        CancellableWorker<T> worker = new CancellableWorker<T>() {

            @Override
            protected T doInBackground() throws Exception {
                return c.call();
            }
        };
        try {
            worker.execute();
            return worker.get(timeout, timeUnit);
        } finally {
            worker.cancel(true);
        }
    }

    public static void cancelBackgroundThreads() {
        List<CancellableWorker> oldWorkers = workers;
        workers = Collections.synchronizedList(new ArrayList<CancellableWorker>());
        for (CancellableWorker worker : oldWorkers) {
            if (worker != null) {
                worker.cancel(true);
            } else {
                Logger.getLogger(CancellableWorker.class.getName()).log(Level.SEVERE, "worker is null");
            }
        }
    }
}
