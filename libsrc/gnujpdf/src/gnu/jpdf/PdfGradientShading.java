package gnu.jpdf;

import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author JPEXS
 */
public class PdfGradientShading extends PDFObject {

    private final MultipleGradientPaint fgrad;
    private static final DecimalFormat matDf = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));

    static {
        matDf.setMaximumFractionDigits(340);
    }
    private final double flen;
    private final boolean useFunctionShading;
    private final PDFObject function3;
    private final PDFObject radialFunction;

    public PdfGradientShading(MultipleGradientPaint fgrad,
            double flen, boolean useFunctionShading, PDFObject function3,
            PDFStream radialFunction) {
        super(null);
        this.fgrad = fgrad;
        this.flen = flen;
        this.useFunctionShading = useFunctionShading;
        this.function3 = function3;
        this.radialFunction = radialFunction;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        writeStart(os);

        if (fgrad instanceof LinearGradientPaint) {
            LinearGradientPaint linGrad = (LinearGradientPaint) fgrad;

            MyDoubleRect coords;

            if (linGrad.getCycleMethod() == MultipleGradientPaint.CycleMethod.NO_CYCLE) {
                Point2D startPointTrans = new Point2D.Double();
                Point2D endPointTrans = new Point2D.Double();
                startPointTrans = linGrad.getStartPoint();
                endPointTrans = linGrad.getEndPoint();

                coords = new MyDoubleRect(startPointTrans.getX(),
                        startPointTrans.getY(),
                        endPointTrans.getX(),
                        endPointTrans.getY());

            } else {
                coords = new MyDoubleRect(0, 0, 0, flen);
            }

            os.write(("/ShadingType 2 /ColorSpace /DeviceRGB "
                    + "/Coords [" + matDf.format(coords.xMin) + " " + matDf.format(coords.yMin) + " " + matDf.format(coords.xMax) + " " + matDf.format(coords.yMax) + "] "
                    + "/Domain [0 1] "
                    + "/Function " + function3.getSerialID() + " 0 R /Extend [true true]\n").getBytes());
            writeEnd(os);
        }
        if (fgrad instanceof RadialGradientPaint) {

            if (useFunctionShading) {
                os.write("/ShadingType 1\n".getBytes());
                os.write("/ColorSpace /DeviceRGB\n".getBytes());
                os.write(("/Function " + radialFunction.getSerialID() + " 0 R\n").getBytes());
            } else {
                RadialGradientPaint radGrad = (RadialGradientPaint) fgrad;
                os.write(("/ShadingType 3 /ColorSpace /DeviceRGB "
                        + "/Coords ["
                        + radGrad.getFocusPoint().getX() + " "
                        + radGrad.getFocusPoint().getY() + " "
                        + "0 "
                        + radGrad.getCenterPoint().getX() + " "
                        + radGrad.getCenterPoint().getY() + " "
                        + radGrad.getRadius()
                        + "] "
                        + "/Domain [0 1] "
                        + "/Function " + function3.getSerialID() + " 0 R /Extend [true true]\n").getBytes());
            }
            writeEnd(os);
        }
    }

}
