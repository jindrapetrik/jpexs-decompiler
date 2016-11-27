package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class IggySwf implements StructureInterface {

    final static int NO_OFFSET = 1;

    @IggyFieldType(value = DataType.widechar_t, count = 48)
    String name;

    Map<Integer, IggyFont> fonts;
    Map<Integer, IggyText> texts;
    Map<Integer, Integer> text2Font;

    private IggyFlashHeader64 header;
    private Map<Long, Long> sizesOfOffsets;
    private List<Long> allOffsets;

    public IggySwf(IggyFlashHeader64 header, ReadDataStreamInterface stream, List<Long> offsets) throws IOException {
        this.header = header;
        sizesOfOffsets = new HashMap<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            sizesOfOffsets.put(offsets.get(i), offsets.get(i + 1) - offsets.get(i));
        }
        sizesOfOffsets.put(offsets.get(offsets.size() - 1), 0L); //Last offset has 0L length?
        this.allOffsets = offsets;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        //here is offset[0]
        StringBuilder nameBuilder = new StringBuilder();
        do {
            char c = (char) stream.readUI16();
            if (c == '\0') {
                break;
            }
            nameBuilder.append(c);
        } while (true);
        name = nameBuilder.toString();
        //here is offset[1]
        int pad8 = 8 - (int) (stream.position() % 8);
        stream.seek(pad8, SeekMode.CUR);
        //here is offset [2]        
        fonts = new HashMap<>();
        int fontIndex = 0;
        for (int i = 2; i < allOffsets.size(); i++) {
            long offset = allOffsets.get(i);
            stream.seek(offset, SeekMode.SET);
            int type = stream.readUI16();
            stream.seek(-2, SeekMode.CUR);
            if (type == IggyFont.ID) {
                IggyFont font = new IggyFont(stream);
                fonts.put(fontIndex++, font);
            }
            if (type == IggyText.ID) {
                //TODO: Texts - incomplete
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //TODO!!!
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        return sb.toString();
    }

}
