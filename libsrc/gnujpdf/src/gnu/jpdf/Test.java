package gnu.jpdf;


import gnu.jpdf.PDFJob;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;



/**
 *
 * @author JPEXS
 */
public class Test {
    public static void main(String[] args) throws Exception {
        PDFJob job=new PDFJob(new FileOutputStream("test.pdf"));   
        PageFormat pf=new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        Paper p = new Paper();
        p.setSize(210,297); //A4
        pf.setPaper(p);
        
        BufferedImage img = ImageIO.read(new File("earth.jpg"));
        
        int w = 200;
        
        for(int i=0;i<10;i++){
            Graphics g = job.getGraphics();
            g.drawImage(img, 0, 0,w,w, null);
            g.dispose();
        }
        
        job.end();
    }
}
