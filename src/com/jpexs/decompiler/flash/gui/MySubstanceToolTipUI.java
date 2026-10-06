/*
 * Copyright (C) 2026 JPEXS
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

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.substance.internal.ui.SubstanceToolTipUI;

/**
 *
 * @author JPEXS
 */
public class MySubstanceToolTipUI extends SubstanceToolTipUI {

    public static ComponentUI createUI(JComponent comp) {
        return new MySubstanceToolTipUI();
    }    
    
    @Override
    public Dimension getPreferredSize(JComponent component) {
        Dimension size = super.getPreferredSize(component);
        size.width += 5;
        return size;
    }

}
