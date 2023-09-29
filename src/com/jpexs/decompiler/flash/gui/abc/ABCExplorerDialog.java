/*
 * Copyright (C) 2023 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class ABCExplorerDialog extends AppDialog {
    
    private final List<ABCContainerTag> abcContainers = new ArrayList<>();
    private final JComboBox<String> abcComboBox;
    private final JLabel tagInfoLabel;
    private final List<Integer> abcFrames = new ArrayList<>();
    private final JTabbedPane mainTabbedPane;        
    private final JTabbedPane cpTabbedPane;
    
    public ABCExplorerDialog(Window owner, SWF swf, ABC abc) {
        super(owner);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel(translate("abc")));
        int frame = 1;
        for (Tag t : swf.getTags()) {
            if (t instanceof ShowFrameTag) {
                frame++;
            }
            if (t instanceof ABCContainerTag) {
                abcContainers.add((ABCContainerTag)t);
                abcFrames.add(frame);
            }
        }
        String[] abcComboBoxData = new String[abcContainers.size()];
        for (int i = 0; i < abcContainers.size(); i++) {
            abcComboBoxData[i] = "tag" + (i+1);
            if (abcContainers.get(i) instanceof DoABC2Tag) {
                DoABC2Tag doa2 = (DoABC2Tag)abcContainers.get(i);
                if (doa2.name != null && !doa2.name.isEmpty()) {
                    abcComboBoxData[i] += " (\"" + Helper.escapePCodeString(doa2.name) + "\")";
                }
            }
        }
        abcComboBox = new JComboBox<>(abcComboBoxData);
        Dimension abcComboBoxSize = new Dimension(500, abcComboBox.getPreferredSize().height);
        abcComboBox.setMinimumSize(abcComboBoxSize);
        abcComboBox.setPreferredSize(abcComboBoxSize);
        topPanel.add(abcComboBox);
        
        tagInfoLabel = new JLabel();
        topPanel.add(tagInfoLabel);
        
        
        abcComboBox.addActionListener(this::abcComboBoxActionPerformed);
                
        mainTabbedPane = new JTabbedPane();                                
        cpTabbedPane = new JTabbedPane();
        
        cnt.add(topPanel, BorderLayout.NORTH);
        cnt.add(mainTabbedPane, BorderLayout.CENTER);
        
        abcComboBoxActionPerformed(null);        
        setSize(800, 600);
        setTitle(translate("title"));
        View.setWindowIcon(this);
        View.centerScreen(this);
    }
   
    private ABC getSelectedAbc() {
        return abcContainers.get(abcComboBox.getSelectedIndex()).getABC();
    }
    
    private void abcComboBoxActionPerformed(ActionEvent e)
    {
        int index = abcComboBox.getSelectedIndex();
        ABC abc = abcContainers.get(index).getABC();
        tagInfoLabel.setText(
                translate("abc.info")
                        .replace("%index%", "" + (index + 1))
                        .replace("%count%", "" + abcComboBox.getItemCount())
                        .replace("%major%", "" + abc.version.major)
                        .replace("%minor%", "" + abc.version.minor)
                        .replace("%size%", Helper.formatFileSize(abc.getDataSize()))
                        .replace("%frame%", "" + abcFrames.get(index))
        );
        
        
        cpTabbedPane.removeAll();
        
        
        cpTabbedPane.addTab("int (" + abc.constants.getIntCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_INT));
        cpTabbedPane.addTab("uint (" + abc.constants.getUIntCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_UINT));
        cpTabbedPane.addTab("dbl (" + abc.constants.getDoubleCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_DOUBLE));
        if (abc.hasDecimalSupport()) {
            cpTabbedPane.addTab("dc (" + abc.constants.getDecimalCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_DECIMAL));
        }
        if (abc.hasFloatSupport()) {
            cpTabbedPane.addTab("fl (" + abc.constants.getFloatCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_FLOAT));
            cpTabbedPane.addTab("fl4 (" + abc.constants.getFloat4Count() + ")", makeTreePanel(abc, TreeType.CONSTANT_FLOAT_4));
        }
        cpTabbedPane.addTab("str (" + abc.constants.getStringCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_STRING));
        cpTabbedPane.addTab("ns (" + abc.constants.getNamespaceCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_NAMESPACE));
        cpTabbedPane.addTab("nss (" + abc.constants.getNamespaceSetCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_NAMESPACE_SET));
        cpTabbedPane.addTab("mn (" + abc.constants.getMultinameCount() + ")", makeTreePanel(abc, TreeType.CONSTANT_MULTINAME));
        
        mainTabbedPane.removeAll();
                
        JPanel cpPanel = new JPanel(new BorderLayout());
        cpPanel.add(cpTabbedPane, BorderLayout.CENTER);
        
        int cpCount = abc.constants.getIntCount() - 1 +
                      abc.constants.getUIntCount() - 1 +
                      abc.constants.getDoubleCount() - 1 +
                      abc.constants.getStringCount() - 1 + 
                      abc.constants.getNamespaceCount() - 1 +
                      abc.constants.getNamespaceSetCount() - 1 +
                      abc.constants.getMultinameCount() - 1 + 
                      (abc.hasDecimalSupport() ? abc.constants.getDecimalCount() - 1 : 0) +
                      (abc.hasFloatSupport()? abc.constants.getFloatCount() - 1 + abc.constants.getFloat4Count() - 1 : 0);
        mainTabbedPane.addTab("cp (" + cpCount + ")", cpPanel);
        mainTabbedPane.addTab("mi (" + abc.method_info.size() + ")", makeTreePanel(abc, TreeType.METHOD_INFO));
        mainTabbedPane.addTab("md (" + abc.metadata_info.size() + ")", makeTreePanel(abc, TreeType.METADATA));
        mainTabbedPane.addTab("ii (" + abc.instance_info.size() + ")", makeTreePanel(abc, TreeType.INSTANCE_INFO));
        mainTabbedPane.addTab("ci (" + abc.class_info.size() + ")", makeTreePanel(abc, TreeType.CLASS_INFO));
        mainTabbedPane.addTab("si (" + abc.script_info.size() + ")", makeTreePanel(abc, TreeType.SCRIPT_INFO));
        mainTabbedPane.addTab("mb (" + abc.bodies.size() + ")", makeTreePanel(abc, TreeType.METHOD_BODY));                        
    }
    
    private JPanel makeTreePanel(ABC abc, TreeType type) {
        JTree tree = new JTree(new ExplorerTreeModel(abc, type));
        if (View.isOceanic()) {
            tree.setBackground(Color.white);
        }
        tree.setCellRenderer(new ExplorerTreeCellRenderer());
        tree.setUI(new BasicTreeUI() {
            {
                if (View.isOceanic()) {
                    setHashColor(Color.gray);
                }
            }
        });
        
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(new FasterScrollPane(tree), BorderLayout.CENTER);
        return treePanel;
    }
    
    private enum TreeType {
        CONSTANT_INT("Integers","int"),
        CONSTANT_UINT("UnsignedIntegers","uint"),
        CONSTANT_DOUBLE("Doubles", "dbl"),
        CONSTANT_DECIMAL("Decimals","dc"), //needs ABC decimal support
        CONSTANT_FLOAT("Floats","fl"), //needs ABC float support
        CONSTANT_FLOAT_4("Floats4","fl4"), //needs ABC float support
        CONSTANT_STRING("Strings","str"),
        CONSTANT_NAMESPACE("Namespaces", "ns"),
        CONSTANT_NAMESPACE_SET("NamespaceSets","nss"),
        CONSTANT_MULTINAME("Multinames","mn"),
        METHOD_INFO("MethodInfos","mi"),
        METADATA("MetadataInfos","md"),
        INSTANCE_INFO("InstanceInfos","ii"),
        CLASS_INFO("ClassInfos","ci"),
        SCRIPT_INFO("ScriptInfos","si"),
        METHOD_BODY("MethodBodys","mb");
        
        private final String name;
        private final String abbreviation;
        
        TreeType(String name, String abbreviation) {
            this.name = name;
            this.abbreviation = abbreviation;
        }

        public String getName() {
            return name;
        }
                
        public String getAbbreviation() {
            return abbreviation;
        }

        @Override
        public String toString() {
            return name;
        }                
    }
    
    private class ValueWithIndex {
        private final int index;
        private final TreeType type;
        private final Object value;
        private final String title;
        private final String prefix;

        public ValueWithIndex(int index, TreeType type, Object value, String title) {
            this.index = index;
            this.type = type;
            this.value = value;
            this.title = title;
            this.prefix = "";
        }        
        public ValueWithIndex(int index, TreeType type, Object value, String title, String prefix) {
            this.index = index;
            this.type = type;
            this.value = value;
            this.title = title;
            this.prefix = prefix;
        }

        public int getIndex() {
            return index;
        }

        public TreeType getType() {
            return type;
        }                

        @Override
        public String toString() {
            boolean implicit = false;
            if (index == 0) {
                switch (type) {
                    case CONSTANT_INT:
                    case CONSTANT_UINT:
                    case CONSTANT_DOUBLE:
                    case CONSTANT_STRING:
                    case CONSTANT_NAMESPACE:
                    case CONSTANT_MULTINAME:
                        implicit = true;
                }
            }
            
            return prefix + (implicit ? "[" : "") +type.getAbbreviation() + index + (implicit ? "]" : "") + ": " + title;
        }      

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + this.index;
            hash = 23 * hash + Objects.hashCode(this.type);
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
            final ValueWithIndex other = (ValueWithIndex) obj;
            if (this.index != other.index) {
                return false;
            }
            return this.type == other.type;
        }
        
        
    }
    
    private class ExplorerTreeModel implements TreeModel {

        private TreeType type;
        private ABC abc;
        
        public ExplorerTreeModel(ABC abc, TreeType type) {
            this.type = type;
            this.abc = abc;
        }
        
        @Override
        public Object getRoot() {
            return type;
        }

        private ValueWithIndex createValueWithIndex(int index, TreeType valueType, String prefix) {
            if (index == 0) {
                    switch(valueType) {
                        case CONSTANT_INT:
                        case CONSTANT_UINT:
                        case CONSTANT_DOUBLE:
                        case CONSTANT_STRING:
                        case CONSTANT_NAMESPACE:
                        case CONSTANT_MULTINAME:
                            return new ValueWithIndex(index, valueType, null, "null", prefix);
                    }                    
                }
                switch(valueType) {
                    case CONSTANT_INT:
                        return new ValueWithIndex(index, valueType, abc.constants.getInt(index), "" + abc.constants.getInt(index), prefix);
                    case CONSTANT_UINT:
                        return new ValueWithIndex(index, valueType, abc.constants.getUInt(index), "" + abc.constants.getUInt(index), prefix);
                    case CONSTANT_DOUBLE:
                        return new ValueWithIndex(index, valueType, abc.constants.getDouble(index), EcmaScript.toString(abc.constants.getDouble(index)), prefix);
                    case CONSTANT_DECIMAL:
                        return new ValueWithIndex(index, valueType, abc.constants.getDecimal(index), "" + abc.constants.getDecimal(index), prefix);
                    case CONSTANT_FLOAT:
                        return new ValueWithIndex(index, valueType, abc.constants.getFloat(index), EcmaScript.toString(abc.constants.getFloat(index)), prefix);
                    case CONSTANT_FLOAT_4:
                        Float4 f4 = abc.constants.getFloat4(index);
                        return new ValueWithIndex(index, valueType, f4,
                                EcmaScript.toString(f4.values[0]) + " " +
                                EcmaScript.toString(f4.values[1]) + " " +
                                EcmaScript.toString(f4.values[2]) + " " + 
                                EcmaScript.toString(f4.values[3]),
                                prefix
                        );
                    case CONSTANT_STRING:
                        return new ValueWithIndex(index, valueType, abc.constants.getString(index), "\"" + Helper.escapePCodeString(abc.constants.getString(index)) +"\"", prefix);
                    case CONSTANT_NAMESPACE:
                        return new ValueWithIndex(index, valueType, abc.constants.getNamespace(index), Multiname.namespaceToString(abc.constants, index), prefix);
                    case CONSTANT_NAMESPACE_SET:
                        return new ValueWithIndex(index, valueType, abc.constants.getNamespaceSet(index), Multiname.namespaceSetToString(abc.constants, index), prefix);
                    case CONSTANT_MULTINAME:
                        Multiname multiname = abc.constants.getMultiname(index);
                        return new ValueWithIndex(index, valueType, multiname, multiname.toString(abc.constants, new ArrayList<DottedChain>()), prefix);
                    default:
                        return new ValueWithIndex(index, valueType, null, "", prefix);
                }
        }
        
        @Override
        public Object getChild(Object parent, int index) {
            if (parent == type) {
                return createValueWithIndex(index, type, "");
            }
            if (parent instanceof ValueWithIndex) {
                ValueWithIndex vwi = (ValueWithIndex)parent;
                if (vwi.value instanceof NamespaceSet) {
                    NamespaceSet nss = (NamespaceSet) vwi.value;
                    int ns = nss.namespaces[index];
                    return new ValueWithIndex(ns, TreeType.CONSTANT_NAMESPACE, abc.constants.getNamespace(ns), Multiname.namespaceToString(abc.constants, ns));
                }
                if (vwi.value instanceof Namespace) {
                    Namespace ns = (Namespace) vwi.value;
                    switch (index) {
                        case 0:
                            return "kind: " + Namespace.kindToStr(ns.kind);
                        case 1:
                            return createValueWithIndex(ns.name_index, TreeType.CONSTANT_STRING, "name: ");
                    }
                }
                if (vwi.value instanceof Multiname) {
                    Multiname m = (Multiname)vwi.value;
                    if (index == 0) {
                        return "kind: " + m.getKindStr();
                    }
                    int kind = m.kind;
                    if ((kind == Multiname.QNAME) || (kind == Multiname.QNAMEA)) {
                        switch (index) {
                            case 1:
                                return createValueWithIndex(m.namespace_index, TreeType.CONSTANT_NAMESPACE, "namespace: ");
                            case 2:
                                return createValueWithIndex(m.name_index, TreeType.CONSTANT_STRING, "name: ");
                        }                                           
                    } else if ((kind == Multiname.RTQNAME) || (kind == Multiname.RTQNAMEA)) {
                        if (index == 1) {
                            return createValueWithIndex(m.name_index, TreeType.CONSTANT_STRING, "name: ");
                        }
                    } else if ((kind == Multiname.RTQNAMEL) || (kind == Multiname.RTQNAMELA)) {
                        //ignore
                    } else if ((kind == Multiname.MULTINAME) || (kind == Multiname.MULTINAMEA)) {
                        switch (index) {
                            case 1:
                                return createValueWithIndex(m.name_index, TreeType.CONSTANT_STRING, "name: ");                        
                            case 2:
                                return createValueWithIndex(m.namespace_set_index, TreeType.CONSTANT_NAMESPACE_SET, "namespace_set: ");
                        }   
                    } else if ((kind == Multiname.MULTINAMEL) || (kind == Multiname.MULTINAMELA)) {
                        if (index == 1) {
                            return createValueWithIndex(m.namespace_set_index, TreeType.CONSTANT_NAMESPACE_SET, "namespace_set: ");
                        }
                    } else if (kind == Multiname.TYPENAME) {
                        if (index == 1) {
                            return createValueWithIndex(m.qname_index, TreeType.CONSTANT_MULTINAME, "qname: ");
                        }
                        if (index >= 2 && index - 2 < m.params.length) {
                            return createValueWithIndex(m.params[index - 2], TreeType.CONSTANT_MULTINAME, "param" + (index - 1) + ": ");
                        }                        
                    }
                }
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == type) {
                switch(type) {
                    case CONSTANT_INT:
                        return abc.constants.getIntCount();
                    case CONSTANT_UINT:
                        return abc.constants.getUIntCount();
                    case CONSTANT_DOUBLE:
                        return abc.constants.getDoubleCount();
                    case CONSTANT_DECIMAL:
                        return abc.constants.getDecimalCount();
                    case CONSTANT_FLOAT:
                        return abc.constants.getFloatCount();
                    case CONSTANT_FLOAT_4:
                        return abc.constants.getFloat4Count();
                    case CONSTANT_STRING:
                        return abc.constants.getStringCount();
                    case CONSTANT_NAMESPACE:
                        return abc.constants.getNamespaceCount();
                    case CONSTANT_NAMESPACE_SET:
                        return abc.constants.getNamespaceSetCount();
                    case CONSTANT_MULTINAME:
                        return abc.constants.getMultinameCount();
                    case METHOD_INFO:
                        return abc.method_info.size();
                    case METADATA:
                        return abc.metadata_info.size();
                    case INSTANCE_INFO:
                        return abc.instance_info.size();
                    case CLASS_INFO:
                        return abc.class_info.size();
                    case SCRIPT_INFO:
                        return abc.script_info.size();
                    case METHOD_BODY:
                        return abc.bodies.size();                                          
                }
            }
            if (parent instanceof ValueWithIndex) {
                ValueWithIndex vwi = (ValueWithIndex)parent;
                if (vwi.value instanceof NamespaceSet) {
                    NamespaceSet nss = (NamespaceSet) vwi.value;
                    return nss.namespaces.length;
                }
                if (vwi.value instanceof Namespace) {
                    //kind, name
                    return 2;
                }
                if (vwi.value instanceof Multiname) {
                    Multiname m = (Multiname)vwi.value;
                    int kind = m.kind;
                    if ((kind == Multiname.QNAME) || (kind == Multiname.QNAMEA)) {
                        return 1 + 2;                                        
                    } else if ((kind == Multiname.RTQNAME) || (kind == Multiname.RTQNAMEA)) {
                        return 1 + 1;                      
                    } else if ((kind == Multiname.RTQNAMEL) || (kind == Multiname.RTQNAMELA)) {
                        return 1;
                    } else if ((kind == Multiname.MULTINAME) || (kind == Multiname.MULTINAMEA)) {
                        return 1 + 2;
                    } else if ((kind == Multiname.MULTINAMEL) || (kind == Multiname.MULTINAMELA)) {
                        return 1 + 1;
                    } else if (kind == Multiname.TYPENAME) {
                        return 1 + 1 + m.params.length;
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            return getChildCount(node) == 0;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (child instanceof ValueWithIndex) {
                ValueWithIndex vwi = (ValueWithIndex)child;
                if (vwi.getType() == type) {
                    return vwi.index;
                }
            }
            return -1;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {            
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }        
    }
    
    public static class ExplorerTreeCellRenderer extends DefaultTreeCellRenderer {

        
        public ExplorerTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            if (View.isOceanic()) {
                setBackgroundNonSelectionColor(Color.white);
            }            
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            /*if (semiTransparent) {
                if (getIcon() != null) {
                    Color color = getBackground();
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2));
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }*/
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
     
            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);                                             
           
            if (View.isOceanic()) {
                setForeground(Color.BLACK);
            }
            setToolTipText(null);

            //semitransparent = true;
            
            return this;
        }
    }
}
