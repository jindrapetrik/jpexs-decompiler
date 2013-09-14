package com.jpexs.browsers.cache.firefox;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class MetaData {

    public int majorVersion;
    public int minorVersion;
    public long location;
    public long fetchCount;
    public long firstFetchTime;
    public long lastFetchTime;
    public long expireTime;
    public long dataSize;
    public long requestSize;
    public long infoSize;
    public String request;
    public Map<String, String> response;

    public MetaData(InputStream is) throws IOException, IncompatibleVersionException {
        CacheInputStream cis = new CacheInputStream(is);
        majorVersion = cis.readInt16();
        if (majorVersion != 1) {
            throw new IncompatibleVersionException(majorVersion);
        }
        minorVersion = cis.readInt16();
        location = cis.readInt32();
        fetchCount = cis.readInt32();
        firstFetchTime = cis.readInt32();
        lastFetchTime = cis.readInt32();
        expireTime = cis.readInt32();
        dataSize = cis.readInt32();
        requestSize = cis.readInt32();
        infoSize = cis.readInt32();
        byte req[] = new byte[(int) requestSize];
        cis.read(req);
        request = new String(req, 0, (int) requestSize - 1/*Ends with char 0*/);
        byte res[] = new byte[(int) infoSize];
        cis.read(res);
        String responseStr = new String(res);
        int nulpos;
        boolean inKey = true;
        String key = null;
        response = new HashMap<>();
        while ((nulpos = responseStr.indexOf(0)) > 0) {
            String v = responseStr.substring(0, nulpos);
            responseStr = responseStr.substring(nulpos + 1);
            if (inKey) {
                key = v;
            } else {
                response.put(key, v);
            }
            inKey = !inKey;
        }
    }
}
