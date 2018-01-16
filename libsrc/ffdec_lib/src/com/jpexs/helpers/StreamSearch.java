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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class StreamSearch implements Searchable {

    private final MemoryInputStream is;

    public StreamSearch(InputStream is) throws IOException {
        this.is = new MemoryInputStream(Helper.readStream(is));
    }

    @Override
    public Map<Long, InputStream> search(byte[]... data) {
        return search(null, data);
    }

    @Override
    public Map<Long, InputStream> search(ProgressListener progListener, byte[]... data) {
        Map<Long, InputStream> ret = new HashMap<>();
        int maxFindLen = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i].length > maxFindLen) {
                maxFindLen = data[i].length;
            }
        }
        try {
            is.seek(0);

            byte[] buf = new byte[4096];
            byte[] last = null;
            int cnt = 0;
            long pos = 0;
            while ((cnt = is.read(buf)) > 0) {

                for (int i = -maxFindLen + 1; i < cnt; i++) {

                    loopdata:
                    for (byte[] onedata : data) {
                        boolean match = true;
                        for (int d = 0; d < onedata.length; d++) {
                            byte b;
                            if (i + d < 0) {
                                if (last != null) {
                                    b = last[last.length + i + d];
                                } else {
                                    continue;
                                }
                            } else if (i + d >= buf.length) {
                                continue;
                            } else {
                                b = buf[i + d];
                            }

                            if (b != onedata[d]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            // todo: support > 2GB files
                            InputStream fis = new MemoryInputStream(is.getAllRead(), (int) pos + i);
                            ret.put(pos + i, fis);
                            continue loopdata;
                        }
                    }
                }
                pos = pos + cnt;
            }

        } catch (IOException ex) {
            Logger.getLogger(StreamSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
