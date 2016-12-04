package com.jpexs.decompiler.flash.iggy.streams;

import com.jpexs.decompiler.flash.iggy.IggyCharKerning;
import com.jpexs.decompiler.flash.iggy.IggyCharOffset;
import com.jpexs.decompiler.flash.iggy.IggyFontBinInfo;
import com.jpexs.decompiler.flash.iggy.IggyFontTypeInfo;
import com.jpexs.decompiler.flash.iggy.IggySequence;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggyShapeNode;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyIndexBuilder {

    private static Logger LOGGER = Logger.getLogger(IggyIndexBuilder.class.getName());

    /*static PrintWriter pw;

    static {
        try {
            pw = new PrintWriter("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_new\\index.txt");
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
    }*/
    private static final int CODE_FC_SKIP1 = 0xFC;
    private static final int CODE_FD_OFS8_SKIP_TWICE8 = 0xFD;
    private static final int CODE_FE_OFS8_POSITIVE = 0xFE;
    private static final int CODE_FF_OFS32 = 0xFF;

    private List<Integer> constTable = new ArrayList<>();
    private Map<Integer, List<Integer>> constTableOffsets = new HashMap<>();
    private Map<Integer, List<Integer>> constTableTypes = new HashMap<>();
    private WriteDataStreamInterface indexStream;

    private static final int CONST_VAL_SHAPE_NODE_SIZE = IggyShapeNode.STRUCT_SIZE;
    private static final int CONST_VAL_KERNING_RECORD_SIZE = IggyCharKerning.STRUCT_SIZE;
    private static final int CONST_VAL_CHAR_OFFSET_SIZE = IggyCharOffset.STRUCT_SIZE;
    private static final int CONST_VAL_BIN_INFO_SIZE = IggyFontBinInfo.STRUCT_SIZE;
    private static final int CONST_VAL_TYPE_INFO_SIZE = IggyFontTypeInfo.STRUCT_SIZE;
    private static final int CONST_VAL_SHAPE_SIZE = IggyShape.STRUCT_SIZE;
    private static final int CONST_VAL_GENERAL_FONT_INFO_SIZE = 112;
    private static final int CONST_VAL_GENERAL_FONT_INFO2_SIZE = 240;
    private static final int CONST_VAL_TEXT_DATA_SIZE = 104;
    private static final int CONST_VAL_SEQUENCE_SIZE = IggySequence.STRUCT_SIZE;

    public static final int CONST_SHAPE_NODE_SIZE = 0;
    public static final int CONST_KERNING_RECORD_SIZE = 1;
    public static final int CONST_CHAR_OFFSET_SIZE = 2;
    public static final int CONST_BIN_INFO_SIZE = 3;
    public static final int CONST_TYPE_INFO_SIZE = 4;
    public static final int CONST_SHAPE_SIZE = 5;
    public static final int CONST_GENERAL_FONT_INFO_SIZE = 6;
    public static final int CONST_GENERAL_FONT_INFO2_SIZE = 7;
    public static final int CONST_TEXT_DATA_SIZE = 8;
    public static final int CONST_SEQUENCE_SIZE = 9;

    /*final byte[] STATIC_HDR = new byte[]{
        (byte) 0x0A, (byte) 0x18, (byte) 0x07, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) 0x05, (byte) 0x08, (byte) 0x05, (byte) 0x0C, (byte) 0x05, (byte) 0x12, (byte) 0x04, (byte) 0x14, (byte) 0x04, (byte) 0x16,
        (byte) 0x04, (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x04, (byte) 0x02, (byte) 0x04, (byte) 0x04, (byte) 0x04, (byte) 0x20, (byte) 0x03, (byte) 0x08, (byte) 0x04, (byte) 0x10, (byte) 0x08, (byte) 0x18,
        (byte) 0x02, (byte) 0x60, (byte) 0x0D, (byte) 0x00, (byte) 0x02, (byte) 0x09, (byte) 0x03, (byte) 0x0C, (byte) 0x05, (byte) 0x10, (byte) 0x05, (byte) 0x14, (byte) 0x05, (byte) 0x18, (byte) 0x05, (byte) 0x1C,
        (byte) 0x05, (byte) 0x20, (byte) 0x05, (byte) 0x24, (byte) 0x05, (byte) 0x28, (byte) 0x04, (byte) 0x30, (byte) 0x02, (byte) 0x38, (byte) 0x04, (byte) 0x3A, (byte) 0x04, (byte) 0x18, (byte) 0x02, (byte) 0x08,
        (byte) 0x02, (byte) 0x10, (byte) 0x05, (byte) 0x40, (byte) 0x09, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) 0x05, (byte) 0x08, (byte) 0x05, (byte) 0x0C, (byte) 0x05, (byte) 0x10, (byte) 0x02, (byte) 0x18,
        (byte) 0x05, (byte) 0x20, (byte) 0x02, (byte) 0x28, (byte) 0x02, (byte) 0x30, (byte) 0x02, (byte) 0x70, (byte) 0x12, (byte) 0x01, (byte) 0x0C, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x05, (byte) 0x10,
        (byte) 0x02, (byte) 0x20, (byte) 0x04, (byte) 0x22, (byte) 0x04, (byte) 0x24, (byte) 0x04, (byte) 0x26, (byte) 0x04, (byte) 0x2B, (byte) 0x03, (byte) 0x30, (byte) 0x02, (byte) 0x38, (byte) 0x02, (byte) 0x40,
        (byte) 0x02, (byte) 0x48, (byte) 0x05, (byte) 0x4C, (byte) 0x05, (byte) 0x50, (byte) 0x05, (byte) 0x54, (byte) 0x05, (byte) 0x58, (byte) 0x05, (byte) 0x60, (byte) 0x02, (byte) 0xF0, (byte) 0x01, (byte) 0x10,
        (byte) 0x02, (byte) 0x68, (byte) 0x12, (byte) 0x01, (byte) 0x0C, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x05, (byte) 0x10, (byte) 0x02, (byte) 0x20, (byte) 0x05, (byte) 0x24, (byte) 0x05, (byte) 0x28,
        (byte) 0x05, (byte) 0x2C, (byte) 0x05, (byte) 0x30, (byte) 0x04, (byte) 0x32, (byte) 0x04, (byte) 0x38, (byte) 0x02, (byte) 0x44, (byte) 0x04, (byte) 0x46, (byte) 0x04, (byte) 0x48, (byte) 0x04, (byte) 0x4A,
        (byte) 0x04, (byte) 0x4C, (byte) 0x04, (byte) 0x4E, (byte) 0x04, (byte) 0x60, (byte) 0x02, (byte) 0x10, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x08, (byte) 0x05};
     */
    private long position = 0;

    public IggyIndexBuilder() throws IOException {
        indexStream = new TemporaryDataStream();

        addConst(CONST_VAL_SHAPE_NODE_SIZE, new int[]{0, 4, 8, 0xC, 0x12, 0x14, 0x16}, new int[]{5, 5, 5, 5, 4, 4, 4});
        addConst(CONST_VAL_KERNING_RECORD_SIZE, new int[]{0, 2, 4}, new int[]{4, 4, 4});
        addConst(CONST_VAL_CHAR_OFFSET_SIZE, new int[]{8, 0x10, 0x18}, new int[]{4, 8, 2});
        addConst(CONST_VAL_BIN_INFO_SIZE, new int[]{0, 9, 0xC, 0x10, 0x14, 0x18, 0x1C, 0x20, 0x24, 0x28, 0x30, 0x38, 0x3A}, new int[]{2, 3, 5, 5, 5, 5, 5, 5, 5, 4, 2, 4, 4});
        addConst(CONST_VAL_TYPE_INFO_SIZE, new int[]{8, 0x10}, new int[]{2, 5});
        addConst(CONST_VAL_SHAPE_SIZE, new int[]{0, 4, 8, 0xC, 0x10, 0x18, 0x20, 0x28, 0x30}, new int[]{5, 5, 5, 5, 2, 5, 2, 2, 2});
        addConst(CONST_VAL_GENERAL_FONT_INFO_SIZE, new int[]{0x1, 0x02, 0x08, 0x10, 0x20, 0x22, 0x24, 0x26, 0x2B, 0x30, 0x38, 0x40, 0x48, 0x4C, 0x50, 0x54, 0x58, 0x60}, new int[]{0xC, 4, 5, 2, 4, 4, 4, 4, 3, 2, 2, 2, 5, 5, 5, 5, 5, 2});
        addConst(CONST_VAL_GENERAL_FONT_INFO2_SIZE, new int[]{0x10}, new int[]{2});
        addConst(CONST_VAL_TEXT_DATA_SIZE, new int[]{0x01, 0x02, 0x08, 0x10, 0x20, 0x24, 0x28, 0x2C, 0x30, 0x32, 0x38, 0x44, 0x46, 0x48, 0x4A, 0x4C, 0x4E, 0x60}, new int[]{0x0C, 4, 5, 2, 5, 5, 5, 5, 4, 4, 2, 4, 4, 4, 4, 4, 4, 2});
        addConst(CONST_VAL_SEQUENCE_SIZE, new int[]{0, 8}, new int[]{2, 5});
    }

    private void addConst(int totalLength, int[] localOffsets, int[] types) {
        if (localOffsets.length != types.length) {
            throw new RuntimeException("Size of localOffsets does not match size of types on adding consts with total length " + totalLength);
        }
        constTable.add(totalLength);
        int newIndex = constTable.size() - 1;
        List<Integer> localOffsetsList = new ArrayList<>();
        for (int t : localOffsets) {
            localOffsetsList.add(t);
        }
        List<Integer> typesList = new ArrayList<>();
        for (int t : types) {
            typesList.add(t);
        }
        constTableOffsets.put(newIndex, localOffsetsList);
        constTableTypes.put(newIndex, typesList);
    }

    private byte[] getIndexTableBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(constTable.size());
        for (int i = 0; i < constTable.size(); i++) {
            baos.write(constTable.get(i));
            baos.write(constTableOffsets.get(i).size());
            for (int j = 0; j < constTableOffsets.get(i).size(); j++) {
                baos.write(constTableOffsets.get(i).get(j));
                baos.write(constTableTypes.get(i).get(j));
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

    public long writeConstLength(int constIndex) {
        return writeIndex(constIndex, false, 0);
    }

    /*private int indexOfConst(int val) {
        int index = constTable.indexOf(val);
        if (index > -1) {
            return index;
        }
        constTable.add(val);
        return constTable.size() - 1;
    }*/
    public long writeConstLengthArray(int constIndex, long cnt) {
        long ret = 0;
        long rem = cnt;
        while (true) {
            if (rem == 1) {
                return writeIndex(constIndex, false, 0);
            }
            if (rem <= 64) {
                ret += writeIndex(0x80 + (int) rem - 1, false, constIndex);
                break;
            } else {
                rem -= 64;
                ret += writeIndex(0x80 + 64 - 1, false, constIndex);
            }
        }
        return ret;
    }

    public long pad8bytes() {
        int pad8 = (int) (position % 8);
        switch (pad8) {
            case 2:
                writePaddingBytes(2); //+6
                break;
            case 4:
                writePaddingBytes(1); //+4
                break;
            case 6:
                writePaddingBytes(0); // +2
                break;

        }
        return 0;
    }

    public long writeTwoPaddingBytes() {
        return writePaddingBytes(0);
    }

    public long writePadding16bit() {
        return writePaddingBytes(0);
    }

    public long writePadding32bit() {
        return writePaddingBytes(1);
    }

    public long writePadding64bit() {
        return writePaddingBytes(2);
    }

    public long writePaddingBytes(int twoPlusHowManyTwoBytes) {
        return writeIndex(0xC0 + twoPlusHowManyTwoBytes, false, 0);
    }

    public long writePointerArray(boolean is64, long cnt) {
        return writeIndex(0xD0 + 0x2, is64, cnt - 1);
    }

    public long write64bitPointerArray(long cnt) {
        return writeIndex(0xD0 + 0x2, true, cnt - 1);
    }

    public long write32bitPointerArray(long cnt) {
        return writeIndex(0xD0 + 0x2, false, cnt - 1);
    }

    public long write16bitArray(long cnt) {
        return writeIndex(0xD0 + 0x4, false, cnt - 1);
    }

    public long write32bitArray(long cnt) {
        return writeIndex(0xD0 + 0x5, false, cnt - 1);
    }

    public long write64bitArray(long cnt) {
        return writeIndex(0xD0 + 0x6, false, cnt - 1);
    }

    public long skipOneInIndex() {
        return writeIndex(CODE_FC_SKIP1, false, 0);
    }

    public long writeLengthCustom(int totalLen, int localOffsets[], int platformTypes[]) {
        return writeIndex(CODE_FD_OFS8_SKIP_TWICE8, false, totalLen, localOffsets, platformTypes);
    }

    public long writeLengthBytePositive(int valUI8) {
        return writeIndex(CODE_FE_OFS8_POSITIVE, false, valUI8);
    }

    public long writeLengthUI32(long offset) {
        return writeIndex(CODE_FF_OFS32, false, offset);
    }

    public static int platformNumSize(boolean is64, int i) {
        if (i <= 2) {
            return is64 ? 8 : 4;
        } else if (i <= 4) {
            return 2;
        } else if (i == 5) {
            return 4;
        } else if (i == 6) {
            return 8;
        }
        throw new RuntimeException("Uknown platform num");
    }

    private long writeIndex(int code, boolean is64, long val) {
        return writeIndex(code, is64, val, null, null);
    }

    private long writeIndex(int code, boolean is64, long val, int localOffsets[], int platformTypes[]) {
        try {
            //LOGGER.finest(String.format("index offset: %d, %04X", STATIC_HDR.length + indexStream.position(), STATIC_HDR.length + indexStream.position()));
            LOGGER.finer(String.format("Code = 0x%02X", code));
            indexStream.writeUI8(code);
            if (code < 0x80) // 0-0x7F
            {
                LOGGER.finest("0-0x7F: code is directly an index to the index_table");
                // code is directly an index to the index_table
                if (code >= constTable.size()) {
                    LOGGER.severe(String.format("< 0x80: index is greater than index_table_size. %x > %x", code, constTable.size()));
                    return 0;
                }

                LOGGER.finest(String.format("LENGTH = indexTable[%d] = %d", code, constTable.get(code)));
                long ret = constTable.get(code);
                position += ret;
                return ret;
            } else if (code < 0xC0) // 0x80-BF
            {
                LOGGER.finest("0x80-BF: table[0..255]*(code-0x7F)");
                int index;

                indexStream.writeUI8((int) val);
                if ((index = (int) val) < 0) {
                    LOGGER.severe(String.format("< 0xC0: Cannot read index."));
                    return 0;
                }

                if (index >= constTable.size()) {
                    LOGGER.severe(String.format("< 0xC0: index is greater than index_table_size. %x > %x", index, constTable.size()));
                    return 0;
                }

                int n = code - 0x7F;
                LOGGER.finest(String.format("index = %d, n = code - 0x7F = %d", index, n));
                LOGGER.finest(String.format("LENGTH = indexTable[index] * n = indexTable[%d] * %d = %d", index, n, constTable.get(index) * n));
                long ret = constTable.get(index) * n;
                position += ret;
                return ret;
            } else if (code < 0xD0) // 0xC0-0xCF
            {
                LOGGER.finest("0xC0-CF: code*2-0x17E");
                long ret = ((code * 2) - 0x17E);
                position += ret;
                return ret;
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
                        position += 8 * n;
                        return 8 * n; // Ptr type
                    } else if (i <= 4) {
                        LOGGER.finest(String.format("offset += %d", 2 * n));
                        position += 2 * n;
                        return 2 * n;
                    } else if (i == 5) {
                        LOGGER.finest(String.format("offset += %d", 4 * n));
                        position += 4 * n;
                        return 4 * n;
                    } else if (i == 6) {
                        LOGGER.finest(String.format("offset += %d", 8 * n));
                        position += 8 * n;
                        return 8 * n; // 64 bits type
                    } else {
                        LOGGER.severe(String.format("< 0xE0: Invalid value for i (%x %x)", i, code));
                        return 0;
                    }
                } else {
                    switch (i) {
                        case 2:
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            position += 4 * n;
                            return 4 * n;  // Ptr type;                        
                        case 4:
                            LOGGER.finest(String.format("offset += %d", 2 * n));
                            position += 2 * n;
                            return 2 * n;
                        case 5:
                            LOGGER.finest(String.format("offset += %d", 4 * n));
                            position += 4 * n;
                            return 4 * n; // 32 bits type
                        case 6:
                            LOGGER.finest(String.format("offset += %d", 8 * n));
                            position += 8 * n;
                            return 8 * n;
                        default:
                            LOGGER.severe(String.format("< 0xE0: invalid value for i (%x %x)", i, code));
                            return 0;
                    }
                }
            } else if (code == CODE_FC_SKIP1) {
                LOGGER.finest(String.format("0xFC: skip 1 "));
                //indexStream.seek(1, SeekMode.CUR);
                indexStream.write(0); //??
                return 1; //seek 1
            } else if (code == CODE_FD_OFS8_SKIP_TWICE8) {
                LOGGER.finest(String.format("0xFD: 0..255, skip 2 * 0..255 "));
                int n, m;

                n = (int) val;
                indexStream.writeUI8((int) val);
                m = localOffsets.length;
                indexStream.writeUI8(localOffsets.length);

                long offset = n;
                position += n;
                LOGGER.finest(String.format("offset += %d", n));
                for (int i = 0; i < localOffsets.length; i++) {
                    indexStream.writeUI8(localOffsets[i]);
                    indexStream.writeUI8(platformTypes[i]);
                }
                return offset;
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
                position += n;
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
                position += n;
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
