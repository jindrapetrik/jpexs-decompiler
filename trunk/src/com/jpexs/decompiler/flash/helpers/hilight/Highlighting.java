/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.helpers.hilight;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.HilightType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class Highlighting implements Serializable {

    public HilightType type;
    public String hilightedText;
    /**
     * Starting position
     */
    public int startPos;
    /**
     * Length of highlighted text
     */
    public int len;
    private final Map<String, String> properties;

    public Long getPropertyLong(String key) {
        String dataStr = getPropertyString(key);
        if (dataStr == null) {
            return null;
        }
        try {
            return Long.parseLong(dataStr);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public String getPropertyString(String key) {
        return properties.get(key);
    }

    public static Highlighting search(List<Highlighting> list, long pos) {
        return search(list, pos, null, null, -1, -1);
    }

    public static Highlighting search(List<Highlighting> list, String property, String value) {
        return search(list, -1, property, value, -1, -1);
    }

    public static Highlighting search(List<Highlighting> list, String property, String value, int from, int to) {
        return search(list, -1, property, value, from, to);
    }

    public static Highlighting search(List<Highlighting> list, long pos, String property, String value, long from, long to) {
        Highlighting ret = null;
        for (Highlighting h : list) {
            if (property != null) {
                String v = h.getPropertyString(property);
                if (v == null) {
                    if (value != null) {
                        continue;
                    }
                } else {
                    if (!v.equals(value)) {
                        continue;
                    }
                }
            }
            if (from > -1) {
                if (h.startPos < from) {
                    continue;
                }
            }
            if (to > -1) {
                if (h.startPos > to) {
                    continue;
                }
            }
            if (pos == -1 || (pos >= h.startPos && (pos < h.startPos + h.len))) {
                if (ret == null || h.startPos > ret.startPos) { //get the closest one
                    ret = h;
                }
            }
            if (pos == -1 && ret != null) {
                return ret;
            }
        }

        if (Configuration.debugMode.get()) {
            if (ret != null) {
                System.out.println("Highlight found: " + ret.hilightedText);
            }
        }

        return ret;
    }

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return startPos + "-" + (startPos + len) + " type:" + type;
    }

    /**
     *
     * @param startPos Starting position
     * @param data Highlighting data
     * @param type Highlighting type
     */
    public Highlighting(int startPos, Map<String, String> data, HilightType type, String text) {
        this.startPos = startPos;
        this.type = type;
        if (Configuration.debugMode.get()) {
            this.hilightedText = text;
        }
        this.properties = data;
    }
}
