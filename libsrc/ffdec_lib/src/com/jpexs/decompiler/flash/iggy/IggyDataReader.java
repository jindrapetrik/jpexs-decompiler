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

    Map<Integer, IggyFont> fonts;
    Map<Integer, IggyText> texts;
    Map<Integer, Integer> text2Font;

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

        long[] fontOffsets = new long[(int) header.font_count];
        for (int i = 0; i < header.font_count; i++) {
            fontOffsets[i] = stream.position() + stream.readUI64();
        }
        List<Long> textOffsets = new ArrayList<>();

        while (true) {
            long val = stream.readUI64();
            if (val == 1) {
                break;
            }
            textOffsets.add(stream.position() + val);
        }

        //long pad_len = 840 - stream.position();
        stream.seek(840, SeekMode.SET);

        Long lastOffset = null;
        texts = new HashMap<>();
        text2Font = new HashMap<>();
        long textDataSizes[] = new long[(int) header.font_count];

        long offsetAfterTexts = header.as3_section_offset;

        for (int i = 0; i < textOffsets.size(); i++) {
            long textOffset = textOffsets.get(i);
            if (lastOffset == null) {
                lastOffset = textOffset;
            } else {
                textDataSizes[i - 1] = textOffset - lastOffset;
            }
        }
        if (lastOffset != null) {
            textDataSizes[(int) header.font_count - 1] = offsetAfterTexts - lastOffset;
        }

        for (int textIndex = 0; textIndex < textOffsets.size(); textIndex++) {
            long textOffset = textOffsets.get(textIndex);
            stream.seek(textOffset, SeekMode.SET);
            byte[] textData = stream.readBytes((int) textDataSizes[textIndex]);
            IggyText text = new IggyText(new ByteArrayDataStream(textData, stream.is64()));
            text2Font.put(textIndex, text.fontIndex);
            texts.put(textIndex, text);
        }
        fonts = new HashMap<>();
        for (int fontIndex = 0; fontIndex < header.font_count; fontIndex++) {
            stream.seek(fontOffsets[fontIndex], SeekMode.SET);
            IggyFont font = new IggyFont(stream);
            fonts.put(fontIndex, font);
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
