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

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggyText implements StructureInterface {

    public static final int STRUCT_SIZE = 104;

    public static final int ID = 0xFF06;

    @IggyFieldType(DataType.uint16_t)
    int type; // Tag type
    @IggyFieldType(DataType.uint16_t)
    int textIndex;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 28)
    byte zeroone[];
    @IggyFieldType(DataType.float_t)
    float par1;
    @IggyFieldType(DataType.float_t)
    float par2;
    @IggyFieldType(DataType.float_t)
    float par3;
    @IggyFieldType(DataType.float_t)
    float par4;
    @IggyFieldType(DataType.uint16_t)
    int enum_hex;

    //Guessed
    boolean hasText;
    boolean wordWrap;
    boolean multiline;
    boolean password;
    boolean readOnly;
    boolean hasTextColor;
    boolean hasMaxLength;
    boolean hasFont;
    boolean hasFontClass;
    boolean autosize;
    boolean hasLayout;
    boolean noSelect;
    boolean border;
    boolean wasStatic;
    boolean html;
    boolean useOutlines;

    @IggyFieldType(DataType.uint16_t)
    int fontIndex;
    @IggyFieldType(DataType.uint32_t)
    long zero;
    @IggyFieldType(DataType.uint64_t)
    long one;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 32)
    byte[] some; // same for different fonts
    long ofs_name;

    @IggyArrayFieldType(value = DataType.wchar_t)
    String initialText; //till end of info file?

    public IggyText(int type, int order_in_iggy_file, byte[] zeroone, float par1, float par2, float par3, float par4, int enum_hex, int for_which_font_order_in_iggyfile, long zero, long one, byte[] some, long offset_of_name, String name) {
        this.type = type;
        this.textIndex = order_in_iggy_file;
        this.zeroone = zeroone;
        this.par1 = par1;
        this.par2 = par2;
        this.par3 = par3;
        this.par4 = par4;
        this.enum_hex = enum_hex;
        this.fontIndex = for_which_font_order_in_iggyfile;
        this.zero = zero;
        this.one = one;
        this.some = some;
        this.initialText = name;
    }

    public IggyText(ReadDataStreamInterface stream) throws IOException {
        this.readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {

        type = s.readUI16();
        //characterId - iggy Id
        textIndex = s.readUI16();
        zeroone = s.readBytes(28);

        //bounds?:
        par1 = s.readFloat();
        par2 = s.readFloat();
        par3 = s.readFloat();
        par4 = s.readFloat();

        enum_hex = s.readUI16();
        fontIndex = s.readUI16(); //fontId
        zero = s.readUI32();
        one = s.readUI64(); //01CB FF33 3333
        some = s.readBytes(32); // [6] => 40, [24] => 8
        ofs_name = s.readUI64();
        long name_address = ofs_name + s.position() - 8;
        s.seek(name_address, SeekMode.SET);
        initialText = s.readWChar();
        s.pad8bytes();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        s.getIndexing().writeConstLength(IggyIndexBuilder.CONST_TEXT_DATA_SIZE);
        s.writeUI16(type);
        s.writeUI16(textIndex);
        s.writeBytes(zeroone);
        s.writeFloat(par1);
        s.writeFloat(par2);
        s.writeFloat(par3);
        s.writeFloat(par4);
        s.writeUI16(enum_hex);
        s.writeUI16(fontIndex);
        s.writeUI32(zero);
        s.writeUI64(one);
        s.writeBytes(some);
        s.writeUI64(ofs_name);
        long name_address = ofs_name + s.position() - 8;
        s.seek(name_address, SeekMode.SET);
        s.writeWChar(initialText);
        s.pad8bytes();

        s.getIndexing().write16bitArray(initialText.length() + 1);
        s.getIndexing().pad8bytes();

    }

    public int getType() {
        return type;
    }

    public int getTextIndex() {
        return textIndex;
    }

    public byte[] getZeroone() {
        return zeroone;
    }

    public float getPar1() {
        return par1;
    }

    public float getPar2() {
        return par2;
    }

    public float getPar3() {
        return par3;
    }

    public float getPar4() {
        return par4;
    }

    public int getEnum_hex() {
        return enum_hex;
    }

    public int getFontIndex() {
        return fontIndex;
    }

    public long getZero() {
        return zero;
    }

    public long getOne() {
        return one;
    }

    public byte[] getSome() {
        return some;
    }

    public String getInitialText() {
        return initialText;
    }

    public void setInitialText(String initialText) {
        this.initialText = initialText;
    }

}
