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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.Freed;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cache.
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author JPEXS
 */
public class Cache<K, V> implements Freed {

    private Map<K, V> cache;
    private Map<K, Long> lastAccessed;

    private static final Object instancesLock = new Object();

    private static final List<WeakReference<Cache>> instances = new ArrayList<>();

    /**
     * Storage type - files
     */
    public static final int STORAGE_FILES = 1;

    /**
     * Storage type - memory
     */
    public static final int STORAGE_MEMORY = 2;

    private final boolean weak;

    private final boolean memoryOnly;

    private final String name;

    private final boolean temporary;

    private static final long CLEAN_INTERVAL = 5 * 1000; //5 seconds

    private static Thread oldCleaner = null;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                synchronized (instancesLock) {
                    for (WeakReference<Cache> cw : instances) {
                        Cache c = cw.get();
                        if (c != null) {
                            c.clear();
                            c.free();
                        }
                    }
                }
            }
        });
    }

    /**
     * Gets instance.
     * @param weak Weak
     * @param memoryOnly Memory only
     * @param name Name
     * @param temporary Temporary
     * @return Cache
     * @param <K> Key
     * @param <V> Value
     */
    public static <K, V> Cache<K, V> getInstance(boolean weak, boolean memoryOnly, String name, boolean temporary) {
        if (oldCleaner == null) {
            oldCleaner = new Thread("Cache cleaner") {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            Thread.sleep(CLEAN_INTERVAL);
                        } catch (InterruptedException ex) {
                            return;
                        }
                        try {
                            clearAllOldAndOverMax();
                        } catch (Exception cme) {
                            Logger.getLogger(Cache.class.getSimpleName()).log(Level.SEVERE, "Error during clearing cache thread", cme);
                        }
                    }
                }
            };
            oldCleaner.setDaemon(true);
            oldCleaner.setPriority(Thread.MIN_PRIORITY);
            oldCleaner.start();
        }
        Cache<K, V> instance = new Cache<>(weak, memoryOnly, name, temporary);
        synchronized (instancesLock) {
            instances.add(new WeakReference<>(instance));
        }
        return instance;
    }

    private static int storageType = STORAGE_FILES;

    /**
     * Clear all caches.
     */
    public static void clearAll() {
        synchronized (instancesLock) {
            for (WeakReference<Cache> cw : instances) {
                Cache c = cw.get();
                if (c != null) {
                    c.clear();
                    c.initCache();
                }
            }
        }
    }

    /**
     * Sets storage type.
     * @param storageType Storage type
     */
    public static void setStorageType(int storageType) {
        if (storageType == Cache.storageType) {
            return;
        }
        switch (storageType) {
            case STORAGE_FILES:
            case STORAGE_MEMORY:
                break;
            default:
                throw new IllegalArgumentException("storageType must be one of STORAGE_FILES or STORAGE_MEMORY");
        }
        if (storageType != Cache.storageType) {
            clearAll();
        }
        Cache.storageType = storageType;
    }

    /**
     * Gets storage type.
     * @return Storage type
     */
    public static int getStorageType() {
        return storageType;
    }

    private void initCache() {
        int thisStorageType = storageType;
        if (memoryOnly) {
            thisStorageType = STORAGE_MEMORY;
        }
        Map<K, V> newCache = null;
        if (thisStorageType == STORAGE_FILES) {
            try {
                newCache = new FileHashMap<>(File.createTempFile("ffdec_cache_" + name + "_", ".tmp"));
            } catch (IOException ex) {
                thisStorageType = STORAGE_MEMORY;
            }
        }
        if (thisStorageType == STORAGE_MEMORY) {
            if (weak) {
                newCache = new WeakHashMap<>();
            } else {
                newCache = new HashMap<>();
            }
        }
        if (this.cache instanceof Freed) {
            ((Freed) this.cache).free();
        }
        this.lastAccessed = new WeakHashMap<>();
        this.cache = newCache;
    }

    private Cache(boolean weak, boolean memoryOnly, String name, boolean temporary) {
        this.weak = weak;
        this.name = name;
        this.memoryOnly = memoryOnly;
        this.temporary = temporary;
        initCache();
    }

    /**
     * Contains.
     * @param key Key
     * @return Contains
     */
    public synchronized boolean contains(K key) {
        boolean ret = cache.containsKey(key);
        if (ret) {
            lastAccessed.put(key, System.currentTimeMillis());
        }
        return ret;
    }

    /**
     * Clears cache.
     */
    public synchronized void clear() {
        cache.clear();
        lastAccessed.clear();
    }

    /**
     * Removes key.
     * @param key Key
     */
    public synchronized void remove(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        if (lastAccessed.containsKey(key)) {
            lastAccessed.remove(key);
        }
    }

    /**
     * Gets item by key.
     * @param key Key
     * @return Item
     */
    public synchronized V get(K key) {
        lastAccessed.put(key, System.currentTimeMillis());
        return cache.get(key);
    }

    /**
     * Puts key and value.
     * @param key Key
     * @param value Value
     */
    public synchronized void put(K key, V value) {
        cache.put(key, value);
        lastAccessed.put(key, System.currentTimeMillis());
    }

    @Override
    public boolean isFreeing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void free() {
        if (cache instanceof Freed) {
            ((Freed) cache).free();
        }
    }

    /**
     * Gets keys.
     * @return Keys
     */
    public Set<K> keys() {
        Set<K> ret = new HashSet<>();
        ret.addAll(cache.keySet());
        return ret;
    }

    private synchronized int clearOverMax() {
        Set<K> keys = new HashSet<>(lastAccessed.keySet());
        int num = 0;

        if (Configuration.maxCachedNum.get() > 0 && keys.size() > Configuration.maxCachedNum.get()) {
            List<K> keysList = new ArrayList<>(keys);
            Collections.sort(keysList, new Comparator<K>() {
                @Override
                public int compare(K o1, K o2) {
                    long t1 = lastAccessed.get(o1);
                    long t2 = lastAccessed.get(o2);
                    if (t1 > t2) {
                        return 1;
                    }
                    if (t2 > t1) {
                        return -1;
                    }
                    return 0;
                }
            });
            int cnt = keysList.size() - Configuration.maxCachedNum.get();
            for (int i = 0; i < cnt; i++) {
                remove(keysList.get(i));
                num++;
            }
        }

        return num;
    }

    private synchronized int clearOld() {
        long currentTime = System.currentTimeMillis();
        Set<K> keys = new HashSet<>(lastAccessed.keySet());
        int temporaryThreshold = Configuration.maxCachedTime.get();
        if (temporaryThreshold == 0) {
            return 0;
        }
        int num = 0;
        for (K key : keys) {
            long time = lastAccessed.get(key);
            if (time < currentTime - temporaryThreshold) {
                remove(key);
                num++;
            }
        }
        return num;
    }

    private static void clearAllOldAndOverMax() {
        int num = 0;
        synchronized (instancesLock) {
            for (WeakReference<Cache> cw : instances) {
                Cache c = cw.get();
                if (c != null) {
                    num += c.clearOverMax();
                    if (c.temporary) {
                        num += c.clearOld();
                    }
                }
            }
        }
        if (num > 0) {
            System.gc();
        }
    }
}
