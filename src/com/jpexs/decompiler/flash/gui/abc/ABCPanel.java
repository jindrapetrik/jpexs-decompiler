/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.multinames.MultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.multinames.TraitMultinameUsage;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItemChangeListener;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DebugPanel;
import com.jpexs.decompiler.flash.gui.DebuggerHandler;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.SearchListener;
import com.jpexs.decompiler.flash.gui.SearchPanel;
import com.jpexs.decompiler.flash.gui.TagEditorPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.editor.LinkHandler;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTreeModel;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceExceptionItem;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.importers.FFDecAs3ScriptReplacer;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import com.jpexs.decompiler.flash.search.ActionScriptSearch;
import com.jpexs.decompiler.flash.search.ScriptSearchListener;
import com.jpexs.decompiler.flash.search.ScriptSearchResult;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import de.hameister.treetable.MyTreeTable;
import de.hameister.treetable.MyTreeTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
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
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreePath;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 * @author JPEXS
 */
public class ABCPanel extends JPanel implements ItemListener, SearchListener<ScriptSearchResult>, TagEditorPanel {

    private As3ScriptReplacerInterface scriptReplacer = null;

    private ScriptPack pack = null;

    private final MainPanel mainPanel;

    public final TraitsList navigator;

    public ABC abc;

    private final JPanel toolbarPanel;

    private final JComboBox<String> libraryComboBox;

    public final DecompiledEditorPane decompiledTextArea;

    public final JScrollPane decompiledScrollPane;

    private final JPersistentSplitPane splitPane;

    public JComboBox<String> constantTypeList;

    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));

    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));

    public final DetailPanel detailPanel;

    public final JPanel navigatorPanel;

    public final JPanel brokenHintPanel;

    public final SearchPanel<ScriptSearchResult> searchPanel;

    private NewTraitDialog newTraitDialog;

    public final JLabel scriptNameLabel;

    private final DebugPanel debugPanel;

    private final JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));

    private final JLabel flexLabel = new JLabel(AppStrings.translate("action.edit.flex"));

    private final JLabel infoNotEditableLabel;

    private final JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit.script.decompiled"), View.getIcon("edit16"));

    private final JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    private final JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    private String lastDecompiled = null;

    private JLabel linksLabel = new JLabel("");

    public MainPanel getMainPanel() {
        View.checkAccess();

        return mainPanel;
    }

    public ScriptPack getPack() {
        return decompiledTextArea.getScriptLeaf();
    }

    public List<ABCSearchResult> search(final Openable openable, final String txt, boolean ignoreCase, boolean regexp, boolean pcode, CancellableWorker<Void> worker, List<ScriptPack> scope) {
        if (txt != null && !txt.isEmpty()) {
            searchPanel.setOptions(ignoreCase, regexp);

            String workText = AppStrings.translate("work.searching");
            String decAdd = AppStrings.translate("work.decompiling");
            return new ActionScriptSearch().searchAs3(openable, txt, ignoreCase, regexp, pcode, new ScriptSearchListener() {
                @Override
                public void onDecompile(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\", " + decAdd + " - (" + pos + "/" + total + ") " + name + "... ", worker);
                }

                @Override
                public void onSearch(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\" - (" + pos + "/" + total + ") " + name + "... ", worker);
                }
            }, scope);
        }

        return null;
    }

    public void setAbc(ABC abc) {
        View.checkAccess();

        if (abc == this.abc) {
            return;
        }

        if (Main.isSwfAir(abc.getOpenable())) {
            libraryComboBox.setSelectedIndex(SWF.LIBRARY_AIR);
        } else {
            libraryComboBox.setSelectedIndex(SWF.LIBRARY_FLASH);
        }
        this.abc = abc;
        setDecompiledEditMode(false);
        navigator.setAbc(abc);
        updateLinksLabel();
    }

    public SWF getSwf() {
        return abc == null ? null : abc.getSwf();
    }

    public Openable getOpenable() {
        return abc == null ? null : abc.getOpenable();
    }

    public List<ABCContainerTag> getAbcList() {
        SWF swf = getSwf();
        return swf == null ? null : swf.getAbcList();
    }

    public void clearSwf() {
        View.checkAccess();

        this.abc = null;
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

        public List<Variable> traits = new ArrayList<>();

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
            traits = new ArrayList<>();

            if ("".equals(var.name)) {
                return;
            }
            InGetVariable igv;

            Long objectId = varToObjectId(var);

            boolean useGetter = (var.flags & VariableFlags.IS_CONST) == 0;

            boolean isAS3 = (Main.getMainFrame().getPanel().getCurrentSwf().isAS3());

            if (parentObjectId == 0 && objectId != 0L && isAS3) {
                igv = Main.getDebugHandler().getVariable(objectId, "", true, useGetter);
            } else {
                igv = Main.getDebugHandler().getVariable(parentObjectId, var.name, true, useGetter);
            }

            //current var is getter function - set it to value really got
            if ((var.flags & VariableFlags.HAS_GETTER) > 0) {
                varInsideGetter = igv.parent;
            }

            Variable curTrait = null;
            for (int i = 0; i < igv.childs.size(); i++) {
                if (!isTraits(igv.childs.get(i))) {
                    Long parentObjectId = varToObjectId(varInsideGetter);
                    childs.add(new VariableNode(path, level + 1, igv.childs.get(i), parentObjectId, curTrait));
                } else {
                    curTrait = igv.childs.get(i);
                    traits.add(curTrait);
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
        if (var == null) {
            return 0L;
        }
        if (var.vType == VariableType.OBJECT) {
            return (Long) var.value;
        }
        if (var.vType == VariableType.MOVIECLIP) {
            return (Long) var.value;
        }
        return 0L;
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

        public VariablesTableModel(MyTreeTable ttable, List<Variable> vars) {
            this.ttable = ttable;

            List<VariableNode> childs = new ArrayList<>();

            for (int i = 0; i < vars.size(); i++) {
                childs.add(new VariableNode(new ArrayList<>(), 1, vars.get(i), 0L, null));
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

            Integer[] unknownFlags = new Integer[]{
                8,
                16,
                64,
                512,
                2048,
                16384,
                32768
            };
            List<String> flagsStr = new ArrayList<>();

            if ((flags & VariableFlags.DONT_ENUMERATE) > 0) {
                flagsStr.add("dontEnumerate");
            }

            if ((flags & VariableFlags.DONT_DELETE) > 0) {
                flagsStr.add("dontDelete");
            }

            if ((flags & VariableFlags.ONLY_SWF6_UP) > 0) {
                flagsStr.add("onlySWF6Up");
            }

            if ((flags & VariableFlags.IGNORE_SWF6) > 0) {
                flagsStr.add("ignoreSWF6");
            }

            if ((flags & VariableFlags.ONLY_SWF7_UP) > 0) {
                flagsStr.add("onlySWF7Up");
            }

            if ((flags & VariableFlags.ONLY_SWF8_UP) > 0) {
                flagsStr.add("onlySWF8Up");
            }

            if ((flags & VariableFlags.ONLY_SWF9_UP) > 0) {
                flagsStr.add("onlySWF9Up");
            }

            if ((flags & VariableFlags.ONLY_SWF10_UP) > 0) {
                flagsStr.add("onlySWF10Up");
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
                return hasDeclaration(token);
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
        decompiledTextArea.addScriptListener(new Runnable() {
            @Override
            public void run() {
                lastDecompiled = decompiledTextArea.getText();
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

        decompiledScrollPane = new FasterScrollPane(decompiledTextArea);

        JPanel iconDecPanel = new JPanel();
        iconDecPanel.setLayout(new BorderLayout());
        JPanel iconsPanel = new JPanel(new FlowLayout());
        //iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.X_AXIS));

        JButton newTraitButton = new JButton(View.getIcon("traitadd16"));
        newTraitButton.setMargin(new Insets(5, 5, 5, 5));
        newTraitButton.addActionListener(this::addTraitButtonActionPerformed);
        newTraitButton.setToolTipText(AppStrings.translate("button.addtrait"));
        iconsPanel.add(newTraitButton);

        JButton removeTraitButton = new JButton(View.getIcon("traitremove16"));
        removeTraitButton.setMargin(new Insets(5, 5, 5, 5));
        removeTraitButton.addActionListener(this::removeTraitButtonActionPerformed);
        removeTraitButton.setToolTipText(AppStrings.translate("button.removetrait"));
        iconsPanel.add(removeTraitButton);

        JButton abcExplorerButton = new JButton(View.getIcon("abcexplorer16"));
        abcExplorerButton.setMargin(new Insets(5, 5, 5, 5));
        abcExplorerButton.addActionListener(this::abcExplorerTraitButtonActionPerformed);
        abcExplorerButton.setToolTipText(AppStrings.translate("button.abcexploretrait"));
        iconsPanel.add(abcExplorerButton);

        JToggleButton deobfuscateButton = new JToggleButton(View.getIcon("deobfuscate16"));
        deobfuscateButton.setMargin(new Insets(5, 5, 5, 5));
        deobfuscateButton.addActionListener(this::deobfuscateButtonActionPerformed);
        deobfuscateButton.setToolTipText(AppStrings.translate("button.deobfuscate"));
        deobfuscateButton.setSelected(Configuration.autoDeobfuscate.get());
        Configuration.autoDeobfuscate.addListener(new ConfigurationItemChangeListener<Boolean>() {
            @Override
            public void configurationItemChanged(Boolean newValue) {
                deobfuscateButton.setSelected(newValue);
            }
        });

        JButton deobfuscateOptionsButton = new JButton(View.getIcon("deobfuscateoptions16"));
        deobfuscateOptionsButton.addActionListener(this::deobfuscateOptionsButtonActionPerformed);
        deobfuscateOptionsButton.setToolTipText(AppStrings.translate("button.deobfuscate_options"));
        deobfuscateOptionsButton.setMargin(new Insets(0, 0, 0, 0));
        deobfuscateOptionsButton.setPreferredSize(new Dimension(30, deobfuscateButton.getPreferredSize().height));

        iconsPanel.add(deobfuscateButton);
        iconsPanel.add(deobfuscateOptionsButton);

        JButton breakpointListButton = new JButton(View.getIcon("breakpointlist16"));
        breakpointListButton.setMargin(new Insets(5, 5, 5, 5));
        breakpointListButton.addActionListener(this::breakPointListButtonActionPerformed);
        breakpointListButton.setToolTipText(AppStrings.translate("button.breakpointList"));
        iconsPanel.add(breakpointListButton);

        scriptNameLabel = new JLabel("-");

        JPanel topPanel = new JPanel(new BorderLayout());
        //iconsPanel.setAlignmentX(0);
        topPanel.add(scriptNameLabel, BorderLayout.NORTH);

        toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(iconsPanel, BorderLayout.WEST);

        JPanel libraryAndLinkPanel = new JPanel(new FlowLayout());

        LinkDialog linkDialog = new LinkDialog(mainPanel);

        libraryComboBox = new JComboBox<>();
        libraryComboBox.addItem("AIR (airglobal.swc)");
        libraryComboBox.addItem("Flash (playerglobal.swc)");
        libraryComboBox.setSelectedIndex(SWF.LIBRARY_FLASH);
        libraryComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SWF swf = getSwf();
                if (swf == null) {
                    return;
                }
                SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf.getShortPathTitle());
                conf.setCustomData(CustomConfigurationKeys.KEY_LIBRARY, "" + libraryComboBox.getSelectedIndex());
                linkDialog.load(getSwf());
                linkDialog.save(getSwf(), true);
            }
        });

        libraryAndLinkPanel.add(new JLabel(AppStrings.translate("library")));
        libraryAndLinkPanel.add(libraryComboBox);

        libraryAndLinkPanel.add(linksLabel);

        JButton linkButton = new JButton(View.getIcon("link16"));
        linkButton.setToolTipText(AppStrings.translate("button.abc.linkedSwfs.hint"));

        linkDialog.addSaveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });

        linkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                linkDialog.setLocationRelativeTo(linkButton);
                Point loc = new Point(0, linkButton.getHeight());
                SwingUtilities.convertPointToScreen(loc, linkButton);
                linkDialog.setLocation(loc);

                linkDialog.show(getSwf());
            }
        });
        libraryAndLinkPanel.add(linkButton);

        toolbarPanel.add(libraryAndLinkPanel, BorderLayout.EAST);

        topPanel.add(toolbarPanel, BorderLayout.CENTER);

        iconDecPanel.add(topPanel, BorderLayout.NORTH);

        JPanel panelWithHint = new JPanel(new BorderLayout());
        panelWithHint.setAlignmentX(CENTER_ALIGNMENT);
        panelWithHint.add(brokenHintPanel, BorderLayout.NORTH);
        panelWithHint.add(decompiledScrollPane, BorderLayout.CENTER);

        brokenHintPanel.setVisible(false);

        infoNotEditableLabel = new JLabel(View.getIcon("information16"));
        infoNotEditableLabel.setToolTipText(AppStrings.translate("info.noteditable.compound"));

        iconDecPanel.add(panelWithHint, BorderLayout.CENTER);
        final JPanel decButtonsPan = new JPanel(new FlowLayout());
        decButtonsPan.setBorder(new BevelBorder(BevelBorder.RAISED));
        decButtonsPan.add(editDecompiledButton);
        decButtonsPan.add(experimentalLabel);
        decButtonsPan.add(flexLabel);
        decButtonsPan.add(infoNotEditableLabel);
        decButtonsPan.add(saveDecompiledButton);
        decButtonsPan.add(cancelDecompiledButton);

        if (Configuration.useFlexAs3Compiler.get()) {
            experimentalLabel.setVisible(false);
        } else {
            flexLabel.setVisible(false);
        }

        infoNotEditableLabel.setVisible(false);

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
        detailPanel = new DetailPanel(this, mainPanel);
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

        decompiledTextArea.changeContentType("text/actionscript3");
        decompiledTextArea.setFont(Configuration.getSourceFont());

        View.addEditorAction(decompiledTextArea, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Reference<ABC> usedAbc = new Reference<>(null);
                int multinameIndex = decompiledTextArea.getMultinameUnderCaret(usedAbc);
                if (multinameIndex > -1) {
                    UsageFrame usageFrame = new UsageFrame(usedAbc.getVal(), multinameIndex, false, ABCPanel.this, false);
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

        navigatorPanel = new JPanel(new BorderLayout());
        JPanel navIconsPanel = new JPanel();
        navIconsPanel.setLayout(new BoxLayout(navIconsPanel, BoxLayout.X_AXIS));
        final JToggleButton sortButton = new JToggleButton(View.getIcon("sort16"));
        sortButton.setMargin(new Insets(3, 3, 3, 3));
        navIconsPanel.add(sortButton);
        //JLabel navigatorLabel = new JLabel(AppStrings.translate("traits"), JLabel.CENTER);
        //navigatorPanel.add(navigatorLabel, BorderLayout.NORTH);
        navigatorPanel.add(navIconsPanel, BorderLayout.SOUTH);
        navigatorPanel.add(new FasterScrollPane(navigator), BorderLayout.CENTER);
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigator.setSorted(sortButton.isSelected());
                navigator.updateUI();
            }
        });

        if (Configuration.displayAs3PCodePanel.get()) {
            splitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, panB, detailPanel, Configuration.guiAvm2SplitPaneDividerLocationPercent);
            splitPane.setContinuousLayout(true);
            add(splitPane, BorderLayout.CENTER);
        } else {
            splitPane = null;
            add(panB, BorderLayout.CENTER);
        }
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

    private boolean hasDeclaration(Token t) {
        if (decompiledTextArea == null) {
            return false; //?
        }

        if (t == null || (t.type != TokenType.IDENTIFIER && t.type != TokenType.KEYWORD && t.type != TokenType.REGEX)) {
            return false;
        }
        int pos = t.start;
        Reference<Integer> abcIndex = new Reference<>(0);
        Reference<Integer> classIndex = new Reference<>(0);
        Reference<Integer> traitIndex = new Reference<>(0);
        Reference<Integer> multinameIndexRef = new Reference<>(0);
        Reference<Boolean> classTrait = new Reference<>(false);
        Reference<ABC> usedAbcRef = new Reference<>(null);
        if (decompiledTextArea.getPropertyTypeAtPos(getSwf().getAbcIndex(), pos, abcIndex, classIndex, traitIndex, classTrait, multinameIndexRef, usedAbcRef)) {
            return true;
        }
        ABC usedAbc = usedAbcRef.getVal();
        int multinameIndex = decompiledTextArea.getMultinameAtPos(pos, usedAbcRef);
        if (multinameIndex > -1) {
            if (multinameIndex == 0) {
                return false;
            }

            Multiname m = usedAbc.constants.getMultiname(multinameIndex);
            if (m == null) {
                return false;
            }
            if (m.kind == Multiname.TYPENAME) {  //Assuming it's a Vector with single parameter
                multinameIndex = m.params[0];
                m = usedAbc.constants.getMultiname(multinameIndex);
            }
            List<MultinameUsage> usages = usedAbc.findMultinameDefinition(multinameIndex);

            //search other ABC tags if this is not private multiname
            if (m.getSingleNamespaceIndex(usedAbc.constants) > 0 && usedAbc.constants.getNamespace(m.getSingleNamespaceIndex(usedAbc.constants)).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == usedAbc) {
                        continue;
                    }

                    List<Integer> mids = a.constants.getMultinameIds(m, usedAbc.constants);
                    for (int mid : mids) {
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
        Reference<ABC> usedAbcRef = new Reference<>(null);
        if (decompiledTextArea.getPropertyTypeAtPos(getSwf().getAbcIndex(), pos, abcIndex, classIndex, traitIndex, classTrait, multinameIndexRef, usedAbcRef)) {
            UsageFrame.gotoUsage(ABCPanel.this, new TraitMultinameUsage(getAbcList().get(abcIndex.getVal()).getABC(), multinameIndexRef.getVal(), decompiledTextArea.getScriptLeaf().scriptIndex, classIndex.getVal(), traitIndex.getVal(), classTrait.getVal() ? TraitMultinameUsage.TRAITS_TYPE_CLASS : TraitMultinameUsage.TRAITS_TYPE_INSTANCE, null, -1) {
            });
            return;
        }
        int multinameIndex = decompiledTextArea.getMultinameAtPos(pos, usedAbcRef);
        ABC usedAbc = usedAbcRef.getVal();
        if (multinameIndex > -1) {

            Multiname m = usedAbc.constants.getMultiname(multinameIndex);
            if (m.kind == Multiname.TYPENAME) { //Assuming it's a Vector with single parameter
                multinameIndex = m.params[0];
                m = usedAbc.constants.getMultiname(multinameIndex);
            }

            List<MultinameUsage> usages = usedAbc.findMultinameDefinition(multinameIndex);

            //search other ABC tags if this is not private multiname
            if (m.getSingleNamespaceIndex(usedAbc.constants) > 0 && m.getSingleNamespace(usedAbc.constants).kind != Namespace.KIND_PRIVATE) {
                for (ABCContainerTag at : getAbcList()) {
                    ABC a = at.getABC();
                    if (a == usedAbc) {
                        continue;
                    }
                    List<Integer> mids = a.constants.getMultinameIds(m, usedAbc.constants);
                    for (int mid : mids) {
                        usages.addAll(a.findMultinameDefinition(mid));
                    }
                }
            }

            //more than one? display list
            if (usages.size() > 1) {
                UsageFrame usageFrame = new UsageFrame(usedAbc, multinameIndex, false, ABCPanel.this, true);
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
            if (ctrlDown && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && !decompiledTextArea.isEditable()) {
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
        updateLinksLabel();
    }

    public void updateLinksLabel() {
        SWF swf = getSwf();
        if (swf == null) {
            linksLabel.setText("");
        } else {
            int num = swf.getNumAbcIndexDependencies();
            if (num == 0) {
                linksLabel.setText("");
            } else {
                linksLabel.setText(AppStrings.translate(num == 1 ? "abc.linkedSwfs.one" : "abc.linkedSwfs.more").replace("%num%", "" + num));
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        View.checkAccess();

        if (e.getSource() == constantTypeList) {
            int index = ((JComboBox) e.getSource()).getSelectedIndex();
            if (index == -1) {
                return;
            }
        }
    }

    public void display() {
        View.checkAccess();

        setVisible(true);
    }

    public void hilightScript(String nameIncludingSwfHash) {
        if (!nameIncludingSwfHash.contains(":")) {
            throw new RuntimeException("Script name should contain swfHash");
        }
        String swfHash = nameIncludingSwfHash.substring(nameIncludingSwfHash.indexOf(":"));
        String name = nameIncludingSwfHash.substring(nameIncludingSwfHash.indexOf(":") + 1);
        Openable openable = null;
        if (swfHash.equals("main")) {
            openable = Main.getRunningSWF();
        } else if (swfHash.startsWith("loaded_")) {
            String hashToSearch = swfHash.substring("loaded_".length());
            loop:
            for (OpenableList sl : Main.getMainFrame().getPanel().getSwfs()) {
                for (int s = 0; s < sl.size(); s++) {
                    Openable op = sl.get(s);
                    String t = op.getTitleOrShortFileName();
                    if (t == null) {
                        t = "";
                    }
                    if (t.endsWith(":" + hashToSearch)) { //this one is already opened
                        openable = op;
                        break loop;
                    }
                }
            }
        }

        if (openable != null) {
            hilightScript(openable, name);
        }
    }

    /**
     * Hilights specific script.
     *
     * @param openable Openable to hilight
     * @param name Full name of the script. It must be printable - deobfuscated,
     * not raw!
     */
    public void hilightScript(Openable openable, String name) {

        TreeItem scriptNode = null;
        if (openable instanceof SWF) {
            SWF swf = (SWF) openable;
            if (mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES) {
                scriptNode = mainPanel.tagTree.getFullModel().getScriptsNode(swf);
            } else {
                List<ABC> allAbcs = new ArrayList<>();
                for (ABCContainerTag container : swf.getAbcList()) {
                    allAbcs.add(container.getABC());
                }
                loopcontainer:
                for (ABCContainerTag container : swf.getAbcList()) {
                    List<ScriptPack> packs = container.getABC().getScriptPacks(null, allAbcs);
                    for (ScriptPack pack : packs) {
                        ClassPath classPath = pack.getClassPath();
                        if (name.endsWith(classPath.className + classPath.namespaceSuffix) && classPath.toRawString().equals(name)) {
                            scriptNode = (Tag) container;
                            break loopcontainer;
                        }
                    }
                }
            }
        } else if (openable instanceof ABC) {
            scriptNode = (ABC) openable;
        }

        if (scriptNode != null) {
            hilightScript(openable, name, scriptNode);
        }
    }

    public void hilightScript(Openable openable, String name, TreeItem scriptNode) {
        View.checkAccess();

        Object item;

        if ((mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES) && (openable instanceof SWF)) {
            item = mainPanel.tagTree.getFullModel().getScriptsNode((SWF) openable);
        } else if (openable instanceof ABC) {
            item = openable;
        } else { //SWF on taglist, should be DoABCContainer
            item = scriptNode;
        }

        AbstractTagTree tree = mainPanel.getCurrentTree();

        String pkg = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : "";
        String[] parts = name.split("\\.");

        List<Object> rootNodes = new ArrayList<>();
        rootNodes.add(item);

        boolean found = false;

        List<? extends TreeItem> firstLevelNodes = tree.getFullModel().getAllChildren(item);
        for (TreeItem ti : firstLevelNodes) {
            if ((ti instanceof AS3Package) && (((AS3Package) ti).isCompoundScript())) {
                rootNodes.add(ti);
                if (parts.length == 1 && parts[0].equals("script_" + ((AS3Package) ti).getCompoundScriptIndex())) {
                    found = true;
                    item = ti;
                    break;
                }
            }
        }

        if (!found) {
            looproot:
            for (Object root : rootNodes) {
                item = root;
                loopparts:
                for (int i = 0; i < parts.length; i++) {
                    found = false;
                    for (TreeItem ti : tree.getFullModel().getAllChildren(item)) {
                        if ((ti instanceof AS3Package) && ((AS3Package) ti).isFlat()) {
                            AS3Package pti = (AS3Package) ti;
                            if ((pkg.isEmpty() && pti.isDefaultPackage()) || (!pti.isDefaultPackage() && pkg.equals(pti.packageName))) {
                                item = pti;
                                i = parts.length - 1 - 1;
                                found = true;
                                break;
                            }
                            continue;
                        }
                        if ((ti instanceof AS3Package) && (((AS3Package) ti).isCompoundScript())) {
                            continue;
                        }
                        if (ti instanceof AS3ClassTreeItem) {
                            AS3ClassTreeItem cti = (AS3ClassTreeItem) ti;

                            if (parts[i].equals(cti.getPrintableNameWithNamespaceSuffix())) {
                                item = ti;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        continue looproot;
                    }
                }
                break; //found
            }
        }
        if (!found) {
            return;
        }
        mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), (TreeItem) item);
        mainPanel.reload(true);
    }

    public void hilightScript(ScriptPack pack) {
        View.checkAccess();

        AbstractTagTreeModel ttm = mainPanel.getCurrentTree().getFullModel();
        TreePath tp = ttm.getTreePath(pack);
        if (tp == null) {
            mainPanel.hideQuickTreeFind();
            tp = ttm.getTreePath(pack);
        }

        mainPanel.getCurrentTree().setSelectionPath(tp);
        mainPanel.getCurrentTree().scrollPathToVisible(tp);
    }

    @Override
    public void updateSearchPos(String searchedText, boolean ignoreCase, boolean regExp, ScriptSearchResult item) {
        View.checkAccess();

        if (!(item instanceof ABCSearchResult)) {
            return;
        }

        ABCSearchResult result = (ABCSearchResult) item;

        searchPanel.setOptions(ignoreCase, regExp);
        searchPanel.setSearchText(searchedText);
        ScriptPack pack = result.getScriptPack();
        setAbc(pack.abc);

        Runnable setScriptComplete = new Runnable() {
            @Override
            public void run() {
                decompiledTextArea.removeScriptListener(this);
                hilightScript(pack);
                setAbc(pack.abc);

                boolean pcode = result.isPcode();
                if (pcode) {
                    decompiledTextArea.setClassIndex(result.getClassIndex());
                    decompiledTextArea.gotoTrait(result.getTraitId());
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

        boolean useFlex = Configuration.useFlexAs3Compiler.get();

        experimentalLabel.setVisible(!useFlex && !val);
        flexLabel.setVisible(useFlex && !val);
        cancelDecompiledButton.setVisible(val);

        decompiledTextArea.getCaret().setVisible(true);
        decLabel.setIcon(val ? View.getIcon("editing16") : null);
        detailPanel.setVisible(!val);

        decompiledTextArea.ignoreCaret = val;
        if (val) {
            decompiledTextArea.requestFocusInWindow();
        }
        toolbarPanel.setVisible(!val);
    }

    private void editDecompiledButtonActionPerformed(ActionEvent evt) {
        scriptReplacer = mainPanel.getAs3ScriptReplacer(Main.isSwfAir(getOpenable()));
        if (scriptReplacer == null) {
            return;
        }

        if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS3Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            pack = decompiledTextArea.getScriptLeaf();
            setDecompiledEditMode(true);
            mainPanel.setEditingStatus();
            SwingWorker initReplaceWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    scriptReplacer.initReplacement(pack, Main.getDependencies(pack.abc.getSwf()));
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
        mainPanel.clearEditingStatus();
    }

    public TreeItem getScriptNodeForPack(ScriptPack pack) {
        TreePath scriptsPath = mainPanel.getCurrentTree().getFullModel().getTreePath(pack);
        while (!(scriptsPath.getLastPathComponent() instanceof ClassesListTreeModel)
                && !(scriptsPath.getLastPathComponent() instanceof ABC)
                && !(scriptsPath.getLastPathComponent() instanceof ABCContainerTag)) {
            scriptsPath = scriptsPath.getParentPath();
        }
        return (TreeItem) scriptsPath.getLastPathComponent();
    }

    private void saveDecompiled(boolean refreshTree) {
        final ABC localAbc = abc;
        int oldIndex = pack.scriptIndex;
        SWF.uncache(pack);
        try {
            String oldSp = pack.getClassPath().toRawString();

            TreeItem scriptNode = getScriptNodeForPack(pack);

            String as = decompiledTextArea.getText();

            localAbc.replaceScriptPack(scriptReplacer, pack, as, Main.getDependencies(pack.abc.getSwf()));
            scriptReplacer.deinitReplacement(pack);
            lastDecompiled = as;
            setDecompiledEditMode(false);
            mainPanel.updateClassesList();

            if (oldSp != null) {
                hilightScript(localAbc.getOpenable(), oldSp, scriptNode);
            }
            reload();
            mainPanel.clearEditingStatus();
            if (refreshTree) {
                ViewMessages.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
            }
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
                ViewMessages.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", firstErrorText).replace("%line%", Long.toString(firstErrorLine)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            } else {
                ViewMessages.showMessageDialog(this, sb.toString(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            }
            decompiledTextArea.requestFocus();

        } catch (Throwable ex) {
            Logger.getLogger(ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveDecompiledButtonActionPerformed(ActionEvent evt) {
        saveDecompiled(true);
    }

    private void deobfuscateButtonActionPerformed(ActionEvent evt) {
        JToggleButton toggleButton = (JToggleButton) evt.getSource();
        boolean selected = toggleButton.isSelected();

        if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("message.confirm.autodeobfuscate") + "\r\n" + (selected ? AppStrings.translate("message.confirm.on") : AppStrings.translate("message.confirm.off")), AppStrings.translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningDeobfuscation, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoDeobfuscate.set(selected);
            mainPanel.autoDeobfuscateChanged();
        } else {
            toggleButton.setSelected(Configuration.autoDeobfuscate.get());
        }
    }

    private void deobfuscateOptionsButtonActionPerformed(ActionEvent evt) {
        JPopupMenu popupMenu = new JPopupMenu();
        JCheckBoxMenuItem simplifyExpressionsMenuItem = new JCheckBoxMenuItem(AppStrings.translate("deobfuscate_options.simplify_expressions"));
        simplifyExpressionsMenuItem.setSelected(Configuration.simplifyExpressions.get());
        simplifyExpressionsMenuItem.addActionListener(this::simplifyExpressionsMenuItemActionPerformed);
        /*JCheckBoxMenuItem removeObfuscatedDeclarationsMenuItem = new JCheckBoxMenuItem(AppStrings.translate("deobfuscate_options.remove_obfuscated_declarations"));
        removeObfuscatedDeclarationsMenuItem.setSelected(Configuration.deobfuscateAs12RemoveInvalidNamesAssignments.get());
        removeObfuscatedDeclarationsMenuItem.addActionListener(this::removeObfuscatedDeclarationsMenuItemActionPerformed);
         */
        popupMenu.add(simplifyExpressionsMenuItem);
        //popupMenu.add(removeObfuscatedDeclarationsMenuItem);

        JButton sourceButton = (JButton) evt.getSource();
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());
    }

    private void simplifyExpressionsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
        Configuration.simplifyExpressions.set(menuItem.isSelected());
        mainPanel.autoDeobfuscateChanged();
    }

    /*private void removeObfuscatedDeclarationsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
        Configuration.deobfuscateAs12RemoveInvalidNamesAssignments.set(menuItem.isSelected());
        mainPanel.autoDeobfuscateChanged();
    }*/
    private void abcExplorerTraitButtonActionPerformed(ActionEvent evt) {
        int classIndex = decompiledTextArea.getClassIndex();
        int globalTraitIndex = decompiledTextArea.lastTraitIndex;

        if ((globalTraitIndex == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER)
                || (globalTraitIndex == GraphTextWriter.TRAIT_CLASS_INITIALIZER)
                || (globalTraitIndex == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER)) {
            ABCExplorerDialog dialog = mainPanel.showAbcExplorer(abc.getOpenable(), abc);
            dialog.selectTrait(decompiledTextArea.getScriptLeaf().scriptIndex, classIndex, globalTraitIndex, globalTraitIndex);
            return;
        }

        int traitType;
        int traitIndex = -1;
        if (globalTraitIndex == GraphTextWriter.TRAIT_UNKNOWN && classIndex >= 0) {
            Traits traits = abc.script_info.get(decompiledTextArea.getScriptLeaf().scriptIndex).traits;
            for (int i = 0; i < traits.traits.size(); i++) {
                if (traits.traits.get(i) instanceof TraitClass) {
                    TraitClass tc = (TraitClass) traits.traits.get(i);
                    if (tc.class_info == classIndex) {
                        traitIndex = i;
                        break;
                    }
                }
            }
            traitType = GraphTextWriter.TRAIT_SCRIPT_INITIALIZER;
        } else if (classIndex < 0) {
            traitIndex = globalTraitIndex;
            traitType = GraphTextWriter.TRAIT_SCRIPT_INITIALIZER;
        } else {
            Traits staticTraits = abc.class_info.get(classIndex).static_traits;
            Traits instanceTraits = abc.instance_info.get(classIndex).instance_traits;
            if (globalTraitIndex >= 0 && globalTraitIndex < abc.class_info.get(classIndex).static_traits.traits.size()) {
                traitIndex = globalTraitIndex;
                traitType = GraphTextWriter.TRAIT_CLASS_INITIALIZER;
            } else {
                if (globalTraitIndex >= 0 && globalTraitIndex < staticTraits.traits.size() + instanceTraits.traits.size()) {
                    traitIndex = globalTraitIndex - staticTraits.traits.size();
                    traitType = GraphTextWriter.TRAIT_INSTANCE_INITIALIZER;
                } else {
                    return;
                }
            }
        }

        ABCExplorerDialog dialog = mainPanel.showAbcExplorer(abc.getOpenable(), abc);
        dialog.selectTrait(decompiledTextArea.getScriptLeaf().scriptIndex, classIndex, traitIndex, traitType);
    }

    private void removeTraitButtonActionPerformed(ActionEvent evt) {
        int classIndex = decompiledTextArea.getClassIndex();
        //int scriptIndex = decompiledTextArea.getScriptLeaf().scriptIndex;
        int traitId = decompiledTextArea.lastTraitIndex;

        if ((traitId == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER)
                || (traitId == GraphTextWriter.TRAIT_CLASS_INITIALIZER)
                || (traitId == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER)) {
            return;
        }

        boolean scriptTraitsUsed = false;
        Traits traits = null;
        int traitIndex = -1;
        if (traitId == GraphTextWriter.TRAIT_UNKNOWN && classIndex >= 0) {
            traits = abc.script_info.get(decompiledTextArea.getScriptLeaf().scriptIndex).traits;
            for (int i = 0; i < traits.traits.size(); i++) {
                if (traits.traits.get(i) instanceof TraitClass) {
                    TraitClass tc = (TraitClass) traits.traits.get(i);
                    if (tc.class_info == classIndex) {
                        traitIndex = i;
                        break;
                    }
                }
            }
            scriptTraitsUsed = true;
        } else if (classIndex < 0) {
            traits = abc.script_info.get(decompiledTextArea.getScriptLeaf().scriptIndex).traits;
            traitIndex = traitId;
            scriptTraitsUsed = true;
        } else {
            Traits staticTraits = abc.class_info.get(classIndex).static_traits;
            Traits instanceTraits = abc.instance_info.get(classIndex).instance_traits;
            if (traitId >= 0 && traitId < abc.class_info.get(classIndex).static_traits.traits.size()) {
                traitIndex = traitId;
                traits = staticTraits;
            } else {
                if (traitId >= 0 && traitId < staticTraits.traits.size() + instanceTraits.traits.size()) {
                    traitIndex = traitId - staticTraits.traits.size();
                    traits = instanceTraits;
                } else {
                    traits = null;
                }
            }
        }

        if (traits == null || traitIndex < 0) {
            return;
        }

        if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.confirm.removetrait"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            navigator.setModel(new DefaultListModel<>());
            if (scriptTraitsUsed) {
                final int fTraitIndex = traitIndex;
                final Traits fTraits = traits;
                Main.getMainFrame().getPanel().treeOperation(new Runnable() {
                    @Override
                    public void run() {
                        ((Tag) abc.parentTag).getSwf().clearScriptCache();
                        fTraits.traits.remove(fTraitIndex);
                        if (fTraits.traits.isEmpty()) {
                            abc.script_info.get(decompiledTextArea.getScriptLeaf().scriptIndex).delete(abc, true);
                            abc.pack();
                        }
                        ((Tag) abc.parentTag).setModified(true);
                        abc.fireChanged();
                    }
                });
            } else {
                traits.traits.remove(traitIndex);
                ((Tag) abc.parentTag).setModified(true);
                reload();
                abc.fireChanged();
            }

        }
    }

    private void breakPointListButtonActionPerformed(ActionEvent evt) {
        Main.showBreakpointsList();
    }

    private void addTraitButtonActionPerformed(ActionEvent evt) {
        int class_index = decompiledTextArea.getClassIndex();
        if (class_index < 0) {
            return;
        }
        if (newTraitDialog == null) {
            newTraitDialog = new NewTraitDialog(Main.getDefaultDialogsOwner());
        }
        int void_type = abc.constants.getPublicQnameId("void", true);
        int int_type = abc.constants.getPublicQnameId("int", true);

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
                ViewMessages.showMessageDialog(this, AppStrings.translate("error.trait.exists").replace("%name%", name), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
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
        navigator.setModel(new DefaultListModel<>());
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
            decompiledTextArea.addScriptListener(new Runnable() {
                @Override
                public void run() {
                    decompiledTextArea.gotoTrait(traitId);
                    decompiledTextArea.removeScriptListener(this);
                }
            });
            reload();
            abc.fireChanged();
        }
    }

    @Override
    public boolean tryAutoSave() {
        View.checkAccess();

        boolean ok = true;
        if (saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveDecompiled(false);
            ok = ok && !(saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled());
        }

        ok = ok && detailPanel.tryAutoSave();

        return ok;
    }

    @Override
    public boolean isEditing() {
        View.checkAccess();

        return (detailPanel.saveButton.isVisible() && detailPanel.saveButton.isEnabled())
                || (saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled());
    }

    public DebugPanel getDebugPanel() {
        return debugPanel;
    }

    public void setCompound(boolean value) {
        infoNotEditableLabel.setVisible(value);
        boolean useFlex = Configuration.useFlexAs3Compiler.get();
        experimentalLabel.setVisible(!useFlex && !value);
        flexLabel.setVisible(useFlex && !value);
        editDecompiledButton.setEnabled(!value);
    }
    
    public void setScript(ScriptPack scriptPack) {
        setDecompiledEditMode(false);
        detailPanel.setEditMode(false);        
        detailPanel.methodTraitPanel.methodCodePanel.clear();
        setAbc(scriptPack.abc);
        decompiledTextArea.setScript(scriptPack, true);
        decompiledTextArea.setNoTrait();
        
    }
}
