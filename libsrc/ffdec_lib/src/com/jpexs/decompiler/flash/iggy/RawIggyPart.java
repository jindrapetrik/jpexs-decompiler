package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class RawIggyPart extends IggyTag {

    byte[] rawData;
    int tagType;
    private int length;

    public RawIggyPart(int tagType, ReadDataStreamInterface stream, int length) throws IOException {
        this.length = length;
        this.tagType = tagType;
        readFromDataStream(stream);
    }

    @Override
    public int getTagType() {
        return tagType;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        rawData = stream.readBytes(length);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeBytes(rawData);
    }

}
