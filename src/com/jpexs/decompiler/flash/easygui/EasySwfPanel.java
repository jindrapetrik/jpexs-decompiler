/*
 *  Copyright (C) 2024-2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.DefineBeforeUsageFixer;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.easygui.properties.panels.BrushPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.DocumentPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.DrawingShapesPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.DrawingStyleSettings;
import com.jpexs.decompiler.flash.easygui.properties.panels.InstancePropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePointPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapeStylePreviewPanel;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.DisplayPoint;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.PointUpdateListener;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
import com.jpexs.decompiler.flash.gui.ShapeSelectionTransform;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.TransformPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.gui.player.ZoomPanel;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.ShapeLayers;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JPEXS
 */
public class EasySwfPanel extends JPanel {

    private LibraryTreeTable libraryTreeTable;
    private JSplitPane verticalSplitPane;
    private JSplitPane horizontalSplitPane;
    private ImagePanel libraryPreviewPanel;
    private ImagePanel stagePanel;
    private TimelinePanel timelinePanel;
    private ShapeTimelinePanel shapeTimelinePanel;
    private JPanel timelineArea;
    private JButton undoButton;
    private JButton redoButton;
    private UndoManager undoManager;
    private JTabbedPane rightTabbedPane;
    private TransformPanel transformPanel;
    private ShapeSelectionTransform.State shapeTransformState;
    private Timelined timelined;
    private final Map<Timelined, Set<Integer>> hiddenDepthsByTimeline = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Timelined, Set<Integer>> lockedDepthsByTimeline = Collections.synchronizedMap(new WeakHashMap<>());
    // Tag replacements share the state objects of layers that survive the edit.
    private final Map<ShapeTag, List<ShapeLayerState>> shapeLayerStatesByTag = new WeakHashMap<>();
    private ShapeTag editingShape;
    private ShapeTag unfilteredShapePreview;
    private List<SHAPEWITHSTYLE> shapeLayers = Collections.emptyList();
    private List<ShapeLayerState> shapeLayerStates = Collections.emptyList();
    private int activeShapeLayer;
    private ShapeTool shapeTool = ShapeTool.SELECT;
    private final Map<Integer, ShapeSelection> shapeModels = new HashMap<>();
    private Map<Integer, List<ShapeSelection.Selection>> shapeSelections = Collections.emptyMap();
    private BrushStroke brushStroke;
    private PencilStroke pencilStroke;
    private ShapeDrawingGesture shapeDrawing;
    private BezierPenTool penTool;
    private ShapeFillTransform fillTransformSession;
    private ShapeSelection.BatchEdit fillTransformEdit;
    private ShapePointEdit pointEdit;
    private ShapeAnchorMove anchorMove;
    private boolean pointEditDragging;
    private boolean editingPoints;
    private Point2D styleClickStart;
    private boolean styleClickDragged;
    private ShapeSelection.Selection eyedropperSample;
    private List<ShapeSelection.Selection> selectedShapeParts = new ArrayList<>();
    private List<ShapeSelection.Selection> hitTestSelection;
    private AffineTransform hitTestTransform;
    private final List<Shape> selectionHitAreas = new ArrayList<>();
    private boolean updatingTimelined;
    private JLabel timelineLabel;
    private JButton closeTimelineButton;
    private JPanel propertiesPanel;

    private static final class ShapeLayerState {

        private boolean hidden;
        private boolean locked;
    }

    private static final String PROPERTIES_DOCUMENT = "Document";
    private static final String PROPERTIES_INSTANCE = "Instance";
    private static final String PROPERTIES_TEXT = "Text";
    private static final String PROPERTIES_SHAPE = "Shape";
    private static final String PROPERTIES_BRUSH = "Brush";
    private static final String PROPERTIES_PENCIL = "Pencil";
    private static final String PROPERTIES_PEN = "Pen";
    private static final String PROPERTIES_ERASER = "Eraser";
    private static final String PROPERTIES_DRAWING_SHAPES = "DrawingShapes";
    private static final String PROPERTIES_PAINT = "Paint";
    private static final String PROPERTIES_INK = "Ink";
    private static final String PROPERTIES_EYEDROPPER = "Eyedropper";
    private static final String PROPERTIES_EMPTY = "Empty";
    private static final String PROPERTIES_POINTS = "Points";
    private DocumentPropertiesPanel documentPropertiesPanel;
    private InstancePropertiesPanel instancePropertiesPanel;
    private InstancePropertiesPanel textInstancePropertiesPanel;
    private ShapePropertiesPanel shapePropertiesPanel;
    private BrushPropertiesPanel brushPropertiesPanel;
    private ShapePropertiesPanel pencilPropertiesPanel;
    private ShapePropertiesPanel penPropertiesPanel;
    private BrushPropertiesPanel eraserPropertiesPanel;
    private DrawingShapesPropertiesPanel drawingShapesPropertiesPanel;
    private ShapePropertiesPanel paintPropertiesPanel;
    private DrawingStyleSettings drawingStyleSettings;
    private ShapePropertiesPanel inkPropertiesPanel;
    private ShapeStylePreviewPanel eyedropperPropertiesPanel;
    private final MainPanel mainPanel;

    /**
     *
     * @param mainPanel
     */
    public EasySwfPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());

        stagePanel = new ImagePanel(this::getHiddenDepths, this::getLockedDepths);
        stagePanel.setTagNameResolver(new EasyTagNameResolver());
        stagePanel.setShowAllDepthLevelsInfo(false);
        stagePanel.setSelectionMode(true);
        stagePanel.setMultiSelect(true);
        stagePanel.addPointUpdateListener(new PointUpdateListener() {
            @Override
            public void pointsUpdated(List<DisplayPoint> points) {
                if (!shapeLayerEditable() || shapeTool != ShapeTool.EDIT_POINTS) {
                    return;
                }
                if (pointEdit == null) {
                    beginPointEdit();
                    pointEditDragging = false;
                }
                if (pointEdit != null) {
                    pointEdit.updatePoints(points);
                    try {
                        previewShape(createShapeWithEditedLayer(pointEdit.getShape()));
                    } catch (IOException | InterruptedException ex) {
                        logShapeEditFailure(ex);
                    }
                    if (!pointEditDragging) {
                        finishPointEdit(false);
                    }
                }
            }

            @Override
            public boolean pointRemoved(int position) {
                pointsRemoved(Collections.singletonList(position));
                return false;
            }

            @Override
            public void pointsRemoved(List<Integer> positions) {
                if (shapeLayerEditable() && shapeTool == ShapeTool.EDIT_POINTS && !positions.isEmpty()) {
                    beginPointEdit();
                    if (pointEdit != null) {
                        pointEdit.removePoints(positions);
                        finishPointEdit(false);
                    }
                }
            }

            @Override
            public boolean edgeSplit(int position, double splitPoint) {
                if (shapeLayerEditable() && shapeTool == ShapeTool.EDIT_POINTS) {
                    beginPointEdit();
                    if (pointEdit != null) {
                        pointEdit.splitEdge(position, splitPoint);
                        finishPointEdit(false);
                    }
                }
                return false;
            }
        });

        stagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (editingShape == null || shapeTool != ShapeTool.SELECT || e.isConsumed() || e.getClickCount() != 1
                        || !SwingUtilities.isLeftMouseButton(e) || e.isAltDown()) {
                    return;
                }
                selectShapeAt(stagePanel.toTimelinedPoint(e.getPoint()), e.isShiftDown());
            }
        });

        stagePanel.addEventListener(new MediaDisplayListener() {
            @Override
            public void mediaDisplayStateChanged(MediaDisplay source) {
                if (updatingTimelined) {
                    return;
                }
                if (stagePanel.getTimelined() != timelined) {
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            setTimelined(stagePanel.getTimelined(), false, stagePanel.getEditedShape());
                        }
                    });
                    timelinePanel.setFrame(stagePanel.getFrame(), stagePanel.getSelectedDepths());
                }
            }

            @Override
            public void playingFinished(MediaDisplay source) {
            }

            @Override
            public void statusChanged(String status) {
            }
        });

        stagePanel.addPlaceObjectSelectedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Integer> depths = stagePanel.getSelectedDepths();
                //timelinePanel.setDepths(depths);
                timelinePanel.setFrame(stagePanel.getFrame(), depths);
                transformPanel.setVisible(!depths.isEmpty());
                updatePropertiesPanel();
            }
        });

        stagePanel.addTransformChangeListener(new Runnable() {
            @Override
            public void run() {
                final List<Integer> depths = stagePanel.getSelectedDepths();
                final int frame = stagePanel.getFrame();
                final Matrix parentMatrix = stagePanel.getParentMatrix();
                final Matrix newMatrix = parentMatrix.inverse().concatenate(stagePanel.getNewMatrix()).concatenate(parentMatrix);
                final Point2D regPoint = stagePanel.getRegistrationPoint();
                final RegistrationPointPosition regPointPos = stagePanel.getRegistrationPointPosition();

                final List<MATRIX> fpreviousMatrices = new ArrayList<>();

                List<DepthState> dss = getSelectedDepthStates();
                for (DepthState ds : dss) {
                    if (ds == null) {
                        fpreviousMatrices.add(null);
                    } else {
                        fpreviousMatrices.add(ds.matrix);
                    }
                }

                final boolean transformEnabled = transformEnabled();
                undoManager.doOperation(new DoableOperation() {

                    private final List<Boolean> wasModified = new ArrayList<>();
                    Timelined timelined = stagePanel.getTimelined();

                    @Override
                    public void doOperation() {
                        if (timelined != EasySwfPanel.this.timelined) {
                            setTimelined(timelined);
                        }
                        timelinePanel.setFrame(frame, depths);
                        for (int i = 0; i < depths.size(); i++) {
                            int depth = depths.get(i);
                            DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                            wasModified.add(ds.placeObjectTag == null ? false : ds.placeObjectTag.isModified());

                            Matrix contMat = newMatrix.concatenate(new Matrix(fpreviousMatrices.get(i)));

                            if (timelined instanceof ButtonTag) {
                                ButtonTag button = (ButtonTag) timelined;
                                BUTTONRECORD rec = button.getButtonRecordAt(frame, depth, true);
                                rec.placeMatrix = contMat.toMATRIX();
                            } else {
                                ds.placeObjectTag.setMatrix(contMat.toMATRIX());
                                ds.placeObjectTag.setPlaceFlagHasMatrix(newMatrix != null);
                                ds.placeObjectTag.setModified(true);
                            }
                        }
                        timelined.resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepths(depths);
                            stagePanel.setRegistrationPoint(regPoint);
                            if (regPointPos != null) {
                                stagePanel.setRegistrationPointPosition(regPointPos);
                            }
                            transformPanel.setVisible(true);
                        } else {
                            stagePanel.selectDepths(depths);
                        }
                    }

                    @Override
                    public void undoOperation() {
                        setTimelined(timelined);
                        timelinePanel.setFrame(frame, depths);
                        for (int i = 0; i < depths.size(); i++) {
                            int depth = depths.get(i);
                            DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                            if (timelined instanceof ButtonTag) {
                                ButtonTag button = (ButtonTag) timelined;
                                BUTTONRECORD rec = button.getButtonRecordAt(frame, depth, true);
                                rec.placeMatrix = fpreviousMatrices.get(i);
                            } else {
                                ds.placeObjectTag.setMatrix(fpreviousMatrices.get(i));
                                ds.placeObjectTag.setPlaceFlagHasMatrix(fpreviousMatrices != null);
                                if (ds.placeObjectTag != null && !wasModified.get(i)) {
                                    ds.placeObjectTag.setModified(false);
                                }
                            }
                        }
                        stagePanel.getTimelined().resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepths(depths);
                            transformPanel.setVisible(true);
                        } else {
                            stagePanel.selectDepths(depths);
                        }
                    }

                    @Override
                    public String getDescription() {
                        return EasyStrings.translate(transformEnabled ? "action.transform" : "action.move");
                    }
                }, timelined.getSwf());

            }

        });

        stagePanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return editingShape == null && timelined != null && support.isDataFlavorSupported(CharacterTagTransferable.CHARACTERTAG_FLAVOR);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    Transferable transferable = support.getTransferable();
                    Integer characterId = (Integer) transferable.getTransferData(CharacterTagTransferable.CHARACTERTAG_FLAVOR);
                    CharacterTag tag = stagePanel.getTimelined().getSwf().getCharacter(characterId);
                    if ((tag instanceof DefineSpriteTag)
                            || (tag instanceof ShapeTag)
                            || (tag instanceof TextTag)
                            || (tag instanceof ButtonTag)) {
                        Timelined target = stagePanel.getTimelined();
                        int depth = target.getTimeline().getMaxDepth() + 1;
                        Set<Integer> locked = getLockedDepths(target);
                        while (locked.contains(depth)) {
                            depth++;
                        }
                        if (depth > 0xffff) {
                            return false;
                        }
                        final int newDepth = depth;

                        undoManager.doOperation(new TimelinedTagListDoableOperation(EasySwfPanel.this, target) {

                            private List<Tag> swfTags;

                            @Override
                            public void doOperation() {
                                super.doOperation();
                                CharacterTag ch = (CharacterTag) tag;

                                if (timelined.getSwf() != timelined) {
                                    swfTags = timelined.getSwf().getTags().toArrayList();
                                }

                                ShowFrameTag showFrameTag = timelined.getTimeline().getFrame(fframe).showFrameTag;
                                PlaceObject2Tag place = new PlaceObject2Tag(timelined.getSwf());
                                place.depth = newDepth;
                                place.placeFlagHasCharacter = true;
                                place.characterId = ch.getCharacterId();
                                place.matrix = new MATRIX();
                                place.placeFlagHasMatrix = true;
                                place.setTimelined(timelined);

                                if (timelined instanceof ButtonTag) {
                                    ButtonTag button = (ButtonTag) timelined;
                                    button.getButtonRecordAt(fframe, newDepth, true).fromPlaceObject(place);
                                } else {
                                    if (showFrameTag == null) {
                                        timelined.addTag(place);
                                    } else {
                                        timelined.addTag(timelined.indexOfTag(showFrameTag), place);

                                        if (fframe < timelined.getFrameCount() - 1) {
                                            RemoveObject2Tag remove = new RemoveObject2Tag(timelined.getSwf());
                                            remove.depth = newDepth;
                                            timelined.addTag(timelined.indexOfTag(showFrameTag) + 1, remove);
                                        }
                                    }
                                }

                                DefineBeforeUsageFixer fixer = new DefineBeforeUsageFixer();
                                boolean tagOrderChanged = fixer.fixDefineBeforeUsage(timelined.getSwf());
                                if (!tagOrderChanged) {
                                    swfTags = null;
                                }

                                timelined.resetTimeline();
                                stagePanel.repaint();
                                timelinePanel.refresh();
                                timelinePanel.setDepth(newDepth);
                            }

                            @Override
                            public void undoOperation() {
                                super.undoOperation();
                                timelined.resetTimeline();

                                //Tag order changed, put the original tags back
                                if (swfTags != null) {
                                    SWF swf = timelined.getSwf();
                                    ReadOnlyTagList newTags = swf.getTags();
                                    int size = newTags.size();
                                    for (int i = 0; i < size; i++) {
                                        swf.removeTag(0);
                                    }
                                    for (int i = 0; i < swfTags.size(); i++) {
                                        swf.addTag(swfTags.get(i));
                                    }
                                    swf.resetTimeline();
                                }
                                stagePanel.repaint();
                                timelinePanel.refresh();
                                timelinePanel.setFrame(fframe, fdepths);
                            }

                            @Override
                            public String getDescription() {
                                return EasyStrings.translate("action.addToStage");
                            }

                        }, timelined.getSwf());

                        return true;
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                    //ignored
                }

                return false;
            }
        });

        undoManager = new UndoManager();

        JPanel topPanel = new JPanel(new BorderLayout());

        undoButton = new JButton(View.getIcon("undo16"));
        //undoButton.setToolTipText("Undo");
        undoButton.setMargin(new Insets(5, 5, 5, 5));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.undo(timelined.getSwf());
            }
        });

        redoButton = new JButton(View.getIcon("redo16"));
        //redoButton.setToolTipText("Redo");
        redoButton.setMargin(new Insets(5, 5, 5, 5));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.redo(timelined.getSwf());
            }
        });

        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoButton.setToolTipText(EasyStrings.translate("undo.cannot"));
        redoButton.setToolTipText(EasyStrings.translate("redo.cannot"));

        Runnable undoChangeListener = new Runnable() {
            @Override
            public void run() {
                updateUndos();
            }
        };

        undoManager.addChangeListener(undoChangeListener);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftToolbar.add(undoButton);
        leftToolbar.add(redoButton);
        leftToolbar.add(Box.createHorizontalStrut(10));
        timelineLabel = new JLabel("");
        leftToolbar.add(timelineLabel);
        leftToolbar.add(Box.createHorizontalStrut(5));
        closeTimelineButton = new JButton(View.getIcon("cancel16"));
        closeTimelineButton.setMargin(new Insets(5, 5, 5, 5));
        leftToolbar.add(closeTimelineButton);
        closeTimelineButton.setToolTipText(EasyStrings.translate("timeline.item.cancel"));
        closeTimelineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finishFloatingSelection();
                if (editingShape != null && stagePanel.closeShapeEdit()) {
                    return;
                }
                SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(timelined.getSwf());
                conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_DEPTHS, new ArrayList<>());
                conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_PARENT_FRAMES, new ArrayList<>());
                
                String firstParentFrame = conf.getCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FIRST_PARENT_FRAME, "1");
                
                conf.setCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FRAME, firstParentFrame);

                setTimelined(timelined.getSwf());                
            }
        });

        toolbarPanel.add(leftToolbar, BorderLayout.WEST);

        ZoomPanel zoomPanel = new ZoomPanel(stagePanel);
        toolbarPanel.add(zoomPanel, BorderLayout.EAST);

        topPanel.add(toolbarPanel, BorderLayout.NORTH);
        topPanel.add(stagePanel, BorderLayout.CENTER);

        timelinePanel = new TimelinePanel(this, undoManager);

        timelinePanel.addChangeListener(new Runnable() {
            @Override
            public void run() {
                stagePanel.gotoFrame(1 + timelinePanel.getTimelineBodyPanel().getFirstFrame());
                stagePanel.repaint();              
            }
        });
        timelinePanel.addFrameSelectionListener(new FrameSelectionListener() {
            @Override
            public void frameSelected(int frame, List<Integer> depths) {
                stagePanel.pause();
                stagePanel.gotoFrame(frame + 1);
                stagePanel.selectDepths(depths);
                if (transformEnabled()) {
                    stagePanel.freeTransformDepths(depths);
                }

                transformPanel.setVisible(!depths.isEmpty());
                updatePropertiesPanel();                
            }
        });

        shapeTimelinePanel = new ShapeTimelinePanel(this, this::selectShapeLayer, this::addShapeLayer,
                this::removeShapeLayer, this::moveShapeLayer);
        timelineArea = new JPanel(new CardLayout());
        timelineArea.add(timelinePanel, "timeline");
        timelineArea.add(shapeTimelinePanel, "shape");
        verticalSplitPane = new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, timelineArea, Configuration.guiSplitPaneEasyVerticalDividerLocationPercent);

        libraryTreeTable = new LibraryTreeTable(this);
        JScrollPane libraryScrollPane = new FasterScrollPane(libraryTreeTable);

        JPanel libraryPanel = new JPanel(new BorderLayout());
        libraryPanel.add(libraryScrollPane, BorderLayout.CENTER);

        libraryPreviewPanel = new ImagePanel();
        libraryPreviewPanel.setTopPanelVisible(false);

        libraryPanel.add(libraryPreviewPanel, BorderLayout.NORTH);

        libraryPreviewPanel.setPreferredSize(new Dimension(200, 200));

        rightTabbedPane = new JTabbedPane();

        propertiesPanel = new JPanel();
        documentPropertiesPanel = new DocumentPropertiesPanel(undoManager);
        propertiesPanel.setLayout(new CardLayout());

        instancePropertiesPanel = new InstancePropertiesPanel(this, undoManager, false);
        textInstancePropertiesPanel = new InstancePropertiesPanel(this, undoManager, true);
        shapePropertiesPanel = new ShapePropertiesPanel();
        shapePropertiesPanel.setPropertyChangeListener((stroke, property) -> changeShapeStyle(stroke, property));
        brushPropertiesPanel = new BrushPropertiesPanel();
        brushPropertiesPanel.setChangeListener(this::updateShapeTool);
        eraserPropertiesPanel = new BrushPropertiesPanel(true);
        eraserPropertiesPanel.setChangeListener(this::updateShapeTool);
        drawingShapesPropertiesPanel = new DrawingShapesPropertiesPanel();
        drawingShapesPropertiesPanel.setChangeListener(this::updateShapeTool);
        pencilPropertiesPanel = new ShapePropertiesPanel(true);
        penPropertiesPanel = ShapePropertiesPanel.createDrawingStyles();
        penPropertiesPanel.add(new JLabel("<html><div style='width: 250px;'>" + EasyStrings.translate("drawing.pen.hint") + "</div></html>"),
                BorderLayout.NORTH);
        penTool = new BezierPenTool(stagePanel, this::paintPen);
        paintPropertiesPanel = ShapePropertiesPanel.createFillSettings();
        inkPropertiesPanel = new ShapePropertiesPanel(true);
        eyedropperPropertiesPanel = new ShapeStylePreviewPanel();
        drawingStyleSettings = new DrawingStyleSettings(brushPropertiesPanel, pencilPropertiesPanel,
                drawingShapesPropertiesPanel, paintPropertiesPanel, inkPropertiesPanel, penPropertiesPanel, this::updateDrawingSettings);
        propertiesPanel.add(documentPropertiesPanel, PROPERTIES_DOCUMENT);
        propertiesPanel.add(instancePropertiesPanel, PROPERTIES_INSTANCE);
        propertiesPanel.add(textInstancePropertiesPanel, PROPERTIES_TEXT);
        propertiesPanel.add(shapePropertiesPanel, PROPERTIES_SHAPE);
        propertiesPanel.add(brushPropertiesPanel, PROPERTIES_BRUSH);
        propertiesPanel.add(pencilPropertiesPanel, PROPERTIES_PENCIL);
        propertiesPanel.add(penPropertiesPanel, PROPERTIES_PEN);
        propertiesPanel.add(eraserPropertiesPanel, PROPERTIES_ERASER);
        propertiesPanel.add(drawingShapesPropertiesPanel, PROPERTIES_DRAWING_SHAPES);
        propertiesPanel.add(paintPropertiesPanel, PROPERTIES_PAINT);
        propertiesPanel.add(inkPropertiesPanel, PROPERTIES_INK);
        propertiesPanel.add(eyedropperPropertiesPanel, PROPERTIES_EYEDROPPER);
        propertiesPanel.add(new ShapePointPropertiesPanel(stagePanel), PROPERTIES_POINTS);
        propertiesPanel.add(new JPanel(), PROPERTIES_EMPTY);

        rightTabbedPane.addTab(EasyStrings.translate("properties"), propertiesPanel);

        rightTabbedPane.addTab(EasyStrings.translate("library"), libraryPanel);

        JPanel transformTab = new JPanel(new BorderLayout());
        transformPanel = new TransformPanel(stagePanel, false);
        transformTab.add(new FasterScrollPane(transformPanel), BorderLayout.CENTER);

        rightTabbedPane.addTab(EasyStrings.translate("transform"), transformTab);
        rightTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (editingShape != null) {
                    updateShapeSelectionTransform();
                    return;
                }
                if (stagePanel.getTimelined() == null) {
                    return;
                }
                List<Integer> depths = stagePanel.getSelectedDepths();
                if (stagePanel.getFrame() >= stagePanel.getTimelined().getFrameCount()) {
                    depths.clear();
                }
                if (!depths.isEmpty()) {
                    Frame frame = stagePanel.getTimelined().getTimeline().getFrame(stagePanel.getFrame());
                    if (frame == null) {
                        depths.clear();
                    } else {
                        for (int i = 0; i < depths.size(); i++) {
                            DepthState ds = frame.layers.get(depths.get(i));
                            if (ds == null) {
                                depths.remove(i);
                                i--;
                            }
                        }
                    }
                }
                transformPanel.setVisible(!depths.isEmpty());
                if (transformEnabled()) {
                    stagePanel.freeTransformDepths(depths);
                    stagePanel.setTransformSelectionMode(true);
                } else {
                    stagePanel.freeTransformDepth(-1);
                    stagePanel.selectDepths(depths);
                    stagePanel.setTransformSelectionMode(false);
                }
            }
        });

        horizontalSplitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplitPane, rightTabbedPane, Configuration.guiSplitPaneEasyHorizontalDividerLocationPercent);

        if (View.isOceanic()) {
            libraryScrollPane.getViewport().setBackground(Color.white);
        } else {
            libraryScrollPane.getViewport().setBackground(UIManager.getColor("Tree.background"));
        }
        add(horizontalSplitPane, BorderLayout.CENTER);

        libraryTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = libraryTreeTable.getSelectedRow();
                if (row == -1) {
                    return;
                }
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) libraryTreeTable.getModel().getValueAt(row, 0);
                Object obj = n.getUserObject();
                if (obj instanceof Tag) {
                    Tag t = (Tag) obj;
                    libraryPreviewPanel.setTimelined(TimelinedMaker.makeTimelined(t), t.getSwf(),
                            -1, false, true, true, true, true, false, true, true, true);
                    libraryPreviewPanel.zoomFit();
                } else {
                    libraryPreviewPanel.clearAll();
                }
            }
        });
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }   
    
    private void updatePropertiesPanel() {
        CardLayout cl = (CardLayout) propertiesPanel.getLayout();
        if (editingShape != null) {
            cl.show(propertiesPanel, shapeTool == ShapeTool.BRUSH ? PROPERTIES_BRUSH
                    : shapeTool == ShapeTool.PENCIL ? PROPERTIES_PENCIL
                            : shapeTool == ShapeTool.PEN ? PROPERTIES_PEN
                            : shapeTool == ShapeTool.ERASER ? PROPERTIES_ERASER
                                    : shapeTool == ShapeTool.SHAPES ? PROPERTIES_DRAWING_SHAPES
                                            : shapeTool == ShapeTool.PAINT ? PROPERTIES_PAINT
                                                    : shapeTool == ShapeTool.INK ? PROPERTIES_INK
                                                            : shapeTool == ShapeTool.EYEDROPPER ? PROPERTIES_EYEDROPPER
                                                                    : shapeTool == ShapeTool.EDIT_POINTS ? PROPERTIES_POINTS
                                                                            : shapeTool == ShapeTool.GRADIENT_TRANSFORM
                                                                                    || shapeTool == ShapeTool.ANCHOR_POINT
                                                                                    ? PROPERTIES_EMPTY : PROPERTIES_SHAPE);
            return;
        }
        List<PlaceObjectTypeTag> places = getSelectedPlaceTags();
        if (places == null || places.isEmpty()) {
            cl.show(propertiesPanel, PROPERTIES_DOCUMENT);
            return;
        }
        if (places.size() == 1 && places.get(0) != null) {
            int chid = places.get(0).getCharacterId();
            if (chid > -1) {
                if (places.get(0).getSwf().getCharacter(chid) instanceof TextTag) {
                    textInstancePropertiesPanel.update();
                    cl.show(propertiesPanel, PROPERTIES_TEXT);
                    return;
                }
            }
        }
        instancePropertiesPanel.update();
        cl.show(propertiesPanel, PROPERTIES_INSTANCE);
    }

    private boolean transformEnabled() {
        return editingShape == null && rightTabbedPane.getSelectedIndex() == 2;
    }

    private boolean shapeSelectionTool() {
        return editingShape != null && (shapeTool == ShapeTool.SELECT || shapeTool == ShapeTool.LASSO);
    }

    private void updateShapeSelectionTransform() {
        rightTabbedPane.setEnabledAt(2, editingShape == null || shapeSelectionTool());
        ShapeSelectionTransform.State current = stagePanel.getShapeSelectionTransformState();
        if (current != null) {
            shapeTransformState = current;
        }
        if (!shapeSelectionTool() || rightTabbedPane.getSelectedIndex() != 2 || selectedShapeParts.isEmpty()) {
            stagePanel.setShapeSelectionTransform(null, null);
            if (editingShape != null) {
                transformPanel.setVisible(false);
            }
            return;
        }
        Path2D.Double geometry = new Path2D.Double();
        for (ShapeSelection.Selection item : selectedShapeParts) {
            geometry.append(item.area, false);
        }
        stagePanel.setShapeSelectionTransform(geometry, this::transformShapeSelection);
        stagePanel.restoreShapeSelectionTransformState(shapeTransformState);
        transformPanel.setVisible(true);
    }

    public void setSwf(SWF swf) {
        if (editingShape != null && editingShape.getSwf() == swf) {
            return;
        }
        Timelined tim = swf;
        if (swf != null) {
            SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(swf);
            if (conf != null) {
                int timId = Integer.parseInt(conf.getCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_TIMELINE, "-1"));
                if (timId > -1) {
                    CharacterTag ct = swf.getCharacter(timId);
                    if (ct != null && ct instanceof Timelined) {
                        tim = (Timelined) ct;
                    }
                }
            }
        }
        setTimelined(tim);
    }
    
    public boolean isShapeEditing() {
        return editingShape != null;
    }

    public ShapeTool getShapeTool() {
        return shapeTool;
    }

    public void replaceShape(boolean updateBounds) {
        if (editingShape == null || Main.isWorking() || shapeLayerStates.stream().anyMatch(state -> state.locked)) {
            return;
        }
        prepareShapeTransfer();
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
        SWF swf = editingShape.getSwf();
        int layer = activeShapeLayer;
        List<ShapeLayerState> beforeStates = shapeLayerStates;
        try {
            ShapeReplaceOperation operation = new ShapeReplaceOperation(editingShape,
                    mainPanel.translate(updateBounds ? "button.replaceNoFill" : "button.replace"), (tag, forward) -> {
                        if (!forward) {
                            shapeLayerStatesByTag.put(tag, beforeStates);
                        }
                        mainPanel.refreshTree(tag.getSwf());
                        editShape(tag, context, forward ? 0 : layer);
                        if (context == null && !stagePanel.getZoom().fit) {
                            stagePanel.setViewPosition(origin);
                        }
                        libraryTreeTable.setSwf(null);
                        libraryTreeTable.setSwf(tag.getSwf());
                    });
            if (mainPanel.replace(Collections.singletonList(editingShape), false, !updateBounds) && operation.finishImport()) {
                undoManager.doOperation(operation, swf);
            }
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    public void exportShape() {
        if (editingShape == null || Main.isWorking()) {
            return;
        }
        prepareShapeTransfer();
        mainPanel.exportSelectionActionPerformed(Collections.singletonList(editingShape));
    }

    private void prepareShapeTransfer() {
        finishPendingShapeEdit();
        finishFloatingSelection();
    }

    private void finishPendingShapeEdit() {
        if (penTool.isPending()) {
            penTool.finish(false);
        }
        finishPointEdit(false);
    }

    public void setShapeTool(ShapeTool tool) {
        Objects.requireNonNull(tool);
        if (shapeTool != tool) {
            if (tool != ShapeTool.SELECT && tool != ShapeTool.LASSO) {
                finishFloatingSelection();
            }
            if (shapeTool == ShapeTool.PEN) {
                penTool.finish(false);
            }
            shapeTool = tool;
            eyedropperSample = null;
            eyedropperPropertiesPanel.setSample(null);
            if (drawingToolSelected() || shapeTool == ShapeTool.EDIT_POINTS) {
                drawingStyleSettings.setSwf(editingShape == null ? null : editingShape.getSwf());
                if (shapeTool == ShapeTool.BRUSH) {
                    drawingStyleSettings.activateBrush();
                }
                showShapeSelections(new ArrayList<>());
                rightTabbedPane.setSelectedIndex(0);
            }
            updateShapeTool();
            updatePropertiesPanel();
            mainPanel.updateUiWithCurrentOpenable();
        }
    }

    private boolean drawingToolSelected() {
        return shapeTool == ShapeTool.BRUSH || shapeTool == ShapeTool.PENCIL || shapeTool == ShapeTool.PEN || shapeTool == ShapeTool.ERASER
                || shapeTool == ShapeTool.SHAPES || shapeTool == ShapeTool.ANCHOR_POINT || clickToolSelected();
    }

    private boolean styleToolSelected() {
        return shapeTool == ShapeTool.PAINT || shapeTool == ShapeTool.INK;
    }

    private boolean clickToolSelected() {
        return styleToolSelected() || shapeTool == ShapeTool.EYEDROPPER || shapeTool == ShapeTool.GRADIENT_TRANSFORM;
    }

    private void selectFillTransform(Point2D point) {
        ShapeSelection.RectangleSelection selection = shapeModel(activeShapeLayer).selectFillTransform(point, stagePanel.getTimelinedToPanelTransform());
        shapeModels.put(activeShapeLayer, selection.model);
        showShapeSelections(!selection.selected.isEmpty() && ShapeFillTransform.canTransform(selection.selected.get(0))
                ? selection.selected : Collections.emptyList());
    }

    private void updateFillTransformHandles() {
        if (shapeTool != ShapeTool.GRADIENT_TRANSFORM) {
            return;
        }
        if (editingShape == null || selectedShapeParts.isEmpty()
                || !ShapeFillTransform.canTransform(selectedShapeParts.get(0))) {
            stagePanel.clearGradientTransform();
            return;
        }
        FILLSTYLE fill = ShapeFillTransform.fillOf(selectedShapeParts.get(0));
        if (fill.hasGradient()) {
            if (fill.gradient instanceof FOCALGRADIENT) {
                stagePanel.setFocalGradientTransform(fill.gradientMatrix, ((FOCALGRADIENT) fill.gradient).focalPoint,
                        matrix -> previewFillTransform(fillTransformSession.setMatrix(matrix)),
                        focal -> previewFillTransform(fillTransformSession.setFocalPoint(focal)));
            } else {
                stagePanel.setGradientTransform(fill.gradientMatrix, fill.fillStyleType == FILLSTYLE.LINEAR_GRADIENT,
                        matrix -> previewFillTransform(fillTransformSession.setMatrix(matrix)));
            }
        } else {
            ImageTag image = editingShape.getSwf().getImage(fill.bitmapId);
            Dimension size = image == null ? null : image.getImageDimension();
            if (size == null || size.width <= 0 || size.height <= 0) {
                stagePanel.clearGradientTransform();
            } else {
                stagePanel.setBitmapTransform(fill.bitmapMatrix, size.width, size.height,
                        matrix -> previewFillTransform(fillTransformSession.setMatrix(matrix)));
            }
        }
    }

    private void beginFillTransform() {
        fillTransformSession = new ShapeFillTransform(shapeModel(activeShapeLayer), selectedShapeParts);
        fillTransformEdit = null;
    }

    private void previewFillTransform(ShapeSelection.BatchEdit edit) {
        fillTransformEdit = edit;
        applyDrawing(edit, EasyStrings.translate("action.transformFill"), false);
    }

    private void finishFillTransform(boolean canceled) {
        ShapeSelection.BatchEdit edit = fillTransformEdit;
        fillTransformSession = null;
        fillTransformEdit = null;
        clearShapePreview();
        if (!canceled && edit != null && edit.changed) {
            applyShapeEdit(edit, EasyStrings.translate("action.transformFill"));
        } else if (shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
            updateFillTransformHandles();
        }
    }

    private ShapeSelection.Selection styleAt(Point2D point) {
        if (point == null || editingShape == null) {
            return null;
        }
        double minimum = Configuration.useMinimumStrokeWidth1Px.get() ? SWF.unitDivisor / stagePanel.getZoom().value : 1;
        return shapeModel(activeShapeLayer).sampleStyle(point, stagePanel.getParentMatrix().toTransform(), minimum);
    }

    private void previewStyleAt(Point2D point) {
        ShapeSelection.Selection sample = styleAt(point);
        if (sample == null ? eyedropperSample == null : eyedropperSample != null
                && sample.stroke == eyedropperSample.stroke && Objects.equals(sample.fill, eyedropperSample.fill)
                && Objects.equals(sample.line, eyedropperSample.line)) {
            return;
        }
        eyedropperSample = sample;
        eyedropperPropertiesPanel.setSample(sample);
    }

    private void sampleStyleAt(Point2D point) {
        ShapeSelection.Selection sample = styleAt(point);
        if (sample == null) {
            return;
        }
        if (sample.stroke) {
            drawingStyleSettings.setSampledStroke(sample.line);
            setShapeTool(ShapeTool.INK);
        } else {
            drawingStyleSettings.setSampledFill(sample.fill);
            setShapeTool(ShapeTool.PAINT);
        }
    }

    private void applyStyleAt(Point2D point) {
        ShapeSelection.BatchEdit edit;
        if (shapeTool == ShapeTool.PAINT) {
            ShapeSelection.Selection region = shapeModel(activeShapeLayer).fillRegionAt(point);
            if (region == null) {
                return;
            }
            edit = shapeModel(activeShapeLayer).setRegionFill(region, paintPropertiesPanel.getFillStyle(region.geometry.getBounds2D()));
        } else {
            ShapeSelection.Selection region = shapeModel(activeShapeLayer).fillRegionAt(point);
            if (region == null) {
                return;
            }
            LINESTYLE2 line = inkPropertiesPanel.getStrokeStyle(region.geometry.getBounds2D());
            edit = shapeModel(activeShapeLayer).outlineRegion(region, line);
        }
        if (edit.changed) {
            applyShapeEdit(edit, EasyStrings.translate(shapeTool == ShapeTool.PAINT ? "action.paintFill" : "action.inkStroke"));
        }
    }

    private BrushPropertiesPanel activeBrushProperties() {
        return shapeTool == ShapeTool.ERASER ? eraserPropertiesPanel : brushPropertiesPanel;
    }

    private Shape drawingCursor(Point2D point) {
        if (shapeTool == ShapeTool.SHAPES) {
            return ShapeDrawingGesture.cursor(point, stagePanel.getParentMatrix().toTransform(), 4 * SWF.unitDivisor / stagePanel.getZoom().value);
        }
        if (shapeTool == ShapeTool.PENCIL) {
            LINESTYLE2 line = pencilPropertiesPanel.getStrokeStyle();
            double minimum = Configuration.useMinimumStrokeWidth1Px.get() ? SWF.unitDivisor / stagePanel.getZoom().value : 1;
            return PencilStroke.cursor(point, line, stagePanel.getParentMatrix().toTransform(), minimum);
        }
        return activeBrushProperties().getBrushShape().tip(point, activeBrushProperties().getBrushSize() * SWF.unitDivisor,
                stagePanel.getParentMatrix().toTransform());
    }

    private void updateDrawingSettings() {
        if (shapeTool == ShapeTool.PEN && penTool.isPending()) {
            penTool.refresh();
        } else {
            updateShapeTool();
        }
    }

    private void updateShapeTool() {
        unfilteredShapePreview = null;
        boolean editable = shapeLayerEditable();
        boolean anchorEditable = editable && shapeTool == ShapeTool.ANCHOR_POINT
                && shapeLayerStates.stream().noneMatch(state -> state.hidden || state.locked);
        stagePanel.setShapeOriginVisible(anchorEditable);
        if (editable && shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
            stagePanel.setGradientTransformGestureListeners(this::beginFillTransform, this::finishFillTransform);
        } else {
            stagePanel.clearGradientTransform();
            stagePanel.setGradientTransformGestureListeners(null, null);
        }
        boolean selecting = shapeSelectionTool();
        stagePanel.setAreaSelectionListener(selecting ? this::selectShapeArea : null, shapeTool == ShapeTool.LASSO);
        stagePanel.setSelectionMoveListeners(!selecting ? null
                : this::shapeSelectionContains,
                !selecting ? null : this::moveShapeSelection);
        stagePanel.setSelectionDeleteListener(!selecting ? null : this::deleteShapeSelection);
        boolean drawing = editable && (shapeTool == ShapeTool.ANCHOR_POINT ? anchorEditable : drawingToolSelected());
        stagePanel.setShapeDrawingTool(!drawing || clickToolSelected() || shapeTool == ShapeTool.ANCHOR_POINT || shapeTool == ShapeTool.PEN
                ? null : this::drawingCursor,
                !drawing ? null : shapeTool == ShapeTool.PEN ? penTool : new ImagePanel.ShapeDrawListener() {
                    @Override
                    public void begin(Point2D point) {
                        if (shapeTool == ShapeTool.ANCHOR_POINT) {
                            try {
                                anchorMove = new ShapeAnchorMove(editingShape, point);
                            } catch (IOException | InterruptedException ex) {
                                logShapeEditFailure(ex);
                            }
                        } else if (clickToolSelected()) {
                            styleClickStart = stagePanel.getTimelinedToPanelTransform().transform(point, null);
                            styleClickDragged = false;
                        } else if (shapeTool == ShapeTool.SHAPES) {
                            if (shapeDrawing == null || !shapeDrawing.isAwaitingOuterRadius()) {
                                shapeDrawing = new ShapeDrawingGesture(drawingShapesPropertiesPanel.getKind(), point,
                                        stagePanel.getParentMatrix().toTransform(), drawingShapesPropertiesPanel.getSides(),
                                        drawingShapesPropertiesPanel.getAngles(), drawingShapesPropertiesPanel.getCornerRadius());
                            }
                            paintPrimitive(point, false);
                        } else if (shapeTool == ShapeTool.PENCIL) {
                            pencilStroke = new PencilStroke(point);
                            paintPencil(point, false);
                        } else {
                            brushStroke = new BrushStroke(activeBrushProperties().getBrushShape(),
                                    activeBrushProperties().getBrushSize() * SWF.unitDivisor, point, stagePanel.getParentMatrix().toTransform());
                            paintBrush(point, false);
                        }
                    }

                    @Override
                    public void update(Point2D point, boolean finished) {
                        if (shapeTool == ShapeTool.ANCHOR_POINT) {
                            moveWholeShape(point, finished);
                        } else if (clickToolSelected()) {
                            if (shapeTool == ShapeTool.EYEDROPPER) {
                                previewStyleAt(point);
                            }
                            Point2D screen = stagePanel.getTimelinedToPanelTransform().transform(point, null);
                            styleClickDragged |= styleClickStart != null && styleClickStart.distance(screen) >= 3;
                            if (finished && styleClickStart != null && !styleClickDragged) {
                                if (shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
                                    selectFillTransform(point);
                                } else if (shapeTool == ShapeTool.EYEDROPPER) {
                                    sampleStyleAt(point);
                                } else {
                                    applyStyleAt(point);
                                }
                            }
                            if (finished) {
                                styleClickStart = null;
                            }
                        } else if (shapeTool == ShapeTool.SHAPES) {
                            paintPrimitive(point, finished);
                        } else if (shapeTool == ShapeTool.PENCIL) {
                            paintPencil(point, finished);
                        } else {
                            paintBrush(point, finished);
                        }
                    }

                    @Override
                    public void cancel() {
                        anchorMove = null;
                        brushStroke = null;
                        pencilStroke = null;
                        shapeDrawing = null;
                        styleClickStart = null;
                        drawingShapesPropertiesPanel.setOuterRadiusPending(false);
                        stagePanel.setShapeDrawingGuide(null);
                        clearShapePreview();
                    }

                    @Override
                    public void move(Point2D point) {
                        if (shapeTool == ShapeTool.EYEDROPPER) {
                            previewStyleAt(point);
                        } else if (shapeTool == ShapeTool.SHAPES && isPending()) {
                            paintPrimitive(point, false);
                        }
                    }

                    @Override
                    public void exit() {
                        if (shapeTool == ShapeTool.EYEDROPPER) {
                            previewStyleAt(null);
                        }
                    }

                    @Override
                    public boolean isPending() {
                        return shapeDrawing != null && shapeDrawing.isAwaitingOuterRadius();
                    }
                }, shapeTool == ShapeTool.PEN ? ImagePanel.ShapeDrawingPointer.PEN
                        : shapeTool == ShapeTool.ANCHOR_POINT ? ImagePanel.ShapeDrawingPointer.MOVE
                        : shapeTool == ShapeTool.PAINT ? ImagePanel.ShapeDrawingPointer.PAINT
                        : shapeTool == ShapeTool.INK ? ImagePanel.ShapeDrawingPointer.INK
                                : shapeTool == ShapeTool.EYEDROPPER ? ImagePanel.ShapeDrawingPointer.EYEDROPPER : ImagePanel.ShapeDrawingPointer.DEFAULT);
        updateFillTransformHandles();
        if (editable && shapeTool == ShapeTool.EDIT_POINTS) {
            editingPoints = true;
            stagePanel.setPointEditGestureListeners(this::beginPointEdit, this::finishPointEdit);
            refreshShapePoints(Collections.emptyList());
        } else if (editingPoints) {
            stagePanel.setPointEditGestureListeners(null, null);
            stagePanel.setHilightedPoints(null);
            editingPoints = false;
        }
        updateShapeSelectionTransform();
    }

    private void beginPointEdit() {
        if (!shapeLayerEditable() || shapeTool != ShapeTool.EDIT_POINTS) {
            return;
        }
        try {
            pointEdit = new ShapePointEdit(editingShape, shapeLayers.get(activeShapeLayer));
            pointEditDragging = true;
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private void refreshShapePoints(List<Integer> selected) {
        if (!shapeLayerEditable() || shapeTool != ShapeTool.EDIT_POINTS) {
            return;
        }
        try {
            ShapePointEdit edit = new ShapePointEdit(editingShape, shapeLayers.get(activeShapeLayer));
            stagePanel.setHilightedPoints(edit.getPoints(), edit.getPathStarts());
            stagePanel.setSelectedPointIndices(selected);
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private void finishPointEdit(boolean canceled) {
        ShapePointEdit edit = pointEdit;
        pointEdit = null;
        pointEditDragging = false;
        List<Integer> selected = stagePanel.getSelectedPointIndices();
        clearShapePreview();
        if (canceled || edit == null || !edit.isChanged()) {
            refreshShapePoints(selected);
            return;
        }
        try {
            applyShapeTagEdit(createShapeWithEditedLayer(edit.getShape()),
                    EasyStrings.translate("action.editShapePoints"), selected, activeShapeLayer);
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
            refreshShapePoints(selected);
        }
    }

    private ShapeTag createShapeWithEditedLayer(ShapeTag editedLayer) throws IOException, InterruptedException {
        if (shapeLayers.size() == 1) {
            return editedLayer;
        }
        List<SHAPEWITHSTYLE> layers = new ArrayList<>(shapeLayers);
        layers.set(activeShapeLayer, editedLayer.getShapes());
        return ShapeStyleChangeOperation.createShape(editingShape, layers);
    }

    private void moveWholeShape(Point2D point, boolean finished) {
        if (anchorMove == null) {
            return;
        }
        try {
            ShapeTag moved = anchorMove.moveTo(point);
            if (finished) {
                anchorMove = null;
                clearShapePreview();
                if (moved != null) {
                    applyShapeTagEdit(moved, EasyStrings.translate("action.move"), Collections.emptyList(), activeShapeLayer);
                }
            } else {
                previewShape(moved);
            }
        } catch (IOException | InterruptedException ex) {
            anchorMove = null;
            clearShapePreview();
            logShapeEditFailure(ex);
        }
    }

    private void applyShapeTagEdit(ShapeTag edited, String description, List<Integer> selected, int afterLayer) {
        applyShapeTagEdit(edited, description, selected, afterLayer, shapeLayerStates);
    }

    private void applyShapeTagEdit(ShapeTag edited, String description, List<Integer> selected, int afterLayer,
            List<ShapeLayerState> afterStates) {
        applyShapeTagEdit(edited, description, selected, afterLayer, afterStates, null);
    }

    private void applyShapeTagEdit(ShapeTag edited, String description, List<Integer> selected, int afterLayer,
            List<ShapeLayerState> afterStates, Consumer<Boolean> restoreSelection) {
        List<ShapeLayerState> beforeStates = shapeLayerStates;
        int beforeLayer = activeShapeLayer;
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        try {
            undoManager.doOperation(new ShapeStyleChangeOperation(editingShape, edited, description, (tag, forward) -> {
                int activeTab = rightTabbedPane.getSelectedIndex();
                Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
                boolean sameShape = editingShape != null && editingShape.getSwf() == tag.getSwf()
                        && editingShape.getCharacterId() == tag.getCharacterId();
                ImagePanel.ShapeEditContext active = stagePanel.getShapeEditContext();
                boolean preserveView = sameShape && context == null && active == null && !stagePanel.getZoom().fit;
                shapeLayerStatesByTag.put(tag, forward ? afterStates : beforeStates);
                editShape(tag, sameShape && context != null && context.sameLocation(active) ? active : context,
                        forward ? afterLayer : beforeLayer);
                if (preserveView) {
                    stagePanel.setViewPosition(origin);
                }
                refreshShapePoints(selected);
                if (restoreSelection != null) {
                    if (activeTab == 2 && rightTabbedPane.isEnabledAt(activeTab)) {
                        rightTabbedPane.setSelectedIndex(activeTab);
                    }
                    restoreSelection.accept(forward);
                }
                libraryTreeTable.setSwf(null);
                libraryTreeTable.setSwf(tag.getSwf());
                mainPanel.refreshTree(tag.getSwf());
            }), editingShape.getSwf());
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
            refreshShapePoints(selected);
        }
    }

    private void logShapeEditFailure(Exception ex) {
        Logger.getLogger(EasySwfPanel.class.getName()).log(Level.SEVERE, "Cannot edit shape", ex);
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private void paintPen(BezierPen pen, boolean finished) {
        if (editingShape == null) {
            return;
        }
        Path2D.Double path = finished ? pen.getPath() : pen.getPreviewPath();
        Rectangle2D bounds = path.getBounds2D();
        LINESTYLE2 line = penPropertiesPanel.getStrokeStyle(bounds);
        applyDrawing(shapeModel(activeShapeLayer).drawShape(path, penPropertiesPanel.getFillStyle(bounds), line),
                EasyStrings.translate("action.pen"), finished);
    }

    private void paintPrimitive(Point2D point, boolean released) {
        if (shapeDrawing == null || editingShape == null) {
            return;
        }
        shapeDrawing.update(point);
        boolean finished = released && shapeDrawing.release();
        Shape geometry = shapeDrawing.getGeometry();
        Rectangle2D bounds = geometry.getBounds2D();
        ShapeSelection.BatchEdit edit = shapeModel(activeShapeLayer).drawShape(geometry, drawingShapesPropertiesPanel.getFill(bounds),
                drawingShapesPropertiesPanel.getStroke(bounds));
        stagePanel.setShapeDrawingGuide(finished ? null : shapeDrawing.getGuide());
        drawingShapesPropertiesPanel.setOuterRadiusPending(!finished && shapeDrawing.isAwaitingOuterRadius());
        if (finished) {
            shapeDrawing = null;
        }
        applyDrawing(edit, EasyStrings.translate("action.drawShape"), finished);
    }

    private void paintBrush(Point2D point, boolean finished) {
        if (brushStroke == null || editingShape == null) {
            return;
        }
        brushStroke.add(point);
        Area area = brushStroke.getArea();
        boolean erasing = shapeTool == ShapeTool.ERASER;
        ShapeSelection.BatchEdit edit;
        if (erasing) {
            edit = shapeModel(activeShapeLayer).erase(area, stagePanel.getParentMatrix().toTransform());
        } else {
            FILLSTYLE fill = brushPropertiesPanel.getFillStyle(area.getBounds2D());
            edit = shapeModel(activeShapeLayer).paint(area, fill);
        }
        if (finished) {
            brushStroke = null;
        }
        applyDrawing(edit, EasyStrings.translate(erasing ? "action.eraser" : "action.brush"), finished);
    }

    private void paintPencil(Point2D point, boolean finished) {
        if (pencilStroke == null || editingShape == null) {
            return;
        }
        pencilStroke.add(point);
        Path2D.Double path = pencilStroke.getPath();
        LINESTYLE2 line = pencilPropertiesPanel.getStrokeStyle();
        Rectangle2D bounds = path.getBounds2D();
        double halfWidth = line == null ? 0 : line.width / 2.0;
        line = pencilPropertiesPanel.getStrokeStyle(new Rectangle2D.Double(bounds.getX() - halfWidth, bounds.getY() - halfWidth,
                bounds.getWidth() + 2 * halfWidth, bounds.getHeight() + 2 * halfWidth));
        ShapeSelection.BatchEdit edit = shapeModel(activeShapeLayer).drawStroke(path, line);
        if (finished) {
            pencilStroke = null;
        }
        applyDrawing(edit, EasyStrings.translate("action.pencil"), finished);
    }

    private void applyDrawing(ShapeSelection.BatchEdit edit, String description, boolean finished) {
        if (finished || !edit.changed) {
            clearShapePreview();
            if (finished && edit.changed) {
                applyShapeEdit(edit, description);
            }
        } else {
            try {
                previewShape(ShapeStyleChangeOperation.createShape(editingShape, shapeLayers, activeShapeLayer, edit.model));
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(EasySwfPanel.class.getName()).log(Level.SEVERE, "Cannot preview drawing", ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void editShape(ShapeTag shape) {
        editShape(shape, null, 0);
    }

    private void editShape(ShapeTag shape, ImagePanel.ShapeEditContext context, int layer) {
        if (context == null) {
            setTimelined(TimelinedMaker.makeTimelined(shape), true, shape, layer);
        } else {
            updatingTimelined = true;
            try {
                stagePanel.restoreShapeEditContext(shape, context);
                updateTimelined(stagePanel.getTimelined(), false, shape, layer);
            } finally {
                updatingTimelined = false;
            }
        }
        rightTabbedPane.setSelectedIndex(0);
    }

    void selectShapeLayerContents(int layer) {
        selectShapeLayer(layer, shapeSelectionTool());
    }

    private void selectShapeLayer(int layer) {
        selectShapeLayer(layer, false);
    }

    private void selectShapeLayer(int layer, boolean selectContents) {
        if (!selectContents && layer == activeShapeLayer && shapeSelections.isEmpty()) {
            return;
        }
        prepareShapeTransfer();
        activeShapeLayer = layer;
        shapeModels.clear();
        eyedropperSample = null;
        eyedropperPropertiesPanel.setSample(null);
        showShapeSelections(selectContents && shapeLayerEditable() ? shapeModel(layer).selectAll() : Collections.emptyList());
        updateShapeTool();
        updatePropertiesPanel();
    }

    private ShapeSelection shapeModel(int layer) {
        return shapeModels.computeIfAbsent(layer, index -> new ShapeSelection(editingShape, shapeLayers.get(index)));
    }

    private void selectShapeAt(Point2D point, boolean additive) {
        AffineTransform transform = stagePanel.getTimelinedToPanelTransform();
        if (!additive) {
            if (isFloatingSelectionAt(point, transform)) {
                return;
            }
            finishFloatingSelection();
        }
        Map<Integer, List<ShapeSelection.Selection>> selected = additive ? new LinkedHashMap<>(shapeSelections) : new LinkedHashMap<>();
        for (int layer = shapeLayers.size() - 1; layer >= 0; layer--) {
            if (!shapeLayerEditable(layer)) {
                continue;
            }
            ShapeSelection.Selection hit = shapeModel(layer).select(point, 3, transform);
            if (hit == null) {
                continue;
            }
            List<ShapeSelection.Selection> next = ShapeSelection.updateSelection(selected.getOrDefault(layer, Collections.emptyList()), hit, additive);
            if (next.isEmpty()) {
                selected.remove(layer);
            } else {
                selected.put(layer, next);
                activeShapeLayer = layer;
            }
            break;
        }
        if (selected.isEmpty()) {
            finishFloatingSelection();
        }
        showStageSelection(selected);
    }

    private boolean isFloatingSelectionAt(Point2D point, AffineTransform transform) {
        if (shapeModels.values().stream().noneMatch(ShapeSelection::hasFloatingSelection) || !shapeSelectionContains(point)) {
            return false;
        }
        for (int layer = shapeLayers.size() - 1; layer >= 0; layer--) {
            if (shapeLayerEditable(layer) && shapeModel(layer).select(point, 3, transform) != null) {
                return shapeModel(layer).selectionContains(shapeSelections.getOrDefault(layer, Collections.emptyList()), point, 3, transform);
            }
        }
        return false;
    }

    private void selectShapeArea(Shape area, boolean additive) {
        if (!additive) {
            finishFloatingSelection();
        }
        Map<Integer, List<ShapeSelection.Selection>> selected = new LinkedHashMap<>();
        for (int layer = 0; layer < shapeLayers.size(); layer++) {
            if (!shapeLayerEditable(layer)) {
                continue;
            }
            ShapeSelection.RectangleSelection result = shapeModel(layer).selectArea(area,
                    shapeSelections.getOrDefault(layer, Collections.emptyList()), additive);
            if (!result.selected.isEmpty()) {
                shapeModels.put(layer, result.model);
                selected.put(layer, result.selected);
            }
        }
        showStageSelection(selected);
    }

    private void showStageSelection(Map<Integer, List<ShapeSelection.Selection>> selected) {
        showLayerSelections(selected);
        if (rightTabbedPane.getSelectedIndex() != 2) {
            rightTabbedPane.setSelectedIndex(0);
        }
        updatePropertiesPanel();
    }

    private void addShapeLayer(int layer) {
        prepareShapeTransfer();
        List<SHAPEWITHSTYLE> layers = new ArrayList<>(shapeLayers);
        int inserted = layer + 1;
        layers.add(inserted, SHAPEWITHSTYLE.createEmpty(editingShape.getShapeNum()));
        List<ShapeLayerState> states = new ArrayList<>(shapeLayerStates);
        states.add(inserted, new ShapeLayerState());
        applyShapeLayers(layers, states, inserted, "timeline.shape.addLayer");
    }

    private void removeShapeLayer(int layer) {
        if (shapeLayers.size() <= 1 || isShapeLayerLocked(layer)) {
            return;
        }
        prepareShapeTransfer();
        List<SHAPEWITHSTYLE> layers = new ArrayList<>(shapeLayers);
        layers.remove(layer);
        List<ShapeLayerState> states = new ArrayList<>(shapeLayerStates);
        states.remove(layer);
        // Keep the active layer if it survives. Otherwise prefer the layer below.
        int next = activeShapeLayer < layer ? activeShapeLayer : Math.max(0, activeShapeLayer - 1);
        applyShapeLayers(layers, states, next, "timeline.shape.removeLayer");
    }

    private void moveShapeLayer(int source, int destination) {
        if (source == destination) {
            return;
        }
        finishPendingShapeEdit();
        List<SHAPEWITHSTYLE> layers = new ArrayList<>(shapeLayers);
        layers.add(destination, layers.remove(source));
        List<ShapeLayerState> states = new ArrayList<>(shapeLayerStates);
        states.add(destination, states.remove(source));
        int nextLayer = movedShapeLayerIndex(activeShapeLayer, source, destination);

        Map<Integer, List<ShapeSelection.Selection>> previous = shapeSelections;
        Map<Integer, ShapeSelection> previousModels = new HashMap<>();
        shapeModels.forEach((index, model) -> {
            if (previous.containsKey(index) || model.hasFloatingSelection()) {
                previousModels.put(index, model);
            }
        });
        ShapeSelectionTransform.State transformState = stagePanel.getShapeSelectionTransformState();
        try {
            ShapeTag edited = ShapeStyleChangeOperation.createShape(editingShape, layers);
            applyShapeTagEdit(edited, EasyStrings.translate("timeline.shape.moveLayer"),
                    stagePanel.getSelectedPointIndices(), nextLayer, states, forward -> {
                        Map<Integer, List<ShapeSelection.Selection>> selection = new LinkedHashMap<>();
                        previousModels.forEach((index, model) -> {
                            int moved = forward ? movedShapeLayerIndex(index, source, destination) : index;
                            if (shapeLayerEditable(moved)) {
                                shapeModels.put(moved, model);
                            }
                        });
                        previous.forEach((index, parts) -> selection.put(forward ? movedShapeLayerIndex(index, source, destination) : index, parts));
                        if (!selection.isEmpty()) {
                            showLayerSelections(selection);
                            if (!selectedShapeParts.isEmpty()) {
                                shapeTransformState = transformState;
                                stagePanel.restoreShapeSelectionTransformState(transformState);
                            }
                        }
                    });
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private static int movedShapeLayerIndex(int index, int source, int destination) {
        if (index == source) {
            return destination;
        }
        if (source < index && index <= destination) {
            return index - 1;
        }
        if (destination <= index && index < source) {
            return index + 1;
        }
        return index;
    }

    private void applyShapeLayers(List<SHAPEWITHSTYLE> layers, List<ShapeLayerState> states, int activeLayer, String action) {
        try {
            ShapeTag edited = ShapeStyleChangeOperation.createShape(editingShape, layers);
            applyShapeTagEdit(edited, EasyStrings.translate(action), Collections.emptyList(), activeLayer, states);
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private void showShapeSelections(List<ShapeSelection.Selection> selected) {
        showLayerSelections(Collections.singletonMap(activeShapeLayer, selected));
    }

    private void showLayerSelections(Map<Integer, List<ShapeSelection.Selection>> selected) {
        shapeSelections = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<ShapeSelection.Selection>> entry : selected.entrySet()) {
            if (!entry.getValue().isEmpty() && shapeLayerEditable(entry.getKey())) {
                shapeSelections.put(entry.getKey(), entry.getValue());
            }
        }
        if (!shapeSelections.isEmpty() && !shapeSelections.containsKey(activeShapeLayer)) {
            activeShapeLayer = Collections.max(shapeSelections.keySet());
        }
        shapeTimelinePanel.setSelection(activeShapeLayer, shapeSelections.keySet());
        stagePanel.setShapeSelectionTransform(null, null);
        shapeTransformState = null;
        selectedShapeParts = highlightShapeSelections(this::shapeModel, shapeSelections);
        if (shapeTool == ShapeTool.EDIT_POINTS) {
            refreshShapePoints(Collections.emptyList());
            return;
        }
        shapePropertiesPanel.setSelections(selectedShapeParts);
        updateFillTransformHandles();
        updateShapeSelectionTransform();
    }

    private List<ShapeSelection.Selection> highlightShapeSelections(IntFunction<ShapeSelection> models,
            Map<Integer, List<ShapeSelection.Selection>> selected) {
        List<ShapeSelection.Selection> displayed = new ArrayList<>();
        List<java.awt.Shape> fills = new ArrayList<>();
        List<ImagePanel.LineHilight> lines = new ArrayList<>();
        for (Map.Entry<Integer, List<ShapeSelection.Selection>> entry : selected.entrySet()) {
            ShapeSelection model = models.apply(entry.getKey());
            for (ShapeSelection.Selection item : entry.getValue()) {
                ShapeSelection.Selection current = model.displaySelection(item);
                displayed.add(current);
                if (shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
                    continue;
                }
                if (current.stroke && current.line != null) {
                    lines.add(new ImagePanel.LineHilight(current.area, current.linePath, current.line));
                } else if (!current.stroke) {
                    fills.add(current.area);
                }
            }
        }
        stagePanel.setHilightedShapes(fills, lines);
        return displayed;
    }

    private boolean shapeSelectionContains(Point2D point) {
        AffineTransform transform = stagePanel.getTimelinedToPanelTransform();
        if (hitTestSelection != selectedShapeParts || !transform.equals(hitTestTransform)) {
            hitTestSelection = selectedShapeParts;
            hitTestTransform = new AffineTransform(transform);
            selectionHitAreas.clear();
            BasicStroke tolerance = new BasicStroke(6);
            for (ShapeSelection.Selection part : selectedShapeParts) {
                // These are already clipped by highlightShapeSelections; do not subtract
                // all covering curves again on every pointer or cursor refresh.
                Shape displayed = transform.createTransformedShape(part.area);
                selectionHitAreas.add(displayed);
                if (part.stroke && part.line != null) {
                    selectionHitAreas.add(tolerance.createStrokedShape(displayed));
                }
            }
        }
        Point2D displayed = transform.transform(point, null);
        for (Shape area : selectionHitAreas) {
            if (area.contains(displayed)) {
                return true;
            }
        }
        return false;
    }

    private void moveShapeSelection(Point2D delta, boolean commit) {
        Map<Integer, ShapeSelection.BatchEdit> edits = editShapeSelections(
                (model, selected) -> model.moveFloatingSelection(selected, delta.getX(), delta.getY()));
        if (!commit || edits.isEmpty()) {
            previewShapeSelections(edits);
            return;
        }
        clearShapePreview();
        ShapeSelectionTransform.State before = shapeTransformState;
        applyShapeEdits(edits, EasyStrings.translate("action.move"), before,
                before == null ? null : before.transformed(Matrix.getTranslateInstance(Math.rint(delta.getX()), Math.rint(delta.getY()))));
    }

    private void finishFloatingSelection() {
        finishFloatingSelection(-1);
    }

    private void finishFloatingSelection(int layer) {
        Map<Integer, ShapeSelection.BatchEdit> edits = new LinkedHashMap<>();
        for (Map.Entry<Integer, ShapeSelection> entry : shapeModels.entrySet()) {
            int index = entry.getKey();
            if ((layer < 0 || layer == index) && shapeLayerEditable(index) && entry.getValue().hasFloatingSelection()) {
                edits.put(index, entry.getValue().mergeFloatingSelection());
            }
        }
        applyShapeEdits(edits, EasyStrings.translate("action.move"));
    }

    private void transformShapeSelection(Matrix matrix, boolean commit) {
        if (!shapeSelectionTool() || shapeSelections.isEmpty()) {
            return;
        }
        Map<Integer, ShapeSelection.BatchEdit> edits = editShapeSelections(
                (model, selected) -> model.transformFloatingSelection(selected, matrix.toTransform()));
        if (commit && !edits.isEmpty()) {
            clearShapePreview();
            ShapeSelectionTransform.State before = stagePanel.getShapeSelectionTransformState();
            applyShapeEdits(edits, EasyStrings.translate("action.transform"), before, before == null ? null : before.transformed(matrix));
        } else {
            previewShapeSelections(edits);
        }
    }

    void deleteShapeSelection() {
        if (!shapeSelectionTool() || shapeSelections.isEmpty()) {
            return;
        }
        clearShapePreview();
        applyShapeEdits(editShapeSelections(ShapeSelection::deleteAll), EasyStrings.translate("action.deleteShapeSelection"));
    }

    private void changeShapeStyle(boolean stroke, String property) {
        if (selectedShapeParts.stream().noneMatch(item -> item.stroke == stroke)) {
            return;
        }
        if (!shapePropertiesPanel.isPropertyChangeValid(stroke, property)) {
            showLayerSelections(shapeSelections);
            return;
        }
        Map<Integer, ShapeSelection.BatchEdit> edits = editShapeSelections((model, selected) -> model.changeAll(selected,
                stroke ? null : item -> shapePropertiesPanel.applyFillChange(item.fill, item.geometry.getBounds2D(), property),
                stroke ? item -> shapePropertiesPanel.applyStrokeChange(item.line, item.area.getBounds2D(), property) : null));
        String description = EasyStrings.translate("action.change").replace("%item%",
                EasyStrings.translate("properties.shape.header." + (stroke ? "stroke" : "fill")));
        if (edits.isEmpty()) {
            showLayerSelections(shapeSelections);
        }
        applyShapeEdits(edits, description);
    }

    private Map<Integer, ShapeSelection.BatchEdit> editShapeSelections(
            BiFunction<ShapeSelection, List<ShapeSelection.Selection>, ShapeSelection.BatchEdit> operation) {
        Map<Integer, ShapeSelection.BatchEdit> edits = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<ShapeSelection.Selection>> entry : shapeSelections.entrySet()) {
            ShapeSelection.BatchEdit edit = operation.apply(shapeModel(entry.getKey()), entry.getValue());
            if (edit.changed) {
                edits.put(entry.getKey(), edit);
            }
        }
        return edits;
    }

    private ShapeTag createEditedShape(Map<Integer, ShapeSelection.BatchEdit> edits) throws IOException, InterruptedException {
        Map<Integer, ShapeSelection> models = new HashMap<>();
        edits.forEach((layer, edit) -> models.put(layer, edit.model));
        return ShapeStyleChangeOperation.createShape(editingShape, shapeLayers, models);
    }

    private Map<Integer, List<ShapeSelection.Selection>> selectionsAfter(Map<Integer, ShapeSelection.BatchEdit> edits) {
        Map<Integer, List<ShapeSelection.Selection>> selected = new LinkedHashMap<>(shapeSelections);
        edits.forEach((layer, edit) -> selected.put(layer, edit.selected));
        return selected;
    }

    private void previewShapeSelections(Map<Integer, ShapeSelection.BatchEdit> edits) {
        if (!edits.isEmpty()) {
            try {
                previewShape(createEditedShape(edits));
                highlightShapeSelections(layer -> edits.containsKey(layer) ? edits.get(layer).model : shapeModel(layer), selectionsAfter(edits));
                return;
            } catch (IOException | InterruptedException ex) {
                logShapeEditFailure(ex);
            }
        }
        clearShapePreview();
        highlightShapeSelections(this::shapeModel, shapeSelections);
    }

    private void applyShapeEdit(ShapeSelection.BatchEdit edit, String description) {
        if (shapeLayerEditable()) {
            applyShapeEdits(Collections.singletonMap(activeShapeLayer, edit), description);
        }
    }

    private void applyShapeEdits(Map<Integer, ShapeSelection.BatchEdit> edits, String description) {
        ShapeSelectionTransform.State state = stagePanel.getShapeSelectionTransformState();
        if (state == null) {
            state = shapeTransformState;
        }
        applyShapeEdits(edits, description, state, state);
    }

    private void applyShapeEdits(Map<Integer, ShapeSelection.BatchEdit> edits, String description,
            ShapeSelectionTransform.State beforeTransform, ShapeSelectionTransform.State afterTransform) {
        if (edits.isEmpty()) {
            return;
        }
        List<ShapeLayerState> states = shapeLayerStates;
        Map<Integer, List<ShapeSelection.Selection>> previous = shapeSelections;
        Map<Integer, List<ShapeSelection.Selection>> next = selectionsAfter(edits);
        Map<Integer, ShapeSelection> beforeModels = new HashMap<>();
        shapeModels.forEach((index, model) -> {
            if (previous.containsKey(index) || edits.containsKey(index) || model.hasFloatingSelection()) {
                beforeModels.put(index, model);
            }
        });
        int layer = activeShapeLayer;
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        try {
            ShapeTag edited = createEditedShape(edits);
            undoManager.doOperation(new ShapeStyleChangeOperation(editingShape, edited, description, (tag, forward) -> {
                int activeTab = rightTabbedPane.getSelectedIndex();
                Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
                boolean sameShape = editingShape != null && editingShape.getSwf() == tag.getSwf()
                        && editingShape.getCharacterId() == tag.getCharacterId();
                ImagePanel.ShapeEditContext activeContext = stagePanel.getShapeEditContext();
                boolean preserveView = sameShape && context == null && activeContext == null && !stagePanel.getZoom().fit;
                shapeLayerStatesByTag.put(tag, states);
                editShape(tag, sameShape && context != null && context.sameLocation(activeContext) ? activeContext : context, layer);
                beforeModels.forEach((index, model) -> {
                    if (shapeLayerEditable(index)) {
                        ShapeSelection.BatchEdit edit = edits.get(index);
                        shapeModels.put(index, forward && edit != null ? edit.committedModel(tag, shapeLayers.get(index)) : model);
                    }
                });
                if (preserveView) {
                    stagePanel.setViewPosition(origin);
                }
                showLayerSelections(forward ? next : previous);
                if (activeTab == 2 && rightTabbedPane.isEnabledAt(activeTab)) {
                    rightTabbedPane.setSelectedIndex(activeTab);
                }
                if (!selectedShapeParts.isEmpty()) {
                    shapeTransformState = forward ? afterTransform : beforeTransform;
                    stagePanel.restoreShapeSelectionTransformState(shapeTransformState);
                }
                libraryTreeTable.setSwf(null);
                libraryTreeTable.setSwf(tag.getSwf());
                mainPanel.refreshTree(tag.getSwf());
            }), editingShape.getSwf());
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
            showLayerSelections(previous);
        }
    }

    public void setTimelined(Timelined timelined) {
        setTimelined(timelined, true);
    }

    private void setTimelined(Timelined timelined, boolean updateStage) {
        setTimelined(timelined, updateStage, null);
    }

    private void setTimelined(Timelined timelined, boolean updateStage, ShapeTag shape) {
        setTimelined(timelined, updateStage, shape, 0);
    }

    private void setTimelined(Timelined timelined, boolean updateStage, ShapeTag shape, int layer) {
        if (this.timelined == timelined) {
            return;
        }
        if (editingShape != null && (shape == null || shape.getSwf() != editingShape.getSwf()
                || shape.getCharacterId() != editingShape.getCharacterId())) {
            finishFloatingSelection();
        }
        if (mainPanel.getCurrentView() != MainPanel.VIEW_EASY) {
            timelined = null;
            shape = null;
        }
        updatingTimelined = true;
        try {
            updateTimelined(timelined, updateStage, shape, layer);
        } finally {
            updatingTimelined = false;
        }
    }

    private void updateTimelined(Timelined timelined, boolean updateStage, ShapeTag shape, int layer) {
        if (shape == null || editingShape == null || shape.getSwf() != editingShape.getSwf()
                || shape.getCharacterId() != editingShape.getCharacterId()) {
            shapeTool = ShapeTool.SELECT;
        }
        editingShape = shape;
        shapeLayers = shape == null ? Collections.emptyList() : ShapeLayers.split(shape.getShapes(), shape.getShapeNum());
        shapeLayerStates = shape == null ? Collections.emptyList() : shapeLayerStatesByTag.compute(shape, (tag, states) -> {
            if (states != null && states.size() == shapeLayers.size()) {
                return states;
            }
            List<ShapeLayerState> newStates = new ArrayList<>();
            for (int i = 0; i < shapeLayers.size(); i++) {
                newStates.add(new ShapeLayerState());
            }
            return newStates;
        });
        activeShapeLayer = shape == null ? 0 : Math.min(layer, shapeLayers.size() - 1);
        shapeModels.clear();
        shapeSelections = Collections.emptyMap();
        selectedShapeParts = new ArrayList<>();
        shapeTransformState = null;
        stagePanel.setShapeSelectionTransform(null, null);
        this.timelined = timelined;
        stagePanel.setSelectionMode(editingShape == null);
        stagePanel.setMultiSelect(editingShape == null);
        stagePanel.setAltSelectionEnabled(editingShape == null);
        updateShapeTool();
        stagePanel.setTransformSelectionMode(transformEnabled());
        ((CardLayout) timelineArea.getLayout()).show(timelineArea, editingShape == null ? "timeline" : "shape");
        shapeTimelinePanel.setLayers(shapeLayers, activeShapeLayer);
        rightTabbedPane.setEnabledAt(2, editingShape == null || shapeSelectionTool());
        transformPanel.setVisible(false);
        shapePropertiesPanel.setShape(editingShape);
        eyedropperSample = null;
        eyedropperPropertiesPanel.setShape(editingShape);
        drawingStyleSettings.setSwf(editingShape == null ? null : editingShape.getSwf());
        if (timelined == null) {
            stagePanel.clearAll();
            timelinePanel.setTimelined(null);
            libraryTreeTable.setSwf(null);
            libraryPreviewPanel.clearAll();
            closeTimelineButton.setVisible(false);
            timelineLabel.setText("");
            documentPropertiesPanel.setSwf(null);
            instancePropertiesPanel.update();
            textInstancePropertiesPanel.update();
        } else {
            SWF swf = timelined.getSwf();            
            documentPropertiesPanel.setSwf(swf);
            libraryTreeTable.setSwf(swf);
            libraryPreviewPanel.clearAll();
            if (timelined instanceof SWF) {
                timelineLabel.setText(EasyStrings.translate("timeline.main"));
                closeTimelineButton.setVisible(false);
            } else {
                EasyTagNameResolver nameResolver = new EasyTagNameResolver();
                timelineLabel.setText(EasyStrings.translate(editingShape == null ? "timeline.item" : "shape.edit")
                        .replace("%item%", nameResolver.getTagName(editingShape == null ? (Tag) timelined : editingShape)));
                closeTimelineButton.setVisible(true);
            }
            timelinePanel.setTimelined(editingShape == null ? timelined : null);
            
            if (updateStage) {
                stagePanel.setTimelined(timelined, swf, 0, true, true, true, true, true, false, true, true, true);
                if (timelined instanceof CharacterTag) {
                    stagePanel.setGuidesCharacter(swf, ((CharacterTag) timelined).getCharacterId());
                } else if (editingShape != null) {
                    stagePanel.setGuidesCharacter(swf, editingShape.getCharacterId());
                } else {
                    stagePanel.setGuidesCharacter(swf, -1);
                }
                stagePanel.pause();
                //stagePanel.gotoFrame(0);
            }
            
            if (updateStage) {                
                //timelinePanel.getTimelineBodyPanel().frameSelect(0, 0);
            }
            
            instancePropertiesPanel.update();
            textInstancePropertiesPanel.update();
        }
        refreshShapeDisplay();
        updatePropertiesPanel();
        updateUndos();
        refreshShapePoints(Collections.emptyList());
    }

    public Openable getOpenable() {
        return timelined.getSwf();
    }

    public void clearUndos() {
        undoManager.clear();
    }

    private void updateUndos() {
        undoButton.setEnabled(timelined != null && undoManager.canUndo(timelined.getSwf()));
        redoButton.setEnabled(timelined != null && undoManager.canRedo(timelined.getSwf()));
        if (timelined != null && undoManager.canUndo(timelined.getSwf())) {
            undoButton.setToolTipText(EasyStrings.translate("undo").replace("%action%", undoManager.getUndoName(timelined.getSwf())));
        } else {
            undoButton.setToolTipText(EasyStrings.translate("undo.cannot"));
        }
        if (timelined != null && undoManager.canRedo(timelined.getSwf())) {
            redoButton.setToolTipText(EasyStrings.translate("redo").replace("%action%", undoManager.getRedoName(timelined.getSwf())));
        } else {
            redoButton.setToolTipText(EasyStrings.translate("redo.cannot"));
        }
        if (stagePanel.getTimelined() == null) {
            return;
        }
        updatePropertiesPanel();
        Main.getMainFrame().getPanel().updateUiWithCurrentOpenable();        
    }

    boolean isShapeLayerHidden(int layer) {
        return shapeLayerStates.get(layer).hidden;
    }

    boolean isShapeLayerLocked(int layer) {
        return shapeLayerStates.get(layer).locked;
    }

    private boolean shapeLayerEditable() {
        return shapeLayerEditable(activeShapeLayer);
    }

    private boolean shapeLayerEditable(int layer) {
        return editingShape != null && !isShapeLayerHidden(layer) && !isShapeLayerLocked(layer);
    }

    void toggleShapeLayerVisibility(int layer) {
        toggleShapeLayerState(layer, true);
    }

    void toggleShapeLayerLock(int layer) {
        toggleShapeLayerState(layer, false);
    }

    private void toggleShapeLayerState(int layer, boolean visibility) {
        List<ShapeLayerState> affected = layer < 0 ? shapeLayerStates : Collections.singletonList(shapeLayerStates.get(layer));
        boolean enabled = !affected.stream().allMatch(state -> visibility ? state.hidden : state.locked);
        boolean wasEditable = shapeLayerEditable();
        boolean blocksSelection = enabled && (layer < 0 ? !shapeSelections.isEmpty() : shapeSelections.containsKey(layer));
        if (enabled && shapeSelectionTool()) {
            finishFloatingSelection(layer);
        } else if (enabled && wasEditable && (layer < 0 || layer == activeShapeLayer)) {
            prepareShapeTransfer();
        }
        for (ShapeLayerState state : affected) {
            if (visibility) {
                state.hidden = enabled;
            } else {
                state.locked = enabled;
            }
        }
        if (wasEditable != shapeLayerEditable() || shapeTool == ShapeTool.ANCHOR_POINT) {
            if (!shapeLayerEditable()) {
                eyedropperSample = null;
                eyedropperPropertiesPanel.setSample(null);
            }
            updateShapeTool();
        }
        if (blocksSelection) {
            showLayerSelections(shapeSelections);
        }
        if (visibility) {
            refreshShapeDisplay();
        }
        shapeTimelinePanel.repaint();
    }

    private void refreshShapeDisplay() {
        try {
            stagePanel.setShapeDisplay(editingShape != null && shapeLayerStates.stream().anyMatch(state -> state.hidden)
                    ? visibleShape(editingShape, shapeLayers) : null);
            if (unfilteredShapePreview != null) {
                previewShape(unfilteredShapePreview);
            }
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private void clearShapePreview() {
        unfilteredShapePreview = null;
        stagePanel.setShapePreview(null);
    }

    private void previewShape(ShapeTag shape) throws IOException, InterruptedException {
        unfilteredShapePreview = shape;
        if (shapeLayerStates.stream().anyMatch(state -> state.hidden)) {
            shape = visibleShape(shape, ShapeLayers.split(shape.getShapes(), shape.getShapeNum()));
        }
        stagePanel.setShapePreview(shape);
    }

    private ShapeTag visibleShape(ShapeTag shape, List<SHAPEWITHSTYLE> layers) throws IOException, InterruptedException {
        List<SHAPEWITHSTYLE> visible = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            if (!isShapeLayerHidden(i)) {
                visible.add(layers.get(i));
            }
        }
        if (visible.isEmpty()) {
            visible.add(SHAPEWITHSTYLE.createEmpty(shape.getShapeNum()));
        }
        return ShapeStyleChangeOperation.createShape(shape, visible);
    }

    Set<Integer> getHiddenDepths(Timelined timelineOwner) {
        return hiddenDepthsByTimeline.getOrDefault(timelineOwner, Collections.emptySet());
    }

    Set<Integer> getLockedDepths(Timelined timelineOwner) {
        return lockedDepthsByTimeline.getOrDefault(timelineOwner, Collections.emptySet());
    }

    void toggleDepthVisibility(int depth) {
        if (timelined != null) {
            toggleDepth(hiddenDepthsByTimeline, depth);
            refreshDepthInteraction();
        }
    }

    void toggleDepthLock(int depth) {
        if (timelined != null) {
            toggleDepth(lockedDepthsByTimeline, depth);
            refreshDepthLocks();
        }
    }

    void toggleAllDepthVisibility() {
        if (toggleAllDepths(hiddenDepthsByTimeline)) {
            refreshDepthInteraction();
        }
    }

    void toggleAllDepthLocks() {
        if (toggleAllDepths(lockedDepthsByTimeline)) {
            refreshDepthLocks();
        }
    }

    private void refreshDepthLocks() {
        timelinePanel.getTimelineBodyPanel().removeLockedSelection();
        refreshDepthInteraction();
    }

    private boolean toggleAllDepths(Map<Timelined, Set<Integer>> depthsByTimeline) {
        int maxDepth = timelined == null ? 0 : timelined.getTimeline().getMaxDepth();
        if (maxDepth == 0) {
            return false;
        }
        Set<Integer> depths = depthsByTimeline.getOrDefault(timelined, Collections.emptySet());
        Set<Integer> allDepths = new HashSet<>();
        for (int depth = 1; depth <= maxDepth; depth++) {
            allDepths.add(depth);
        }
        setDepths(depthsByTimeline, depths.containsAll(allDepths) ? Collections.emptySet() : allDepths);
        return true;
    }

    private void toggleDepth(Map<Timelined, Set<Integer>> depthsByTimeline, int depth) {
        Set<Integer> depths = new HashSet<>(depthsByTimeline.getOrDefault(timelined, Collections.emptySet()));
        if (!depths.remove(depth)) {
            depths.add(depth);
        }
        setDepths(depthsByTimeline, depths);
    }

    private void setDepths(Map<Timelined, Set<Integer>> depthsByTimeline, Set<Integer> depths) {
        if (depths.isEmpty()) {
            depthsByTimeline.remove(timelined);
        } else {
            depthsByTimeline.put(timelined, Collections.unmodifiableSet(depths));
        }
    }

    private void refreshDepthInteraction() {
        stagePanel.refreshDepthInteraction();
        transformPanel.setVisible(!stagePanel.getSelectedDepths().isEmpty());
        updatePropertiesPanel();
    }

    void swfClosed(SWF swf) {
        shapeLayerStatesByTag.keySet().removeIf(shape -> shape.getSwf() == swf);
        hiddenDepthsByTimeline.keySet().removeIf(owner -> owner.getSwf() == swf);
        lockedDepthsByTimeline.keySet().removeIf(owner -> owner.getSwf() == swf);
    }

    public void dispose() {
        setTimelined(null);
        hiddenDepthsByTimeline.clear();
        lockedDepthsByTimeline.clear();
        shapeLayerStatesByTag.clear();
        undoManager.clear();
    }

    public List<Integer> getDepths() {
        return stagePanel.getSelectedDepths();
    }

    public int getFrame() {
        return timelinePanel.getTimelineBodyPanel().getFirstFrame();
    }

    public ImagePanel getStagePanel() {
        return stagePanel;
    }

    public SWF getSwf() {
        return timelined == null ? null : timelined.getSwf();
    }

    public List<DepthState> getSelectedDepthStates() {
        if (timelined == null) {
            return null;
        }
        int frame = stagePanel.getFrame();
        List<Integer> depths = stagePanel.getSelectedDepths();
        List<DepthState> ret = new ArrayList<>();
        if (editingShape != null) {
            return ret;
        }
        for (int i = 0; i < depths.size(); i++) {
            ret.add(timelined.getTimeline().getDepthState(frame, depths.get(i)));
        }
        return ret;
    }

    public List<PlaceObjectTypeTag> getSelectedPlaceTags() {
        List<DepthState> dss = getSelectedDepthStates();
        if (dss == null) {
            return null;
        }
        List<PlaceObjectTypeTag> ret = new ArrayList<>();
        for (DepthState ds : dss) {
            if (ds == null) {
                ret.add(null);
            } else {
                if (ds.placeObjectTag == null) {
                    ret.add(ds.toPlaceObjectTag(ds.depth));
                } else {
                    ret.add(ds.placeObjectTag);
                }
            }
        }
        return ret;
    }

    public Timelined getTimelined() {
        return timelined;
    }

    public void setFrame(int frame, List<Integer> depths) {
        timelinePanel.setFrame(frame, depths);
    }

    CharacterTag getSelectedLibraryCharacter() {
        int row = libraryTreeTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) libraryTreeTable.getValueAt(row, 0);
        Object item = node.getUserObject();
        return item instanceof CharacterTag ? (CharacterTag) item : null;
    }
}
