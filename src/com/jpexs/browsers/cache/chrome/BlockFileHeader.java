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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class BlockFileHeader {

    protected static final int kBlockHeaderSize = 8192;  // Two pages: almost 64k entries

    private static final int kMaxBlocks = (kBlockHeaderSize - 80) * 8;

    private final long magic;   // c3 ca 04 c1

    private final long version; // 00 00 02 00

    private final int this_file;

    private final int next_file;

    private final int entry_size;

    private final int num_entries;

    private final int max_entries;

    private int[] empty = new int[4];

    private int[] hints = new int[4];

    private final int updating;

    private int[] user = new int[5];

    private final long allocation_map[];

    public BlockFileHeader(InputStream is) throws IOException {
        this.allocation_map = new long[kMaxBlocks / 32];
        IndexInputStream iis = new IndexInputStream(is);
        magic = iis.readUInt32();
        version = iis.readUInt32();
        this_file = iis.readInt16();
        next_file = iis.readInt16();
        entry_size = iis.readInt32();
        num_entries = iis.readInt32();
        max_entries = iis.readInt32();
        empty = new int[4];
        for (int i = 0; i < 4; i++) {
            empty[i] = iis.readInt32();
        }
        hints = new int[4];
        for (int i = 0; i < 4; i++) {
            hints[i] = iis.readInt32();
        }
        updating = iis.readInt32();
        user = new int[5];
        for (int i = 0; i < 5; i++) {
            user[i] = iis.readInt32();
        }
        for (int i = 0; i < allocation_map.length; i++) {
            allocation_map[i] = iis.readUInt32();
        }
    }
}
