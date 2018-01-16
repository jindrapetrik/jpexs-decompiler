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
package com.jpexs.decompiler.flash.gui.hexview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author JPEXS
 */
public class HexView extends JTable {

    private static final int bytesInRow = 16;

    private long[] highlightStarts;

    private long[] highlightEnds;

    private final String[] highlightColorsStr = new String[]{/*"EEEEEE", */"29AEC2", "9AC88C", "DF5F80", "EEA32E", "FFD200", "5E9B4C", "D3E976", "A3AEC2"};

    private final Color[] highlightColors;

    private final Color bgColor = Color.decode("#F7F7F7");

    private final Color bgColorAlternate = Color.decode("#EDEDED");

    private int mouseOverIdx = -1;

    private int selectionStart = -1;

    private int selectionEnd = -1;

    private HexViewListener listener;

    private class HighlightCellRenderer extends DefaultTableCellRenderer {

        public int byteIndex;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            int level = -1;
            int idx = getIdxByColAndRow(row, col);
            if (highlightStarts != null) {
                byteIndex = idx;
                for (int i = 0; i < highlightStarts.length; i++) {
                    if (highlightStarts[i] <= idx && highlightEnds[i] >= idx) {
                        level++;
                    } else {
                        break;
                    }
                }
            }

            Color foreground;
            Color background;
            if (level > -1) {
                foreground = Color.white;
                background = highlightColors[level % highlightColors.length];
            } else {
                foreground = Color.black;
                background = row % 2 == 0 ? bgColor : bgColorAlternate;
            }

            if (idx != -1 && (idx == mouseOverIdx
                    || (idx >= selectionStart && idx <= selectionEnd))) {
                foreground = new Color(255 - foreground.getRed(), 255 - foreground.getGreen(), 255 - foreground.getBlue());
                background = new Color(255 - background.getRed(), 255 - background.getGreen(), 255 - background.getBlue());
            }

            l.setForeground(foreground);
            l.setBackground(background);

            return l;
        }
    }

    private class HexViewSelectionListener implements ListSelectionListener {

        private final HexView table;

        public HexViewSelectionListener(HexView table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {

            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();

            int idx = getIdxByColAndRow(row, col);
            if (listener != null) {
                listener.byteValueChanged(idx, idx == -1 ? 0 : getModel().getData()[idx]);
            }
        }
    }

    private class HexViewMouseAdapter extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            HexView table = (HexView) e.getSource();
            Point point = e.getPoint();
            int col = table.columnAtPoint(point);
            int row = table.rowAtPoint(point);
            mouseOverIdx = -1;
            getModel().fireTableCellUpdated(row, col);
            if (listener != null) {
                listener.byteMouseMoved(-1, (byte) 0);
            }
        }
    }

    private class HexViewMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            HexView table = (HexView) e.getSource();
            Point point = e.getPoint();
            int col = table.columnAtPoint(point);
            int row = table.rowAtPoint(point);
            int idx = getIdxByColAndRow(row, col);
            mouseOverIdx = idx;
            getModel().fireTableCellUpdated(row, col);

            if (listener != null) {
                listener.byteMouseMoved(idx, idx == -1 ? 0 : getModel().getData()[idx]);
            }
        }
    }

    public HexView() {
        super(new HexViewTableModel(bytesInRow));
        highlightColors = new Color[highlightColorsStr.length];
        for (int i = 0; i < highlightColors.length; i++) {
            highlightColors[i] = Color.decode("#" + highlightColorsStr[i]);
        }

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

        addMouseListener(new HexViewMouseAdapter());
        addMouseMotionListener(new HexViewMouseMotionAdapter());
        ListSelectionModel rowSelModel = getSelectionModel();
        ListSelectionModel colSelModel = getColumnModel().getSelectionModel();
        ListSelectionListener selectionListener = new HexViewSelectionListener(this);
        rowSelModel.addListSelectionListener(selectionListener);
        colSelModel.addListSelectionListener(selectionListener);
    }

    @Override
    public HexViewTableModel getModel() {
        TableModel model = super.getModel();
        return (HexViewTableModel) model;
    }

    public void setData(byte[] data, long[] highlightStarts, long[] highlightEnds) {

        if ((highlightStarts == null) ^ (highlightEnds == null)) {
            throw new Error("highlightStarts and highlightEnds should be both null or not null.");
        }

        if (highlightStarts != null && highlightStarts.length != highlightEnds.length) {
            throw new Error("highlightStarts and highlightEnds should have the same number of elements.");
        }

        getModel().setData(data);
        this.highlightStarts = highlightStarts;
        this.highlightEnds = highlightEnds;
    }

    public byte[] getData() {
        return getModel().getData();
    }

    public void selectByte(long byteNum) {
        scrollToByte(byteNum);
        listener.byteValueChanged((int) byteNum, getData()[(int) byteNum]);
    }

    public void selectBytes(long byteNum, int length) {
        selectionStart = (int) byteNum;
        selectionEnd = (int) (byteNum + length - 1);
        scrollToByte(new long[]{byteNum}, new long[]{byteNum + length - 1});
        listener.byteValueChanged((int) byteNum, getData()[(int) byteNum]);
        getModel().fireTableDataChanged();
    }

    public void clearSelectedBytes() {
        selectionStart = -1;
        selectionEnd = -1;
        getModel().fireTableDataChanged();
    }

    public void scrollToByte(long byteNum) {

        int row = (int) (byteNum / bytesInRow);

        //final int pageSize = (int) (getParent().getSize().getHeight() / getRowHeight());
        getSelectionModel().setSelectionInterval(row, row);
        scrollRectToVisible(new Rectangle(getCellRect(row, 0, true)));
    }

    private int getIdxByColAndRow(int row, int col) {
        int idx = -1;
        if (row < 0 || col < 0) {
            return -1;
        }
        if (col > 0 && col != bytesInRow + 1) {
            idx = row * bytesInRow + ((col > bytesInRow + 1) ? (col - bytesInRow - 2) : (col - 1));
        }
        byte[] data = getModel().getData();
        if (idx >= data.length) {
            idx = -1;
        }
        return idx;
    }

    public int getFocusedByteIdx() {
        int col = getSelectedColumn();
        int row = getSelectedRow();

        int idx = getIdxByColAndRow(row, col);
        return idx;
    }

    public void scrollToByte(long[] byteNumStarts, long[] byteNumEnds) {
        for (int i = 0; i < byteNumStarts.length; i++) {
            scrollToByte(byteNumStarts[i]);
            scrollToByte(byteNumEnds[i]);
            scrollToByte(byteNumStarts[i]);
        }
    }

    public void addListener(HexViewListener listener) {
        this.listener = listener;
    }
}
