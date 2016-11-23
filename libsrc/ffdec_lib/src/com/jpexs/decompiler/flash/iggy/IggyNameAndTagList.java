package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyNameAndTagList implements StructureInterface {

    @IggyFieldType(value = DataType.widechar_t)
    private String name;

    @IggyArrayFieldType(value = DataType.ubits, count = 10)
    private List<Integer> tagIds;

    @IggyArrayFieldType(value = DataType.uint16_t)
    private List<Long> tagIdsExtraInfo;

    public IggyNameAndTagList(String name, List<Integer> tagIds, List<Long> tagIdsExtendedInfo) {
        this.name = name;
        this.tagIds = tagIds;
        this.tagIdsExtraInfo = tagIdsExtendedInfo;
    }

    public IggyNameAndTagList(AbstractDataStream stream) throws IOException {
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        StringBuilder nameBuilder = new StringBuilder();
        do {
            char c = (char) stream.readUI16();
            if (c == '\0') {
                break;
            }
            nameBuilder.append(c);
        } while (true);
        name = nameBuilder.toString();

        tagIds = new ArrayList<>();
        tagIdsExtraInfo = new ArrayList<>();
        while (true) {
            long typeLen = stream.readUI32();
            if (typeLen == 0) {
                break;
            }
            long tagLength = stream.readUI32();
            int tagType = (int) ((typeLen >>> 6) + 10) & 0x3FF;
            tagIds.add(tagType);
            tagIdsExtraInfo.add(tagLength);
        }
    }

    public String getName() {
        return name;
    }

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public List<Long> getTagIdsExtraInfo() {
        return tagIdsExtraInfo;
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
