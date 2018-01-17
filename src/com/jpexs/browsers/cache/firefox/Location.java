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
package com.jpexs.browsers.cache.firefox;

import com.jpexs.browsers.cache.RafInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class Location {

    public int locationSelector;

    public int extraBlocks;

    public long blockNumber;

    public int fileGeneration;

    public int fileSize;

    public boolean isMetadata;

    public long hash;

    private final File rootDir;

    public static final long eReservedMask = 0x4C000000L;

    public static final long eLocationSelectorMask = 0x30000000L;

    public static final int eLocationSelectorOffset = 28;

    public static final long eExtraBlocksMask = 0x03000000L;

    public static final int eExtraBlocksOffset = 24;

    public static final long eBlockNumberMask = 0x00FFFFFFL;

    public static final long eFileGenerationMask = 0x000000FFL;

    public static final long eFileSizeMask = 0x00FFFF00L;

    public static final int eFileSizeOffset = 8;

    public static final long eFileReservedMask = 0x4F000000L;

    public static int size_shift(int idx) {
        return (2 * ((idx) - 1));
    }

    public static int bitmapSizeForIndex(int idx) {
        return ((idx > 0) ? (131072 >> size_shift(idx)) : 0);
    }

    public static int blockSizeForIndex(int idx) {
        return ((idx > 0) ? (256 << size_shift(idx)) : 0);
    }
    /*
     #define BLOCK_SIZE_FOR_INDEX(idx)  ((idx) ? (256    << SIZE_SHIFT(idx)) : 0)
     #define BITMAP_SIZE_FOR_INDEX(idx) ((idx) ? (131072 >> SIZE_SHIFT(idx)) : 0)
     */

    private final Map<Integer, RandomAccessFile> dataFiles;

    public InputStream getInputStream() throws IOException {
        String fileName = getFileName();
        if (locationSelector > 0) {
            RandomAccessFile raf = dataFiles.get(locationSelector);
            raf.seek(bitmapSizeForIndex(locationSelector) / 8 + blockSizeForIndex(locationSelector) * blockNumber);
            return new RafInputStream(raf);
        }
        return new FileInputStream(new File(rootDir, fileName));
    }

    public Location(long val, boolean isMetadata, long hash, File rootDir, Map<Integer, RandomAccessFile> dataFiles) {
        this.hash = hash;
        this.dataFiles = dataFiles;
        this.rootDir = rootDir;
        this.isMetadata = isMetadata;
        locationSelector = (int) ((val & eLocationSelectorMask) >> eLocationSelectorOffset);
        if (locationSelector > 0) {
            extraBlocks = (int) ((val & eExtraBlocksMask) >> eExtraBlocksOffset);
            blockNumber = (int) (val & eBlockNumberMask);
        } else {
            fileSize = (int) ((val & eFileSizeMask) >> eFileSizeOffset);
            fileGeneration = (int) (val & eFileGenerationMask);
        }
    }

    public String getFileName() {
        if (locationSelector > 0) {
            return "_CACHE_00" + locationSelector + "_";
        }
        String hashHex = Long.toHexString(hash & 0xffffffffL).toUpperCase();
        while (hashHex.length() < 8) {
            hashHex = "0" + hashHex;
        }
        String genHex = Integer.toHexString(fileGeneration);
        while (genHex.length() < 2) {
            genHex = "0" + genHex;
        }
        return hashHex.charAt(0) + File.separator + hashHex.substring(1, 1 + 2) + File.separator + hashHex.substring(3) + (isMetadata ? "m" : "d") + genHex;
    }

    @Override
    public String toString() {
        if (locationSelector > 0) {
            return getFileName() + " block " + blockNumber + " extraBlocks " + extraBlocks;
        }
        return getFileName();
    }
}
