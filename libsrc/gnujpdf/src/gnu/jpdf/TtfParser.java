package gnu.jpdf;

import java.awt.FontFormatException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class TtfParser {

    private int ascent;
    private int descent;
    private int leading;
    private int maxWidth;

    private float scale;
    private int numberOfHMetrics;

    private Map<Integer, Integer> cw = new TreeMap<>();
    private Map<Integer, Integer> ctg = new TreeMap<>();
    private Map<Integer, MyRect> cbbox = new HashMap<>();

    private MyRect bbox;

    private int indexToLocFormat;

    private int flags;

    private String name;

    private float italicAngle;
    private int underlinePosition;
    private int underlineThickness;

    private int numGlyphs;
    private int xHeight;
    private int capHeight;
    private int missingWidth;

    private int avgWidth;
    private int stemV;
    private int stemH;

    private int dw;

    Map<Integer, Long> indexToLoc = new HashMap<>();

    Map<String, Long> tableOffsets = new HashMap<>();
    Map<String, Long> tableLengths = new HashMap<>();

    private byte[] cidtogidmap = new byte[131072];

    public void loadFromTTF(File file) throws IOException, FontFormatException {
        RandomAccessFile input = new RandomAccessFile(file, "r");
        readTableDirectory(input);
        readHEAD(input);
        readOS2(input);
        readHHEA(input);
        readCMAP(input);
        readLOCA(input);
        readNAME(input);
        readPOST(input);
        readMAXP(input);
        readGLYF(input);
        readHMTX(input);

        if (missingWidth > 0) {
            dw = missingWidth;
        } else {
            dw = avgWidth;
        }

        for (int cid : ctg.keySet()) {
            int gid = ctg.get(cid);
            if ((gid >= 0) && (gid <= 0xFFFF) && (gid >= 0)) {
                if (gid > 0xFFFF) {
                    gid -= 0x10000;
                }
                cidtogidmap[(cid * 2)] = (byte) (gid >> 8);
                cidtogidmap[((cid * 2) + 1)] = (byte) (gid & 0xFF);
            }
        }

        input.close();
    }

    public byte[] getCidtogidmap() {
        return cidtogidmap;
    }

    public int getAvgWidth() {
        return avgWidth;
    }

    public MyRect getBbox() {
        return bbox;
    }

    public Map<Integer, MyRect> getCbbox() {
        return cbbox;
    }

    public Map<Integer, Integer> getCw() {
        return cw;
    }

    public Map<Integer, Integer> getCmap() {
        return ctg;
    }

    public int getLeading() {
        return leading;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public Map<Integer, Integer> getCtg() {
        return ctg;
    }

    public int getFlags() {
        return flags;
    }

    public String getName() {
        return name;
    }

    public float getItalicAngle() {
        return italicAngle;
    }

    public float getUnderlinePosition() {
        return underlinePosition;
    }

    public float getUnderlineThickness() {
        return underlineThickness;
    }

    public int getxHeight() {
        return xHeight;
    }

    public int getMissingWidth() {
        return missingWidth;
    }

    public int getStemV() {
        return stemV;
    }

    public int getStemH() {
        return stemH;
    }

    public int getDw() {
        return dw;
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

            tableOffsets.put(tag, offset);
            tableLengths.put(tag, length);
            //System.err.println("tag " + tag + ", length: " + length);            
        }
    }

    private void readCMAP(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("cmap"));
        int version = readUnsignedShort(input);
        int numberSubtables = readUnsignedShort(input);

        for (int i = 0; i < numberSubtables; i++) {
            int platFormId = readUnsignedShort(input);
            int platFormSpecificId = readUnsignedShort(input);
            long offset = readUnsignedLong(input);
            seek(input, tableOffsets.get("cmap") + offset);

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
                    long a = startA;
                    for (; a < tableOffsets.get("cmap") + offset + length; a += 2) {
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
                                ctg.put(k, glyph);

                            } else {
                                int glyphIndex = (idRangeOffsets.get(j) - 2 * (segCount - j)) / 2 + (k - startCodes.get(j));
                                int glyph = glyphIndices.get(glyphIndex);
                                ctg.put(k, glyph);
                            }
                        }
                    }
                    break;
            }

            break;
        }
        if (!ctg.containsKey(0)) {
            ctg.put(0, 0);
        }

    }

    private void readHMTX(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("hmtx"));
        List<Integer> cw = new ArrayList<>();
        for (int i = 0; i < numberOfHMetrics; i++) {
            int advanceWidth = readUnsignedShort(input);
            cw.add(Math.round(scale * advanceWidth));
            skip(input, 2); //skip leftSideBearing
        }
        if (numberOfHMetrics < numGlyphs) {
            for (int i = numberOfHMetrics; i < numGlyphs; i++) {
                cw.add(cw.get(numberOfHMetrics - 1));
            }
        }
        missingWidth = cw.get(0);
        for (int cid = 0; cid <= 65535; cid++) {
            if (ctg.containsKey(cid)) {
                if (ctg.get(cid) < cw.size() && ctg.get(cid) >= 0) {
                    this.cw.put(cid, cw.get(ctg.get(cid)));
                }
            }
        }
    }

    private void readHHEA(RandomAccessFile input) throws IOException {

        seek(input, tableOffsets.get("hhea"));
        skip(input, 4); // skip Table version number
        ascent = Math.round(scale * readShort(input));
        descent = Math.round(scale * readShort(input));
        leading = Math.round(scale * readShort(input));
        maxWidth = Math.round(scale * readShort(input));
        skip(input, 22);
        numberOfHMetrics = readUnsignedShort(input);

    }

    private void readHEAD(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("head"));
        skip(input, 2 * 4 + 2 * 4 + 2);
        int unitsPerEm = readUnsignedShort(input);
        scale = (float) 1000 / (float) unitsPerEm;

        skip(input, 16);
        int xMin = Math.round(scale * readShort(input));
        int yMin = Math.round(scale * readShort(input));
        int xMax = Math.round(scale * readShort(input));
        int yMax = Math.round(scale * readShort(input));
        bbox = new MyRect(xMin, yMin, xMax, yMax);

        flags = 32;
        int macStyle = readUnsignedShort(input);

        if ((macStyle & 2) == 2) {
            flags |= 64;
        }

        seek(input, tableOffsets.get("head") + 50);
        indexToLocFormat = readShort(input);
    }

    private void readLOCA(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("loca"));

        boolean shortOffset = indexToLocFormat == 0;

        if (shortOffset) {
            int totNumGlyphs = (int) (tableLengths.get("local") / 2); // numGlyphs + 1
            for (int i = 0; i < totNumGlyphs; i++) {
                indexToLoc.put(i, (long) readUnsignedShort(input) * 2);
                if (indexToLoc.containsKey(i - 1) && ((long) indexToLoc.get(i) == (long) indexToLoc.get(i - 1))) {
                    // the last glyph didn't have an outline
                    indexToLoc.remove(i - 1);
                }
            }
        } else {
            int totNumGlyphs = (int) Math.floor(tableLengths.get("loca") / 4); // numGlyphs + 1
            for (int i = 0; i < totNumGlyphs; i++) {
                indexToLoc.put(i, readUnsignedLong(input));
                if (indexToLoc.containsKey(i - 1) && ((long) indexToLoc.get(i) == (long) indexToLoc.get(i - 1))) {
                    // the last glyph didn't have an outline
                    indexToLoc.remove(i - 1);
                }
            }
        }
    }

    private void readOS2(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("OS/2"));
        skip(input, 2);
        avgWidth = Math.round(readShort(input) * scale);
        float usWeightClass = readUnsignedShort(input) * scale;
        stemV = Math.round((70 * usWeightClass) / 400);
        stemH = Math.round((30 * usWeightClass) / 400);
        skip(input, 2); //usWidthClass
        int fsType = readShort(input);
        if (fsType == 2) {
            //This Font cannot be modified, embedded or exchanged in any manner without first obtaining permission of the legal owner.            
        }
    }

    private void readNAME(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("name"));
        skip(input, 2);
        int numNameRecords = readUnsignedShort(input);
        int stringStorageOffset = readUnsignedShort(input);
        for (int i = 0; i < numNameRecords; i++) {
            skip(input, 6); //skip Platform ID, Platform-specific encoding ID, Language ID.
            int nameID = readUnsignedShort(input);
            if (nameID == 6) {
                int stringLength = readUnsignedShort(input);
                int stringOffset = readUnsignedShort(input);
                long offset = tableOffsets.get("name") + stringStorageOffset + stringOffset;
                byte data[] = new byte[stringLength];
                seek(input, offset);
                input.read(data);
                name = new String(data);
                name = name.replaceAll("[^a-zA-Z0-9_\\-]", "");
            } else {
                skip(input, 4);// skip String length, String offset
            }
        }
    }

    private void readPOST(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("post"));
        skip(input, 4);  // skip Format Type
        italicAngle = readFixed(input);
        underlinePosition = Math.round(readShort(input) * scale);
        underlineThickness = Math.round(readShort(input) * scale);
        boolean isFixedPitch = readUnsignedLong(input) == 0 ? false : true;
        if (isFixedPitch) {
            flags |= 1;
        }
    }

    private void readMAXP(RandomAccessFile input) throws IOException {
        seek(input, tableOffsets.get("maxp"));
        skip(input, 4); //skip Table version number
        numGlyphs = readUnsignedShort(input);

    }

    private void readGLYF(RandomAccessFile input) throws IOException {
        if (ctg.containsKey(120)) {
            seek(input, tableOffsets.get("glyf") + indexToLoc.get(ctg.get(120)) + 4);
            int yMin = readShort(input);
            skip(input, 2);
            int yMax = readShort(input);
            xHeight = Math.round((yMax - yMin) * scale);
        }
        if (ctg.containsKey(72)) {
            seek(input, tableOffsets.get("glyf") + indexToLoc.get(ctg.get(72)) + 4);
            int yMin = readShort(input);
            skip(input, 2);
            int yMax = readShort(input);
            capHeight = Math.round((yMax - yMin) * scale);
        }

        for (int cid = 0; cid <= 65535; cid++) {
            if (ctg.containsKey(cid)) {
                if (indexToLoc.containsKey(ctg.get(cid))) {
                    seek(input, tableOffsets.get("glyf") + indexToLoc.get(ctg.get(cid)));
                    int xMin = readShort(input);
                    int yMin = readShort(input);
                    int xMax = readShort(input);
                    int yMax = readShort(input);
                    cbbox.put(cid, new MyRect(xMin, yMin, xMax, yMax));
                }
            }
        }
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

    private float readFixed(RandomAccessFile input) throws IOException {
        int m = readShort(input);
        int f = readUnsignedShort(input);
        return Float.parseFloat("" + m + "." + f);
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
