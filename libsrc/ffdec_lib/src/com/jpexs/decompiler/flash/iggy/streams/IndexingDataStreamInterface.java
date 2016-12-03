package com.jpexs.decompiler.flash.iggy.streams;

/**
 *
 * @author JPEXS
 */
public interface IndexingDataStreamInterface {

    public void writeIndexToTable(int val0to255);

    public long writeIndexFromTable(int tableIndex0to127);

    public long writeIndex40(int num1to40, int countUI8);

    public long writeIndexMultiply2(int num0to15);

    public long writeIndexPtr(boolean is64, long cnt);

    public long writeIndex16bit(long cnt);

    public long writeIndex32bit(long cnt);

    public long writeIndex64bit(long cnt);

    public long writeIndexSkip1();

    public long writeIndexUI8SkipTwice8(int ofs, int skipTwice);

    public long writeIndexUI8Positive(int val);

    public long writeIndexUI32(long offset);

    public long writeIndex(int code, boolean is64, long val, long val2);

    public byte[] getIndexTableBytes();

    public byte[] getIndexBytes();
}
