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
    private List<Long> tagIds;

    @IggyArrayFieldType(value = DataType.uint16_t)
    private List<Long> tagIdsExtraInfo;

    public IggyNameAndTagList(String name, List<Long> tagIds, List<Long> tagIdsExtendedInfo) {
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
            long tagType = stream.readUI32() & 0xffffffffL;
            if (tagType == 0) {
                break;
            }
            long tagLength = stream.readUI32();
            tagIds.add(tagType);
            tagIdsExtraInfo.add(tagLength);
        }
    }

    public String getName() {
        return name;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public List<Long> getTagIdsExtraInfo() {
        return tagIdsExtraInfo;
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\r\n");
        sb.append("name ").append(name).append("\r\n");
        sb.append("tags:").append("\r\n");
        for (int i = 0; i < tagIds.size(); i++) {
            sb.append("tag ").append(String.format("%08X", tagIds.get(i))).append(" extra ").append(tagIdsExtraInfo.get(i)).append("\r\n");
        }
        sb.append("]");
        return sb.toString();
    }

}
