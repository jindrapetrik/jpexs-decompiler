/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class BinarySWFBundle implements Bundle {

    private final SWFSearch search;

    public BinarySWFBundle(InputStream is, boolean noCheck, SearchMode searchMode) throws IOException {
        search = new SWFSearch(new SwfHeaderStreamSearch(is), noCheck, searchMode);
        search.process();
    }

    @Override
    public int length() {
        return search.length();
    }

    @Override
    public Set<String> getKeys() {
        Set<String> ret = new LinkedHashSet<>();
        for (Long address : search.getAddresses()) {
            ret.add("[" + address + "]");
        }
        return ret;
    }

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

    @Override
    public Map<String, SeekableInputStream> getAll() {
        Map<String, SeekableInputStream> ret = new LinkedHashMap<>();
        for (String key : getKeys()) {
            ret.put(key, getOpenable(key));
        }
        return ret;
    }

    @Override
    public String getExtension() {
        return "bin";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean putOpenable(String key, InputStream is) {
        throw new UnsupportedOperationException("Save not supported for this type of bundle");
    }
}
