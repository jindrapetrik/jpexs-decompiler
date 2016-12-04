package com.jpexs.decompiler.flash.iggy.streams;

import com.jpexs.decompiler.flash.iggy.streams.AbstractDataStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface StructureInterface {

    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException;

    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException;
}
