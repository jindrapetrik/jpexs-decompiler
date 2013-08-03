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
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
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
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.BaseRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;

/**
 *
 * @author Jindra
 */
public class MainFrame extends AppRibbonFrame implements ActionListener, TreeSelectionListener, Freed {

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
    final static String CARDFONTPANEL = "Font card";
    private LineMarkedEditorPane textValue;
    private JPEGTablesTag jtt;
    private HashMap<Integer, CharacterTag> characters;
    private List<ABCContainerTag> abcList;
    JSplitPane splitPane1;
    JSplitPane splitPane2;
    private boolean splitsInited = false;
    private JPanel detailPanel;
    private JTextField filterField = new MyTextField("");
    private JPanel searchPanel;
    private JCheckBox miAutoDeobfuscation;
    private JPanel displayWithPreview;
    private JButton textSaveButton;
    private JButton textEditButton;
    private JButton textCancelButton;
    private JPanel parametersPanel;
    private JSplitPane previewSplitPane;
    private JButton imageReplaceButton;
    private JPanel imageButtonsPanel;
    private JCheckBox miInternalViewer;
    private JCheckBox miParallelSpeedUp;
    private JCheckBox miAssociate;
    private JCheckBox miDecompile;
    private JCheckBox miCacheDisk;
    private JCheckBox miGotoMainClassOnStartup;
    private JLabel fontNameLabel;
    private JLabel fontIsBoldLabel;
    private JLabel fontIsItalicLabel;
    private JLabel fontAscentLabel;
    private JLabel fontDescentLabel;
    private JLabel fontLeadingLabel;
    private JTextArea fontCharactersTextArea;
    private JTextField fontAddCharactersField;
    private JButton errorNotificationButton;
    private ErrorLogFrame errorLogFrame;
    private ComponentListener fontChangeList;
    private JComboBox<String> fontSelection;
    private Map<Integer, String> sourceFontsMap = new HashMap<>();
    private AbortRetryIgnoreHandler errorHandler = new AbortRetryIgnoreHandler() {
        @Override
        public int handle(Throwable thrown) {
            synchronized (MainFrame.class) {
                String options[] = new String[]{translate("button.abort"), translate("button.retry"), translate("button.ignore")};
                return View.showOptionDialog(null, translate("error.occured").replace("%error%", thrown.getLocalizedMessage()), translate("error"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "");
            }
        }
    };

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

    private String fixCommandTitle(String title) {
        if (title.length() > 2) {
            if (title.charAt(1) == ' ') {
                title = title.charAt(0) + "\u00A0" + title.substring(2);
            }
        }
        return title;
    }

    private void assignListener(JCheckBox b, final String command) {
        b.setActionCommand(command);
        b.addActionListener(this);
    }

    private void assignListener(JCommandButton b, final String command) {
        final MainFrame t = this;
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                t.actionPerformed(new ActionEvent(e.getSource(), 0, command));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public MainFrame(SWF swf) {
        super();
        JRibbon rib = getRibbon();

        JRibbonBand editBand = new JRibbonBand(translate("menu.general"), null);
        editBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(editBand.getControlPanel()), new IconRibbonBandResizePolicy(editBand.getControlPanel())));
        JCommandButton openCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.open")), View.getResizableIcon("open32"));
        assignListener(openCommandButton, "OPEN");
        JCommandButton saveCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.save")), View.getResizableIcon("save32"));
        assignListener(saveCommandButton, "SAVE");
        JCommandButton saveasCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveas")), View.getResizableIcon("saveas32"));
        assignListener(saveasCommandButton, "SAVEAS");



        editBand.addCommandButton(openCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveasCommandButton, RibbonElementPriority.TOP);


        JRibbonBand exportBand = new JRibbonBand(translate("menu.export"), null);
        exportBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(exportBand.getControlPanel()), new IconRibbonBandResizePolicy(exportBand.getControlPanel())));
        JCommandButton exportFlaCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.fla")), View.getResizableIcon("exportfla32"));
        assignListener(exportFlaCommandButton, "EXPORTFLA");
        JCommandButton exportAllCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.all")), View.getResizableIcon("export16"));
        assignListener(exportAllCommandButton, "EXPORT");
        JCommandButton exportSelectionCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.selection")), View.getResizableIcon("exportsel16"));
        assignListener(exportSelectionCommandButton, "EXPORTSEL");

        exportBand.addCommandButton(exportFlaCommandButton, RibbonElementPriority.TOP);
        exportBand.addCommandButton(exportAllCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportSelectionCommandButton, RibbonElementPriority.MEDIUM);

        RibbonTask fileTask = new RibbonTask(translate("menu.file"), editBand, exportBand);
        //----------------------------------------- TOOLS -----------------------------------
        JRibbonBand toolsBand = new JRibbonBand(translate("menu.tools"), null);
        toolsBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(toolsBand.getControlPanel()), new IconRibbonBandResizePolicy(toolsBand.getControlPanel())));

        JCommandButton searchCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchas")), View.getResizableIcon("search32"));
        assignListener(searchCommandButton, "SEARCHAS");
        JCommandButton proxyCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.proxy")), View.getResizableIcon("proxy32"));
        assignListener(proxyCommandButton, "SHOWPROXY");
        JCommandButton gotoDocumentClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.gotodocumentclass")), View.getResizableIcon("gotomainclass32"));
        assignListener(gotoDocumentClassCommandButton, "GOTODOCUMENTCLASS");

        toolsBand.addCommandButton(searchCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(proxyCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(gotoDocumentClassCommandButton, RibbonElementPriority.TOP);

        JRibbonBand deobfuscationBand = new JRibbonBand(translate("menu.tools.deobfuscation"), null);
        deobfuscationBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(deobfuscationBand.getControlPanel()), new IconRibbonBandResizePolicy(deobfuscationBand.getControlPanel())));

        JCommandButton deobfuscationCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.pcode")), View.getResizableIcon("deobfuscate32"));
        assignListener(deobfuscationCommandButton, "DEOBFUSCATE");
        JCommandButton globalrenameCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.globalrename")), View.getResizableIcon("rename16"));
        assignListener(globalrenameCommandButton, "RENAMEONEIDENTIFIER");
        JCommandButton renameinvalidCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.renameinvalid")), View.getResizableIcon("renameall16"));
        assignListener(renameinvalidCommandButton, "RENAMEIDENTIFIERS");

        deobfuscationBand.addCommandButton(deobfuscationCommandButton, RibbonElementPriority.TOP);
        deobfuscationBand.addCommandButton(globalrenameCommandButton, RibbonElementPriority.MEDIUM);
        deobfuscationBand.addCommandButton(renameinvalidCommandButton, RibbonElementPriority.MEDIUM);

        RibbonTask toolsTask = new RibbonTask(translate("menu.tools"), toolsBand, deobfuscationBand);


        //----------------------------------------- SETTINGS -----------------------------------


        JRibbonBand settingsBand = new JRibbonBand(translate("menu.settings"), null);
        settingsBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(settingsBand.getControlPanel()), new IconRibbonBandResizePolicy(settingsBand.getControlPanel())));

        miAutoDeobfuscation = new JCheckBox(translate("menu.settings.autodeobfuscation"));
        //assignListener(autoDeobfuscateMenuItem,"AUTODEOBFUSCATE");

        miInternalViewer = new JCheckBox(translate("menu.settings.internalflashviewer"));
        //assignListener(miInternalViewer,"INTERNALVIEWERSWITCH");
        miParallelSpeedUp = new JCheckBox(translate("menu.settings.parallelspeedup"));
        //assignListener(miParallelSpeedUp,"PARALLELSPEEDUP");
        miDecompile = new JCheckBox(translate("menu.settings.disabledecompilation"));
        //assignListener(miDecompile,"DISABLEDECOMPILATION");
        miAssociate = new JCheckBox(translate("menu.settings.addtocontextmenu"));
        //assignListener(miAssociate,"ASSOCIATE");
        miCacheDisk = new JCheckBox(translate("menu.settings.cacheOnDisk"));
        //assignListener(miCacheDisk,"CACHEONDISK");
        miGotoMainClassOnStartup = new JCheckBox(translate("menu.settings.gotoMainClassOnStartup"));
        //assignListener(miGotoMainClassOnStartup,"GOTODOCUMENTCLASSONSTARTUP");


        settingsBand.addRibbonComponent(new JRibbonComponent(miAutoDeobfuscation));
        settingsBand.addRibbonComponent(new JRibbonComponent(miInternalViewer));
        settingsBand.addRibbonComponent(new JRibbonComponent(miParallelSpeedUp));
        settingsBand.addRibbonComponent(new JRibbonComponent(miDecompile));
        settingsBand.addRibbonComponent(new JRibbonComponent(miAssociate));
        settingsBand.addRibbonComponent(new JRibbonComponent(miCacheDisk));
        settingsBand.addRibbonComponent(new JRibbonComponent(miGotoMainClassOnStartup));

        JRibbonBand languageBand = new JRibbonBand(translate("menu.language"), null);
        languageBand.setResizePolicies((List) Arrays.asList(new BaseRibbonBandResizePolicy<AbstractBandControlPanel>(languageBand.getControlPanel()) {
            @Override
            public int getPreferredWidth(int i, int i1) {
                return 105;
            }

            @Override
            public void install(int i, int i1) {
            }
        }, new IconRibbonBandResizePolicy(languageBand.getControlPanel())));
        JCommandButton setLanguageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.settings.language")), View.getResizableIcon("setlanguage32"));
        assignListener(setLanguageCommandButton, "SETLANGUAGE");
        languageBand.addCommandButton(setLanguageCommandButton, RibbonElementPriority.TOP);
        RibbonTask settingsTask = new RibbonTask(translate("menu.settings"), settingsBand, languageBand);


        //----------------------------------------- HELP -----------------------------------

        JRibbonBand helpBand = new JRibbonBand(translate("menu.help"), null);
        helpBand.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.Mirror(helpBand.getControlPanel()), new IconRibbonBandResizePolicy(helpBand.getControlPanel())));

        JCommandButton checkForUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.checkupdates")), View.getResizableIcon("update16"));
        assignListener(checkForUpdatesCommandButton, "CHECKUPDATES");
        JCommandButton helpUsUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.helpus")), View.getResizableIcon("donate32"));
        assignListener(helpUsUpdatesCommandButton, "HELPUS");
        JCommandButton homepageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.homepage")), View.getResizableIcon("homepage16"));
        assignListener(homepageCommandButton, "HOMEPAGE");
        JCommandButton aboutCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.about")), View.getResizableIcon("about32"));
        assignListener(aboutCommandButton, "ABOUT");

        helpBand.addCommandButton(aboutCommandButton, RibbonElementPriority.TOP);
        helpBand.addCommandButton(checkForUpdatesCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(homepageCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(helpUsUpdatesCommandButton, RibbonElementPriority.TOP);
        RibbonTask helpTask = new RibbonTask(translate("menu.help"), helpBand);


        rib.addTask(fileTask);
        rib.addTask(toolsTask);
        rib.addTask(settingsTask);
        rib.addTask(helpTask);


        RibbonApplicationMenu mainMenu = new RibbonApplicationMenu();
        RibbonApplicationMenuEntryPrimary exportFlaMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportfla32"), translate("menu.file.export.fla"), new ActionRedirector(this, "EXPORTFLA"), CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportAllMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("export32"), translate("menu.file.export.all"), new ActionRedirector(this, "EXPORTSEL"), CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportSelMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportsel32"), translate("menu.file.export.selection"), new ActionRedirector(this, "EXPORTSEL"), CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary checkUpdatesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("update32"), translate("menu.help.checkupdates"), new ActionRedirector(this, "CHECKUPDATES"), CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary aboutMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("about32"), translate("menu.help.about"), new ActionRedirector(this, "ABOUT"), CommandButtonKind.ACTION_ONLY);
        //

        RibbonApplicationMenuEntryFooter exitMenu = new RibbonApplicationMenuEntryFooter(View.getResizableIcon("exit32"), translate("menu.file.exit"), new ActionRedirector(this, "EXIT"));

        mainMenu.addMenuEntry(exportFlaMenu);
        mainMenu.addMenuEntry(exportAllMenu);
        mainMenu.addMenuEntry(exportSelMenu);
        mainMenu.addMenuSeparator();
        mainMenu.addMenuEntry(checkUpdatesMenu);
        mainMenu.addMenuEntry(aboutMenu);
        mainMenu.addFooterEntry(exitMenu);
        mainMenu.addMenuSeparator();
        /*ResizableIcon ic = View.getResizableIcon("icon_round256");
         setApplicationIcon(ic);*/
        rib.setApplicationMenu(mainMenu);


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
        setTitle(Main.applicationVerName + ((swf != null && Configuration.DISPLAY_FILENAME) ? " - " + Main.getFileTitle() : ""));
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

        //autoDeobfuscateMenuItem = new JCheckBoxMenuItem(translate("menu.settings.autodeobfuscation"));
        miAutoDeobfuscation.setSelected((Boolean) Configuration.getConfig("autoDeobfuscate", true));
        miAutoDeobfuscation.addActionListener(this);
        miAutoDeobfuscation.setActionCommand("AUTODEOBFUSCATE");

        JMenuItem miRenameOneIdentifier = new JMenuItem(translate("menu.tools.deobfuscation.globalrename"));
        miRenameOneIdentifier.setActionCommand("RENAMEONEIDENTIFIER");
        miRenameOneIdentifier.addActionListener(this);

        JMenuItem miRenameIdentifiers = new JMenuItem(translate("menu.tools.deobfuscation.renameinvalid"));
        miRenameIdentifiers.setActionCommand("RENAMEIDENTIFIERS");
        miRenameIdentifiers.addActionListener(this);


        menuDeobfuscation.add(miRenameOneIdentifier);
        menuDeobfuscation.add(miRenameIdentifiers);
        menuDeobfuscation.add(miDeobfuscation);
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

        //miInternalViewer = new JCheckBox(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected((Boolean) Configuration.getConfig("internalFlashViewer", (Boolean) (flashPanel == null)));
        if (flashPanel == null) {
            miInternalViewer.setSelected(true);
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.setActionCommand("INTERNALVIEWERSWITCH");
        miInternalViewer.addActionListener(this);

        //miParallelSpeedUp = new JCheckBox(translate("menu.settings.parallelspeedup"));
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

        //miDecompile = new JCheckBox(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!(Boolean) Configuration.getConfig("decompile", Boolean.TRUE));
        miDecompile.setActionCommand("DISABLEDECOMPILATION");
        miDecompile.addActionListener(this);


        //miCacheDisk = new JCheckBox(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected((Boolean) Configuration.getConfig("cacheOnDisk", Boolean.TRUE));
        miCacheDisk.setActionCommand("CACHEONDISK");
        miCacheDisk.addActionListener(this);

        // miGotoMainClassOnStartup = new JCheckBox(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected((Boolean) Configuration.getConfig("gotoMainClassOnStartup", Boolean.FALSE));
        miGotoMainClassOnStartup.setActionCommand("GOTODOCUMENTCLASSONSTARTUP");
        miGotoMainClassOnStartup.addActionListener(this);

        /*JMenu menuSettings = new JMenu(translate("menu.settings"));
         menuSettings.add(autoDeobfuscateMenuItem);
         menuSettings.add(miInternalViewer);
         menuSettings.add(miParallelSpeedUp);
         menuSettings.add(miDecompile);
         menuSettings.add(miCacheDisk);
         menuSettings.add(miGotoMainClassOnStartup);*/

        // miAssociate = new JCheckBox(translate("menu.settings.addtocontextmenu"));
        miAssociate.setActionCommand("ASSOCIATE");
        miAssociate.addActionListener(this);
        miAssociate.setSelected(Main.isAddedToContextMenu());


        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.setActionCommand("SETLANGUAGE");
        miLanguage.addActionListener(this);

        /* if (Platform.isWindows()) {
         menuSettings.add(miAssociate);
         }
         menuSettings.add(miLanguage);

         menuBar.add(menuSettings);*/
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

        //setJMenuBar(menuBar);

        List<Object> objs = new ArrayList<>();
        if (swf != null) {
            objs.addAll(swf.tags);
        }

        this.swf = swf;
        java.awt.Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(getRibbon(), BorderLayout.NORTH);

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
            gotoDocumentClassCommandButton.setEnabled(false);
            actionPanel = new ActionPanel();
            deobfuscationCommandButton.setEnabled(false);
            //miDeobfuscation.setEnabled(false);
        }

        if (swf == null) {
            renameinvalidCommandButton.setEnabled(false);
            globalrenameCommandButton.setEnabled(false);
            saveCommandButton.setEnabled(false);
            saveasCommandButton.setEnabled(false);
            exportAllCommandButton.setEnabled(false);
            exportAllMenu.setEnabled(false);
            exportFlaCommandButton.setEnabled(false);
            exportFlaMenu.setEnabled(false);
            exportSelectionCommandButton.setEnabled(false);
            exportSelMenu.setEnabled(false);
            deobfuscationCommandButton.setEnabled(false);
            searchCommandButton.setEnabled(false);
        }

        UIManager.getDefaults().put("TreeUI", BasicTreeUI.class.getName());
        if (swf == null) {
            tagTree = new JTree((TreeModel) null);
        } else {
            tagTree = new JTree(new TagTreeModel(createTagList(objs, null), new SWFRoot((new File(Main.file)).getName())));
        }
        tagTree.addTreeSelectionListener(this);
        tagTree.setBackground(Color.white);
        tagTree.setUI(new BasicTreeUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                setHashColor(Color.gray);
                super.paint(g, c);
            }
        });



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
                setPreferredSize(new Dimension(18 + sw, getPreferredSize().height));
                setUI(new BasicLabelUI());
                setOpaque(false);
                //setBackground(Color.green);
                setBackgroundNonSelectionColor(Color.white);
                //setBackgroundSelectionColor(Color.ORANGE);
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

        errorLogFrame = new ErrorLogFrame();
        errorNotificationButton = new JButton("");
        errorNotificationButton.setIcon(View.getIcon("okay16"));
        errorNotificationButton.setBorderPainted(false);
        errorNotificationButton.setFocusPainted(false);
        errorNotificationButton.setContentAreaFilled(false);
        errorNotificationButton.setMargin(new Insets(2, 2, 2, 2));
        errorNotificationButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        errorNotificationButton.setActionCommand("SHOWERRORLOG");
        errorNotificationButton.addActionListener(this);
        errorNotificationButton.setToolTipText(translate("errors.none"));
        statusPanel.add(errorNotificationButton, BorderLayout.EAST);


        loadingPanel.setVisible(false);
        cnt.add(statusPanel, BorderLayout.SOUTH);

        if (swf != null) {
            for (Tag t : swf.tags) {
                if (t instanceof JPEGTablesTag) {
                    jtt = (JPEGTablesTag) t;
                }
            }
        }
        characters = new HashMap<>();
        List<Object> list2 = new ArrayList<>();
        if (swf != null) {
            list2.addAll(swf.tags);
        }
        parseCharacters(list2);
        JPanel textTopPanel = new JPanel(new BorderLayout());
        textValue = new LineMarkedEditorPane();
        textTopPanel.add(new JScrollPane(textValue), BorderLayout.CENTER);
        textValue.setEditable(false);
        //textValue.setFont(UIManager.getFont("TextField.font"));



        JPanel textButtonsPanel = new JPanel();
        textButtonsPanel.setLayout(new FlowLayout());


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

        textButtonsPanel.add(textEditButton);
        textButtonsPanel.add(textSaveButton);
        textButtonsPanel.add(textCancelButton);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        textTopPanel.add(textButtonsPanel, BorderLayout.SOUTH);

        displayWithPreview = new JPanel(new CardLayout());

        displayWithPreview.add(textTopPanel, CARDTEXTPANEL);



        //TODO: This layout SUCKS! If you know something better, please fix it!
        final JPanel fontPanel = new JPanel();
        final JPanel fontParams2 = new JPanel();
        fontParams2.setLayout(null);
        final Component ctable[][] = new Component[][]{
            {new JLabel(translate("font.name")), fontNameLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.isbold")), fontIsBoldLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.isitalic")), fontIsItalicLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.ascent")), fontAscentLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.descent")), fontDescentLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.leading")), fontLeadingLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.characters")), fontCharactersTextArea = new JTextArea("")}
        };
        fontCharactersTextArea.setLineWrap(true);
        fontCharactersTextArea.setWrapStyleWord(true);
        fontCharactersTextArea.setOpaque(false);
        fontCharactersTextArea.setEditable(false);
        fontCharactersTextArea.setFont(new JLabel().getFont());

        final int borderLeft = 10;

        final int maxws[] = new int[ctable[0].length];
        for (int x = 0; x < ctable[0].length; x++) {
            int maxw = 0;
            for (int y = 0; y < ctable.length; y++) {
                Dimension d = ctable[y][x].getPreferredSize();
                if (d.width > maxw) {
                    maxw = d.width;
                }
            }
            maxws[x] = maxw;
        }

        for (int i = 0; i < ctable.length; i++) {
            fontParams2.add(ctable[i][0]);
            fontParams2.add(ctable[i][1]);
        }

        fontParams2.setPreferredSize(new Dimension(600, ctable.length * 25));
        fontChangeList = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                int h = 0;
                Insets is = fontPanel.getInsets();
                Insets is2 = fontParams2.getInsets();
                for (int i = 0; i < ctable.length; i++) {
                    Dimension d = ctable[i][0].getPreferredSize();
                    Dimension d2 = ctable[i][1].getPreferredSize();
                    ctable[i][0].setBounds(borderLeft, h, maxws[0], 25);

                    int w2 = fontPanel.getWidth() - 3 * borderLeft - maxws[0] - is.left - is.right - 10;
                    ctable[i][1].setBounds(borderLeft + maxws[0] + borderLeft, h, w2, d2.height);
                    h += Math.max(Math.max(d.height, d2.height), 25);
                }

                fontParams2.setPreferredSize(new Dimension(fontPanel.getWidth() - 20, h));
                fontPanel.revalidate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                componentResized(null);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                componentResized(null);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                componentResized(null);
            }
        };
        final JPanel fontParams1 = new JPanel();
        fontPanel.addComponentListener(fontChangeList);

        fontChangeList.componentResized(null);
        fontParams1.setLayout(new BoxLayout(fontParams1, BoxLayout.Y_AXIS));
        fontParams1.add(fontParams2);

        JPanel fontAddCharsPanel = new JPanel(new FlowLayout());
        fontAddCharsPanel.add(new JLabel(translate("font.characters.add")));
        fontAddCharactersField = new MyTextField();
        fontAddCharactersField.setPreferredSize(new Dimension(150, fontAddCharactersField.getPreferredSize().height));
        fontAddCharsPanel.add(fontAddCharactersField);
        JButton fontAddCharsButton = new JButton(translate("button.ok"));
        fontAddCharsButton.setActionCommand("FONTADDCHARS");
        fontAddCharsButton.addActionListener(this);
        fontAddCharsPanel.add(fontAddCharsButton);


        fontParams1.add(fontAddCharsPanel);
        JPanel fontSelectionPanel = new JPanel(new FlowLayout());
        fontSelectionPanel.add(new JLabel(translate("font.source")));
        fontSelection = new JComboBox<>(FontTag.fontNames.toArray(new String[FontTag.fontNames.size()]));
        fontSelection.setSelectedIndex(0);
        fontSelection.setSelectedItem("Times New Roman");
        fontSelection.setSelectedItem("Arial");
        fontSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (oldValue instanceof FontTag) {
                    FontTag f = (FontTag) oldValue;
                    sourceFontsMap.put(f.getFontId(), (String) fontSelection.getSelectedItem());
                }
            }
        });
        fontSelectionPanel.add(fontSelection);

        JPanel fontCharPanel = new JPanel();
        fontCharPanel.setLayout(new ListLayout());
        fontCharPanel.add(fontAddCharsPanel);
        fontCharPanel.add(fontSelectionPanel);
        fontParams1.add(fontCharPanel);
        fontPanel.setLayout(new BorderLayout());
        fontParams1.add(Box.createVerticalGlue());
        fontPanel.add(new JScrollPane(fontParams1), BorderLayout.CENTER);

        displayWithPreview.add(fontPanel, CARDFONTPANEL);


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
        JLabel prevLabel = new HeaderLabel(translate("swfpreview"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        JLabel paramsLabel = new HeaderLabel(translate("parameters"));
        paramsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //paramsLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
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
        JLabel prevIntLabel = new HeaderLabel(translate("swfpreview.internal"));
        prevIntLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevIntLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
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


        if (swf == null) {
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
            cnt.add(welcomePanel, BorderLayout.CENTER);
        } else {
            cnt.add(splitPane1, BorderLayout.CENTER);
        }
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

        Logger logger = Logger.getLogger("");
        logger.addHandler(errorLogFrame.getHandler());
        logger.addHandler(new Handler() {
            private Timer timer = null;
            private int pos = 0;

            @Override
            public void publish(final LogRecord record) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (record.getLevel() == Level.SEVERE) {
                            errorNotificationButton.setIcon(View.getIcon("error16"));
                            errorNotificationButton.setToolTipText(translate("errors.present"));
                            if (timer != null) {
                                timer.cancel();
                            }
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    View.execInEventDispatch(new Runnable() {
                                        @Override
                                        public void run() {
                                            pos++;
                                            if ((pos % 2) == 0 || (pos >= 4)) {
                                                errorNotificationButton.setIcon(View.getIcon("error16"));
                                            } else {
                                                errorNotificationButton.setIcon(null);
                                                errorNotificationButton.setSize(16, 16);
                                            }
                                        }
                                    });

                                    if (pos >= 4) {
                                        cancel();
                                    }
                                }
                            }, 500, 500);
                        }
                    }
                });

            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
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
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    tagTree.updateUI();
                }
            });

        }
    }

    private static void getApplicationMenuButtons(Component comp, List<JRibbonApplicationMenuButton> ret) {
        if (comp instanceof JRibbonApplicationMenuButton) {
            ret.add((JRibbonApplicationMenuButton) comp);
            return;
        }
        if (comp instanceof java.awt.Container) {
            java.awt.Container cont = (java.awt.Container) comp;
            for (int i = 0; i < cont.getComponentCount(); i++) {
                getApplicationMenuButtons(cont.getComponent(i), ret);
            }
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

            final MainFrame t = this;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    List<JRibbonApplicationMenuButton> mbuttons = new ArrayList<>();
                    getApplicationMenuButtons(t, mbuttons);

                    for (final JRibbonApplicationMenuButton mbutton : mbuttons) {
                        mbutton.setIcon(View.getResizableIcon("buttonicon_256"));
                        mbutton.setDisplayState(new CommandButtonDisplayState(
                                "My Ribbon Application Menu Button", mbutton.getSize().width) {
                            @Override
                            public org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager createLayoutManager(
                                    org.pushingpixels.flamingo.api.common.AbstractCommandButton commandButton) {
                                return new CommandButtonLayoutManager() {
                                    @Override
                                    public int getPreferredIconSize() {
                                        return mbutton.getSize().width;
                                    }

                                    @Override
                                    public CommandButtonLayoutManager.CommandButtonLayoutInfo getLayoutInfo(
                                            AbstractCommandButton commandButton, Graphics g) {
                                        CommandButtonLayoutManager.CommandButtonLayoutInfo result = new CommandButtonLayoutManager.CommandButtonLayoutInfo();
                                        result.actionClickArea = new Rectangle(0, 0, 0, 0);
                                        result.popupClickArea = new Rectangle(0, 0, commandButton
                                                .getWidth(), commandButton.getHeight());
                                        result.popupActionRect = new Rectangle(0, 0, 0, 0);
                                        ResizableIcon icon = commandButton.getIcon();
                                        icon.setDimension(new Dimension(commandButton.getWidth(), commandButton.getHeight()));
                                        result.iconRect = new Rectangle(
                                                0,
                                                0,
                                                commandButton.getWidth(), commandButton.getHeight());
                                        result.isTextInActionArea = false;
                                        return result;
                                    }

                                    @Override
                                    public Dimension getPreferredSize(
                                            AbstractCommandButton commandButton) {
                                        return new Dimension(40, 40);
                                    }

                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                    }

                                    @Override
                                    public Point getKeyTipAnchorCenterPoint(
                                            AbstractCommandButton commandButton) {
                                        // dead center
                                        return new Point(commandButton.getWidth() / 2,
                                                commandButton.getHeight() / 2);
                                    }
                                };
                            }
                        });

                        MyRibbonApplicationMenuButtonUI mui = (MyRibbonApplicationMenuButtonUI) mbutton.getUI();
                        mui.setHoverIcon(View.getResizableIcon("buttonicon_hover_256"));
                        mui.setNormalIcon(View.getResizableIcon("buttonicon_256"));
                        mui.setClickIcon(View.getResizableIcon("buttonicon_down_256"));
                    }
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
        String newName = View.showInputDialog(translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                swf.renameAS2Identifier(oldName, newName);
                View.showMessageDialog(null, translate("rename.finished.identifier"));
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
        String newName = View.showInputDialog(translate("rename.enternew"), oldName);
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
                View.showMessageDialog(null, translate("rename.finished.multiname").replace("%count%", "" + mulCount));
                if (abcPanel != null) {
                    abcPanel.reload();
                }
                doFilter();
                reload(true);
                abcPanel.hilightScript(abcPanel.decompiledTextArea.getScriptLeaf().getPath().toString());
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
        if (tm == null) {
            return null;
        }
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
                    tti.subItems = createTagList(subItems, t);
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
        return View.showConfirmDialog(null, translate("message.confirm.experimental"), translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }
    private SearchDialog searchDialog;

    public List<File> exportSelection(AbortRetryIgnoreHandler handler, String selFile, ExportDialog export) throws IOException {
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
        ret.addAll(swf.exportImages(handler, selFile + File.separator + "images", images));
        ret.addAll(SWF.exportShapes(handler, selFile + File.separator + "shapes", shapes));
        ret.addAll(swf.exportTexts(handler, selFile + File.separator + "texts", texts, isFormatted));
        ret.addAll(swf.exportMovies(handler, selFile + File.separator + "movies", movies));
        ret.addAll(swf.exportSounds(handler, selFile + File.separator + "sounds", sounds, isMp3OrWav, isMp3OrWav));
        ret.addAll(swf.exportBinaryData(handler, selFile + File.separator + "binaryData", binaryData));
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
                ret.addAll(TagNode.exportNodeAS(swf.tags, handler, allNodes, selFile, isPcode));
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
            showCard(CARDACTIONSCRIPTPANEL);
            abcPanel.hilightScript(documentClass);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "SHOWERRORLOG":
                errorLogFrame.setVisible(true);
                break;
            case "FONTADDCHARS":
                String newchars = fontAddCharactersField.getText();
                if (oldValue instanceof FontTag) {
                    FontTag f = (FontTag) oldValue;
                    String oldchars = f.getCharacters(swf.tags);

                    for (int i = 0; i < newchars.length(); i++) {
                        char c = newchars.charAt(i);
                        if (oldchars.indexOf((int) c) == -1) {
                            Font font = new Font(fontSelection.getSelectedItem().toString(), f.getFontStyle(), 1024);
                            if (!font.canDisplay(c)) {
                                View.showMessageDialog(null, translate("error.font.nocharacter").replace("%char%", "" + c), translate("error"), JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    for (int i = 0; i < newchars.length(); i++) {
                        char c = newchars.charAt(i);
                        if (oldchars.indexOf((int) c) == -1) {
                            f.addCharacter(swf.tags, c, fontSelection.getSelectedItem().toString());
                        }
                    }
                    fontAddCharactersField.setText("");
                    reload(true);
                }
                break;
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
                    View.showMessageDialog(null, "Changing language needs application restart.\r\nApplication will exit now, please run it again.");
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
                if (miAssociate.isSelected() == Main.isAddedToContextMenu()) {
                    return;
                }
                Main.addToContextMenu(miAssociate.isSelected());

                //Update checkbox menuitem accordingly (User can cancel rights elevation)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        miAssociate.setSelected(Main.isAddedToContextMenu());
                    }
                }, 1000); //It takes some time registry change to apply
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
                if (View.showConfirmDialog(null, confStr, translate("message.parallel"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.setConfig("paralelSpeedUp", (Boolean) miParallelSpeedUp.isSelected());
                } else {
                    miParallelSpeedUp.setSelected(!miParallelSpeedUp.isSelected());
                }
                break;
            case "INTERNALVIEWERSWITCH":
                Configuration.setConfig("internalFlashViewer", (Boolean) miInternalViewer.isSelected());
                reload(true);
                break;
            case "SEARCHAS":
                if (searchDialog == null) {
                    searchDialog = new SearchDialog();
                }
                searchDialog.setVisible(true);
                if (searchDialog.result) {
                    final String txt = searchDialog.searchField.getText();
                    if (!txt.equals("")) {
                        if (abcPanel != null) {
                            (new Thread() {
                                @Override
                                public void run() {
                                    if (abcPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                        showDetail(DETAILCARDAS3NAVIGATOR);
                                        showCard(CARDACTIONSCRIPTPANEL);
                                    } else {
                                        View.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            }).start();
                        } else {
                            (new Thread() {
                                @Override
                                public void run() {
                                    if (actionPanel.search(txt, searchDialog.ignoreCaseCheckBox.isSelected(), searchDialog.regexpCheckBox.isSelected())) {
                                        showCard(CARDACTIONSCRIPTPANEL);
                                    } else {
                                        View.showMessageDialog(null, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                    }
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
                                View.showMessageDialog(null, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
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
                    if (View.showConfirmDialog(this, translate("message.confirm.remove").replace("%item%", tagObj.toString()), translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
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
                        if (((TextTag) oldValue).setFormattedText(new MissingCharacterHandler() {
                            @Override
                            public boolean handle(FontTag font, List<Tag> tags, char character) {
                                String fontName = sourceFontsMap.get(font.getFontId());
                                if (fontName == null) {
                                    fontName = font.getFontName(tags);
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
                        }, swf.tags, textValue.getText(), fontSelection.getSelectedItem().toString())) {
                            setEditText(false);
                        }
                    } catch (ParseException ex) {
                        View.showMessageDialog(null, translate("error.text.invalid").replace("%text%", ex.text).replace("%line%", "" + ex.line), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;

            case "AUTODEOBFUSCATE":
                if (View.showConfirmDialog(this, translate("message.confirm.autodeobfuscate") + "\r\n" + (miAutoDeobfuscation.isSelected() ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.setConfig("autoDeobfuscate", miAutoDeobfuscation.isSelected());
                    clearCache();
                    if (abcPanel != null) {
                        abcPanel.reload();
                    }
                    reload(true);
                    doFilter();
                } else {
                    miAutoDeobfuscation.setSelected(!miAutoDeobfuscation.isSelected());
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
                        View.showMessageDialog(null, translate("message.rename.notfound.multiname"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
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
                        View.showMessageDialog(null, translate("message.rename.notfound.identifier"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
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
                    View.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
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
                            try {
                                if (compressed) {
                                    swf.exportFla(errorHandler, selfile.getAbsolutePath(), new File(Main.file).getName(), Main.applicationName, Main.applicationVerName, Main.version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                                } else {
                                    swf.exportXfl(errorHandler, selfile.getAbsolutePath(), new File(Main.file).getName(), Main.applicationName, Main.applicationVerName, Main.version, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                                }
                            } catch (IOException ex) {
                                View.showMessageDialog(null, translate("error.export") + ": " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
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
                                        exportSelection(errorHandler, selFile, export);
                                    } else {
                                        swf.exportImages(errorHandler, selFile + File.separator + "images");
                                        swf.exportShapes(errorHandler, selFile + File.separator + "shapes");
                                        swf.exportTexts(errorHandler, selFile + File.separator + "texts", isFormatted);
                                        swf.exportMovies(errorHandler, selFile + File.separator + "movies");
                                        swf.exportSounds(errorHandler, selFile + File.separator + "sounds", isMp3OrWav, isMp3OrWav);
                                        swf.exportBinaryData(errorHandler, selFile + File.separator + "binaryData");
                                        swf.exportActionScript(errorHandler, selFile, isPcode, (Boolean) Configuration.getConfig("paralelSpeedUp", Boolean.TRUE));
                                    }
                                } catch (Exception ex) {
                                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Error during export", ex);
                                    View.showMessageDialog(null, translate("error.export") + ": " + ex.getLocalizedMessage());
                                }
                                Main.stopWork();
                                long timeAfter = System.currentTimeMillis();
                                long timeMs = timeAfter - timeBefore;

                                setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                            }
                        }).start();

                    }
                }
                break;

            case "CHECKUPDATES":
                if (!Main.checkForUpdates()) {
                    View.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
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
                    View.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
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
                    View.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
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
                            View.showMessageDialog(null, "Control flow restored");
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
                                    View.showMessageDialog(null, translate("message.rename.renamed").replace("%count%", "" + cnt));
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
                                            abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic(), ""/*FIXME*/);
                                        } else if (deobfuscationDialog.codeProcessingLevel.getValue() == DeobfuscationDialog.LEVEL_RESTORE_CONTROL_FLOW) {
                                            abcPanel.abc.bodies[bi].removeTraps(abcPanel.abc.constants, abcPanel.abc, abcPanel.decompiledTextArea.getScriptLeaf().scriptIndex, abcPanel.decompiledTextArea.getClassIndex(), abcPanel.decompiledTextArea.getIsStatic(), ""/*FIXME*/);
                                            abcPanel.abc.bodies[bi].restoreControlFlow(abcPanel.abc.constants);
                                        }
                                    }
                                    abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abcPanel.abc);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Deobfuscation error", ex);
                            }
                            Main.stopWork();
                            View.showMessageDialog(null, translate("work.deobfuscating.complete"));
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
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                int classIndex = -1;
                                for (Trait t : scriptLeaf.abc.script_info[scriptLeaf.scriptIndex].traits.traits) {
                                    if (t instanceof TraitClass) {
                                        classIndex = ((TraitClass) t).class_info;
                                        break;
                                    }
                                }
                                abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setCode("");
                                abcPanel.navigator.setABC(abcList, scriptLeaf.abc);
                                abcPanel.navigator.setClassIndex(classIndex, scriptLeaf.scriptIndex);
                                abcPanel.setAbc(scriptLeaf.abc);
                                abcPanel.decompiledTextArea.setScript(scriptLeaf, abcList);
                                abcPanel.decompiledTextArea.setClassIndex(classIndex);
                                abcPanel.decompiledTextArea.setNoTrait();
                                Main.stopWork();
                            }
                        });
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
            previewImagePanel.setDrawable((DrawableTag) tagObj, swf, characters);
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
                    sos2.writeTag(new SetBackgroundColorTag(null, new RGB(255, 255, 255)));

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
                                    sos2.writeTag(new PlaceObject2Tag(null, false, false, false, false, false, true, false, true, depth, chid, mat, null, 0, null, 0, null));

                                }
                            }
                        }
                        sos2.writeTag(new ShowFrameTag(null));
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
                        } else {
                            sos2.writeTag(new PlaceObject2Tag(null, false, false, false, true, false, true, true, false, 1, chtId, mat, null, 0, null, 0, null));
                            sos2.writeTag(new ShowFrameTag(null));
                        }
                    }//not showframe

                    sos2.writeTag(new EndTag(null));
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
            } else if (tagObj instanceof FontTag) {
                parametersPanel.setVisible(true);
                previewSplitPane.setDividerLocation(previewSplitPane.getWidth() / 2);
                FontTag ft = (FontTag) tagObj;
                fontNameLabel.setText(ft.getFontName(swf.tags));
                fontIsBoldLabel.setText(ft.isBold() ? translate("yes") : translate("no"));
                fontIsItalicLabel.setText(ft.isItalic() ? translate("yes") : translate("no"));
                fontDescentLabel.setText(ft.getDescent() == -1 ? translate("value.unknown") : "" + ft.getDescent());
                fontAscentLabel.setText(ft.getAscent() == -1 ? translate("value.unknown") : "" + ft.getAscent());
                fontLeadingLabel.setText(ft.getLeading() == -1 ? translate("value.unknown") : "" + ft.getLeading());
                String chars = ft.getCharacters(swf.tags);
                fontCharactersTextArea.setText(chars);
                if (sourceFontsMap.containsKey(ft.getFontId())) {
                    fontSelection.setSelectedItem(sourceFontsMap.get(ft.getFontId()));
                } else {
                    fontSelection.setSelectedItem(FontTag.findInstalledFontName(ft.getFontName(swf.tags)));
                }
                fontChangeList.componentResized(null);
                showDetailWithPreview(CARDFONTPANEL);
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

    @Override
    public void free() {
        Helper.emptyObject(this);
    }
}
