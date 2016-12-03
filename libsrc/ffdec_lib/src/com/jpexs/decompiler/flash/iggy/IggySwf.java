package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
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

    Map<Integer, IggyFont> fonts;

    private IggyFlashHeader64 hdr;

    private byte[] allFontBytes;

    public IggySwf(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    private long font_data_addresses[];
    private long font_data_sizes[];
    private List<Long> text_addresses = new ArrayList<>();
    private List<Long> text_data_sizes = new ArrayList<>();
    private List<byte[]> text_data_bytes = new ArrayList<>();
    private byte font_add_data[];
    private List<Long> font_add_off = new ArrayList<>();
    private List<Long> font_add_size = new ArrayList<>();
    private FontBinInfo font_bin_info[];
    private IggySequence sequence;
    private IggyFontTypeInfo type_info[];
    private String type_info_name[];
    private IggyDeclStrings decl_strings;

    public IggyFlashHeader64 getHdr() {
        return hdr;
    }

    public void replaceFontTag(int fontIndex, IggyFont newFont) throws IOException {
        long newLen;
        byte newData[];
        try (WriteDataStreamInterface stream = new TemporaryDataStream()) {
            newFont.writeToDataStream(stream);
            newData = stream.getAllBytes();
            newLen = newData.length;
        }

        //FIXME
        Helper.writeFile("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_new\\font" + fontIndex + ".bin", newData);

        /* long oldLen = font_data_sizes[fontIndex];
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
        long curPos = s.position();
        long maxPos = hdr.getFontEndAddress();
        allFontBytes = s.readBytes((int) (maxPos - curPos));
        s.seek(curPos, SeekMode.SET);
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

        font_data_addresses = new long[(int) hdr.font_count];
        font_data_sizes = new long[(int) hdr.font_count];

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

        if (hdr.isImported()) { // tohle muze narusit iggy order, ale oni to pak stejne pocitaji dle infa 
            long additionalOffset = s.readUI64();
            font_add_off.add(additionalOffset + s.position() - 8);
            font_add_size.add(hdr.getFontEndAddress() - font_add_off.get(0));
            if (s.readUI8(font_add_off.get(0)) != 22) { //22 = Text
                for (int i = 0; i < text_addresses.size(); i++) {
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
        //here is offset [3]  - 744
        fonts = new HashMap<>();
        for (int i = 0; i < hdr.font_count; i++) {
            s.seek(font_data_addresses[i], SeekMode.SET);
            byte font_data[] = s.readBytes((int) font_data_sizes[i]);
            //FIXME
            Helper.writeFile("d:\\Dropbox\\jpexs-laptop\\iggi\\extraxtdir_orig\\font" + i + ".bin", font_data);
            s.seek(-font_data_sizes[i], SeekMode.CUR);
            IggyFont font = new IggyFont(s);
            fonts.put(i, font);
        }

        for (int i = 0; i < text_addresses.size(); i++) {
            s.seek(text_addresses.get(i), SeekMode.SET);
            byte text_data[] = s.readBytes((int) (long) text_data_sizes.get(i));
            text_data_bytes.add(text_data);
        }

        if (hdr.isImported()) {
            s.seek(font_add_off.get(0), SeekMode.SET);
            font_add_data = s.readBytes((int) (long) font_add_size.get(0));
        }
        s.seek(hdr.getFontEndAddress(), SeekMode.SET);
        //here is offset [4]  - 856 ?        
        font_bin_info = new FontBinInfo[(int) hdr.font_count];
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i] = new FontBinInfo(s);
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
        hdr.writeToDataStream(s);
        s.getIndexing().writeIndexUI8SkipTwice8(184, 25);
        s.writeWChar(name);
        s.pad8bytes();
        s.writeUI64(1);
        for (int i = 0; i < font_data_addresses.length; i++) {
            long offset = font_data_addresses[i] - s.position();
            s.writeUI64(offset);
        }
        for (int i = 0; i < text_addresses.size(); i++) {
            long offset = text_addresses.get(i) - s.position();
            s.writeUI64(offset);
        }
        if (hdr.font_count > 0) {
            while (s.position() < font_data_addresses[0]) {
                s.writeUI64(1);
            }
        }
        for (int i = 0; i < hdr.font_count; i++) {
            s.seek(font_data_addresses[i], SeekMode.SET);
            fonts.get(i).writeToDataStream(s);
        }
        for (int i = 0; i < text_data_bytes.size(); i++) {
            s.seek(text_addresses.get(i), SeekMode.SET);
            s.writeBytes(text_data_bytes.get(i));
        }

        if (hdr.isImported()) {
            s.seek(font_add_off.get(0), SeekMode.SET);
            s.writeBytes(font_add_data);
        }
        s.seek(hdr.getFontEndAddress(), SeekMode.SET);
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i].writeToDataStream(s);
        }
        s.seek(hdr.getSequenceStartAddress1(), SeekMode.SET);
        sequence.writeToDataStream(s);

        s.seek(hdr.getTypeFontsAddress(), SeekMode.SET);
        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i].writeToDataStream(s);
        }

        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i].writeFontInfo(type_info_name[i], s);
        }
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
