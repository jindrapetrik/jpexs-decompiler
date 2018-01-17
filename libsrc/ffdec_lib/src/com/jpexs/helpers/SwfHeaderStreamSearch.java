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
public class SwfHeaderStreamSearch implements Searchable {

    private final MemoryInputStream is;

    public SwfHeaderStreamSearch(InputStream is) throws IOException {
        this.is = new MemoryInputStream(Helper.readStream(is));
    }

    @Override
    public Map<Long, InputStream> search(byte[]... data) {
        return search(null, data);
    }

    @Override
    public Map<Long, InputStream> search(ProgressListener progListener, byte[]... data) {
        // Ignore data parameter, find only FWS, CWS, ZWS, GFX and CFX

        Map<Long, InputStream> ret = new HashMap<>();
        byte[] buf = is.getAllRead();
        byte byte2 = buf[0], byte3 = buf[1];
        boolean match = false;
        for (int i = 2; i < buf.length - 2; i++) {
            byte b = byte2;
            byte2 = byte3;
            byte3 = buf[i];
            if (byte2 == 'W' && byte3 == 'S') {
                if (b == 'F' || b == 'C' || b == 'Z') {
                    match = true;
                }
            } else if (byte2 == 'F' && byte3 == 'X') {
                if (b == 'G' || b == 'C') {
                    match = true;
                }
            }
            if (match) {
                // todo: support > 2GB files
                InputStream fis;
                try {
                    fis = new MemoryInputStream(buf, i - 2);
                    ret.put((long) i - 2, fis);
                    match = false;
                } catch (IOException ex) {
                    Logger.getLogger(SwfHeaderStreamSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return ret;
    }
}
