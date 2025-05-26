/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

/*private static final Map<Thread, CancellableWorker> threadWorkers = Collections.synchronizedMap(new WeakHashMap<>()); 
    
    private static final Map<Thread, CancellableWorker> parentThreadWorkers = Collections.synchronizedMap(new WeakHashMap<>());         
    
    private static final Map<Thread, Thread> threadToParentThread = Collections.synchronizedMap(new WeakHashMap<>()); 
 */

/**
 * Cancellable worker.
 *
 * @param <T> Result type
 * @author JPEXS
 */
public abstract class CancellableWorker<T> implements RunnableFuture<T> {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static List<CancellableWorker> workers = Collections.synchronizedList(new ArrayList<CancellableWorker>());

    private FutureTask<T> future;

    private List<CancellableWorker> subWorkers = Collections.synchronizedList(new ArrayList<>());

    private static final Map<Thread, CancellableWorker> thread2Worker = Collections.synchronizedMap(new WeakHashMap<>());

    private Thread parentThread;

    private Thread thread;

    private String name;

    private CancellableWorker parentWorker;

    private boolean canceled = false;

    private List<Runnable> cancelListeners = new ArrayList<>();

    public Thread getThread() {
        return thread;
    }

    public static void assignThreadToWorker(Thread t, CancellableWorker w) {
        if (w == null) {
            return;
        }
        thread2Worker.put(t, w);
    }

    public static CancellableWorker getCurrent() {
        Thread t = Thread.currentThread();

        CancellableWorker w = thread2Worker.get(t);
        return w;
    }

    public void addCancelListener(Runnable listener) {
        cancelListeners.add(listener);
    }

    public void removeCancelListener(Runnable listener) {
        cancelListeners.remove(listener);
    }

    public static boolean isInterrupted() {
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) {
            return true;
        }

        CancellableWorker w = thread2Worker.get(t);
        if (w != null) {
            while (w != null) {
                t = w.thread;
                if (t != null && t.isInterrupted()) {
                    return true;
                }
                if (w.canceled) {
                    return true;
                }
                w = w.parentWorker;
            }
        }
        return false;
    }

    /**
     * Constructor.
     *
     * @param name Identifier of action
     */
    public CancellableWorker(String name) {
        super();
        this.name = name;
        Callable<T> callable = new Callable<T>() {
            @Override
            public T call() throws Exception {
                thread = Thread.currentThread();
                thread2Worker.put(thread, CancellableWorker.this);
                if (isInterrupted()) {
                    throw new InterruptedException();
                }
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

    /**
     * Do in background.
     *
     * @return Result
     * @throws Exception On error
     */
    protected abstract T doInBackground() throws Exception;

    @Override
    public final void run() {
        synchronized (CancellableWorker.class) {
            workers.add(this);
        }        
        future.run();
    }

    /**
     * Called before starting the worker.
     */
    protected void onStart() {
    }

    /**
     * Called after the worker is done.
     */
    protected void done() {
    }

    /**
     * Executes the worker.
     */
    @SuppressWarnings("unchecked")
    public final void execute() {
        parentThread = Thread.currentThread();
        if (thread2Worker.containsKey(parentThread)) {
            CancellableWorker currentWorker = thread2Worker.get(parentThread);
            currentWorker.subWorkers.add(this);
            parentWorker = currentWorker;
        }
        onStart();
        THREAD_POOL.execute(this);
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        canceled = true;
        boolean r = future.cancel(mayInterruptIfRunning);
        List<CancellableWorker> sw = new ArrayList<>(subWorkers);
        for (CancellableWorker w : sw) {
            w.cancel(mayInterruptIfRunning);
        }

        if (r) {

            List<Runnable> cls = new ArrayList<>(cancelListeners);

            for (Runnable listener : cls) {
                listener.run();
            }

            workerCancelled();
        }
        return r;
    }

    /**
     * Called when the worker is cancelled.
     */
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

    @Override
    public String toString() {
        return "[CancellableWorker \"" + name + "\" on thread " + thread + ", subworkers: " + subWorkers.size() + ", " + (canceled ? " canceled" : "") + ", " + (parentWorker != null ? "HASPARENT" : "NOPARENT");
    }

    private void workerDone() {
        if (thread != null && thread2Worker.get(thread) == this) {
            //thread2Worker.remove(thread);
        }
        synchronized (CancellableWorker.class) {
            workers.remove(this);
        }
        done();
    }

    /**
     * Calls a callable with a timeout.
     *
     * @param name Name
     * @param c Callable
     * @param timeout Timeout
     * @param timeUnit Time unit
     * @return Result
     * @param <T> Result type
     * @throws InterruptedException On interrupt
     * @throws ExecutionException On execution error
     * @throws TimeoutException On timeout
     */
    public static <T> T call(String name, final Callable<T> c, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        CancellableWorker<T> worker = new CancellableWorker<T>(name) {

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

    /**
     * Cancels all background threads.
     */
    public static void cancelBackgroundThreads() {
        synchronized (CancellableWorker.class) {
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

    public static void cancelThread(Thread t) {
        List<CancellableWorker> ws = new ArrayList<>(workers);
        for (CancellableWorker w : ws) {
            if (w != null) {
                if (w.parentThread == t) {
                    w.cancel(true);
                }
            }
        }
    }

    /**
     * Frees the worker.
     */
    public void free() {
        future = null;
        for (CancellableWorker w : subWorkers) {
            w.free();
        }
    }

    @SuppressWarnings("unchecked")
    private void printTree(List<String> out) {
        out.add("worker " + name + " on thread " + thread);
        List<CancellableWorker> sws = new ArrayList<>(subWorkers);
        for (CancellableWorker sw : sws) {
            List<String> subout = new ArrayList<>();
            sw.printTree(subout);
            for (String s : subout) {
                out.add("-" + s);
            }
        }
    }

    public void printTree() {
        System.err.println("=======================");
        List<String> out = new ArrayList<>();
        printTree(out);
        for (String s : out) {
            System.err.println(s);
        }
        System.err.println("/=======================");
    }

    public static void printAllWorkers() {
        List<CancellableWorker> aw = new ArrayList<>(workers);

        System.err.println("====ALL WORKERS ====");
        for (CancellableWorker w : aw) {
            System.err.println("" + w);
        }
        System.err.println("/====================");
    }

    public void startWorkerMonitor() {
        Thread monit = new Thread() {
            @Override
            public void run() {
                CancellableWorker w = null;
                while (true) {
                    CancellableWorker.printAllWorkers();
                    w.printTree();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        };
        monit.start();
    }
}
