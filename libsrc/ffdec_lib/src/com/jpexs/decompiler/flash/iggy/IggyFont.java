package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
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
    @IggyFieldType(DataType.uint64_t)
    long start_of_char_struct;
    @IggyFieldType(DataType.uint64_t)
    long start_of_char_index;
    @IggyFieldType(DataType.uint64_t)
    long start_of_scale;
    @IggyFieldType(DataType.uint32_t)
    long kern_count;
    @IggyArrayFieldType(value = DataType.float_t, count = 5)
    float[] unk_float;
    @IggyFieldType(DataType.uint64_t)
    long start_of_kern;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd;
    @IggyFieldType(DataType.uint64_t)
    long what_2;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd_2;
    @IggyFieldType(DataType.uint64_t)
    long start_of_name;
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
    @IggyFieldType(value = DataType.uint8_t, count = 272)
    byte[] zeroes;
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

    @IggyFieldType(value = DataType.widechar_t, count = 16)
    String name;

    List<IggyCharOffset> charOffsets;
    List<IggyShape> glyphs;
    IggyCharIndices codePoints;
    IggyCharAdvances charScales;
    IggyCharKerning charKernings;

    byte[] padTo4byteBoundary;

    public IggyFont(int type, int order_in_iggy_file, byte[] zeroone, int char_count2, int ascent, int descent, int leading, long flags, long start_of_char_struct, long start_of_char_index, long start_of_scale, long kern_count, float[] unk_float, long start_of_kern, long zero_padd, long what_2, long zero_padd_2, long start_of_name, long one_padd, int xscale, int yscale, long zero_padd_3, float ssr1, float ssr2, long char_count, long zero_padd_4, long what_3, byte[] zeroes, float sss1, long one_padd2, float sss2, long one_padd3, float sss3, long one_padd4, float sss4, long one_padd5, String name, List<IggyCharOffset> charOffsets, List<IggyShape> chars, IggyCharIndices charIndices, IggyCharAdvances charScales, IggyCharKerning charKernings, byte[] padTo4byteBoundary) {
        this.type = type;
        this.fontId = order_in_iggy_file;
        this.zeroone = zeroone;
        this.char_count2 = char_count2;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.flags = flags;
        this.start_of_char_struct = start_of_char_struct;
        this.start_of_char_index = start_of_char_index;
        this.start_of_scale = start_of_scale;
        this.kern_count = kern_count;
        this.unk_float = unk_float;
        this.start_of_kern = start_of_kern;
        this.zero_padd = zero_padd;
        this.what_2 = what_2;
        this.zero_padd_2 = zero_padd_2;
        this.start_of_name = start_of_name;
        this.one_padd = one_padd;
        this.xscale = xscale;
        this.yscale = yscale;
        this.zero_padd_3 = zero_padd_3;
        this.ssr1 = ssr1;
        this.ssr2 = ssr2;
        this.char_count = char_count;
        this.zero_padd_4 = zero_padd_4;
        this.what_3 = what_3;
        this.zeroes = zeroes;
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
        this.glyphs = chars;
        this.codePoints = charIndices;
        this.charScales = charScales;
        this.charKernings = charKernings;
        this.padTo4byteBoundary = padTo4byteBoundary;
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
        return offset == 1 ? 0 : s.position() - 8 + offset;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        long basePos = s.position();
        type = s.readUI16();
        fontId = s.readUI16();
        zeroone = s.readBytes(28);
        char_count2 = s.readUI16();
        ascent = s.readUI16();
        descent = s.readUI16();
        leading = s.readUI16();
        flags = s.readUI64();
        start_of_char_struct = s.readUI64();
        long abs_start_of_char_struct = makeAbsOffset(s, start_of_char_struct);
        start_of_char_index = s.readUI64();
        long abs_start_of_char_index = makeAbsOffset(s, start_of_char_index);
        start_of_scale = s.readUI64();
        long abs_start_of_scale = makeAbsOffset(s, start_of_scale);

        kern_count = s.readUI32();
        unk_float = new float[5];
        for (int i = 0; i < unk_float.length; i++) {
            unk_float[i] = s.readFloat();
        }
        start_of_kern = s.readUI64();
        long abs_start_of_kern = makeAbsOffset(s, start_of_kern);
        zero_padd = s.readUI64();
        what_2 = s.readUI64();
        zero_padd_2 = s.readUI64();
        start_of_name = s.readUI64();
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
        s.seek(basePos + 272, SeekMode.SET);
        sss1 = s.readFloat();
        one_padd2 = s.readUI32();
        sss2 = s.readFloat();
        one_padd3 = s.readUI32();
        sss3 = s.readFloat();
        one_padd4 = s.readUI32();
        sss4 = s.readFloat();
        one_padd5 = s.readUI32();
        if (abs_start_of_name != 0) {
            s.seek(abs_start_of_name, SeekMode.SET);
            StringBuilder nameBuilder = new StringBuilder();
            int nameCharCnt = 0;
            do {
                char c = (char) s.readUI16();
                nameCharCnt++;
                if (c == '\0') {
                    break;
                }
                nameBuilder.append(c);
            } while (true);
            s.seek(32 - nameCharCnt * 2, SeekMode.CUR);
            name = nameBuilder.toString();
        }
        s.readUI64(); //pad zero        
        if (abs_start_of_char_struct != 0) {
            s.seek(abs_start_of_char_struct, SeekMode.SET);
            charOffsets = new ArrayList<>();
            for (int i = 0; i < char_count; i++) {
                charOffsets.add(new IggyCharOffset(s));
            }
            glyphs = new ArrayList<>();
            for (int i = 0; i < char_count; i++) {
                long offset = charOffsets.get(i).offset;
                if (offset != 0) {
                    glyphs.add(new IggyShape(s, offset));
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
        s.writeUI16(type);
        s.writeUI16(fontId);
        s.writeBytes(zeroone);
        s.writeUI16(char_count2);
        s.writeUI16(ascent);
        s.writeUI16(descent);
        s.writeUI16(leading);
        s.writeUI64(flags);
        long abs_start_of_char_struct = s.position() + start_of_char_struct;
        s.writeUI64(start_of_char_struct);
        long abs_start_of_char_index = s.position() + start_of_char_index;
        s.writeUI64(start_of_char_index);
        long abs_start_of_scale = s.position() + start_of_scale;
        s.writeUI64(start_of_scale);
        s.writeUI32(kern_count);
        for (int i = 0; i < unk_float.length; i++) {
            s.writeFloat(unk_float[i]);
        }
        long abs_start_of_kern = s.position() + start_of_kern;
        s.writeUI64(start_of_kern);
        s.writeUI64(zero_padd);
        s.writeUI64(what_2);
        s.writeUI64(zero_padd_2);
        long abs_start_of_name = s.position() + start_of_name;
        s.writeUI64(start_of_name);
        s.writeUI64(one_padd);
        s.writeUI16(xscale);
        s.writeUI16(yscale);
        s.writeUI64(zero_padd_3);
        s.writeFloat(ssr1);
        s.writeFloat(ssr2);
        s.writeUI32(char_count);
        s.writeUI64(zero_padd_4);
        s.writeUI64(what_3);
        s.seek(272, SeekMode.SET);
        s.writeFloat(sss1);
        s.writeUI32(one_padd2);
        s.writeFloat(sss2);
        s.writeUI32(one_padd3);
        s.writeFloat(sss3);
        s.writeUI32(one_padd4);
        s.writeFloat(sss4);
        s.writeUI32(one_padd5);
        if (abs_start_of_name != 0) {
            s.seek(abs_start_of_name, SeekMode.SET);
            for (char c : name.toCharArray()) {
                s.writeUI16(c);
            }
            s.writeUI16(0);

            //align to 8 bytes boundary
            int len = name.length() * 2 + 2;
            int rem = 8 - (len % 8);
            for (int i = 0; i < rem; i++) {
                s.write(0);
            }
        }
        s.writeUI64(0); //pad zero
        if (abs_start_of_char_struct != 0) {
            s.seek(abs_start_of_char_struct, SeekMode.SET);
            //offsets of shapes
            for (IggyCharOffset ofs : charOffsets) {
                ofs.writeToDataStream(s);
            }
            for (int i = 0; i < glyphs.size(); i++) {
                IggyShape shp = glyphs.get(i);
                if (shp != null) {
                    s.seek(charOffsets.get(i).offset, SeekMode.SET);
                    shp.writeToDataStream(s);
                }
            }
        }
        if (abs_start_of_char_index != 0) {
            s.seek(abs_start_of_char_index, SeekMode.SET);
            for (char c : codePoints.chars) {
                s.writeUI16(c);
            }
            s.writeUI32(0);
        }
        if (abs_start_of_scale != 0) {
            s.seek(abs_start_of_scale, SeekMode.SET);
            charScales.writeToDataStream(s);
        }
        if (abs_start_of_kern != 0) {
            s.seek(abs_start_of_kern, SeekMode.SET);
            charKernings.writeToDataStream(s);
        }
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

}
