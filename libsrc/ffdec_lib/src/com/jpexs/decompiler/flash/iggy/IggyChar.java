package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class IggyChar implements StructureInterface {

    private static Logger LOGGER = Logger.getLogger(IggyChar.class.getName());

    //if offset = 1, zadnychar, 1 byte[1]
    @IggyFieldType(DataType.float_t)
    float minx;
    @IggyFieldType(DataType.float_t)
    float miny;
    @IggyFieldType(DataType.float_t)
    float maxx;
    @IggyFieldType(DataType.float_t)
    float maxy;
    @IggyFieldType(DataType.uint64_t)
    long unk; // stejny vetsinou - napr. 48
    @IggyFieldType(DataType.uint64_t)
    long count;
    @IggyFieldType(DataType.uint64_t)
    long one; // 1
    @IggyFieldType(DataType.uint64_t)
    long one2; // 1
    @IggyFieldType(DataType.uint64_t)
    long one3; // 1
    @IggyFieldType(DataType.uint32_t)
    long one4; // 1
    @IggyFieldType(DataType.uint32_t)
    long two1; // 2

    List<IggyCharNode> nodes;

    private long offset;

    public IggyChar(AbstractDataStream stream, long offset) throws IOException {
        this.offset = offset;
        readFromDataStream(stream);
    }

    public IggyChar(float minx, float miny, float maxx, float maxy, long unk, long count, long one, long one2, long one3, long one4, long two1, List<IggyCharNode> nodes) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.unk = unk;
        this.count = count;
        this.one = one;
        this.one2 = one2;
        this.one3 = one3;
        this.one4 = one4;
        this.two1 = two1;
        this.nodes = nodes;
    }

    @Override
    public void readFromDataStream(AbstractDataStream s) throws IOException {
        s.seek(offset, SeekMode.SET);
        minx = s.readFloat();
        miny = s.readFloat();
        maxx = s.readFloat();
        maxy = s.readFloat();
        unk = s.readUI64();
        count = s.readUI64();
        one = s.readUI64();
        one2 = s.readUI64();
        one3 = s.readUI64();
        one4 = s.readUI32();
        two1 = s.readUI32();

        if ((one != 1) | (one2 != 1) | (one3 != 1) | (one4 != 1) | (two1 != 2)) {
            LOGGER.info(String.format("Unique header at pos %d\n", offset));
        }

        nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nodes.add(new IggyCharNode(s, i == 0));
        }

    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
