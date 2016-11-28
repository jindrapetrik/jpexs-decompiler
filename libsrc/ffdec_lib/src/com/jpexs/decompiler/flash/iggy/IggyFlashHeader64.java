package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.FieldPrinter;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 *
 * @author Jindra
 *
 * Based of works of somebody called eternity.
 */
public class IggyFlashHeader64 implements IggyFlashHeaderInterface {

    @IggyFieldType(DataType.uint64_t)
    long off_start; // 0 Relative offset to first section (matches sizeof header);
    @IggyFieldType(DataType.uint64_t)
    long off_seq_end; // 8  Relative offset to as3 file names table...
    @IggyFieldType(DataType.uint64_t)
    long off_font_end; // 0x10   relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long off_seq_start1; // 0x18  relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long pad_to_match; //  0x20 relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long off_seq_start2; // 0x28 names_offset; 0x50 relative pointer to the names/import section of the file
    @IggyFieldType(DataType.uint32_t)
    long xmin; // 0x30 in pixels
    @IggyFieldType(DataType.uint32_t)
    long ymin; // 0x34 in pixels
    @IggyFieldType(DataType.uint32_t)
    long xmax; // 0x38 in pixels
    @IggyFieldType(DataType.uint32_t)
    long ymax; // 0x3C in pixels
    @IggyFieldType(DataType.uint32_t)
    long unk_40; // probably numer of blocks/objects after header
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
    @IggyFieldType(DataType.uint32_t)
    long additional_import1;
    @IggyFieldType(DataType.uint32_t)
    long zero1;

    //local
    private int imported = 0;

    @IggyFieldType(DataType.uint64_t)
    long unk_guid; // same for some fonts (eng + chinese)
    @IggyFieldType(DataType.uint64_t)
    long off_names; // 0x70 relative offset to the names/import section of the file  - end of fonts
    @IggyFieldType(DataType.uint64_t)
    long off_unk78; // 0x78 relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long off_unk80;
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
    private long sequence_start_address;
    private long names_address;
    private long unk78_address;
    private long unk80_address;
    private long last_section_address;
    private long flash_filename_address;
    private long decl_strings_address;
    private long type_fonts_address;

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

    public boolean isImported() {
        return imported == 1;
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

    public long getSequenceStartAddress() {
        return sequence_start_address;
    }

    public long getNamesAddress() {
        return names_address;
    }

    public long getUnk78Address() {
        return unk78_address;
    }

    public long getUnk80Address() {
        return unk80_address;
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
        off_start = stream.readUI64();
        base_address = off_start + stream.position() - 8;
        off_seq_end = stream.readUI64();
        sequence_end_address = off_seq_end + stream.position() - 8;
        off_font_end = stream.readUI64();
        font_end_address = off_font_end + stream.position() - 8;
        off_seq_start1 = stream.readUI64(); //to 1 padd occurence (2 times)
        pad_to_match = stream.readUI64();
        if (pad_to_match != 1) {
            throw new IOException("Wrong iggy file - no pad to match 1");
        }
        off_seq_start2 = stream.readUI64();
        if (off_seq_start1 != off_seq_start2) {
            throw new IOException("Wrong iggy font format (sequence_start)!\n");
        }
        sequence_start_address = off_seq_start2 + stream.position() - 8;
        xmin = stream.readUI32();
        ymin = stream.readUI32();
        xmax = stream.readUI32();
        ymax = stream.readUI32();
        unk_40 = stream.readUI32();
        unk_44 = stream.readUI32();
        unk_48 = stream.readUI32();
        unk_4C = stream.readUI32();
        unk_50 = stream.readUI32();
        unk_54 = stream.readUI32();
        frame_rate = stream.readFloat();
        unk_5C = stream.readUI32();
        additional_import1 = stream.readUI32();
        if (additional_import1 > 0) {
            imported = 1;
        }
        zero1 = stream.readUI32();
        unk_guid = stream.readUI64();

        off_names = stream.readUI64();
        names_address = off_names + stream.position() - 8;
        off_unk78 = stream.readUI64();
        unk78_address = off_unk78 + stream.position() - 8;
        off_unk80 = stream.readUI64();
        unk80_address = off_unk80 + stream.position() - 8;
        off_last_section = stream.readUI64();
        last_section_address = off_last_section + stream.position() - 8;
        off_flash_filename = stream.readUI64();
        flash_filename_address = off_flash_filename + stream.position() - 8;
        off_decl_strings = stream.readUI64();
        decl_strings_address = off_decl_strings + stream.position() - 8;
        off_type_of_fonts = stream.readUI64();
        type_fonts_address = off_type_of_fonts + stream.position() - 8;

        flags = stream.readUI64();
        font_count = stream.readUI32();
        zero2 = stream.readUI32();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        off_start = base_address - stream.position();
        stream.writeUI64(off_start);
        off_seq_end = sequence_end_address - stream.position();
        stream.writeUI64(off_seq_end);
        off_font_end = font_end_address - stream.position();
        stream.writeUI64(off_font_end);
        off_seq_start1 = sequence_start_address - stream.position();
        stream.writeUI64(off_seq_start1);
        stream.writeUI64(pad_to_match);
        off_seq_start2 = sequence_start_address - stream.position();
        stream.writeUI64(off_seq_start2);
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
        stream.writeUI32(additional_import1);
        stream.writeUI32(zero1);
        stream.writeUI64(unk_guid);
        off_names = names_address - stream.position();
        stream.writeUI64(off_names);
        off_unk78 = unk78_address - stream.position();
        stream.writeUI64(off_unk78);
        off_unk80 = unk80_address - stream.position();
        stream.writeUI64(off_unk80);
        off_last_section = last_section_address - stream.position();
        stream.writeUI64(off_last_section);
        off_flash_filename = flash_filename_address - stream.position();
        stream.writeUI64(off_flash_filename);
        off_decl_strings = decl_strings_address - stream.position();
        stream.writeUI64(off_decl_strings);
        off_type_of_fonts = type_fonts_address - stream.position();
        stream.writeUI64(off_type_of_fonts);
        stream.writeUI64(flags);
        stream.writeUI32(font_count);
        stream.writeUI32(zero2);
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
