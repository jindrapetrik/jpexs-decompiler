package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharAdvances implements StructureInterface {

    List<Float> advances;
    private List<IggyShape> glyphs;

    public List<Float> getScales() {
        return advances;
    }

    public IggyCharAdvances(ReadDataStreamInterface stream, List<IggyShape> glyphs) throws IOException {
        this.glyphs = glyphs;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        advances = new ArrayList<>();
        for (int i = 0; i < glyphs.size(); i++) {
            if (glyphs.get(i) != null) {
                advances.add(stream.readFloat());
            } else {
                advances.add(null);
            }
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        for (int i = 0; i < advances.size(); i++) {
            if (advances.get(i) != null) {
                stream.writeFloat(advances.get(i));
            }
        }
    }
}
