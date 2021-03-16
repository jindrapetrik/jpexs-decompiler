package gnu.jpdf;

import static gnu.jpdf.PDFImage.handlePixel;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author JPEXS
 */
public class PDFMask extends PDFStream implements ImageObserver {

    private final Image img;
    private int width;
    private int height;

    public PDFMask(Image img) {
        super("/XObject");
        this.img = img;
        width = img.getWidth(this);
        height = img.getHeight(this);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (infoflags == ImageObserver.WIDTH) {
            width = w;
        }
        if (infoflags == ImageObserver.HEIGHT) {
            height = h;
        }

        //return true;
        //}
        return false;
    }

    @Override
    public void writeStream(OutputStream os) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(b);
        //,new Deflater(Deflater.BEST_COMPRESSION,true));
        //buf.writeTo(dos);

        int w = width;
        int h = height;
        int x = 0;
        int y = 0;
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return;
        }
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int p = pixels[j * w + i];
                int alpha = 255 - (p >> 24) & 0xff;
                dos.write(alpha);
            }
        }

        dos.finish();
        dos.close();

        os.write("/Subtype /Image\n".getBytes());
        os.write(("/Width " + width + "\n").getBytes());
        os.write(("/Height " + height + "\n").getBytes());
        os.write(("/BitsPerComponent 8\n").getBytes());
        os.write(("/ColorSpace /DeviceGray\n").getBytes());
        os.write(("/Decode [1 0]\n").getBytes());


        // FlatDecode is compatible with the java.util.zip.Deflater class
        os.write("/Filter /FlateDecode\n".getBytes());
        os.write("/Length ".getBytes());
        os.write(Integer.toString(b.size() + 1).getBytes());
        os.write("\n>>\nstream\n".getBytes());
        b.writeTo(os);
        os.write("\n".getBytes());

        os.write("endstream\nendobj\n".getBytes());

        // Unlike most PDF objects, we dont call writeEnd(os) because we
        // contain a stream
    }

}
