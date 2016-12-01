package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggyFontTypeInfo implements StructureInterface {

    @IggyFieldType(DataType.uint64_t)
    long zero;
    @IggyFieldType(DataType.uint64_t)
    long ofs_local_name;
    @IggyFieldType(DataType.uint64_t)
    long font_info_num;

    private long local_name_address;

    public long getLocal_name_address() {
        return local_name_address;
    }

    public IggyFontTypeInfo(ReadDataStreamInterface s) throws IOException {
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        zero = s.readUI64();
        ofs_local_name = s.readUI64();
        local_name_address = ofs_local_name + s.position() - 8;
        font_info_num = s.readUI64();
    }

    public String readFontInfo(ReadDataStreamInterface s) throws IOException {
        long pos = s.position();
        s.seek(local_name_address, SeekMode.SET);
        StringBuilder inf_font_builder = new StringBuilder();
        while (true) {
            char c = (char) s.readUI16();
            if (c == '\0') {
                break;
            }
            inf_font_builder.append(c);
        }
        s.seek(pos, SeekMode.SET);
        return inf_font_builder.toString();
    }

    public void writeFontInfo(String info_name, WriteDataStreamInterface s) throws IOException {
        long pos = s.position();
        s.seek(local_name_address, SeekMode.SET);
        for (int i = 0; i < info_name.length(); i++) {
            s.writeUI16(info_name.charAt(i));
        }
        s.writeUI16(0);
        s.seek(pos, SeekMode.SET);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.writeUI64(zero);
        s.writeUI64(ofs_local_name);
        s.writeUI64(font_info_num);
    }

}
