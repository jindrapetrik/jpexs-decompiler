/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.MyTextField;
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
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.DocumentSearchData;

public class ABCPanel extends JPanel implements ItemListener, ActionListener, Freed {

    private MainFrame mainFrame;
    public TraitsList navigator;
    public ClassesListTree classTree;
    public ABC abc;
    public List<ABCContainerTag> list;
    public SWF swf;
    public JComboBox abcComboBox;
    public int listIndex = -1;
    public DecompiledEditorPane decompiledTextArea;
    public JScrollPane decompiledScrollPane;
    public JSplitPane splitPane;
    //public JSplitPane splitPaneTreeVSNavigator;
    //public JSplitPane splitPaneTreeNavVSDecompiledDetail;
    private JTable constantTable;
    public JComboBox constantTypeList;
    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));
    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));
    public DetailPanel detailPanel;
    public JTextField filterField = new MyTextField();
    public JPanel navPanel;
    public JTabbedPane tabbedPane;
    public JPanel searchPanel;
    public JLabel searchPos;
    private List<ScriptPack> found = new ArrayList<>();
    private List<ClassPath> foundPath = new ArrayList<>();
    private int foundPos = 0;
    private JLabel searchForLabel;
    private String searchFor;
    private boolean searchIgnoreCase;
    private boolean searchRegexp;
    private NewTraitDialog newTraitDialog;
    public JLabel scriptNameLabel;

    static final String ACTION_ADD_TRAIT = "ADDTRAIT";
    public static final String ACTION_FILTER_SCRIPT = "FILTERSCRIPT";
    static final String ACTION_SEARCH_CANCEL = "SEARCHCANCEL";
    static final String ACTION_SEARCH_PREV = "SEARCHPREV";
    static final String ACTION_SEARCH_NEXT = "SEARCHNEXT";
    
    public boolean search(String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.isEmpty())) {
            searchIgnoreCase = ignoreCase;
            searchRegexp = regexp;
            ClassesListTreeModel clModel = (ClassesListTreeModel) classTree.getModel();
            List<MyEntry<ClassPath, ScriptPack>> allpacks = clModel.getList();
            found = new ArrayList<>();
            final Pattern pat = regexp ?
                Pattern.compile(txt, ignoreCase ? Pattern.CASE_INSENSITIVE : 0) :
                Pattern.compile(Pattern.quote(txt), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
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
                            decompiledTextArea.cacheScriptPack(item.value, list);
                            if (pat.matcher(decompiledTextArea.getCachedText(item.value)).find()) {
                                found.add(item.value);
                                foundPath.add(item.key);
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

            System.gc();
            Main.stopWork();
            if (found.isEmpty()) {
                searchPanel.setVisible(false);
                return false;
            } else {
                foundPos = 0;
                decompiledTextArea.setScript(found.get(foundPos), list);
                searchPanel.setVisible(true);
                searchFor = txt;
                updateSearchPos();
                searchForLabel.setText(AppStrings.translate("search.info").replace("%text%", txt) + " ");
            }
            return true;
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

    @SuppressWarnings("unchecked")
    public void setSwf(List<ABCContainerTag> list, SWF swf) {
        this.list = list;
        this.swf = swf;
        switchAbc(0); // todo honika: do we need this?
        abcComboBox.setModel(new ABCComboBoxModel(list));
        if (list.size() > 0) {
            this.abc = list.get(0).getABC();
        }

        navigator.setABC(list, abc);
    }
    
    public void switchAbc(int index) {
        listIndex = index;
        classTree.setDoABCTags(list, swf);

        if (index != -1) {
            this.abc = list.get(index).getABC();
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

    @SuppressWarnings("unchecked")
    public ABCPanel(MainFrame mainFrame) {
        DefaultSyntaxKit.initKit();

        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        decompiledTextArea = new DecompiledEditorPane(this);

        searchPanel = new JPanel(new FlowLayout());

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

        JPanel decPanel = new JPanel(new BorderLayout());
        decPanel.add(searchPanel, BorderLayout.NORTH);
        decPanel.add(iconDecPanel, BorderLayout.CENTER);
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
                Configuration.guiAvm2SplitPaneDividerLocation.set((int) pce.getNewValue());
            }
        });
        decompiledTextArea.setContentType("text/actionscript");
        decompiledTextArea.setFont(new Font("Monospaced", Font.PLAIN, decompiledTextArea.getFont().getSize()));


        JPanel pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        pan2.add((abcComboBox = new JComboBox(new ABCComboBoxModel(new ArrayList<ABCContainerTag>()))), BorderLayout.NORTH);

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

        filterField.setActionCommand(ACTION_FILTER_SCRIPT);
        filterField.addActionListener(this);


        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                doFilter();
            }
        });

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(new JScrollPane(classTree = new ClassesListTree(this)), BorderLayout.CENTER);
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BorderLayout());
        filterPanel.add(filterField, BorderLayout.CENTER);
        JButton prevSearchButton = new JButton(View.getIcon("prev16"));
        prevSearchButton.setMargin(new Insets(3, 3, 3, 3));
        prevSearchButton.addActionListener(this);
        prevSearchButton.setActionCommand(ACTION_SEARCH_PREV);

        JButton nextSearchButton = new JButton(View.getIcon("next16"));
        nextSearchButton.setMargin(new Insets(3, 3, 3, 3));
        nextSearchButton.addActionListener(this);
        nextSearchButton.setActionCommand(ACTION_SEARCH_NEXT);
        JButton cancelSearchButton = new JButton(View.getIcon("cancel16"));
        cancelSearchButton.setMargin(new Insets(3, 3, 3, 3));
        cancelSearchButton.addActionListener(this);
        cancelSearchButton.setActionCommand(ACTION_SEARCH_CANCEL);
        searchPos = new JLabel("0/0");
        searchForLabel = new JLabel(AppStrings.translate("search.info").replace("%text%", "") + " ");
        searchPanel.add(searchForLabel);
        searchPanel.add(prevSearchButton);
        searchPanel.add(new JLabel(AppStrings.translate("search.script") + " "));
        searchPanel.add(searchPos);
        searchPanel.add(nextSearchButton);
        searchPanel.add(cancelSearchButton);
        searchPanel.setVisible(false);
        JLabel picLabel = new JLabel(View.getIcon("search16"));
        filterPanel.add(picLabel, BorderLayout.EAST);
        treePanel.add(filterPanel, BorderLayout.NORTH);

        /* splitPaneTreeVSNavigator = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
         treePanel,
         navPanel);
         splitPaneTreeVSNavigator.setResizeWeight(0.5);
         splitPaneTreeVSNavigator.setContinuousLayout(true);*/
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(AppStrings.translate("traits"), navPanel);
        //tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

        //pan2.add(tabbedPane, BorderLayout.CENTER);
        abcComboBox.addItemListener(this);
        /*

         splitPaneTreeNavVSDecompiledDetail = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         pan2,
         splitPaneDecompiledVSDetail);
         splitPaneTreeNavVSDecompiledDetail.setResizeWeight(0);
         splitPaneTreeNavVSDecompiledDetail.setContinuousLayout(true);
         //pan2.setPreferredSize(new Dimension(300, 200));





         add(splitPaneTreeNavVSDecompiledDetail, BorderLayout.CENTER);*/
        add(splitPane, BorderLayout.CENTER);

        JPanel panConstants = new JPanel();
        panConstants.setLayout(new BorderLayout());

        constantTypeList = new JComboBox(new Object[]{"UINT", "INT", "DOUBLE", "DECIMAL", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
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
                            UsageFrame usageFrame = new UsageFrame(t.list, abc, multinameIndex, t);
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

    public void doFilter() {
        classTree.applyFilter(filterField.getText());
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

    public void hilightScript(String name) {
        ClassesListTreeModel clModel = (ClassesListTreeModel) classTree.getModel();
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

    public void hilightScript(ScriptPack pack) {
        TagTreeModel ttm = (TagTreeModel) mainFrame.tagTree.getModel();
        final TreePath tp = ttm.getTagPath(pack);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainFrame.tagTree.setSelectionPath(tp);
                mainFrame.tagTree.scrollPathToVisible(tp);
            }
        });

    }

    public void updateSearchPos() {
        searchPos.setText((foundPos + 1) + "/" + found.size());
        ScriptPack pack = found.get(foundPos);
        setAbc(pack.abc);
        decompiledTextArea.setScript(pack, list);
        hilightScript(found.get(foundPos));
        decompiledTextArea.setCaretPosition(0);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DocumentSearchData dsd = DocumentSearchData.getFromEditor(decompiledTextArea);
                dsd.setPattern(searchFor, searchRegexp, searchIgnoreCase);
                dsd.showQuickFindDialogEx(decompiledTextArea, searchIgnoreCase, searchRegexp);
            }
        });


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
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
                    m = new Multiname(Multiname.QNAME, abc.constants.getStringId(name, true), abc.constants.getNamespaceId(new Namespace(nskind, abc.constants.getStringId("", true)), 0, true), -1, -1, new ArrayList<Integer>());
                    int mid = abc.constants.getMultinameId(m);
                    if (mid == 0) {
                        break;
                    }
                    for (Trait tr : abc.class_info[class_index].static_traits.traits) {
                        if (tr.name_index == mid) {
                            again = true;
                            break;
                        }
                    }

                    for (Trait tr : abc.instance_info[class_index].instance_traits.traits) {
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
                        traits.traits = new Trait[0];
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
                        traitId = abc.class_info[class_index].static_traits.addTrait(t);
                    } else {
                        traitId = abc.class_info[class_index].static_traits.traits.length + abc.instance_info[class_index].instance_traits.addTrait(t);
                    }
                    reload();
                    decompiledTextArea.gotoTrait(traitId);
                }

                break;
            case ACTION_FILTER_SCRIPT:
                doFilter();
                break;
            case ACTION_SEARCH_CANCEL:
                foundPos = 0;
                searchPanel.setVisible(false);
                found = new ArrayList<>();
                searchFor = null;
                break;
            case ACTION_SEARCH_PREV:
                foundPos--;
                if (foundPos < 0) {
                    foundPos += found.size();
                }
                updateSearchPos();
                break;
            case ACTION_SEARCH_NEXT:
                foundPos = (foundPos + 1) % found.size();
                updateSearchPos();
                break;
        }
    }
}
