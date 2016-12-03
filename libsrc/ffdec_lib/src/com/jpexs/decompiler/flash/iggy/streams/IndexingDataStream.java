package com.jpexs.decompiler.flash.iggy.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IndexingDataStream implements IndexingDataStreamInterface {

    public static final int CODE_FC_SKIP1 = 0xFC;
    public static final int CODE_FD_OFS8_SKIP_TWICE8 = 0xFD;
    public static final int CODE_FE_OFS8_POSITIVE = 0xFE;
    public static final int CODE_FF_OFS32 = 0xFF;

    private static Logger LOGGER = Logger.getLogger(IndexingDataStream.class.getName());

    private List<Integer> indexTable;
    private WriteDataStreamInterface indexStream;

    public IndexingDataStream() throws IOException {
        indexStream = new TemporaryDataStream();
        indexTable = new ArrayList<>();
    }

    @Override
    public byte[] getIndexTableBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(indexTable.size());
        for (int i = 0; i < indexTable.size(); i++) {
            baos.write(indexTable.get(i));
            if (i == indexTable.size() - 1) {
                int pad8 = (indexTable.size() * 2) % 8;
                baos.write(pad8 / 2); //pad to 8 bytes(?)
            } else {
                baos.write(0); //how many bytes to skip
            }
        }
        return baos.toByteArray();
    }

    public byte[] getIndexBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(getIndexTableBytes());
            baos.write(indexStream.getAllBytes());
        } catch (IOException ex) {
            //should not happen
        }
        return baos.toByteArray();
    }

    @Override
    public void writeIndexToTable(int val0to255) {
        indexTable.add(val0to255);
    }

    @Override
    public long writeIndexFromTable(int tableIndex0to127) {
        return writeIndex(tableIndex0to127, false, 0, 0);
    }

    @Override
    public long writeIndex40(int num1to40, int countUI8) {
        return writeIndex(0x80 + num1to40, false, countUI8, 0);
    }

    @Override
    public long writeIndexMultiply2(int num0to15) {
        return writeIndex(0xC0 + num0to15, false, 0, 0);
    }

    @Override
    public long writeIndexPtr(boolean is64, long cnt) {
        return writeIndex(0xD0 + 0x2, false, cnt - 1, 0);
    }

    @Override
    public long writeIndex16bit(long cnt) {
        return writeIndex(0xD0 + 0x4, false, cnt - 1, 0);
    }

    @Override
    public long writeIndex32bit(long cnt) {
        return writeIndex(0xD0 + 0x5, false, cnt - 1, 0);
    }

    @Override
    public long writeIndex64bit(long cnt) {
        return writeIndex(0xD0 + 0x6, false, cnt - 1, 0);
    }

    @Override
    public long writeIndexSkip1() {
        return writeIndex(CODE_FC_SKIP1, false, 0, 0);
    }

    @Override
    public long writeIndexUI8SkipTwice8(int ofs, int skipTwice) {
        return writeIndex(CODE_FD_OFS8_SKIP_TWICE8, false, ofs, skipTwice);
    }

    @Override
    public long writeIndexUI8Positive(int val) {
        return writeIndex(CODE_FE_OFS8_POSITIVE, false, val, 0);
    }

    @Override
    public long writeIndexUI32(long offset) {
        return writeIndex(CODE_FF_OFS32, false, offset, 0);
    }

    @Override
    public long writeIndex(int code, boolean is64, long val, long skipNum) {
        try {
            indexStream.writeUI8(code);
            if (code < 0x80) // 0-0x7F
            {
                LOGGER.finest("0-0x7F: code is directly an index to the index_table");
                // code is directly an index to the index_table
                if (code >= indexTable.size()) {
                    LOGGER.severe(String.format("< 0x80: index is greater than index_table_size. %x > %x", code, indexTable.size()));
                    return 0;
                }

                LOGGER.finest(String.format("ofset += %d", indexTable.get(code)));
                return indexTable.get(code);
            } else if (code < 0xC0) // 0x80-BF
            {
                LOGGER.finest("0x80-BF: table[0..255]*(code-0x7F)");
                int index;

                indexStream.writeUI8((int) val);
                if ((index = (int) val) < 0) {
                    LOGGER.severe(String.format("< 0xC0: Cannot read index."));
                    return 0;
                }

                if (index >= indexTable.size()) {
                    LOGGER.severe(String.format("< 0xC0: index is greater than index_table_size. %x > %x", index, indexTable.size()));
                    return 0;
                }

                int n = code - 0x7F;
                return indexTable.get(index) * n;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                LOGGER.finest("0xC0-CF: code*2-0x17E");
                return ((code * 2) - 0x17E);
            } else if (code < 0xE0) // 0xD0-0xDF
            {
                LOGGER.finest("0xD0-0xDF: platform based");

                // Code here depends on plattform[0], we are assuming it is 1, as we checked in load function
                int i = code & 0xF;
                int n8;
                int n;

                indexStream.writeUI8((int) val);
                if ((n8 = (int) val) < 0) {
                    LOGGER.severe(String.format("< 0xE0: Cannot read n."));
                    return 0;
                }

                n = n8 + 1;

                LOGGER.finest(String.format("i=%X", i));
                LOGGER.finest(String.format("n=%d", n));

                if (is64) {
                    if (i <= 2) {
                        LOGGER.finest(String.format("offset += %d", 8 * n));
                        return 8 * n; // Ptr type

                    } else if (i <= 4) {
                        LOGGER.finest(String.format("offset += %d", 2 * n));
                        return 2 * n;
                    } else if (i == 5) {
                        LOGGER.finest(String.format("offset += %d", 4 * n));
                        return 4 * n;
                    } else if (i == 6) {
                        LOGGER.finest(String.format("offset += %d", 8 * n));
                        return 8 * n; // 64 bits type
                    } else {
                        LOGGER.severe(String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                        return 0;
                    }
                } else {
                    switch (i) {
                        case 2:
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            return 4 * n;  // Ptr type;                        
                        case 4:
                            LOGGER.finest(String.format("offset += %d", 2 * n));
                            return 2 * n;
                        case 5:
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            return 4 * n; // 32 bits type
                        case 6:
                            LOGGER.finest(String.format("offset += %d", 8 * n));
                            return 8 * n;
                        default:
                            LOGGER.severe(String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                            return 0;
                    }
                }
            } else if (code == CODE_FC_SKIP1) {
                LOGGER.finest(String.format("0xFC: skip 1 "));
                //indexStream.seek(1, SeekMode.CUR);
                return 1; //seek 1
            } else if (code == CODE_FD_OFS8_SKIP_TWICE8) {
                LOGGER.finest(String.format("0xFD: 0..255, skip 2 * 0..255 "));
                int n, m;

                indexStream.writeUI8((int) val);

                if ((n = (int) val) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read n."));
                    return 0;
                }

                indexStream.writeUI8((int) skipNum);

                if ((m = (int) skipNum) < 0) {
                    LOGGER.severe(String.format("0xFD: Cannot read m."));
                    return 0;
                }

                long offset = n;
                LOGGER.finest(String.format("offset += %d", n));
                long skip = m * 2;
                LOGGER.finest(String.format("skip %d", m * 2));
                return offset + skip;
            } else if (code == CODE_FE_OFS8_POSITIVE) {
                LOGGER.finest(String.format("0xFD: 0..255 + 1 "));
                int n8;
                int n;

                indexStream.writeUI8((int) val);
                if ((n8 = (int) val) < 0) {
                    LOGGER.severe(String.format("0xFE: Cannot read n."));
                    return 0;
                }

                n = n8 + 1;
                LOGGER.finest(String.format("offset += %d", n));
                return n;
            } else if (code == CODE_FF_OFS32) {
                LOGGER.finest(String.format("0xFF: 32bit "));
                long n;

                indexStream.writeUI32(val);
                if ((n = val) < 0) {
                    LOGGER.severe(String.format("0xFF: Cannot read n."));
                    return 0;
                }

                LOGGER.finest(String.format("offset += %d", n));
                return n;
            } else {
                LOGGER.warning(String.format("Unrecognized code: %x", code));
                return 0;
            }
        } catch (IOException ex) {
            return 0;
        }
    }
}
