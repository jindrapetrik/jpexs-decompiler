/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyFont extends IggyTag {

    public static final int ID = 0xFF16;

    @IggyFieldType(DataType.uint16_t)
    int type;   //stejny pro rozdilne fonty
    @IggyFieldType(DataType.uint16_t)
    int fontId;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 28)
    byte[] zeroone; // stejny pro rozdilne fonty
    @IggyFieldType(DataType.uint16_t)
    int char_count2;
    @IggyFieldType(value = DataType.uint16_t)
    int ascent;
    @IggyFieldType(value = DataType.uint16_t)
    int descent;
    @IggyFieldType(value = DataType.uint16_t)
    int leading;
    @IggyFieldType(DataType.uint64_t)
    long flags;
    //@IggyFieldType(DataType.uint64_t)
    //long start_of_char_struct;
    //@IggyFieldType(DataType.uint64_t)
    //long start_of_char_index;
    //@IggyFieldType(DataType.uint64_t)
    //long start_of_scale;
    @IggyFieldType(DataType.uint32_t)
    long kern_count;
    @IggyArrayFieldType(value = DataType.float_t, count = 5)
    float[] unk_float;
    //@IggyFieldType(DataType.uint64_t)
    //long start_of_kern;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd;
    @IggyFieldType(DataType.uint64_t)
    long what_2;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd_2;
    //@IggyFieldType(DataType.uint64_t)
    //long start_of_name;
    @IggyFieldType(DataType.uint64_t)
    long one_padd;
    @IggyFieldType(DataType.uint16_t)
    int xscale;
    @IggyFieldType(DataType.uint16_t)
    int yscale;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd_3;
    @IggyFieldType(DataType.float_t)
    float ssr1;
    @IggyFieldType(DataType.float_t)
    float ssr2;
    @IggyFieldType(DataType.uint32_t)
    long char_count;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd_4;
    @IggyFieldType(DataType.uint64_t)
    long what_3;
    @IggyFieldType(value = DataType.wchar_t, count = 40)
    String subName = "";
    @IggyFieldType(value = DataType.uint8_t, count = 48)
    byte[] zeroes48a;

    @IggyFieldType(value = DataType.uint8_t, count = 48)
    byte[] zeroes48b;

    @IggyFieldType(DataType.float_t)
    float sss1;
    @IggyFieldType(DataType.uint32_t)
    long one_padd2;
    @IggyFieldType(DataType.float_t)
    float sss2;
    @IggyFieldType(DataType.uint32_t)
    long one_padd3;
    @IggyFieldType(DataType.float_t)
    float sss3;
    @IggyFieldType(DataType.uint32_t)
    long one_padd4;
    @IggyFieldType(DataType.float_t)
    float sss4;
    @IggyFieldType(DataType.uint32_t)
    long one_padd5;

    @IggyFieldType(value = DataType.wchar_t, count = 16)
    String name;

    List<IggyCharOffset> charOffsets;
    List<IggyShape> glyphs;
    IggyCharIndices codePoints;
    IggyCharAdvances charScales;
    IggyCharKerning charKernings;

    public IggyFont(int type, int fontId, byte[] zeroone, int char_count2, int ascent, int descent, int leading, long flags, long kern_count, float[] unk_float, long zero_padd, long what_2, long zero_padd_2, long one_padd, int xscale, int yscale, long zero_padd_3, float ssr1, float ssr2, long char_count, long zero_padd_4, long what_3, byte[] zeroes48a, byte[] zeroes48b, float sss1, long one_padd2, float sss2, long one_padd3, float sss3, long one_padd4, float sss4, long one_padd5, String name, List<IggyCharOffset> charOffsets, List<IggyShape> glyphs, IggyCharIndices codePoints, IggyCharAdvances charScales, IggyCharKerning charKernings) {
        this.type = type;
        this.fontId = fontId;
        this.zeroone = zeroone;
        this.char_count2 = char_count2;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.flags = flags;
        this.kern_count = kern_count;
        this.unk_float = unk_float;
        this.zero_padd = zero_padd;
        this.what_2 = what_2;
        this.zero_padd_2 = zero_padd_2;
        this.one_padd = one_padd;
        this.xscale = xscale;
        this.yscale = yscale;
        this.zero_padd_3 = zero_padd_3;
        this.ssr1 = ssr1;
        this.ssr2 = ssr2;
        this.char_count = char_count;
        this.zero_padd_4 = zero_padd_4;
        this.what_3 = what_3;
        this.zeroes48a = zeroes48a;
        this.zeroes48b = zeroes48b;
        this.sss1 = sss1;
        this.one_padd2 = one_padd2;
        this.sss2 = sss2;
        this.one_padd3 = one_padd3;
        this.sss3 = sss3;
        this.one_padd4 = one_padd4;
        this.sss4 = sss4;
        this.one_padd5 = one_padd5;
        this.name = name;
        this.charOffsets = charOffsets;
        this.glyphs = glyphs;
        this.codePoints = codePoints;
        this.charScales = charScales;
        this.charKernings = charKernings;
    }

    public IggyFont(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    private long readAbsoluteOffset(ReadDataStreamInterface stream) throws IOException {
        long offset = stream.readUI64();
        if (offset == 1) {
            return 0;
        }
        return stream.position() - 8 + offset;
    }

    private void writeAbsoluteOffset(WriteDataStreamInterface stream, long offset) throws IOException {
        if (offset == 0) {
            stream.writeUI64(1);
        } else {
            stream.writeUI64(offset - stream.position());
        }
    }

    private void writeRelativeOffset(WriteDataStreamInterface stream, long offset) throws IOException {
        if (offset == 0) {
            stream.writeUI64(1);
        } else {
            stream.writeUI64(offset);
        }
    }

    private long makeAbsOffset(ReadDataStreamInterface s, long offset) {
        return offset == 1 ? 0 : offset + s.position() - 8;
    }

    private long makeAbsOffset(WriteDataStreamInterface s, long offset) {
        return offset == 1 ? 0 : offset + s.position() - 8;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        type = s.readUI16();
        fontId = s.readUI16();
        zeroone = s.readBytes(28);
        char_count2 = s.readUI16();
        ascent = s.readUI16();
        descent = s.readUI16();
        leading = s.readUI16();
        flags = s.readUI64();
        long start_of_char_struct = s.readUI64();
        long abs_start_of_char_struct = makeAbsOffset(s, start_of_char_struct);
        long start_of_char_index = s.readUI64();
        long abs_start_of_char_index = makeAbsOffset(s, start_of_char_index);
        long start_of_scale = s.readUI64();
        long abs_start_of_scale = makeAbsOffset(s, start_of_scale);

        kern_count = s.readUI32();
        unk_float = new float[5];
        for (int i = 0; i < unk_float.length; i++) {
            unk_float[i] = s.readFloat();
        }
        long start_of_kern = s.readUI64();
        long abs_start_of_kern = makeAbsOffset(s, start_of_kern);
        zero_padd = s.readUI64();

        //--------------------------------------
        what_2 = s.readUI64();
        zero_padd_2 = s.readUI64();
        long start_of_name = s.readUI64();
        long abs_start_of_name = makeAbsOffset(s, start_of_name);
        one_padd = s.readUI64();
        xscale = s.readUI16();
        yscale = s.readUI16();
        zero_padd_3 = s.readUI64();
        ssr1 = s.readFloat();
        ssr2 = s.readFloat();
        char_count = s.readUI32();
        zero_padd_4 = s.readUI64();
        what_3 = s.readUI64();
        StringBuilder subNameBuilder = new StringBuilder();

        boolean snFinish = false;
        for (int i = 0; i < 20; i++) {
            char c = (char) s.readUI16();
            if (c == '\0') {
                snFinish = true;
            }
            if (!snFinish) {
                subNameBuilder.append(c);
            }
        }
        subName = subNameBuilder.toString();
        zeroes48a = s.readBytes(48);

        sss1 = s.readFloat();
        one_padd2 = s.readUI32();
        sss2 = s.readFloat();
        one_padd3 = s.readUI32();
        sss3 = s.readFloat();
        one_padd4 = s.readUI32();
        sss4 = s.readFloat();
        one_padd5 = s.readUI32();
        zeroes48b = s.readBytes(48);

        if (abs_start_of_name != 0) {
            //here is offset [5]  - 1096       
            s.seek(abs_start_of_name, SeekMode.SET);
            name = s.readWChar();
            //here is offset [6]  - 1130
            s.pad8bytes();
        }
        if (abs_start_of_char_struct != 0) {
            //here is offset [7]  - 1136
            s.seek(abs_start_of_char_struct, SeekMode.SET);
            charOffsets = new ArrayList<>();
            List<Long> charAddresses = new ArrayList<>();
            for (int i = 0; i < char_count; i++) {
                IggyCharOffset iggyOffset = new IggyCharOffset(s);
                charOffsets.add(iggyOffset);
                if (iggyOffset.offset == 1) {
                    charAddresses.add(0L);
                } else {
                    charAddresses.add(iggyOffset.offset + s.position() - 8);
                }
            }
            glyphs = new ArrayList<>();
            for (int i = 0; i < char_count; i++) {
                long addr = charAddresses.get(i);
                if (addr != 0) {
                    s.seek(addr, SeekMode.SET);
                    glyphs.add(new IggyShape(s));
                } else {
                    glyphs.add(null);
                }
            }
        }
        if (abs_start_of_char_index != 0) {
            s.seek(abs_start_of_char_index, SeekMode.SET);
            codePoints = new IggyCharIndices(s, char_count);
        }
        if (abs_start_of_scale != 0) {
            s.seek(abs_start_of_scale, SeekMode.SET);
            charScales = new IggyCharAdvances(s, char_count);
        }
        if (abs_start_of_kern != 0) {
            s.seek(abs_start_of_kern, SeekMode.SET);
            charKernings = new IggyCharKerning(s, kern_count);
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        final int FILL_LATER_IF_AVAILABLE = 1;
        IggyIndexBuilder ib = s.getIndexing();
        ib.writeConstLength(IggyIndexBuilder.CONST_GENERAL_FONT_INFO_SIZE);
        s.writeUI16(type);
        s.writeUI16(fontId);
        s.writeBytes(zeroone);
        s.writeUI16(char_count2);
        s.writeUI16(ascent);
        s.writeUI16(descent);
        s.writeUI16(leading);
        s.writeUI64(flags);
        long start_of_char_struct_ofs_pos = s.position();
        s.writeUI64(FILL_LATER_IF_AVAILABLE);
        long start_of_char_index_ofs_pos = s.position();
        s.writeUI64(FILL_LATER_IF_AVAILABLE);
        long start_of_scale_ofs_pos = s.position();
        s.writeUI64(FILL_LATER_IF_AVAILABLE);
        s.writeUI32(kern_count);
        for (int i = 0; i < unk_float.length; i++) {
            s.writeFloat(unk_float[i]);
        }
        long start_of_kern_ofs_pos = s.position();
        s.writeUI64(FILL_LATER_IF_AVAILABLE);
        s.writeUI64(zero_padd);
        ib.writeConstLength(IggyIndexBuilder.CONST_GENERAL_FONT_INFO2_SIZE);
        s.writeUI64(what_2);
        s.writeUI64(zero_padd_2);
        long start_of_name_ofs_pos = s.position();
        s.writeUI64(FILL_LATER_IF_AVAILABLE);
        s.writeUI64(one_padd);
        s.writeUI16(xscale);
        s.writeUI16(yscale);
        s.writeUI64(zero_padd_3);
        s.writeFloat(ssr1);
        s.writeFloat(ssr2);
        s.writeUI32(char_count);
        s.writeUI64(zero_padd_4);
        s.writeUI64(what_3);
        for (int i = 0; i < 20; i++) {
            if (i < subName.length()) {
                s.writeUI16((char) subName.charAt(i));
            } else {
                s.writeUI16(0);
            }
        }
        s.writeBytes(zeroes48a);
        s.writeFloat(sss1);
        s.writeUI32(one_padd2);
        s.writeFloat(sss2);
        s.writeUI32(one_padd3);
        s.writeFloat(sss3);
        s.writeUI32(one_padd4);
        s.writeFloat(sss4);
        s.writeUI32(one_padd5);
        s.writeBytes(zeroes48b);
        if (name != null) {
            s.setOlderOffsetToThisPos(start_of_name_ofs_pos);
            for (char c : name.toCharArray()) {
                s.writeUI16(c);
            }
            s.writeUI16(0);

            //align to 8 bytes boundary
            int len = name.length() * 2 + 2;

            ib.write16bitArray(name.length() + 1);
            ib.pad8bytes();
            int pad8 = 8 - (len % 8);
            if (pad8 < 8) {
                for (int i = 0; i < pad8; i++) {
                    s.write(0);
                }
            }
        }

        //s.writeUI64(0); //pad zero
        if (charOffsets != null) {
            s.setOlderOffsetToThisPos(start_of_char_struct_ofs_pos);

            ib.writeConstLengthArray(IggyIndexBuilder.CONST_CHAR_OFFSET_SIZE, charOffsets.size());

            List<Long> toFixOffsets = new ArrayList<>();
            //offsets of shapes
            for (IggyCharOffset ofs : charOffsets) {
                ofs.offset = FILL_LATER_IF_AVAILABLE;
                ofs.writeToDataStream(s);
                toFixOffsets.add(s.position() - 8);
            }
            for (int i = 0; i < glyphs.size(); i++) {
                IggyShape shp = glyphs.get(i);
                if (shp != null) {
                    s.setOlderOffsetToThisPos(toFixOffsets.get(i));
                    shp.writeToDataStream(s);
                }
            }
        }
        if (codePoints != null) {
            s.setOlderOffsetToThisPos(start_of_char_index_ofs_pos);
            for (char c : codePoints.chars) {
                s.writeUI16(c);
            }
            ib.write16bitArray(codePoints.chars.size());
            ib.pad8bytes();
            s.pad8bytes();
        }
        if (charScales != null) {
            s.setOlderOffsetToThisPos(start_of_scale_ofs_pos);
            charScales.writeToDataStream(s);
            ib.write32bitArray(charScales.advances.size());
            ib.pad8bytes();
            s.pad8bytes();
        }
        if (charKernings != null) {
            s.setOlderOffsetToThisPos(start_of_kern_ofs_pos);
            ib.writeConstLengthArray(IggyIndexBuilder.CONST_KERNING_RECORD_SIZE, kern_count);
            charKernings.writeToDataStream(s);
            ib.pad8bytes();
            s.pad8bytes();
        }

        /*if ((s.position() - abs_start_of_char_index) % 8 != 0) {
            byte padd[] = new byte[(int) (((s.position() - abs_start_of_char_index) / 8 + 1) * 8 - (s.position() - abs_start_of_char_index))];
            s.writeBytes(padd);
        }
        ib.writePaddingTo8(s.position());*/
    }

    public int getType() {
        return type;
    }

    public long getFlags() {
        return flags;
    }

    public int getXscale() {
        return xscale;
    }

    public int getYscale() {
        return yscale;
    }

    public long getCharacterCount() {
        return char_count;
    }

    public String getName() {
        return name;
    }

    public List<IggyShape> getChars() {
        return glyphs;
    }

    public IggyCharIndices getCharIndices() {
        return codePoints;
    }

    public IggyCharAdvances getCharAdvances() {
        return charScales;
    }

    public IggyCharKerning getCharKernings() {
        return charKernings;
    }

    public float[] getUnk_float() {
        return unk_float;
    }

    public int getAscent() {
        return ascent;
    }

    public int getDescent() {
        return descent;
    }

    public int getLeading() {
        return leading;
    }

    public long getWhat_2() {
        return what_2;
    }

    public long getWhat_3() {
        return what_3;
    }

    public List<IggyCharOffset> getCharOffsets() {
        return charOffsets;
    }

    @Override
    public int getTagType() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("IggyFontTag (%04X)", ID);
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setFontId(int fontId) {
        this.fontId = fontId;
    }

    public void setCharCount2(int char_count2) {
        this.char_count2 = char_count2;
    }

    public void setAscent(int ascent) {
        this.ascent = ascent;
    }

    public void setDescent(int descent) {
        this.descent = descent;
    }

    public void setLeading(int leading) {
        this.leading = leading;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public void setUnkFloat(float[] unk_float) {
        this.unk_float = unk_float;
    }

    public void setWhat2(long what_2) {
        this.what_2 = what_2;
    }

    public void setXScale(int xscale) {
        this.xscale = xscale;
    }

    public void setYScale(int yscale) {
        this.yscale = yscale;
    }

    public void setSsr1(float ssr1) {
        this.ssr1 = ssr1;
    }

    public void setSsr2(float ssr2) {
        this.ssr2 = ssr2;
    }

    public void setCharCount(long char_count) {
        this.char_count = char_count;
    }

    public void setWhat3(long what_3) {
        this.what_3 = what_3;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public void setSss1(float sss1) {
        this.sss1 = sss1;
    }

    public void setSss2(float sss2) {
        this.sss2 = sss2;
    }

    public void setSss3(float sss3) {
        this.sss3 = sss3;
    }

    public void setSss4(float sss4) {
        this.sss4 = sss4;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCharOffsets(List<IggyCharOffset> charOffsets) {
        this.charOffsets = charOffsets;
    }

    public void setGlyphs(List<IggyShape> glyphs) {
        this.glyphs = glyphs;
    }

    public void setCodePoints(IggyCharIndices codePoints) {
        this.codePoints = codePoints;
    }

    public void setCharScales(IggyCharAdvances charScales) {
        this.charScales = charScales;
    }

    public void setCharKernings(IggyCharKerning charKernings) {
        this.charKernings = charKernings;
    }

}
