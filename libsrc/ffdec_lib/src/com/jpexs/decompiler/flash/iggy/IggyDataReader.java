package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class IggyDataReader implements StructureInterface {

    final static int NO_OFFSET = 1;

    @IggyFieldType(value = DataType.widechar_t, count = 48)
    String name;

    List<IggyFontData> fontDatas;
    List<IggyFontInfo> fontInfos;

    private IggyFlashHeader64 header;
    private Map<Long, Long> sizesOfOffsets;

    public IggyDataReader(IggyFlashHeader64 header, AbstractDataStream stream, List<Long> offsets) throws IOException {
        this.header = header;
        sizesOfOffsets = new HashMap<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            sizesOfOffsets.put(offsets.get(i), offsets.get(i + 1) - offsets.get(i));
        }
        sizesOfOffsets.put(offsets.get(offsets.size() - 1), 0L); //Last offset has 0L length?
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream stream) throws IOException {
        StringBuilder nameBuilder = new StringBuilder();
        int charCnt = 0;
        do {
            char c = (char) stream.readUI16();
            charCnt++;
            if (c == '\0') {
                break;
            }
            nameBuilder.append(c);
        } while (true);
        stream.seek(48 - charCnt * 2, SeekMode.CUR);
        name = nameBuilder.toString();

        stream.readUI64(); //pad 1

        long[] font_main_offsets = new long[(int) header.font_count];
        for (int i = 0; i < header.font_count; i++) {
            font_main_offsets[i] = stream.position() + stream.readUI64();
        }
        long[] font_info_offsets = new long[(int) header.font_count];
        for (int i = 0; i < header.font_count; i++) {
            long pos = stream.position();
            long offset = stream.readUI64();
            font_info_offsets[i] = offset == NO_OFFSET ? NO_OFFSET : pos + offset;
        }
        long pad_len = 840 - stream.position();
        stream.seek(pad_len, SeekMode.CUR);

        /*List<ByteArrayDataStream> fontMainStreams = new ArrayList<>();

        for (int i = 0; i < header.font_count; i++) {
            long fontMainOffset = font_main_offsets[i];
            //long fontMainSize = sizesOfOffsets.get(font_main_offsets[i]);
            //stream.seek(fontMainOffset, SeekMode.SET);
            //byte[] fontMainData = stream.readBytes((int) fontMainSize);
            //fontMainStreams.add(new ByteArrayDataStream(fontMainData, stream.is64()));
        }*/
        List<ByteArrayDataStream> fontInfoStreams = new ArrayList<>();

        for (int i = 0; i < header.font_count; i++) {
            long fontInfoOffset = font_info_offsets[i];
            if (fontInfoOffset == NO_OFFSET) {
                fontInfoStreams.add(null);
            } else {
                long fontInfoSize = sizesOfOffsets.get(font_info_offsets[i]);

                stream.seek(fontInfoOffset, SeekMode.SET);
                byte[] fontInfoData = stream.readBytes((int) fontInfoSize);

                fontInfoStreams.add(new ByteArrayDataStream(fontInfoData, stream.is64()));
            }
        }

        fontDatas = new ArrayList<>();
        fontInfos = new ArrayList<>();
        for (int i = 0; i < header.font_count; i++) {
            stream.seek(font_main_offsets[i], SeekMode.SET);
            IggyFontData part = new IggyFontData(stream);
            fontDatas.add(part);
        }
    }

    public String getName() {
        return name;
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
        return sb.toString();
    }

}
