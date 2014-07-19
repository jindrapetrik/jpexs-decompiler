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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.SearchListener;
import com.jpexs.decompiler.flash.gui.SearchPanel;
import com.jpexs.decompiler.flash.gui.SearchResultsDialog;
import com.jpexs.decompiler.flash.gui.TagTreeModel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.DecimalTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.DoubleTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.IntTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.MultinameTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.NamespaceSetTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.NamespaceTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.StringTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.UIntTableModel;
import com.jpexs.decompiler.flash.helpers.Freed;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;

public class ABCPanel extends JPanel implements ItemListener, ActionListener, SearchListener<ABCPanelSearchResult>, Freed {

    private MainPanel mainPanel;
    public TraitsList navigator;
    public ABC abc;
    public SWF swf;
    public JComboBox<ABCContainerTag> abcComboBox;
    public int listIndex = -1;
    public DecompiledEditorPane decompiledTextArea;
    public JScrollPane decompiledScrollPane;
    public JSplitPane splitPane;
    //public JSplitPane splitPaneTreeVSNavigator;
    //public JSplitPane splitPaneTreeNavVSDecompiledDetail;
    private JTable constantTable;
    public JComboBox<String> constantTypeList;
    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));
    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));
    public DetailPanel detailPanel;
    public JPanel navPanel;
    public JTabbedPane tabbedPane;
    public SearchPanel<ABCPanelSearchResult> searchPanel;
    private NewTraitDialog newTraitDialog;
    public JLabel scriptNameLabel;

    static final String ACTION_SAVE_DECOMPILED = "SAVEDECOMPILED";
    static final String ACTION_EDIT_DECOMPILED = "EDITDECOMPILED";
    static final String ACTION_CANCEL_DECOMPILED = "CANCELDECOMPILED";

    public JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));
    public JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));
    public JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));
    public JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    static final String ACTION_ADD_TRAIT = "ADDTRAIT";

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public boolean search(final String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.isEmpty())) {
            searchPanel.setOptions(ignoreCase, regexp);
            TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
            TreeNode scriptsNode = ttm.getSwfNode(mainPanel.getCurrentSwf()).scriptsNode;
            final List<ABCPanelSearchResult> found = new ArrayList<>();
            if (scriptsNode.getItem() instanceof ClassesListTreeModel) {
                ClassesListTreeModel clModel = (ClassesListTreeModel) scriptsNode.getItem();
                List<MyEntry<ClassPath, ScriptPack>> allpacks = clModel.getList();
                final Pattern pat = regexp
                        ? Pattern.compile(txt, ignoreCase ? Pattern.CASE_INSENSITIVE : 0)
                        : Pattern.compile(Pattern.quote(txt), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
                int pos = 0;
                for (final MyEntry<ClassPath, ScriptPack> item : allpacks) {
                    pos++;
                    String workText = AppStrings.translate("work.searching");
                    String decAdd = "";
                    if (!decompiledTextArea.isCached(item.value)) {
                        decAdd = ", " + AppStrings.translate("work.decompiling");
                    }

                    try {
                        CancellableWorker worker = new CancellableWorker() {

                            @Override
                            public Void doInBackground() throws Exception {
                                decompiledTextArea.cacheScriptPack(item.value, swf.abcList);
                                if (pat.matcher(decompiledTextArea.getCachedText(item.value)).find()) {
                                    ABCPanelSearchResult searchResult = new ABCPanelSearchResult();
                                    searchResult.scriptPack = item.value;
                                    searchResult.classPath = item.key;
                                    found.add(searchResult);
                                }
                                return null;
                            }
                        };
                        worker.execute();
                        Main.startWork(workText + " \"" + txt + "\"" + decAdd + " - (" + pos + "/" + allpacks.size() + ") " + item.key.toString() + "... ", worker);
                        worker.get();
                    } catch (InterruptedException ex) {
                        break;
                    } catch (ExecutionException ex) {
                        Logger.getLogger(ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            Main.stopWork();

            searchPanel.setSearchText(txt);

            View.execInEventDispatch(new Runnable() {

                @Override
                public void run() {
                    SearchResultsDialog<ABCPanelSearchResult> sr = new SearchResultsDialog<>(ABCPanel.this.mainPanel.getMainFrame().getWindow(), txt, ABCPanel.this);
                    sr.setResults(found);
                    sr.setVisible(true);
                }
            });

            return true;

            //return searchPanel.setResults(found);
        }
        return false;
    }

    private JTable autoResizeColWidth(final JTable table, final TableModel model) {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setModel(model);

                int margin = 5;

                for (int i = 0; i < table.getColumnCount(); i++) {
                    int vColIndex = i;
                    DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
                    TableColumn col = colModel.getColumn(vColIndex);
                    int width;

                    // Get width of column header
                    TableCellRenderer renderer = col.getHeaderRenderer();

                    if (renderer == null) {
                        renderer = table.getTableHeader().getDefaultRenderer();
                    }

                    Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

                    width = comp.getPreferredSize().width;

                    // Get maximum width of column data
                    for (int r = 0; r < table.getRowCount(); r++) {
                        renderer = table.getCellRenderer(r, vColIndex);
                        comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                                r, vColIndex);
                        width = Math.max(width, comp.getPreferredSize().width);
                    }

                    // Add margin
                    width += 2 * margin;

                    // Set the width
                    col.setPreferredWidth(width);
                }

                ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
                        SwingConstants.LEFT);

                // table.setAutoCreateRowSorter(true);
                table.getTableHeader().setReorderingAllowed(false);
            }
        });

        return table;
    }

    public void setAbc(ABC abc) {
        this.abc = abc;
        updateConstList();
    }

    public void updateConstList() {
        switch (constantTypeList.getSelectedIndex()) {
            case 0:
                autoResizeColWidth(constantTable, new UIntTableModel(abc));
                break;
            case 1:
                autoResizeColWidth(constantTable, new IntTableModel(abc));
                break;
            case 2:
                autoResizeColWidth(constantTable, new DoubleTableModel(abc));
                break;
            case 3:
                autoResizeColWidth(constantTable, new DecimalTableModel(abc));
                break;
            case 4:
                autoResizeColWidth(constantTable, new StringTableModel(abc));
                break;
            case 5:
                autoResizeColWidth(constantTable, new NamespaceTableModel(abc));
                break;
            case 6:
                autoResizeColWidth(constantTable, new NamespaceSetTableModel(abc));
                break;
            case 7:
                autoResizeColWidth(constantTable, new MultinameTableModel(abc));
                break;
        }
        //DefaultTableColumnModel colModel  = (DefaultTableColumnModel) constantTable.getColumnModel();
        //colModel.getColumn(0).setMaxWidth(50);
    }

    public void clearSwf() {
        this.swf = null;
        this.abc = null;
        constantTable.setModel(new DefaultTableModel());
        abcComboBox.setModel(new ABCComboBoxModel(new ArrayList<ABCContainerTag>()));
        navigator.clearABC();
    }

    public void setSwf(SWF swf) {
        if (this.swf != swf) {
            this.swf = swf;
            listIndex = -1;
            switchAbc(0); // todo honika: do we need this?
            abcComboBox.setModel(new ABCComboBoxModel(swf.abcList));
            if (swf.abcList.size() > 0) {
                this.abc = swf.abcList.get(0).getABC();
            }

            navigator.setABC(swf.abcList, abc);
        }
    }

    public void switchAbc(int index) {
        listIndex = index;

        if (index != -1) {
            this.abc = swf.abcList.get(index).getABC();
        }
        updateConstList();
    }

    public void initSplits() {
        //splitPaneTreeVSNavigator.setDividerLocation(splitPaneTreeVSNavigator.getHeight() / 2);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        //splitPaneTreeNavVSDecompiledDetail.setDividerLocation(splitPaneTreeNavVSDecompiledDetail.getWidth() * 1 / 3);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        splitPane.setDividerLocation(Configuration.guiAvm2SplitPaneDividerLocation.get(splitPane.getWidth() * 1 / 2));

    }

    private boolean isFreeing;

    @Override
    public boolean isFreeing() {
        return isFreeing;
    }

    @Override
    public void free() {
        isFreeing = true;
        Helper.emptyObject(this);
    }

    public ABCPanel(MainPanel mainPanel) {
        DefaultSyntaxKit.initKit();

        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());

        decompiledTextArea = new DecompiledEditorPane(this);

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        decompiledScrollPane = new JScrollPane(decompiledTextArea);

        JPanel iconDecPanel = new JPanel();
        iconDecPanel.setLayout(new BoxLayout(iconDecPanel, BoxLayout.Y_AXIS));
        JPanel iconsPanel = new JPanel();
        iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.X_AXIS));

        JButton newTraitButton = new JButton(View.getIcon("traitadd16"));
        newTraitButton.setMargin(new Insets(5, 5, 5, 5));
        newTraitButton.addActionListener(this);
        newTraitButton.setActionCommand(ACTION_ADD_TRAIT);
        newTraitButton.setToolTipText(AppStrings.translate("button.addtrait"));
        iconsPanel.add(newTraitButton);

        scriptNameLabel = new JLabel("-");
        scriptNameLabel.setAlignmentX(0);
        iconsPanel.setAlignmentX(0);
        decompiledScrollPane.setAlignmentX(0);
        iconDecPanel.add(scriptNameLabel);
        iconDecPanel.add(iconsPanel);
        iconDecPanel.add(decompiledScrollPane);

        JPanel decButtonsPan = new JPanel(new FlowLayout());
        decButtonsPan.setBorder(new BevelBorder(BevelBorder.RAISED));
        decButtonsPan.add(editDecompiledButton);
        decButtonsPan.add(experimentalLabel);
        decButtonsPan.add(saveDecompiledButton);
        decButtonsPan.add(cancelDecompiledButton);

        editDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        saveDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        cancelDecompiledButton.setMargin(new Insets(3, 3, 3, 10));

        saveDecompiledButton.addActionListener(this);
        saveDecompiledButton.setActionCommand(ACTION_SAVE_DECOMPILED);
        editDecompiledButton.addActionListener(this);
        editDecompiledButton.setActionCommand(ACTION_EDIT_DECOMPILED);

        cancelDecompiledButton.addActionListener(this);
        cancelDecompiledButton.setActionCommand(ACTION_CANCEL_DECOMPILED);
        saveDecompiledButton.setVisible(false);
        cancelDecompiledButton.setVisible(false);
        decButtonsPan.setAlignmentX(0);

        JPanel decPanel = new JPanel(new BorderLayout());
        decPanel.add(searchPanel, BorderLayout.NORTH);
        decPanel.add(iconDecPanel, BorderLayout.CENTER);
        decPanel.add(decButtonsPan, BorderLayout.SOUTH);
        detailPanel = new DetailPanel(this);
        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        panB.add(decPanel, BorderLayout.CENTER);
        panB.add(decLabel, BorderLayout.NORTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panB, detailPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);

        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (!directEditing) {
                    Configuration.guiAvm2SplitPaneDividerLocation.set((int) pce.getNewValue());
                }
            }
        });
        decompiledTextArea.setContentType("text/actionscript");
        decompiledTextArea.setFont(new Font("Monospaced", Font.PLAIN, decompiledTextArea.getFont().getSize()));

        View.addEditorAction(decompiledTextArea, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int multinameIndex = decompiledTextArea.getMultinameUnderCursor();
                if (multinameIndex > -1) {
                    UsageFrame usageFrame = new UsageFrame(swf.abcList, abc, multinameIndex, ABCPanel.this, false);
                    usageFrame.setVisible(true);
                }
            }
        }, "find-usages", AppStrings.translate("abc.action.find-usages"), "control U");

        View.addEditorAction(decompiledTextArea, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoDeclaration();
            }
        }, "find-declaration", AppStrings.translate("abc.action.find-declaration"), "control B");

        CtrlClickHandler cch = new CtrlClickHandler();
        decompiledTextArea.addKeyListener(cch);
        decompiledTextArea.addMouseListener(cch);
        decompiledTextArea.addMouseMotionListener(cch);

        JPanel pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        pan2.add((abcComboBox = new JComboBox<>(new ABCComboBoxModel(new ArrayList<ABCContainerTag>()))), BorderLayout.NORTH);

        navigator = new TraitsList(this);

        navPanel = new JPanel(new BorderLayout());
        JPanel navIconsPanel = new JPanel();
        navIconsPanel.setLayout(new BoxLayout(navIconsPanel, BoxLayout.X_AXIS));
        final JToggleButton sortButton = new JToggleButton(View.getIcon("sort16"));
        sortButton.setMargin(new Insets(3, 3, 3, 3));
        navIconsPanel.add(sortButton);
        navPanel.add(navIconsPanel, BorderLayout.SOUTH);
        navPanel.add(new JScrollPane(navigator), BorderLayout.CENTER);
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.setSorted(sortButton.isSelected());
                navigator.updateUI();
            }
        });

        Main.startWork(AppStrings.translate("work.buildingscripttree") + "...");

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(AppStrings.translate("traits"), navPanel);
        abcComboBox.addItemListener(this);
        add(splitPane, BorderLayout.CENTER);

        JPanel panConstants = new JPanel();
        panConstants.setLayout(new BorderLayout());

        constantTypeList = new JComboBox<>(new String[]{"UINT", "INT", "DOUBLE", "DECIMAL", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
        constantTable = new JTable();
        if (abc != null) {
            autoResizeColWidth(constantTable, new UIntTableModel(abc));
        }
        constantTable.setAutoCreateRowSorter(true);

        final ABCPanel t = this;
        constantTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (constantTypeList.getSelectedIndex() == 7) { //MULTINAME
                        int rowIndex = constantTable.getSelectedRow();
                        if (rowIndex == -1) {
                            return;
                        }
                        int multinameIndex = constantTable.convertRowIndexToModel(rowIndex);
                        if (multinameIndex > 0) {
                            UsageFrame usageFrame = new UsageFrame(t.swf.abcList, abc, multinameIndex, t, false);
                            usageFrame.setVisible(true);
                        }
                    }
                }
            }
        });
        constantTypeList.addItemListener(this);
        panConstants.add(constantTypeList, BorderLayout.NORTH);
        panConstants.add(new JScrollPane(constantTable), BorderLayout.CENTER);
        tabbedPane.addTab(AppStrings.translate("constants"), panConstants);
    }

    private void gotoDeclaration() {
        int multinameIndex = decompiledTextArea.getMultinameUnderCursor();
        if (multinameIndex > -1) {
            List<MultinameUsage> usages = abc.findMultinameDefinition(swf.abcList, multinameIndex);

            Multiname m = abc.constants.constant_multiname.get(multinameIndex);
            //search other ABC tags if this is not private multiname
            if (m.namespace_index > 0 && abc.constants.constant_namespace.get(m.namespace_index).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : swf.abcList) {
                    ABC a = at.getABC();
                    if (a == abc) {
                        continue;
                    }
                    int mid = a.constants.getMultinameId(m, false);
                    if (mid > 0) {
                        usages.addAll(a.findMultinameDefinition(swf.abcList, mid));
                    }
                }
            }

            //more than one? display list
            if (usages.size() > 1) {
                UsageFrame usageFrame = new UsageFrame(swf.abcList, abc, multinameIndex, ABCPanel.this, true);
                usageFrame.setVisible(true);
            } else if (!usages.isEmpty()) { //one
                UsageFrame.gotoUsage(ABCPanel.this, usages.get(0));
            }
        }
    }

    private class CtrlClickHandler extends KeyAdapter implements MouseListener, MouseMotionListener {

        private boolean ctrlDown = false;

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 17 && !decompiledTextArea.isEditable()) {
                ctrlDown = true;
                decompiledTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 17) {
                ctrlDown = false;
                decompiledTextArea.setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (ctrlDown && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1 && !decompiledTextArea.isEditable()) {
                ctrlDown = false;
                decompiledTextArea.setCursor(Cursor.getDefaultCursor());
                gotoDeclaration();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ctrlDown = false;
            decompiledTextArea.setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (ctrlDown && decompiledTextArea.isEditable()) {
                ctrlDown = false;
                decompiledTextArea.setCursor(Cursor.getDefaultCursor());
            }
        }

    }

    public void reload() {
        switchAbc(listIndex);
        decompiledTextArea.clearScriptCache();
        decompiledTextArea.reloadClass();
        detailPanel.methodTraitPanel.methodCodePanel.clear();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == abcComboBox) {
            int index = ((JComboBox) e.getSource()).getSelectedIndex();
            if (index == -1) {
                return;
            }
            switchAbc(index - 1);
        }
        if (e.getSource() == constantTypeList) {
            int index = ((JComboBox) e.getSource()).getSelectedIndex();
            if (index == -1) {
                return;
            }
            updateConstList();
        }
    }

    public void display() {
        setVisible(true);
    }

    public void hilightScript(SWF swf, String name) {
        TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
        TreeNode scriptsNode = ttm.getSwfNode(swf).scriptsNode;
        if (scriptsNode.getItem() instanceof ClassesListTreeModel) {
            ClassesListTreeModel clModel = (ClassesListTreeModel) scriptsNode.getItem();
            ScriptPack pack = null;
            for (MyEntry<ClassPath, ScriptPack> item : clModel.getList()) {
                if (item.key.toString().equals(name)) {
                    pack = item.value;
                    break;
                }
            }
            if (pack != null) {
                hilightScript(pack);
            }
        }
    }

    public void hilightScript(ScriptPack pack) {
        TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
        final TreePath tp = ttm.getTagPath(pack);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainPanel.tagTree.setSelectionPath(tp);
                mainPanel.tagTree.scrollPathToVisible(tp);
            }
        });

    }

    @Override
    public void updateSearchPos(ABCPanelSearchResult item) {
        ScriptPack pack = item.scriptPack;
        setAbc(pack.abc);
        decompiledTextArea.setScript(pack, swf.abcList);
        hilightScript(pack);
        decompiledTextArea.setCaretPosition(0);

        View.execInEventDispatchLater(new Runnable() {

            @Override
            public void run() {
                searchPanel.showQuickFindDialog(decompiledTextArea);
            }
        });

    }

    public String lastDecompiled = null;
    public boolean directEditing = false;
    private int detWidth = 0;
    private int detsp = 0;

    public void setDecompiledEditMode(boolean val) {
        if (val) {
            lastDecompiled = decompiledTextArea.getText();
            decompiledTextArea.setEditable(true);
            saveDecompiledButton.setVisible(true);
            editDecompiledButton.setVisible(false);
            experimentalLabel.setVisible(false);
            cancelDecompiledButton.setVisible(true);
            decompiledTextArea.getCaret().setVisible(true);
            decLabel.setIcon(View.getIcon("editing16"));
            directEditing = true;
            detWidth = detailPanel.getWidth();
            detsp = splitPane.getDividerLocation();
            detailPanel.setVisible(false);
        } else {
            decompiledTextArea.setText(lastDecompiled);
            decompiledTextArea.setEditable(false);
            saveDecompiledButton.setVisible(false);
            editDecompiledButton.setVisible(true);
            experimentalLabel.setVisible(true);
            cancelDecompiledButton.setVisible(false);
            decompiledTextArea.getCaret().setVisible(true);
            decLabel.setIcon(null);
            directEditing = false;
            detailPanel.setVisible(true);
            detailPanel.setSize(detailPanel.getHeight(), detWidth);
            splitPane.setDividerLocation(detsp);
        }
        decompiledTextArea.ignoreCarret = directEditing;

        decompiledTextArea.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_EDIT_DECOMPILED:
                File swc = Configuration.getPlayerSWC();
                final String adobePage = "http://www.adobe.com/support/flashplayer/downloads.html";
                if (swc == null) {
                    if (View.showConfirmDialog(this, AppStrings.translate("message.action.playerglobal.needed").replace("%adobehomepage%", adobePage), AppStrings.translate("message.action.playerglobal.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {

                        java.awt.Desktop desktop = null;
                        if (java.awt.Desktop.isDesktopSupported()) {
                            desktop = java.awt.Desktop.getDesktop();
                            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                                try {
                                    java.net.URI uri = new java.net.URI(adobePage);
                                    desktop.browse(uri);
                                } catch (URISyntaxException | IOException ex) {
                                }
                            } else {
                                desktop = null;
                            }
                        }

                        int ret = 0;
                        do {
                            ret = View.showConfirmDialog(this, AppStrings.translate("message.action.playerglobal.place").replace("%libpath%", Configuration.getFlashLibPath().getAbsolutePath()), AppStrings.translate("message.action.playerglobal.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                            swc = Configuration.getPlayerSWC();
                        } while (ret == JOptionPane.OK_OPTION && swc == null);
                    }
                }
                if (swc != null) {
                    if (View.showConfirmDialog(null, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS3Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
                        setDecompiledEditMode(true);
                    }
                }
                break;
            case ACTION_CANCEL_DECOMPILED:
                setDecompiledEditMode(false);
                break;
            case ACTION_SAVE_DECOMPILED:
                ScriptPack pack = decompiledTextArea.getScriptLeaf();
                String scriptName = pack.getPathScriptName() + ".as";
                int oldIndex = pack.scriptIndex;
                int newIndex = abc.script_info.size();
                String documentClass = "";
                loopt:
                for (Tag t : swf.tags) {
                    if (t instanceof SymbolClassTag) {
                        SymbolClassTag sc = (SymbolClassTag) t;
                        for (int i = 0; i < sc.tags.length; i++) {
                            if (sc.tags[i] == 0) {
                                documentClass = sc.names[i];
                                break loopt;
                            }
                        }
                    }
                }
                boolean isDocumentClass = documentClass.equals(pack.getPath().toString());

                try {
                    abc.script_info.get(oldIndex).delete(abc, true);
                    ActionScriptParser.compile(decompiledTextArea.getText(), abc, new ArrayList<ABC>(), isDocumentClass, scriptName);
                    //Move newly added script to its position
                    abc.script_info.set(oldIndex, abc.script_info.get(newIndex));
                    abc.script_info.remove(newIndex);
                    abc.pack(); //removes old classes/methods
                    ((Tag) abc.parentTag).setModified(true);
                    lastDecompiled = decompiledTextArea.getText();
                    mainPanel.updateClassesList();
                    List<MyEntry<ClassPath, ScriptPack>> packs = abc.script_info.get(oldIndex).getPacks(abc, oldIndex);
                    if (!packs.isEmpty()) {
                        hilightScript(swf, packs.get(0).key.toString());
                    }
                    //decompiledTextArea.setClassIndex(-1);
                    //navigator.setClassIndex(-1, oldIndex);
                    setDecompiledEditMode(false);
                    View.showMessageDialog(this, AppStrings.translate("message.action.saved"));
                    //reload();
                } catch (ParseException ex) {
                    abc.script_info.get(oldIndex).delete(abc, false);
                    View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    decompiledTextArea.gotoLine((int) ex.line);
                } catch (CompilationException ex) {
                    abc.script_info.get(oldIndex).delete(abc, false);
                    View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    decompiledTextArea.gotoLine((int) ex.line);
                } catch (IOException | InterruptedException ex) {
                    //ignore                    
                }
                break;
            case ACTION_ADD_TRAIT:
                int class_index = decompiledTextArea.getClassIndex();
                if (class_index < 0) {
                    return;
                }
                if (newTraitDialog == null) {
                    newTraitDialog = new NewTraitDialog();
                }
                int void_type = abc.constants.getPublicQnameId("void", true);//abc.constants.forceGetMultinameId(new Multiname(Multiname.QNAME, abc.constants.forceGetStringId("void"), abc.constants.forceGetNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.forceGetStringId("")), 0), -1, -1, new ArrayList<Integer>()));
                int int_type = abc.constants.getPublicQnameId("int", true); //abc.constants.forceGetMultinameId(new Multiname(Multiname.QNAME, abc.constants.forceGetStringId("int"), abc.constants.forceGetNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.forceGetStringId("")), 0), -1, -1, new ArrayList<Integer>()));

                Trait t = null;
                int kind;
                int nskind;
                String name = null;
                boolean isStatic;
                Multiname m;

                boolean again = false;
                loopm:
                do {
                    if (again) {
                        View.showMessageDialog(null, AppStrings.translate("error.trait.exists").replace("%name%", name), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                    again = false;
                    if (!newTraitDialog.display()) {
                        return;
                    }
                    kind = newTraitDialog.getTraitType();
                    nskind = newTraitDialog.getNamespaceKind();
                    name = newTraitDialog.getTraitName();
                    isStatic = newTraitDialog.getStatic();
                    m = new Multiname(Multiname.QNAME, abc.constants.getStringId(name, true), abc.constants.getNamespaceId(new Namespace(nskind, abc.constants.getStringId("", true)), 0, true), 0, 0, new ArrayList<Integer>());
                    int mid = abc.constants.getMultinameId(m);
                    if (mid == 0) {
                        break;
                    }
                    for (Trait tr : abc.class_info.get(class_index).static_traits.traits) {
                        if (tr.name_index == mid) {
                            again = true;
                            break;
                        }
                    }

                    for (Trait tr : abc.instance_info.get(class_index).instance_traits.traits) {
                        if (tr.name_index == mid) {
                            again = true;
                            break;
                        }
                    }
                } while (again);
                switch (kind) {
                    case Trait.TRAIT_GETTER:
                    case Trait.TRAIT_SETTER:
                    case Trait.TRAIT_METHOD:
                        TraitMethodGetterSetter tm = new TraitMethodGetterSetter();
                        MethodInfo mi = new MethodInfo(new int[0], void_type, abc.constants.getStringId(name, true), 0, new ValueKind[0], new int[0]);
                        int method_info = abc.addMethodInfo(mi);
                        tm.method_info = method_info;
                        MethodBody body = new MethodBody();
                        body.method_info = method_info;
                        body.init_scope_depth = 1;
                        body.max_regs = 1;
                        body.max_scope_depth = 1;
                        body.max_stack = 1;
                        body.exceptions = new ABCException[0];
                        AVM2Code code = new AVM2Code();
                        code.code.add(new AVM2Instruction(0, new GetLocal0Ins(), new int[0], new byte[0]));
                        code.code.add(new AVM2Instruction(0, new PushScopeIns(), new int[0], new byte[0]));
                        code.code.add(new AVM2Instruction(0, new ReturnVoidIns(), new int[0], new byte[0]));
                        body.code = code;
                        Traits traits = new Traits();
                        traits.traits = new ArrayList<>();
                        body.traits = traits;
                        abc.addMethodBody(body);
                        mi.setBody(body);
                        t = tm;
                        break;
                    case Trait.TRAIT_SLOT:
                    case Trait.TRAIT_CONST:
                        TraitSlotConst ts = new TraitSlotConst();
                        ts.type_index = int_type;
                        ts.value_kind = ValueKind.CONSTANT_Int;
                        ts.value_index = abc.constants.getIntId(0, true);
                        t = ts;
                        break;
                }
                if (t != null) {
                    t.kindType = kind;
                    t.name_index = abc.constants.getMultinameId(m, true);
                    int traitId;
                    if (isStatic) {
                        traitId = abc.class_info.get(class_index).static_traits.addTrait(t);
                    } else {
                        traitId = abc.class_info.get(class_index).static_traits.traits.size() + abc.instance_info.get(class_index).instance_traits.addTrait(t);
                    }
                    reload();
                    decompiledTextArea.gotoTrait(traitId);
                }

                break;
        }
    }

}
