/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.gui.player.FlashDisplay;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class ImagePanel extends JPanel implements ActionListener, FlashDisplay {

    static final String ACTION_SELECT_BKCOLOR = "SELECTCOLOR";
    static final int frameRate = 25;
    // play morph shape in 2 second(s)
    // this settings should be synchronized with frameCount and frameRate
    // settings in Mainpanel.createAndShowTempSwf
    static final int morphShapeAnimationLength = 2;

    public JLabel label = new JLabel();
    public DrawableTag drawable;
    private Timer timer;
    private int frame = -1;
    private SWF swf;
    private boolean loaded;
    private Point mousePos = null;
    private int mouseButton;
    
    

    @Override
    public void setBackground(Color bg) {
        if (label != null) {
            label.setBackground(bg);
        }
        super.setBackground(bg);
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        label.addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener l) {
        label.removeMouseListener(l);
    }

    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        label.addMouseMotionListener(l);
    }

    @Override
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        label.removeMouseMotionListener(l);
    }

    public ImagePanel() {
        super(new BorderLayout());
        label.setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setBackground(View.DEFAULT_BACKGROUND_COLOR);
        
        
        
        JPanel labelPan = new JPanel(new GridBagLayout()){

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d=(Graphics2D)g;
                g2d.setPaint(View.transparentPaint);
                g2d.fill(new Rectangle(0,0,getWidth(),getHeight()));
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setPaint(View.swfBackgroundColor);
                g2d.fill(new Rectangle(0,0,getWidth(),getHeight()));
            }
            
        };        
        labelPan.add(label,new GridBagConstraints());
        add(labelPan,BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton selectColorButton = new JButton(View.getIcon("color16"));
        selectColorButton.addActionListener(this);
        selectColorButton.setActionCommand(ACTION_SELECT_BKCOLOR);
        selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));
        buttonsPanel.add(selectColorButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        label.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                mousePos = e.getPoint();
                drawFrame();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mousePos = null;
                drawFrame();
            }

            
            
            
            @Override
            public void mousePressed(MouseEvent e) {
                mouseButton = e.getButton();
                drawFrame();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseButton = 0;
                drawFrame();
            }
            
});
        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                
            }            
});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_SELECT_BKCOLOR)) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.swfBackgroundColor);
                    if (newColor != null) {
                        View.swfBackgroundColor = newColor;
                        setBackground(newColor);
                        repaint();
                    }
                }
            });

        }
    }

    public void setImage(byte[] data) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        drawable = null;
        loaded = true;
        ImageIcon icon = new ImageIcon(data);
        label.setIcon(icon);
    }

    public void setDrawable(final DrawableTag drawable, final SWF swf, int frameRate) {
        pause();
        this.drawable = drawable;
        this.swf = swf;
        loaded = true;

        if (drawable.getNumFrames() == 0) {
            label.setIcon(null);
            return;
        }
        frame = 0;
        /*if (drawable.getNumFrames() == 1) {
            Matrix mat = new Matrix();
            mat.translateX = swf.displayRect.Xmin;
            mat.translateY = swf.displayRect.Ymin;
            String key = "drawable_0_" + drawable.hashCode();
            SerializableImage img = SWF.getFromCache(key);
            if (img == null) {
                if (drawable instanceof BoundedTag) {
                    BoundedTag bounded = (BoundedTag) drawable;
                    RECT rect = bounded.getRect();
                    int width = (int) (rect.getWidth() / SWF.unitDivisor);
                    int height = (int) (rect.getHeight() / SWF.unitDivisor);
                    if (rect.getWidth() > width * SWF.unitDivisor) {
                        width++;
                    }
                    if (rect.getHeight()> height * SWF.unitDivisor) {
                        height++;
                    }
                    SerializableImage image = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
                    image.fillTransparent();
                    Matrix m = new Matrix();
                    m.translate(-rect.Xmin, -rect.Ymin);
                    drawable.toImage(0, 0, mousePos,mouseButton,image, m, new ColorTransform());
                    img = image;
                } else if (drawable instanceof FontTag) {
                    // only DefineFont tags
                    FontTag fontTag = (FontTag) drawable;
                    img = fontTag.toImage(0, 0, Matrix.getScaleInstance(1 / SWF.unitDivisor), new ColorTransform());
                }
                SWF.putToCache(key, img);
            }
            if (img != null) {
                setImage(img);
            }
            return;
        }*/
        play();
    }

    public void setImage(SerializableImage image) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        drawable = null;
        loaded = true;
        ImageIcon icon = new ImageIcon(image.getBufferedImage());
        label.setIcon(icon);
    }

    @Override
    public int getCurrentFrame() {
        return frame;
    }

    @Override
    public int getTotalFrames() {
        if (drawable == null) {
            return 0;
        }
        return drawable.getNumFrames();
    }

    @Override
    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void nextFrame() {
        int newframe = frame == drawable.getNumFrames() - 1 ? 0 : frame + 1;
        if (drawable instanceof MorphShapeTag) {
            newframe = frame + drawable.getNumFrames() / frameRate / morphShapeAnimationLength;
            if (newframe > drawable.getNumFrames()) {
                newframe = 0;
            }
        }
        if (newframe != frame) {
            frame = newframe;
            drawFrame();
        }
    }

    private static SerializableImage getFrame(SWF swf, int frame, DrawableTag drawable,Point mousePos, int mouseButton) {
        String key = "drawable_" + frame + "_" + drawable.hashCode()+"_"+mouseButton+"_"+(mousePos==null?"out":"over");
        SerializableImage img = SWF.getFromCache(key);
        if (img == null) {
            if (drawable instanceof BoundedTag) {
                BoundedTag bounded = (BoundedTag) drawable;
                RECT rect = bounded.getRect();
                if (rect == null) { //??? Why?
                    rect = new RECT(0, 0, 1, 1);
                }
                int width = rect.getWidth();
                int height = rect.getHeight();
                double scale = 1.0;
                if (width > swf.displayRect.getWidth()) {
                    scale = (double) swf.displayRect.getWidth() / (double) width;
                    width = swf.displayRect.getWidth();
                }
                SerializableImage image = new SerializableImage((int) (width / SWF.unitDivisor) + 1,
                        (int) (height / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
                image.fillTransparent();
                Matrix m = new Matrix();
                m.translate(-rect.Xmin, -rect.Ymin);
                m.scale(scale);
                drawable.toImage(frame, frame,mousePos,mouseButton, image, m, new ColorTransform());
                img = image;
            } else if (drawable instanceof FontTag) {
                // only DefineFont tags
                FontTag fontTag = (FontTag) drawable;
                img = fontTag.toImage(frame, frame, Matrix.getScaleInstance(1 / SWF.unitDivisor), new ColorTransform());
            }
            SWF.putToCache(key, img);
        }
        return img;
    }

    private void drawFrame() {
        if (drawable == null) {
            return;
        }
        Matrix mat = new Matrix();
        mat.translateX = swf.displayRect.Xmin;
        mat.translateY = swf.displayRect.Ymin;
        ImageIcon icon = new ImageIcon(getFrame(swf, frame, drawable,mousePos,mouseButton).getBufferedImage());
        label.setIcon(icon);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void play() {
        pause();
        if (drawable.getNumFrames() > 1) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    nextFrame();
                }
            }, 0, 1000 / frameRate);
        }else{
            drawFrame();
        }
    }

    @Override
    public void rewind() {
        frame = 0;
        drawFrame();
    }

    @Override
    public boolean isPlaying() {
        if (drawable == null) {
            return false;
        }
        return (drawable.getNumFrames() <= 1) || (timer != null);
    }

    @Override
    public void gotoFrame(int frame) {
        this.frame = frame;
        drawFrame();
    }

    @Override
    public int getFrameRate() {
        if (drawable == null) {
            return 1;
        }
        if (drawable instanceof MorphShapeTag) {
            return drawable.getNumFrames() / morphShapeAnimationLength;
        }
        return frameRate;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
