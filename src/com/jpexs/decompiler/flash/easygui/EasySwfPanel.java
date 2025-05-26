/*
 *  Copyright (C) 2024-2025 JPEXS
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
import com.jpexs.decompiler.flash.easygui.properties.panels.DocumentPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.InstancePropertiesPanel;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
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
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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
    private Timelined timelined;
    private JLabel timelineLabel;
    private JButton closeTimelineButton;
    private JPanel propertiesPanel;

    private static final String PROPERTIES_DOCUMENT = "Document";
    private static final String PROPERTIES_INSTANCE = "Instance";
    private DocumentPropertiesPanel documentPropertiesPanel;
    private InstancePropertiesPanel instancePropertiesPanel;
    private final MainPanel mainPanel;

    public EasySwfPanel(MainPanel mainPanel) {
        setLayout(new BorderLayout());

        stagePanel = new ImagePanel();
        stagePanel.setTagNameResolver(new EasyTagNameResolver());
        stagePanel.setShowAllDepthLevelsInfo(false);
        stagePanel.setSelectionMode(true);
        stagePanel.setMultiSelect(true);

        stagePanel.addEventListener(new MediaDisplayListener() {
            @Override
            public void mediaDisplayStateChanged(MediaDisplay source) {
                if (stagePanel.getTimelined() != timelined) {
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            setTimelined(stagePanel.getTimelined(), false);
                        }
                    });

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
                timelinePanel.setDepths(depths);
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
                return support.isDataFlavorSupported(CharacterTagTransferable.CHARACTERTAG_FLAVOR);
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

        instancePropertiesPanel = new InstancePropertiesPanel(this, undoManager);
        propertiesPanel.add(documentPropertiesPanel, PROPERTIES_DOCUMENT);
        propertiesPanel.add(instancePropertiesPanel, PROPERTIES_INSTANCE);

        rightTabbedPane.addTab(EasyStrings.translate("properties"), propertiesPanel);

        rightTabbedPane.addTab(EasyStrings.translate("library"), libraryPanel);

        JPanel transformTab = new JPanel(new BorderLayout());
        transformPanel = new TransformPanel(stagePanel, false);
        transformTab.add(new FasterScrollPane(transformPanel), BorderLayout.CENTER);

        rightTabbedPane.addTab(EasyStrings.translate("transform"), transformTab);
        rightTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
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
        this.mainPanel = mainPanel;
    }

    private void updatePropertiesPanel() {
        CardLayout cl = (CardLayout) propertiesPanel.getLayout();
        List<PlaceObjectTypeTag> places = getSelectedPlaceTags();
        if (places == null || places.isEmpty()) {
            cl.show(propertiesPanel, PROPERTIES_DOCUMENT);
            return;
        }
        instancePropertiesPanel.update();
        cl.show(propertiesPanel, PROPERTIES_INSTANCE);
    }

    private boolean transformEnabled() {
        return rightTabbedPane.getSelectedIndex() == 2;
    }

    public void setTimelined(Timelined timelined) {
        setTimelined(timelined, true);
    }

    private void setTimelined(Timelined timelined, boolean updateStage) {
        if (this.timelined == timelined) {
            return;
        }
        if (mainPanel.getCurrentView() != MainPanel.VIEW_EASY) {
            timelined = null;
        }
        this.timelined = timelined;
        if (timelined == null) {
            stagePanel.clearAll();
            timelinePanel.setTimelined(null);
            libraryTreeTable.setSwf(null);
            libraryPreviewPanel.clearAll();
            closeTimelineButton.setVisible(false);
            timelineLabel.setText("");
            documentPropertiesPanel.setSwf(null);
            instancePropertiesPanel.update();
        } else {
            SWF swf = timelined.getSwf();
            documentPropertiesPanel.setSwf(swf);
            libraryTreeTable.setSwf(swf);
            libraryPreviewPanel.clearAll();
            if (updateStage) {
                stagePanel.setTimelined(timelined, swf, 0, true, true, true, true, true, false, true, true, true);
                if (timelined instanceof CharacterTag) {
                    stagePanel.setGuidesCharacter(swf, ((CharacterTag) timelined).getCharacterId());
                } else {
                    stagePanel.setGuidesCharacter(swf, -1);
                }
                stagePanel.pause();
                stagePanel.gotoFrame(0);
            }
            timelinePanel.setTimelined(timelined);
            if (timelined instanceof SWF) {
                timelineLabel.setText(EasyStrings.translate("timeline.main"));
                closeTimelineButton.setVisible(false);
            } else {
                EasyTagNameResolver nameResolver = new EasyTagNameResolver();
                timelineLabel.setText(EasyStrings.translate("timeline.item").replace("%item%", nameResolver.getTagName((Tag) timelined)));
                closeTimelineButton.setVisible(true);
            }
            instancePropertiesPanel.update();
        }
        updateUndos();
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
        return stagePanel.getFrame();
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
}
