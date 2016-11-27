package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.AbstractDataStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class RawIggyTag extends IggyTag {

    byte[] rawData;
    int tagType;
    private int length;

    public RawIggyTag(int tagType, AbstractDataStream stream, int length) throws IOException {
        this.length = length;
        this.tagType = tagType;
        readFromDataStream(stream);
    }

    @Override
    public int getTagType() {
        return tagType;
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        rawData = stream.readBytes(length);
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        stream.writeBytes(rawData);
    }

}
