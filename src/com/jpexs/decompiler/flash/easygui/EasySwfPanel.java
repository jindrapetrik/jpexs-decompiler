/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.DefineBeforeUsageFixer;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.TransformPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
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
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.BorderLayout;
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
import java.util.List;
import javax.swing.JButton;
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

    public EasySwfPanel() {
        setLayout(new BorderLayout());

        stagePanel = new ImagePanel();
        stagePanel.setTagNameResolver(new EasyTagNameResolver());
        stagePanel.setShowAllDepthLevelsInfo(false);
        stagePanel.setSelectionMode(true);
        stagePanel.addPlaceObjectSelectedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaceObjectTypeTag pl = stagePanel.getPlaceTagUnderCursor();
                if (pl != null) {
                    timelinePanel.setDepth(pl.getDepth());
                }
                transformPanel.setVisible(pl != null);
            }
        });

        stagePanel.addTransformChangeListener(new Runnable() {
            @Override
            public void run() {
                final int depth = stagePanel.getSelectedDepth();
                final int frame = stagePanel.getFrame();
                final MATRIX newMatrix = stagePanel.getNewMatrix().toMATRIX();
                MATRIX previousMatrix = null;
                synchronized (stagePanel) {
                    DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                    previousMatrix = ds.placeObjectTag.getMatrix();
                }

                final Point2D regPoint = stagePanel.getRegistrationPoint();
                final RegistrationPointPosition regPointPos = stagePanel.getRegistrationPointPosition();

                final MATRIX fpreviousMatrix = previousMatrix;

                final boolean transformEnabled = transformEnabled();
                undoManager.doOperation(new DoableOperation() {

                    private boolean wasModified = false;
                    
                    @Override
                    public void doOperation() {
                        timelinePanel.setFrame(frame, depth);
                        DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                        wasModified = ds.placeObjectTag.isModified();
                        ds.placeObjectTag.setMatrix(newMatrix);
                        ds.placeObjectTag.setPlaceFlagHasMatrix(newMatrix != null);
                        ds.placeObjectTag.setModified(true);    
                        stagePanel.getTimelined().resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepth(depth);
                            stagePanel.setRegistrationPoint(regPoint);
                            if (regPointPos != null) {
                                stagePanel.setRegistrationPointPosition(regPointPos);
                            }
                            transformPanel.setVisible(true);
                        }
                    }

                    @Override
                    public void undoOperation() {
                        timelinePanel.setFrame(frame, depth);
                        DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                        ds.placeObjectTag.setMatrix(fpreviousMatrix);
                        ds.placeObjectTag.setPlaceFlagHasMatrix(fpreviousMatrix != null);
                        if (!wasModified) {
                            ds.placeObjectTag.setModified(false);
                        }
                        stagePanel.getTimelined().resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepth(depth);
                            transformPanel.setVisible(true);
                        }
                    }

                    @Override
                    public String getDescription() {
                        return EasyStrings.translate(transformEnabled ? "action.transform" : "action.move");
                    }
                });

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
                            || (tag instanceof ButtonTag)
                            ) {

                        undoManager.doOperation(new TimelinedTagListDoableOperation(stagePanel.getTimelined()) {
                            
                            private List<Tag> swfTags;
                            private final int fframe = stagePanel.getFrame();
                            private final int fdepth = stagePanel.getSelectedDepth();
                            
                            @Override
                            public void doOperation() {
                                super.doOperation();
                                timelinePanel.setFrame(fframe, fdepth);
                                CharacterTag ch = (CharacterTag) tag;
                                int maxDepth = stagePanel.getTimelined().getTimeline().getMaxDepth();
                                int newDepth = maxDepth + 1;
                                Timelined timelined = stagePanel.getTimelined();
                                
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
                                if (showFrameTag == null) {
                                    timelined.addTag(place);
                                } else {
                                    timelined.addTag(timelined.indexOfTag(showFrameTag), place);
                                    
                                    RemoveObject2Tag remove = new RemoveObject2Tag(timelined.getSwf());
                                    remove.depth = newDepth;
                                    timelined.addTag(timelined.indexOfTag(showFrameTag) + 1, remove);                                    
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
                                timelinePanel.setFrame(fframe, fdepth);
                            }

                            @Override
                            public String getDescription() {
                                return EasyStrings.translate("action.addToStage");
                            }

                        });

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

        

        undoButton = new JButton(View.getIcon("rotateanticlockwise16"));
        //undoButton.setToolTipText("Undo");
        undoButton.setMargin(new Insets(5, 5, 5, 5));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.undo();
            }
        });

        redoButton = new JButton(View.getIcon("rotateclockwise16"));
        //redoButton.setToolTipText("Redo");
        redoButton.setMargin(new Insets(5, 5, 5, 5));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
            }
        });

        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoButton.setToolTipText(EasyStrings.translate("undo.cannot"));
        redoButton.setToolTipText(EasyStrings.translate("redo.cannot"));
        
        Runnable undoChangeListener = new Runnable() {
            @Override
            public void run() {
                undoButton.setEnabled(undoManager.canUndo());
                redoButton.setEnabled(undoManager.canRedo());
                if (undoManager.canUndo()) {
                    undoButton.setToolTipText(EasyStrings.translate("undo").replace("%action%",undoManager.getUndoName()));
                } else {
                    undoButton.setToolTipText(EasyStrings.translate("undo.cannot"));
                }
                if (undoManager.canRedo()) {
                    redoButton.setToolTipText(EasyStrings.translate("redo").replace("%action%", undoManager.getRedoName()));
                } else {
                    redoButton.setToolTipText(EasyStrings.translate("redo.cannot"));
                }
                if (stagePanel.getTimelined() == null) {
                    return;
                }
                Main.getMainFrame().getPanel().updateUiWithCurrentOpenable();
            }
        };

        undoManager.addChangeListener(undoChangeListener);

        
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftToolbar.add(undoButton);
        leftToolbar.add(redoButton);
        
        toolbarPanel.add(leftToolbar, BorderLayout.WEST);
        
        ZoomPanel zoomPanel = new ZoomPanel(stagePanel);
        toolbarPanel.add(zoomPanel, BorderLayout.EAST);
        
        topPanel.add(toolbarPanel, BorderLayout.NORTH);
        topPanel.add(stagePanel, BorderLayout.CENTER);

        timelinePanel = new TimelinePanel(undoManager);
        
        timelinePanel.addChangeListener(new Runnable() {
            @Override
            public void run() {
                stagePanel.repaint();
            }            
        });
        timelinePanel.addFrameSelectionListener(new FrameSelectionListener() {
            @Override
            public void frameSelected(int frame, int depth) {
                stagePanel.pause();
                stagePanel.gotoFrame(frame + 1);
                stagePanel.selectDepth(depth);
                if (transformEnabled()) {
                    stagePanel.freeTransformDepth(depth);
                }
                
                if (depth != -1) {
                    DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(stagePanel.getFrame()).layers.get(depth);                    
                    if (ds == null) {
                        depth = -1;
                    }
                }
                transformPanel.setVisible(depth != -1);
            }
        });
        
        verticalSplitPane = new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, timelinePanel, Configuration.guiSplitPaneEasyVerticaldividerLocationPercent);

        libraryTreeTable = new LibraryTreeTable();
        JScrollPane libraryScrollPane = new FasterScrollPane(libraryTreeTable);

        JPanel libraryPanel = new JPanel(new BorderLayout());
        libraryPanel.add(libraryScrollPane, BorderLayout.CENTER);

        libraryPreviewPanel = new ImagePanel();
        libraryPreviewPanel.setTopPanelVisible(false);

        libraryPanel.add(libraryPreviewPanel, BorderLayout.NORTH);

        libraryPreviewPanel.setPreferredSize(new Dimension(200, 200));

        rightTabbedPane = new JTabbedPane();
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
                int depth = stagePanel.getSelectedDepth();
                if (stagePanel.getFrame() >= stagePanel.getTimelined().getFrameCount()) {
                    depth = -1;
                }
                if (depth != -1) {  
                    Frame frame = stagePanel.getTimelined().getTimeline().getFrame(stagePanel.getFrame());
                    if (frame == null) {
                        depth = -1;
                    } else {
                        DepthState ds = frame.layers.get(depth);                    
                        if (ds == null) {
                            depth = -1;
                        }
                    }
                }
                transformPanel.setVisible(depth != -1);
                if (transformEnabled()) {
                    stagePanel.freeTransformDepth(depth);
                    stagePanel.setTransformSelectionMode(true);
                } else {
                    stagePanel.freeTransformDepth(-1);
                    stagePanel.selectDepth(depth);
                    stagePanel.setTransformSelectionMode(false);
                }
            }
        });

        horizontalSplitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplitPane, rightTabbedPane, Configuration.guiSplitPaneEasyHorizontaldividerLocationPercent);

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
                            -1, false, true, true, true, true, false, true);
                    libraryPreviewPanel.zoomFit();
                } else {
                    libraryPreviewPanel.clearAll();
                }
            }
        });
    }

    private boolean transformEnabled() {
        return rightTabbedPane.getSelectedIndex() == 1;
    }  
    
    public void setTimelined(Timelined timelined) {        
        this.timelined = timelined;
        if (timelined == null) {
            stagePanel.clearAll();
            timelinePanel.setTimelined(null);
        } else {
            SWF swf = timelined.getSwf();
            libraryTreeTable.setSwf(swf);
            stagePanel.setTimelined(swf, swf, 0, true, true, true, true, true, false, true);
            stagePanel.pause();
            stagePanel.gotoFrame(0);
            timelinePanel.setTimelined(swf);  
        }
        undoManager.clear();
    }

    public Openable getOpenable() {
        return timelined.getSwf();
    }
}
