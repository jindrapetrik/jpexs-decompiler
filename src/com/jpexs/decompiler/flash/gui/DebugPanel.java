/*
 * Copyright (C) 2015 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.decompiler.flash.gui.DebuggerHandler.BreakListener;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author JPEXS
 */
public class DebugPanel extends JPanel {

    private JTable debugRegistersTable;

    private JTable debugLocalsTable;

    private JTable debugScopeTable;

    private JTable callStackTable;

    private JTable stackTable;

    private JTabbedPane varTabs;

    private BreakListener listener;

    private JTextArea traceLogTextarea;

    private int logLength = 0;

    private List<SelectedTab> tabTypes = new ArrayList<>();

    private boolean loading = false;

    public static enum SelectedTab {

        LOG, STACK, SCOPECHAIN, LOCALS, REGISTERS, CALLSTACK
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    public synchronized void setLoading(boolean loading) {
        this.loading = loading;
    }

    private SelectedTab selectedTab = null;

    public DebugPanel() {
        super(new BorderLayout());
        debugRegistersTable = new JTable(new ABCPanel.VariablesTableModel(new ArrayList<>()));
        debugLocalsTable = new JTable(new ABCPanel.VariablesTableModel(new ArrayList<>()));
        debugScopeTable = new JTable(new ABCPanel.VariablesTableModel(new ArrayList<>()));
        callStackTable = new JTable();
        stackTable = new JTable();
        traceLogTextarea = new JTextArea();
        traceLogTextarea.setEditable(false);
        traceLogTextarea.setOpaque(false);
        traceLogTextarea.setFont(new JLabel().getFont());
        traceLogTextarea.setBackground(Color.white);

        Main.getDebugHandler().addTraceListener(new DebuggerHandler.TraceListener() {

            @Override
            public void trace(String... val) {
                for (String s : val) {
                    String add = "trace: " + s + "\r\n";
                    boolean wasEmpty = logLength == 0;
                    logLength += add.length();
                    traceLogTextarea.append(add);
                    try {
                        traceLogTextarea.setCaretPosition(logLength);
                    } catch (IllegalArgumentException iex) {
                        //ignore
                    }
                    if (wasEmpty) {
                        refresh();
                    }
                }
            }
        });

        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {

            @Override
            public void connected() {
            }

            @Override
            public void disconnected() {
                refresh();
            }
        });

        Main.getDebugHandler().addBreakListener(listener = new DebuggerHandler.BreakListener() {

            @Override
            public void doContinue() {
                View.execInEventDispatch(new Runnable() {

                    @Override
                    public void run() {
                        refresh();
                    }

                });
            }

            @Override
            public void breakAt(String scriptName, int line, int classIndex, int traitIndex, int methodIndex) {
                View.execInEventDispatch(new Runnable() {

                    @Override
                    public void run() {
                        refresh();
                    }
                });

            }
        });

        varTabs = new JTabbedPane();
        varTabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == varTabs) {
                    if (isLoading()) {
                        return;
                    }
                    synchronized (DebugPanel.this) {
                        int si = varTabs.getSelectedIndex();
                        if (si > -1 && si < tabTypes.size()) {
                            selectedTab = tabTypes.get(si);
                        }
                    }
                }
            }
        });

        add(new HeaderLabel(AppStrings.translate("debugpanel.header")), BorderLayout.NORTH);
        add(varTabs, BorderLayout.CENTER);
    }

    public void refresh() {

        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                setLoading(true);
                synchronized (DebugPanel.this) {

                    SelectedTab oldSel = selectedTab;
                    InFrame f = Main.getDebugHandler().getFrame();
                    if (f != null) {
                        debugRegistersTable.setModel(new ABCPanel.VariablesTableModel(f.registers));
                        List<Variable> locals = new ArrayList<>();
                        locals.addAll(f.arguments);
                        locals.addAll(f.variables);
                        debugLocalsTable.setModel(new ABCPanel.VariablesTableModel(locals));
                        debugScopeTable.setModel(new ABCPanel.VariablesTableModel(f.scopeChain));
                    } else {
                        debugRegistersTable.setModel(new DefaultTableModel());
                        debugLocalsTable.setModel(new DefaultTableModel());
                        debugScopeTable.setModel(new DefaultTableModel());
                    }
                    InBreakAtExt info = Main.getDebugHandler().getBreakInfo();
                    if (info != null) {
                        //InBreakReason reason = Main.getDebugHandler().getBreakReason();
                        List<String> callStackFiles = new ArrayList<>();
                        List<Integer> callStackLines = new ArrayList<>();

                        callStackFiles.add(Main.getDebugHandler().moduleToString(info.file));
                        callStackLines.add(info.line);

                        for (int i = 0; i < info.files.size(); i++) {
                            callStackFiles.add(Main.getDebugHandler().moduleToString(info.files.get(i)));
                            callStackLines.add(info.lines.get(i));
                        }
                        Object[][] data = new Object[callStackFiles.size()][2];
                        for (int i = 0; i < callStackFiles.size(); i++) {
                            data[i][0] = callStackFiles.get(i);
                            data[i][1] = callStackLines.get(i);
                        }

                        DefaultTableModel tm = new DefaultTableModel(data, new Object[]{
                            AppStrings.translate("callStack.header.file"),
                            AppStrings.translate("callStack.header.line")
                        });
                        callStackTable.setModel(tm);

                        Object[][] data2 = new Object[info.stacks.size()][1];
                        for (int i = 0; i < info.stacks.size(); i++) {
                            data2[i][0] = info.stacks.get(i);
                        }
                        stackTable.setModel(new DefaultTableModel(data2, new Object[]{AppStrings.translate("stack.header.item")}));
                    } else {
                        callStackTable.setModel(new DefaultTableModel());
                        stackTable.setModel(new DefaultTableModel());
                    }

                    varTabs.removeAll();
                    tabTypes.clear();
                    JPanel pa;
                    if (debugRegistersTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.REGISTERS);
                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(debugRegistersTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.registers"), pa);
                    }
                    if (debugLocalsTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.LOCALS);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(debugLocalsTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.locals"), pa);
                    }

                    if (debugScopeTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.SCOPECHAIN);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(debugScopeTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.scopeChain"), pa);
                    }

                    if (callStackTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.CALLSTACK);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(callStackTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("callStack.header"), pa);
                    }
                    if (stackTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.STACK);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(stackTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("stack.header"), pa);
                    }
                    if (logLength > 0) {
                        tabTypes.add(SelectedTab.LOG);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(traceLogTextarea), BorderLayout.CENTER);
                        JButton clearButton = new JButton(AppStrings.translate("debuglog.button.clear"));
                        clearButton.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                traceLogTextarea.setText("");
                                logLength = 0;
                                refresh();
                            }
                        });
                        JPanel butPanel = new JPanel(new FlowLayout());
                        butPanel.add(clearButton);
                        pa.add(butPanel, BorderLayout.SOUTH);
                        varTabs.addTab(AppStrings.translate("debuglog.header"), pa);
                    }
                    boolean newVisible = !tabTypes.isEmpty();
                    if (newVisible != isVisible()) {
                        setVisible(newVisible);
                    }
                    if (!tabTypes.isEmpty()) {
                        if (oldSel != null && !tabTypes.contains(oldSel)) {
                            oldSel = null;
                        }
                    }
                    if (oldSel != null) {
                        selectedTab = oldSel;
                        varTabs.setSelectedIndex(tabTypes.indexOf(selectedTab));
                    }
                    setLoading(false);
                }

            }
        });

    }

    public void dispose() {
        Main.getDebugHandler().removeBreakListener(listener);
    }
}
