/*
 *  Copyright (C) 2010-2025 JPEXS
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
import com.jpexs.debugger.flash.messages.in.InConstantPool;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.DebuggerHandler.BreakListener;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import com.jpexs.helpers.Helper;
import de.hameister.treetable.MyTreeTable;
import de.hameister.treetable.MyTreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class DebugPanel extends JPanel {

    private MyTreeTable debugRegistersTable;

    private MyTreeTable debugLocalsTable; //JTable debugLocalsTable;

    private MyTreeTable debugScopeTable;

    private JTable constantPoolTable;

    private JTabbedPane varTabs;

    private BreakListener breakListener;

    private DebuggerHandler.FrameChangeListener frameChangeListener;

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

    private void logAdd(String message) {
        boolean wasEmpty = logLength == 0;
        String add = message + "\r\n";
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

    public DebugPanel() {
        super(new BorderLayout());
        debugRegistersTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugRegistersTable, new ArrayList<>()), false);
        debugLocalsTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugLocalsTable, new ArrayList<>()), false);

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
                    v = vn.varInsideGetter != null ? vn.varInsideGetter : vn.var;
                } else {
                    return;
                }

                JPopupMenu pm = new JPopupMenu();
                boolean isByteArray = false;
                for (Variable t : vn.traits) {
                    if ("flash.utils::ByteArray".equals(t.name)) {
                        isByteArray = true;
                        break;
                    }
                }
                if (isByteArray) {
                    JMenu exportMenu = new JMenu(AppStrings.translate("debug.export").replace("%name%", v.name));
                    JMenuItem exportByteArrayMenuItem = new JMenuItem(AppStrings.translate("debug.export.bytearray"));
                    exportByteArrayMenuItem.addActionListener((ActionEvent e1) -> {
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
                        if (fc.showSaveDialog(Main.getDefaultMessagesComponent()) == JFileChooser.APPROVE_OPTION) {
                            File file = Helper.fixDialogFile(fc.getSelectedFile());

                            //Variant with direct calling readByte - SLOW
                            /*
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                Main.debugExportByteArray(v, fos);
                                fos.write(data);
                                Configuration.lastExportDir.set(file.getParentFile().getAbsolutePath());
                            } catch (IOException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex); 
                                ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                            }
                             */
                            //Asynchronous variant
                            /*DebuggerTools.initDebugger().addMessageListener(new DebugAdapter() {                                

                                @Override
                                public void onDumpByteArray(String clientId, byte[] data) {
                                    try (FileOutputStream fos = new FileOutputStream(file)) {
                                        fos.write(data);
                                        Configuration.lastExportDir.set(file.getParentFile().getAbsolutePath());
                                    } catch (IOException ex) {
                                        Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex); 
                                        ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                                    }
                                    DebuggerTools.initDebugger().removeMessageListener(this);
                                }                                                 
                            });    
                            Variable debugConnectionClass = Main.getDebugHandler().getVariable(0, Main.currentDebuggerPackage + "::DebugConnection", false, false).parent;
                            try {
                                Main.getDebugHandler().callMethod(debugConnectionClass, "writeMsg", Arrays.asList(v, (Double) (double) Debugger.MSG_DUMP_BYTEARRAY));
                            } catch (DebuggerHandler.ActionScriptException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, "Error exporting ByteArray", ex);
                            }*/
                            //Variant using comma separated bytes, pretty fast
                            try {
                                Variable debugConnectionClass = Main.getDebugHandler().getVariable(0, Main.currentDebuggerPackage + "::DebugConnection", false, false).parent;
                                String dataStr = (String) Main.getDebugHandler().callMethod(debugConnectionClass, "readCommaSeparatedFromByteArray", Arrays.asList(v)).variables.get(0).value;
                                String[] parts = dataStr.split(",");
                                byte[] data = new byte[parts.length];
                                for (int i = 0; i < parts.length; i++) {
                                    data[i] = (byte) Integer.parseInt(parts[i]);
                                }

                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(data);
                                    Configuration.lastExportDir.set(file.getParentFile().getAbsolutePath());
                                } catch (IOException ex) {
                                    Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex);
                                    ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (DebuggerHandler.ActionScriptException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, "Error exporting ByteArray", ex);
                            }
                        }
                    });

                    exportMenu.add(exportByteArrayMenuItem);
                    pm.add(exportMenu);

                    JMenu importMenu = new JMenu(AppStrings.translate("debug.import").replace("%name%", v.name));
                    JMenuItem importByteArrayMenuItem = new JMenuItem(AppStrings.translate("debug.import.bytearray"));
                    importByteArrayMenuItem.addActionListener((ActionEvent e1) -> {
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
                        if (fc.showOpenDialog(Main.getDefaultMessagesComponent()) == JFileChooser.APPROVE_OPTION) {
                            File file = Helper.fixDialogFile(fc.getSelectedFile());
                            Configuration.lastOpenDir.set(file.getParentFile().getAbsolutePath());

                            //Variant with asynchronous connection
                            /*DebuggerTools.initDebugger().addMessageListener(new DebugAdapter() {  
                                @Override
                                public byte[] onRequestBytes(String clientId) {
                                    byte[] data = null;
                                    try {
                                        data = Helper.readFileEx(file.getAbsolutePath());
                                    } catch (IOException ex) {
                                        Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex); 
                                        ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);                                                               
                                    }
                                    DebuggerTools.initDebugger().removeMessageListener(this);
                                    return data;
                                }
                            });
                            Variable debugConnectionClass = Main.getDebugHandler().getVariable(0, Main.currentDebuggerPackage + "::DebugConnection", false, false).parent;
                            try {
                                Main.getDebugHandler().callMethod(debugConnectionClass, "writeMsg", Arrays.asList(v, (Double) (double) Debugger.MSG_REQUEST_BYTEARRAY));
                            } catch (DebuggerHandler.ActionScriptException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, "Error exporting ByteArray", ex);
                            }*/
                            //Variant with direct writeByte calls - SLOW
                            /*try (FileInputStream fis = new FileInputStream(file)) {
                                Main.debugImportByteArray(v, fis);
                            } catch (IOException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex); 
                                ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);                                                               
                            }*/
                            //Variant with using comma separated bytes, pretty fast
                            try {
                                byte[] data = Helper.readFileEx(file.getAbsolutePath());
                                String splitter = "";
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < data.length; i++) {
                                    sb.append(splitter);
                                    sb.append(data[i] & 0xff);
                                    splitter = ",";
                                }
                                String dataStr = sb.toString();
                                Variable debugConnectionClass = Main.getDebugHandler().getVariable(0, Main.currentDebuggerPackage + "::DebugConnection", false, false).parent;
                                try {
                                    Main.getDebugHandler().callMethod(debugConnectionClass, "writeCommaSeparatedToByteArray", Arrays.asList(dataStr, v));
                                } catch (DebuggerHandler.ActionScriptException ex) {
                                    Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, "Error exporting ByteArray", ex);
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(DebugPanel.class.getName()).log(Level.SEVERE, null, ex);
                                ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("error.file.save") + ": " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    importMenu.add(importByteArrayMenuItem);
                    pm.add(importMenu);
                }
                long watchParentId = vn.parentObjectId;

                JMenu addWatchMenu = new JMenu(AppStrings.translate("debug.watch.add").replace("%name%", v.name));
                JMenuItem watchReadMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.read"));
                watchReadMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, true, false)) {
                        ViewMessages.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
                JMenuItem watchWriteMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.write"));
                watchWriteMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, false, true)) {
                        ViewMessages.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                });
                JMenuItem watchReadWriteMenuItem = new JMenuItem(AppStrings.translate("debug.watch.add.readwrite"));
                watchReadWriteMenuItem.addActionListener((ActionEvent e1) -> {
                    if (!Main.addWatch(v, watchParentId, true, true)) {
                        ViewMessages.showMessageDialog(DebugPanel.this, AppStrings.translate("error.debug.watch.add"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
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
        debugScopeTable = new MyTreeTable(new ABCPanel.VariablesTableModel(debugScopeTable, new ArrayList<>()), false);

        constantPoolTable = new JTable();
        traceLogTextarea = new JTextArea();
        traceLogTextarea.setEditable(false);
        traceLogTextarea.setOpaque(false);
        traceLogTextarea.setFont(new JLabel().getFont());
        if (View.isOceanic()) {
            traceLogTextarea.setBackground(Color.white);
        }

        Main.getDebugHandler().addTraceListener(new DebuggerHandler.TraceListener() {

            @Override
            public void trace(String... val) {
                for (String s : val) {
                    logAdd("trace: " + s);
                }
            }
        });

        Main.getDebugHandler().addErrorListener(new DebuggerHandler.ErrorListener() {
            @Override
            public void errorException(String message, Variable thrownVar) {
                logAdd("unhandled exception: " + message);
                selectedTab = tabTypes.get(tabTypes.size() - 1);
                refresh();
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

        Main.getDebugHandler().addFrameChangeListener(frameChangeListener = new DebuggerHandler.FrameChangeListener() {
            @Override
            public void frameChanged() {
                View.execInEventDispatchLater(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        });

        Main.getDebugHandler().addBreakListener(breakListener = new DebuggerHandler.BreakListener() {

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
                    SWF swf = Main.getMainFrame().getPanel().getCurrentSwf();
                    if (swf == null) {
                        return;
                    }
                    boolean as3 = swf.isAS3();
                    InFrame f = Main.getDebugHandler().getFrame();
                    if (f != null) {
                        
                        
                        List<Variable> locals = new ArrayList<>();
                        
                        
                        Map<String, Long> placedObjects = Main.getDebugHandler().getPlacedObjects();
                        for (String poName : placedObjects.keySet()) {
                            String realName = poName;
                            if ("/".equals(realName)) {
                                realName = "_root";
                            } else if (realName.startsWith("/")) {
                                continue;
                            }
                            Variable placedVar = Main.getDebugHandler().getVariable(0, realName, false, false).parent;
                            if (placedVar != null) {
                                locals.add(placedVar);
                            }
                        }
                        
                        safeSetTreeModel(debugRegistersTable, new ABCPanel.VariablesTableModel(debugRegistersTable, f.registers));
                        
                        locals.addAll(f.arguments);
                        locals.addAll(f.variables);

                        localsTable = new ABCPanel.VariablesTableModel(debugLocalsTable, locals);
                        safeSetTreeModel(debugLocalsTable, localsTable);
                        safeSetTreeModel(debugScopeTable, new ABCPanel.VariablesTableModel(debugScopeTable, f.scopeChain));

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
                        debugRegistersTable.setTreeModel(new ABCPanel.VariablesTableModel(debugRegistersTable, new ArrayList<>()));
                        debugLocalsTable.setTreeModel(new ABCPanel.VariablesTableModel(debugLocalsTable, new ArrayList<>()));
                        debugScopeTable.setTreeModel(new ABCPanel.VariablesTableModel(debugScopeTable, new ArrayList<>()));
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
                    } else {
                        constantPoolTable.setModel(new DefaultTableModel());
                    }

                    varTabs.removeAll();
                    tabTypes.clear();
                    JPanel pa;
                    if (debugRegistersTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.REGISTERS);
                        pa = new JPanel(new BorderLayout());
                        pa.add(new FasterScrollPane(debugRegistersTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.registers"), pa);
                    }
                    if (debugLocalsTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.LOCALS);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new FasterScrollPane(debugLocalsTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.locals"), pa);
                    }

                    if (debugScopeTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.SCOPECHAIN);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new FasterScrollPane(debugScopeTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("variables.header.scopeChain"), pa);
                    }

                    if (constantPoolTable.getRowCount() > 0) {
                        tabTypes.add(SelectedTab.CONSTANTPOOL);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new FasterScrollPane(constantPoolTable), BorderLayout.CENTER);
                        varTabs.addTab(AppStrings.translate("constantpool.header"), pa);
                    }

                    if (logLength > 0) {
                        tabTypes.add(SelectedTab.LOG);

                        pa = new JPanel(new BorderLayout());
                        pa.add(new FasterScrollPane(traceLogTextarea), BorderLayout.CENTER);
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
        Main.getDebugHandler().removeBreakListener(breakListener);
        Main.getDebugHandler().removeFrameChangeListener(frameChangeListener);
    }
}
