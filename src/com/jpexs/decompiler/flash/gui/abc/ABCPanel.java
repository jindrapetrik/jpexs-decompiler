/*
 *  Copyright (C) 2010-2016 JPEXS
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

import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.VariableType;
import com.jpexs.debugger.flash.messages.in.InGetVariable;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.Reference;
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
import com.jpexs.decompiler.flash.abc.usages.TraitMultinameUsage;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DebugPanel;
import com.jpexs.decompiler.flash.gui.DebuggerHandler;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.SearchListener;
import com.jpexs.decompiler.flash.gui.SearchPanel;
import com.jpexs.decompiler.flash.gui.TagEditorPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.DecimalTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.DoubleTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.IntTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.MultinameTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.NamespaceSetTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.NamespaceTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.StringTableModel;
import com.jpexs.decompiler.flash.gui.abc.tablemodels.UIntTableModel;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.editor.LinkHandler;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import de.hameister.treetable.MyTreeTable;
import de.hameister.treetable.MyTreeTableModel;
import java.awt.BorderLayout;
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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreePath;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 *
 * @author JPEXS
 */
public class ABCPanel extends JPanel implements ItemListener, SearchListener<ABCPanelSearchResult>, TagEditorPanel {

    private final MainPanel mainPanel;

    public final TraitsList navigator;

    public ABC abc;

    public final DecompiledEditorPane decompiledTextArea;

    public final JScrollPane decompiledScrollPane;

    private final JPersistentSplitPane splitPane;

    private final JTable constantTable;

    public JComboBox<String> constantTypeList;

    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));

    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));

    public final DetailPanel detailPanel;

    private final JPanel navPanel;

    public final JTabbedPane tabbedPane;

    public final SearchPanel<ABCPanelSearchResult> searchPanel;

    private NewTraitDialog newTraitDialog;

    public final JLabel scriptNameLabel;

    private final DebugPanel debugPanel;

    private final JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));

    private final JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));

    private final JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    private final JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    private String lastDecompiled = null;

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public List<ABCPanelSearchResult> search(final String txt, boolean ignoreCase, boolean regexp, CancellableWorker<Void> worker) {
        List<String> ignoredClasses = new ArrayList<>();
        List<String> ignoredNss = new ArrayList<>();

        if (Configuration._ignoreAdditionalFlexClasses.get()) {
            abc.getSwf().getFlexMainClass(ignoredClasses, ignoredNss);
        }
        if (txt != null && !txt.isEmpty()) {
            searchPanel.setOptions(ignoreCase, regexp);
            TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
            TreeItem scriptsNode = ttm.getScriptsNode(mainPanel.getCurrentSwf());
            final List<ABCPanelSearchResult> found = new ArrayList<>();
            if (scriptsNode instanceof ClassesListTreeModel) {
                ClassesListTreeModel clModel = (ClassesListTreeModel) scriptsNode;
                List<ScriptPack> allpacks = clModel.getList();
                final Pattern pat = regexp
                        ? Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0)
                        : Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
                int pos = 0;
                loop:
                for (final ScriptPack pack : allpacks) {
                    pos++;
                    if (!pack.isSimple && Configuration.ignoreCLikePackages.get()) {
                        continue;
                    }
                    if (Configuration._ignoreAdditionalFlexClasses.get()) {
                        String fullName = pack.getClassPath().packageStr.add(pack.getClassPath().className).toRawString();
                        if (ignoredClasses.contains(fullName)) {
                            continue;
                        }
                        for (String ns : ignoredNss) {
                            if (fullName.startsWith(ns + ".")) {
                                continue loop;
                            }
                        }
                    }

                    String workText = AppStrings.translate("work.searching");
                    String decAdd = "";
                    if (!SWF.isCached(pack)) {
                        decAdd = ", " + AppStrings.translate("work.decompiling");
                    }

                    Main.startWork(workText + " \"" + txt + "\"" + decAdd + " - (" + pos + "/" + allpacks.size() + ") " + pack.getClassPath().toString() + "... ", worker);
                    try {
                        if (pat.matcher(SWF.getCached(pack).text).find()) {
                            ABCPanelSearchResult searchResult = new ABCPanelSearchResult(pack);
                            found.add(searchResult);
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }

            return found;
        }

        return null;
    }

    public void setAbc(ABC abc) {
        if (abc == this.abc) {
            return;
        }
        this.abc = abc;
        setDecompiledEditMode(false);
        navigator.setAbc(abc);
        updateConstList();
    }

    public void updateConstList() {
        switch (constantTypeList.getSelectedIndex()) {
            case 0:
                View.autoResizeColWidth(constantTable, new UIntTableModel(abc));
                break;
            case 1:
                View.autoResizeColWidth(constantTable, new IntTableModel(abc));
                break;
            case 2:
                View.autoResizeColWidth(constantTable, new DoubleTableModel(abc));
                break;
            case 3:
                View.autoResizeColWidth(constantTable, new DecimalTableModel(abc));
                break;
            case 4:
                View.autoResizeColWidth(constantTable, new StringTableModel(abc));
                break;
            case 5:
                View.autoResizeColWidth(constantTable, new NamespaceTableModel(abc));
                break;
            case 6:
                View.autoResizeColWidth(constantTable, new NamespaceSetTableModel(abc));
                break;
            case 7:
                View.autoResizeColWidth(constantTable, new MultinameTableModel(abc));
                break;
        }
        //DefaultTableColumnModel colModel  = (DefaultTableColumnModel) constantTable.getColumnModel();
        //colModel.getColumn(0).setMaxWidth(50);
    }

    public SWF getSwf() {
        return abc == null ? null : abc.getSwf();
    }

    public List<ABCContainerTag> getAbcList() {
        SWF swf = getSwf();
        return swf == null ? null : swf.getAbcList();
    }

    public void clearSwf() {
        this.abc = null;
        constantTable.setModel(new DefaultTableModel());
        navigator.clearAbc();
        decompiledTextArea.clearScript();
    }

    public static class VariableNode {

        public List<VariableNode> path = new ArrayList<>();

        public Long parentId;

        public int level;

        public Variable thisVar;

        public Variable thisTrait;

        public long thisTraitId;

        private List<Variable> childs;

        private List<Variable> childTraits;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.parentId);
            hash = 53 * hash + (this.thisVar == null ? 0 : Objects.hashCode(this.thisVar.name));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VariableNode other = (VariableNode) obj;
            if (!Objects.equals(this.parentId, other.parentId)) {
                return false;
            }
            if (this.thisVar == null && other.thisVar == null) {
                return true;
            }
            if (this.thisVar == null) {
                return false;
            }
            if (other.thisVar == null) {
                return false;
            }
            return Objects.equals(this.thisVar.name, other.thisVar.name);
        }

        public boolean loaded = false;

        private static boolean isTraits(Variable v) {
            return (v.vType == VariableType.UNKNOWN && "traits".equals(v.typeName));
        }

        @Override
        public String toString() {
            if (level == 0) {
                return "root"; //TODO: localize?
            }
            return thisVar.name;
        }

        private void refresh() {
            if (path.size() > 1) {
                path.get(path.size() - 2).reloadChildren();
            } else {
                //Main.getDebugHandler().refreshFrame();
                //InFrame fr = Main.getDebugHandler().getFrame();
            }
        }

        private void reloadChildren() {
            InGetVariable igv = Main.getDebugHandler().getVariable(parentId, thisVar.name, true);
            childs = new ArrayList<>();
            childTraits = new ArrayList<>();

            Variable curTrait = null;

            for (int i = 0; i < igv.childs.size(); i++) {
                if (!isTraits(igv.childs.get(i))) {
                    childs.add(igv.childs.get(i));
                    childTraits.add(curTrait);
                } else {
                    curTrait = igv.childs.get(i);
                }
            }
        }

        private void ensureLoaded() {
            if (!loaded) {
                reloadChildren();
                loaded = true;
            }
        }

        public VariableNode getChildAt(int index) {
            ensureLoaded();
            Long parId = 0L;
            if (thisVar != null && (thisVar.vType == VariableType.OBJECT || thisVar.vType == VariableType.MOVIECLIP)) {
                parId = (Long) thisVar.value;
            }
            VariableNode vn = new VariableNode(level + 1, childs.get(index), parId, childTraits.get(index));
            vn.path.addAll(path);
            vn.path.add(vn);
            return vn;
        }

        public int getChildCount() {
            ensureLoaded();
            return childs.size();
        }

        public VariableNode(int level, Variable thisVar, Long parentId, Variable thisTrait) {
            this.parentId = parentId;
            this.thisVar = thisVar;
            this.level = level;
            this.thisTrait = thisTrait;
        }

        public VariableNode(int level, Variable thisVar, Long parentId, Variable thisTrait, Long thisTraitId, List<Variable> vars, List<Variable> varTraits) {
            this.parentId = parentId;

            this.thisVar = thisVar;

            this.level = level;
            this.childs = vars;

            this.thisTrait = thisTrait;

            this.childTraits = varTraits;
            this.path.add(this);

            loaded = true;
        }
    }

    public static class VariablesTableModel implements MyTreeTableModel {

        List<TableModelListener> tableListeners = new ArrayList<>();

        VariableNode root;

        private Map<VariableNode, List<VariableNode>> nodeCache = new HashMap<>();

        protected EventListenerList listenerList = new EventListenerList();

        private static final int CHANGED = 0;

        private static final int INSERTED = 1;

        private static final int REMOVED = 2;

        private static final int STRUCTURE_CHANGED = 3;

        private MyTreeTable ttable;

        public VariablesTableModel(MyTreeTable ttable, List<Variable> vars, List<Long> parentIds) {
            this.ttable = ttable;
            List<Variable> varTraits = new ArrayList<>();
            for (int i = 0; i < vars.size(); i++) {
                varTraits.add(null);
            }
            root = new VariableNode(0, null, 0L, null, 0L, vars, varTraits);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return AppStrings.translate("variables.column.name");
                case 1:
                    return AppStrings.translate("variables.column.type");
                case 2:
                    return AppStrings.translate("variables.column.value");
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return MyTreeTableModel.class;
            }
            return String.class;
        }

        @Override
        public Object getValueAt(Object node, int columnIndex) {
            if (node == root) {
                if (columnIndex == 0) {
                    return "root";
                }
                return "";
            }
            Variable v = ((VariableNode) node).thisVar;

            switch (columnIndex) {
                case 0:
                    return v.name;
                case 1:
                    String typeStr = v.getTypeAsStr();
                    if ("Object".equals(typeStr)) {
                        typeStr = v.className;
                    }
                    if ("Object".equals(typeStr)) {
                        typeStr = v.typeName;
                    }
                    return typeStr;
                case 2:
                    switch (v.vType) {
                        case VariableType.OBJECT:
                        case VariableType.MOVIECLIP:
                        case VariableType.FUNCTION:
                            return v.getTypeAsStr() + "(" + v.value + ")";
                        case VariableType.STRING:
                            return "\"" + Helper.escapeActionScriptString("" + v.value) + "\"";
                        default:
                            return EcmaScript.toString(v.value);
                    }

            }
            return null;
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return column == 0 || (column == 2 && node != root && ((VariableNode) node).thisVar.isPrimitive);
        }

        @Override
        public void setValueAt(Object aValue, Object node, int column) {
            ActionScriptLexer lexer = new ActionScriptLexer(new StringReader("" + aValue));
            ParsedSymbol symb;
            try {
                symb = lexer.lex();
                ParsedSymbol f = lexer.yylex();
                if (f.type != SymbolType.EOF) {
                    return;
                }
            } catch (IOException | ActionParseException ex) {
                return;
            }
            int valType;
            switch (symb.type) {
                case DOUBLE:
                    valType = VariableType.NUMBER;
                    break;
                case INTEGER:
                    valType = VariableType.NUMBER;
                    break;
                case NULL:
                    valType = VariableType.NULL;
                    break;
                case STRING:
                    valType = VariableType.STRING;
                    break;
                case UNDEFINED:
                    valType = VariableType.UNDEFINED;
                    break;
                default:
                    return;
            }
            Main.getDebugHandler().setVariable(((VariableNode) node).parentId, ((VariableNode) node).thisVar.name, valType, symb.value);
            //((VariableNode) node).refresh();
            Object path[] = new Object[((VariableNode) node).path.size()];
            for (int i = 0; i < path.length; i++) {
                path[i] = ((VariableNode) node).path.get(i);
            }
            valueForPathChanged(new TreePath(path), aValue);
            //fireTreeNodesChanged(this, path, new int[0]/*removed*/, new Object[]{node});
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((VariableNode) parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            int cnt = ((VariableNode) parent).getChildCount();
            return cnt;
        }

        @Override
        public boolean isLeaf(Object node) {
            return getChildCount(node) == 0;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            fireTreeNodesChanged(ttable, path.getParentPath().getPath(), new int[0], new Object[]{path.getLastPathComponent()});
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            int cnt = getChildCount(parent);
            for (int i = 0; i < cnt; i++) {
                if (getChild(parent, i) == child) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listenerList.add(TreeModelListener.class, l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listenerList.remove(TreeModelListener.class, l);
        }

        protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
            fireTreeNode(CHANGED, source, path, childIndices, children);
        }

        protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
            fireTreeNode(INSERTED, source, path, childIndices, children);
        }

        protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
            fireTreeNode(REMOVED, source, path, childIndices, children);
        }

        protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
            fireTreeNode(STRUCTURE_CHANGED, source, path, childIndices, children);
        }

        private void fireTreeNode(int changeType, Object source, Object[] path, int[] childIndices, Object[] children) {
            Object[] listeners = listenerList.getListenerList();
            TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == TreeModelListener.class) {

                    switch (changeType) {
                        case CHANGED:
                            ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
                            break;
                        case INSERTED:
                            ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
                            break;
                        case REMOVED:
                            ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
                            break;
                        case STRUCTURE_CHANGED:
                            ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
                            break;
                        default:
                            break;
                    }

                }
            }
        }
    }

    public ABCPanel(MainPanel mainPanel) {

        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());

        decompiledTextArea = new DecompiledEditorPane(this);
        decompiledTextArea.addTextChangedListener(this::decompiledTextAreaTextChanged);

        decompiledTextArea.setLinkHandler(new LinkHandler() {

            @Override
            public boolean isLink(Token token) {
                return hasDeclaration(token.start);
            }

            @Override
            public void handleLink(Token token) {
                gotoDeclaration(token.start);
            }

            @Override
            public Highlighter.HighlightPainter linkPainter() {
                return decompiledTextArea.linkPainter();
            }
        });

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        decompiledScrollPane = new JScrollPane(decompiledTextArea);

        JPanel iconDecPanel = new JPanel();
        iconDecPanel.setLayout(new BoxLayout(iconDecPanel, BoxLayout.Y_AXIS));
        JPanel iconsPanel = new JPanel();
        iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.X_AXIS));

        JButton newTraitButton = new JButton(View.getIcon("traitadd16"));
        newTraitButton.setMargin(new Insets(5, 5, 5, 5));
        newTraitButton.addActionListener(this::addTraitButtonActionPerformed);
        newTraitButton.setToolTipText(AppStrings.translate("button.addtrait"));
        iconsPanel.add(newTraitButton);

        scriptNameLabel = new JLabel("-");
        scriptNameLabel.setAlignmentX(0);
        iconsPanel.setAlignmentX(0);
        decompiledScrollPane.setAlignmentX(0);
        iconDecPanel.add(scriptNameLabel);
        iconDecPanel.add(iconsPanel);
        iconDecPanel.add(decompiledScrollPane);

        final JPanel decButtonsPan = new JPanel(new FlowLayout());
        decButtonsPan.setBorder(new BevelBorder(BevelBorder.RAISED));
        decButtonsPan.add(editDecompiledButton);
        decButtonsPan.add(experimentalLabel);
        decButtonsPan.add(saveDecompiledButton);
        decButtonsPan.add(cancelDecompiledButton);

        editDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        saveDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        cancelDecompiledButton.setMargin(new Insets(3, 3, 3, 10));

        saveDecompiledButton.addActionListener(this::saveDecompiledButtonActionPerformed);
        editDecompiledButton.addActionListener(this::editDecompiledButtonActionPerformed);
        cancelDecompiledButton.addActionListener(this::cancelDecompiledButtonActionPerformed);

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
        panB.add(decLabel, BorderLayout.NORTH);

        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {

            @Override
            public void connected() {
                decButtonsPan.setVisible(false);
            }

            @Override
            public void disconnected() {
                decButtonsPan.setVisible(true);
            }
        });

        debugPanel = new DebugPanel();

        JPersistentSplitPane sp2;

        panB.add(sp2 = new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, decPanel, debugPanel, Configuration.guiAvm2VarsSplitPaneDividerLocationPercent), BorderLayout.CENTER);
        sp2.setContinuousLayout(true);

        debugPanel.setVisible(false);

        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        splitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, panB, detailPanel, Configuration.guiAvm2SplitPaneDividerLocationPercent);
        splitPane.setContinuousLayout(true);

        decompiledTextArea.changeContentType("text/actionscript");
        decompiledTextArea.setFont(new Font("Monospaced", Font.PLAIN, decompiledTextArea.getFont().getSize()));

        View.addEditorAction(decompiledTextArea, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int multinameIndex = decompiledTextArea.getMultinameUnderCaret();
                if (multinameIndex > -1) {
                    UsageFrame usageFrame = new UsageFrame(abc, multinameIndex, ABCPanel.this, false);
                    usageFrame.setVisible(true);
                }
            }
        }, "find-usages", AppStrings.translate("abc.action.find-usages"), "control U");

        View.addEditorAction(decompiledTextArea, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoDeclaration(decompiledTextArea.getCaretPosition());
            }
        }, "find-declaration", AppStrings.translate("abc.action.find-declaration"), "control B");

        CtrlClickHandler cch = new CtrlClickHandler();
        decompiledTextArea.addKeyListener(cch);
        decompiledTextArea.addMouseListener(cch);
        decompiledTextArea.addMouseMotionListener(cch);

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

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(AppStrings.translate("traits"), navPanel);
        add(splitPane, BorderLayout.CENTER);

        JPanel panConstants = new JPanel();
        panConstants.setLayout(new BorderLayout());

        constantTypeList = new JComboBox<>(new String[]{"UINT", "INT", "DOUBLE", "DECIMAL", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
        constantTable = new JTable();
        if (abc != null) {
            View.autoResizeColWidth(constantTable, new UIntTableModel(abc));
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
                            UsageFrame usageFrame = new UsageFrame(abc, multinameIndex, t, false);
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

    private void decompiledTextAreaTextChanged() {
        setModified(true);
    }

    private boolean isModified() {
        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    private void setModified(boolean value) {
        saveDecompiledButton.setEnabled(value);
        cancelDecompiledButton.setEnabled(value);
    }

    private boolean hasDeclaration(int pos) {
        if (decompiledTextArea == null) {
            return false; //?
        }
        SyntaxDocument sd = (SyntaxDocument) decompiledTextArea.getDocument();
        Token t = sd.getTokenAt(pos);
        if (t == null || (t.type != TokenType.IDENTIFIER && t.type != TokenType.KEYWORD && t.type != TokenType.REGEX)) {
            return false;
        }
        Reference<Integer> abcIndex = new Reference<>(0);
        Reference<Integer> classIndex = new Reference<>(0);
        Reference<Integer> traitIndex = new Reference<>(0);
        Reference<Integer> multinameIndexRef = new Reference<>(0);
        Reference<Boolean> classTrait = new Reference<>(false);

        if (decompiledTextArea.getPropertyTypeAtPos(pos, abcIndex, classIndex, traitIndex, classTrait, multinameIndexRef)) {
            return true;
        }
        int multinameIndex = decompiledTextArea.getMultinameAtPos(pos);
        if (multinameIndex > -1) {
            if (multinameIndex == 0) {
                return false;
            }
            List<MultinameUsage> usages = abc.findMultinameDefinition(multinameIndex);

            Multiname m = abc.constants.getMultiname(multinameIndex);
            //search other ABC tags if this is not private multiname
            if (m.namespace_index > 0 && abc.constants.getNamespace(m.namespace_index).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == abc) {
                        continue;
                    }
                    int mid = a.constants.getMultinameId(m, false);
                    if (mid > 0) {
                        usages.addAll(a.findMultinameDefinition(mid));
                    }
                }
            }

            //more than one? display list
            if (!usages.isEmpty()) {
                return true;
            }
        }

        return decompiledTextArea.getLocalDeclarationOfPos(pos, new Reference<>(null)) != -1;
    }

    private void gotoDeclaration(int pos) {
        Reference<Integer> abcIndex = new Reference<>(0);
        Reference<Integer> classIndex = new Reference<>(0);
        Reference<Integer> traitIndex = new Reference<>(0);
        Reference<Boolean> classTrait = new Reference<>(false);
        Reference<Integer> multinameIndexRef = new Reference<>(0);

        if (decompiledTextArea.getPropertyTypeAtPos(pos, abcIndex, classIndex, traitIndex, classTrait, multinameIndexRef)) {
            UsageFrame.gotoUsage(ABCPanel.this, new TraitMultinameUsage(getAbcList().get(abcIndex.getVal()).getABC(), multinameIndexRef.getVal(), classIndex.getVal(), traitIndex.getVal(), classTrait.getVal(), null, -1) {
            });
            return;
        }
        int multinameIndex = decompiledTextArea.getMultinameAtPos(pos);
        if (multinameIndex > -1) {
            List<MultinameUsage> usages = abc.findMultinameDefinition(multinameIndex);

            Multiname m = abc.constants.getMultiname(multinameIndex);
            //search other ABC tags if this is not private multiname
            if (m.namespace_index > 0 && abc.constants.getNamespace(m.namespace_index).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == abc) {
                        continue;
                    }
                    int mid = a.constants.getMultinameId(m, false);
                    if (mid > 0) {
                        usages.addAll(a.findMultinameDefinition(mid));
                    }
                }
            }

            //more than one? display list
            if (usages.size() > 1) {
                UsageFrame usageFrame = new UsageFrame(abc, multinameIndex, ABCPanel.this, true);
                usageFrame.setVisible(true);
                return;
            } else if (!usages.isEmpty()) { //one
                UsageFrame.gotoUsage(ABCPanel.this, usages.get(0));
                return;
            }
        }

        int dpos = decompiledTextArea.getLocalDeclarationOfPos(pos, new Reference<>(null));
        if (dpos > -1) {
            decompiledTextArea.setCaretPosition(dpos);

        }

    }

    private class CtrlClickHandler extends KeyAdapter implements MouseListener, MouseMotionListener {

        private boolean ctrlDown = false;

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 17 && !decompiledTextArea.isEditable()) {
                ctrlDown = true;
                //decompiledTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 17) {
                ctrlDown = false;
                //decompiledTextArea.setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (ctrlDown && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1 && !decompiledTextArea.isEditable()) {
                ctrlDown = false;
                //decompiledTextArea.setCursor(Cursor.getDefaultCursor());
                //gotoDeclaration();
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
        lastDecompiled = "";
        SWF swf = getSwf();
        if (swf != null) {
            swf.clearScriptCache();
        }

        decompiledTextArea.reloadClass();
        detailPanel.methodTraitPanel.methodCodePanel.clear();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
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
        TreeItem scriptsNode = ttm.getScriptsNode(swf);
        if (scriptsNode instanceof ClassesListTreeModel) {
            ClassesListTreeModel clModel = (ClassesListTreeModel) scriptsNode;
            ScriptPack pack = null;
            for (ScriptPack item : clModel.getList()) {
                if (!item.isSimple && Configuration.ignoreCLikePackages.get()) {
                    continue;
                }
                ClassPath classPath = item.getClassPath();

                // first check the className to avoid calling unnecessary toString
                if (name.endsWith(classPath.className) && classPath.toString().equals(name)) {
                    pack = item;
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
        final TreePath tp = ttm.getTreePath(pack);
        View.execInEventDispatchLater(() -> {
            mainPanel.tagTree.setSelectionPath(tp);
            mainPanel.tagTree.scrollPathToVisible(tp);
        });

    }

    @Override
    public void updateSearchPos(ABCPanelSearchResult item) {
        ScriptPack pack = item.getScriptPack();
        setAbc(pack.abc);
        decompiledTextArea.setScript(pack, false);
        hilightScript(pack);
        decompiledTextArea.setCaretPosition(0);

        View.execInEventDispatchLater(() -> {
            searchPanel.showQuickFindDialog(decompiledTextArea);
        });

    }

    public boolean isDirectEditing() {
        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    public void setDecompiledEditMode(boolean val) {
        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                if (val) {
                    lastDecompiled = decompiledTextArea.getText();
                } else {
                    decompiledTextArea.setText(lastDecompiled);
                }

                decompiledTextArea.setEditable(val);
                saveDecompiledButton.setVisible(val);
                saveDecompiledButton.setEnabled(false);
                editDecompiledButton.setVisible(!val);
                experimentalLabel.setVisible(!val);
                cancelDecompiledButton.setVisible(val);
                decompiledTextArea.getCaret().setVisible(true);
                decLabel.setIcon(val ? View.getIcon("editing16") : null);
                detailPanel.setVisible(!val);

                decompiledTextArea.ignoreCarret = val;
                decompiledTextArea.requestFocusInWindow();
            }
        });

    }

    private void editDecompiledButtonActionPerformed(ActionEvent evt) {
        File swc = Configuration.getPlayerSWC();
        if (swc == null || !swc.exists()) {
            if (View.showConfirmDialog(this, AppStrings.translate("message.playerpath.lib.notset"), AppStrings.translate("message.action.playerglobal.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                Main.advancedSettings("paths");
                return;
            }
        }
        if (View.showConfirmDialog(null, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS3Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            setDecompiledEditMode(true);
        }
    }

    private void cancelDecompiledButtonActionPerformed(ActionEvent evt) {
        setDecompiledEditMode(false);
    }

    private void saveDecompiledButtonActionPerformed(ActionEvent evt) {
        ScriptPack pack = decompiledTextArea.getScriptLeaf();
        int oldIndex = pack.scriptIndex;
        SWF.uncache(pack);

        try {
            String oldSp = pack.getClassPath().toString();
            /*List<ScriptPack> packs = abc.script_info.get(oldIndex).getPacks(abc, oldIndex, null, pack.allABCs);
             if (!packs.isEmpty()) {

             }*/

            String as = decompiledTextArea.getText();
            abc.replaceScriptPack(pack, as);
            lastDecompiled = as;
            setDecompiledEditMode(false);
            mainPanel.updateClassesList();

            if (oldSp != null) {
                hilightScript(getSwf(), oldSp);
            }

            reload();
            View.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
        } catch (AVM2ParseException ex) {
            abc.script_info.get(oldIndex).delete(abc, false);
            decompiledTextArea.gotoLine((int) ex.line);
            decompiledTextArea.markError();
            View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (CompilationException ex) {
            abc.script_info.get(oldIndex).delete(abc, false);
            decompiledTextArea.gotoLine((int) ex.line);
            decompiledTextArea.markError();
            View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);

        } catch (Throwable ex) {
            Logger.getLogger(ABCPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addTraitButtonActionPerformed(ActionEvent evt) {
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
            if (newTraitDialog.showDialog() != AppDialog.OK_OPTION) {
                return;
            }
            kind = newTraitDialog.getTraitType();
            nskind = newTraitDialog.getNamespaceKind();
            name = newTraitDialog.getTraitName();
            isStatic = newTraitDialog.getStatic();
            m = Multiname.createQName(false, abc.constants.getStringId(name, true), abc.constants.getNamespaceId(nskind, "", 0, true));
            int mid = abc.constants.getMultinameId(m, false);
            if (mid <= 0) {
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
                MethodBody body = new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]);
                body.method_info = method_info;
                body.init_scope_depth = 1;
                body.max_regs = 1;
                body.max_scope_depth = 1;
                body.max_stack = 1;
                body.exceptions = new ABCException[0];
                AVM2Code code = new AVM2Code();
                code.code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                code.code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
                code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
                body.setCode(code);
                Traits traits = new Traits();
                traits.traits = new ArrayList<>();
                body.traits = traits;
                abc.addMethodBody(body);
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
            int scriptIndex = decompiledTextArea.getScriptLeaf().scriptIndex;
            if (scriptIndex >= 0 && scriptIndex < abc.script_info.size()) {
                abc.script_info.get(scriptIndex).setModified(true);
            }
            ((Tag) abc.parentTag).setModified(true);
            reload();
            decompiledTextArea.gotoTrait(traitId);
        }
    }

    @Override
    public boolean tryAutoSave() {
        // todo: implement
        return false;
    }

    @Override
    public boolean isEditing() {
        return detailPanel.isEditing() || isModified();
    }
}
