/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Shape node.
 *
 * @author JPEXS
 */
public class IggyShapeNode implements StructureInterface {

    public static final int STRUCT_SIZE = 24;

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
    @IggyFieldType(DataType.uint8_t) // 208 start smooth (for j=1 only), 61 smooth interrupt (muze a nemusi byt pro novy oddeleny kus charu - kdyz je subtype predchoziho vetsi nez 0 (kupr 5) bude pro oddeleny usek 61, jinak pokud je subtype predchoziho 0 bude pro oddeleny usek 0)
    int node_subtype;
    @IggyFieldType(DataType.uint8_t)
    int zer1;
    @IggyFieldType(DataType.uint8_t)
    int zer2;
    @IggyFieldType(DataType.uint32_t)
    long isstart; //  1 v prubehu nebo 0 pouze pro prvni (i kdyz jsou delene jako dvojtecka!!!) 

    private boolean first;

    public IggyShapeNode(float targetX, float targetY, float controlX, float controlY, int node_type, int node_subtype, boolean first) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.controlX = controlX;
        this.controlY = controlY;
        this.node_type = node_type;
        this.node_subtype = node_subtype;
        this.zer1 = 0;
        this.zer2 = 0;
        this.first = first;
        this.isstart = first ? 0 : 1;
    }

    public IggyShapeNode(ReadDataStreamInterface s, boolean first) throws IOException {
        this.first = first;
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
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
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
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

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getControlX() {
        return controlX;
    }

    public float getControlY() {
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
