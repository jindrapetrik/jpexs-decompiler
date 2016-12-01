package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggySequence implements StructureInterface {

    @IggyArrayFieldType(value = DataType.uint64_t, count = 2)
    public long onepadd[];

    @IggyFieldType(DataType.uint64_t)
    public long local_seq_offset;

    @IggyFieldType(DataType.uint64_t)
    public long zero;

    @IggyFieldType(DataType.wchar_t)
    public String sequence_name;

    @IggyArrayFieldType(DataType.uint8_t)
    byte pad[];

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
        local_seq_offset = s.readUI64();
        zero = s.readUI64();
        StringBuilder sequence_name_builder = new StringBuilder();
        for (int i = 0; i < local_seq_offset; i++) {
            sequence_name_builder.append((char) s.readUI16());
        }
        sequence_name = sequence_name_builder.toString();
        int pad8 = 8 - (int) (s.position() % 8);
        if (pad8 < 8) {
            pad = s.readBytes(pad8);
        } else {
            pad = new byte[0];
        }
        zero2 = s.readUI64(); //zero2
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        for (int i = 0; i < onepadd.length; i++) {
            s.writeUI64(onepadd[i]);
        }
        s.writeUI64(local_seq_offset);
        s.writeUI64(zero);
        for (int i = 0; i < sequence_name.length(); i++) {
            s.writeUI16(sequence_name.charAt(i));
        }
        s.writeBytes(pad);
        s.writeUI64(zero2);
    }
}
