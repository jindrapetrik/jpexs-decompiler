package com.jpexs.decompiler.flash.iggy;

import static com.jpexs.decompiler.flash.iggy.IggyFontBinInfo.STRUCT_SIZE;
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
public class IggySequence implements StructureInterface {

    public static final int STRUCT_SIZE = 16;

    @IggyArrayFieldType(value = DataType.uint64_t, count = 2)
    public long onepadd[];

    @IggyFieldType(DataType.uint64_t)
    public long local_seq_offset;

    @IggyFieldType(DataType.uint64_t)
    public long zero;

    @IggyFieldType(DataType.wchar_t)
    public String sequence_name;

    @IggyFieldType(DataType.uint64_t)
    public long zero2;

    //       wchar_t sequencname[(sequenceendaddress-sequencestartaddress-localseqoffset)/2];
//    if((sequenceendaddress-sequencestartaddress)%8!=0) byte padd[((sequenceendaddress-sequencestartaddress)/8+1)*8-(sequenceendaddress-sequencestartaddress)]; 
    public IggySequence(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        onepadd = new long[2];
        for (int i = 0; i < onepadd.length; i++) {
            onepadd[i] = s.readUI64();
        }
        //sequence start
        local_seq_offset = s.readUI64();
        zero = s.readUI64();
        sequence_name = s.readWChar();
        s.pad8bytes();
        zero2 = s.readUI64(); //zero2
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        IggyIndexBuilder ib = s.getIndexing();
        ib.writeLengthCustom(16, new int[]{0x00}, new int[]{2});
        for (int i = 0; i < onepadd.length; i++) {
            s.writeUI64(onepadd[i]);
        }
        ib.writeConstLength(IggyIndexBuilder.CONST_SEQUENCE_SIZE);
        s.writeUI64(local_seq_offset);
        s.writeUI64(zero);
        ib.write16bitArray(sequence_name.length() + 1);
        s.writeWChar(sequence_name);
        s.pad8bytes();
        ib.pad8bytes();
        s.writeUI64(zero2);
    }
}
