/*
 * Copyright (C) 2013 JPEXS
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

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import javax.swing.tree.TreePath;
import org.pushingpixels.substance.internal.ui.SubstanceTreeUI;

/**
 *
 * @author JPEXS
 */
class MyTreeUI extends SubstanceTreeUI {

    @Override
    protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds,
            Insets insets, Rectangle bounds, TreePath path, int row,
            boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
        System.out.println("");
        if (this.tree instanceof MyTree) {
            Field f = null;
            Boolean v = false;
            try {
                f = SubstanceTreeUI.class.getDeclaredField("inside");
                f.setAccessible(true);
                v = (Boolean) f.get(this);
                f.set(this, Boolean.TRUE);
                ((MyTree) this.tree).setOverrideIsEnable(true);
            } catch (Throwable t) {
                //want to default back to substanceUI if this fails.
            }

            super.paintHorizontalPartOfLeg(g, bounds, insets, bounds, path, row, isLeaf, isLeaf, isLeaf);
            try {
                f.set(this, v);
                ((MyTree) this.tree).setOverrideIsEnable(true);
            } catch (Throwable t) {
                //see above
            }
        }
    }
//repeat for Vertical
}
