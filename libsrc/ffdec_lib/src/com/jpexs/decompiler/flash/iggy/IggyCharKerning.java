package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharKerning implements StructureInterface {

    public static final int STRUCT_SIZE = 6;

    private long kernCount;
    List<Character> charsA;
    List<Character> charsB;
    List<Short> kerningOffsets;

    public long getKernCount() {
        return kernCount;
    }

    public List<Character> getCharsA() {
        return charsA;
    }

    public List<Character> getCharsB() {
        return charsB;
    }

    public List<Short> getKerningOffsets() {
        return kerningOffsets;
    }

    public IggyCharKerning(List<Character> charsA, List<Character> charsB, List<Short> kerningOffsets) {
        if ((charsA.size() != charsB.size()) || (charsB.size() != kerningOffsets.size())) {
            throw new RuntimeException("sizes of charsA, charsB and offsets must match");
        }
        this.kernCount = charsA.size();
        this.charsA = charsA;
        this.charsB = charsB;
        this.kerningOffsets = kerningOffsets;
    }

    public IggyCharKerning(ReadDataStreamInterface stream, long kernCount) throws IOException {
        this.kernCount = kernCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        charsA = new ArrayList<>();
        charsB = new ArrayList<>();
        kerningOffsets = new ArrayList<>();
        for (int i = 0; i < kernCount; i++) {
            charsA.add((char) stream.readUI16());
            charsB.add((char) stream.readUI16());
            kerningOffsets.add((short) stream.readUI16());
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        for (int i = 0; i < kernCount; i++) {
            stream.writeUI16(charsA.get(i));
            stream.writeUI16(charsB.get(i));
            stream.writeUI16(kerningOffsets.get(i));
        }
    }

}
