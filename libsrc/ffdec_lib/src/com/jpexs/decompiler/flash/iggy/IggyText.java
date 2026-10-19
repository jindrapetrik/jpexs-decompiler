/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Text.
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
    // Common character header (also present on shapes, sprites and fonts).
    // x64 offsets are relative to the beginning of the complete text object.
    @IggyFieldType(DataType.uint32_t)
    long commonHeaderAlignmentPadding; // +0x04; absent on x86
    @IggyFieldType(DataType.uint32_t)
    long unknownHeaderWord; // +0x08; x86 +0x04; not necessarily zero
    @IggyFieldType(DataType.uint32_t)
    long unknownHeaderUpperWord; // +0x0C; role unconfirmed (possibly padding); absent on x86
    @IggyFieldType(DataType.uint64_t)
    long symbolClassNameOffset; // +0x10 ref64; x86 +0x08 ref32
    @IggyFieldType(DataType.uint64_t)
    long runtimeClassPointer; // +0x18; x86 +0x0C; raw AS3 runtime pointer, not ref
    private boolean is64 = true;
    private String symbolClassName;
    private String fontClassName;
    private boolean symbolClassNameChanged;
    private boolean fontClassNameChanged;
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

    // Behavior bits retained by the DLL from DefineEditText.
    public static final int FLAG_WORD_WRAP = 0x001;
    public static final int FLAG_MULTILINE = 0x002;
    public static final int FLAG_PASSWORD = 0x004;
    public static final int FLAG_READ_ONLY = 0x008;
    public static final int FLAG_AUTO_SIZE = 0x010;
    public static final int FLAG_NO_SELECT = 0x020;
    public static final int FLAG_BORDER = 0x040;
    public static final int FLAG_WAS_STATIC = 0x080;
    public static final int FLAG_HTML = 0x100;
    public static final int FLAG_USE_OUTLINES = 0x200;
    public static final int SWF_BEHAVIOR_MASK = 0x03ff;

    @IggyFieldType(DataType.uint16_t)
    int fontIndex;
    @IggyFieldType(DataType.uint32_t)
    long fontClassNameAlignmentPadding; // +0x34; absent on x86
    @IggyFieldType(DataType.uint64_t)
    long fontClassNameOffset; // +0x38 ref64; x86 +0x24 ref32; 1 means null
    @IggyArrayFieldType(value = DataType.uint8_t, count = 4)
    byte[] textColorRgba; // +0x40; x86 +0x28; R,G,B,A byte order
    @IggyFieldType(DataType.uint16_t)
    int fontHeight; // +0x44; x86 +0x2C; twips
    @IggyFieldType(DataType.uint16_t)
    int maxLength; // +0x46; x86 +0x2E; zero means no limit
    @IggyFieldType(DataType.uint16_t)
    int leftMargin; // +0x48; x86 +0x30; twips
    @IggyFieldType(DataType.uint16_t)
    int rightMargin; // +0x4A; x86 +0x32; twips
    @IggyFieldType(DataType.uint16_t)
    int indent; // +0x4C; x86 +0x34; twips
    @IggyFieldType(DataType.int16_t)
    int leading; // +0x4E; x86 +0x36; signed twips
    @IggyFieldType(DataType.uint8_t)
    int align; // +0x50; x86 +0x38; 0 left, 1 right, 2 center, 3 justify
    @IggyArrayFieldType(value = DataType.uint8_t, count = 15)
    byte[] unknownLayoutData; // +0x51..0x5F; x86 +0x39..0x3F (7 bytes)
    long ofs_name;

    @IggyArrayFieldType(value = DataType.wchar_t)
    String initialText; // UTF-16 string addressed by the final relative reference

    public IggyText(int type, int order_in_iggy_file, byte[] commonHeaderData, float par1, float par2, float par3, float par4, int enum_hex, int for_which_font_order_in_iggyfile, long fontClassNameAlignmentPadding, long fontClassNameOffset, byte[] textStyleData, long offset_of_name, String name) {
        this.type = type;
        this.textIndex = order_in_iggy_file;
        if (commonHeaderData.length != 28 || textStyleData.length != 32) {
            throw new IllegalArgumentException("Expected a 28-byte common header and 32-byte x64 text style");
        }
        ByteBuffer header = ByteBuffer.wrap(commonHeaderData).order(ByteOrder.LITTLE_ENDIAN);
        commonHeaderAlignmentPadding = header.getInt() & 0xffffffffL;
        unknownHeaderWord = header.getInt() & 0xffffffffL;
        unknownHeaderUpperWord = header.getInt() & 0xffffffffL;
        symbolClassNameOffset = header.getLong();
        runtimeClassPointer = header.getLong();
        this.par1 = par1;
        this.par2 = par2;
        this.par3 = par3;
        this.par4 = par4;
        this.enum_hex = enum_hex;
        this.fontIndex = for_which_font_order_in_iggyfile;
        this.fontClassNameAlignmentPadding = fontClassNameAlignmentPadding;
        this.fontClassNameOffset = fontClassNameOffset;
        ByteBuffer style = ByteBuffer.wrap(textStyleData).order(ByteOrder.LITTLE_ENDIAN);
        textColorRgba = new byte[4];
        style.get(textColorRgba);
        fontHeight = style.getShort() & 0xffff;
        maxLength = style.getShort() & 0xffff;
        leftMargin = style.getShort() & 0xffff;
        rightMargin = style.getShort() & 0xffff;
        indent = style.getShort() & 0xffff;
        leading = style.getShort();
        align = style.get() & 255;
        unknownLayoutData = new byte[15];
        style.get(unknownLayoutData);
        this.initialText = name;
    }

    protected IggyText() {
    }

    public IggyText(ReadDataStreamInterface stream) throws IOException {
        this.readFromDataStream(stream);
    }

    /** Create an empty native edit-text definition with null relative references. */
    public static IggyText create(boolean wide, int id) throws IOException {
        MemoryDataStream data = new MemoryDataStream();
        data.writeBytes(new byte[wide ? 104 : 68]);
        data.seek(0, SeekMode.SET);
        data.writeUI16(ID);
        data.writeUI16(id);
        for (int offset : wide ? new int[]{16, 56, 96} : new int[]{8, 36, 64}) {
            data.seek(offset, SeekMode.SET);
            if (wide) {
                data.writeUI64(1);
            } else {
                data.writeUI32(1);
            }
        }
        data.seek(0, SeekMode.SET);
        return wide ? new IggyText(data) : new IggyText32(data);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {

        long start = s.position();
        type = s.readUI16();
        //characterId - iggy Id
        textIndex = s.readUI16();
        readCommonHeader(s, true);

        // Text bounds in pixels: xmin, ymin, xmax, ymax.
        par1 = s.readFloat();
        par2 = s.readFloat();
        par3 = s.readFloat();
        par4 = s.readFloat();

        enum_hex = s.readUI16();
        fontIndex = s.readUI16(); //fontId
        fontClassNameAlignmentPadding = s.readUI32();
        fontClassNameOffset = s.readUI64();
        readTextStyle(s);
        ofs_name = s.readUI64();
        long name_address = ofs_name == 1 ? 0 : ofs_name + s.position() - 8;
        initialText = null;
        if (name_address != 0) {
            s.seek(name_address, SeekMode.SET);
            initialText = s.readWChar();
        }
        s.pad8bytes();
        readClassNames(s, start);
    }

    /**
     * Resolve class names in the containing main block, keeping raw references
     * for unchanged names.
     */
    protected final void readClassNames(ReadDataStreamInterface s, long start) throws IOException {
        long saved = s.position();
        symbolClassName = readName(s, start + (is64 ? 16 : 8));
        fontClassName = readName(s, start + (is64 ? 56 : 36));
        symbolClassNameChanged = false;
        fontClassNameChanged = false;
        s.seek(saved, SeekMode.SET);
    }

    private String readName(ReadDataStreamInterface s, long position) throws IOException {
        s.seek(position, SeekMode.SET);
        long address = IggyLayout.readPointer(s, is64);
        if (address == 0) {
            return null;
        }
        IggyLayout.checkRange(s, address, 2);
        s.seek(address, SeekMode.SET);
        return s.readWChar();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        long start = s.position();
        s.getIndexing().writeConstLength(IggyIndexBuilder.CONST_TEXT_DATA_SIZE);
        s.writeUI16(type);
        s.writeUI16(textIndex);
        writeCommonHeader(s);
        s.writeFloat(par1);
        s.writeFloat(par2);
        s.writeFloat(par3);
        s.writeFloat(par4);
        s.writeUI16(enum_hex);
        s.writeUI16(fontIndex);
        s.writeUI32(fontClassNameAlignmentPadding);
        s.writeUI64(fontClassNameOffset);
        writeTextStyle(s);
        long pointerPosition = s.position();
        s.writeUI64(1);
        if (initialText != null) {
            s.setOlderOffsetToThisPos(pointerPosition);
            s.writeWChar(initialText);
            s.pad8bytes();
            s.getIndexing().write16bitArray(initialText.length() + 1);
            s.getIndexing().pad8bytes();
        }

        writeChangedClassNames(s, start, true);
    }

    /**
     * Append edited names and emit their UTF-16 fixups; untouched references
     * are relocated by IggySwf.
     */
    protected final void writeChangedClassNames(WriteDataStreamInterface s, long start, boolean wide) throws IOException {
        if (symbolClassNameChanged) {
            writeClassName(s, start + (wide ? 16 : 8), symbolClassName, wide);
        }
        if (fontClassNameChanged) {
            writeClassName(s, start + (wide ? 56 : 36), fontClassName, wide);
        }
    }

    private void writeClassName(WriteDataStreamInterface s, long field, String name, boolean wide) throws IOException {
        long end = s.position();
        s.seek(field, SeekMode.SET);
        IggyLayout.writePointer(s, wide, name == null ? 0 : end);
        s.seek(end, SeekMode.SET);
        if (name != null) {
            s.writeWChar(name);
            if (wide) {
                s.getIndexing().write16bitArray(name.length() + 1);
                s.pad8bytes();
                s.getIndexing().pad8bytes();
            }
        }
    }

    final boolean isGeneratedPointer(long offset) {
        return offset == (is64 ? 96 : 64)
                || (symbolClassNameChanged && offset == (is64 ? 16 : 8))
                || (fontClassNameChanged && offset == (is64 ? 56 : 36));
    }

    protected final void readCommonHeader(ReadDataStreamInterface s, boolean is64) throws IOException {
        this.is64 = is64;
        commonHeaderAlignmentPadding = is64 ? s.readUI32() : 0;
        unknownHeaderWord = s.readUI32();
        unknownHeaderUpperWord = is64 ? s.readUI32() : 0;
        symbolClassNameOffset = is64 ? s.readUI64() : s.readUI32();
        runtimeClassPointer = is64 ? s.readUI64() : s.readUI32();
    }

    private void writeCommonHeader(WriteDataStreamInterface s) throws IOException {
        s.writeUI32(commonHeaderAlignmentPadding);
        s.writeUI32(unknownHeaderWord);
        s.writeUI32(unknownHeaderUpperWord);
        s.writeUI64(symbolClassNameOffset);
        s.writeUI64(runtimeClassPointer);
    }

    protected final void readTextStyle(ReadDataStreamInterface s) throws IOException {
        textColorRgba = s.readBytes(4);
        fontHeight = s.readUI16();
        maxLength = s.readUI16();
        leftMargin = s.readUI16();
        rightMargin = s.readUI16();
        indent = s.readUI16();
        leading = s.readSI16();
        align = s.readUI8();
        unknownLayoutData = s.readBytes(is64 ? 15 : 7);
    }

    protected final void writeTextStyle(WriteDataStreamInterface s) throws IOException {
        s.writeBytes(textColorRgba);
        s.writeUI16(fontHeight);
        s.writeUI16(maxLength);
        s.writeUI16(leftMargin);
        s.writeUI16(rightMargin);
        s.writeUI16(indent);
        s.writeSI16(leading);
        s.writeUI8(align);
        s.writeBytes(unknownLayoutData);
    }

    public int getType() {
        return type;
    }

    public int getTextIndex() {
        return textIndex;
    }

    /**
     * @deprecated Use the individual common-header fields instead.
     */
    @Deprecated
    public byte[] getZeroone() {
        ByteBuffer data = ByteBuffer.allocate(is64 ? 28 : 12).order(ByteOrder.LITTLE_ENDIAN);
        if (is64) {
            data.putInt((int) commonHeaderAlignmentPadding);
            data.putInt((int) unknownHeaderWord);
            data.putInt((int) unknownHeaderUpperWord);
            data.putLong(symbolClassNameOffset);
            data.putLong(runtimeClassPointer);
        } else {
            data.putInt((int) unknownHeaderWord);
            data.putInt((int) symbolClassNameOffset);
            data.putInt((int) runtimeClassPointer);
        }
        return data.array();
    }

    public long getCommonHeaderAlignmentPadding() {
        return commonHeaderAlignmentPadding;
    }

    /**
     * Exact meaning unresolved; x86 AS3 library binding can set this to 1.
     */
    public long getUnknownHeaderWord() {
        return unknownHeaderWord;
    }

    /**
     * Upper word of the opaque x64 header slot; preserved without
     * interpretation.
     */
    public long getUnknownHeaderUpperWord() {
        return unknownHeaderUpperWord;
    }

    /**
     * Relative UTF-16 name reference for this character's SymbolClass.
     */
    public long getSymbolClassNameOffset() {
        return symbolClassNameOffset;
    }

    /**
     * Raw pointer slot populated with a resolved AS3 class by the DLL.
     */
    public long getRuntimeClassPointer() {
        return runtimeClassPointer;
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

    /**
     * @deprecated Use getFontClassNameAlignmentPadding().
     */
    @Deprecated
    public long getZero() {
        return getFontClassNameAlignmentPadding();
    }

    /**
     * @deprecated Use getFontClassNameOffset().
     */
    @Deprecated
    public long getOne() {
        return getFontClassNameOffset();
    }

    /**
     * @deprecated Use the individual text-style fields instead.
     */
    @Deprecated
    public byte[] getSome() {
        ByteBuffer data = ByteBuffer.allocate(17 + unknownLayoutData.length).order(ByteOrder.LITTLE_ENDIAN);
        data.put(textColorRgba).putShort((short) fontHeight).putShort((short) maxLength);
        data.putShort((short) leftMargin).putShort((short) rightMargin);
        data.putShort((short) indent).putShort((short) leading).put((byte) align);
        data.put(unknownLayoutData);
        return data.array();
    }

    public long getFontClassNameAlignmentPadding() {
        return fontClassNameAlignmentPadding;
    }

    /**
     * Relative UTF-16 font-class name reference, distinct from SymbolClass.
     */
    public long getFontClassNameOffset() {
        return fontClassNameOffset;
    }

    public byte[] getTextColorRgba() {
        return textColorRgba.clone();
    }

    /**
     * Font height in twips (20 twips per pixel).
     */
    public int getFontHeight() {
        return fontHeight;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public int getIndent() {
        return indent;
    }

    /**
     * Signed line spacing in twips.
     */
    public int getLeading() {
        return leading;
    }

    /**
     * 0 left, 1 right, 2 center, 3 justify.
     */
    public int getAlign() {
        return align;
    }

    public byte[] getUnknownLayoutData() {
        return unknownLayoutData.clone();
    }

    public String getInitialText() {
        return initialText;
    }

    public void setInitialText(String initialText) {
        this.initialText = initialText;
    }

    /**
     * Bounds in pixels, in xmin, ymin, xmax, ymax order.
     */
    public void setBounds(float xmin, float ymin, float xmax, float ymax) {
        par1 = xmin;
        par2 = ymin;
        par3 = xmax;
        par4 = ymax;
    }

    /**
     * Set the complete behavior word, including unknown bits.
     */
    public void setEnum_hex(int flags) {
        enum_hex = flags;
    }

    public void setFontIndex(int fontId) {
        fontIndex = fontId;
    }

    public String getSymbolClassName() {
        return symbolClassName;
    }

    public void setSymbolClassName(String name) {
        if (!java.util.Objects.equals(symbolClassName, name)) {
            symbolClassName = name;
            symbolClassNameChanged = true;
        }
    }

    public String getFontClassName() {
        return fontClassName;
    }

    public void setFontClassName(String name) {
        if (!java.util.Objects.equals(fontClassName, name)) {
            fontClassName = name;
            fontClassNameChanged = true;
        }
    }

    public void setTextColorRgba(byte[] rgba) {
        if (rgba.length != 4) {
            throw new IllegalArgumentException("Expected four RGBA bytes");
        }
        textColorRgba = rgba.clone();
    }

    public void setFontHeight(int value) {
        fontHeight = value;
    }

    public void setMaxLength(int value) {
        maxLength = value;
    }

    public void setLeftMargin(int value) {
        leftMargin = value;
    }

    public void setRightMargin(int value) {
        rightMargin = value;
    }

    public void setIndent(int value) {
        indent = value;
    }

    public void setLeading(int value) {
        leading = value;
    }

    public void setAlign(int value) {
        align = value;
    }

}
