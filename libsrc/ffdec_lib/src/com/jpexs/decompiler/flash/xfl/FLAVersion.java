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
package com.jpexs.decompiler.flash.xfl;

/**
 * FLA version enumeration.
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
