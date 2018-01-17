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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 * @param <K>
 * @param <V>
 */
public class FileHashMap<K, V> extends AbstractMap<K, V> implements Freed {

    private static final Logger logger = Logger.getLogger(FileHashMap.class.getName());

    private final Map<K, Integer> lengths = new HashMap<>();

    private final Map<K, Long> offsets = new HashMap<>();

    private long fileLen = 0;

    private final RandomAccessFile file;

    private final File fileName;

    private final Set<Gap> gaps = new TreeSet<>();

    private int maxGapLen = 0;

    private boolean deleted = false;

    private static class Gap implements Comparable<Gap> {

        public long offset;

        public int length;

        public Gap(long offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public int compareTo(Gap o) {
            return o.length - length;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (int) (this.offset ^ (this.offset >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Gap other = (Gap) obj;
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }
    }

    public static class FileEntry<K, V> implements Map.Entry<K, V> {

        private final FileHashMap<K, V> parent;

        private final K key;

        public FileEntry(FileHashMap<K, V> parent, K key) {
            this.parent = parent;
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return parent.get(key);
        }

        @Override
        public V setValue(V value) {
            return parent.put(key, value);
        }
    }

    public FileHashMap(File file) throws IOException {
        this.file = new RandomAccessFile(file, "rw");
        this.file.setLength(0);
        this.fileName = file;
        file.deleteOnExit();
    }

    @Override
    public boolean containsKey(Object key) {
        if (deleted) {
            throw new NullPointerException();
        }
        return offsets.containsKey(key);
    }

    @Override
    public Set<K> keySet() {
        if (deleted) {
            throw new NullPointerException();
        }
        return offsets.keySet();
    }

    @Override
    public V get(Object key) {
        if (deleted) {
            throw new NullPointerException();
        }
        try {
            if (!offsets.containsKey(key)) {
                return null;
            }
            long ofs = offsets.get(key);
            int len = lengths.get(key);
            file.seek(ofs);
            byte[] data = new byte[len];
            file.readFully(data);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                @SuppressWarnings("unchecked")
                V ret = (V) ois.readObject();
                return ret;
            } catch (ClassNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
                return null;
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public synchronized V put(K key, V value) {
        if (deleted) {
            throw new NullPointerException();
        }
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            byte[] data = baos.toByteArray();
            if (offsets.containsKey(key)) {
                long origOffset = offsets.get(key);
                int origLen = lengths.get(key);
                if (data.length <= origLen) {
                    file.seek(origOffset);
                    file.write(data);
                    lengths.put(key, data.length);
                    if (data.length < origLen) {
                        Gap g = new Gap(origOffset + data.length, origLen - data.length);
                        if (g.length > maxGapLen) {
                            maxGapLen = g.length;
                        }
                        gaps.add(g);
                    }

                    return value;
                }
            }
            if (data.length <= maxGapLen) {

                for (Iterator<Gap> i = gaps.iterator(); i.hasNext();) {
                    Gap g = i.next();
                    if (g.length >= data.length) {
                        file.seek(g.offset);
                        file.write(data);
                        offsets.put(key, g.offset);
                        lengths.put(key, g.length);
                        if (g.length > data.length) {
                            g.offset = g.offset + data.length;
                            g.length = g.length - data.length;
                        } else {
                            i.remove();
                        }
                    }
                }
            } else {
                file.seek(fileLen);
                file.write(data);
                offsets.put(key, fileLen);
                lengths.put(key, data.length);
                fileLen += data.length;
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                // ignore
            }
        }
        return value;
    }

    @Override
    public V remove(Object objKey) {
        if (deleted) {
            throw new NullPointerException();
        }
        @SuppressWarnings("unchecked")
        K key = (K) objKey;
        if (!containsKey(key)) {
            return null;
        }
        V val = get((K) key);
        Gap g = new Gap(offsets.get(key), lengths.get(key));
        offsets.remove(key);
        lengths.remove(key);
        if (g.offset + g.length == fileLen) {
            fileLen -= g.length;
        } else {
            if (g.length > maxGapLen) {
                maxGapLen = g.length;
            }
            gaps.add(g);
        }
        return val;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (deleted) {
            throw new NullPointerException();
        }
        Set<Entry<K, V>> ret = new HashSet<>();
        for (K key : keySet()) {
            ret.add(new FileEntry<>(this, key));
        }
        return ret;
    }

    @Override
    public void clear() {
        if (deleted) {
            throw new NullPointerException();
        }
        offsets.clear();
        lengths.clear();
        fileLen = 0;
        maxGapLen = 0;
        try {
            file.setLength(0);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void delete() {
        if (deleted) {
            throw new NullPointerException();
        }
        try {
            file.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        fileName.delete();
        deleted = true;

    }

    @Override
    public boolean isFreeing() {
        return !deleted;
    }

    @Override
    public void free() {
        if (!deleted) {
            delete();
        }
    }

    @Override
    public boolean isEmpty() {
        return offsets.isEmpty();
    }

    @Override
    public int size() {
        return offsets.size();
    }
}
