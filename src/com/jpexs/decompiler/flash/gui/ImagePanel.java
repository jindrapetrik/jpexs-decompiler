/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.ConstantColorColorTransform;
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

public final class ImagePanel extends JPanel implements MediaDisplay {

    private static final Logger logger = Logger.getLogger(ImagePanel.class.getName());

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private Timelined timelined;

    private boolean stillFrame = false;

    private Timer timer;

    private int frame = -1;

    private boolean loop;

    private boolean zoomAvailable = false;

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

    private Zoom zoom = new Zoom();

    private final Object delayObject = new Object();

    private boolean drawReady;

    private final int drawWaitLimit = 50; // ms

    private TextTag textTag;

    private TextTag newTextTag;

    public synchronized void selectDepth(int depth) {
        if (depth != selectedDepth) {
            this.selectedDepth = depth;
        }

        hideMouseSelection();
    }

    public void fireMediaDisplayStateChanged() {
        for (MediaDisplayListener l : listeners) {
            l.mediaDisplayStateChanged(this);
        }
    }

    @Override
    public void addEventListener(MediaDisplayListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(MediaDisplayListener listener) {
        listeners.remove(listener);
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
            g2d.setPaint(View.getSwfBackgroundColor());
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

    private void updatePos(Timelined timelined, MouseEvent lastMouseEvent, Timer thisTimer) {
        boolean handCursor = false;
        DepthState newStateUnderCursor = null;
        if (timelined != null) {

            Timeline tim = ((Timelined) timelined).getTimeline();
            BoundedTag bounded = (BoundedTag) timelined;
            RECT rect = bounded.getRect();
            int width = rect.getWidth();
            double scale = 1.0;
            /*if (width > swf.displayRect.getWidth()) {
             scale = (double) swf.displayRect.getWidth() / (double) width;
             }*/
            Matrix m = new Matrix();
            m.translate(-rect.Xmin, -rect.Ymin);
            m.scale(scale);

            Point p = lastMouseEvent == null ? null : lastMouseEvent.getPoint();
            List<DepthState> objs = new ArrayList<>();
            StringBuilder ret = new StringBuilder();

            synchronized (ImagePanel.class) {
                if (timer == thisTimer) {
                    p = p == null ? null : iconPanel.toImagePoint(p);
                    if (p != null) {
                        int x = p.x;
                        int y = p.y;
                        objs = iconPanel.getObjectsUnderPoint(p);

                        ret.append(" [").append(x).append(",").append(y).append("] : ");
                    }
                }
            }

            boolean first = true;
            for (int i = 0; i < objs.size(); i++) {
                DepthState ds = objs.get(i);
                if (!first) {
                    ret.append(", ");
                }
                first = false;
                CharacterTag c = tim.swf.getCharacter(ds.characterId);
                if (c instanceof ButtonTag) {
                    newStateUnderCursor = ds;
                    handCursor = true;
                }
                ret.append(c.toString());
                if (timelined instanceof ButtonTag) {
                    handCursor = true;
                }
            }
            if (first) {
                ret.append(" - ");
            }

            synchronized (ImagePanel.class) {
                if (timer == thisTimer) {
                    debugLabel.setText(ret.toString());

                    if (handCursor) {
                        iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                    if (newStateUnderCursor != stateUnderCursor) {
                        stateUnderCursor = newStateUnderCursor;
                    }
                }
            }
        }
    }

    private void showSelectedName() {
        if (selectedDepth > -1 && frame > -1) {
            DepthState ds = timelined.getTimeline().getFrame(frame).layers.get(selectedDepth);
            if (ds != null) {
                CharacterTag cht = timelined.getTimeline().swf.getCharacter(ds.characterId);
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
        setBackground(View.getDefaultBackgroundColor());

        loop = true;
        iconPanel = new IconPanel();
        //labelPan.add(label, new GridBagConstraints());
        add(iconPanel, BorderLayout.CENTER);
        add(debugLabel, BorderLayout.NORTH);
        iconPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    lastMouseEvent = e;
                    redraw();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    stateUnderCursor = null;
                    lastMouseEvent = null;
                    hideMouseSelection();
                    redraw();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    mouseButton = e.getButton();
                    lastMouseEvent = e;
                    redraw();
                    if (stateUnderCursor != null) {
                        ButtonTag b = (ButtonTag) swf.getCharacter(stateUnderCursor.characterId);
                        DefineButtonSoundTag sounds = b.getSounds();
                        if (sounds != null && sounds.buttonSoundChar2 != 0) { //OverUpToOverDown
                            playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar2), timer);
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    mouseButton = 0;
                    lastMouseEvent = e;
                    redraw();
                    if (stateUnderCursor != null) {
                        ButtonTag b = (ButtonTag) swf.getCharacter(stateUnderCursor.characterId);
                        DefineButtonSoundTag sounds = b.getSounds();
                        if (sounds != null && sounds.buttonSoundChar3 != 0) { //OverDownToOverUp
                            playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar3), timer);
                        }
                    }
                }
            }

        });
        iconPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    lastMouseEvent = e;
                    redraw();
                    DepthState lastUnderCur = stateUnderCursor;
                    if (stateUnderCursor != null) {
                        if (lastUnderCur == null || lastUnderCur.instanceId != stateUnderCursor.instanceId) {
                            // New mouse entered
                            ButtonTag b = (ButtonTag) swf.getCharacter(stateUnderCursor.characterId);
                            DefineButtonSoundTag sounds = b.getSounds();
                            if (sounds != null && sounds.buttonSoundChar1 != 0) { //IddleToOverUp
                                playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar1), timer);
                            }
                        }
                    }
                    if (lastUnderCur != null) {
                        if (stateUnderCursor == null || stateUnderCursor.instanceId != lastUnderCur.instanceId) {
                            // Old mouse leave
                            ButtonTag b = (ButtonTag) swf.getCharacter(lastUnderCur.characterId);
                            DefineButtonSoundTag sounds = b.getSounds();
                            if (sounds != null && sounds.buttonSoundChar0 != 0) { //OverUpToIddle
                                playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar0), timer);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    lastMouseEvent = e;
                    redraw();
                }
            }

        });
    }

    private synchronized void redraw() {
        if (timer == null && timelined != null) {
            startTimer(timelined.getTimeline(), false);
        }
    }

    @Override
    public synchronized void zoom(Zoom zoom) {
        boolean modified = this.zoom.value != zoom.value || this.zoom.fit != zoom.fit;
        if (modified) {
            this.zoom = zoom;
            redraw();
            if (textTag != null) {
                setText(textTag, newTextTag);
            }

            fireMediaDisplayStateChanged();
        }
    }

    @Override
    public synchronized BufferedImage printScreen() {
        return iconPanel.getLastImage();
    }

    @Override
    public synchronized double getZoomToFit() {
        if (timelined != null) {
            RECT bounds = timelined.getRect();
            double w1 = bounds.getWidth() / SWF.unitDivisor;
            double h1 = bounds.getHeight() / SWF.unitDivisor;

            double w2 = getWidth();
            double h2 = getHeight();

            double w;
            double h;
            h = h1 * w2 / w1;
            if (h > h2) {
                w = w1 * h2 / h1;
            } else {
                w = w2;
            }

            if (w1 <= Double.MIN_NORMAL) {
                return 1.0;
            }

            return (double) w / (double) w1;
        }

        return 1;
    }

    public void setImage(byte[] data) {
        try {
            setImage(new SerializableImage(ImageIO.read(new ByteArrayInputStream(data))));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized boolean zoomAvailable() {
        return zoomAvailable;
    }

    public void setTimelined(final Timelined drawable, final SWF swf, int frame) {
        synchronized (ImagePanel.class) {
            stopInternal();
            if (drawable instanceof ButtonTag) {
                frame = ButtonTag.FRAME_UP;
            }

            this.timelined = drawable;
            this.swf = swf;
            zoomAvailable = true;
            timer = null;
            if (frame > -1) {
                this.frame = frame;
                this.stillFrame = true;
            } else {
                this.frame = 0;
                this.stillFrame = false;
            }

            loaded = true;

            if (drawable.getTimeline().getFrameCount() == 0) {
                clearImagePanel();
                return;
            }

            time = 0;
            drawReady = false;
            redraw();
            play();
        }

        synchronized (delayObject) {
            try {
                delayObject.wait(drawWaitLimit);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        synchronized (ImagePanel.class) {
            if (!drawReady) {
                clearImagePanel();
            }
        }

        fireMediaDisplayStateChanged();
    }

    public synchronized void setImage(SerializableImage image) {
        setBackground(View.getSwfBackgroundColor());
        clear();

        timelined = null;
        loaded = true;
        stillFrame = true;
        zoomAvailable = false;
        iconPanel.setImg(image);
        iconPanel.setOutlines(new ArrayList<>(), new ArrayList<>());
        drawReady = true;

        fireMediaDisplayStateChanged();
    }

    public synchronized void setText(TextTag textTag, TextTag newTextTag) {
        setBackground(View.getSwfBackgroundColor());
        clear();

        timelined = null;
        loaded = true;
        stillFrame = true;
        zoomAvailable = true;

        this.textTag = textTag;
        this.newTextTag = newTextTag;

        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

        RECT rect = textTag.getRect();
        int width = (int) (rect.getWidth() * zoomDouble);
        int height = (int) (rect.getHeight() * zoomDouble);
        SerializableImage image = new SerializableImage((int) (width / SWF.unitDivisor) + 1,
                (int) (height / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        Matrix m = new Matrix();
        m.translate(-rect.Xmin * zoomDouble, -rect.Ymin * zoomDouble);
        m.scale(zoomDouble);
        textTag.toImage(0, 0, 0, new RenderContext(), image, m, new ConstantColorColorTransform(0xFFC0C0C0));

        if (newTextTag != null) {
            newTextTag.toImage(0, 0, 0, new RenderContext(), image, m, new ConstantColorColorTransform(0xFF000000));
        }

        iconPanel.setImg(image);
        iconPanel.setOutlines(new ArrayList<>(), new ArrayList<>());
        drawReady = true;

        fireMediaDisplayStateChanged();
    }

    private synchronized void clearImagePanel() {
        iconPanel.setImg(null);
        iconPanel.setOutlines(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public synchronized int getCurrentFrame() {
        return frame;
    }

    @Override
    public synchronized int getTotalFrames() {
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
        stopInternal();
        redraw();
    }

    @Override
    public void stop() {
        stopInternal();
        rewind();
        redraw();
    }

    @Override
    public void close() throws IOException {
        stopInternal();
    }

    private void stopAllSounds() {
        for (int i = soundPlayers.size() - 1; i >= 0; i--) {
            SoundTagPlayer pl = soundPlayers.get(i);
            pl.close();
        }
        soundPlayers.clear();
    }

    private void clear() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            fireMediaDisplayStateChanged();
        }

        textTag = null;
        newTextTag = null;
    }

    private void nextFrame(Timer thisTimer) {
        drawFrame(thisTimer);
        synchronized (ImagePanel.class) {
            if (timelined != null && timer == thisTimer) {
                int frameCount = timelined.getTimeline().getFrameCount();

                if (!stillFrame && frame == frameCount - 1 && !loop) {
                    stopInternal();
                    return;
                }

                int newframe;
                if (frameCount > 0) {
                    newframe = (frame + 1) % frameCount;
                } else {
                    newframe = frame;
                }

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
        }

        fireMediaDisplayStateChanged();
    }

    private static SerializableImage getFrame(SWF swf, int frame, int time, Timelined drawable, DepthState stateUnderCursor, int mouseButton, int selectedDepth, double zoom) {
        Timeline timeline = drawable.getTimeline();
        String key = "drawable_" + frame + "_" + drawable.hashCode() + "_" + mouseButton + "_depth" + selectedDepth + "_" + (stateUnderCursor == null ? "out" : stateUnderCursor.hashCode()) + "_" + zoom + "_" + timeline.fontFrameNum;
        SerializableImage img = swf.getFromCache(key);
        if (img == null) {
            boolean shouldCache = timeline.isSingleFrame(frame);
            RECT rect = drawable.getRect();

            int width = (int) (rect.getWidth() * zoom);
            int height = (int) (rect.getHeight() * zoom);
            SerializableImage image = new SerializableImage((int) Math.ceil(width / SWF.unitDivisor),
                    (int) Math.ceil(height / SWF.unitDivisor), SerializableImage.TYPE_INT_ARGB);
            image.fillTransparent();
            Matrix m = new Matrix();
            m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
            m.scale(zoom);
            RenderContext renderContext = new RenderContext();
            renderContext.stateUnderCursor = stateUnderCursor;
            renderContext.mouseButton = mouseButton;
            timeline.toImage(frame, time, frame, renderContext, image, m, new ColorTransform());

            Graphics2D gg = (Graphics2D) image.getGraphics();
            gg.setStroke(new BasicStroke(3));
            gg.setPaint(Color.green);
            gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
            List<DepthState> dss = new ArrayList<>();
            List<Shape> os = new ArrayList<>();
            DepthState ds = null;
            if (timeline.getFrameCount() > frame) {
                ds = timeline.getFrame(frame).layers.get(selectedDepth);
            }

            if (ds != null) {
                CharacterTag cht = swf.getCharacter(ds.characterId);
                if (cht != null) {
                    if (cht instanceof DrawableTag) {
                        DrawableTag dt = (DrawableTag) cht;
                        Shape outline = dt.getOutline(0, ds.time, ds.ratio, renderContext, new Matrix(ds.matrix));
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

            if (shouldCache) {
                swf.putToCache(key, img);
            }
        }
        return img;
    }

    private void drawFrame(Timer thisTimer) {
        Timelined timelined;
        MouseEvent lastMouseEvent;
        int frame;
        int time;
        DepthState stateUnderCursor;
        int mouseButton;
        int selectedDepth;
        Zoom zoom;
        SWF swf;

        synchronized (ImagePanel.class) {
            timelined = this.timelined;
            lastMouseEvent = this.lastMouseEvent;
        }

        synchronized (ImagePanel.class) {
            frame = this.frame;
            time = this.time;
            stateUnderCursor = this.stateUnderCursor;
            mouseButton = this.mouseButton;
            selectedDepth = this.selectedDepth;
            zoom = this.zoom;
            swf = this.swf;
        }

        if (timelined == null) {
            return;
        }

        SerializableImage img;
        try {
            Timeline timeline = timelined.getTimeline();
            if (frame >= timeline.getFrameCount()) {
                return;
            }

            double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
            getOutlines(timelined, frame, time, zoomDouble, stateUnderCursor, mouseButton, thisTimer);
            updatePos(timelined, lastMouseEvent, thisTimer);

            Matrix mat = new Matrix();
            mat.translateX = swf.displayRect.Xmin;
            mat.translateY = swf.displayRect.Ymin;

            img = getFrame(swf, frame, time, timelined, stateUnderCursor, mouseButton, selectedDepth, zoomDouble);

            List<Integer> sounds = new ArrayList<>();
            List<String> soundClasses = new ArrayList<>();
            timeline.getSounds(frame, time, stateUnderCursor, mouseButton, sounds, soundClasses);
            for (int cid : swf.getCharacters().keySet()) {
                CharacterTag c = swf.getCharacter(cid);
                for (String cls : soundClasses) {
                    if (cls.equals(c.getClassName())) {
                        sounds.add(cid);
                    }
                }
            }

            for (int sndId : sounds) {
                CharacterTag c = swf.getCharacter(sndId);
                if (c instanceof SoundTag) {
                    SoundTag st = (SoundTag) c;
                    playSound(st, thisTimer);
                }
            }
        } catch (Throwable ex) {
            // swf was closed during the rendering probably
            return;
        }

        synchronized (ImagePanel.class) {
            if (timer == thisTimer) {
                iconPanel.setImg(img);
                drawReady = true;
                synchronized (delayObject) {
                    delayObject.notify();
                }
            }
        }
    }

    private void playSound(SoundTag st, Timer thisTimer) {
        final SoundTagPlayer sp;
        try {
            sp = new SoundTagPlayer(st, 1, false);
            sp.addEventListener(new MediaDisplayListener() {

                @Override
                public void mediaDisplayStateChanged(MediaDisplay source) {
                }

                @Override
                public void playingFinished(MediaDisplay source) {
                    synchronized (ImagePanel.class) {
                        sp.close();
                        soundPlayers.remove(sp);
                    }
                }
            });

            synchronized (ImagePanel.class) {
                if (timer != null && timer == thisTimer) {
                    soundPlayers.add(sp);
                    sp.play();
                } else {
                    sp.close();
                }
            }
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            logger.log(Level.SEVERE, "Error during playing sound", ex);
        }
    }

    private List<Shape> getOutlines(Timelined timelined, int frame, int time, double zoom, DepthState stateUnderCursor, int mouseButton, Timer thisTimer) {
        List<DepthState> objs = new ArrayList<>();
        List<Shape> outlines = new ArrayList<>();
        Matrix m = new Matrix();
        Timeline timeline = timelined.getTimeline();
        RECT rect = timeline.displayRect;
        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
        m.scale(zoom);

        timeline.getObjectsOutlines(frame, time, frame, stateUnderCursor, mouseButton, m, objs, outlines);
        for (int i = 0; i < outlines.size(); i++) {
            outlines.set(i, SHAPERECORD.twipToPixelShape(outlines.get(i)));
        }

        synchronized (ImagePanel.class) {
            if (timer == thisTimer) {
                iconPanel.setOutlines(objs, outlines);
            }
        }

        return outlines;
    }

    public synchronized void clearAll() {
        stopInternal();
        clearImagePanel();
        timelined = null;
        swf = null;

        fireMediaDisplayStateChanged();
    }

    private synchronized void stopInternal() {
        clear();
        stopAllSounds();
    }

    @Override
    public synchronized void play() {
        stopInternal();
        if (timelined != null) {
            Timeline timeline = timelined.getTimeline();
            if (!stillFrame && frame == timeline.getFrameCount() - 1) {
                frame = 0;
            }

            startTimer(timeline, true);
        }
    }

    private void startTimer(Timeline timeline, boolean playing) {

        float frameRate = timeline.frameRate;
        int msPerFrame = frameRate == 0 ? 1000 : (int) (1000.0 / frameRate);
        final boolean singleFrame = !playing || (timeline.getRealFrameCount() <= 1 && timeline.isSingleFrame());

        timer = new Timer();
        TimerTask task = new TimerTask() {
            public final Timer thisTimer = timer;

            public final boolean isSingleFrame = singleFrame;

            @Override
            public void run() {
                try {
                    synchronized (ImagePanel.class) {
                        if (timer != thisTimer) {
                            return;
                        }
                    }

                    if (isSingleFrame) {
                        drawFrame(thisTimer);
                        synchronized (ImagePanel.class) {
                            thisTimer.cancel();
                            if (timer == thisTimer) {
                                timer = null;
                            }
                        }

                        fireMediaDisplayStateChanged();
                    } else {
                        nextFrame(thisTimer);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };

        if (singleFrame) {
            timer.schedule(task, 0);
        } else {
            timer.schedule(task, 0, msPerFrame);
        }
    }

    @Override
    public synchronized void rewind() {
        frame = 0;
        fireMediaDisplayStateChanged();
    }

    @Override
    public synchronized boolean isPlaying() {
        if (timelined == null || stillFrame) {
            return false;
        }

        return (timelined.getTimeline().getFrameCount() <= 1) || (timer != null);
    }

    @Override
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    public synchronized void gotoFrame(int frame) {
        if (timelined == null) {
            return;
        }
        Timeline timeline = timelined.getTimeline();
        if (frame >= timeline.getFrameCount()) {
            return;
        }
        if (frame < 0) {
            return;
        }

        this.frame = frame;
        stopInternal();
        redraw();
        fireMediaDisplayStateChanged();
    }

    @Override
    public synchronized float getFrameRate() {
        if (timelined == null) {
            return 1;
        }
        if (stillFrame) {
            return 1;
        }
        return timelined.getTimeline().frameRate;
    }

    @Override
    public synchronized boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean loopAvailable() {
        return false;
    }

    @Override
    public boolean screenAvailable() {
        return true;
    }

    @Override
    public synchronized Zoom getZoom() {
        return zoom;
    }
}
