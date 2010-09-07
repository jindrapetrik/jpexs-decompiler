package com.jpexs.asdec.action.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.action.parser.ASMParser;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.gui.LoadingPanel;
import com.jpexs.asdec.gui.View;
import com.jpexs.asdec.helpers.Highlighting;
import com.jpexs.asdec.tags.ASMSource;
import com.jpexs.asdec.tags.Tag;
import jsyntaxpane.DefaultSyntaxKit;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class MainFrame extends JFrame implements TreeSelectionListener, ActionListener {

    public JTree tagTree;
    public JEditorPane editor;
    public JEditorPane decompiledEditor;
    public List<Tag> list;
    public JSplitPane splitPane;
    public JSplitPane splitPane2;
    public JButton saveButton = new JButton("Save");
    public JLabel asmLabel = new JLabel("P-code source (editable)");
    public JLabel decLabel = new JLabel("ActionScript source");
    public JPanel statusPanel = new JPanel();
    public LoadingPanel loadingPanel = new LoadingPanel(20, 20);
    public JLabel statusLabel = new JLabel("");

    public MainFrame(List<Tag> list) {
        this.list = list;
        DefaultSyntaxKit.initKit();
        editor = new JEditorPane();
        decompiledEditor = new JEditorPane();
        tagTree = new JTree(new TagTreeModel(list));

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        ClassLoader cldr = this.getClass().getClassLoader();
        java.net.URL imageURL = cldr.getResource("com/jpexs/asdec/action/gui/graphics/class.png");
        ImageIcon leafIcon = new ImageIcon(imageURL);
        treeRenderer.setLeafIcon(leafIcon);
        tagTree.setCellRenderer(treeRenderer);

        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        asmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        asmLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panB.add(asmLabel, BorderLayout.NORTH);
        panB.add(new JScrollPane(editor), BorderLayout.CENTER);

        JPanel buttonsPan = new JPanel();
        buttonsPan.setLayout(new FlowLayout());
        buttonsPan.add(saveButton);
        panB.add(buttonsPan, BorderLayout.SOUTH);

        saveButton.addActionListener(this);
        saveButton.setActionCommand("SAVEACTION");

        JPanel panA = new JPanel();
        panA.setLayout(new BorderLayout());
        panA.add(new JScrollPane(decompiledEditor), BorderLayout.CENTER);
        panA.add(decLabel, BorderLayout.NORTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));



        loadingPanel.setPreferredSize(new Dimension(30, 30));
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(1, 30));
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(loadingPanel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        loadingPanel.setVisible(false);


        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());
        cont.add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB)), BorderLayout.CENTER);
        cont.add(statusPanel, BorderLayout.SOUTH);

        editor.setContentType("text/flasm");
        decompiledEditor.setContentType("text/actionscript");
        setSize(640, 480);
        tagTree.addTreeSelectionListener(this);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.proxyFrame != null) {
                    if (Main.proxyFrame.isVisible()) {
                        return;
                    }
                }
                Main.exit();
            }
        });
        View.setWindowIcon(this);
        View.centerScreen(this);

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
        //menuFile.add(miExport);
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

        setTitle(Main.applicationName + " - " + Main.getFileTitle());
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (Main.isWorking()) return;
        Object obj = ((JTree) e.getSource()).getLastSelectedPathComponent();
        if (obj instanceof TagTreeItem) {
            obj = ((TagTreeItem) obj).tag;
            if (obj instanceof ASMSource) {
                Main.startWork("Decompiling...");
                final ASMSource asm = (ASMSource) obj;
                (new Thread() {

                    @Override
                    public void run() {
                        editor.setText(asm.getASMSource(10)); //TODO: Ensure correct version here
                        decompiledEditor.setText(Highlighting.stripHilights(com.jpexs.asdec.action.Action.actionsToSource(asm.getActions(), 10))); //TODO:Ensure correct version here
                        Main.stopWork();
                    }
                }).start();
            }
        }
    }

    public void display() {
        setVisible(true);
        splitPane.setDividerLocation(0.5);
        splitPane2.setDividerLocation(0.5);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("EXIT")) {
            System.exit(0);
        }
        if (e.getActionCommand().equals("SHOWPROXY")) {
            Main.showProxy();
        }
        if (Main.isWorking()) return;
        if (e.getActionCommand().equals("SAVEACTION")) {
            TagTreeItem ti = (TagTreeItem) tagTree.getLastSelectedPathComponent();
            if (ti.tag instanceof ASMSource) {
                ASMSource dat = (ASMSource) ti.tag;
                try {
                    dat.setActions(ASMParser.parse(new ByteArrayInputStream(editor.getText().getBytes()), 10)); //TODO:Ensure correct version here
                } catch (IOException ex) {
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "" + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (e.getActionCommand().equals("SAVE")) {
            try {
                Main.saveFile(Main.file);
            } catch (IOException ex) {
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
    }

    public void setStatus(String s) {
        if (s.equals("")) {
            loadingPanel.setVisible(false);
        } else {
            loadingPanel.setVisible(true);
        }
        statusLabel.setText(s);
    }
}
