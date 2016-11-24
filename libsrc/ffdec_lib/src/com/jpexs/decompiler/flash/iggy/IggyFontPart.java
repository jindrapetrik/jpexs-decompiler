package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;

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
    int char_count;
    @IggyFieldType(DataType.uint64_t)
    long zero_padd_4;
    @IggyFieldType(DataType.uint64_t)
    long what_3;

    //TO BE CONTINUED...

    /*
    local uint printchars=0; 
    local uint64 start_charstruct;
local uint64 start_charindex;
local uint64 start_scale;
local uint64 start_kern;
local uint64 start_name;
     */
    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
