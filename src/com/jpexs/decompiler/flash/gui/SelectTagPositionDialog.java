/*
 *  Copyright (C) 2022-2025 JPEXS
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
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
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
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

    private boolean selectNext;

    private static class MyTreeNode implements TreeNode {

        private final List<TreeNode> children = new ArrayList<>();
        private TreeNode parent;
        private Object data;

        @Override
        public String toString() {
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
        private List<String> labels;

        public MyFrame(int frame, List<String> labels) {
            this.frame = frame;
            this.labels = labels;
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
            String name = "frame " + frame;
            if (!labels.isEmpty()) {
                name += " (" + String.join(", ", labels) + ")";
            }
            return name;
        }
    }

    private void populateNodes(MyTreeNode root, Timelined tim, int minFrame) {
        int f = 1;

        MyTreeNode frameNode = new MyTreeNode();
        List<String> labels = new ArrayList<>();
        if (minFrame <= 1) {
            frameNode.setData(new MyFrame(1, labels));
            frameNode.setParent(root);
            root.addChild(frameNode);
        }

        boolean wasMinFrame = minFrame <= 1;

        for (Tag t : tim.getTags()) {

            if (wasMinFrame) {
                MyTreeNode node = new MyTreeNode();
                node.setData(t);
                frameNode.addChild(node);

                if (t instanceof DefineSpriteTag) {
                    if (allowInsideSprites) {
                        populateNodes(node, (DefineSpriteTag) t, 1);
                    }
                }
                if (t instanceof FrameLabelTag) {
                    labels.add(((FrameLabelTag) t).name);
                }
            }
            if (t instanceof ShowFrameTag) {
                f++;
                if (f >= minFrame) {
                    wasMinFrame = true;

                    frameNode = new MyTreeNode();
                    labels = new ArrayList<>();
                    frameNode.setData(new MyFrame(f, labels));
                    frameNode.setParent(root);
                    root.addChild(frameNode);
                }
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

    private void selectPath(List<Object> path) {
        Object[] pathArray = path.toArray(new Object[path.size()]);
        TreePath tpath = new TreePath(pathArray);
        positionTree.setSelectionPath(tpath);
        int row = positionTree.getRowForPath(tpath);
        if (row != -1) {
            Rectangle rect = positionTree.getRowBounds(row);
            rect.width += rect.x;
            rect.x = 0;
            positionTree.scrollRectToVisible(rect);
        }
    }

    private void selectCurrent(MyTreeNode root, Timelined timelined, List<Object> path) {
        for (int i = 0; i < root.getChildCount(); i++) {
            MyTreeNode node = (MyTreeNode) root.getChildAt(i);

            List<Object> subPath = new ArrayList<>(path);
            subPath.add(node);

            List<Object> nextPath = new ArrayList<>(path);
            if (i + 1 < root.getChildCount()) {
                nextPath.add(root.getChildAt(i + 1));
            }

            if (timelined == selectedTimelined && ((node.getData() == selectedTag))) {
                selectPath(selectNext ? nextPath : subPath);
                return;
            }
            if (timelined == selectedTimelined && (node.getData() instanceof MyTimelineEnd) && selectedTag == null) {
                selectPath(subPath);
                return;
            }
            if ((selectedTimelined instanceof DefineSpriteTag) && !allowInsideSprites && node.getData() == selectedTimelined) {
                selectPath(nextPath);
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
        this(parent, swf, allowInsideSprites, null, 1);
    }

    public SelectTagPositionDialog(Window parent, SWF swf, boolean allowInsideSprites, String newTypeName, int minFrame) {
        this(parent, swf, null, null, allowInsideSprites, false, newTypeName, minFrame);
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
                if (subValue instanceof MyTimelineEnd) {
                    lab.setIcon(TagTree.getIconForType(TreeNodeType.END));
                }

                if (subValue instanceof MyFrame) {
                    lab.setIcon(TagTree.getIconForType(TreeNodeType.FRAME));
                }
                if (subValue instanceof TreeItem) {
                    lab.setIcon(TagTree.getIconForType(TagTree.getTreeNodeType((TreeItem) subValue)));
                }
            }
            return renderer;
        }
    }

    public SelectTagPositionDialog(Window parent, SWF swf, Tag selectedTag, Timelined selectedTimelined, boolean allowInsideSprites, boolean selectNext, String newTypeName, int minFrame) {
        super(parent);
        this.swf = swf;
        this.selectedTag = selectedTag;
        this.selectedTimelined = selectedTimelined;
        this.allowInsideSprites = allowInsideSprites;
        this.selectNext = selectNext;
        if (newTypeName != null) {
            setTitle(translate("dialog.title.new.typed").replace("%type%", newTypeName).replace("%filetitle%", swf.getShortPathTitle()));
        } else {
            setTitle(translate("dialog.title").replace("%filetitle%", swf.getShortPathTitle()));
        }
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

        populateNodes(root, swf, minFrame);

        positionTree = new JTree(root) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int[] rows = getSelectionRows();
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
        positionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        previewPanel = new PreviewPanel(Main.getMainFrame().getPanel());
        previewPanel.setReadOnly(true);
        previewPanel.setPreferredSize(new Dimension(300, 1));
        previewPanel.showEmpty();
        previewPanel.setParametersPanelVisible(false);

        JScrollPane positionTreeScrollPane = new FasterScrollPane(positionTree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, positionTreeScrollPane, previewPanel);
        splitPane.setDividerLocation(400);
        cnt.add(splitPane, BorderLayout.CENTER);

        List<Object> path = new ArrayList<>();
        path.add(root);
        selectCurrent(root, swf, path);

        setSize(1024, 600);
        setModal(true);
        setResizable(true);
        View.centerScreen(this);
        View.setWindowIcon(this);

        calculateEnabled();
    }

    public void positionTreeValueChanged(TreeSelectionEvent e) {
        calculateEnabled();
    }

    private void calculateEnabled() {
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
                previewPanel.showImagePanel((DefineSpriteTag) parent, swf, f - 1, true, true, !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, false, false);
            } else {
                previewPanel.showImagePanel(swf, swf, f - 1, true, true, !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, false, false);
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
     */
    public Tag getSelectedTag() {
        return selectedTag;
    }

    /**
     * Gets selected timelined
     */
    public Timelined getSelectedTimelined() {
        return selectedTimelined;
    }

}
