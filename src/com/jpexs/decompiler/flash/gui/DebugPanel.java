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

import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InConstantPool;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.decompiler.flash.gui.DebuggerHandler.BreakListener;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import de.hameister.treetable.MyTreeTable;
import de.hameister.treetable.MyTreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class DebugPanel extends JPanel {

    private MyTreeTable debugRegistersTable;

    private MyTreeTable debugLocalsTable; //JTable debugLocalsTable;

    private MyTreeTable debugScopeTable;

    private JTable callStackTable;

    private JTable stackTable;

    private JTable constantPoolTable;

    private JTabbedPane varTabs;

    private BreakListener listener;

    private JTextArea traceLogTextarea;

    private int logLength = 0;

    private List<SelectedTab> tabTypes = new ArrayList<>();

    private boolean loading = false;

    public ABCPanel.VariablesTableModel localsTable;

    public static enum SelectedTab {

        LOG, STACK, SCOPECHAIN, LOCALS, REGISTERS, CALLSTACK, CONSTANTPOOL
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    public synchronized void setLoading(boolean loading) {
        this.loading = loading;
    }

    private SelectedTab selectedTab = null;

    private void safeSetTreeModel(MyTreeTable tt, MyTreeTableModel tmodel) {
        List<List<String>> expanded = View.getExpandedNodes(tt.getTree());

        int[] selRows = tt.getSelectedRows();

        TreePath[] selPaths = new TreePath[selRows.length];
        for (int i = 0; i < selRows.length; i++) {
            selPaths[i] = tt.getTree().getPathForRow(selRows[i]);
        }
        tt.setTreeModel(tmodel);
        //tt.getTree().setRootVisible(false);

        View.expandTreeNodes(tt.getTree(), expanded);
        for (int i = 0; i < selRows.length; i++) {
            selRows[i] = tt.getTree().getRowForPath(selPaths[i]);
            if (selRows[i] == -1) {
                continue;
            }
            if (i == 0) {
                tt.setRowSelectionInterval(selRows[i], selRows[i]);
            } else {
                tt.addRowSelectionInterval(selRows[i], selRows[i]);
            }
        }

        int ROW_HEIGHT = new JLabel("A").getPreferredSize().height;

        JTree tree = tt.getTree();
        tree.setRowHeight(ROW_HEIGHT);
        for (int i = 0; i < tt.getRowCount(); i++) {
            tt.setRowHeight(i, ROW_HEIGHT);
        }

    }

    public DebugPanel() {
        super(new BorderLayout());
        debugRegistersTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugRegistersTable, new ArrayList<>(), new ArrayList<>()), false);
        debugLocalsTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugLocalsTable, new ArrayList<>(), new ArrayList<>()), false);

        MouseAdapter watchHandler = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    dopop(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    dopop(e);
                }
            }

            private void dopop(MouseEvent e) {
                if (debugLocalsTable.getSelectedRow() == -1) {
                    return;
                }
                Object node = debugLocalsTable.getTree().getPathForRow(debugLocalsTable.getSelectedRow()).getLastPathComponent();
                Variable v;
                ABCPanel.VariableNode vn;
                if (node instanceof ABCPanel.VariableNode) {
                    vn = ((ABCPanel.VariableNode) node);
                    v = vn.var;
                } else {
                    return;
                }

                JPopupMenu pm = new JPopupMenu();
                //TODO!!
                /*if (v.typeName != null && v.typeName.startsWith("flash.utils::ByteArray")) {
                    JMenu exportMenu = new JMenu("Export %name%".replace("%name%", v.name));
                    JMenuItem exportByteArray = new JMenuItem("Export bytearray");
                    exportByteArray.addActionListener((ActionEvent e1) -> {
                        Main.dumpBytes(v);
                    });

                    exportMenu.add(exportByteArray);
                    pm.add(exportMenu);
                }*/
                long watchParentId = vn.parentObjectId;

                JMenu addWatchMenu = new JMenu(AppStrings.translate("debug.watch.add").replace("%name%", v.name));
                JMenuItem watchReadMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.read"));
                watchReadMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, true, false)) {
                        View.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
                JMenuItem watchWriteMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.write"));
                watchWriteMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, false, true)) {
                        View.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
                JMenuItem watchReadWriteMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.readwrite"));
                watchReadWriteMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, true, true)) {
                        View.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                });

                addWatchMenu.add(watchReadMenuItem);
                addWatchMenu.add(watchWriteMenuItem);
                addWatchMenu.add(watchReadWriteMenuItem);
                pm.add(addWatchMenu);
                pm.show(e.getComponent(), e.getX(), e.getY());
            }
        };

        debugLocalsTable.addMouseListener(watchHandler);

        //debugScopeTable.addMouseListener(watchHandler);                           
        debugScopeTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugScopeTable, new ArrayList<>(), new ArrayList<>()), false);

        callStackTable = new JTable();
        stackTable = new JTable();
        constantPoolTable = new JTable();
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

    /*    private void getVariableList() {
        // make sure the player has stopped and send our message awaiting a response

        Main.getDebugHandler().getVariable(VariableConstants.GLOBAL_ID, TOOL_TIP_TEXT_KEY, loading)

        requestFrame(0, isolateId);  // our 0th frame gets our local context

        // now let's request all of the special variables too
        getValueWorker(VariableConstants.GLOBAL_ID, isolateId);
        getValueWorker(VariableConstants.THIS_ID, isolateId);
        getValueWorker(VariableConstants.ROOT_ID, isolateId);

        // request as many levels as we can get
        int i = 0;
        Value v = null;
        do {
            v = getValueWorker(Value.LEVEL_ID - i, isolateId);
        } while (i++ < 128 && v != null);

        // now that we've primed the DManager we can request the base variable whose
        // children are the variables that are available
        v = m_manager.getValue(Value.BASE_ID, isolateId);
        if (v == null) {
            throw new VersionException();
        }
        return v.getMembers(this);
    }*/
    public void refresh() {

        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                setLoading(true);
                synchronized (DebugPanel.this) {

                    SelectedTab oldSel = selectedTab;
                    localsTable = null;
                    InFrame f = Main.getDebugHandler().getFrame();
                    if (f != null) {

                        List<Long> regVarIds = new ArrayList<>();
                        for (int i = 0; i < f.registers.size(); i++) {
                            regVarIds.add(0L);
                        }
                        safeSetTreeModel(debugRegistersTable, new ABCPanel.VariablesTableModel(debugRegistersTable, f.registers, regVarIds));
                        List<Variable> locals = new ArrayList<>();
                        locals.addAll(f.arguments);
                        locals.addAll(f.variables);

                        List<Long> localIds = new ArrayList<>();
                        localIds.addAll(f.argumentFrameIds);
                        localIds.addAll(f.frameIds);

                        localsTable = new ABCPanel.VariablesTableModel(debugLocalsTable, locals, localIds);
                        safeSetTreeModel(debugLocalsTable, localsTable);
                        safeSetTreeModel(debugScopeTable, new ABCPanel.VariablesTableModel(debugScopeTable, f.scopeChain, f.scopeChainFrameIds));

                        /*TableModelListener refreshListener = new TableModelListener() {
                         @Override
                         public void tableChanged(TableModelEvent e) {
                         Main.getDebugHandler().refreshFrame();
                         refresh();
                         }
                         };*/
                        TreeModelListener refreshListener = new TreeModelListener() {
                            @Override
                            public void treeNodesChanged(TreeModelEvent e) {
                                Main.getDebugHandler().refreshFrame();
                                refresh();
                            }

                            @Override
                            public void treeNodesInserted(TreeModelEvent e) {
                                Main.getDebugHandler().refreshFrame();
                                refresh();
                            }

                            @Override
                            public void treeNodesRemoved(TreeModelEvent e) {
                                Main.getDebugHandler().refreshFrame();
                                refresh();
                            }

                            @Override
                            public void treeStructureChanged(TreeModelEvent e) {
                                Main.getDebugHandler().refreshFrame();
                                refresh();
                            }
                        };
                        debugLocalsTable.getTreeTableModel().addTreeModelListener(refreshListener);
                        debugScopeTable.getTreeTableModel().addTreeModelListener(refreshListener);
                    } else {
                        debugRegistersTable.setTreeModel(new ABCPanel.VariablesTableModel(debugRegistersTable, new ArrayList<>(), new ArrayList<>()));
                        debugLocalsTable.setTreeModel(new ABCPanel.VariablesTableModel(debugLocalsTable, new ArrayList<>(), new ArrayList<>()));
                        debugScopeTable.setTreeModel(new ABCPanel.VariablesTableModel(debugScopeTable, new ArrayList<>(), new ArrayList<>()));
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
                        }) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }

                        };
                        callStackTable.setModel(tm);

                        Object[][] data2 = new Object[info.stacks.size()][1];
                        for (int i = 0; i < info.stacks.size(); i++) {
                            data2[i][0] = info.stacks.get(i);
                        }
                        stackTable.setModel(new DefaultTableModel(data2, new Object[]{AppStrings.translate("stack.header.item")}) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }

                        });
                    } else {
                        callStackTable.setModel(new DefaultTableModel());
                        stackTable.setModel(new DefaultTableModel());
                    }
                    InConstantPool cpool = Main.getDebugHandler().getConstantPool();
                    if (cpool != null) {
                        Object[][] data2 = new Object[cpool.vars.size()][2];
                        for (int i = 0; i < cpool.vars.size(); i++) {
                            data2[i][0] = cpool.ids.get(i);
                            data2[i][1] = cpool.vars.get(i).value;
                        }
                        constantPoolTable.setModel(new DefaultTableModel(data2, new Object[]{
                            AppStrings.translate("constantpool.header.id"),
                            AppStrings.translate("constantpool.header.value")
                        }) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }

                        });
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

                    if (constantPoolTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.CONSTANTPOOL);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new JScrollPane(constantPoolTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("constantpool.header"), pa);
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
