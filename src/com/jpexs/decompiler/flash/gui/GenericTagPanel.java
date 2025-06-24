/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Old Generic Tag editor
 *
 * @author JPEXS
 */
public abstract class GenericTagPanel extends JPanel {

    protected final MainPanel mainPanel;

    public GenericTagPanel(MainPanel mainPanel) {
        super(new BorderLayout());

        this.mainPanel = mainPanel;
    }

    public abstract void clear();

    public abstract void setEditMode(boolean edit, Tag tag);

    public abstract boolean tryAutoSave();

    public abstract boolean save();

    public abstract Tag getTag();

    //@Override
    //public abstract void change(GenericTagEditor ed);
}
