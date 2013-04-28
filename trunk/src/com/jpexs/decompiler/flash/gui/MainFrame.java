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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.gui.ABCPanel;
import com.jpexs.decompiler.flash.abc.gui.ClassesListTreeModel;
import com.jpexs.decompiler.flash.abc.gui.DeobfuscationDialog;
import com.jpexs.decompiler.flash.abc.gui.TreeElement;
import com.jpexs.decompiler.flash.abc.gui.TreeLeafScript;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.action.gui.ActionPanel;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Jindra
 */
public class MainFrame extends JFrame implements ActionListener, TreeSelectionListener {

    private SWF swf;
    public ABCPanel abcPanel;
    public ActionPanel actionPanel;
    private JTabbedPane tabPane;
    public LoadingPanel loadingPanel = new LoadingPanel(20, 20);
    public JLabel statusLabel = new JLabel("");
    public JPanel statusPanel = new JPanel();
    public JProgressBar progressBar = new JProgressBar(0, 100);
    private DeobfuscationDialog deobfuscationDialog;
    public JTree tagTree;
    public FlashPlayerPanel flashPanel;
    public JPanel displayPanel;
    public ImagePanel imagePanel;
    final static String CARDFLASHPANEL = "Flash card";
    final static String CARDIMAGEPANEL = "Image card";
    final static String CARDEMPTYPANEL = "Empty card";
    final static String CARDACTIONSCRIPTPANEL = "ActionScript card";
    final static String DETAILCARDAS3NAVIGATOR = "Traits list";
    final static String DETAILCARDEMPTYPANEL = "Empty card";
    final static String CARDTEXTPANEL = "Text card";
    private JTextField textValue = new JTextField("");
    private JPEGTablesTag jtt;
    private HashMap<Integer, CharacterTag> characters;
    private List<ABCContainerTag> abcList;
    JSplitPane splitPane1;
    JSplitPane splitPane2;
    private JPanel detailPanel;
    private JTextField filterField = new JTextField("");
    private JPanel searchPanel;
    private JCheckBoxMenuItem autoDeobfuscateMenuItem;
    private JPanel displayWithPreview;
    private JButton textSaveButton;
    private JButton textEditButton;
    private JButton textCancelButton;

    public void setPercent(int percent) {
        progressBar.setValue(percent);
        progressBar.setVisible(true);
    }

    public void hidePercent() {
        if (progressBar.isVisible()) {
            progressBar.setVisible(false);
        }
    }

    static {
        try {
            File.createTempFile("temp", ".swf").delete(); //First call to this is slow, so make it first
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void addTab(JTabbedPane tabbedPane, Component tab, String title, Icon icon) {
        tabbedPane.add(tab);

        JLabel lbl = new JLabel(title);
        lbl.setIcon(icon);
        lbl.setIconTextGap(5);
        lbl.setHorizontalTextPosition(SwingConstants.RIGHT);

        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, lbl);
    }

    public void setStatus(String s) {
        statusLabel.setText(s);
    }

    public void setWorkStatus(String s) {
        if (s.equals("")) {
            loadingPanel.setVisible(false);
        } else {
            loadingPanel.setVisible(true);
        }
        statusLabel.setText(s);
    }

    public MainFrame(SWF swf) {
        setSize(1000, 700);
        tabPane = new JTabbedPane();
        View.setWindowIcon(this);
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
        setTitle(Main.applicationVerName + (Main.DISPLAY_FILENAME ? " - " + Main.getFileTitle() : ""));
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem miOpen = new JMenuItem("Open...");
        miOpen.setIcon(View.getIcon("open16"));
        miOpen.setActionCommand("OPEN");
        miOpen.addActionListener(this);
        JMenuItem miSave = new JMenuItem("Save");
        miSave.setIcon(View.getIcon("save16"));
        miSave.setActionCommand("SAVE");
        miSave.addActionListener(this);
        JMenuItem miSaveAs = new JMenuItem("Save as...");
        miSaveAs.setIcon(View.getIcon("saveas16"));
        miSaveAs.setActionCommand("SAVEAS");
        miSaveAs.addActionListener(this);
        JMenuItem menuExportAll = new JMenuItem("Export all");
        menuExportAll.setActionCommand("EXPORT");
        menuExportAll.addActionListener(this);
        JMenuItem menuExportSel = new JMenuItem("Export selection");
        menuExportSel.setActionCommand("EXPORTSEL");
        menuExportSel.addActionListener(this);
        menuExportAll.setIcon(View.getIcon("export16"));
        menuExportSel.setIcon(View.getIcon("exportsel16"));



        menuFile.add(miOpen);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(menuExportAll);
        menuFile.add(menuExportSel);
        menuFile.addSeparator();
        JMenuItem miClose = new JMenuItem("Exit");
        miClose.setIcon(View.getIcon("exit16"));
        miClose.setActionCommand("EXIT");
        miClose.addActionListener(this);
        menuFile.add(miClose);
        menuBar.add(menuFile);
        JMenu menuDeobfuscation = new JMenu("Deobfuscation");
        menuDeobfuscation.setIcon(View.getIcon("deobfuscate16"));

        JMenuItem miDeobfuscation = new JMenuItem("PCode deobfuscation...");
        miDeobfuscation.setActionCommand("DEOBFUSCATE");
        miDeobfuscation.addActionListener(this);

        autoDeobfuscateMenuItem = new JCheckBoxMenuItem("Automatic deobfuscation");
        autoDeobfuscateMenuItem.setState((Boolean) Configuration.getConfig("autoDeobfuscate", true));
        autoDeobfuscateMenuItem.addActionListener(this);
        autoDeobfuscateMenuItem.setActionCommand("AUTODEOBFUSCATE");

        menuDeobfuscation.add(autoDeobfuscateMenuItem);
        JCheckBoxMenuItem miSubLimiter = new JCheckBoxMenuItem("Enable sub limiter");
        miSubLimiter.setActionCommand("SUBLIMITER");
        miSubLimiter.addActionListener(this);

        JMenuItem miRenameIdentifiers = new JMenuItem("Rename identifiers");
        miRenameIdentifiers.setActionCommand("RENAMEIDENTIFIERS");
        miRenameIdentifiers.addActionListener(this);

        JMenuItem miRemoveDeadCode = new JMenuItem("Remove dead code");
        miRemoveDeadCode.setActionCommand("REMOVEDEADCODE");
        miRemoveDeadCode.addActionListener(this);

        JMenuItem miRemoveDeadCodeAll = new JMenuItem("Remove all dead code");
        miRemoveDeadCodeAll.setActionCommand("REMOVEDEADCODEALL");
        miRemoveDeadCodeAll.addActionListener(this);

        JMenuItem miTraps = new JMenuItem("Remove traps");
        miTraps.setActionCommand("REMOVETRAPS");
        miTraps.addActionListener(this);

        JMenuItem miTrapsAll = new JMenuItem("Remove all traps");
        miTrapsAll.setActionCommand("REMOVETRAPSALL");
        miTrapsAll.addActionListener(this);

        JMenuItem miControlFlow = new JMenuItem("Restore control flow");
        miControlFlow.setActionCommand("RESTORECONTROLFLOW");
        miControlFlow.addActionListener(this);

        JMenuItem miControlFlowAll = new JMenuItem("Restore all control flow");
        miControlFlowAll.setActionCommand("RESTORECONTROLFLOWALL");
        miControlFlowAll.addActionListener(this);


        menuDeobfuscation.add(miRenameIdentifiers);
        //menuDeobfuscation.add(miSubLimiter);
        menuDeobfuscation.add(miDeobfuscation);
        /*menuDeobfuscation.add(miDeobfuscate);
         menuDeobfuscation.addSeparator();*/
        /*menuDeobfuscation.add(miRemoveDeadCode);
         menuDeobfuscation.add(miRemoveDeadCodeAll);
         menuDeobfuscation.add(miTraps);
         menuDeobfuscation.add(miTrapsAll);
         menuDeobfuscation.add(miControlFlow);
         menuDeobfuscation.add(miControlFlowAll);
         */
        JMenu menuTools = new JMenu("Tools");
        JMenuItem miProxy = new JMenuItem("Proxy");
        miProxy.setActionCommand("SHOWPROXY");
        miProxy.setIcon(View.getIcon("proxy16"));
        miProxy.addActionListener(this);
        menuTools.add(miProxy);

        //menuTools.add(menuDeobfuscation);
        menuTools.add(menuDeobfuscation);
        menuBar.add(menuTools);

        JMenu menuHelp = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About...");
        miAbout.setIcon(View.getIcon("about16"));

        miAbout.setActionCommand("ABOUT");
        miAbout.addActionListener(this);

        JMenuItem miCheckUpdates = new JMenuItem("Check for updates...");
        miCheckUpdates.setActionCommand("CHECKUPDATES");
        miCheckUpdates.setIcon(View.getIcon("update16"));
        miCheckUpdates.addActionListener(this);

        JMenuItem miHelpUs = new JMenuItem("Help us!");
        miHelpUs.setActionCommand("HELPUS");
        miHelpUs.setIcon(View.getIcon("donate16"));
        miHelpUs.addActionListener(this);

        JMenuItem miHomepage = new JMenuItem("Visit homepage");
        miHomepage.setActionCommand("HOMEPAGE");
        miHomepage.setIcon(View.getIcon("homepage16"));
        miHomepage.addActionListener(this);


        menuHelp.add(miCheckUpdates);
        menuHelp.add(miHelpUs);
        menuHelp.add(miHomepage);
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
        List<Object> objs = new ArrayList<Object>();
        objs.addAll(swf.tags);


        this.swf = swf;
        java.awt.Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());


        detailPanel = new JPanel();
        detailPanel.setLayout(new CardLayout());
        JPanel whitePanel = new JPanel();
        whitePanel.setBackground(Color.white);
        detailPanel.add(whitePanel, DETAILCARDEMPTYPANEL);
        CardLayout cl2 = (CardLayout) (detailPanel.getLayout());
        cl2.show(detailPanel, DETAILCARDEMPTYPANEL);


        abcList = new ArrayList<ABCContainerTag>();
        getActionScript3(objs, abcList);
        if (!abcList.isEmpty()) {
            addTab(tabPane, abcPanel = new ABCPanel(abcList), "ActionScript3", View.getIcon("as16"));
            detailPanel.add(abcPanel.tabbedPane, DETAILCARDAS3NAVIGATOR);
        } else {
            actionPanel = new ActionPanel();
            addTab(tabPane, actionPanel, "ActionScript", View.getIcon("as16"));

            miDeobfuscation.setEnabled(false);
        }


        tagTree = new JTree(new TagTreeModel(createTagList(objs, null), (new File(Main.file)).getName()));
        tagTree.addTreeSelectionListener(this);
        final JPopupMenu spritePopupMenu = new JPopupMenu();
        JMenuItem removeMenuItem = new JMenuItem("Remove");
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand("REMOVEITEM");
        spritePopupMenu.add(removeMenuItem);
        tagTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = tagTree.getClosestRowForLocation(e.getX(), e.getY());
                    tagTree.setSelectionRow(row);
                    Object tagObj = tagTree.getLastSelectedPathComponent();
                    if (tagObj == null) {
                        return;
                    }

                    if (tagObj instanceof TagNode) {
                        tagObj = ((TagNode) tagObj).tag;
                    }
                    if (tagObj instanceof DefineSpriteTag) {
                        spritePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        TreeCellRenderer tcr = new DefaultTreeCellRenderer() {
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
                Object val = value;
                if (val instanceof TagNode) {
                    val = ((TagNode) val).tag;
                }
                String type = getTagType(val);
                if (row == 0) {
                    setIcon(View.getIcon("flash16"));
                } else if (type != null) {
                    if (type.equals("folder") && expanded) {
                        type = "folderopen";
                    }
                    setIcon(View.getIcon(type + "16"));
                    //setToolTipText("This book is in the Tutorial series.");
                } else {
                    //setToolTipText(null); //no tool tip
                }
                return this;
            }
        };

        tagTree.setCellRenderer(tcr);
        loadingPanel.setPreferredSize(new Dimension(30, 30));
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(1, 30));
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(loadingPanel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        loadingPanel.setVisible(false);
        cnt.add(statusPanel, BorderLayout.SOUTH);

        for (Tag t : swf.tags) {
            if (t instanceof JPEGTablesTag) {
                jtt = (JPEGTablesTag) t;
            }
        }
        characters = new HashMap<Integer, CharacterTag>();
        List<Object> list2 = new ArrayList<Object>();
        list2.addAll(swf.tags);
        parseCharacters(list2);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textValue, BorderLayout.CENTER);
        textValue.setEditable(false);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));


        textSaveButton = new JButton("Save", View.getIcon("save16"));
        textSaveButton.setMargin(new Insets(3, 3, 3, 10));
        textSaveButton.setActionCommand("SAVETEXT");
        textSaveButton.addActionListener(this);

        textEditButton = new JButton("Edit", View.getIcon("edit16"));
        textEditButton.setMargin(new Insets(3, 3, 3, 10));
        textEditButton.setActionCommand("EDITTEXT");
        textEditButton.addActionListener(this);

        textCancelButton = new JButton("Cancel", View.getIcon("cancel16"));
        textCancelButton.setMargin(new Insets(3, 3, 3, 10));
        textCancelButton.setActionCommand("CANCELTEXT");
        textCancelButton.addActionListener(this);

        buttonsPanel.add(textEditButton);
        buttonsPanel.add(textSaveButton);
        buttonsPanel.add(textCancelButton);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        textPanel.add(buttonsPanel, BorderLayout.EAST);

        displayWithPreview = new JPanel(new CardLayout());
        displayWithPreview.add(textPanel, CARDTEXTPANEL);
        displayWithPreview.setVisible(false);

        JPanel panWithPreview = new JPanel(new BorderLayout());
        panWithPreview.add(displayWithPreview, BorderLayout.SOUTH);

        try {
            flashPanel = new FlashPlayerPanel(this);
        } catch (FlashUnsupportedException fue) {
        }
        displayPanel = new JPanel(new CardLayout());
        displayPanel.add(panWithPreview, CARDFLASHPANEL);
        if (flashPanel != null) {
            panWithPreview.add(flashPanel, BorderLayout.CENTER);
        } else {
            JPanel swtPanel = new JPanel(new BorderLayout());
            swtPanel.add(new JLabel("<html><center>Preview of this object is not available on this platform. (Windows only)</center></html>", JLabel.CENTER), BorderLayout.CENTER);
            swtPanel.setBackground(Color.white);
            panWithPreview.add(swtPanel, BorderLayout.CENTER);
        }
        imagePanel = new ImagePanel();
        displayPanel.add(imagePanel, CARDIMAGEPANEL);
        displayPanel.add(new JPanel(), CARDEMPTYPANEL);
        if (actionPanel != null) {
            displayPanel.add(actionPanel, CARDACTIONSCRIPTPANEL);
        }
        if (abcPanel != null) {
            displayPanel.add(abcPanel, CARDACTIONSCRIPTPANEL);
        }
        CardLayout cl = (CardLayout) (displayPanel.getLayout());
        cl.show(displayPanel, CARDEMPTYPANEL);

        searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(filterField, BorderLayout.CENTER);
        searchPanel.add(new JLabel(View.getIcon("search16")), BorderLayout.WEST);
        JLabel closeSearchButton = new JLabel(View.getIcon("cancel16"));
        closeSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterField.setText("");
                doFilter();
                searchPanel.setVisible(false);
            }
        });
        searchPanel.add(closeSearchButton, BorderLayout.EAST);
        JPanel pan1 = new JPanel(new BorderLayout());
        pan1.add(new JScrollPane(tagTree), BorderLayout.CENTER);
        pan1.add(searchPanel, BorderLayout.SOUTH);

        filterField.setActionCommand("FILTERSCRIPT");
        filterField.addActionListener(this);

        searchPanel.setVisible(false);

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

        //displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pan1, detailPanel);
        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane2, displayPanel);
        cnt.add(splitPane1, BorderLayout.CENTER);
        splitPane1.setDividerLocation(0.5);
        View.centerScreen(this);
        tagTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == 'F') && (e.isControlDown())) {
                    searchPanel.setVisible(true);
                    filterField.requestFocusInWindow();
                }
            }
        });
        detailPanel.setVisible(false);

    }

    public void doFilter() {
        TagNode n = getASTagNode(tagTree);
        if (n != null) {
            if (n.tag instanceof ClassesListTreeModel) {
                n.tag = new ClassesListTreeModel(abcPanel.classTree.treeList, filterField.getText());
            }
            tagTree.updateUI();
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            if (abcPanel != null) {
                abcPanel.initSplits();
            }
            if (actionPanel != null) {
                actionPanel.initSplits();
            }
            splitPane1.setDividerLocation(getWidth() / 3);
            splitPane2.setDividerLocation(splitPane2.getHeight() * 3 / 5);
            splitPos = splitPane2.getDividerLocation();
        }
    }

    private void parseCharacters(List<Object> list) {
        for (Object t : list) {
            if (t instanceof CharacterTag) {
                characters.put(((CharacterTag) t).getCharacterID(), (CharacterTag) t);
            }
            if (t instanceof Container) {
                parseCharacters(((Container) t).getSubItems());
            }
        }
    }

    public static void getShapes(List<Object> list, List<Tag> shapes) {
        for (Object t : list) {
            if (t instanceof Container) {
                getShapes(((Container) t).getSubItems(), shapes);
            }
            if ((t instanceof DefineShapeTag)
                    || (t instanceof DefineShape2Tag)
                    || (t instanceof DefineShape3Tag)
                    || (t instanceof DefineShape4Tag)) {
                shapes.add((Tag) t);
            }
        }
    }

    public static void getFonts(List<Object> list, List<Tag> fonts) {
        for (Object t : list) {
            if (t instanceof Container) {
                getFonts(((Container) t).getSubItems(), fonts);
            }
            if ((t instanceof DefineFontTag)
                    || (t instanceof DefineFont2Tag)
                    || (t instanceof DefineFont3Tag)
                    || (t instanceof DefineFont4Tag)) {
                fonts.add((Tag) t);
            }
        }
    }

    public static void getActionScript3(List<Object> list, List<ABCContainerTag> actionScripts) {
        for (Object t : list) {
            if (t instanceof Container) {
                getActionScript3(((Container) t).getSubItems(), actionScripts);
            }
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    public static void getMorphShapes(List<Object> list, List<Tag> morphShapes) {
        for (Object t : list) {
            if (t instanceof Container) {
                getMorphShapes(((Container) t).getSubItems(), morphShapes);
            }
            if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
                morphShapes.add((Tag) t);
            }
        }
    }

    public static void getImages(List<Object> list, List<Tag> images) {
        for (Object t : list) {
            if (t instanceof Container) {
                getImages(((Container) t).getSubItems(), images);
            }
            if ((t instanceof DefineBitsTag)
                    || (t instanceof DefineBitsJPEG2Tag)
                    || (t instanceof DefineBitsJPEG3Tag)
                    || (t instanceof DefineBitsJPEG4Tag)
                    || (t instanceof DefineBitsLosslessTag)
                    || (t instanceof DefineBitsLossless2Tag)) {
                images.add((Tag) t);
            }
        }
    }

    public static void getTexts(List<Object> list, List<Tag> texts) {
        for (Object t : list) {
            if (t instanceof Container) {
                getTexts(((Container) t).getSubItems(), texts);
            }
            if ((t instanceof DefineTextTag)
                    || (t instanceof DefineText2Tag)
                    || (t instanceof DefineEditTextTag)) {
                texts.add((Tag) t);
            }
        }
    }

    public static void getSprites(List<Object> list, List<Tag> sprites) {
        for (Object t : list) {
            if (t instanceof Container) {
                getSprites(((Container) t).getSubItems(), sprites);
            }
            if (t instanceof DefineSpriteTag) {
                sprites.add((Tag) t);
            }
        }
    }

    public static void getButtons(List<Object> list, List<Tag> buttons) {
        for (Object t : list) {
            if (t instanceof Container) {
                getButtons(((Container) t).getSubItems(), buttons);
            }
            if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
                buttons.add((Tag) t);
            }
        }
    }

    public List<TagNode> getSelectedNodes() {
        List<TagNode> ret = new ArrayList<TagNode>();
        TreePath tps[] = tagTree.getSelectionPaths();
        if (tps == null) {
            return ret;
        }
        for (TreePath tp : tps) {
            TagNode te = (TagNode) tp.getLastPathComponent();
            ret.add(te);
        }
        return ret;
    }

    public String getTagType(Object t) {
        if ((t instanceof DefineFontTag)
                || (t instanceof DefineFont2Tag)
                || (t instanceof DefineFont3Tag)
                || (t instanceof DefineFont4Tag)) {
            return "font";
        }
        if ((t instanceof DefineTextTag)
                || (t instanceof DefineText2Tag)
                || (t instanceof DefineEditTextTag)) {
            return "text";
        }

        if ((t instanceof DefineBitsTag)
                || (t instanceof DefineBitsJPEG2Tag)
                || (t instanceof DefineBitsJPEG3Tag)
                || (t instanceof DefineBitsJPEG4Tag)
                || (t instanceof DefineBitsLosslessTag)
                || (t instanceof DefineBitsLossless2Tag)) {
            return "image";
        }
        if ((t instanceof DefineShapeTag)
                || (t instanceof DefineShape2Tag)
                || (t instanceof DefineShape3Tag)
                || (t instanceof DefineShape4Tag)) {
            return "shape";
        }

        if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
            return "morphshape";
        }

        if (t instanceof DefineSpriteTag) {
            return "sprite";
        }
        if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
            return "button";
        }
        if (t instanceof ASMSource) {
            return "as";
        }
        if (t instanceof TreeElement) {
            TreeElement te = (TreeElement) t;
            if (te.getItem() instanceof TreeLeafScript) {
                return "as";
            } else {
                return "package";
            }
        }
        if (t instanceof FrameNode) {
            return "frame";
        }
        if (t instanceof ShowFrameTag) {
            return "showframe";
        }

        if (t instanceof DefineVideoStreamTag) {
            return "movie";
        }

        if ((t instanceof DefineSoundTag) || (t instanceof SoundStreamHeadTag) || (t instanceof SoundStreamHead2Tag)) {
            return "sound";
        }

        if (t instanceof DefineBinaryDataTag) {
            return "binaryData";
        }

        return "folder";
    }

    public List<Object> getTagsWithType(List<Object> list, String type) {
        List<Object> ret = new ArrayList<Object>();
        for (Object o : list) {
            String ttype = getTagType(o);
            if (type.equals(ttype)) {
                ret.add(o);
            }
        }
        return ret;
    }

    public List<TagNode> getTagNodesWithType(List<Object> list, String type, Object parent, boolean display) {
        List<TagNode> ret = new ArrayList<TagNode>();
        int frameCnt = 0;
        for (Object o : list) {
            String ttype = getTagType(o);
            if ("showframe".equals(ttype) && "frame".equals(type)) {
                frameCnt++;
                ret.add(new TagNode(new FrameNode(frameCnt, parent, display)));
            } else if (type.equals(ttype)) {
                ret.add(new TagNode(o));
            }
        }
        return ret;
    }

    public List<Object> getAllSubs(JTree tree, Object o) {
        TreeModel tm = tree.getModel();
        List<Object> ret = new ArrayList<Object>();
        for (int i = 0; i < tm.getChildCount(o); i++) {
            Object c = tm.getChild(o, i);
            ret.add(c);
            ret.addAll(getAllSubs(tree, c));
        }
        return ret;
    }

    public List<Object> getAllSelected(JTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath tps[] = tsm.getSelectionPaths();
        List<Object> ret = new ArrayList<Object>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            Object o = tp.getLastPathComponent();
            ret.add(o);
            ret.addAll(getAllSubs(tree, o));
        }
        return ret;
    }

    public TagNode getASTagNode(JTree tree) {
        TreeModel tm = tree.getModel();
        Object root = tm.getRoot();
        for (int i = 0; i < tm.getChildCount(root); i++) {
            Object node = tm.getChild(root, i);
            if (node instanceof TagNode) {
                Object tag = ((TagNode) tm.getChild(root, i)).tag;
                if (tag != null) {
                    if (tag.toString().equals("scripts")) {
                        return (TagNode) node;
                    }
                }
            }
        }
        return null;
    }

    public List<TagNode> createTagList(List<Object> list, Object parent) {
        List<TagNode> ret = new ArrayList<TagNode>();
        List<TagNode> frames = getTagNodesWithType(list, "frame", parent, true);
        List<TagNode> shapes = getTagNodesWithType(list, "shape", parent, true);
        List<TagNode> morphShapes = getTagNodesWithType(list, "morphshape", parent, true);
        List<TagNode> sprites = getTagNodesWithType(list, "sprite", parent, true);
        List<TagNode> buttons = getTagNodesWithType(list, "button", parent, true);
        List<TagNode> images = getTagNodesWithType(list, "image", parent, true);
        List<TagNode> fonts = getTagNodesWithType(list, "font", parent, true);
        List<TagNode> texts = getTagNodesWithType(list, "text", parent, true);
        List<TagNode> movies = getTagNodesWithType(list, "movie", parent, true);
        List<TagNode> sounds = getTagNodesWithType(list, "sound", parent, true);
        List<TagNode> binaryData = getTagNodesWithType(list, "binaryData", parent, true);
        List<TagNode> actionScript = new ArrayList<TagNode>();

        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i).tag instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = new ArrayList<SoundStreamBlockTag>();
                SWF.populateSoundStreamBlocks(list, (Tag) sounds.get(i).tag, blocks);
                if (blocks.isEmpty()) {
                    sounds.remove(i);
                    i--;
                }
            }
        }

        for (TagNode n : sprites) {
            n.subItems = getTagNodesWithType(new ArrayList<Object>(((DefineSpriteTag) n.tag).subTags), "frame", n.tag, true);
        }

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
        for (Object t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            /*if (t instanceof ASMSource) {
             TagNode tti = new TagNode(t);
             ret.add(tti);
             } else */
            if (t instanceof Container) {
                TagNode tti = new TagNode(t);
                if (((Container) t).getItemCount() > 0) {
                    List<Object> subItems = ((Container) t).getSubItems();
                    tti.subItems = createTagList(subItems, parent);
                }
                //ret.add(tti);
            }
        }

        actionScript = SWF.createASTagList(list, null);
        TagNode textsNode = new TagNode("texts");
        textsNode.subItems.addAll(texts);

        TagNode imagesNode = new TagNode("images");
        imagesNode.subItems.addAll(images);

        TagNode moviesNode = new TagNode("movies");
        moviesNode.subItems.addAll(movies);

        TagNode soundsNode = new TagNode("sounds");
        soundsNode.subItems.addAll(sounds);


        TagNode binaryDataNode = new TagNode("binaryData");
        binaryDataNode.subItems.addAll(binaryData);

        TagNode fontsNode = new TagNode("fonts");
        fontsNode.subItems.addAll(fonts);


        TagNode spritesNode = new TagNode("sprites");
        spritesNode.subItems.addAll(sprites);

        TagNode shapesNode = new TagNode("shapes");
        shapesNode.subItems.addAll(shapes);

        TagNode morphShapesNode = new TagNode("morphshapes");
        morphShapesNode.subItems.addAll(morphShapes);

        TagNode buttonsNode = new TagNode("buttons");
        buttonsNode.subItems.addAll(buttons);

        TagNode framesNode = new TagNode("frames");
        framesNode.subItems.addAll(frames);

        TagNode actionScriptNode = new TagNode("scripts");
        actionScriptNode.subItems.addAll(actionScript);

        if (!shapesNode.subItems.isEmpty()) {
            ret.add(shapesNode);
        }
        if (!morphShapesNode.subItems.isEmpty()) {
            ret.add(morphShapesNode);
        }
        if (!spritesNode.subItems.isEmpty()) {
            ret.add(spritesNode);
        }
        if (!textsNode.subItems.isEmpty()) {
            ret.add(textsNode);
        }
        if (!imagesNode.subItems.isEmpty()) {
            ret.add(imagesNode);
        }
        if (!moviesNode.subItems.isEmpty()) {
            ret.add(moviesNode);
        }
        if (!soundsNode.subItems.isEmpty()) {
            ret.add(soundsNode);
        }
        if (!buttonsNode.subItems.isEmpty()) {
            ret.add(buttonsNode);
        }
        if (!fontsNode.subItems.isEmpty()) {
            ret.add(fontsNode);
        }
        if (!binaryDataNode.subItems.isEmpty()) {
            ret.add(binaryDataNode);
        }
        if (!framesNode.subItems.isEmpty()) {
            ret.add(framesNode);
        }




        if (abcPanel != null) {
            actionScriptNode.subItems.clear();
            actionScriptNode.tag = abcPanel.classTree.getModel();
        }
        if ((!actionScriptNode.subItems.isEmpty()) || (abcPanel != null)) {
            ret.add(actionScriptNode);
        }

        return ret;
    }

    public boolean confirmExperimental() {
        return JOptionPane.showConfirmDialog(null, "Following procedure can damage SWF file which can be then unplayable.\r\nUSE IT ON YOUR OWN RISK. Do you want to continue?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("REMOVEITEM")) {
            Object tagObj = tagTree.getLastSelectedPathComponent();
            if (tagObj == null) {
                return;
            }

            if (tagObj instanceof TagNode) {
                tagObj = ((TagNode) tagObj).tag;
            }
            if (tagObj instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tagObj;
                for (int i = 0; i < swf.tags.size(); i++) {
                    Tag t = swf.tags.get(i);
                    if (t == sprite) {
                        swf.tags.remove(i);
                        i--;
                    } else if (t instanceof DefineSpriteTag) {
                        DefineSpriteTag st = (DefineSpriteTag) t;
                        for (int j = 0; j < st.subTags.size(); j++) {
                            Tag t2 = st.subTags.get(j);
                            Set<Integer> needed = t2.getNeededCharacters();
                            if (needed.contains(sprite.spriteId)) {
                                st.subTags.remove(j);
                                j--;
                            }
                        }
                    } else {
                        Set<Integer> needed = t.getNeededCharacters();
                        if (needed.contains(sprite.spriteId)) {
                            swf.tags.remove(i);
                            i--;
                        }
                    }
                }
                showCard(CARDEMPTYPANEL);
                refreshTree();
            }
        }
        if (e.getActionCommand().equals("EDITTEXT")) {
            setEditText(true);
        }
        if (e.getActionCommand().equals("CANCELTEXT")) {
            setEditText(false);
        }
        if (e.getActionCommand().equals("SAVETEXT")) {
            if (oldValue instanceof TextTag) {
                try {
                    ((TextTag) oldValue).setFormattedText(swf.tags, textValue.getText());
                    setEditText(false);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid text: " + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        if (e.getActionCommand().equals("AUTODEOBFUSCATE")) {
            if (JOptionPane.showConfirmDialog(this, "Automatic deobfuscation is a way to decompile obfuscated code.\r\nDeobfuscation leads to slower decompilation and some of the dead code may be eliminated.\r\nIf the code is not obfuscated, it's better to turn autodeobfuscation off.\r\nDo you really want to " + (autoDeobfuscateMenuItem.getState() ? "turn ON" : "turn OFF") + " automatic debfuscation?", "Confirm", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Configuration.setConfig("autoDeobfuscate", autoDeobfuscateMenuItem.getState());
            } else {
                autoDeobfuscateMenuItem.setState(!autoDeobfuscateMenuItem.getState());
            }
        }
        if (e.getActionCommand().equals("EXIT")) {
            setVisible(false);
            if (Main.proxyFrame != null) {
                if (Main.proxyFrame.isVisible()) {
                    return;
                }
            }
            Main.exit();
        }
        if (Main.isWorking()) {
            return;
        }

        if (e.getActionCommand().equals("ABOUT")) {
            Main.about();
        }


        if (e.getActionCommand().equals("SHOWPROXY")) {
            Main.showProxy();
        }

        if (e.getActionCommand().equals("SUBLIMITER")) {
            if (e.getSource() instanceof JCheckBoxMenuItem) {
                Main.setSubLimiter(((JCheckBoxMenuItem) e.getSource()).getState());
            }

        }

        if (e.getActionCommand().equals("SAVE")) {
            try {
                Main.saveFile(Main.file);
            } catch (IOException ex) {
                Logger.getLogger(com.jpexs.decompiler.flash.abc.gui.ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (e.getActionCommand().equals("SAVEAS")) {
            if (Main.saveFileDialog()) {
                setTitle(Main.applicationVerName + (Main.DISPLAY_FILENAME ? " - " + Main.getFileTitle() : ""));
            }
        }
        if (e.getActionCommand().equals("OPEN")) {
            Main.openFileDialog();

        }

        if (e.getActionCommand().startsWith("EXPORT")) {
            final ExportDialog export = new ExportDialog();
            export.setVisible(true);
            if (!export.cancelled) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File((String) Configuration.getConfig("lastExportDir", ".")));
                chooser.setDialogTitle("Select directory to export");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    final long timeBefore = System.currentTimeMillis();
                    Main.startWork("Exporting...");
                    final String selFile = chooser.getSelectedFile().getAbsolutePath();
                    Configuration.setConfig("lastExportDir", chooser.getSelectedFile().getParentFile().getAbsolutePath());
                    final boolean isPcode = export.getOption(ExportDialog.OPTION_ACTIONSCRIPT) == 1;
                    final boolean isMp3 = export.getOption(ExportDialog.OPTION_SOUNDS) == 0;
                    final boolean isFormatted = export.getOption(ExportDialog.OPTION_TEXTS) == 1;
                    final boolean onlySel = e.getActionCommand().endsWith("SEL");
                    (new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (onlySel) {
                                    List<Object> sel = getAllSelected(tagTree);

                                    List<TreeLeafScript> tlsList = new ArrayList<TreeLeafScript>();
                                    JPEGTablesTag jtt = null;
                                    for (Tag t : swf.tags) {
                                        if (t instanceof JPEGTablesTag) {
                                            jtt = (JPEGTablesTag) t;
                                            break;
                                        }
                                    }
                                    List<Tag> images = new ArrayList<Tag>();
                                    List<Tag> shapes = new ArrayList<Tag>();
                                    List<Tag> movies = new ArrayList<Tag>();
                                    List<Tag> sounds = new ArrayList<Tag>();
                                    List<Tag> texts = new ArrayList<Tag>();
                                    List<TagNode> actionNodes = new ArrayList<TagNode>();
                                    List<Tag> binaryData = new ArrayList<Tag>();
                                    for (Object d : sel) {
                                        if (d instanceof TagNode) {
                                            TagNode n = (TagNode) d;
                                            if ("image".equals(getTagType(n.tag))) {
                                                images.add((Tag) n.tag);
                                            }
                                            if ("shape".equals(getTagType(n.tag))) {
                                                shapes.add((Tag) n.tag);
                                            }
                                            if ("as".equals(getTagType(n.tag))) {
                                                actionNodes.add(n);
                                            }
                                            if ("movie".equals(getTagType(n.tag))) {
                                                movies.add((Tag) n.tag);
                                            }
                                            if ("sound".equals(getTagType(n.tag))) {
                                                sounds.add((Tag) n.tag);
                                            }
                                            if ("binaryData".equals(getTagType(n.tag))) {
                                                binaryData.add((Tag) n.tag);
                                            }
                                            if ("text".equals(getTagType(n.tag))) {
                                                texts.add((Tag) n.tag);
                                            }
                                        }
                                        if (d instanceof TreeElement) {
                                            if (((TreeElement) d).isLeaf()) {
                                                tlsList.add((TreeLeafScript) ((TreeElement) d).getItem());
                                            }
                                        }
                                    }
                                    SWF.exportImages(selFile + File.separator + "images", images, jtt);
                                    SWF.exportShapes(selFile + File.separator + "shapes", shapes);
                                    swf.exportTexts(selFile + File.separator + "texts", texts, isFormatted);
                                    swf.exportMovies(selFile + File.separator + "movies", movies);
                                    swf.exportSounds(selFile + File.separator + "sounds", sounds, isMp3);
                                    swf.exportBinaryData(selFile + File.separator + "binaryData", binaryData);
                                    if (abcPanel != null) {
                                        for (int i = 0; i < tlsList.size(); i++) {
                                            TreeLeafScript tls = tlsList.get(i);
                                            Main.startWork("Exporting " + (i + 1) + "/" + tlsList.size() + " " + tls.abc.script_info[tls.scriptIndex].getPath(tls.abc) + " ...");
                                            tls.abc.script_info[tls.scriptIndex].export(tls.abc, abcPanel.list, selFile, isPcode, tls.scriptIndex);
                                        }
                                    } else {
                                        List<TagNode> allNodes = new ArrayList<TagNode>();
                                        TagNode asn = getASTagNode(tagTree);
                                        if (asn != null) {
                                            allNodes.add(asn);
                                            TagNode.setExport(allNodes, false);
                                            TagNode.setExport(actionNodes, true);
                                            TagNode.exportNodeAS(allNodes, selFile, isPcode);
                                        }
                                    }
                                } else {
                                    swf.exportImages(selFile + File.separator + "images");
                                    swf.exportShapes(selFile + File.separator + "shapes");
                                    swf.exportTexts(selFile + File.separator + "texts", isFormatted);
                                    swf.exportMovies(selFile + File.separator + "movies");
                                    swf.exportSounds(selFile + File.separator + "sounds", isMp3);
                                    swf.exportBinaryData(selFile + File.separator + "binaryData");
                                    swf.exportActionScript(selFile, isPcode);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Error during export", ex);
                                JOptionPane.showMessageDialog(null, "Error during export");
                            }
                            Main.stopWork();
                            long timeAfter = System.currentTimeMillis();
                            long timeMs = timeAfter - timeBefore;
                            long timeS = timeMs / 1000;
                            timeMs = timeMs % 1000;
                            long timeM = timeS / 60;
                            timeS = timeS % 60;
                            long timeH = timeM / 60;
                            timeM = timeM % 60;
                            String timeStr = "";
                            if (timeH > 0) {
                                timeStr += Helper.padZeros(timeH, 2) + ":";
                            }
                            timeStr += Helper.padZeros(timeM, 2) + ":";
                            timeStr += Helper.padZeros(timeS, 2) + "." + Helper.padZeros(timeMs, 3);
                            setStatus("Exported in " + timeStr);
                        }
                    }).start();

                }
            }
        }

        if (e.getActionCommand().equals("CHECKUPDATES")) {
            if (!Main.checkForUpdates()) {
                JOptionPane.showMessageDialog(null, "No new version available.");
            }
        }

        if (e.getActionCommand().equals("HELPUS")) {
            String helpUsURL = Main.projectPage + "/help_us.html";
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                try {
                    java.net.URI uri = new java.net.URI(helpUsURL);
                    desktop.browse(uri);
                } catch (Exception ex) {
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please visit\r\n" + helpUsURL + "\r\nfor details.");
            }
        }

        if (e.getActionCommand().equals("HOMEPAGE")) {
            String homePageURL = Main.projectPage;
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                try {
                    java.net.URI uri = new java.net.URI(homePageURL);
                    desktop.browse(uri);
                } catch (Exception ex) {
                }
            } else {
                JOptionPane.showMessageDialog(null, "Visit homepage at: \r\n" + homePageURL);
            }
        }

        if (e.getActionCommand().startsWith("RESTORECONTROLFLOW")) {
            Main.startWork("Restoring control flow...");
            final boolean all = e.getActionCommand().endsWith("ALL");
            if ((!all) || confirmExperimental()) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        int cnt = 0;
                        if (all) {
                            for (ABCContainerTag tag : abcPanel.list) {
                                tag.getABC().restoreControlFlow();
                            }
                        } else {
                            int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                            if (bi != -1) {
                                abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                        }
                        Main.stopWork();
                        JOptionPane.showMessageDialog(null, "Control flow restored");
                        abcPanel.reload();
                        return true;
                    }
                }.execute();
            }
        }

        if (e.getActionCommand().startsWith("REMOVETRAPS")) {
            Main.startWork("Removing traps...");
            final boolean all = e.getActionCommand().endsWith("ALL");
            if ((!all) || confirmExperimental()) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        int cnt = 0;
                        if (all) {
                            for (ABCContainerTag tag : abcPanel.list) {
                                cnt += tag.getABC().removeTraps();
                            }
                        } else {
                            int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                            if (bi != -1) {
                                cnt += abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc);
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                        }
                        Main.stopWork();
                        JOptionPane.showMessageDialog(null, "Traps removed: " + cnt);
                        abcPanel.reload();
                        return true;
                    }
                }.execute();
            }
        }

        if (e.getActionCommand().startsWith("REMOVEDEADCODE")) {
            Main.startWork("Removing dead code...");
            final boolean all = e.getActionCommand().endsWith("ALL");
            if ((!all) || confirmExperimental()) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        int cnt = 0;
                        if (all) {
                            for (ABCContainerTag tag : abcPanel.list) {
                                cnt += tag.getABC().removeDeadCode();
                            }
                        } else {
                            int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                            if (bi != -1) {
                                cnt += abcPanel.abc.bodies[bi].removeDeadCode(abcPanel.abc.constants);
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                        }
                        Main.stopWork();
                        JOptionPane.showMessageDialog(null, "Instructions removed: " + cnt);
                        abcPanel.reload();
                        return true;
                    }
                }.execute();
            }
        }

        if (e.getActionCommand().equals("RENAMEIDENTIFIERS")) {
            if (confirmExperimental()) {

                Main.startWork("Renaming identifiers...");
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        int cnt = 0;

                        if (abcPanel != null) {
                            HashMap<String, String> namesMap = new HashMap<String, String>();
                            for (ABCContainerTag tag : abcPanel.list) {
                                cnt += tag.getABC().deobfuscateIdentifiers(namesMap);
                            }
                        } else {
                            cnt = swf.deobfuscateAS2Identifiers();
                        }
                        Main.stopWork();
                        JOptionPane.showMessageDialog(null, "Identifiers renamed: " + cnt);
                        if (abcPanel != null) {
                            abcPanel.reload();
                        }
                        doFilter();
                        reload(true);
                        return true;
                    }
                }.execute();


            }
        }

        if (e.getActionCommand().startsWith("DEOBFUSCATE")) {
            if (deobfuscationDialog == null) {
                deobfuscationDialog = new DeobfuscationDialog();
            }
            deobfuscationDialog.setVisible(true);
            if (deobfuscationDialog.ok) {
                Main.startWork("Deobfuscating...");
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        if (deobfuscationDialog.processAllCheckbox.isSelected()) {
                            for (ABCContainerTag tag : abcPanel.list) {
                                if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_DEAD_CODE) {
                                    tag.getABC().removeDeadCode();
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_TRAPS) {
                                    tag.getABC().removeTraps();
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                                    tag.getABC().removeTraps();
                                    tag.getABC().restoreControlFlow();
                                }
                            }
                        } else {
                            int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                            if (bi != -1) {
                                if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_DEAD_CODE) {
                                    abcPanel.abc.bodies[bi].removeDeadCode(abcPanel.abc.constants);
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_TRAPS) {
                                    abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc);
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                                    abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc);
                                    abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                                }
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                        }
                        Main.stopWork();
                        JOptionPane.showMessageDialog(null, "Deobfuscation complete");
                        abcPanel.reload();
                        return true;
                    }
                }.execute();
            }
        }
    }
    private int splitPos = 0;

    public void showDetailWithPreview(String card) {
        CardLayout cl = (CardLayout) (displayWithPreview.getLayout());
        cl.show(displayWithPreview, card);
    }

    public void showDetail(String card) {
        CardLayout cl = (CardLayout) (detailPanel.getLayout());
        cl.show(detailPanel, card);
        if (card.equals(DETAILCARDEMPTYPANEL)) {
            if (detailPanel.isVisible()) {
                splitPos = splitPane2.getDividerLocation();
                detailPanel.setVisible(false);
            }
        } else {
            if (!detailPanel.isVisible()) {
                detailPanel.setVisible(true);
                splitPane2.setDividerLocation(splitPos);
            }
        }

    }

    public void showCard(String card) {
        CardLayout cl = (CardLayout) (displayPanel.getLayout());
        cl.show(displayPanel, card);
    }
    private Object oldValue;
    private File tempFile;

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        setEditText(false);
        reload(false);
    }

    public void reload(boolean forceReload) {
        Object tagObj = tagTree.getLastSelectedPathComponent();
        if (tagObj == null) {
            return;
        }

        if (tagObj instanceof TagNode) {
            tagObj = ((TagNode) tagObj).tag;
        }
        if (tagObj instanceof TreeElement) {
            tagObj = ((TreeElement) tagObj).getItem();
        }

        if (!forceReload && (tagObj == oldValue)) {
            return;
        }
        oldValue = tagObj;
        if (tagObj instanceof TreeLeafScript) {
            final TreeLeafScript scriptLeaf = (TreeLeafScript) tagObj;
            if (!Main.isWorking()) {
                Main.startWork("Decompiling...");
                (new Thread() {
                    @Override
                    public void run() {
                        int classIndex = -1;
                        for (Trait t : scriptLeaf.abc.script_info[scriptLeaf.scriptIndex].traits.traits) {
                            if (t instanceof TraitClass) {
                                classIndex = ((TraitClass) t).class_info;
                                break;
                            }
                        }
                        abcPanel.navigator.setABC(abcList, scriptLeaf.abc);
                        abcPanel.navigator.setClassIndex(classIndex, scriptLeaf.scriptIndex);
                        abcPanel.setAbc(scriptLeaf.abc);
                        abcPanel.decompiledTextArea.setScript(scriptLeaf.scriptIndex, scriptLeaf.abc, abcList);
                        abcPanel.decompiledTextArea.setClassIndex(classIndex);
                        abcPanel.decompiledTextArea.setNoTrait();
                        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setCode("");
                        Main.stopWork();
                    }
                }).start();
            }
            showDetail(DETAILCARDAS3NAVIGATOR);
            showCard(CARDACTIONSCRIPTPANEL);
            return;
        } else {
            showDetail(DETAILCARDEMPTYPANEL);
        }
        if (tagObj instanceof DefineVideoStreamTag) {
            showCard(CARDEMPTYPANEL);
        } else if ((tagObj instanceof DefineSoundTag) || (tagObj instanceof SoundStreamHeadTag) || (tagObj instanceof SoundStreamHead2Tag)) {
            showCard(CARDEMPTYPANEL);
        } else if (tagObj instanceof DefineBinaryDataTag) {
            showCard(CARDEMPTYPANEL);
        } else if (tagObj instanceof ASMSource) {
            showCard(CARDACTIONSCRIPTPANEL);
            actionPanel.setSource((ASMSource) tagObj);
        } else if (tagObj instanceof DefineBitsTag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsTag) tagObj).getFullImageData(jtt));
        } else if (tagObj instanceof DefineBitsJPEG2Tag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsJPEG2Tag) tagObj).imageData);
        } else if (tagObj instanceof DefineBitsJPEG3Tag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsJPEG3Tag) tagObj).imageData);
        } else if (tagObj instanceof DefineBitsJPEG4Tag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsJPEG4Tag) tagObj).imageData);
        } else if (tagObj instanceof DefineBitsLosslessTag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsLosslessTag) tagObj).getImage());
        } else if (tagObj instanceof DefineBitsLossless2Tag) {
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((DefineBitsLossless2Tag) tagObj).getImage());
        } else if ((tagObj instanceof FrameNode && ((FrameNode) tagObj).isDisplayed()) || (((tagObj instanceof CharacterTag) || (tagObj instanceof FontTag)) && (tagObj instanceof Tag))) {
            try {

                if (tempFile != null) {
                    tempFile.delete();
                }
                tempFile = File.createTempFile("temp", ".swf");
                tempFile.deleteOnExit();

                FileOutputStream fos = new FileOutputStream(tempFile);
                SWFOutputStream sos = new SWFOutputStream(fos, 10);
                sos.write("FWS".getBytes());
                sos.write(13);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos2 = new SWFOutputStream(baos, 10);
                int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
                int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
                sos2.writeRECT(swf.displayRect);
                sos2.writeUI8(0);
                sos2.writeUI8(swf.frameRate);
                sos2.writeUI16(100); //framecnt
                sos2.writeTag(new SetBackgroundColorTag(new RGB(255, 255, 255)));

                if (tagObj instanceof FrameNode) {
                    FrameNode fn = (FrameNode) tagObj;
                    Object parent = fn.getParent();
                    List<Object> subs = new ArrayList<Object>();
                    if (parent == null) {
                        subs.addAll(swf.tags);
                    } else {
                        if (parent instanceof Container) {
                            subs = ((Container) parent).getSubItems();
                        }
                    }
                    List<Integer> doneCharacters = new ArrayList<Integer>();
                    int frameCnt = 1;
                    for (Object o : subs) {
                        if (o instanceof ShowFrameTag) {
                            frameCnt++;
                            continue;
                        }
                        if (frameCnt > fn.getFrame()) {
                            break;
                        }
                        Tag t = (Tag) o;
                        Set<Integer> needed = t.getDeepNeededCharacters(characters);
                        for (int n : needed) {
                            if (!doneCharacters.contains(n)) {
                                sos2.writeTag(characters.get(n));
                                doneCharacters.add(n);
                            }
                        }
                        if (t instanceof CharacterTag) {
                            doneCharacters.add(((CharacterTag) t).getCharacterID());
                        }
                        sos2.writeTag(t);

                        if (parent != null) {
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag pot = (PlaceObjectTypeTag) t;
                                int chid = pot.getCharacterId();
                                int depth = pot.getDepth();
                                MATRIX mat = pot.getMatrix();
                                if (mat == null) {
                                    mat = new MATRIX();
                                }
                                mat = (MATRIX) Helper.deepCopy(mat);
                                if (parent instanceof BoundedTag) {
                                    RECT r = ((BoundedTag) parent).getRect(characters);
                                    mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                                    mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
                                } else {
                                    mat.translateX = mat.translateX + width / 2;
                                    mat.translateY = mat.translateY + height / 2;
                                }
                                sos2.writeTag(new PlaceObject2Tag(false, false, false, false, false, true, false, true, depth, chid, mat, null, 0, null, 0, null));

                            }
                        }
                    }
                    sos2.writeTag(new ShowFrameTag());
                } else {

                    if (tagObj instanceof DefineBitsTag) {
                        if (jtt != null) {
                            sos2.writeTag(jtt);
                        }
                    } else if (tagObj instanceof AloneTag) {
                    } else {
                        Set<Integer> needed = ((Tag) tagObj).getDeepNeededCharacters(characters);
                        for (int n : needed) {
                            sos2.writeTag(characters.get(n));
                        }
                    }

                    sos2.writeTag(((Tag) tagObj));

                    int chtId = 0;
                    if (tagObj instanceof CharacterTag) {
                        chtId = ((CharacterTag) tagObj).getCharacterID();
                    }

                    MATRIX mat = new MATRIX();
                    mat.hasRotate = false;
                    mat.hasScale = false;
                    mat.translateX = 0;
                    mat.translateY = 0;
                    if (tagObj instanceof BoundedTag) {
                        RECT r = ((BoundedTag) tagObj).getRect(characters);
                        mat.translateX = -r.Xmin;
                        mat.translateY = -r.Ymin;
                        mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                        mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
                    } else {
                        mat.translateX = width / 4;
                        mat.translateY = height / 4;
                    }
                    if (tagObj instanceof FontTag) {

                        int countGlyphs = ((FontTag) tagObj).getGlyphShapeTable().length;
                        int fontId = ((FontTag) tagObj).getFontId();
                        int sloupcu = (int) Math.ceil(Math.sqrt(countGlyphs));
                        int radku = (int) Math.ceil(((float) countGlyphs) / ((float) sloupcu));
                        int x = 0;
                        int y = 1;
                        for (int f = 0; f < countGlyphs; f++) {
                            if (x >= sloupcu) {
                                x = 0;
                                y++;
                            }
                            List<TEXTRECORD> rec = new ArrayList<TEXTRECORD>();
                            TEXTRECORD tr = new TEXTRECORD();
                            int textHeight = height / radku;
                            tr.fontId = fontId;
                            tr.styleFlagsHasFont = true;
                            tr.textHeight = textHeight;
                            tr.glyphEntries = new GLYPHENTRY[1];
                            tr.styleFlagsHasColor = true;
                            tr.textColor = new RGB(0, 0, 0);
                            tr.glyphEntries[0] = new GLYPHENTRY();
                            tr.glyphEntries[0].glyphAdvance = 0;
                            tr.glyphEntries[0].glyphIndex = f;
                            rec.add(tr);
                            mat.translateX = x * width / sloupcu;
                            mat.translateY = y * height / radku;
                            sos2.writeTag(new DefineTextTag(999 + f, new RECT(0, width, 0, height), new MATRIX(), SWFOutputStream.getNeededBitsU(countGlyphs - 1), SWFOutputStream.getNeededBitsU(0), rec));
                            sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1 + f, 999 + f, mat, null, 0, null, 0, null));
                            x++;
                        }
                        sos2.writeTag(new ShowFrameTag());
                    } else if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                        sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                        sos2.writeTag(new ShowFrameTag());
                        int numFrames = 100;
                        for (int ratio = 0; ratio < 65536; ratio += 65536 / numFrames) {
                            sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, false, true, 1, chtId, mat, null, ratio, null, 0, null));
                            sos2.writeTag(new ShowFrameTag());
                        }
                    } else {
                        sos2.writeTag(new PlaceObject2Tag(false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                        sos2.writeTag(new ShowFrameTag());
                    }
                }//not showframe

                sos2.writeTag(new EndTag());
                byte data[] = baos.toByteArray();

                sos.writeUI32(sos.getPos() + data.length + 4);
                sos.write(data);
                fos.close();
                showCard(CARDFLASHPANEL);
                if (flashPanel != null) {
                    if (flashPanel instanceof FlashPlayerPanel) {
                        flashPanel.displaySWF(tempFile.getAbsolutePath());
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (tagObj instanceof TextTag) {
                displayWithPreview.setVisible(true);
                showDetailWithPreview(CARDTEXTPANEL);
                textValue.setText(((TextTag) tagObj).getFormattedText(swf.tags));
            } else {
                displayWithPreview.setVisible(false);
            }

        } else {
            showCard(CARDEMPTYPANEL);
        }
    }

    public void refreshTree() {
        List<Object> objs = new ArrayList<Object>();
        objs.addAll(swf.tags);
        tagTree.setModel(new TagTreeModel(createTagList(objs, null), (new File(Main.file)).getName()));
    }

    public void setEditText(boolean edit) {
        textValue.setEditable(edit);
        textSaveButton.setVisible(edit);
        textEditButton.setVisible(!edit);
        textCancelButton.setVisible(edit);
        if (!edit) {
            reload(true);
        }
    }
}
