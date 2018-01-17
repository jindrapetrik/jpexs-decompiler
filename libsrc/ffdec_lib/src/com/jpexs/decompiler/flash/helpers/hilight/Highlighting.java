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
package com.jpexs.decompiler.flash.helpers.hilight;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class Highlighting implements Serializable {

    public HighlightType type;

    public String HighlightedText;

    /**
     * Starting position
     */
    public int startPos;

    /**
     * Length of highlighted text
     */
    public int len;

    private final HighlightData properties;

    public HighlightData getProperties() {
        return properties;
    }

    public static Highlighting search(List<Highlighting> list, HighlightData properties, long from, long to) {
        Highlighting ret = null;
        looph:
        for (Highlighting h : list) {
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
            HighlightData hProp = h.getProperties();
            if (properties.declaration && !hProp.declaration) {
                continue;
            }
            if (properties.declaredType != null && !properties.declaredType.equals(hProp.declaredType)) {
                continue;
            }
            if (properties.localName != null && !properties.localName.equals(hProp.localName)) {
                continue;
            }
            if (properties.specialValue != null && !properties.specialValue.equals(hProp.specialValue)) {
                continue;
            }

            return h;
        }

        return null;
    }

    public static Highlighting searchPos(List<Highlighting> list, long pos) {
        return searchPos(list, pos, -1, -1);
    }

    public static Highlighting searchPos(List<Highlighting> list, long pos, long from, long to) {
        Highlighting ret = null;
        looph:
        for (Highlighting h : list) {
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
            if (pos == -1 || (pos >= h.startPos && ((h.len == 0 && pos == h.startPos) || pos < h.startPos + h.len))) {
                if (ret == null || h.startPos > ret.startPos) { //get the closest one
                    ret = h;
                }
            }
            if (pos == -1 && ret != null) {
                return ret;
            }
        }

        return ret;
    }

    public static Highlighting searchOffset(List<Highlighting> list, long offset) {
        return searchOffset(list, offset, -1, -1);
    }

    public static Highlighting searchOffset(List<Highlighting> list, long offset, long from, long to) {
        looph:
        for (Highlighting h : list) {
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
            if (h.getProperties().offset != offset) {
                continue;
            }

            return h;
        }

        return null;
    }

    public static Highlighting searchIndex(List<Highlighting> list, long index) {
        return searchIndex(list, index, -1, -1);
    }

    public static Highlighting searchIndex(List<Highlighting> list, long index, long from, long to) {
        looph:
        for (Highlighting h : list) {
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
            if (h.getProperties().index != index) {
                continue;
            }

            return h;
        }

        return null;
    }

    public static List<Highlighting> searchAllPos(List<Highlighting> list, long pos) {
        List<Highlighting> ret = new ArrayList<>();
        for (Highlighting h : list) {
            if (pos == -1 || (pos >= h.startPos && (pos < h.startPos + h.len))) {
                ret.add(h);
            }
        }

        return ret;
    }

    public static List<Highlighting> searchAllIndexes(List<Highlighting> list, long index) {
        List<Highlighting> ret = new ArrayList<>();
        for (Highlighting h : list) {
            long i = h.getProperties().index;
            if (i == index) {
                ret.add(h);
            }
        }

        return ret;
    }

    public static List<Highlighting> searchAllLocalNames(List<Highlighting> list, String localName) {
        List<Highlighting> ret = new ArrayList<>();
        for (Highlighting h : list) {
            if (localName.equals(h.getProperties().localName)) {
                ret.add(h);
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
     * @param text
     */
    public Highlighting(int startPos, HighlightData data, HighlightType type, String text) {
        this.startPos = startPos;
        this.type = type;
        if (Configuration._debugMode.get()) {
            this.HighlightedText = text;
        }
        this.properties = data;
    }
}
