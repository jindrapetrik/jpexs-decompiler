package com.jpexs.decompiler.flash.iggy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharKerning implements StructureInterface {

    private long kernCount;
    List<Character> charsA;
    List<Character> charsB;
    List<Short> kerningOffsets;
    long pad;

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

    public IggyCharKerning(AbstractDataStream stream, long kernCount) throws IOException {
        this.kernCount = kernCount;
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        charsA = new ArrayList<>();
        charsB = new ArrayList<>();
        kerningOffsets = new ArrayList<>();
        for (int i = 0; i < kernCount; i++) {
            charsA.add((char) stream.readUI16());
            charsB.add((char) stream.readUI16());
            kerningOffsets.add((short) stream.readUI16());
        }
        pad = stream.readUI32();
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
