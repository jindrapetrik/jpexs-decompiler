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

import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 68-byte edit-text object from the x86 SWF decoder (sub_1000CB90).
 */
public final class IggyText32 extends IggyText {

    public IggyText32(ReadDataStreamInterface s) throws IOException {
        readFromDataStream(s);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        long start = s.position();
        IggyLayout.checkRange(s, start, 68);
        MemoryDataStream h = new MemoryDataStream(s.readBytes(68));
        type = h.readUI16();
        textIndex = h.readUI16();
        readCommonHeader(h, false);
        par1 = h.readFloat();
        par2 = h.readFloat();
        par3 = h.readFloat();
        par4 = h.readFloat();
        enum_hex = h.readUI16();
        fontIndex = h.readUI16();
        fontClassNameOffset = h.readUI32();
        readTextStyle(h);
        s.seek(start + 64, SeekMode.SET);
        long text = IggyLayout.readPointer(s, false);
        initialText = null;
        if (text != 0) {
            IggyLayout.checkRange(s, text, 2);
            s.seek(text, SeekMode.SET);
            initialText = s.readWChar();
        }
        readClassNames(s, start);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        long start = s.position();
        s.writeUI16(type);
        s.writeUI16(textIndex);
        s.writeUI32(unknownHeaderWord);
        s.writeUI32(symbolClassNameOffset);
        s.writeUI32(runtimeClassPointer);
        s.writeFloat(par1);
        s.writeFloat(par2);
        s.writeFloat(par3);
        s.writeFloat(par4);
        s.writeUI16(enum_hex);
        s.writeUI16(fontIndex);
        s.writeUI32(fontClassNameOffset);
        writeTextStyle(s);
        s.seek(start + 64, SeekMode.SET);
        IggyLayout.writePointer(s, false, initialText == null ? 0 : start + 68);
        List<IggyFixup.Field> fields = new ArrayList<>();
        fields.add(new IggyFixup.Field(2, 4));
        for (int offset : new int[]{16, 20, 24, 28}) {
            fields.add(new IggyFixup.Field(offset, 5));
        }
        fields.add(new IggyFixup.Field(32, 4));
        fields.add(new IggyFixup.Field(34, 4));
        fields.add(new IggyFixup.Field(8, 2));
        fields.add(new IggyFixup.Field(36, 2));
        for (int offset = 44; offset <= 54; offset += 2) {
            fields.add(new IggyFixup.Field(offset, 4));
        }
        fields.add(new IggyFixup.Field(64, 2));
        if (initialText != null) {
            s.writeWChar(initialText);
            for (int i = 0; i <= initialText.length(); i++) {
                fields.add(new IggyFixup.Field(68 + 2L * i, 4));
            }
        }
        long namesStart = s.position();
        writeChangedClassNames(s, start, false);
        for (long offset = namesStart - start; offset < s.position() - start; offset += 2) {
            fields.add(new IggyFixup.Field(offset, 4));
        }
        IggyFixup.writeFields(s.getIndexing(), fields, s.position() - start);
    }
}
