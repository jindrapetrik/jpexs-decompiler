/*
 *  Copyright (C) 2022-2025 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * @author JPEXS
 */
public class BreakpointListDialog extends AppDialog {

    private SWF swf;
    private JTable table = new JTable();
    private List<Breakpoint> breakpointList = new ArrayList<>();
    private Timer refreshTimer = null;
    private static final int REFRESH_TIMEOUT = 1000;

    private class Breakpoint {

        public String scriptName;
        public int line;

        public Breakpoint(String scriptName, int line) {
            this.scriptName = scriptName;
            this.line = line;
        }
    }

    public BreakpointListDialog(Window owner, SWF swf) {
        super(owner);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title") + " - " + swf.getShortFileName());
        this.swf = swf;
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (table.getSelectedRow() != -1 && row != -1) {
                        gotoButtonActionPerformed(null);
                    }
                }
            }
        });
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        JButton gotoButton = new JButton(translate("button.goto"));
        gotoButton.addActionListener(this::gotoButtonActionPerformed);
        buttonsPanel.add(gotoButton);

        JButton removeButton = new JButton(translate("button.remove"));
        removeButton.addActionListener(this::removeButtonActionPerformed);
        buttonsPanel.add(removeButton);

        JButton removeAllButton = new JButton(translate("button.removeAll"));
        removeAllButton.addActionListener(this::removeAllButtonActionPerformed);
        buttonsPanel.add(removeAllButton);

        JButton closeButton = new JButton(translate("button.close"));
        closeButton.addActionListener(this::closeButtonActionPerformed);
        buttonsPanel.add(closeButton);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        refresh();

        setSize(500, 300);

        //View.setWindowIcon(this);
        List<Image> images = new ArrayList<>();
        images.add(View.loadImage("breakpointlist16"));
        setIconImages(images);
        View.centerScreen(this);
    }

    private Breakpoint getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return null;
        }
        return breakpointList.get(row);
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        Breakpoint breakpoint = getSelected();
        if (breakpoint == null) {
            return;
        }
        Main.getDebugHandler().removeBreakPoint(swf, breakpoint.scriptName, breakpoint.line);
        refreshMarkers();
        refresh();
    }

    private void refreshMarkers() {
        if (swf.isAS3()) {
            Main.getMainFrame().getPanel().getABCPanel().decompiledTextArea.refreshMarkers();
            Main.getMainFrame().getPanel().getABCPanel().detailPanel.methodTraitPanel.methodCodePanel.refreshMarkers();
        } else {
            Main.getMainFrame().getPanel().getActionPanel().decompiledEditor.refreshMarkers();
            Main.getMainFrame().getPanel().getActionPanel().editor.refreshMarkers();
        }
    }

    private void removeAllButtonActionPerformed(ActionEvent evt) {
        Main.getDebugHandler().clearBreakPoints(swf);
        refreshMarkers();
        refresh();
    }

    private void gotoButtonActionPerformed(ActionEvent evt) {
        Breakpoint breakpoint = getSelected();
        if (breakpoint == null) {
            return;
        }
        int classIndex = -1;
        int traitIndex = -1;
        int methodIndex = -1;
        /*if (Main.getDebugHandler().getDebuggedSwf() == swf) {        
            int f = Main.getDebugHandler().moduleIdOf(breakpoint.scriptName);
            if (f != -1) {
                Integer newClassIndex = Main.getDebugHandler().moduleToClassIndex(f);
                classIndex = newClassIndex == null ? -1 : newClassIndex;
                Integer newMethodIndex = Main.getDebugHandler().moduleToMethodIndex(f);
                methodIndex = newMethodIndex == null ? -1 : newMethodIndex;
                Integer newTraitIndex = Main.getDebugHandler().moduleToTraitIndex(f);;
                traitIndex = newTraitIndex == null ? -1 : newTraitIndex;
            }
        }*/
        Pattern abcPcodePattern = Pattern.compile("^#PCODE abc:(?<abc>[0-9]+),body:(?<body>[0-9]+);.*");
        Matcher m = abcPcodePattern.matcher(breakpoint.scriptName);
        if (m.matches()) {
            int abcIndex = Integer.parseInt(m.group("abc"));
            int bodyIndex = Integer.parseInt(m.group("body"));
            ABC abc = swf.getAbcList().get(abcIndex).getABC();
            methodIndex = abc.bodies.get(bodyIndex).method_info;
        }

        Main.getMainFrame().getPanel().gotoScriptLine(swf, breakpoint.scriptName, breakpoint.line, classIndex, traitIndex, methodIndex, breakpoint.scriptName.startsWith("#PCODE"));
    }

    public void refresh() {
        DefaultTableModel defaultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        defaultTableModel.addColumn(translate("breakpoint.scriptName"));
        defaultTableModel.addColumn(translate("breakpoint.line"));
        defaultTableModel.addColumn(translate("breakpoint.status"));

        Map<String, Set<Integer>> breakpoints = Main.getDebugHandler().getAllBreakPoints(swf, false);

        List<Breakpoint> newBreakpointList = new ArrayList<>();
        for (String scriptName : breakpoints.keySet()) {
            for (int line : breakpoints.get(scriptName)) {
                newBreakpointList.add(new Breakpoint(scriptName, line));
                String status = "unknown";
                if (Main.getDebugHandler().isBreakpointInvalid(swf, scriptName, line)) {
                    status = "invalid";
                } else if (Main.getDebugHandler().isBreakpointConfirmed(swf, scriptName, line)) {
                    status = "confirmed";
                }
                defaultTableModel.addRow(new Object[]{scriptName, line, translate("breakpoint.status." + status)});
            }
        }
        breakpointList = newBreakpointList;
        int selectedRow = table.getSelectedRow();
        table.setModel(defaultTableModel);
        if (selectedRow >= 0 && selectedRow < defaultTableModel.getRowCount()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(75);
    }

    @Override
    public void setVisible(boolean b) {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
        if (b) {
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    View.execInEventDispatch(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                }

            }, REFRESH_TIMEOUT, REFRESH_TIMEOUT);
        }
        super.setVisible(b);
    }
}
