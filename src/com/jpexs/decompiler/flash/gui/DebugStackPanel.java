/*
 *  Copyright (C) 2023-2025 JPEXS
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

import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.decompiler.flash.SWF;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * @author JPEXS
 */
public class DebugStackPanel extends JPanel {

    private JTable stackTable;

    private boolean active = false;

    private int depth = 0;

    private String[] swfHashes = new String[0];
    private int[] classIndices = new int[0];
    private int[] methodIndices = new int[0];
    private int[] traitIndices = new int[0];

    public DebugStackPanel() {
        stackTable = new JTable();
        Main.getDebugHandler().addFrameChangeListener(new DebuggerHandler.FrameChangeListener() {
            @Override
            public void frameChanged() {
                depth = Main.getDebugHandler().getDepth();
                refresh();
            }
        });
        Main.getDebugHandler().addBreakListener(new DebuggerHandler.BreakListener() {
            @Override
            public void breakAt(String scriptName, int line, int classIndex, int traitIndex, int methodIndex) {

            }

            @Override
            public void doContinue() {
                clear();
            }
        });

        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {
            @Override
            public void connected() {
            }

            @Override
            public void disconnected() {
                clear();
            }
        });

        //JLabel titleLabel = new JLabel(AppStrings.translate("callStack.header"), JLabel.CENTER);
        setLayout(new BorderLayout());
        //add(titleLabel, BorderLayout.NORTH);
        add(new FasterScrollPane(stackTable), BorderLayout.CENTER);

        stackTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = stackTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String swfHash = swfHashes[row];
                        String scriptName = (String) stackTable.getModel().getValueAt(row, 1);
                        int line = (int) (Integer) stackTable.getModel().getValueAt(row, 2);
                        SWF swf = swfHash == null ? Main.getRunningSWF() : Main.getSwfByHash(swfHash);
                        Main.getMainFrame().getPanel().gotoScriptLine(swf,
                                scriptName, line, classIndices[row], traitIndices[row], methodIndices[row], Main.isDebugPCode());
                        Main.getDebugHandler().setDepth(row);
                    }
                }
            }
        });
    }

    public void clear() {
        stackTable.setModel(new DefaultTableModel());
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void refresh() {
        InBreakAtExt info = Main.getDebugHandler().getBreakInfo();
        if (info == null) {
            clear();
            return;
        }
        active = true;
        Object[][] data = new Object[info.files.size()][4];
        String[] newSwfHashes = new String[info.files.size()];
        int[] newClassIndices = new int[info.files.size()];
        int[] newMethodIndices = new int[info.files.size()];
        int[] newTraitIndices = new int[info.files.size()];
        for (int i = 0; i < info.files.size(); i++) {
            int f = info.files.get(i);
            String moduleName = Main.getDebugHandler().moduleToString(f);
            String swfHash = null;
            if (moduleName.contains(":")) {
                swfHash = moduleName.substring(0, moduleName.indexOf(":"));
                moduleName = moduleName.substring(moduleName.indexOf(":") + 1);
            }
            newSwfHashes[i] = swfHash;
            data[i][0] = swfHash == null ? "unknown" : Main.getSwfByHash(swfHash).toString();
            data[i][1] = moduleName;
            data[i][2] = info.lines.get(i);
            data[i][3] = info.stacks.get(i);
            Integer newClassIndex = Main.getDebugHandler().moduleToClassIndex(f);
            newClassIndices[i] = newClassIndex == null ? -1 : newClassIndex;
            Integer newMethodIndex = Main.getDebugHandler().moduleToMethodIndex(f);
            newMethodIndices[i] = newMethodIndex == null ? -1 : newMethodIndex;
            Integer newTraitIndex = Main.getDebugHandler().moduleToTraitIndex(f);
            ;
            newTraitIndices[i] = newTraitIndex == null ? -1 : newTraitIndex;
        }

        DefaultTableModel tm = new DefaultTableModel(data, new Object[]{
            AppStrings.translate("callStack.header.swf"),
            AppStrings.translate("callStack.header.file"),
            AppStrings.translate("callStack.header.line"),
            AppStrings.translate("stack.header.item")
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
        stackTable.setModel(tm);
        this.swfHashes = newSwfHashes;
        this.classIndices = newClassIndices;
        this.methodIndices = newMethodIndices;
        this.traitIndices = newTraitIndices;

        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                TableCellRenderer renderer = new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (row == depth) {
                            label.setFont(label.getFont().deriveFont(Font.BOLD));
                        }
                        return label;
                    }

                };
                stackTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
                stackTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
                stackTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
                repaint();
            }
        });
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
