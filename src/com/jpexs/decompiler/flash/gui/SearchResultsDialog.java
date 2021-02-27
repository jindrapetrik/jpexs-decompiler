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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.search.SearchResult;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 * @param <E> Element to search
 */
public class SearchResultsDialog<E extends SearchResult> extends AppDialog {

    private final JTree resultsTree;

    private final JList<SearchResult> resultsList;

    private final JPanel resultsPanel;

    private final boolean regExp;

    private final List<SearchListener<E>> listeners;

    private final JButton gotoButton = new JButton(translate("button.goto"));

    private final JButton closeButton = new JButton(translate("button.close"));

    private String text;
    private final boolean ignoreCase;

    private Map<SWF, List<SearchResult>> swfToResults = new LinkedHashMap<>();

    public SearchResultsDialog(Window owner, String text, boolean ignoreCase, boolean regExp, List<SearchListener<E>> listeners) {
        super(owner);
        setTitle(translate("dialog.title").replace("%text%", text));
        this.text = text;
        Container cnt = getContentPane();
        resultsTree = new JTree(new BasicTreeNode("root"));
        resultsList = new JList<>(new DefaultListModel<>());
        this.regExp = regExp;
        this.listeners = listeners;

        gotoButton.addActionListener(this::gotoButtonActionPerformed);
        closeButton.addActionListener(this::closeButtonActionPerformed);

        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new FlowLayout());
        JLabel searchTextLabel = new JLabel(AppDialog.translateForDialog("label.searchtext", SearchDialog.class) + text);
        JLabel ignoreCaseLabel = new JLabel(AppDialog.translateForDialog("checkbox.ignorecase", SearchDialog.class) + ": " + (ignoreCase ? AppStrings.translate("yes") : AppStrings.translate("no")));
        JLabel regExpLabel = new JLabel(AppDialog.translateForDialog("checkbox.regexp", SearchDialog.class) + ": " + (regExp ? AppStrings.translate("yes") : AppStrings.translate("no")));
        paramsPanel.add(ignoreCaseLabel);
        paramsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        paramsPanel.add(regExpLabel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(gotoButton);
        buttonsPanel.add(closeButton);
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gotoElement();
                }
            }
        };
        resultsTree.addKeyListener(keyListener);

        resultsList.addKeyListener(keyListener);

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    gotoElement();
                }
            }
        };
        resultsTree.addMouseListener(mouseListener);
        resultsList.addMouseListener(mouseListener);

        resultsTree.setRootVisible(false);

        resultsTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                throw new ExpandVetoException(event, "Collapsing tree not allowed");
            }
        });

        final ImageIcon flashIcon = View.getIcon("flash16");

        resultsTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    BasicTreeNode node = (BasicTreeNode) value;
                    if (node.getData() instanceof SWF) {
                        label.setIcon(flashIcon);
                    } else {
                        label.setIcon(null);
                    }
                }
                return c;
            }

        });

        resultsTree.setUI(new BasicTreeUI() {

            @Override
            public void paint(Graphics g, JComponent c) {
                if (tree != c) {
                    throw new InternalError("incorrect component");
                }

                // Should never happen if installed for a UI
                if (treeState == null) {
                    return;
                }

                Rectangle paintBounds = g.getClipBounds();
                Insets insets = tree.getInsets();
                TreePath initialPath = getClosestPathForLocation(tree, 0, paintBounds.y);
                Enumeration<?> paintingEnumerator = treeState.getVisiblePathsFrom(initialPath);
                int row = treeState.getRowForPath(initialPath);
                int endY = paintBounds.y + paintBounds.height;

                drawingCache.clear();

                if (initialPath != null && paintingEnumerator != null) {
                    TreePath parentPath = initialPath;

                    // Draw the lines, knobs, and rows
                    // Find each parent and have them draw a line to their last child
                    parentPath = parentPath.getParentPath();
                    while (parentPath != null) {
                        paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
                        drawingCache.put(parentPath, Boolean.TRUE);
                        parentPath = parentPath.getParentPath();
                    }

                    boolean done = false;
                    // Information for the node being rendered.
                    boolean isExpanded;
                    boolean hasBeenExpanded;
                    boolean isLeaf;
                    Rectangle boundsBuffer = new Rectangle();
                    Rectangle bounds;
                    TreePath path;
                    boolean rootVisible = isRootVisible();

                    while (!done && paintingEnumerator.hasMoreElements()) {
                        path = (TreePath) paintingEnumerator.nextElement();
                        if (path != null) {
                            isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                            if (isLeaf) {
                                isExpanded = hasBeenExpanded = false;
                            } else {
                                isExpanded = treeState.getExpandedState(path);
                                hasBeenExpanded = tree.hasBeenExpanded(path);
                            }
                            bounds = getPathBounds(path, insets, boundsBuffer);
                            if (bounds == null) // This will only happen if the model changes out
                            // from under us (usually in another thread).
                            // Swing isn't multithreaded, but I'll put this
                            // check in anyway.
                            {
                                return;
                            }
                            // See if the vertical line to the parent has been drawn.
                            parentPath = path.getParentPath();
                            if (parentPath != null) {
                                if (drawingCache.get(parentPath) == null) {
                                    paintVerticalPartOfLeg(g, paintBounds,
                                            insets, parentPath);
                                    drawingCache.put(parentPath, Boolean.TRUE);
                                }
                                paintHorizontalPartOfLeg(g, paintBounds, insets,
                                        bounds, path, row,
                                        isExpanded,
                                        hasBeenExpanded, isLeaf);
                            } else if (rootVisible && row == 0) {
                                paintHorizontalPartOfLeg(g, paintBounds, insets,
                                        bounds, path, row,
                                        isExpanded,
                                        hasBeenExpanded, isLeaf);
                            }
                            if (shouldPaintExpandControl(path, row, isExpanded,
                                    hasBeenExpanded, isLeaf)) {
                                paintExpandControl(g, paintBounds, insets, bounds,
                                        path, row, isExpanded,
                                        hasBeenExpanded, isLeaf);
                            }
                            paintRow(g, paintBounds, insets, bounds, path,
                                    row, isExpanded, hasBeenExpanded, isLeaf);
                            if ((bounds.y + bounds.height) >= endY) {
                                done = true;
                            }
                        } else {
                            done = true;
                        }
                        row++;
                    }
                }

                paintDropLine(g);

                // Empty out the renderer pane, allowing renderers to be gc'ed.
                rendererPane.removeAll();

                drawingCache.clear();
            }

            @Override
            public Rectangle getPathBounds(JTree tree, TreePath path) {
                if (tree != null && treeState != null) {
                    return getPathBounds(path, tree.getInsets(), new Rectangle());
                }
                return null;
            }

            private Rectangle getPathBounds(
                    TreePath path, Insets insets, Rectangle bounds) {
                bounds = treeState.getBounds(path, bounds);
                if (bounds != null) {
                    bounds.width = tree.getWidth();
                    bounds.y += insets.top;
                }
                return bounds;
            }
        });

        cnt.setLayout(new BorderLayout());

        resultsPanel = new JPanel(new CardLayout());
        resultsPanel.add(resultsTree, "tree");
        resultsPanel.add(resultsList, "list");

        JScrollPane sp = new FasterScrollPane(resultsPanel);
        sp.setPreferredSize(new Dimension(300, 300));
        cnt.add(sp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JPanel searchTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchTextPanel.add(searchTextLabel);
        if (Configuration.parametersPanelInSearchResults.get()) {
            bottomPanel.add(searchTextPanel);
            bottomPanel.add(paramsPanel);
            bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        bottomPanel.add(buttonsPanel);
        cnt.add(bottomPanel, BorderLayout.SOUTH);
        pack();
        View.centerScreen(this);
        View.setWindowIcon(this);
        this.ignoreCase = ignoreCase;
    }

    private static class BasicTreeNode implements TreeNode {

        private final List<TreeNode> children = new ArrayList<>();
        private TreeNode parent;
        private final Object data;

        public BasicTreeNode(Object data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return data.toString();
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

    public void setResults(List<E> results) {
        swfToResults.clear();
        for (E e : results) {
            if (!swfToResults.containsKey(e.getSWF())) {
                swfToResults.put(e.getSWF(), new ArrayList<>());
            }
            swfToResults.get(e.getSWF()).add(e);
        }
        updateModel();
    }

    private void updateModel() {
        boolean showSwfTitles = swfToResults.size() > 1;

        if (showSwfTitles) {
            BasicTreeNode rootNode = new BasicTreeNode("root");
            List<BasicTreeNode> swfNodes = new ArrayList<>();
            for (SWF s : swfToResults.keySet()) {
                BasicTreeNode swfNode = new BasicTreeNode(s);
                if (showSwfTitles) {
                    rootNode.addChild(swfNode);
                    swfNode.setParent(rootNode);
                    swfNodes.add(swfNode);
                }
                for (SearchResult r : swfToResults.get(s)) {
                    BasicTreeNode rNode = new BasicTreeNode(r);
                    if (showSwfTitles) {
                        swfNode.addChild(rNode);
                        rNode.setParent(swfNode);
                    } else {
                        rootNode.addChild(rNode);
                        rNode.setParent(rootNode);
                    }
                }
            }
            resultsTree.setModel(new DefaultTreeModel(rootNode, false));
            for (TreeNode t : swfNodes) {
                TreePath tp = new TreePath(new Object[]{rootNode, t});
                resultsTree.expandPath(tp);
            }
        } else {
            DefaultListModel<SearchResult> model = (DefaultListModel<SearchResult>) resultsList.getModel();
            model.clear();
            if (!swfToResults.isEmpty()) {
                List<SearchResult> elements = swfToResults.get(swfToResults.keySet().iterator().next());
                for (SearchResult e : elements) {
                    model.addElement(e);
                }
            }
        }

        ((CardLayout) resultsPanel.getLayout()).show(resultsPanel, showSwfTitles ? "tree" : "list");
    }

    public void removeSwf(SWF swf) {
        swfToResults.remove(swf);
        updateModel();
    }

    public boolean isEmpty() {
        return swfToResults.isEmpty();
    }

    private void gotoButtonActionPerformed(ActionEvent evt) {
        gotoElement();
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    @SuppressWarnings("unchecked")
    private void gotoElement() {
        if (swfToResults.size() > 1) {
            BasicTreeNode selection = (BasicTreeNode) resultsTree.getLastSelectedPathComponent();
            if (selection.getData() instanceof SearchResult) {
                for (SearchListener<E> listener : listeners) {
                    listener.updateSearchPos(text, ignoreCase, regExp, (E) selection.getData());
                }
            }
        } else {
            for (SearchListener<E> listener : listeners) {
                listener.updateSearchPos(text, ignoreCase, regExp, (E) resultsList.getSelectedValue());
            }
        }
    }
}
