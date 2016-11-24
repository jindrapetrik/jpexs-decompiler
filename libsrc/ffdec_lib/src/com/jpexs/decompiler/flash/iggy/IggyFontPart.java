package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.List;
import org.omg.CORBA.StructMemberHelper;

/**
 *
 * @author JPEXS
 */
public class IggyFontPart implements StructureInterface {

    @IggyFieldType(DataType.uint16_t)
    int type;   //stejny pro rozdilne fonty
    @IggyFieldType(DataType.uint16_t)
    int order_in_iggy_file;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 28)
    byte[] zeroone; // stejny pro rozdilne fonty
    @IggyFieldType(DataType.uint16_t)
    int char_count2;
    @IggyArrayFieldType(value = DataType.uint16_t, count = 3)
    int[] what_1;
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

    //FSeek(start_name);
    @IggyFieldType(value = DataType.widechar_t, count = 16)
    String name;

    List<IggyCharOffset> charOffsets;
    List<IggyChar> chars;
    IggyCharIndices charIndices;
    IggyCharScales charScales;
    IggyCharKerning charKernings;

    byte[] padTo4byteBoundary;

    @Override
    public void readFromDataStream(AbstractDataStream s) throws IOException {
        type = s.readUI16();
        order_in_iggy_file = s.readUI16();
        zeroone = s.readBytes(28);
        char_count2 = s.readUI16();
        what_1 = new int[3];
        for (int i = 0; i < what_1.length; i++) {
            what_1[i] = s.readUI16();
        }
        flags = s.readUI64();
        start_of_char_struct = s.position() + s.readUI64();
        start_of_char_index = s.position() + s.readUI64();
        start_of_scale = s.position() + s.readUI64();
        kern_count = s.readUI32();
        unk_float = new float[5];
        for (int i = 0; i < unk_float.length; i++) {
            unk_float[i] = s.readFloat();
        }
        start_of_kern = s.position() + s.readUI64();
        zero_padd = s.readUI64();
        what_2 = s.readUI64();
        zero_padd_2 = s.readUI64();
        start_of_name = s.position() + s.readUI64();
        one_padd = s.readUI64();
        xscale = s.readUI16();
        yscale = s.readUI16();
        zero_padd_3 = s.readUI64();
        ssr1 = s.readFloat();
        ssr2 = s.readFloat();
        char_count = s.readUI32();
        zero_padd_4 = s.readUI64();
        what_3 = s.readUI64();
        s.seek(272, SeekMode.CUR);
        sss1 = s.readFloat();
        one_padd2 = s.readUI32();
        sss2 = s.readFloat();
        one_padd3 = s.readUI32();
        sss3 = s.readFloat();
        one_padd4 = s.readUI32();
        sss4 = s.readFloat();
        one_padd5 = s.readUI32();
        s.seek(start_of_name, SeekMode.CUR);

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
        s.seek(16 - nameCharCnt * 2, SeekMode.CUR);
        name = nameBuilder.toString();
        s.seek(start_of_char_struct, SeekMode.CUR);

        for (int i = 0; i < char_count; i++) {
            charOffsets.add(new IggyCharOffset(s));
        }
        for (int i = 0; i < char_count; i++) {
            long offset = charOffsets.get(i).offset;
            if (offset > 0) {
                chars.add(new IggyChar(s, offset));
            } else {
                s.readUI8();
                chars.add(null);
            }
        }
        s.seek(start_of_char_index, SeekMode.CUR);
        charIndices = new IggyCharIndices(s, char_count);
        s.seek(start_of_scale, SeekMode.CUR);
        charScales = new IggyCharScales(s, char_count);
        s.seek(start_of_kern, SeekMode.CUR);
        charKernings = new IggyCharKerning(s, kern_count);
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
