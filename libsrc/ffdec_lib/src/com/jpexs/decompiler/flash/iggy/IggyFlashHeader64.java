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

import com.jpexs.decompiler.flash.iggy.annotations.FieldPrinter;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 */
public class IggyFlashHeader64 implements IggyFlashHeaderInterface {

    @IggyFieldType(DataType.uint64_t)
    long off_base; // 0 Relative offset to first section (matches sizeof header);

    @IggyFieldType(DataType.uint64_t)
    long off_sequence_end; // 8  Relative offset to as3 file names table...

    @IggyFieldType(DataType.uint64_t)
    long off_font_end; // 0x10   relative offset to something

    @IggyFieldType(DataType.uint64_t)
    long off_sequence_start1; // 0x18  relative offset to something

    @IggyFieldType(DataType.uint64_t)
    long off_sequence_start2; //  0x20 relative offset to something

    @IggyFieldType(DataType.uint64_t)
    long off_sequence_start3; // 0x28 names_offset; 0x50 relative pointer to the names/import section of the file

    @IggyFieldType(DataType.uint32_t)
    long xmin; // 0x30 in pixels

    @IggyFieldType(DataType.uint32_t)
    long ymin; // 0x34 in pixels

    @IggyFieldType(DataType.uint32_t)
    long xmax; // 0x38 in pixels

    @IggyFieldType(DataType.uint32_t)
    long ymax; // 0x3C in pixels

    @IggyFieldType(DataType.uint32_t)
    long unk_40; // probably number of blocks/objects after header

    @IggyFieldType(DataType.uint32_t)
    long unk_44;

    @IggyFieldType(DataType.uint32_t)
    long unk_48;

    @IggyFieldType(DataType.uint32_t)
    long unk_4C;

    @IggyFieldType(DataType.uint32_t)
    long unk_50;

    @IggyFieldType(DataType.uint32_t)
    long unk_54;

    @IggyFieldType(DataType.float_t)
    float frame_rate;

    @IggyFieldType(DataType.uint32_t)
    long unk_5C;

    @IggyFieldType(DataType.uint64_t)
    long imported_guid;

    @IggyFieldType(DataType.uint64_t)
    long my_guid; // same for some fonts (eng + chinese)

    @IggyFieldType(DataType.uint64_t)
    long off_names; // 0x70 relative offset to the names/import section of the file  - end of fonts

    @IggyFieldType(DataType.uint64_t)
    long off_unk78; // 0x78 relative offset to something

    @IggyFieldType(DataType.uint64_t)
    long unk80;

    @IggyFieldType(DataType.uint64_t)
    long off_last_section;

    @IggyFieldType(DataType.uint64_t)
    long off_flash_filename;

    @IggyFieldType(DataType.uint64_t)
    long off_decl_strings;

    @IggyFieldType(DataType.uint64_t)
    long off_type_of_fonts;

    @IggyFieldType(DataType.uint64_t)
    long flags;

    @IggyFieldType(DataType.uint32_t)
    long font_count;

    @IggyFieldType(DataType.uint32_t)
    long zero2;

    // Offset 0xB8 (outside header): there are *unk_40* relative offsets that point to flash objects.
    // The flash objects are in a format different to swf but there is probably a way to convert between them.
    // After the offsets, the bodies of objects pointed above, which apparently have a code like 0xFFXX to identify the type of object, followed by a (unique?) identifier
    // for the object.
    // A DefineEditText-like object can be easily spotted and apparently uses type code 0x06 (or 0xFF06) but as stated above,
    // it is written in a different way.
    public IggyFlashHeader64(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    private long base_address;

    private long sequence_end_address;

    private long font_end_address;

    private long sequence_start_address1;

    private long sequence_start_address2;

    private long sequence_start_address3;

    private long names_address;

    private long unk78_address;

    private long last_section_address;

    private long flash_filename_address;

    private long decl_strings_address;

    private long type_fonts_address;

    private long base_ofs_pos;

    private long sequence_end_ofs_pos;

    private long font_end_ofs_pos;

    private long sequence_start1_ofs_pos;

    private long sequence_start2_ofs_pos;

    private long sequence_start3_ofs_pos;

    private long names_ofs_pos;

    private long unk78_ofs_pos;

    private long last_section_ofs_pos;

    private long flash_filename_ofs_pos;

    private long decl_strings_ofs_pos;

    private long type_fonts_ofs_pos;

    public long getBase_ofs_pos() {
        return base_ofs_pos;
    }

    public long getSequence_end_ofs_pos() {
        return sequence_end_ofs_pos;
    }

    public long getFont_end_ofs_pos() {
        return font_end_ofs_pos;
    }

    public long getSequence_start1_ofs_pos() {
        return sequence_start1_ofs_pos;
    }

    public long getSequence_start2_ofs_pos() {
        return sequence_start2_ofs_pos;
    }

    public long getSequence_start3_ofs_pos() {
        return sequence_start3_ofs_pos;
    }

    public long getNames_ofs_pos() {
        return names_ofs_pos;
    }

    public long getUnk78_ofs_pos() {
        return unk78_ofs_pos;
    }

    public long getLast_section_ofs_pos() {
        return last_section_ofs_pos;
    }

    public long getFlash_filename_ofs_pos() {
        return flash_filename_ofs_pos;
    }

    public long getDecl_strings_ofs_pos() {
        return decl_strings_ofs_pos;
    }

    public long getType_fonts_ofs_pos() {
        return type_fonts_ofs_pos;
    }

    /**
     * Updates all addresses by inserting gap
     *
     * @param pos Position to insert bytes
     * @param size Number of bytes
     */
    public void insertGapAfter(long pos, long size) {
        for (Field f : IggyFlashHeader64.class.getDeclaredFields()) {
            if (f.getName().endsWith("_address")) {
                try {
                    long val = f.getLong(this);
                    if (val > pos) {
                        long newval = val + size;
                        f.setLong(this, newval);
                    }
                } catch (IllegalAccessException iex) {
                    //should not happen
                }
            }
        }
    }

    public long getImported_guid() {
        return imported_guid;
    }

    public long getBaseAddress() {
        return base_address;
    }

    public long getSequenceEndAddress() {
        return sequence_end_address;
    }

    public long getFontEndAddress() {
        return font_end_address;
    }

    public void setFontEndAddress(long val) {
        this.font_end_address = val;
    }

    public long getSequenceStartAddress1() {
        return sequence_start_address1;
    }

    public long getSequenceStartAddress2() {
        return sequence_start_address2;
    }

    public long getSequenceStartAddress3() {
        return sequence_start_address3;
    }

    public long getNamesAddress() {
        return names_address;
    }

    public long getUnk78Address() {
        return unk78_address;
    }

    public long getLastSectionAddress() {
        return last_section_address;
    }

    public long getFlashFilenameAddress() {
        return flash_filename_address;
    }

    public long getDeclStringsAddress() {
        return decl_strings_address;
    }

    public long getTypeFontsAddress() {
        return type_fonts_address;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        base_ofs_pos = stream.position();
        off_base = stream.readUI64();
        base_address = off_base == 1 ? 0 : off_base + stream.position() - 8;

        sequence_end_ofs_pos = stream.position();
        off_sequence_end = stream.readUI64();
        sequence_end_address = off_sequence_end == 1 ? 0 : off_sequence_end + stream.position() - 8;

        font_end_ofs_pos = stream.position();
        off_font_end = stream.readUI64();
        font_end_address = off_font_end == 1 ? 0 : off_font_end + stream.position() - 8;

        sequence_start1_ofs_pos = stream.position();
        off_sequence_start1 = stream.readUI64();
        sequence_start_address1 = off_sequence_start1 == 1 ? 0 : off_sequence_start1 + stream.position() - 8;

        sequence_start2_ofs_pos = stream.position();
        off_sequence_start2 = stream.readUI64();
        sequence_start_address2 = off_sequence_start2 == 1 ? 0 : off_sequence_start2 + stream.position() - 8;

        sequence_start3_ofs_pos = stream.position();
        off_sequence_start3 = stream.readUI64();
        sequence_start_address3 = off_sequence_start3 == 1 ? 0 : off_sequence_start3 + stream.position() - 8;

        xmin = stream.readUI32();
        ymin = stream.readUI32();
        xmax = stream.readUI32();
        ymax = stream.readUI32();

        unk_40 = stream.readUI32(); // probably number of blocks/objects after header
        unk_44 = stream.readUI32();
        unk_48 = stream.readUI32();
        unk_4C = stream.readUI32();
        unk_50 = stream.readUI32();
        unk_54 = stream.readUI32();
        frame_rate = stream.readFloat();
        unk_5C = stream.readUI32();
        imported_guid = stream.readUI64();
        my_guid = stream.readUI64();

        names_ofs_pos = stream.position();
        off_names = stream.readUI64();
        names_address = off_names == 1 ? 0 : off_names + stream.position() - 8;

        unk78_ofs_pos = stream.position();
        off_unk78 = stream.readUI64();
        unk78_address = off_unk78 == 1 ? 0 : off_unk78 + stream.position() - 8;

        unk80 = stream.readUI64(); //Maybe number of imports/names pointed by names_offset

        last_section_ofs_pos = stream.position();
        off_last_section = stream.readUI64();
        last_section_address = off_last_section == 1 ? 0 : off_last_section + stream.position() - 8;

        flash_filename_ofs_pos = stream.position();
        off_flash_filename = stream.readUI64();
        flash_filename_address = off_flash_filename == 1 ? 0 : off_flash_filename + stream.position() - 8;

        decl_strings_ofs_pos = stream.position();
        off_decl_strings = stream.readUI64(); //relative offset to as3 code (16 bytes header + abc blob)
        decl_strings_address = off_decl_strings == 1 ? 0 : off_decl_strings + stream.position() - 8;

        type_fonts_ofs_pos = stream.position();
        off_type_of_fonts = stream.readUI64(); //relative offset to as3 file names table (or classes names or whatever)
        type_fonts_address = off_type_of_fonts == 1 ? 0 : off_type_of_fonts + stream.position() - 8;

        flags = stream.readUI64();
        font_count = stream.readUI32();
        zero2 = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        off_base = base_address == 0 ? 1 : base_address - stream.position();
        stream.writeUI64(off_base);
        off_sequence_end = sequence_end_address == 0 ? 1 : sequence_end_address - stream.position();
        stream.writeUI64(off_sequence_end);
        off_font_end = font_end_address == 0 ? 1 : font_end_address - stream.position();
        stream.writeUI64(off_font_end);
        off_sequence_start1 = sequence_start_address1 == 0 ? 1 : sequence_start_address1 - stream.position();
        stream.writeUI64(off_sequence_start1);
        off_sequence_start2 = sequence_start_address2 == 0 ? 1 : sequence_start_address2 - stream.position();
        stream.writeUI64(off_sequence_start2);
        off_sequence_start3 = sequence_start_address3 == 0 ? 1 : sequence_start_address3 - stream.position();
        stream.writeUI64(off_sequence_start3);
        stream.writeUI32(xmin);
        stream.writeUI32(ymin);
        stream.writeUI32(xmax);
        stream.writeUI32(ymax);
        stream.writeUI32(unk_40);
        stream.writeUI32(unk_44);
        stream.writeUI32(unk_48);
        stream.writeUI32(unk_4C);
        stream.writeUI32(unk_50);
        stream.writeUI32(unk_54);
        stream.writeFloat(frame_rate);
        stream.writeUI32(unk_5C);
        stream.writeUI64(imported_guid);
        stream.writeUI64(my_guid);
        off_names = names_address == 0 ? 1 : names_address - stream.position();
        stream.writeUI64(off_names);
        off_unk78 = unk78_address == 0 ? 1 : unk78_address - stream.position();
        stream.writeUI64(off_unk78);
        stream.writeUI64(unk80);
        off_last_section = last_section_address == 0 ? 1 : last_section_address - stream.position();
        stream.writeUI64(off_last_section);
        off_flash_filename = flash_filename_address == 0 ? 1 : flash_filename_address - stream.position();
        stream.writeUI64(off_flash_filename);
        off_decl_strings = decl_strings_address == 0 ? 1 : decl_strings_address - stream.position();
        stream.writeUI64(off_decl_strings);
        off_type_of_fonts = type_fonts_address == 0 ? 1 : type_fonts_address - stream.position();
        stream.writeUI64(off_type_of_fonts);
        stream.writeUI64(flags);
        stream.writeUI32(font_count);
        stream.writeUI32(zero2);

        stream.getIndexing().writeLengthCustom(184, new int[]{0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x34, 0x38, 0x3C, 0x40, 0x44, 0x48, 0x4C, 0x50, 0x58, 0x70, 0x78, 0x80, 0x84, 0x88, 0x90, 0x98, 0xA0, 0xB0}, new int[]{2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 2, 5, 5, 2, 2, 2, 2, 5});

    }

    @Override
    public String toString() {
        return FieldPrinter.getObjectSummary(this);
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
        return frame_rate;
    }
}
