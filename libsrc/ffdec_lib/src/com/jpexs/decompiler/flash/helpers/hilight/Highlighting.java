/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.helpers.hilight;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class Highlighting implements Serializable {

    /**
     * Type
     */
    public HighlightType type;

    /**
     * Highlighted text
     */
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

    /**
     * Gets properties.
     * @return Properties
     */
    public HighlightData getProperties() {
        return properties;
    }

    /**
     * Searches for a highlighting with the specified properties.
     * @param list List of highlightings
     * @param properties Highlighting properties
     * @param from Starting position
     * @param to Ending position
     * @return Highlighting
     */
    public static Highlighting search(HighlightingList list, HighlightData properties, long from, long to) {
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

    /**
     * Searches for a highlighting with the specified position.
     * @param list List of highlightings
     * @param pos Position
     * @return Highlighting
     */
    public static Highlighting searchPos(HighlightingList list, long pos) {
        return searchPos(list, pos, -1, -1);
    }

    /**
     * Searches for a highlighting with the specified position.
     * @param list List of highlightings
     * @param pos Position
     * @param from Starting position
     * @param to Ending position
     * @return Highlighting
     */
    public static Highlighting searchPos(HighlightingList list, long pos, long from, long to) {
        return searchPosNew(list, pos, from, to);
    }

    /**
     * Searches for a highlighting with the specified position. New version.
     * @param list List of highlightings
     * @param pos Position
     * @param from Starting position
     * @param to Ending position
     * @return Highlighting
     */
    public static Highlighting searchPosNew(HighlightingList list, long pos, long from, long to) {
        Highlighting[] hmap = posToHighlightMap(list);
        if (pos > -1) {
            if (pos >= hmap.length) {
                return null;
            }
            return hmap[(int) pos];
        }
        if (from == -1) {
            from = 0;
        }
        if (to == -1) {
            to = hmap.length;
        }
        for (long i = from; i < to; i++) {
            Highlighting h = hmap[(int) i];
            if (h != null) {
                return h;
            }
        }
        return null;
    }

    /*public static Highlighting searchPosOld(HighlightingList list, long pos, long from, long to) {                
        
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
    }*/

    private static final Map<HighlightingList, Highlighting[]> listToPosMap = new WeakHashMap<>();

    private static Highlighting[] posToHighlightMap(HighlightingList list) {
        if (list.isEmpty()) {
            return new Highlighting[0];
        }
        if (listToPosMap.containsKey(list)) {
            return listToPosMap.get(list);
        }
        Highlighting lastH = list.get(list.size() - 1);
        int maxPos = lastH.startPos + Math.max(1, lastH.len);
        Highlighting[] map = new Highlighting[maxPos];
        for (Highlighting h : list) {
            for (int i = h.startPos; i < h.startPos + Math.max(1, h.len); i++) {
                Highlighting oldH = map[i];
                if (oldH == null) {
                    map[i] = h;
                    continue;
                }
                if (h.len < oldH.len) {
                    map[i] = h;
                }
            }
        }
        listToPosMap.put(list, map);
        return map;
    }

    /**
     * Searches for a highlighting with the specified offset.
     * @param list List of highlightings
     * @param offset Offset
     * @return Highlighting
     */
    public static Highlighting searchOffset(HighlightingList list, long offset) {
        return searchOffset(list, offset, -1, -1);
    }

    /**
     * Searches for a highlighting with the specified offset.
     * @param list List of highlightings
     * @param offset Offset
     * @param from Starting position
     * @param to Ending position
     * @return Highlighting
     */
    public static Highlighting searchOffset(HighlightingList list, long offset, long from, long to) {
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

    /**
     * Searches for a highlighting with the specified index.
     * @param list List of highlightings
     * @param index Index
     * @return Highlighting
     */
    public static Highlighting searchIndex(HighlightingList list, long index) {
        return searchIndex(list, index, -1, -1);
    }

    /**
     * Searches for a highlighting with the specified index.
     * @param list List of highlightings
     * @param index Index
     * @param from Starting position
     * @param to Ending position
     * @return Highlighting
     */
    public static Highlighting searchIndex(HighlightingList list, long index, long from, long to) {
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

    /**
     * Search all highlightings with the specified position.
     * @param list List of highlightings
     * @param pos Position
     * @return List of highlightings
     */
    public static HighlightingList searchAllPos(HighlightingList list, long pos) {
        HighlightingList ret = new HighlightingList();
        for (Highlighting h : list) {
            if (pos == -1 || (pos >= h.startPos && (pos < h.startPos + h.len))) {
                ret.add(h);
            }
        }

        return ret;
    }

    /**
     * Search all highlightings with the specified index.
     * @param list List of highlightings
     * @param index Index
     * @return List of highlightings
     */
    public static HighlightingList searchAllIndexes(HighlightingList list, long index) {
        HighlightingList ret = new HighlightingList();
        for (Highlighting h : list) {
            long i = h.getProperties().index;
            if (i == index) {
                ret.add(h);
            }
        }

        return ret;
    }


    /**
     * Search all highlightings with the specified local name.
     * @param list List of highlightings
     * @param localName Local name
     * @return List of highlightings
     */
    public static HighlightingList searchAllLocalNames(HighlightingList list, String localName) {
        HighlightingList ret = new HighlightingList();
        for (Highlighting h : list) {
            if (localName.equals(h.getProperties().localName)) {
                ret.add(h);
            }
        }

        return ret;
    }

    @Override
    public String toString() {
        return startPos + "-" + (startPos + len) + " type:" + type;
    }

    /**
     * @param startPos Starting position
     * @param data Highlighting data
     * @param type Highlighting type
     * @param text Highlighted text
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
