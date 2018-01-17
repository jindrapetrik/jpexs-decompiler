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

import com.jpexs.decompiler.flash.helpers.Freed;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author JPEXS
 * @param <K>
 * @param <V>
 */
public class Cache<K, V> implements Freed {

    private Map<K, V> cache;

    private static final List<WeakReference<Cache>> instances = new ArrayList<>();

    public static final int STORAGE_FILES = 1;

    public static final int STORAGE_MEMORY = 2;

    private final boolean weak;

    private final boolean memoryOnly;

    private final String name;

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

    public static <K, V> Cache<K, V> getInstance(boolean weak, boolean memoryOnly, String name) {
        Cache<K, V> instance = new Cache<>(weak, memoryOnly, name);
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
        this.cache = newCache;
    }

    private Cache(boolean weak, boolean memoryOnly, String name) {
        this.weak = weak;
        this.name = name;
        this.memoryOnly = memoryOnly;
        initCache();
    }

    public synchronized boolean contains(K key) {
        return cache.containsKey(key);
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized void remove(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
    }

    public synchronized V get(K key) {
        return cache.get(key);
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
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
}
