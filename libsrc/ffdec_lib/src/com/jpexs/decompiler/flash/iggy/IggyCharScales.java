package com.jpexs.decompiler.flash.iggy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharScales implements StructureInterface {

    List<Float> scales;
    private long charCount;

    public IggyCharScales(AbstractDataStream stream, long charCount) throws IOException {
        this.charCount = charCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        scales = new ArrayList<>();
        for (int i = 0; i < charCount; i++) {
            scales.add(stream.readFloat());
        }
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
