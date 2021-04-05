/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.base.DisplayObjectCacheKey;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.Stopwatch;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

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
    private int prevFrame = -1;

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

    private final Cache<DisplayObjectCacheKey, SerializableImage> displayObjectCache = Cache.getInstance(false, false, "displayObject");

    private final IconPanel iconPanel;

    private int time = 0;

    private int selectedDepth = -1;

    private int freeTransformDepth = -1;

    private Zoom zoom = new Zoom();

    private final Object delayObject = new Object();

    private boolean drawReady;

    private final int drawWaitLimit = 50; // ms

    private TextTag textTag;

    private TextTag newTextTag;

    private int msPerFrame;

    private final boolean lowQuality = false;

    private Object lock = new Object();

    private Point2D registrationPoint = null;
    private Point2D registrationPointUpdated = null;

    private int mode = Cursor.DEFAULT_CURSOR;
    private Rectangle2D bounds;

    private Matrix transform;
    private AffineTransform transformUpdated;

    private final double LQ_FACTOR = 2;

    private static final int TOLERANCE_SCALESHEAR = 8;

    private static final int TOLERANCE_ROTATE = 30;
    private static final int REGISTRATION_TOLERANCE = 8;

    private static final double CENTER_POINT_SIZE = 8;

    private static final double HANDLES_WIDTH = 5;

    private static final int HANDLES_STROKE_WIDTH = 2;

    private static final int MODE_ROTATE_NE = -1;
    private static final int MODE_ROTATE_SE = -2;
    private static final int MODE_ROTATE_NW = -3;
    private static final int MODE_ROTATE_SW = -4;

    private static final int MODE_SHEAR_S = -5;
    private static final int MODE_SHEAR_E = -6;

    private static final int MODE_SHEAR_N = -7;
    private static final int MODE_SHEAR_W = -8;

    private static Cursor moveCursor;
    private static Cursor moveRegPointCursor;
    private static Cursor resizeNWSECursor;
    private static Cursor resizeSWNECursor;
    private static Cursor resizeXCursor;
    private static Cursor resizeYCursor;
    private static Cursor rotateCursor;
    private static Cursor selectCursor;
    private static Cursor shearXCursor;
    private static Cursor shearYCursor;

    private Point offsetPoint = new Point(0, 0);
    private Rectangle _rect = null;

    private ExportRectangle _viewRect = new ExportRectangle(0, 0, 1, 1);

    private boolean playing = false;

    private static Cursor loadCursor(String name, int x, int y) throws IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = ImageIO.read(MainPanel.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/cursors/" + name + ".png"));
        return toolkit.createCustomCursor(image, new Point(x, y), name);
    }

    static {
        try {
            moveCursor = loadCursor("move", 0, 0);
            moveRegPointCursor = loadCursor("move_regpoint", 0, 0);
            resizeNWSECursor = loadCursor("resize_nw_se", 5, 5);
            resizeSWNECursor = loadCursor("resize_sw_ne", 5, 5);
            resizeXCursor = loadCursor("resize_x", 7, 4);
            resizeYCursor = loadCursor("resize_y", 4, 7);
            rotateCursor = loadCursor("rotate", 10, 7);
            selectCursor = loadCursor("select", 0, 0);
            shearXCursor = loadCursor("shear_x", 9, 5);
            shearYCursor = loadCursor("shear_y", 5, 9);

        } catch (IOException ex) {
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MATRIX getNewMatrix() {
        synchronized (lock) {
            DepthState ds = null;
            Timeline timeline = timelined.getTimeline();
            if (freeTransformDepth > -1 && timeline.getFrameCount() > frame) {
                ds = timeline.getFrame(frame).layers.get(freeTransformDepth);
            }

            if (ds != null) {
                CharacterTag cht = swf.getCharacter(ds.characterId);
                if (cht != null) {
                    if (cht instanceof DrawableTag) {
                        RECT rect = timelined.getRect();
                        Matrix m = new Matrix();
                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                        if (lowQuality) {
                            zoomDouble /= LQ_FACTOR;
                        }
                        double zoom = zoomDouble;
                        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
                        m.scale(zoom);

                        Matrix eMatrix = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).inverse();

                        return transform.preConcatenate(eMatrix).toMATRIX();
                    }
                }
            }
            return null;
        }
    }

    public synchronized void selectDepth(int depth) {
        if (depth != selectedDepth) {
            this.selectedDepth = depth;
            freeTransformDepth = -1;
        }

        hideMouseSelection();
        redraw();
    }

    private void calculateFreeTransform() {
        DepthState ds = null;
        Timeline timeline = timelined.getTimeline();
        if (freeTransformDepth > -1 && timeline.getFrameCount() > frame) {
            ds = timeline.getFrame(frame).layers.get(freeTransformDepth);
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
                    Matrix m = new Matrix();
                    double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                    if (lowQuality) {
                        zoomDouble /= LQ_FACTOR;
                    }
                    double zoom = zoomDouble;
                    m.translate(-_viewRect.xMin * zoom, -_viewRect.yMin * zoom);
                    m.scale(zoom);

                    transform = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).concatenate(new Matrix(ds.matrix));
                }
            }
        }
    }

    public synchronized void freeTransformDepth(int depth) {
        if (depth != freeTransformDepth) {
            this.freeTransformDepth = depth;
        }
        registrationPoint = null;
        calculateFreeTransform();
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

    @Override
    public Color getBackgroundColor() {
        if (swf != null && swf.getBackgroundColor() != null) {
            return swf.getBackgroundColor().backgroundColor.toColor();
        }
        return Color.white;
    }

    private class IconPanel extends JPanel {

        private SerializableImage _img;

        private ButtonTag mouseOverButton = null;

        private boolean autoFit = false;

        private boolean allowMove = true;

        private Point dragStart = null;

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
            /*if (img == null) {
                return;
            }*/

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
                    if (rect != null && img != null) {
                        int x = rect.x < 0 ? 0 : rect.x;
                        int y = rect.y < 0 ? 0 : rect.y;
                        g2.drawImage(img.getBufferedImage(), x, y, x + img.getWidth(), y + img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);

                        /*g2.setColor(Color.red);
                        g2.drawRect(x, y, img.getWidth(), img.getHeight());*/
                    }
                } finally {
                    if (g2 != null) {
                        g2.dispose();
                    }
                }

            } while (ri.contentsLost());
        }

        private boolean ctrlDown = false;

        public IconPanel() {

            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if ((e.getID() == KeyEvent.KEY_PRESSED) || (e.getID() == KeyEvent.KEY_RELEASED)) {
                        ctrlDown = ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
                    }
                    return false;
                }
            });

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

                    calcRect();
                    render();
                    repaint();
                }
            });

            MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        dragStart = e.getPoint();
                    }
                    requestFocusInWindow();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        dragStart = null;

                        if (freeTransformDepth > -1 && mode != Cursor.DEFAULT_CURSOR && registrationPointUpdated != null && transformUpdated != null) {
                            synchronized (lock) {
                                registrationPoint = new Point2D.Double(registrationPointUpdated.getX(), registrationPointUpdated.getY());
                                transform = new Matrix(transformUpdated);
                                transformUpdated = null;
                            }
                            repaint();
                        }
                        mode = Cursor.DEFAULT_CURSOR;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart != null && allowMove && mode == Cursor.DEFAULT_CURSOR) {
                        Point dragEnd = e.getPoint();
                        Point delta = new Point(dragEnd.x - dragStart.x, dragEnd.y - dragStart.y);
                        offsetPoint.x += delta.x;
                        offsetPoint.y += delta.y;
                        ExportRectangle oldViewRect = new ExportRectangle(_viewRect);
                        dragStart = dragEnd;
                        iconPanel.calcRect();
                        _viewRect = getViewRect();

                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

                        synchronized (lock) {

                            if (transform != null) {
                                Matrix prevTransform = transform.clone();

                                Matrix m = new Matrix();
                                m.scale(1 / SWF.unitDivisor);
                                m.translate(-oldViewRect.xMin * zoomDouble, -oldViewRect.yMin * zoomDouble);
                                //m.scale(zoomDouble);
                                Matrix mi = m.inverse();

                                transform = transform.preConcatenate(mi);

                                Matrix m2 = new Matrix();
                                m2.scale(1 / SWF.unitDivisor);
                                m2.translate(-_viewRect.xMin * zoomDouble, -_viewRect.yMin * zoomDouble);
                                //m2.scale(zoomDouble);

                                transform = transform.preConcatenate(m2);
                                if (registrationPoint != null) {

                                    double deltaX = ((oldViewRect.xMin - _viewRect.xMin) * zoomDouble / SWF.unitDivisor);
                                    double deltaY = ((oldViewRect.yMin - _viewRect.yMin) * zoomDouble / SWF.unitDivisor);

                                    registrationPoint = new Point2D.Double(registrationPoint.getX() + deltaX, registrationPoint.getY() + deltaY);
                                }
                            }

                        }
                        repaint();
                        return;
                    }

                    if (dragStart != null && freeTransformDepth > -1) {
                        if (transform == null) {
                            return;
                        }
                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

                        int ex = e.getX() - _rect.x - (int) (_viewRect.xMin * zoomDouble / SWF.unitDivisor);
                        int ey = e.getY() - _rect.y - (int) (_viewRect.yMin * zoomDouble / SWF.unitDivisor);
                        int dsx = dragStart.x - _rect.x - (int) (_viewRect.xMin * zoomDouble / SWF.unitDivisor);
                        int dsy = dragStart.y - _rect.y - (int) (_viewRect.yMin * zoomDouble / SWF.unitDivisor);
                        if (mode == MODE_SHEAR_N) {

                            double shearX = -(ex - dsx) / (bounds.getHeight());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(bounds.getX(), bounds.getY());
                            t.shear(shearX, 0);
                            t.translate(-bounds.getX(), -bounds.getY());
                            t.translate(ex - dsx, 0);

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == MODE_SHEAR_S) {

                            double shearX = (ex - dsx) / (bounds.getHeight());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(bounds.getX(), bounds.getY());
                            t.shear(shearX, 0);
                            t.translate(-bounds.getX(), -bounds.getY());

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_SHEAR_W) {
                            double shearY = -(ey - dsy) / (bounds.getWidth());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(bounds.getX(), bounds.getY());
                            t.shear(0, shearY);
                            t.translate(-bounds.getX(), -bounds.getY());
                            t.translate(0, ey - dsy);

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == MODE_SHEAR_E) {
                            double shearY = (ey - dsy) / (bounds.getWidth());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(bounds.getX(), bounds.getY());
                            t.shear(0, shearY);
                            t.translate(-bounds.getX(), -bounds.getY());

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_ROTATE_SE) {
                            double deltaStartX = Math.abs(dsx - registrationPoint.getX());
                            double deltaStartY = Math.abs(dsy - registrationPoint.getY());

                            double deltaEndX = Math.abs(ex - registrationPoint.getX());
                            double deltaEndY = Math.abs(ey - registrationPoint.getY());

                            double deltaTheta = 0;
                            if (ex >= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //same
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaEnd - thetaStart;
                            } else if (ex >= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //anti clockwise
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = -(thetaStart + thetaEnd);
                            } else if (ex <= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //clock wise
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = thetaStart + thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //opposite
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart + Math.toRadians(90) + thetaEnd;
                            }
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.rotate(deltaTheta, registrationPoint.getX(), registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_ROTATE_NW) {
                            double deltaStartX = Math.abs(dsx - registrationPoint.getX());
                            double deltaStartY = Math.abs(dsy - registrationPoint.getY());

                            double deltaEndX = Math.abs(ex - registrationPoint.getX());
                            double deltaEndY = Math.abs(ey - registrationPoint.getY());

                            double deltaTheta = 0;
                            if (ex >= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //opposite
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart + Math.toRadians(90) + thetaEnd;
                            } else if (ex >= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //clock wise
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = thetaStart + thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //anti clockwise
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = -(thetaStart + thetaEnd);
                            } else if (ex <= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //same
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaEnd - thetaStart;
                            }
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.rotate(deltaTheta, registrationPoint.getX(), registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_ROTATE_NE) {
                            double deltaStartX = Math.abs(dsx - registrationPoint.getX());
                            double deltaStartY = Math.abs(dsy - registrationPoint.getY());

                            double deltaEndX = Math.abs(ex - registrationPoint.getX());
                            double deltaEndY = Math.abs(ey - registrationPoint.getY());

                            double deltaTheta = 0;
                            if (ex >= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //clock wise
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart + thetaEnd;
                            } else if (ex >= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //same
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart - thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //opposite
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = thetaStart + Math.toRadians(90) + thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //anti clockwise
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = -(thetaStart + thetaEnd);
                            }
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.rotate(deltaTheta, registrationPoint.getX(), registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_ROTATE_SW) {
                            double deltaStartX = Math.abs(dsx - registrationPoint.getX());
                            double deltaStartY = Math.abs(dsy - registrationPoint.getY());

                            double deltaEndX = Math.abs(ex - registrationPoint.getX());
                            double deltaEndY = Math.abs(ey - registrationPoint.getY());

                            double deltaTheta = 0;
                            if (ex >= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //anti clockwise
                                double thetaStart = Math.atan(deltaStartX / deltaStartY);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = -(thetaStart + thetaEnd);
                            } else if (ex >= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //opposite
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndX / deltaEndY);
                                deltaTheta = thetaStart + Math.toRadians(90) + thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey >= registrationPoint.getY()) {
                                //same
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart - thetaEnd;
                            } else if (ex <= registrationPoint.getX() && ey <= registrationPoint.getY()) {
                                //clock wise
                                double thetaStart = Math.atan(deltaStartY / deltaStartX);
                                double thetaEnd = Math.atan(deltaEndY / deltaEndX);
                                deltaTheta = thetaStart + thetaEnd;
                            }
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.rotate(deltaTheta, registrationPoint.getX(), registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.HAND_CURSOR) {
                            transformUpdated = new AffineTransform(transform.toTransform());
                            registrationPointUpdated = new Point2D.Double(ex, ey);
                            repaint();
                        }
                        if (mode == Cursor.MOVE_CURSOR) {
                            int deltaX = ex - dsx;
                            int deltaY = ey - dsy;

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(deltaX, deltaY);
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.E_RESIZE_CURSOR) {
                            double deltaBefore = bounds.getX() + bounds.getWidth() - registrationPoint.getX();
                            double deltaX = ex - registrationPoint.getX();
                            double scaleX = deltaX / deltaBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), 0);
                            t.scale(scaleX, 1);
                            t.translate(-registrationPoint.getX(), 0);
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == Cursor.W_RESIZE_CURSOR) {
                            double deltaBefore = registrationPoint.getX() - bounds.getX();
                            double deltaX = registrationPoint.getX() - ex;
                            double scaleX = deltaX / deltaBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), 0);
                            t.scale(scaleX, 1);
                            t.translate(-registrationPoint.getX(), 0);
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.S_RESIZE_CURSOR) {
                            double deltaBefore = bounds.getY() + bounds.getHeight() - registrationPoint.getY();
                            double deltaY = ey - registrationPoint.getY();
                            double scaleY = deltaY / deltaBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(0, registrationPoint.getY());
                            t.scale(1, scaleY);
                            t.translate(0, -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == Cursor.N_RESIZE_CURSOR) {
                            double deltaBefore = registrationPoint.getY() - bounds.getY();
                            double deltaY = registrationPoint.getY() - ey;
                            double scaleY = deltaY / deltaBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(0, registrationPoint.getY());
                            t.scale(1, scaleY);
                            t.translate(0, -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == Cursor.SE_RESIZE_CURSOR) {
                            double deltaXBefore = bounds.getX() + bounds.getWidth() - registrationPoint.getX();
                            double deltaYBefore = bounds.getY() + bounds.getHeight() - registrationPoint.getY();
                            double deltaX = ex - registrationPoint.getX();
                            double deltaY = ey - registrationPoint.getY();
                            double scaleX = deltaX / deltaXBefore;
                            double scaleY = deltaY / deltaYBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), registrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-registrationPoint.getX(), -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.NE_RESIZE_CURSOR) {
                            double deltaXBefore = bounds.getX() + bounds.getWidth() - registrationPoint.getX();
                            double deltaYBefore = registrationPoint.getY() - bounds.getY();
                            double deltaX = ex - registrationPoint.getX();
                            double deltaY = registrationPoint.getY() - ey;
                            double scaleX = deltaX / deltaXBefore;
                            double scaleY = deltaY / deltaYBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), registrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-registrationPoint.getX(), -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.SW_RESIZE_CURSOR) {
                            double deltaXBefore = registrationPoint.getX() - bounds.getX();
                            double deltaYBefore = bounds.getY() + bounds.getHeight() - registrationPoint.getY();
                            double deltaX = registrationPoint.getX() - ex;
                            double deltaY = ey - registrationPoint.getY();
                            double scaleX = deltaX / deltaXBefore;
                            double scaleY = deltaY / deltaYBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), registrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-registrationPoint.getX(), -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == Cursor.NW_RESIZE_CURSOR) {
                            double deltaXBefore = registrationPoint.getX() - bounds.getX();
                            double deltaYBefore = registrationPoint.getY() - bounds.getY();
                            double deltaX = registrationPoint.getX() - ex;
                            double deltaY = registrationPoint.getY() - ey;
                            double scaleX = deltaX / deltaXBefore;
                            double scaleY = deltaY / deltaYBefore;
                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            t.translate(registrationPoint.getX(), registrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-registrationPoint.getX(), -registrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Point2D.Double();
                            t.transform(registrationPoint, newRegistrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (freeTransformDepth > -1) {
                        if (bounds == null) {
                            return;
                        }
                        if (registrationPoint == null) {
                            return;
                        }

                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                        int ex = e.getX() - _rect.x - (int) (_viewRect.xMin * zoomDouble / SWF.unitDivisor);
                        int ey = e.getY() - _rect.y - (int) (_viewRect.yMin * zoomDouble / SWF.unitDivisor);

                        boolean left = ex >= bounds.getX() - TOLERANCE_SCALESHEAR && ex <= bounds.getX() + TOLERANCE_SCALESHEAR;
                        boolean right = ex >= bounds.getX() + bounds.getWidth() - TOLERANCE_SCALESHEAR && ex <= bounds.getX() + bounds.getWidth() + TOLERANCE_SCALESHEAR;
                        boolean top = ey >= bounds.getY() - TOLERANCE_SCALESHEAR && ey <= bounds.getY() + TOLERANCE_SCALESHEAR;
                        boolean bottom = ey >= bounds.getY() + bounds.getHeight() - TOLERANCE_SCALESHEAR && ey <= bounds.getY() + bounds.getHeight() + TOLERANCE_SCALESHEAR;

                        boolean xcenter = ex >= bounds.getCenterX() - TOLERANCE_SCALESHEAR && ex <= bounds.getCenterX() + TOLERANCE_SCALESHEAR;
                        boolean ycenter = ey >= bounds.getCenterY() - TOLERANCE_SCALESHEAR && ey <= bounds.getCenterY() + TOLERANCE_SCALESHEAR;

                        boolean registration = ex >= registrationPoint.getX() - REGISTRATION_TOLERANCE
                                && ex <= registrationPoint.getX() + REGISTRATION_TOLERANCE
                                && ey >= registrationPoint.getY() - REGISTRATION_TOLERANCE
                                && ey <= registrationPoint.getY() + REGISTRATION_TOLERANCE;

                        boolean rightRotate = ex > bounds.getX() + bounds.getWidth() - TOLERANCE_ROTATE && ex
                                <= bounds.getX() + bounds.getWidth() + TOLERANCE_ROTATE;
                        boolean bottomRotate = ey > bounds.getY() + bounds.getHeight() - TOLERANCE_ROTATE && ey
                                <= bounds.getY() + bounds.getHeight() + TOLERANCE_ROTATE;

                        boolean leftRotate = ex < bounds.getX() + TOLERANCE_ROTATE
                                && ex >= bounds.getX() - TOLERANCE_ROTATE;

                        boolean topRotate = ey < bounds.getY() + TOLERANCE_ROTATE
                                && ey >= bounds.getY() - TOLERANCE_ROTATE;

                        boolean inBounds = bounds.contains(ex, ey);

                        boolean shearX = ex > bounds.getX() && ex < bounds.getX() + bounds.getWidth();
                        boolean shearY = ey > bounds.getY() && ey < bounds.getY() + bounds.getHeight();

                        Cursor cursor;
                        if (top && left) {
                            mode = Cursor.NW_RESIZE_CURSOR;
                            cursor = resizeNWSECursor;
                        } else if (bottom && left) {
                            mode = Cursor.SW_RESIZE_CURSOR;
                            cursor = resizeSWNECursor;
                        } else if (top && right) {
                            mode = Cursor.NE_RESIZE_CURSOR;
                            cursor = resizeSWNECursor;
                        } else if (bottom && right) {
                            mode = Cursor.SE_RESIZE_CURSOR;
                            cursor = resizeNWSECursor;
                        } else if (top && xcenter) {
                            mode = Cursor.N_RESIZE_CURSOR;
                            cursor = resizeYCursor;
                        } else if (bottom && xcenter) {
                            mode = Cursor.S_RESIZE_CURSOR;
                            cursor = resizeYCursor;
                        } else if (left && ycenter) {
                            mode = Cursor.W_RESIZE_CURSOR;
                            cursor = resizeXCursor;
                        } else if (right && ycenter) {
                            mode = Cursor.E_RESIZE_CURSOR;
                            cursor = resizeXCursor;
                        } else if (registration) {
                            mode = Cursor.HAND_CURSOR;
                            cursor = moveRegPointCursor;
                        } else if (!inBounds && rightRotate && topRotate) {
                            mode = MODE_ROTATE_NE;
                            cursor = rotateCursor;
                        } else if (!inBounds && rightRotate && bottomRotate) {
                            mode = MODE_ROTATE_SE;
                            cursor = rotateCursor;
                        } else if (!inBounds && leftRotate && topRotate) {
                            mode = MODE_ROTATE_NW;
                            cursor = rotateCursor;
                        } else if (!inBounds && leftRotate && bottomRotate) {
                            mode = MODE_ROTATE_SW;
                            cursor = rotateCursor;
                        } else if (shearY && (left || right)) {
                            if (left) {
                                mode = MODE_SHEAR_W;
                            } else {
                                mode = MODE_SHEAR_E;
                            }
                            cursor = shearYCursor;
                        } else if (shearX && (top || bottom)) {
                            if (top) {
                                mode = MODE_SHEAR_N;
                            } else {
                                mode = MODE_SHEAR_S;
                            }
                            cursor = shearXCursor;
                        } else if (inBounds) {
                            mode = Cursor.MOVE_CURSOR;
                            cursor = moveCursor;
                        } else {
                            mode = Cursor.DEFAULT_CURSOR;
                            cursor = selectCursor;
                        }

                        setCursor(cursor);
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (ctrlDown) {
                        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                            int rotation = e.getWheelRotation();
                            if (rotation < 0) {
                                zoomIn();
                            } else {
                                zoomOut();
                            }
                        }
                    }
                }

            };
            addMouseListener(mouseInputAdapter);
            addMouseMotionListener(mouseInputAdapter);
            addMouseWheelListener(mouseInputAdapter);
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
            calcRect();
            render();
            repaint();
        }

        public synchronized Point toImagePoint(Point p) {
            if (_img == null) {
                return null;
            }

            if (timelined != null) {
                double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                int w1 = (int) (timelined.getRect().Xmin * zoomDouble / SWF.unitDivisor);
                int h1 = (int) (timelined.getRect().Ymin * zoomDouble / SWF.unitDivisor);
                return new Point((p.x - _rect.x + w1), (p.y - _rect.y + h1));
            }
            return new Point((p.x - _rect.x), (p.y - _rect.y));
        }

        private void setAllowMove(boolean allowMove) {
            this.allowMove = allowMove;
            if (!allowMove) {
                offsetPoint = new Point();
            }
        }

        private synchronized void calcRect() {
            _rect = calcRect(zoom);
        }

        private synchronized Rectangle calcRect(Zoom z) {
            if (_img != null || timelined != null) {
                //int w1 = (int) (_img.getWidth() * (lowQuality ? LQ_FACTOR : 1));
                //int h1 = (int) (_img.getHeight() * (lowQuality ? LQ_FACTOR : 1));
                double zoomDouble = z.fit ? getZoomToFit() : z.value;
                int w1;
                int h1;
                if (timelined != null) {
                    w1 = (int) (timelined.getRectWithStrokes().getWidth() * zoomDouble / SWF.unitDivisor);
                    h1 = (int) (timelined.getRectWithStrokes().getHeight() * zoomDouble / SWF.unitDivisor);
                } else {
                    w1 = (int) (_img.getWidth() * (lowQuality ? LQ_FACTOR : 1));
                    h1 = (int) (_img.getHeight() * (lowQuality ? LQ_FACTOR : 1));
                }

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
                Rectangle r2 = new Rectangle(getWidth() / 2 - w / 2 + offsetPoint.x, getHeight() / 2 - h / 2 + offsetPoint.y, w, h);
                return r2;
            }
            return null;
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
        if (selectedDepth > -1 && frame > -1 && timelined != null) {
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
                    if (button != null && freeTransformDepth == -1) {
                        DefineButtonSoundTag sounds = button.getSounds();
                        if (sounds != null && sounds.buttonSoundChar2 != 0) { // OverUpToOverDown
                            playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar2), sounds.buttonSoundInfo2, timer);
                        }
                        List<ByteArrayRange> actions = new ArrayList<>();
                        if (button instanceof DefineButton2Tag) {
                            DefineButton2Tag button2 = (DefineButton2Tag) button;
                            for (BUTTONCONDACTION ca : button2.actions) {
                                if (ca.condOverUpToOverDown) { //press
                                    actions.add(ca.actionBytes);
                                }
                            }
                        }
                        if (button instanceof DefineButtonTag) {
                            DefineButtonTag button1 = (DefineButtonTag) button;
                            actions.add(button1.actionBytes);
                        }

                        for (ByteArrayRange actionBytes : actions) {
                            try {
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
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (ImagePanel.class) {
                    mouseButton = 0;
                    lastMouseEvent = e;
                    redraw();
                    ButtonTag button = iconPanel.mouseOverButton;
                    if (button != null && freeTransformDepth == -1) {
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
        double zoomDoubleBefore = this.zoom.fit ? getZoomToFit() : this.zoom.value;

        boolean modified = this.zoom.value != zoom.value || this.zoom.fit != zoom.fit;
        if (modified) {
            ExportRectangle oldViewRect = new ExportRectangle(_viewRect);
            Rectangle oldRect = new Rectangle(_rect);
            this.zoom = zoom;
            displayObjectCache.clear();
            double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
            offsetPoint.x = (int) (offsetPoint.x * zoomDouble / zoomDoubleBefore);
            offsetPoint.y = (int) (offsetPoint.y * zoomDouble / zoomDoubleBefore);
            iconPanel.calcRect();
            _viewRect = getViewRect();
            synchronized (lock) {
                if (transform != null) {

                    Matrix m = new Matrix();
                    m.scale(1 / SWF.unitDivisor);
                    m.translate(-oldViewRect.xMin * zoomDoubleBefore, -oldViewRect.yMin * zoomDoubleBefore);
                    m.scale(zoomDoubleBefore);
                    Matrix mi = m.inverse();

                    transform = transform.preConcatenate(mi);

                    Matrix m2 = new Matrix();
                    m2.scale(1 / SWF.unitDivisor);
                    m2.translate(-_viewRect.xMin * zoomDouble, -_viewRect.yMin * zoomDouble);
                    m2.scale(zoomDouble);

                    transform = transform.preConcatenate(m2);

                    if (registrationPoint != null) {
                        double actualRegPointX = (oldViewRect.xMin * zoomDoubleBefore / SWF.unitDivisor + registrationPoint.getX());
                        double actualRegPointY = (oldViewRect.yMin * zoomDoubleBefore / SWF.unitDivisor + registrationPoint.getY());
                        double sizedRegPointX = (actualRegPointX * zoomDouble / zoomDoubleBefore);
                        double sizedRegPointY = (actualRegPointY * zoomDouble / zoomDoubleBefore);

                        double regPointX = (sizedRegPointX - _viewRect.xMin * zoomDouble / SWF.unitDivisor);
                        double regPointY = (sizedRegPointY - _viewRect.yMin * zoomDouble / SWF.unitDivisor);

                        registrationPoint = new Point2D.Double(regPointX, regPointY);
                    }
                }

            }

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
            RECT bounds = timelined.getRectWithStrokes();
            double w1 = bounds.getWidth() / SWF.unitDivisor;
            double h1 = bounds.getHeight() / SWF.unitDivisor;

            double w2 = iconPanel.getWidth();
            double h2 = iconPanel.getHeight();

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
            this.prevFrame = -1;

            loaded = true;

            if (drawable.getTimeline().getFrameCount() == 0) {
                clearImagePanel();
                fireMediaDisplayStateChanged();
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
        int width = (int) (rect.Xmax * zoomDouble);
        int height = (int) (rect.Ymax * zoomDouble);
        SerializableImage image = new SerializableImage((int) (width / SWF.unitDivisor) + 1,
                (int) (height / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        Matrix m = Matrix.getTranslateInstance(-rect.Xmin * zoomDouble, -rect.Ymin * zoomDouble);
        m.scale(zoomDouble);
        textTag.toImage(0, 0, 0, new RenderContext(), image, image, false, m, m, m, m, new ConstantColorColorTransform(0xFFC0C0C0), zoomDouble, false, new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL);

        if (newTextTag != null) {
            newTextTag.toImage(0, 0, 0, new RenderContext(), image, image, false, m, m, m, m, new ConstantColorColorTransform(0xFF000000), zoomDouble, false, new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL);
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
        return frame + 1;
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
        fireMediaDisplayStateChanged();
    }

    @Override
    public void stop() {
        stopInternal();
        rewind();
        redraw();
        fireMediaDisplayStateChanged();
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
            Timer ptimer = timer;
            timer = null;
            ptimer.cancel();
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

    private static SerializableImage getFrame(ExportRectangle viewRect, SWF swf, int frame, int time, Timelined drawable, RenderContext renderContext, int selectedDepth, int freeTransformDepth, double zoom, Reference<Point2D> registrationPointRef, Reference<Rectangle2D> boundsRef, Matrix transform, Matrix temporaryMatrix) {
        Timeline timeline = drawable.getTimeline();
        //int mouseButton = renderContext.mouseButton;
        //Point cursorPosition = renderContext.cursorPosition;
        //String key = "drawable_" + frame + "_" + drawable.hashCode() + "_" + mouseButton + "_depth" + selectedDepth + "_" + (cursorPosition == null ? "out" : cursorPosition.hashCode()) + "_" + zoom + "_" + timeline.fontFrameNum;
        SerializableImage img;
        //SerializableImage img = swf.getFromCache(key);
        //if (img == null) {
        //boolean shouldCache = timeline.isSingleFrame(frame);
        RECT rect = drawable.getRect();

        int width = (int) (viewRect.getWidth() * zoom); //int) (rect.getWidth() * zoom);
        int height = (int) (viewRect.getHeight() * zoom);//(int) (rect.getHeight() * zoom);(int) (rect.getHeight() * zoom);
        if (width == 0) {
            width = 1;
        }
        if (height == 0) {
            height = 1;
        }
        SerializableImage image = new SerializableImage((int) Math.ceil(width / SWF.unitDivisor),
                (int) Math.ceil(height / SWF.unitDivisor), SerializableImage.TYPE_INT_ARGB);
        //renderContext.borderImage = new SerializableImage(image.getWidth(), image.getHeight(), SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();

        Matrix m = new Matrix();
        m.translate(-viewRect.xMin * zoom, -viewRect.yMin * zoom);
        m.scale(zoom);

        Matrix fullM = m.clone();

        MATRIX oldMatrix = null;
        if (freeTransformDepth > -1) {

            Matrix eMatrix = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).inverse();

            MATRIX newMatrix = transform.preConcatenate(eMatrix).toMATRIX();
            oldMatrix = timeline.getFrame(frame).layers.get(freeTransformDepth).matrix;

            timeline.getFrame(frame).layers.get(freeTransformDepth).matrix = newMatrix;
        }

        timeline.toImage(frame, time, renderContext, image, image, false, m, new Matrix(), m, null, zoom, false, viewRect, fullM, true, Timeline.DRAW_MODE_ALL);

        Graphics2D gg = (Graphics2D) image.getGraphics();
        gg.setStroke(new BasicStroke(3));
        gg.setPaint(Color.green);
        gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
        DepthState ds = null;
        if (selectedDepth > -1 && timeline.getFrameCount() > frame) {
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

        ds = null;
        if (freeTransformDepth > -1 && timeline.getFrameCount() > frame) {
            ds = timeline.getFrame(frame).layers.get(freeTransformDepth);
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
                    //Matrix finalMatrix = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).concatenate(new Matrix(ds.matrix));
                    Shape outline = dt.getOutline(dframe, time, ds.ratio, renderContext, transform, true);

                    if (temporaryMatrix != null) {
                        Shape tempOutline = dt.getOutline(dframe, time, ds.ratio, renderContext, temporaryMatrix, true);
                        gg.setStroke(new BasicStroke(1));
                        gg.setPaint(Color.black);
                        gg.draw(tempOutline);
                    }

                    Rectangle bounds = outline.getBounds();
                    boundsRef.setVal(bounds);
                    gg.setStroke(new BasicStroke(1));
                    gg.setPaint(Color.black);
                    gg.draw(bounds);
                    drawHandles(gg, bounds);

                    if (registrationPointRef.getVal() == null) {
                        registrationPointRef.setVal(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()));
                    }
                    drawRegistrationPoint(gg, registrationPointRef.getVal());
                }
            }
        }

        if (freeTransformDepth > -1 && timeline != null && timeline.getFrameCount() > frame) {
            timeline.getFrame(frame).layers.get(freeTransformDepth).matrix = oldMatrix;
        }
        img = image;

        /*if (shouldCache) {
         swf.putToCache(key, img);
         }*/
        //}
        return img;
    }

    private static void drawRegistrationPoint(Graphics2D g2, Point2D registrationPoint) {
        Stroke stroke = new BasicStroke(1);
        g2.setStroke(stroke);
        g2.setColor(Color.white);
        Shape registrationPointShape = new Ellipse2D.Double(registrationPoint.getX() - CENTER_POINT_SIZE / 2,
                registrationPoint.getY() - CENTER_POINT_SIZE / 2,
                CENTER_POINT_SIZE,
                CENTER_POINT_SIZE);
        g2.fill(registrationPointShape);
        g2.setColor(Color.black);
        g2.draw(registrationPointShape);
    }

    private static void drawHandles(Graphics2D g2, Rectangle bounds) {
        drawHandle(g2, bounds.getX(), bounds.getY());
        drawHandle(g2, bounds.getCenterX(), bounds.getY());
        drawHandle(g2, bounds.getX() + bounds.getWidth(), bounds.getY());
        drawHandle(g2, bounds.getX(), bounds.getCenterY());
        drawHandle(g2, bounds.getX(), bounds.getY() + bounds.getHeight());
        drawHandle(g2, bounds.getX() + bounds.getWidth(), bounds.getCenterY());
        drawHandle(g2, bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight());
        drawHandle(g2, bounds.getCenterX(), bounds.getY() + bounds.getHeight());
    }

    private static void drawHandle(Graphics2D g2, double x, double y) {
        Shape handleTopCenter = new Rectangle2D.Double(
                x - HANDLES_WIDTH / 2,
                y - HANDLES_WIDTH / 2,
                HANDLES_WIDTH + HANDLES_STROKE_WIDTH,
                HANDLES_WIDTH + HANDLES_STROKE_WIDTH);
        g2.setColor(Color.black);
        g2.fill(handleTopCenter);

        Stroke stroke = new BasicStroke(HANDLES_STROKE_WIDTH);
        g2.setStroke(stroke);
        g2.setColor(Color.white);
        g2.draw(handleTopCenter);
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

    private ExportRectangle getViewRect() {

        Zoom zoom;
        synchronized (ImagePanel.class) {
            zoom = this.zoom;

            if (timelined == null) {
                return new ExportRectangle(0, 0, 1, 1);
            }
        }

        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }
        ExportRectangle aRect;
        if (_rect == null) {
            iconPanel.calcRect();
        }
        aRect = new ExportRectangle(_rect);

        ExportRectangle viewRect = new ExportRectangle(new RECT());
        RECT timRect = timelined.getRectWithStrokes();

        double w = timRect.getWidth() * zoomDouble / SWF.unitDivisor;

        if (aRect.xMin >= 0) {
            viewRect.xMin = 0;
        } else {
            viewRect.xMin = -aRect.xMin;
        }

        if (w + aRect.xMin > iconPanel.getWidth()) {
            w = -aRect.xMin + iconPanel.getWidth();
        }

        viewRect.xMax = w;

        double h = timRect.getHeight() * zoomDouble / SWF.unitDivisor;

        if (aRect.yMin >= 0) {
            viewRect.yMin = 0;
        } else {
            viewRect.yMin = -aRect.yMin;
        }

        if (h + aRect.yMin > iconPanel.getHeight()) {
            h = -aRect.yMin + iconPanel.getHeight();
        }

        viewRect.yMax = h;

        viewRect.xMin *= SWF.unitDivisor;
        viewRect.xMax *= SWF.unitDivisor;
        viewRect.yMin *= SWF.unitDivisor;
        viewRect.yMax *= SWF.unitDivisor;

        viewRect.xMin /= zoomDouble;
        viewRect.xMax /= zoomDouble;
        viewRect.yMin /= zoomDouble;
        viewRect.yMax /= zoomDouble;

        viewRect.xMin += timRect.Xmin;
        viewRect.yMin += timRect.Ymin;
        viewRect.xMax += timRect.Xmin;
        viewRect.yMax += timRect.Ymin;
        return viewRect;
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

        boolean shownAgain = false;

        synchronized (ImagePanel.class) {
            frame = this.frame;
            time = this.time;
            if (this.frame == this.prevFrame) {
                shownAgain = true;
            }

            this.prevFrame = this.frame;
            cursorPosition = this.cursorPosition;
            if (cursorPosition
                    != null) {
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

        iconPanel.calcRect();

        RenderContext renderContext = new RenderContext();
        renderContext.displayObjectCache = displayObjectCache;
        if (cursorPosition != null && freeTransformDepth == -1) {
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

                synchronized (lock) {
                    Reference<Point2D> registrationPointRef = new Reference<>(registrationPoint);
                    Reference<Rectangle2D> boundsRef = new Reference<>(bounds);

                    _viewRect = getViewRect();
                    if (_viewRect.getHeight() < 0 || _viewRect.getWidth() < 0) {
                        img = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                    } else {
                        img = getFrame(_viewRect, swf, frame, time, timelined, renderContext, selectedDepth, freeTransformDepth, zoomDouble, registrationPointRef, boundsRef, transform, transformUpdated == null ? null : new Matrix(transformUpdated));
                    }
                    bounds = boundsRef.getVal();
                    registrationPoint = registrationPointRef.getVal();
                }

                sw.stop();
                if (sw.getElapsedMilliseconds() > 100) {
                    if (Configuration.showSlowRenderingWarning.get()) {
                        logger.log(Level.WARNING, "Slow rendering. {0}. frame, time={1}, {2}ms", new Object[]{frame, time, sw.getElapsedMilliseconds()});
                    }
                }

                if (renderContext.borderImage != null) {
                    img = renderContext.borderImage;
                }
            }

            if (!shownAgain) {
                List<Integer> sounds = new ArrayList<>();
                List<String> soundClasses = new ArrayList<>();
                List<SOUNDINFO> soundInfos = new ArrayList<>();
                timeline.getSounds(frame, time, renderContext.mouseOverButton, mouseButton, sounds, soundClasses, soundInfos);
                for (int cid : swf.getCharacters().keySet()) {
                    CharacterTag c = swf.getCharacter(cid);
                    for (int k = 0; k < soundClasses.size(); k++) {
                        String cls = soundClasses.get(k);
                        if (cls == null) {
                            continue;
                        }
                        if (cls.equals(c.getClassName())) {
                            sounds.set(k, cid);
                        }
                    }
                }

                for (int s = 0; s < sounds.size(); s++) {
                    int sndId = sounds.get(s);
                    if (sndId == -1) {
                        continue;
                    }
                    CharacterTag c = swf.getCharacter(sndId);
                    if (c instanceof SoundTag) {
                        SoundTag st = (SoundTag) c;
                        playSound(st, soundInfos.get(s), thisTimer);
                    }
                }
                executeFrame(frame);
            }
        } catch (Throwable ex) {
            // swf was closed during the rendering probably
            ex.printStackTrace();
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
                    if (freeTransformDepth == -1) {
                        if (handCursor) {
                            iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else if (iconPanel.hasAllowMove()) {
                            iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        } else {
                            iconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    }

                    if (lastMouseOverButton != renderContext.mouseOverButton) {
                        ButtonTag b = renderContext.mouseOverButton;
                        if (b != null && freeTransformDepth == -1) {
                            // New mouse entered
                            DefineButtonSoundTag sounds = b.getSounds();
                            if (sounds != null && sounds.buttonSoundChar1 != 0) { // IdleToOverUp
                                playSound((SoundTag) swf.getCharacter(sounds.buttonSoundChar1), sounds.buttonSoundInfo1, timer);
                            }
                        }

                        b = lastMouseOverButton;
                        if (b != null && freeTransformDepth == -1) {
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

            sp = new SoundTagPlayer(soundInfo, st, loopCount, false);
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
        lda = null;

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
                prevFrame = -1;
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
                        /*synchronized (ImagePanel.class) {
                            thisTimer.cancel();
                            if (timer == thisTimer) {
                                timer = null;
                            }
                        }*/

                        fireMediaDisplayStateChanged();
                    } else {
                        //Time before drawing current frame
                        long frameTimeMsIs = System.currentTimeMillis();
                        //Total number of frames in this timeline
                        int frameCount = timelined.getTimeline().getFrameCount();
                        if (frameCount == 0) {
                            return;
                        }
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
                    synchronized (ImagePanel.class) {
                        if (timer != thisTimer) {
                            return;
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

        this.playing = playing;
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
        prevFrame = -1;
        fireMediaDisplayStateChanged();
    }

    @Override
    public synchronized boolean isPlaying() {
        if (timelined == null || stillFrame) {
            return false;
        }

        return this.playing;
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
        if (frame > timeline.getFrameCount()) {
            return;
        }
        if (frame < 1) {
            return;
        }

        this.frame = frame - 1;
        this.prevFrame = -1;
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
        if (zoom.fit) {
            zoom.value = getZoomToFit();
        }
        return zoom;
    }

    private static final int ZOOM_DECADE_STEPS = 10;

    private static final double ZOOM_MULTIPLIER = Math.pow(10, 1.0 / ZOOM_DECADE_STEPS);

    private double getRealZoom() {
        if (zoom.fit) {
            return getZoomToFit();
        }

        return zoom.value;
    }

    private final double MAX_ZOOM = 1.0e6; //in larger zooms, flash viewer stops working

    private synchronized void zoomIn() {
        double currentRealZoom = getRealZoom();
        if (currentRealZoom >= MAX_ZOOM) {
            return;
        }
        Zoom newZoom = new Zoom();
        newZoom.value = currentRealZoom * ZOOM_MULTIPLIER;
        newZoom.fit = false;

        if (cursorPosition != null) {
            Point aCursor = iconPanel.toImagePoint(cursorPosition);

            Rectangle curRect = iconPanel.calcRect(zoom);
            Rectangle newRect = iconPanel.calcRect(newZoom);

            offsetPoint.x = (int) (aCursor.x + curRect.x + offsetPoint.x - aCursor.x * newZoom.value / zoom.value - newRect.x);
            offsetPoint.y = (int) (aCursor.y + curRect.y + offsetPoint.y - aCursor.y * newZoom.value / zoom.value - newRect.y);

            //zoom method does the opposite:
            offsetPoint.x = (int) (offsetPoint.x * zoom.value / newZoom.value);
            offsetPoint.y = (int) (offsetPoint.y * zoom.value / newZoom.value);

        }
        zoom(newZoom);
    }

    private synchronized void zoomOut() {
        Zoom newZoom = new Zoom();
        newZoom.value = getRealZoom() / ZOOM_MULTIPLIER;
        newZoom.fit = false;

        if (cursorPosition != null) {
            Point aCursor = iconPanel.toImagePoint(cursorPosition);

            Rectangle curRect = iconPanel.calcRect(zoom);
            Rectangle newRect = iconPanel.calcRect(newZoom);

            offsetPoint.x = (int) (aCursor.x + curRect.x + offsetPoint.x - aCursor.x * newZoom.value / zoom.value - newRect.x);
            offsetPoint.y = (int) (aCursor.y + curRect.y + offsetPoint.y - aCursor.y * newZoom.value / zoom.value - newRect.y);

            //zoom method does the opposite:
            offsetPoint.x = (int) (offsetPoint.x * zoom.value / newZoom.value);
            offsetPoint.y = (int) (offsetPoint.y * zoom.value / newZoom.value);
        }
        zoom(newZoom);
    }
}
