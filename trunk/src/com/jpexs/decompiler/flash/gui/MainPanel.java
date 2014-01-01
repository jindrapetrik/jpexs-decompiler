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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.FrameNode;
import com.jpexs.decompiler.flash.PackageNode;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.TreeElementItem;
import com.jpexs.decompiler.flash.TreeNode;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.DeobfuscationDialog;
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.abc.TreeElement;
import com.jpexs.decompiler.flash.gui.action.ActionPanel;
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import com.jpexs.decompiler.flash.gui.player.PlayerControls;
import com.jpexs.decompiler.flash.helpers.Freed;
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
import com.jpexs.decompiler.flash.tags.DoActionTag;
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
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JPEXS
 */
public final class MainPanel extends JPanel implements ActionListener, TreeSelectionListener, Freed {
    
    private MainFrame mainFrame;
    private List<SWF> swfs;
    private ABCPanel abcPanel;
    private ActionPanel actionPanel;
    private JPanel welcomePanel;
    private MainFrameStatusPanel statusPanel;
    private MainFrameMenu mainMenu;
    private FontPanel fontPanel;
    private JProgressBar progressBar = new JProgressBar(0, 100);
    private DeobfuscationDialog deobfuscationDialog;
    public TagTree tagTree;
    private FlashPlayerPanel flashPanel;
    private JPanel contentPanel;
    private JPanel displayPanel;
    private ImagePanel imagePanel;
    private BinaryPanel binaryPanel;
    private ImagePanel previewImagePanel;
    private SWFPreviwPanel swfPreviewPanel;
    private boolean isWelcomeScreen = true;
    private static final String CARDFLASHPANEL = "Flash card";
    private static final String CARDSWFPREVIEWPANEL = "SWF card";
    private static final String CARDDRAWPREVIEWPANEL = "Draw card";
    private static final String CARDIMAGEPANEL = "Image card";
    private static final String CARDBINARYPANEL = "Binary card";
    private static final String CARDEMPTYPANEL = "Empty card";
    private static final String CARDACTIONSCRIPTPANEL = "ActionScript card";
    private static final String CARDACTIONSCRIPT3PANEL = "ActionScript3 card";
    private static final String DETAILCARDAS3NAVIGATOR = "Traits list";
    private static final String DETAILCARDEMPTYPANEL = "Empty card";
    private static final String CARDTEXTPANEL = "Text card";
    private static final String CARDFONTPANEL = "Font card";
    private static final String FLASH_VIEWER_CARD = "FLASHVIEWER";
    private static final String INTERNAL_VIEWER_CARD = "INTERNALVIEWER";
    private static final String SPLIT_PANE1 = "SPLITPANE1";
    private static final String WELCOME_PANEL = "WELCOMEPANEL";
    private LineMarkedEditorPane textValue;
    private JSplitPane splitPane1;
    private JSplitPane splitPane2;
    private boolean splitsInited = false;
    private JPanel detailPanel;
    private JTextField filterField = new MyTextField("");
    private JPanel searchPanel;
    private JPanel displayWithPreview;
    private JButton textSaveButton;
    private JButton textEditButton;
    private JButton textCancelButton;
    private JPanel parametersPanel;
    private JSplitPane previewSplitPane;
    private JButton imageReplaceButton;
    private JPanel imageButtonsPanel;
    private JButton binaryReplaceButton;
    private JPanel binaryButtonsPanel;
    private PlayerControls flashControls;
    private ImagePanel internelViewerPanel;
    private JPanel viewerCards;
    private AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
    private CancellableWorker setSourceWorker;
    public TreeNode oldNode;
    public TreeElementItem oldTag;
    private File tempFile;

    private static final String ACTION_SELECT_COLOR = "SELECTCOLOR";
    private static final String ACTION_REPLACE_IMAGE = "REPLACEIMAGE";
    private static final String ACTION_REPLACE_BINARY = "REPLACEBINARY";
    private static final String ACTION_REMOVE_ITEM = "REMOVEITEM";
    private static final String ACTION_EDIT_TEXT = "EDITTEXT";
    private static final String ACTION_CANCEL_TEXT = "CANCELTEXT";
    private static final String ACTION_SAVE_TEXT = "SAVETEXT";
    private static final String ACTION_CLOSE_SWF = "CLOSESWF";
    
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
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
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
        statusPanel.setStatus(s);
    }

    public void setWorkStatus(String s, CancellableWorker worker) {
        statusPanel.setWorkStatus(s, worker);
    }

    private void createContextMenu() {
        final JPopupMenu contextPopupMenu = new JPopupMenu();
        final JMenuItem removeMenuItem = new JMenuItem(translate("contextmenu.remove"));
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand(ACTION_REMOVE_ITEM);
        final JMenuItem exportSelectionMenuItem = new JMenuItem(translate("menu.file.export.selection"));
        exportSelectionMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_SEL);
        exportSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(exportSelectionMenuItem);
        final JMenuItem replaceImageSelectionMenuItem = new JMenuItem(translate("button.replace"));
        replaceImageSelectionMenuItem.setActionCommand(ACTION_REPLACE_IMAGE);
        replaceImageSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(replaceImageSelectionMenuItem);
        final JMenuItem replaceBinarySelectionMenuItem = new JMenuItem(translate("button.replace"));
        replaceBinarySelectionMenuItem.setActionCommand(ACTION_REPLACE_BINARY);
        replaceBinarySelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(replaceBinarySelectionMenuItem);
        final JMenuItem closeSelectionMenuItem = new JMenuItem(translate("contextmenu.closeSwf"));
        closeSelectionMenuItem.setActionCommand(ACTION_CLOSE_SWF);
        closeSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(closeSelectionMenuItem);

        contextPopupMenu.add(removeMenuItem);
        tagTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = tagTree.getClosestRowForLocation(e.getX(), e.getY());
                    int[] selectionRows = tagTree.getSelectionRows();
                    if (!Helper.contains(selectionRows, row)) {
                        tagTree.setSelectionRow(row);
                    }

                    TreePath[] paths = tagTree.getSelectionPaths();
                    if (paths == null || paths.length == 0) {
                        return;
                    }
                    boolean allSelectedIsTag = true;
                    for (TreePath treePath : paths) {
                        Object tagObj = treePath.getLastPathComponent();

                        if (tagObj instanceof TagNode) {
                            Object tag = ((TagNode) tagObj).tag;
                            if (!(tag instanceof Tag)) {
                                allSelectedIsTag = false;
                                break;
                            }
                        } else {
                            allSelectedIsTag = false;
                        }
                    }

                    replaceImageSelectionMenuItem.setVisible(false);
                    replaceBinarySelectionMenuItem.setVisible(false);
                    closeSelectionMenuItem.setVisible(false);
                    
                    if (paths.length == 1) {
                        Object tagObj = paths[0].getLastPathComponent();

                        if (tagObj instanceof TagNode) {
                            Object tag = ((TagNode) tagObj).tag;

                            if (tag instanceof ImageTag && ((ImageTag) tag).importSupported()) {
                                replaceImageSelectionMenuItem.setVisible(true);
                            }
                            if (tag instanceof DefineBinaryDataTag) {
                                replaceBinarySelectionMenuItem.setVisible(true);
                            }
                        }

                        if (tagObj instanceof SWFRoot) {
                            closeSelectionMenuItem.setVisible(true);
                        }
                    }
                    
                    removeMenuItem.setVisible(allSelectedIsTag);
                    contextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        JLabel welcomeToLabel = new JLabel(translate("startup.welcometo"));
        welcomeToLabel.setFont(welcomeToLabel.getFont().deriveFont(40));
        welcomeToLabel.setAlignmentX(0.5f);
        JPanel appNamePanel = new JPanel(new FlowLayout());
        JLabel jpLabel = new JLabel("JPEXS ");
        jpLabel.setAlignmentX(0.5f);
        jpLabel.setForeground(new Color(0, 0, 160));
        jpLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        jpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(jpLabel);

        JLabel ffLabel = new JLabel("Free Flash ");
        ffLabel.setAlignmentX(0.5f);
        ffLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        ffLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(ffLabel);

        JLabel decLabel = new JLabel("Decompiler");
        decLabel.setAlignmentX(0.5f);
        decLabel.setForeground(Color.red);
        decLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(decLabel);
        appNamePanel.setAlignmentX(0.5f);
        welcomePanel.add(Box.createGlue());
        welcomePanel.add(welcomeToLabel);
        welcomePanel.add(appNamePanel);
        JLabel startLabel = new JLabel(translate("startup.selectopen"));
        startLabel.setAlignmentX(0.5f);
        startLabel.setFont(startLabel.getFont().deriveFont(30));
        welcomePanel.add(startLabel);
        welcomePanel.add(Box.createGlue());
        return welcomePanel;
    }

    private JPanel createImagesCard() {
        JPanel imagesCard = new JPanel(new BorderLayout());
        imagePanel = new ImagePanel();
        imagesCard.add(imagePanel, BorderLayout.CENTER);

        imageReplaceButton = new JButton(translate("button.replace"), View.getIcon("edit16"));
        imageReplaceButton.setMargin(new Insets(3, 3, 3, 10));
        imageReplaceButton.setActionCommand(ACTION_REPLACE_IMAGE);
        imageReplaceButton.addActionListener(this);
        imageButtonsPanel = new JPanel(new FlowLayout());
        imageButtonsPanel.add(imageReplaceButton);

        imagesCard.add(imageButtonsPanel, BorderLayout.SOUTH);
        return imagesCard;
    }
    
    private JPanel createBinaryCard() {
        JPanel binaryCard = new JPanel(new BorderLayout());
        binaryPanel = new BinaryPanel();
        binaryCard.add(binaryPanel, BorderLayout.CENTER);

        binaryReplaceButton = new JButton(translate("button.replace"), View.getIcon("edit16"));
        binaryReplaceButton.setMargin(new Insets(3, 3, 3, 10));
        binaryReplaceButton.setActionCommand(ACTION_REPLACE_BINARY);
        binaryReplaceButton.addActionListener(this);
        binaryButtonsPanel = new JPanel(new FlowLayout());
        binaryButtonsPanel.add(binaryReplaceButton);

        binaryCard.add(binaryButtonsPanel, BorderLayout.SOUTH);
        return binaryCard;
    }
    
    private void showHideImageReplaceButton(boolean show) {
        imageReplaceButton.setVisible(show);
        setImageButtonPanelVisibility();
    }
    
    private void setImageButtonPanelVisibility() {
        // hide button panel when no button is visible
        // now there is only one button, later add here the other buttons
        boolean visible = imageReplaceButton.isVisible();
        imageButtonsPanel.setVisible(visible);
    }
    
    public String translate(String key) {
        return mainFrame.translate(key);
    }

    public MainPanel(MainFrame mainFrame, MainFrameMenu mainMenu, FlashPlayerPanel flashPanel) {
        super();

        this.mainFrame = mainFrame;
        this.mainMenu = mainMenu;
        this.flashPanel = flashPanel;
        
        mainFrame.setTitle(ApplicationInfo.applicationVerName);
        
        setLayout(new BorderLayout());
        swfs = new ArrayList<>();

        detailPanel = new JPanel();
        detailPanel.setLayout(new CardLayout());
        JPanel whitePanel = new JPanel();
        whitePanel.setBackground(Color.white);
        detailPanel.add(whitePanel, DETAILCARDEMPTYPANEL);
        CardLayout cl2 = (CardLayout) (detailPanel.getLayout());
        cl2.show(detailPanel, DETAILCARDEMPTYPANEL);

        UIManager.getDefaults().put("TreeUI", BasicTreeUI.class.getName());
        tagTree = new TagTree((TagTreeModel) null);
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
                            File fTempDir = new File(tempDir);
                            if (!fTempDir.exists()) {
                                if (!fTempDir.mkdirs()) {
                                    if (!fTempDir.exists()) {
                                        throw new IOException("cannot create directory " + fTempDir);
                                    }
                                }
                            }
                            final ExportDialog export = new ExportDialog();

                            try {
                                File ftemp = new File(tempDir);
                                files = exportSelection(errorHandler, tempDir, export);
                                files.clear();

                                File[] fs = ftemp.listFiles();
                                files.addAll(Arrays.asList(fs));

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

        createContextMenu();
        
        statusPanel = new MainFrameStatusPanel(this);
        add(statusPanel, BorderLayout.SOUTH);

        JPanel textTopPanel = new JPanel(new BorderLayout());
        textValue = new LineMarkedEditorPane();
        textTopPanel.add(new JScrollPane(textValue), BorderLayout.CENTER);
        textValue.setEditable(false);
        //textValue.setFont(UIManager.getFont("TextField.font"));



        JPanel textButtonsPanel = new JPanel();
        textButtonsPanel.setLayout(new FlowLayout());


        textSaveButton = new JButton(translate("button.save"), View.getIcon("save16"));
        textSaveButton.setMargin(new Insets(3, 3, 3, 10));
        textSaveButton.setActionCommand(ACTION_SAVE_TEXT);
        textSaveButton.addActionListener(this);

        textEditButton = new JButton(translate("button.edit"), View.getIcon("edit16"));
        textEditButton.setMargin(new Insets(3, 3, 3, 10));
        textEditButton.setActionCommand(ACTION_EDIT_TEXT);
        textEditButton.addActionListener(this);

        textCancelButton = new JButton(translate("button.cancel"), View.getIcon("cancel16"));
        textCancelButton.setMargin(new Insets(3, 3, 3, 10));
        textCancelButton.setActionCommand(ACTION_CANCEL_TEXT);
        textCancelButton.addActionListener(this);

        textButtonsPanel.add(textEditButton);
        textButtonsPanel.add(textSaveButton);
        textButtonsPanel.add(textCancelButton);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        textTopPanel.add(textButtonsPanel, BorderLayout.SOUTH);

        displayWithPreview = new JPanel(new CardLayout());

        displayWithPreview.add(textTopPanel, CARDTEXTPANEL);

        fontPanel = new FontPanel(this);
        displayWithPreview.add(fontPanel, CARDFONTPANEL);


        Component leftComponent;


        displayPanel = new JPanel(new CardLayout());

        if (flashPanel != null) {
            JPanel flashPlayPanel = new JPanel(new BorderLayout());
            flashPlayPanel.add(flashPanel, BorderLayout.CENTER);
            flashPlayPanel.add(flashControls = new PlayerControls(flashPanel), BorderLayout.SOUTH);
            leftComponent = flashPlayPanel;
        } else {
            JPanel swtPanel = new JPanel(new BorderLayout());
            swtPanel.add(new JLabel("<html><center>" + translate("notavailonthisplatform") + "</center></html>", JLabel.CENTER), BorderLayout.CENTER);
            swtPanel.setBackground(View.DEFAULT_BACKGROUND_COLOR);
            leftComponent = swtPanel;
        }

        previewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        previewSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (splitsInited) {
                    Configuration.guiPreviewSplitPaneDividerLocation.set((int) pce.getNewValue());
                }
            }
        });
        
        JPanel pan = new JPanel(new BorderLayout());
        JLabel prevLabel = new HeaderLabel(translate("swfpreview"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        JLabel paramsLabel = new HeaderLabel(translate("parameters"));
        paramsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //paramsLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        pan.add(prevLabel, BorderLayout.NORTH);

        viewerCards = new JPanel();
        viewerCards.setLayout(new CardLayout());
        //viewerCards.add(leftComponent,FLASH_VIEWER_CARD);

        internelViewerPanel = new ImagePanel();
        JPanel ivPanel = new JPanel(new BorderLayout());
        ivPanel.add(new HeaderLabel(translate("swfpreview.internal")), BorderLayout.NORTH);
        ivPanel.add(internelViewerPanel, BorderLayout.CENTER);
        viewerCards.add(ivPanel, INTERNAL_VIEWER_CARD);

        ((CardLayout) viewerCards.getLayout()).show(viewerCards, FLASH_VIEWER_CARD);

        if (flashPanel != null) {
            JPanel bottomPanel = new JPanel(new BorderLayout());
            JPanel buttonsPanel = new JPanel(new FlowLayout());
            JButton selectColorButton = new JButton(View.getIcon("color16"));
            selectColorButton.addActionListener(this);
            selectColorButton.setActionCommand(ACTION_SELECT_COLOR);
            selectColorButton.setToolTipText(AppStrings.translate("button.selectcolor.hint"));
            buttonsPanel.add(selectColorButton);
            bottomPanel.add(buttonsPanel, BorderLayout.EAST);
            pan.add(bottomPanel, BorderLayout.SOUTH);
        }
        pan.add(leftComponent, BorderLayout.CENTER);
        viewerCards.add(pan, FLASH_VIEWER_CARD);
        previewSplitPane.setLeftComponent(viewerCards);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(paramsLabel, BorderLayout.NORTH);
        parametersPanel.add(displayWithPreview, BorderLayout.CENTER);
        previewSplitPane.setRightComponent(parametersPanel);
        parametersPanel.setVisible(false);
        displayPanel.add(previewSplitPane, CARDFLASHPANEL);

        displayPanel.add(createImagesCard(), CARDIMAGEPANEL);
        displayPanel.add(createBinaryCard(), CARDBINARYPANEL);

        JPanel shapesCard = new JPanel(new BorderLayout());

        JPanel previewPanel = new JPanel(new BorderLayout());

        previewImagePanel = new ImagePanel();

        JPanel previewCnt = new JPanel(new BorderLayout());
        previewCnt.add(previewImagePanel, BorderLayout.CENTER);
        previewCnt.add(new PlayerControls(previewImagePanel), BorderLayout.SOUTH);
        previewPanel.add(previewCnt, BorderLayout.CENTER);
        JLabel prevIntLabel = new HeaderLabel(translate("swfpreview.internal"));
        prevIntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevIntLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        previewPanel.add(prevIntLabel, BorderLayout.NORTH);
        shapesCard.add(previewPanel, BorderLayout.CENTER);
        displayPanel.add(shapesCard, CARDDRAWPREVIEWPANEL);

        swfPreviewPanel = new SWFPreviwPanel();
        displayPanel.add(swfPreviewPanel, CARDSWFPREVIEWPANEL);


        displayPanel.add(new JPanel(), CARDEMPTYPANEL);
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


        welcomePanel = createWelcomePanel();
        add(welcomePanel, BorderLayout.CENTER);

        splitPane1.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (splitsInited) {
                    Configuration.guiSplitPane1DividerLocation.set((int) pce.getNewValue());
                }
            }
        });

        splitPane2.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (detailPanel.isVisible()) {
                    Configuration.guiSplitPane2DividerLocation.set((int) pce.getNewValue());
                }
            }
        });

        CardLayout cl3 = new CardLayout();
        contentPanel = new JPanel(cl3);
        contentPanel.add(welcomePanel, WELCOME_PANEL);
        contentPanel.add(splitPane1, SPLIT_PANE1);
        add(contentPanel);
        cl3.show(contentPanel, WELCOME_PANEL);
        
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

        updateUi();
        
        //Opening files with drag&drop to main window
        enableDrop(true);
    }
    
    public void load(SWF swf, boolean first) {
        List<ContainerItem> objs = new ArrayList<>();
        objs.addAll(swf.tags);

        ArrayList<ABCContainerTag> abcList = new ArrayList<>();
        getActionScript3(objs, abcList);

        swfs.add(swf);
        swf.abcList = abcList;
        tagTree.setModel(new TagTreeModel(mainFrame, swfs));
        
        boolean hasAbc = !abcList.isEmpty();

        if (hasAbc) {
            if (abcPanel == null) {
                abcPanel = new ABCPanel(this);
                displayPanel.add(abcPanel, CARDACTIONSCRIPT3PANEL);
                detailPanel.add(abcPanel.tabbedPane, DETAILCARDAS3NAVIGATOR);
            }
            abcPanel.setSwf(swf);
        } else {
            if (actionPanel == null) {
                actionPanel = new ActionPanel(this);
                displayPanel.add(actionPanel, CARDACTIONSCRIPTPANEL);
            }
        }

        expandSwfRoots();

        for (Tag t : swf.tags) {
            if (t instanceof JPEGTablesTag) {
                swf.jtt = (JPEGTablesTag) t;
            }
        }

        List<ContainerItem> list2 = new ArrayList<>();
        list2.addAll(swf.tags);
        swf.characters = new HashMap<>();
        parseCharacters(swf, list2);

        if (Configuration.autoRenameIdentifiers.get()) {
            try {
                swf.deobfuscateIdentifiers(RenameType.TYPENUMBER);
                swf.assignClassesToSymbols();
                clearCache();
                if (abcPanel != null) {
                    abcPanel.reload();
                }
                updateClassesList();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        showDetail(DETAILCARDEMPTYPANEL);
        showCard(CARDEMPTYPANEL);
        updateUi(swf);

        if (first && Configuration.gotoMainClassOnStartup.get()) {
            gotoDocumentClass(swf);
        }
    }

    private void updateUi(final SWF swf) {

        mainFrame.setTitle(ApplicationInfo.applicationVerName + (Configuration.displayFileName.get() ? " - " + swf.getFileTitle() : ""));

        List<ABCContainerTag> abcList = swf.abcList;
        
        boolean hasAbc = !abcList.isEmpty();

        if (hasAbc) {
            abcPanel.setSwf(swf);
        }

        if (isWelcomeScreen) {
            CardLayout cl = (CardLayout) (contentPanel.getLayout());
            cl.show(contentPanel, SPLIT_PANE1);
            isWelcomeScreen = false;
        }
        
        mainMenu.updateComponets(swf, abcList);
    }
    
    private void updateUi() {
        if (!isWelcomeScreen) {
            CardLayout cl = (CardLayout) (contentPanel.getLayout());
            cl.show(contentPanel, WELCOME_PANEL);
            isWelcomeScreen = true;
        }
        
        if (swfs.isEmpty()) {
            mainMenu.updateComponets(null, null);
        } else {
            SWF swf = swfs.get(0);
            updateUi(swf);
        }
    }
    
    public void closeAll() {
        swfs.clear();
        oldNode = null;
        oldTag = null;
        if (abcPanel != null) {
            abcPanel.clearSwf();
        }
        if (actionPanel != null) {
            actionPanel.clearSource();
        }
        updateUi();
        updateTagTree();
    }

    public void close(SWF swf) {
        swfs.remove(swf);
        if (abcPanel != null && abcPanel.swf == swf) {
            abcPanel.clearSwf();
        }
        if (actionPanel != null) {
            actionPanel.clearSource();
        }
        oldNode = null;
        oldTag = null;
        updateUi();
        updateTagTree();
    }
    
    private void updateTagTree() {
        tagTree.setModel(new TagTreeModel(mainFrame, swfs));
        expandSwfRoots();
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
                            String path = droppedFiles.get(0).getAbsolutePath();
                            Main.openFile(path, null);
                        }
                    } catch (UnsupportedFlavorException | IOException ex) {
                    }
                }
            });
        } else {
            setDropTarget(null);
        }
    }

    public void updateClassesList() {
        List<TagNode> nodes = getASTagNode(tagTree);
        boolean updateNeeded = false;
        for (TagNode n : nodes) {
            if (n.tag instanceof ClassesListTreeModel) {
                ((ClassesListTreeModel) n.tag).update();
                updateNeeded = true;
            }
        }
    
        refreshTree();
        
        if (updateNeeded) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    tagTree.updateUI();
                }
            });
        }
    }

    public void doFilter() {
        List<TagNode> nodes = getASTagNode(tagTree);
        boolean updateNeeded = false;
        for (TagNode n : nodes) {
            if (n.tag instanceof ClassesListTreeModel) {
                ((ClassesListTreeModel) n.tag).setFilter(filterField.getText());
                updateNeeded = true;
            }
        }
        
        if (updateNeeded) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    tagTree.updateUI();
                }
            });
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

            final MainPanel t = this;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    splitPane1.setDividerLocation(Configuration.guiSplitPane1DividerLocation.get(getWidth() / 3));
                    int confDivLoc = Configuration.guiSplitPane2DividerLocation.get(splitPane2.getHeight() * 3 / 5);
                    if (confDivLoc > splitPane2.getHeight() - 10) { //In older releases, divider location was saved when detailPanel was invisible too
                        confDivLoc = splitPane2.getHeight() * 3 / 5;
                    }
                    splitPane2.setDividerLocation(confDivLoc);
                    previewSplitPane.setDividerLocation(Configuration.guiPreviewSplitPaneDividerLocation.get(previewSplitPane.getWidth() / 2));

                    splitPos = splitPane2.getDividerLocation();
                    splitsInited = true;
                }
            });


        }
    }

    private void parseCharacters(SWF swf, List<ContainerItem> list) {
        for (ContainerItem t : list) {
            if (t instanceof CharacterTag) {
                swf.characters.put(((CharacterTag) t).getCharacterId(), (CharacterTag) t);
            }
            if (t instanceof Container) {
                parseCharacters(swf, ((Container) t).getSubItems());
            }
        }
    }

    public static void getShapes(List<ContainerItem> list, List<Tag> shapes) {
        for (ContainerItem t : list) {
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

    public static void getFonts(List<ContainerItem> list, List<Tag> fonts) {
        for (ContainerItem t : list) {
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

    public static void getActionScript3(List<ContainerItem> list, List<ABCContainerTag> actionScripts) {
        for (ContainerItem t : list) {
            if (t instanceof Container) {
                getActionScript3(((Container) t).getSubItems(), actionScripts);
            }
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    public static void getMorphShapes(List<ContainerItem> list, List<Tag> morphShapes) {
        for (ContainerItem t : list) {
            if (t instanceof Container) {
                getMorphShapes(((Container) t).getSubItems(), morphShapes);
            }
            if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
                morphShapes.add((Tag) t);
            }
        }
    }

    public static void getImages(List<ContainerItem> list, List<Tag> images) {
        for (ContainerItem t : list) {
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

    public static void getTexts(List<ContainerItem> list, List<Tag> texts) {
        for (ContainerItem t : list) {
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

    public static void getSprites(List<ContainerItem> list, List<Tag> sprites) {
        for (ContainerItem t : list) {
            if (t instanceof Container) {
                getSprites(((Container) t).getSubItems(), sprites);
            }
            if (t instanceof DefineSpriteTag) {
                sprites.add((Tag) t);
            }
        }
    }

    public static void getButtons(List<ContainerItem> list, List<Tag> buttons) {
        for (ContainerItem t : list) {
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
        TreePath[] tps = tagTree.getSelectionPaths();
        if (tps == null) {
            return ret;
        }
        for (TreePath tp : tps) {
            TagNode te = (TagNode) tp.getLastPathComponent();
            ret.add(te);
        }
        return ret;
    }

    public void renameIdentifier(SWF swf, String identifier) throws InterruptedException {
        String oldName = identifier;
        String newName = View.showInputDialog(translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                swf.renameAS2Identifier(oldName, newName);
                View.showMessageDialog(null, translate("rename.finished.identifier"));
                updateClassesList();
                reload(true);
            }
        }
    }

    public void renameMultiname(List<ABCContainerTag> abcList, int multiNameIndex) {
        String oldName = "";
        if (abcPanel.abc.constants.getMultiname(multiNameIndex).name_index > 0) {
            oldName = abcPanel.abc.constants.getString(abcPanel.abc.constants.getMultiname(multiNameIndex).name_index);
        }
        String newName = View.showInputDialog(translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                int mulCount = 0;
                for (ABCContainerTag cnt : abcList) {
                    ABC abc = cnt.getABC();
                    for (int m = 1; m < abc.constants.getMultinameCount(); m++) {
                        int ni = abc.constants.getMultiname(m).name_index;
                        String n = "";
                        if (ni > 0) {
                            n = abc.constants.getString(ni);
                        }
                        if (n.equals(oldName)) {
                            abc.renameMultiname(m, newName);
                            mulCount++;
                        }
                    }
                }
                View.showMessageDialog(null, translate("rename.finished.multiname").replace("%count%", "" + mulCount));
                if (abcPanel != null) {
                    abcPanel.reload();
                }
                updateClassesList();
                reload(true);
                abcPanel.hilightScript(abcPanel.swf, abcPanel.decompiledTextArea.getScriptLeaf().getPath().toString());
            }
        }
    }

    public List<Object> getSelected(JTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<Object> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            Object o = tp.getLastPathComponent();
            ret.add(o);
        }
        return ret;
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
        TreePath[] tps = tsm.getSelectionPaths();
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

    public List<TagNode> getASTagNode(JTree tree) {
        List<TagNode> result = new ArrayList<>();
        TagTreeModel tm = (TagTreeModel) tree.getModel();
        if (tm == null) {
            return result;
        }
        TreeNode root = tm.getRoot();
        for (int j = 0; j < tm.getChildCount(root); j++) {
            SWFRoot swfRoot = (SWFRoot) tm.getChild(root, j);
                for (int i = 0; i < tm.getChildCount(swfRoot); i++) {
                TreeNode node = tm.getChild(swfRoot, i);
                if (node instanceof TagNode) {
                    TagNode tagNode = (TagNode) node;
                    TreeElementItem tag = tagNode.tag;
                    if (tag != null && "scripts".equals(tagNode.mark)) {
                        result.add((TagNode) node);
                    } 
                }
            }
        }
        return result;
    }

    public boolean confirmExperimental() {
        return View.showConfirmDialog(null, translate("message.confirm.experimental"), translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }
    private SearchDialog searchDialog;

    public List<File> exportSelection(AbortRetryIgnoreHandler handler, String selFile, ExportDialog export) throws IOException {
        final ExportMode exportMode = ExportMode.get(export.getOption(ExportDialog.OPTION_ACTIONSCRIPT));
        final boolean isMp3OrWav = export.getOption(ExportDialog.OPTION_SOUNDS) == 0;
        final boolean isFormatted = export.getOption(ExportDialog.OPTION_TEXTS) == 1;
        
        List<File> ret = new ArrayList<>();
        List<Object> sel = getAllSelected(tagTree);

        for (SWF swf : swfs) {
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
                    if (n.getSwf() != swf) {
                        continue;
                    }
                    if (TagTree.getTagType(n.tag) == TagType.IMAGE) {
                        images.add((Tag) n.tag);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.SHAPE) {
                        shapes.add((Tag) n.tag);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.AS) {
                        actionNodes.add(n);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.MOVIE) {
                        movies.add((Tag) n.tag);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.SOUND) {
                        sounds.add((Tag) n.tag);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.BINARY_DATA) {
                        binaryData.add((Tag) n.tag);
                    }
                    if (TagTree.getTagType(n.tag) == TagType.TEXT) {
                        texts.add((Tag) n.tag);
                    }
                }
                if (d instanceof TreeElement) {
                    if (((TreeElement) d).isLeaf()) {
                        TreeElement treeElement = (TreeElement) d;
                        if (treeElement.getSwf() == swf) {
                            tlsList.add((ScriptPack) treeElement.getItem());
                        }
                    }
                }
            }
            ret.addAll(swf.exportImages(handler, selFile + File.separator + "images", images));
            ret.addAll(SWF.exportShapes(handler, selFile + File.separator + "shapes", shapes));
            ret.addAll(swf.exportTexts(handler, selFile + File.separator + "texts", texts, isFormatted));
            ret.addAll(swf.exportMovies(handler, selFile + File.separator + "movies", movies));
            ret.addAll(swf.exportSounds(handler, selFile + File.separator + "sounds", sounds, isMp3OrWav, isMp3OrWav));
            ret.addAll(SWF.exportBinaryData(handler, selFile + File.separator + "binaryData", binaryData));
            List<ABCContainerTag> abcList = swf.abcList;
            if (abcPanel != null) {
                for (int i = 0; i < tlsList.size(); i++) {
                    ScriptPack tls = tlsList.get(i);
                    Main.startWork(translate("work.exporting") + " " + (i + 1) + "/" + tlsList.size() + " " + tls.getPath() + " ...");
                    ret.add(tls.export(selFile, abcList, exportMode, Configuration.parallelSpeedUp.get()));
                }
            } else {
                List<TagNode> allNodes = new ArrayList<>();
                List<TagNode> asNodes = getASTagNode(tagTree);
                for (TagNode asn : asNodes) {
                    allNodes.add(asn);
                    TagNode.setExport(allNodes, false);
                    TagNode.setExport(actionNodes, true);
                    ret.addAll(TagNode.exportNodeAS(handler, allNodes, selFile, exportMode, null));
                }
            }
        }
        return ret;
    }

    public SWF getCurrentSwf() {
        if (swfs == null || swfs.isEmpty()) {
            return null;
        }
        
        TreeNode treeNode = (TreeNode) tagTree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return swfs.get(0);
        }
        
        return treeNode.getSwf();
    }
    
    private void clearCache() {
        if (abcPanel != null) {
            abcPanel.decompiledTextArea.clearScriptCache();
        }
        if (actionPanel != null) {
            actionPanel.clearCache();
        }
    }

    public void gotoDocumentClass(SWF swf) {
        if (swf == null) {
            return;
        }
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
            showCard(CARDACTIONSCRIPT3PANEL);
            abcPanel.setSwf(swf);
            abcPanel.hilightScript(swf, documentClass);
        }
    }

    public void disableDecompilationChanged() {
        clearCache();
        if (abcPanel != null) {
            abcPanel.reload();
        }
        reload(true);
        updateClassesList();
    }
    
    public void searchAs() {
        if (searchDialog == null) {
            searchDialog = new SearchDialog();
        }
        searchDialog.setVisible(true);
        if (searchDialog.result) {
            final String txt = searchDialog.searchField.getText();
            if (!txt.isEmpty()) {
                if (abcPanel != null) {
                    new CancellableWorker() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            if (abcPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                View.execInEventDispatch(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDetail(DETAILCARDAS3NAVIGATOR);
                                        showCard(CARDACTIONSCRIPT3PANEL);
                                    }
                                });
                            } else {
                                View.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                            }
                            return null;
                        }
                    }.execute();
                } else {
                    new CancellableWorker() {
                        @Override
                        protected Void doInBackground() {
                            if (actionPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                View.execInEventDispatch(new Runnable() {
                                    @Override
                                    public void run() {
                                        showCard(CARDACTIONSCRIPTPANEL);
                                    }
                                });
                            } else {
                                View.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }
    
    public void autoDeobfuscateChanged() {
        clearCache();
        if (abcPanel != null) {
            abcPanel.reload();
        }
        reload(true);
        updateClassesList();
    }
    
    public void renameOneIdentifier(final SWF swf) {
        if (swf.fileAttributes.actionScript3) {
            final int multiName = abcPanel.decompiledTextArea.getMultinameUnderCursor();
            final List<ABCContainerTag> abcList = swf.abcList;
            if (multiName > 0) {
                new CancellableWorker() {
                    @Override
                    public Void doInBackground() throws Exception {
                        Main.startWork(translate("work.renaming") + "...");
                        renameMultiname(abcList, multiName);
                        return null;
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();
                    }
                }.execute();

            } else {
                View.showMessageDialog(null, translate("message.rename.notfound.multiname"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            final String identifier = actionPanel.getStringUnderCursor();
            if (identifier != null) {
                new CancellableWorker() {
                    @Override
                    public Void doInBackground() throws Exception {
                        Main.startWork(translate("work.renaming") + "...");
                        try {
                            renameIdentifier(swf, identifier);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();
                    }
                }.execute();
            } else {
                View.showMessageDialog(null, translate("message.rename.notfound.identifier"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    public void exportFla(final SWF swf) {
        JFileChooser fc = new JFileChooser();
        String selDir = Configuration.lastOpenDir.get();
        fc.setCurrentDirectory(new File(selDir));
        if (!selDir.endsWith(File.separator)) {
            selDir += File.separator;
        }
        String fileName = (new File(swf.file).getName());
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
            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File sf = Helper.fixDialogFile(fc.getSelectedFile());

            Main.startWork(translate("work.exporting.fla") + "...");
            final boolean compressed = fc.getFileFilter() == fla;
            if (!compressed) {
                if (sf.getName().endsWith(".fla")) {
                    sf = new File(sf.getAbsolutePath().substring(0, sf.getAbsolutePath().length() - 4) + ".xfl");
                }
            }
            final File selfile = sf;
            new CancellableWorker() {
                @Override
                protected Void doInBackground() throws Exception {
                    Helper.freeMem();
                    try {
                        if (compressed) {
                            swf.exportFla(errorHandler, selfile.getAbsolutePath(), new File(swf.file).getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get());
                        } else {
                            swf.exportXfl(errorHandler, selfile.getAbsolutePath(), new File(swf.file).getName(), ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get());
                        }
                    } catch (IOException ex) {
                        View.showMessageDialog(null, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                    Helper.freeMem();
                    return null;
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    if (Configuration.openFolderAfterFlaExport.get()) {
                        try {
                            Desktop.getDesktop().open(selfile.getAbsoluteFile().getParentFile());
                        } catch (IOException ex) {
                            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }.execute();
        }
    }
    
    public void export(final boolean onlySel) {
        final ExportDialog export = new ExportDialog();
        export.setVisible(true);
        if (!export.cancelled) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
            chooser.setDialogTitle(translate("export.select.directory"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                final long timeBefore = System.currentTimeMillis();
                Main.startWork(translate("work.exporting") + "...");
                final String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
                Configuration.lastExportDir.set(Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath());
                final ExportMode exportMode = ExportMode.get(export.getOption(ExportDialog.OPTION_ACTIONSCRIPT));
                final boolean isMp3OrWav = export.getOption(ExportDialog.OPTION_SOUNDS) == 0;
                final boolean isFormatted = export.getOption(ExportDialog.OPTION_TEXTS) == 1;
                final SWF swf = getCurrentSwf();
                new CancellableWorker() {
                    @Override
                    public Void doInBackground() throws Exception {
                        try {
                            if (onlySel) {
                                exportSelection(errorHandler, selFile, export);
                            } else {
                                swf.exportImages(errorHandler, selFile + File.separator + "images");
                                swf.exportShapes(errorHandler, selFile + File.separator + "shapes");
                                swf.exportTexts(errorHandler, selFile + File.separator + "texts", isFormatted);
                                swf.exportMovies(errorHandler, selFile + File.separator + "movies");
                                swf.exportSounds(errorHandler, selFile + File.separator + "sounds", isMp3OrWav, isMp3OrWav);
                                swf.exportBinaryData(errorHandler, selFile + File.separator + "binaryData");
                                swf.exportActionScript(errorHandler, selFile, exportMode, Configuration.parallelSpeedUp.get());
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, "Error during export", ex);
                            View.showMessageDialog(null, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();
                        long timeAfter = System.currentTimeMillis();
                        final long timeMs = timeAfter - timeBefore;

                        View.execInEventDispatchLater(new Runnable() {

                            @Override
                            public void run() {
                                setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                            }
                        });
                    }
                }.execute();

            }
        }
    }
    
    public void restoreControlFlow(final boolean all) {
        Main.startWork(translate("work.restoringControlFlow"));
        if ((!all) || confirmExperimental()) {
            new CancellableWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    int cnt = 0;
                    if (all) {
                        for (ABCContainerTag tag : abcPanel.swf.abcList) {
                            tag.getABC().restoreControlFlow();
                        }
                    } else {
                        int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                        if (bi != -1) {
                            abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants, abcPanel.decompiledTextArea.getCurrentTrait(), abcPanel.abc.method_info[abcPanel.abc.bodies[bi].method_info]);
                        }
                        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc, abcPanel.decompiledTextArea.getCurrentTrait());
                    }
                    return true;
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    View.showMessageDialog(null, translate("work.restoringControlFlow.complete"));

                    View.execInEventDispatch(new Runnable() {

                        @Override
                        public void run() {
                            abcPanel.reload();
                            updateClassesList();
                        }
                    });
                }
            }.execute();
        }
    }
    
    public void renameIdentifiers(final SWF swf) {
        if (confirmExperimental()) {
            final RenameType renameType = new RenameDialog().display();
            if (renameType != null) {
                Main.startWork(translate("work.renaming.identifiers") + "...");
                new CancellableWorker<Integer>() {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        int cnt = swf.deobfuscateIdentifiers(renameType);
                        return cnt;
                    }

                    @Override
                    protected void done() {
                        View.execInEventDispatch(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    int cnt = get();
                                    Main.stopWork();
                                    View.showMessageDialog(null, translate("message.rename.renamed").replace("%count%", "" + cnt));
                                    swf.assignClassesToSymbols();
                                    clearCache();
                                    if (abcPanel != null) {
                                        abcPanel.reload();
                                    }
                                    updateClassesList();
                                    reload(true);
                                } catch (Exception ex) {
                                    Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, "Error during renaming identifiers", ex);
                                    Main.stopWork();
                                    View.showMessageDialog(null, translate("error.occured").replace("%error%", ex.getClass().getSimpleName()));
                                }
                            }
                        });
                    }
                }.execute();
            }
        }
    }
    
    public void deobfuscate() {
        if (deobfuscationDialog == null) {
            deobfuscationDialog = new DeobfuscationDialog();
        }
        deobfuscationDialog.setVisible(true);
        if (deobfuscationDialog.ok) {
            Main.startWork(translate("work.deobfuscating") + "...");
            new CancellableWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        if (deobfuscationDialog.processAllCheckbox.isSelected()) {
                            for (ABCContainerTag tag : abcPanel.swf.abcList) {
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
                            Trait t = abcPanel.decompiledTextArea.getCurrentTrait();
                            if (bi != -1) {
                                if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_DEAD_CODE) {
                                    abcPanel.abc.bodies[bi].removeDeadCode(abcPanel.abc.constants, t, abcPanel.abc.method_info[abcPanel.abc.bodies[bi].method_info]);
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_REMOVE_TRAPS) {
                                    abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, t, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic(), ""/*FIXME*/);
                                } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                                    abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, t, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic(), ""/*FIXME*/);
                                    abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants, t, abcPanel.abc.method_info[abcPanel.abc.bodies[bi].method_info]);
                                }
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc, t);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, "Deobfuscation error", ex);
                    }
                    return true;
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    View.showMessageDialog(null, translate("work.deobfuscating.complete"));

                    View.execInEventDispatch(new Runnable() {

                        @Override
                        public void run() {
                            clearCache();
                            abcPanel.reload();
                            updateClassesList();
                        }
                    });
                }
            }.execute();
        }
    }
    
    public void removeNonScripts(SWF swf) {
        List<Tag> tags = new ArrayList<>(swf.tags);
        for (Tag tag : tags) {
            System.out.println(tag.getClass());
            if (!(tag instanceof ABCContainerTag || tag instanceof ASMSource)) {
                swf.removeTag(tag);
            }
        }
        showCard(CARDEMPTYPANEL);
        refreshTree();
    }
    
    public void refreshDecompiled() {
        clearCache();
        if (abcPanel != null) {
            abcPanel.reload();
        }
        reload(true);
        updateClassesList();
    }
    
    public boolean saveText(TextTag textTag, String text) {
        try {
            if (textTag.setFormattedText(new MissingCharacterHandler() {
                @Override
                public boolean handle(FontTag font, List<Tag> tags, char character) {
                    String fontName = font.getSwf().sourceFontsMap.get(font.getFontId());
                    if (fontName == null) {
                        fontName = font.getFontName();
                    }
                    fontName = FontTag.findInstalledFontName(fontName);
                    Font f = new Font(fontName, font.getFontStyle(), 18);
                    if (!f.canDisplay(character)) {
                        View.showMessageDialog(null, translate("error.font.nocharacter").replace("%char%", "" + character), translate("error"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    font.addCharacter(tags, character, fontName);
                    return true;

                }
            }, textTag.getSwf().tags, text)) {
                return true;
            }
        } catch (ParseException ex) {
            View.showMessageDialog(null, translate("error.text.invalid").replace("%text%", ex.text).replace("%line%", "" + ex.line), translate("error"), JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_SELECT_COLOR:
                Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectcolor.title"), View.swfBackgroundColor);
                if (newColor != null) {
                    View.swfBackgroundColor = newColor;
                    reload(true);
                }
                break;
            case ACTION_REPLACE_IMAGE:
                {
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
                            fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
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
                                Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                                File selfile = Helper.fixDialogFile(fc.getSelectedFile());
                                byte[] data = Helper.readFile(selfile.getAbsolutePath());
                                try {
                                    it.setImage(data);
                                    it.getSwf().clearImageCache();
                                } catch (IOException ex) {
                                    Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, "Invalid image", ex);
                                    View.showMessageDialog(null, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
                                }
                                reload(true);
                            }
                        }
                    }
                }
                break;
            case ACTION_REPLACE_BINARY:
                {
                    Object tagObj = tagTree.getLastSelectedPathComponent();
                    if (tagObj == null) {
                        return;
                    }

                    if (tagObj instanceof TagNode) {
                        tagObj = ((TagNode) tagObj).tag;
                    }
                    if (tagObj instanceof DefineBinaryDataTag) {
                        DefineBinaryDataTag bt = (DefineBinaryDataTag) tagObj;
                        JFileChooser fc = new JFileChooser();
                        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
                        JFrame f = new JFrame();
                        View.setWindowIcon(f);
                        int returnVal = fc.showOpenDialog(f);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
                            byte[] data = Helper.readFile(selfile.getAbsolutePath());
                            bt.binaryData = data;
                            reload(true);
                        }
                    }
                }
                break;
            case ACTION_REMOVE_ITEM:
                List<Object> sel = getSelected(tagTree);

                List<Tag> tagsToRemove = new ArrayList<>();
                for (Object o : sel) {
                    Object tag = o;
                    if (o instanceof TagNode) {
                        tag = ((TagNode) o).tag;
                    }
                    if (tag instanceof Tag) {
                        tagsToRemove.add((Tag) tag);
                    }
                }

                if (tagsToRemove.size() == 1) {
                    Tag tag = tagsToRemove.get(0);
                    if (View.showConfirmDialog(this, translate("message.confirm.remove").replace("%item%", tag.toString()), translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        tag.getSwf().removeTag(tag);
                        showCard(CARDEMPTYPANEL);
                        refreshTree();
                    }
                } else if (tagsToRemove.size() > 1) {
                    if (View.showConfirmDialog(this, translate("message.confirm.removemultiple").replace("%count%", Integer.toString(tagsToRemove.size())), translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        for (Tag tag : tagsToRemove) {
                            tag.getSwf().removeTag(tag);
                        }
                        showCard(CARDEMPTYPANEL);
                        refreshTree();
                    }
                }
                break;
            case ACTION_EDIT_TEXT:
                setEditText(true);
                break;
            case ACTION_CANCEL_TEXT:
                setEditText(false);
                break;
            case ACTION_SAVE_TEXT:
                if (oldTag instanceof TextTag) {
                    TextTag textTag = (TextTag) oldTag;
                    if (saveText(textTag, textValue.getText())) {
                        setEditText(false);
                    }
                }
                break;
            case ACTION_CLOSE_SWF:
                {
                    Main.closeFile(getCurrentSwf());
                }
        }
        if (Main.isWorking()) {
            return;
        }

        switch (e.getActionCommand()) {

            case MainFrameRibbonMenu.ACTION_EXPORT_SEL:
                export(true);
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

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreeNode treeNode = (TreeNode) e.getPath().getLastPathComponent();
        SWF swf = treeNode.getSwf();
        if (swfs.contains(swf)) {
            updateUi(swf);
        } else {
            updateUi();
        }
        setEditText(false, false);
        reload(false);
    }

    private void stopFlashPlayer() {
        if (flashPanel != null) {
            if (!flashPanel.isStopped()) {
                flashPanel.stopSWF();
            }
        }
    }

    private static Tag classicTag(Tag t) {
        if (t instanceof DefineCompactedFont) {
            return ((DefineCompactedFont) t).toClassicFont();
        }
        return t;
    }

    public void reload(boolean forceReload) {
        TreeNode treeNode = (TreeNode) tagTree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return;
        }

        if (!forceReload && (treeNode == oldNode)) {
            return;
        }

        oldNode = treeNode;

        TreeElementItem  tagObj = null;
        if (treeNode instanceof TagNode) {
            tagObj = ((TagNode) treeNode).tag;
        }
        if (treeNode instanceof TreeElement) {
            tagObj = ((TreeElement) treeNode).getItem();
        }

        oldTag = tagObj;

        if (flashPanel != null) {
            flashPanel.specialPlayback = false;
        }
        swfPreviewPanel.stop();
        stopFlashPlayer();
        if (tagObj instanceof ScriptPack) {
            final ScriptPack scriptLeaf = (ScriptPack) tagObj;
            final List<ABCContainerTag> abcList = scriptLeaf.abc.swf.abcList;
            if (setSourceWorker != null) {
                setSourceWorker.cancel(true);
            }
            if (!Main.isWorking()) {
                Main.startWork(AppStrings.translate("work.decompiling") + "...");
                CancellableWorker worker = new CancellableWorker() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        int classIndex = -1;
                        for (Trait t : scriptLeaf.abc.script_info[scriptLeaf.scriptIndex].traits.traits) {
                            if (t instanceof TraitClass) {
                                classIndex = ((TraitClass) t).class_info;
                                break;
                            }
                        }
                        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.clear();
                        abcPanel.navigator.setABC(abcList, scriptLeaf.abc);
                        abcPanel.navigator.setClassIndex(classIndex, scriptLeaf.scriptIndex);
                        abcPanel.setAbc(scriptLeaf.abc);
                        abcPanel.decompiledTextArea.setScript(scriptLeaf, abcList);
                        abcPanel.decompiledTextArea.setClassIndex(classIndex);
                        abcPanel.decompiledTextArea.setNoTrait();
                        return null;
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();

                        View.execInEventDispatch(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    get();
                                } catch (CancellationException ex) {
                                    abcPanel.decompiledTextArea.setText("//" + AppStrings.translate("work.canceled"));
                                } catch (Exception ex) {
                                    abcPanel.decompiledTextArea.setText("//Decompilation error: " + ex);
                                }
                            }
                        });
                    }
                };
                worker.execute();
                setSourceWorker = worker;
                Main.startWork(translate("work.decompiling") + "...", worker);
            }
            
            showDetail(DETAILCARDAS3NAVIGATOR);
            showCard(CARDACTIONSCRIPT3PANEL);
            return;
        } else {
            showDetail(DETAILCARDEMPTYPANEL);
        }


        if (treeNode instanceof SWFRoot) {
            SWFRoot swfRoot = (SWFRoot) treeNode;
            SWF swf = swfRoot.getSwf();
            if (mainMenu.isInternalFlashViewerSelected()) {
                showCard(CARDSWFPREVIEWPANEL);
                swfPreviewPanel.load(swf);
                swfPreviewPanel.play();
            } else {
                showCard(CARDFLASHPANEL);
                parametersPanel.setVisible(false);
                if (flashPanel != null) {
                    Color backgroundColor = View.DEFAULT_BACKGROUND_COLOR;
                    for (Tag t : swf.tags) {
                        if (t instanceof SetBackgroundColorTag) {
                            backgroundColor = ((SetBackgroundColorTag) t).backgroundColor.toColor();
                            break;
                        }
                    }
                    ((CardLayout) viewerCards.getLayout()).show(viewerCards, FLASH_VIEWER_CARD);
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                    try {
                        tempFile = File.createTempFile("temp", ".swf");
                        tempFile.deleteOnExit();
                        swf.saveTo(new BufferedOutputStream(new FileOutputStream(tempFile)));
                        flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, swf.frameRate);
                    } catch (IOException iex) {
                        Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, "Cannot create tempfile", iex);
                    }
                }
            }

        } /*else if (tagObj instanceof DefineVideoStreamTag) {
         showCard(CARDEMPTYPANEL);
         } else if ((tagObj instanceof DefineSoundTag) || (tagObj instanceof SoundStreamHeadTag) || (tagObj instanceof SoundStreamHead2Tag)) {
         showCard(CARDEMPTYPANEL);
         } */ else if (tagObj instanceof DefineBinaryDataTag) {
            DefineBinaryDataTag binaryTag = (DefineBinaryDataTag) tagObj;
            showCard(CARDBINARYPANEL);
            binaryPanel.setBinaryData(binaryTag.binaryData);
        } else if (tagObj instanceof ASMSource) {
            showCard(CARDACTIONSCRIPTPANEL);
            actionPanel.setSource((ASMSource) tagObj, !forceReload);
        } else if (tagObj instanceof ImageTag) {
            ImageTag imageTag = (ImageTag) tagObj;
            showHideImageReplaceButton(imageTag.importSupported());
            showCard(CARDIMAGEPANEL);
            imagePanel.setImage(imageTag.getImage(imageTag.getSwf().tags));
        } else if ((tagObj instanceof DrawableTag) && (!(tagObj instanceof TextTag)) && (!(tagObj instanceof FontTag)) && (mainMenu.isInternalFlashViewerSelected())) {
            Tag tag = (Tag) tagObj;
            showCard(CARDDRAWPREVIEWPANEL);
            previewImagePanel.setDrawable((DrawableTag) tag, tag.getSwf(), tag.getSwf().characters, 50/*FIXME*/);
        } else if ((tagObj instanceof FontTag) && (mainMenu.isInternalFlashViewerSelected())) {
            Tag tag = (Tag) tagObj;
            showCard(CARDFLASHPANEL);
            previewImagePanel.setDrawable((DrawableTag) tag, tag.getSwf(), tag.getSwf().characters, 50/*FIXME*/);
            showFontTag((FontTag) tagObj);
        } else if (tagObj instanceof FrameNode && ((FrameNode) tagObj).isDisplayed() && (mainMenu.isInternalFlashViewerSelected())) {
            showCard(CARDDRAWPREVIEWPANEL);
            FrameNode fn = (FrameNode) tagObj;
            SWF swf = fn.getSwf();
            List<Tag> controlTags = swf.tags;
            int containerId = 0;
            RECT rect = swf.displayRect;
            int totalFrameCount = swf.frameCount;
            if (fn.getParent() instanceof DefineSpriteTag) {
                controlTags = ((DefineSpriteTag) fn.getParent()).subTags;
                containerId = ((DefineSpriteTag) fn.getParent()).spriteId;
                rect = ((DefineSpriteTag) fn.getParent()).getRect(swf.characters, new Stack<Integer>());
                totalFrameCount = ((DefineSpriteTag) fn.getParent()).frameCount;
            }
            previewImagePanel.setImage(SWF.frameToImage(containerId, fn.getFrame() - 1, swf.tags, controlTags, rect, totalFrameCount, new Stack<Integer>()));
        } else if (((tagObj instanceof FrameNode) && ((FrameNode) tagObj).isDisplayed()) || ((tagObj instanceof CharacterTag) || (tagObj instanceof FontTag)) && (tagObj instanceof Tag)) {
            ((CardLayout) viewerCards.getLayout()).show(viewerCards, FLASH_VIEWER_CARD);
            createAndShowTempSwf(tagObj);
            
            if (tagObj instanceof TextTag) {
                TextTag textTag = (TextTag) tagObj;
                parametersPanel.setVisible(true);
                showDetailWithPreview(CARDTEXTPANEL);
                textValue.setContentType("text/swf_text");
                textValue.setText(textTag.getFormattedText(textTag.getSwf().tags));
                textValue.setCaretPosition(0);
            } else if (tagObj instanceof FontTag) {
                showFontTag((FontTag) tagObj);
            } else {
                parametersPanel.setVisible(false);
            }

        } else {
            showCard(CARDEMPTYPANEL);
        }
    }
    
    private void createAndShowTempSwf(Object tagObj) {
        SWF swf;
        try {
            if (tempFile != null) {
                tempFile.delete();
            }
            tempFile = File.createTempFile("temp", ".swf");
            tempFile.deleteOnExit();

            Color backgroundColor = View.swfBackgroundColor;
            
            if (tagObj instanceof FontTag) { //Fonts are always black on white
                backgroundColor = View.DEFAULT_BACKGROUND_COLOR;
            }

            if (tagObj instanceof FrameNode) {
                FrameNode fn = (FrameNode) tagObj;
                swf = fn.getSwf();
                if (fn.getParent() == null) {
                    for (Tag t : swf.tags) {
                        if (t instanceof SetBackgroundColorTag) {
                            backgroundColor = ((SetBackgroundColorTag) t).backgroundColor.toColor();
                            break;
                        }
                    }
                }
            } else {
                Tag tag = (Tag) tagObj;
                swf = tag.getSwf();
            }

            int frameCount = 1;
            int frameRate = swf.frameRate;
            HashMap<Integer, VideoFrameTag> videoFrames = new HashMap<>();
            DefineVideoStreamTag vs = null;
            if (tagObj instanceof DefineVideoStreamTag) {
                vs = (DefineVideoStreamTag) tagObj;
                swf.populateVideoFrames(vs.getCharacterId(), swf.tags, videoFrames);
                frameCount = videoFrames.size();
            }

            List<SoundStreamBlockTag> soundFrames = new ArrayList<>();
            if (tagObj instanceof SoundStreamHeadTypeTag) {
                SWF.populateSoundStreamBlocks(swf.tags, (Tag) tagObj, soundFrames);
                frameCount = soundFrames.size();
            }

            if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                frameCount = 100;
                frameRate = 50;
            }

            if (tagObj instanceof DefineSoundTag) {
                frameCount = 1;
            }

            byte[] data;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                SWFOutputStream sos2 = new SWFOutputStream(baos, 10);
                int width = swf.displayRect.Xmax - swf.displayRect.Xmin;
                int height = swf.displayRect.Ymax - swf.displayRect.Ymin;
                sos2.writeRECT(swf.displayRect);
                sos2.writeUI8(0);
                sos2.writeUI8(frameRate);
                sos2.writeUI16(frameCount); //framecnt

                /*FileAttributesTag fa = new FileAttributesTag();
                 sos2.writeTag(fa);
                 */
                sos2.writeTag(new SetBackgroundColorTag(null, new RGB(backgroundColor)));

                if (tagObj instanceof FrameNode) {
                    FrameNode fn = (FrameNode) tagObj;
                    Object parent = fn.getParent();
                    List<ContainerItem> subs = new ArrayList<>();
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
                        Set<Integer> needed = t.getDeepNeededCharacters(swf.characters, new ArrayList<Integer>());
                        for (int n : needed) {
                            if (!doneCharacters.contains(n)) {
                                sos2.writeTag(classicTag(swf.characters.get(n)));
                                doneCharacters.add(n);
                            }
                        }
                        if (t instanceof CharacterTag) {
                            doneCharacters.add(((CharacterTag) t).getCharacterId());
                        }
                        sos2.writeTag(classicTag(t));

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
                                    RECT r = ((BoundedTag) parent).getRect(swf.characters, new Stack<Integer>());
                                    mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                                    mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
                                } else {
                                    mat.translateX += width / 2;
                                    mat.translateY += height / 2;
                                }
                                sos2.writeTag(new PlaceObject2Tag(null, false, false, false, false, false, true, false, true, depth, chid, mat, null, 0, null, 0, null));

                            }
                        }
                    }
                    sos2.writeTag(new ShowFrameTag(null));
                } else {

                    if (tagObj instanceof DefineBitsTag) {
                        JPEGTablesTag jtt = swf.jtt;
                        if (jtt != null) {
                            sos2.writeTag(jtt);
                        }
                    } else if (tagObj instanceof AloneTag) {
                    } else {
                        Set<Integer> needed = ((Tag) tagObj).getDeepNeededCharacters(swf.characters, new ArrayList<Integer>());
                        for (int n : needed) {
                            sos2.writeTag(classicTag(swf.characters.get(n)));
                        }
                    }

                    sos2.writeTag(classicTag((Tag) tagObj));

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
                        RECT r = ((BoundedTag) tagObj).getRect(swf.characters, new Stack<Integer>());
                        mat.translateX = -r.Xmin;
                        mat.translateY = -r.Ymin;
                        mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                        mat.translateY = mat.translateY + height / 2 - r.getHeight() / 2;
                    } else {
                        mat.translateX = width / 4;
                        mat.translateY = height / 4;
                    }
                    if (tagObj instanceof FontTag) {

                        int countGlyphs = ((FontTag) tagObj).getGlyphShapeTable().size();
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
                            sos2.writeTag(new DefineTextTag(null, 999 + f, new RECT(0, width, 0, height), new MATRIX(), rec));
                            sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, true, true, false, 1 + f, 999 + f, mat, null, 0, null, 0, null));
                            x++;
                        }
                        sos2.writeTag(new ShowFrameTag(null));
                    } else if ((tagObj instanceof DefineMorphShapeTag) || (tagObj instanceof DefineMorphShape2Tag)) {
                        sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                        sos2.writeTag(new ShowFrameTag(null));
                        int numFrames = 100;
                        for (int ratio = 0; ratio < 65536; ratio += 65536 / numFrames) {
                            sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, true, false, true, 1, chtId, mat, null, ratio, null, 0, null));
                            sos2.writeTag(new ShowFrameTag(null));
                        }
                    } else if (tagObj instanceof SoundStreamHeadTypeTag) {
                        for (SoundStreamBlockTag blk : soundFrames) {
                            sos2.writeTag(blk);
                            sos2.writeTag(new ShowFrameTag(null));
                        }
                    } else if (tagObj instanceof DefineSoundTag) {
                        ExportAssetsTag ea = new ExportAssetsTag();
                        DefineSoundTag ds = (DefineSoundTag) tagObj;
                        ea.tags.add(ds.soundId);
                        ea.names.add("my_define_sound");
                        sos2.writeTag(ea);
                        List<Action> actions;
                        DoActionTag doa;


                        doa = new DoActionTag(null, new byte[]{}, SWF.DEFAULT_VERSION, 0);
                        actions = ASMParser.parse(0, 0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\"\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", SWF.DEFAULT_VERSION, false);
                        doa.setActions(actions, SWF.DEFAULT_VERSION);
                        sos2.writeTag(doa);
                        sos2.writeTag(new ShowFrameTag(null));


                        actions = ASMParser.parse(0, 0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"start\"\n"
                                + "StopSounds\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Push 9999 0.0 2 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", SWF.DEFAULT_VERSION, false);
                        doa.setActions(actions, SWF.DEFAULT_VERSION);
                        sos2.writeTag(doa);
                        sos2.writeTag(new ShowFrameTag(null));

                        actions = ASMParser.parse(0, 0, false,
                                "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"onSoundComplete\" \"start\" \"execParam\"\n"
                                + "StopSounds\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\" 0.0 \"Sound\"\n"
                                + "NewObject\n"
                                + "SetMember\n"
                                + "Push \"my_define_sound\" 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"attachSound\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"onSoundComplete\"\n"
                                + "DefineFunction2 \"\" 0 2 false true true false true false true false false  {\n"
                                + "Push 0.0 register1 \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "}\n"
                                + "SetMember\n"
                                + "Push \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"execParam\"\n"
                                + "GetMember\n"
                                + "Push 1 \"_root\"\n"
                                + "GetVariable\n"
                                + "Push \"my_sound\"\n"
                                + "GetMember\n"
                                + "Push \"start\"\n"
                                + "CallMethod\n"
                                + "Pop\n"
                                + "Stop", SWF.DEFAULT_VERSION, false);
                        doa.setActions(actions, SWF.DEFAULT_VERSION);
                        sos2.writeTag(doa);
                        sos2.writeTag(new ShowFrameTag(null));


                        actions = ASMParser.parse(0, 0, false,
                                "StopSounds\n"
                                + "Stop", SWF.DEFAULT_VERSION, false);
                        doa.setActions(actions, SWF.DEFAULT_VERSION);
                        sos2.writeTag(doa);
                        sos2.writeTag(new ShowFrameTag(null));


                        sos2.writeTag(new ShowFrameTag(null));
                        if (flashPanel != null) {
                            flashPanel.specialPlayback = true;
                        }
                    } else if (tagObj instanceof DefineVideoStreamTag) {

                        sos2.writeTag(new PlaceObject2Tag(null, false, false, false, false, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                        List<VideoFrameTag> frs = new ArrayList<>(videoFrames.values());
                        Collections.sort(frs, new Comparator<VideoFrameTag>() {
                            @Override
                            public int compare(VideoFrameTag o1, VideoFrameTag o2) {
                                return o1.frameNum - o2.frameNum;
                            }
                        });
                        boolean first = true;
                        int ratio = 0;
                        for (VideoFrameTag f : frs) {
                            if (!first) {
                                ratio++;
                                sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, false, false, true, 1, 0, null, null, ratio, null, 0, null));
                            }
                            sos2.writeTag(f);
                            sos2.writeTag(new ShowFrameTag(null));
                            first = false;
                        }
                    } else {
                        sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                        sos2.writeTag(new ShowFrameTag(null));
                    }


                }//not showframe

                sos2.writeTag(new EndTag(null));
                data = baos.toByteArray();
            }

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                SWFOutputStream sos = new SWFOutputStream(fos, 10);
                sos.write("FWS".getBytes());
                sos.write(swf.version);
                sos.writeUI32(sos.getPos() + data.length + 4);
                sos.write(data);
                fos.flush();
            }
            
            showCard(CARDFLASHPANEL);
            if (flashPanel != null) {
                flashPanel.displaySWF(tempFile.getAbsolutePath(), backgroundColor, frameRate);
            }
        } catch (IOException | com.jpexs.decompiler.flash.action.parser.ParseException ex) {
            Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showFontTag(FontTag ft) {
        if (mainMenu.isInternalFlashViewerSelected() /*|| ft instanceof GFxDefineCompactedFont*/) {
            ((CardLayout) viewerCards.getLayout()).show(viewerCards, INTERNAL_VIEWER_CARD);
            internelViewerPanel.setDrawable(ft, ft.getSwf(), ft.getSwf().characters, 1);
        } else {
            ((CardLayout) viewerCards.getLayout()).show(viewerCards, FLASH_VIEWER_CARD);
        }

        parametersPanel.setVisible(true);
        fontPanel.showFontTag(ft);
        showDetailWithPreview(CARDFONTPANEL);
    }

    public void refreshTree() {
        List<List<String>> expandedNodes = getExpandedNodes(tagTree);

        tagTree.setModel(new TagTreeModel(mainFrame, swfs));

        expandSwfRoots();
        expandTreeNodes(tagTree, expandedNodes);
    }
    
    public void expandSwfRoots() {
        TreeModel model = tagTree.getModel();
        Object node = model.getRoot();
        int childCount = model.getChildCount(node);
        for (int j = 0; j < childCount; j++) {
            Object child = model.getChild(node, j);
            tagTree.expandPath(new TreePath(new Object[] {node, child}));
        }
    }

    private List<List<String>> getExpandedNodes(JTree tree) {
        List<List<String>> expandedNodes = new ArrayList<>();
        int rowCount = tree.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            TreePath path = tree.getPathForRow(i);
            if (tree.isExpanded(path)) {
                List<String> pathAsStringList = new ArrayList<>();
                for (Object pathCompnent : path.getPath()) {
                    pathAsStringList.add(pathCompnent.toString());
                }
                expandedNodes.add(pathAsStringList);
            }
        }
        return expandedNodes;
    }

    private void expandTreeNodes(JTree tree, List<List<String>> pathsToExpand) {
        for (List<String> pathAsStringList : pathsToExpand) {
            expandTreeNode(tree, pathAsStringList);
        }
    }

    private void expandTreeNode(JTree tree, List<String> pathAsStringList) {
        TreeModel model = tree.getModel();
        Object node = model.getRoot();

        if (pathAsStringList.isEmpty()) {
            return;
        }
        if (!pathAsStringList.get(0).equals(node.toString())) {
            return;
        }

        List<Object> path = new ArrayList<>();
        path.add(node);

        for (int i = 1; i < pathAsStringList.size(); i++) {
            String name = pathAsStringList.get(i);
            int childCount = model.getChildCount(node);
            for (int j = 0; j < childCount; j++) {
                Object child = model.getChild(node, j);
                if (child.toString().equals(name)) {
                    node = child;
                    path.add(node);
                    break;
                }
            }
        }

        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        tree.expandPath(tp);
    }

    public void setEditText(boolean edit) {
        setEditText(edit, true);
    }

    public void setEditText(boolean edit, boolean reload) {
        textValue.setEditable(edit);
        textSaveButton.setVisible(edit);
        textEditButton.setVisible(!edit);
        textCancelButton.setVisible(edit);
        if (!edit && reload) {
            reload(true);
        }
    }

    private boolean isFreeing;
    
    @Override
    public boolean isFreeing() {
        return isFreeing;
    }

    @Override
    public void free() {
        isFreeing = true;
        Helper.emptyObject(mainMenu);
        Helper.emptyObject(statusPanel);
        Helper.emptyObject(this);
    }

    public void clearErrorState() {
        statusPanel.clearErrorState();
    }
    public void setErrorState() {
        statusPanel.setErrorState();
    }
}
