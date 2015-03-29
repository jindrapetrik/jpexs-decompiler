package fontatest;

import fontastic.FPoint;
import fontastic.Fontastic;
import java.io.File;
import java.io.IOException;

public class FontaTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        File file = new File("example.ttf");
        file.delete();
        Fontastic f = new Fontastic("ExampleFont", file);
        f.setAuthor("Nobody");
        FPoint[] points = new FPoint[]{ // Define a FPoint array containing the points of the shape
            new FPoint(0, 0),
            new FPoint(512, 0),
            //new FPoint(256, 1024),
            new FPoint(new FPoint(256, 1024), new FPoint(512, 512)),
            new FPoint(0, 0)
        };
        f.addGlyph('P').addContour(points);             // Assign contour to character A
        f.buildFont();
    }
}
