package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagTypeInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jindra
 *
 * Based of works of somebody called eternity.
 */
public class IggyFlashHeader64 implements IggyFlashHeaderInterface {

    @IggyFieldType(DataType.uint64_t)
    long main_offset; // 0 Relative offset to first section (matches sizeof header);
    @IggyFieldType(DataType.uint64_t)
    long as3_section_offset; // 8  Relative offset to as3 file names table...
    @IggyFieldType(DataType.uint64_t)
    long unk_offset; // 0x10   relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long unk_offset2; // 0x18  relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long unk_offset3; //  0x20 relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long unk_offset4; // 0x28 names_offset; 0x50 relative pointer to the names/import section of the file
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
    float frame_rate;
    @IggyFieldType(DataType.uint32_t)
    long unk_5C;
    @IggyFieldType(DataType.uint64_t)
    long unk_60;
    @IggyFieldType(DataType.uint64_t)
    long unk_68;
    @IggyFieldType(DataType.uint64_t)
    long names_offset; // 0x70 relative offset to the names/import section of the file
    @IggyFieldType(DataType.uint64_t)
    long unk_offset5; // 0x78 relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long unk_80; // Maybe number of imports/names pointed by names_offset
    @IggyFieldType(DataType.uint64_t)
    long last_section_offset; // 0x88 relative offset, points to the small last section of the file
    @IggyFieldType(DataType.uint64_t)
    long unk_offset6; // 0x90 relative offset to something
    @IggyFieldType(DataType.uint64_t)
    long as3_code_offset; // 0x98 relative offset to as3 code (16 bytes header + abc blob)
    @IggyFieldType(DataType.uint64_t)
    long as3_names_offset; // 0xA0 relative offset to as3 file names table (or classes names or whatever)
    @IggyFieldType(DataType.uint32_t)
    long unk_A8;
    @IggyFieldType(DataType.uint32_t)
    long unk_AC;
    @IggyFieldType(DataType.uint32_t)
    long unk_B0; // Maybe number of classes / as3 names
    @IggyFieldType(DataType.uint32_t)
    long unk_B4;

    /*@IggyArrayFieldType(value = DataType.uint32_t, count = 20)
    long unk_offsets_a[] = new long[20];
    @IggyArrayFieldType(value = DataType.uint32_t, count = 20)
    long unk_offsets_b[] = new long[20];*/
    // Offset 0xB8 (outside header): there are *unk_40* relative offsets that point to flash objects.
    // The flash objects are in a format different to swf but there is probably a way to convert between them.
    // After the offsets, the bodies of objects pointed above, which apparently have a code like 0xFFXX to identify the type of object, followed by a (unique?) identifier
    // for the object.
    // A DefineEditText-like object can be easily spotted and apparently uses type code 0x06 (or 0xFF06) but as stated above,
    // it is written in a different way.
    public IggyFlashHeader64(AbstractDataStream stream) throws IOException {
        readFromDataStream(stream);
    }

    private int ofs = 0;
    private List<Long> offsets;

    private String currentOffset() {
        return String.format(" [offset: %d]", offsets.get(ofs++));
    }

    /*
    offsets:
    name (UI16 chars, zero terminated)
    UI16
    taglist_offset (aka main_offset)
    after_taglist
    
    
     */
    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        this.offsets = offsets;
        main_offset = stream.readUI64();
        as3_section_offset = stream.readUI64();
        unk_offset = stream.readUI64();
        unk_offset2 = stream.readUI64();
        unk_offset3 = stream.readUI64();
        unk_offset4 = stream.readUI64();
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
        unk_60 = stream.readUI64();
        unk_68 = stream.readUI64();
        names_offset = stream.readUI64();
        unk_offset5 = stream.readUI64();
        unk_80 = stream.readUI64();
        last_section_offset = stream.readUI64();
        unk_offset6 = stream.readUI64();
        as3_code_offset = stream.readUI64();
        as3_names_offset = stream.readUI64();
        unk_A8 = stream.readUI32();
        unk_AC = stream.readUI32();
        unk_B0 = stream.readUI32();
        unk_B4 = stream.readUI32();
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("main_offset ").append(main_offset).append(currentOffset()).append("\r\n");
        sb.append("as3_section_offset ").append(as3_section_offset).append(currentOffset()).append("\r\n");
        sb.append("unk_offset ").append(unk_offset).append(currentOffset()).append("\r\n");
        sb.append("unk_offset2 ").append(unk_offset2).append(currentOffset()).append("\r\n");
        sb.append("unk_offset3 ").append(unk_offset3).append(currentOffset()).append("\r\n");
        sb.append("unk_offset4 ").append(unk_offset4).append(currentOffset()).append("\r\n");
        sb.append("xmin ").append(xmin).append(currentOffset()).append("\r\n");
        sb.append("ymin ").append(ymin).append(currentOffset()).append("\r\n");
        sb.append("xmax ").append(ymax).append(currentOffset()).append("\r\n");
        sb.append("ymax ").append(ymax).append(currentOffset()).append("\r\n");
        sb.append("unk_40 ").append(unk_40).append(currentOffset()).append("\r\n");
        sb.append("unk_44 ").append(unk_44).append(currentOffset()).append("\r\n");
        sb.append("unk_48 ").append(unk_48).append("\r\n");
        sb.append("unk_4C ").append(unk_4C).append("\r\n");
        sb.append("unk_50 ").append(unk_50).append("\r\n");
        sb.append("unk_54 ").append(unk_54).append("\r\n");
        sb.append("frame_rate ").append(frame_rate).append("\r\n");
        sb.append("unk_5C ").append(unk_5C).append("\r\n");
        sb.append("unk_60 ").append(unk_60).append("\r\n");
        sb.append("unk_68 ").append(unk_68).append("\r\n");
        sb.append("names_offset ").append(names_offset).append("\r\n");
        sb.append("unk_offset5 ").append(unk_offset5).append("\r\n");
        sb.append("unk_80 ").append(unk_80).append("\r\n");
        sb.append("last_section_offset ").append(last_section_offset).append("\r\n");
        sb.append("unk_offset6 ").append(unk_offset6).append("\r\n");
        sb.append("as3_code_offset ").append(as3_code_offset).append("\r\n");
        sb.append("as3_names_offset ").append(as3_names_offset).append("\r\n");
        sb.append("unk_A8 ").append(unk_A8).append("\r\n");
        sb.append("unk_AC ").append(unk_AC).append("\r\n");
        sb.append("unk_B0 ").append(unk_B0).append("\r\n");
        sb.append("unk_B4 ").append(unk_B4).append("\r\n");
        sb.append("]");
        return sb.toString();

    }

}
