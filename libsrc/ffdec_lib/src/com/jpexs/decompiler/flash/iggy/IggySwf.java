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
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggySwf implements StructureInterface {

    final static int NO_OFFSET = 1;

    @IggyFieldType(value = DataType.wchar_t, count = 48)
    String name;

    private List<IggyFont> fonts = new ArrayList<>();
    // private List<Long> font_data_addresses = new ArrayList<>();
    private List<IggyFont> add_fonts = new ArrayList<>();
//    private List<Long> add_font_data_addresses = new ArrayList<>();

    private IggyFlashHeader64 hdr;

    public IggySwf(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    private List<IggyText> texts = new ArrayList<>();
    //private List<Long> text_data_addresses = new ArrayList<>();
    private List<IggyText> add_texts = new ArrayList<>();
    //private List<Long> add_text_data_addresses = new ArrayList<>();

    //private byte font_add_data[];
    //private List<Long> font_additional_size = new ArrayList<>();
    private IggyFontBinInfo font_bin_info[];
    private List<String> sequenceNames = new ArrayList<>();
    //private List<Long> sequenceValues = new ArrayList<>();

    private IggyFontTypeInfo type_info[];

    private String type_info_name[];

    private IggyDeclStrings decl_strings;

    private long ofs_additional;

    private long additional_address;

    public IggyFlashHeader64 getHdr() {
        return hdr;
    }

    public List<IggyFont> getFonts() {
        return fonts;
    }

    public List<IggyFont> getAddFonts() {
        return add_fonts;
    }

    public List<IggyText> getTexts() {
        return texts;
    }

    public List<IggyText> getAddTexts() {
        return add_texts;
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
                    //font_data_addresses.add(addr);
                    fonts.add(font);
                    break;
                case 6 /*TEXT*/:
                    IggyText text = new IggyText(s);
                    //text_data_addresses.add(addr);
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
                        //add_font_data_addresses.add(addr);
                        add_fonts.add(font);
                        break;
                    case 6 /*TEXT*/:
                        IggyText text = new IggyText(s);
                        //add_text_data_addresses.add(addr);
                        add_texts.add(text);
                        break;
                    default:
                        throw new RuntimeException("Unknown imported item kind: " + kind);
                }
            }
        }
        s.seek(hdr.getFontEndAddress(), SeekMode.SET);
        //here is offset [4]  - 856 ?
        font_bin_info = new IggyFontBinInfo[(int) hdr.font_count];
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i] = new IggyFontBinInfo(s);
        }

        sequenceNames = new ArrayList<>();

        long seq_addresses[] = new long[]{hdr.getSequenceStartAddress1(), hdr.getSequenceStartAddress2(), hdr.getSequenceStartAddress3()};
        long seq_name_addresses[] = new long[3];
        for (int i = 0; i < 3; i++) {
            if (seq_addresses[i] == 0) {
                seq_name_addresses[i] = 0;
                //0
            } else {
                s.seek(seq_addresses[i], SeekMode.SET);
                long ofs_seq_name = s.readUI64();
                seq_name_addresses[i] = ofs_seq_name == 1 ? 0 : ofs_seq_name + s.position() - 8;
                s.readUI64(); //is this crucial?
            }
        }

        for (int i = 0; i < 3; i++) {
            if (seq_name_addresses[i] > 0) {
                s.seek(seq_name_addresses[i], SeekMode.SET);
                sequenceNames.add(s.readWChar());
            } else {
                sequenceNames.add(null);
            }
        }
        s.pad8bytes();

        //sequence = new IggySequence(s);
        s.seek(hdr.getTypeFontsAddress(), SeekMode.SET);
        type_info = new IggyFontTypeInfo[(int) hdr.font_count];
        type_info_name = new String[(int) hdr.font_count];
        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i] = new IggyFontTypeInfo(s);
        }
        for (int i = 0; i < hdr.font_count; i++) {
            s.seek(type_info[i].getLocal_name_ofs_pos() + type_info[i].ofs_local_name, SeekMode.SET);
            type_info_name[i] = s.readWChar();
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
        long posBeforeOffsets = s.position();

        final int FILL_LATER = 1;

        List<Long> fontPosFillLater = new ArrayList<>();

        for (int i = 0; i < fonts.size(); i++) {
            fontPosFillLater.add(s.position());
            s.writeUI64(FILL_LATER);
        }
        List<Long> textPosFillLater = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            textPosFillLater.add(s.position());
            s.writeUI64(FILL_LATER);
        }
        s.writeUI64(1);

        long addPosFillLater = s.position();
        s.writeUI64(FILL_LATER);
        long posAfter = posBeforeOffsets + 64 * 8;
        long curPos = s.position();
        long numLeft = posAfter - curPos;
        long ofsLeft = numLeft / 8;
        for (int i = 0; i < ofsLeft - 1; i++) {
            s.writeUI64(FILL_LATER);
        }

        for (int i = 0; i < fonts.size(); i++) {
            s.setOlderOffsetToThisPos(fontPosFillLater.get(i));
            fonts.get(i).writeToDataStream(s);
        }
        for (int i = 0; i < texts.size(); i++) {
            s.setOlderOffsetToThisPos(textPosFillLater.get(i));
            texts.get(i).writeToDataStream(s);
        }

        if (!add_fonts.isEmpty() || !add_texts.isEmpty()) {
            s.setOlderOffsetToThisPos(addPosFillLater);

            List<Long> addFontPosFillLater = new ArrayList<>();

            for (int i = 0; i < add_fonts.size(); i++) {
                addFontPosFillLater.add(s.position());
                s.writeUI64(FILL_LATER);
            }
            List<Long> addTextPosFillLater = new ArrayList<>();
            for (int i = 0; i < add_texts.size(); i++) {
                addTextPosFillLater.add(s.position());
                s.writeUI64(FILL_LATER);
            }
            s.writeUI64(FILL_LATER);
            for (int i = 0; i < add_fonts.size(); i++) {
                s.setOlderOffsetToThisPos(addFontPosFillLater.get(i));
                add_fonts.get(i).writeToDataStream(s);
            }
            for (int i = 0; i < add_texts.size(); i++) {
                s.setOlderOffsetToThisPos(addTextPosFillLater.get(i));
                add_texts.get(i).writeToDataStream(s);
            }
        }
        s.setOlderOffsetToThisPos(hdr.getFont_end_ofs_pos());
        ib.writeConstLengthArray(IggyIndexBuilder.CONST_BIN_INFO_SIZE, hdr.font_count);
        for (int i = 0; i < hdr.font_count; i++) {
            font_bin_info[i].writeToDataStream(s);
        }

        long seq_ofs_pos[] = new long[]{hdr.getSequence_start1_ofs_pos(), hdr.getSequence_start2_ofs_pos(), hdr.getSequence_start3_ofs_pos()};
        long off_seq_expected[] = new long[]{hdr.off_sequence_start1, hdr.off_sequence_start2, hdr.off_sequence_start3};

        long seq_name_fill_later[] = new long[3];

        s.setOlderOffsetToThisPos(seq_ofs_pos[0]);
        s.writeUI64(1);
        s.writeUI64(1);
        ib.writeLengthCustom(16, new int[]{0}, new int[]{2});

        seq_name_fill_later[2] = s.position();
        s.setOlderOffsetToThisPos(seq_ofs_pos[2]);
        s.writeUI64(FILL_LATER);
        s.writeUI64(0);
        ib.writeConstLength(IggyIndexBuilder.CONST_SEQUENCE_SIZE);
        for (int i = 0; i < 3; i++) {
            if (sequenceNames.get(i) != null) {
                s.setOlderOffsetToThisPos(seq_name_fill_later[i]);
                ib.write16bitArray(sequenceNames.get(i).length() + 1);
                s.writeWChar(sequenceNames.get(i));
            }
        }
        s.setOlderOffsetToThisPos(hdr.getSequence_end_ofs_pos());
        s.pad8bytes();
        ib.pad8bytes();

        ib.writeConstLengthArray(IggyIndexBuilder.CONST_TYPE_INFO_SIZE, hdr.font_count);
        s.setOlderOffsetToThisPos(hdr.getType_fonts_ofs_pos());
        //s.seek(hdr.getTypeFontsAddress(), SeekMode.SET);
        for (int i = 0; i < hdr.font_count; i++) {
            type_info[i].writeToDataStream(s);
        }

        for (int i = 0; i < hdr.font_count; i++) {
            ib.write16bitArray(type_info_name[i].length() + 1);
            s.setOlderOffsetToThisPos(type_info[i].getLocal_name_ofs_pos());
            s.writeWChar(type_info_name[i]);
        }
        s.pad8bytes();
        ib.pad8bytes();

        s.setOlderOffsetToThisPos(hdr.getDecl_strings_ofs_pos());
        //s.seek(hdr.getDeclStringsAddress(), SeekMode.SET);
        decl_strings.writeToDataStream(s);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        return sb.toString();
    }

    public IggyDeclStrings getDeclStrings() {
        return decl_strings;
    }
}
