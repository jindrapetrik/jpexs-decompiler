package com.jpexs.browsers.cache.chrome;

/**
 *
 * @author JPEXS
 */
public class RankingsNode {

    public long last_used;
    public long last_modified;
    CacheAddr next;
    CacheAddr prev;
    CacheAddr contents;
    int dirty;
    long self_hash;
}
