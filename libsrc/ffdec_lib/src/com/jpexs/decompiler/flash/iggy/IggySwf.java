package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import com.jpexs.helpers.Helper;
import java.io.FileOutputStream;
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

    @IggyFieldType(value = DataType.wchar_t, count = 48)
    String name;

    List<IggyFont> fonts = new ArrayList<>();
    private List<Long> font_data_addresses = new ArrayList<>();
    List<IggyFont> add_fonts = new ArrayList<>();
    private List<Long> add_font_data_addresses = new ArrayList<>();

    private IggyFlashHeader64 hdr;

    public IggySwf(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }
    private List<IggyText> texts = new ArrayList<>();
    private List<Long> text_data_addresses = new ArrayList<>();
    private List<IggyText> add_texts = new ArrayList<>();
    private List<Long> add_text_data_addresses = new ArrayList<>();

    private byte font_add_data[];
    private List<Long> font_additional_size = new ArrayList<>();
    private IggyFontBinInfo font_bin_info[];
    private IggySequence sequence;
    private IggyFontTypeInfo type_info[];
    private String type_info_name[];
    private IggyDeclStrings decl_strings;
    private long ofs_additional;
    private long additional_address;

    public IggyFlashHeader64 getHdr() {
        return hdr;
    }

    public void replaceFontTag(int fontIndex, IggyFont newFont) throws IOException {
        /*long newLen;
        byte newData[];
        try (WriteDataStreamInterface stream = new TemporaryDataStream()) {
            newFont.writeToDataStream(stream);
            newData = stream.getAllBytes();
            newLen = newData.length;
        }

        //FIXME
        Helper.writeFile("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_new\\font" + fontIndex + ".bin", newData);

         long oldLen = font_data_sizes[fontIndex];
        long diff = newLen - oldLen;
        if (diff != 0) {
            font_data_sizes[fontIndex] = newLen;
            for (int i = fontIndex; i < hdr.font_count; i++) {
                font_data_addresses[i] += diff;
            }
            for (int i = 0; i < text_addresses.size(); i++) {
                text_addresses.set(i, text_addresses.get(i) + diff);
            }
            for (int i = 0; i < font_add_off.size(); i++) {
                font_add_off.set(i, font_add_off.get(i) + diff);
            }
        }
        hdr.insertGapAfter(font_data_addresses[fontIndex], diff);
         */
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        this.hdr = new IggyFlashHeader64(s);
        //Save all font bytes to buffer for later easy modification
        //here is offset[0] - 184
        name = s.readWChar();
        //here is offset[1] - 230
        int pad8 = 8 - (int) (s.position() % 8);
        if (pad8 > 8) {
            s.seek(pad8, SeekMode.CUR);
        }
        //here is offset [2]  - 232
        s.seek(hdr.getBaseAddress(), SeekMode.SET);
        s.readUI64(); //pad 1

        List<Long> itemsAddresses = new ArrayList<>();

        while (true) {
            long offset = s.readUI64();
            if (offset == 1) {
                break;
            }
            itemsAddresses.add(offset + s.position() - 8);
        }
        if (hdr.getImported_guid() != 0) {
            ofs_additional = s.readUI64();
            additional_address = ofs_additional == 1 ? 0 : ofs_additional + s.position() - 8;
        }
        for (Long addr : itemsAddresses) {
            s.seek(addr, SeekMode.SET);
            int kind = s.readUI8();
            s.seek(-1, SeekMode.CUR);
            switch (kind) {
                case 22 /*FONT*/:
                    IggyFont font = new IggyFont(s);
                    font_data_addresses.add(addr);
                    fonts.add(font);
                    break;
                case 6 /*TEXT*/:
                    IggyText text = new IggyText(s);
                    text_data_addresses.add(addr);
                    texts.add(text);
                    break;
                default:
                    throw new RuntimeException("Unknown item kind: " + kind);
            }
        }

        if (additional_address != 0) {
            s.seek(additional_address, SeekMode.SET);
            List<Long> additionalItemsAddresses = new ArrayList<>();
            while (true) {
                long offset = s.readUI64();
                if (offset == 1) {
                    break;
                }
                additionalItemsAddresses.add(offset + s.position() - 8);
            }
            for (Long addr : additionalItemsAddresses) {
                s.seek(addr, SeekMode.SET);
                int kind = s.readUI8();
                s.seek(-1, SeekMode.CUR);
                switch (kind) {
                    case 22 /*FONT*/:
                        IggyFont font = new IggyFont(s);
                        add_font_data_addresses.add(addr);
                        add_fonts.add(font);
                        break;
                    case 6 /*TEXT*/:
                        IggyText text = new IggyText(s);
                        add_text_data_addresses.add(addr);
                        add_texts.add(text);
                        break;
                    default:
                        throw new RuntimeException("Unknown imported item kind: " + kind);
                }
            }
        }

        /*
        for (int i = 0; i < hdr.font_count; i++) {
            long offset = s.readUI64();
            font_data_addresses[i] = offset + s.position() - 8;
            long next_offset = s.readUI64();
            s.seek(-8, SeekMode.CUR);
            if (next_offset == 1) {
                font_data_sizes[i] = hdr.getFontEndAddress() - font_data_addresses[i];
            } else {
                font_data_sizes[i] = next_offset - offset + 8;
            }
        }
        while (true) {
            long offset = s.readUI64();
            if (offset == 1) {
                break;
            }
            long text_addr = offset + s.position() - 8;
            text_addresses.add(text_addr);
            long next_offset = s.readUI64();
            s.seek(-8, SeekMode.CUR);
            if (next_offset == 1) {
                text_data_sizes.add(hdr.getFontEndAddress() - text_addr);
                break;
            } else {
                text_data_sizes.add(next_offset - offset + 8);
            }
        }
        s.readUI64(); //1

        if (hdr.getImported_guid() != 0) {
            ofs_additional = s.readUI64();
            additional_address = ofs_additional + s.position() - 8;
            font_additional_addresses.add(additional_address);
            font_additional_size.add(hdr.getFontEndAddress() - font_additional_addresses.get(0));

            if (s.readUI8(font_additional_addresses.get(0)) != 22) { //additional is not text
                // font je hozen mezi infa...
                for (int i = 0; i < text_addresses.size(); i++) { //walk all already read texts
                    if (s.readUI8(text_addresses.get(i)) == 22) { //check if it's text
                        long pomoff;
                        long pomsize;
                        pomoff = text_addresses.get(i);
                        pomsize = text_data_sizes.get(i);
                        text_addresses.set(i, font_additional_addresses.get(0));
                        text_data_sizes.set(i, font_additional_size.get(0));
                        font_additional_addresses.set(0, pomoff);
                        font_additional_size.set(0, pomsize);
                    }
                }
            }
        }*/

 /*        if (s.readUI8(s.position()) == 22) { //22 = Text
            byte[] textHdr = s.readBytes(IggyText.STRUCT_SIZE);
            String textStr = s.readWChar();
            s.pad8bytes();
        }*/
        //here is offset [3]  - 744
        /* fonts = new ArrayList<>();
        for (int i = 0; i < hdr.font_count; i++) {
            s.seek(font_data_addresses[i], SeekMode.SET);
            byte font_data[] = s.readBytes((int) font_data_sizes[i]);
            //FIXME
            //Helper.writeFile("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_orig\\font" + i + ".bin", font_data);
            s.seek(-font_data_sizes[i], SeekMode.CUR);
            IggyFont font = new IggyFont(s);
            fonts.add(font);
        }

        for (int i = 0;
                i < text_addresses.size();
                i++) {
            s.seek(text_addresses.get(i), SeekMode.SET);
            texts.add(new IggyText(s));
            //byte text_data[] = s.readBytes((int) (long) text_data_sizes.get(i));
            //text_data_bytes.add(text_data);
        }

        if (hdr.getImported_guid()
                != 0) {
            s.seek(font_additional_addresses.get(0), SeekMode.SET);
            font_add_data = s.readBytes((int) (long) font_additional_size.get(0));
        }*/
        s.seek(hdr.getFontEndAddress(), SeekMode.SET);
        //here is offset [4]  - 856 ?        
        font_bin_info = new IggyFontBinInfo[(int) hdr.font_count];
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i] = new IggyFontBinInfo(s);
        }

        s.seek(hdr.getSequenceStartAddress1(), SeekMode.SET);
        sequence = new IggySequence(s);

        s.seek(hdr.getTypeFontsAddress(), SeekMode.SET);
        type_info = new IggyFontTypeInfo[(int) hdr.font_count];
        type_info_name = new String[(int) hdr.font_count];
        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i] = new IggyFontTypeInfo(s);
        }
        for (int i = 0; i < hdr.font_count; i++) {
            type_info_name[i] = type_info[i].readFontInfo(s);
        }

        s.seek(hdr.getDeclStringsAddress(), SeekMode.SET);
        decl_strings = new IggyDeclStrings(s);
        /*WriteDataStreamInterface outs = new TemporaryDataStream();
        writeToDataStream(outs);
        Helper.writeFile("d:\\Dropbox\\jpexs-laptop\\iggi\\parts\\swf_out.bin", outs.getAllBytes());*/
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        IggyIndexBuilder ib = s.getIndexing();
        hdr.writeToDataStream(s);
        s.writeWChar(name);
        s.pad8bytes();
        s.writeUI64(1);
        ib.write16bitArray(name.length() + 1);
        ib.writeTwoPaddingBytes();
        ib.write64bitPointerArray(64);
        for (int i = 0; i < font_data_addresses.size(); i++) {
            long offset = font_data_addresses.get(i) - s.position();
            s.writeUI64(offset);
        }
        for (int i = 0; i < text_data_addresses.size(); i++) {
            long offset = text_data_addresses.get(i) - s.position();
            s.writeUI64(offset);
        }
        s.writeUI64(1);
        if (additional_address != 0) {
            long ofs_additional = additional_address - s.position();
            s.writeUI64(ofs_additional);
        }

        while (s.position() < font_data_addresses.get(0)) {
            s.writeUI64(1);
        }
        for (int i = 0; i < fonts.size(); i++) {
            s.seek(font_data_addresses.get(i), SeekMode.SET);
            fonts.get(i).writeToDataStream(s);
        }
        for (int i = 0; i < texts.size(); i++) {
            s.seek(text_data_addresses.get(i), SeekMode.SET);
            texts.get(i).writeToDataStream(s);
        }

        if (hdr.getImported_guid() != 0) {
            for (int i = 0; i < add_fonts.size(); i++) {
                s.seek(add_font_data_addresses.get(i), SeekMode.SET);
                fonts.get(i).writeToDataStream(s);
            }
            for (int i = 0; i < add_texts.size(); i++) {
                s.seek(add_text_data_addresses.get(i), SeekMode.SET);
                texts.get(i).writeToDataStream(s);
            }
        }
        s.seek(hdr.getFontEndAddress(), SeekMode.SET);
        ib.writeConstLengthArray(IggyIndexBuilder.CONST_BIN_INFO_SIZE, hdr.font_count);
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i].writeToDataStream(s);
        }

        s.seek(hdr.getSequenceStartAddress1(), SeekMode.SET);
        sequence.writeToDataStream(s);

        ib.writeConstLengthArray(IggyIndexBuilder.CONST_TYPE_INFO_SIZE, hdr.font_count);
        s.seek(hdr.getTypeFontsAddress(), SeekMode.SET);
        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i].writeToDataStream(s);
        }

        for (int i = 0; i < hdr.font_count; i++) {
            ib.write16bitArray(type_info_name[i].length() + 1);
            type_info[i].writeFontInfo(type_info_name[i], s);
        }
        s.pad8bytes();
        ib.pad8bytes();
        s.seek(hdr.getDeclStringsAddress(), SeekMode.SET);
        decl_strings.writeToDataStream(s);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        return sb.toString();
    }

}
