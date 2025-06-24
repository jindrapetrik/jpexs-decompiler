/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import de.javagl.treetable.JTreeTable;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author JPEXS
 */
class CharacterTagTransferHandler extends TransferHandler {

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTreeTable table = (JTreeTable) c;
        int selectedRow = table.getSelectedRow();

        if (selectedRow >= 0) {
            DefaultMutableTreeNode mn = (DefaultMutableTreeNode) table.getModel().getValueAt(selectedRow, 0);
            Object o = mn.getUserObject();
            if (
                    (o instanceof DefineSpriteTag)
                    || (o instanceof ShapeTag)
                    || (o instanceof TextTag)
                    || (o instanceof ButtonTag)
                ) {
                return new CharacterTagTransferable((CharacterTag) o);
            }
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
