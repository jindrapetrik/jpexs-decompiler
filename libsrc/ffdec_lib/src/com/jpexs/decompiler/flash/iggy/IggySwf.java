package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import java.io.IOException;
import java.util.ArrayList;
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
    private List<Long> offsets;
    private List<IggyTag> tags = new ArrayList<>();

    public List<IggyTag> getTags() {
        return tags;
    }

    public IggySwf(IggyFlashHeader64 header, ReadDataStreamInterface stream, List<Long> offsets) throws IOException {
        this.header = header;
        this.offsets = offsets;
        calcSizesFromOffsets();
        readFromDataStream(stream);
    }

    private void calcSizesFromOffsets() {
        sizesOfOffsets = new HashMap<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            sizesOfOffsets.put(offsets.get(i), offsets.get(i + 1) - offsets.get(i));
        }
        sizesOfOffsets.put(offsets.get(offsets.size() - 1), 0L); //Last offset has 0L length?
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
        for (int ofs = 2; ofs < offsets.size(); ofs++) {
            long offset = offsets.get(ofs);
            if (offset < stream.position()) {
                continue;
            }
            stream.seek(offset, SeekMode.SET);
            int type = stream.readUI16();
            stream.seek(-2, SeekMode.CUR);
            if (type == IggyFont.ID) {
                IggyFont font = new IggyFont(stream);
                fonts.put(fontIndex++, font);
                tags.add(font);
            } else if (ofs < offsets.size() - 1) {
                int len = (int) (offsets.get(ofs + 1) - offsets.get(ofs));
                RawIggyPart rtag = new RawIggyPart(type, stream, len);
                tags.add(rtag);
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        List<Long> newOffsets = new ArrayList<>();

        try {
            newOffsets.add(stream.position());
            for (int i = 0; i < name.length(); i++) {
                stream.writeUI16(name.charAt(i));
            }
            stream.writeUI16(0);
            newOffsets.add(stream.position());
            long pad8 = 8 - (stream.position() % 8);
            for (int i = 0; i < pad8; i++) {
                stream.write(0);
            }
            newOffsets.add(stream.position());
            for (IggyTag tag : tags) {
                tag.writeToDataStream(stream);
                newOffsets.add(stream.position());
            }
        } catch (IOException ex) {
            //ignore
        }
        this.offsets = newOffsets;
        calcSizesFromOffsets();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        return sb.toString();
    }

}
