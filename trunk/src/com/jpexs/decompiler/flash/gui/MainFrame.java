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
import com.jpexs.decompiler.flash.FrameNode;
import com.jpexs.decompiler.flash.PackageNode;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.DeobfuscationDialog;
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.abc.TreeElement;
import com.jpexs.decompiler.flash.gui.action.ActionPanel;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.helpers.Cache;
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
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.sun.jna.Platform;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Jindra
 */
public class MainFrame extends AppFrame implements ActionListener, TreeSelectionListener {

    private SWF swf;
    public ABCPanel abcPanel;
    public ActionPanel actionPanel;
    public LoadingPanel loadingPanel = new LoadingPanel(20, 20);
    public JLabel statusLabel = new JLabel("");
    public JPanel statusPanel = new JPanel();
    public JProgressBar progressBar = new JProgressBar(0, 100);
    private DeobfuscationDialog deobfuscationDialog;
    public JTree tagTree;
    public FlashPlayerPanel flashPanel;
    public JPanel displayPanel;
    public ImagePanel imagePanel;
    public ImagePanel previewImagePanel;
    public SWFPreviwPanel swfPreviewPanel;
    final static String CARDFLASHPANEL = "Flash card";
    final static String CARDSWFPREVIEWPANEL = "SWF card";
    final static String CARDDRAWPREVIEWPANEL = "Draw card";
    final static String CARDIMAGEPANEL = "Image card";
    final static String CARDEMPTYPANEL = "Empty card";
    final static String CARDACTIONSCRIPTPANEL = "ActionScript card";
    final static String DETAILCARDAS3NAVIGATOR = "Traits list";
    final static String DETAILCARDEMPTYPANEL = "Empty card";
    final static String CARDTEXTPANEL = "Text card";
    private LineMarkedEditorPane textValue;
    private JPEGTablesTag jtt;
    private HashMap<Integer, CharacterTag> characters;
    private List<ABCContainerTag> abcList;
    JSplitPane splitPane1;
    JSplitPane splitPane2;
    private boolean splitsInited = false;
    private JPanel detailPanel;
    private JTextField filterField = new JTextField("");
    private JPanel searchPanel;
    private JCheckBoxMenuItem autoDeobfuscateMenuItem;
    private JPanel displayWithPreview;
    private JButton textSaveButton;
    private JButton textEditButton;
    private JButton textCancelButton;
    private JPanel parametersPanel;
    private JSplitPane previewSplitPane;
    private JButton imageReplaceButton;
    private JPanel imageButtonsPanel;
    private JCheckBoxMenuItem miInternalViewer;
    private JCheckBoxMenuItem miParallelSpeedUp;
    private JCheckBoxMenuItem miAssociate;
    private JCheckBoxMenuItem miDecompile;
    private JCheckBoxMenuItem miCacheDisk;
    private JCheckBoxMenuItem miGotoMainClassOnStartup;

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

        int w = (Integer) Configuration.getConfig("gui.window.width", 1000);
        int h = (Integer) Configuration.getConfig("gui.window.height", 700);
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (w > dim.width) {
            w = dim.width;
        }
        if (h > dim.height) {
            h = dim.height;
        }
        setSize(w, h);

        boolean maximizedHorizontal = (Boolean) Configuration.getConfig("gui.window.maximized.horizontal", false);
        boolean maximizedVertical = (Boolean) Configuration.getConfig("gui.window.maximized.vertical", false);

        int state = 0;
        if (maximizedHorizontal) {
            state = state | JFrame.MAXIMIZED_HORIZ;
        }
        if (maximizedVertical) {
            state = state | JFrame.MAXIMIZED_VERT;
        }
        setExtendedState(state);

        View.setWindowIcon(this);
        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                int state = e.getNewState();
                Configuration.setConfig("gui.window.maximized.horizontal", (state & JFrame.MAXIMIZED_HORIZ) == JFrame.MAXIMIZED_HORIZ);
                Configuration.setConfig("gui.window.maximized.vertical", (state & JFrame.MAXIMIZED_VERT) == JFrame.MAXIMIZED_VERT);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int state = getExtendedState();
                if ((state & JFrame.MAXIMIZED_HORIZ) == 0) {
                    Configuration.setConfig("gui.window.width", getWidth());
                }
                if ((state & JFrame.MAXIMIZED_VERT) == 0) {
                    Configuration.setConfig("gui.window.height", getHeight());
                }
            }
        });
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
        setTitle(Main.applicationVerName + (Configuration.DISPLAY_FILENAME ? " - " + Main.getFileTitle() : ""));
        JMenuBar menuBar = new JMenuBar();


        try {
            flashPanel = new FlashPlayerPanel(this);
        } catch (FlashUnsupportedException fue) {
        }

        JMenu menuFile = new JMenu(translate("menu.file"));
        JMenuItem miOpen = new JMenuItem(translate("menu.file.open"));
        miOpen.setIcon(View.getIcon("open16"));
        miOpen.setActionCommand("OPEN");
        miOpen.addActionListener(this);
        JMenuItem miSave = new JMenuItem(translate("menu.file.save"));
        miSave.setIcon(View.getIcon("save16"));
        miSave.setActionCommand("SAVE");
        miSave.addActionListener(this);
        JMenuItem miSaveAs = new JMenuItem(translate("menu.file.saveas"));
        miSaveAs.setIcon(View.getIcon("saveas16"));
        miSaveAs.setActionCommand("SAVEAS");
        miSaveAs.addActionListener(this);

        JMenuItem menuExportFla = new JMenuItem(translate("menu.file.export.fla"));
        menuExportFla.setActionCommand("EXPORTFLA");
        menuExportFla.addActionListener(this);
        menuExportFla.setIcon(View.getIcon("flash16"));

        JMenuItem menuExportAll = new JMenuItem(translate("menu.file.export.all"));
        menuExportAll.setActionCommand("EXPORT");
        menuExportAll.addActionListener(this);
        JMenuItem menuExportSel = new JMenuItem(translate("menu.file.export.selection"));
        menuExportSel.setActionCommand("EXPORTSEL");
        menuExportSel.addActionListener(this);
        menuExportAll.setIcon(View.getIcon("export16"));
        menuExportSel.setIcon(View.getIcon("exportsel16"));



        menuFile.add(miOpen);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(menuExportFla);
        menuFile.add(menuExportAll);
        menuFile.add(menuExportSel);
        menuFile.addSeparator();
        JMenuItem miClose = new JMenuItem(translate("menu.file.exit"));
        miClose.setIcon(View.getIcon("exit16"));
        miClose.setActionCommand("EXIT");
        miClose.addActionListener(this);
        menuFile.add(miClose);
        menuBar.add(menuFile);
        JMenu menuDeobfuscation = new JMenu(translate("menu.tools.deobfuscation"));
        menuDeobfuscation.setIcon(View.getIcon("deobfuscate16"));

        JMenuItem miDeobfuscation = new JMenuItem(translate("menu.tools.deobfuscation.pcode"));
        miDeobfuscation.setActionCommand("DEOBFUSCATE");
        miDeobfuscation.addActionListener(this);

        autoDeobfuscateMenuItem = new JCheckBoxMenuItem(translate("menu.settings.autodeobfuscation"));
        autoDeobfuscateMenuItem.setState((Boolean) Configuration.getConfig("autoDeobfuscate", true));
        autoDeobfuscateMenuItem.addActionListener(this);
        autoDeobfuscateMenuItem.setActionCommand("AUTODEOBFUSCATE");


        /*     JCheckBoxMenuItem miSubLimiter = new JCheckBoxMenuItem("Enable sub limiter");
         miSubLimiter.setActionCommand("SUBLIMITER");
         miSubLimiter.addActionListener(this);
         */
        JMenuItem miRenameOneIdentifier = new JMenuItem(translate("menu.tools.deobfuscation.globalrename"));
        miRenameOneIdentifier.setActionCommand("RENAMEONEIDENTIFIER");
        miRenameOneIdentifier.addActionListener(this);

        JMenuItem miRenameIdentifiers = new JMenuItem(translate("menu.tools.deobfuscation.renameinvalid"));
        miRenameIdentifiers.setActionCommand("RENAMEIDENTIFIERS");
        miRenameIdentifiers.addActionListener(this);

        /*JMenuItem miRemoveDeadCode = new JMenuItem("Remove dead code");
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
         miControlFlowAll.addActionListener(this);*/

        menuDeobfuscation.add(miRenameOneIdentifier);
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
        JMenu menuTools = new JMenu(translate("menu.tools"));
        JMenuItem miProxy = new JMenuItem(translate("menu.tools.proxy"));
        miProxy.setActionCommand("SHOWPROXY");
        miProxy.setIcon(View.getIcon("proxy16"));
        miProxy.addActionListener(this);

        JMenuItem miSearchScript = new JMenuItem(translate("menu.tools.searchas"));
        miSearchScript.addActionListener(this);
        miSearchScript.setActionCommand("SEARCHAS");
        miSearchScript.setIcon(View.getIcon("search16"));

        menuTools.add(miSearchScript);

        miInternalViewer = new JCheckBoxMenuItem(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected((Boolean) Configuration.getConfig("internalFlashViewer", (Boolean) (flashPanel == null)));
        if (flashPanel == null) {
            miInternalViewer.setSelected(true);
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.setActionCommand("INTERNALVIEWERSWITCH");
        miInternalViewer.addActionListener(this);

        miParallelSpeedUp = new JCheckBoxMenuItem(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected((Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
        miParallelSpeedUp.setActionCommand("PARALLELSPEEDUP");
        miParallelSpeedUp.addActionListener(this);


        menuTools.add(miProxy);

        //menuTools.add(menuDeobfuscation);
        menuTools.add(menuDeobfuscation);

        JMenuItem miGotoDocumentClass = new JMenuItem(translate("menu.tools.gotodocumentclass"));
        miGotoDocumentClass.setActionCommand("GOTODOCUMENTCLASS");
        miGotoDocumentClass.addActionListener(this);
        menuBar.add(menuTools);

        miDecompile = new JCheckBoxMenuItem(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!(Boolean) Configuration.getConfig("decompile", Boolean.TRUE));
        miDecompile.setActionCommand("DISABLEDECOMPILATION");
        miDecompile.addActionListener(this);


        miCacheDisk = new JCheckBoxMenuItem(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected((Boolean) Configuration.getConfig("cacheOnDisk", Boolean.TRUE));
        miCacheDisk.setActionCommand("CACHEONDISK");
        miCacheDisk.addActionListener(this);

        miGotoMainClassOnStartup = new JCheckBoxMenuItem(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected((Boolean) Configuration.getConfig("gotoMainClassOnStartup", Boolean.FALSE));
        miGotoMainClassOnStartup.setActionCommand("GOTODOCUMENTCLASSONSTARTUP");
        miGotoMainClassOnStartup.addActionListener(this);

        JMenu menuSettings = new JMenu(translate("menu.settings"));
        menuSettings.add(autoDeobfuscateMenuItem);
        menuSettings.add(miInternalViewer);
        menuSettings.add(miParallelSpeedUp);
        menuSettings.add(miDecompile);
        menuSettings.add(miCacheDisk);
        menuSettings.add(miGotoMainClassOnStartup);

        miAssociate = new JCheckBoxMenuItem(translate("menu.settings.addtocontextmenu"));
        miAssociate.setActionCommand("ASSOCIATE");
        miAssociate.addActionListener(this);
        miAssociate.setState(Main.isAddedToContextMenu());


        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.setActionCommand("SETLANGUAGE");
        miLanguage.addActionListener(this);

        if (Platform.isWindows()) {
            menuSettings.add(miAssociate);
        }
        menuSettings.add(miLanguage);

        menuBar.add(menuSettings);
        JMenu menuHelp = new JMenu(translate("menu.help"));
        JMenuItem miAbout = new JMenuItem(translate("menu.help.about"));
        miAbout.setIcon(View.getIcon("about16"));

        miAbout.setActionCommand("ABOUT");
        miAbout.addActionListener(this);

        JMenuItem miCheckUpdates = new JMenuItem(translate("menu.help.checkupdates"));
        miCheckUpdates.setActionCommand("CHECKUPDATES");
        miCheckUpdates.setIcon(View.getIcon("update16"));
        miCheckUpdates.addActionListener(this);

        JMenuItem miHelpUs = new JMenuItem(translate("menu.help.helpus"));
        miHelpUs.setActionCommand("HELPUS");
        miHelpUs.setIcon(View.getIcon("donate16"));
        miHelpUs.addActionListener(this);

        JMenuItem miHomepage = new JMenuItem(translate("menu.help.homepage"));
        miHomepage.setActionCommand("HOMEPAGE");
        miHomepage.setIcon(View.getIcon("homepage16"));
        miHomepage.addActionListener(this);


        menuHelp.add(miCheckUpdates);
        menuHelp.add(miHelpUs);
        menuHelp.add(miHomepage);
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
        List<Object> objs = new ArrayList<>();
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


        abcList = new ArrayList<>();
        getActionScript3(objs, abcList);
        if (!abcList.isEmpty()) {
            abcPanel = new ABCPanel(abcList, swf);
            detailPanel.add(abcPanel.tabbedPane, DETAILCARDAS3NAVIGATOR);
            menuTools.add(miGotoDocumentClass);
        } else {
            actionPanel = new ActionPanel();
            miDeobfuscation.setEnabled(false);
        }


        tagTree = new JTree(new TagTreeModel(createTagList(objs, null), new SWFRoot((new File(Main.file)).getName())));
        tagTree.addTreeSelectionListener(this);

        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(tagTree, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {
            @Override
            public void dragGestureRecognized(DragGestureEvent dge) {
                dge.startDrag(DragSource.DefaultCopyDrop, new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(DataFlavor.javaFileListFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                            List<File> files = new ArrayList<>();
                            String tempDir = System.getProperty("java.io.tmpdir");
                            if (!tempDir.endsWith(File.separator)) {
                                tempDir += File.separator;
                            }
                            Random rnd = new Random();
                            tempDir += "ffdec" + File.separator + "export" + File.separator + System.currentTimeMillis() + "_" + rnd.nextInt(1000);
                            new File(tempDir).mkdirs();
                            final ExportDialog export = new ExportDialog();

                            try {
                                File ftemp = new File(tempDir);
                                files = exportSelection(tempDir, export);
                                files.clear();

                                File fs[] = ftemp.listFiles();
                                for (File f : fs) {
                                    files.add(f);
                                }

                                Main.stopWork();
                            } catch (IOException ex) {
                                return null;
                            }
                            for (File f : files) {
                                f.deleteOnExit();
                            }
                            new File(tempDir).deleteOnExit();
                            return files;

                        }
                        return null;
                    }
                }, new DragSourceListener() {
                    @Override
                    public void dragEnter(DragSourceDragEvent dsde) {
                        enableDrop(false);
                    }

                    @Override
                    public void dragOver(DragSourceDragEvent dsde) {
                    }

                    @Override
                    public void dropActionChanged(DragSourceDragEvent dsde) {
                    }

                    @Override
                    public void dragExit(DragSourceEvent dse) {
                    }

                    @Override
                    public void dragDropEnd(DragSourceDropEvent dsde) {
                        enableDrop(true);
                    }
                });
            }
        });
        final JPopupMenu contextPopupMenu = new JPopupMenu();
        final JMenuItem removeMenuItem = new JMenuItem(translate("contextmenu.remove"));
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand("REMOVEITEM");
        JMenuItem exportSelectionMenuItem = new JMenuItem(translate("menu.file.export.selection"));
        exportSelectionMenuItem.setActionCommand("EXPORTSEL");
        exportSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(exportSelectionMenuItem);

        contextPopupMenu.add(removeMenuItem);
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
                    removeMenuItem.setVisible(tagObj instanceof Tag);
                    contextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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

                String tos = value.toString();
                int sw = getFontMetrics(getFont()).stringWidth(tos);
                setPreferredSize(new Dimension(18 + sw, 32));
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
        characters = new HashMap<>();
        List<Object> list2 = new ArrayList<>();
        list2.addAll(swf.tags);
        parseCharacters(list2);
        JPanel textTopPanel = new JPanel(new BorderLayout());
        textValue = new LineMarkedEditorPane();
        textTopPanel.add(new JScrollPane(textValue), BorderLayout.CENTER);
        textValue.setEditable(false);
        //textValue.setFont(UIManager.getFont("TextField.font"));

        /*JPanel textBottomPanel = new JPanel();
         textBottomPanel.setLayout(new BoxLayout(textBottomPanel, BoxLayout.X_AXIS));
         textBottomPanel.add(new JLabel("Xmin:"));
         textBottomPanel.add(textRectXmin);
         textBottomPanel.add(new JLabel("Ymin:"));
         textBottomPanel.add(textRectYmin);
         textBottomPanel.add(new JLabel("Xmax:"));
         textBottomPanel.add(textRectXmax);        
         textBottomPanel.add(new JLabel("Ymax:"));
         textBottomPanel.add(textRectYmax);*/



        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());


        textSaveButton = new JButton(translate("button.save"), View.getIcon("save16"));
        textSaveButton.setMargin(new Insets(3, 3, 3, 10));
        textSaveButton.setActionCommand("SAVETEXT");
        textSaveButton.addActionListener(this);

        textEditButton = new JButton(translate("button.edit"), View.getIcon("edit16"));
        textEditButton.setMargin(new Insets(3, 3, 3, 10));
        textEditButton.setActionCommand("EDITTEXT");
        textEditButton.addActionListener(this);

        textCancelButton = new JButton(translate("button.cancel"), View.getIcon("cancel16"));
        textCancelButton.setMargin(new Insets(3, 3, 3, 10));
        textCancelButton.setActionCommand("CANCELTEXT");
        textCancelButton.addActionListener(this);

        buttonsPanel.add(textEditButton);
        buttonsPanel.add(textSaveButton);
        buttonsPanel.add(textCancelButton);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        textTopPanel.add(buttonsPanel, BorderLayout.SOUTH);

        displayWithPreview = new JPanel(new CardLayout());


        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(textTopPanel);
        //textPanel.add(textBottomPanel);

        displayWithPreview.add(textPanel, CARDTEXTPANEL);
        //displayWithPreview.setVisible(false);



        Component leftComponent = null;


        displayPanel = new JPanel(new CardLayout());

        if (flashPanel != null) {
            leftComponent = flashPanel;
        } else {
            JPanel swtPanel = new JPanel(new BorderLayout());
            swtPanel.add(new JLabel("<html><center>" + translate("notavailonthisplatform") + "</center></html>", JLabel.CENTER), BorderLayout.CENTER);
            swtPanel.setBackground(Color.white);
            leftComponent = swtPanel;
        }

        textValue.setContentType("text/swf_text");

        previewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        previewSplitPane.setDividerLocation(300);
        JPanel pan = new JPanel(new BorderLayout());
        JLabel prevLabel = new JLabel(translate("swfpreview"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);
        prevLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        JLabel paramsLabel = new JLabel(translate("parameters"));
        paramsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        paramsLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        pan.add(prevLabel, BorderLayout.NORTH);
        pan.add(leftComponent, BorderLayout.CENTER);
        previewSplitPane.setLeftComponent(pan);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(paramsLabel, BorderLayout.NORTH);
        parametersPanel.add(displayWithPreview, BorderLayout.CENTER);
        previewSplitPane.setRightComponent(parametersPanel);
        parametersPanel.setVisible(false);
        displayPanel.add(previewSplitPane, CARDFLASHPANEL);
        imagePanel = new ImagePanel();
        JPanel imagesCard = new JPanel(new BorderLayout());
        imagesCard.add(imagePanel, BorderLayout.CENTER);




        imageReplaceButton = new JButton(translate("button.replace"), View.getIcon("edit16"));
        imageReplaceButton.setMargin(new Insets(3, 3, 3, 10));
        imageReplaceButton.setActionCommand("REPLACEIMAGE");
        imageReplaceButton.addActionListener(this);
        imageButtonsPanel = new JPanel(new FlowLayout());
        imageButtonsPanel.add(imageReplaceButton);

        imagesCard.add(imageButtonsPanel, BorderLayout.SOUTH);

        displayPanel.add(imagesCard, CARDIMAGEPANEL);

        JPanel shapesCard = new JPanel(new BorderLayout());

        JPanel previewPanel = new JPanel(new BorderLayout());

        previewImagePanel = new ImagePanel();
        previewPanel.add(previewImagePanel, BorderLayout.CENTER);
        JLabel prevIntLabel = new JLabel(translate("swfpreview.internal"));
        prevIntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        prevIntLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        previewPanel.add(prevIntLabel, BorderLayout.NORTH);
        shapesCard.add(previewPanel, BorderLayout.CENTER);
        displayPanel.add(shapesCard, CARDDRAWPREVIEWPANEL);

        swfPreviewPanel = new SWFPreviwPanel();
        displayPanel.add(swfPreviewPanel, CARDSWFPREVIEWPANEL);


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
        //splitPane1.setDividerLocation(0.5);

        splitPane1.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (splitsInited) {
                    Configuration.setConfig("gui.splitPane1.dividerLocation", pce.getNewValue());
                }
            }
        });

        splitPane2.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (detailPanel.isVisible()) {
                    Configuration.setConfig("gui.splitPane2.dividerLocation", pce.getNewValue());
                }
            }
        });
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

        //Opening files with drag&drop to main window
        enableDrop(true);
    }

    public void enableDrop(boolean value) {
        if (value) {
            setDropTarget(new DropTarget() {
                @Override
                public synchronized void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (!droppedFiles.isEmpty()) {
                            Main.openFile(droppedFiles.get(0).getAbsolutePath());
                        }
                    } catch (Exception ex) {
                    }
                }
            });
        } else {
            setDropTarget(null);
        }
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

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    splitPane1.setDividerLocation((Integer) Configuration.getConfig("gui.splitPane1.dividerLocation", getWidth() / 3));
                    int confDivLoc = (Integer) Configuration.getConfig("gui.splitPane2.dividerLocation", splitPane2.getHeight() * 3 / 5);
                    if (confDivLoc > splitPane2.getHeight() - 10) { //In older releases, divider location was saved when detailPanel was invisible too
                        confDivLoc = splitPane2.getHeight() * 3 / 5;
                    }
                    splitPane2.setDividerLocation(confDivLoc);

                    splitPos = splitPane2.getDividerLocation();
                    splitsInited = true;
                    if (miGotoMainClassOnStartup.isSelected()) {
                        gotoDocumentClass();
                    }
                }
            });


        }
    }

    private void parseCharacters(List<Object> list) {
        for (Object t : list) {
            if (t instanceof CharacterTag) {
                characters.put(((CharacterTag) t).getCharacterId(), (CharacterTag) t);
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
        List<TagNode> ret = new ArrayList<>();
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
            if (te.getItem() instanceof ScriptPack) {
                return "as";
            } else {
                return "package";
            }
        }
        if (t instanceof PackageNode) {
            return "package";
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
        List<Object> ret = new ArrayList<>();
        for (Object o : list) {
            String ttype = getTagType(o);
            if (type.equals(ttype)) {
                ret.add(o);
            }
        }
        return ret;
    }

    public List<TagNode> getTagNodesWithType(List<Object> list, String type, Object parent, boolean display) {
        List<TagNode> ret = new ArrayList<>();
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

    public void renameIdentifier(String identifier) {
        String oldName = identifier;
        String newName = JOptionPane.showInputDialog(translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                swf.renameAS2Identifier(oldName, newName);
                JOptionPane.showMessageDialog(null, translate("rename.finished.identifier"));
                doFilter();
                reload(true);
            }
        }
    }

    public void renameMultiname(int multiNameIndex) {
        String oldName = "";
        if (abcPanel.abc.constants.constant_multiname[multiNameIndex].name_index > 0) {
            oldName = abcPanel.abc.constants.constant_string[abcPanel.abc.constants.constant_multiname[multiNameIndex].name_index];
        }
        String newName = JOptionPane.showInputDialog(translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                int mulCount = 0;
                for (ABCContainerTag cnt : abcList) {
                    ABC abc = cnt.getABC();
                    for (int m = 1; m < abc.constants.constant_multiname.length; m++) {
                        int ni = abc.constants.constant_multiname[m].name_index;
                        String n = "";
                        if (ni > 0) {
                            n = abc.constants.constant_string[ni];
                        }
                        if (n.equals(oldName)) {
                            abc.renameMultiname(m, newName);
                            mulCount++;
                        }
                    }
                }
                JOptionPane.showMessageDialog(null, translate("rename.finished.multiname").replace("%count%", "" + mulCount));
                if (abcPanel != null) {
                    abcPanel.reload();
                }
                doFilter();
                reload(true);
            }
        }
    }

    public List<Object> getAllSubs(JTree tree, Object o) {
        TreeModel tm = tree.getModel();
        List<Object> ret = new ArrayList<>();
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
        List<Object> ret = new ArrayList<>();
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
                    if ("scripts".equals(((TagNode) node).mark)) {
                        return (TagNode) node;
                    }
                }
            }
        }
        return null;
    }

    public List<TagNode> createTagList(List<Object> list, Object parent) {
        List<TagNode> ret = new ArrayList<>();
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
        List<TagNode> actionScript = new ArrayList<>();

        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i).tag instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = new ArrayList<>();
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

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
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
        TagNode textsNode = new TagNode(translate("node.texts"));
        textsNode.subItems.addAll(texts);

        TagNode imagesNode = new TagNode(translate("node.images"));
        imagesNode.subItems.addAll(images);

        TagNode moviesNode = new TagNode(translate("node.movies"));
        moviesNode.subItems.addAll(movies);

        TagNode soundsNode = new TagNode(translate("node.sounds"));
        soundsNode.subItems.addAll(sounds);


        TagNode binaryDataNode = new TagNode(translate("node.binaryData"));
        binaryDataNode.subItems.addAll(binaryData);

        TagNode fontsNode = new TagNode(translate("node.fonts"));
        fontsNode.subItems.addAll(fonts);


        TagNode spritesNode = new TagNode(translate("node.sprites"));
        spritesNode.subItems.addAll(sprites);

        TagNode shapesNode = new TagNode(translate("node.shapes"));
        shapesNode.subItems.addAll(shapes);

        TagNode morphShapesNode = new TagNode(translate("node.morphshapes"));
        morphShapesNode.subItems.addAll(morphShapes);

        TagNode buttonsNode = new TagNode(translate("node.buttons"));
        buttonsNode.subItems.addAll(buttons);

        TagNode framesNode = new TagNode(translate("node.frames"));
        framesNode.subItems.addAll(frames);

        TagNode actionScriptNode = new TagNode(translate("node.scripts"));
        actionScriptNode.mark = "scripts";
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
        return JOptionPane.showConfirmDialog(null, translate("message.confirm.experimental"), translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }
    private SearchDialog searchDialog;

    public List<File> exportSelection(String selFile, ExportDialog export) throws IOException {
        final boolean isPcode = export.getOption(ExportDialog.OPTION_ACTIONSCRIPT) == 1;
        final boolean isMp3OrWav = export.getOption(ExportDialog.OPTION_SOUNDS) == 0;
        final boolean isFormatted = export.getOption(ExportDialog.OPTION_TEXTS) == 1;

        List<File> ret = new ArrayList<>();
        List<Object> sel = getAllSelected(tagTree);

        List<ScriptPack> tlsList = new ArrayList<>();
        JPEGTablesTag jtt = null;
        for (Tag t : swf.tags) {
            if (t instanceof JPEGTablesTag) {
                jtt = (JPEGTablesTag) t;
                break;
            }
        }
        List<Tag> images = new ArrayList<>();
        List<Tag> shapes = new ArrayList<>();
        List<Tag> movies = new ArrayList<>();
        List<Tag> sounds = new ArrayList<>();
        List<Tag> texts = new ArrayList<>();
        List<TagNode> actionNodes = new ArrayList<>();
        List<Tag> binaryData = new ArrayList<>();
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
                    tlsList.add((ScriptPack) ((TreeElement) d).getItem());
                }
            }
        }
        ret.addAll(swf.exportImages(selFile + File.separator + "images", images));
        ret.addAll(SWF.exportShapes(selFile + File.separator + "shapes", shapes));
        ret.addAll(swf.exportTexts(selFile + File.separator + "texts", texts, isFormatted));
        ret.addAll(swf.exportMovies(selFile + File.separator + "movies", movies));
        ret.addAll(swf.exportSounds(selFile + File.separator + "sounds", sounds, isMp3OrWav, isMp3OrWav));
        ret.addAll(swf.exportBinaryData(selFile + File.separator + "binaryData", binaryData));
        if (abcPanel != null) {
            for (int i = 0; i < tlsList.size(); i++) {
                ScriptPack tls = tlsList.get(i);
                Main.startWork(translate("work.exporting") + " " + (i + 1) + "/" + tlsList.size() + " " + tls.getPath() + " ...");
                ret.add(tls.export(selFile, abcList, isPcode, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE)));
            }
        } else {
            List<TagNode> allNodes = new ArrayList<>();
            TagNode asn = getASTagNode(tagTree);
            if (asn != null) {
                allNodes.add(asn);
                TagNode.setExport(allNodes, false);
                TagNode.setExport(actionNodes, true);
                ret.addAll(TagNode.exportNodeAS(allNodes, selFile, isPcode));
            }
        }
        return ret;
    }

    private void clearCache() {
        if (abcPanel != null) {
            abcPanel.decompiledTextArea.clearScriptCache();
        }
        if (actionPanel != null) {
            actionPanel.clearCache();
        }
    }

    public void gotoDocumentClass() {
        String documentClass = null;
        loopdc:
        for (Tag t : swf.tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tagIDs.length; i++) {
                    if (sc.tagIDs[i] == 0) {
                        documentClass = sc.classNames[i];
                        break loopdc;
                    }
                }
            }
        }
        if (documentClass != null) {
            showDetail(DETAILCARDAS3NAVIGATOR);
            showCard(CARDACTIONSCRIPTPANEL);
            abcPanel.hilightScript(documentClass);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "GOTODOCUMENTCLASSONSTARTUP":
                Configuration.setConfig("gotoMainClassOnStartup", miGotoMainClassOnStartup.isSelected());
                break;
            case "CACHEONDISK":
                Configuration.setConfig("cacheOnDisk", miCacheDisk.isSelected());
                if (miCacheDisk.isSelected()) {
                    Cache.setStorageType(Cache.STORAGE_FILES);
                } else {
                    Cache.setStorageType(Cache.STORAGE_MEMORY);
                }
                break;
            case "SETLANGUAGE":
                String newLanguage = new SelectLanguageDialog().display();
                if (newLanguage != null) {
                    if (newLanguage.equals("en")) {
                        newLanguage = "";
                    }
                    Configuration.setConfig("locale", newLanguage);
                    JOptionPane.showMessageDialog(null, "Changing language needs application restart.\r\nApplication will exit now, please run it again.");
                    Main.exit();
                }
                break;
            case "DISABLEDECOMPILATION":
                Configuration.setConfig("decompile", !miDecompile.isSelected());
                clearCache();
                if (abcPanel != null) {
                    abcPanel.reload();
                }
                reload(true);
                doFilter();
                break;
            case "ASSOCIATE":
                if (miAssociate.getState() == Main.isAddedToContextMenu()) {
                    return;
                }
                Main.addToContextMenu(miAssociate.getState());

                //Update checkbox menuitem accordingly (User can cancel rights elevation)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        miAssociate.setState(Main.isAddedToContextMenu());
                    }
                }, 500); //It takes some time registry change to apply
                break;
            case "GOTODOCUMENTCLASS":
                gotoDocumentClass();
                break;
            case "PARALLELSPEEDUP":
                String confStr = translate("message.confirm.parallel") + "\r\n";
                if (miParallelSpeedUp.isSelected()) {
                    confStr += " " + translate("message.confirm.on");
                } else {
                    confStr += " " + translate("message.confirm.off");
                }
                if (JOptionPane.showConfirmDialog(null, confStr, translate("message.parallel"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.setConfig("paralelSpeedUp", (Boolean) miParallelSpeedUp.isSelected());
                } else {
                    miParallelSpeedUp.setSelected(!miParallelSpeedUp.isSelected());
                }
                break;
            case "INTERNALVIEWERSWITCH":
                Configuration.setConfig("internalFlashViewer", (Boolean) miInternalViewer.isSelected());
                break;
            case "SEARCHAS":
                if (searchDialog == null) {
                    searchDialog = new SearchDialog();
                }
                searchDialog.setVisible(true);
                if (searchDialog.result) {
                    final String txt = searchDialog.searchField.getText();
                    if (!txt.equals("")) {
                        Main.startWork(translate("work.searching") + " \"" + txt + "\"...");
                        if (abcPanel != null) {
                            (new Thread() {
                                @Override
                                public void run() {
                                    if (abcPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                        showDetail(DETAILCARDAS3NAVIGATOR);
                                        showCard(CARDACTIONSCRIPTPANEL);
                                    } else {
                                        JOptionPane.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    Main.stopWork();
                                }
                            }).start();
                        } else {
                            (new Thread() {
                                @Override
                                public void run() {
                                    if (actionPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                        showCard(CARDACTIONSCRIPTPANEL);
                                    } else {
                                        JOptionPane.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    Main.stopWork();
                                }
                            }).start();
                        }
                    }
                }
                break;
            case "REPLACEIMAGE":
                Object tagObj = tagTree.getLastSelectedPathComponent();
                if (tagObj == null) {
                    return;
                }

                if (tagObj instanceof TagNode) {
                    tagObj = ((TagNode) tagObj).tag;
                }
                if (tagObj instanceof ImageTag) {
                    ImageTag it = (ImageTag) tagObj;
                    if (it.importSupported()) {
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory(new File((String) Configuration.getConfig("lastOpenDir", ".")));
                        fc.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return (f.getName().toLowerCase().endsWith(".jpg"))
                                        || (f.getName().toLowerCase().endsWith(".jpeg"))
                                        || (f.getName().toLowerCase().endsWith(".gif"))
                                        || (f.getName().toLowerCase().endsWith(".png"))
                                        || (f.isDirectory());
                            }

                            @Override
                            public String getDescription() {
                                return translate("filter.images");
                            }
                        });
                        JFrame f = new JFrame();
                        View.setWindowIcon(f);
                        int returnVal = fc.showOpenDialog(f);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            Configuration.setConfig("lastOpenDir", Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
                            byte data[] = Helper.readFile(selfile.getAbsolutePath());
                            try {
                                it.setImage(data);
                                swf.clearImageCache();
                            } catch (IOException ex) {
                                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Invalid image", ex);
                                JOptionPane.showMessageDialog(null, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
                            }
                            reload(true);
                        }
                    }
                }
                break;
            case "REMOVEITEM":
                tagObj = tagTree.getLastSelectedPathComponent();
                if (tagObj == null) {
                    return;
                }

                if (tagObj instanceof TagNode) {
                    tagObj = ((TagNode) tagObj).tag;
                }
                if (tagObj instanceof Tag) {
                    if (JOptionPane.showConfirmDialog(this, translate("message.confirm.remove").replace("%item%", tagObj.toString()), translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        swf.removeTag((Tag) tagObj);
                        showCard(CARDEMPTYPANEL);
                        refreshTree();
                    }
                }
                break;
            case "EDITTEXT":
                setEditText(true);
                break;
            case "CANCELTEXT":
                setEditText(false);
                break;
            case "SAVETEXT":
                if (oldValue instanceof TextTag) {
                    try {
                        ((TextTag) oldValue).setFormattedText(swf.tags, textValue.getText());
                        setEditText(false);
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(null, translate("error.text.invalid").replace("%text%", ex.text).replace("%line%", "" + ex.line), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;

            case "AUTODEOBFUSCATE":
                if (JOptionPane.showConfirmDialog(this, translate("message.confirm.autodeobfuscate") + "\r\n" + (autoDeobfuscateMenuItem.getState() ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.setConfig("autoDeobfuscate", autoDeobfuscateMenuItem.getState());
                    clearCache();
                    if (abcPanel != null) {
                        abcPanel.reload();
                    }
                    reload(true);
                    doFilter();
                } else {
                    autoDeobfuscateMenuItem.setState(!autoDeobfuscateMenuItem.getState());
                }
                break;
            case "EXIT":
                setVisible(false);
                if (Main.proxyFrame != null) {
                    if (Main.proxyFrame.isVisible()) {
                        return;
                    }
                }
                Main.exit();
                break;
        }
        if (Main.isWorking()) {
            return;
        }

        switch (e.getActionCommand()) {
            case "RENAMEONEIDENTIFIER":

                if (swf.fileAttributes.actionScript3) {
                    final int multiName = abcPanel.decompiledTextArea.getMultinameUnderCursor();
                    if (multiName > 0) {
                        (new Thread() {
                            @Override
                            public void run() {
                                Main.startWork(translate("work.renaming") + "...");
                                renameMultiname(multiName);
                                Main.stopWork();
                            }
                        }).start();

                    } else {
                        JOptionPane.showMessageDialog(null, translate("message.rename.notfound.multiname"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    final String identifier = actionPanel.getStringUnderCursor();
                    if (identifier != null) {
                        (new Thread() {
                            @Override
                            public void run() {
                                Main.startWork(translate("work.renaming") + "...");
                                renameIdentifier(identifier);
                                Main.stopWork();
                            }
                        }).start();
                    } else {
                        JOptionPane.showMessageDialog(null, translate("message.rename.notfound.identifier"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                break;
            case "ABOUT":
                Main.about();
                break;

            case "SHOWPROXY":
                Main.showProxy();
                break;

            case "SUBLIMITER":
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    Main.setSubLimiter(((JCheckBoxMenuItem) e.getSource()).getState());
                }

                break;

            case "SAVE":
                try {
                    Main.saveFile(Main.file);
                } catch (IOException ex) {
                    Logger.getLogger(com.jpexs.decompiler.flash.gui.abc.ABCPanel.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case "SAVEAS":
                if (Main.saveFileDialog()) {
                    setTitle(Main.applicationVerName + (Configuration.DISPLAY_FILENAME ? " - " + Main.getFileTitle() : ""));
                }
                break;
            case "OPEN":
                Main.openFileDialog();
                break;
            case "EXPORTFLA":
                JFileChooser fc = new JFileChooser();
                String selDir = (String) Configuration.getConfig("lastOpenDir", ".");
                fc.setCurrentDirectory(new File(selDir));
                if (!selDir.endsWith(File.separator)) {
                    selDir += File.separator;
                }
                String fileName = (new File(Main.file).getName());
                fileName = fileName.substring(0, fileName.length() - 4) + ".fla";
                fc.setSelectedFile(new File(selDir + fileName));
                FileFilter fla = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || (f.getName().toLowerCase().endsWith(".fla"));
                    }

                    @Override
                    public String getDescription() {
                        return translate("filter.fla");
                    }
                };
                FileFilter xfl = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || (f.getName().toLowerCase().endsWith(".xfl"));
                    }

                    @Override
                    public String getDescription() {
                        return translate("filter.xfl");
                    }
                };
                fc.setFileFilter(fla);
                fc.addChoosableFileFilter(xfl);
                fc.setAcceptAllFileFilterUsed(false);
                JFrame f = new JFrame();
                View.setWindowIcon(f);
                int returnVal = fc.showSaveDialog(f);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Configuration.setConfig("lastOpenDir", Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                    File sf = Helper.fixDialogFile(fc.getSelectedFile());

                    Main.startWork(translate("work.exporting.fla") + "...");
                    final boolean compressed = fc.getFileFilter() == fla;
                    if (!compressed) {
                        if (sf.getName().endsWith(".fla")) {
                            sf = new File(sf.getAbsolutePath().substring(0, sf.getAbsolutePath().length() - 4) + ".xfl");
                        }
                    }
                    final File selfile = sf;
                    (new Thread() {
                        @Override
                        public void run() {
                            if (compressed) {
                                swf.exportFla(selfile.getAbsolutePath(), new File(Main.file).getName(), Main.applicationName, Main.applicationVerName, Main.version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                            } else {
                                swf.exportXfl(selfile.getAbsolutePath(), new File(Main.file).getName(), Main.applicationName, Main.applicationVerName, Main.version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                            }
                            Main.stopWork();
                        }
                    }).start();
                }
                break;
            case "EXPORTSEL":
            case "EXPORT":
                final ExportDialog export = new ExportDialog();
                export.setVisible(true);
                if (!export.cancelled) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new java.io.File((String) Configuration.getConfig("lastExportDir", ".")));
                    chooser.setDialogTitle(translate("export.select.directory"));
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        final long timeBefore = System.currentTimeMillis();
                        Main.startWork(translate("work.exporting") + "...");
                        final String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
                        Configuration.setConfig("lastExportDir", Helper.fixDialogFile(chooser.getSelectedFile()).getParentFile().getAbsolutePath());
                        final boolean isPcode = export.getOption(ExportDialog.OPTION_ACTIONSCRIPT) == 1;
                        final boolean isMp3OrWav = export.getOption(ExportDialog.OPTION_SOUNDS) == 0;
                        final boolean isFormatted = export.getOption(ExportDialog.OPTION_TEXTS) == 1;
                        final boolean onlySel = e.getActionCommand().endsWith("SEL");
                        (new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (onlySel) {
                                        exportSelection(selFile, export);
                                    } else {
                                        swf.exportImages(selFile + File.separator + "images");
                                        swf.exportShapes(selFile + File.separator + "shapes");
                                        swf.exportTexts(selFile + File.separator + "texts", isFormatted);
                                        swf.exportMovies(selFile + File.separator + "movies");
                                        swf.exportSounds(selFile + File.separator + "sounds", isMp3OrWav, isMp3OrWav);
                                        swf.exportBinaryData(selFile + File.separator + "binaryData");
                                        swf.exportActionScript(selFile, isPcode, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                                    }
                                } catch (Exception ex) {
                                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Error during export", ex);
                                    JOptionPane.showMessageDialog(null, translate("error.export"));
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
                                setStatus(translate("export.finishedin").replace("%time%", timeStr));
                            }
                        }).start();

                    }
                }
                break;

            case "CHECKUPDATES":
                if (!Main.checkForUpdates()) {
                    JOptionPane.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
                }
                break;

            case "HELPUS":
                String helpUsURL = Main.projectPage + "/help_us.html";
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        java.net.URI uri = new java.net.URI(helpUsURL);
                        desktop.browse(uri);
                    } catch (Exception ex) {
                    }
                } else {
                    JOptionPane.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
                }
                break;

            case "HOMEPAGE":
                String homePageURL = Main.projectPage;
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        java.net.URI uri = new java.net.URI(homePageURL);
                        desktop.browse(uri);
                    } catch (Exception ex) {
                    }
                } else {
                    JOptionPane.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
                }
                break;

            case "RESTORECONTROLFLOW":
            case "RESTORECONTROLFLOWALL":
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
                            doFilter();
                            return true;
                        }
                    }.execute();
                }
                break;
            case "RENAMEIDENTIFIERS":
                if (confirmExperimental()) {
                    final RenameType renameType = new RenameDialog().display();
                    if (renameType != null) {
                        Main.startWork(translate("work.renaming.identifiers") + "...");
                        new SwingWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                try {
                                    int cnt = 0;
                                    cnt = swf.deobfuscateIdentifiers(renameType);
                                    Main.stopWork();
                                    JOptionPane.showMessageDialog(null, translate("message.rename.renamed").replace("%count%", "" + cnt));
                                    swf.assignClassesToSymbols();
                                    clearCache();
                                    if (abcPanel != null) {
                                        abcPanel.reload();
                                    }
                                    doFilter();
                                    reload(true);
                                } catch (Exception ex) {
                                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Error during renaming identifiers", ex);
                                }
                                return true;
                            }
                        }.execute();

                    }
                }
                break;
            case "DEOBFUSCATE":
            case "DEOBFUSCATEALL":
                if (deobfuscationDialog == null) {
                    deobfuscationDialog = new DeobfuscationDialog();
                }
                deobfuscationDialog.setVisible(true);
                if (deobfuscationDialog.ok) {
                    Main.startWork(translate("work.deobfuscating") + "...");
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            try {
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
                                            abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic());
                                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                                            abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic());
                                            abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                                        }
                                    }
                                    abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Deobfuscation error", ex);
                            }
                            Main.stopWork();
                            JOptionPane.showMessageDialog(null, translate("work.deobfuscating.complete"));
                            clearCache();
                            abcPanel.reload();
                            doFilter();
                            return true;
                        }
                    }.execute();
                }
                break;
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
        if (tagObj instanceof ScriptPack) {
            final ScriptPack scriptLeaf = (ScriptPack) tagObj;
            if (!Main.isWorking()) {
                Main.startWork(translate("work.decompiling") + "...");
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
                        abcPanel.decompiledTextArea.setScript(scriptLeaf, abcList);
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
        swfPreviewPanel.stop();
        if ((tagObj instanceof SWFRoot) && miInternalViewer.isSelected()) {
            showCard(CARDSWFPREVIEWPANEL);
            swfPreviewPanel.load(swf);
            swfPreviewPanel.play();
        } else if (tagObj instanceof DefineVideoStreamTag) {
            showCard(CARDEMPTYPANEL);
        } else if ((tagObj instanceof DefineSoundTag) || (tagObj instanceof SoundStreamHeadTag) || (tagObj instanceof SoundStreamHead2Tag)) {
            showCard(CARDEMPTYPANEL);
        } else if (tagObj instanceof DefineBinaryDataTag) {
            showCard(CARDEMPTYPANEL);
        } else if (tagObj instanceof ASMSource) {
            showCard(CARDACTIONSCRIPTPANEL);
            actionPanel.setSource((ASMSource) tagObj, !forceReload);
        } else if (tagObj instanceof ImageTag) {
            imageButtonsPanel.setVisible(((ImageTag) tagObj).importSupported());
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(((ImageTag) tagObj).getImage(swf.tags));
        } else if ((tagObj instanceof DrawableTag) && (!(tagObj instanceof TextTag)) && (miInternalViewer.isSelected())) {
            showCard(CARDDRAWPREVIEWPANEL);
            previewImagePanel.setDrawable((DrawableTag) tagObj, swf, characters);//.setImage(((DrawableTag) tagObj).toImage(1, swf.tags, swf.displayRect, characters));
        } else if (tagObj instanceof FrameNode && ((FrameNode) tagObj).isDisplayed() && (miInternalViewer.isSelected())) {
            showCard(CARDDRAWPREVIEWPANEL);
            FrameNode fn = (FrameNode) tagObj;
            List<Tag> controlTags = swf.tags;
            int containerId = 0;
            RECT rect = swf.displayRect;
            int totalFrameCount = swf.frameCount;
            if (fn.getParent() instanceof DefineSpriteTag) {
                controlTags = ((DefineSpriteTag) fn.getParent()).subTags;
                containerId = ((DefineSpriteTag) fn.getParent()).spriteId;
                rect = ((DefineSpriteTag) fn.getParent()).getRect(characters, new Stack<Integer>());
                totalFrameCount = ((DefineSpriteTag) fn.getParent()).frameCount;
            }
            previewImagePanel.setImage(SWF.frameToImage(containerId, ((FrameNode) tagObj).getFrame() - 1, swf.tags, controlTags, rect, totalFrameCount, new Stack<Integer>()));
        } else if (((tagObj instanceof FrameNode) && ((FrameNode) tagObj).isDisplayed()) || ((tagObj instanceof CharacterTag) || (tagObj instanceof FontTag)) && (tagObj instanceof Tag)) {
            try {

                if (tempFile != null) {
                    tempFile.delete();
                }
                tempFile = File.createTempFile("temp", ".swf");
                tempFile.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
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
                        List<Object> subs = new ArrayList<>();
                        if (parent == null) {
                            subs.addAll(swf.tags);
                        } else {
                            if (parent instanceof Container) {
                                subs = ((Container) parent).getSubItems();
                            }
                        }
                        List<Integer> doneCharacters = new ArrayList<>();
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
                            Set<Integer> needed = t.getDeepNeededCharacters(characters, new ArrayList<Integer>());
                            for (int n : needed) {
                                if (!doneCharacters.contains(n)) {
                                    sos2.writeTag(characters.get(n));
                                    doneCharacters.add(n);
                                }
                            }
                            if (t instanceof CharacterTag) {
                                doneCharacters.add(((CharacterTag) t).getCharacterId());
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
                                        RECT r = ((BoundedTag) parent).getRect(characters, new Stack<Integer>());
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
                            Set<Integer> needed = ((Tag) tagObj).getDeepNeededCharacters(characters, new ArrayList<Integer>());
                            for (int n : needed) {
                                sos2.writeTag(characters.get(n));
                            }
                        }

                        sos2.writeTag(((Tag) tagObj));

                        int chtId = 0;
                        if (tagObj instanceof CharacterTag) {
                            chtId = ((CharacterTag) tagObj).getCharacterId();
                        }

                        MATRIX mat = new MATRIX();
                        mat.hasRotate = false;
                        mat.hasScale = false;
                        mat.translateX = 0;
                        mat.translateY = 0;
                        if (tagObj instanceof BoundedTag) {
                            RECT r = ((BoundedTag) tagObj).getRect(characters, new Stack<Integer>());
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
                                List<TEXTRECORD> rec = new ArrayList<>();
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
                }
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
                parametersPanel.setVisible(true);
                previewSplitPane.setDividerLocation(previewSplitPane.getWidth() / 2);
                showDetailWithPreview(CARDTEXTPANEL);
                textValue.setText(((TextTag) tagObj).getFormattedText(swf.tags));
            } else {
                parametersPanel.setVisible(false);
            }

        } else {
            showCard(CARDEMPTYPANEL);
        }
    }

    public void refreshTree() {
        List<Object> objs = new ArrayList<>();
        objs.addAll(swf.tags);
        tagTree.setModel(new TagTreeModel(createTagList(objs, null), new SWFRoot((new File(Main.file)).getName())));
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
