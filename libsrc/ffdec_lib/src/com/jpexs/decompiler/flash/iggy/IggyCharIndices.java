package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharIndices implements StructureInterface {

    @IggyFieldType(value = DataType.widechar_t)
    List<Character> chars;
    @IggyFieldType(DataType.uint32_t)
    long padd;

    public List<Character> getChars() {
        return chars;
    }

    private long charCount;

    public IggyCharIndices(AbstractDataStream stream, long charCount) throws IOException {
        this.charCount = charCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        chars = new ArrayList<>();
        for (int i = 0; i < charCount; i++) {
            chars.add((char) stream.readUI16());
        }
        padd = stream.readUI32();
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
