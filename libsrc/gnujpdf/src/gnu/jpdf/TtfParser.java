package gnu.jpdf;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class TtfParser {

    private int size = -1;


    private long headOffset = -1;

    private long os2Offset = -1;
    private long hheaOffset = -1;
    private long hmtxOffset = -1;
    private long cmapOffset = -1;

    private Font font;

    private int ascent;
    private int descent;
    private int capHeight;

    private float scale;
    private int numOfHMetrics;

    private List<Integer> advanceWidths = new ArrayList<>();
    private Map<Integer, Integer> cmap = new TreeMap<>();

    private int firstChar = -1;
    private int lastChar = -1;

    public void loadFromTTF(File file, int size) throws IOException, FontFormatException {
        font = Font.createFont(Font.TRUETYPE_FONT, file);
        RandomAccessFile input = new RandomAccessFile(file, "r");
        this.size = size;
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null.");
        }
        readTableDirectory(input);
        if (headOffset == -1) {
            throw new IOException("HEAD table not found.");
        }
        readHEAD(input);
        readOS2(input);
        readHHEA(input);
        readHMTX(input);
        readCMAP(input);

        input.close();
    }

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }


    public List<Integer> getAdvanceWidths() {
        return advanceWidths;
    }

    public Map<Integer, Integer> getCmap() {
        return cmap;
    }

    private void readTableDirectory(RandomAccessFile input) throws IOException {
        skip(input, 4);
        int tableCount = readUnsignedShort(input);
        skip(input, 6);

        byte[] tagBytes = new byte[4];
        long prevOffset = 0;
        for (int i = 0; i < tableCount; i++) {
            tagBytes[0] = readByte(input);
            tagBytes[1] = readByte(input);
            tagBytes[2] = readByte(input);
            tagBytes[3] = readByte(input);
            skip(input, 4);
            long offset = readUnsignedLong(input);
            long length = readUnsignedLong(input);
            prevOffset = offset;
            String tag = new String(tagBytes, "ISO-8859-1");
            //System.err.println("tag " + tag + ", length: " + length);
            if (tag.equals("head")) {
                headOffset = offset;
            }
            if (tag.equals("OS/2")) {
                os2Offset = offset;
            }
            if (tag.equals("hhea")) {
                hheaOffset = offset;
            }
            if (tag.equals("hmtx")) {
                hmtxOffset = offset;
            }
            if (tag.equals("cmap")) {
                cmapOffset = offset;
            }
        }
    }

    private void readCMAP(RandomAccessFile input) throws IOException {
        seek(input, cmapOffset);
        int version = readUnsignedShort(input);
        int numberSubtables = readUnsignedShort(input);

        for (int i = 0; i < numberSubtables; i++) {
            int platFormId = readUnsignedShort(input);
            int platFormSpecificId = readUnsignedShort(input);
            long offset = readUnsignedLong(input);
            seek(input, cmapOffset + offset);

            int format = readUnsignedShort(input);
            switch (format) {
                case 4:
                    int length = readUnsignedShort(input);
                    int languageCode = readUnsignedShort(input);
                    int segCountX2 = readUnsignedShort(input);
                    int segCount = segCountX2 / 2;
                    int searchRange = readUnsignedShort(input);
                    int entrySelector = readUnsignedShort(input);
                    int rangeShift = readUnsignedShort(input);

                    List<Integer> endCodes = new ArrayList<>();
                    List<Integer> startCodes = new ArrayList<>();
                    List<Integer> idDeltas = new ArrayList<>();
                    List<Integer> idRangeOffsets = new ArrayList<>();


                    for (int j = 0; j < segCount; j++) {
                        int endCode = readUnsignedShort(input);
                        endCodes.add(endCode);
                    }
                    readUnsignedShort(input);//pad
                    for (int j = 0; j < segCount; j++) {
                        int startCode = readUnsignedShort(input);
                        startCodes.add(startCode);
                    }
                    for (int j = 0; j < segCount; j++) {
                        int idDelta = readShort(input);
                        idDeltas.add(idDelta);
                    }
                    for (int j = 0; j < segCount; j++) {
                        int idRangeOffset = readUnsignedShort(input);
                        idRangeOffsets.add(idRangeOffset);
                    }

                    List<Integer> glyphIndices = new ArrayList<>();
                    long startA = input.getFilePointer();
                    ;
                    long a = startA;
                    for (; a < cmapOffset + offset + length; a += 2) {
                        int glyphIndex = readUnsignedShort(input);
                        glyphIndices.add(glyphIndex);
                    }

                    for (int j = 0; j < segCount; j++) {
                        for (int k = startCodes.get(j); k <= endCodes.get(j); k++) {
                            if (k == 65535) {
                                continue;
                            }
                            if (idRangeOffsets.get(j) == 0) {
                                int glyph = (idDeltas.get(j) + k) % 65536;
                                cmap.put(k, glyph);

                            } else {
                                int glyphIndex = (idRangeOffsets.get(j) - 2 * (segCount - j)) / 2 + (k - startCodes.get(j));
                                int glyph = glyphIndices.get(glyphIndex);
                                cmap.put(k, glyph);
                            }
                        }
                    }
                    break;
            }
            
            break;
        }
        //System.err.println("format=" + format);
    }

    private void readHMTX(RandomAccessFile input) throws IOException {
        seek(input, hmtxOffset);
        for (int i = 0; i < numOfHMetrics; i++) {
            int advanceWidth = readUnsignedShort(input);
            advanceWidths.add((int) (scale * advanceWidth));
            int leftSideBearing = readUnsignedShort(input);
        }
    }

    private void readHHEA(RandomAccessFile input) throws IOException {

        seek(input, hheaOffset + 4);
        ascent = (int) (scale * readShort(input));
        descent = (int) (scale * readShort(input));
        capHeight = ascent;
        seek(input, hheaOffset + 34);
        numOfHMetrics = readUnsignedShort(input);
    }
    private void readHEAD(RandomAccessFile input) throws IOException {
        seek(input, headOffset + 2 * 4 + 2 * 4 + 2);
        int unitsPerEm = readUnsignedShort(input);
        scale = (float) size / (float) unitsPerEm;
    }

    private void readOS2(RandomAccessFile input) throws IOException {
        seek(input, os2Offset + 68);
        //ascent = readShort(input);
        //descent = readShort(input);
        seek(input, os2Offset + 88);
        //capHeight = readShort(input);
    }

    public int getAscent() {
        return ascent;
    }

    public int getDescent() {
        return descent;
    }

    public int getCapHeight() {
        return capHeight;
    }


    private int readUnsignedByte(RandomAccessFile input) throws IOException {
        int b = input.read();
        if (b == -1) {
            throw new EOFException("Unexpected end of file.");
        }
        return b;
    }

    private byte readByte(RandomAccessFile input) throws IOException {
        return (byte) readUnsignedByte(input);
    }

    private int readUnsignedShort(RandomAccessFile input) throws IOException {
        return (readUnsignedByte(input) << 8) + readUnsignedByte(input);
    }

    private short readShort(RandomAccessFile input) throws IOException {
        return (short) readUnsignedShort(input);
    }

    private long readUnsignedLong(RandomAccessFile input) throws IOException {
        long value = readUnsignedByte(input);
        value = (value << 8) + readUnsignedByte(input);
        value = (value << 8) + readUnsignedByte(input);
        value = (value << 8) + readUnsignedByte(input);
        return value;
    }

    private void skip(RandomAccessFile input, long skip) throws IOException {
        input.seek(input.getFilePointer() + skip);
    }

    private void seek(RandomAccessFile input, long position) throws IOException {
        //skip(input, position - bytePosition);
        input.seek(position);
    }
}
