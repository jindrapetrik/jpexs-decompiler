package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.AbstractDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Jindra
 */
public class IggyIndexParser {

    private static Logger LOGGER = Logger.getLogger(IggyIndexParser.class.getName());

    static {
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                System.out.println("" + record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    /**
     * Parser for index data. It creates table of indices and table of offsets
     *
     * @param indexStream Stream of index
     * @param indexTableEntry Output index tabke
     * @param offsets Output list of offsets
     * @throws IOException on error
     */
    public static void parseIndex(boolean is64, ReadDataStreamInterface indexStream, List<Integer> indexTableEntry, List<Long> offsets) throws IOException {
        int indexTableSize = indexStream.readUI8();
        int[] indexTable = new int[indexTableSize];
        for (int i = 0; i < indexTableSize; i++) {
            int offset = indexStream.readUI8();
            LOGGER.fine(String.format("index_table_entry: %02x", offset));
            indexTable[i] = offset;
            indexTableEntry.add(offset);
            int num = indexStream.readUI8();
            indexStream.seek(num * 2, SeekMode.CUR);
        }

        long offset = 0;
        int code;

        while ((code = indexStream.readUI8()) > -1) {
            LOGGER.finer(String.format("Code = %x", code));

            if (code < 0x80) // 0-0x7F
            {
                LOGGER.finest("0-0x7F: code is directly an index to the index_table");
                // code is directly an index to the index_table
                if (code >= indexTableSize) {
                    LOGGER.severe(String.format("< 0x80: index is greater than index_table_size. %x > %x", code, indexTableSize));
                    return;
                }

                offset += indexTable[code];
                LOGGER.finest(String.format("ofset += %d", indexTable[code]));

            } else if (code < 0xC0) // 0x80-BF
            {
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
                offset += indexTable[index] * n;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                LOGGER.finest("0xC0-CF: code*2-0x17E");
                offset += ((code * 2) - 0x17E);
            } else if (code < 0xE0) // 0xD0-0xDF
            {
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
                        LOGGER.finest(String.format("offset += %d", 8 * n));
                    } else if (i <= 4) {
                        offset += 2 * n;
                        LOGGER.finest(String.format("offset += %d", 2 * n));
                    } else if (i == 5) {
                        offset += 4 * n;
                        LOGGER.finest(String.format("offset += %d", 4 * n));
                    } else if (i == 6) {
                        offset += 8 * n; // 64 bits type
                        LOGGER.finest(String.format("offset += %d", 8 * n));
                    } else {
                        LOGGER.severe(String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                    }
                } else {
                    switch (i) {
                        case 2:
                            offset += 4 * n;  // Ptr type
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            break;
                        case 4:
                            offset += 2 * n;
                            LOGGER.finest(String.format("offset += %d", 2 * n));
                            break;
                        case 5:
                            offset += 4 * n; // 32 bits type
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            break;
                        case 6:
                            offset += 8 * n;
                            LOGGER.finest(String.format("offset += %d", 8 * n));
                            break;
                        default:
                            LOGGER.severe(String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                    }
                }
            } else if (code == 0xFC) {
                LOGGER.finest(String.format("0xFC: skip 1 "));
                indexStream.seek(1, SeekMode.CUR);
            } else if (code == 0xFD) {
                LOGGER.finest(String.format("0xFD: 0..255, skip 2 * 0..255 "));
                int n, m;

                if ((n = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read n."));
                    return;
                }

                if ((m = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read m."));
                    return;
                }

                offset += n;
                LOGGER.finest(String.format("offset += %d", n));
                indexStream.seek(m * 2, SeekMode.CUR);
                LOGGER.finest(String.format("skip %d", m * 2));
            } else if (code == 0xFE) {
                LOGGER.finest(String.format("0xFD: 0..255 + 1 "));
                int n8;
                int n;

                if ((n8 = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFE: Cannot read n."));
                    return;
                }

                n = n8 + 1;
                offset += n;
                LOGGER.finest(String.format("offset += %d", n));
            } else if (code == 0xFF) {
                LOGGER.finest(String.format("0xFF: 32bit "));
                long n;

                if ((n = indexStream.readUI32()) < 0) {
                    LOGGER.severe(String.format("0xFF: Cannot read n."));
                    return;
                }

                offset += n;
                LOGGER.finest(String.format("offset += %d", n));
            } else {
                LOGGER.warning(String.format("Unrecognized code: %x", code));
            }

            LOGGER.finer(String.format("OFFSET: %d", offset));

            offsets.add(offset);
        }
    }
}
