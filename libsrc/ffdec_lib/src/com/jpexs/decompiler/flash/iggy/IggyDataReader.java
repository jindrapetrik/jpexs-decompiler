package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyDataReader implements StructureInterface {

    @IggyFieldType(value = DataType.widechar_t, count = 48)
    String name;
    @IggyFieldType(value = DataType.uint64_t)
    long unk_pad;
    @IggyArrayFieldType(value = DataType.uint64_t, countField = "font_count")
    long[] font_main_offsets;
    @IggyArrayFieldType(value = DataType.uint64_t, countField = "font_count")
    long font_info_offsets[];

    private IggyFlashHeader64 header;

    public IggyDataReader(IggyFlashHeader64 header, AbstractDataStream stream) throws IOException {
        this.header = header;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {

        stream.seek(header.swf_name_offset, SeekMode.SET);

        StringBuilder nameBuilder = new StringBuilder();
        int charCnt = 0;
        do {
            char c = (char) stream.readUI16();
            charCnt++;
            if (c == '\0') {
                break;
            }
            nameBuilder.append(c);
        } while (true);
        stream.seek(48 - charCnt * 2, SeekMode.CUR);
        name = nameBuilder.toString();
        unk_pad = stream.readUI64(); //padding one
        for (int i = 0; i < header.font_count; i++) {
            font_main_offsets[i] = stream.readUI64();
        }
        for (int i = 0; i < header.font_count; i++) {
            font_info_offsets[i] = stream.readUI64();
        }
        System.out.println("pos=" + stream.position());

    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        sb.append("]");
        return sb.toString();
    }

}
