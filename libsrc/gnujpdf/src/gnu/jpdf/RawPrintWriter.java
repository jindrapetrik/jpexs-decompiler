package gnu.jpdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class RawPrintWriter {

    private final OutputStream os;

    public RawPrintWriter(OutputStream os) {
        this.os = os;
    }

    public void print(String s) {
        try {
            os.write(s.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RawPrintWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printRaw(byte[] data) {
        try {
            os.write(data);
        } catch (IOException ex) {
            Logger.getLogger(RawPrintWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void println(String s) {
        try {
            os.write((s + "\n").getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RawPrintWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(RawPrintWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
