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
package com.jpexs.decompiler.flash.tags.font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class CharacterRanges {

    public static int rangeCount() {
        return languages.size();
    }

    public static String rangeName(int index) {
        return languages.get(index).name;
    }

    public static int glyphCount(int index) {
        List<Range> ranges = languages.get(index).ranges;
        int cnt = 0;
        for (Range r : ranges) {
            for (int i = r.from; i <= r.to; i++) {
                cnt++;
            }
        }
        return cnt;
    }

    public static List<String> rangeNames() {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < rangeCount(); i++) {
            ret.add(rangeName(i));
        }
        return ret;
    }

    public static int[] rangeCodes(int index) {
        List<Range> ranges = languages.get(index).ranges;
        List<Integer> ret = new ArrayList<>();
        for (Range r : ranges) {
            for (int i = r.from; i <= r.to; i++) {
                ret.add(i);
            }
        }
        int[] retArr = new int[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            retArr[i] = ret.get(i);
        }
        return retArr;
    }

    private static class Range {

        public int from;

        public int to;

        public Range(int fromto) {
            from = fromto;
            to = fromto;
        }

        public Range(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            if (from == to) {
                return "" + from;
            }
            return "" + from + "-" + to;
        }
    }

    private static class Language {

        String name;

        List<Range> ranges;

        public Language(String name) {
            this.name = name;
            ranges = new ArrayList<>();
        }

        public Language(String name, List<Range> ranges) {
            this.name = name;
            this.ranges = ranges;
        }
    }

    private static final List<Language> languages = new ArrayList<>();

    static {
        BufferedReader br = new BufferedReader(new InputStreamReader(CharacterRanges.class.getResourceAsStream("/com/jpexs/decompiler/flash/tags/font/character_ranges.txt")));
        String s;
        try {
            while ((s = br.readLine()) != null) {
                String[] parts = s.split(":");
                Language lng = new Language(parts[0]);
                String[] ranges = parts[1].split(",");
                for (String r : ranges) {
                    if (r.contains("-")) {
                        String[] fromTo = r.split("-");
                        lng.ranges.add(new Range(Integer.parseInt(fromTo[0], 16), Integer.parseInt(fromTo[1], 16)));
                    } else {
                        lng.ranges.add(new Range(Integer.parseInt(r, 16)));
                    }
                }
                languages.add(lng);
            }
        } catch (IOException ex) {
            Logger.getLogger(CharacterRanges.class.getName()).log(Level.SEVERE, "Cannot read unicode character ranges", ex);
        }

    }
}
