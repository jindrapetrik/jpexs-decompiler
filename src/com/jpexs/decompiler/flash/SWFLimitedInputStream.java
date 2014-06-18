/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SWFLimitedInputStream {

    private SWFInputStream sis;
    public SWF swf;
    
    public SWFLimitedInputStream(SWF swf, SWFInputStream sis, long limit) {
        this.swf = swf;
        this.sis = sis;
    }

    public int available() throws IOException {
        return sis.available();
    }

    public long readUB(int nBits) throws IOException {
        return sis.readUB(nBits);
    }

    public int readUI8() throws IOException {
        return sis.readUI8();
    }

    public int readUI16() throws IOException {
        return sis.readUI16();
    }

    public long readUI32() throws IOException {
        return sis.readUI32();
    }

    public int readSI16() throws IOException {
        return sis.readSI16();
    }

    public long readEncodedU32() throws IOException {
        return sis.readEncodedU32();
    }

    public float readFLOAT() throws IOException {
        return sis.readFLOAT();
    }

    public byte[] readBytesEx(long count) throws IOException {
        return sis.readBytesEx(count);
    }

    public byte[] readBytesZlib(long count) throws IOException {
        return sis.readBytesZlib(count);
    }

    public String readString() throws IOException {
        return sis.readString();
    }

    public MATRIX readMatrix() throws IOException {
        return sis.readMatrix();
    }

    public List<Tag> readTagList(SWF swf, Timelined timelined, int level, boolean parallel, boolean skipUnusualTags, boolean parseTags, boolean gfx) throws IOException, InterruptedException {
        return sis.readTagList(swf, timelined, level, parallel, skipUnusualTags, parseTags, gfx);
    }

    public List<BUTTONRECORD> readBUTTONRECORDList(boolean inDefineButton2) throws IOException {
        return sis.readBUTTONRECORDList(inDefineButton2);
    }

    public List<BUTTONCONDACTION> readBUTTONCONDACTIONList(SWF swf, Tag tag) throws IOException {
        return sis.readBUTTONCONDACTIONList(swf, tag);
    }

    public CLIPACTIONS readCLIPACTIONS(SWF swf, Tag tag) throws IOException {
        return sis.readCLIPACTIONS(swf, tag);
    }

    public CXFORM readCXFORM() throws IOException {
        return sis.readCXFORM();
    }

    public CXFORMWITHALPHA readCXFORMWITHALPHA() throws IOException {
        return sis.readCXFORMWITHALPHA();
    }

    public List<FILTER> readFILTERLIST() throws IOException {
        return sis.readFILTERLIST();
    }

    public LANGCODE readLANGCODE() throws IOException {
        return sis.readLANGCODE();
    }

    public MORPHFILLSTYLEARRAY readMORPHFILLSTYLEARRAY() throws IOException {
        return sis.readMORPHFILLSTYLEARRAY();
    }

    public MORPHLINESTYLEARRAY readMORPHLINESTYLEARRAY(int morphShapeNum) throws IOException {
        return sis.readMORPHLINESTYLEARRAY(morphShapeNum);
    }

    public RGB readRGB() throws IOException {
        return sis.readRGB();
    }

    public RGBA readRGBA() throws IOException {
        return sis.readRGBA();
    }

    public SHAPE readSHAPE(int shapeNum, boolean morphShape) throws IOException {
        return sis.readSHAPE(shapeNum, morphShape);
    }

    public SHAPEWITHSTYLE readSHAPEWITHSTYLE(int shapeNum, boolean morphShape) throws IOException {
        return sis.readSHAPEWITHSTYLE(shapeNum, morphShape);
    }

    public KERNINGRECORD readKERNINGRECORD(boolean fontFlagsWideCodes) throws IOException {
        return sis.readKERNINGRECORD(fontFlagsWideCodes);
    }

    public TEXTRECORD readTEXTRECORD(boolean inDefineText2, int glyphBits, int advanceBits) throws IOException {
        return sis.readTEXTRECORD(inDefineText2, glyphBits, advanceBits);
    }

    public RECT readRECT() throws IOException {
        return sis.readRECT();
    }

    public SOUNDINFO readSOUNDINFO() throws IOException {
        return sis.readSOUNDINFO();
    }

    public ZONERECORD readZONERECORD() throws IOException {
        return sis.readZONERECORD();
    }

    public long getPos() {
        return sis.getPos();
    }

    public InputStream getBaseStream() {
        return sis.getBaseStream();
    }

}
