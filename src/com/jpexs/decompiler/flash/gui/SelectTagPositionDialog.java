/*
 * Copyright (C) 2022 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import static com.jpexs.decompiler.flash.gui.AppDialog.CANCEL_OPTION;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class SelectTagPositionDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private JTree positionTree;

    private PreviewPanel previewPanel;

    private final SWF swf;

    private int result = ERROR_OPTION;

    private Tag selectedTag = null;
    private Timelined selectedTimelined = null;
    private boolean allowInsideSprites;

    private static class MyTreeNode implements TreeNode {

        private final List<TreeNode> children = new ArrayList<>();
        private TreeNode parent;
        private Object data;

        @Override
        public String toString() {
            if (data instanceof DoInitActionTag) {
                DoInitActionTag doinit = (DoInitActionTag) data;
                String exportName = doinit.getSwf().getExportName(doinit.spriteId);
                if (exportName != null && !exportName.isEmpty()) {
                    return DoInitActionTag.NAME + " (" + doinit.spriteId + ") : " + exportName;
                }
            }
            return data.toString();
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public void addChild(TreeNode node) {
            children.add(node);
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return Collections.enumeration(children);
        }
    }

    private static class MyTimelineEnd {

        @Override
        public String toString() {
            return AppDialog.translateForDialog("timeline.end", SelectTagPositionDialog.class);
        }
    }

    private static class MyFrame {

        private final int frame;
        private boolean invalid;

        public MyFrame(int frame) {
            this.frame = frame;
        }

        public int getFrame() {
            return frame;
        }

        public void setInvalid(boolean invalid) {
            this.invalid = invalid;
        }

        public boolean isInvalid() {
            return invalid;
        }

        @Override
        public String toString() {
            return "frame " + frame;
        }
    }

    private void populateNodes(MyTreeNode root, Timelined tim, int currentFrame) {
        int f = 1;

        MyTreeNode frameNode = new MyTreeNode();
        frameNode.setData(new MyFrame(1));
        frameNode.setParent(root);
        root.addChild(frameNode);

        for (Tag t : tim.getTags()) {
            MyTreeNode node = new MyTreeNode();
            node.setData(t);
            frameNode.addChild(node);

            if (t instanceof DefineSpriteTag) {
                if (allowInsideSprites) {
                    populateNodes(node, (DefineSpriteTag) t, 1);
                }
            }
            if (t instanceof ShowFrameTag) {
                f++;
                frameNode = new MyTreeNode();
                frameNode.setData(new MyFrame(f));
                frameNode.setParent(root);
                root.addChild(frameNode);
            }
        }
        if (frameNode.isLeaf()) {
            root.children.remove(root.children.size() - 1);
        }
        MyTimelineEnd end = new MyTimelineEnd();
        MyTreeNode endNode = new MyTreeNode();
        endNode.setData(end);
        root.addChild(endNode);
    }

    private void selectCurrent(MyTreeNode root, Timelined timelined, List<Object> path) {

        for (int i = 0; i < root.getChildCount(); i++) {
            MyTreeNode node = (MyTreeNode) root.getChildAt(i);

            List<Object> subPath = new ArrayList<>(path);
            subPath.add(node);

            if (node.getData() == selectedTag && timelined == selectedTimelined) {
                Object[] pathArray = subPath.toArray(new Object[subPath.size()]);
                TreePath tpath = new TreePath(pathArray);
                positionTree.setSelectionPath(tpath);
                positionTree.scrollPathToVisible(tpath);
                return;
            }

            if (node.getData() instanceof DefineSpriteTag) {
                selectCurrent(node, (DefineSpriteTag) node.getData(), subPath);
            } else {
                selectCurrent(node, timelined, subPath);
            }
        }
    }

    public SelectTagPositionDialog(Window parent, SWF swf, boolean allowInsideSprites) {
        this(parent, swf, null, null, allowInsideSprites);
    }

    private static class PositionTreeCellRenderer extends DefaultTreeCellRenderer {

        private boolean selected;

        public PositionTreeCellRenderer() {
            if (View.isOceanic()) {
                setUI(new BasicLabelUI());
                setOpaque(false);
                setBackgroundNonSelectionColor(Color.white);
            }
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            this.selected = sel;
            if (renderer instanceof JLabel) {
                JLabel lab = (JLabel) renderer;

                Object subValue = value;
                if (value instanceof MyTreeNode) {
                    subValue = ((MyTreeNode) value).getData();
                }
                TreeNodeType nodeType;
                if (subValue instanceof MyFrame) {
                    nodeType = TreeNodeType.FRAME;
                } else if (subValue instanceof FontTag) {
                    nodeType = TreeNodeType.FONT;
                } else if (subValue instanceof TextTag) {
                    nodeType = TreeNodeType.TEXT;
                } else if (subValue instanceof ImageTag) {
                    nodeType = TreeNodeType.IMAGE;
                } else if (subValue instanceof ShapeTag) {
                    nodeType = TreeNodeType.SHAPE;
                } else if (subValue instanceof MorphShapeTag) {
                    nodeType = TreeNodeType.MORPH_SHAPE;
                } else if (subValue instanceof DefineSpriteTag) {
                    nodeType = TreeNodeType.SPRITE;
                } else if (subValue instanceof ButtonTag) {
                    nodeType = TreeNodeType.BUTTON;
                } else if (subValue instanceof DefineVideoStreamTag) {
                    nodeType = TreeNodeType.MOVIE;
                } else if ((subValue instanceof DefineSoundTag) || (subValue instanceof SoundStreamHeadTypeTag) || (subValue instanceof SoundStreamBlockTag)) {
                    nodeType = TreeNodeType.SOUND;
                } else if (subValue instanceof DefineBinaryDataTag) {
                    nodeType = TreeNodeType.BINARY_DATA;
                } else if ((subValue instanceof DoActionTag)
                        || (subValue instanceof DoInitActionTag)
                        || (subValue instanceof DoABCTag)
                        || (subValue instanceof DoABC2Tag)) {
                    nodeType = TreeNodeType.AS;
                } else if (subValue instanceof ShowFrameTag) {
                    nodeType = TreeNodeType.FRAME;
                } else if (subValue instanceof SetBackgroundColorTag) {
                    nodeType = TreeNodeType.SET_BACKGROUNDCOLOR;
                } else if (subValue instanceof FileAttributesTag) {
                    nodeType = TreeNodeType.FILE_ATTRIBUTES;
                } else if (subValue instanceof MetadataTag) {
                    nodeType = TreeNodeType.METADATA;
                } else if (subValue instanceof PlaceObjectTypeTag) {
                    nodeType = TreeNodeType.PLACE_OBJECT;
                } else if (subValue instanceof RemoveTag) {
                    nodeType = TreeNodeType.REMOVE_OBJECT;
                } else if (subValue instanceof MyTimelineEnd) {
                    nodeType = null;
                } else {
                    nodeType = TreeNodeType.OTHER_TAG;
                }
                if (nodeType == null) {
                    //nothing
                } else {
                    lab.setIcon(TagTree.getIconForType(nodeType));
                }
            }
            return renderer;
        }

    }

    public SelectTagPositionDialog(Window parent, SWF swf, Tag selectedTag, Timelined selectedTimelined, boolean allowInsideSprites) {
        super(parent);
        this.swf = swf;
        this.selectedTag = selectedTag;
        this.selectedTimelined = selectedTimelined;
        this.allowInsideSprites = allowInsideSprites;
        setTitle(translate("dialog.title"));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        MyTreeNode root = new MyTreeNode();
        root.setData("root");

        populateNodes(root, swf, 1);

        positionTree = new JTree(root) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int rows[] = getSelectionRows();
                if (rows.length == 1) {
                    int row = rows[0];

                    Object selection = getLastSelectedPathComponent();
                    boolean onFrame = false;
                    if (selection != null && (((MyTreeNode) selection).getData() instanceof MyFrame)) { // && !isCollapsed(row)) {
                        onFrame = true;
                        return;
                    }
                    Rectangle rect = this.getRowBounds(row);
                    int sideWidth = 6;
                    int sideHeight = 6;
                    int offsetX = -5;

                    int lineStartX = offsetX + rect.x;
                    int backStartX = getWidth() + offsetX;
                    if (onFrame) {
                        g.fillRect(lineStartX, rect.y + rect.height - 1, getWidth() - lineStartX, 1);
                    } else {
                        g.fillRect(lineStartX, rect.y, getWidth() - lineStartX, 1);
                    }
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(getForeground());
                    GeneralPath path = new GeneralPath();

                    if (onFrame) {
                        path.moveTo(backStartX - 6, rect.y + rect.height - 1 - 6);
                        path.lineTo(backStartX, rect.y + rect.height - 1);
                        path.lineTo(backStartX + 6, rect.y + rect.height - 1 - 6);
                    } else {
                        path.moveTo(lineStartX - 6, rect.y + 6);
                        path.lineTo(lineStartX, rect.y);
                        path.lineTo(lineStartX + 6, rect.y + 6);
                    }
                    path.closePath();
                    g2d.fill(path);
                }
            }

        };
        if (View.isOceanic()) {
            positionTree.setBackground(Color.white);
            positionTree.setUI(new BasicTreeUI() {
                {
                    setHashColor(Color.gray);
                }
            });
        }
        positionTree.setCellRenderer(new PositionTreeCellRenderer());
        positionTree.setRootVisible(false);
        positionTree.setShowsRootHandles(true);
        positionTree.addTreeSelectionListener(this::spriteValueChanged);
        positionTree.addTreeSelectionListener(this::positionTreeValueChanged);

        previewPanel = new PreviewPanel(Main.getMainFrame().getPanel(), null);
        previewPanel.setReadOnly(true);
        previewPanel.setPreferredSize(new Dimension(300, 1));
        previewPanel.showEmpty();
        previewPanel.setParametersPanelVisible(false);

        JScrollPane positionTreeScrollPane = new FasterScrollPane(positionTree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, positionTreeScrollPane, previewPanel);
        //splitPane.setDividerLocation(600);
        cnt.add(splitPane, BorderLayout.CENTER);

        List<Object> path = new ArrayList<>();
        path.add(root);
        selectCurrent(root, swf, path);

        setSize(1024, 600);
        setModal(true);
        setResizable(true);
        View.centerScreen(this);
        View.setWindowIcon(this);
    }

    public void positionTreeValueChanged(TreeSelectionEvent e) {
        MyTreeNode node = (MyTreeNode) positionTree.getLastSelectedPathComponent();
        boolean enabled = true;
        if (node == null || (node.getData() instanceof MyFrame)) {
            enabled = false;
        }
        okButton.setEnabled(enabled);
    }

    private int getCurrentSelectedFrame() {
        TreePath path = positionTree.getSelectionPath();
        for (int i = path.getPathCount() - 1; i >= 0; i--) {
            MyTreeNode node = (MyTreeNode) path.getPathComponent(i);
            if (node.getData() instanceof MyFrame) {
                MyFrame frame = (MyFrame) node.getData();
                return frame.frame;
            }
        }
        return -1;
    }

    private Timelined getCurrentSelectedTimelined() {
        TreePath path = positionTree.getSelectionPath();
        for (int i = path.getPathCount() - 1 - 1 /*sprite can be last, use its parent*/; i >= 0; i--) {
            MyTreeNode node = (MyTreeNode) path.getPathComponent(i);
            if ("root".equals(node.getData())) {
                return swf;
            }
            if (node.getData() instanceof DefineSpriteTag) {
                return (Timelined) node.getData();
            }
        }
        return null;
    }

    private void spriteValueChanged(TreeSelectionEvent e) {

        TreePath selection = positionTree.getSelectionPath();
        if (selection == null) {
            previewPanel.showEmpty();
            return;
        }
        MyTreeNode tnode = (MyTreeNode) selection.getLastPathComponent();
        if (tnode.getData() instanceof Tag) {
            MainPanel.showPreview((TreeItem) tnode.getData(), previewPanel, getCurrentSelectedFrame() - 1, getCurrentSelectedTimelined());
        } else if (tnode.getData() instanceof MyFrame) {
            int f = ((MyFrame) tnode.getData()).frame;
            Object parent = ((MyTreeNode) tnode.getParent()).getData();
            if (parent instanceof DefineSpriteTag) {
                previewPanel.showImagePanel((DefineSpriteTag) parent, swf, f - 1, true);
            } else {
                previewPanel.showImagePanel(swf, swf, f - 1, true);
            }
        } else {
            previewPanel.showEmpty();
        }
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        previewPanel.clear();
        setVisible(false);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        MyTreeNode node = (MyTreeNode) positionTree.getLastSelectedPathComponent();
        if (node.getData() instanceof MyFrame) {
            return;
        }
        if (node.getData() instanceof MyTimelineEnd) {
            selectedTag = null;
        } else {
            selectedTag = (Tag) node.getData();
        }

        selectedTimelined = getCurrentSelectedTimelined();

        result = OK_OPTION;
        previewPanel.clear();
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }

    /**
     * Gets current selected tag to determine position. null = end of timeline
     * position
     *
     * @return
     */
    public Tag getSelectedTag() {
        return selectedTag;
    }

    /**
     * Gets selected timelined
     *
     * @return
     */
    public Timelined getSelectedTimelined() {
        return selectedTimelined;
    }

}
