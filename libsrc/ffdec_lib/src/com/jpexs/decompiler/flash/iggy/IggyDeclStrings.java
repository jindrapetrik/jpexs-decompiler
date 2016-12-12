package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggyDeclStrings implements StructureInterface {

    @IggyFieldType(DataType.uint64_t)
    long one;
    @IggyFieldType(DataType.uint32_t)
    long size;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 3)
    byte xxx[];
    @IggyArrayFieldType(value = DataType.uint8_t, countField = "size")
    byte data[];
    byte padd[];
    @IggyFieldType(DataType.uint64_t)
    long one2;
    @IggyFieldType(DataType.uint64_t)
    long zero;

    public IggyDeclStrings(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        one = s.readUI64();
        size = s.readUI32();
        xxx = s.readBytes(3);
        data = s.readBytes((int) size);
        if ((15 + size) % 8 != 0) {
            padd = s.readBytes((int) (((15 + size) / 8 + 1) * 8 - 15 - size));
        } else {
            padd = new byte[0];
        }
        one = s.readUI64();
        if (one != 1) {
            throw new IOException("Wrong iggy font format (declend)!");
        }
        zero = s.readUI64();
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        IggyIndexBuilder ib = s.getIndexing();
        s.writeUI64(one);
        s.writeUI32(size);
        s.writeBytes(xxx);
        ib.writeLengthCustom(15, new int[]{0x00, 0x08}, new int[]{2, 5});
        ib.writeLengthUI32(size);
        s.writeBytes(data);
        ib.writeConstLength(IggyIndexBuilder.CONST_SEQUENCE_SIZE);
        s.writeBytes(padd);
        s.writeUI64(one);
        s.writeUI64(zero);
    }

}
