package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.math.BigInteger;

/**
 *
 * @author JPEXS
 *
 * little endian all
 *
 * Based of works of somebody called eternity.
 */
public class IggyHeader implements StructureInterface {

    public static long MAGIC = 0xED0A6749;

    //Must be 0xED0A6749
    @IggyFieldType(DataType.uint32_t)
    public long magic = MAGIC;

    //Assume 0x900
    @IggyFieldType(DataType.uint32_t)
    public long version;

    //Assuming: 1
    @IggyFieldType(value = DataType.uint8_t)
    public int platform1;

    //32/64
    @IggyFieldType(value = DataType.uint8_t)
    public int platform2_bittness;

    //Assuming: 1
    @IggyFieldType(value = DataType.uint8_t)
    public int platform3;

    //Usually: 3
    @IggyFieldType(value = DataType.uint8_t)
    public int platform4;

    //flags for platform 64?
    @IggyFieldType(DataType.uint32_t)
    public long unk_0C;

    @IggyArrayFieldType(value = DataType.uint8_t, count = 12)
    public byte[] reserved;

    @IggyFieldType(value = DataType.uint32_t)
    public long num_subfiles;

    public IggyHeader(AbstractDataStream stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggyHeader(long version, int platform1, int platform2_bittness, int platform3, int platform4, long unk_0C, byte[] reserved, long num_subfiles) {
        this.version = version;
        this.platform1 = platform1;
        this.platform2_bittness = platform2_bittness;
        this.platform3 = platform3;
        this.platform4 = platform4;
        this.unk_0C = unk_0C;
        this.reserved = reserved;
        this.num_subfiles = num_subfiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("version: ").append(version).append(", ");
        sb.append("platform: ").append(platform1).append(" ").append(platform2_bittness).append(" ").append(platform3).append(" ").append(platform4).append(", ");
        sb.append("unk_0C: ").append(String.format("%08X", unk_0C)).append(", ");
        sb.append("reserved: 12 bytes").append(", ");
        sb.append("num_subfiles: ").append(num_subfiles);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        magic = stream.readUI32();
        if (magic != IggyHeader.MAGIC) {
            throw new IOException("Invalid Iggy file");
        }
        version = stream.readUI32();
        platform1 = stream.readUI8();
        platform2_bittness = stream.readUI8();
        platform3 = stream.readUI8();
        platform4 = stream.readUI8();
        unk_0C = stream.readUI32();
        reserved = stream.readBytes(12);
        num_subfiles = stream.readUI32();
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean is64() {
        return platform2_bittness == 64;
    }

}
