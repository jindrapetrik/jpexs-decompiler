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

    private final String descriptor;
    private String name;
    private String font;
    private String type;
    private final String widths;
    private final int firstChar;
    private final int lastChar;

    public PDFEmbeddedFont(String name, String font, int style, String descriptor, String widths, int firstChar, int lastChar) {
        super(name, "/TrueType", font, style);
        this.descriptor = descriptor;
        this.name = name;
        this.font = font;
        this.type = "/TrueType";
        this.widths = widths;
        this.firstChar = firstChar;
        this.lastChar = lastChar;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        // Write the object header
        writeStart(os);

        // now the objects body
        os.write("/Subtype ".getBytes());
        os.write(type.getBytes());
        os.write("\n/Name ".getBytes());
        os.write(name.getBytes());
        os.write("\n/BaseFont ".getBytes());
        os.write(font.getBytes());
        os.write("\n".getBytes());

        //os.write("/Encoding /WinAnsiEncoding\n".getBytes());

        os.write(("/FirstChar " + firstChar + "\n").getBytes());
        os.write(("/LastChar " + lastChar + "\n").getBytes());
        os.write(("/Widths " + widths + "\n").getBytes());
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

        os.write("/FontDescriptor ".getBytes());
        os.write(descriptor.getBytes());
        os.write("\n".getBytes());

        /*os.write("/ToUnicode ".getBytes());
        os.write(toUnicode.getBytes());
        os.write("\n".getBytes());*/
        // finish off with its footer
        writeEnd(os);
    }

}
