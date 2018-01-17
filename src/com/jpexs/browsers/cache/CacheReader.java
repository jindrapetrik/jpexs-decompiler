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
