/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Bundle implementation for ZIP files.
 *
 * @author JPEXS
 */
public class ZippedBundle implements Bundle {

    /**
     * Key set
     */
    protected Set<String> keySet = new HashSet<>();  

    /**
     * File name of the ZIP file
     */
    protected File filename;        
   
    /**
     * Constructs a new ZippedBundle from a file.
     *
     * @param filename File
     * @throws IOException On I/O error
     */
    public ZippedBundle(File filename) throws IOException {
        initBundle(filename);
    }

    /**
     * Initializes the bundle.
     *
     * @param is Input stream
     * @param filename File
     * @throws IOException On I/O error
     */
    protected void initBundle(File filename) throws IOException {
        this.filename = filename;
        ZipFile zipFile = new ZipFile(filename);
        keySet.clear();
                
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().toLowerCase().endsWith(".swf")
                    || entry.getName().toLowerCase().endsWith(".spl")
                    || entry.getName().toLowerCase().endsWith(".gfx")
                    || entry.getName().toLowerCase().endsWith(".abc")) {
                keySet.add(entry.getName());
            }
        }
        zipFile.close();
    }

    @Override
    public int length() {
        return keySet.size();
    }


    @Override
    public Set<String> getKeys() {
        return keySet;
    }

    @Override
    public SeekableInputStream getOpenable(String key) throws IOException {
        if (!keySet.contains(key)) {
            return null;
        }
        ZipFile zipFile = new ZipFile(filename);
        ZipEntry entry = zipFile.getEntry(key);        
        MemoryInputStream mis = new MemoryInputStream(Helper.readStream(zipFile.getInputStream(entry)));
        SeekableInputStream ret = mis;                
        zipFile.close();

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
        return "zip";
    }

    @Override
    public boolean isReadOnly() {
        return this.filename == null || !this.filename.canWrite();
    }

    @Override
    public boolean putOpenable(String key, InputStream swfIs) throws IOException {
        if (this.isReadOnly()) {
            return false;
        }
        if (key == null) {
            return false;
        }
        if (!getKeys().contains(key)) { //replace only existing keys
            return false;
        }
        
        ZipFile zipFile = new ZipFile(filename);
        
        //Write to temp file first
        File tempFile = new File((filename.getAbsolutePath()) + ".tmp");

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile));
            ZipEntry entryIn;
            ZipEntry entryOut;

            byte[] swfData = Helper.readStream(swfIs);
            
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            try {
                while (entries.hasMoreElements()) {
                    InputStream src;
                    entryIn = entries.nextElement();
                    if (entryIn.getName().equals(key)) {
                        entryOut = new ZipEntry(key);
                        src = new ByteArrayInputStream(swfData);
                    } else {
                        src = zipFile.getInputStream(entryIn);
                        entryOut = entryIn;
                    }
                    zos.putNextEntry(entryOut);
                    Helper.copyStream(src, zos, entryOut.getSize() == -1 ? Long.MAX_VALUE : entryOut.getSize());
                    zos.closeEntry();
                }
            
            } finally {
                zos.close();
                zipFile.close();
            }            
        } catch (IOException ex) {
            tempFile.delete();
            throw ex;
        }
        filename.delete();
        tempFile.renameTo(filename);
        initBundle(filename);

        return true;
    }
}
