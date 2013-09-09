package com.jpexs.browsers.cache;

import com.jpexs.browsers.cache.chrome.ChromeCache;
import com.jpexs.browsers.cache.firefox.FirefoxCache;

/**
 *
 * @author JPEXS
 */
public class CacheReader {

    public static final String BROWSER_FIREFOX = "firefox";
    public static final String BROWSER_CHROME = "chrome";

    public static String[] availableBrowsers() {
        return new String[]{BROWSER_FIREFOX, BROWSER_CHROME};
    }

    public static CacheImplementation getBrowserCache(String browser) {
        switch (browser) {
            case BROWSER_CHROME:
                return ChromeCache.getInstance();
            case BROWSER_FIREFOX:
                return FirefoxCache.getInstance();
            default:
                throw new IllegalArgumentException("Invalid browser:" + browser);
        }
    }
}
