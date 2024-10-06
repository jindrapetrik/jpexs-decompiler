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
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.TransformPanel;
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
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
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
public class MainFrame extends JFrame {

    private SWF swf;
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

    public MainFrame() {
        setTitle("JPEXS FFDec Easy GUI");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

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

                    @Override
                    public void doOperation() {
                        timelinePanel.setFrame(frame, depth);
                        DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                        ds.placeObjectTag.setMatrix(newMatrix);
                        ds.placeObjectTag.setPlaceFlagHasMatrix(newMatrix != null);
                        stagePanel.getTimelined().resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepth(depth);
                            stagePanel.setRegistrationPoint(regPoint);
                            if (regPointPos != null) {
                                stagePanel.setRegistrationPointPosition(regPointPos);
                            }
                        }
                    }

                    @Override
                    public void undoOperation() {
                        timelinePanel.setFrame(frame, depth);
                        DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(frame).layers.get(depth);
                        ds.placeObjectTag.setMatrix(fpreviousMatrix);
                        ds.placeObjectTag.setPlaceFlagHasMatrix(fpreviousMatrix != null);
                        stagePanel.getTimelined().resetTimeline();
                        stagePanel.repaint();
                        if (transformEnabled()) {
                            stagePanel.freeTransformDepth(depth);
                        }
                    }

                    @Override
                    public String getDescription() {
                        return transformEnabled ? "Transform" : "Move";
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

                        undoManager.doOperation(new DoableOperation() {
                            
                            private PlaceObject2Tag place;
                            private RemoveObject2Tag remove;
                            private List<Tag> tags;
                            private int frame = stagePanel.getFrame();
                            private int depth = stagePanel.getSelectedDepth();
                            
                            @Override
                            public void doOperation() {
                                timelinePanel.setFrame(frame, depth);
                                CharacterTag ch = (CharacterTag) tag;
                                int maxDepth = stagePanel.getTimelined().getTimeline().getMaxDepth();
                                int newDepth = maxDepth + 1;
                                Timelined timelined = stagePanel.getTimelined();
                                
                                tags = timelined.getSwf().getTags().toArrayList();
                                
                                ShowFrameTag showFrameTag = timelined.getTimeline().getFrame(frame).showFrameTag;
                                place = new PlaceObject2Tag(timelined.getSwf());
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
                                    
                                    remove = new RemoveObject2Tag(timelined.getSwf());
                                    remove.depth = newDepth;
                                    timelined.addTag(timelined.indexOfTag(showFrameTag) + 1, remove);                                    
                                }
                                
                                
                                DefineBeforeUsageFixer fixer = new DefineBeforeUsageFixer();
                                boolean tagOrderChanged = fixer.fixDefineBeforeUsage(timelined.getSwf());
                                if (!tagOrderChanged) {
                                    tags = null;
                                }
                                
                                timelined.resetTimeline();
                                stagePanel.repaint();                                
                                timelinePanel.refresh();
                                timelinePanel.setDepth(newDepth);
                            }

                            @Override
                            public void undoOperation() {
                                Timelined timelined = place.getTimelined();
                                timelined.removeTag(place);
                                if (remove != null) {
                                    timelined.removeTag(remove);
                                }
                                timelined.resetTimeline();
                                
                                //Tag order changed, put the original tags back
                                if (tags != null) {
                                    SWF swf = timelined.getSwf();
                                    ReadOnlyTagList newTags = swf.getTags();
                                    int size = newTags.size();
                                    for (int i = 0; i < size; i++) {
                                        swf.removeTag(0);
                                    }
                                    for (int i = 0; i < tags.size(); i++) {
                                        if (tags.get(i) == place) {
                                            continue;
                                        }
                                        swf.addTag(tags.get(i));
                                    }
                                    swf.resetTimeline();
                                }
                                
                                stagePanel.repaint();
                                timelinePanel.refresh();
                                timelinePanel.setFrame(frame, depth);
                            }

                            @Override
                            public String getDescription() {
                                return "Add to stage";
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

        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        undoButton = new JButton(View.getIcon("rotateanticlockwise16"));
        undoButton.setToolTipText("Undo");
        undoButton.setMargin(new Insets(5, 5, 5, 5));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.undo();
            }
        });

        redoButton = new JButton(View.getIcon("rotateclockwise16"));
        redoButton.setToolTipText("Redo");
        redoButton.setMargin(new Insets(5, 5, 5, 5));
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
            }
        });

        Runnable undoChangeListener = new Runnable() {
            @Override
            public void run() {
                undoButton.setEnabled(undoManager.canUndo());
                redoButton.setEnabled(undoManager.canRedo());
                if (undoManager.canUndo()) {
                    undoButton.setToolTipText("Undo " + undoManager.getUndoName());
                } else {
                    undoButton.setToolTipText("Cannot undo");
                }
                if (undoManager.canRedo()) {
                    redoButton.setToolTipText("Redo " + undoManager.getRedoName());
                } else {
                    redoButton.setToolTipText("Cannot redo");
                }
            }
        };

        undoManager.addChangeListener(undoChangeListener);
        undoChangeListener.run();

        toolbarPanel.add(undoButton);
        toolbarPanel.add(redoButton);

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
        
        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, timelinePanel);

        libraryTreeTable = new LibraryTreeTable();
        JScrollPane libraryScrollPane = new JScrollPane(libraryTreeTable);

        JPanel libraryPanel = new JPanel(new BorderLayout());
        libraryPanel.add(libraryScrollPane, BorderLayout.CENTER);

        libraryPreviewPanel = new ImagePanel();
        libraryPreviewPanel.setTopPanelVisible(false);

        libraryPanel.add(libraryPreviewPanel, BorderLayout.NORTH);

        libraryPreviewPanel.setPreferredSize(new Dimension(200, 200));

        rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Library", libraryPanel);

        JPanel transformTab = new JPanel(new BorderLayout());
        transformPanel = new TransformPanel(stagePanel, false);
        transformTab.add(new JScrollPane(transformPanel), BorderLayout.CENTER);

        rightTabbedPane.addTab("Transform", transformTab);
        rightTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int depth = stagePanel.getSelectedDepth();
                if (depth != -1) {
                    DepthState ds = stagePanel.getTimelined().getTimeline().getFrame(stagePanel.getFrame()).layers.get(depth);                    
                    if (ds == null) {
                        depth = -1;
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

        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplitPane, rightTabbedPane);
        libraryScrollPane.getViewport().setBackground(UIManager.getColor("Tree.background"));
        cnt.add(horizontalSplitPane, BorderLayout.CENTER);

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

    public void open(File file) throws IOException, InterruptedException {
        try (FileInputStream fis = new FileInputStream(file)) {
            swf = new SWF(fis, true);
        }

        libraryTreeTable.setSwf(swf);
        stagePanel.setTimelined(swf, swf, 0, true, true, true, true, true, false, true);
        stagePanel.pause();
        stagePanel.gotoFrame(0);
        timelinePanel.setTimelined(swf);        
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        verticalSplitPane.setDividerLocation(0.7);
        horizontalSplitPane.setDividerLocation(0.7);
    }
}
