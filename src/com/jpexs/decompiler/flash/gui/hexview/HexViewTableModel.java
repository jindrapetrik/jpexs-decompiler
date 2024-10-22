/*
 *  Copyright (C) 2010-2024 JPEXS
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
package com.jpexs.decompiler.flash.gui.hexview;

import javax.swing.table.AbstractTableModel;

/**
 * @author JPEXS
 */
public class HexViewTableModel extends AbstractTableModel {

    private byte[] data;

    private final int bytesInRow;

    public HexViewTableModel(int bytesInRow) {
        this.bytesInRow = bytesInRow;
    }

    @Override
    public int getRowCount() {
        if (data == null) {
            return 0;
        }
        int byteCount = data.length;
        int rowCount = byteCount / bytesInRow;
        if (byteCount % bytesInRow != 0) {
            rowCount++;
        }
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return 2 * bytesInRow + 1 + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Address";
        } else if (column <= bytesInRow) {
            return String.format("%01X", column - 1);
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return String.format("%08X", (long) row * bytesInRow);
        } else if (column <= bytesInRow) {
            int pos = row * bytesInRow + column - 1;
            if (pos < data.length) {
                return String.format("%02X", data[pos]);
            }
            return null;
        } else if (column == 1 + bytesInRow) {
            return null;
        } else {
            int pos = row * bytesInRow + column - bytesInRow - 1 - 1;
            if (pos < data.length) {
                return (char) (data[pos] & 0xff);
            }
            return null;
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void fireTableCellUpdated(int row, int column) {
        super.fireTableCellUpdated(row, column);
    }
}
