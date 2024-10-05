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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.Tag;
import de.javagl.treetable.JTreeTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JPEXS
 */
public class MainFrame extends JFrame {
    private File file;
    private SWF swf;
    private LibraryTreeTable libraryTreeTable;
    private JSplitPane splitPane;
    private ImagePanel libraryPreviewPanel;
    public MainFrame() {
        setTitle("JPEXS FFDec Easy GUI");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
                
        libraryTreeTable = new LibraryTreeTable();
        JScrollPane libraryScrollPane = new JScrollPane(libraryTreeTable);
        
        JPanel libraryPanel = new JPanel(new BorderLayout());
        libraryPanel.add(libraryScrollPane, BorderLayout.CENTER);
        
        libraryPreviewPanel = new ImagePanel();
        libraryPreviewPanel.setTopPanelVisible(false); 
        
        JPanel topLibraryPanel = new JPanel(new BorderLayout());
        JLabel libraryLabel = new JLabel("Library");
        libraryLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        topLibraryPanel.add(libraryLabel, BorderLayout.NORTH);
        topLibraryPanel.add(libraryPreviewPanel, BorderLayout.CENTER);
        libraryPanel.add(topLibraryPanel, BorderLayout.NORTH);
        
        libraryPreviewPanel.setPreferredSize(new Dimension(200,200));
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), libraryPanel);        
        libraryScrollPane.getViewport().setBackground(UIManager.getColor("Tree.background"));        
        cnt.add(splitPane, BorderLayout.CENTER);    
        
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
                }
            }            
        });
    }
    
    public void open(File file) throws IOException, InterruptedException {
        this.file = file;
        try(FileInputStream fis = new FileInputStream(file)) {
            swf = new SWF(fis, true);
        }
        
        libraryTreeTable.setSwf(swf);
        
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        splitPane.setDividerLocation(0.7);
    }
    
    
}
