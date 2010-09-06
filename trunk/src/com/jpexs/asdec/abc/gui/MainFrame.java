/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.gui.tablemodels.*;
import com.jpexs.asdec.gui.LoadingPanel;
import com.jpexs.asdec.gui.View;
import com.jpexs.asdec.tags.DoABCTag;
import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;


public class MainFrame extends JFrame implements ActionListener, ItemListener {

    public ASMSourceEditorPane sourceTextArea;
    public TraitsList navigator;
    public ClassesListTree classTree;
    public ABC abc;
    public List<DoABCTag> list;
    public JComboBox abcComboBox;
    public int listIndex = 0;
    public DecompiledEditorPane decompiledTextArea;
    public JScrollPane decompiledScrollPane;
    public JSplitPane splitPane1;
    public JSplitPane splitPane2;
    public JSplitPane splitPane3;
    //private ConstantsListModel constantListModel;
    private JTable constantTable;
    //private JList constantsList;
    public JComboBox constantTypeList;
    public JPanel statusPanel = new JPanel();
    public LoadingPanel loadingPanel = new LoadingPanel(20, 20);
    public JLabel statusLabel = new JLabel("");
    public JLabel asmLabel = new JLabel("P-code source (editable)");
    public JLabel decLabel = new JLabel("ActionScript source");

    public void setStatus(String s) {
        if (s.equals("")) {
            //statusLabel.setOpaque(false);
            loadingPanel.setVisible(false);
        } else {
            loadingPanel.setVisible(true);
            //statusLabel.setForeground(Color.white);
            //statusLabel.setBackground(Color.red);
            //statusLabel.setOpaque(true);
        }
        statusLabel.setText(s);
    }


    public JTable autoResizeColWidth(JTable table, TableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);

        int margin = 5;

        for (int i = 0; i < table.getColumnCount(); i++) {
            int vColIndex = i;
            DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn col = colModel.getColumn(vColIndex);
            int width = 0;

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
                autoResizeColWidth(constantTable, new StringTableModel(abc));
                break;
            case 4:
                autoResizeColWidth(constantTable, new NamespaceTableModel(abc));
                break;
            case 5:
                autoResizeColWidth(constantTable, new NamespaceSetTableModel(abc));
                break;
            case 6:
                autoResizeColWidth(constantTable, new MultinameTableModel(abc));
                break;
        }
        //DefaultTableColumnModel colModel  = (DefaultTableColumnModel) constantTable.getColumnModel();
        //colModel.getColumn(0).setMaxWidth(50);
    }

    public void switchAbc(int index) {
        listIndex = index;
        this.abc = list.get(listIndex).abc;
        classTree.setABC(abc);
        decompiledTextArea.setABC(abc);
        navigator.setABC(abc);
        //constantTypeList = new JComboBox(new String[]{"UINT", "INT", "DOUBLE", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
        updateConstList();

    }


    public MainFrame(List<DoABCTag> list) {

        View.setWindowIcon(this);

        DefaultSyntaxKit.initKit();

        this.list = list;
        setSize(800, 600);
        this.abc = list.get(listIndex).abc;
        getContentPane().setLayout(new BorderLayout());
        sourceTextArea = new ASMSourceEditorPane();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
        sourceTextArea.setContentType("text/flasm3");
        JPanel buttonsPan = new JPanel();
        buttonsPan.setLayout(new FlowLayout());
        JButton verifyButton = new JButton("Verify");
        verifyButton.setActionCommand("VERIFYBODY");
        verifyButton.addActionListener(this);

        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("SAVEBODY");
        saveButton.addActionListener(this);


        buttonsPan.add(saveButton);
        rightPanel.add(buttonsPan, BorderLayout.SOUTH);
        decompiledTextArea = new DecompiledEditorPane();

        decompiledScrollPane = new JScrollPane(decompiledTextArea);

        JPanel panA = new JPanel();
        panA.setLayout(new BorderLayout());
        panA.add(rightPanel, BorderLayout.CENTER);
        panA.add(asmLabel, BorderLayout.NORTH);

        asmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        asmLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        panB.add(decompiledScrollPane, BorderLayout.CENTER);
        panB.add(decLabel, BorderLayout.NORTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panB, panA);
        decompiledTextArea.setContentType("text/actionscript");

        JPanel pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        pan2.add((abcComboBox = new JComboBox(new ABCComboBoxModel(list))), BorderLayout.NORTH);

        navigator = new TraitsList();
        navigator.setABC(abc);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Traits", new JScrollPane(navigator));
        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(classTree = new ClassesListTree(abc)),
                tabbedPane);

        pan2.add(splitPane2, BorderLayout.CENTER);
        abcComboBox.addItemListener(this);


        splitPane3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                pan2,
                splitPane1);

        pan2.setPreferredSize(new Dimension(300, 200));


        loadingPanel.setPreferredSize(new Dimension(30, 30));
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(1, 30));
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(loadingPanel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        loadingPanel.setVisible(false);


        getContentPane().add(splitPane3, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.proxyFrame != null) {
                    if (Main.proxyFrame.isVisible()) return;
                }
                Main.exit();
            }
        });
        setTitle(Main.applicationName + " - " + Main.getFileTitle());

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem miOpen = new JMenuItem("Open...");
        miOpen.setActionCommand("OPEN");
        miOpen.addActionListener(this);
        JMenuItem miSave = new JMenuItem("Save");
        miSave.setActionCommand("SAVE");
        miSave.addActionListener(this);
        JMenuItem miSaveAs = new JMenuItem("Save as...");
        miSaveAs.setActionCommand("SAVEAS");
        miSaveAs.addActionListener(this);
        JMenuItem miExport = new JMenuItem("Export...");
        miExport.setActionCommand("EXPORT");
        miExport.addActionListener(this);
        menuFile.add(miOpen);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(miExport);
        menuFile.addSeparator();
        JMenuItem miClose = new JMenuItem("Exit");
        miClose.setActionCommand("EXIT");
        miClose.addActionListener(this);
        menuFile.add(miClose);
        menuBar.add(menuFile);

        JMenu menuTools = new JMenu("Tools");
        JMenuItem miProxy = new JMenuItem("Proxy");
        miProxy.setActionCommand("SHOWPROXY");
        miProxy.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy16.png")));
        miProxy.addActionListener(this);
        menuTools.add(miProxy);
        menuBar.add(menuTools);

        setJMenuBar(menuBar);

        /* Constants */
        JPanel panConstants = new JPanel();
        panConstants.setLayout(new BorderLayout());
        constantTypeList = new JComboBox(new String[]{"UINT", "INT", "DOUBLE", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
        constantTable = new JTable();
        autoResizeColWidth(constantTable, new UIntTableModel(abc));
        constantTable.setAutoCreateRowSorter(true);
        constantTypeList.addItemListener(this);
        panConstants.add(constantTypeList, BorderLayout.NORTH);
        panConstants.add(new JScrollPane(constantTable), BorderLayout.CENTER);
        tabbedPane.addTab("Constants", panConstants);
        View.centerScreen(this);

    }

    public void actionPerformed(ActionEvent e) {
        if (Main.isWorking()) return;
        if (e.getActionCommand().equals("SHOWPROXY")) {
            Main.showProxy();
        }
        if (e.getActionCommand().equals("VERIFYBODY")) {
            sourceTextArea.verify(abc.constants, abc);
        }
        if (e.getActionCommand().equals("SAVEBODY")) {
            sourceTextArea.save(abc.constants);
        }
        if (e.getActionCommand().equals("SAVE")) {
            try {
                Main.saveFile(Main.file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (e.getActionCommand().equals("SAVEAS")) {
            if (Main.saveFileDialog(this)) {
                setTitle(Main.applicationName + " - " + Main.getFileTitle());
            }
        }
        if (e.getActionCommand().equals("OPEN")) {
            Main.openFileDialog();

        }

        if (e.getActionCommand().equals("EXPORT")) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Select directory to export");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Main.startWork("Exporting...");
                final String selFile = chooser.getSelectedFile().getAbsolutePath();
                (new Thread() {

                    @Override
                    public void run() {
                        try {
                            for (DoABCTag tag : list) {
                                tag.abc.export(selFile);
                            }
                        } catch (IOException ignored) {
                            JOptionPane.showMessageDialog(null, "Cannot write to the file");
                        }
                        Main.stopWork();
                    }
                }).start();

            }

        }
        if (e.getActionCommand().equals("EXIT")) {
            setVisible(false);
            if (Main.proxyFrame != null) {
                if (Main.proxyFrame.isVisible()) return;
            }
            Main.exit();
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == abcComboBox) {
            int index = ((JComboBox) e.getSource()).getSelectedIndex();
            if (index == -1) {
                return;
            }
            switchAbc(index);
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
        splitPane2.setDividerLocation(0.5);
        splitPane1.setDividerLocation(0.5); 
    }
}
