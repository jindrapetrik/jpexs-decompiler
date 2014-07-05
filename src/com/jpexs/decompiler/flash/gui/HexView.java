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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author JPEXS
 */
public class HexView extends JTable {
    
    private final int bytesInRow = 16;
    private long[] highlightStarts;
    private long[] highlightEnds;
    private byte[] data;
    private final String[] highlightColorsStr = new String[]{/*"EEEEEE", */"29AEC2", "9AC88C", "DF5F80", "EEA32E", "FFD200", "5E9B4C", "D3E976", "A3AEC2"};
    private final Color[] highlightColors;
    private Color bgColor = Color.decode("#F7F7F7");
    private Color bgColorAlternate = Color.decode("#EDEDED");

    public class HighlightCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            int level = -1;
            if (col > 0 && highlightStarts != null) {
                if(col!=bytesInRow+1){
                    int idx = row * bytesInRow + ((col > bytesInRow + 1) ? (col - bytesInRow - 2) : (col - 1));
                    for (int i = 0; i < highlightStarts.length; i++) {
                        if (highlightStarts[i] <= idx && highlightEnds[i] >= idx) {
                            level++;
                        } else {
                            break;
                        }
                    }
                }
            }

            if (level > -1) {
                l.setForeground(Color.white);
                l.setBackground(highlightColors[level % highlightColors.length]);
            } else {
                l.setForeground(Color.black);
                l.setBackground(row % 2 == 0 ? bgColor : bgColorAlternate);
            }

            return l;
        }
    }    

    public HexView() {
        highlightColors = new Color[highlightColorsStr.length];
        for (int i = 0; i < highlightColors.length; i++) {
            highlightColors[i] = Color.decode("#" + highlightColorsStr[i]);
        }
        
        setModel(new AbstractTableModel() {

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
                        return (char) data[pos];
                    }
                    return null;
                }
            }
        });

        setBackground(Color.white);
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setTableHeader(new JTableHeader());
        setMaximumSize(new Dimension(200, 200));
        
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);

        HighlightCellRenderer cellRenderer = new HighlightCellRenderer();
        TableColumn column = columnModel.getColumn(0);
        column.setMaxWidth(80);
        //column.setCellRenderer(cellRenderer);
        for (int i = 0; i < bytesInRow; i++) {
            column = columnModel.getColumn(i + 1);
            column.setMaxWidth(25);
            column.setCellRenderer(cellRenderer);
        }
        
        column = columnModel.getColumn(bytesInRow + 1);
        column.setMaxWidth(10);
        
        for (int i = 0; i < bytesInRow; i++) {
            column = columnModel.getColumn(i + bytesInRow + 1 + 1);
            column.setMaxWidth(10);
            column.setCellRenderer(cellRenderer);
        }
    }

    public void setData(byte[] data, long[] highlightStarts, long[] highlightEnds) {
        
        if ((highlightStarts == null) ^ (highlightEnds == null)) {
            throw new Error("highlightStarts and highlightEnds should be both null or not null.");
        }
        
        if (highlightStarts != null && highlightStarts.length != highlightEnds.length) {
            throw new Error("highlightStarts and highlightEnds should have the same number of elements.");
        }
        
        this.data = data;
        this.highlightStarts = highlightStarts;
        this.highlightEnds = highlightEnds;
    }
    
    public void scrollToByte(long byteNum) {
        int row = (int) (byteNum / bytesInRow);

        //final int pageSize = (int) (getParent().getSize().getHeight() / getRowHeight());
        getSelectionModel().setSelectionInterval(row, row);
        scrollRectToVisible(new Rectangle(getCellRect(row, 0, true)));            
    }
    
    public void scrollToByte(long[] byteNumStarts, long[] byteNumEnds) {
        for (int i = 0; i < byteNumStarts.length; i++) {
            scrollToByte(byteNumStarts[i]);
            scrollToByte(byteNumEnds[i]);
            scrollToByte(byteNumStarts[i]);
        }
    }
}
