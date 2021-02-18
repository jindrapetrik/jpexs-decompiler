package gnu.jpdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class PDFFontDescriptor extends PDFObject implements Serializable {

    private final String fontName;
    private final int ascent;
    private final int descent;
    private final int capheight;
    private final int stemv;
    private final String fontFile2;

    public PDFFontDescriptor(String fontName, int ascent, int descent, int capheight, int stemv, String fontFile2) {
        super("/FontDescriptor");
        this.fontName = fontName;
        this.ascent = ascent;
        this.descent = descent;
        this.capheight = capheight;
        this.stemv = stemv;
        this.fontFile2 = fontFile2;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);
        os.write("/FontName ".getBytes());
        os.write(fontName.getBytes());
        os.write("\n".getBytes());
        os.write("/Flags 4\n".getBytes());
        os.write("/FontBBox [0 -16 725 863]\n".getBytes());
        os.write("/ItalicAngle 0\n".getBytes());
        os.write(("/Ascent " + ascent + "\n").getBytes());
        os.write(("/Descent " + descent + "\n").getBytes());
        os.write(("/CapHeight " + capheight + "\n").getBytes());
        os.write(("/StemV " + stemv + "\n").getBytes());
        //os.write("/MissingWidth 0\n".getBytes());
        os.write(("/FontFile2 " + fontFile2 + "\n").getBytes());
        writeEnd(os);
    }

}
