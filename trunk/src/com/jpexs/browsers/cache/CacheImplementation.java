package com.jpexs.browsers.cache;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface CacheImplementation {

    public List<CacheEntry> getEntries();

    public void refresh();
}
