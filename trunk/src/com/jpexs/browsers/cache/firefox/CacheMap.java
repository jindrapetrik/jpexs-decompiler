package com.jpexs.browsers.cache.firefox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class CacheMap {

    public long version;
    public long datasize;
    public long entryCount;
    public long dirtyFlag;
    public long recordCount;
    public long evictionRank[];
    public long bucketUsage[];
    public List<MapBucket> mapBuckets;

    public CacheMap(File file) throws IOException {
        File cacheDir = file.getParentFile();
        CacheInputStream cis = new CacheInputStream(new FileInputStream(file));
        version = cis.readInt32();
        datasize = cis.readInt32();
        entryCount = cis.readInt32();
        dirtyFlag = cis.readInt32();
        recordCount = cis.readInt32();
        evictionRank = new long[32];
        for (int i = 0; i < evictionRank.length; i++) {
            evictionRank[i] = cis.readInt32();
        }
        bucketUsage = new long[32];
        for (int i = 0; i < bucketUsage.length; i++) {
            bucketUsage[i] = cis.readInt32();
        }
        mapBuckets = new ArrayList<>();
        Map<Integer, RandomAccessFile> cacheFiles = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            cacheFiles.put(i, new RandomAccessFile(new File(cacheDir, "_CACHE_00" + i + "_"), "r"));
        }
        while (cis.available() > 0) {
            MapBucket mb = new MapBucket(cis, cacheDir, cacheFiles);
            if (mb.hash == 0) {
                continue;
            }
            mapBuckets.add(mb);
        }
    }
}
