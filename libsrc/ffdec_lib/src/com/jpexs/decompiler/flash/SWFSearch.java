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

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.PosMarkedInputStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Searchable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * SWF search class.
 *
 * @author JPEXS
 */
public class SWFSearch {

    /**
     * Searchable object
     */
    protected Searchable s;

    /**
     * No check for validity
     */
    private final boolean noCheck;

    /**
     * Search mode
     */
    private final SearchMode searchMode;

    /**
     * Already processed
     */
    private boolean processed = false;

    /**
     * Progress listeners
     */
    private final Set<ProgressListener> listeners = new HashSet<>();

    /**
     * SWF streams
     */
    private final Map<Long, MemoryInputStream> swfStreams = new LinkedHashMap<>();

    /**
     * Constructs SWF search object.
     *
     * @param s Searchable object
     * @param noCheck No check for validity
     * @param searchMode Search mode
     */
    public SWFSearch(Searchable s, boolean noCheck, SearchMode searchMode) {
        this.s = s;
        this.noCheck = noCheck;
        this.searchMode = searchMode;
    }

    /**
     * Adds progress listener.
     *
     * @param l Progress listener
     */
    public void addProgressListener(ProgressListener l) {
        listeners.add(l);
    }

    /**
     * Removes progress listener.
     *
     * @param l Progress listener
     */
    public void removeProgressListener(ProgressListener l) {
        listeners.remove(l);
    }

    /**
     * Sets progress.
     *
     * @param p Progress
     */
    private void setProgress(int p) {
        for (ProgressListener l : listeners) {
            l.progress(p);
        }
    }

    /**
     * Processes SWF search.
     */
    public void process() {
        Map<Long, InputStream> ret;
        ret = s.search(new ProgressListener() {
            @Override
            public void progress(int p) {
                setProgress(p);
            }

            @Override
            public void status(String status) {
            }
        },
                "FWS".getBytes(), // Uncompressed Flash
                "CWS".getBytes(), // ZLib compressed Flash
                "ZWS".getBytes(), // LZMA compressed Flash
                "GFX".getBytes(), // Uncompressed ScaleForm GFx
                "CFX".getBytes(), // Compressed ScaleForm GFx
                "fWS".getBytes(), //Harman encrypted uncompressed Flash,
                "cWS".getBytes(), //Harman encrypted ZLib compressed Flash,
                "zWS".getBytes() //Harman encrypted LZMA compressed Flash
        );

        int pos = 0;
        long biggestSize = 0;
        long smallestSize = Long.MAX_VALUE;
        addressLoop:
        for (Long addr : ret.keySet()) {
            setProgress(pos * 100 / ret.size());
            pos++;
            try {
                MemoryInputStream mis = (MemoryInputStream) ret.get(addr);
                mis.reset();
                PosMarkedInputStream pmi = new PosMarkedInputStream(mis);
                SWF swf = noCheck ? new SWF(pmi) : new SWF(pmi, null, null, null, false, true, true);
                boolean valid = swf.fileSize > 0
                        && swf.version > 0
                        && (!swf.getTags().isEmpty() || noCheck)
                        && swf.version <= SWF.MAX_VERSION;
                if (valid) {
                    long limit = pmi.getPos();
                    MemoryInputStream is = new MemoryInputStream(mis.getAllRead(), (int) (long) addr, (int) limit);
                    switch (searchMode) {
                        case ALL:
                            swfStreams.put(addr, is);
                            break;
                        case BIGGEST:
                            if (limit > biggestSize) {
                                biggestSize = limit;
                                swfStreams.clear();
                                swfStreams.put(addr, is);
                            }
                            break;
                        case SMALLEST:
                            if (limit < smallestSize) {
                                smallestSize = limit;
                                swfStreams.clear();
                                swfStreams.put(addr, is);
                            }
                            break;
                        case FIRST:
                            swfStreams.put(addr, is);
                            break addressLoop;
                        case LAST:
                            swfStreams.clear();
                            swfStreams.put(addr, is);
                            break;
                    }
                }
            } catch (OutOfMemoryError ome) {
                Helper.freeMem();
            } catch (Exception | Error ex) {
                //ignored
            }
        }
        setProgress(100);
        processed = true;
    }

    /**
     * Gets SWF stream.
     *
     * @param listener Progress listener
     * @param address Address
     * @return SWF stream
     * @throws IOException On I/O error
     */
    public MemoryInputStream get(ProgressListener listener, long address) throws IOException {
        if (!processed) {
            return null;
        }
        if (!swfStreams.containsKey(address)) {
            return null;
        }
        return swfStreams.get(address);
    }

    /**
     * Gets list of addresses.
     *
     * @return List of addresses
     */
    public Set<Long> getAddresses() {
        return swfStreams.keySet();
    }

    /**
     * Gets number of SWF streams.
     *
     * @return Number of SWF streams
     */
    public int length() {
        if (!processed) {
            return 0;
        }
        return swfStreams.size();
    }
}
