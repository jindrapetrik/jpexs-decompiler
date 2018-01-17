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
package com.jpexs.browsers.cache.chrome;

import com.jpexs.browsers.cache.RafInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class CacheAddr {

    private static final int EXTERNAL = 0;

    private static final int RANKINGS = 1;

    private static final int BLOCK_256 = 2;

    private static final int BLOCK_1K = 3;

    private static final int BLOCK_4K = 4;

    private static final int BLOCK_FILES = 5;

    private static final int BLOCK_ENTRIES = 6;

    private static final int BLOCK_EVICTED = 7;

    private static final String[] blockNames = new String[]{"EXTERNAL", "RANKINGS", "BLOCK_256", "BLOCK_1K", "BLOCK_4K", "BLOCK_FILES", "BLOCK_ENTRIES", "BLOCK_EVICTED"};

    private static final int[] blockSizes = new int[]{0, 36, 256, 1024, 4096, 8, 104, 48};

    private static final long kInitializedMask = 0x80000000L;

    private static final long kFileTypeMask = 0x70000000L;

    private static final int kFileTypeOffset = 28;

    private static final long kReservedBitsMask = 0x0c000000L;

    private static final long kNumBlocksMask = 0x03000000L;

    private static final int kNumBlocksOffset = 24;

    private static final long kFileSelectorMask = 0x00ff0000L;

    private static final int FileSelectorOffset = 16;

    private static final long kStartBlockMask = 0x0000FFFFL;

    private static final long kFileNameMask = 0x0FFFFFFFL;

    private final boolean initialized;

    private final int fileType;

    private int numBlocks;

    private int fileSelector;

    private int startBlock;

    private int fileName;

    private final long val;

    private final File rootPath;

    private final Map<Integer, RandomAccessFile> dataFiles;

    private final File externalFilesDir;

    public CacheAddr(InputStream is, File rootPath, Map<Integer, RandomAccessFile> dataFiles, File externalFilesDir) throws IOException {
        this.dataFiles = dataFiles;
        this.rootPath = rootPath;
        this.externalFilesDir = externalFilesDir;
        IndexInputStream iis = new IndexInputStream(is);
        val = iis.readUInt32();
        initialized = (val & kInitializedMask) == kInitializedMask;
        fileType = (int) ((val & kFileTypeMask) >> kFileTypeOffset);
        if (fileType == EXTERNAL) {
            fileName = (int) (val & kFileNameMask);
        } else {
            numBlocks = (int) ((val & kNumBlocksMask) >> kNumBlocksOffset);
            fileSelector = (int) ((val & kFileSelectorMask) >> FileSelectorOffset);
            startBlock = (int) (val & kStartBlockMask);
        }
    }

    @Override
    public String toString() {

        String ft = blockNames[fileType];
        if (fileType == EXTERNAL) {
            return ft + ":" + fileName;
        }
        if (!initialized) {
            return "uninitialized";
        }
        return ft + ": numBlocks " + numBlocks + " fileSelector " + fileSelector + " startBlock " + startBlock;
    }

    public InputStream getInputStream() throws IOException {
        if (!initialized) {
            return null;
        }
        switch (fileType) {
            case EXTERNAL:
                String fileNameStr = Long.toHexString(fileName);
                while (fileNameStr.length() < 6) {
                    fileNameStr = "0" + fileNameStr;
                }
                fileNameStr = "f_" + fileNameStr;
                return new RafInputStream(new RandomAccessFile(new File(externalFilesDir, fileNameStr), "r"));
            case BLOCK_1K:
            case BLOCK_256:
            case BLOCK_4K:

                RandomAccessFile raf;

                if (dataFiles.containsKey(fileSelector)) {
                    raf = dataFiles.get(fileSelector);
                } else {
                    raf = new RandomAccessFile(rootPath + "\\data_" + fileSelector, "r");
                    dataFiles.put(fileSelector, raf);
                }
                raf.seek(BlockFileHeader.kBlockHeaderSize + startBlock * blockSizes[fileType]);
                return new RafInputStream(raf);
        }
        return null;
    }
}
