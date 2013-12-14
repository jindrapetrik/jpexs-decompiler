/*
 *  Copyright (C) 2013 JPEXS
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

import java.awt.Color;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

/**
 *
 * @author JPEXS
 */
public class MyTree extends JTree {

    public MyTree() {
        setUI(new MyTreeUI());
        setBackground(Color.white);
    }

    public MyTree(TreeModel newModel) {
        super(newModel);
        setUI(new MyTreeUI());
        setBackground(Color.white);
    }
    private boolean overrideIsEnabled = false;

    public void setOverrideIsEnable(boolean b) {
        overrideIsEnabled = true;
    }

    public boolean isOverrideIsEnable(boolean b) {
        return overrideIsEnabled;
    }

    @Override
    public boolean isEnabled() {
        if (overrideIsEnabled) {
            return false;
        }
        return super.isEnabled();
    }
    /*
     @Override
     public void paint(Graphics g) {
     g.setColor(Color.white);
     g.fillRect(0, 0, getWidth(), getHeight());
     super.paint(g);
     }*/
}
