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
package com.jpexs.decompiler.flash.xfl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public enum FLAVersion {

    CS5("CS5", "Flash CS 5", "2.0", 10),
    CS5_5("CS5.5", "Flash CS 5.5", "2.1", 11),
    CS6("CS6", "Flash CS 6", "2.2", 17),
    CC("CC", "Flash CC", "2.4", Integer.MAX_VALUE) {
        @Override
        public int minASVersion() {
            return 3; //AS 1/2 not supported anymore
        }
    };

    private final String xflVersion;

    private final String shortName;

    private final String applicationName;

    private final int maxSwfVersion;

    private static final Map<Integer, String> versionToPlayerMap = new HashMap<>();

    static {
        versionToPlayerMap.put(9, "FlashPlayer9.0"); // 9.0.115.0
        versionToPlayerMap.put(10, "FlashPlayer10.0"); //10 & 10.1
        versionToPlayerMap.put(11, "FlashPlayer10.2");
        versionToPlayerMap.put(12, "FlashPlayer10.3");
        versionToPlayerMap.put(13, "FlashPlayer11.0");
        versionToPlayerMap.put(14, "FlashPlayer11.1");
        versionToPlayerMap.put(15, "FlashPlayer11.2");
        versionToPlayerMap.put(16, "FlashPlayer11.3");
        versionToPlayerMap.put(17, "FlashPlayer11.4");
        versionToPlayerMap.put(18, "FlashPlayer11.5");
        versionToPlayerMap.put(19, "FlashPlayer11.6");
        versionToPlayerMap.put(20, "FlashPlayer11.7");
        versionToPlayerMap.put(21, "FlashPlayer11.8");
        versionToPlayerMap.put(22, "FlashPlayer11.9");
        versionToPlayerMap.put(23, "FlashPlayer12.0");
        versionToPlayerMap.put(24, "FlashPlayer13.0");
        versionToPlayerMap.put(25, "FlashPlayer14.0");
        versionToPlayerMap.put(26, "FlashPlayer15.0");
        versionToPlayerMap.put(27, "FlashPlayer16.0");
        versionToPlayerMap.put(28, "FlashPlayer17.0");
        versionToPlayerMap.put(29, "FlashPlayer18.0");
    }

    private FLAVersion(String shortName, String applicationName, String xflVersion, int maxSwfVersion) {
        this.xflVersion = xflVersion;
        this.shortName = shortName;
        this.applicationName = applicationName;
        this.maxSwfVersion = maxSwfVersion;
    }

    public String xflVersion() {
        return xflVersion;
    }

    public int maxSwfVersion() {
        return maxSwfVersion;
    }

    public int minASVersion() {
        return 1;
    }

    public String applicationName() {
        return applicationName;
    }

    public String shortName() {
        return shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    public static String swfVersionToPlayer(int version) {
        if (versionToPlayerMap.containsKey(version)) {
            return versionToPlayerMap.get(version);
        }
        return "";
    }

    public static FLAVersion fromString(String s) {
        if (s == null) {
            return null;
        }
        for (FLAVersion v : FLAVersion.values()) {
            if (v.shortName.toLowerCase().equals(s.toLowerCase())) {
                return v;
            }
            if (v.xflVersion.equals(s)) {
                return v;
            }
        }
        return null;
    }
}
