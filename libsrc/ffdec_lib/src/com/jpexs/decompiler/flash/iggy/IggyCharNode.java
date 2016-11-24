package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyCharNode implements StructureInterface {

    private static Logger LOGGER = Logger.getLogger(IggyCharNode.class.getName());

    @IggyFieldType(DataType.float_t)
    float x1;
    @IggyFieldType(DataType.float_t)
    float y1; // zaporne
    @IggyFieldType(DataType.float_t)
    float x2;
    @IggyFieldType(DataType.float_t)
    float y2; // zaporne 
    @IggyFieldType(DataType.uint8_t)  //1-pocatek,2-point/line,3-curve 
    int node_type;
    @IggyFieldType(DataType.uint8_t) // 208 start smooth (for j=1 only), 61 smooth interupt (muze a nemusi byt pro novy oddeleny kus charu - kdyz je subtype predchoziho vetsi nez 0 (kupr 5) bude pro oddeleny usek 61, jinak pokud je subtype predchoziho 0 bude pro oddeleny usek 0)
    int node_subtype;
    @IggyFieldType(DataType.uint8_t)
    int zer1;
    @IggyFieldType(DataType.uint8_t)
    int zer2;
    @IggyFieldType(DataType.uint32_t)
    long isstart; //  1 v prubehu nebo 0 pouze pro prvni (i kdyz jsou delene jako dvojtecka!!!) 

    private boolean first;

    public IggyCharNode(float x1, float y1, float x2, float y2, int node_type, int node_subtype, int zer1, int zer2, long isstart) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.node_type = node_type;
        this.node_subtype = node_subtype;
        this.zer1 = zer1;
        this.zer2 = zer2;
        this.isstart = isstart;
    }

    public IggyCharNode(AbstractDataStream s, boolean first) throws IOException {
        this.first = first;
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(AbstractDataStream s) throws IOException {
        x1 = s.readFloat();
        y1 = s.readFloat();
        x2 = s.readFloat();
        y2 = s.readFloat();
        node_type = s.readUI8();
        node_subtype = s.readUI8();
        zer1 = s.readUI8();
        zer2 = s.readUI8();
        isstart = s.readUI32();

        if ((zer1 != 0) | (zer2 != 0)) {
            LOGGER.warning(String.format("Unknown zeroes at pos %08X\n", s.position() - 6));
        }
        if ((!first) & (isstart != 1)) {
            LOGGER.warning(String.format("Unknown format at pos %08X\n", s.position() - 4));
        }
        if ((first) & (isstart != 0)) {
            LOGGER.warning(String.format("Unknown format at pos %08X\n", s.position() - 4));
        }
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
