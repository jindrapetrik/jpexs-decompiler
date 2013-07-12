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

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.Main;
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
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.DocumentSearchData;

public class ABCPanel extends JPanel implements ItemListener, ActionListener {

    public TraitsList navigator;
    public ClassesListTree classTree;
    public ABC abc;
    public List<ABCContainerTag> list;
    public JComboBox abcComboBox;
    public int listIndex = -1;
    public DecompiledEditorPane decompiledTextArea;
    public JScrollPane decompiledScrollPane;
    public JSplitPane splitPane;
    //public JSplitPane splitPaneTreeVSNavigator;
    //public JSplitPane splitPaneTreeNavVSDecompiledDetail;
    private JTable constantTable;
    public JComboBox constantTypeList;
    public JLabel asmLabel = new JLabel(translate("panel.disassembled"));
    public JLabel decLabel = new JLabel(translate("panel.decompiled"));
    public DetailPanel detailPanel;
    public JTextField filterField = new JTextField("");
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

    public boolean search(String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.equals(""))) {
            searchIgnoreCase = ignoreCase;
            searchRegexp = regexp;
            ClassesListTreeModel clModel = (ClassesListTreeModel) classTree.getModel();
            List<MyEntry<ClassPath, ScriptPack>> allpacks = clModel.getList();
            found = new ArrayList<>();
            Pattern pat = null;
            if (regexp) {
                pat = Pattern.compile(txt, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } else {
                pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            }
            int pos = 0;
            for (MyEntry<ClassPath, ScriptPack> item : allpacks) {
                pos++;
                String workText = translate("work.searching");
                String decAdd = "";
                if (!decompiledTextArea.isCached(item.value)) {
                    decAdd = ", " + translate("work.decompiling");
                }
                Main.startWork(workText + " \"" + txt + "\"" + decAdd + " - (" + pos + "/" + allpacks.size() + ") " + item.key.toString() + "... ");
                decompiledTextArea.cacheScriptPack(item.value, list);
                if (pat.matcher(decompiledTextArea.getCachedText(item.value)).find()) {
                    found.add(item.value);
                    foundPath.add(item.key);
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
                searchForLabel.setText(translate("search.info").replace("%text%", txt) + " ");
            }
            return true;
        }
        return false;
    }

    private JTable autoResizeColWidth(JTable table, TableModel model) {
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

    public void switchAbc(int index) {
        listIndex = index;
        classTree.setDoABCTags(list);

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


        splitPane.setDividerLocation((Integer) Configuration.getConfig("gui.avm2.splitPane.dividerLocation", splitPane.getWidth() * 1 / 2));

    }

    @SuppressWarnings("unchecked")
    public ABCPanel(List<ABCContainerTag> list, SWF swf) {


        DefaultSyntaxKit.initKit();

        this.list = list;
        if (list.size() > 0) {
            this.abc = list.get(0).getABC();
        }
        setLayout(new BorderLayout());






        decompiledTextArea = new DecompiledEditorPane(this);


        searchPanel = new JPanel(new FlowLayout());

        decompiledScrollPane = new JScrollPane(decompiledTextArea);

        JPanel decPanel = new JPanel(new BorderLayout());
        decPanel.add(searchPanel, BorderLayout.NORTH);
        decPanel.add(decompiledScrollPane, BorderLayout.CENTER);
        detailPanel = new DetailPanel(this);
        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        panB.add(decPanel, BorderLayout.CENTER);
        panB.add(decLabel, BorderLayout.NORTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panB, detailPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);

        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                Configuration.setConfig("gui.avm2.splitPane.dividerLocation", pce.getNewValue());
            }
        });
        decompiledTextArea.setContentType("text/actionscript");
        decompiledTextArea.setFont(new Font("Monospaced", Font.PLAIN, decompiledTextArea.getFont().getSize()));


        JPanel pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        pan2.add((abcComboBox = new JComboBox(new ABCComboBoxModel(list))), BorderLayout.NORTH);

        navigator = new TraitsList(this);
        navigator.setABC(list, abc);


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

        Main.startWork(translate("work.buildingscripttree") + "...");

        filterField.setActionCommand("FILTERSCRIPT");
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
        treePanel.add(new JScrollPane(classTree = new ClassesListTree(list, this, swf)), BorderLayout.CENTER);
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BorderLayout());
        filterPanel.add(filterField, BorderLayout.CENTER);
        JButton prevSearchButton = new JButton(View.getIcon("prev16"));
        prevSearchButton.setMargin(new Insets(3, 3, 3, 3));
        prevSearchButton.addActionListener(this);
        prevSearchButton.setActionCommand("SEARCHPREV");
        JButton nextSearchButton = new JButton(View.getIcon("next16"));
        nextSearchButton.setMargin(new Insets(3, 3, 3, 3));
        nextSearchButton.addActionListener(this);
        nextSearchButton.setActionCommand("SEARCHNEXT");
        JButton cancelSearchButton = new JButton(View.getIcon("cancel16"));
        cancelSearchButton.setMargin(new Insets(3, 3, 3, 3));
        cancelSearchButton.addActionListener(this);
        cancelSearchButton.setActionCommand("SEARCHCANCEL");
        searchPos = new JLabel("0/0");
        searchForLabel = new JLabel(translate("search.info").replace("%text%", "") + " ");
        searchPanel.add(searchForLabel);
        searchPanel.add(prevSearchButton);
        searchPanel.add(new JLabel(translate("search.script") + " "));
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
        tabbedPane.addTab(translate("traits"), navPanel);
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

        final List<ABCContainerTag> inlist = list;
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
                            UsageFrame usageFrame = new UsageFrame(inlist, abc, multinameIndex, t);
                            usageFrame.setVisible(true);
                        }
                    }
                }
            }
        });
        constantTypeList.addItemListener(this);
        panConstants.add(constantTypeList, BorderLayout.NORTH);
        panConstants.add(new JScrollPane(constantTable), BorderLayout.CENTER);
        tabbedPane.addTab(translate("constants"), panConstants);
    }

    public void doFilter() {
        classTree.applyFilter(filterField.getText());
    }

    public void reload() {
        switchAbc(listIndex);
        decompiledTextArea.clearScriptCache();
        decompiledTextArea.reloadClass();
        detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(-1, abc);
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
        TagTreeModel ttm = (TagTreeModel) Main.mainFrame.tagTree.getModel();
        TreePath tp = ttm.getTagPath(pack);
        if (tp == null) {
        }
        Main.mainFrame.tagTree.setSelectionPath(tp);
        Main.mainFrame.tagTree.scrollPathToVisible(tp);
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
        if (e.getActionCommand().equals("FILTERSCRIPT")) {
            doFilter();
        }
        if (e.getActionCommand().equals("SEARCHCANCEL")) {
            foundPos = 0;
            searchPanel.setVisible(false);
            found = new ArrayList<>();
            searchFor = null;
        }
        if (e.getActionCommand().equals("SEARCHPREV")) {
            foundPos--;
            if (foundPos < 0) {
                foundPos += found.size();
            }
            updateSearchPos();
        }
        if (e.getActionCommand().equals("SEARCHNEXT")) {
            foundPos = (foundPos + 1) % found.size();
            updateSearchPos();
        }
    }
}
