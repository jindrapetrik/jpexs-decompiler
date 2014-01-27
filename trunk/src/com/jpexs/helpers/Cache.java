/*
 *  Copyright (C) 2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.helpers;

import com.jpexs.decompiler.flash.types.shaperecords.SerializableImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Cache {

    private final Map<Object, File> cacheFiles;
    private final Map<Object, Object> cacheMemory;
    private static final List<Cache> instances = new ArrayList<>();
    public static final int STORAGE_FILES = 1;
    public static final int STORAGE_MEMORY = 2;

    public static Cache getInstance(boolean weak) {
        Cache instance = new Cache(weak);
        instances.add(instance);
        return instance;
    }

    private static int storageType = STORAGE_FILES;

    public static void clearAll() {
        for (Cache c : instances) {
            c.clear();
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

    private Cache(boolean weak) {
        if (weak) {
            cacheFiles = new WeakHashMap<>();
            cacheMemory = new WeakHashMap<>();
        } else {
            cacheFiles = new HashMap<>();
            cacheMemory = new HashMap<>();
        }
    }

    public boolean contains(Object key) {
        if (storageType == STORAGE_FILES) {
            return cacheFiles.containsKey(key);
        } else if (storageType == STORAGE_MEMORY) {
            return cacheMemory.containsKey(key);
        }
        return false;
    }

    public void clear() {
        cacheMemory.clear();
        for (File f : cacheFiles.values()) {
            f.delete();
        }
        cacheFiles.clear();
    }

    public void remove(Object key) {
        if (storageType == STORAGE_FILES) {
            if (cacheFiles.containsKey(key)) {
                File f = cacheFiles.get(key);
                f.delete();
                cacheFiles.remove(key);
            }
        } else if (storageType == STORAGE_MEMORY) {
            if (cacheMemory.containsKey(key)) {
                cacheMemory.remove(key);
            }
        }

    }

    public Object get(Object key) {
        if (storageType == STORAGE_FILES) {
            if (!cacheFiles.containsKey(key)) {
                return null;
            }
            File f = cacheFiles.get(key);
            try (FileInputStream fis = new FileInputStream(f)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object item = ois.readObject();
                if (item instanceof SerializableImage) {
                    item = ((SerializableImage) item).getImage();
                }
                return item;
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        } else if (storageType == STORAGE_MEMORY) {
            if (cacheMemory.containsKey(key)) {
                return cacheMemory.get(key);
            }
            return null;
        }
        return null;
    }

    public void put(Object key, Object value) {
        if (storageType == STORAGE_FILES) {
            File temp = null;
            try {
                temp = File.createTempFile("ffdec_cache", ".tmp");
            } catch (IOException ex) {
                Logger.getLogger(Cache.class
                        .getName()).log(Level.SEVERE, null, ex);

                return;
            }
            try {
                temp.deleteOnExit();
            } catch (IllegalStateException iex) {
                return;
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
                if (value instanceof Serializable) {
                    oos.writeObject(value);
                } else {
                    if (value instanceof BufferedImage) {
                        value = new SerializableImage((BufferedImage) value);
                        oos.writeObject(value);
                    } else {
                        // Object serialization not supported
                        return;
                    }
                }
                oos.flush();

                cacheFiles.put(key, temp);

            } catch (IOException ex) {
                //ignore
            }
        } else if (storageType == STORAGE_MEMORY) {
            cacheMemory.put(key, value);
        }
    }
}
