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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.VariableFlags;
import com.jpexs.debugger.flash.VariableType;
import com.jpexs.debugger.flash.messages.in.InGetVariable;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.helpers.Reference;
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
import com.jpexs.decompiler.flash.action.deobfuscation.BrokenScriptDetector;
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
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceExceptionItem;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.importers.FFDecAs3ScriptReplacer;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import com.jpexs.decompiler.flash.search.ActionScriptSearch;
import com.jpexs.decompiler.flash.search.ScriptSearchListener;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import de.hameister.treetable.MyTreeTable;
import de.hameister.treetable.MyTreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
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
public class ABCPanel extends JPanel implements ItemListener, SearchListener<ABCSearchResult>, TagEditorPanel {

    private As3ScriptReplacerInterface scriptReplacer = null;

    private ScriptPack pack = null;

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

    public final JPanel brokenHintPanel;

    public final JTabbedPane tabbedPane;

    public final SearchPanel<ABCSearchResult> searchPanel;

    private NewTraitDialog newTraitDialog;

    public final JLabel scriptNameLabel;

    private final DebugPanel debugPanel;

    private final JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));

    private final JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit.script.decompiled"), View.getIcon("edit16"));

    private final JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    private final JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    private String lastDecompiled = null;

    public MainPanel getMainPanel() {
        View.checkAccess();

        return mainPanel;
    }

    public List<ABCSearchResult> search(final SWF swf, final String txt, boolean ignoreCase, boolean regexp, boolean pcode, CancellableWorker<Void> worker) {
        if (txt != null && !txt.isEmpty()) {
            searchPanel.setOptions(ignoreCase, regexp);

            String workText = AppStrings.translate("work.searching");
            String decAdd = AppStrings.translate("work.decompiling");
            return new ActionScriptSearch().searchAs3(swf, txt, ignoreCase, regexp, pcode, new ScriptSearchListener() {
                @Override
                public void onDecompile(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\", " + decAdd + " - (" + pos + "/" + total + ") " + name + "... ", worker);
                }

                @Override
                public void onSearch(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\" - (" + pos + "/" + total + ") " + name + "... ", worker);
                }
            });
        }

        return null;
    }

    public void setAbc(ABC abc) {
        View.checkAccess();

        if (abc == this.abc) {
            return;
        }

        this.abc = abc;
        setDecompiledEditMode(false);
        navigator.setAbc(abc);
        updateConstList();
    }

    public void updateConstList() {
        View.checkAccess();

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
        View.checkAccess();

        this.abc = null;
        constantTable.setModel(new DefaultTableModel());
        navigator.clearAbc();
        decompiledTextArea.clearScript();
    }

    public static class VariableNode {

        public List<VariableNode> path = new ArrayList<>();

        public Variable var;

        public Variable varInsideGetter;

        public Long parentObjectId;

        public int level;

        public Variable trait;

        public long traitId;

        private List<VariableNode> childs;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.parentObjectId);
            hash = 53 * hash + (this.var == null ? 0 : Objects.hashCode(this.var.name));
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
            if (!Objects.equals(this.parentObjectId, other.parentObjectId)) {
                return false;
            }
            if (this.var == null && other.var == null) {
                return true;
            }
            if (this.var == null) {
                return false;
            }
            if (other.var == null) {
                return false;
            }
            return Objects.equals(this.var.name, other.var.name);
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
            return var.name;
        }

        private void refresh() {
            if (path.size() > 1) {
                path.get(path.size() - 2).reloadChildren();
            }
        }

        private void reloadChildren() {
            childs = new ArrayList<>();

            if ("".equals(var.name)) {
                return;
            }
            InGetVariable igv;

            Long objectId = varToObjectId(varInsideGetter);

            if (parentObjectId == 0 && objectId != 0) {
                igv = Main.getDebugHandler().getVariable(objectId, "", true);
            } else {
                igv = Main.getDebugHandler().getVariable(parentObjectId, var.name, true);
            }

            //current var is getter function - set it to value really got
            if ((var.flags & VariableFlags.HAS_GETTER) > 0) {
                varInsideGetter = igv.parent;
            }

            Variable curTrait = null;
            for (int i = 0; i < igv.childs.size(); i++) {
                if (!isTraits(igv.childs.get(i))) {
                    Long parentObjectId = varToObjectId(var);
                    childs.add(new VariableNode(path, level + 1, igv.childs.get(i), parentObjectId, curTrait));//igv.parentId
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
            return childs.get(index);
        }

        public int getChildCount() {
            ensureLoaded();
            return childs.size();
        }

        public VariableNode(List<VariableNode> parentPath, int level, Variable var, Long parentObjectId, Variable trait) {
            this.var = var;
            this.varInsideGetter = var;
            this.parentObjectId = parentObjectId;
            this.level = level;
            this.trait = trait;
            this.path.addAll(parentPath);
            this.path.add(this);
            loaded = false;
        }

        public VariableNode(List<VariableNode> parentPath, int level, Variable var, Long parentObjectId, Variable trait, List<VariableNode> subvars) {
            this.var = var;
            this.varInsideGetter = var;
            this.parentObjectId = parentObjectId;
            this.level = level;
            this.trait = trait;

            this.childs = subvars;
            this.path.addAll(parentPath);
            this.path.add(this);
            for (VariableNode vn : subvars) {
                vn.path.clear();
                vn.path.addAll(this.path);
                vn.path.add(vn);
            }
            loaded = true;
        }
    }

    public static Long varToObjectId(Variable var) {
        if (var != null && (var.vType == VariableType.OBJECT)) //|| var.vType == VariableType.MOVIECLIP)) {
        {
            return (Long) var.value;
        } else {
            return 0L;
        }
    }

    public static class VariablesTableModel implements MyTreeTableModel {

        List<TableModelListener> tableListeners = new ArrayList<>();

        VariableNode root;

        private final Map<VariableNode, List<VariableNode>> nodeCache = new HashMap<>();

        protected EventListenerList listenerList = new EventListenerList();

        private static final int CHANGED = 0;

        private static final int INSERTED = 1;

        private static final int REMOVED = 2;

        private static final int STRUCTURE_CHANGED = 3;

        private final MyTreeTable ttable;

        public VariablesTableModel(MyTreeTable ttable, List<Variable> vars, List<Long> parentIds) {
            this.ttable = ttable;

            List<VariableNode> childs = new ArrayList<>();

            for (int i = 0; i < vars.size(); i++) {
                childs.add(new VariableNode(new ArrayList<>(), 1, vars.get(i), 0L/*parentIds.get(i)*/, null));
            }
            root = new VariableNode(new ArrayList<>(), 0, null, 0L, null, childs);
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        public Variable getVarAt(Object node) {
            if (node == root) {
                return null;
            }
            return ((VariableNode) node).var;
        }

        private static final int COLUMN_NAME = 0;

        private static final int COLUMN_TRAIT = 1;

        private static final int COLUMN_SCOPE = 2;

        private static final int COLUMN_FLAGS = 3;

        private static final int COLUMN_TYPE = 4;

        private static final int COLUMN_VALUE = 5;

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case COLUMN_NAME:
                    return AppStrings.translate("variables.column.name");
                case COLUMN_SCOPE:
                    return AppStrings.translate("variables.column.scope");
                case COLUMN_FLAGS:
                    return AppStrings.translate("variables.column.flags");
                case COLUMN_TYPE:
                    return AppStrings.translate("variables.column.type");
                case COLUMN_VALUE:
                    return AppStrings.translate("variables.column.value");
                case COLUMN_TRAIT:
                    return AppStrings.translate("variables.column.trait");
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == COLUMN_NAME) {
                return MyTreeTableModel.class;
            }
            return String.class;
        }

        private String flagsToScopeString(int flags) {
            int scope = flags & VariableFlags.SCOPE_MASK;
            switch (scope) {
                case VariableFlags.PRIVATE_SCOPE:
                    return "private";
                case VariableFlags.PROTECTED_SCOPE:
                    return "protected";
                case VariableFlags.PUBLIC_SCOPE:
                    return "public";
                case VariableFlags.NAMESPACE_SCOPE:
                    return "namespace";
                case VariableFlags.INTERNAL_SCOPE:
                    return "internal";
                default:
                    return "?";
            }
        }

        /*
        1 DONT_ENUMERATE
        2 ?
        4 READ_ONLY
        8 ?
        16 ?
        32 IS_LOCAL
        64
        128
        256
        512
        1024
        2048
        4096
        8192
        16384
        32768
        65536 IS_ARGUMENT
        131072 IS_DYNAMIC
        262144 IS_EXCEPTION
        524288 HAS_GETTER
        1048576 HAS_SETTER
        2097152 IS_STATIC
        4194304 IS_CONST
        8388608,16777216,33554432 SCOPE
        67108864 IS_CLASS
         */
        private String flagsToString(int flags) {

            Integer unknownFlags[] = new Integer[]{
                2,
                8,
                16,
                64,
                128,
                256,
                512,
                1024,
                2048,
                4096,
                8192,
                16384,
                32768
            };
            List<String> flagsStr = new ArrayList<>();

            if ((flags & VariableFlags.DONT_ENUMERATE) > 0) {
                flagsStr.add("dontEnumerate");
            }
            for (Integer f : unknownFlags) {
                if ((flags & f) > 0) {
                    flagsStr.add("unk" + f);
                }
            }
            if ((flags & VariableFlags.HAS_GETTER) > 0) {
                flagsStr.add("get");
            }
            if ((flags & VariableFlags.HAS_SETTER) > 0) {
                flagsStr.add("set");
            }
            if ((flags & VariableFlags.READ_ONLY) > 0) {
                flagsStr.add("readonly");
            }
            if ((flags & VariableFlags.IS_CONST) > 0) {
                flagsStr.add("const");
            }
            if ((flags & VariableFlags.IS_DYNAMIC) > 0) {
                flagsStr.add("dynamic");
            }
            if ((flags & VariableFlags.IS_CLASS) > 0) {
                flagsStr.add("class");
            }
            if ((flags & VariableFlags.IS_ARGUMENT) > 0) {
                flagsStr.add("argument");
            }
            if ((flags & VariableFlags.IS_EXCEPTION) > 0) {
                flagsStr.add("exception");
            }
            if ((flags & VariableFlags.IS_LOCAL) > 0) {
                flagsStr.add("local");
            }
            if ((flags & VariableFlags.IS_STATIC) > 0) {
                flagsStr.add("static");
            }

            return String.join(", ", flagsStr);
        }

        @Override
        public Object getValueAt(Object node, int columnIndex) {
            if (node == root) {
                if (columnIndex == 0) {
                    return "root";
                }
                return "";
            }
            Variable var = ((VariableNode) node).var;
            Variable var_getter = ((VariableNode) node).varInsideGetter;
            Variable trait = ((VariableNode) node).trait;
            boolean readOnly = (var.flags & VariableFlags.READ_ONLY) > 0;
            boolean hasGetter = (var.flags & VariableFlags.HAS_GETTER) > 0;
            boolean hasSetter = (var.flags & VariableFlags.HAS_SETTER) > 0;
            boolean onlySetter = hasSetter && !hasGetter;

            //String flagStr = flagsToString(var.flags);
            Variable val = var;
            if (var_getter != null) {
                val = var_getter;
            }

            switch (columnIndex) {
                case COLUMN_NAME:
                    return var.name;
                case COLUMN_SCOPE:
                    return flagsToScopeString(var.flags);
                case COLUMN_FLAGS:
                    return flagsToString(var.flags);
                case COLUMN_TYPE:
                    String typeStr = val.getTypeAsStr();
                    if ("Object".equals(typeStr)) {
                        typeStr = val.className;
                    }
                    if ("Object".equals(typeStr)) {
                        typeStr = val.typeName;
                    }
                    return typeStr;
                case COLUMN_VALUE:
                    switch (val.vType) {
                        case VariableType.OBJECT:
                        case VariableType.MOVIECLIP:
                        case VariableType.FUNCTION:
                            return var.getTypeAsStr() + "(" + val.value + ")";
                        case VariableType.STRING:
                            return "\"" + Helper.escapeActionScriptString("" + val.value) + "\"";
                        default:
                            return EcmaScript.toString(val.value);
                    }
                case COLUMN_TRAIT:
                    if (trait != null) {
                        return trait.name;
                    }
                    return "";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return column == COLUMN_NAME || (column == COLUMN_VALUE && node != root && ((VariableNode) node).var.isPrimitive);
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
            Main.getDebugHandler().setVariable(((VariableNode) node).parentObjectId, ((VariableNode) node).var.name, valType, symb.value);
            //((VariableNode) node).refresh();
            Object[] path = new Object[((VariableNode) node).path.size()];
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

        public String tryGetDebugHoverToolTipText(String varName) {
            String lowerName = varName.toLowerCase();
            StringBuilder builder = new StringBuilder();

            findVarAndAppendDataToString(root, lowerName, builder);
            String text = builder.toString();

            if (text == null || text.isEmpty()) {
                return null;
            } else {
                return "<html>" + text + "</html>";
            }
        }

        private void findVarAndAppendDataToString(VariableNode node, String lowerVarName, StringBuilder builder) {
            if (node.var != null && node.var.name.toLowerCase().contains(lowerVarName)) {
                builder.append(node.var.name + ": " + node.var.getValueAsStr() + "<br>");
            }

            if (node.childs != null) {
                for (int i = 0; i < node.childs.size(); i++) {
                    findVarAndAppendDataToString(node.childs.get(i), lowerVarName, builder);
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

        // Register the component on the tooltip manager
        // So that #getToolTipText(MouseEvent) gets invoked when the mouse
        // hovers the component, and we can show debug information
        ToolTipManager.sharedInstance().registerComponent(decompiledTextArea);
        decompiledTextArea.addMouseListener(new MouseAdapter() {
            final int initialTimeout = ToolTipManager.sharedInstance().getInitialDelay();
            final int dismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();

            @Override
            public void mouseEntered(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(0);
                ToolTipManager.sharedInstance().setDismissDelay(1000 * 1000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(initialTimeout);
                ToolTipManager.sharedInstance().setDismissDelay(dismissTimeout);
            }
        });

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        brokenHintPanel = new JPanel(new BorderLayout(10, 10));
        brokenHintPanel.add(new JLabel("<html>" + AppStrings.translate("script.seemsBroken") + "</html>"), BorderLayout.CENTER);
        brokenHintPanel.setBackground(new Color(253, 205, 137));
        brokenHintPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), new EmptyBorder(5, 5, 5, 5)));

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

        JPanel panelWithHint = new JPanel(new BorderLayout());
        panelWithHint.setAlignmentX(0);
        panelWithHint.add(brokenHintPanel, BorderLayout.NORTH);
        panelWithHint.add(decompiledScrollPane, BorderLayout.CENTER);

        iconDecPanel.add(panelWithHint);
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
        decompiledTextArea.setFont(Configuration.getSourceFont());

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
        View.checkAccess();

        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    private void setModified(boolean value) {
        View.checkAccess();

        saveDecompiledButton.setEnabled(value);
        cancelDecompiledButton.setEnabled(value);
    }

    private boolean hasDeclaration(int pos) {
        if (decompiledTextArea == null) {
            return false; //?
        }
        SyntaxDocument sd = (SyntaxDocument) decompiledTextArea.getDocument();
        Token currentChartoken = sd.getTokenAt(pos);
        Token nextChartoken = sd.getTokenAt(pos + 1);

        Token t = currentChartoken != null && currentChartoken.length == 1 ? currentChartoken : nextChartoken;
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
            if (m.getSingleNamespaceIndex(abc.constants) > 0 && abc.constants.getNamespace(m.getSingleNamespaceIndex(abc.constants)).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == abc) {
                        continue;
                    }

                    int mid = a.constants.getMultinameId(m, abc.constants);
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
        View.checkAccess();

        Reference<Integer> abcIndex = new Reference<>(0);
        Reference<Integer> classIndex = new Reference<>(0);
        Reference<Integer> traitIndex = new Reference<>(0);
        Reference<Boolean> classTrait = new Reference<>(false);
        Reference<Integer> multinameIndexRef = new Reference<>(0);

        if (decompiledTextArea.getPropertyTypeAtPos(pos, abcIndex, classIndex, traitIndex, classTrait, multinameIndexRef)) {
            UsageFrame.gotoUsage(ABCPanel.this, new TraitMultinameUsage(getAbcList().get(abcIndex.getVal()).getABC(), multinameIndexRef.getVal(), decompiledTextArea.getScriptLeaf().scriptIndex, classIndex.getVal(), traitIndex.getVal(), classTrait.getVal() ? TraitMultinameUsage.TRAITS_TYPE_CLASS : TraitMultinameUsage.TRAITS_TYPE_INSTANCE, null, -1) {
            });
            return;
        }
        int multinameIndex = decompiledTextArea.getMultinameAtPos(pos);
        if (multinameIndex > -1) {
            List<MultinameUsage> usages = abc.findMultinameDefinition(multinameIndex);

            Multiname m = abc.constants.getMultiname(multinameIndex);
            //search other ABC tags if this is not private multiname
            if (m.getSingleNamespaceIndex(abc.constants) > 0 && m.getSingleNamespace(abc.constants).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == abc) {
                        continue;
                    }
                    int mid = a.constants.getMultinameId(m, abc.constants);
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
        View.checkAccess();

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
        View.checkAccess();

        if (e.getSource() == constantTypeList) {
            int index = ((JComboBox) e.getSource()).getSelectedIndex();
            if (index == -1) {
                return;
            }
            updateConstList();
        }
    }

    public void display() {
        View.checkAccess();

        setVisible(true);
    }

    public void hilightScript(SWF swf, String name) {
        View.checkAccess();

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
                if (name.endsWith(classPath.className + classPath.namespaceSuffix) && classPath.toRawString().equals(name)) {
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
        View.checkAccess();

        TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
        TreePath tp = ttm.getTreePath(pack);
        if (tp == null) {
            mainPanel.closeTagTreeSearch();
            tp = ttm.getTreePath(pack);
        }

        mainPanel.tagTree.setSelectionPath(tp);
        mainPanel.tagTree.scrollPathToVisible(tp);
    }

    @Override
    public void updateSearchPos(ABCSearchResult item) {
        View.checkAccess();

        ScriptPack pack = item.getScriptPack();
        setAbc(pack.abc);

        Runnable setScriptComplete = new Runnable() {
            @Override
            public void run() {
                decompiledTextArea.removeScriptListener(this);
                hilightScript(pack);

                boolean pcode = item.isPcode();
                if (pcode) {
                    decompiledTextArea.setClassIndex(item.getClassIndex());
                    decompiledTextArea.gotoTrait(item.getTraitId());
                } else {
                    decompiledTextArea.setCaretPosition(0);
                }

                decompiledTextArea.caretUpdate(null);

                if (pcode) {
                    searchPanel.showQuickFindDialog(detailPanel.methodTraitPanel.methodCodePanel.getSourceTextArea());
                } else {
                    searchPanel.showQuickFindDialog(decompiledTextArea);
                }
            }
        };

        decompiledTextArea.addScriptListener(setScriptComplete);

        decompiledTextArea.setScript(pack, false);
    }

    public boolean isDirectEditing() {
        View.checkAccess();

        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    public void setDecompiledEditMode(boolean val) {
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

    private void editDecompiledButtonActionPerformed(ActionEvent evt) {
        scriptReplacer = mainPanel.getAs3ScriptReplacer();
        if (scriptReplacer == null) {
            return;
        }

        if (View.showConfirmDialog(null, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS3Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            pack = decompiledTextArea.getScriptLeaf();
            setDecompiledEditMode(true);
            SwingWorker initReplaceWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    scriptReplacer.initReplacement(pack);
                    return null;
                }
            };
            initReplaceWorker.execute();
        }
    }

    private void cancelDecompiledButtonActionPerformed(ActionEvent evt) {
        setDecompiledEditMode(false);
        if (scriptReplacer != null) {
            scriptReplacer.deinitReplacement(pack);
        }
    }

    private void saveDecompiledButtonActionPerformed(ActionEvent evt) {
        int oldIndex = pack.scriptIndex;
        SWF.uncache(pack);
        try {
            String oldSp = pack.getClassPath().toRawString();
            String as = decompiledTextArea.getText();
            abc.replaceScriptPack(scriptReplacer, pack, as);
            scriptReplacer.deinitReplacement(pack);
            lastDecompiled = as;
            setDecompiledEditMode(false);
            mainPanel.updateClassesList();

            if (oldSp != null) {
                hilightScript(getSwf(), oldSp);
            }

            reload();
            View.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
        } catch (As3ScriptReplaceException asre) {
            StringBuilder sb = new StringBuilder();
            int firstErrorLine = As3ScriptReplaceExceptionItem.LINE_UNKNOWN;
            int firstErrorCol = As3ScriptReplaceExceptionItem.COL_UNKNOWN;
            String firstErrorText = null;
            abc.script_info.get(oldIndex).delete(abc, false);

            for (As3ScriptReplaceExceptionItem item : asre.getExceptionItems()) {
                if (firstErrorLine == As3ScriptReplaceExceptionItem.LINE_UNKNOWN) {
                    firstErrorLine = item.getLine();
                    firstErrorCol = item.getCol();
                }
                if (firstErrorText == null) {
                    firstErrorText = item.getMessage();
                }
                sb.append(item.getFile()).append(":").append(item.getLine()).append(" (column ").append(item.getCol()).append(")").append(":").append("\n");
                sb.append("  ").append(item.getMessage());
                sb.append("\n");
                sb.append("\n");
            }
            if (firstErrorLine != As3ScriptReplaceExceptionItem.LINE_UNKNOWN) {
                if (firstErrorCol != As3ScriptReplaceExceptionItem.COL_UNKNOWN) {
                    decompiledTextArea.gotoLineCol(firstErrorLine, firstErrorCol);
                } else {
                    decompiledTextArea.gotoLine(firstErrorLine);
                }
                decompiledTextArea.markError();
            }

            if (scriptReplacer instanceof FFDecAs3ScriptReplacer) { //oldStyle error display - single error, no column
                View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", firstErrorText).replace("%line%", Long.toString(firstErrorLine)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            } else {
                View.showMessageDialog(this, sb.toString(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            }
            decompiledTextArea.requestFocus();

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
        View.checkAccess();

        // todo: implement
        return false;
    }

    @Override
    public boolean isEditing() {
        View.checkAccess();

        return detailPanel.isEditing() || isModified();
    }

    public DebugPanel getDebugPanel() {
        return debugPanel;
    }
}
