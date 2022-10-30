package gnu.jpdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class PDFEmbeddedFont extends PDFFont {

    private final String name;
    private final String font;
    private final String descendantFont;
    private final String toUnicode;

    public PDFEmbeddedFont(String name, String font, int style, String descendantFont, String toUnicode) {
        super(name, "/TrueType", font, style);
        this.name = name;
        this.font = font;
        this.descendantFont = descendantFont;
        this.toUnicode = toUnicode;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);
        os.write("/Subtype /Type0\n".getBytes("UTF-8"));
        os.write(("/BaseFont " + font + "\n").getBytes("UTF-8"));
        os.write(("/Name " + name + "\n").getBytes("UTF-8"));
        os.write("/Encoding /Identity-H\n".getBytes("UTF-8"));
        os.write(("/ToUnicode " + toUnicode + "\n").getBytes("UTF-8"));
        os.write(("/DescendantFonts [" + descendantFont + "]\n").getBytes("UTF-8"));
        writeEnd(os);
    }

}
