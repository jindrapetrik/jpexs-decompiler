package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class IggySwf implements StructureInterface {

    final static int NO_OFFSET = 1;

    @IggyFieldType(value = DataType.widechar_t, count = 48)
    String name;

    Map<Integer, IggyFont> fonts;
    Map<Integer, IggyText> texts;
    Map<Integer, Integer> text2Font;

    private IggyFlashHeader64 hdr;
    private Map<Long, Long> sizesOfOffsets;
    private List<Long> offsets;
    private List<IggyTag> tags = new ArrayList<>();

    public List<IggyTag> getTags() {
        return tags;
    }

    public IggySwf(IggyFlashHeader64 header, ReadDataStreamInterface stream, List<Long> offsets) throws IOException {
        this.hdr = header;
        this.offsets = offsets;
        calcSizesFromOffsets();
        readFromDataStream(stream);
    }

    private void calcSizesFromOffsets() {
        sizesOfOffsets = new HashMap<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            sizesOfOffsets.put(offsets.get(i), offsets.get(i + 1) - offsets.get(i));
        }
        sizesOfOffsets.put(offsets.get(offsets.size() - 1), 0L); //Last offset has 0L length?
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        //here is offset[0]
        StringBuilder nameBuilder = new StringBuilder();
        do {
            char c = (char) s.readUI16();
            if (c == '\0') {
                break;
            }
            nameBuilder.append(c);
        } while (true);
        name = nameBuilder.toString();
        //here is offset[1]
        int pad8 = 8 - (int) (s.position() % 8);
        s.seek(pad8, SeekMode.CUR);
        //here is offset [2]
        s.seek(hdr.getBaseAddress(), SeekMode.SET);
        s.readUI64(); //one pad

        long font_data_addresses[] = new long[(int) hdr.font_count];
        long font_data_sizes[] = new long[(int) hdr.font_count];

        for (int i = 0; i < hdr.font_count; i++) {
            long offset = s.readUI64();
            font_data_addresses[i] = offset + s.position() - 8;
            long next_offset = s.readUI64();
            s.seek(-8, SeekMode.CUR);
            if (next_offset == 1) {
                font_data_sizes[i] = hdr.getFontEndAddress() - font_data_addresses[i];
            } else {
                font_data_sizes[i] = next_offset - offset;
            }
        }
        List<Long> text_addresses = new ArrayList<>();
        List<Long> text_data_sizes = new ArrayList<>();
        while (true) {
            long offset = s.readUI64();
            long text_addr = offset + s.position() - 8;
            text_addresses.add(text_addr);
            long next_offset = s.readUI64();
            s.seek(-8, SeekMode.CUR);
            if (next_offset == 1) {
                text_data_sizes.add(hdr.getFontEndAddress() - text_addr);
                break;
            } else {
                text_data_sizes.add(next_offset - offset);
            }
        }

        List<Long> font_add_off = new ArrayList<>();
        List<Long> font_add_size = new ArrayList<>();
        if (hdr.isImported()) { // tohle muze narusit iggy order, ale oni to pak stejne pocitaji dle infa 
            long additionalOffset = s.readUI64();
            font_add_off.add(additionalOffset + s.position() - 8);
            font_add_size.add(hdr.getFontEndAddress() - font_add_off.get(0));
            if (s.readUI8(font_add_off.get(0)) != 22) { //22 = Text
                for (int i = 0; i < hdr.font_count; i++) {
                    if (s.readUI8(text_addresses.get(i)) == 22) { //TEXT
                        long pomoff;
                        long pomsize;
                        pomoff = text_addresses.get(i);
                        pomsize = text_data_sizes.get(i);
                        text_addresses.set(i, font_add_off.get(0));
                        text_data_sizes.set(i, font_add_size.get(0));
                        font_add_off.set(0, pomoff);
                        font_add_size.set(0, pomsize);
                    }
                }
            }
        }

        //long skipBytes = 840 - s.position();
        fonts = new HashMap<>();
        for (int i = 0; i < hdr.font_count; i++) {
            s.seek(font_data_addresses[i], SeekMode.SET);
            //byte font_data[] = s.readBytes((int) font_data_sizes[i]);
            IggyFont font = new IggyFont(s);
            tags.add(font);
            fonts.put(i/*??*/, font);
        }

        for (int i = 0; i < text_addresses.size(); i++) {
            byte text_data[] = s.readBytes((int) (long) text_data_sizes.get(i));
        }

        if (hdr.isImported()) {
            s.seek(font_add_off.get(0), SeekMode.SET);
            byte font_add_data[] = s.readBytes((int) (long) font_add_size.get(0));
        }

        s.seek(hdr.getFontEndAddress(), SeekMode.SET);

    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        List<Long> newOffsets = new ArrayList<>();

        try {
            newOffsets.add(stream.position());
            for (int i = 0; i < name.length(); i++) {
                stream.writeUI16(name.charAt(i));
            }
            stream.writeUI16(0);
            newOffsets.add(stream.position());
            long pad8 = 8 - (stream.position() % 8);
            for (int i = 0; i < pad8; i++) {
                stream.write(0);
            }
            newOffsets.add(stream.position());
            for (IggyTag tag : tags) {
                tag.writeToDataStream(stream);
                newOffsets.add(stream.position());
            }
        } catch (IOException ex) {
            //ignore
        }
        this.offsets = newOffsets;
        calcSizesFromOffsets();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        return sb.toString();
    }

}
