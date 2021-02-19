package gnu.jpdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PDFEmbeddedFont extends PDFFont {

    private String name;
    private String font;
    private String type;
    private final String descendantFont;
    private final String toUnicode;

    public PDFEmbeddedFont(String name, String font, int style, String descendantFont, String toUnicode) {
        super(name, "/TrueType", font, style);
        this.name = name;
        this.font = font;
        this.type = "/TrueType";
        this.descendantFont = descendantFont;
        this.toUnicode = toUnicode;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        // Write the object header
        writeStart(os);

        // now the objects body
        os.write("/Subtype /Type0\n".getBytes());
        os.write(("/BaseFont " + font + "\n").getBytes());
        os.write(("/Name " + name + "\n").getBytes());
        os.write("/Encoding /Identity-H\n".getBytes());
        //System.err.println("descendantFont=" + descendantFont);
        os.write(("/ToUnicode " + toUnicode + "\n").getBytes());
        os.write(("/DescendantFonts [" + descendantFont + "]\n").getBytes());
        /*int cnt = 300;

        os.write(("/FirstChar 0\n").getBytes());
        os.write(("/LastChar " + (cnt - 1) + "\n").getBytes());

        os.write(("/Widths [").getBytes());
        for (int i = 0; i < cnt; i++) {
            os.write((" " + i).getBytes());
        }
        os.write(("]\n").getBytes());
*/
        //os.write("/Widths [500 583 587 796]".getBytes());

        /*os.write("/FontDescriptor ".getBytes());
        os.write(descriptor.getBytes());
        os.write("\n".getBytes());*/

        /*os.write("/ToUnicode ".getBytes());
        os.write(toUnicode.getBytes());
        os.write("\n".getBytes());*/
        // finish off with its footer
        writeEnd(os);
    }

}
