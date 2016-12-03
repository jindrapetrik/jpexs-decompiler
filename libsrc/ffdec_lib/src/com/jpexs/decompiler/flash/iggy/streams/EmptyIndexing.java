package com.jpexs.decompiler.flash.iggy.streams;

/**
 *
 * @author JPEXS
 */
public class EmptyIndexing implements IndexingDataStreamInterface {

    @Override
    public void writeIndexToTable(int val0to255) {

    }

    @Override
    public long writeIndexFromTable(int tableIndex0to127) {
        return 0;
    }

    @Override
    public long writeIndex40(int num1to40, int countUI8) {
        return 0;
    }

    @Override
    public long writeIndexMultiply2(int num0to15) {
        return 0;
    }

    @Override
    public long writeIndexPtr(boolean is64, long cnt) {
        return 0;
    }

    @Override
    public long writeIndex16bit(long cnt) {
        return 0;
    }

    @Override
    public long writeIndex32bit(long cnt) {
        return 0;
    }

    @Override
    public long writeIndex64bit(long cnt) {
        return 0;
    }

    @Override
    public long writeIndexSkip1() {
        return 0;
    }

    @Override
    public long writeIndexUI8SkipTwice8(int ofs, int skipTwice) {
        return 0;
    }

    @Override
    public long writeIndexUI8Positive(int val) {
        return 0;
    }

    @Override
    public long writeIndexUI32(long offset) {
        return 0;
    }

    @Override
    public long writeIndex(int code, boolean is64, long val, long val2) {
        return 0;
    }

    @Override
    public byte[] getIndexTableBytes() {
        return new byte[0];
    }

    @Override
    public byte[] getIndexBytes() {
        return new byte[0];
    }

}
