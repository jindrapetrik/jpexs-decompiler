package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.annotations.IggyArrayFieldType;
import com.jpexs.decompiler.flash.iggy.annotations.IggyFieldType;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class IggyText implements StructureInterface {

    public static final int ID = 0xFF06;

    @IggyFieldType(DataType.uint16_t)
    int type; // Tag type
    @IggyFieldType(DataType.uint16_t)
    int textIndex;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 28)
    byte zeroone[];
    @IggyFieldType(DataType.float_t)
    float par1;
    @IggyFieldType(DataType.float_t)
    float par2;
    @IggyFieldType(DataType.float_t)
    float par3;
    @IggyFieldType(DataType.float_t)
    float par4;
    @IggyFieldType(DataType.uint16_t)
    int enum_hex;

    //Guessed
    boolean hasText;
    boolean wordWrap;
    boolean multiline;
    boolean password;
    boolean readOnly;
    boolean hasTextColor;
    boolean hasMaxLength;
    boolean hasFont;
    boolean hasFontClass;
    boolean autosize;
    boolean hasLayout;
    boolean noSelect;
    boolean border;
    boolean wasStatic;
    boolean html;
    boolean useOutlines;

    @IggyFieldType(DataType.uint16_t)
    int fontIndex;
    @IggyFieldType(DataType.uint32_t)
    long zero;
    @IggyFieldType(DataType.uint64_t)
    long one;
    @IggyArrayFieldType(value = DataType.uint8_t, count = 32)
    byte[] some; // same for different fonts
    @IggyArrayFieldType(value = DataType.widechar_t)
    String initialText; //till end of info file?

    public IggyText(int type, int order_in_iggy_file, byte[] zeroone, float par1, float par2, float par3, float par4, int enum_hex, int for_which_font_order_in_iggyfile, long zero, long one, byte[] some, long offset_of_name, String name) {
        this.type = type;
        this.textIndex = order_in_iggy_file;
        this.zeroone = zeroone;
        this.par1 = par1;
        this.par2 = par2;
        this.par3 = par3;
        this.par4 = par4;
        this.enum_hex = enum_hex;
        this.fontIndex = for_which_font_order_in_iggyfile;
        this.zero = zero;
        this.one = one;
        this.some = some;
        this.initialText = name;
    }

    public IggyText(AbstractDataStream stream) throws IOException {
        this.readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(AbstractDataStream s) throws IOException {

        type = s.readUI16();
        //characterId - iggy Id
        textIndex = s.readUI16();
        zeroone = s.readBytes(28);

        //bounds?:
        par1 = s.readFloat();
        par2 = s.readFloat();
        par3 = s.readFloat();
        par4 = s.readFloat();

        //flags
        enum_hex = s.readUI16();

        int en = enum_hex;

        //guessing - it could be like DefineEditText?...
        hasText = ((en >> 0) & 1) == 1;
        wordWrap = ((en >> 1) & 1) == 1;
        multiline = ((en >> 2) & 1) == 1;
        password = ((en >> 3) & 1) == 1;
        readOnly = ((en >> 4) & 1) == 1;
        hasTextColor = ((en >> 5) & 1) == 1;
        hasMaxLength = ((en >> 6) & 1) == 1;
        hasFont = ((en >> 7) & 1) == 1;
        hasFontClass = ((en >> 8) & 1) == 1;
        autosize = ((en >> 9) & 1) == 1;
        hasLayout = ((en >> 10) & 1) == 1;
        noSelect = ((en >> 11) & 1) == 1;
        border = ((en >> 12) & 1) == 1;
        wasStatic = ((en >> 13) & 1) == 1;
        html = ((en >> 14) & 1) == 1;
        useOutlines = ((en >> 15) & 1) == 1;

        //if hasFont?
        fontIndex = s.readUI16(); //fontId
        //if hasFontClass - readString?
        //if hasFont || hasFontClass - readFontHeight?
        //if hasTextColor....?
        zero = s.readUI32();
        one = s.readUI64(); //01CB FF33 3333
        some = s.readBytes(32); // [6] => 40, [24] => 8
        StringBuilder textBuilder = new StringBuilder();
        do {
            char c = (char) s.readUI16();
            if (c == '\0') {
                break;
            }
            textBuilder.append(c);
        } while (true);
        initialText = textBuilder.toString();
    }

    @Override
    public void writeToDataStream(AbstractDataStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getType() {
        return type;
    }

    public int getTextIndex() {
        return textIndex;
    }

    public byte[] getZeroone() {
        return zeroone;
    }

    public float getPar1() {
        return par1;
    }

    public float getPar2() {
        return par2;
    }

    public float getPar3() {
        return par3;
    }

    public float getPar4() {
        return par4;
    }

    public int getEnum_hex() {
        return enum_hex;
    }

    public int getFontIndex() {
        return fontIndex;
    }

    public long getZero() {
        return zero;
    }

    public long getOne() {
        return one;
    }

    public byte[] getSome() {
        return some;
    }

    public String getInitialText() {
        return initialText;
    }

}
