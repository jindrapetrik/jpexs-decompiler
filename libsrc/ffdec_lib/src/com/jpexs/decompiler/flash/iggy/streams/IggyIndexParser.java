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
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Index parser.
 *
 * @author JPEXS
 */
public class IggyIndexParser {

    private static Logger LOGGER = Logger.getLogger(IggyIndexParser.class.getName());

    /*static PrintWriter pw;

    static {
        try {
            pw = new PrintWriter("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_orig\\index2b.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IggyIndexParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                pw.println("" + record.getMessage());
            }

            @Override
            public void flush() {
                pw.flush();
            }

            @Override
            public void close() throws SecurityException {
                pw.close();
            }
        });
    }
     */
    /**
     * Parser for index data. It creates table of indices and table of offsets
     *
     * @param indexStream Stream of index
     * @param indexTableEntry Output index table
     * @param offsets Output list of offsets
     * @throws IOException on error
     */
    public static void parseIndex(boolean is64, ReadDataStreamInterface indexStream, List<Integer> indexTableEntry, List<Long> offsets) throws IOException {
        int indexTableSize = indexStream.readUI8();
        int[] indexTable = new int[indexTableSize];
        for (int i = 0; i < indexTableSize; i++) {
            int offset = indexStream.readUI8();
            LOGGER.fine(String.format("index_table_entry: %d", offset));
            indexTable[i] = offset;
            indexTableEntry.add(offset);
            int num = indexStream.readUI8();
            for (int j = 0; j < num; j++) {
                int locOffset = indexStream.readUI8();
                int type = indexStream.readUI8();
                LOGGER.finer(String.format("- local offset: %d, type: %d", locOffset, type));
            }
        }

        long offset = 0;
        int code;

        String tabs = "\t\t\t\t";

        LOGGER.finer(String.format("-- OFFSET: 0" + tabs));

        while ((code = indexStream.readUI8()) > -1) {
            LOGGER.finer(String.format("Code = 0x%02X", code));

            if (code < 0x80) { // 0-0x7F            
                LOGGER.finest("0-0x7F: code is directly an index to the index_table");
                // code is directly an index to the index_table
                if (code >= indexTableSize) {
                    LOGGER.severe(String.format("< 0x80: index is greater than index_table_size. %x > %x", code, indexTableSize));
                    return;
                }

                offset += indexTable[code];
                LOGGER.finest(String.format("LENGTH = indexTable[%d] = %d", code, indexTable[code]));

            } else if (code < 0xC0) { // 0x80-BF            
                LOGGER.finest("0x80-BF: table[0..255]*(code-0x7F)");
                int index;

                if ((index = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("< 0xC0: Cannot read index."));
                    return;
                }

                if (index >= indexTableSize) {
                    LOGGER.severe(String.format("< 0xC0: index is greater than index_table_size. %x > %x", index, indexTableSize));
                    return;
                }

                int n = code - 0x7F;
                LOGGER.finest(String.format("index = %d, n = code - 0x7F = %d", index, n));
                LOGGER.finest(String.format("LENGTH = indexTable[index] * n = indexTable[%d] * %d = %d", index, n, indexTable[index] * n));
                offset += indexTable[index] * n;
            } else if (code < 0xD0) { // 0xC0-0xCF            
                LOGGER.finest("0xC0-CF: code*2-0x17E");
                offset += ((code * 2) - 0x17E);
                LOGGER.finest(String.format("LENGTH = (code * 2) - 0x17E = (0x%02X * 2) - 0x17E = %d", code, ((code * 2) - 0x17E)));
            } else if (code < 0xE0) { // 0xD0-0xDF            
                LOGGER.finest("0xD0-0xDF: platform based");

                // Code here depends on plattform[0], we are assuming it is 1, as we checked in load function
                int i = code & 0xF;
                int n8;
                int n;

                if ((n8 = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("< 0xE0: Cannot read n."));
                    return;
                }

                n = n8 + 1;

                LOGGER.finest(String.format("i=%X", i));
                LOGGER.finest(String.format("n=%d", n));

                if (is64) {
                    if (i <= 2) {
                        offset += 8 * n; // Ptr type
                        LOGGER.finest(String.format("LENGTH = 8 * n = 8 * %d = %d", n, 8 * n));
                    } else if (i <= 4) {
                        offset += 2 * n;
                        LOGGER.finest(String.format("LENGTH = 2 * n = 2 * %d = %d", n, 2 * n));
                    } else if (i == 5) {
                        offset += 4 * n;
                        LOGGER.finest(String.format("LENGTH = 4 * n = 4 * %d = %d", n, 4 * n));
                    } else if (i == 6) {
                        offset += 8 * n; // 64 bits type
                        LOGGER.finest(String.format("LENGTH = 8 * n = 8 * %d = %d", n, 8 * n));
                    } else {
                        LOGGER.severe(String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                    }
                } else {
                    switch (i) {
                        case 2:
                            offset += 4 * n;  // Ptr type
                            LOGGER.finest(String.format("LENGTH = 4 * n = 4 * %d = %d", n, 4 * n));
                            break;
                        case 4:
                            offset += 2 * n;
                            LOGGER.finest(String.format("LENGTH = 2 * n = 2 * %d = %d", n, 2 * n));
                            break;
                        case 5:
                            offset += 4 * n; // 32 bits type
                            LOGGER.finest(String.format("LENGTH = 4 * n = 4 * %d = %d", n, 4 * n));
                            break;
                        case 6:
                            offset += 8 * n;
                            LOGGER.finest(String.format("LENGTH = 8 * n = 8 * %d = %d", n, 8 * n));
                            break;
                        default:
                            LOGGER.severe(String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                    }
                }
            } else if (code == 0xFC) {
                LOGGER.finest(String.format("0xFC: SKIP 1 "));
                indexStream.seek(1, SeekMode.CUR);
            } else if (code == 0xFD) {
                LOGGER.finest(String.format("0xFD: 0..255, skip 2 * 0..255 "));
                int n;
                int m;

                if ((n = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read n."));
                    return;
                }
                LOGGER.finest(String.format("n = %d", n));

                if ((m = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read m."));
                    return;
                }
                LOGGER.finest(String.format("m = %d", m));

                offset += n;
                LOGGER.finest(String.format("LENGTH = n = %d", n));
                StringBuilder locOffStr = new StringBuilder();
                StringBuilder platStr = new StringBuilder();
                for (int i = 0; i < m; i++) {
                    int localOffset = indexStream.readUI8();
                    int platformType = indexStream.readUI8();
                    if (i > 0) {
                        locOffStr.append(", ");
                        platStr.append(", ");
                    }
                    locOffStr.append(String.format("0x%02X", localOffset));
                    platStr.append(platformType);
                    LOGGER.finest(String.format("- localOffset 0x%02X, platformType %d", localOffset, platformType));
                }
                LOGGER.finer(String.format("stream.writeLengthCustom(%s,new int[]{%s},new int[]{%s}", n, locOffStr, platStr));
                //indexStream.seek(m * 2, SeekMode.CUR);
                //LOGGER.finest(String.format("SKIP m * 2 = skip %d * 2 = %d", m, m * 2));
            } else if (code == 0xFE) {
                LOGGER.finest(String.format("0xFD: 0..255 + 1 "));
                int n8;
                int n;

                if ((n8 = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFE: Cannot read n."));
                    return;
                }
                LOGGER.finest(String.format("n8 = %d", n8));

                n = n8 + 1;
                offset += n;
                LOGGER.finest(String.format("LENGTH = n8 + 1 = %d + 1 = %d", n8, n));
            } else if (code == 0xFF) {
                LOGGER.finest(String.format("0xFF: 32bit "));
                long n;

                if ((n = indexStream.readUI32()) < 0) {
                    LOGGER.severe(String.format("0xFF: Cannot read n."));
                    return;
                }

                offset += n;
                LOGGER.finest(String.format("LENGTH = n = %d", n));
            } else {
                LOGGER.warning(String.format("Unrecognized code: %x", code));
            }

            LOGGER.finer(String.format("-- OFFSET: %d" + tabs, offset));

            offsets.add(offset);
        }
    }
}
