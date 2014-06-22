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

import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
    private DumpInfo[] dumpInfos;
    private final String[] highlightColorsStr = new String[]{/*"EEEEEE", */"29AEC2", "9AC88C", "DF5F80", "EEA32E", "FFD200", "5E9B4C", "D3E976", "A3AEC2"};
    private final Color[] highlightColors;

    public class HighlightCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            int idx = row * bytesInRow + col - 1;
            int level = -1;
            for (int i = 0; i < dumpInfos.length; i++) {
                DumpInfo di = dumpInfos[i];
                if (di.startByte <= idx && getEndIndex(di) >= idx) {
                    level++;
                } else {
                    break;
                }
            }
            if (level > -1) {
                l.setBackground(highlightColors[level % highlightColors.length]);
            } else {
                l.setBackground(Color.white);
            }
            return l;
        }
    }    
    
    public DumpViewPanel() {
        super(new BorderLayout());

        highlightColors = new Color[highlightColorsStr.length];
        for (int i = 0; i < highlightColors.length; i++) {
            highlightColors[i] = Color.decode("#" + highlightColorsStr[i]);
        }
        
        dumpViewLabel = new JLabel();
        dumpViewLabel.setMinimumSize(new Dimension(100, 20));
        add(dumpViewLabel, BorderLayout.SOUTH);

        dumpViewHexTable = new JTable();
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

        dumpViewHexTable.setBackground(Color.white);
        dumpViewHexTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dumpViewHexTable.setTableHeader(new JTableHeader());
        dumpViewHexTable.setMaximumSize(new Dimension(200, 200));
        
        dumpViewHexTable.setShowHorizontalLines(false);
        dumpViewHexTable.setShowVerticalLines(false);

        HighlightCellRenderer cellRenderer = new HighlightCellRenderer();
        TableColumnModel columnModel = dumpViewHexTable.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(80);
        for (int i = 0; i < bytesInRow; i++) {
            TableColumn column = columnModel.getColumn(i + 1);
            column.setMaxWidth(25);
            column.setCellRenderer(cellRenderer);
        }
        for (int i = 0; i < bytesInRow; i++) {
            TableColumn column = columnModel.getColumn(i + bytesInRow + 1);
            column.setMaxWidth(10);
        }

        add(new JScrollPane(dumpViewHexTable), BorderLayout.CENTER);
    }

    public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

        table.scrollRectToVisible(rect);

        // Scroll the area into view
        //viewport.scrollRectToVisible(rect);
    }
    
    private int getEndIndex(DumpInfo dumpInfo) {
        int end = (int) dumpInfo.startByte;
        if (dumpInfo.lengthBytes != 0) {
            end += dumpInfo.lengthBytes;
        } else {
            int bits = dumpInfo.startBit + dumpInfo.lengthBits;
            end += bits / 8;
            if (bits % 8 != 0) {
                end++;
            }
        }
        return end - 1;
    }
    
    public void setData(byte[] data, DumpInfo dumpInfo) {
        this.data = data;
        List<DumpInfo> dumpInfos = new ArrayList<>();
        DumpInfo di = dumpInfo;
        while (di.parent != null) {
            dumpInfos.add(di);
            di = di.parent;
        }
        DumpInfo[] dumpInfos1 = new DumpInfo[dumpInfos.size()];
        for (int i = 0; i < dumpInfos1.length; i++) {
            dumpInfos1[i] = dumpInfos.get(dumpInfos1.length - i - 1);
        }
        this.dumpInfos = dumpInfos1;

        if (dumpInfo.lengthBytes != 0 || dumpInfo.lengthBits != 0) {
            int selectionStart = (int) dumpInfo.startByte;
            int selectionEnd = getEndIndex(dumpInfo);
            //setSelectedRange(selectionStart, end - 1);
            setLabelText("startByte: " + dumpInfo.startByte
                    + " startBit: " + dumpInfo.startBit
                    + " lengthBytes: " + dumpInfo.lengthBytes
                    + " lengthBits: " + dumpInfo.lengthBits
                    + " selectionStart: " + selectionStart
                    + " selectionEnd: " + selectionEnd);
        }
        
        scrollToVisible(dumpViewHexTable, (int) (dumpInfo.startByte / bytesInRow), 0);
        
        repaint();
    }

    public void setLabelText(String text) {
        dumpViewLabel.setText(text);
    }
}
