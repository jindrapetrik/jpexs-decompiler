package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyShapeNode implements StructureInterface {

    private static Logger LOGGER = Logger.getLogger(IggyShapeNode.class.getName());

    public static int NODE_TYPE_MOVE = 1;
    public static int NODE_TYPE_LINE_TO = 2;
    public static int NODE_TYPE_CURVE_POINT = 3;

    @IggyFieldType(DataType.float_t)
    float targetX;
    @IggyFieldType(DataType.float_t)
    float targetY; // negative
    @IggyFieldType(DataType.float_t)
    float controlX; // for curves
    @IggyFieldType(DataType.float_t)
    float controlY; // for curves, negative 
    @IggyFieldType(DataType.uint8_t)  //1-moveto, 2-lineto , 3 - curve to
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

    public IggyShapeNode(float x1, float y1, float x2, float y2, int node_type, int node_subtype, int zer1, int zer2, long isstart) {
        this.targetX = x1;
        this.targetY = y1;
        this.controlX = x2;
        this.controlY = y2;
        this.node_type = node_type;
        this.node_subtype = node_subtype;
        this.zer1 = zer1;
        this.zer2 = zer2;
        this.isstart = isstart;
    }

    public IggyShapeNode(AbstractDataStream s, boolean first) throws IOException {
        this.first = first;
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(AbstractDataStream s) throws IOException {
        targetX = s.readFloat();
        targetY = s.readFloat();
        controlX = s.readFloat();
        controlY = s.readFloat();
        node_type = s.readUI8();
        node_subtype = s.readUI8();
        zer1 = s.readUI8();
        zer2 = s.readUI8();
        isstart = s.readUI32();

        if ((zer1 != 0) | (zer2 != 0)) {
            LOGGER.fine(String.format("Unknown zeroes at pos %08X\n", s.position() - 6));
        }
        if ((!first) & (isstart != 1)) {
            LOGGER.fine(String.format("Unknown format at pos %08X\n", s.position() - 4));
        }
        if ((first) & (isstart != 0)) {
            LOGGER.fine(String.format("Unknown format at pos %08X\n", s.position() - 4));
        }
    }

    @Override
    public void writeToDataStream(AbstractDataStream s) throws IOException {
        s.writeFloat(targetX);
        s.writeFloat(targetY);
        s.writeFloat(controlX);
        s.writeFloat(controlY);
        s.writeUI8(node_type);
        s.writeUI8(node_subtype);
        s.writeUI8(zer1);
        s.writeUI8(zer2);
        s.writeUI32(isstart);
    }

    public float getX1() {
        return targetX;
    }

    public float getY1() {
        return targetY;
    }

    public float getX2() {
        return controlX;
    }

    public float getY2() {
        return controlY;
    }

    public int getNodeType() {
        return node_type;
    }

    public int getNodeSubType() {
        return node_subtype;
    }

    public int getZer1() {
        return zer1;
    }

    public int getZer2() {
        return zer2;
    }

    public boolean isStart() {
        return isstart == 1;
    }

}
