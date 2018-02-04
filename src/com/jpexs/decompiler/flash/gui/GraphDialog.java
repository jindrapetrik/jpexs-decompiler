/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.exporters.script.PcodeGraphVizExporter;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.graph.AbstractGraphPanel;
import com.jpexs.decompiler.flash.gui.graph.GraphPanelSimple;
import com.jpexs.decompiler.flash.gui.graph.GraphVizGraphPanel;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.graph.Graph;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author JPEXS
 */
public class GraphDialog extends AppDialog {

    AbstractGraphPanel gp;

    int scrollBarWidth;

    int scrollBarHeight;

    int frameWidthDiff;

    int frameHeightDiff;

    private Graph graph;

    private static final Logger logger = Logger.getLogger(GraphDialog.class.getName());

    public GraphDialog(Window owner, Graph graph, String name) throws InterruptedException {
        super(owner);
        this.graph = graph;
        setSize(500, 500);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        if (GraphVizGraphPanel.isAvailable()) {
            gp = new GraphVizGraphPanel(graph);
        } else {
            gp = new GraphPanelSimple(graph);
            JPanel betterGraphInfo = new JPanel(new FlowLayout());
            JLabel lab = new JLabel("<html><font color=\"#0000CF\"><u>" + translate("graph.better.dot") + "</u></font></html>");
            lab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lab.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Main.advancedSettings("paths");
                }

            });
            betterGraphInfo.add(lab);
            cnt.add(betterGraphInfo, BorderLayout.SOUTH);
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem(translate("menu.copygraph.gv"));
        copyMenuItem.addActionListener(this::copyToClipBoardActionPerformed);
        popupMenu.add(copyMenuItem);
        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });

        setTitle(translate("graph") + " " + name);
        JScrollPane scrollPane = new JScrollPane(gp);
        scrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
        scrollBarHeight = scrollPane.getHorizontalScrollBar().getPreferredSize().height;
        cnt.add(scrollPane, BorderLayout.CENTER);
        pack();

        Dimension size = getSize();
        Dimension innerSize = getContentPane().getSize();

        frameWidthDiff = size.width - innerSize.width;
        frameHeightDiff = size.height - innerSize.height;

        View.setWindowIcon(this);

        MouseAdapter ma = new MouseAdapter() {

            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, gp);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        gp.scrollRectToVisible(view);
                    }
                }
            }

        };

        gp.addMouseListener(ma);
        gp.addMouseMotionListener(ma);
    }

    private void copyToClipBoardActionPerformed(ActionEvent evt) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            StringBuilderTextWriter stringBuilderWriter = new StringBuilderTextWriter(null, stringBuilder);
            new PcodeGraphVizExporter().export(graph, stringBuilderWriter);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while generating graph", ex);
            return;
        }
        try {
            StringSelection stringSelection = new StringSelection(stringBuilder.toString());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot copy to clipboard", ex);
        }
    }

    @Override
    public void setVisible(boolean b) {

        super.setVisible(b);
        Dimension screen = getToolkit().getScreenSize();
        Dimension dim = new Dimension(0, 0);
        Dimension panDim = gp.getPreferredSize();
        // add some magic constants
        panDim = new Dimension(panDim.width + 3, panDim.height + 2);

        boolean tooHigh = false;
        boolean tooWide = false;

        if (panDim.width + frameWidthDiff < screen.width) {
            dim.width = panDim.width;
        } else {
            dim.width = screen.width;
            tooWide = true;
        }
        if (panDim.height + frameHeightDiff < screen.height) {
            dim.height = panDim.height;
        } else {
            dim.height = screen.height;
            tooHigh = true;
        }

        if (tooWide) {
            dim.height += scrollBarHeight;
            dim.height = Math.min(dim.height, screen.height);
        }
        if (tooHigh) {
            dim.width += scrollBarWidth;
            dim.width = Math.min(dim.width, screen.width);
        }

        setVisibleSize(dim);
        View.centerScreen(this);
    }

    private void setVisibleSize(Dimension dim) {
        setSize(new Dimension(dim.width + frameWidthDiff, dim.height + frameHeightDiff));
    }

}
