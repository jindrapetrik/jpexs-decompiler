/*
 * Copyright (C) 2021 JPEXS
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

import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

/**
 *
 * @author JPEXS
 */
public class FasterScrollPane extends JScrollPane {

    public FasterScrollPane(Component view) {
        super(view);
    }

    public FasterScrollPane() {
        super();
    }

    public FasterScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    public FasterScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    @Override
    public void setViewportView(Component view) {
        super.setViewportView(view);
        if (!(view instanceof Scrollable)) {
            getVerticalScrollBar().setUnitIncrement(20);
        }
    }

}
