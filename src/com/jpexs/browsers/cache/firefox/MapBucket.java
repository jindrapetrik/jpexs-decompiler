/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.browsers.cache.firefox;

import com.jpexs.browsers.cache.CacheEntry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class MapBucket extends CacheEntry {

    public long hash;

    public long enviction;

    public Location dataLocation;

    public Location metadataLocation;

    private MetaData metadata;

    public MapBucket(InputStream is, File rootDir, Map<Integer, RandomAccessFile> dataFiles) throws IOException {
        CacheInputStream cis = new CacheInputStream(is);
        hash = cis.readInt32();
        enviction = cis.readInt32();
        dataLocation = new Location(cis.readInt32(), false, hash, rootDir, dataFiles);
        metadataLocation = new Location(cis.readInt32(), true, hash, rootDir, dataFiles);
    }

    public InputStream getMetaDataStream() throws IOException {
        return metadataLocation.getInputStream();
    }

    public MetaData getMetaData() {
        if (metadata == null) {
            try {
                metadata = new MetaData(getMetaDataStream());
            } catch (IncompatibleVersionException ie) {
            } catch (IOException ex) {
                Logger.getLogger(MapBucket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return metadata;
    }

    @Override
    public String getRequestURL() {
        MetaData m = getMetaData();
        if (m == null) {
            return null;
        }
        String req = m.request;
        if (req == null) {
            return null;
        }
        if (req.startsWith("HTTP:")) {
            req = req.substring("HTTP:".length());
        }
        return req;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        MetaData m = getMetaData();
        if (m == null) {
            return null;
        }
        String responseHead = m.response.get("response-head");
        if (responseHead == null) {
            return null;
        }
        String[] headers = responseHead.split("\r\n");
        Map<String, String> ret = new HashMap<>();
        for (int h = 1; h < headers.length; h++) {
            String hs = headers[h];
            if (hs.contains(":")) {
                String[] hp = hs.split(":");
                ret.put(hp[0].trim(), hp[1].trim());
            }
        }
        return ret;
    }

    @Override
    public String getStatusLine() {
        MetaData m = getMetaData();
        if (m == null) {
            return null;
        }
        String responseHead = m.response.get("response-head");
        String[] headers = responseHead.split("\r\n");
        return headers[0];
    }

    @Override
    public String getRequestMethod() {
        return "GET";  //No POST caching in Firefox
    }

    @Override
    public InputStream getResponseRawDataStream() {
        try {
            return dataLocation.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(MapBucket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
