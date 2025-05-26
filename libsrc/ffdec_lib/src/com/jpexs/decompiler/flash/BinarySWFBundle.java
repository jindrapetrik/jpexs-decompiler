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
package com.jpexs.decompiler.flash;

import com.jpexs.helpers.SwfHeaderStreamSearch;
import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Binary search SWF bundle
 *
 * @author JPEXS
 */
public class BinarySWFBundle implements Bundle {

    /**
     * SWF search
     */
    private final SWFSearch search;

    /**
     * Constructs a new BinarySWFBundle.
     *
     * @param is Input stream
     * @param noCheck Do not check
     * @param searchMode Search mode
     * @throws IOException On I/O error
     */
    public BinarySWFBundle(InputStream is, boolean noCheck, SearchMode searchMode) throws IOException {
        search = new SWFSearch(new SwfHeaderStreamSearch(is), noCheck, searchMode);
        search.process();
    }

    /**
     * Gets size of the search.
     *
     * @return Size of the search
     */
    @Override
    public int length() {
        return search.length();
    }

    /**
     * Gets keys.
     *
     * @return Keys
     */
    @Override
    public Set<String> getKeys() {
        Set<String> ret = new LinkedHashSet<>();
        for (Long address : search.getAddresses()) {
            ret.add("[" + address + "]");
        }
        return ret;
    }

    /**
     * Gets openable.
     *
     * @param key Key
     * @return Openable
     */
    @Override
    public SeekableInputStream getOpenable(String key) {
        if (!key.startsWith("[")) {
            return null;
        }
        if (!key.endsWith("]")) {
            return null;
        }
        key = key.substring(1, key.length() - 1);
        try {
            int address = Integer.parseInt(key);
            return search.get(null, address);
        } catch (IOException | NumberFormatException iex) {
            return null;
        }
    }

    /**
     * Gets all.
     *
     * @return Map of string to seekable input stream
     */
    @Override
    public Map<String, SeekableInputStream> getAll() {
        Map<String, SeekableInputStream> ret = new LinkedHashMap<>();
        for (String key : getKeys()) {
            ret.put(key, getOpenable(key));
        }
        return ret;
    }

    /**
     * Gets extension.
     *
     * @return Extension
     */
    @Override
    public String getExtension() {
        return "bin";
    }

    /**
     * Checks if read only.
     *
     * @return True if read only, false otherwise
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Replaces openable.
     *
     * @param key Key
     * @param is Input stream
     * @return True if successful, false otherwise
     */
    @Override
    public boolean putOpenable(String key, InputStream is) {
        throw new UnsupportedOperationException("Save not supported for this type of bundle");
    }
}
