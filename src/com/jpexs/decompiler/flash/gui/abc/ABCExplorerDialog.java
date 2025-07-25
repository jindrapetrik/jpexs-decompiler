/*
 *  Copyright (C) 2023-2025 JPEXS
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.abc.usages.simple.ABCCleaner;
import com.jpexs.decompiler.flash.abc.usages.simple.ABCSimpleUsageDetector;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class ABCExplorerDialog extends AppDialog {

    private final List<ABC> abcs = new ArrayList<>();
    private JComboBox<String> abcComboBox = null;
    private final JLabel tagInfoLabel;
    private final List<Integer> abcFrames = new ArrayList<>();
    private final JTabbedPane mainTabbedPane;
    private final JTabbedPane cpTabbedPane;
    private MainPanel mainPanel;

    private Runnable packListener;

    private ABCSimpleUsageDetector usageDetector = null;

    private JButton cleanButton = new JButton(View.getIcon("clean16"));

    private JTable usagesTable = new JTable(new DefaultTableModel()) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ABCExplorerDialog(Window owner, MainPanel mainPanel, Openable openable, ABC abc) {
        super(owner);
        this.mainPanel = mainPanel;
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanel.add(new JLabel(translate("abc")));
        int selectedIndex = 0;
        int frame = 1;
        if (openable instanceof SWF) {
            SWF swf = (SWF) openable;
            List<ABCContainerTag> abcContainers = new ArrayList<>();
            for (Tag t : swf.getTags()) {
                if (t instanceof ShowFrameTag) {
                    frame++;
                }
                if (t instanceof ABCContainerTag) {
                    ABCContainerTag abcCnt = (ABCContainerTag) t;
                    if (abcCnt.getABC() == abc) {
                        selectedIndex = abcs.size();
                    }
                    abcContainers.add(abcCnt);
                    abcs.add(abcCnt.getABC());
                    abcFrames.add(frame);
                }
            }
            String[] abcComboBoxData = new String[abcs.size()];

            for (int i = 0; i < abcContainers.size(); i++) {
                abcComboBoxData[i] = "tag" + (i + 1);
                if (abcContainers.get(i) instanceof DoABC2Tag) {
                    DoABC2Tag doa2 = (DoABC2Tag) abcContainers.get(i);
                    if (doa2.name != null && !doa2.name.isEmpty()) {
                        abcComboBoxData[i] += " (\"" + Helper.escapePCodeString(doa2.name) + "\")";
                    }
                }
            }
            abcComboBox = new JComboBox<>(abcComboBoxData);

            Dimension abcComboBoxSize = new Dimension(500, abcComboBox.getPreferredSize().height);
            abcComboBox.setMinimumSize(abcComboBoxSize);
            abcComboBox.setPreferredSize(abcComboBoxSize);
            topLeftPanel.add(abcComboBox);
            abcComboBox.addActionListener(this::abcComboBoxActionPerformed);

        } else if (openable instanceof ABC) {
            abcs.add((ABC) openable);
            abcFrames.add(-1);
        }

        this.packListener = new Runnable() {
            @Override
            public void run() {
                int cpIndex = cpTabbedPane.getSelectedIndex();
                int mainIndex = mainTabbedPane.getSelectedIndex();
                abcComboBoxActionPerformed(null);
                cpTabbedPane.setSelectedIndex(cpIndex);
                mainTabbedPane.setSelectedIndex(mainIndex);
                refreshUsages();
            }
        };

        tagInfoLabel = new JLabel();
        topLeftPanel.add(tagInfoLabel);

        cleanButton.setToolTipText(translate("button.clean"));
        cleanButton.addActionListener(this::cleanActionPerformed);

        JPanel topRightPanel = new JPanel(new FlowLayout());
        topRightPanel.add(cleanButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(topLeftPanel, BorderLayout.WEST);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        mainTabbedPane = new JTabbedPane();
        cpTabbedPane = new JTabbedPane();

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn(translate("usages").replace("%item%", "-"));
        usagesTable.setModel(model);

        usagesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = usagesTable.getSelectedRow();
                    if (row == -1) {
                        return;
                    }
                    String path = (String) usagesTable.getModel().getValueAt(row, 0);
                    selectPath(path);
                }
            }
        });

        usagesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = usagesTable.rowAtPoint(e.getPoint());
                    int column = usagesTable.columnAtPoint(e.getPoint());
                    if (!usagesTable.isRowSelected(row)) {
                        usagesTable.changeSelection(row, column, false, false);
                    }
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem hilightMenuItem = new JMenuItem(translate("hilight.usage"), View.getIcon("jumpto16"));
                    hilightMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int row = usagesTable.getSelectedRow();
                            if (row == -1) {
                                return;
                            }
                            selectPath((String) usagesTable.getModel().getValueAt(row, 0));
                        }
                    });
                    JMenuItem copyMenuItem = new JMenuItem(translate("copy.paths"), View.getIcon("copy16"));
                    copyMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int[] rows = usagesTable.getSelectedRows();
                            List<String> values = new ArrayList<>();
                            for (int row : rows) {
                                values.add((String) usagesTable.getModel().getValueAt(row, 0));
                            }
                            copyToClipboard(String.join("\r\n", values));
                        }
                    });
                    JMenuItem copyAllMenuItem = new JMenuItem(translate("copy.paths.all"), View.getIcon("copy16"));
                    copyAllMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<String> values = new ArrayList<>();
                            for (int row = 0; row < usagesTable.getModel().getRowCount(); row++) {
                                values.add((String) usagesTable.getModel().getValueAt(row, 0));
                            }
                            copyToClipboard(String.join("\r\n", values));
                        }
                    });
                    popupMenu.add(hilightMenuItem);
                    popupMenu.add(copyMenuItem);
                    popupMenu.add(copyAllMenuItem);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JPanel centralPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new FasterScrollPane(usagesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        //rightPanel.add(calculateUsagesButton, BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainTabbedPane, rightPanel);
        splitPane.setDividerLocation(800);
        centralPanel.add(splitPane);

        cnt.add(topPanel, BorderLayout.NORTH);
        cnt.add(centralPanel, BorderLayout.CENTER);

        if (!abcs.isEmpty()) {
            if (abcComboBox != null) {
                abcComboBox.setSelectedIndex(selectedIndex);
            } else {
                abcComboBoxActionPerformed(null);
            }
        }

        JRootPane rootPane = getRootPane();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(keyStroke, "ctrlGAction");
        actionMap.put("ctrlGAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = ViewMessages.showInputDialog(ABCExplorerDialog.this, translate("goto.path.label"), translate("goto.path"), "");
                if (path == null || path.isEmpty()) {
                    return;
                }
                selectPath(path);
            }
        });

        setSize(1024, 600);
        setTitle(translate("title") + " - " + openable.getTitleOrShortFileName());
        List<Image> images = new ArrayList<>();
        images.add(View.loadImage("abcexplorer16"));
        images.add(View.loadImage("abcexplorer32"));
        setIconImages(images);
        View.centerScreen(this);
    }

    public void selectAbc(ABC abc) {
        if (abcComboBox == null) {
            return;
        }
        if (abc == null && !abcs.isEmpty()) {
            abcComboBox.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < abcs.size(); i++) {
            if (abcs.get(i) == abc) {
                abcComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private ABC getSelectedAbc() {
        if (abcs.isEmpty()) {
            return null;
        }
        if (abcComboBox == null) {
            return abcs.get(0);
        }
        return abcs.get(abcComboBox.getSelectedIndex());
    }

    private void abcComboBoxActionPerformed(ActionEvent e) {
        usageDetector = null;
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn(translate("usages").replace("%item%", "-"));
        usagesTable.setModel(model);

        int index = abcComboBox == null ? 0 : abcComboBox.getSelectedIndex();
        if (index == -1) {
            return;
        }
        if (abcs.isEmpty()) {
            return;
        }
        ABC abc = abcs.get(index);
        if (abcComboBox == null) {
            tagInfoLabel.setText(translate("abc.info.standalone")
                    .replace("%major%", "" + abc.version.major)
                    .replace("%minor%", "" + abc.version.minor)
                    .replace("%size%", Helper.formatFileSize(abc.getDataSize()))
            );
        } else {
            tagInfoLabel.setText(
                    translate("abc.info")
                            .replace("%index%", "" + (index + 1))
                            .replace("%count%", "" + abcComboBox.getItemCount())
                            .replace("%major%", "" + abc.version.major)
                            .replace("%minor%", "" + abc.version.minor)
                            .replace("%size%", Helper.formatFileSize(abc.getDataSize()))
                            .replace("%frame%", "" + abcFrames.get(index))
            );
        }

        cpTabbedPane.removeAll();

        cpTabbedPane.addTab("int (" + Math.max(0, abc.constants.getIntCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_INT.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_INT));
        cpTabbedPane.addTab("uint (" + Math.max(0, abc.constants.getUIntCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_UINT.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_UINT));
        cpTabbedPane.addTab("dbl (" + Math.max(0, abc.constants.getDoubleCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_DOUBLE.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_DOUBLE));
        if (abc.hasDecimalSupport()) {
            cpTabbedPane.addTab("dc (" + Math.max(0, abc.constants.getDecimalCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_DECIMAL.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_DECIMAL));
        }
        if (abc.hasFloatSupport()) {
            cpTabbedPane.addTab("fl (" + Math.max(0, abc.constants.getFloatCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_FLOAT.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_FLOAT));
        }
        if (abc.hasFloat4Support()) {
            cpTabbedPane.addTab("fl4 (" + Math.max(0, abc.constants.getFloat4Count() - 1) + ")", View.getIcon(TreeType.CONSTANT_FLOAT_4.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_FLOAT_4));
        }
        cpTabbedPane.addTab("str (" + Math.max(0, abc.constants.getStringCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_STRING.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_STRING));
        cpTabbedPane.addTab("ns (" + Math.max(0, abc.constants.getNamespaceCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_NAMESPACE.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_NAMESPACE));
        cpTabbedPane.addTab("nss (" + Math.max(0, abc.constants.getNamespaceSetCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_NAMESPACE_SET.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_NAMESPACE_SET));
        cpTabbedPane.addTab("mn (" + Math.max(0, abc.constants.getMultinameCount() - 1) + ")", View.getIcon(TreeType.CONSTANT_MULTINAME.getIcon().getFile()), makeTreePanel(abc, TreeType.CONSTANT_MULTINAME));

        mainTabbedPane.removeAll();

        JPanel cpPanel = new JPanel(new BorderLayout());
        cpPanel.add(cpTabbedPane, BorderLayout.CENTER);

        int cpCount = Math.max(0, abc.constants.getIntCount() - 1)
                + Math.max(0, abc.constants.getUIntCount() - 1)
                + Math.max(0, abc.constants.getDoubleCount() - 1)
                + Math.max(0, abc.constants.getStringCount() - 1)
                + Math.max(0, abc.constants.getNamespaceCount() - 1)
                + Math.max(0, abc.constants.getNamespaceSetCount() - 1)
                + Math.max(0, abc.constants.getMultinameCount() - 1)
                + (abc.hasDecimalSupport() ? Math.max(0, abc.constants.getDecimalCount() - 1) : 0)
                + (abc.hasFloatSupport() ? Math.max(0, abc.constants.getFloatCount() - 1) : 0)
                + (abc.hasFloat4Support() ? Math.max(0, abc.constants.getFloat4Count() - 1) : 0);
        mainTabbedPane.addTab("cp (" + cpCount + ")", View.getIcon("abcconstantpool16"), cpPanel);
        mainTabbedPane.addTab("mi (" + abc.method_info.size() + ")", View.getIcon(TreeType.METHOD_INFO.getIcon().getFile()), makeTreePanel(abc, TreeType.METHOD_INFO));
        mainTabbedPane.addTab("md (" + abc.metadata_info.size() + ")", View.getIcon(TreeType.METADATA_INFO.getIcon().getFile()), makeTreePanel(abc, TreeType.METADATA_INFO));
        mainTabbedPane.addTab("ii (" + abc.instance_info.size() + ")", View.getIcon(TreeType.INSTANCE_INFO.getIcon().getFile()), makeTreePanel(abc, TreeType.INSTANCE_INFO));
        mainTabbedPane.addTab("ci (" + abc.class_info.size() + ")", View.getIcon(TreeType.CLASS_INFO.getIcon().getFile()), makeTreePanel(abc, TreeType.CLASS_INFO));
        mainTabbedPane.addTab("si (" + abc.script_info.size() + ")", View.getIcon(TreeType.SCRIPT_INFO.getIcon().getFile()), makeTreePanel(abc, TreeType.SCRIPT_INFO));
        mainTabbedPane.addTab("mb (" + abc.bodies.size() + ")", View.getIcon(TreeType.METHOD_BODY.getIcon().getFile()), makeTreePanel(abc, TreeType.METHOD_BODY));

        abc.removeChangeListener(packListener);
        abc.addChangeListener(packListener);
        refreshUsages();
        repaint();
    }

    private void refreshUsages() {
        ABCSimpleUsageDetector newUsageDetector = new ABCSimpleUsageDetector(getSelectedAbc());
        newUsageDetector.detect();
        usageDetector = newUsageDetector;
        int zeroUsages = newUsageDetector.getZeroUsagesCount();
        cleanButton.setText("(" + zeroUsages + ")");
        cleanButton.setEnabled(zeroUsages > 0);
    }

    private JTree getCurrentTree() {
        JPanel pan;
        if (mainTabbedPane.getSelectedIndex() == 0) { //cp
            pan = (JPanel) cpTabbedPane.getSelectedComponent();
        } else {
            pan = (JPanel) mainTabbedPane.getSelectedComponent();
        }
        FasterScrollPane fasterScrollPane = (FasterScrollPane) pan.getComponent(0);
        JTree tree = (JTree) fasterScrollPane.getViewport().getView();
        return tree;
    }

    private String getCurrentPath() {
        JTree tree = getCurrentTree();
        TreePath tp = tree.getSelectionPath();
        if (tp == null) {
            return "";
        }
        List<String> pathParts = new ArrayList<>();
        Object[] path = tp.getPath();
        for (int i = 1; i < path.length; i++) {
            Object child = path[i];
            String key;
            if (child instanceof ValueWithIndex) {
                ValueWithIndex vwi = (ValueWithIndex) child;
                if (!vwi.getTitle().isEmpty()) {
                    key = vwi.getTitle();
                } else {
                    key = vwi.getType().getAbbreviation() + vwi.getIndex();
                }
            } else if (child instanceof SubValue) {
                SubValue sv = (SubValue) child;
                key = sv.getTitle();
            } else if (child instanceof SimpleValue) {
                SimpleValue sv = (SimpleValue) child;
                key = sv.getTitle();
            } else {
                break;
            }
            pathParts.add(key);
        }
        return String.join("/", pathParts);
    }

    public void selectTrait(int scriptIndex, int classIndex, int traitIndex, int traitType) {
        selectScriptInfo(scriptIndex);

        JTree tree = getCurrentTree();
        Object selection = tree.getLastSelectedPathComponent();
        if (selection == null) {
            return;
        }
        int classTraitIndexInScript = -1;
        if (classIndex != -1) {
            classTraitIndexInScript = findClassTraitIndexInScript(scriptIndex, classIndex);
            if (classTraitIndexInScript == -1) {
                return;
            }
        }
        if (traitIndex == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/init");
        } else if (traitIndex == GraphTextWriter.TRAIT_CLASS_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/traits/t" + classTraitIndexInScript + "/class_info/cinit");
        } else if (traitIndex == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/traits/t" + classTraitIndexInScript + "/instance_info/iinit");
        } else if (traitType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/traits/t" + traitIndex);
        } else if (traitType == GraphTextWriter.TRAIT_CLASS_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/traits/t" + classTraitIndexInScript + "/class_info/traits/t" + traitIndex);
        } else if (traitType == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER) {
            selectPath(tree, "si" + scriptIndex + "/traits/t" + classTraitIndexInScript + "/instance_info/traits/t" + traitIndex);
        }
    }

    private int findClassTraitIndexInScript(int scriptIndex, int classIndex) {
        ABC abc = getSelectedAbc();
        for (int i = 0; i < abc.script_info.get(scriptIndex).traits.traits.size(); i++) {
            Trait t = abc.script_info.get(scriptIndex).traits.traits.get(i);
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                if (tc.class_info == classIndex) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void selectPath(String path) {
        String[] parts = path.split("/");
        String mainItem = parts[0];
        TreeType selectedType = null;
        for (TreeType type : TreeType.values()) {
            if (mainItem.startsWith(type.getAbbreviation())) {
                selectedType = type;
            }
        }
        if (selectedType == null) {
            return;
        }

        if (mainTabbedPane.getTabCount() == 0) {
            return;
        }

        int stringOffset = 0;
        if (getSelectedAbc().hasDecimalSupport()) {
            stringOffset = 1;
        }
        if (getSelectedAbc().hasFloatSupport()) {
            stringOffset++;
        }
        if (getSelectedAbc().hasFloat4Support()) {
            stringOffset++;
        }
        switch (selectedType) {
            case CONSTANT_INT:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(0);
                break;
            case CONSTANT_UINT:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(1);
                break;
            case CONSTANT_DOUBLE:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(2);
                break;
            case CONSTANT_DECIMAL:
                if (!getSelectedAbc().hasDecimalSupport()) {
                    return;
                }
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(3);
                break;
            case CONSTANT_FLOAT:
                if (!getSelectedAbc().hasFloatSupport()) {
                    return;
                }
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(3);
                break;
            case CONSTANT_FLOAT_4:
                if (!getSelectedAbc().hasFloat4Support()) {
                    return;
                }
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(4);
                break;
            case CONSTANT_STRING:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(3 + stringOffset);
                break;
            case CONSTANT_NAMESPACE:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(4 + stringOffset);
                break;
            case CONSTANT_NAMESPACE_SET:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(5 + stringOffset);
                break;
            case CONSTANT_MULTINAME:
                mainTabbedPane.setSelectedIndex(0);
                cpTabbedPane.setSelectedIndex(6 + stringOffset);
                break;
            case METHOD_INFO:
                mainTabbedPane.setSelectedIndex(1);
                break;
            case METADATA_INFO:
                mainTabbedPane.setSelectedIndex(2);
                break;
            case INSTANCE_INFO:
                mainTabbedPane.setSelectedIndex(3);
                break;
            case CLASS_INFO:
                mainTabbedPane.setSelectedIndex(4);
                break;
            case SCRIPT_INFO:
                mainTabbedPane.setSelectedIndex(5);
                break;
            case METHOD_BODY:
                mainTabbedPane.setSelectedIndex(6);
                break;
        }

        selectPath(getCurrentTree(), path);
    }

    private void selectPath(JTree tree, String path) {
        String[] parts = path.split("/");
        TreeModel model = tree.getModel();
        Object root = model.getRoot();
        Object parent = root;

        List<Object> treePathObjectsList = new ArrayList<>();
        /*Object[] treePathObjects = new Object[parts.length + 1];
        treePathObjects[0] = root;*/
        treePathObjectsList.add(root);

        loopp:
        for (int p = 0; p < parts.length; p++) {
            String part = parts[p];
            for (int i = 0; i < model.getChildCount(parent); i++) {
                Object child = model.getChild(parent, i);
                String key = "";
                if (child instanceof ValueWithIndex) {
                    ValueWithIndex vwi = (ValueWithIndex) child;
                    if (!vwi.getTitle().isEmpty()) {
                        key = vwi.getTitle();
                    } else {
                        key = vwi.getType().getAbbreviation() + vwi.getIndex();
                    }
                } else if (child instanceof SubValue) {
                    SubValue sv = (SubValue) child;
                    key = sv.getTitle();
                } else if (child instanceof SimpleValue) {
                    SimpleValue sv = (SimpleValue) child;
                    key = sv.getTitle();
                }
                if (key.equals(part)) {
                    //treePathObjects[1 + p] = child;
                    treePathObjectsList.add(child);
                    parent = child;
                    continue loopp;
                }
            }
        }
        TreePath treePath = new TreePath(treePathObjectsList.toArray(new Object[treePathObjectsList.size()]));
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }

    public void selectScriptInfo(int scriptIndex) {
        if (mainTabbedPane.getTabCount() > 0) {
            mainTabbedPane.setSelectedIndex(5);
            JPanel pan = (JPanel) mainTabbedPane.getComponentAt(5);
            FasterScrollPane fasterScrollPane = (FasterScrollPane) pan.getComponent(0);
            JTree tree = (JTree) fasterScrollPane.getViewport().getView();
            TreeModel model = tree.getModel();
            if (scriptIndex >= model.getChildCount(model.getRoot())) {
                return;
            }
            Object scriptInfoNode = model.getChild(model.getRoot(), scriptIndex);
            TreePath path = new TreePath(new Object[]{
                model.getRoot(),
                scriptInfoNode
            });
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }

    private JPanel makeTreePanel(ABC abc, TreeType type) {
        JTree tree = new JTree(new ExplorerTreeModel(abc, type));
        if (View.isOceanic()) {
            tree.setBackground(Color.white);
        }
        tree.setCellRenderer(new ExplorerTreeCellRenderer(this));
        tree.setUI(new BasicTreeUI() {
            {
                if (View.isOceanic()) {
                    setHashColor(Color.gray);
                }
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultTableModel model = new DefaultTableModel();

                if (tree.getSelectionCount() != 1 || usageDetector == null) {
                    //usagesList.setModel(new DefaultListModel<>());
                    model.addColumn(translate("usages").replace("%item%", "-"));
                    usagesTable.setModel(model);
                    return;
                }

                Object selection = tree.getLastSelectedPathComponent();

                if (selection instanceof ValueWithIndex) {
                    ValueWithIndex vwi = (ValueWithIndex) selection;
                    if (vwi.getType().getUsageKind() != null) {
                        List<String> newUsages = usageDetector.getUsages(vwi.type.getUsageKind(), vwi.index);
                        //DefaultListModel<String> model = new DefaultListModel<>();
                        //model.addAll(newUsages);
                        //usagesList.setModel(model);                   
                        model.addColumn(translate("usages").replace("%item%", vwi.type.getAbbreviation() + vwi.index + ": " + vwi.getDescription()));
                        for (String usage : newUsages) {
                            model.addRow(new Object[]{usage});
                        }
                        usagesTable.setModel(model);
                    } else {
                        model.addColumn(translate("usages").replace("%item%", "-"));
                    }
                } else {
                    model.addColumn(translate("usages").replace("%item%", "-"));
                }
                usagesTable.setModel(model);
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    JPopupMenu popupMenu = createTreePopup(tree);
                    if (popupMenu == null) {
                        return;
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(new FasterScrollPane(tree), BorderLayout.CENTER);
        return treePanel;
    }

    private JPopupMenu createTreePopup(JTree tree) {
        JPopupMenu menu = new JPopupMenu();
        if (tree.getSelectionCount() != 1) {
            return null;
        }

        Object selection = tree.getLastSelectedPathComponent();

        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            if (vwi.getType() == TreeType.SCRIPT_INFO) {
                JMenuItem showInMainWindowMenuItem = new JMenuItem(translate("show.script"), View.getIcon("show16"));
                showInMainWindowMenuItem.addActionListener(this::showInMainWindowActionPerformed);
                menu.add(showInMainWindowMenuItem);
            }

            if (vwi.getType() == TreeType.METHOD_INFO || vwi.getType() == TreeType.METHOD_BODY) {
                JMenuItem showInMainWindowMenuItem = new JMenuItem(translate("show.method"), View.getIcon("show16"));
                showInMainWindowMenuItem.addActionListener(this::showInMainWindowActionPerformed);
                menu.add(showInMainWindowMenuItem);
            }

            if (vwi.getType() == TreeType.INSTANCE_INFO || vwi.getType() == TreeType.CLASS_INFO) {
                JMenuItem showInMainWindowMenuItem = new JMenuItem(translate("show.class"), View.getIcon("show16"));
                showInMainWindowMenuItem.addActionListener(this::showInMainWindowActionPerformed);
                menu.add(showInMainWindowMenuItem);
            }
        }
        if (selection instanceof SubValue) {
            SubValue sv = (SubValue) selection;

            switch (sv.getIcon()) {
                case TRAIT_CLASS:
                case TRAIT_CONST:
                case TRAIT_FUNCTION:
                case TRAIT_GETTER:
                case TRAIT_METHOD:
                case TRAIT_SETTER:
                case TRAIT_SLOT:
                    if (!(sv.parentValue instanceof MethodBody)) {
                        JMenuItem showInMainWindowMenuItem = new JMenuItem(translate("show.trait"), View.getIcon("show16"));
                        showInMainWindowMenuItem.addActionListener(this::showInMainWindowActionPerformed);
                        menu.add(showInMainWindowMenuItem);
                    }
                    break;
            }
        }

        if (selection != null) {
            JMenuItem copyMenuItem = new JMenuItem(translate("copy.path"), View.getIcon("copy16"));
            copyMenuItem.addActionListener(this::copyPathActionPerformed);
            menu.add(copyMenuItem);

            JMenuItem copyRowMenuItem = new JMenuItem(translate("copy.row"), View.getIcon("copy16"));
            copyRowMenuItem.addActionListener(this::copyRowActionPerformed);
            menu.add(copyRowMenuItem);
        }
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;

            if (!vwi.getTitle().isEmpty()) {
                JMenuItem copyTitleMenuItem = new JMenuItem(translate("copy.title"), View.getIcon("copy16"));
                copyTitleMenuItem.addActionListener(this::copyTitleActionPerformed);
                menu.add(copyTitleMenuItem);
            }
            JMenuItem copyTypeIdMenuItem = new JMenuItem(translate("copy.typeid"), View.getIcon("copy16"));
            copyTypeIdMenuItem.addActionListener(this::copyTypeIdActionPerformed);
            menu.add(copyTypeIdMenuItem);

            if (!vwi.getDescription().isEmpty()) {
                JMenuItem copyValueMenuItem = new JMenuItem(translate("copy.value"), View.getIcon("copy16"));
                copyValueMenuItem.addActionListener(this::copyValueActionPerformed);
                menu.add(copyValueMenuItem);
            }
            if (vwi.getType() == TreeType.CONSTANT_STRING && vwi.getIndex() > 0) {
                JMenuItem copyRawStringValueMenuItem = new JMenuItem(translate("copy.rawstring"), View.getIcon("copy16"));
                copyRawStringValueMenuItem.addActionListener(this::copyRawStringValueActionPerformed);
                menu.add(copyRawStringValueMenuItem);
            }
        } else if (selection instanceof SubValue) {
            SubValue sv = (SubValue) selection;
            JMenuItem copyTitleMenuItem = new JMenuItem(translate("copy.title"), View.getIcon("copy16"));
            copyTitleMenuItem.addActionListener(this::copyTitleActionPerformed);
            menu.add(copyTitleMenuItem);
            if (!sv.getDescription().isEmpty()) {
                JMenuItem copyValueMenuItem = new JMenuItem(translate("copy.value"), View.getIcon("copy16"));
                copyValueMenuItem.addActionListener(this::copyValueActionPerformed);
                menu.add(copyValueMenuItem);
            }
        } else if (selection instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) selection;

            JMenuItem copyTitleMenuItem = new JMenuItem(translate("copy.title"), View.getIcon("copy16"));
            copyTitleMenuItem.addActionListener(this::copyTitleActionPerformed);
            menu.add(copyTitleMenuItem);

            if (!sv.getValue().isEmpty()) {
                JMenuItem copyValueMenuItem = new JMenuItem(translate("copy.value"), View.getIcon("copy16"));
                copyValueMenuItem.addActionListener(this::copyValueActionPerformed);
                menu.add(copyValueMenuItem);
            }
        } else {
            return null;
        }

        return menu;
    }

    private void copyPathActionPerformed(ActionEvent e) {
        copyToClipboard(getCurrentPath());
    }

    private void copyRowActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        if (selection != null) {
            copyToClipboard(selection.toString());
        }
    }

    private boolean showMethodInfoTraits(int round, int scriptIndex, int classIndex, int methodInfo, ABC abc, Traits traits, int traitsType, int scriptTraitIndex) {
        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = (Trait) traits.traits.get(j);
            if (traitsType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
                scriptTraitIndex = j;
            }
            int globalTraitIndex = j;
            if (traitsType == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER) {
                globalTraitIndex += abc.class_info.get(classIndex).static_traits.traits.size();
            }
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                if (showMethodInfo(round, abc, abc.class_info.get(tc.class_info).cinit_index, methodInfo, scriptIndex, tc.class_info, GraphTextWriter.TRAIT_CLASS_INITIALIZER, scriptTraitIndex)) {
                    return true;
                }

                if (showMethodInfo(round, abc, abc.instance_info.get(tc.class_info).iinit_index, methodInfo, scriptIndex, tc.class_info, GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, scriptTraitIndex)) {
                    return true;
                }

                if (showMethodInfoTraits(round, scriptIndex, tc.class_info, methodInfo, abc, abc.class_info.get(tc.class_info).static_traits, GraphTextWriter.TRAIT_CLASS_INITIALIZER, scriptTraitIndex)) {
                    return true;
                }
                if (showMethodInfoTraits(round, scriptIndex, tc.class_info, methodInfo, abc, abc.instance_info.get(tc.class_info).instance_traits, GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, scriptTraitIndex)) {
                    return true;
                }
            }
            if (t instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
                if (showMethodInfo(round, abc, tmgs.method_info, methodInfo, scriptIndex, classIndex, globalTraitIndex, scriptTraitIndex)) {
                    return true;
                }
            }
            if (t instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) t;
                if (showMethodInfo(round, abc, tf.method_info, methodInfo, scriptIndex, classIndex, globalTraitIndex, scriptTraitIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean showMethodInfo(int round, ABC abc, int methodInfo, int searchMethodInfo, int scriptIndex, int classIndex, int globalTraitIndex, int scriptTraitIndex) {
        boolean found = false;
        if (methodInfo == searchMethodInfo) {
            found = true;
        } else if (round == 2) {
            Queue<Integer> methods = new ArrayDeque<>();
            Set<Integer> visitedMethods = new HashSet<>();
            methods.add(methodInfo);

            loopm:
            while (!methods.isEmpty()) {
                methodInfo = methods.poll();
                if (visitedMethods.contains(methodInfo)) {
                    continue;
                }
                visitedMethods.add(methodInfo);
                MethodBody body = abc.findBody(methodInfo);
                if (body != null) {
                    for (AVM2Instruction ins : body.getCode().code) {
                        if (ins.definition instanceof NewFunctionIns) {
                            if (ins.operands[0] == searchMethodInfo) {
                                found = true;
                                break loopm;
                            } else {
                                methods.add(ins.operands[0]);
                            }
                        }
                    }
                }
            }
        }
        if (found) {
            DottedChain scriptNameDc = abc.script_info.get(scriptIndex).getSimplePackName(abc, new LinkedHashSet<>());
            if (scriptNameDc == null && scriptTraitIndex > -1) {
                scriptNameDc = abc.script_info.get(scriptIndex).traits.traits.get(scriptTraitIndex).getName(abc).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, false);
            }
            String scriptName = (scriptNameDc == null ? "script_" + scriptIndex : scriptNameDc.toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true));
            //mainPanel.gotoScriptTrait(abc.getSwf(), scriptName, classIndex, globalTraitIndex);
            mainPanel.gotoScriptMethod(abc.getSwf(), scriptName, searchMethodInfo);
        }
        return found;
    }

    private void showMethodInfo(int methodInfo) {
        ABC abc = getSelectedAbc();
        for (int round = 1; round <= 2; round++) {
            for (int i = 0; i < abc.script_info.size(); i++) {
                if (showMethodInfo(round, abc, abc.script_info.get(i).init_index, methodInfo, i, -1, GraphTextWriter.TRAIT_SCRIPT_INITIALIZER, -1)) {
                    return;
                }
                if (showMethodInfoTraits(round, i, -1, methodInfo, abc, abc.script_info.get(i).traits, GraphTextWriter.TRAIT_SCRIPT_INITIALIZER, -1)) {
                    return;
                }
            }
        }
    }

    private void showInMainWindowActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        ABC abc = getSelectedAbc();
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            int scriptIndex = -1;
            switch (vwi.type) {
                case SCRIPT_INFO:
                    scriptIndex = vwi.getIndex();
                    DottedChain scriptNameDc = abc.script_info.get(scriptIndex).getSimplePackName(abc, new LinkedHashSet<>());
                    String scriptName = (scriptNameDc == null ? "script_" + scriptIndex : scriptNameDc.toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true));
                    mainPanel.gotoScriptName(abc.getSwf(), scriptName);
                    break;
                case METHOD_BODY:
                    MethodBody body = (MethodBody) vwi.getRawValue();
                    if (body != null) {
                        showMethodInfo(body.method_info);
                    }
                    break;
                case METHOD_INFO:
                    showMethodInfo(vwi.getIndex());
                    break;
                case INSTANCE_INFO:
                case CLASS_INFO:
                    int classIndex = vwi.getIndex();
                    int scriptTraitIndex = -1;
                    loopc:
                    for (int i = 0; i < abc.script_info.size(); i++) {
                        for (int j = 0; j < abc.script_info.get(i).traits.traits.size(); j++) {
                            Trait t = (Trait) abc.script_info.get(i).traits.traits.get(j);
                            if (t instanceof TraitClass) {
                                TraitClass tc = (TraitClass) t;
                                if (tc.class_info == classIndex) {
                                    scriptIndex = i;
                                    scriptTraitIndex = j;
                                    break loopc;
                                }
                            }
                        }
                    }
                    if (scriptIndex != -1) {
                        DottedChain scriptNameDc2 = abc.script_info.get(scriptIndex).getSimplePackName(abc, new LinkedHashSet<>());
                        if (scriptNameDc2 == null && scriptTraitIndex != -1) {
                            scriptNameDc2 = abc.script_info.get(scriptIndex).traits.traits.get(scriptTraitIndex).getName(abc).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, false);
                        }
                        String scriptName2 = (scriptNameDc2 == null ? "script_" + scriptIndex : scriptNameDc2.toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true));
                        mainPanel.gotoScriptTrait(abc.getSwf(), scriptName2, classIndex, GraphTextWriter.TRAIT_CLASS_INITIALIZER);
                    }
                    break;
            }
        }
        if (selection instanceof SubValue) {
            SubValue sv = (SubValue) selection;
            switch (sv.getIcon()) {
                case TRAIT_CLASS:
                case TRAIT_CONST:
                case TRAIT_FUNCTION:
                case TRAIT_GETTER:
                case TRAIT_METHOD:
                case TRAIT_SETTER:
                case TRAIT_SLOT:
                    int traitIndex = sv.getIndex();
                    int globalTraitIndex = traitIndex;
                    SubValue sv1 = (SubValue) sv.getParent();
                    ValueWithIndex wvi = (ValueWithIndex) sv1.getParent();
                    int scriptIndex = -1;
                    int classIndex = -1;
                    int scriptTraitIndex = -1;
                    if (sv.getParentValue() instanceof ScriptInfo) {
                        scriptIndex = wvi.getIndex();
                        scriptTraitIndex = traitIndex;
                    } else {
                        classIndex = wvi.getIndex();
                        if (sv.getParentValue() instanceof InstanceInfo) {
                            globalTraitIndex += abc.class_info.get(classIndex).static_traits.traits.size();
                        }

                        loopi:
                        for (int i = 0; i < abc.script_info.size(); i++) {
                            for (int j = 0; j < abc.script_info.get(i).traits.traits.size(); j++) {
                                Trait t = (Trait) abc.script_info.get(i).traits.traits.get(j);
                                if (t instanceof TraitClass) {
                                    TraitClass tc = (TraitClass) t;
                                    if (tc.class_info == classIndex) {
                                        scriptIndex = i;
                                        scriptTraitIndex = j;
                                        break loopi;
                                    }
                                }
                            }
                        }
                    }

                    if (scriptIndex != -1) {
                        DottedChain scriptNameDc = abc.script_info.get(scriptIndex).getSimplePackName(abc, new LinkedHashSet<>());
                        if (scriptNameDc == null && scriptTraitIndex != -1) {
                            scriptNameDc = abc.script_info.get(scriptIndex).traits.traits.get(scriptTraitIndex).getName(abc).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, false);
                        }

                        String scriptName = (scriptNameDc == null ? "script_" + scriptIndex : scriptNameDc.toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true));
                        mainPanel.gotoScriptTrait(abc.getSwf(), scriptName, classIndex, globalTraitIndex);
                    }

                    break;
            }
        }
    }

    private void copyTitleActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        if (selection instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) selection;
            copyToClipboard(sv.getTitle());
        }
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            copyToClipboard(vwi.getTitle());
        }
        if (selection instanceof SubValue) {
            SubValue sv = (SubValue) selection;
            copyToClipboard(sv.getTitle());
        }
    }

    private void copyValueActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        if (selection instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) selection;
            copyToClipboard(sv.getValue());
        }
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            copyToClipboard(vwi.getDescription());
        }
        if (selection instanceof SubValue) {
            SubValue sv = (SubValue) selection;
            copyToClipboard(sv.getDescription());
        }
    }

    private void copyRawStringValueActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            copyToClipboard((String) vwi.getRawValue());
        }
    }

    private void copyTypeIdActionPerformed(ActionEvent e) {
        Object selection = getCurrentTree().getLastSelectedPathComponent();
        if (selection instanceof ValueWithIndex) {
            ValueWithIndex vwi = (ValueWithIndex) selection;
            copyToClipboard(vwi.getType().getAbbreviation() + vwi.getIndex());
        }
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private enum TreeIcon {
        CONSTANT_INT("abcint16"),
        CONSTANT_UINT("abcuint16"),
        CONSTANT_DOUBLE("abcdouble16"),
        CONSTANT_DECIMAL("abcdecimal16"), //needs ABC decimal support
        CONSTANT_FLOAT("abcfloat16"), //needs ABC float support
        CONSTANT_FLOAT_4("abcfloat416"), //needs ABC float support
        CONSTANT_STRING("abcstring16"),
        CONSTANT_NAMESPACE("abcnamespace16"),
        CONSTANT_NAMESPACE_SET("abcnamespaceset16"),
        CONSTANT_MULTINAME("abcmultiname16"),
        METHOD_INFO("abcmethodinfo16"),
        METADATA_INFO("abcmetadata16"),
        INSTANCE_INFO("abcinstanceinfo16"),
        CLASS_INFO("abcclassinfo16"),
        SCRIPT_INFO("abcscriptinfo16"),
        METHOD_BODY("abcmethodbody16"),
        TRAIT_METADATA("folder16"),
        METADATA_PAIRS("folder16"),
        METADATA_PAIRS_SUB(""),
        TRAITS("abctraits16"),
        TRAITS_SUB(""),
        TRAIT_SLOT("variable"),
        TRAIT_CONST("constant"),
        TRAIT_CLASS("asclass16"),
        TRAIT_METHOD("function"),
        TRAIT_GETTER("function"),
        TRAIT_SETTER("function"),
        TRAIT_FUNCTION("function"),
        PARAM_TYPES(""),
        OPTIONAL("folder16"),
        OPTIONAL_SUB(""),
        PARAM_NAMES("folder16"),
        EXCEPTIONS("folder16"),
        EXCEPTIONS_SUB("abcexception16"),
        INTERFACES("asinterface16"),
        KIND("abcbulletblue16"),
        FLAGS("abcflags16"),
        SLOT_ID(""),
        VALUE_INDEX(""),
        VALUE_KIND(""),
        DISP_ID(""),
        MAX_STACK(""),
        MAX_REGS(""),
        INIT_SCOPE_DEPTH(""),
        MAX_SCOPE_DEPTH(""),
        CODE("abccode16"),
        EXCEPTION_START(""),
        EXCEPTION_END(""),
        EXCEPTION_TARGET("");

        private final String DEFAULT_FILE = "abcbulletgray16";

        private String file;

        private TreeIcon(String file) {
            this.file = file;
        }

        public String getFile() {
            if (file.isEmpty()) {
                return DEFAULT_FILE;
            }
            return file;
        }
    }

    private enum TreeType {
        CONSTANT_INT("Integers", "int", TreeIcon.CONSTANT_INT, ABCSimpleUsageDetector.ItemKind.INT),
        CONSTANT_UINT("UnsignedIntegers", "uint", TreeIcon.CONSTANT_UINT, ABCSimpleUsageDetector.ItemKind.UINT),
        CONSTANT_DOUBLE("Doubles", "dbl", TreeIcon.CONSTANT_DOUBLE, ABCSimpleUsageDetector.ItemKind.DOUBLE),
        CONSTANT_DECIMAL("Decimals", "dc", TreeIcon.CONSTANT_DECIMAL, ABCSimpleUsageDetector.ItemKind.DECIMAL), //needs ABC decimal support
        CONSTANT_FLOAT("Floats", "fl", TreeIcon.CONSTANT_FLOAT, ABCSimpleUsageDetector.ItemKind.FLOAT), //needs ABC float support
        CONSTANT_FLOAT_4("Floats4", "fl4", TreeIcon.CONSTANT_FLOAT_4, ABCSimpleUsageDetector.ItemKind.FLOAT4), //needs ABC float4 support
        CONSTANT_STRING("Strings", "str", TreeIcon.CONSTANT_STRING, ABCSimpleUsageDetector.ItemKind.STRING),
        CONSTANT_NAMESPACE("Namespaces", "ns", TreeIcon.CONSTANT_NAMESPACE, ABCSimpleUsageDetector.ItemKind.NAMESPACE),
        CONSTANT_NAMESPACE_SET("NamespaceSets", "nss", TreeIcon.CONSTANT_NAMESPACE_SET, ABCSimpleUsageDetector.ItemKind.NAMESPACESET),
        CONSTANT_MULTINAME("Multinames", "mn", TreeIcon.CONSTANT_MULTINAME, ABCSimpleUsageDetector.ItemKind.MULTINAME),
        METHOD_INFO("MethodInfos", "mi", TreeIcon.METHOD_INFO, ABCSimpleUsageDetector.ItemKind.METHODINFO),
        METADATA_INFO("MetadataInfos", "md", TreeIcon.METADATA_INFO, ABCSimpleUsageDetector.ItemKind.METADATAINFO),
        INSTANCE_INFO("InstanceInfos", "ii", TreeIcon.INSTANCE_INFO, ABCSimpleUsageDetector.ItemKind.CLASS),
        CLASS_INFO("ClassInfos", "ci", TreeIcon.CLASS_INFO, ABCSimpleUsageDetector.ItemKind.CLASS),
        SCRIPT_INFO("ScriptInfos", "si", TreeIcon.SCRIPT_INFO, null),
        METHOD_BODY("MethodBodys", "mb", TreeIcon.METHOD_BODY, ABCSimpleUsageDetector.ItemKind.METHODBODY);

        private final String name;
        private final String abbreviation;
        private final TreeIcon icon;
        private final ABCSimpleUsageDetector.ItemKind usageKind;

        TreeType(String name, String abbreviation, TreeIcon icon, ABCSimpleUsageDetector.ItemKind usageKind) {
            this.name = name;
            this.abbreviation = abbreviation;
            this.icon = icon;
            this.usageKind = usageKind;
        }

        public String getName() {
            return name;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public TreeIcon getIcon() {
            return icon;
        }

        @Override
        public String toString() {
            return name;
        }

        public ABCSimpleUsageDetector.ItemKind getUsageKind() {
            return usageKind;
        }
    }

    private class SimpleValue implements HasIcon {

        private final int currentLevelIndex;
        private final Object parent;
        private final String title;
        private final TreeIcon icon;
        private final String value;

        public SimpleValue(Object parent, int currentLevelIndex, String title, String value, TreeIcon icon) {
            this.currentLevelIndex = currentLevelIndex;
            this.parent = parent;
            this.title = title;
            this.icon = icon;
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        @Override
        public TreeIcon getIcon() {
            return icon;
        }

        public int getCurrentLevelIndex() {
            return currentLevelIndex;
        }

        public Object getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return title + (!value.isEmpty() ? ": " + value : "");
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.currentLevelIndex;
            hash = 47 * hash + Objects.hashCode(this.parent);
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
            final SimpleValue other = (SimpleValue) obj;
            if (this.currentLevelIndex != other.currentLevelIndex) {
                return false;
            }
            return Objects.equals(this.parent, other.parent);
        }

    }

    private interface HasIcon {

        public TreeIcon getIcon();
    }

    private class SubValue implements HasIcon {

        private final int currentLevelIndex;
        private final Object parent;
        private final Object parentValue;
        private final String property;
        private final String title;
        private final int index;
        private final TreeIcon icon;
        private final String description;

        public SubValue(Object parent, int currentLevelIndex, Object parentValue, String property, String title, String description, TreeIcon icon) {
            this.currentLevelIndex = currentLevelIndex;
            this.parent = parent;
            this.parentValue = parentValue;
            this.property = property;
            this.title = title;
            this.index = -1;
            this.icon = icon;
            this.description = description;
        }

        public SubValue(Object parent, int currentLevelIndex, int index, Object parentValue, String property, String title, String description, TreeIcon icon) {
            this.currentLevelIndex = currentLevelIndex;
            this.index = index;
            this.parent = parent;
            this.parentValue = parentValue;
            this.property = property;
            this.title = title;
            this.icon = icon;
            this.description = description;
        }

        public int getIndex() {
            return index;
        }

        public int getCurrentLevelIndex() {
            return currentLevelIndex;
        }

        @Override
        public String toString() {
            return title + (!description.isEmpty() ? ": " + description : "");
        }

        public Object getParent() {
            return parent;
        }

        public Object getParentValue() {
            return parentValue;
        }

        public String getProperty() {
            return property;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + this.currentLevelIndex;
            hash = 41 * hash + Objects.hashCode(this.parent);
            hash = 41 * hash + Objects.hashCode(this.property);
            hash = 41 * hash + this.index;
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
            final SubValue other = (SubValue) obj;
            if (this.currentLevelIndex != other.currentLevelIndex) {
                return false;
            }
            if (this.index != other.index) {
                return false;
            }
            if (!Objects.equals(this.property, other.property)) {
                return false;
            }
            return Objects.equals(this.parent, other.parent);
        }

        @Override
        public TreeIcon getIcon() {
            return icon;
        }
    }

    private class ValueWithIndex implements HasIcon {

        private final Object parent;
        private final int index;
        private final int currentLevelIndex;
        private final TreeType type;
        private final Object value;
        private final String description;
        private final String title;

        public ValueWithIndex(Object parent, int currentLevelIndex, int index, TreeType type, Object value, String description) {
            this.parent = parent;
            this.currentLevelIndex = currentLevelIndex;
            this.index = index;
            this.type = type;
            this.value = value;
            this.description = description;
            this.title = "";
        }

        public ValueWithIndex(Object parent, int currentLevelIndex, int index, TreeType type, Object value, String description, String title) {
            this.parent = parent;
            this.currentLevelIndex = currentLevelIndex;
            this.index = index;
            this.type = type;
            this.value = value;
            this.description = description;
            this.title = title;
        }

        public Object getRawValue() {
            return value;
        }

        public int getCurrentLevelIndex() {
            return currentLevelIndex;
        }

        public Object getParent() {
            return parent;
        }

        public int getIndex() {
            return index;
        }

        public TreeType getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            boolean implicit = false;
            if (index == 0) {
                switch (type) {
                    case CONSTANT_INT:
                    case CONSTANT_UINT:
                    case CONSTANT_DOUBLE:
                    case CONSTANT_DECIMAL:
                    case CONSTANT_FLOAT:
                    case CONSTANT_FLOAT_4:
                    case CONSTANT_STRING:
                    case CONSTANT_NAMESPACE:
                    case CONSTANT_NAMESPACE_SET:
                    case CONSTANT_MULTINAME:
                        implicit = true;
                }
            }

            return (!title.isEmpty() ? title + ": " : "") + (implicit ? "[" : "") + type.getAbbreviation() + index + (implicit ? "]" : "") + ": " + description;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + Objects.hashCode(this.parent);
            hash = 31 * hash + this.currentLevelIndex;
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
            if (this.currentLevelIndex != other.currentLevelIndex) {
                return false;
            }
            return Objects.equals(this.parent, other.parent);
        }

        @Override
        public TreeIcon getIcon() {
            return type.getIcon();
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

        private ValueWithIndex createValueWithIndex(Object parent, int currentLevelIndex, int index, TreeType valueType, String title) {
            if (index == 0) {
                switch (valueType) {
                    case CONSTANT_INT:
                    case CONSTANT_UINT:
                    case CONSTANT_DOUBLE:
                    case CONSTANT_DECIMAL:
                    case CONSTANT_FLOAT:
                    case CONSTANT_FLOAT_4:
                    case CONSTANT_STRING:
                    case CONSTANT_NAMESPACE:
                    case CONSTANT_NAMESPACE_SET:
                    case CONSTANT_MULTINAME:
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "null", title);
                }
            }
            switch (valueType) {
                case CONSTANT_INT:
                    if (index >= abc.constants.getIntCount()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getInt(index), "" + abc.constants.getInt(index), title);
                case CONSTANT_UINT:
                    if (index >= abc.constants.getUIntCount()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getUInt(index), "" + abc.constants.getUInt(index), title);
                case CONSTANT_DOUBLE:
                    if (index >= abc.constants.getDoubleCount()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getDouble(index), EcmaScript.toString(abc.constants.getDouble(index)), title);
                case CONSTANT_DECIMAL:
                    if (index >= abc.constants.getDecimalCount()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getDecimal(index), "" + abc.constants.getDecimal(index), title);
                case CONSTANT_FLOAT:
                    if (index >= abc.constants.getFloatCount()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getFloat(index), EcmaScript.toString(abc.constants.getFloat(index)), title);
                case CONSTANT_FLOAT_4:
                    if (index >= abc.constants.getFloat4Count()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }

                    Float4 f4 = abc.constants.getFloat4(index);
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, f4,
                            EcmaScript.toString(f4.values[0]) + " "
                            + EcmaScript.toString(f4.values[1]) + " "
                            + EcmaScript.toString(f4.values[2]) + " "
                            + EcmaScript.toString(f4.values[3]),
                            title
                    );
                case CONSTANT_STRING:
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, index < abc.constants.getStringCount() ? abc.constants.getString(index) : null, formatString(index), title);
                case CONSTANT_NAMESPACE:
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, index < abc.constants.getNamespaceCount() ? abc.constants.getNamespace(index) : null, Multiname.namespaceToString(abc.constants, index), title);
                case CONSTANT_NAMESPACE_SET:
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, abc.constants.getNamespaceSet(index), Multiname.namespaceSetToString(abc.constants, index), title);
                case CONSTANT_MULTINAME:
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, index < abc.constants.getMultinameCount() ? abc.constants.getMultiname(index) : null,
                            index < abc.constants.getMultinameCount()
                            ? abc.constants.getMultiname(index).toString(abc.constants, new ArrayList<DottedChain>())
                            : "Unknown(" + index + ")",
                            title);
                case METHOD_INFO:
                    if (index >= abc.method_info.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    MethodInfo mi = abc.method_info.get(index);
                    StringBuilder miStrSb = new StringBuilder();
                    miStrSb.append("(");
                    StringBuilderTextWriter miParamStrSbW = new StringBuilderTextWriter(new CodeFormatting(), miStrSb);
                    mi.getParamStr(miParamStrSbW, abc.constants, null, abc, new ArrayList<>(), new LinkedHashSet<>());
                    miStrSb.append("): ");
                    String miReturnType = mi.getReturnTypeRaw(abc, abc.constants, new ArrayList<>());
                    miStrSb.append(miReturnType);
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, mi, miStrSb.toString(), title);
                case METHOD_BODY:
                    if (index >= abc.bodies.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    MethodBody b = abc.bodies.get(index);
                    String exceptionsAdd = "";
                    if (b.exceptions.length > 0) {
                        exceptionsAdd = ", " + b.exceptions.length + " exceptions";
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, b, "mi" + b.method_info + ", " + b.getCodeBytes().length + " bytes code" + exceptionsAdd, title);
                case INSTANCE_INFO:
                    if (index >= abc.instance_info.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    InstanceInfo ii = abc.instance_info.get(index);
                    String iiName;
                    if (ii.name_index >= abc.constants.getMultinameCount() || ii.getName(abc.constants).namespace_index >= abc.constants.getNamespaceCount()) {
                        iiName = "";
                    } else {
                        iiName = "\"" + Helper.escapePCodeString(ii.getName(abc.constants).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, false).toRawString()) + "\"";
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, TreeType.INSTANCE_INFO, ii, iiName + (ii.instance_traits.traits.isEmpty() ? "" : ", " + ii.instance_traits.traits.size() + " traits"), title);
                case CLASS_INFO:
                    if (index >= abc.class_info.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    ClassInfo ci = abc.class_info.get(index);
                    return new ValueWithIndex(parent, currentLevelIndex, index, TreeType.CLASS_INFO, ci, "mi" + ci.cinit_index + (ci.static_traits.traits.isEmpty() ? "" : ", " + ci.static_traits.traits.size() + " traits"), title);
                case SCRIPT_INFO:
                    if (index >= abc.script_info.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    ScriptInfo si = abc.script_info.get(index);
                    String siName = "";
                    try {
                        DottedChain simplePackName = si.getSimplePackName(abc, new LinkedHashSet<>());
                        if (simplePackName != null) {
                            siName = " (\"" + Helper.escapePCodeString(simplePackName.toRawString()) + "\")";
                        }
                    } catch (IndexOutOfBoundsException iob) {
                        //ignore
                    }
                    return new ValueWithIndex(parent, currentLevelIndex, index, TreeType.SCRIPT_INFO, si, "mi" + si.init_index + (si.traits.traits.isEmpty() ? "" : ", " + si.traits.traits.size() + " traits") + siName, title);

                case METADATA_INFO:
                    if (index >= abc.metadata_info.size()) {
                        return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "Unknown(" + index + ")", title);
                    }
                    MetadataInfo md = abc.metadata_info.get(index);
                    String mdName = formatString(md.name_index);
                    mdName += " (" + md.values.length + " items)";
                    return new ValueWithIndex(parent, currentLevelIndex, index, TreeType.METADATA_INFO, md, mdName);
                default:
                    return new ValueWithIndex(parent, currentLevelIndex, index, valueType, null, "", title);
            }
        }

        private int handleGetChildCountTrait(SubValue sv, Traits traits) {
            if (sv.getIndex() > -1) {
                Trait t = traits.traits.get(sv.getIndex());
                int count = 3;
                if ((t.kindFlags & Trait.ATTR_Metadata) > 0) {
                    count++;
                }
                if (t instanceof TraitSlotConst) {
                    TraitSlotConst tsc = (TraitSlotConst) t;
                    if (tsc.value_index == 0) {
                        return count + 3;
                    }
                    return count + 4;
                }
                if (t instanceof TraitMethodGetterSetter) {
                    return count + 2;
                }

                if (t instanceof TraitClass) {
                    return count + 3;
                }

                if (t instanceof TraitFunction) {
                    return count + 2;
                }
            }
            return traits.traits.size();
        }

        private Object handleGetChildTrait(Object parent, int index, Object parentValue, SubValue sv, Traits traits) {
            if (sv.getIndex() > -1) {
                Trait t = traits.traits.get(sv.getIndex());

                int currentIndex = 0;
                switch (index) {
                    case 0:
                        return createValueWithIndex(parent, index, t.name_index, TreeType.CONSTANT_MULTINAME, "name");
                    case 1:
                        return new SimpleValue(parent, index, "kind", String.format("0x%02X", t.kindType) + " (" + t.getKindToStr() + ")", TreeIcon.KIND);
                    case 2:
                        List<String> flagList = new ArrayList<>();
                        if ((t.kindFlags & Trait.ATTR_Final) > 0) {
                            flagList.add("FINAL");
                        }
                        if ((t.kindFlags & Trait.ATTR_Override) > 0) {
                            flagList.add("OVERRIDE");
                        }
                        if ((t.kindFlags & Trait.ATTR_Metadata) > 0) {
                            flagList.add("METADATA");
                        }
                        if ((t.kindFlags & Trait.ATTR_0x8) > 0) {
                            flagList.add("0x8");
                        }
                        return new SimpleValue(parent, index, "kind_flags", String.format("0x%02X", t.kindFlags) + (flagList.isEmpty() ? "" : " (" + String.join(", ", flagList) + ")"), TreeIcon.FLAGS);
                }
                if (t instanceof TraitSlotConst) {
                    TraitSlotConst tsc = (TraitSlotConst) t;
                    switch (index) {
                        case 3:
                            return new SimpleValue(parent, index, "slot_id", "" + tsc.slot_id, TreeIcon.SLOT_ID);
                        case 4:
                            return createValueWithIndex(parent, index, tsc.type_index, TreeType.CONSTANT_MULTINAME, "type");
                        case 5:
                            if (tsc.value_index == 0) {
                                return new SimpleValue(parent, index, "value_index", "null", TreeIcon.VALUE_INDEX);
                            }
                            switch (tsc.value_kind) {
                                case ValueKind.CONSTANT_Int:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_INT, "value_index");
                                case ValueKind.CONSTANT_UInt:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_UINT, "value_index");
                                case ValueKind.CONSTANT_Double:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_DOUBLE, "value_index");
                                case ValueKind.CONSTANT_DecimalOrFloat:
                                    if (abc.hasDecimalSupport()) {
                                        return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_DECIMAL, "value_index");
                                    }
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_FLOAT, "value_index");
                                case ValueKind.CONSTANT_Float4:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_FLOAT_4, "value_index");
                                case ValueKind.CONSTANT_Utf8:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_STRING, "value_index");
                                case ValueKind.CONSTANT_True:
                                case ValueKind.CONSTANT_False:
                                case ValueKind.CONSTANT_Null:
                                case ValueKind.CONSTANT_Undefined:
                                    return new SimpleValue(parent, index, "value_index", "" + tsc.value_index, TreeIcon.VALUE_INDEX);
                                case ValueKind.CONSTANT_Namespace:
                                case ValueKind.CONSTANT_PackageInternalNs:
                                case ValueKind.CONSTANT_ProtectedNamespace:
                                case ValueKind.CONSTANT_ExplicitNamespace:
                                case ValueKind.CONSTANT_StaticProtectedNs:
                                case ValueKind.CONSTANT_PrivateNs:
                                    return createValueWithIndex(parent, index, tsc.value_index, TreeType.CONSTANT_NAMESPACE, "value_index");
                            }
                        case 6:
                            switch (tsc.value_kind) {
                                case ValueKind.CONSTANT_Int:
                                    return new SimpleValue(parent, index, "value_kind", "Integer", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_UInt:
                                    return new SimpleValue(parent, index, "value_kind", "UInteger", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Double:
                                    return new SimpleValue(parent, index, "value_kind", "Double", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_DecimalOrFloat:
                                    if (abc.hasDecimalSupport()) {
                                        return new SimpleValue(parent, index, "value_kind", "Decimal", TreeIcon.VALUE_KIND);
                                    }
                                    return new SimpleValue(parent, index, "value_kind", "Float", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Float4:
                                    return new SimpleValue(parent, index, "value_kind", "Float4", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Utf8:
                                    return new SimpleValue(parent, index, "value_kind", "String", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_True:
                                    return new SimpleValue(parent, index, "value_kind", "True", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_False:
                                    return new SimpleValue(parent, index, "value_kind", "False", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Null:
                                    return new SimpleValue(parent, index, "value_kind", "Null", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Undefined:
                                    return new SimpleValue(parent, index, "value_kind", "Undefined", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_Namespace:
                                    return new SimpleValue(parent, index, "value_kind", "Namespace", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_PackageInternalNs:
                                    return new SimpleValue(parent, index, "value_kind", "PackageInternalNs", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_ProtectedNamespace:
                                    return new SimpleValue(parent, index, "value_kind", "ProtectedNamespace", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_ExplicitNamespace:
                                    return new SimpleValue(parent, index, "value_kind", "ExplicitNamespace", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_StaticProtectedNs:
                                    return new SimpleValue(parent, index, "value_kind", "StaticProtectedNs", TreeIcon.VALUE_KIND);
                                case ValueKind.CONSTANT_PrivateNs:
                                    return new SimpleValue(parent, index, "value_kind", "PrivateNamespace", TreeIcon.VALUE_KIND);
                            }
                    }
                    currentIndex = 7;
                }
                if (t instanceof TraitMethodGetterSetter) {
                    TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
                    switch (index) {
                        case 3:
                            return new SimpleValue(parent, index, "disp_id", "" + tmgs.disp_id, TreeIcon.DISP_ID);
                        case 4:
                            return createValueWithIndex(parent, index, tmgs.method_info, TreeType.METHOD_INFO, "method_info");
                    }
                    currentIndex = 5;
                }
                if (t instanceof TraitClass) {
                    TraitClass tc = (TraitClass) t;
                    switch (index) {
                        case 3:
                            return new SimpleValue(parent, index, "slot_id", "" + tc.slot_id, TreeIcon.SLOT_ID);
                        case 4:
                            return createValueWithIndex(parent, index, tc.class_info, TreeType.INSTANCE_INFO, "instance_info");
                        case 5:
                            return createValueWithIndex(parent, index, tc.class_info, TreeType.CLASS_INFO, "class_info");
                    }
                    currentIndex = 6;
                }

                if (t instanceof TraitFunction) {
                    TraitFunction tf = (TraitFunction) t;
                    switch (index) {
                        case 3:
                            return new SimpleValue(parent, index, "slot_id", "" + tf.slot_id, TreeIcon.SLOT_ID);
                        case 4:
                            return createValueWithIndex(parent, index, tf.method_info, TreeType.METHOD_INFO, "method_index");
                    }
                    currentIndex = 5;
                }

                if (index == currentIndex) {
                    if ((t.kindFlags & Trait.ATTR_Metadata) > 0) {
                        return new SubValue(parent, currentIndex, t, "metadata", "metadata", "", TreeIcon.TRAIT_METADATA);
                    }
                }
            }
            Trait t = traits.traits.get(index);
            String traitName = formatString(t.getName(abc).name_index);
            TreeIcon icon = TreeIcon.TRAITS_SUB;
            switch (t.kindType) {
                case Trait.TRAIT_CLASS:
                    icon = TreeIcon.TRAIT_CLASS;
                    break;
                case Trait.TRAIT_CONST:
                    icon = TreeIcon.TRAIT_CONST;
                    break;
                case Trait.TRAIT_FUNCTION:
                    icon = TreeIcon.TRAIT_FUNCTION;
                    break;
                case Trait.TRAIT_GETTER:
                    icon = TreeIcon.TRAIT_GETTER;
                    break;
                case Trait.TRAIT_METHOD:
                    icon = TreeIcon.TRAIT_METHOD;
                    break;
                case Trait.TRAIT_SETTER:
                    icon = TreeIcon.TRAIT_SETTER;
                    break;
                case Trait.TRAIT_SLOT:
                    icon = TreeIcon.TRAIT_SLOT;
                    break;
            }
            return new SubValue(parent, index, index, parentValue, "traits", "t" + index, t.getKindToStr() + ": " + traitName, icon);
        }

        private String formatString(int index) {
            if (index == 0) {
                return "null";
            }
            if (index >= abc.constants.getStringCount()) {
                return "Unknown(" + index + ")";
            }
            return "\"" + Helper.escapePCodeString(abc.constants.getString(index)) + "\"";
        }

        @Override
        public Object getChild(Object parent, int index) {
            try {
                if (parent == type) {
                    return createValueWithIndex(parent, index, index, type, "");
                }
                if (parent instanceof ValueWithIndex) {
                    ValueWithIndex vwi = (ValueWithIndex) parent;
                    if (vwi.value instanceof NamespaceSet) {
                        NamespaceSet nss = (NamespaceSet) vwi.value;
                        int ns = nss.namespaces[index];
                        return new ValueWithIndex(parent, index, ns, TreeType.CONSTANT_NAMESPACE, abc.constants.getNamespace(ns), Multiname.namespaceToString(abc.constants, ns));
                    }
                    if (vwi.value instanceof Namespace) {
                        Namespace ns = (Namespace) vwi.value;
                        switch (index) {
                            case 0:
                                return new SimpleValue(parent, index, "kind", Namespace.kindToStr(ns.kind), TreeIcon.KIND);
                            case 1:
                                return createValueWithIndex(parent, index, ns.name_index, TreeType.CONSTANT_STRING, "name");
                        }
                    }
                    if (vwi.value instanceof Multiname) {
                        Multiname m = (Multiname) vwi.value;
                        if (index == 0) {
                            return new SimpleValue(parent, index, "kind", m.getKindStr(), TreeIcon.KIND);
                        }
                        int kind = m.kind;
                        if ((kind == Multiname.QNAME) || (kind == Multiname.QNAMEA)) {
                            switch (index) {
                                case 1:
                                    return createValueWithIndex(parent, index, m.namespace_index, TreeType.CONSTANT_NAMESPACE, "namespace");
                                case 2:
                                    return createValueWithIndex(parent, index, m.name_index, TreeType.CONSTANT_STRING, "name");
                            }
                        } else if ((kind == Multiname.RTQNAME) || (kind == Multiname.RTQNAMEA)) {
                            if (index == 1) {
                                return createValueWithIndex(parent, index, m.name_index, TreeType.CONSTANT_STRING, "name");
                            }
                        } else if ((kind == Multiname.RTQNAMEL) || (kind == Multiname.RTQNAMELA)) {
                            //ignore
                        } else if ((kind == Multiname.MULTINAME) || (kind == Multiname.MULTINAMEA)) {
                            switch (index) {
                                case 1:
                                    return createValueWithIndex(parent, index, m.name_index, TreeType.CONSTANT_STRING, "name");
                                case 2:
                                    return createValueWithIndex(parent, index, m.namespace_set_index, TreeType.CONSTANT_NAMESPACE_SET, "namespace_set");
                            }
                        } else if ((kind == Multiname.MULTINAMEL) || (kind == Multiname.MULTINAMELA)) {
                            if (index == 1) {
                                return createValueWithIndex(parent, index, m.namespace_set_index, TreeType.CONSTANT_NAMESPACE_SET, "namespace_set");
                            }
                        } else if (kind == Multiname.TYPENAME) {
                            if (index == 1) {
                                return createValueWithIndex(parent, index, m.qname_index, TreeType.CONSTANT_MULTINAME, "qname");
                            }
                            if (index >= 2 && index - 2 < m.params.length) {
                                return createValueWithIndex(parent, index, m.params[index - 2], TreeType.CONSTANT_MULTINAME, "param" + (index - 2));
                            }
                        }
                    }
                    if (vwi.value instanceof MethodInfo) {
                        MethodInfo mi = (MethodInfo) vwi.value;
                        switch (index) {
                            case 0:
                                return new SubValue(parent, index, mi, "param_types", "param_types", "", TreeIcon.PARAM_TYPES);
                            case 1:
                                return createValueWithIndex(parent, index, mi.ret_type, TreeType.CONSTANT_MULTINAME, "return_type");
                            case 2:
                                return createValueWithIndex(parent, index, mi.name_index, TreeType.CONSTANT_STRING, "name");
                            case 3:
                                List<String> flagList = new ArrayList<>();
                                if (mi.flagNative()) {
                                    flagList.add("NATIVE");
                                }
                                if (mi.flagHas_optional()) {
                                    flagList.add("HAS_OPTIONAL");
                                }
                                if (mi.flagHas_paramnames()) {
                                    flagList.add("HAS_PARAM_NAMES");
                                }
                                if (mi.flagIgnore_rest()) {
                                    flagList.add("IGNORE_REST");
                                }
                                if (mi.flagNeed_activation()) {
                                    flagList.add("NEED_ACTIVATION");
                                }
                                if (mi.flagNeed_arguments()) {
                                    flagList.add("NEED_ARGUMENTS");
                                }
                                if (mi.flagNeed_rest()) {
                                    flagList.add("NEED_REST");
                                }
                                if (mi.flagSetsdxns()) {
                                    flagList.add("SET_DXNS");
                                }

                                return new SimpleValue(parent, index, "flags", String.format("0x%02X", mi.flags) + (!flagList.isEmpty() ? " (" + String.join(", ", flagList) + ")" : ""), TreeIcon.FLAGS);
                        }

                        int currentIndex = 4;

                        if (mi.flagHas_optional()) {
                            if (index == currentIndex) {
                                return new SubValue(parent, index, mi, "optional", "optional", "", TreeIcon.OPTIONAL);
                            }
                            currentIndex++;
                        }

                        if (mi.flagHas_paramnames()) {
                            if (index == currentIndex) {
                                return new SubValue(parent, index, mi, "param_names", "param_names", "", TreeIcon.PARAM_NAMES);
                            }
                            currentIndex++;
                        }

                        if (index == currentIndex) {
                            int bodyIndex = abc.findBodyIndex(vwi.getIndex());
                            if (bodyIndex != -1) {
                                return createValueWithIndex(parent, index, bodyIndex, TreeType.METHOD_BODY, "method_body");
                            }
                        }
                    }
                    if (vwi.value instanceof MethodBody) {
                        MethodBody body = (MethodBody) vwi.value;
                        switch (index) {
                            case 0:
                                return createValueWithIndex(parent, index, body.method_info, TreeType.METHOD_INFO, "method_info");
                            case 1:
                                return new SimpleValue(parent, index, "max_stack", "" + body.max_stack, TreeIcon.MAX_STACK);
                            case 2:
                                return new SimpleValue(parent, index, "max_regs", "" + body.max_regs, TreeIcon.MAX_REGS);
                            case 3:
                                return new SimpleValue(parent, index, "init_scope_depth", "" + body.init_scope_depth, TreeIcon.INIT_SCOPE_DEPTH);
                            case 4:
                                return new SimpleValue(parent, index, "max_scope_depth", "" + body.max_scope_depth, TreeIcon.MAX_SCOPE_DEPTH);
                            case 5:
                                return new SimpleValue(parent, index, "code", "" + body.getCodeBytes().length + " bytes", TreeIcon.CODE);
                            case 6:
                                return new SubValue(parent, index, body, "exceptions", "exceptions", "", TreeIcon.EXCEPTIONS);
                            case 7:
                                return new SubValue(parent, index, body, "traits", "traits", "", TreeIcon.TRAITS);
                        }
                    }
                    if (vwi.value instanceof InstanceInfo) {
                        InstanceInfo ii = (InstanceInfo) vwi.value;
                        switch (index) {
                            case 0:
                                return createValueWithIndex(parent, index, ii.name_index, TreeType.CONSTANT_MULTINAME, "name");
                            case 1:
                                return createValueWithIndex(parent, index, ii.super_index, TreeType.CONSTANT_MULTINAME, "super");
                            case 2:
                                List<String> flagList = new ArrayList<>();
                                if ((ii.flags & InstanceInfo.CLASS_SEALED) == InstanceInfo.CLASS_SEALED) {
                                    flagList.add("SEALED");
                                }
                                if ((ii.flags & InstanceInfo.CLASS_FINAL) == InstanceInfo.CLASS_FINAL) {
                                    flagList.add("FINAL");
                                }
                                if ((ii.flags & InstanceInfo.CLASS_INTERFACE) == InstanceInfo.CLASS_INTERFACE) {
                                    flagList.add("INTERFACE");
                                }
                                if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
                                    flagList.add("PROTECTEDNS");
                                }
                                if ((ii.flags & InstanceInfo.CLASS_NON_NULLABLE) == InstanceInfo.CLASS_NON_NULLABLE) {
                                    flagList.add("NON_NULLABLE");
                                }
                                return new SimpleValue(parent, index, "flags", String.format("0x%02X", ii.flags) + (!flagList.isEmpty() ? " (" + String.join(", ", flagList) + ")" : ""), TreeIcon.FLAGS);
                        }
                        int currentIndex = 3;
                        if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
                            if (index == currentIndex) {
                                return createValueWithIndex(parent, index, ii.protectedNS, TreeType.CONSTANT_NAMESPACE, "protected_ns");
                            }
                            currentIndex++;
                        }
                        if (index == currentIndex) {
                            return new SubValue(parent, index, ii, "interfaces", "interfaces", "", TreeIcon.INTERFACES);
                        }
                        currentIndex++;
                        if (index == currentIndex) {
                            return createValueWithIndex(parent, currentIndex, ii.iinit_index, TreeType.METHOD_INFO, "iinit");
                        }
                        currentIndex++;
                        if (index == currentIndex) {
                            return new SubValue(parent, index, ii, "traits", "traits", "", TreeIcon.TRAITS);
                        }
                    }
                    if (vwi.value instanceof ClassInfo) {
                        ClassInfo ci = (ClassInfo) vwi.value;
                        switch (index) {
                            case 0:
                                return createValueWithIndex(parent, index, ci.cinit_index, TreeType.METHOD_INFO, "cinit");
                            case 1:
                                return new SubValue(parent, index, ci, "traits", "traits", "", TreeIcon.TRAITS);
                        }
                    }
                    if (vwi.value instanceof ScriptInfo) {
                        ScriptInfo si = (ScriptInfo) vwi.value;
                        switch (index) {
                            case 0:
                                return createValueWithIndex(parent, index, si.init_index, TreeType.METHOD_INFO, "init");
                            case 1:
                                return new SubValue(parent, index, si, "traits", "traits", "", TreeIcon.TRAITS);
                        }
                    }

                    if (vwi.value instanceof MetadataInfo) {
                        MetadataInfo md = (MetadataInfo) vwi.value;
                        switch (index) {
                            case 0:
                                return createValueWithIndex(parent, index, md.name_index, TreeType.CONSTANT_STRING, "name");
                            case 1:
                                return new SubValue(parent, index, md, "pairs", "pairs", "", TreeIcon.METADATA_PAIRS);
                        }
                    }
                }
                if (parent instanceof SubValue) {
                    SubValue sv = (SubValue) parent;
                    if (sv.getParentValue() instanceof MethodInfo) {
                        MethodInfo mi = (MethodInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "param_types":
                                return createValueWithIndex(parent, index, mi.param_types[index], TreeType.CONSTANT_MULTINAME, "pt" + index);
                            case "optional":
                                if (sv.getIndex() > -1) {
                                    if (index == 0) {
                                        switch (mi.optional[sv.getIndex()].value_kind) {
                                            case ValueKind.CONSTANT_Int:
                                                return new SimpleValue(parent, index, "value_kind", "Integer", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_UInt:
                                                return new SimpleValue(parent, index, "value_kind", "UInteger", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Double:
                                                return new SimpleValue(parent, index, "value_kind", "Double", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_DecimalOrFloat:
                                                if (abc.hasDecimalSupport()) {
                                                    return new SimpleValue(parent, index, "value_kind", "Decimal", TreeIcon.VALUE_KIND);
                                                }
                                                return new SimpleValue(parent, index, "value_kind", "Float", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Float4:
                                                return new SimpleValue(parent, index, "value_kind", "Float4", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Utf8:
                                                return new SimpleValue(parent, index, "value_kind", "String", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_True:
                                                return new SimpleValue(parent, index, "value_kind", "True", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_False:
                                                return new SimpleValue(parent, index, "value_kind", "False", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Null:
                                                return new SimpleValue(parent, index, "value_kind", "Null", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Undefined:
                                                return new SimpleValue(parent, index, "value_kind", "Undefined", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_Namespace:
                                                return new SimpleValue(parent, index, "value_kind", "Namespace", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_PackageInternalNs:
                                                return new SimpleValue(parent, index, "value_kind", "PackageInternalNs", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_ProtectedNamespace:
                                                return new SimpleValue(parent, index, "value_kind", "ProtectedNamespace", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_ExplicitNamespace:
                                                return new SimpleValue(parent, index, "value_kind", "ExplicitNamespace", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_StaticProtectedNs:
                                                return new SimpleValue(parent, index, "value_kind", "StaticProtectedNs", TreeIcon.VALUE_KIND);
                                            case ValueKind.CONSTANT_PrivateNs:
                                                return new SimpleValue(parent, index, "value_kind", "PrivateNamespace", TreeIcon.VALUE_KIND);
                                        }
                                    }
                                    if (index == 1) {
                                        int value_index = mi.optional[sv.getIndex()].value_index;
                                        switch (mi.optional[sv.getIndex()].value_kind) {
                                            case ValueKind.CONSTANT_Int:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_INT, "value_index");
                                            case ValueKind.CONSTANT_UInt:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_UINT, "value_index");
                                            case ValueKind.CONSTANT_Double:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_DOUBLE, "value_index");
                                            case ValueKind.CONSTANT_DecimalOrFloat:
                                                if (abc.hasDecimalSupport()) {
                                                    return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_DECIMAL, "value_index");
                                                }
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_FLOAT, "value_index");
                                            case ValueKind.CONSTANT_Float4:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_FLOAT_4, "value_index");
                                            case ValueKind.CONSTANT_Utf8:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_STRING, "value_index");
                                            case ValueKind.CONSTANT_True:
                                                break;
                                            case ValueKind.CONSTANT_False:
                                                break;
                                            case ValueKind.CONSTANT_Null:
                                                break;
                                            case ValueKind.CONSTANT_Undefined:
                                                break;
                                            case ValueKind.CONSTANT_Namespace:
                                            case ValueKind.CONSTANT_PackageInternalNs:
                                            case ValueKind.CONSTANT_ProtectedNamespace:
                                            case ValueKind.CONSTANT_ExplicitNamespace:
                                            case ValueKind.CONSTANT_StaticProtectedNs:
                                            case ValueKind.CONSTANT_PrivateNs:
                                                return createValueWithIndex(parent, index, value_index, TreeType.CONSTANT_NAMESPACE, "value_index");
                                        }
                                    }
                                } else {
                                    return new SubValue(parent, index, index, mi, "optional", "op" + index, mi.optional[index].toASMString(abc), TreeIcon.OPTIONAL_SUB);
                                }
                                break;
                            case "param_names":
                                return createValueWithIndex(parent, index, mi.paramNames[index], TreeType.CONSTANT_STRING, "pn" + index);
                        }
                    }
                    if (sv.getParentValue() instanceof MethodBody) {
                        MethodBody body = (MethodBody) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "exceptions":
                                if (sv.getIndex() > -1) {
                                    ABCException ex = body.exceptions[sv.getIndex()];
                                    switch (index) {
                                        case 0:
                                            return new SimpleValue(parent, index, "start", "" + ex.start, TreeIcon.EXCEPTION_START);
                                        case 1:
                                            return new SimpleValue(parent, index, "end", "" + ex.end, TreeIcon.EXCEPTION_END);
                                        case 2:
                                            return new SimpleValue(parent, index, "target", "" + ex.target, TreeIcon.EXCEPTION_TARGET);
                                        case 3:
                                            return createValueWithIndex(parent, index, ex.name_index, TreeType.CONSTANT_MULTINAME, "name");
                                        case 4:
                                            return createValueWithIndex(parent, index, ex.type_index, TreeType.CONSTANT_MULTINAME, "type");
                                    }
                                } else {
                                    return new SubValue(parent, index, index, body, "exceptions", "ex" + index, "", TreeIcon.EXCEPTIONS_SUB);
                                }
                            case "traits":
                                return handleGetChildTrait(parent, index, body, sv, body.traits);
                        }
                    }
                    if (sv.getParentValue() instanceof InstanceInfo) {
                        InstanceInfo ii = (InstanceInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "interfaces":
                                return createValueWithIndex(parent, index, ii.interfaces[index], TreeType.CONSTANT_MULTINAME, "in" + index);
                            case "traits":
                                return handleGetChildTrait(parent, index, ii, sv, ii.instance_traits);
                        }
                    }
                    if (sv.getParentValue() instanceof ClassInfo) {
                        ClassInfo ci = (ClassInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "traits":
                                return handleGetChildTrait(parent, index, ci, sv, ci.static_traits);
                        }
                    }
                    if (sv.getParentValue() instanceof ScriptInfo) {
                        ScriptInfo ci = (ScriptInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "traits":
                                return handleGetChildTrait(parent, index, ci, sv, ci.traits);
                        }
                    }

                    if (sv.getParentValue() instanceof MetadataInfo) {
                        MetadataInfo md = (MetadataInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "pairs":
                                if (sv.getIndex() > -1) {
                                    switch (index) {
                                        case 0:
                                            return createValueWithIndex(parent, index, md.keys[sv.getIndex()], TreeType.CONSTANT_STRING, "key");
                                        case 1:
                                            return createValueWithIndex(parent, index, md.values[sv.getIndex()], TreeType.CONSTANT_STRING, "value");
                                    }
                                    return null;
                                }
                                String pairTitle = formatString(md.keys[index]) + " : " + formatString(md.values[index]);
                                return new SubValue(parent, index, index, md, "pairs", "p" + index, pairTitle, TreeIcon.METADATA_PAIRS_SUB);
                        }
                    }

                    if (sv.getParentValue() instanceof Trait) {
                        Trait t = (Trait) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "metadata":
                                return createValueWithIndex(parent, index, t.metadata[index], TreeType.METADATA_INFO, "");
                        }
                    }

                }
                return null;
            } catch (IndexOutOfBoundsException iex) {
                return "";
            }
        }

        @Override
        public int getChildCount(Object parent) {
            try {
                if (parent == type) {
                    switch (type) {
                        case CONSTANT_INT:
                            return Math.max(1, abc.constants.getIntCount());
                        case CONSTANT_UINT:
                            return Math.max(1, abc.constants.getUIntCount());
                        case CONSTANT_DOUBLE:
                            return Math.max(1, abc.constants.getDoubleCount());
                        case CONSTANT_DECIMAL:
                            return Math.max(1, abc.constants.getDecimalCount());
                        case CONSTANT_FLOAT:
                            return Math.max(1, abc.constants.getFloatCount());
                        case CONSTANT_FLOAT_4:
                            return Math.max(1, abc.constants.getFloat4Count());
                        case CONSTANT_STRING:
                            return Math.max(1, abc.constants.getStringCount());
                        case CONSTANT_NAMESPACE:
                            return Math.max(1, abc.constants.getNamespaceCount());
                        case CONSTANT_NAMESPACE_SET:
                            return abc.constants.getNamespaceSetCount();
                        case CONSTANT_MULTINAME:
                            return Math.max(1, abc.constants.getMultinameCount());
                        case METHOD_INFO:
                            return abc.method_info.size();
                        case METADATA_INFO:
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
                    ValueWithIndex vwi = (ValueWithIndex) parent;
                    if (vwi.value instanceof NamespaceSet) {
                        NamespaceSet nss = (NamespaceSet) vwi.value;
                        return nss.namespaces.length;
                    }
                    if (vwi.value instanceof Namespace) {
                        //kind, name
                        return 2;
                    }
                    if (vwi.value instanceof Multiname) {
                        Multiname m = (Multiname) vwi.value;
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
                    if (vwi.value instanceof MethodInfo) {
                        MethodInfo mi = (MethodInfo) vwi.value;

                        int count = 4;
                        if (mi.flagHas_optional()) {
                            count++;
                        }
                        if (mi.flagHas_paramnames()) {
                            count++;
                        }
                        int bodyIndex = abc.findBodyIndex(vwi.getIndex());
                        if (bodyIndex != -1) {
                            count++;
                        }

                        return count;
                    }
                    if (vwi.value instanceof MethodBody) {
                        return 8;
                    }
                    if (vwi.value instanceof InstanceInfo) {
                        InstanceInfo ii = (InstanceInfo) vwi.value;
                        if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
                            return 7;
                        }
                        return 6;
                    }

                    if (vwi.value instanceof ClassInfo) {
                        return 2;
                    }

                    if (vwi.value instanceof ScriptInfo) {
                        return 2;
                    }

                    if (vwi.value instanceof MetadataInfo) {
                        MetadataInfo md = (MetadataInfo) vwi.value;
                        return 2;
                    }
                }
                if (parent instanceof SubValue) {
                    SubValue sv = (SubValue) parent;
                    if (sv.getParentValue() instanceof MethodInfo) {
                        MethodInfo mi = (MethodInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "param_types":
                                return mi.param_types.length;
                            case "optional":
                                if (sv.getIndex() > -1) {
                                    int index = sv.getIndex();
                                    int value_index = mi.optional[index].value_index;
                                    switch (mi.optional[index].value_kind) {
                                        case ValueKind.CONSTANT_True:
                                        case ValueKind.CONSTANT_False:
                                        case ValueKind.CONSTANT_Null:
                                        case ValueKind.CONSTANT_Undefined:
                                            return 1;
                                        case ValueKind.CONSTANT_Int:
                                        case ValueKind.CONSTANT_UInt:
                                        case ValueKind.CONSTANT_Double:
                                        case ValueKind.CONSTANT_DecimalOrFloat:
                                        case ValueKind.CONSTANT_Float4:
                                        case ValueKind.CONSTANT_Utf8:
                                        case ValueKind.CONSTANT_Namespace:
                                        case ValueKind.CONSTANT_PackageInternalNs:
                                        case ValueKind.CONSTANT_ProtectedNamespace:
                                        case ValueKind.CONSTANT_ExplicitNamespace:
                                        case ValueKind.CONSTANT_StaticProtectedNs:
                                        case ValueKind.CONSTANT_PrivateNs:
                                            return 2;
                                    }
                                    return 0;
                                }
                                return mi.optional.length;
                            case "param_names":
                                return mi.paramNames.length;
                        }
                    }
                    if (sv.getParentValue() instanceof MethodBody) {
                        MethodBody body = (MethodBody) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "exceptions":
                                if (sv.getIndex() > -1) {
                                    return 5;
                                }
                                return body.exceptions.length;
                            case "traits":
                                return handleGetChildCountTrait(sv, body.traits);
                        }
                    }
                    if (sv.getParentValue() instanceof InstanceInfo) {
                        InstanceInfo ii = (InstanceInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "interfaces":
                                return ii.interfaces.length;
                            case "traits":
                                return handleGetChildCountTrait(sv, ii.instance_traits);
                        }
                    }
                    if (sv.getParentValue() instanceof ClassInfo) {
                        ClassInfo ci = (ClassInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "traits":
                                return handleGetChildCountTrait(sv, ci.static_traits);
                        }
                    }
                    if (sv.getParentValue() instanceof ScriptInfo) {
                        ScriptInfo ci = (ScriptInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "traits":
                                return handleGetChildCountTrait(sv, ci.traits);
                        }
                    }

                    if (sv.getParentValue() instanceof MetadataInfo) {
                        MetadataInfo md = (MetadataInfo) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "pairs":
                                if (sv.getIndex() > -1) {
                                    return 2;
                                }
                                return md.keys.length;
                        }
                    }

                    if (sv.getParentValue() instanceof Trait) {
                        Trait t = (Trait) sv.getParentValue();
                        switch (sv.getProperty()) {
                            case "metadata":
                                return t.metadata.length;
                        }
                    }
                }
                return 0;
            } catch (IndexOutOfBoundsException iex) {
                return 0;
            }
        }

        @Override
        public boolean isLeaf(Object node) {
            try {
                return getChildCount(node) == 0;
            } catch (IndexOutOfBoundsException iex) {
                return false;
            }
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (child instanceof ValueWithIndex) {
                ValueWithIndex vwi = (ValueWithIndex) child;
                if (vwi.getParent() == parent) {
                    return vwi.getCurrentLevelIndex();
                }
            }
            if (child instanceof SubValue) {
                SubValue sv = (SubValue) child;
                if (sv.parent == parent) {
                    return sv.getCurrentLevelIndex();
                }
            }
            if (child instanceof SimpleValue) {
                SimpleValue sv = (SimpleValue) child;
                if (sv.parent == parent) {
                    return sv.getCurrentLevelIndex();
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

        Map<String, ImageIcon> iconCache = new HashMap<>();
        boolean semiTransparent = false;
        private final ABCExplorerDialog dialog;

        public ExplorerTreeCellRenderer(ABCExplorerDialog dialog) {
            setUI(new BasicLabelUI());
            setOpaque(false);
            if (View.isOceanic()) {
                setBackgroundNonSelectionColor(Color.white);
            }
            this.dialog = dialog;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (semiTransparent) {
                if (getIcon() != null) {
                    Color color = getBackground();
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2));
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
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

            if (value instanceof HasIcon) {
                HasIcon hi = (HasIcon) value;
                String iconFile = hi.getIcon().getFile();
                if (!iconFile.isEmpty()) {
                    ImageIcon icon;
                    if (iconCache.containsKey(iconFile)) {
                        icon = iconCache.get(iconFile);
                    } else {
                        icon = View.getIcon(iconFile);
                        iconCache.put(iconFile, icon);
                    }
                    setIcon(icon);
                }
            } else {
                setIcon(null);
            }

            semiTransparent = false;
            if (value instanceof ValueWithIndex) {
                if (dialog.usageDetector != null) {
                    ValueWithIndex vwi = (ValueWithIndex) value;
                    if (vwi.getType().getUsageKind() != null) {
                        List<String> usages = dialog.usageDetector.getUsages(vwi.getType().getUsageKind(), vwi.getIndex());
                        semiTransparent = usages.isEmpty();
                    }
                }

            }
            return this;
        }
    }

    private void cleanActionPerformed(ActionEvent e) {
        ABC abc = getSelectedAbc();
        if (abc != null) {
            if (ViewMessages.showConfirmDialog(this, AppStrings.translate("warning.cleanAbc"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningAbcClean, JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
            int mainIndex = mainTabbedPane.getSelectedIndex();
            int cpIndex = cpTabbedPane.getSelectedIndex();
            ABCCleaner cleaner = new ABCCleaner();
            cleaner.clean(abc);
            if (cpIndex > -1) {
                cpTabbedPane.setSelectedIndex(cpIndex);
            }
            if (mainIndex > -1) {
                mainTabbedPane.setSelectedIndex(mainIndex);
            }
            Main.getMainFrame().getPanel().refreshTree();
        }
    }
}
