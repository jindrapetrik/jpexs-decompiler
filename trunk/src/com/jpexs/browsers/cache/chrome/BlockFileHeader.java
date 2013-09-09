package com.jpexs.browsers.cache.chrome;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class BlockFileHeader {

    static final int kBlockHeaderSize = 8192;  // Two pages: almost 64k entries
    static final int kMaxBlocks = (kBlockHeaderSize - 80) * 8;
    long magic;   // c3 ca 04 c1 
    long version; // 00 00 02 00
    int this_file;
    int next_file;
    int entry_size;
    int num_entries;
    int max_entries;
    int empty[] = new int[4];
    int hints[] = new int[4];
    int updating;
    int user[] = new int[5];
    long allocation_map[] = new long[kMaxBlocks / 32];

    public BlockFileHeader(InputStream is) throws IOException {
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
