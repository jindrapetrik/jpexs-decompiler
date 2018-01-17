/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyShape implements StructureInterface {

    public static final int STRUCT_SIZE = 64;

    private static Logger LOGGER = Logger.getLogger(IggyShape.class.getName());

    @IggyFieldType(DataType.float_t)
    float minx; //bearing X - this is the horizontal distance from the current pen position to the glyph's left bbox edge.
    @IggyFieldType(DataType.float_t)
    float miny; //bearing Y - this is the vertical distance from the baseline to the top of the glyph's bbox.
    @IggyFieldType(DataType.float_t)
    float maxx; //advanceX - bearingX
    @IggyFieldType(DataType.float_t)
    float maxy; //advanceY - bearingY
    @IggyFieldType(DataType.uint64_t)
    long unk; // stejny vetsinou - napr. 48 - JP: to by mohlo byt advance
    @IggyFieldType(DataType.uint64_t)
    long count;
    @IggyFieldType(DataType.uint64_t)
    long one; // 1
    @IggyFieldType(DataType.uint64_t)
    long one2; // 1
    @IggyFieldType(DataType.uint64_t)
    long one3; // 1
    @IggyFieldType(DataType.uint32_t)
    long one4; // 1
    @IggyFieldType(DataType.uint32_t)
    long two1; // 2

    public float getBearingX() {
        return minx;
    }

    public float getBearingY() {
        return miny;
    }

    public float getWidth() {
        return maxx - minx;
    }

    public float getHeight() {
        return maxy - miny;
    }

    List<IggyShapeNode> nodes;

    public IggyShape(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggyShape(float minx, float miny, float maxx, float maxy, List<IggyShapeNode> nodes) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.unk = 0; //??
        this.one = 1;
        this.one2 = 1;
        this.one3 = 1;
        this.one4 = 1;
        this.two1 = 2;
        this.count = nodes.size();
        this.nodes = nodes;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        minx = s.readFloat();
        miny = s.readFloat();
        maxx = s.readFloat();
        maxy = s.readFloat();
        unk = s.readUI64();
        count = s.readUI64();
        one = s.readUI64();
        one2 = s.readUI64();
        one3 = s.readUI64();
        one4 = s.readUI32();
        two1 = s.readUI32();

        if ((one != 1) || (one2 != 1) || (one3 != 1) || (one4 != 1) || (two1 != 2)) {
            LOGGER.fine(String.format("Unique header at one: %d, one2: %d, one3: %d, one4: %d, two1: %d\n", one, one2, one3, one4, two1));
        }

        nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nodes.add(new IggyShapeNode(s, i == 0));
        }

    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.getIndexing().writeConstLength(IggyIndexBuilder.CONST_SHAPE_SIZE);
        s.writeFloat(minx);
        s.writeFloat(miny);
        s.writeFloat(maxx);
        s.writeFloat(maxy);
        s.writeUI64(unk);
        s.writeUI64(count);
        s.writeUI64(one);
        s.writeUI64(one2);
        s.writeUI64(one3);
        s.writeUI32(one4);
        s.writeUI32(two1);

        s.getIndexing().writeConstLengthArray(IggyIndexBuilder.CONST_SHAPE_NODE_SIZE, nodes.size());

        for (IggyShapeNode node : nodes) {
            node.writeToDataStream(s);
        }
    }

    public float getMinx() {
        return minx;
    }

    public float getMiny() {
        return miny;
    }

    public float getMaxx() {
        return maxx;
    }

    public float getMaxy() {
        return maxy;
    }

    public long getUnk() {
        return unk;
    }

    public long getOne() {
        return one;
    }

    public long getOne2() {
        return one2;
    }

    public long getOne3() {
        return one3;
    }

    public long getOne4() {
        return one4;
    }

    public long getTwo1() {
        return two1;
    }

    public List<IggyShapeNode> getNodes() {
        return nodes;
    }

}
