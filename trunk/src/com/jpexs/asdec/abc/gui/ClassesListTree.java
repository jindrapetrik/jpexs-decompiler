/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;


public class ClassesListTree extends JTree implements TreeSelectionListener {
    public ABC abc;

    public ClassesListTree(ABC abc) {
        this.abc = abc;
        setModel(new ClassesListTreeModel(abc));
        addTreeSelectionListener(this);
        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        ClassLoader cldr = this.getClass().getClassLoader();
        java.net.URL imageURL = cldr.getResource("com/jpexs/asdec/abc/gui/graphics/class.png");
        ImageIcon leafIcon = new ImageIcon(imageURL);
        treeRenderer.setLeafIcon(leafIcon);
        setCellRenderer(treeRenderer);
    }

    public void setABC(ABC abc) {
        setModel(new ClassesListTreeModel(abc));
        this.abc = abc;
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (Main.isWorking()) return;
        final TreePart tp = (TreePart) getLastSelectedPathComponent();
        if (tp == null) return;
        if (tp.classIndex != -1) {
            if (!Main.isWorking()) {
                Main.startWork("Decompiling class...");
                (new Thread() {
                    @Override
                    public void run() {
                        Main.abcMainFrame.navigator.setClassIndex(tp.classIndex);
                        Main.abcMainFrame.decompiledTextArea.setClassIndex(tp.classIndex, abc);
                        Main.abcMainFrame.sourceTextArea.setText("");
                        Main.stopWork();
                    }
                }).start();
            }
        }
    }

}
