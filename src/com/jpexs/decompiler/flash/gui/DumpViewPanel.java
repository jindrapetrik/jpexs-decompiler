/*
 *  Copyright (C) 2010-2014 JPEXS
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author JPEXS
 */
public class DumpViewPanel extends JPanel {

    private final int bytesInRow = 16;
    private final JLabel dumpViewLabel;
    private final JTable dumpViewHexTable;
    private byte[] data;

    public DumpViewPanel() {
        super(new BorderLayout());

        dumpViewLabel = new JLabel();
        dumpViewLabel.setMinimumSize(new Dimension(100, 20));
        add(dumpViewLabel, BorderLayout.SOUTH);

        dumpViewHexTable = new JTable();
        dumpViewHexTable.setBackground(Color.white);
        dumpViewHexTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dumpViewHexTable.setTableHeader(new JTableHeader());
        dumpViewHexTable.setMaximumSize(new Dimension(200, 200));
        TableColumnModel columnModel = dumpViewHexTable.getColumnModel();
        columnModel.addColumn(new TableColumn());
        columnModel.getColumn(0).setMinWidth(100);
        for (int i = 0; i < bytesInRow; i++) {
            columnModel.addColumn(new TableColumn());
            columnModel.getColumn(i + 1).setWidth(20);
        }
        for (int i = 0; i < bytesInRow; i++) {
            columnModel.addColumn(new TableColumn());
            columnModel.getColumn(i + bytesInRow + 1).setWidth(10);
        }
        dumpViewHexTable.setShowHorizontalLines(false);
        dumpViewHexTable.setModel(new AbstractTableModel() {

            @Override
            public int getRowCount() {
                if (data == null) {
                    return 0;
                }
                int byteCount = data.length;
                int rowCount = byteCount / bytesInRow;
                if (byteCount + bytesInRow != 0) {
                    rowCount++;
                }
                return rowCount;
            }

            @Override
            public int getColumnCount() {
                return 2 * bytesInRow + 1;
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
                } else {
                    int pos = row * bytesInRow + column - bytesInRow - 1;
                    if (pos < data.length) {
                        return (char) data[pos];
                    }
                    return null;
                }
            }
        });
        add(new JScrollPane(dumpViewHexTable), BorderLayout.CENTER);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLabelText(String text) {
        dumpViewLabel.setText(text);
    }
}
