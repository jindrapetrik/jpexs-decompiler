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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 *
 * @author JPEXS
 */
public class ListLayout implements LayoutManager {

    private int border;

    public ListLayout() {
        this(5);
    }

    public ListLayout(int border) {
        this.border = border;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int h = 0;
        int maxw = 0;
        Insets ins = parent.getInsets();
        boolean first = true;
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            if (true) { //!first) {
                h += border;
            }
            Dimension pref = c.getPreferredSize();
            if (pref.width > maxw) {
                maxw = pref.width;
            }
            h += pref.height;
            first = false;
        }
        h += border;

        maxw = (parent.getSize().width == 0 ? maxw : parent.getSize().width) - ins.left - ins.right;
        return new Dimension(maxw, h);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        Dimension dim = preferredLayoutSize(parent);
        Insets ins = parent.getInsets();
        int top = ins.top;
        boolean first = true;
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            if (!first) {
                top += border;
            }
            Dimension pref = c.getPreferredSize();
            c.setPreferredSize(new Dimension(dim.width, pref.height));
            c.setMinimumSize(new Dimension(dim.width, pref.height));
            c.setBounds(0, top, dim.width, pref.height);
            top += pref.height;
            first = false;
        }

    }
}
