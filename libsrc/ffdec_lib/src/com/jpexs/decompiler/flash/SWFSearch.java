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
package com.jpexs.decompiler.flash;

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.PosMarkedInputStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Searchable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class SWFSearch {

    protected Searchable s;

    private final boolean noCheck;

    private final SearchMode searchMode;

    private boolean processed = false;

    private final Set<ProgressListener> listeners = new HashSet<>();

    private final Map<Long, MemoryInputStream> swfStreams = new HashMap<>();

    public SWFSearch(Searchable s, boolean noCheck, SearchMode searchMode) {
        this.s = s;
        this.noCheck = noCheck;
        this.searchMode = searchMode;
    }

    public void addProgressListener(ProgressListener l) {
        listeners.add(l);
    }

    public void removeProgressListener(ProgressListener l) {
        listeners.remove(l);
    }

    private void setProgress(int p) {
        for (ProgressListener l : listeners) {
            l.progress(p);
        }
    }

    public void process() {
        Map<Long, InputStream> ret;
        ret = s.search(new ProgressListener() {
            @Override
            public void progress(int p) {
                setProgress(p);
            }
        },
                "FWS".getBytes(), // Uncompressed Flash
                "CWS".getBytes(), // ZLib compressed Flash
                "ZWS".getBytes(), // LZMA compressed Flash
                "GFX".getBytes(), // Uncompressed ScaleForm GFx
                "CFX".getBytes()); // Compressed ScaleForm GFx
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
            }
        }
        setProgress(100);
        processed = true;
    }

    public MemoryInputStream get(ProgressListener listener, long address) throws IOException {
        if (!processed) {
            return null;
        }
        if (!swfStreams.containsKey(address)) {
            return null;
        }
        return swfStreams.get(address);
    }

    public Set<Long> getAddresses() {
        return swfStreams.keySet();
    }

    public int length() {
        if (!processed) {
            return 0;
        }
        return swfStreams.size();
    }
}
