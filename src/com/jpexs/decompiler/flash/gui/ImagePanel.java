/*
 *  Copyright (C) 2010-2025 JPEXS
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
import com.jpexs.decompiler.flash.configuration.ConfigurationItemChangeListener;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.ecma.EcmaNumberToString;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.math.BezierUtils;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DisplayObjectCacheKey;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.ConstantColorColorTransform;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.Stopwatch;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;

/**
 * @author JPEXS
 */
public final class ImagePanel extends JPanel implements MediaDisplay {

    private static final int MAX_SOUND_CHANNELS = 8; //TODO: Maybe add to Advanced settings

    private static final Logger logger = Logger.getLogger(ImagePanel.class.getName());

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private final List<Runnable> textChangedListeners = new ArrayList<>();

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

    private static final String DEFAULT_DEBUG_LABEL_TEXT = " - ";

    private final JLabel debugLabel = new JLabel(DEFAULT_DEBUG_LABEL_TEXT);

    private JPanel pointEditPanel;
    private JTextField pointXTextField;

    private JTextField pointYTextField;

    private Point cursorPosition = null;

    private MouseEvent lastMouseEvent = null;

    private final List<SoundTagPlayer> soundPlayers = new ArrayList<>();

    private final Cache<DisplayObjectCacheKey, SerializableImage> displayObjectCache = Cache.getInstance(false, false, "displayObject", true);

    private final IconPanel iconPanel;

    private int time = 0;

    private boolean doFreeTransform = false;

    private Zoom zoom = new Zoom();

    private final Object delayObject = new Object();

    private boolean drawReady;

    private final int drawWaitLimit = 50; // ms

    private TextTag textTag;

    private TextTag newTextTag;

    private boolean showObjectsUnderCursor;

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

    private static final int MODE_GUIDE_X = -9;
    private static final int MODE_GUIDE_Y = -10;

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
    private static Cursor movePointCursor;
    private static Cursor defaultCursor;
    private static Cursor addPointCursor;
    private static Cursor guideXCursor;
    private static Cursor guideYCursor;
    private static Cursor textCursor;

    private Point2D offsetPoint = new Point2D.Double(0, 0);

    private ExportRectangle _viewRect = new ExportRectangle(0, 0, 1, 1);

    private boolean playing = false;

    private boolean autoPlayed = false;

    private boolean frozen = false;

    private boolean frozenButtons = false;

    private boolean muted = false;

    private boolean resample = false;

    private boolean mutable = false;

    private boolean alwaysDisplay = false;

    private boolean allowSelectAllTextTypes = false;

    private RegistrationPointPosition registrationPointPosition = RegistrationPointPosition.CENTER;

    private DepthState depthStateUnderCursor = null;

    private List<ActionListener> placeObjectSelectedListeners = new ArrayList<>();

    private Point[] hilightedEdge = null;

    private List<DisplayPoint> hilightedPoints = null;

    private DisplayPoint touchPointOffset = null;

    private static final int TOUCH_POINT_DISTANCE = 15;

    private Point2D snapOffset = new Point2D.Double(0, 0);

    private DisplayPoint snapAlignXPoint1 = null;
    private DisplayPoint snapAlignXPoint2 = null;

    private DisplayPoint snapAlignYPoint1 = null;
    private DisplayPoint snapAlignYPoint2 = null;

    private static final int SNAP_ALIGN_DISTANCE = 5;

    private static final int SNAP_TO_OBJECTS_DISTANCE = 10;

    private static final int SNAP_ALIGN_AFTER_LINE = 50;

    //private DisplayPoint closestPoint = null;
    private List<Integer> pointsUnderCursor = new ArrayList<>();
    private List<Integer> selectedPoints = new ArrayList<>();

    private List<DistanceItem> pathPointsUnderCursor = new ArrayList<>();

    private DisplayPoint closestPoint = null;

    private List<DisplayPoint> selectedPointsOriginalValues = new ArrayList<>();

    private int hilightEdgeColorStep = 10;
    private int hilightEdgeColor = 0;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private boolean updatingScrollBars = false;
    private final int SCROLL_SPACE_BEFORE = (int) SWF.unitDivisor * 500;

    private List<java.awt.Point> showPoints1 = new ArrayList<>();

    private List<java.awt.Point> showPoints2 = new ArrayList<>();

    private int displayedFrame = 0;

    private JPanel topPanel;

    private TagNameResolverInterface tagNameResolver = new DefaultTagNameResolver();

    private boolean showAllDepthLevelsInfo = true;

    private boolean selectionMode = false;

    private boolean transformSelectionMode = false;

    private boolean multiSelect = false;

    private boolean inMoving = false;

    private List<Integer> selectedDepths = new ArrayList<>();

    private final List<Integer> parentFrames = new ArrayList<>();

    private final List<Integer> parentDepths = new ArrayList<>();

    private final List<Timelined> parentTimelineds = new ArrayList<>();

    private JPanel topRuler;

    private JPanel leftRuler;

    private boolean draggingGuideY = false;

    private boolean draggingGuideX = false;

    private int guideDragX = -1;

    private int guideDragY = -1;

    private boolean contentCanHaveRuler = false;

    private List<Double> guidesX = new ArrayList<>();

    private List<Double> guidesY = new ArrayList<>();

    private static final int GUIDE_THICKNESS = 20;

    private static final int GUIDE_FONT_HEIGHT = 11;

    private static final int GUIDE_TEXT_OFFSET = 10;

    private static final int GUIDE_MOVE_TOLERANCE = 2;

    private SWF guidesSwf = null;

    private int guidesCharacterId = -1;

    private Rectangle2D textSelectionStartGlyphRect = null;
    private Double textSelectionStartGlyphXPosition = null;
    private Double textSelectionStartPrecise = null;
    private Double textSelectionEndPrecise = null;
    private TextTag lastMouseOverText = null;
    private TextTag textSelectionText = null;
    private boolean selectingText = false;

    private boolean textCursorBlinkOn = false;
    
    /**
     * This was a test to edit texts inline, but it failed horribly.
     * You can try to enable it, but the results are bad, very bad.
     */
    private boolean editTexts = false;

    private Timer textCursorBlinkTimer;
    
    private void setTextSelection(int value) {
        int selStart = getSelectionStartInt();
        int delta = value - selStart;
        changeTextSelection(delta);
    }
    
    private void changeTextSelection(int delta) {
        TextTag text = textSelectionText;
        if (text == null) {
            return;                        
        }
        int selStart = getSelectionStartInt();
        List<TEXTRECORD> textRecords = new ArrayList<>();
        if (text instanceof StaticTextTag) {
            textRecords = ((StaticTextTag) text).textRecords;
        }
        if (text instanceof DefineEditTextTag) {
            textRecords = ((DefineEditTextTag) text).getTextRecords(text.getSwf());
        }

        List<RECT> glyphPositions = TextTag.getGlyphEntriesPositions(textRecords, text.getSwf());
        
        selStart += delta;

        if (selStart < 0) {
            selStart = 0;
        }
        if (selStart > glyphPositions.size()) {
            selStart = glyphPositions.size();
        }                
        
        if (glyphPositions.size() == 0) {
            textSelectionStartPrecise = (double) selStart;
            textSelectionEndPrecise = (double) selStart;
            return;
        }
        
        RECT gp = glyphPositions.get(selStart == glyphPositions.size() ? selStart - 1 : selStart);
        Rectangle2D r = new Rectangle2D.Double(
                gp.Xmin,
                gp.Ymin,
                gp.Xmax - gp.Xmin,
                gp.Ymax - gp.Ymin
        );
        textSelectionStartGlyphRect = r;
        textSelectionStartGlyphXPosition = r.getX();
        if (selStart == glyphPositions.size()) {
            textSelectionStartGlyphXPosition = r.getMaxX();
        }

        textSelectionStartPrecise = (double) selStart;
        textSelectionEndPrecise = (double) selStart;
    }

    private static int getSnapGuidesDistance() {
        return Configuration.guidesSnapAccuracy.get().getDistance();
    }

    private static int getSnapGridDistance() {
        return Configuration.gridSnapAccuracy.get().getDistance();
    }

    @Override
    public boolean canHaveRuler() {
        return this.contentCanHaveRuler;
    }

    @Override
    public boolean canUseSnapping() {
        return selectionMode || doFreeTransform || hilightedPoints != null;
    }

    public void setFrozenButtons(boolean frozenButtons) {
        this.frozenButtons = frozenButtons;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setTransformSelectionMode(boolean transformSelectionMode) {
        this.transformSelectionMode = transformSelectionMode;
    }

    public void setTagNameResolver(TagNameResolverInterface tagNameResolver) {
        this.tagNameResolver = tagNameResolver;
    }

    public void setShowAllDepthLevelsInfo(boolean showAllDepthLevelsInfo) {
        this.showAllDepthLevelsInfo = showAllDepthLevelsInfo;
    }

    public void setTopPanelVisible(boolean visible) {
        topPanel.setVisible(visible);

        updateRulerVisibility();
    }

    public void setShowPoints(List<java.awt.Point> showPoints1, List<java.awt.Point> showPoints2) {
        this.showPoints1 = showPoints1;
        this.showPoints2 = showPoints2;
    }

    private static String formatDouble(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    private static double parseDouble(String value) {
        try {
            return DECIMAL_FORMAT.parse(value).doubleValue();
        } catch (ParseException ex) {
            throw new NumberFormatException();
        }
    }

    private static Cursor loadCursor(String name, int x, int y) throws IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = ImageIO.read(MainPanel.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/cursors/" + name + ".png"));
        return toolkit.createCustomCursor(image, new Point(x, y), name);
    }

    private SerializableImage imgPlay = null;

    private List<BoundsChangeListener> boundsChangeListeners = new ArrayList<>();

    private List<PointUpdateListener> pointUpdateListeners = new ArrayList<>();

    private List<Runnable> transformChangeListeners = new ArrayList<>();

    public void addTransformChangeListener(Runnable listener) {
        transformChangeListeners.add(listener);
    }

    public void removeTransformChangeListener(Runnable listener) {
        transformChangeListeners.remove(listener);
    }

    public void addPointUpdateListener(PointUpdateListener listener) {
        pointUpdateListeners.add(listener);
    }

    public void removePointUpdateListener(PointUpdateListener listener) {
        pointUpdateListeners.remove(listener);
    }

    private void firePointsUpdated(List<DisplayPoint> points) {
        for (PointUpdateListener listener : pointUpdateListeners) {
            listener.pointsUpdated(points);
        }
    }

    private void fireTransformChanged() {
        for (Runnable listener : transformChangeListeners) {
            listener.run();
        }
    }

    private void fireStatusChanged(String status) {
        for (MediaDisplayListener listener : listeners) {
            listener.statusChanged(status);
        }
    }

    private boolean fireEdgeSplit(int position, double splitPoint) {
        boolean result = true;
        for (PointUpdateListener listener : pointUpdateListeners) {
            result = result && listener.edgeSplit(position, splitPoint);
        }
        return result;
    }

    private boolean firePointRemoved(int position) {
        boolean result = true;
        for (PointUpdateListener listener : pointUpdateListeners) {
            result = result && listener.pointRemoved(position);
        }
        return result;
    }

    private void applyPointsXY() {
        try {
            int x = (int) Math.round(parseDouble(pointXTextField.getText()) * SWF.unitDivisor);
            int y = (int) Math.round(parseDouble(pointYTextField.getText()) * SWF.unitDivisor);

            java.awt.Point minSelectedPoint = getMinSelectedPoint();
            if (minSelectedPoint == null) {
                return;
            }
            for (int index : selectedPoints) {
                DisplayPoint point = hilightedPoints.get(index);
                point.x = point.x - minSelectedPoint.x + x;
                point.y = point.y - minSelectedPoint.y + y;
            }

            firePointsUpdated(hilightedPoints);
        } catch (NumberFormatException nfe) {
            //ignore
        }
    }

    private java.awt.Point getMinSelectedPoint() {
        if (selectedPoints.isEmpty()) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int index : selectedPoints) {
            DisplayPoint point = hilightedPoints.get(index);
            if (point.x < minX) {
                minX = point.x;
            }
            if (point.y < minY) {
                minY = point.y;
            }
        }
        return new java.awt.Point(minX, minY);
    }

    private void calculatePointsXY() {
        Point2D minPoint = getMinSelectedPoint();
        if (minPoint == null) {
            pointXTextField.setText("");
            pointYTextField.setText("");
            return;
        }

        pointXTextField.setText(formatDouble(minPoint.getX() / SWF.unitDivisor));
        pointYTextField.setText(formatDouble(minPoint.getY() / SWF.unitDivisor));
    }

    public void setHilightedPoints(List<DisplayPoint> hilightedPoints) {
        hilightedEdge = null;
        selectedPoints = new ArrayList<>();
        calculatePointsXY();
        this.hilightedPoints = hilightedPoints;
        pointEditPanel.setVisible(hilightedPoints != null);
        redraw();
    }

    public void setHilightedEdge(Point[] hilightedEdge) {
        this.hilightedEdge = hilightedEdge;
        hilightedPoints = null;
        hilightEdgeColor = 255;
        pointEditPanel.setVisible(false);
        redraw();
    }

    public void setStatus(String status) {
        fireStatusChanged(status);
    }

    public void setNoStatus() {
        fireStatusChanged("");
    }

    public void addBoundsChangeListener(BoundsChangeListener listener) {
        boundsChangeListeners.add(listener);
    }

    public void addPlaceObjectSelectedListener(ActionListener listener) {
        placeObjectSelectedListeners.add(listener);
    }

    public void removePlaceObjectSelectedListener(ActionListener listener) {
        placeObjectSelectedListeners.remove(listener);
    }

    private void firePlaceObjectSelected() {
        ActionEvent e = new ActionEvent(this, 0, "");
        for (ActionListener listener : placeObjectSelectedListeners) {
            listener.actionPerformed(e);
        }
    }

    public PlaceObjectTypeTag getPlaceTagUnderCursor() {
        if (depthStateUnderCursor == null) {
            return null;
        }
        return depthStateUnderCursor.placeObjectTag;
    }

    public void removeBoundsChangeListener(BoundsChangeListener listener) {
        boundsChangeListeners.remove(listener);
    }

    private void fireBoundsChange(Rectangle2D bounds, Point2D registrationPoint, RegistrationPointPosition registrationPointPosition) {
        for (BoundsChangeListener listener : boundsChangeListeners) {
            listener.boundsChanged(bounds, registrationPoint, registrationPointPosition);
        }
    }

    private SerializableImage getImagePlay() {
        if (imgPlay != null) {
            return imgPlay;
        }

        Color bgColor;
        if (Configuration.useRibbonInterface.get()) {
            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            bgColor = (skin.getColorScheme(DecorationAreaType.HEADER, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor());
        } else {
            bgColor = SystemColor.control;
        }
        Color fgColor;
        if (Configuration.useRibbonInterface.get()) {
            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            fgColor = (skin.getColorScheme(DecorationAreaType.HEADER, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getForegroundColor());
        } else {
            fgColor = SystemColor.controlText;
        }

        int size = 200;
        imgPlay = new SerializableImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE);
        imgPlay.fillTransparent();
        Graphics2D g2d = (Graphics2D) imgPlay.getGraphics();
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath path = new GeneralPath();
        path.moveTo(0, 0);
        path.lineTo(size, size / 2);
        path.lineTo(0, size);
        path.closePath();
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setPaint(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), fgColor.getAlpha() / 2));
        g2d.fill(path);
        g2d.setPaint(fgColor);
        g2d.draw(path);

        return imgPlay;
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
            movePointCursor = loadCursor("move_point", 0, 0);
            defaultCursor = loadCursor("default", 0, 0);
            addPointCursor = loadCursor("add_point", 0, 0);
            guideXCursor = loadCursor("guide_x", 0, 0);
            guideYCursor = loadCursor("guide_y", 0, 0);
            textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        } catch (IOException ex) {
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Matrix getNewToImageMatrix(Matrix newMatrix) {
        Matrix m = new Matrix();
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }
        double zoom = zoomDouble;
        m.translate(-_viewRect.xMin * zoom, -_viewRect.yMin * zoom);
        m.scale(zoom);

        return Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).concatenate(newMatrix);
    }

    public Matrix getNewMatrix() {
        if (transform == null) {
            return new Matrix();
        }
        return transform;
    }

    public Matrix getOriginalMatrix() {
        if (selectedDepths.size() == 1 && timelined != null) {
            int depth = selectedDepths.get(0);
            Frame fr = timelined.getTimeline().getFrame(frame);
            if (fr != null && fr.layers.containsKey(depth)) {
                return new Matrix(fr.layers.get(depth).matrix);
            }
        }
        return new Matrix();
    }

    public synchronized void selectDepth(int depth) {
        List<Integer> depths = new ArrayList<>();
        if (depth != -1) {
            depths.add(depth);
        }
        selectDepths(depths);
    }

    public synchronized void selectDepths(List<Integer> depths) {

        depths = new ArrayList<>(depths);

        if (timelined == null) {
            depths = new ArrayList<>();
        } else {
            Frame fr = timelined.getTimeline().getFrame(frame);

            for (int i = 0; i < depths.size(); i++) {
                int depth = depths.get(i);
                if (fr == null || !fr.layers.containsKey(depth)) {
                    depths.remove(i);
                    i--;
                }
            }
        }

        transformUpdated = null;
        registrationPointUpdated = null;
        transform = null;

        selectedDepths = new ArrayList<>(depths);
        doFreeTransform = false;

        if (selectionMode) {
            calculateFreeOrSelectionTransform();
        }
        hideMouseSelection();
        redraw();
    }

    public List<Integer> getSelectedDepths() {
        return new ArrayList<>(selectedDepths);
    }

    private void calculateFreeOrSelectionTransform() {
        if (!(doFreeTransform || selectionMode)) {
            return;
        }
        if (selectedDepths.isEmpty()) {
            return;
        }

        DepthState ds = null;
        Timeline timeline = timelined.getTimeline();

        if (timeline.getFrameCount() <= frame) {
            return;
        }

        if (selectedDepths.size() == 1) {
            ds = timeline.getFrame(frame).layers.get(selectedDepths.get(0));
        }

        _viewRect = getViewRect();

        /*if (ds != null && !selectionMode) {
            CharacterTag cht = ds.getCharacter();
            if (cht != null) {
                if (cht instanceof DrawableTag) {
                    DrawableTag dt = (DrawableTag) cht;
                    int drawableFrameCount = dt.getNumFrames();
                    if (drawableFrameCount == 0) {
                        drawableFrameCount = 1;
                    }
        
                    if (dt instanceof ButtonTag) {
                        dframe = ButtonTag.FRAME_HITTEST;
                    }

                    transform = new Matrix(ds.matrix);

                    Rectangle2D transformBounds = getTransformBounds();
                    registrationPointPosition = RegistrationPointPosition.CENTER;
                    fireBoundsChange(transformBounds, new Point2D.Double(transformBounds.getCenterX(), transformBounds.getCenterY()), registrationPointPosition);
                }
            }
        } else {*/
        transform = new Matrix();
        Rectangle2D transformBounds = getTransformBounds();
        registrationPointPosition = RegistrationPointPosition.CENTER;
        fireBoundsChange(transformBounds, new Point2D.Double(transformBounds.getCenterX(), transformBounds.getCenterY()), registrationPointPosition);
        //}
    }

    public synchronized void freeTransformDepth(int depth) {
        List<Integer> depths = new ArrayList<>();
        if (depth != -1) {
            depths.add(depth);
        }
        freeTransformDepths(depths);
    }

    public synchronized void freeTransformDepths(List<Integer> depths) {
        selectedDepths = new ArrayList<>(depths);
        doFreeTransform = !depths.isEmpty();
        hilightedEdge = null;
        hilightedPoints = null;
        pointEditPanel.setVisible(false);
        registrationPoint = null;
        calculateFreeOrSelectionTransform();
        hideMouseSelection();
        redraw();
        iconPanel.requestFocusInWindow();
    }

    private void centerImage() {
        Timelined tim = timelined;
        if (tim == null) {
            return;
        }
        RECT rect = tim.getRect();
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        double w = rect.getWidth() / SWF.unitDivisor * zoomDouble;
        double h = rect.getHeight() / SWF.unitDivisor * zoomDouble;
        double dw = rect.Xmin * zoomDouble / SWF.unitDivisor;
        double dh = rect.Ymin * zoomDouble / SWF.unitDivisor;
        offsetPoint.setLocation(
                iconPanel.getWidth() / 2 - w / 2 - dw,
                iconPanel.getHeight() / 2 - h / 2 - dh
        );
        /*Timer tim = new Timer();
        tim.schedule(new TimerTask() {
            @Override
            public void run() {                
                updateScrollBars();
                redraw();
            }

        }, 100);*/

    }

    public void fireMediaDisplayStateChanged() {
        List<MediaDisplayListener> ls = new ArrayList<>(listeners);
        for (MediaDisplayListener l : ls) {
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

    public void addTextChangedListener(Runnable listener) {
        textChangedListeners.add(listener);
    }

    public void removeTextChangedListener(Runnable listener) {
        textChangedListeners.remove(listener);
    }

    private void fireTextChanged() {
        for (Runnable r : textChangedListeners) {
            r.run();
        }
    }

    @Override
    public Color getBackgroundColor() {
        if (swf != null && swf.getBackgroundColor() != null) {
            return swf.getBackgroundColor().backgroundColor.toColor();
        }
        return Color.white;
    }

    @Override
    public void setDisplayed(boolean value) {
        autoPlayed = value;
        Configuration.autoPlayPreviews.set(value);
    }

    @Override
    public void setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean isDisplayed() {
        return autoPlayed;
    }

    @Override
    public boolean alwaysDisplay() {
        return alwaysDisplay;
    }

    @Override
    public void setMuted(boolean value) {
        this.muted = value;
        if (value) {
            stopAllSounds();
        } else {
            prevFrame = -1; //initiate refreshing frame to play sounds again
        }
    }

    @Override
    public void setResample(boolean resample) {
        this.resample = resample;
    }

    private static void drawGridSwf(Graphics2D g, Rectangle realRect, double zoom) {
        g.setColor(Configuration.gridColor.get());
        double x;
        double y;
        int ix;
        int iy;
        int minIx = 0;
        int minIy = 0;
        int maxIx;
        int maxIy;

        ix = 0;
        while ((double) realRect.x + ix * Configuration.gridHorizontalSpace.get() * zoom < realRect.getMaxX()) {
            ix++;
        }
        maxIx = ix - 1;

        iy = 0;
        while ((double) realRect.y + iy * Configuration.gridVerticalSpace.get() * zoom < realRect.getMaxY()) {
            iy++;
        }
        maxIy = iy - 1;

        for (ix = minIx; ix <= maxIx; ix++) {
            x = realRect.x + ix * Configuration.gridHorizontalSpace.get() * zoom;
            Point2D p1 = new Point2D.Double(x, realRect.getMinY());
            Point2D p2 = new Point2D.Double(x, realRect.getMaxY());
            g.drawLine(
                    (int) Math.round(p1.getX()),
                    (int) Math.round(p1.getY()),
                    (int) Math.round(p2.getX()),
                    (int) Math.round(p2.getY())
            );
        }

        for (iy = minIy; iy <= maxIy; iy++) {
            y = realRect.y + iy * Configuration.gridVerticalSpace.get() * zoom;
            Point2D p1 = new Point2D.Double(realRect.getMinX(), y);
            Point2D p2 = new Point2D.Double(realRect.getMaxX(), y);
            g.drawLine(
                    (int) Math.round(p1.getX()),
                    (int) Math.round(p1.getY()),
                    (int) Math.round(p2.getX()),
                    (int) Math.round(p2.getY())
            );
        }
    }

    private class IconPanel extends JPanel {

        private SerializableImage _img;

        private ButtonTag mouseOverButton = null;

        private TextTag mouseOverText = null;

        private Matrix selectionAbsMatrix = null;

        private int glyphPosUnderCursor = -1;

        private Rectangle2D glyphUnderCursorRect = null;

        private double glyphUnderCursorXPosition = 0;

        private boolean autoFit = false;

        private boolean allowMove = true;

        private Point2D dragStart = null;

        private Point2D selectionEnd = null;

        private boolean canInvert = true;

        private Rectangle2D getSelectionRect() {
            Point2D selectStart = dragStart;
            Point2D selectEnd = selectionEnd;

            if (selectStart == null || selectEnd == null) {
                return null;
            }
            double startX = Math.min(selectStart.getX(), selectEnd.getX());
            double startY = Math.min(selectStart.getY(), selectEnd.getY());

            double endX = Math.max(selectStart.getX(), selectEnd.getX());
            double endY = Math.max(selectStart.getY(), selectEnd.getY());

            return new Rectangle2D.Double(startX, startY, endX - startX, endY - startY);
        }

        private synchronized Point2D getDragStart() {
            return dragStart;
        }

        private synchronized void setDragStart(Point dragStart) {
            this.dragStart = dragStart;
        }

        private synchronized SerializableImage getImg() {
            return _img;
        }

        public boolean hasAllowMove() {
            return allowMove;
        }

        VolatileImage renderImage;

        private void drawGridNoSwf(Graphics2D g2, int x, int y) {
            double zoomDouble = getRealZoom();
            g2.setColor(Configuration.gridColor.get());
            double gx;
            double gy;
            int ix = 0;
            int iy = 0;
            int minIx;
            int minIy;
            int maxIx;
            int maxIy;
            double sx = x + offsetPoint.getX();
            double sy = y + offsetPoint.getY();

            while (sx + ix * Configuration.gridHorizontalSpace.get() * zoomDouble > 0) {
                ix--;
            }
            minIx = ix;
            ix = 0;
            while (sx + ix * Configuration.gridHorizontalSpace.get() * zoomDouble < getWidth()) {
                ix++;
            }
            maxIx = ix;

            while (sy + iy * Configuration.gridVerticalSpace.get() * zoomDouble > 0) {
                iy--;
            }
            minIy = iy;

            iy = 0;
            while (sy + iy * Configuration.gridVerticalSpace.get() * zoomDouble < getHeight()) {
                iy++;
            }
            maxIy = iy;

            for (ix = minIx; ix <= maxIx; ix++) {
                gx = sx + ix * Configuration.gridHorizontalSpace.get() * zoomDouble;

                Point2D p1 = new Point2D.Double(gx, sy + minIy * Configuration.gridVerticalSpace.get() * zoomDouble);
                Point2D p2 = new Point2D.Double(gx, sy + maxIy * Configuration.gridVerticalSpace.get() * zoomDouble);
                g2.drawLine(
                        (int) Math.round(p1.getX()),
                        (int) Math.round(p1.getY()),
                        (int) Math.round(p2.getX()),
                        (int) Math.round(p2.getY())
                );
            }

            for (iy = minIy; iy <= maxIy; iy++) {
                gy = sy + iy * Configuration.gridVerticalSpace.get() * zoomDouble;

                Point2D p1 = new Point2D.Double(sx + minIx * Configuration.gridHorizontalSpace.get() * zoomDouble, gy);
                Point2D p2 = new Point2D.Double(sx + maxIx * Configuration.gridHorizontalSpace.get() * zoomDouble, gy);
                g2.drawLine(
                        (int) Math.round(p1.getX()),
                        (int) Math.round(p1.getY()),
                        (int) Math.round(p2.getX()),
                        (int) Math.round(p2.getY())
                );
            }
        }

        public void render() {
            SerializableImage img = getImg();
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
                    Color swfBackgroundColor = View.getSwfBackgroundColor();
                    if (swfBackgroundColor.getAlpha() < 255) {
                        g2.setPaint(View.transparentPaint);
                        g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
                    }
                    g2.setComposite(AlphaComposite.Src);
                    g2.setPaint(View.getSwfBackgroundColor());
                    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));

                    g2.setComposite(AlphaComposite.SrcOver);
                    if (img != null) {
                        int x = 0;
                        int y = 0;
                        if (timelined == null || !autoPlayed) {
                            x = (int) offsetPoint.getX();
                            y = (int) offsetPoint.getY();
                        }

                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

                        if (Configuration.showGrid.get() && !(timelined instanceof SWF) && !Configuration.gridOverObjects.get()) {
                            drawGridNoSwf(g2, x, y);
                        }

                        g2.drawImage(img.getBufferedImage(), x, y, x + img.getWidth(), y + img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);

                        if (Configuration.showGrid.get() && !(timelined instanceof SWF) && Configuration.gridOverObjects.get()) {
                            drawGridNoSwf(g2, x, y);
                        }

                        if (hilightedEdge != null || hilightedPoints != null) {
                            hilightEdgeColor += hilightEdgeColorStep;
                            if (hilightEdgeColor < 100 || hilightEdgeColor > 255) {
                                hilightEdgeColorStep = -hilightEdgeColorStep;
                                hilightEdgeColor += hilightEdgeColorStep * 2;
                            }
                            RECT timRect = timelined.getRect();
                            AffineTransform trans = new AffineTransform();
                            trans.translate(offsetPoint.getX(), offsetPoint.getY());
                            trans.scale(1 / SWF.unitDivisor, 1 / SWF.unitDivisor);
                            trans.scale(zoomDouble, zoomDouble);
                            AffineTransform oldTransform = g2.getTransform();
                            g2.transform(trans);

                            if (hilightedEdge != null) {
                                g2.setStroke(new BasicStroke((float) (SWF.unitDivisor * 6 / zoomDouble)));
                                g2.setPaint(new Color(hilightEdgeColor, hilightEdgeColor, hilightEdgeColor));
                                Point[] edge = hilightedEdge;
                                GeneralPath path = new GeneralPath();
                                if (edge.length == 2) {
                                    path.moveTo(edge[0].x, edge[0].y);
                                    path.lineTo(edge[1].x, edge[1].y);
                                }
                                if (edge.length == 3) {
                                    path.moveTo(edge[0].x, edge[0].y);
                                    path.quadTo(edge[1].x, edge[1].y, edge[2].x, edge[2].y);
                                }
                                if (edge.length == 1) {
                                    double crossSize = (SWF.unitDivisor * 10 / zoomDouble);
                                    path.moveTo(edge[0].x - crossSize, edge[0].y);
                                    path.lineTo(edge[0].x + crossSize, edge[0].y);
                                    path.moveTo(edge[0].x, edge[0].y - crossSize);
                                    path.lineTo(edge[0].x, edge[0].y + crossSize);
                                }
                                g2.draw(path);

                                double pointSize = SWF.unitDivisor * 4 / zoomDouble;

                                g2.setPaint(Color.red);
                                g2.fill(new Ellipse2D.Double(edge[edge.length - 1].x - pointSize, edge[edge.length - 1].y - pointSize, pointSize * 2, pointSize * 2));
                                g2.setPaint(Color.green);
                                g2.fill(new Ellipse2D.Double(edge[0].x - pointSize, edge[0].y - pointSize, pointSize * 2, pointSize * 2));
                            }

                            List<DisplayPoint> points = hilightedPoints;
                            if (points != null) {
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                                g2.setStroke(new BasicStroke((float) (SWF.unitDivisor * 1 / zoomDouble)));
                                double pointSize = SWF.unitDivisor * 4 / zoomDouble;
                                //selectedPoints
                                for (int i = 0; i < points.size(); i++) {
                                    DisplayPoint p = points.get(i);
                                    Shape pointShape;
                                    if (p.onPath) {
                                        pointShape = new Rectangle2D.Double(p.x - pointSize, p.y - pointSize, pointSize * 2, pointSize * 2);
                                    } else {
                                        pointShape = new Ellipse2D.Double(p.x - pointSize, p.y - pointSize, pointSize * 2, pointSize * 2);
                                    }
                                    if (selectedPoints.contains(i)) {
                                        g2.setPaint(Color.black);
                                    } else {
                                        g2.setPaint(Color.white);
                                    }
                                    g2.fill(pointShape);
                                    if (selectedPoints.contains(i)) {
                                        g2.setPaint(Color.white);
                                    } else {
                                        g2.setPaint(Color.black);
                                    }
                                    g2.draw(pointShape);
                                }
                            }

                            for (int i = 0; i < showPoints1.size(); i++) {
                                int xt = showPoints1.get(i).x;
                                int pointSize = 3;
                                int yt = showPoints1.get(i).y;
                                Shape pointShape;
                                pointShape = new Ellipse2D.Double(xt - pointSize, yt - pointSize, pointSize * 2, pointSize * 2);
                                g2.setPaint(Color.blue);
                                g2.fill(pointShape);
                            }

                            for (int i = 0; i < showPoints2.size(); i++) {
                                int xt = showPoints2.get(i).x;
                                int pointSize = 3;
                                int yt = showPoints2.get(i).y;
                                Shape pointShape;
                                pointShape = new Ellipse2D.Double(xt - pointSize, yt - pointSize, pointSize * 2, pointSize * 2);
                                g2.setPaint(Color.red);
                                g2.fill(pointShape);
                            }
                            g2.setTransform(oldTransform);
                        }
                        if (!(timelined instanceof SWF) && (doFreeTransform || hilightedPoints != null)) {
                            int axisX = 0;
                            int axisY = 0;
                            RECT timRect = timelined.getRect();
                            axisX = (int) Math.round(offsetPoint.getX());
                            axisY = (int) Math.round(offsetPoint.getY());
                            if (canInvert) {
                                g2.setComposite(BlendComposite.Invert);
                            } else {
                                g2.setComposite(AlphaComposite.SrcOver);
                            }
                            g2.setPaint(new Color(255, 255, 255, 128));
                            float dp;
                            dp = -(float) offsetPoint.getY();
                            while (dp < 0) {
                                dp += 10;
                            }
                            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, dp));
                            GeneralPath p = new GeneralPath();
                            p.moveTo(axisX, 0);
                            p.lineTo(axisX, getHeight());
                            g2.draw(p);
                            dp = -(float) offsetPoint.getX();
                            while (dp < 0) {
                                dp += 10;
                            }
                            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, dp));
                            p = new GeneralPath();
                            p.moveTo(0, axisY);
                            p.lineTo(getWidth(), axisY);
                            g2.draw(p);
                            g2.setComposite(AlphaComposite.SrcOver);
                        }

                        Rectangle2D selectionRect = getSelectionRect();
                        if (selectionRect != null) {
                            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, new float[]{2, 2}, 0f));
                            if (canInvert) {
                                g2.setComposite(BlendComposite.Invert);
                            } else {
                                g2.setComposite(AlphaComposite.SrcOver);
                            }
                            g2.draw(new Rectangle2D.Double(selectionRect.getX(), selectionRect.getY(), selectionRect.getWidth(), selectionRect.getHeight()));
                            g2.setComposite(AlphaComposite.SrcOver);
                        }

                        if (touchPointOffset != null) {
                            boolean snapped = snapOffset.getX() != 0 || snapOffset.getY() != 0;
                            g2.setStroke(new BasicStroke((float) ((snapped ? 2 : 1))));
                            Point2D p = new Point2D.Double(lastMouseEvent.getX() + touchPointOffset.x + snapOffset.getX(), lastMouseEvent.getY() + touchPointOffset.y + snapOffset.getY());
                            double pointSize = (snapped ? 4 : 3);
                            Shape pointShape = new Ellipse2D.Double(p.getX() - pointSize, p.getY() - pointSize, pointSize * 2, pointSize * 2);
                            g2.setPaint(Color.black);
                            g2.draw(pointShape);
                        }

                        DisplayPoint snapAlignStart;
                        DisplayPoint snapAlignEnd;

                        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, new float[]{2}, 0));
                        GeneralPath gp = new GeneralPath();

                        snapAlignStart = snapAlignXPoint1;
                        snapAlignEnd = snapAlignXPoint2;

                        if (snapAlignStart != null && snapAlignEnd != null) {
                            gp.moveTo(snapAlignStart.x, snapAlignStart.y);
                            gp.lineTo(snapAlignEnd.x, snapAlignEnd.y);
                        }
                        snapAlignStart = snapAlignYPoint1;
                        snapAlignEnd = snapAlignYPoint2;
                        if (snapAlignStart != null && snapAlignEnd != null) {
                            gp.moveTo(snapAlignStart.x, snapAlignStart.y);
                            gp.lineTo(snapAlignEnd.x, snapAlignEnd.y);
                        }
                        if (canInvert) {
                            g2.setComposite(BlendComposite.Invert);
                            g2.setPaint(Color.black);
                        } else {
                            g2.setComposite(AlphaComposite.SrcOver);
                            g2.setPaint(Color.cyan);
                        }
                        g2.draw(gp);
                        g2.setComposite(AlphaComposite.SrcOver);

                        Rectangle2D curRect = textSelectionStartGlyphRect;
                        Matrix mat = iconPanel.selectionAbsMatrix;
                        Double xPos = textSelectionStartGlyphXPosition;
                        if (textCursorBlinkOn && curRect != null && mat != null && textSelectionStartPrecise != null && xPos != null) {
                            double rectPos = (xPos - curRect.getX()) / curRect.getWidth();
                            if (rectPos < 0.7) {
                                curRect = new Rectangle2D.Double(curRect.getX(), curRect.getY(), 1 * SWF.unitDivisor, curRect.getHeight());
                            } else {
                                curRect = new Rectangle2D.Double(curRect.getMaxX(), curRect.getY(), 1 * SWF.unitDivisor, curRect.getHeight());
                            }

                            Matrix matScale = Matrix.getScaleInstance(1 / SWF.unitDivisor);
                            Shape shape = mat.preConcatenate(matScale).toTransform().createTransformedShape(curRect);
                            g2.setColor(Color.black);
                            g2.fill(shape);
                            /*g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
                            g2.draw(shape);*/
                        }
                    }
                } catch (InternalError ie) {
                    //On some devices like Linux X11 - BlendComposite.Invert is not available
                    //since sun.java2d.xr.XRSurfaceData.getRaster(XRSurfaceData.java:72) is not implemented
                    // (tried in WSL)
                    if (canInvert) {
                        canInvert = false;
                        continue;
                    }
                } finally {
                    if (g2 != null) {
                        g2.dispose();
                    }
                }

            } while (ri.contentsLost());
        }

        private boolean ctrlDown = false;

        private boolean altDown = false;

        private boolean shiftDown = false;

        private List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();
        private List<MouseListener> mouseListeners = new ArrayList<>();
        private List<MouseWheelListener> mouseWheelListeners = new ArrayList<>();

        @Override
        public synchronized void addMouseMotionListener(MouseMotionListener l) {
            mouseMotionListeners.add(l);
        }

        @Override
        public synchronized void removeMouseMotionListener(MouseMotionListener l) {
            mouseMotionListeners.remove(l);
        }

        @Override
        public synchronized void addMouseListener(MouseListener l) {
            mouseListeners.add(l);
        }

        @Override
        public synchronized void removeMouseListener(MouseListener l) {
            mouseListeners.remove(l);
        }

        @Override
        public synchronized void addMouseWheelListener(MouseWheelListener l) {
            mouseWheelListeners.add(l);
        }

        @Override
        public synchronized void removeMouseWheelListener(MouseWheelListener l) {
            mouseWheelListeners.remove(l);
        }

        public boolean isAltDown() {
            return altDown;
        }

        public boolean isCtrlDown() {
            return ctrlDown;
        }

        public IconPanel() {

            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if ((e.getID() == KeyEvent.KEY_PRESSED) || (e.getID() == KeyEvent.KEY_RELEASED)) {
                        ctrlDown = ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
                        altDown = ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK);
                        shiftDown = ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK);
                    }
                    return false;
                }
            });

            addKeyListener(new KeyAdapter() {

                private void move(int x, int y) {
                    if (hilightedPoints != null) {
                        java.awt.Point minPoint = getMinSelectedPoint();
                        if (minPoint != null) {
                            pointXTextField.setText(formatDouble(minPoint.x / SWF.unitDivisor + x));
                            pointYTextField.setText(formatDouble(minPoint.y / SWF.unitDivisor + y));
                            applyPointsXY();
                        }
                    } else {
                        Matrix matrix = new Matrix();
                        matrix.translate(x * SWF.unitDivisor, y * SWF.unitDivisor);
                        applyTransformMatrix(matrix);
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {                                            
                    
                    if (!editTexts) {
                        return;
                    }
                    
                    if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                        if (textSelectionText != null) {
                            TextTag text = textSelectionText;
                            int selStart = getSelectionStartInt();
                            if (text != null && selStart > -1) {
                                text.removeCharacterGlyph(selStart);
                                fireTextChanged();
                            }
                        }
                        return;
                    }

                    if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        if (textSelectionText != null) {
                            TextTag text = textSelectionText;
                            int selStart = getSelectionStartInt();
                            if (text != null && selStart > 0) {
                                changeTextSelection(-1);
                                text.removeCharacterGlyph(selStart - 1);
                                fireTextChanged();                                
                            }
                        }
                        return;
                    }
                    char c = e.getKeyChar();
                    TextTag text = textSelectionText;
                    int selStart = getSelectionStartInt();
                    if (text != null && selStart > -1) {
                        text.insertCharacterGlyph(selStart, c);
                        fireTextChanged();
                        changeTextSelection(+1);
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (textSelectionText != null) {
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            changeTextSelection(+1);
                        }
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            changeTextSelection(-1);
                        }
                    }
                    
                    if (hilightedPoints != null) {
                        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                            List<Integer> selectedPointsDesc = new ArrayList<>(selectedPoints);
                            selectedPointsDesc.sort(new Comparator<Integer>() {
                                @Override
                                public int compare(Integer o1, Integer o2) {
                                    return o2 - o1;
                                }
                            });
                            for (int i : selectedPointsDesc) {
                                firePointRemoved(i);
                            }
                            selectedPoints.clear();
                            repaint();
                        }
                    }
                    if (doFreeTransform || hilightedPoints != null) {
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            move(-1, 0);
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            move(1, 0);
                        }
                        if (e.getKeyCode() == KeyEvent.VK_UP) {
                            move(0, -1);
                        }
                        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                            move(0, 1);
                        }
                    }
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
                    updateScrollBars();
                    render();
                    repaint();
                }
            });

            MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (SwingUtilities.isRightMouseButton(e) && !selectionMode && !doFreeTransform) {
                        TextTag text = mouseOverText;
                        if (text != null) {
                            if (text instanceof DefineEditTextTag) {
                                DefineEditTextTag dtext = (DefineEditTextTag) text;
                                List<TEXTRECORD> recs = dtext.getTextRecords(dtext.getSwf());
                                FontTag font = null;
                                int pos = 0;
                                int selStart = getSelectionStartInt();
                                int selEnd = getSelectionEndInt();
                                StringBuilder sb = new StringBuilder();
                                int y = 0;
                                for (TEXTRECORD r : recs) {
                                    if (r.styleFlagsHasFont) {
                                        font = r.getFont(dtext.getSwf());                                        
                                    }
                                    if (r.styleFlagsHasYOffset) {
                                        int oldY = y;
                                        y = r.yOffset;
                                        if (text == textSelectionText && pos >= selStart && pos < selEnd) {
                                            if (y > oldY && sb.length() > 0) {
                                                sb.append("\n");
                                            }
                                        }
                                    }
                                    if (font == null) {
                                        continue;
                                    }
                                    for (GLYPHENTRY g : r.glyphEntries) {
                                        if (text == textSelectionText && pos >= selStart && pos < selEnd) {
                                            sb.append(font.glyphToChar(g.glyphIndex));
                                        }
                                        pos++;
                                    }
                                }
                                String textToCopy = sb.toString();
                                int fullLength = pos;
                                
                                JPopupMenu pm = new JPopupMenu();
                                JMenuItem copyMenuItem = new JMenuItem(AppStrings.translate("text.copy"), View.getIcon("copy16"));
                                copyMenuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        StringSelection stringSelection = new StringSelection(textToCopy);
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(stringSelection, null);
                                    }                                    
                                });
                                JMenuItem selectAllMenuItem = new JMenuItem(AppStrings.translate("text.selectAll"), View.getIcon("selectall16"));
                                selectAllMenuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        textSelectionText = text;
                                        textSelectionStartPrecise = 0.0;
                                        textSelectionEndPrecise = (double) fullLength;
                                        textSelectionStartGlyphRect = new Rectangle2D.Double(); //??
                                        textSelectionStartGlyphXPosition = 0.0; //??                                
                                
                                        repaint();
                                    }                                    
                                });
                                
                                if (!textToCopy.isEmpty()) {
                                    pm.add(copyMenuItem);                                    
                                }
                                pm.add(selectAllMenuItem);
                                pm.show(iconPanel, e.getX(), e.getY());
                                
                            }
                        }
                    }
                    
                    if (e.getClickCount() == 2) {
                        if (Configuration.showGuides.get() && !Configuration.lockGuides.get()) {
                            Point mousePoint = e.getPoint();
                            for (int d = 0; d < guidesX.size(); d++) {
                                Double guide = guidesX.get(d);
                                int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getX());
                                if (mousePoint.x == guideInPanel) {
                                    String newPositionStr = ViewMessages.showInputDialog(ImagePanel.this, AppStrings.translate("move_guide.position"), AppStrings.translate("move_guide"), View.getIcon("guidemovex32"), EcmaNumberToString.stringFor(guide));
                                    if (newPositionStr != null) {
                                        try {
                                            double newPosition = Double.parseDouble(newPositionStr);
                                            guidesX.set(d, newPosition);
                                            saveGuides();
                                            repaint();
                                        } catch (NumberFormatException nfe) {
                                            //ignore
                                        }
                                    }
                                    return;
                                }
                            }

                            for (int d = 0; d < guidesY.size(); d++) {
                                Double guide = guidesY.get(d);
                                int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getY());
                                if (mousePoint.y == guideInPanel) {
                                    String newPositionStr = ViewMessages.showInputDialog(ImagePanel.this, AppStrings.translate("move_guide.position"), AppStrings.translate("move_guide"), View.getIcon("guidemovey32"), EcmaNumberToString.stringFor(guide));
                                    if (newPositionStr != null) {
                                        try {
                                            double newPosition = Double.parseDouble(newPositionStr);
                                            guidesY.set(d, newPosition);
                                            saveGuides();
                                            repaint();
                                        } catch (NumberFormatException nfe) {
                                            //ignore
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                    }

                    if (e.getClickCount() == 2 && selectionMode && !transformSelectionMode) {

                        DepthState ds = depthStateUnderCursor;
                        if (ds != null) {                            
                            openDepth(frame, ds.depth);
                            //gotoFrame(1);
                        }

                        return;
                    }

                    if (shiftDown) {
                        List<Integer> newSelectedPoints = new ArrayList<>(pointsUnderCursor);
                        for (int i : selectedPoints) {
                            if (!newSelectedPoints.contains(i)) {
                                newSelectedPoints.add(i);
                            } else {
                                newSelectedPoints.remove((Integer) i);
                            }
                        }
                        selectedPoints = newSelectedPoints;
                    } else {
                        selectedPoints = new ArrayList<>(pointsUnderCursor);
                    }
                    calculatePointsXY();

                    if (altDown || selectionMode) {
                        if (depthStateUnderCursor != null) {

                            List<Integer> newSelectedDepths = new ArrayList<>();
                            if (ctrlDown) {
                                newSelectedDepths.addAll(selectedDepths);
                                if (newSelectedDepths.contains(depthStateUnderCursor.depth)) {
                                    newSelectedDepths.remove((Integer) depthStateUnderCursor.depth);
                                } else {
                                    newSelectedDepths.add(depthStateUnderCursor.depth);
                                }
                            } else {
                                newSelectedDepths.add(depthStateUnderCursor.depth);
                            }

                            if (transformSelectionMode) {
                                if (mode == Cursor.DEFAULT_CURSOR) {
                                    freeTransformDepths(newSelectedDepths);
                                    firePlaceObjectSelected();
                                }
                            } else if ((altDown && !selectionMode && !doFreeTransform) || selectionMode) {
                                selectDepths(newSelectedDepths);
                                firePlaceObjectSelected();
                            }
                        } else {
                            if (transformSelectionMode) {
                                if (mode == Cursor.DEFAULT_CURSOR) {
                                    freeTransformDepths(new ArrayList<>());
                                    selectDepths(new ArrayList<>());
                                    firePlaceObjectSelected();
                                }
                            } else if ((altDown && !selectionMode) || selectionMode) {
                                selectDepths(new ArrayList<>());
                                firePlaceObjectSelected();
                            }
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        if (altDown || selectionMode) {
                            if (depthStateUnderCursor != null && selectedDepths.contains(depthStateUnderCursor.depth)) {
                                inMoving = selectionMode;
                                calculateFreeOrSelectionTransform();
                            }
                            if (!selectionMode) {
                                return;
                            }
                        }

                        mouseMoved(e); //to correctly calculate mode, because mouseMoved event is not called during dragging                                                                                               

                        if (Configuration.showGuides.get() && !Configuration.lockGuides.get()) {
                            Point mousePoint = e.getPoint();
                            for (int d = 0; d < guidesX.size(); d++) {
                                Double guide = guidesX.get(d);
                                int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getX());
                                if (mousePoint.x >= guideInPanel - GUIDE_MOVE_TOLERANCE && mousePoint.x <= guideInPanel + GUIDE_MOVE_TOLERANCE) {
                                    guidesX.remove(d);
                                    guideDragX = guideInPanel;
                                    draggingGuideX = true;
                                    saveGuides();
                                    return;
                                }
                            }

                            for (int d = 0; d < guidesY.size(); d++) {
                                Double guide = guidesY.get(d);
                                int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getY());
                                if (mousePoint.y >= guideInPanel - GUIDE_MOVE_TOLERANCE && mousePoint.y <= guideInPanel + GUIDE_MOVE_TOLERANCE) {
                                    guidesY.remove(d);
                                    guideDragY = guideInPanel;
                                    draggingGuideY = true;
                                    saveGuides();
                                    return;
                                }
                            }
                        }

                        setDragStart(e.getPoint());

                        if (!shiftDown) {
                            boolean selectedUnderCursor = false;
                            for (int p : pointsUnderCursor) {
                                if (selectedPoints.contains(p)) {
                                    selectedUnderCursor = true;
                                    break;
                                }
                            }
                            if (!selectedUnderCursor) {
                                selectedPoints = new ArrayList<>(pointsUnderCursor);
                                calculatePointsXY();
                            }
                        }

                        List<DisplayPoint> newPointsUnderCursorValues = new ArrayList<>();
                        for (int i : selectedPoints) {
                            newPointsUnderCursorValues.add(new DisplayPoint(hilightedPoints.get(i)));
                        }

                        selectedPointsOriginalValues = newPointsUnderCursorValues;

                        if ((selectionMode && depthStateUnderCursor != null && selectedDepths.contains(depthStateUnderCursor.depth))
                                || (!selectionMode && doFreeTransform && depthStateUnderCursor != null)) {
                            Matrix matrix = new Matrix();
                            if (depthStateUnderCursor.matrix != null) {
                                matrix = matrix.preConcatenate(new Matrix(depthStateUnderCursor.matrix));
                            }
                            matrix = matrix.concatenate(getParentMatrix());
                            Matrix scaleMatrix = Matrix.getScaleInstance(getRealZoom() / SWF.unitDivisor);

                            matrix = matrix.preConcatenate(scaleMatrix);
                            matrix = matrix.preConcatenate(Matrix.getTranslateInstance(offsetPoint.getX(), offsetPoint.getY()));

                            Point2D cursorPos = new Point2D.Double(e.getX(), e.getY());

                            CharacterTag ch = depthStateUnderCursor.getCharacter();
                            if (ch != null) {
                                if (ch instanceof BoundedTag) {
                                    BoundedTag bt = (BoundedTag) ch;
                                    RECT rect = bt.getRect();

                                    Point2D[] importantPoints = new Point2D[]{
                                        new Point2D.Double(rect.Xmin, rect.Ymin),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, rect.Ymin),
                                        new Point2D.Double(rect.Xmax, rect.Ymin),
                                        new Point2D.Double(rect.Xmin, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double(rect.Xmax, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double(rect.Xmin, rect.Ymax),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, rect.Ymax),
                                        new Point2D.Double(rect.Xmax, rect.Ymax)
                                    };

                                    Point2D nearestPoint = null;
                                    double distance = Double.MAX_VALUE;
                                    for (Point2D p : importantPoints) {
                                        Point2D windowPoint = matrix.transform(p);
                                        double d = windowPoint.distance(cursorPos);
                                        if (d < distance) {
                                            distance = d;
                                            nearestPoint = windowPoint;
                                        }
                                    }

                                    if (distance < TOUCH_POINT_DISTANCE) {
                                        touchPointOffset = new DisplayPoint((int) Math.round(nearestPoint.getX() - cursorPos.getX()), (int) Math.round(nearestPoint.getY() - cursorPos.getY()));
                                    } else {
                                        touchPointOffset = new DisplayPoint(0, 0);
                                    }
                                }

                                //If we wanted touch point on center of edge, then something like this:
                                /*else if (ch instanceof ShapeTag) {
                                    ShapeTag st = (ShapeTag) ch;
                                    List<SHAPERECORD> records = st.getShapes().shapeRecords;
                                    
                                    List<Point2D> points = new ArrayList<>();
                                    int x = 0;
                                    int y = 0;
                                    DisplayPoint prevPoint = new DisplayPoint(x, y);
                                    Point2D prevPoint2d = matrix.transform(new Point2D.Double(0, 0));
                                    for (SHAPERECORD rec : records) {
                                        if (rec instanceof StraightEdgeRecord) {
                                            StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                                            DisplayPoint point = new DisplayPoint(x + ser.deltaX, y + ser.deltaY);
                                            
                                            Point2D point2d = matrix.transform(new Point2D.Double(point.x, point.y));
                                            
                                            BezierEdge be = new BezierEdge(Arrays.asList(prevPoint2d, point2d));
                                            
                                            if (!be.isEmpty()) {
                                                points.add(be.pointAt(0.5));
                                            }
                                            
                                            points.add(point2d);                                            
                                                                                        
                                            prevPoint2d = point2d;
                                            prevPoint = point;
                                        }
                                        if (rec instanceof CurvedEdgeRecord) {
                                            CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                                            DisplayPoint controlPoint = new DisplayPoint(x + cer.controlDeltaX, y + cer.controlDeltaY, false);
                                            DisplayPoint anchorPoint = new DisplayPoint(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);                                            
                                            
                                            Point2D controlPoint2d = matrix.transform(new Point2D.Double(controlPoint.x, controlPoint.y));
                                            Point2D anchorPoint2d = matrix.transform(new Point2D.Double(anchorPoint.x, anchorPoint.y));
                                            BezierEdge be = new BezierEdge(Arrays.asList(prevPoint2d, controlPoint2d, anchorPoint2d));
                                            if (!be.isEmpty()) {
                                                points.add(be.pointAt(0.5));
                                            }
                                            points.add(anchorPoint2d);
                                            prevPoint2d = anchorPoint2d;
                                            prevPoint = anchorPoint;
                                        }
                                        if (rec instanceof StyleChangeRecord) {
                                            StyleChangeRecord scr = (StyleChangeRecord) rec;
                                            if (scr.stateMoveTo) {
                                                DisplayPoint point = new DisplayPoint(scr.moveDeltaX, scr.moveDeltaY);                                                                                                                                                
                                                Point2D point2d = matrix.transform(new Point2D.Double(point.x, point.y));                                            
                                                
                                                points.add(point2d);                                                
                                                prevPoint2d = point2d;
                                                prevPoint = point;                                                
                                            }
                                        }

                                        x = rec.changeX(x);
                                        y = rec.changeY(y);
                                    }
                                    
                                    Point2D nearestPoint = null;
                                    double distance = Double.MAX_VALUE;
                                    for (Point2D p : points) {
                                        double d = p.distance(cursorPos);
                                        if (d < distance) {
                                            nearestPoint = p;
                                            distance = d;
                                        }
                                    }
                                    if (nearestPoint != null && distance <= TOUCH_POINT_DISTANCE) {
                                        touchPointOffset = new DisplayPoint((int) Math.round(nearestPoint.getX() - cursorPos.getX()), (int) Math.round(nearestPoint.getY() - cursorPos.getY()));                                        
                                    } else {
                                        touchPointOffset = new DisplayPoint(0, 0);
                                    }
                                }*/
                            }
                        }

                        if (!autoPlayed) {
                            Configuration.autoPlayPreviews.set(true);
                            autoPlayed = true;
                            play();
                        }
                    }
                    requestFocusInWindow();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {

                        if (hilightedPoints != null) {
                            Rectangle2D selectionRect = getSelectionRect();
                            if (selectionRect != null) {
                                List<Integer> newSelectedPoints = new ArrayList<>();
                                for (int i = 0; i < hilightedPoints.size(); i++) {
                                    DisplayPoint p = hilightedPoints.get(i);
                                    Point2D ip = toImagePoint(new Point2D.Double(p.x, p.y));
                                    if (selectionRect.contains(ip)) {
                                        newSelectedPoints.add(i);
                                    }
                                }
                                if (shiftDown) {
                                    for (int p : selectedPoints) {
                                        if (!newSelectedPoints.contains(p)) {
                                            newSelectedPoints.add(p);
                                        }
                                    }
                                }
                                selectedPoints = newSelectedPoints;
                                calculatePointsXY();
                            }

                            if (ctrlDown && !pathPointsUnderCursor.isEmpty()) {
                                /*List<Integer> positions = new ArrayList<>();
                                List<Double> splitPositions = new ArrayList<>();
                                
                                for(DistanceItem di:pathPointsUnderCursor) {
                                    positions.add(di.pathPoint);
                                    splitPositions.add(di.pathPosition);
                                }*/
                                for (DistanceItem di : pathPointsUnderCursor) {
                                    fireEdgeSplit(di.pathPoint, di.pathPosition);
                                }
                                selectedPoints.clear();
                                pointsUnderCursor.clear();
                                pathPointsUnderCursor.clear();
                                repaint();
                            }
                            updateScrollBarMinMax();
                        }

                        if (dragStart != null && !inMoving && multiSelect) {
                            Rectangle2D selectionRect = getSelectionRect();
                            if (selectionRect != null) {
                                Frame fr = timelined.getTimeline().getFrame(frame);
                                List<Integer> newSelectedDepths = new ArrayList<>();
                                if (fr != null) {
                                    for (int d : fr.layers.keySet()) {
                                        DepthState ds = fr.layers.get(d);
                                        CharacterTag cht = ds.getCharacter();
                                        if (!(cht instanceof DrawableTag)) {
                                            continue;
                                        }
                                        DrawableTag dt = (DrawableTag) cht;
                                        int drawableFrameCount = dt.getNumFrames();
                                        if (drawableFrameCount == 0) {
                                            drawableFrameCount = 1;
                                        }

                                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                                        if (lowQuality) {
                                            zoomDouble /= LQ_FACTOR;
                                        }
                                        Matrix m = new Matrix();
                                        m.translate(-_viewRect.xMin * zoomDouble, -_viewRect.yMin * zoomDouble);
                                        m.scale(zoomDouble);
                                        Matrix transformation = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m.concatenate(new Matrix(ds.matrix)));
                                        RECT dtRect = dt.getRect();
                                        Rectangle2D dtRect2D = new Rectangle2D.Double(dtRect.Xmin, dtRect.Ymin, dtRect.getWidth(), dtRect.getHeight());
                                        Shape outline = transformation.toTransform().createTransformedShape(dtRect2D);
                                        //dt.getOutline(dframe, time, ds.ratio, renderContext, Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m.concatenate(new Matrix(ds.matrix))), true, viewRect, zoom);
                                        Rectangle bounds = outline.getBounds();

                                        if (selectionRect.contains(bounds)) {
                                            newSelectedDepths.add(d);
                                        }
                                    }
                                    if (transformSelectionMode) {
                                        freeTransformDepths(newSelectedDepths);
                                    } else {
                                        selectDepths(newSelectedDepths);
                                    }
                                    firePlaceObjectSelected();
                                }
                            }
                        }

                        dragStart = null;
                        selectionEnd = null;
                        inMoving = false;
                        touchPointOffset = null;
                        snapAlignXPoint1 = null;
                        snapAlignXPoint2 = null;
                        snapAlignYPoint1 = null;
                        snapAlignYPoint2 = null;

                        if (((doFreeTransform && mode != Cursor.DEFAULT_CURSOR) || (selectionMode && transform != null)) && registrationPointUpdated != null && transformUpdated != null) {
                            synchronized (lock) {
                                Rectangle2D transBoundsBefore = getTransformBounds();
                                Point2D transRegPointBefore = registrationPoint;
                                Point transRegPointBeforeTwip = new Point((int) Math.round(transRegPointBefore.getX() - transBoundsBefore.getX()), (int) Math.round(transRegPointBefore.getY() - transBoundsBefore.getY()));
                                Point2D transRegPointPercentBefore = new Point2D.Double(transRegPointBeforeTwip.getX() / transBoundsBefore.getWidth(), transRegPointBeforeTwip.getY() / transBoundsBefore.getHeight());
                                registrationPoint = new Point2D.Double(registrationPointUpdated.getX(), registrationPointUpdated.getY());
                                transform = new Matrix(transformUpdated);
                                transformUpdated = null;

                                Rectangle2D transBoundsAfter = getTransformBounds();
                                Point2D transRegPointAfter = registrationPoint;
                                Point transRegPointAfterTwip = new Point((int) Math.round(transRegPointAfter.getX() - transBoundsAfter.getX()), (int) Math.round(transRegPointAfter.getY() - transBoundsAfter.getY()));
                                Point2D transRegPointPercentAfter = new Point2D.Double(transRegPointAfterTwip.getX() / transBoundsAfter.getWidth(), transRegPointAfterTwip.getY() / transBoundsAfter.getHeight());

                                boolean isResize
                                        = mode == Cursor.E_RESIZE_CURSOR
                                        || mode == Cursor.NE_RESIZE_CURSOR
                                        || mode == Cursor.NW_RESIZE_CURSOR
                                        || mode == Cursor.N_RESIZE_CURSOR
                                        || mode == Cursor.SE_RESIZE_CURSOR
                                        || mode == Cursor.SW_RESIZE_CURSOR
                                        || mode == Cursor.S_RESIZE_CURSOR
                                        || mode == Cursor.W_RESIZE_CURSOR;
                                if (!isResize && mode != Cursor.DEFAULT_CURSOR && !transRegPointPercentBefore.equals(transRegPointPercentAfter)) {
                                    registrationPointPosition = null;
                                }
                            }

                            calcRect(); //do not put this inside synchronized block, it cause deadlock
                            fireBoundsChange(getTransformBounds(), registrationPoint, registrationPointPosition);
                            fireTransformChanged();
                            repaint();
                        }
                        if (selectionMode && !doFreeTransform) {
                            //transform = null;
                        }
                        if (mode != MODE_GUIDE_X && mode != MODE_GUIDE_Y) {
                            mode = Cursor.DEFAULT_CURSOR;
                        }
                        snapOffset = new Point2D.Double(0, 0);
                    }
                }

                private void stopDragging() {

                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    List<DisplayPoint> points = hilightedPoints;

                    if (dragStart != null && multiSelect && !inMoving && mode == Cursor.DEFAULT_CURSOR) {
                        selectionEnd = e.getPoint();
                        repaint();
                        return;
                    }

                    //Snapping
                    if (dragStart != null
                            && (selectionMode
                            || (doFreeTransform && mode == Cursor.MOVE_CURSOR)
                            || (points != null && !selectedPoints.isEmpty() && !pointsUnderCursor.isEmpty()))) {
                        Point2D touchPointPos = new Point2D.Double(e.getX(), e.getY());
                        if (touchPointOffset != null) {
                            touchPointPos = new Point2D.Double(e.getX() + touchPointOffset.x, e.getY() + touchPointOffset.y);
                        }

                        Double snapOffsetX = null;
                        Double snapOffsetY = null;

                        double zoomDouble = getRealZoom();
                        Matrix parentMatrix = getParentMatrix();

                        if (Configuration.snapAlign.get() && timelined != null && points == null && transform != null) {
                            Frame fr = timelined.getTimeline().getFrame(frame);
                            if (fr != null) {
                                Timeline timeline = timelined.getTimeline();

                                Point2D mouseTransPoint = toTransformPoint(new Point2D.Double(e.getX(), e.getY()));
                                double ex = mouseTransPoint.getX();
                                double ey = mouseTransPoint.getY();
                                Point2D dragStartTransPoint = toTransformPoint(dragStart);
                                double dsx = dragStartTransPoint.getX();
                                double dsy = dragStartTransPoint.getY();

                                double deltaX = ex - dsx;
                                double deltaY = ey - dsy;

                                AffineTransform newTransform = new AffineTransform(transform.toTransform());
                                AffineTransform t = parentMatrix.toTransform();
                                t.translate(deltaX, deltaY);
                                AffineTransform tx = parentMatrix.inverse().toTransform();
                                t.concatenate(tx);
                                newTransform.preConcatenate(t);

                                Rectangle2D selectedBounds = null; //toImageRect(getTransformBounds(new Matrix(newTransform))); //null;
                                for (int i = 0; i < selectedDepths.size(); i++) {
                                    int selectedDepth = selectedDepths.get(i);
                                    DepthState ds = null;
                                    if (selectedDepth > -1 && timeline.getFrameCount() > frame && fr != null) {
                                        ds = fr.layers.get(selectedDepth);
                                    }
                                    if (ds != null) {
                                        CharacterTag cht = ds.getCharacter();
                                        if (cht != null) {
                                            if (cht instanceof BoundedTag) {
                                                BoundedTag bt = (BoundedTag) cht;
                                                RECT rect = bt.getRect();

                                                Matrix matrix = toImageMatrix(new Matrix(newTransform));
                                                if (ds.matrix != null) {
                                                    matrix = matrix.concatenate(new Matrix(ds.matrix));
                                                }
                                                Rectangle2D bounds = matrix.transform(new Rectangle2D.Double(rect.Xmin, rect.Ymin, rect.Xmax - rect.Xmin, rect.Ymax - rect.Ymin));
                                                if (selectedBounds == null) {
                                                    selectedBounds = bounds;
                                                } else {
                                                    selectedBounds.add(bounds);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (selectedBounds != null) {
                                    boolean snapAlignedX = false;
                                    boolean snapAlignedY = false;

                                    if (timelined instanceof SWF) {
                                        RECT stageRect = timelined.getRect();

                                        stageRect = new RECT(
                                                (int) Math.round(stageRect.Xmin + Configuration.snapAlignStageBorder.get() * SWF.unitDivisor),
                                                (int) Math.round(stageRect.Xmax - Configuration.snapAlignStageBorder.get() * SWF.unitDivisor),
                                                (int) Math.round(stageRect.Ymin + Configuration.snapAlignStageBorder.get() * SWF.unitDivisor),
                                                (int) Math.round(stageRect.Ymax - Configuration.snapAlignStageBorder.get() * SWF.unitDivisor)
                                        );

                                        Matrix scaleMatrix = Matrix.getScaleInstance(zoomDouble / SWF.unitDivisor);
                                        Matrix matrix = new Matrix();
                                        matrix = matrix.concatenate(Matrix.getTranslateInstance(offsetPoint.getX(), offsetPoint.getY()));
                                        matrix = matrix.concatenate(scaleMatrix);

                                        Rectangle2D bounds = matrix.transform(new Rectangle2D.Double(stageRect.Xmin, stageRect.Ymin, stageRect.getWidth(), stageRect.getHeight()));

                                        if (Math.abs(bounds.getMinX() - selectedBounds.getMinX()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetX = bounds.getMinX() - selectedBounds.getMinX();
                                            snapAlignXPoint1 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMinX()),
                                                    (int) Math.round(selectedBounds.getMinY() - SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignXPoint2 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMinX()),
                                                    (int) Math.round(selectedBounds.getMaxY() + SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignedX = true;
                                        } else if (Math.abs(bounds.getMaxX() - selectedBounds.getMinX()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetX = bounds.getMaxX() - selectedBounds.getMinX();
                                            snapAlignXPoint1 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMaxX()),
                                                    (int) Math.round(selectedBounds.getMinY() - SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignXPoint2 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMaxX()),
                                                    (int) Math.round(selectedBounds.getMaxY() + SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignedX = true;
                                        } else if (Math.abs(bounds.getMaxX() - selectedBounds.getMaxX()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetX = bounds.getMaxX() - selectedBounds.getMaxX();
                                            snapAlignXPoint1 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMaxX()),
                                                    (int) Math.round(selectedBounds.getMinY() - SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignXPoint2 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMaxX()),
                                                    (int) Math.round(selectedBounds.getMaxY() + SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignedX = true;
                                        } else if (Math.abs(bounds.getMinX() - selectedBounds.getMaxX()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetX = bounds.getMinX() - selectedBounds.getMaxX();
                                            snapAlignXPoint1 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMinX()),
                                                    (int) Math.round(selectedBounds.getMinY() - SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignXPoint2 = new DisplayPoint(
                                                    (int) Math.round(bounds.getMinX()),
                                                    (int) Math.round(selectedBounds.getMaxY() + SNAP_ALIGN_AFTER_LINE)
                                            );
                                            snapAlignedX = true;
                                        }

                                        if (Math.abs(bounds.getMinY() - selectedBounds.getMinY()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetY = bounds.getMinY() - selectedBounds.getMinY();
                                            snapAlignYPoint1 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMinX() - SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMinY())
                                            );
                                            snapAlignYPoint2 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMaxX() + SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMinY())
                                            );
                                            snapAlignedY = true;
                                        } else if (Math.abs(bounds.getMaxY() - selectedBounds.getMinY()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetY = bounds.getMaxY() - selectedBounds.getMinY();
                                            snapAlignYPoint1 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMinX() - SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMaxY())
                                            );
                                            snapAlignYPoint2 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMaxX() + SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMaxY())
                                            );
                                            snapAlignedY = true;
                                        } else if (Math.abs(bounds.getMaxY() - selectedBounds.getMaxY()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetY = bounds.getMaxY() - selectedBounds.getMaxY();
                                            snapAlignYPoint1 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMinX() - SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMaxY())
                                            );
                                            snapAlignYPoint2 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMaxX() + SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMaxY())
                                            );
                                            snapAlignedY = true;
                                        } else if (Math.abs(bounds.getMinY() - selectedBounds.getMaxY()) < SNAP_ALIGN_DISTANCE) {
                                            snapOffsetY = bounds.getMinY() - selectedBounds.getMaxY();
                                            snapAlignYPoint1 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMinX() - SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMinY())
                                            );
                                            snapAlignYPoint2 = new DisplayPoint(
                                                    (int) Math.round(selectedBounds.getMaxX() + SNAP_ALIGN_AFTER_LINE),
                                                    (int) Math.round(bounds.getMinY())
                                            );
                                            snapAlignedY = true;
                                        }
                                    }

                                    for (DepthState ds : fr.layers.values()) {
                                        if (selectedDepths.contains(ds.depth)) {
                                            continue;
                                        }
                                        CharacterTag ct = ds.getCharacter();
                                        if (ct != null) {
                                            if (ct instanceof BoundedTag) {
                                                BoundedTag bt = (BoundedTag) ct;
                                                RECT rect = bt.getRect();

                                                Matrix scaleMatrix = Matrix.getScaleInstance(zoomDouble / SWF.unitDivisor);
                                                Matrix translateMatrix = Matrix.getTranslateInstance(offsetPoint.getX(), offsetPoint.getY());

                                                Matrix matrix = translateMatrix.concatenate(scaleMatrix);

                                                Matrix dsMatrix = new Matrix();
                                                if (ds.matrix != null) {
                                                    dsMatrix = new Matrix(ds.matrix);
                                                }
                                                dsMatrix = dsMatrix.concatenate(parentMatrix);

                                                Rectangle2D bounds = dsMatrix.transform(new Rectangle2D.Double(rect.Xmin, rect.Ymin, rect.Xmax - rect.Xmin, rect.Ymax - rect.Ymin));
                                                bounds = new Rectangle2D.Double(
                                                        bounds.getX() - Configuration.snapAlignObjectHorizontalSpace.get() * SWF.unitDivisor,
                                                        bounds.getY() - Configuration.snapAlignObjectVerticalSpace.get() * SWF.unitDivisor,
                                                        bounds.getWidth() + 2 * Configuration.snapAlignObjectHorizontalSpace.get() * SWF.unitDivisor,
                                                        bounds.getHeight() + 2 * Configuration.snapAlignObjectVerticalSpace.get() * SWF.unitDivisor
                                                );

                                                bounds = matrix.transform(bounds);

                                                if (!snapAlignedX) {
                                                    if (Configuration.snapAlignCenterAlignmentVertical.get() && Math.abs(bounds.getCenterX() - selectedBounds.getCenterX()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetX = bounds.getCenterX() - selectedBounds.getCenterX();
                                                        snapAlignXPoint1 = new DisplayPoint(
                                                                (int) Math.round(bounds.getCenterX()),
                                                                (int) Math.round(Math.min(bounds.getMinY(), selectedBounds.getMinY()) - SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignXPoint2 = new DisplayPoint(
                                                                (int) Math.round(bounds.getCenterX()),
                                                                (int) Math.round(Math.max(bounds.getMaxY(), selectedBounds.getMaxY()) + SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignedX = true;
                                                    } else if (Math.abs(bounds.getMinX() - selectedBounds.getMinX()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetX = bounds.getMinX() - selectedBounds.getMinX();
                                                        snapAlignXPoint1 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMinX()),
                                                                (int) Math.round(Math.min(bounds.getMinY(), selectedBounds.getMinY()) - SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignXPoint2 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMinX()),
                                                                (int) Math.round(Math.max(bounds.getMaxY(), selectedBounds.getMaxY()) + SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignedX = true;
                                                    } else if (Math.abs(bounds.getMaxX() - selectedBounds.getMinX()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetX = bounds.getMaxX() - selectedBounds.getMinX();
                                                        snapAlignXPoint1 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMaxX()),
                                                                (int) Math.round(Math.min(bounds.getMinY(), selectedBounds.getMinY()) - SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignXPoint2 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMaxX()),
                                                                (int) Math.round(Math.max(bounds.getMaxY(), selectedBounds.getMaxY()) + SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignedX = true;
                                                    } else if (Math.abs(bounds.getMaxX() - selectedBounds.getMaxX()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetX = bounds.getMaxX() - selectedBounds.getMaxX();
                                                        snapAlignXPoint1 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMaxX()),
                                                                (int) Math.round(Math.min(bounds.getMinY(), selectedBounds.getMinY()) - SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignXPoint2 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMaxX()),
                                                                (int) Math.round(Math.max(bounds.getMaxY(), selectedBounds.getMaxY()) + SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignedX = true;
                                                    } else if (Math.abs(bounds.getMinX() - selectedBounds.getMaxX()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetX = bounds.getMinX() - selectedBounds.getMaxX();
                                                        snapAlignXPoint1 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMinX()),
                                                                (int) Math.round(Math.min(bounds.getMinY(), selectedBounds.getMinY()) - SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignXPoint2 = new DisplayPoint(
                                                                (int) Math.round(bounds.getMinX()),
                                                                (int) Math.round(Math.max(bounds.getMaxY(), selectedBounds.getMaxY()) + SNAP_ALIGN_AFTER_LINE)
                                                        );
                                                        snapAlignedX = true;
                                                    }
                                                }

                                                if (!snapAlignedY) {
                                                    if (Configuration.snapAlignCenterAlignmentHorizontal.get() && Math.abs(bounds.getCenterY() - selectedBounds.getCenterY()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetY = bounds.getCenterY() - selectedBounds.getCenterY();
                                                        snapAlignYPoint1 = new DisplayPoint(
                                                                (int) Math.round(Math.min(bounds.getMinX(), selectedBounds.getMinX()) - SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getCenterY())
                                                        );
                                                        snapAlignYPoint2 = new DisplayPoint(
                                                                (int) Math.round(Math.max(bounds.getMaxX(), selectedBounds.getMaxX()) + SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getCenterY())
                                                        );
                                                        snapAlignedY = true;
                                                    } else if (Math.abs(bounds.getMinY() - selectedBounds.getMinY()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetY = bounds.getMinY() - selectedBounds.getMinY();
                                                        snapAlignYPoint1 = new DisplayPoint(
                                                                (int) Math.round(Math.min(bounds.getMinX(), selectedBounds.getMinX()) - SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMinY())
                                                        );
                                                        snapAlignYPoint2 = new DisplayPoint(
                                                                (int) Math.round(Math.max(bounds.getMaxX(), selectedBounds.getMaxX()) + SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMinY())
                                                        );
                                                        snapAlignedY = true;
                                                    } else if (Math.abs(bounds.getMaxY() - selectedBounds.getMinY()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetY = bounds.getMaxY() - selectedBounds.getMinY();
                                                        snapAlignYPoint1 = new DisplayPoint(
                                                                (int) Math.round(Math.min(bounds.getMinX(), selectedBounds.getMinX()) - SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMaxY())
                                                        );
                                                        snapAlignYPoint2 = new DisplayPoint(
                                                                (int) Math.round(Math.max(bounds.getMaxX(), selectedBounds.getMaxX()) + SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMaxY())
                                                        );
                                                        snapAlignedY = true;
                                                    } else if (Math.abs(bounds.getMaxY() - selectedBounds.getMaxY()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetY = bounds.getMaxY() - selectedBounds.getMaxY();
                                                        snapAlignYPoint1 = new DisplayPoint(
                                                                (int) Math.round(Math.min(bounds.getMinX(), selectedBounds.getMinX()) - SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMaxY())
                                                        );
                                                        snapAlignYPoint2 = new DisplayPoint(
                                                                (int) Math.round(Math.max(bounds.getMaxX(), selectedBounds.getMaxX()) + SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMaxY())
                                                        );
                                                        snapAlignedY = true;
                                                    } else if (Math.abs(bounds.getMinY() - selectedBounds.getMaxY()) < SNAP_ALIGN_DISTANCE) {
                                                        snapOffsetY = bounds.getMinY() - selectedBounds.getMaxY();
                                                        snapAlignYPoint1 = new DisplayPoint(
                                                                (int) Math.round(Math.min(bounds.getMinX(), selectedBounds.getMinX()) - SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMinY())
                                                        );
                                                        snapAlignYPoint2 = new DisplayPoint(
                                                                (int) Math.round(Math.max(bounds.getMaxX(), selectedBounds.getMaxX()) + SNAP_ALIGN_AFTER_LINE),
                                                                (int) Math.round(bounds.getMinY())
                                                        );
                                                        snapAlignedY = true;
                                                    }
                                                }

                                                if (snapAlignedX && snapAlignedY) {
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (!snapAlignedX) {
                                        snapAlignXPoint1 = null;
                                        snapAlignXPoint2 = null;
                                    }
                                    if (!snapAlignedY) {
                                        snapAlignYPoint1 = null;
                                        snapAlignYPoint2 = null;
                                    }
                                }
                            }
                        }

                        if (Configuration.snapToObjects.get()
                                && depthStateUnderCursor != null
                                && !selectedDepths.contains(depthStateUnderCursor.depth)) {
                            CharacterTag ch = depthStateUnderCursor.getCharacter();
                            if (ch != null) {
                                if (ch instanceof BoundedTag) {
                                    BoundedTag bt = (BoundedTag) ch;
                                    RECT rect = bt.getRect();

                                    Matrix matrix = new Matrix();
                                    if (depthStateUnderCursor.matrix != null) {
                                        matrix = matrix.preConcatenate(new Matrix(depthStateUnderCursor.matrix));
                                    }
                                    matrix = matrix.concatenate(parentMatrix);

                                    Matrix scaleMatrix = Matrix.getScaleInstance(zoomDouble / SWF.unitDivisor);

                                    matrix = matrix.preConcatenate(scaleMatrix);
                                    matrix = matrix.preConcatenate(Matrix.getTranslateInstance(offsetPoint.getX(), offsetPoint.getY()));

                                    Point2D[] importantPoints = new Point2D[]{
                                        new Point2D.Double(rect.Xmin, rect.Ymin),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, rect.Ymin),
                                        new Point2D.Double(rect.Xmax, rect.Ymin),
                                        new Point2D.Double(rect.Xmin, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double(rect.Xmax, (rect.Ymin + rect.Ymax) / 2.0),
                                        new Point2D.Double(rect.Xmin, rect.Ymax),
                                        new Point2D.Double((rect.Xmin + rect.Xmax) / 2.0, rect.Ymax),
                                        new Point2D.Double(rect.Xmax, rect.Ymax)
                                    };

                                    Point2D nearestPoint = null;
                                    double distance = Double.MAX_VALUE;
                                    for (Point2D p : importantPoints) {
                                        Point2D windowPoint = matrix.transform(p);
                                        double d = windowPoint.distance(touchPointPos);
                                        if (d < distance) {
                                            distance = d;
                                            nearestPoint = windowPoint;
                                        }
                                    }
                                    if (distance < SNAP_TO_OBJECTS_DISTANCE) {
                                        snapOffsetX = nearestPoint.getX() - touchPointPos.getX();
                                        snapOffsetY = nearestPoint.getY() - touchPointPos.getY();
                                    }
                                }
                            }
                        }

                        if (Configuration.snapToGuides.get()) {
                            if (snapOffsetX == null) {
                                Double nearestGuideX = null;
                                double distance = Double.MAX_VALUE;

                                for (Double gx : guidesX) {
                                    gx = gx * zoomDouble + offsetPoint.getX();
                                    double d = Math.abs(gx - touchPointPos.getX());
                                    if (d < distance) {
                                        distance = d;
                                        nearestGuideX = gx;
                                    }
                                }

                                if (distance < getSnapGuidesDistance()) {
                                    snapOffsetX = nearestGuideX - touchPointPos.getX();
                                }
                            }

                            if (snapOffsetY == null) {
                                Double nearestGuideY = null;
                                double distance = Double.MAX_VALUE;

                                for (Double gy : guidesY) {
                                    gy = gy * zoomDouble + offsetPoint.getY();

                                    double d = Math.abs(gy - touchPointPos.getY());
                                    if (d < distance) {
                                        distance = d;
                                        nearestGuideY = gy;
                                    }
                                }

                                if (distance < getSnapGuidesDistance()) {
                                    snapOffsetY = nearestGuideY - touchPointPos.getY();
                                }
                            }
                        }

                        if (Configuration.showGrid.get() && Configuration.snapToGrid.get()) {
                            if (snapOffsetX == null) {
                                int positionPxX = (int) Math.round((touchPointPos.getX() - offsetPoint.getX()) / zoomDouble);
                                int d = (positionPxX / Configuration.gridHorizontalSpace.get()) * Configuration.gridHorizontalSpace.get();
                                if ((positionPxX - d) * zoomDouble < getSnapGridDistance()) {
                                    snapOffsetX = d * zoomDouble - touchPointPos.getX() + offsetPoint.getX();
                                } else if ((d + Configuration.gridHorizontalSpace.get() - positionPxX) * zoomDouble < getSnapGridDistance()) {
                                    snapOffsetX = (d + Configuration.gridHorizontalSpace.get()) * zoomDouble - touchPointPos.getX() + offsetPoint.getX();
                                }
                            }
                            if (snapOffsetY == null) {
                                int positionPxY = (int) Math.round((touchPointPos.getY() - offsetPoint.getY()) / zoomDouble);
                                int d = (positionPxY / Configuration.gridVerticalSpace.get()) * Configuration.gridVerticalSpace.get();
                                if ((positionPxY - d) * zoomDouble < getSnapGridDistance()) {
                                    snapOffsetY = d * zoomDouble - touchPointPos.getY() + offsetPoint.getY();
                                } else if ((d + Configuration.gridVerticalSpace.get() - positionPxY) * zoomDouble < getSnapGridDistance()) {
                                    snapOffsetY = (d + Configuration.gridVerticalSpace.get()) * zoomDouble - touchPointPos.getY() + offsetPoint.getY();
                                }
                            }
                        }

                        if (Configuration.snapToPixels.get()) {
                            if (snapOffsetX == null) {
                                int positionPxX = (int) Math.round((touchPointPos.getX() - offsetPoint.getX()) / zoomDouble);
                                snapOffsetX = positionPxX * zoomDouble - touchPointPos.getX() + offsetPoint.getX();
                            }
                            if (snapOffsetY == null) {
                                int positionPxY = (int) Math.round((touchPointPos.getY() - offsetPoint.getY()) / zoomDouble);
                                snapOffsetY = positionPxY * zoomDouble - touchPointPos.getY() + offsetPoint.getY();
                            }
                        }

                        if (snapOffsetX == null) {
                            snapOffsetX = 0.0;
                        }
                        if (snapOffsetY == null) {
                            snapOffsetY = 0.0;
                        }

                        snapOffset = new Point2D.Double(snapOffsetX, snapOffsetY);
                    }

                    if (dragStart != null && points != null) {
                        if (pointsUnderCursor.isEmpty()) {
                            selectionEnd = e.getPoint();
                            redraw();
                        }
                        if (!selectedPoints.isEmpty() && !pointsUnderCursor.isEmpty()) {
                            boolean selectedUnderCursor = false;
                            for (int p : pointsUnderCursor) {
                                if (selectedPoints.contains(p)) {
                                    selectedUnderCursor = true;
                                    break;
                                }
                            }
                            if (!selectedUnderCursor) {
                                return;
                            }
                            for (int i = 0; i < selectedPoints.size(); i++) {
                                int pointIndex = selectedPoints.get(i);
                                DisplayPoint pointStart = selectedPointsOriginalValues.get(i);
                                Point2D dragEnd = new Point2D.Double(e.getX() + snapOffset.getX(), e.getY() + snapOffset.getY());
                                Point2D startTransformPoint = toTransformPoint(dragStart);
                                Point2D endTransformPoint = toTransformPoint(dragEnd);
                                Point2D delta = new Point2D.Double(endTransformPoint.getX() - startTransformPoint.getX(), endTransformPoint.getY() - startTransformPoint.getY());
                                DisplayPoint newPoint = new DisplayPoint((int) Math.round(pointStart.x + delta.getX()), (int) Math.round(pointStart.y + delta.getY()), pointStart.onPath);
                                points.set(pointIndex, newPoint);
                            }
                            firePointsUpdated(points);
                            calculatePointsXY();
                            repaint();
                            return;
                        }
                    }
                    if (dragStart != null && allowMove && mode == Cursor.DEFAULT_CURSOR && !selectingText) {
                        Point2D dragEnd = e.getPoint();
                        Point2D delta = new Point2D.Double(dragEnd.getX() - dragStart.getX(), dragEnd.getY() - dragStart.getY());
                        Point2D regPointImage = registrationPoint == null ? null : toImagePoint(registrationPoint);
                        offsetPoint.setLocation(offsetPoint.getX() + delta.getX(), offsetPoint.getY() + delta.getY());
                        updateScrollBars();

                        ExportRectangle oldViewRect = new ExportRectangle(_viewRect);
                        dragStart = dragEnd;
                        iconPanel.calcRect();
                        _viewRect = getViewRect();

                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

                        synchronized (lock) {

                            if (transform != null) {
                                /*Matrix prevTransform = transform.clone();

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
                                 */
                                if (registrationPoint != null) {
                                    Point2D regPointImageUpdated = new Point2D.Double(regPointImage.getX() + delta.getX(), regPointImage.getY() + delta.getY());
                                    registrationPoint = toTransformPoint(regPointImageUpdated);
                                }
                            }

                        }
                        repaint();
                        return;
                    }

                    //move in selection mode
                    if (dragStart != null && selectionMode && !doFreeTransform) {
                        if (transform == null) {
                            return;
                        }
                        Matrix parentMatrix = getParentMatrix();

                        Point2D mouseTransPoint = toTransformPoint(new Point2D.Double(e.getX() + snapOffset.getX(), e.getY() + snapOffset.getY()));
                        Point2D mouseTransPointNoSnapOffset = toTransformPoint(new Point2D.Double(e.getX(), e.getY()));
                        double ex = mouseTransPoint.getX();
                        double ey = mouseTransPoint.getY();
                        Point2D dragStartTransPoint = toTransformPoint(dragStart);
                        double dsx = dragStartTransPoint.getX();
                        double dsy = dragStartTransPoint.getY();

                        double deltaX = ex - dsx;
                        double deltaY = ey - dsy;

                        AffineTransform newTransform = new AffineTransform(transform.toTransform());
                        AffineTransform t = parentMatrix.toTransform();
                        t.translate(deltaX, deltaY);
                        AffineTransform tx = parentMatrix.inverse().toTransform();
                        t.concatenate(tx);
                        newTransform.preConcatenate(t);

                        Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

                        transformUpdated = newTransform;
                        registrationPointUpdated = newRegistrationPoint;
                        repaint();
                        return;
                    }

                    if (dragStart != null && doFreeTransform) {
                        if (transform == null) {
                            return;
                        }

                        Matrix parentMatrix = getParentMatrix();
                        Point2D mouseTransPoint = toTransformPoint(new Point2D.Double(e.getX(), e.getY()));
                        Point2D mouseTransPointSnapped = toTransformPoint(new Point2D.Double(e.getX() + snapOffset.getX(), e.getY() + snapOffset.getY()));

                        //mouseTransPoint = parentMatrix.inverse().transform(mouseTransPoint);
                        double ex = mouseTransPoint.getX();
                        double ey = mouseTransPoint.getY();
                        double exSnapped = mouseTransPointSnapped.getX();
                        double eySnapped = mouseTransPointSnapped.getY();
                        Point2D dragStartTransPoint = toTransformPoint(dragStart);
                        Point2D parentRegistrationPoint = parentMatrix.transform(registrationPoint);
                        double dsx = dragStartTransPoint.getX();
                        double dsy = dragStartTransPoint.getY();

                        if (mode == MODE_SHEAR_N) {

                            double shearX = -(ex - dsx) / (bounds.getHeight());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            Point2D bStart = parentMatrix.transform(new Point2D.Double(bounds.getX(), bounds.getY()));
                            t.translate(bStart.getX(), bStart.getY());
                            t.shear(shearX, 0);
                            t.translate(-bStart.getX(), -bStart.getY());
                            t.translate(ex - dsx, 0);

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == MODE_SHEAR_S) {

                            double shearX = (ex - dsx) / (bounds.getHeight());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            Point2D bStart = parentMatrix.transform(new Point2D.Double(bounds.getX(), bounds.getY()));

                            t.translate(bStart.getX(), bStart.getY());
                            t.shear(shearX, 0);
                            t.translate(-bStart.getX(), -bStart.getY());

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }

                        if (mode == MODE_SHEAR_W) {
                            double shearY = -(ey - dsy) / (bounds.getWidth());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            Point2D bStart = parentMatrix.transform(new Point2D.Double(bounds.getX(), bounds.getY()));
                            t.translate(bStart.getX(), bStart.getY());
                            t.shear(0, shearY);
                            t.translate(-bStart.getX(), -bStart.getY());
                            t.translate(0, ey - dsy);

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                        if (mode == MODE_SHEAR_E) {
                            double shearY = (ey - dsy) / (bounds.getWidth());

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = new AffineTransform();
                            Point2D bStart = parentMatrix.transform(new Point2D.Double(bounds.getX(), bounds.getY()));
                            t.translate(bStart.getX(), bStart.getY());
                            t.shear(0, shearY);
                            t.translate(-bStart.getX(), -bStart.getY());

                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.rotate(deltaTheta, parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.rotate(deltaTheta, parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.rotate(deltaTheta, parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.rotate(deltaTheta, parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            double deltaX = exSnapped - dsx;
                            double deltaY = eySnapped - dsy;

                            AffineTransform newTransform = new AffineTransform(transform.toTransform());
                            AffineTransform t = parentMatrix.toTransform();
                            t.translate(deltaX, deltaY);
                            t.concatenate(parentMatrix.inverse().toTransform());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), 0);
                            t.scale(scaleX, 1);
                            t.translate(-parentRegistrationPoint.getX(), 0);
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), 0);
                            t.scale(scaleX, 1);
                            t.translate(-parentRegistrationPoint.getX(), 0);
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(0, parentRegistrationPoint.getY());
                            t.scale(1, scaleY);
                            t.translate(0, -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(0, parentRegistrationPoint.getY());
                            t.scale(1, scaleY);
                            t.translate(0, -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-parentRegistrationPoint.getX(), -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-parentRegistrationPoint.getX(), -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-parentRegistrationPoint.getX(), -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

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
                            t.translate(parentRegistrationPoint.getX(), parentRegistrationPoint.getY());
                            t.scale(scaleX, scaleY);
                            t.translate(-parentRegistrationPoint.getX(), -parentRegistrationPoint.getY());
                            newTransform.preConcatenate(t);

                            Point2D newRegistrationPoint = new Matrix(t).preConcatenate(parentMatrix.inverse()).concatenate(parentMatrix).transform(registrationPoint);

                            transformUpdated = newTransform;
                            registrationPointUpdated = newRegistrationPoint;
                            repaint();
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    List<DisplayPoint> points = hilightedPoints;
                    if (points != null) {
                        int maxDistance = 5;
                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                        List<Integer> newPointsUnderCursor = new ArrayList<>();
                        for (int i = 0; i < points.size(); i++) {
                            DisplayPoint p = points.get(i);
                            Point2D ip = toImagePoint(new Point2D.Double(p.x, p.y));
                            int ex = e.getX();
                            int ey = e.getY();
                            if (ex > ip.getX() - maxDistance && ex < ip.getX() + maxDistance) {
                                if (ey > ip.getY() - maxDistance && ey < ip.getY() + maxDistance) {
                                    newPointsUnderCursor.add(i);
                                }
                            }
                        }

                        Point2D p = toTransformPoint(e.getPoint());

                        List<DistanceItem> distanceList = new ArrayList<>();
                        for (int i = 0; i < points.size() - 1; i++) {
                            if (points.get(i).onPath && points.get(i + 1).onPath) {
                                DisplayPoint p0 = points.get(i);
                                DisplayPoint p1 = points.get(i + 1);

                                //y = mx + b
                                double lineDistance;
                                Point2D closestPoint;
                                if (p1.x == p0.x) {
                                    lineDistance = Math.abs(p1.x - p.getX());
                                    closestPoint = new Point2D.Double(p1.x, p.getY());
                                } else if (p1.y == p0.y) {
                                    lineDistance = Math.abs(p1.y - p.getY());
                                    closestPoint = new Point2D.Double(p.getX(), p1.y);
                                } else {
                                    double m = (p1.y - p0.y) / (double) (p1.x - p0.x);
                                    double b = p0.y - m * p0.x;

                                    double m_perp = -1 / m;

                                    double b_perp = p.getY() - m_perp * p.getX();

                                    double x = (b_perp - b) / (m - m_perp);
                                    double y = m * x + b;
                                    closestPoint = new Point2D.Double(x, y);
                                    lineDistance = p.distance(closestPoint);
                                }

                                double minX = Math.min(p0.x, p1.x) - maxDistance * SWF.unitDivisor / zoomDouble;
                                double minY = Math.min(p0.y, p1.y) - maxDistance * SWF.unitDivisor / zoomDouble;

                                double maxX = Math.max(p0.x, p1.x) + maxDistance * SWF.unitDivisor / zoomDouble;
                                double maxY = Math.max(p0.y, p1.y) + maxDistance * SWF.unitDivisor / zoomDouble;

                                if (p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY) {
                                    double t = p0.toPoint2D().distance(closestPoint) / p0.toPoint2D().distance(p1.toPoint2D());
                                    if (lineDistance <= maxDistance * SWF.unitDivisor / zoomDouble) {
                                        distanceList.add(new DistanceItem(lineDistance, i + 1, t, new DisplayPoint(closestPoint)));
                                    }
                                }

                            }
                            if (i < points.size() - 2 && !points.get(i + 1).onPath) {
                                DisplayPoint p0 = points.get(i);
                                DisplayPoint p1 = points.get(i + 1);
                                DisplayPoint p2 = points.get(i + 2);

                                BezierUtils bezierUtils = new BezierUtils();
                                double t = bezierUtils.closestPointToBezier(p, p0.toPoint2D(), p1.toPoint2D(), p2.toPoint2D());
                                DisplayPoint closestPoint = new DisplayPoint(bezierUtils.pointAt(t, p0.toPoint2D(), p1.toPoint2D(), p2.toPoint2D()));
                                double curveDistance = Math.sqrt((p.getX() - closestPoint.x) * (p.getX() - closestPoint.x)
                                        + (p.getY() - closestPoint.y) * (p.getY() - closestPoint.y));
                                if (curveDistance <= maxDistance * SWF.unitDivisor / zoomDouble) {
                                    distanceList.add(new DistanceItem(curveDistance, i + 1, t, closestPoint));
                                }
                            }
                        }

                        distanceList.sort(new Comparator<DistanceItem>() {
                            @Override
                            public int compare(DistanceItem o1, DistanceItem o2) {
                                return o2.pathPoint - o1.pathPoint; //Double.compare(o1.distance, o2.distance);
                            }
                        });
                        if (dragStart == null) {
                            /*if (!distanceList.isEmpty()) {
                                DistanceItem di = distanceList.get(0);
                                pathPointUnderCursor = di.pathPoint;
                                pathPointPosition = di.pathPosition;
                                closestPoint = di.closestPoint;
                            } else {
                                pathPointUnderCursor = null;
                                pathPointPosition = null;
                                closestPoint = null;
                            }*/
                            pathPointsUnderCursor = distanceList;
                            pointsUnderCursor = newPointsUnderCursor;
                        }
                        return;
                    }

                    boolean nearGuideX = draggingGuideX;
                    boolean nearGuideY = draggingGuideY;

                    if (!draggingGuideX && !draggingGuideY && Configuration.showGuides.get() && !Configuration.lockGuides.get()) {
                        Point mousePoint = e.getPoint();
                        for (int d = 0; d < guidesX.size(); d++) {
                            Double guide = guidesX.get(d);
                            int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getX());
                            if (mousePoint.x >= guideInPanel - GUIDE_MOVE_TOLERANCE && mousePoint.x <= guideInPanel + GUIDE_MOVE_TOLERANCE) {
                                nearGuideX = true;
                                break;
                            }
                        }

                        for (int d = 0; d < guidesY.size(); d++) {
                            Double guide = guidesY.get(d);
                            int guideInPanel = (int) Math.round(guide * getRealZoom() + offsetPoint.getY());
                            if (mousePoint.y >= guideInPanel - GUIDE_MOVE_TOLERANCE && mousePoint.y <= guideInPanel + GUIDE_MOVE_TOLERANCE) {
                                nearGuideY = true;
                                break;
                            }
                        }
                    }

                    if (doFreeTransform) {
                        if (bounds == null) {
                            return;
                        }
                        if (registrationPoint == null) {
                            return;
                        }

                        Rectangle2D boundsImage = toImageRect(bounds);
                        Point2D regPointImage = toImagePoint(registrationPoint);
                        int ex = e.getX();
                        int ey = e.getY();

                        boolean left = ex >= boundsImage.getX() - TOLERANCE_SCALESHEAR && ex <= boundsImage.getX() + TOLERANCE_SCALESHEAR;
                        boolean right = ex >= boundsImage.getX() + boundsImage.getWidth() - TOLERANCE_SCALESHEAR && ex <= boundsImage.getX() + boundsImage.getWidth() + TOLERANCE_SCALESHEAR;
                        boolean top = ey >= boundsImage.getY() - TOLERANCE_SCALESHEAR && ey <= boundsImage.getY() + TOLERANCE_SCALESHEAR;
                        boolean bottom = ey >= boundsImage.getY() + boundsImage.getHeight() - TOLERANCE_SCALESHEAR && ey <= boundsImage.getY() + boundsImage.getHeight() + TOLERANCE_SCALESHEAR;

                        boolean xcenter = ex >= boundsImage.getCenterX() - TOLERANCE_SCALESHEAR && ex <= boundsImage.getCenterX() + TOLERANCE_SCALESHEAR;
                        boolean ycenter = ey >= boundsImage.getCenterY() - TOLERANCE_SCALESHEAR && ey <= boundsImage.getCenterY() + TOLERANCE_SCALESHEAR;

                        boolean registration = ex >= regPointImage.getX() - REGISTRATION_TOLERANCE
                                && ex <= regPointImage.getX() + REGISTRATION_TOLERANCE
                                && ey >= regPointImage.getY() - REGISTRATION_TOLERANCE
                                && ey <= regPointImage.getY() + REGISTRATION_TOLERANCE;

                        boolean rightRotate = ex > boundsImage.getX() + boundsImage.getWidth() - TOLERANCE_ROTATE && ex
                                <= boundsImage.getX() + boundsImage.getWidth() + TOLERANCE_ROTATE;
                        boolean bottomRotate = ey > boundsImage.getY() + boundsImage.getHeight() - TOLERANCE_ROTATE && ey
                                <= boundsImage.getY() + boundsImage.getHeight() + TOLERANCE_ROTATE;

                        boolean leftRotate = ex < boundsImage.getX() + TOLERANCE_ROTATE
                                && ex >= boundsImage.getX() - TOLERANCE_ROTATE;

                        boolean topRotate = ey < boundsImage.getY() + TOLERANCE_ROTATE
                                && ey >= boundsImage.getY() - TOLERANCE_ROTATE;

                        boolean inBounds = boundsImage.contains(ex, ey);

                        boolean shearX = ex > boundsImage.getX() && ex < boundsImage.getX() + boundsImage.getWidth();
                        boolean shearY = ey > boundsImage.getY() && ey < boundsImage.getY() + boundsImage.getHeight();

                        Cursor cursor;
                        int newMode;
                        if (top && left) {
                            newMode = Cursor.NW_RESIZE_CURSOR;
                            cursor = resizeNWSECursor;
                        } else if (bottom && left) {
                            newMode = Cursor.SW_RESIZE_CURSOR;
                            cursor = resizeSWNECursor;
                        } else if (top && right) {
                            newMode = Cursor.NE_RESIZE_CURSOR;
                            cursor = resizeSWNECursor;
                        } else if (bottom && right) {
                            newMode = Cursor.SE_RESIZE_CURSOR;
                            cursor = resizeNWSECursor;
                        } else if (top && xcenter) {
                            newMode = Cursor.N_RESIZE_CURSOR;
                            cursor = resizeYCursor;
                        } else if (bottom && xcenter) {
                            newMode = Cursor.S_RESIZE_CURSOR;
                            cursor = resizeYCursor;
                        } else if (left && ycenter) {
                            newMode = Cursor.W_RESIZE_CURSOR;
                            cursor = resizeXCursor;
                        } else if (right && ycenter) {
                            newMode = Cursor.E_RESIZE_CURSOR;
                            cursor = resizeXCursor;
                        } else if (registration) {
                            newMode = Cursor.HAND_CURSOR;
                            cursor = moveRegPointCursor;
                        } else if (!inBounds && rightRotate && topRotate) {
                            newMode = MODE_ROTATE_NE;
                            cursor = rotateCursor;
                        } else if (!inBounds && rightRotate && bottomRotate) {
                            newMode = MODE_ROTATE_SE;
                            cursor = rotateCursor;
                        } else if (!inBounds && leftRotate && topRotate) {
                            newMode = MODE_ROTATE_NW;
                            cursor = rotateCursor;
                        } else if (!inBounds && leftRotate && bottomRotate) {
                            newMode = MODE_ROTATE_SW;
                            cursor = rotateCursor;
                        } else if (shearY && (left || right)) {
                            if (left) {
                                newMode = MODE_SHEAR_W;
                            } else {
                                newMode = MODE_SHEAR_E;
                            }
                            cursor = shearYCursor;
                        } else if (shearX && (top || bottom)) {
                            if (top) {
                                newMode = MODE_SHEAR_N;
                            } else {
                                newMode = MODE_SHEAR_S;
                            }
                            cursor = shearXCursor;
                        } else if (nearGuideX) {
                            newMode = MODE_GUIDE_X;
                            cursor = guideXCursor;
                        } else if (nearGuideY) {
                            newMode = MODE_GUIDE_Y;
                            cursor = guideYCursor;
                        } else if (inBounds) {
                            newMode = Cursor.MOVE_CURSOR;
                            cursor = moveCursor;
                        } else {
                            newMode = Cursor.DEFAULT_CURSOR;
                            cursor = defaultCursor;
                        }

                        if (getCursor() != cursor) {
                            setCursor(cursor);
                        }
                        mode = newMode;
                    } else {
                        Cursor cursor = null;
                        Integer newMode = null;
                        if (nearGuideX) {
                            newMode = MODE_GUIDE_X;
                            //cursor = guideXCursor;
                        } else if (nearGuideY) {
                            newMode = MODE_GUIDE_Y;
                            //cursor = guideYCursor;
                        } else {
                            newMode = Cursor.DEFAULT_CURSOR;
                            //cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                        }
                        /*if (getCursor() != cursor) {
                            setCursor(cursor);
                        }*/
                        mode = newMode;
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
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {
                    render();
                    repaint();
                }
            });

        }

        private void setAllowMove(boolean allowMove) {
            this.allowMove = allowMove;
        }

        private void calcRect() {
            synchronized (ImagePanel.this) {
                synchronized (this) {
                    calcRect(zoom);
                }
            }
        }

        private void calcRect(Zoom z) {
            synchronized (ImagePanel.this) {

                Timelined topTimelined = getTopTimelined();
                if (_img != null || topTimelined != null) {
                    //int w1 = (int) (_img.getWidth() * (lowQuality ? LQ_FACTOR : 1));
                    //int h1 = (int) (_img.getHeight() * (lowQuality ? LQ_FACTOR : 1));
                    double zoomDouble = z.fit ? getZoomToFit() : z.value;
                    int w1;
                    int h1;
                    int dx;
                    int dy;
                    if (topTimelined == null || (!autoPlayed && _img != null)) {
                        w1 = (int) (_img.getWidth() * (lowQuality ? LQ_FACTOR : 1));
                        h1 = (int) (_img.getHeight() * (lowQuality ? LQ_FACTOR : 1));
                        dx = 0;
                        dy = 0;
                    } else {
                        w1 = (int) (topTimelined.getRect().getWidth() * zoomDouble / SWF.unitDivisor);
                        h1 = (int) (topTimelined.getRect().getHeight() * zoomDouble / SWF.unitDivisor);
                        dx = (int) (topTimelined.getRect().Xmin * zoomDouble / SWF.unitDivisor);
                        dy = (int) (topTimelined.getRect().Ymin * zoomDouble / SWF.unitDivisor);
                    }

                    //HERE
                    if (doFreeTransform) {
                        //w1 = Math.max(w1, getWidth());
                        //h1 = Math.max(h1, getHeight());
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

                    if (hilightedPoints != null) {
                        setAllowMove(false);
                        //updateScrollBars();
                    } else if (doFreeTransform) {
                        setAllowMove(true);
                    } else if (selectionMode) {
                        setAllowMove(false);
                        if (h < h2 && w < w2) {
                            offsetPoint.setLocation(iconPanel.getWidth() / 2 - w / 2 - dx, iconPanel.getHeight() / 2 - h / 2 - dy);
                            updateScrollBars();
                        }
                    } else {
                        boolean doMove = h > h2 || w > w2;
                        if (zoom.fit) {
                            doMove = false;
                        }
                        setAllowMove(doMove);
                        if (!doMove) {
                            offsetPoint.setLocation(iconPanel.getWidth() / 2 - w / 2 - dx, iconPanel.getHeight() / 2 - h / 2 - dy);
                            updateScrollBars();
                        }
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            VolatileImage ri = this.renderImage;
            if (ri != null) {
                if (ri.validate(View.getDefaultConfiguration()) != VolatileImage.IMAGE_OK) {
                    ri = View.createRenderImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
                    render();
                }

                if (ri != null) {
                    g2d.drawImage(ri, 0, 0, null);
                }
            }

            g2d.setColor(Configuration.guidesColor.get());
            if (draggingGuideX && lastMouseEvent != null) {
                g2d.drawLine(guideDragX, 0, guideDragX, getHeight());
            }

            if (draggingGuideY && lastMouseEvent != null) {
                g2d.drawLine(0, guideDragY, getWidth(), guideDragY);
            }

            if (!Configuration.showGuides.get() && (draggingGuideX || draggingGuideY) && (guideDragX > 0 || guideDragY > 0)) {
                Configuration.showGuides.set(true);
            }

            if (Configuration.showGuides.get()) {
                for (Double guide : guidesX) {
                    int guideRealPx = (int) Math.round(offsetPoint.getX() + guide * getRealZoom());
                    g2d.drawLine(guideRealPx, 0, guideRealPx, getHeight());
                }

                for (Double guide : guidesY) {
                    int guideRealPx = (int) Math.round(offsetPoint.getY() + guide * getRealZoom());
                    g2d.drawLine(0, guideRealPx, getWidth(), guideRealPx);
                }
            }

            if (Configuration._debugMode.get()) {
                g2d.setColor(Color.red);
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                df.setMinimumFractionDigits(0);
                df.setGroupingUsed(false);
                g2d.drawString("frameLoss:" + df.format(getFrameLoss()) + "%", 20, 20);
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
            if (rect == null) {
                return;
            }
            int width = rect.getWidth();
            double scale = 1.0;
            /*if (width > swf.displayRect.getWidth()) {
             scale = (double) swf.displayRect.getWidth() / (double) width;
             }*/
            Matrix m = Matrix.getTranslateInstance(-rect.Xmin, -rect.Ymin);
            m.scale(scale);

            Point p = lastMouseEvent == null ? null : lastMouseEvent.getPoint();

            synchronized (ImagePanel.this) {
                if (timer == thisTimer) {
                    cursorPosition = p;
                }
            }
        }
    }

    private void showSelectedName() {
        if (!selectedDepths.isEmpty() && frame > -1 && timelined != null) {
            Frame f = timelined.getTimeline().getFrame(frame);
            if (f == null) {
                return;
            }
            if (selectedDepths.size() == 1) {
                DepthState ds = f.layers.get(selectedDepths.get(0));
                if (ds != null) {
                    CharacterTag cht = ds.getCharacter();
                    if (cht != null) {
                        debugLabel.setText(cht.getName());
                    }
                }
            } else {
                debugLabel.setText("" + selectedDepths.size() + "x"); //TODO: translate
            }
        }
    }

    public void hideMouseSelection() {
        if (!selectedDepths.isEmpty()) {
            showSelectedName();
        } else {
            debugLabel.setText(DEFAULT_DEBUG_LABEL_TEXT);
        }
    }

    private Integer getRulerFullLinePixels(double z) {
        if (z < 0.0375) {
            return null;
        }

        int fullLinePixels = 2000;

        if (z >= 0.075) {
            fullLinePixels = 1000;
        }

        if (z >= 0.15) {
            fullLinePixels = 500;
        }

        if (z >= 0.31) {
            fullLinePixels = 100;
        }
        if (z >= 0.6) {
            fullLinePixels = 50;
        }
        if (z >= 1.5) {
            fullLinePixels = 20;
        }
        if (z >= 2.99) {
            fullLinePixels = 10;
        }
        if (z >= 5.95) {
            fullLinePixels = 5;
        }
        if (z >= 14.64) {
            fullLinePixels = 2;
        }
        return fullLinePixels;
    }

    public ImagePanel() {
        super(new BorderLayout());
        JPanel p = new JPanel();
        add(p, BorderLayout.CENTER);

        //This is a bit hack so we can drag guides from rulers to iconPanel
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

            private MouseEvent convertMouseEvent(MouseEvent originalEvent, Component newSourceComponent) {
                Point newPoint = SwingUtilities.convertPoint(
                        originalEvent.getComponent(),
                        originalEvent.getPoint(),
                        newSourceComponent
                );

                return new MouseEvent(
                        newSourceComponent,
                        originalEvent.getID(),
                        originalEvent.getWhen(),
                        originalEvent.getModifiersEx(),
                        newPoint.x,
                        newPoint.y,
                        originalEvent.getClickCount(),
                        originalEvent.isPopupTrigger(),
                        originalEvent.getButton()
                );
            }

            private MouseWheelEvent convertMouseWheelEvent(MouseWheelEvent originalEvent, Component newSourceComponent) {
                Point newPoint = SwingUtilities.convertPoint(
                        originalEvent.getComponent(),
                        originalEvent.getPoint(),
                        newSourceComponent
                );

                return new MouseWheelEvent(
                        newSourceComponent,
                        originalEvent.getID(),
                        originalEvent.getWhen(),
                        originalEvent.getModifiersEx(),
                        newPoint.x,
                        newPoint.y,
                        originalEvent.getClickCount(),
                        originalEvent.isPopupTrigger(),
                        originalEvent.getScrollType(),
                        originalEvent.getScrollAmount(),
                        originalEvent.getWheelRotation()
                );
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c != iconPanel) {
                    return;
                }
                for (MouseListener l : iconPanel.mouseListeners) {
                    l.mouseClicked(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());

                if (c == topRuler) {
                    if (topRuler.getCursor() != guideYCursor) {
                        topRuler.setCursor(guideYCursor);
                    }
                } else if (c == leftRuler) {
                    if (leftRuler.getCursor() != guideXCursor) {
                        leftRuler.setCursor(guideXCursor);
                    }
                }

                if (c != iconPanel) {
                    return;
                }
                for (MouseMotionListener l : iconPanel.mouseMotionListeners) {
                    l.mouseDragged(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c != iconPanel) {
                    return;
                }
                for (MouseListener l : iconPanel.mouseListeners) {
                    l.mouseEntered(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c != iconPanel) {
                    return;
                }
                for (MouseListener l : iconPanel.mouseListeners) {
                    l.mouseExited(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c != iconPanel) {
                    return;
                }
                for (MouseMotionListener l : iconPanel.mouseMotionListeners) {
                    l.mouseMoved(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c == topRuler) {
                    draggingGuideY = true;
                    guideDragY = -1;
                    topRuler.setCursor(guideYCursor);
                    mode = MODE_GUIDE_Y;
                    iconPanel.setCursor(guideYCursor);
                } else if (c == leftRuler) {
                    draggingGuideX = true;
                    guideDragX = -1;
                    leftRuler.setCursor(guideXCursor);
                    mode = MODE_GUIDE_X;
                    iconPanel.setCursor(guideXCursor);
                } else if (c == iconPanel) {
                    for (MouseListener l : iconPanel.mouseListeners) {
                        l.mousePressed(convertMouseEvent(e, iconPanel));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());

                if (c == iconPanel) {
                    if (draggingGuideX && guideDragX > 0) {
                        double guide = (guideDragX - offsetPoint.getX()) / getRealZoom();
                        guidesX.add(guide);
                        saveGuides();
                    }
                    if (draggingGuideY && guideDragY > 0) {
                        double guide = (guideDragY - offsetPoint.getY()) / getRealZoom();
                        guidesY.add(guide);
                        saveGuides();
                    }
                }

                draggingGuideX = false;
                draggingGuideY = false;
                guideDragX = -1;
                guideDragY = -1;

                topRuler.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                leftRuler.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                if (c != iconPanel) {
                    return;
                }
                for (MouseListener l : iconPanel.mouseListeners) {
                    l.mouseReleased(convertMouseEvent(e, iconPanel));
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY());
                if (c != iconPanel) {
                    return;
                }
                for (MouseWheelListener l : iconPanel.mouseWheelListeners) {
                    l.mouseWheelMoved(convertMouseWheelEvent(e, iconPanel));
                }
            }
        };
        super.addMouseListener(mouseInputAdapter);
        super.addMouseMotionListener(mouseInputAdapter);
        super.addMouseWheelListener(mouseInputAdapter);

        setOpaque(true);
        setBackground(View.getDefaultBackgroundColor());

        loop = true;
        iconPanel = new IconPanel();
        add(iconPanel, BorderLayout.CENTER);

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        debugLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        topPanel.add(debugLabel);

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyPointsXY();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyPointsXY();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyPointsXY();
            }
        };

        pointEditPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pointEditPanel.add(new JLabel(AppStrings.translate("edit.points.x")));
        pointXTextField = new JTextField(6);
        pointXTextField.getDocument().addDocumentListener(documentListener);
        pointEditPanel.add(pointXTextField);
        pointEditPanel.add(new JLabel(AppStrings.translate("edit.points.y")));
        pointYTextField = new JTextField(6);
        pointYTextField.getDocument().addDocumentListener(documentListener);
        pointEditPanel.add(pointYTextField);

        pointEditPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        topPanel.add(pointEditPanel);

        topRuler = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                double z = getRealZoom();

                Integer fullLinePixels = getRulerFullLinePixels(z);
                if (fullLinePixels == null) {
                    return;
                }

                double fullLineDistance = fullLinePixels * z;
                double leftOffset = GUIDE_THICKNESS;
                Graphics2D g2 = (Graphics2D) g;
                g2.setFont(new Font("Monospaced", Font.PLAIN, GUIDE_FONT_HEIGHT));
                GeneralPath gp = new GeneralPath();

                double minX = leftOffset + offsetPoint.getX();
                for (; minX >= 0; minX -= fullLineDistance) {
                    //empty
                }

                for (double x = minX; x < getWidth(); x += fullLineDistance) {
                    gp.moveTo(x, 0);
                    gp.lineTo(x, getHeight());
                    int px = (int) Math.round((x - leftOffset - offsetPoint.getX()) / z);

                    int smallLineLength = 4;
                    int smallerLineLength = 2;
                    int k = 0;
                    for (double i = 0; i < fullLineDistance; i += fullLineDistance / 10.0, k++) {
                        gp.moveTo(x + i, GUIDE_THICKNESS);
                        gp.lineTo(x + i, GUIDE_THICKNESS - (k % 2 == 0 ? smallLineLength : smallerLineLength));
                    }

                    g2.drawString("" + px, (int) x + 5, GUIDE_THICKNESS - GUIDE_TEXT_OFFSET);
                }
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
                g2.draw(gp);

                if (guideDragX == -1 && lastMouseEvent != null) {
                    int triangleX = lastMouseEvent.getX() + GUIDE_THICKNESS;
                    int triangleHalfWidth = 3;
                    int triangleHeight = 3;
                    int triangleYOffset = 3;

                    Polygon triangle = new Polygon(
                            new int[]{triangleX - triangleHalfWidth, triangleX + triangleHalfWidth, triangleX},
                            new int[]{GUIDE_THICKNESS - triangleHeight - triangleYOffset, GUIDE_THICKNESS - triangleHeight - triangleYOffset, GUIDE_THICKNESS - triangleYOffset},
                            3);
                    g2.setPaint(getForeground());
                    g2.fill(triangle);
                }

                g2.setPaint(getBackground());
                g2.fillRect(0, 0, GUIDE_THICKNESS, GUIDE_THICKNESS);

                if (guideDragX > -1) {
                    g2.setColor(Configuration.guidesColor.get());
                    g2.drawLine(GUIDE_THICKNESS + guideDragX, 0, GUIDE_THICKNESS + guideDragX, GUIDE_THICKNESS);
                }

                if (!selectedDepths.isEmpty() && transform != null) {
                    Rectangle2D transformBounds = transformUpdated == null ? getTransformBounds() : getTransformBounds(new Matrix(transformUpdated));
                    g2.setColor(getForeground());

                    Rectangle2D imgBounds = toImageRect(transformBounds);
                    g2.drawLine((int) Math.round(leftOffset + imgBounds.getMinX()), 0, (int) Math.round(leftOffset + imgBounds.getMinX()), getHeight());
                    g2.drawLine((int) Math.round(leftOffset + imgBounds.getMaxX()), 0, (int) Math.round(leftOffset + imgBounds.getMaxX()), getHeight());
                }
            }
        };
        topRuler.setPreferredSize(new Dimension(1, GUIDE_THICKNESS));
        topPanel.add(topRuler);

        leftRuler = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                double z = getRealZoom();

                Integer fullLinePixels = getRulerFullLinePixels(z);
                if (fullLinePixels == null) {
                    return;
                }

                double fullLineDistance = fullLinePixels * z;
                double topOffset = 0;
                Graphics2D g2 = (Graphics2D) g;
                g2.setFont(new Font("Monospaced", Font.PLAIN, GUIDE_FONT_HEIGHT));
                GeneralPath gp = new GeneralPath();

                double minY = topOffset + offsetPoint.getY();
                for (; minY >= 0; minY -= fullLineDistance) {
                    //empty
                }

                AffineTransform origTransform = g2.getTransform();

                for (double y = minY; y < getHeight(); y += fullLineDistance) {
                    gp.moveTo(0, y);
                    gp.lineTo(getWidth(), y);
                    int py = (int) Math.round((y - topOffset - offsetPoint.getY()) / z);

                    int smallLineLength = 4;
                    int smallerLineLength = 2;
                    int k = 0;
                    for (double i = 0; i < fullLineDistance; i += fullLineDistance / 10.0, k++) {
                        gp.moveTo(GUIDE_THICKNESS, y + i);
                        gp.lineTo(GUIDE_THICKNESS - (k % 2 == 0 ? smallLineLength : smallerLineLength), y + i);
                    }

                    g2.setTransform(origTransform);
                    String drawnString = "" + py;
                    int stringWidth = g2.getFontMetrics().stringWidth(drawnString);
                    AffineTransform fontTrans = new AffineTransform();
                    fontTrans.rotate(-Math.PI / 2, GUIDE_THICKNESS - GUIDE_TEXT_OFFSET, GUIDE_THICKNESS - GUIDE_TEXT_OFFSET);
                    g2.transform(fontTrans);
                    g2.drawString(drawnString, GUIDE_THICKNESS - stringWidth - Math.round(y) - 5, GUIDE_THICKNESS - GUIDE_TEXT_OFFSET);
                }
                g2.setTransform(origTransform);

                if (guideDragY == -1 && lastMouseEvent != null) {
                    int triangleY = lastMouseEvent.getY();
                    int triangleHalfHeight = 3;
                    int triangleWidth = 3;
                    int triangleXOffset = 3;

                    Polygon triangle = new Polygon(
                            new int[]{GUIDE_THICKNESS - triangleWidth - triangleXOffset, GUIDE_THICKNESS - triangleWidth - triangleXOffset, GUIDE_THICKNESS - triangleXOffset},
                            new int[]{triangleY - triangleHalfHeight, triangleY + triangleHalfHeight, triangleY},
                            3);
                    g2.setPaint(getForeground());
                    g2.fill(triangle);
                }

                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
                g2.draw(gp);

                if (guideDragY > -1) {
                    g2.setColor(Configuration.guidesColor.get());
                    g2.drawLine(0, guideDragY, GUIDE_THICKNESS, guideDragY);
                }

                if (!selectedDepths.isEmpty() && transform != null) {
                    Rectangle2D transformBounds = transformUpdated == null ? getTransformBounds() : getTransformBounds(new Matrix(transformUpdated));
                    g2.setColor(getForeground());

                    Rectangle2D imgBounds = toImageRect(transformBounds);
                    g2.drawLine(0, (int) Math.round(imgBounds.getMinY()), getWidth(), (int) Math.round(imgBounds.getMinY()));
                    g2.drawLine(0, (int) Math.round(imgBounds.getMaxY()), getWidth(), (int) Math.round(imgBounds.getMaxY()));
                }
            }
        };
        leftRuler.setPreferredSize(new Dimension(GUIDE_THICKNESS, 1));
        add(leftRuler, BorderLayout.WEST);

        super.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!draggingGuideX && !draggingGuideY) {
                        return;
                    }
                    if (iconPanel == SwingUtilities.getDeepestComponentAt(ImagePanel.this, e.getX(), e.getY())) {
                        Point p = SwingUtilities.convertPoint(ImagePanel.this, e.getX(), e.getY(), iconPanel);
                        if (draggingGuideX) {
                            guideDragX = p.x;
                        }
                        if (draggingGuideY) {
                            guideDragY = p.y;
                        }
                        iconPanel.repaint();
                    }
                }
            }
        });

        pointEditPanel.setVisible(false);
        add(topPanel, BorderLayout.NORTH);

        leftRuler.setVisible(false);
        topRuler.setVisible(false);

        Configuration.showRuler.addListener(new ConfigurationItemChangeListener<Boolean>() {
            @Override
            public void configurationItemChanged(Boolean newValue) {
                updateRulerVisibility();
            }
        });

        horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);

        horizontalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (updatingScrollBars) {
                    return;
                }
                updatingScrollBars = true;
                double zoomDouble = ImagePanel.this.zoom.fit ? getZoomToFit() : ImagePanel.this.zoom.value;
                updateScrollBarMinMax();
                //horizontalScrollBar.setVisible(horizontalScrollBar.getVisibleAmount() < horizontalScrollBar.getMaximum() - horizontalScrollBar.getMinimum());
                //verticalScrollBar.setVisible(verticalScrollBar.getVisibleAmount() < verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum());

                offsetPoint.setLocation(-(horizontalScrollBar.getValue()) * zoomDouble / SWF.unitDivisor, offsetPoint.getY());
                iconPanel.calcRect();
                _viewRect = getViewRect();
                updatingScrollBars = false;
                redraw();
            }
        });

        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (updatingScrollBars) {
                    return;
                }
                updatingScrollBars = true;
                double zoomDouble = ImagePanel.this.zoom.fit ? getZoomToFit() : ImagePanel.this.zoom.value;
                updateScrollBarMinMax();
                //horizontalScrollBar.setVisible(horizontalScrollBar.getVisibleAmount() < horizontalScrollBar.getMaximum() - horizontalScrollBar.getMinimum());
                //verticalScrollBar.setVisible(verticalScrollBar.getVisibleAmount() < verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum());
                offsetPoint.setLocation(offsetPoint.getX(), -(verticalScrollBar.getValue()) * zoomDouble / SWF.unitDivisor);
                iconPanel.calcRect();
                _viewRect = getViewRect();
                redraw();
                updatingScrollBars = false;
            }
        });

        add(horizontalScrollBar, BorderLayout.SOUTH);
        add(verticalScrollBar, BorderLayout.EAST);

        iconPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                synchronized (ImagePanel.this) {
                    lastMouseEvent = e;
                    redraw();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                synchronized (ImagePanel.this) {
                    lastMouseEvent = null;
                    hideMouseSelection();
                    redraw();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (ImagePanel.this) {
                    mouseButton = e.getButton();
                    lastMouseEvent = e;
                    redraw();

                    TextTag text = iconPanel.mouseOverText;
                    if (text == null || (!allowSelectAllTextTypes && (!(text instanceof DefineEditTextTag) || ((DefineEditTextTag) text).noSelect))) {
                        text = null;
                    }
                    if (text != null && !doFreeTransform && !selectionMode) {
                        Rectangle2D glyphRect = iconPanel.glyphUnderCursorRect;
                        if (iconPanel.glyphPosUnderCursor > -1 && glyphRect != null) {
                            if (mouseButton == 1) {
                                textSelectionText = text;
                                textSelectionStartPrecise = iconPanel.glyphPosUnderCursor + (glyphRect.getWidth() == 0 ? 0 : (iconPanel.glyphUnderCursorXPosition - glyphRect.getX()) / glyphRect.getWidth());
                                textSelectionEndPrecise = null;
                                selectingText = true;
                                textSelectionStartGlyphRect = iconPanel.glyphUnderCursorRect;
                                textSelectionStartGlyphXPosition = iconPanel.glyphUnderCursorXPosition;
                            }
                        } else {
                            textSelectionStartPrecise = null;
                        }
                    } else {
                        textSelectionStartPrecise = null;
                    }

                    ButtonTag button = iconPanel.mouseOverButton;
                    if (button != null && !doFreeTransform && !frozenButtons) {
                        DefineButtonSoundTag sounds = button.getSounds();
                        if (!muted && sounds != null && sounds.buttonSoundChar2 != 0) { // OverUpToOverDown
                            CharacterTag soundCharTag = swf.getCharacter(sounds.buttonSoundChar2);
                            if (soundCharTag instanceof SoundTag) {
                                playSound((SoundTag) soundCharTag, sounds.buttonSoundInfo2, timer);
                            }
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
                synchronized (ImagePanel.this) {
                    mouseButton = 0;
                    lastMouseEvent = e;
                    redraw();

                    TextTag text = iconPanel.mouseOverText;
                    if (text == null || (!allowSelectAllTextTypes && (!(text instanceof DefineEditTextTag) || ((DefineEditTextTag) text).noSelect))) {
                        text = null;
                    }
                    if (selectingText && textSelectionStartPrecise != null && text != null && !doFreeTransform && SwingUtilities.isLeftMouseButton(e)) {
                        Rectangle2D glyphRect = iconPanel.glyphUnderCursorRect;
                        if (iconPanel.glyphPosUnderCursor > -1 && glyphRect != null) {
                            textSelectionEndPrecise = iconPanel.glyphPosUnderCursor + (glyphRect.getWidth() == 0 ? 0 : (iconPanel.glyphUnderCursorXPosition - glyphRect.getX()) / glyphRect.getWidth());
                        }
                    }

                    ButtonTag button = iconPanel.mouseOverButton;
                    if (!muted && button != null && !doFreeTransform && !frozenButtons) {
                        DefineButtonSoundTag sounds = button.getSounds();
                        if (sounds != null && sounds.buttonSoundChar3 != 0) { // OverDownToOverUp
                            CharacterTag soundCharTag = swf.getCharacter(sounds.buttonSoundChar3);
                            if (soundCharTag instanceof SoundTag) {
                                playSound((SoundTag) soundCharTag, sounds.buttonSoundInfo3, timer);
                            }
                        }
                    }
                    selectingText = false;
                }
            }
        });
        iconPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                synchronized (ImagePanel.this) {
                    lastMouseEvent = e;
                    redraw();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                synchronized (ImagePanel.this) {
                    lastMouseEvent = e;
                    redraw();

                    TextTag text = iconPanel.mouseOverText;
                    if (text == null || (!allowSelectAllTextTypes && (!(text instanceof DefineEditTextTag) || ((DefineEditTextTag) text).noSelect))) {
                        text = null;
                    }
                    if (selectingText && textSelectionStartPrecise != null && text != null && !doFreeTransform && SwingUtilities.isLeftMouseButton(e)) {
                        Rectangle2D glyphRect = iconPanel.glyphUnderCursorRect;
                        if (iconPanel.glyphPosUnderCursor > -1 && glyphRect != null) {
                            textSelectionEndPrecise = iconPanel.glyphPosUnderCursor + (glyphRect.getWidth() == 0 ? 0 : (iconPanel.glyphUnderCursorXPosition - glyphRect.getX()) / glyphRect.getWidth());
                        }
                    }
                }
            }
        });
        textCursorBlinkTimer = new Timer();
        textCursorBlinkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (textSelectionStartPrecise != null && editTexts) {
                    textCursorBlinkOn = !textCursorBlinkOn;
                    repaint();
                }
            }
        }, 500, 500);
        //*/
    }

    private synchronized void redraw() {
        final Timer thisTimer = timer;
        if (timelined == null) {
            return;
        }
        leftRuler.repaint();
        topRuler.repaint();
        if (thisTimer == null) {
            startTimer(timelined.getTimeline(), false);
        } else {
            //if there is no frameloss (no frames waiting in the queue), 
            // then we can draw immediately to avoid long waiting between frames.
            // This can happen on SWFs with small frameRate
            if (Float.compare(getFrameLoss(), 0f) == 0) {
                thisTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        drawFrame(thisTimer, true);
                    }
                }, 0);
            }
        }
    }

    public Timelined getTimelined() {
        return timelined;
    }

    private void updateScrollBarMinMax() {

        if (timelined == null) {
            horizontalScrollBar.setVisible(false);
            verticalScrollBar.setVisible(false);
            return;
        }
        RECT timRect = timelined.getRect();

        if (timRect == null) {
            return;
        }

        /*
        int h_value = horizontalScrollBar.getValue();
        int h_visibleAmount = horizontalScrollBar.getVisibleAmount();
         */
        int h_maximum = timRect.Xmax;

        if (hilightedPoints != null || doFreeTransform) {
            h_maximum += SCROLL_SPACE_BEFORE;
        }

        /*if (h_value + h_visibleAmount > h_maximum) {
            h_maximum = h_value + h_visibleAmount;
        }*/
        int h_minimum = timRect.Xmin;
        if (hilightedPoints != null || doFreeTransform) {
            h_minimum = timRect.Xmin > 0 ? 0 : timRect.Xmin;
            h_minimum -= SCROLL_SPACE_BEFORE;
        }
        horizontalScrollBar.setMinimum(h_minimum);
        horizontalScrollBar.setMaximum(h_maximum);

        /*int v_value = verticalScrollBar.getValue();
        int v_visibleAmount = verticalScrollBar.getVisibleAmount();
         */
        int v_maximum = timRect.Ymax;
        if (hilightedPoints != null || doFreeTransform) {
            v_maximum += SCROLL_SPACE_BEFORE;
        }
        /*if (v_value + v_visibleAmount > v_maximum) {
            v_maximum = v_value + v_visibleAmount;
        }*/

        int v_minimum = timRect.Ymin;
        if (hilightedPoints != null || doFreeTransform) {
            v_minimum = timRect.Ymin > 0 ? 0 : timRect.Ymin;
            v_minimum -= SCROLL_SPACE_BEFORE;
        }

        verticalScrollBar.setMinimum(v_minimum);
        verticalScrollBar.setMaximum(v_maximum);

        horizontalScrollBar.setVisible(horizontalScrollBar.getVisibleAmount() < horizontalScrollBar.getMaximum() - horizontalScrollBar.getMinimum());
        verticalScrollBar.setVisible(verticalScrollBar.getVisibleAmount() < verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum());
    }

    private synchronized void updateScrollBars() {
        if (!zoomAvailable) {
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {
                    horizontalScrollBar.setVisible(false);
                    verticalScrollBar.setVisible(false);
                }
            });
            return;
        }
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                if (timelined == null) {
                    return;
                }

                updatingScrollBars = true;
                double zoomDouble = ImagePanel.this.zoom.fit ? getZoomToFit() : ImagePanel.this.zoom.value;

                RECT timRect = timelined.getRect();

                int w = iconPanel.getWidth();
                int h = iconPanel.getHeight();

                int h_visibleAmount = (int) Math.round(w * SWF.unitDivisor / zoomDouble);

                Point2D leftTop = toTransformPoint(new Point2D.Double(0, 0));

                Point2D rightBottom = toTransformPoint(new Point2D.Double(w, h));

                int h_value = (int) Math.round(leftTop.getX());
                horizontalScrollBar.setVisibleAmount(h_visibleAmount);
                horizontalScrollBar.setValue(h_value);

                int v_visibleAmount = (int) Math.round(h * SWF.unitDivisor / zoomDouble);
                int v_value = (int) Math.round(leftTop.getY());

                verticalScrollBar.setVisibleAmount(v_visibleAmount);
                verticalScrollBar.setValue(v_value);

                updateScrollBarMinMax();

                if (zoom.fit) {
                    verticalScrollBar.setVisible(false);
                    horizontalScrollBar.setVisible(false);
                    updatingScrollBars = false;
                    return;
                }
                boolean hVisibleBefore = horizontalScrollBar.isVisible();
                horizontalScrollBar.setVisible(horizontalScrollBar.getVisibleAmount() < horizontalScrollBar.getMaximum() - horizontalScrollBar.getMinimum());
                boolean hVisibleAfter = horizontalScrollBar.isVisible();

                boolean vVisibleBefore = verticalScrollBar.isVisible();
                verticalScrollBar.setVisible(verticalScrollBar.getVisibleAmount() < verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum());
                boolean vVisibleAfter = verticalScrollBar.isVisible();

                if (hVisibleAfter != hVisibleBefore || vVisibleAfter != vVisibleBefore) {
                    updateScrollBars();
                }
                updatingScrollBars = false;

            }
        });
    }

    @Override
    public synchronized void zoom(Zoom zoom) {
        zoom(zoom, false, false);
    }

    private synchronized void zoom(Zoom zoom, boolean useCursor, boolean forced) {
        if (!zoomAvailable) {
            return;
        }
        double zoomDoubleBefore = this.zoom.fit ? getZoomToFit() : this.zoom.value;

        boolean modified = this.zoom.value != zoom.value || this.zoom.fit != zoom.fit;
        if (modified || forced) {
            Point localCursorPosition = this.cursorPosition;
            if (!useCursor || localCursorPosition == null) {
                localCursorPosition = new Point(iconPanel.getWidth() / 2, iconPanel.getHeight() / 2);
            }
            Point2D cursorTransBefore = toTransformPoint(localCursorPosition);

            ExportRectangle oldViewRect = new ExportRectangle(_viewRect);
            this.zoom = zoom;
            displayObjectCache.clear();
            double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

            iconPanel.calcRect();
            _viewRect = getViewRect();

            Point2D cursorTransAfter = toTransformPoint(localCursorPosition);

            int dx = (int) (((cursorTransAfter.getX() - cursorTransBefore.getX()) * zoomDouble) / SWF.unitDivisor);
            int dy = (int) (((cursorTransAfter.getY() - cursorTransBefore.getY()) * zoomDouble) / SWF.unitDivisor);

            offsetPoint.setLocation(offsetPoint.getX() + dx, offsetPoint.getY() + dy);

            updateScrollBars();

            iconPanel.calcRect();
            _viewRect = getViewRect();

            synchronized (lock) {
                if (registrationPoint != null) {
                    //registrationPoint = new Point2D.Double(registrationPoint.getX() * zoomDouble / zoomDoubleBefore, registrationPoint.getY() * zoomDouble / zoomDoubleBefore);
                }
            }

            redraw();
            if (textTag != null) {
                setText(textTag, newTextTag);
            }
            topRuler.repaint();
            leftRuler.repaint();

            fireMediaDisplayStateChanged();
        }
    }

    public void zoomFit() {
        Zoom z = new Zoom();
        z.value = 1.0;
        z.fit = true;
        zoom(z, false, true);
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

    private Timer setTimelinedTimer = null;

    public void setTimelined(final Timelined drawable, final SWF swf, int frame, boolean showObjectsUnderCursor, boolean autoPlay, boolean frozen, boolean alwaysDisplay, boolean muted, boolean mutable, boolean allowZoom, boolean frozenButtons, boolean canHaveRuler) {
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
        synchronized (ImagePanel.this) {
            updatingScrollBars = true;
            stopInternal();
            if (drawable instanceof ButtonTag) {
                frame = ButtonTag.FRAME_UP;
            }

            bounds = null;
            displayObjectCache.clear();
            this.timelined = drawable;
            this.parentTimelineds.clear();
            this.parentFrames.clear();
            this.parentDepths.clear();            
            centerImage();
            this.swf = swf;
            zoomAvailable = allowZoom;
            if (frame > -1) {
                this.frame = frame;
                this.stillFrame = true;
            } else {
                this.frame = 0;
                this.stillFrame = false;
            }
            this.prevFrame = -1;
            this.displayedFrame = this.frame;

            RECT timRect = drawable.getRect();

            double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;

            horizontalScrollBar.setMaximum(timRect.Xmax);
            horizontalScrollBar.setMinimum(timRect.Xmin);

            verticalScrollBar.setMaximum(timRect.Ymax);
            verticalScrollBar.setMinimum(timRect.Ymin);

            updateScrollBars();

            loaded = true;

            if (drawable.getTimeline().getFrameCount() == 0) {
                clearImagePanel();
                fireMediaDisplayStateChanged();
                return;
            }

            time = 0;
            drawReady = false;
            autoPlayed = autoPlay;
            this.alwaysDisplay = alwaysDisplay;
            this.frozen = frozen;
            this.frozenButtons = frozenButtons;
            this.muted = muted;
            this.resample = Configuration.previewResampleSound.get();
            this.mutable = mutable;
            depthStateUnderCursor = null;
            hilightedEdge = null;
            hilightedPoints = null;
            selectedDepths = new ArrayList<>();
            selectedPoints = new ArrayList<>();
            textSelectionEndPrecise = null;
            textSelectionStartPrecise = null;
            textSelectionStartGlyphRect = null;
            textSelectionStartGlyphXPosition = null;
            textSelectionText = null;
            pointEditPanel.setVisible(false);
            this.showObjectsUnderCursor = showObjectsUnderCursor;
            this.registrationPointPosition = RegistrationPointPosition.CENTER;
            iconPanel.calcRect();
            
            if (selectionMode) {                
                SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf);
                int chid = -1;
                if (timelined instanceof CharacterTag) {
                    chid = ((CharacterTag) timelined).getCharacterId();
                }
                conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_TIMELINE, "" + chid);
                
                
                loadOpenedDepths();                                
            }
            
            

            clearGuidesInternal();
            setNoGuidesCharacter();

            contentCanHaveRuler = canHaveRuler;
            updateRulerVisibility();
            redraw();
            if (autoPlay) {
                play();
            }
        }

        synchronized (delayObject) {
            try {
                delayObject.wait(drawWaitLimit);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        synchronized (ImagePanel.this) {
            if (!drawReady) {
                clearImagePanel();
            }
        }

        fireMediaDisplayStateChanged();
    }

    @Override
    public synchronized void clearGuides() {
        clearGuidesInternal();
        saveGuides();
        repaint();
    }

    private synchronized void clearGuidesInternal() {
        draggingGuideX = false;
        draggingGuideY = false;
        guideDragX = -1;
        guideDragY = -1;
        guidesX.clear();
        guidesY.clear();
    }

    public synchronized void addGuideX(double guidePixels) {
        guidesX.add(guidePixels);
        repaint();
    }

    public synchronized void addGuideY(double guidePixels) {
        guidesY.add(guidePixels);
        repaint();
    }

    public synchronized void setNoGuidesCharacter() {
        guidesSwf = null;
        guidesCharacterId = -1;
    }

    public synchronized void setGuidesCharacter(SWF swf, int characterId) {
        guidesSwf = swf;
        guidesCharacterId = characterId;
        loadGuidesCharacter();
    }

    private synchronized void loadGuidesCharacter() {
        clearGuidesInternal();
        if (guidesSwf == null) {
            return;
        }
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(guidesSwf.getShortPathTitle());
        if (conf == null) {
            return;
        }
        String guides = conf.getCustomData(CustomConfigurationKeys.KEY_GUIDES, "");
        if (guides.isEmpty()) {
            return;
        }
        List<String> parts = new ArrayList<>();
        if (!guides.isEmpty()) {
            String[] partsArr = guides.split("\\|", -1);
            parts = new ArrayList<>(Arrays.asList(partsArr));
        }
        for (String part : parts) {
            if (part.startsWith("" + guidesCharacterId + ":")) {
                part = part.substring(part.indexOf(":") + 1);
                String[] xy = part.split(";", -1);
                String[] xArr = xy[0].split(",", -1);
                String[] yArr = xy[1].split(",", -1);

                try {
                    if (!xy[0].isEmpty()) {
                        for (String x : xArr) {
                            guidesX.add(Double.parseDouble(x));
                        }
                    }
                    if (!xy[1].isEmpty()) {
                        for (String y : yArr) {
                            guidesY.add(Double.parseDouble(y));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Logger.getLogger(ImagePanel.class.getName()).warning("Invalid configuration of guides. Cannot load.");
                }
                return;
            }
        }
    }

    private synchronized void saveGuides() {
        if (guidesSwf == null) {
            return;
        }
        SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(guidesSwf.getShortPathTitle());
        String previous = conf.getCustomData(CustomConfigurationKeys.KEY_GUIDES, "");

        //Format: character1:x1,x2,x3,x4;y1,y2|character2:...
        List<String> parts = new ArrayList<>();
        if (!previous.isEmpty()) {
            String[] partsArr = previous.split("\\|", -1);
            parts = new ArrayList<>(Arrays.asList(partsArr));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(guidesCharacterId);
        sb.append(":");
        boolean first = true;
        for (Double guide : guidesX) {
            if (!first) {
                sb.append(",");
            }
            sb.append(guide);
            first = false;
        }
        sb.append(";");
        first = true;
        for (Double guide : guidesY) {
            if (!first) {
                sb.append(",");
            }
            sb.append(guide);
            first = false;
        }
        String guidesStr = sb.toString();

        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).startsWith("" + guidesCharacterId + ":")) {
                parts.remove(i);
                i--;
                continue;
            }
            String part = parts.get(0);
            String noChar = part.substring(part.indexOf(":") + 1);
            if (";".equals(noChar)) {
                parts.remove(i);
                i--;
            }
        }
        if (!("" + guidesCharacterId + ":;").equals(guidesStr)) {
            parts.add(guidesStr);
        }

        conf.setCustomData(CustomConfigurationKeys.KEY_GUIDES, String.join("|", parts));
    }

    public synchronized void setImage(SerializableImage image) {
        lda = null;
        setBackground(View.getSwfBackgroundColor());
        clear();

        timelined = null;
        loaded = true;
        stillFrame = true;
        zoomAvailable = false;
        hilightedEdge = null;
        hilightedPoints = null;
        pointEditPanel.setVisible(false);
        iconPanel.setImg(image);
        drawReady = true;

        horizontalScrollBar.setVisible(false);
        verticalScrollBar.setVisible(false);

        clearGuidesInternal();
        setNoGuidesCharacter();

        contentCanHaveRuler = false;
        updateRulerVisibility();
        fireMediaDisplayStateChanged();
    }

    private void updateRulerVisibility() {
        topRuler.setVisible(contentCanHaveRuler && topPanel.isVisible() && Configuration.showRuler.get());
        leftRuler.setVisible(contentCanHaveRuler && topPanel.isVisible() && Configuration.showRuler.get());
    }

    public synchronized void setText(TextTag textTag, TextTag newTextTag) {
        setBackground(View.getSwfBackgroundColor());
        clear();

        lda = null;
        timelined = null;
        loaded = true;
        stillFrame = true;
        zoomAvailable = true;

        contentCanHaveRuler = true;
        updateRulerVisibility();

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
        textTag.toImage(0, 0, 0, new RenderContext(), image, image, false, m, m, m, m, new ConstantColorColorTransform(0xFFC0C0C0), zoomDouble, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, false);

        if (newTextTag != null) {
            newTextTag.toImage(0, 0, 0, new RenderContext(), image, image, false, m, m, m, m, new ConstantColorColorTransform(0xFF000000), zoomDouble, false, new ExportRectangle(rect), new ExportRectangle(rect), true, Timeline.DRAW_MODE_ALL, 0, false);
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
        fireStatusChanged("");
    }

    @Override
    public void close() throws IOException {
        stopInternal();
    }

    private synchronized void stopAllSounds() {
        for (int i = soundPlayers.size() - 1; i >= 0; i--) {
            SoundTagPlayer pl = soundPlayers.get(i);
            pl.close();
        }
        soundPlayers.clear();
    }

    private void clear() {
        Timer ptimer = timer;

        if (ptimer != null) {
            timer = null;
            ptimer.cancel();
            fireMediaDisplayStateChanged();
        }

        textTag = null;
        newTextTag = null;
        displayObjectCache.clear();
    }

    private void nextFrame(Timer thisTimer, final int cnt, final int timeShouldBe) {
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
                drawFrame(thisTimer, true);
            }
        }
        fireMediaDisplayStateChanged();
    }

    public Matrix getParentMatrix() {
        synchronized (lock) {
            Matrix parentMatrix = new Matrix();
            for (int i = 0; i < parentTimelineds.size(); i++) {
                DepthState parentDepthState = parentTimelineds.get(i).getTimeline().getDepthState(parentFrames.get(i), parentDepths.get(i));
                if (parentDepthState == null) {
                    continue;
                }

                parentMatrix = parentMatrix.concatenate(new Matrix(parentDepthState.matrix));
            }
            return parentMatrix;
        }
    }

    public synchronized int getFrame() {
        return frame;
    }

    private static SerializableImage getFrame(Rectangle realRect, RECT rect, ExportRectangle viewRect, SWF swf, int frame, int time, Timelined drawable, RenderContext renderContext, List<Integer> selectedDepths, boolean doFreeTransform, double zoom, Reference<Point2D> registrationPointRef, Reference<Rectangle2D> boundsRef, Matrix transform, Matrix temporaryMatrix, Matrix newMatrix, boolean selectionMode,
            List<Timelined> parentTimelineds, List<Integer> parentDepths, List<Integer> parentFrames,
            Matrix parentMatrix
    ) {
        Timeline timeline = drawable.getTimeline();
        SerializableImage img;

        int width = (int) (viewRect.getWidth() * zoom);
        int height = (int) (viewRect.getHeight() * zoom);
        if (width == 0) {
            width = 1;
        }
        if (height == 0) {
            height = 1;
        }
        SerializableImage image = new SerializableImage((int) Math.ceil(width / SWF.unitDivisor),
                (int) Math.ceil(height / SWF.unitDivisor), SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();

        Matrix m = new Matrix();
        m.translate(-viewRect.xMin * zoom, -viewRect.yMin * zoom);
        m.scale(zoom);

        Matrix fullM = m.clone();

        for (int i = 0; i < selectedDepths.size(); i++) {
            if (newMatrix != null) {
                Frame fr = timeline.getFrame(frame);
                if (fr == null) {
                    continue;
                }
                DepthState ds = fr.layers.get(selectedDepths.get(i));
                if (ds != null) {
                    ds.temporaryMatrix = newMatrix.concatenate(new Matrix(ds.matrix)).toMATRIX();
                }
            }
        }

        Frame fr = timeline.getFrame(frame);

        Frame bgFr = timeline.getFrame(frame);

        if (!parentTimelineds.isEmpty()) {
            bgFr = parentTimelineds.get(0).getTimeline().getFrame(parentFrames.get(0));
        }

        if (bgFr == null || fr == null) {
            return image;
        }
        RGB backgroundColor = bgFr.backgroundColor;
        if (backgroundColor != null) {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            g.setPaint(backgroundColor.toColor());
            g.fillRect(realRect.x, realRect.y, realRect.width, realRect.height);
        }

        if (Configuration.showGrid.get() && (drawable instanceof SWF) && !Configuration.gridOverObjects.get()) {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            drawGridSwf(g, realRect, zoom);
        }

        parentMatrix = new Matrix();
        List<Integer> ignoreDepths = new ArrayList<>();
        for (int i = 0; i < parentTimelineds.size(); i++) {
            Timelined parentTimelined = parentTimelineds.get(i);
            DepthState parentDepthState = parentTimelineds.get(i).getTimeline().getDepthState(parentFrames.get(i), parentDepths.get(i));

            ignoreDepths.add(parentDepthState.depth);
            if (Configuration.halfTransparentParentLayersEasy.get()) {
                parentTimelined.getTimeline().toImage(parentFrames.get(i), 0, new RenderContext(), image, image, false,
                        parentMatrix.preConcatenate(m), new Matrix(), parentMatrix.preConcatenate(m), null, zoom, true, viewRect, viewRect, parentMatrix.preConcatenate(m), true, Timeline.DRAW_MODE_ALL, 0, !Configuration.disableBitmapSmoothing.get(),
                        ignoreDepths);
            }
            parentMatrix = parentMatrix.concatenate(new Matrix(parentDepthState.matrix));
            ignoreDepths.clear();
        }

        if (!parentTimelineds.isEmpty()) {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            g.setPaint(new Color(255, 255, 255, 128));
            g.fillRect(realRect.x, realRect.y, realRect.width, realRect.height);
        }

        timeline.toImage(frame, time, renderContext, image, image, false, parentMatrix.preConcatenate(m), new Matrix(), parentMatrix.preConcatenate(m), null, zoom, true, viewRect, viewRect, parentMatrix.preConcatenate(m), true, Timeline.DRAW_MODE_ALL, 0, !Configuration.disableBitmapSmoothing.get(), ignoreDepths);

        Graphics2D gg = (Graphics2D) image.getGraphics();
        gg.setStroke(new BasicStroke(3));
        gg.setPaint(Color.green);
        gg.setTransform(AffineTransform.getTranslateInstance(0, 0));

        if (!doFreeTransform) {
            for (int selectedDepth : selectedDepths) {
                DepthState ds = null;
                if (selectedDepth > -1 && timeline.getFrameCount() > frame && fr != null) {
                    ds = fr.layers.get(selectedDepth);
                }

                if (ds != null) {
                    CharacterTag cht = ds.getCharacter();
                    if (cht != null) {
                        if (cht instanceof DrawableTag) {
                            DrawableTag dt = (DrawableTag) cht;
                            int drawableFrameCount = dt.getNumFrames();
                            if (drawableFrameCount == 0) {
                                drawableFrameCount = 1;
                            }

                            int dframe = time % drawableFrameCount;
                            Matrix transformation = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m.concatenate(parentMatrix).concatenate(new Matrix(ds.matrix)));
                            RECT dtRect = dt.getRect();
                            Rectangle2D dtRect2D = new Rectangle2D.Double(dtRect.Xmin, dtRect.Ymin, dtRect.getWidth(), dtRect.getHeight());
                            Shape outline = transformation.toTransform().createTransformedShape(dtRect2D);
                            //dt.getOutline(dframe, time, ds.ratio, renderContext, Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m.concatenate(new Matrix(ds.matrix))), true, viewRect, zoom);
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
            }
        }

        Rectangle totalBounds = null;
        for (int i = 0; i < selectedDepths.size(); i++) {
            int selectedDepth = selectedDepths.get(i);
            DepthState ds = null;
            if (selectedDepth > -1 && timeline.getFrameCount() > frame && fr != null) {
                ds = fr.layers.get(selectedDepth);
            }
            if (ds != null) {
                CharacterTag cht = ds.getCharacter();
                if (cht != null) {
                    if (cht instanceof DrawableTag) {
                        DrawableTag dt = (DrawableTag) cht;
                        int drawableFrameCount = dt.getNumFrames();
                        if (drawableFrameCount == 0) {
                            drawableFrameCount = 1;
                        }

                        int dframe = time % drawableFrameCount;

                        if (cht instanceof ButtonTag) {
                            dframe = ButtonTag.FRAME_HITTEST;
                        }

                        //Matrix finalMatrix = Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).concatenate(new Matrix(ds.matrix));
                        Matrix transform2 = transform;

                        transform2 = transform.concatenate(new Matrix(ds.matrix));

                        Shape outline = dt.getOutline(true, dframe, time, ds.ratio, renderContext, transform2, true, viewRect, zoom);

                        if (temporaryMatrix != null) {
                            Matrix tMatrix = temporaryMatrix;
                            tMatrix = tMatrix.concatenate(new Matrix(ds.matrix));
                            Shape tempOutline = dt.getOutline(true, dframe, time, ds.ratio, renderContext, tMatrix, true, viewRect, zoom);
                            gg.setStroke(new BasicStroke(1));
                            gg.setPaint(Color.black);
                            gg.draw(tempOutline);
                        }

                        Rectangle bounds = outline.getBounds();
                        if (totalBounds == null) {
                            totalBounds = new Rectangle(bounds);
                        } else {
                            totalBounds.add(bounds);
                        }
                    }
                }
            }
        }

        if (totalBounds == null) {
            totalBounds = new Rectangle(0, 0, 1, 1);
        }

        boundsRef.setVal(totalBounds);
        gg.setStroke(new BasicStroke(1));
        gg.setPaint(Color.black);
        if (doFreeTransform) {
            gg.draw(totalBounds);
            drawHandles(gg, totalBounds);
            Point2D regPoint = registrationPointRef.getVal();
            if (regPoint == null) {
                regPoint = new Point2D.Double(totalBounds.getCenterX(), totalBounds.getCenterY());
            }
            drawRegistrationPoint(gg, regPoint);
        }

        if (timeline != null && timeline.getFrameCount() > frame) {
            for (int i = 0; i < selectedDepths.size(); i++) {
                int selectedDepth = selectedDepths.get(i);
                DepthState ds = timeline.getDepthState(frame, selectedDepth);
                if (ds != null) {
                    ds.temporaryMatrix = null;
                }
            }
        }

        if (Configuration.showGrid.get() && (drawable instanceof SWF) && Configuration.gridOverObjects.get()) {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            drawGridSwf(g, realRect, zoom);
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
        synchronized (ImagePanel.this) {
            zoom = this.zoom;

            if (timelined == null) {
                return new ExportRectangle(0, 0, 1, 1);
            }
        }

        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }

        RECT timRect = timelined.getRect();
        ExportRectangle viewRect = new ExportRectangle(new RECT());
        viewRect.xMin = -offsetPoint.getX();
        viewRect.yMin = -offsetPoint.getY();

        viewRect.xMin *= SWF.unitDivisor;
        viewRect.xMax *= SWF.unitDivisor;
        viewRect.yMin *= SWF.unitDivisor;
        viewRect.yMax *= SWF.unitDivisor;

        viewRect.xMin /= zoomDouble;
        viewRect.xMax /= zoomDouble;
        viewRect.yMin /= zoomDouble;
        viewRect.yMax /= zoomDouble;

        /*viewRect.xMin += timRect.Xmin;
        viewRect.yMin += timRect.Ymin;
        viewRect.xMax += timRect.Xmin;
        viewRect.yMax += timRect.Ymin;*/
        viewRect.xMax = viewRect.xMin + (int) (iconPanel.getWidth() * SWF.unitDivisor / zoomDouble);
        viewRect.yMax = viewRect.yMin + (int) (iconPanel.getHeight() * SWF.unitDivisor / zoomDouble);
        return viewRect;
    }

    private int getSelectionStartInt() {
        Double selStart = textSelectionStartPrecise;
        Double selEnd = textSelectionEndPrecise;

        if (selStart == null) {
            return -1;
        }

        if (selStart != null && selEnd != null) {
            if (selStart > selEnd) {
                double tmp = selStart;
                selStart = selEnd;
                selEnd = tmp;
            }
        }
        int selStartInt = (int) Math.floor(selStart);
        double startFract = selStart - selStartInt;
        if (startFract > 0.7) {
            selStartInt++;
        }
        return selStartInt;
    }
    
    private int getSelectionEndInt() {
        Double selStart = textSelectionStartPrecise;
        Double selEnd = textSelectionEndPrecise;

        if (selStart == null) {
            return -1;
        }

        if (selStart != null && selEnd != null) {
            if (selStart > selEnd) {
                double tmp = selStart;
                selStart = selEnd;
                selEnd = tmp;
            }
        }
        int selEndInt = (int) Math.floor(selEnd);
        double endFract = selEnd - selEndInt;
        if (endFract > 0.7) {
            selEndInt++;
        }
        return selEndInt;
    }

    private void drawFrame(Timer thisTimer, boolean display) {
        Timelined timelined;
        MouseEvent lastMouseEvent;
        int frame;
        int time;
        Point2D cursorPosition;
        int mouseButton;
        List<Integer> selectedDepths;
        Zoom zoom;
        SWF swf;

        synchronized (ImagePanel.this) {
            timelined = this.timelined;
            lastMouseEvent = this.lastMouseEvent;
        }

        boolean shownAgain = false;

        synchronized (ImagePanel.this) {
            frame = this.frame;
            time = this.time;
            if (this.frame == this.prevFrame) {
                shownAgain = true;
            }

            this.prevFrame = this.frame;
            cursorPosition = this.cursorPosition;
            if (cursorPosition != null) {
                Point2D p2d = toTransformPoint(cursorPosition);
                //p2d = getParentMatrix().inverse().transform(p2d);                
                cursorPosition = new Point2D.Double(p2d.getX() / SWF.unitDivisor, p2d.getY() / SWF.unitDivisor);
            }

            mouseButton = this.mouseButton;
            selectedDepths = this.selectedDepths;
            zoom = this.zoom;
            swf = this.swf;
        }

        if (timelined == null) {
            return;
        }

        /*synchronized (ImagePanel.this) {
            iconPanel.calcRect();
        }*/
        RenderContext renderContext = new RenderContext();
        renderContext.displayObjectCache = displayObjectCache;
        if (cursorPosition != null) { // && (!doFreeTransform || transformSelectionMode)) {
            DisplayPoint touchPoint = new DisplayPoint(cursorPosition);
            if (touchPointOffset != null) {
                touchPoint = new DisplayPoint((int) Math.round(cursorPosition.getX() + touchPointOffset.x), (int) Math.round(cursorPosition.getY() + touchPointOffset.y));
            }

            renderContext.cursorPosition = new Point((int) (touchPoint.x * SWF.unitDivisor), (int) (touchPoint.y * SWF.unitDivisor));
            renderContext.cursorPosition = getParentMatrix().transform(renderContext.cursorPosition);
        }

        renderContext.mouseButton = mouseButton;
        renderContext.stateUnderCursor = new ArrayList<>();
        renderContext.enableButtons = !frozenButtons;

        Double selStart = textSelectionStartPrecise;
        Double selEnd = textSelectionEndPrecise;
        if (selStart != null && selEnd != null) {
            if (selStart > selEnd) {
                double tmp = selStart;
                selStart = selEnd;
                selEnd = tmp;
            }
            int selStartInt = (int) Math.floor(selStart);
            double startFract = selStart - selStartInt;
            int selEndInt = (int) Math.floor(selEnd);
            double endFract = selEnd - selEndInt;

            if (endFract > 0.7) {
                selEndInt++;
            }
            if (startFract > 0.7) {
                selStartInt++;
            }

            renderContext.selectionStart = selStartInt;
            renderContext.selectionEnd = selEndInt;
            renderContext.selectionText = textSelectionText;
        }

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

                Reference<Rectangle2D> boundsRef = new Reference<>(null);

                Timelined t = getTopTimelined();
                if (t == null) {
                    return;
                }
                RECT rect = t.getRect();

                synchronized (ImagePanel.this) {
                    synchronized (lock) {
                        _viewRect = getViewRect();
                    }
                }

                Matrix trans2 = transform == null ? new Matrix() : transform.clone();

                trans2 = toImageMatrix(trans2);

                AffineTransform tempTrans2 = null;
                if (transformUpdated != null) {
                    Matrix matrixUpdated = new Matrix(transformUpdated);
                    matrixUpdated = toImageMatrix(matrixUpdated);
                    tempTrans2 = matrixUpdated.toTransform();
                }

                Rectangle realRect = new Rectangle(rect.Xmin, rect.Ymin, rect.Xmax - rect.Xmin, rect.Ymax - rect.Ymin);
                realRect.x *= zoomDouble;
                realRect.y *= zoomDouble;
                realRect.width *= zoomDouble;
                realRect.height *= zoomDouble;
                realRect.x /= SWF.unitDivisor;
                realRect.y /= SWF.unitDivisor;
                realRect.width /= SWF.unitDivisor;
                realRect.height /= SWF.unitDivisor;
                realRect.x += offsetPoint.getX();
                realRect.y += offsetPoint.getY();

                Point2D rawRegistrationPoint = registrationPoint == null ? null : toImagePoint(registrationPoint);
                Reference<Point2D> registrationPointRef = new Reference<>(rawRegistrationPoint);
                if (!autoPlayed) {
                    img = getImagePlay();
                } else if (_viewRect.getHeight() < 0 || _viewRect.getWidth() < 0) {
                    img = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                } else {
                    img = getFrame(realRect, rect, _viewRect, swf, frame, frozen ? 0 : time, timelined, renderContext, selectedDepths, doFreeTransform, zoomDouble, registrationPointRef, boundsRef, trans2, tempTrans2 == null ? null : new Matrix(tempTrans2), transform, selectionMode, parentTimelineds, parentDepths, parentFrames, getParentMatrix());
                }

                synchronized (ImagePanel.this) {
                    synchronized (lock) {

                        Rectangle2D newBounds = getTransformBounds();
                        if (newBounds != null) {
                            bounds = newBounds;
                            if (registrationPoint == null) {
                                registrationPoint = new Point2D.Double(
                                        newBounds.getCenterX(),
                                        newBounds.getCenterY());
                            }
                        }
                    }
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

            if (autoPlayed) { //!shownAgain
                if (!muted) {
                    List<Integer> sounds = new ArrayList<>();
                    List<String> soundClasses = new ArrayList<>();
                    List<SOUNDINFO> soundInfos = new ArrayList<>();
                    timeline.getSounds(frame, time, renderContext.mouseOverButton, mouseButton, sounds, soundClasses, soundInfos);
                    for (int cid : swf.getCharacters(true).keySet()) {
                        CharacterTag c = swf.getCharacter(cid);
                        for (int k = 0; k < soundClasses.size(); k++) {
                            String cls = soundClasses.get(k);
                            if (cls == null) {
                                continue;
                            }
                            if (c.getClassNames().contains(cls)) {
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

            if (cursorPosition != null && autoPlayed) {
                ret.append(" [").append(formatDouble(cursorPosition.getX())).append(";").append(formatDouble(cursorPosition.getY())).append("]");
                if (showObjectsUnderCursor) {
                    ret.append(" : ");
                }
            }

            boolean handCursor = renderContext.mouseOverButton != null || !autoPlayed && !frozenButtons;

            if (autoPlayed) {
                if (!renderContext.stateUnderCursor.isEmpty()) {
                    depthStateUnderCursor = renderContext.stateUnderCursor.get(renderContext.stateUnderCursor.size() - 1);
                } else {
                    depthStateUnderCursor = null;
                }
            } else {
                depthStateUnderCursor = null;
            }

            if (showObjectsUnderCursor && autoPlayed) {
                boolean first = true;
                for (int i = renderContext.stateUnderCursor.size() - 1; i >= 0; i--) {
                    DepthState ds = renderContext.stateUnderCursor.get(i);
                    if (!first) {
                        if (!showAllDepthLevelsInfo) {
                            break;
                        }
                        ret.append(", ");
                    }

                    first = false;
                    CharacterTag c = ds.getCharacter();

                    ret.append(tagNameResolver.getTagName(c));
                    if (ds.depth > -1) {
                        ret.append(" ");
                        ret.append(AppStrings.translate("imagePanel.depth"));
                        ret.append(" ");
                        ret.append(ds.depth);
                    }
                }

                if (first) {
                    ret.append(DEFAULT_DEBUG_LABEL_TEXT);
                }
            }

            ButtonTag lastMouseOverButton;
            boolean doFreeTransform = this.doFreeTransform;
            synchronized (ImagePanel.this) {
                if (timer == thisTimer) {
                    iconPanel.setImg(img);
                    lastMouseOverButton = iconPanel.mouseOverButton;
                    lastMouseOverText = iconPanel.mouseOverText;
                    iconPanel.mouseOverButton = renderContext.mouseOverButton;
                    iconPanel.mouseOverText = renderContext.mouseOverText;
                    iconPanel.selectionAbsMatrix = renderContext.selectionAbsMatrix;
                    iconPanel.glyphUnderCursorRect = renderContext.glyphUnderCursorRect;
                    iconPanel.glyphUnderCursorXPosition = renderContext.glyphUnderCursorXPosition;
                    iconPanel.glyphPosUnderCursor = renderContext.glyphPosUnderCursor;
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret.length() == 0) {
                                debugLabel.setText(DEFAULT_DEBUG_LABEL_TEXT);
                            } else {
                                debugLabel.setText(ret.toString());
                            }
                            if (hilightedPoints != null) {
                                Cursor newCursor;
                                if (!pointsUnderCursor.isEmpty()) {
                                    newCursor = movePointCursor;
                                } else if (!pathPointsUnderCursor.isEmpty()) {
                                    if (iconPanel.isCtrlDown()) {
                                        newCursor = addPointCursor;
                                    } else {
                                        newCursor = defaultCursor;
                                    }
                                } else {
                                    newCursor = selectCursor;
                                }
                                if (iconPanel.getCursor() != newCursor) { //call setcursor only when needed to avoid cursor flickering when dragging in the tree
                                    iconPanel.setCursor(newCursor);
                                }
                            }
                            if (!doFreeTransform && hilightedPoints == null) {
                                Cursor newCursor;
                                if (mode == MODE_GUIDE_X) {
                                    newCursor = guideXCursor;
                                } else if (mode == MODE_GUIDE_Y) {
                                    newCursor = guideYCursor;
                                } else if (iconPanel.isAltDown() && !selectionMode && !doFreeTransform) {
                                    if (depthStateUnderCursor == null) {
                                        newCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                                    } else {
                                        newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                                    }
                                } else if (handCursor) {
                                    newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                                } else if (!selectionMode && !doFreeTransform && (selectingText || (iconPanel.mouseOverText != null && (allowSelectAllTextTypes || (iconPanel.mouseOverText instanceof DefineEditTextTag && !((DefineEditTextTag) iconPanel.mouseOverText).noSelect))))) {
                                    newCursor = textCursor;
                                } else if (zoomAvailable && iconPanel.hasAllowMove()) {
                                    newCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                                } else {
                                    newCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                                }
                                if (iconPanel.getCursor() != newCursor) { //call setcursor only when needed to avoid cursor flickering when dragging in the tree
                                    iconPanel.setCursor(newCursor);
                                }
                            }
                        }
                    }
                    );

                    if (lastMouseOverText != renderContext.mouseOverText) {
                        /*textSelectionStart = 0;
                        textSelectionEnd = 0;
                        textSelectionStartPrecise = null;
                        textSelectionEndPrecise = null;*/
                    }

                    if (!muted) {
                        if (lastMouseOverButton != renderContext.mouseOverButton) {
                            ButtonTag b = renderContext.mouseOverButton;
                            if (b != null && !doFreeTransform) {
                                // New mouse entered
                                DefineButtonSoundTag sounds = b.getSounds();
                                if (sounds != null && sounds.buttonSoundChar1 != 0) { // IdleToOverUp
                                    CharacterTag soundCharTag = swf.getCharacter(sounds.buttonSoundChar1);
                                    if (soundCharTag instanceof SoundTag) {
                                        playSound((SoundTag) soundCharTag, sounds.buttonSoundInfo1, timer);
                                    }
                                }
                            }

                            b = lastMouseOverButton;
                            if (b != null && !doFreeTransform) {
                                // Old mouse leave
                                DefineButtonSoundTag sounds = b.getSounds();
                                if (sounds != null && sounds.buttonSoundChar0 != 0) { // OverUpToIdle
                                    CharacterTag soundCharTag = swf.getCharacter(sounds.buttonSoundChar0);
                                    if (soundCharTag instanceof SoundTag) {
                                        playSound((SoundTag) soundCharTag, sounds.buttonSoundInfo0, timer);
                                    }
                                }
                            }
                        }
                    }

                    drawReady = true;
                }
            }
            synchronized (delayObject) {
                delayObject.notify();
            }
        }
    }

    private void playSound(SoundTag st, SOUNDINFO soundInfo, Timer thisTimer) {
        synchronized (ImagePanel.this) {
            if (soundInfo.syncNoMultiple || soundInfo.syncStop) {
                for (int s = soundPlayers.size() - 1; s >= 0; s--) {
                    SoundTagPlayer sp = soundPlayers.get(s);
                    if (sp.getTag() == st) {
                        if (soundInfo.syncNoMultiple) {
                            //already playing same sound, return
                            return;
                        }
                        if (soundInfo.syncStop) {
                            sp.stop();
                        }
                    }
                }

                if (soundInfo.syncStop) {
                    return;
                }
            }

            if (soundPlayers.size() > MAX_SOUND_CHANNELS) {
                return;
            }
        }
        final SoundTagPlayer sp;
        try {
            int loopCount = 1;
            if (soundInfo != null && soundInfo.hasLoops) {
                loopCount = Math.max(1, soundInfo.loopCount);
            }

            sp = new SoundTagPlayer(soundInfo, st, loopCount, false, resample);
            sp.addEventListener(new MediaDisplayListener() {
                @Override
                public void mediaDisplayStateChanged(MediaDisplay source) {
                }

                @Override
                public void playingFinished(MediaDisplay source) {
                    synchronized (ImagePanel.this) {
                        sp.close();
                        soundPlayers.remove(sp);
                    }
                }

                @Override
                public void statusChanged(String status) {
                }
            });

            synchronized (ImagePanel.this) {
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
        guidesSwf = null;
        lda = null;
        showObjectsUnderCursor = false;
        fireMediaDisplayStateChanged();
    }

    private synchronized void stopInternal() {
        clear();
        stopAllSounds();
    }

    @Override
    public synchronized void play() {
        this.autoPlayed = true;
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

    private synchronized float getFrameLoss() {
        return 100 - (getFpsIs() / fpsShouldBe * 100);
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

    private void scheduleTask(boolean singleFrame, long msDelay, boolean first) {
        TimerTask task = new TimerTask() {
            public final Timer thisTimer = timer;

            public final boolean isSingleFrame = singleFrame;

            @Override
            public void run() {
                try {
                    synchronized (ImagePanel.this) {
                        if (timer != thisTimer) {
                            return;
                        }
                    }
                    int curFrame = frame;
                    long delay = getMsPerFrame();
                    if (isSingleFrame) {
                        drawFrame(thisTimer, true);

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
                        int ticksFromStart = (int) Math.floor((frameTimeMsIs - startRun) / (double) getMsPerFrame());

                        //How many frames are there between last displayed frame and now. For perfect display(=no framedrop), value should be 1
                        int skipFrames;
                        //Add ticks to first frame when hitting play button, ignoring total framecount => this value can be larger than number of frames in timeline
                        int frameOverMaxShouldBeNow;
                        if (stillFrame) {
                            frameOverMaxShouldBeNow = ticksFromStart;
                            skipFrames = ticksFromStart - time;
                        } else {
                            frameOverMaxShouldBeNow = startFrame + ticksFromStart;

                            //Apply maximum frames repeating, this is actual frame which should be drawed now
                            int frameShouldBeNow = frameOverMaxShouldBeNow % frameCount;

                            skipFrames = frameShouldBeNow - curFrame;
                        }

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
                        if (frameCount == 1 || stillFrame) { //We have only one frame, so the ticks on that frame equal ticks on whole timeline
                            currentFrameTicks = ticksFromStart;
                        }

                        if (first) {
                            drawFrame(thisTimer, true);
                        } else {
                            nextFrame(thisTimer, skipFrames, currentFrameTicks);
                        }

                        long afterDrawFrameTimeMsIs = System.currentTimeMillis();

                        int nextFrameOverMax = frameOverMaxShouldBeNow;
                        while (delay < 0) { //while the frame time already passed
                            nextFrameOverMax++;
                            long nextFrameOverMaxTimeMsShouldBe = startRun + getMsPerFrame() * nextFrameOverMax;
                            delay = nextFrameOverMaxTimeMsShouldBe - afterDrawFrameTimeMsIs;
                        }
                    }
                    synchronized (ImagePanel.this) {
                        if (timer != thisTimer) {
                            return;
                        }
                    }
                    //schedule next run of the task
                    scheduleTask(isSingleFrame, delay, false);

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Frame drawing error", ex);
                }
            }
        };
        synchronized (ImagePanel.this) {
            if (timer != null) {
                timer.schedule(task, msDelay);
            }
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
        scheduleTask(singleFrame, 0, true);
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

    /**
     * Goto frame
     *
     * @param frame 1-based frame
     */
    @Override
    public synchronized void gotoFrame(int frame) {
        if (timelined == null) {
            return;
        }
        Timeline timeline = timelined.getTimeline();
        if (frame > timeline.getFrameCount()) {
            frame = timeline.getFrameCount();
        }
        if (frame < 1) {
            frame = 1;
        }
               
        this.autoPlayed = true;
        this.frame = frame - 1;
        this.prevFrame = -1;
        
        if (selectionMode) {
            SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(timelined.getSwf());
            conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FRAME, "" + frame);
        }
        
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
        zoom(newZoom, true, false);
    }

    private synchronized void zoomOut() {
        Zoom newZoom = new Zoom();
        newZoom.value = getRealZoom() / ZOOM_MULTIPLIER;
        newZoom.fit = false;
        zoom(newZoom, true, false);
    }

    @Override
    public boolean isMutable() {
        return mutable;
    }

    public Point2D getRegistrationPoint() {
        return registrationPoint;
    }

    public RegistrationPointPosition getRegistrationPointPosition() {
        return registrationPointPosition;
    }

    public void setRegistrationPoint(Point2D registrationPoint) {
        this.registrationPoint = registrationPoint;
        this.registrationPointPosition = null;
        redraw();
        fireBoundsChange(getTransformBounds(), registrationPoint, registrationPointPosition);
    }

    public void setRegistrationPointPosition(RegistrationPointPosition position) {
        Rectangle2D transformBounds = getTransformBounds();
        Point2D newRegistrationPoint = new Point2D.Double(
                transformBounds.getX() + transformBounds.getWidth() * position.getPositionX(),
                transformBounds.getY() + transformBounds.getHeight() * position.getPositionY()
        );
        this.registrationPoint = newRegistrationPoint;
        this.registrationPointPosition = position;
        redraw();
        fireBoundsChange(getTransformBounds(), registrationPoint, position);
    }

    public void applyTransformMatrix(Matrix matrix) {
        applyTransformMatrixInternal(matrix);
        redraw();
        fireBoundsChange(getTransformBounds(), registrationPoint, registrationPointPosition);
        fireTransformChanged();
    }

    private void applyTransformMatrixInternal(Matrix matrix) {
        Matrix parentMatrix = getParentMatrix();
        transform = parentMatrix.concatenate(matrix).concatenate(transform).concatenate(parentMatrix.inverse());

        Point2D newRegistrationPoint = new Point2D.Double();
        matrix.toTransform().transform(registrationPoint, newRegistrationPoint);
        registrationPoint = newRegistrationPoint;
    }

    public void setFullTransformMatrix(Matrix matrix) {
        Matrix relativeMatrix = matrix.concatenate(getOriginalMatrix().inverse()).concatenate(transform.inverse());
        Matrix parentMatrix = getParentMatrix();
        transform = parentMatrix.concatenate(matrix).concatenate(getOriginalMatrix().inverse()).concatenate(parentMatrix.inverse());

        Point2D newRegistrationPoint = new Point2D.Double();
        relativeMatrix.toTransform().transform(registrationPoint, newRegistrationPoint);
        registrationPoint = newRegistrationPoint;

        redraw();
        fireBoundsChange(getTransformBounds(), registrationPoint, registrationPointPosition);
        fireTransformChanged();
    }

    private Point2D toTransformPoint(Point2D point) {
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }
        //RECT timRect = timelined.getRect();
        double rx = (point.getX() - offsetPoint.getX()) * SWF.unitDivisor / zoomDouble; // + timRect.Xmin;
        double ry = (point.getY() - offsetPoint.getY()) * SWF.unitDivisor / zoomDouble; // + timRect.Ymin;
        Point2D ret = new Point2D.Double(rx, ry);
        ret = getParentMatrix().inverse().transform(ret);
        return ret;
    }

    private Matrix toImageMatrix(Matrix transform) {
        Matrix m = new Matrix();
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }
        double zoom = zoomDouble;
        m.translate(-_viewRect.xMin * zoom, -_viewRect.yMin * zoom);
        m.scale(zoom);

        Matrix p = getParentMatrix();
        return Matrix.getScaleInstance(1 / SWF.unitDivisor).concatenate(m).concatenate(transform).concatenate(p);
    }

    private Point2D toImagePoint(Point2D point) {
        point = getParentMatrix().transform(point);
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        if (lowQuality) {
            zoomDouble /= LQ_FACTOR;
        }
        double rx = point.getX() * zoomDouble / SWF.unitDivisor + offsetPoint.getX(); // + offsetXRef.getVal();
        double ry = point.getY() * zoomDouble / SWF.unitDivisor + offsetPoint.getY(); // + offsetYRef.getVal();

        Point2D ret = new Point2D.Double(rx, ry);
        return ret;
    }

    private Rectangle2D toImageRect(Rectangle2D rect) {
        Point2D topLeft = toImagePoint(new Point2D.Double(rect.getMinX(), rect.getMinY()));
        Point2D bottomRight = toImagePoint(new Point2D.Double(rect.getMaxX(), rect.getMaxY()));
        return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(), bottomRight.getY() - topLeft.getY());
    }

    private Point2D toParentPoint(Point2D point) {
        point = getParentMatrix().transform(point);
        return point;
    }

    private Rectangle2D toParentRect(Rectangle2D rect) {
        Point2D topLeft = toParentPoint(new Point2D.Double(rect.getMinX(), rect.getMinY()));
        Point2D bottomRight = toParentPoint(new Point2D.Double(rect.getMaxX(), rect.getMaxY()));
        return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(), bottomRight.getY() - topLeft.getY());
    }

    private Rectangle2D getTransformBounds() {
        return getTransformBounds(getNewMatrix());
    }

    private Rectangle2D getTransformBounds(Matrix newMatrix) {
        if (timelined == null) {
            return null;
        }
        int time = frozen ? 0 : this.time;
        DepthState ds = null;
        Timeline timeline = timelined.getTimeline();

        if (timeline.getFrameCount() <= frame) {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }

        if (newMatrix == null) {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }
        RenderContext renderContext = new RenderContext();
        renderContext.displayObjectCache = displayObjectCache;
        if (cursorPosition != null && !doFreeTransform) {
            renderContext.cursorPosition = new Point((int) (cursorPosition.x * SWF.unitDivisor), (int) (cursorPosition.y * SWF.unitDivisor));
        }

        renderContext.mouseButton = mouseButton;
        renderContext.stateUnderCursor = new ArrayList<>();

        Rectangle2D totalBounds = null;
        for (int selectedDepth : selectedDepths) {
            ds = timeline.getFrame(frame).layers.get(selectedDepth);
            if (ds != null) {
                CharacterTag cht = ds.getCharacter();
                if (cht != null) {
                    if (cht instanceof DrawableTag) {
                        DrawableTag dt = (DrawableTag) cht;
                        int drawableFrameCount = dt.getNumFrames();
                        if (drawableFrameCount == 0) {
                            drawableFrameCount = 1;
                        }

                        Matrix b = newMatrix; //getParentMatrix().concatenate(newMatrix); //.concatenate(new Matrix(ds.matrix).inverse());
                        int dframe = time % drawableFrameCount;

                        if (cht instanceof ButtonTag) {
                            dframe = ButtonTag.FRAME_HITTEST;
                        }

                        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
                        if (lowQuality) {
                            zoomDouble /= LQ_FACTOR;
                        }
                        Shape outline = dt.getOutline(true, dframe, time, ds.ratio, renderContext, b.concatenate(new Matrix(ds.matrix)), true, _viewRect, zoomDouble);
                        if (totalBounds == null) {
                            Rectangle2D r = outline.getBounds2D();
                            totalBounds = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                        } else {
                            totalBounds.add(outline.getBounds2D());
                        }
                    }
                }
            }
        }
        if (totalBounds == null) {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }
        return totalBounds;
    }

    class DistanceItem {

        public double distance;
        public int pathPoint;
        public double pathPosition;
        public DisplayPoint closestPoint;

        public DistanceItem(double distance, int pathPoint, double pathPosition, DisplayPoint closestPoint) {
            this.distance = distance;
            this.pathPoint = pathPoint;
            this.pathPosition = pathPosition;
            this.closestPoint = closestPoint;
        }
    }

    public Timelined getTopTimelined() {
        synchronized (lock) {
            if (!parentTimelineds.isEmpty()) {
                return parentTimelineds.get(0);
            }
            return timelined;
        }
    }
    
    public void loadOpenedDepths() {
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(swf);
        if (conf != null) {
            int chid = -1;
            if (!parentTimelineds.isEmpty()) {
                Timelined firstTimelined = parentTimelineds.get(0);
                if (firstTimelined instanceof CharacterTag) {
                    chid = ((CharacterTag) firstTimelined).getCharacterId();
                }
            } else {
                if (timelined instanceof CharacterTag) {
                    chid = ((CharacterTag) timelined).getCharacterId();
                }
            }
            int lastChid = Integer.parseInt(conf.getCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_TIMELINE, "-1"));
            if (lastChid != chid) {
                return;
            }            
            List<String> parentDepths = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_DEPTHS);
            List<String> parentFrames = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_FRAMES);
            while (parentFrames.size() < parentDepths.size()) {
                parentFrames.add("0");
            }
            
            conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_DEPTHS, new ArrayList<>());
            conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_FRAMES, new ArrayList<>());
            
            int frame = Integer.parseInt(conf.getCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FRAME, "1"));
            
            for (int i = 0; i < parentDepths.size(); i++) {                
                openDepth(Integer.parseInt(parentFrames.get(i)), Integer.parseInt(parentDepths.get(i)));
            }

            gotoFrame(frame);            
        } else {
            gotoFrame(1);
        }
    }
    
    public void openDepth(int frame, int depth) {
        Timelined tim = timelined;
        if (tim == null) {
            return;
        }
        if (tim.getTimeline().getFrame(frame) == null) {
            return;
        }
        DepthState ds = tim.getTimeline().getFrame(frame).layers.get(depth);
        if (ds != null) {                            
            CharacterTag cht = ds.getCharacter();
            if (cht instanceof Timelined) {
                int newFrame = 0;
                synchronized (lock) {
                    parentTimelineds.add(timelined);
                    parentDepths.add(ds.depth);
                    parentFrames.add(ds.frame.frame);
                    timelined = (Timelined) cht;
                    selectedDepths.clear();
                    
                    int time = ds.time;
                    newFrame = time % timelined.getFrameCount();
                    if (timelined instanceof ButtonTag) {
                        newFrame = ButtonTag.FRAME_UP;
                    }
                    frame = newFrame;

                    SWF swf = parentTimelineds.get(0).getSwf();
                    SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf);
                    if (parentTimelineds.size() == 1) {
                        conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FIRST_PARENT_FRAME, "" + (ds.frame.frame + 1));
                    }
                    
                    List<String> parentDepths = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_DEPTHS);
                    parentDepths.add("" + ds.depth);
                    conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_DEPTHS, parentDepths);
                    
                    List<String> parentFrames = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_FRAMES);
                    parentFrames.add("" + ds.frame.frame);
                    conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_FRAMES, parentFrames);                
                }
                gotoFrame(newFrame + 1);
                fireMediaDisplayStateChanged();
            }
        }
    }
}
