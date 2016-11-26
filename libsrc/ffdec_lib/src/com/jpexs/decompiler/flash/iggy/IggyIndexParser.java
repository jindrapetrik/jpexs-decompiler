package com.jpexs.decompiler.flash.iggy;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Jindra
 */
public class IggyIndexParser {

    private static Logger LOGGER = Logger.getLogger(IggyIndexParser.class.getName());

    /*
    Offsets:
    1) name
    2) UI16 - zero
    3) tag list
    4) ... tag data offsets
    
     */
    /**
     * Parser for index data. It creates table of indices and table of offsets
     *
     * @param indexStream Stream of index
     * @param indexTableEntry Output index tabke
     * @param offsets Output list of offsets
     * @throws IOException on error
     */
    public static void parseIndex(ByteArrayDataStream indexStream, List<Integer> indexTableEntry, List<Long> offsets) throws IOException {
        int indexTableSize = indexStream.readUI8();
        int[] indexTable = new int[indexTableSize];
        for (int i = 0; i < indexTableSize; i++) {
            int offset = indexStream.readUI8();
            LOGGER.fine(String.format("index_table_entry: %02x\n", offset));
            indexTable[i] = offset;
            indexTableEntry.add(offset);
            int num = indexStream.readUI8();
            indexStream.seek(num * 2, SeekMode.CUR);
        }

        long offset = 0;
        int code;

        while ((code = indexStream.readUI8()) > -1) {
            LOGGER.finer(String.format("Code = %x\n", code));

            if (code < 0x80) // 0-0x7F
            {
                // code is directly an index to the index_table
                if (code >= indexTableSize) {
                    LOGGER.severe(String.format("< 0x80: index is greater than index_table_size. %x > %x\n", code, indexTableSize));
                    return;
                }

                offset += indexTable[code];
            } else if (code < 0xC0) // 0x80-BF
            {
                int index;

                if ((index = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("< 0xC0: Cannot read index.\n"));
                    return;
                }

                if (index >= indexTableSize) {
                    LOGGER.severe(String.format("< 0xC0: index is greater than index_table_size. %x > %x\n", index, indexTableSize));
                    return;
                }

                int n = code - 0x7F;
                offset += indexTable[index] * n;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                offset += ((code * 2) - 0x17E);
            } else if (code < 0xE0) // 0xD0-0xDF
            {
                // Code here depends on plattform[0], we are assuming it is 1, as we checked in load function
                int i = code & 0xF;
                int n8;
                int n;

                if ((n8 = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("< 0xE0: Cannot read n.\n"));
                    return;
                }

                n = n8 + 1;

                if (indexStream.is64()) {
                    if (i <= 2) {
                        offset += 8 * n; // Ptr type
                    } else if (i <= 4) {
                        offset += 2 * n;
                    } else if (i == 5) {
                        offset += 4 * n;
                    } else if (i == 6) {
                        offset += 8 * n; // 64 bits type
                    } else {
                        LOGGER.severe(String.format("< 0xE0: Invalid value for i (%x %x)\n", i, code));
                    }
                } else {
                    switch (i) {
                        case 2:
                            offset += 4 * n;  // Ptr type
                            break;
                        case 4:
                            offset += 2 * n;
                            break;
                        case 5:
                            offset += 4 * n; // 32 bits type
                            break;
                        case 6:
                            offset += 8 * n;
                            break;
                        default:
                            LOGGER.severe(String.format("< 0xE0: invalid value for i (%x %x)\n", i, code));
                    }
                }
            } else if (code == 0xFC) {
                indexStream.seek(1, SeekMode.CUR);
            } else if (code == 0xFD) {
                int n, m;

                if ((n = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read n.\n"));
                    return;
                }

                if ((m = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read m.\n"));
                    return;
                }

                offset += n;
                indexStream.seek(m * 2, SeekMode.CUR);
            } else if (code == 0xFE) {
                int n8;
                int n;

                if ((n8 = indexStream.readUI8()) < 0) {
                    LOGGER.severe(String.format("0xFE: Cannot read n.\n"));
                    return;
                }

                n = n8 + 1;
                offset += n;
            } else if (code == 0xFF) {
                long n;

                if ((n = indexStream.readUI32()) < 0) {
                    LOGGER.severe(String.format("0xFF: Cannot read n.\n"));
                    return;
                }

                offset += n;
            } else {
                LOGGER.warning(String.format("Unrecognized code: %x\n", code));
            }

            offsets.add(offset);
        }
    }
}
