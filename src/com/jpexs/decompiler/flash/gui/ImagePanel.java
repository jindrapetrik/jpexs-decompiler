/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.Stage;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.ConstantColorColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.Stopwatch;
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
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public final class ImagePanel extends JPanel implements MediaDisplay {

    private static final Logger logger = Logger.getLogger(ImagePanel.class.getName());

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private Timelined timelined;

    private boolean stillFrame = false;

    private volatile Timer timer;

    private int frame = -1;

    private boolean loop;

    private LocalDataArea lda;

    private boolean zoomAvailable = false;

    private SWF swf;

    private boolean loaded;

    private int mouseButton;

    private final JLabel debugLabel = new JLabel("-");

    private Point cursorPosition = null;

    private MouseEvent lastMouseEvent = null;

    private final List<SoundTagPlayer> soundPlayers = new ArrayList<>();

    private final Cache<PlaceObjectTypeTag, SerializableImage> displayObjectCache = Cache.getInstance(false, false, "displayObject");

    private final IconPanel iconPanel;

    private int time = 0;

    private int selectedDepth = -1;

    private Zoom zoom = new Zoom();

    private final Object delayObject = new Object();

    private boolean drawReady;

    private final int drawWaitLimit = 50; // ms

    private TextTag textTag;

    private TextTag newTextTag;

    private int msPerFrame;

    private final boolean lowQuality = false;

    private final double LQ_FACTOR = 2;

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

        private SerializableImage _img;

        private Rectangle _rect = null;

        private ButtonTag mouseOverButton = null;

        private boolean autoFit = false;

        private boolean allowMove = true;

        private Point dragStart = null;

        private Point offsetPoint = new Point(0, 0);

        private synchronized SerializableImage getImg() {
            return _img;
        }

        public synchronized Rectangle getRect() {
            return _rect;
        }

        public boolean hasAllowMove() {
            return allowMove;
        }

        VolatileImage renderImage;

        public void render() {
            SerializableImage img = getImg();
            Rectangle rect = getRect();
            if (img == null) {
                return;
            }

            Graphics2D g2 = null;
            VolatileImage ri;
            do {
                ri = this.renderImage;
                if (ri == null) {
                    return;
                }

                int valid = ri.validate(View.getDefaultConfiguration());

                if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
                    ri = View.createRenderImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
                }

                try {
                    g2 = ri.createGraphics();
                    g2.setPaint(View.transparentPaint);
                    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
                    g2.setComposite(AlphaComposite.SrcOver);
                    g2.setPaint(View.getSwfBackgroundColor());
                    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));

                    g2.setComposite(AlphaComposite.SrcOver);
                    if (rect != null) {
                        g2.drawImage(img.getBufferedImage(), rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, 0, 0, img.getWidth(), img.getHeight(), null);
                    }
                } finally {
                    if (g2 != null) {
                        g2.dispose();
                    }
                }

            } while (ri.contentsLost());
        }

        public IconPanel() {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int width = getWidth();
                    int height = getHeight();
                    if (width > 0 && height > 0) {
                        renderImage = View.createRenderImage(width, height, Transparency.TRANSLUCENT);
                    } else {
                        renderImage = null;
                    }

                    if (_img != null) {
                        calcRect();
                        render();
                    }
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        dragStart = e.getPoint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        dragStart = null;
                    }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart != null && allowMove) {
                        Point dragEnd = e.getPoint();
                        Point delta = new Point(dragEnd.x - dragStart.x, dragEnd.y - dragStart.y);
                        offsetPoint.x += delta.x;
                        offsetPoint.y += delta.y;
                        dragStart = dragEnd;
                        repaint();
                    }
                }
            });
        }

        public void setAutoFit(boolean autoFit) {
            this.autoFit = autoFit;
            repaint();
        }

        public synchronized BufferedImage getLastImage() {
            if (_img == null) {
                return null;
            }
            return _img.getBufferedImage();
        }

        public synchronized void setImg(SerializableImage img) {
            this._img = img;
            if (img != null) {
                calcRect();
                render();
            }
            repaint();
        }

        public synchronized Point toImagePoint(Point p) {
            if (_img == null) {
                return null;
            }

            return new Point((p.x - _rect.x) * _img.getWidth() / _rect.width, (p.y - _rect.y) * _img.getHeight() / _rect.height);
        }

        private void setAllowMove(boolean allowMove) {
            this.allowMove = allowMove;
            if (!allowMove) {
                offsetPoint = new Point();
            }
        }

        private synchronized void calcRect() {
            if (_img != null) {
                int w1 = (int) (_img.getWidth() * (lowQuality ? LQ_FACTOR : 1));
                int h1 = (int) (_img.getHeight() * (lowQuality ? LQ_FACTOR : 1));

                int w2 = getWidth();
                int h2 = getHeight();

                int w;
                int h;
                if (autoFit) {
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
                } else {
                    w = w1;
                    h = h1;
                }

                setAllowMove(h > h2 || w > w2);
                _rect = new Rectangle(getWidth() / 2 - w / 2 + offsetPoint.x, getHeight() / 2 - h / 2 + offsetPoint.y, w, h);
            } else {
                _rect = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            VolatileImage ri = this.renderImage;
            if (ri != null) {
                calcRect();
                if (ri.validate(View.getDefaultConfiguration()) != VolatileImage.IMAGE_OK) {
                    ri = View.createRenderImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
                    render();
                }

                if (ri != null) {
                    g2d.drawImage(ri, 0, 0, null);
                }
            }
            g2d.setColor(Color.red);

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);

            float frameLoss = 100 - (getFpsIs() / fpsShouldBe * 100);

            if (Configuration._debugMode.get()) {
                g2d.drawString("frameLoss:" + df.format(frameLoss) + "%", 20, 20);
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
        if (timelined != null) {

            BoundedTag bounded = (BoundedTag) timelined;
            RECT rect = bounded.getRect();
            int width = rect.getWidth();
            double scale = 1.0;
            /*if (width > swf.displayRect.getWidth()) {
             scale = (double) swf.displayRect.getWidth() / (double) width;
             }*/
            Matrix m = Matrix.getTranslateInstance(-rect.Xmin, -rect.Ymin);
            m.scale(scale);

            Point p = lastMouseEvent == null ? null : lastMouseEvent.getPoint();

            synchronized (ImagePanel.class) {
                if (timer == thisTimer) {
                    cursorPosition = p;
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
                    ButtonTag button = iconPanel.mouseOverButton;
                    if (button != null) {
                        DefineButtonSoundTag sounds = button.getSounds();
                        if (sounds != null && sounds.buttonSoundChar2 != 0) { // OverUpToOverDown
                            playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar2), sounds.buttonSoundInfo2, timer);
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
                    ButtonTag button = iconPanel.mouseOverButton;
                    if (button != null) {
                        DefineButtonSoundTag sounds = button.getSounds();
                        if (sounds != null && sounds.buttonSoundChar3 != 0) { // OverDownToOverUp
                            playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar3), sounds.buttonSoundInfo3, timer);
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
            displayObjectCache.clear();
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

    @Override
    public synchronized boolean zoomAvailable() {
        return zoomAvailable;
    }

    public void setTimelined(final Timelined drawable, final SWF swf, int frame) {
        Stage stage = new Stage(drawable) {
            @Override
            public void callFrame(int frame) {
                executeFrame(frame);
            }

            @Override
            public Object callFunction(long functionAddress, long functionLength, List<Object> args, Map<Integer, String> regNames, Object thisObj) {
                try {
                    SWFInputStream sis = new SWFInputStream(swf, swf.uncompressedData, functionAddress, (int) (functionAddress + functionLength));
                    return execute(sis);
                } catch (IOException ex) {
                    Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                return Undefined.INSTANCE;
            }

            @Override
            public int getCurrentFrame() {
                return ImagePanel.this.getCurrentFrame();
            }

            @Override
            public int getTotalFrames() {
                return ImagePanel.this.getTotalFrames();
            }

            @Override
            public void gotoFrame(int frame) {
                ImagePanel.this.pause();
                ImagePanel.this.gotoFrame(frame);
            }

            @Override
            public void gotoLabel(String label) {
                //TODO
            }

            @Override
            public void pause() {
                ImagePanel.this.pause();
            }

            @Override
            public void play() {
                ImagePanel.this.play();
            }

            @Override
            public void trace(Object... val) {
                for (Object o : val) {
                    System.out.println("trace:" + o.toString());
                }
            }
        };
        lda = new LocalDataArea(stage);
        synchronized (ImagePanel.class) {
            stopInternal();
            if (drawable instanceof ButtonTag) {
                frame = ButtonTag.FRAME_UP;
            }

            displayObjectCache.clear();
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
        lda = null;
        setBackground(View.getSwfBackgroundColor());
        clear();

        timelined = null;
        loaded = true;
        stillFrame = true;
        zoomAvailable = false;
        iconPanel.setImg(image);
        drawReady = true;

        fireMediaDisplayStateChanged();
    }

    public synchronized void setText(TextTag textTag, TextTag newTextTag) {
        setBackground(View.getSwfBackgroundColor());
        clear();

        lda = null;
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
        Matrix m = Matrix.getTranslateInstance(-rect.Xmin * zoomDouble, -rect.Ymin * zoomDouble);
        m.scale(zoomDouble);
        textTag.toImage(0, 0, 0, new RenderContext(), image, false, m, m, m, new ConstantColorColorTransform(0xFFC0C0C0));

        if (newTextTag != null) {
            newTextTag.toImage(0, 0, 0, new RenderContext(), image, false, m, m, m, new ConstantColorColorTransform(0xFF000000));
        }

        iconPanel.setImg(image);
        drawReady = true;

        fireMediaDisplayStateChanged();
    }

    private synchronized void clearImagePanel() {
        iconPanel.setImg(null);
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
        displayObjectCache.clear();
    }

    private void nextFrame(Timer thisTimer, final int cnt, final int timeShouldBe) {
        drawFrame(thisTimer, true);

        synchronized (ImagePanel.class) {
            if (timelined != null && timer == thisTimer) {
                int frameCount = timelined.getTimeline().getFrameCount();

                int oldFrame = frame;
                for (int i = 0; i < cnt; i++) {
                    if (!stillFrame && frameCount > 0) {
                        frame = (frame + 1) % frameCount;
                    }

                    if (!stillFrame && frame == frameCount - 1 && !loop) {
                        stopInternal();
                        return;
                    }

                    if (i < cnt - 1) {                 //skip not displayed frames, do not display, only play sounds, etc.
                        drawFrame(thisTimer, false);
                    }
                }
                if (frame != oldFrame) {
                    if (frame == 0) {
                        stopAllSounds();
                    }
                    time = 0;
                } else {
                    time = timeShouldBe;
                }
            }
        }

        fireMediaDisplayStateChanged();
    }

    private static SerializableImage getFrame(SWF swf, int frame, int time, Timelined drawable, RenderContext renderContext, int selectedDepth, double zoom) {
        Timeline timeline = drawable.getTimeline();
        //int mouseButton = renderContext.mouseButton;
        //Point cursorPosition = renderContext.cursorPosition;
        //String key = "drawable_" + frame + "_" + drawable.hashCode() + "_" + mouseButton + "_depth" + selectedDepth + "_" + (cursorPosition == null ? "out" : cursorPosition.hashCode()) + "_" + zoom + "_" + timeline.fontFrameNum;
        SerializableImage img;
        //SerializableImage img = swf.getFromCache(key);
        //if (img == null) {
        //boolean shouldCache = timeline.isSingleFrame(frame);
        RECT rect = drawable.getRect();

        int width = (int) (rect.getWidth() * zoom);
        int height = (int) (rect.getHeight() * zoom);
        SerializableImage image = new SerializableImage((int) Math.ceil(width / SWF.unitDivisor),
                (int) Math.ceil(height / SWF.unitDivisor), SerializableImage.TYPE_INT_ARGB);
        //renderContext.borderImage = new SerializableImage(image.getWidth(), image.getHeight(), SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        Matrix m = new Matrix();
        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
        m.scale(zoom);
        timeline.toImage(frame, time, renderContext, image, false, m, m, m, null);

        Graphics2D gg = (Graphics2D) image.getGraphics();
        gg.setStroke(new BasicStroke(3));
        gg.setPaint(Color.green);
        gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
        DepthState ds = null;
        if (timeline.getFrameCount() > frame) {
            ds = timeline.getFrame(frame).layers.get(selectedDepth);
        }

        if (ds != null) {
            CharacterTag cht = swf.getCharacter(ds.characterId);
            if (cht != null) {
                if (cht instanceof DrawableTag) {
                    DrawableTag dt = (DrawableTag) cht;
                    int drawableFrameCount = dt.getNumFrames();
                    if (drawableFrameCount == 0) {
                        drawableFrameCount = 1;
                    }

                    int dframe = time % drawableFrameCount;
                    Shape outline = dt.getOutline(dframe, time, ds.ratio, renderContext, Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m.concatenate(new Matrix(ds.matrix))), true);
                    Rectangle bounds = outline.getBounds();
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

        /*if (shouldCache) {
         swf.putToCache(key, img);
         }*/
        //}
        return img;
    }

    private Object execute(SWFInputStream sis) throws IOException {
        if (!Configuration.internalFlashViewerExecuteAs12.get()) {
            return Undefined.INSTANCE;
        }
        if (lda == null) {
            return Undefined.INSTANCE;
        }
        long ip = sis.getPos();
        //System.err.println("=============");
        Action a;
        while ((a = sis.readAction()) != null) {
            int actionLengthWithHeader = a.getTotalActionLength();
            a.setAddress(ip);
            a.execute(lda);
            /*System.err.print("" + a + ", stack: [");
             for (Object o : lda.stack) {
             System.err.print("" + o + ",");
             }
             System.err.println("]");*/
            if (lda.returnValue != null) {
                return lda.returnValue;
            }
            if (lda.jump != null) {
                ip = lda.jump;
                lda.jump = null;
            } else {
                ip += actionLengthWithHeader;
            }
            sis.seek(ip);
        }
        return Undefined.INSTANCE;
    }

    private void executeFrame(int frame) {
        if (!Configuration.internalFlashViewerExecuteAs12.get()) {
            return;
        }
        if (timelined == null) {
            return;
        }
        Frame f = timelined.getTimeline().getFrame(frame);
        List<DoActionTag> actions = f.actions;
        if (lda != null) {
            lda.clear();
        }
        for (DoActionTag src : actions) {
            try {
                ByteArrayRange actionBytes = src.getActionBytes();
                int prevLength = actionBytes.getPos();
                SWFInputStream rri = new SWFInputStream(swf, actionBytes.getArray(), 0, prevLength + actionBytes.getLength());
                if (prevLength != 0) {
                    rri.seek(prevLength);
                }
                execute(rri);
            } catch (IOException ex) {
                Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void drawFrame(Timer thisTimer, boolean display) {
        Timelined timelined;
        MouseEvent lastMouseEvent;
        int frame;
        int time;
        Point cursorPosition;
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
            cursorPosition = this.cursorPosition;
            if (cursorPosition != null) {
                cursorPosition = iconPanel.toImagePoint(cursorPosition);
            }

            mouseButton = this.mouseButton;
            selectedDepth = this.selectedDepth;
            zoom = this.zoom;
            swf = this.swf;
        }

        if (timelined == null) {
            return;
        }

        RenderContext renderContext = new RenderContext();
        renderContext.displayObjectCache = displayObjectCache;
        if (cursorPosition != null) {
            renderContext.cursorPosition = new Point((int) (cursorPosition.x * SWF.unitDivisor), (int) (cursorPosition.y * SWF.unitDivisor));
        }

        renderContext.mouseButton = mouseButton;
        renderContext.stateUnderCursor = new ArrayList<>();

        SerializableImage img;
        try {
            Timeline timeline = timelined.getTimeline();
            if (frame >= timeline.getFrameCount()) {
                return;
            }

            double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
            if (lowQuality) {
                zoomDouble /= LQ_FACTOR;
            }
            updatePos(timelined, lastMouseEvent, thisTimer);

            Matrix mat = new Matrix();
            mat.translateX = swf.displayRect.Xmin;
            mat.translateY = swf.displayRect.Ymin;

            img = null;
            if (display) {
                Stopwatch sw = Stopwatch.startNew();
                img = getFrame(swf, frame, time, timelined, renderContext, selectedDepth, zoomDouble);
                sw.stop();
                if (sw.getElapsedMilliseconds() > 100) {
                    logger.log(Level.WARNING, "Slow rendering. {0}. frame, time={1}, {2}ms", new Object[]{frame, time, sw.getElapsedMilliseconds()});
                }

                if (renderContext.borderImage != null) {
                    img = renderContext.borderImage;
                }
            }

            List<Integer> sounds = new ArrayList<>();
            List<String> soundClasses = new ArrayList<>();
            timeline.getSounds(frame, time, renderContext.mouseOverButton, mouseButton, sounds, soundClasses);
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
                    playSound(st, null, thisTimer);
                }
            }
            executeFrame(frame);
        } catch (Throwable ex) {
            // swf was closed during the rendering probably
            return;
        }
        if (display) {

            StringBuilder ret = new StringBuilder();

            if (cursorPosition != null) {
                ret.append(" [").append(cursorPosition.x).append(",").append(cursorPosition.y).append("] : ");
            }

            boolean handCursor = renderContext.mouseOverButton != null;
            boolean first = true;
            for (int i = renderContext.stateUnderCursor.size() - 1; i >= 0; i--) {
                DepthState ds = renderContext.stateUnderCursor.get(i);
                if (!first) {
                    ret.append(", ");
                }

                first = false;
                CharacterTag c = swf.getCharacter(ds.characterId);
                ret.append(c.toString());
            }

            if (first) {
                ret.append(" - ");
            }

            ButtonTag lastMouseOverButton;
            synchronized (ImagePanel.class) {
                if (timer == thisTimer) {
                    iconPanel.setImg(img);
                    lastMouseOverButton = iconPanel.mouseOverButton;
                    iconPanel.mouseOverButton = renderContext.mouseOverButton;
                    debugLabel.setText(ret.toString());
                    if (handCursor) {
                        iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else if (iconPanel.hasAllowMove()) {
                        iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }

                    if (lastMouseOverButton != renderContext.mouseOverButton) {
                        ButtonTag b = renderContext.mouseOverButton;
                        if (b != null) {
                            // New mouse entered
                            DefineButtonSoundTag sounds = b.getSounds();
                            if (sounds != null && sounds.buttonSoundChar1 != 0) { // IdleToOverUp
                                playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar1), sounds.buttonSoundInfo1, timer);
                            }
                        }

                        b = lastMouseOverButton;
                        if (b != null) {
                            // Old mouse leave
                            DefineButtonSoundTag sounds = b.getSounds();
                            if (sounds != null && sounds.buttonSoundChar0 != 0) { // OverUpToIdle
                                playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar0), sounds.buttonSoundInfo0, timer);
                            }
                        }
                    }

                    drawReady = true;
                    synchronized (delayObject) {
                        delayObject.notify();
                    }
                }
            }
        }
    }

    private void playSound(SoundTag st, SOUNDINFO soundInfo, Timer thisTimer) {
        final SoundTagPlayer sp;
        try {
            int loopCount = 1;
            if (soundInfo != null && soundInfo.hasLoops) {
                loopCount = Math.max(1, soundInfo.loopCount);
            }

            sp = new SoundTagPlayer(st, loopCount, false);
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

    private synchronized void setMsPerFrame(int val) {
        this.msPerFrame = val;
    }

    private synchronized int getMsPerFrame() {
        return this.msPerFrame;
    }

    private long startRun = 0L;

    private final long startDrop = 0L;

    private int skippedFrames = 0;

    private float fpsShouldBe = 0;

    private float fpsIs = 0;

    private Timer fpsTimer;

    private int startFrame = 0;

    private synchronized void setFpsIs(float val) {
        fpsIs = val;
    }

    private synchronized float getFpsIs() {
        return fpsIs;
    }

    private synchronized void setSkippedFrames(int val) {
        skippedFrames = val;
    }

    private synchronized void addSkippedFrames(int val) {
        skippedFrames += val;
    }

    private synchronized int getSkippedFrames() {
        return skippedFrames;
    }

    private synchronized int getAndResetSkippedFrames() {
        int ret = skippedFrames;
        skippedFrames = 0;
        return ret;
    }

    private void scheduleTask(boolean singleFrame, long msDelay) {
        TimerTask task = new TimerTask() {
            public final Timer thisTimer = timer;

            public final boolean isSingleFrame = singleFrame;

            private long lastRun = 0L;

            @Override
            public void run() {
                try {
                    synchronized (ImagePanel.class) {
                        if (timer != thisTimer) {
                            return;
                        }
                    }
                    lastRun = System.currentTimeMillis();
                    int curFrame = frame;
                    long delay = getMsPerFrame();
                    if (isSingleFrame) {
                        drawFrame(thisTimer, true);
                        synchronized (ImagePanel.class) {
                            thisTimer.cancel();
                            if (timer == thisTimer) {
                                timer = null;
                            }
                        }

                        fireMediaDisplayStateChanged();
                    } else {
                        //Time before drawing current frame
                        long frameTimeMsIs = System.currentTimeMillis();
                        //Total number of frames in this timeline
                        int frameCount = timelined.getTimeline().getFrameCount();
                        //How many ticks (= times where frame should be displayed in framerate) are there from hitting play button
                        int ticksFromStart = (int) Math.floor((frameTimeMsIs - startRun) / (double) getMsPerFrame()) + 1;
                        //Add ticks to first frame when hitting play button, ignoring total framecount => this value can be larger than number of frames in timeline
                        int frameOverMaxShouldBeNow = startFrame + ticksFromStart;
                        //Apply maximum frames repating, this is actual frame which should be drawed now
                        int frameShouldBeNow = frameOverMaxShouldBeNow % frameCount;

                        //How many frames are there between last displayed frame and now. For perfect display(=no framedrop), value should be 1
                        int skipFrames = frameShouldBeNow - curFrame;
                        //It is negative for some reason, this will display older frames. Add frameCount to stay in modulu framecount.
                        if (skipFrames < 0) {
                            skipFrames += frameCount;
                        }
                        //Change for more than 1 frame
                        if (skipFrames > 1) {
                            addSkippedFrames(skipFrames - 1); //drop those frames, draw only last one
                        }
                        //Frame "time" - ticks in current frame
                        int currentFrameTicks = 0;
                        if (frameCount == 1) { //We have only one frame, so the ticks on that frame equal ticks on whole timeline
                            currentFrameTicks = ticksFromStart;
                        }
                        nextFrame(thisTimer, skipFrames, currentFrameTicks);

                        long afterDrawFrameTimeMsIs = System.currentTimeMillis();

                        int nextFrameOverMax = frameOverMaxShouldBeNow;
                        while (delay < 0) { //while the frame time already passed
                            nextFrameOverMax++;
                            long nextFrameOverMaxTimeMsShouldBe = startRun + getMsPerFrame() * nextFrameOverMax;
                            delay = nextFrameOverMaxTimeMsShouldBe - afterDrawFrameTimeMsIs;
                        }
                    }
                    //schedule next run of the task
                    scheduleTask(isSingleFrame, delay);

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Frame drawing error", ex);
                }
            }
        };
        if (timer != null) {
            timer.schedule(task, msDelay);
        }
    }

    private synchronized void startTimer(Timeline timeline, boolean playing) {

        startRun = System.currentTimeMillis();
        startFrame = frame;
        float frameRate = timeline.frameRate;
        setMsPerFrame(frameRate == 0 ? 1000 : (int) (1000.0 / frameRate));
        final boolean singleFrame = !playing
                || (stillFrame && timeline.isSingleFrame(frame))
                || (!stillFrame && timeline.getRealFrameCount() <= 1 && timeline.isSingleFrame());

        if (fpsTimer == null) {
            fpsTimer = new Timer();
            fpsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    float skipped = getAndResetSkippedFrames();
                    setFpsIs(fpsShouldBe - skipped);
                }
            }, 1000, 1000);
        }
        timer = new Timer();
        fpsShouldBe = timeline.frameRate;
        fpsIs = fpsShouldBe;
        scheduleTask(singleFrame, 0);
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
