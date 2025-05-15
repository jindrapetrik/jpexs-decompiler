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
package com.jpexs.decompiler.flash;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Flash player version to SWF version and vice versa.
 *
 * @author JPEXS
 */
public class FlashPlayerVersion {

    private static final Map<String, Integer> flashPlayerToSwfVersion = new HashMap<>();
    private static final Map<String, Integer> airToSwfVersion = new HashMap<>();
    private static final Map<Integer, String> swfVersionToFlashPlayer = new HashMap<>();
    private static final Map<Integer, String> swfVersionToAir = new HashMap<>();

    static {
        flashPlayerToSwfVersion.put("9.0", 9); //9.0.115.0
        flashPlayerToSwfVersion.put("10.0", 10);
        flashPlayerToSwfVersion.put("10.1", 10);
        flashPlayerToSwfVersion.put("10.2", 11);
        flashPlayerToSwfVersion.put("10.3", 12);
        flashPlayerToSwfVersion.put("11.0", 13);
        flashPlayerToSwfVersion.put("11.1", 14);
        flashPlayerToSwfVersion.put("11.2", 15);
        flashPlayerToSwfVersion.put("11.3", 16);
        flashPlayerToSwfVersion.put("11.4", 17);
        flashPlayerToSwfVersion.put("11.5", 18);
        flashPlayerToSwfVersion.put("11.6", 19);
        flashPlayerToSwfVersion.put("11.7", 20);
        flashPlayerToSwfVersion.put("11.8", 21);
        flashPlayerToSwfVersion.put("11.9", 22);
        flashPlayerToSwfVersion.put("12.0", 23);
        flashPlayerToSwfVersion.put("13.0", 24);
        flashPlayerToSwfVersion.put("14.0", 25);
        flashPlayerToSwfVersion.put("15.0", 26);
        flashPlayerToSwfVersion.put("16.0", 27);
        flashPlayerToSwfVersion.put("17.0", 28);
        flashPlayerToSwfVersion.put("18.0", 29);
        flashPlayerToSwfVersion.put("19.0", 30);
        flashPlayerToSwfVersion.put("20.0", 31);
        flashPlayerToSwfVersion.put("21.0", 32);
        flashPlayerToSwfVersion.put("22.0", 33);
        flashPlayerToSwfVersion.put("23.0", 34);
        flashPlayerToSwfVersion.put("24.0", 35);
        flashPlayerToSwfVersion.put("25.0", 36);
        flashPlayerToSwfVersion.put("26.0", 37);
        flashPlayerToSwfVersion.put("27.0", 38);
        flashPlayerToSwfVersion.put("28.0", 39);
        flashPlayerToSwfVersion.put("29.0", 40);
        flashPlayerToSwfVersion.put("30.0", 41);
        flashPlayerToSwfVersion.put("31.0", 42);
        flashPlayerToSwfVersion.put("32.0", 43);
        flashPlayerToSwfVersion.put("33.0", 44);
        flashPlayerToSwfVersion.put("33.1", 44);
        flashPlayerToSwfVersion.put("50.0", 50);
        flashPlayerToSwfVersion.put("51.0", 51);

        airToSwfVersion.put("1.5", 10);
        airToSwfVersion.put("2.0", 10);
        airToSwfVersion.put("2.6", 11);
        airToSwfVersion.put("2.7", 12);
        airToSwfVersion.put("3.0", 13);
        airToSwfVersion.put("3.1", 14);
        airToSwfVersion.put("3.2", 15);
        airToSwfVersion.put("3.3", 16);
        airToSwfVersion.put("3.4", 17);
        airToSwfVersion.put("3.5", 18);
        airToSwfVersion.put("3.6", 19);
        airToSwfVersion.put("3.7", 20);
        airToSwfVersion.put("3.8", 21);
        airToSwfVersion.put("3.9", 22);
        airToSwfVersion.put("4.0", 23);
        airToSwfVersion.put("13.0", 24);
        airToSwfVersion.put("14.0", 25);
        airToSwfVersion.put("15.0", 26);
        airToSwfVersion.put("16.0", 27);
        airToSwfVersion.put("17.0", 28);
        airToSwfVersion.put("18.0", 29);
        airToSwfVersion.put("19.0", 30);
        airToSwfVersion.put("20.0", 31);
        airToSwfVersion.put("21.0", 32);
        airToSwfVersion.put("22.0", 33);
        airToSwfVersion.put("23.0", 34);
        airToSwfVersion.put("24.0", 35);
        airToSwfVersion.put("25.0", 36);
        airToSwfVersion.put("26.0", 37);
        airToSwfVersion.put("27.0", 38);
        airToSwfVersion.put("28.0", 39);
        airToSwfVersion.put("29.0", 40);
        airToSwfVersion.put("30.0", 41);
        airToSwfVersion.put("31.0", 42);
        airToSwfVersion.put("32.0", 43);
        airToSwfVersion.put("33.0", 44);
        airToSwfVersion.put("33.1", 44);
        airToSwfVersion.put("50.0", 50);
        airToSwfVersion.put("51.0", 51);

        for (String flashPlayer : flashPlayerToSwfVersion.keySet()) {
            int swfVersion = flashPlayerToSwfVersion.get(flashPlayer);
            if (!swfVersionToFlashPlayer.containsKey(swfVersion)) {
                swfVersionToFlashPlayer.put(swfVersion, flashPlayer);
            }
        }

        for (String air : airToSwfVersion.keySet()) {
            int swfVersion = airToSwfVersion.get(air);
            if (!swfVersionToAir.containsKey(swfVersion)) {
                swfVersionToAir.put(swfVersion, air);
            }
        }
    }

    /**
     * Gets Flash player version by SWF version
     *
     * @param swfVersion SWF version
     * @return Flash player version or null if not found
     */
    public static String getFlashPlayerBySwfVersion(int swfVersion) {
        if (swfVersion > 51) {
            return "51.0"; //??
        }
        return swfVersionToFlashPlayer.get(swfVersion);
    }

    /**
     * Gets SWF version by Flash player version.
     *
     * @param flashPlayerVersion Flash player version
     * @return SWF version or -1 if not found
     */
    public static int getSwfVersionByFlashPlayer(String flashPlayerVersion) {
        if (!flashPlayerToSwfVersion.containsKey(flashPlayerVersion)) {
            return -1;
        }
        return flashPlayerToSwfVersion.get(flashPlayerVersion);
    }

    /**
     * Gets Air version by SWF version
     *
     * @param swfVersion SWF version
     * @return Air version or null if not found
     */
    public static String getAirBySwfVersion(int swfVersion) {
        if (swfVersion > 51) {
            return "51.0"; //??
        }
        return swfVersionToFlashPlayer.get(swfVersion);
    }

    /**
     * Gets SWF version by Air version.
     *
     * @param air Air version
     * @return SWF version or -1 if not found
     */
    public static int getSwfVersionByAir(String air) {
        if (!airToSwfVersion.containsKey(air)) {
            return -1;
        }
        return airToSwfVersion.get(air);
    }
}
