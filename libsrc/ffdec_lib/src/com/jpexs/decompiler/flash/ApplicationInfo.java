/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author JPEXS
 */
public class ApplicationInfo {

    public static final String APPLICATION_NAME = "JPEXS Free Flash Decompiler";
    public static final String SHORT_APPLICATION_NAME = "FFDec";
    public static final String VENDOR = "JPEXS";
    public static String version = "";
    public static String build = "";
    public static boolean nightly = false;
    public static String applicationVerName;
    public static String shortApplicationVerName;
    public static final String PROJECT_PAGE = "http://www.free-decompiler.com/flash";
    public static String updatePageStub = "http://www.free-decompiler.com/flash/update.html?currentVersion=";
    public static String updatePage;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties prop = new Properties();
        try {
            prop.load(ApplicationInfo.class.getResourceAsStream("/project.properties"));
            version = prop.getProperty("version");
            build = prop.getProperty("build");
            nightly = prop.getProperty("nightly").equals("true");
            if (nightly) {
                version = version + " nightly build " + build.substring(0, 7);
            }
        } catch (IOException | NullPointerException ex) {
            //ignore
            version = "unknown";
        }

        applicationVerName = APPLICATION_NAME + " v." + version;
        updatePage = updatePageStub + version;
        shortApplicationVerName = SHORT_APPLICATION_NAME + " v." + version;
    }

}
