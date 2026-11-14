/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.flash.fla.converter.FlaFormatVersion;

/**
 * FLA version enumeration.
 *
 * @author JPEXS
 */
public enum FLAVersion {
    F1("F1", "FutureSplash Animator", FlaFormatVersion.F1, null, 1, 1, 1),
    F2("F2", "Macromedia Flash 2", FlaFormatVersion.F2, null, 2, 1, 1),
    F3("F3", "Macromedia Flash 3", FlaFormatVersion.F3, null, 3, 1, 1),
    F4("F4", "Macromedia Flash 4", FlaFormatVersion.F4, null, 4, 1, 1),
    F5("F5", "Macromedia Flash 5", FlaFormatVersion.F5, null, 5, 1, 1),
    MX("MX", "Macromedia Flash MX", FlaFormatVersion.MX, null, 6, 1, 1),
    MX2004("MX2004", "Macromedia Flash MX 2004", FlaFormatVersion.MX2004, null, 7, 1, 2),
    F8("F8", "Macromedia Flash 8", FlaFormatVersion.F8, null, 8, 1, 2),
    CS3("CS3", "Adobe Flash Professional CS 3", FlaFormatVersion.CS3, null, 9, 1, 3),
    CS4("CS4", "Adobe Flash Professional CS 4", FlaFormatVersion.CS4, null, 10, 1, 3),
    CS5("CS5", "Adobe Flash Professional CS 5", null, "2.0", 10, 1, 3),
    CS5_5("CS5.5", "Adobe Flash Professional CS 5.5", null, "2.1", 11, 1, 3),
    CS6("CS6", "Adobe Flash Professional CS 6", null, "2.2", 17, 1, 3),
    CC("CC", "Adobe Flash Professional CC", null, "2.4", Integer.MAX_VALUE, 3, 3);
    private final FlaFormatVersion cfbFlaVersion;

    private final String xflVersion;

    private final String shortName;

    private final String applicationName;

    private final int maxSwfVersion;
        
    private final int minASVersion;

    private final int maxASVersion;

    private FLAVersion(String shortName, String applicationName, FlaFormatVersion cfbFlaVersion, String xflVersion, int maxSwfVersion, int minASVersion, int maxASVersion) {
        this.cfbFlaVersion = cfbFlaVersion;
        this.xflVersion = xflVersion;
        this.shortName = shortName;
        this.applicationName = applicationName;
        this.maxSwfVersion = maxSwfVersion;
        this.minASVersion = minASVersion;
        this.maxASVersion = maxASVersion;
    }

    public FlaFormatVersion getCfbFlaVersion() {
        return cfbFlaVersion;
    }   
    
    public String xflVersion() {
        return xflVersion;
    }

    public boolean supportsLibraryFolders() {
        return ordinal() >= F4.ordinal();
    }

    public int maxSwfVersion() {
        return maxSwfVersion;
    }

    public int minASVersion() {
        return minASVersion;
    }
    
    public int maxASVersion() {
        return maxASVersion;
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
            if (v.xflVersion != null && v.xflVersion.equals(s)) {
                return v;
            }
        }
        return null;
    }
}
