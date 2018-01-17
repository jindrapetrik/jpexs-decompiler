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
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 *
 * All relative offsets are relative from that specific field position All
 * relative offsets can get value "1" to indicate "nothing"
 */
public class IggyFlashHeader32 implements IggyFlashHeaderInterface {

    @IggyFieldType(DataType.uint32_t)
    long main_offset; // 0 Relative offset to first section (matches sizeof header)

    @IggyFieldType(DataType.uint32_t)
    long as3_section_offset; // 4  Relative offset to as3 file names table...

    @IggyFieldType(DataType.uint32_t)
    long unk_offset; // 8   relative offset to something

    @IggyFieldType(DataType.uint32_t)
    long unk_offset2; // 0xC  relative offset to something

    @IggyFieldType(DataType.uint32_t)
    long unk_offset3; //  0x10 relative offset to something

    @IggyFieldType(DataType.uint32_t)
    long unk_offset4; // 0x14 relative offset to something

    @IggyFieldType(DataType.uint32_t)
    long xmin; //0x18 in pixels

    @IggyFieldType(DataType.uint32_t)
    long ymin; //0x0C in pixels

    @IggyFieldType(DataType.uint32_t)
    long xmax; // 0x20 in pixels

    @IggyFieldType(DataType.uint32_t)
    long ymax; // 0x24 in pixels

    @IggyFieldType(DataType.uint32_t)
    long unk_28; // probably number of blocks/objects after header

    @IggyFieldType(DataType.uint32_t)
    long unk_2C;

    @IggyFieldType(DataType.uint32_t)
    long unk_30;

    @IggyFieldType(DataType.uint32_t)
    long unk_34;

    @IggyFieldType(DataType.uint32_t)
    long unk_38;

    @IggyFieldType(DataType.uint32_t)
    long unk_3C;

    @IggyFieldType(DataType.float_t)
    float frameRate;

    @IggyFieldType(DataType.uint32_t)
    long unk_44;

    @IggyFieldType(DataType.uint32_t)
    long unk_48;

    @IggyFieldType(DataType.uint32_t)
    long unk_4C;

    @IggyFieldType(DataType.uint32_t)
    long names_offset; // 0x50 relative offset to the names/import section of the file

    @IggyFieldType(DataType.uint32_t)
    long unk_offset5; // 0x54 relative offset to something

    @IggyFieldType(DataType.uint64_t)
    long unk_58; // Maybe number of imports/names pointed by names_offset

    @IggyFieldType(DataType.uint32_t)
    long last_section_offset; // 0x60 relative offset, points to the small last section of the file

    @IggyFieldType(DataType.uint32_t)
    long unk_offset6; // 0x64 relative offset to something

    @IggyFieldType(DataType.uint32_t)
    long as3_code_offset; // 0x68 relative offset to as3 code (8 bytes header + abc blob)

    @IggyFieldType(DataType.uint32_t)
    long as3_names_offset; // 0x6C relative offset to as3 file names table (or classes names or whatever)

    @IggyFieldType(DataType.uint32_t)
    long unk_70;

    @IggyFieldType(DataType.uint32_t)
    long unk_74;

    @IggyFieldType(DataType.uint32_t)
    long unk_78; // Maybe number of classes / as3 names

    @IggyFieldType(DataType.uint32_t)
    long unk_7C;

    // Offset 0x80 (outside header): there are *unk_28* relative offsets that point to flash objects.
    // The flash objects are in a format different to swf but there is probably a way to convert between them.
    // After the offsets, the bodies of objects pointed above, which apparently have a code like 0xFFXX to identify the type of object, followed by a (unique?) identifier
    // for the object.
    // A DefineEditText-like object can be easily spotted and apparently uses type code 0x06 (or 0xFF06) but as stated above,
    // it is written in a different way.
    public IggyFlashHeader32(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        main_offset = stream.readUI32();
        as3_section_offset = stream.readUI32();
        unk_offset = stream.readUI32();
        unk_offset2 = stream.readUI32();
        unk_offset3 = stream.readUI32();
        unk_offset4 = stream.readUI32();
        xmin = stream.readUI32();
        ymin = stream.readUI32();
        xmax = stream.readUI32();
        ymax = stream.readUI32();
        unk_28 = stream.readUI32();
        unk_2C = stream.readUI32();
        unk_30 = stream.readUI32();
        unk_34 = stream.readUI32();
        unk_38 = stream.readUI32();
        unk_3C = stream.readUI32();
        frameRate = stream.readFloat();
        unk_44 = stream.readUI32();
        unk_48 = stream.readUI32();
        unk_4C = stream.readUI32();
        unk_3C = stream.readUI32();
        names_offset = stream.readUI32();
        unk_offset5 = stream.readUI32();
        unk_58 = stream.readUI64();
        last_section_offset = stream.readUI32();
        unk_offset6 = stream.readUI32();
        as3_code_offset = stream.readUI32();
        as3_names_offset = stream.readUI32();
        unk_70 = stream.readUI32();
        unk_74 = stream.readUI32();
        unk_78 = stream.readUI32();
        unk_7C = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[\r\n");
        sb.append("main_offset ").append(main_offset).append("\r\n");
        sb.append("as3_section_offset ").append(as3_section_offset).append("\r\n");
        sb.append("unk_offset ").append(unk_offset).append("\r\n");
        sb.append("unk_offset2 ").append(unk_offset2).append("\r\n");
        sb.append("unk_offset3 ").append(unk_offset3).append("\r\n");
        sb.append("unk_offset4 ").append(unk_offset4).append("\r\n");
        sb.append("xmin ").append(xmin).append("\r\n");
        sb.append("ymin ").append(ymin).append("\r\n");
        sb.append("xmax ").append(xmax).append("\r\n");
        sb.append("ymax ").append(ymax).append("\r\n");
        sb.append("unk_28 ").append(unk_28).append("\r\n");
        sb.append("unk_2C ").append(unk_2C).append("\r\n");
        sb.append("unk_30 ").append(unk_30).append("\r\n");
        sb.append("unk_34 ").append(unk_34).append("\r\n");
        sb.append("unk_38 ").append(unk_38).append("\r\n");
        sb.append("unk_3C ").append(unk_3C).append("\r\n");
        sb.append("frameRate ").append(frameRate).append("\r\n");
        sb.append("unk_44 ").append(unk_44).append("\r\n");
        sb.append("unk_48 ").append(unk_48).append("\r\n");
        sb.append("unk_4C ").append(unk_4C).append("\r\n");
        sb.append("names_offset ").append(names_offset).append("\r\n");
        sb.append("unk_offset5 ").append(unk_offset5).append("\r\n");
        sb.append("unk_58 ").append(unk_58).append("\r\n");
        sb.append("last_section_offset ").append(last_section_offset).append("\r\n");
        sb.append("unk_offset6 ").append(unk_offset6).append("\r\n");
        sb.append("as3_code_offset ").append(as3_code_offset).append("\r\n");
        sb.append("as3_names_offset ").append(as3_names_offset).append("\r\n");
        sb.append("unk_70 ").append(unk_70).append("\r\n");
        sb.append("unk_74 ").append(unk_74).append("\r\n");
        sb.append("unk_78 ").append(unk_78).append("\r\n");
        sb.append("unk_7C ").append(unk_7C).append("\r\n");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public long getXMin() {
        return xmin;
    }

    @Override
    public long getYMin() {
        return ymin;
    }

    @Override
    public long getXMax() {
        return xmax;
    }

    @Override
    public long getYMax() {
        return ymax;
    }

    @Override
    public float getFrameRate() {
        return frameRate;
    }
}
