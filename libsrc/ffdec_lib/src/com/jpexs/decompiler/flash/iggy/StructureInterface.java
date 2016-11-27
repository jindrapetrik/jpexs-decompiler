package com.jpexs.decompiler.flash.iggy;

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
