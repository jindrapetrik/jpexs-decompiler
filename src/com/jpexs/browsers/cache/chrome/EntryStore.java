/*
 *  Copyright (C) 2010-2015 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.browsers.cache.chrome;

import com.jpexs.browsers.cache.CacheEntry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class EntryStore extends CacheEntry {

    public static final int ENTRY_NORMAL = 0;

    public static final int ENTRY_EVICTED = 1;    // The entry was recently evicted from the cache.

    public static final int ENTRY_DOOMED = 2;      // The entry was doomed

    public static final int PARENT_ENTRY = 1;         // This entry has children (sparse) entries.

    public static final int CHILD_ENTRY = 1 << 1;

    public long hash;               // Full hash of the key.

    public CacheAddr next;               // Next entry with the same hash or bucket.

    public CacheAddr rankings_node;      // Rankings node for this entry.

    public int reuse_count;        // How often is this entry used.

    public int refetch_count;      // How often is this fetched from the net.

    public int state;              // Current state.

    public long creation_time;

    public int key_len;

    public CacheAddr long_key;           // Optional address of a long key.

    public int data_size[] = new int[4];       // We can store up to 4 data streams for each

    public CacheAddr data_addr[] = new CacheAddr[4];       // entry.

    public long flags;              // Any combination of EntryFlags.

    public int pad[] = new int[4];

    public long self_hash;          // The hash of EntryStore up to this point.

    public byte key[] = new byte[256 - 24 * 4];  // null terminated

    public EntryStore(InputStream is, File rootDir, Map<Integer, RandomAccessFile> dataFiles, File externalFilesDir) throws IOException {
        IndexInputStream iis = new IndexInputStream(is);
        hash = iis.readUInt32();
        next = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        rankings_node = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        reuse_count = iis.readInt32();
        refetch_count = iis.readInt32();
        state = iis.readInt32();
        creation_time = iis.readUInt64();
        key_len = iis.readInt32();
        long_key = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        data_size = new int[4];
        for (int i = 0; i < 4; i++) {
            data_size[i] = iis.readInt32();
        }
        data_addr = new CacheAddr[4];
        for (int i = 0; i < 4; i++) {
            data_addr[i] = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        }
        flags = iis.readUInt32();
        pad = new int[4];
        for (int i = 0; i < 4; i++) {
            pad[i] = iis.readInt32();
        }
        self_hash = iis.readUInt32();
        key = new byte[256 - 24 * 4];
        if (iis.read(key) != key.length) {
            throw new IOException();
        }
    }

    public HttpResponseInfo getResponseInfo() {
        try {
            InputStream is = data_addr[0].getInputStream();
            if (is == null) {
                return null;
            }
            return new HttpResponseInfo(is);
        } catch (IOException ex) {
            Logger.getLogger(EntryStore.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int getResponseDataSize() {
        return data_size[1];
    }

    @Override
    public InputStream getResponseRawDataStream() {
        try {
            return data_addr[1].getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(EntryStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getKey() {
        if (key_len < 0) {
            return null;
        }
        return new String(key, 0, key_len > key.length ? key.length : key_len);
    }

    @Override
    public String getRequestURL() {
        return getKey();
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        HttpResponseInfo ri = getResponseInfo();
        if (ri == null) {
            return new HashMap<>();
        }
        List<String> headers = ri.headers;
        Map<String, String> ret = new HashMap<>();
        for (int h = 1; h < headers.size(); h++) {
            String hs = headers.get(h);
            if (hs.contains(":")) {
                String hp[] = hs.split(":");
                ret.put(hp[0].trim(), hp[1].trim());
            }
        }
        return ret;
    }

    @Override
    public String getStatusLine() {
        HttpResponseInfo ri = getResponseInfo();
        return ri.headers.get(0);
    }

    @Override
    public String getRequestMethod() {
        return "GET";
    }
}
