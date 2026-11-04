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
import java.util.List;
import java.util.Objects;
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
    private JButton undoButton;
    private JButton redoButton;
    private UndoManager undoManager;
    private JTabbedPane rightTabbedPane;
    private TransformPanel transformPanel;
    private ShapeSelectionTransform.State shapeTransformState;
    private Timelined timelined;
    private ShapeTag editingShape;
    private ShapeTool shapeTool = ShapeTool.SELECT;
    private ShapeSelection shapeSelection;
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

        stagePanel = new ImagePanel();
        stagePanel.setTagNameResolver(new EasyTagNameResolver());
        stagePanel.setShowAllDepthLevelsInfo(false);
        stagePanel.setSelectionMode(true);
        stagePanel.setMultiSelect(true);
        stagePanel.addPointUpdateListener(new PointUpdateListener() {
            @Override
            public void pointsUpdated(List<DisplayPoint> points) {
                if (editingShape == null || shapeTool != ShapeTool.EDIT_POINTS) {
                    return;
                }
                if (pointEdit == null) {
                    beginPointEdit();
                    pointEditDragging = false;
                }
                if (pointEdit != null) {
                    pointEdit.updatePoints(points);
                    stagePanel.setShapePreview(pointEdit.getShape());
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
                if (editingShape != null && shapeTool == ShapeTool.EDIT_POINTS && !positions.isEmpty()) {
                    beginPointEdit();
                    if (pointEdit != null) {
                        pointEdit.removePoints(positions);
                        finishPointEdit(false);
                    }
                }
            }

            @Override
            public boolean edgeSplit(int position, double splitPoint) {
                if (editingShape != null && shapeTool == ShapeTool.EDIT_POINTS) {
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
                if (shapeSelection == null || shapeTool != ShapeTool.SELECT || e.isConsumed() || e.getClickCount() != 1
                        || !SwingUtilities.isLeftMouseButton(e) || e.isAltDown()) {
                    return;
                }
                Point2D point = stagePanel.toTimelinedPoint(e.getPoint());
                if (!e.isShiftDown()) {
                    if (shapeSelection.hasFloatingSelection() && shapeSelectionContains(point)) {
                        return;
                    }
                    finishFloatingSelection();
                }
                ShapeSelection.Selection selected = shapeSelection.select(point, 3, stagePanel.getTimelinedToPanelTransform());
                List<ShapeSelection.Selection> next = ShapeSelection.updateSelection(selectedShapeParts, selected, e.isShiftDown());
                if (next.isEmpty()) {
                    finishFloatingSelection();
                }
                showShapeSelections(next);
                if (rightTabbedPane.getSelectedIndex() != 2) {
                    rightTabbedPane.setSelectedIndex(0);
                }
                updatePropertiesPanel();
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

                        undoManager.doOperation(new TimelinedTagListDoableOperation(EasySwfPanel.this, stagePanel.getTimelined()) {

                            private List<Tag> swfTags;

                            @Override
                            public void doOperation() {
                                super.doOperation();
                                CharacterTag ch = (CharacterTag) tag;
                                int maxDepth = timelined.getTimeline().getMaxDepth();
                                int newDepth = maxDepth + 1;

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

        verticalSplitPane = new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, timelinePanel, Configuration.guiSplitPaneEasyVerticalDividerLocationPercent);

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
        if (editingShape == null || Main.isWorking()) {
            return;
        }
        prepareShapeTransfer();
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
        SWF swf = editingShape.getSwf();
        try {
            ShapeReplaceOperation operation = new ShapeReplaceOperation(editingShape,
                    mainPanel.translate(updateBounds ? "button.replaceNoFill" : "button.replace"), (tag, forward) -> {
                        mainPanel.refreshTree(tag.getSwf());
                        editShape(tag, context);
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
        if (penTool.isPending()) {
            penTool.finish(false);
        }
        finishPointEdit(false);
        finishFloatingSelection();
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
        ShapeSelection.RectangleSelection selection = shapeSelection.selectFillTransform(point, stagePanel.getTimelinedToPanelTransform());
        shapeSelection = selection.model;
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
        fillTransformSession = new ShapeFillTransform(shapeSelection, selectedShapeParts);
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
        stagePanel.setShapePreview(null);
        if (!canceled && edit != null && edit.changed) {
            applyShapeEdit(edit, EasyStrings.translate("action.transformFill"));
        } else if (shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
            updateFillTransformHandles();
        }
    }

    private ShapeSelection.Selection styleAt(Point2D point) {
        if (point == null || shapeSelection == null) {
            return null;
        }
        double minimum = Configuration.useMinimumStrokeWidth1Px.get() ? SWF.unitDivisor / stagePanel.getZoom().value : 1;
        return shapeSelection.sampleStyle(point, stagePanel.getParentMatrix().toTransform(), minimum);
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
            ShapeSelection.Selection region = shapeSelection.fillRegionAt(point);
            if (region == null) {
                return;
            }
            edit = shapeSelection.setRegionFill(region, paintPropertiesPanel.getFillStyle(region.geometry.getBounds2D()));
        } else {
            ShapeSelection.Selection region = shapeSelection.fillRegionAt(point);
            if (region == null) {
                return;
            }
            LINESTYLE2 line = inkPropertiesPanel.getStrokeStyle(region.geometry.getBounds2D());
            if (line != null) {
                line.noClose = false;
            }
            edit = shapeSelection.outlineRegion(region, line);
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
        stagePanel.setShapeOriginVisible(editingShape != null && shapeTool == ShapeTool.ANCHOR_POINT);
        if (shapeTool == ShapeTool.GRADIENT_TRANSFORM) {
            stagePanel.setGradientTransformGestureListeners(this::beginFillTransform, this::finishFillTransform);
        } else {
            stagePanel.clearGradientTransform();
            stagePanel.setGradientTransformGestureListeners(null, null);
        }
        boolean selecting = editingShape != null && (shapeTool == ShapeTool.SELECT || shapeTool == ShapeTool.LASSO);
        stagePanel.setAreaSelectionListener(!selecting ? null : (area, additive) -> {
            if (!additive) {
                finishFloatingSelection();
            }
            ShapeSelection.RectangleSelection selection = shapeSelection.selectArea(area, selectedShapeParts, additive);
            shapeSelection = selection.model;
            showShapeSelections(selection.selected);
            if (rightTabbedPane.getSelectedIndex() != 2) {
                rightTabbedPane.setSelectedIndex(0);
            }
            updatePropertiesPanel();
        }, shapeTool == ShapeTool.LASSO);
        stagePanel.setSelectionMoveListeners(!selecting ? null
                : this::shapeSelectionContains,
                !selecting ? null : this::moveShapeSelection);
        stagePanel.setSelectionDeleteListener(!selecting ? null : this::deleteShapeSelection);
        boolean drawing = editingShape != null && drawingToolSelected();
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
                        stagePanel.setShapePreview(null);
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
        if (editingShape != null && shapeTool == ShapeTool.EDIT_POINTS) {
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
        if (editingShape == null || shapeTool != ShapeTool.EDIT_POINTS) {
            return;
        }
        try {
            pointEdit = new ShapePointEdit(editingShape);
            pointEditDragging = true;
        } catch (IOException | InterruptedException ex) {
            logShapeEditFailure(ex);
        }
    }

    private void refreshShapePoints(List<Integer> selected) {
        if (editingShape == null || shapeTool != ShapeTool.EDIT_POINTS) {
            return;
        }
        try {
            ShapePointEdit edit = new ShapePointEdit(editingShape);
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
        stagePanel.setShapePreview(null);
        if (canceled || edit == null || !edit.isChanged()) {
            refreshShapePoints(selected);
            return;
        }
        applyShapeTagEdit(edit.getShape(), EasyStrings.translate("action.editShapePoints"), selected);
    }

    private void moveWholeShape(Point2D point, boolean finished) {
        if (anchorMove == null) {
            return;
        }
        try {
            ShapeTag moved = anchorMove.moveTo(point);
            if (finished) {
                anchorMove = null;
                stagePanel.setShapePreview(null);
                if (moved != null) {
                    applyShapeTagEdit(moved, EasyStrings.translate("action.move"), Collections.emptyList());
                }
            } else {
                stagePanel.setShapePreview(moved);
            }
        } catch (IOException | InterruptedException ex) {
            anchorMove = null;
            stagePanel.setShapePreview(null);
            logShapeEditFailure(ex);
        }
    }

    private void applyShapeTagEdit(ShapeTag edited, String description, List<Integer> selected) {
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        try {
            undoManager.doOperation(new ShapeStyleChangeOperation(editingShape, edited, description, (tag, forward) -> {
                Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
                boolean sameShape = editingShape != null && editingShape.getSwf() == tag.getSwf()
                        && editingShape.getCharacterId() == tag.getCharacterId();
                ImagePanel.ShapeEditContext active = stagePanel.getShapeEditContext();
                boolean preserveView = sameShape && context == null && active == null && !stagePanel.getZoom().fit;
                editShape(tag, sameShape && context != null && context.sameLocation(active) ? active : context);
                if (preserveView) {
                    stagePanel.setViewPosition(origin);
                }
                refreshShapePoints(selected);
                libraryTreeTable.setSwf(null);
                libraryTreeTable.setSwf(tag.getSwf());
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
        if (line != null) {
            line.noClose = !pen.isClosed();
        }
        applyDrawing(shapeSelection.drawShape(path, penPropertiesPanel.getFillStyle(bounds), line),
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
        ShapeSelection.BatchEdit edit = shapeSelection.drawShape(geometry, drawingShapesPropertiesPanel.getFill(bounds),
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
            edit = shapeSelection.erase(area, stagePanel.getParentMatrix().toTransform());
        } else {
            FILLSTYLE fill = brushPropertiesPanel.getFillStyle(area.getBounds2D());
            edit = shapeSelection.paint(area, fill);
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
        ShapeSelection.BatchEdit edit = shapeSelection.drawStroke(path, line);
        if (finished) {
            pencilStroke = null;
        }
        applyDrawing(edit, EasyStrings.translate("action.pencil"), finished);
    }

    private void applyDrawing(ShapeSelection.BatchEdit edit, String description, boolean finished) {
        if (finished || !edit.changed) {
            stagePanel.setShapePreview(null);
            if (finished && edit.changed) {
                applyShapeEdit(edit, description);
            }
        } else {
            try {
                stagePanel.setShapePreview(ShapeStyleChangeOperation.createShape(editingShape, edit.model));
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(EasySwfPanel.class.getName()).log(Level.SEVERE, "Cannot preview drawing", ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void editShape(ShapeTag shape) {
        setTimelined(TimelinedMaker.makeTimelined(shape), true, shape);
        rightTabbedPane.setSelectedIndex(0);
    }

    private void editShape(ShapeTag shape, ImagePanel.ShapeEditContext context) {
        if (context == null) {
            editShape(shape);
            return;
        }
        updatingTimelined = true;
        try {
            stagePanel.restoreShapeEditContext(shape, context);
            updateTimelined(stagePanel.getTimelined(), false, shape);
        } finally {
            updatingTimelined = false;
        }
        rightTabbedPane.setSelectedIndex(0);
    }

    private void showShapeSelections(List<ShapeSelection.Selection> selected) {
        stagePanel.setShapeSelectionTransform(null, null);
        shapeTransformState = null;
        if (shapeTool == ShapeTool.EDIT_POINTS) {
            selectedShapeParts = new ArrayList<>();
            refreshShapePoints(Collections.emptyList());
            return;
        }
        selectedShapeParts = highlightShapeSelections(shapeSelection, selected);
        shapePropertiesPanel.setSelections(selectedShapeParts);
        updateFillTransformHandles();
        updateShapeSelectionTransform();
    }

    private List<ShapeSelection.Selection> highlightShapeSelections(ShapeSelection model, List<ShapeSelection.Selection> selected) {
        List<ShapeSelection.Selection> displayed = new ArrayList<>();
        List<java.awt.Shape> fills = new ArrayList<>();
        List<ImagePanel.LineHilight> lines = new ArrayList<>();
        for (ShapeSelection.Selection item : selected) {
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
        if (editingShape == null || selectedShapeParts.isEmpty()) {
            return;
        }
        ShapeSelection.BatchEdit edit = shapeSelection.moveFloatingSelection(selectedShapeParts, delta.getX(), delta.getY());
        if (commit || !edit.changed) {
            stagePanel.setShapePreview(null);
        }
        if (!edit.changed) {
            showShapeSelections(selectedShapeParts);
        } else if (commit) {
            ShapeSelectionTransform.State before = shapeTransformState;
            applyShapeEdit(edit, EasyStrings.translate("action.move"), before,
                    before == null ? null : before.transformed(Matrix.getTranslateInstance(Math.rint(delta.getX()), Math.rint(delta.getY()))));
        } else {
            try {
                stagePanel.setShapePreview(ShapeStyleChangeOperation.createShape(editingShape, edit.model));
                highlightShapeSelections(edit.model, edit.selected);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(EasySwfPanel.class.getName()).log(Level.SEVERE, "Cannot preview shape move", ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                stagePanel.setShapePreview(null);
                showShapeSelections(selectedShapeParts);
            }
        }
    }

    private void finishFloatingSelection() {
        if (editingShape != null && shapeSelection != null && shapeSelection.hasFloatingSelection()) {
            applyShapeEdit(shapeSelection.mergeFloatingSelection(), EasyStrings.translate("action.move"));
        }
    }

    private void transformShapeSelection(Matrix matrix, boolean commit) {
        if (!shapeSelectionTool() || selectedShapeParts.isEmpty()) {
            return;
        }
        ShapeSelection.BatchEdit edit = shapeSelection.transformFloatingSelection(selectedShapeParts, matrix.toTransform());
        if (commit) {
            stagePanel.setShapePreview(null);
            if (edit.changed) {
                ShapeSelectionTransform.State before = stagePanel.getShapeSelectionTransformState();
                applyShapeEdit(edit, EasyStrings.translate("action.transform"), before, before == null ? null : before.transformed(matrix));
            }
        } else {
            applyDrawing(edit, EasyStrings.translate("action.transform"), false);
            highlightShapeSelections(edit.model, edit.selected);
        }
    }

    private void deleteShapeSelection() {
        if (editingShape == null || selectedShapeParts.isEmpty()) {
            return;
        }
        stagePanel.setShapePreview(null);
        ShapeSelection.BatchEdit edit = shapeSelection.deleteAll(selectedShapeParts);
        if (edit.changed) {
            applyShapeEdit(edit, EasyStrings.translate("action.deleteShapeSelection"));
        } else {
            showShapeSelections(edit.selected);
        }
    }

    private void changeShapeStyle(boolean stroke, String property) {
        if (editingShape == null || selectedShapeParts.stream().noneMatch(item -> item.stroke == stroke)) {
            return;
        }
        List<ShapeSelection.Selection> previous = new ArrayList<>(selectedShapeParts);
        if (!shapePropertiesPanel.isPropertyChangeValid(stroke, property)) {
            showShapeSelections(previous);
            return;
        }
        ShapeSelection.BatchEdit edit = shapeSelection.changeAll(previous,
                stroke ? null : item -> shapePropertiesPanel.applyFillChange(item.fill, item.geometry.getBounds2D(), property),
                stroke ? item -> shapePropertiesPanel.applyStrokeChange(item.line, item.area.getBounds2D(), property) : null);
        if (!edit.changed) {
            showShapeSelections(previous);
            return;
        }
        String description = EasyStrings.translate("action.change").replace("%item%",
                EasyStrings.translate("properties.shape.header." + (stroke ? "stroke" : "fill")));
        applyShapeEdit(edit, description);
    }

    private void applyShapeEdit(ShapeSelection.BatchEdit edit, String description) {
        ShapeSelectionTransform.State state = stagePanel.getShapeSelectionTransformState();
        if (state == null) {
            state = shapeTransformState;
        }
        applyShapeEdit(edit, description, state, state);
    }

    private void applyShapeEdit(ShapeSelection.BatchEdit edit, String description,
            ShapeSelectionTransform.State beforeTransform, ShapeSelectionTransform.State afterTransform) {
        List<ShapeSelection.Selection> previous = new ArrayList<>(selectedShapeParts);
        ShapeSelection beforeModel = shapeSelection;
        ImagePanel.ShapeEditContext context = stagePanel.getShapeEditContext();
        try {
            undoManager.doOperation(new ShapeStyleChangeOperation(editingShape, edit.model, description, (tag, forward) -> {
                int activeTab = rightTabbedPane.getSelectedIndex();
                Point2D origin = stagePanel.toTimelinedPoint(new Point(0, 0));
                boolean sameShape = editingShape != null && editingShape.getSwf() == tag.getSwf()
                        && editingShape.getCharacterId() == tag.getCharacterId();
                ImagePanel.ShapeEditContext activeContext = stagePanel.getShapeEditContext();
                boolean preserveView = sameShape && context == null && activeContext == null && !stagePanel.getZoom().fit;
                editShape(tag, sameShape && context != null && context.sameLocation(activeContext) ? activeContext : context);
                shapeSelection = forward ? edit.committedModel(tag) : beforeModel;
                if (preserveView) {
                    stagePanel.setViewPosition(origin);
                }
                showShapeSelections(forward ? edit.selected : previous);
                if (activeTab == 2 && rightTabbedPane.isEnabledAt(activeTab)) {
                    rightTabbedPane.setSelectedIndex(activeTab);
                }
                if (!selectedShapeParts.isEmpty()) {
                    shapeTransformState = forward ? afterTransform : beforeTransform;
                    stagePanel.restoreShapeSelectionTransformState(shapeTransformState);
                }
                libraryTreeTable.setSwf(null);
                libraryTreeTable.setSwf(tag.getSwf());
            }), editingShape.getSwf());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(EasySwfPanel.class.getName()).log(Level.SEVERE, "Cannot edit shape", ex);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            showShapeSelections(previous);
        }
    }

    public void setTimelined(Timelined timelined) {
        setTimelined(timelined, true);
    }

    private void setTimelined(Timelined timelined, boolean updateStage) {
        setTimelined(timelined, updateStage, null);
    }

    private void setTimelined(Timelined timelined, boolean updateStage, ShapeTag shape) {
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
            updateTimelined(timelined, updateStage, shape);
        } finally {
            updatingTimelined = false;
        }
    }

    private void updateTimelined(Timelined timelined, boolean updateStage, ShapeTag shape) {
        if (shape == null || editingShape == null || shape.getSwf() != editingShape.getSwf()
                || shape.getCharacterId() != editingShape.getCharacterId()) {
            shapeTool = ShapeTool.SELECT;
        }
        editingShape = shape;
        shapeSelection = shape == null ? null : new ShapeSelection(shape);
        selectedShapeParts = new ArrayList<>();
        shapeTransformState = null;
        stagePanel.setShapeSelectionTransform(null, null);
        this.timelined = timelined;
        stagePanel.setSelectionMode(editingShape == null);
        stagePanel.setMultiSelect(editingShape == null);
        stagePanel.setAltSelectionEnabled(editingShape == null);
        updateShapeTool();
        stagePanel.setTransformSelectionMode(transformEnabled());
        timelinePanel.setVisible(editingShape == null);
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

    public void dispose() {
        setTimelined(null);
        undoManager.clear();
    }

    public List<Integer> getDepths() {
        return stagePanel.getSelectedDepths();
    }

    public int getFrame() {
        Point p = timelinePanel.getTimelineBodyPanel().cursor.iterator().next();
        if (p == null) {
            return 0;
        }
        return p.x;
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
