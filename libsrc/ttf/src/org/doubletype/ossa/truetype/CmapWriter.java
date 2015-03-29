/*
 * $Id: CmapWriter.java,v 1.8 2004/01/27 00:35:08 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2003, e.e d3si9n $
 * $License:
 * This source code is part of DoubleType.
 * DoubleType is a graphical typeface designer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, e.e d3si9n gives permission to
 * link the code of this program with any Java Platform that is available
 * to public with free of charge, including but not limited to
 * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
 * and distribute linked combinations including the two.
 * You must obey the GNU General Public License in all respects for all
 * of the code used other than Java Platform. If you modify this file,
 * you may extend this exception to your version of the file, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * $
 */
package org.doubletype.ossa.truetype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author e.e
 */
public class CmapWriter extends FontFormatWriter {

    final long k_basicLatinStart = 0x20;

    final long k_basicLatinEnd = 0x7e;

    final long k_tableEnd = 0xffff;

    final int k_unmappedChar = 0x0;

    private OS2Writer m_os2;

    private List<Long> m_unicodes = new ArrayList<>();

    private List<Long> m_startCodes = new ArrayList<>();

    private List<Long> m_endCodes = new ArrayList<>();

    private Map<Long, Long> m_unicode2glyph = new HashMap<>();

    private List<TTUnicodeRange> m_unicodeRanges = new ArrayList<>();

    private List<Long> m_idDeltas = new ArrayList<>();

    private List<Long> m_idRangeOffsets = new ArrayList<>();

    private boolean m_isIncludeVersion0;

    private byte[] m_version0;

    private byte[] m_version4;

    private byte[] m_version12;

    public CmapWriter(OS2Writer a_os2) {
        super();

        m_os2 = a_os2;
        m_isIncludeVersion0 = false;
    }

    private void prepare() {
        Collections.sort(m_unicodeRanges);

        TTUnicodeRange range = (TTUnicodeRange) m_unicodeRanges.get(0);
        m_os2.m_usFirstCharIndex = (int) range.getStartCode();
        m_os2.m_usLastCharIndex = (int) range.getEndCode();

        int i;
        for (i = 0; i < m_unicodeRanges.size(); i++) {
            range = m_unicodeRanges.get(i);

            m_startCodes.add(range.getStartCode());
            m_endCodes.add(range.getEndCode());
            m_idDeltas.add(0L);
            m_idRangeOffsets.add(
                    2L * (m_unicodeRanges.size() - i) + 2L
                    + 2L * (m_unicodes.size()));

            m_os2.m_usLastCharIndex = (int) range.getEndCode();
            m_os2.setUnicodeRangeFlag(range.getOsTwoFlag());

            for (long unicode = range.getStartCode();
                    unicode <= range.getEndCode(); unicode++) {
                m_unicodes.add(unicode);
            } // for unicode
        }

        m_startCodes.add(k_tableEnd);
        m_endCodes.add(k_tableEnd);
        m_idDeltas.add(1L);
        m_idRangeOffsets.add(0L);
    }

    public void write() throws IOException {
        prepare();

        if (m_isIncludeVersion0) {
            storeVersion0();
        }
        storeVersion4();

        reset();

        writeUInt16(0); // table version number
        writeUInt16(getNumOfEncoding()); // num of encodings

        if (m_isIncludeVersion0) {
            writeUInt16(TTName.k_macintosh);
            writeUInt16(TTName.k_macRomanEncode);
            writeUInt32(size() + 4 + 8);
        }

        writeUInt16(TTName.k_microsoft);
        writeUInt16(TTName.k_winUnicodeEncode);
        int version4Offset = size() + 4;
        if (m_isIncludeVersion0) {
            version4Offset += m_version0.length;
        }
        writeUInt32(version4Offset);

        if (m_isIncludeVersion0) {
            m_buffer.write(m_version0);
        }

        m_buffer.write(m_version4);
        pad();
    }

    private int getNumOfEncoding() {
        if (m_isIncludeVersion0) {
            return 2;
        }

        return 1;
    }

    public void addUnicodeRange(TTUnicodeRange a_range) {
        m_unicodeRanges.add(a_range);
    }

    public void addMapping(long a_unicode, long a_glyfIndex) {
        m_unicode2glyph.put(a_unicode, a_glyfIndex);
    }

    /**
     * Find 'glyf' index for the given unicode.
     * This method returns 0, if a_key was not found, which will be treated
     * as unmapped character.
     *
     * @param a_key Long object with unicode value.
     * @return 'glyf' index if a_key was found; 0 otherwise.
     */
    public long getGlyfIndex(Long a_key) {
        long retval = 0;

        if (m_unicode2glyph.containsKey(a_key)) {
            retval = m_unicode2glyph.get(a_key);
        }

        return retval;
    }

    private void storeVersion0() throws IOException {
        reset();
        writeVersion0();
        m_version0 = toByteArray();
        reset();
    }

    private void storeVersion4() throws IOException {
        reset();
        writeVersion4();
        m_version4 = toByteArray();
        reset();
    }

    private void storeVersion12() throws IOException {
        reset();
        writeVersion12();
        m_version12 = toByteArray();
        reset();
    }

    protected String getTag() {
        return "cmap";
    }

    private void writeVersion0() throws IOException {
        writeUInt16(0);
        writeUInt16(262);
        writeUInt16(0);
        int i;
        for (i = 0; i < 256; i++) {
            if ((i == 0x000) || (i == 0x0008) || (i == 0x001D)) {
                writeUInt8((int) getGlyfIndex(TTUnicodeRange.k_null)); // .null
            } else if ((i == 0x0009) || (i == 0x000d)) {
                writeUInt8((int) getGlyfIndex(TTUnicodeRange.k_cr)); // CR
            } else {
                writeUInt8((int) getGlyfIndex((long) i));
            }
        }
    }

    private void writeVersion4() throws IOException {
        int segCount = m_startCodes.size();
        int i;

        // endCount
        for (i = 0; i < segCount; i++) {
            Long n = (Long) m_endCodes.get(i);
            writeUInt16(n.intValue());
        }

        // reserverdPad
        writeUInt16(0);

        // startCount
        for (i = 0; i < segCount; i++) {
            Long n = m_startCodes.get(i);
            writeUInt16(n.intValue());
        }

        // idDelta
        for (i = 0; i < segCount; i++) {
            Long n = m_idDeltas.get(i);
            writeInt16(n.intValue());
        }

        // idRangeOffset
        for (i = 0; i < segCount; i++) {
            Long n = m_idRangeOffsets.get(i);
            writeInt16(n.intValue());
        }

        // glyphIdArray 2 bytes each
        for (i = 0; i < m_unicodes.size(); i++) {
            Long unicode = m_unicodes.get(i);
            writeUInt16((int) getGlyfIndex(unicode));
        }

        byte[] bytes = m_bytes.toByteArray();

        reset();

        writeUInt16(4);
        writeUInt16(bytes.length + 14);
        writeUInt16(0);
        writeUInt16(segCount * 2);

        int searchRange = getSearchRange(segCount);
        writeUInt16(searchRange);
        writeUInt16(getEntrySelector(searchRange));
        writeUInt16(getRangeShift(segCount, searchRange));
        m_buffer.write(bytes);
    }

    public void writeVersion12() throws IOException {
        ArrayList<Long> startCharCode = new ArrayList<>();
        ArrayList<Long> endCharCode = new ArrayList<>();
        ArrayList<Long> startGlyphCode = new ArrayList<>();

        // TODO: map to real one
        startCharCode.add(k_basicLatinStart);
        endCharCode.add(k_basicLatinEnd);
        startGlyphCode.add(1L);

        long length = 16 + 12 * startCharCode.size();

        writeFixed32(12.0);
        writeUInt32(length);
        writeUInt32(0);
        writeUInt32(startCharCode.size());

        int i;
        for (i = 0; i < startCharCode.size(); i++) {
            writeUInt32(startCharCode.get(i));
            writeUInt32(endCharCode.get(i));
            writeUInt32(startGlyphCode.get(i));
        }
    }

    /**
     * Used for searchRange
     *
     * @param a_value
     * @return
     */
    private int getSearchRange(int a_value) {
        int retval
                = (int) Math.pow(2, Math.floor(Math.log(a_value) / Math.log(2)));
        return 2 * retval;
    }

    private int getEntrySelector(int a_searchRange) {
        int retval
                = (int) (Math.log(a_searchRange / 2) / Math.log(2));
        return retval;
    }

    private int getRangeShift(int a_value, int a_searchRange) {
        int retval
                = 2 * a_value - a_searchRange;
        return retval;
    }
}
