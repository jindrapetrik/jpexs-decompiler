package gnu.jpdf;

import java.awt.MultipleGradientPaint;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PDFGradientFunction3 extends PDFObject {

    private final MultipleGradientPaint fgrad;

    private final List<String> functions2Refs;

    public PDFGradientFunction3(MultipleGradientPaint fgrad, List<String> functions2Refs) {
        super(null);
        this.fgrad = fgrad;
        this.functions2Refs = functions2Refs;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);
        os.write(("/FunctionType 3 /Domain [0 1] /Functions [" + String.join(" ", functions2Refs) + "] ").getBytes());
        int lastcols = fgrad.getColors().length - 1;
        List<String> bounds = new ArrayList<>();
        List<String> encode = new ArrayList<>();
        for (int i = 1; i < fgrad.getColors().length; i++) {
            if (i < lastcols) {
                bounds.add("" + fgrad.getFractions()[i]);
            }
            encode.add("0 1");
        }
        os.write(("/Bounds [" + String.join(" ", bounds) + "] ").getBytes());
        os.write(("/Encode [" + String.join(" ", encode) + "]\n").getBytes());
        writeEnd(os);
    }

}
