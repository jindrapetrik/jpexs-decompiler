/*
 *  Copyright (C) 2023-2026 JPEXS
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
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
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

    private JComboBox<SessionItem> sessionComboBox = new JComboBox<>();
    private boolean sessionComboBoxCreating = false;
    private int lastSessionComboBoxIndex = -1;
    
    private JTable stackTable;

    private boolean active = false;

    private int depth = 0;

    private String[] swfHashes = new String[0];
    private int[] classIndices = new int[0];
    private int[] methodIndices = new int[0];
    private int[] traitIndices = new int[0];
    private WeakReference<DebuggerSession> currentSessionRef = null;
    
    public DebugStackPanel() {
        stackTable = new JTable();
        Main.getDebugHandler().addFrameChangeListener(new DebuggerHandler.FrameChangeListener() {
            @Override
            public void frameChanged(DebuggerSession session) {
                if (session == null) {
                    refresh(null);
                    return;
                }
                depth = session.getDepth();
                refresh(session);
            }
        });
        Main.getDebugHandler().addBreakListener(new DebuggerHandler.BreakListener() {
            @Override
            public void breakAt(DebuggerSession session, String scriptName, int line, int classIndex, int traitIndex, int methodIndex) {

            }

            @Override
            public void doContinue(DebuggerSession session) {
                clear();
            }
        });

        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {
            @Override
            public void connected(DebuggerSession session) {
            }

            @Override
            public void disconnected(DebuggerSession session) {
                clear();
            }
        });
        
        Main.getDebugHandler().addSelectionListener(new DebuggerHandler.SessionSelectionListener() {
            @Override
            public void sessionSelected(DebuggerSession newSession, int oldSessionId) {
                refresh(newSession);
            }            
        });

        //JLabel titleLabel = new JLabel(AppStrings.translate("callStack.header"), JLabel.CENTER);
        setLayout(new BorderLayout());
        //add(titleLabel, BorderLayout.NORTH);
        add(new FasterScrollPane(stackTable), BorderLayout.CENTER);
        add(sessionComboBox, BorderLayout.NORTH);

        stackTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = stackTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        gotoRow(row);
                    }
                }
            }
        });
        
        sessionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sessionComboBoxCreating) {
                    return;
                }
                SessionItem selection = (SessionItem) sessionComboBox.getSelectedItem();
                if (selection != null) {
                    DebuggerSession session = Main.getDebugHandler().getSessionById(selection.id);
                    if (session != null) {    
                        if (!session.isPaused()) {
                            sessionComboBox.setSelectedIndex(lastSessionComboBoxIndex);
                            return;
                        }
                        lastSessionComboBoxIndex = sessionComboBox.getSelectedIndex();
                        
                        Main.getDebugHandler().setSelectedSessionId(session.getId());
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                refresh(session);
                            }                            
                        });                        
                        View.execInEventDispatchLater(new Runnable() {
                            @Override
                            public void run() {                                
                                if (!session.isPaused()) {
                                    List<SWF> debuggedSwfs = new ArrayList<>(session.getDebuggedSwfs().values());
                                    gotoScriptNodeOrSwf(debuggedSwfs.get(debuggedSwfs.size() - 1));
                                } else {
                                    gotoRow(0);
                                }
                            }                            
                        });
                    }
                }
            }                     
        });
        sessionComboBox.setRenderer(new DefaultListCellRenderer() {
            
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {

                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                
                SessionItem item = (SessionItem) value;
                if (item != null) {
                    DebuggerSession session = Main.getDebugHandler().getSessionById(item.id);
                    if (!session.isPaused()) {
                        c.setForeground(Color.GRAY);
                        c.setBackground(list.getBackground());
                    } else {
                        c.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    }
                }

                return c;
            }
        });
    }
    
    private void gotoScriptNodeOrSwf(SWF swf) {
        if (Main.getMainFrame().getPanel().getCurrentView() == MainPanel.VIEW_RESOURCES) {
            TreeItem scriptNode = Main.getMainFrame().getPanel().tagTree.getFullModel().getScriptsNode(swf);
            if (scriptNode != null) {
                Main.getMainFrame().getPanel().setTagTreeSelectedNode(Main.getMainFrame().getPanel().getCurrentTree(), scriptNode);
                return;
            }                
        }
        Main.getMainFrame().getPanel().setTagTreeSelectedNode(Main.getMainFrame().getPanel().getCurrentTree(), swf);        
    }
    
    private void gotoRow(int row) {
        if (stackTable.getModel().getRowCount() < 1) {
            return;
        }
        String swfHash = swfHashes[row];
        String scriptName = (String) stackTable.getModel().getValueAt(row, 1);
        int line = (int) (Integer) stackTable.getModel().getValueAt(row, 2);
        SWF swf = swfHash == null ? Main.getRunningSWF() : Main.findOpenedSwfByHash(swfHash);
        boolean scriptFound = Main.getMainFrame().getPanel().gotoScriptLine(swf,
                scriptName, line, classIndices[row], traitIndices[row], methodIndices[row], Main.isDebugPCode());
        
        if (!scriptFound) {
            gotoScriptNodeOrSwf(swf);
        }
        DebuggerSession session = null;
        if (currentSessionRef != null) {
            session = currentSessionRef.get();
        }
        if (session != null) {
            session.setDepth(row);
        }
    }
    
    private class SessionItem {
        private int id;

        public SessionItem(int id) {
            this.id = id;
        }       
        
        @Override
        public String toString() {
            DebuggerSession session = Main.getDebugHandler().getSessionById(id);
            if (session == null) {
                return "-";
            }
            return AppStrings.translate("debug.session").replace("%id%", "" + id) + (session.isPaused() ? "" : " " + AppStrings.translate("debug.session.running"));
        }                
    }

    public void clear() {
        stackTable.setModel(new DefaultTableModel());
        if (Main.getDebugHandler().getActiveSessions().isEmpty()) {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }
        

    public void refresh(DebuggerSession session) {
        
        Map<Integer, DebuggerSession> allSessions = Main.getDebugHandler().getActiveSessions();
        DefaultComboBoxModel<SessionItem> model = new DefaultComboBoxModel<>();
        int itemIndex = -1;
        int j = 0;
        for (int id : allSessions.keySet()) {
            DebuggerSession s = allSessions.get(id);
            if (s == Main.getCurrentDebugSession()) {
                itemIndex = j;
            }
            model.addElement(new SessionItem(id));
            j++;
        }
        sessionComboBoxCreating = true;
        sessionComboBox.setModel(model);
        if (itemIndex > -1) {
            final int fItemIndex = itemIndex;
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {                    
                    sessionComboBox.setSelectedIndex(fItemIndex);
                    lastSessionComboBoxIndex = fItemIndex;
                    sessionComboBoxCreating = false;
                }               
            });            
        }
        
        
        if (session == null) {
            clear();
            return;
        }
        InBreakAtExt info = session.getBreakInfo();
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
            String moduleName = session.moduleToString(f);
            String swfHash = null;
            String swfName = "unknown";
            if (moduleName.contains(":")) {
                swfHash = moduleName.substring(0, moduleName.indexOf(":"));
                moduleName = moduleName.substring(moduleName.indexOf(":") + 1);
                SWF swf = Main.findOpenedSwfByHash(swfHash);
                if (swf != null) {
                    swfName = swf.toString();
                }
            } else {
                List<SWF> debuggedSwfs = new ArrayList<>(session.getDebuggedSwfs().values());
                SWF lastSwf = debuggedSwfs.get(debuggedSwfs.size() - 1);
                swfHash = Main.getSwfHash(lastSwf);
                swfName = lastSwf.toString();
            }            
            
            newSwfHashes[i] = swfHash;
            data[i][0] = swfName;
            data[i][1] = moduleName;
            data[i][2] = info.lines.get(i);
            data[i][3] = info.stacks.get(i);
            Integer newClassIndex = session.moduleToClassIndex(f);
            newClassIndices[i] = newClassIndex == null ? -1 : newClassIndex;
            Integer newMethodIndex = session.moduleToMethodIndex(f);
            newMethodIndices[i] = newMethodIndex == null ? -1 : newMethodIndex;
            Integer newTraitIndex = session.moduleToTraitIndex(f);            
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
                if (stackTable.getColumnModel().getColumnCount() >= 3) {
                    stackTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
                    stackTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
                    stackTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
                }
                repaint();
            }
        });
        currentSessionRef = new WeakReference<>(session);        
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
