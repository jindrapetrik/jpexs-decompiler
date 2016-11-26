package com.jpexs.decompiler.flash.iggy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharAdvances implements StructureInterface {

    List<Float> advances;
    private long charCount;

    public List<Float> getScales() {
        return advances;
    }

    public IggyCharAdvances(AbstractDataStream stream, long charCount) throws IOException {
        this.charCount = charCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        advances = new ArrayList<>();
        for (int i = 0; i < charCount; i++) {
            advances.add(stream.readFloat());
        }
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
