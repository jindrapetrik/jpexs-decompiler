package com.jpexs.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class StrokeTest extends JFrame {

    public StrokeTest() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {                
                super.paintComponent(g);
                
                float miterLimit = 3f;//9.6f;
                Graphics2D g2d = (Graphics2D)g;
                GeneralPath p = new GeneralPath();
                ExtendedBasicStroke ebs = new ExtendedBasicStroke(30, 
                        ExtendedBasicStroke.CAP_BUTT,
                        ExtendedBasicStroke.JOIN_MITER_CLIP,
                        miterLimit);
                g2d.setStroke(ebs);
                g2d.setPaint(Color.RED);
                /*path = new GeneralPath();
                path.moveTo(10, 10);
                path.lineTo(30, 200);
                path.lineTo(50, 10);
                g2d.draw(path);
                
                
                path = new GeneralPath();
                path.moveTo(10, 300+200);
                path.lineTo(30, 300+10);
                path.lineTo(50, 300+200);
                g2d.draw(path);
                
                path = new GeneralPath();
                path.moveTo(50, 600+200);
                path.lineTo(30, 600+10);
                path.lineTo(10, 600+200);
                g2d.draw(path);
                
                path = new GeneralPath();
                path.moveTo(200, 300);
                path.lineTo(200+190, 300+20);
                path.lineTo(200, 300+40);
                g2d.draw(path);*/
                
                double zoom=1;
                GeneralPath path = new GeneralPath();
                path.moveTo(5+15 * zoom, 15*zoom);
                path.lineTo(5+45*zoom, 185*zoom);
                path.lineTo(5+85*zoom, 15*zoom);
                g2d.draw(path);
                
                /*p.moveTo(730.0,435.0);
p.quadTo(730.0,582.0,523.0,702.0);
p.quadTo(315.0,822.0,35.0,822.0);
p.quadTo(-306.0,822.0,-535.0,589.0);
p.quadTo(-764.0,356.0,-764.0,4.0);
p.quadTo(-764.0,-349.0,-542.0,-585.0);
p.quadTo(-320.0,-821.0,12.0,-821.0);
p.quadTo(332.0,-821.0,548.0,-599.0);
p.quadTo(764.0,-375.0,764.0,-57.0);
p.quadTo(764.0,46.0,717.0,88.0);
p.quadTo(669.0,130.0,535.0,130.0);
p.lineTo(-290.0,130.0);
p.quadTo(-275.0,257.0,-184.0,332.0);
p.quadTo(-92.0,406.0,59.0,406.0);
p.quadTo(190.0,406.0,349.0,331.0);
p.quadTo(508.0,256.0,545.0,256.0);
p.quadTo(629.0,256.0,679.0,306.0);
p.quadTo(730.0,356.0,730.0,435.0);*/
/*p.moveTo(220.0,-368.0);
p.quadTo(132.0,-446.0,15.0,-446.0);
p.quadTo(-103.0,-446.0,-186.0,-373.0);*/
/*p.moveTo(-186.0,-373.0);//navic
p.quadTo(-269.0,-300.0,-295.0,-172.0);
p.lineTo(314.0,-172.0);
//p.quadTo(314.0,-274.0,220.0,-368.0);
p.closePath();
g2d.setTransform(new AffineTransform());
g2d.translate(500, 500);
//g2d.scale(0.8, 0.8);
g2d.draw(p);*/

                
                /*g2d.setStroke(new BasicStroke(30, 
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER,
                        miterLimit));
                */
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(Color.BLACK);
                g2d.drawLine(5, 229, 200, 229);
                
                /*path = new GeneralPath();
                path.moveTo(200+10, 10);
                path.lineTo(200+30, 200);
                path.lineTo(200+50, 10);
                g2d.draw(path);
               */
                /*for(Point2D po:ebs.testPoints){
                    drawPoint(g2d, po);
                }*/
                
            }
            
            private void drawPoint(Graphics2D g, Point2D p){
                g.setColor(Color.GREEN);
                int width = 5;                
                g.drawRect((int)p.getX() - width/2, (int)p.getY() - width/2, width, width);
            }
            
        });
        setSize(800, 800);
    }
    
    
    
    public static void main(String[] args) {
        new StrokeTest().setVisible(true);
    }
}
