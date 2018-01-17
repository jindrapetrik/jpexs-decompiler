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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class LruData {

    int[] pad1 = new int[2];

    int filled;

    int[] sizes = new int[5];

    CacheAddr[] heads = new CacheAddr[5];

    CacheAddr[] tails = new CacheAddr[5];

    CacheAddr transaction;

    int operation;

    int operation_list;

    int[] pad2 = new int[7];

    public LruData(InputStream is, File rootDir, Map<Integer, RandomAccessFile> dataFiles, File externalFilesDir) throws IOException {
        IndexInputStream iis = new IndexInputStream(is);
        pad1 = new int[2];
        pad1[0] = iis.readInt32();
        pad1[1] = iis.readInt32();
        filled = iis.readInt32();
        sizes = new int[5];
        for (int i = 0; i < 5; i++) {
            sizes[i] = iis.readInt32();
        }
        sizes = new int[5];
        for (int i = 0; i < 5; i++) {
            sizes[i] = iis.readInt32();
        }
        heads = new CacheAddr[5];
        for (int i = 0; i < 5; i++) {
            heads[i] = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        }
        tails = new CacheAddr[5];
        for (int i = 0; i < 5; i++) {
            tails[i] = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        }
        transaction = new CacheAddr(is, rootDir, dataFiles, externalFilesDir);
        operation = iis.readInt32();
        operation_list = iis.readInt32();
        pad2 = new int[7];
        for (int i = 0; i < 7; i++) {
            pad2[i] = iis.readInt32();
        }
    }
}
