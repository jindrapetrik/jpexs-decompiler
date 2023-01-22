/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author JPEXS
 * @param <K>
 * @param <V>
 */
public class Cache<K, V> implements Freed {

    private Map<K, V> cache;
    private Map<K, Long> lastAccessed;

    private static final List<WeakReference<Cache>> instances = new ArrayList<>();

    public static final int STORAGE_FILES = 1;

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
                for (WeakReference<Cache> cw : instances) {
                    Cache c = cw.get();
                    if (c != null) {
                        c.clear();
                        c.free();
                    }
                }
            }

        });
    }

    public static <K, V> Cache<K, V> getInstance(boolean weak, boolean memoryOnly, String name, boolean temporary) {
        if (oldCleaner == null) {
            oldCleaner = new Thread("Old cache cleaner") {
                @Override
                public void run() {
                    while(!Thread.interrupted()) {
                        try {
                            Thread.sleep(CLEAN_INTERVAL);
                        } catch (InterruptedException ex) {
                            return;
                        }
                        clearAllOld();                        
                    }
                }                
            };
            oldCleaner.setDaemon(true);
            oldCleaner.setPriority(Thread.MIN_PRIORITY);
            oldCleaner.start();
        }
        Cache<K, V> instance = new Cache<>(weak, memoryOnly, name, temporary);
        instances.add(new WeakReference<>(instance));
        return instance;
    }

    private static int storageType = STORAGE_FILES;

    public static void clearAll() {
        for (WeakReference<Cache> cw : instances) {
            Cache c = cw.get();
            if (c != null) {
                c.clear();
                c.initCache();
            }
        }
    }

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

    public synchronized boolean contains(K key) {        
        boolean ret = cache.containsKey(key);
        if (ret) {
            lastAccessed.put(key, System.currentTimeMillis());
        }
        return ret;
    }

    public synchronized void clear() {
        cache.clear();
        lastAccessed.clear();
    }

    public synchronized void remove(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        if (lastAccessed.containsKey(key)) {
            lastAccessed.remove(key);
        }
    }

    public synchronized V get(K key) {
        lastAccessed.put(key, System.currentTimeMillis());
        return cache.get(key);
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
        lastAccessed.put(key, System.currentTimeMillis());
    }

    @Override
    public boolean isFreeing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void free() {
        if (cache instanceof Freed) {
            ((Freed) cache).free();
        }
    }

    public Set<K> keys() {
        Set<K> ret = new HashSet<>();
        ret.addAll(cache.keySet());
        return ret;
    }
    
    private synchronized int clearOld() {
        long currentTime = System.currentTimeMillis();
        Set<K> keys = new HashSet<>(lastAccessed.keySet());
        int temporaryThreshold = Configuration.maxCachedTime.get();
        if (temporaryThreshold == 0) {
            return 0;
        }
        int num = 0;
        for(K key:keys) {
            long time = lastAccessed.get(key);
            if (time < currentTime - temporaryThreshold) {
                remove(key);
                num++;
            }
        }        
        return num;
    }
    
    private static void clearAllOld() {
        int num = 0;
        for (WeakReference<Cache> cw : instances) {
            Cache c = cw.get();
            if (c != null) {
                if (c.temporary) {
                    num += c.clearOld();
                }
            }
        }        
        if (num > 0) {
            System.gc();
        }
    }
}
