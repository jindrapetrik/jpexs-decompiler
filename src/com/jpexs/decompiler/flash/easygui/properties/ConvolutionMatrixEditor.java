/*
 * Copyright (C) 2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author JPEXS
 */
public class ConvolutionMatrixEditor extends JPanel implements PropertyEditor {

    private final JLabel previewLabel;
    private final CONVOLUTIONFILTER filter;
    private List<ChangeListener> changeListeners = new ArrayList<>();

    private void updateLabel() {
        StringBuilder sb = new StringBuilder();
        sb.append(filter.matrixX);
        sb.append("x");
        sb.append(filter.matrixY);
        sb.append(" [");
        int length = filter.matrix.length;
        int i = 0;
        for (int y = 0; y < filter.matrixY; y++) {
            if (y > 0) {
                sb.append("; ");
            }
            for (int x = 0; x < filter.matrixX; x++) {
                if (x > 0) {
                    sb.append(", ");
                }
                sb.append(EcmaScript.toString(filter.matrix[i]));
                i++;
            }
        }
        sb.append("]");
        previewLabel.setText(sb.toString());
    }

    public ConvolutionMatrixEditor(CONVOLUTIONFILTER filter) {
        this.filter = filter;

        setLayout(new BorderLayout());

        previewLabel = new JLabel();

        updateLabel();

        previewLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JDialog dialog = new JDialog();
                    dialog.setUndecorated(true);
                    dialog.setResizable(false);
                    dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
                    MatrixEditorPanel matrixEditorPanel = new MatrixEditorPanel(dialog);
                    dialog.setContentPane(matrixEditorPanel);
                    dialog.pack();

                    Window window = SwingUtilities.getWindowAncestor(ConvolutionMatrixEditor.this);

                    dialog.setLocationRelativeTo(window);
                    Point loc = SwingUtilities.convertPoint(previewLabel, 0, 0, window);
                    if (loc.x + dialog.getWidth() > window.getWidth()) {
                        loc.x -= loc.x + dialog.getWidth() - window.getWidth();
                    }
                    if (loc.y + dialog.getHeight() > window.getHeight()) {
                        loc.y -= loc.y + dialog.getHeight() - window.getHeight();
                    }
                    SwingUtilities.convertPointToScreen(loc, window);

                    dialog.setLocation(loc);
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowDeactivated(WindowEvent e) {
                            matrixEditorPanel.stopEditing();
                            filter.matrixX = matrixEditorPanel.getMatrixX();
                            filter.matrixY = matrixEditorPanel.getMatrixY();
                            filter.matrix = matrixEditorPanel.getMatrix();
                            updateLabel();
                            dialog.setVisible(false);
                            for (ChangeListener l : changeListeners) {
                                l.change(ConvolutionMatrixEditor.this);
                            }
                        }
                    });
                    dialog.setVisible(true);
                }
            }
        });

        add(previewLabel, BorderLayout.WEST);

    }

    private class MatrixEditorPanel extends JPanel {

        private final JTable matrixTable;
        private final JDialog dialog;

        private final JButton enlargeVerticalButton;
        private final JButton shrinkVerticalButton;
        private final JButton enlargeHorizontalButton;
        private final JButton shrinkHorizontalButton;

        public int getMatrixX() {
            return matrixTable.getModel().getColumnCount();
        }

        public int getMatrixY() {
            return matrixTable.getModel().getRowCount();
        }

        public float[] getMatrix() {
            int cols = getMatrixX();
            int rows = getMatrixY();
            float[] matrix = new float[cols * rows];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    matrix[y * cols + x] = (float) matrixTable.getModel().getValueAt(y, x);
                }
            }
            return matrix;
        }
        
        public void stopEditing() {
            TableCellEditor editor = matrixTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }

        public MatrixEditorPanel(JDialog dialog) {
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            DefaultTableModel model = new DefaultTableModel(filter.matrixY, filter.matrixX) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return float.class;
                }
            };
            for (int y = 0; y < filter.matrixY; y++) {
                for (int x = 0; x < filter.matrixX; x++) {
                    model.setValueAt(filter.matrix[y * filter.matrixX + x], y, x);
                }
            }

            matrixTable = new JTable(model);

            matrixTable.setDefaultRenderer(float.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setText(EcmaScript.toString(value));
                    return label;
                }
            });

            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            
            matrixTable.setDefaultEditor(float.class, new TableCellEditor() {

                private List<CellEditorListener> listeners = new ArrayList<>();

                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                    textField.setText(EcmaScript.toString(value));
                    SwingUtilities.invokeLater(() -> textField.selectAll());
                    return textField;
                }

                @Override
                public Object getCellEditorValue() {
                    String input = textField.getText();
                    try {
                        return Float.valueOf(input);
                    } catch (NumberFormatException nfe) {
                        return 0.0f;
                    }
                }

                @Override
                public boolean isCellEditable(EventObject anEvent) {
                    return true;
                }

                @Override
                public boolean shouldSelectCell(EventObject anEvent) {
                    return true;
                }

                @Override
                public boolean stopCellEditing() {
                    List<CellEditorListener> listeners2 = new ArrayList<>(listeners);
                    for (CellEditorListener l : listeners2) {
                        l.editingStopped(new ChangeEvent(this));
                    }
                    return true;
                }

                @Override
                public void cancelCellEditing() {
                    List<CellEditorListener> listeners2 = new ArrayList<>(listeners);
                    for (CellEditorListener l : listeners2) {
                        l.editingCanceled(new ChangeEvent(this));
                    }
                }

                @Override
                public void addCellEditorListener(CellEditorListener l) {
                    listeners.add(l);
                }

                @Override
                public void removeCellEditorListener(CellEditorListener l) {
                    listeners.remove(l);
                }

            });

            matrixTable.setTableHeader(null);

            matrixTable.setUI(new BasicTableUI());

            if (View.isOceanic()) {
                matrixTable.setBackground(Color.WHITE);
            }

            matrixTable.setRowSelectionAllowed(false);
            matrixTable.setColumnSelectionAllowed(false);
            matrixTable.setCellSelectionEnabled(true);
            
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            JPanel buttonsVerticalPanel = new JPanel(new FlowLayout());
            enlargeVerticalButton = new JButton("+");
            enlargeVerticalButton.addActionListener(this::enlargeVerticalActionPerformed);
            enlargeVerticalButton.setPreferredSize(new Dimension(30, 30));
            enlargeVerticalButton.setMinimumSize(new Dimension(30, 30));
            shrinkVerticalButton = new JButton("-");
            shrinkVerticalButton.addActionListener(this::shrinkVerticalActionPerformed);
            shrinkVerticalButton.setPreferredSize(new Dimension(30, 30));
            shrinkVerticalButton.setMinimumSize(new Dimension(30, 30));
            buttonsVerticalPanel.add(enlargeVerticalButton);
            buttonsVerticalPanel.add(shrinkVerticalButton);

            JPanel buttonsHorizontalPanel = new JPanel();
            buttonsHorizontalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            buttonsHorizontalPanel.setLayout(new BoxLayout(buttonsHorizontalPanel, BoxLayout.Y_AXIS));
            enlargeHorizontalButton = new JButton("+");
            enlargeHorizontalButton.addActionListener(this::enlargeHorizontalActionPerformed);
            enlargeHorizontalButton.setPreferredSize(new Dimension(30, 30));
            enlargeHorizontalButton.setMinimumSize(new Dimension(30, 30));
            shrinkHorizontalButton = new JButton("-");
            shrinkHorizontalButton.addActionListener(this::shrinkHorizontalActionPerformed);
            shrinkHorizontalButton.setPreferredSize(new Dimension(30, 30));
            shrinkHorizontalButton.setMinimumSize(new Dimension(30, 30));
            buttonsHorizontalPanel.add(Box.createVerticalGlue());
            buttonsHorizontalPanel.add(enlargeHorizontalButton);
            buttonsHorizontalPanel.add(shrinkHorizontalButton);
            buttonsHorizontalPanel.add(Box.createVerticalGlue());

            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;
            gbc.gridy = 0;

            add(matrixTable, gbc);
            gbc.gridx++;
            add(buttonsHorizontalPanel, gbc);
            gbc.gridy = 1;
            gbc.gridx = 0;
            add(buttonsVerticalPanel, gbc);
            this.dialog = dialog;
        }

        private void updateDialogPosition() {
            Window window = SwingUtilities.getWindowAncestor(ConvolutionMatrixEditor.this);
            dialog.setLocationRelativeTo(window);
            Point loc = SwingUtilities.convertPoint(previewLabel, 0, 0, window);
            if (loc.x + dialog.getWidth() > window.getWidth()) {
                loc.x -= loc.x + dialog.getWidth() - window.getWidth();
            }
            if (loc.y + dialog.getHeight() > window.getHeight()) {
                loc.y -= loc.y + dialog.getHeight() - window.getHeight();
            }
            SwingUtilities.convertPointToScreen(loc, window);
            dialog.setLocation(loc);
        }

        private void enlargeVerticalActionPerformed(ActionEvent aev) {
            DefaultTableModel model = (DefaultTableModel) matrixTable.getModel();
            int columnCount = model.getColumnCount();
            int rowCount = model.getRowCount();
            model.setRowCount(rowCount + 2);

            for (int i = 0; i < columnCount; i++) {
                model.setValueAt(0f, rowCount, i);
                model.setValueAt(0f, rowCount + 1, i);
            }

            for (int y = rowCount - 1; y >= 0; y--) {
                for (int x = columnCount - 1; x >= 0; x--) {
                    model.setValueAt(model.getValueAt(y, x), y + 1, x);
                }
            }
            for (int i = 0; i < columnCount; i++) {
                model.setValueAt(0f, 0, i);
            }
            dialog.pack();
            updateDialogPosition();
            shrinkVerticalButton.setEnabled(true);
        }

        private void shrinkVerticalActionPerformed(ActionEvent aev) {
            DefaultTableModel model = (DefaultTableModel) matrixTable.getModel();

            int columnCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            for (int y = 0; y < rowCount - 2; y++) {
                for (int x = 0; x < columnCount; x++) {
                    model.setValueAt(model.getValueAt(y + 1, x), y, x);
                }
            }

            model.setRowCount(rowCount - 2);

            dialog.pack();
            shrinkVerticalButton.setEnabled(rowCount - 2 > 1);
            updateDialogPosition();
        }

        private void enlargeHorizontalActionPerformed(ActionEvent aev) {
            DefaultTableModel model = (DefaultTableModel) matrixTable.getModel();
            int columnCount = model.getColumnCount();
            int rowCount = model.getRowCount();
            model.setColumnCount(columnCount + 2);

            for (int i = 0; i < rowCount; i++) {
                model.setValueAt(0f, i, columnCount);
                model.setValueAt(0f, i, columnCount + 1);
            }

            for (int y = rowCount - 1; y >= 0; y--) {
                for (int x = columnCount - 1; x >= 0; x--) {
                    model.setValueAt(model.getValueAt(y, x), y, x + 1);
                }
            }
            for (int i = 0; i < rowCount; i++) {
                model.setValueAt(0f, i, 0);
            }
            dialog.pack();
            updateDialogPosition();
            shrinkHorizontalButton.setEnabled(true);
        }

        private void shrinkHorizontalActionPerformed(ActionEvent aev) {
            DefaultTableModel model = (DefaultTableModel) matrixTable.getModel();

            int columnCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            for (int y = 0; y < rowCount; y++) {
                for (int x = 0; x < columnCount - 2; x++) {
                    model.setValueAt(model.getValueAt(y, x + 1), y, x);
                }
            }

            model.setColumnCount(columnCount - 2);

            dialog.pack();

            shrinkHorizontalButton.setEnabled(columnCount - 2 > 1);
            updateDialogPosition();
        }
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public void reset() {

    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }
}
