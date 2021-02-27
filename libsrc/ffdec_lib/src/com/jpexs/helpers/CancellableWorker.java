/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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

    private FutureTask<T> future;

    private static final Map<Thread, CancellableWorker> threadWorkers = Collections.synchronizedMap(new WeakHashMap<>());

    private List<CancellableWorker> subWorkers = Collections.synchronizedList(new ArrayList<>());

    private Thread thread;

    public CancellableWorker() {
        super();
        Callable<T> callable = new Callable<T>() {
            @Override
            public T call() throws Exception {
                thread = Thread.currentThread();
                threadWorkers.put(thread, CancellableWorker.this);
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

    @SuppressWarnings("unchecked")
    public final void execute() {
        Thread t = Thread.currentThread();
        if (threadWorkers.containsKey(t)) {
            threadWorkers.get(t).subWorkers.add(this);
        }
        onStart();
        THREAD_POOL.execute(this);
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        List<CancellableWorker> sw = new ArrayList<>(subWorkers);
        for (CancellableWorker w : sw) {
            w.cancel(mayInterruptIfRunning);
        }
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
        if (thread != null && threadWorkers.get(thread) == this) {
            threadWorkers.remove(thread);
        }
        workers.remove(this);
        done();
    }

    public static <T> T call(final Callable<T> c, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) {
            throw new InterruptedException();
        }
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

    public void free() {
        future = null;
        for (CancellableWorker w : subWorkers) {
            w.free();
        }
    }
}
