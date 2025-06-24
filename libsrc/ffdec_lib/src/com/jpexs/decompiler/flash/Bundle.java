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

import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Bundle interface. A bundle is a collection of openable files. (e.g. SWC, ZIP,
 * etc.)
 *
 * @author JPEXS
 */
public interface Bundle {

    /**
     * Gets number of openable files in the bundle.
     *
     * @return Number of openable files in the bundle
     */
    public int length();

    /**
     * Gets keys of openable files in the bundle.
     *
     * @return Keys of openable files in the bundle
     */
    public Set<String> getKeys();

    /**
     * Gets openable file by key.
     *
     * @param key Key
     * @return Openable file
     * @throws IOException On I/O error
     */
    public SeekableInputStream getOpenable(String key) throws IOException;

    /**
     * Gets all openable files in the bundle.
     *
     * @return Map from key to seekable input stream
     * @throws IOException On I/O error
     */
    public Map<String, SeekableInputStream> getAll() throws IOException;

    /**
     * Gets extension of the bundle. (without dot)
     *
     * @return Extension of the bundle
     */
    public String getExtension();

    /**
     * Checks if the bundle is read-only.
     *
     * @return True if the bundle is read-only, false otherwise
     */
    public boolean isReadOnly();

    /**
     * Replace openable file by key.
     *
     * @param key Key
     * @param is New input stream
     * @return True if the file was replaced, false otherwise
     * @throws IOException On I/O error
     */
    public boolean putOpenable(String key, InputStream is) throws IOException;
}
