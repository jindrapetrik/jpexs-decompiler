/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Cache {

    private Map<Object, File> cacheFiles;

    public Cache(boolean weak) {
        if (weak) {
            cacheFiles = new WeakHashMap<>();
        } else {
            cacheFiles = new HashMap<>();
        }
    }

    public boolean contains(Object key) {
        return cacheFiles.containsKey(key);
    }

    public void clear() {
        for (File f : cacheFiles.values()) {
            f.delete();
        }
        cacheFiles.clear();
    }

    public void remove(Object key) {
        if (cacheFiles.containsKey(key)) {
            File f = cacheFiles.get(key);
            f.delete();
            cacheFiles.remove(key);
        }
    }

    public Object get(Object key) {
        if (!cacheFiles.containsKey(key)) {
            return null;
        }
        File f = cacheFiles.get(key);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
        return null;
    }

    public void put(Object key, Object value) {
        File temp = null;
        ObjectOutputStream oos = null;
        try {
            temp = File.createTempFile("ffdec_cache", ".tmp");
            temp.deleteOnExit();
            oos = new ObjectOutputStream(new FileOutputStream(temp));
            oos.writeObject(value);
            oos.flush();

            cacheFiles.put(key, temp);
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }
}
