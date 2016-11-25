package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyCharOffset implements StructureInterface {

    private static Logger LOGGER = Logger.getLogger(IggyCharOffset.class.getName());

    @IggyFieldType(DataType.uint64_t)
    long zero;
    @IggyFieldType(DataType.uint16_t)
    int ischar1;
    @IggyFieldType(DataType.uint16_t)
    int ischar2;
    @IggyFieldType(DataType.uint32_t)
    long zero2;
    @IggyFieldType(DataType.uint16_t)
    int xscale;
    @IggyFieldType(DataType.uint16_t)
    int yscale;
    @IggyFieldType(DataType.uint32_t)
    long zero3;
    @IggyFieldType(DataType.uint64_t)
    long offset;

    public IggyCharOffset(AbstractDataStream stream) throws IOException {
        readFromDataStream(stream);
    }

    public IggyCharOffset(long zero, int ischar1, int ischar2, long zero2, int xscale, int yscale, long zero3, long offset) {
        this.zero = zero;
        this.ischar1 = ischar1;
        this.ischar2 = ischar2;
        this.zero2 = zero2;
        this.xscale = xscale;
        this.yscale = yscale;
        this.zero3 = zero3;
        this.offset = offset;
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        zero = stream.readUI64();
        ischar1 = stream.readUI16();
        ischar2 = stream.readUI16();
        zero2 = stream.readUI32();
        xscale = stream.readUI16();
        yscale = stream.readUI16();
        zero3 = stream.readUI32();
        long cur_position = stream.position();
        long relative_offset = stream.readUI64();
        if (ischar1 > 0) {
            offset = cur_position + relative_offset;
        } else {
            offset = 0;
            LOGGER.finer(String.format("Empty char"));
        }
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getZero() {
        return zero;
    }

    public int getIschar1() {
        return ischar1;
    }

    public int getIschar2() {
        return ischar2;
    }

    public long getZero2() {
        return zero2;
    }

    public int getXscale() {
        return xscale;
    }

    public int getYscale() {
        return yscale;
    }

    public long getZero3() {
        return zero3;
    }

}
