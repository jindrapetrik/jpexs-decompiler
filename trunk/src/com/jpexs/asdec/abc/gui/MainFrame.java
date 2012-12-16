/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Configuration;
import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
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
import java.util.ArrayList;
import java.util.List;
import jsyntaxpane.actions.ComboCompletionAction;
import jsyntaxpane.syntaxkits.Flasm3SyntaxKit;


public class MainFrame extends JFrame implements ActionListener, ItemListener {

    
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
    public DetailPanel detailPanel;


    public void setStatus(String s) {
        if (s.equals("")) {
            loadingPanel.setVisible(false);
        } else {
            loadingPanel.setVisible(true);
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
        setSize(1024, 600);
        this.abc = list.get(listIndex).abc;
        getContentPane().setLayout(new BorderLayout());
        

        
        

       
        
        decompiledTextArea = new DecompiledEditorPane();

        decompiledScrollPane = new JScrollPane(decompiledTextArea);

        detailPanel=new DetailPanel();       
        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        panB.add(decompiledScrollPane, BorderLayout.CENTER);
        panB.add(decLabel, BorderLayout.NORTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                panB, detailPanel);
        decompiledTextArea.setContentType("text/actionscript");
        
        JPanel pan2 = new JPanel();
        pan2.setLayout(new BorderLayout());
        pan2.add((abcComboBox = new JComboBox(new ABCComboBoxModel(list))), BorderLayout.NORTH);

        navigator = new TraitsList();
        navigator.setABC(abc);

        
        JPanel navPanel=new JPanel(new BorderLayout());
        JLabel traitsLabel=new JLabel("Traits");
        navPanel.add(traitsLabel,BorderLayout.NORTH);

        traitsLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        navPanel.add(navigator,BorderLayout.CENTER);

        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(classTree = new ClassesListTree(abc)),
                navPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Classes", splitPane2);

        pan2.add(tabbedPane, BorderLayout.CENTER);
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
        miOpen.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/open16.png")));
        miOpen.setActionCommand("OPEN");
        miOpen.addActionListener(this);
        JMenuItem miSave = new JMenuItem("Save");
        miSave.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/save16.png")));
        miSave.setActionCommand("SAVE");
        miSave.addActionListener(this);
        JMenuItem miSaveAs = new JMenuItem("Save as...");
        miSaveAs.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/save16.png")));
        miSaveAs.setActionCommand("SAVEAS");
        miSaveAs.addActionListener(this);
        JMenuItem miExport = new JMenuItem("Export as ActionScript...");
        miExport.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportas16.png")));
        miExport.setActionCommand("EXPORT");
        miExport.addActionListener(this);

        JMenuItem miExportPCode = new JMenuItem("Export as PCode...");
        miExportPCode.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exportpc16.png")));
        miExportPCode.setActionCommand("EXPORTPCODE");
        miExportPCode.addActionListener(this);
        menuFile.add(miOpen);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(miExport);
        menuFile.add(miExportPCode);
        menuFile.addSeparator();
        JMenuItem miClose = new JMenuItem("Exit");
        miClose.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exit16.png")));
        miClose.setActionCommand("EXIT");
        miClose.addActionListener(this);
        menuFile.add(miClose);
        menuBar.add(menuFile);

        JMenu menuOptions = new JMenu("Options");
        JCheckBoxMenuItem  miSubLimiter = new JCheckBoxMenuItem ("Enable sub limiter");
        miSubLimiter.setActionCommand("SUBLIMITER");
        miSubLimiter.addActionListener(this);
        menuOptions.add(miSubLimiter);
        menuBar.add(menuOptions);
        
        JMenu menuTools = new JMenu("Tools");
        JMenuItem miProxy = new JMenuItem("Proxy");
        miProxy.setActionCommand("SHOWPROXY");
        miProxy.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy16.png")));
        miProxy.addActionListener(this);
        menuTools.add(miProxy);
        menuBar.add(menuTools);

        JMenu menuHelp = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About...");
        miAbout.setActionCommand("ABOUT");
        miAbout.addActionListener(this);
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);

        /* Constants */
        JPanel panConstants = new JPanel();
        panConstants.setLayout(new BorderLayout());
        constantTypeList = new JComboBox(new String[]{"UINT", "INT", "DOUBLE","DECIMAL", "STRING", "NAMESPACE", "NAMESPACESET", "MULTINAME"});
        constantTable = new JTable();
        autoResizeColWidth(constantTable, new UIntTableModel(abc));
        constantTable.setAutoCreateRowSorter(true);
        constantTable.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                   if(constantTypeList.getSelectedIndex()==7){ //MULTINAME
                       int rowIndex=constantTable.getSelectedRow();
                       if(rowIndex==-1) return;
                       int multinameIndex=constantTable.convertRowIndexToModel(rowIndex);
                       if(multinameIndex>0){
                           UsageFrame usageFrame=new UsageFrame(abc, multinameIndex);
                           usageFrame.setVisible(true);
                       }
                   }
                }
            }

        });
        constantTypeList.addItemListener(this);
        panConstants.add(constantTypeList, BorderLayout.NORTH);
        panConstants.add(new JScrollPane(constantTable), BorderLayout.CENTER);
        tabbedPane.addTab("Constants", panConstants);
        View.centerScreen(this);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("EXIT")) {
            setVisible(false);
            if (Main.proxyFrame != null) {
                if (Main.proxyFrame.isVisible()) return;
            }
            Main.exit();
        }
        if (Main.isWorking()) return;

        if (e.getActionCommand().equals("ABOUT")) {
            Main.about();
        }
       

        if (e.getActionCommand().equals("SHOWPROXY")) {
            Main.showProxy();
        }    
        
        if (e.getActionCommand().equals("SUBLIMITER")) {      
            if(e.getSource() instanceof JCheckBoxMenuItem){
                  Main.setSubLimiter(((JCheckBoxMenuItem)e.getSource()).getState());
            }
            
        } 
         
        if (e.getActionCommand().equals("SAVE")) {
            try {
                Main.saveFile(Main.file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (e.getActionCommand().equals("SAVEAS")) {
            if (Main.saveFileDialog()) {
                setTitle(Main.applicationName + " - " + Main.getFileTitle());
            }
        }
        if (e.getActionCommand().equals("OPEN")) {
            Main.openFileDialog();

        }

        if (e.getActionCommand().equals("EXPORT")||e.getActionCommand().equals("EXPORTPCODE")) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File((String)Configuration.getConfig("lastExportDir", ".")));
            chooser.setDialogTitle("Select directory to export");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Main.startWork("Exporting...");
                final String selFile = chooser.getSelectedFile().getAbsolutePath();
                Configuration.setConfig("lastExportDir", chooser.getSelectedFile().getParentFile().getAbsolutePath());
                final boolean isPcode=e.getActionCommand().equals("EXPORTPCODE");
                (new Thread() {

                    @Override
                    public void run() {
                        try {
                            for (DoABCTag tag : list) {
                                tag.abc.export(selFile,isPcode);
                            }
                        } catch (IOException ignored) {
                            JOptionPane.showMessageDialog(null, "Cannot write to the file");
                        }
                        Main.stopWork();
                    }
                }).start();

            }

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
