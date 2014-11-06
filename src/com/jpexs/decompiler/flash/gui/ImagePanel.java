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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class ImagePanel extends JPanel implements ActionListener, MediaDisplay {

    private Timelined timelined;
    private boolean stillFrame = false;
    private Timer timer;
    private int frame = -1;
    private SWF swf;
    private boolean loaded;
    private int mouseButton;
    private final JLabel debugLabel = new JLabel("-");
    private DepthState stateUnderCursor = null;
    private MouseEvent lastMouseEvent = null;
    private final List<SoundTagPlayer> soundPlayers = new ArrayList<>();
    private final IconPanel iconPanel;
    private int time = 0;
    private int selectedDepth = -1;
    private double zoom = 1.0;

    public void selectDepth(int depth) {
        if (depth != selectedDepth) {
            this.selectedDepth = depth;
        }
        hideMouseSelection();
    }

    private class IconPanel extends JPanel {

        private SerializableImage img;

        private Rectangle rect = null;
        private List<DepthState> dss;
        private List<Shape> outlines;

        public BufferedImage getLastImage() {
            return img.getBufferedImage();
        }

        public synchronized void setOutlines(List<DepthState> dss, List<Shape> outlines) {
            this.outlines = outlines;
            this.dss = dss;
        }

        public void setImg(SerializableImage img) {
            this.img = img;
            calcRect();
            repaint();
        }

        public synchronized List<DepthState> getObjectsUnderPoint(Point p) {
            List<DepthState> ret = new ArrayList<>();
            for (int i = 0; i < outlines.size(); i++) {
                if (outlines.get(i).contains(p)) {
                    ret.add(dss.get(i));
                }
            }
            return ret;
        }

        public Rectangle getRect() {
            return rect;
        }

        public Point toImagePoint(Point p) {
            if (img == null) {
                return null;
            }
            return new Point((p.x - rect.x) * img.getWidth() / rect.width, (p.y - rect.y) * img.getHeight() / rect.height);
        }

        private void calcRect() {
            if (img != null) {
                int w1 = img.getWidth();
                int h1 = img.getHeight();

                int w2 = getWidth();
                int h2 = getHeight();

                int w;
                int h;
                if (w1 <= w2 && h1 <= h2) {
                    w = w1;
                    h = h1;
                } else {

                    h = h1 * w2 / w1;
                    if (h > h2) {
                        w = w1 * h2 / h1;
                        h = h2;
                    } else {
                        w = w2;
                    }
                }

                rect = new Rectangle(getWidth() / 2 - w / 2, getHeight() / 2 - h / 2, w, h);
            } else {
                rect = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(View.transparentPaint);
            g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setPaint(View.swfBackgroundColor);
            g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
            if (img != null) {
                calcRect();
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.drawImage(img.getBufferedImage(), rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, 0, 0, img.getWidth(), img.getHeight(), null);
            }

        }
    }

    @Override
    public void setBackground(Color bg) {
        if (iconPanel != null) {
            iconPanel.setBackground(bg);
        }
        super.setBackground(bg);
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        iconPanel.addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener l) {
        iconPanel.removeMouseListener(l);
    }

    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        iconPanel.addMouseMotionListener(l);
    }

    @Override
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        iconPanel.removeMouseMotionListener(l);
    }

    private void updatePos(MouseEvent e, boolean draw) {
        if (e == null) {
            return;
        }
        synchronized (iconPanel) {
            lastMouseEvent = e;
            boolean handCursor = false;
            DepthState newStateUnderCursor = null;
            if (timelined != null) {

                Timeline tim = ((Timelined) timelined).getTimeline();
                BoundedTag bounded = (BoundedTag) timelined;
                RECT rect = bounded.getRect(new HashSet<BoundedTag>());
                int width = rect.getWidth();
                double scale = 1.0;
                /*if (width > swf.displayRect.getWidth()) {
                 scale = (double) swf.displayRect.getWidth() / (double) width;
                 }*/
                Matrix m = new Matrix();
                m.translate(-rect.Xmin, -rect.Ymin);
                m.scale(scale);
                Point p = e.getPoint();
                p = iconPanel.toImagePoint(p);
                List<DepthState> objs = new ArrayList<>();
                String ret = "";
                if (p != null) {
                    int x = p.x;
                    int y = p.y;
                    objs = iconPanel.getObjectsUnderPoint(p);

                    ret += " [" + x + "," + y + "] : ";
                }

                boolean first = true;
                for (int i = 0; i < objs.size(); i++) {
                    DepthState ds = objs.get(i);
                    if (!first) {
                        ret += ", ";
                    }
                    first = false;
                    CharacterTag c = tim.swf.characters.get(ds.characterId);
                    if (c instanceof ButtonTag) {
                        newStateUnderCursor = ds;
                        handCursor = true;
                    }
                    ret += c.toString();
                    if (timelined instanceof ButtonTag) {
                        handCursor = true;
                    }
                }
                if (first) {
                    ret += " - ";
                }
                debugLabel.setText(ret);

                if (handCursor) {
                    iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                if (newStateUnderCursor != stateUnderCursor) {
                    stateUnderCursor = newStateUnderCursor;
                    if (draw) {
                        drawFrame();
                    }
                }
            }
        }
    }

    private void showSelectedName() {
        if (selectedDepth > -1 && frame > -1) {
            DepthState ds = timelined.getTimeline().getFrames().get(frame).layers.get(selectedDepth);
            if (ds != null) {
                CharacterTag cht = timelined.getTimeline().swf.characters.get(ds.characterId);
                if (cht != null) {
                    debugLabel.setText(cht.getName());
                }
            }
        }
    }

    public void hideMouseSelection() {
        if (selectedDepth > -1) {
            showSelectedName();
        } else {
            debugLabel.setText(" - ");
        }
    }

    public ImagePanel() {
        super(new BorderLayout());
        //iconPanel.setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);
        setBackground(View.DEFAULT_BACKGROUND_COLOR);

        iconPanel = new IconPanel();
        //labelPan.add(label, new GridBagConstraints());
        add(iconPanel, BorderLayout.CENTER);
        add(debugLabel, BorderLayout.NORTH);
        iconPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                drawFrame();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stateUnderCursor = null;
                lastMouseEvent = null;
                drawFrame();
                hideMouseSelection();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseButton = e.getButton();
                updatePos(e, true);
                drawFrame();
                if (stateUnderCursor != null) {
                    ButtonTag b = (ButtonTag) swf.characters.get(stateUnderCursor.characterId);
                    DefineButtonSoundTag sounds = b.getSounds();
                    if (sounds != null && sounds.buttonSoundChar2 != 0) { //OverUpToOverDown
                        playSound((SoundTag) swf.characters.get(sounds.buttonSoundChar2));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseButton = 0;
                updatePos(e, true);
                drawFrame();
                if (stateUnderCursor != null) {
                    ButtonTag b = (ButtonTag) swf.characters.get(stateUnderCursor.characterId);
                    DefineButtonSoundTag sounds = b.getSounds();
                    if (sounds != null && sounds.buttonSoundChar3 != 0) { //OverDownToOverUp
                        playSound((SoundTag) swf.characters.get(sounds.buttonSoundChar3));
                    }
                }
            }

        });
        iconPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                DepthState lastUnderCur = stateUnderCursor;
                updatePos(e, true);
                if (stateUnderCursor != null) {
                    if (lastUnderCur == null || lastUnderCur.instanceId != stateUnderCursor.instanceId) {
                        //New mouse entered
                        ButtonTag b = (ButtonTag) swf.characters.get(stateUnderCursor.characterId);
                        DefineButtonSoundTag sounds = b.getSounds();
                        if (sounds != null && sounds.buttonSoundChar1 != 0) { //IddleToOverUp
                            playSound((SoundTag) swf.characters.get(sounds.buttonSoundChar1));
                        }
                    }
                }
                if (lastUnderCur != null) {
                    if (stateUnderCursor == null || stateUnderCursor.instanceId != lastUnderCur.instanceId) {
                        //Old mouse leave
                        ButtonTag b = (ButtonTag) swf.characters.get(lastUnderCur.characterId);
                        DefineButtonSoundTag sounds = b.getSounds();
                        if (sounds != null && sounds.buttonSoundChar0 != 0) { //OverUpToIddle
                            playSound((SoundTag) swf.characters.get(sounds.buttonSoundChar0));
                        }
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updatePos(e, true);
            }

        });
    }

    @Override
    public void zoom(double zoom) {
        this.zoom = zoom;
        drawFrame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public BufferedImage printScreen() {
        return iconPanel.getLastImage();
    }

    @Override
    public double getZoomToFit() {
        if (timelined instanceof BoundedTag) {
            RECT bounds = ((BoundedTag) timelined).getRect(new HashSet<BoundedTag>());
            double w1 = bounds.getWidth() / SWF.unitDivisor;
            double h1 = bounds.getHeight() / SWF.unitDivisor;

            double w2 = getWidth();
            double h2 = getHeight();

            double w;
            double h;
            h = h1 * w2 / w1;
            if (h > h2) {
                w = w1 * h2 / h1;
                h = h2;
            } else {
                w = w2;
            }
            return (double) w / (double) w1;
        }
        
        return 1;
    }

    public void setImage(byte[] data) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        timelined = null;
        loaded = true;
        try {
            iconPanel.setImg(new SerializableImage(ImageIO.read(new ByteArrayInputStream(data))));
            iconPanel.setOutlines(new ArrayList<DepthState>(), new ArrayList<Shape>());
        } catch (IOException ex) {
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean zoomAvailable() {
        return timelined != null;
    }

    public synchronized void setTimelined(final Timelined drawable, final SWF swf, int frame) {
        pause();
        if (drawable instanceof ButtonTag) {
            frame = ButtonTag.FRAME_UP;
        }
        this.timelined = drawable;
        this.swf = swf;
        if (frame > -1) {
            this.frame = frame;
            this.stillFrame = true;
        } else {
            this.frame = 0;
            this.stillFrame = false;
        }
        loaded = true;

        if (drawable.getTimeline().getFrames().isEmpty()) {
            iconPanel.setImg(null);
            iconPanel.setOutlines(new ArrayList<DepthState>(), new ArrayList<Shape>());
            return;
        }
        time = 0;
        play();
    }

    public void setImage(SerializableImage image) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        timelined = null;
        loaded = true;
        stillFrame = true;
        iconPanel.setImg(image);
        iconPanel.setOutlines(new ArrayList<DepthState>(), new ArrayList<Shape>());
    }

    @Override
    public int getCurrentFrame() {
        return frame;
    }

    @Override
    public int getTotalFrames() {
        if (timelined == null) {
            return 0;
        }
        if (stillFrame) {
            return 0;
        }
        return timelined.getTimeline().getFrameCount();
    }

    @Override
    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopAllSounds();
    }

    private void stopAllSounds() {
        for (int i = soundPlayers.size() - 1; i >= 0; i--) {
            SoundTagPlayer pl = soundPlayers.get(i);
            pl.pause();
        }
        soundPlayers.clear();
    }

    private void nextFrame() {
        drawFrame();
        int newframe = (frame + 1) % timelined.getTimeline().getFrameCount();
        if (stillFrame) {
            newframe = frame;
        }
        if (newframe != frame) {
            if (newframe == 0) {
                stopAllSounds();
            }
            frame = newframe;
            time = 0;
        } else {
            time++;
        }
    }

    private static SerializableImage getFrame(SWF swf, int frame, int time, Timelined drawable, DepthState stateUnderCursor, int mouseButton, int selectedDepth, double zoom) {
        String key = "drawable_" + frame + "_" + drawable.hashCode() + "_" + mouseButton + "_depth" + selectedDepth + "_" + (stateUnderCursor == null ? "out" : stateUnderCursor.hashCode()) + "_" + zoom;
        SerializableImage img = SWF.getFromCache(key);
        if (img == null) {
            if (drawable instanceof BoundedTag) {
                BoundedTag bounded = (BoundedTag) drawable;
                RECT rect = bounded.getRect(new HashSet<BoundedTag>());
                if (rect == null) { //??? Why?
                    rect = new RECT(0, 0, 1, 1);
                }
                int width = (int) (rect.getWidth() * zoom);
                int height = (int) (rect.getHeight() * zoom);
                SerializableImage image = new SerializableImage((int) (width / SWF.unitDivisor) + 1,
                        (int) (height / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
                image.fillTransparent();
                Matrix m = new Matrix();
                m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
                m.scale(zoom);
                drawable.getTimeline().toImage(frame, time, frame, stateUnderCursor, mouseButton, image, m, new ColorTransform());

                Graphics2D gg = (Graphics2D) image.getGraphics();
                gg.setStroke(new BasicStroke(3));
                gg.setPaint(Color.green);
                gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
                List<DepthState> dss = new ArrayList<>();
                List<Shape> os = new ArrayList<>();
                DepthState ds = drawable.getTimeline().getFrames().get(frame).layers.get(selectedDepth);
                if (ds != null) {
                    CharacterTag cht = swf.characters.get(ds.characterId);
                    if (cht != null) {
                        if (cht instanceof DrawableTag) {
                            DrawableTag dt = (DrawableTag) cht;
                            Shape outline = dt.getOutline(0, ds.time, ds.ratio, stateUnderCursor, mouseButton, new Matrix(ds.matrix));
                            Rectangle bounds = outline.getBounds();
                            bounds.x *= zoom;
                            bounds.y *= zoom;
                            bounds.width *= zoom;
                            bounds.height *= zoom;
                            bounds.x /= 20;
                            bounds.y /= 20;
                            bounds.width /= 20;
                            bounds.height /= 20;
                            bounds.x -= rect.Xmin / 20;
                            bounds.y -= rect.Ymin / 20;
                            //SHAPERECORD.resizeSHAPE(outline, 1/20)
                            gg.setStroke(new BasicStroke(2.0f,
                                    BasicStroke.CAP_BUTT,
                                    BasicStroke.JOIN_MITER,
                                    10.0f, new float[]{10.0f}, 0.0f));
                            gg.setPaint(Color.red);
                            gg.draw(bounds);
                        }
                    }
                }

                img = image;
            }
            if (drawable.getTimeline().isSingleFrame()) {
                SWF.putToCache(key, img);
            }
        }
        return img;
    }

    private synchronized void drawFrame() {
        if (timelined == null) {
            return;
        }
        Timeline timeline = timelined.getTimeline();
        if (frame >= timeline.getFrameCount()) {
            return;
        }

        getOutlines();
        Matrix mat = new Matrix();
        mat.translateX = swf.displayRect.Xmin;
        mat.translateY = swf.displayRect.Ymin;
        updatePos(lastMouseEvent, false);
        SerializableImage img = getFrame(swf, frame, time, timelined, stateUnderCursor, mouseButton, selectedDepth, zoom);
        List<Integer> sounds = new ArrayList<>();
        List<String> soundClasses = new ArrayList<>();
        timeline.getSounds(frame, time, stateUnderCursor, mouseButton, sounds, soundClasses);
        for (int cid : swf.characters.keySet()) {
            CharacterTag c = swf.characters.get(cid);
            for (String cls : soundClasses) {
                if (cls.equals(c.getClassName())) {
                    sounds.add(cid);
                }
            }
        }
        for (int sndId : sounds) {
            CharacterTag c = swf.characters.get(sndId);
            if (c instanceof SoundTag) {
                SoundTag st = (SoundTag) c;
                playSound(st);
            }
        }

        iconPanel.setImg(img);
    }

    private void playSound(SoundTag st) {
        final SoundTagPlayer sp;
        try {
            sp = new SoundTagPlayer(st, 1);

            synchronized (ImagePanel.class) {
                soundPlayers.add(sp);
            }
            sp.addListener(new PlayerListener() {

                @Override
                public void playingFinished() {
                    synchronized (ImagePanel.class) {
                        soundPlayers.remove(sp);
                    }
                }
            });
            sp.play();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, "Error during playing sound", ex);
        }
    }

    private void getOutlines() {
        List<DepthState> objs = new ArrayList<>();
        List<Shape> outlines = new ArrayList<>();
        Matrix m = new Matrix();
        RECT rect = timelined.getTimeline().displayRect;
        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
        m.scale(zoom);

        timelined.getTimeline().getObjectsOutlines(frame, time, frame, stateUnderCursor, mouseButton, m, objs, outlines);
        for (int i = 0; i < outlines.size(); i++) {
            outlines.set(i, SHAPERECORD.twipToPixelShape(outlines.get(i)));
        }
        iconPanel.setOutlines(objs, outlines);
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
        if (timelined != null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                boolean first = true;

                @Override
                public void run() {
                    Timeline timeline = timelined.getTimeline();
                    if (timeline.getFrameCount() <= 1 && timeline.isSingleFrame()) {
                        if (first) {
                            drawFrame();
                            first = false;
                        }
                    } else {
                        nextFrame();
                    }
                }
            }, 0, 1000 / timelined.getTimeline().frameRate);
        } else {
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
        if (timelined == null) {
            return false;
        }
        if (stillFrame) {
            return false;
        }
        return (timelined.getTimeline().getFrameCount() <= 1) || (timer != null);
    }

    @Override
    public void gotoFrame(int frame) {
        this.frame = frame;
        drawFrame();
    }

    @Override
    public int getFrameRate() {
        if (timelined == null) {
            return 1;
        }
        if (stillFrame) {
            return 1;
        }
        return timelined.getTimeline().frameRate;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean screenAvailable() {
        return true;
    }

    @Override
    public double getZoom() {
        return zoom;
    }

}
