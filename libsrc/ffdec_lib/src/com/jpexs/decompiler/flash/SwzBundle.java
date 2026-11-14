/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bundle implementation for SWZ files.
 *
 * @author JPEXS
 */
public class SwzBundle implements Bundle {

    /**
     * File name of the SWZ file
     */
    protected File filename;        
    
    protected String key;
   
    /**
     * Constructs a new SwzBundle from a file.
     *
     * @param filename File
     * @throws IOException On I/O error
     */
    public SwzBundle(File filename) throws IOException {
        initBundle(filename);
    }

    /**
     * Initializes the bundle.
     *
     * @param filename File
     * @throws IOException On I/O error
     */
    protected void initBundle(File filename) throws IOException {
        this.filename = filename;
        
        key = filename.getName();        
        if (!key.contains(".")) {
            key = key + ".swf";
        } else {
            key = key.substring(0, key.lastIndexOf(".")) + ".swf";
        }        
    }

    @Override
    public int length() {
        return 1;
    }


    @Override
    public Set<String> getKeys() {
        Set<String> ret = new LinkedHashSet<>();
        ret.add(key);
        return ret;
    }

    @Override
    public SeekableInputStream getOpenable(String key) throws IOException {
        if (!this.key.equals(key)) {
            return null;
        }
        MemoryInputStream mis = new MemoryInputStream(SwzExtractor.extract(Helper.readFile(filename.getAbsolutePath())));
        SeekableInputStream ret = mis;                
        
        return ret;
    }

    @Override
    public Map<String, SeekableInputStream> getAll() throws IOException {
        Map<String, SeekableInputStream> ret = new HashMap<>();
        for (String key : getKeys()) { // cache everything first
            ret.put(key, getOpenable(key));
        }
        return ret;
    }

    @Override
    public String getExtension() {
        return "swz";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean putOpenable(String key, InputStream swfIs) throws IOException {
        return false;
    }
}
