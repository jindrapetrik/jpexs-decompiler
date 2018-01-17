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

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JPEXS
 */
public class EachRowRendererEditor extends JTable {

    private static final long serialVersionUID = 1L;

    private Class editingClass;

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        editingClass = null;
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            Object value = getModel().getValueAt(row, modelColumn);
            Class rowClass = value == null ? String.class : value.getClass();
            return getDefaultRenderer(rowClass);
        } else {
            return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        editingClass = null;
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            Object value = getModel().getValueAt(row, modelColumn);
            editingClass = value == null ? String.class : value.getClass();
            return getDefaultEditor(editingClass);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    //  This method is also invoked by the editor when the value in the editor
    //  component is saved in the TableModel. The class was saved when the
    //  editor was invoked so the proper class can be created.
    @Override
    public Class getColumnClass(int column) {
        return editingClass != null ? editingClass : super.getColumnClass(column);
    }
}
