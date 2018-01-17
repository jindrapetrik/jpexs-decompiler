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
public class IndexHeader {

    long magic;   //c3 ca 03 c1

    long version; //01 00 02 00

    int num_entries;

    int num_bytes;

    int last_file;

    int this_id;

    CacheAddr stats;

    int table_len;

    int crash;

    int experiment;

    long create_time;

    int[] pad = new int[52];

    LruData lru;

    public IndexHeader(InputStream is, File rootDir, Map<Integer, RandomAccessFile> dataFiles, File externalFilesDir) throws IOException {
        IndexInputStream iis = new IndexInputStream(is);
        magic = iis.readUInt32();
        version = iis.readUInt32();
        num_entries = iis.readInt32();
        num_bytes = iis.readInt32();
        last_file = iis.readInt32();
        this_id = iis.readInt32();
        stats = new CacheAddr(iis, rootDir, dataFiles, externalFilesDir);
        table_len = iis.readInt32();
        crash = iis.readInt32();
        experiment = iis.readInt32();
        create_time = iis.readUInt64();
        pad = new int[52];
        for (int i = 0; i < 52; i++) {
            pad[i] = iis.readInt32();
        }
        lru = new LruData(is, rootDir, dataFiles, externalFilesDir);
    }
}
