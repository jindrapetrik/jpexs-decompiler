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
import de.javagl.treetable.JTreeTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
public class MainFrame extends JFrame {
    private File file;
    private SWF swf;
    private LibraryTreeTable libraryTreeTable;
    private JSplitPane splitPane;
    public MainFrame() {
        setTitle("JPEXS FFDec Easy GUI");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
                
        libraryTreeTable = new LibraryTreeTable();
        JScrollPane scrollPane = new JScrollPane(libraryTreeTable);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), scrollPane);        
        scrollPane.getViewport().setBackground(UIManager.getColor("Tree.background"));        
        cnt.add(splitPane, BorderLayout.CENTER);        
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
