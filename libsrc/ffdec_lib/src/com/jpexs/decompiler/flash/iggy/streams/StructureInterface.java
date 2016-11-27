package com.jpexs.decompiler.flash.iggy.streams;

import com.jpexs.decompiler.flash.iggy.streams.AbstractDataStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface StructureInterface {

    public void readFromDataStream(AbstractDataStream stream) throws IOException;

    public void writeToDataStream(AbstractDataStream stream) throws IOException;
}
