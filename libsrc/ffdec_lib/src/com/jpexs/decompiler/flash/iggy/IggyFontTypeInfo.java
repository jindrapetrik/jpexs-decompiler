package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggyFontTypeInfo implements StructureInterface {

    public static final int STRUCT_SIZE = 24;
    @IggyFieldType(DataType.uint64_t)
    long zero;
    @IggyFieldType(DataType.uint64_t)
    long ofs_local_name;
    @IggyFieldType(DataType.uint64_t)
    long font_info_num;

    private long local_name_ofs_pos;

    public long getLocal_name_ofs_pos() {
        return local_name_ofs_pos;
    }

    public IggyFontTypeInfo(ReadDataStreamInterface s) throws IOException {
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        zero = s.readUI64();
        local_name_ofs_pos = s.position();
        ofs_local_name = s.readUI64();
        font_info_num = s.readUI64();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.writeUI64(zero);
        local_name_ofs_pos = s.position();
        s.writeUI64(ofs_local_name);
        s.writeUI64(font_info_num);
    }

}
