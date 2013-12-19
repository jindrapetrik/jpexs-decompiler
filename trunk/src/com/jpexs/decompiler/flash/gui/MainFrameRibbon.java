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

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.helpers.Cache;
import com.jpexs.process.ProcessTools;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
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
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;

/**
 *
 * @author JPEXS
 */
public class MainFrameRibbon implements ActionListener {

    static final String ACTION_RELOAD = "RELOAD";
    static final String ACTION_ADVANCED_SETTINGS = "ADVANCEDSETTINGS";
    static final String ACTION_LOAD_MEMORY = "LOADMEMORY";
    static final String ACTION_LOAD_CACHE = "LOADCACHE";
    static final String ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP = "GOTODOCUMENTCLASSONSTARTUP";
    static final String ACTION_CACHE_ON_DISK = "CACHEONDISK";
    static final String ACTION_SET_LANGUAGE = "SETLANGUAGE";
    static final String ACTION_DISABLE_DECOMPILATION = "DISABLEDECOMPILATION";
    static final String ACTION_ASSOCIATE = "ASSOCIATE";
    static final String ACTION_GOTO_DOCUMENT_CLASS = "GOTODOCUMENTCLASS";
    static final String ACTION_PARALLEL_SPEED_UP = "PARALLELSPEEDUP";
    static final String ACTION_INTERNAL_VIEWER_SWITCH = "INTERNALVIEWERSWITCH";
    static final String ACTION_SEARCH_AS = "SEARCHAS";
    static final String ACTION_AUTO_DEOBFUSCATE = "AUTODEOBFUSCATE";
    static final String ACTION_EXIT = "EXIT";

    static final String ACTION_RENAME_ONE_IDENTIFIER = "RENAMEONEIDENTIFIER";
    static final String ACTION_ABOUT = "ABOUT";
    static final String ACTION_SHOW_PROXY = "SHOWPROXY";
    static final String ACTION_SUB_LIMITER = "SUBLIMITER";
    static final String ACTION_SAVE = "SAVE";
    static final String ACTION_SAVE_AS = "SAVEAS";
    static final String ACTION_OPEN = "OPEN";
    static final String ACTION_EXPORT_FLA = "EXPORTFLA";
    public static final String ACTION_EXPORT_SEL = "EXPORTSEL";
    static final String ACTION_EXPORT = "EXPORT";
    static final String ACTION_CHECK_UPDATES = "CHECKUPDATES";
    static final String ACTION_HELP_US = "HELPUS";
    static final String ACTION_HOMEPAGE = "HOMEPAGE";
    static final String ACTION_RESTORE_CONTROL_FLOW = "RESTORECONTROLFLOW";
    static final String ACTION_RESTORE_CONTROL_FLOW_ALL = "RESTORECONTROLFLOWALL";
    static final String ACTION_RENAME_IDENTIFIERS = "RENAMEIDENTIFIERS";
    static final String ACTION_DEOBFUSCATE = "DEOBFUSCATE";
    static final String ACTION_DEOBFUSCATE_ALL = "DEOBFUSCATEALL";
    static final String ACTION_REMOVE_NON_SCRIPTS = "REMOVENONSCRIPTS";
    static final String ACTION_REFRESH_DECOMPILED = "REFRESHDECOMPILED";

    private MainFrame mainFrame;

    private JCheckBox miAutoDeobfuscation;
    private JCheckBox miInternalViewer;
    private JCheckBox miParallelSpeedUp;
    private JCheckBox miAssociate;
    private JCheckBox miDecompile;
    private JCheckBox miCacheDisk;
    private JCheckBox miGotoMainClassOnStartup;
    private JCommandButton saveCommandButton;
    
    public MainFrameRibbon(MainFrame mainFrame, JRibbon ribbon, boolean swfLoaded, boolean hasAbc, boolean externalFlashPlayerUnavailable) {
        this.mainFrame = mainFrame;

        ribbon.addTask(createFileRibbonTask(swfLoaded));
        ribbon.addTask(createToolsRibbonTask(swfLoaded, hasAbc));
        ribbon.addTask(createSettingsRibbonTask());
        ribbon.addTask(createHelpRibbonTask());

        if (Configuration.debugMode.get()) {
            ribbon.addTask(createDebugRibbonTask());
        }

        ribbon.setApplicationMenu(createMainMenu(swfLoaded));

        createMenuBar(hasAbc, externalFlashPlayerUnavailable);
    }

    public boolean isInternalFlashViewerSelected() {
        return miInternalViewer.isSelected();
    }
    
    private String translate(String key) {
        return mainFrame.translate(key);
    }

    private void assignListener(JCommandButton b, final String command) {
        final MainFrameRibbon t = this;
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                t.actionPerformed(new ActionEvent(e.getSource(), 0, command));
            }
        });
    }

    private String fixCommandTitle(String title) {
        if (title.length() > 2) {
            if (title.charAt(1) == ' ') {
                title = title.charAt(0) + "\u00A0" + title.substring(2);
            }
        }
        return title;
    }

    private void createMenuBar(boolean hasAbc, boolean externalFlashPlayerUnavailable) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu(translate("menu.file"));
        JMenuItem miOpen = new JMenuItem(translate("menu.file.open"));
        miOpen.setIcon(View.getIcon("open16"));
        miOpen.setActionCommand(ACTION_OPEN);
        miOpen.addActionListener(this);
        JMenuItem miSave = new JMenuItem(translate("menu.file.save"));
        miSave.setIcon(View.getIcon("save16"));
        miSave.setActionCommand(ACTION_SAVE);
        miSave.addActionListener(this);
        JMenuItem miSaveAs = new JMenuItem(translate("menu.file.saveas"));
        miSaveAs.setIcon(View.getIcon("saveas16"));
        miSaveAs.setActionCommand(ACTION_SAVE_AS);
        miSaveAs.addActionListener(this);

        JMenuItem menuExportFla = new JMenuItem(translate("menu.file.export.fla"));
        menuExportFla.setActionCommand(ACTION_EXPORT_FLA);
        menuExportFla.addActionListener(this);
        menuExportFla.setIcon(View.getIcon("flash16"));

        JMenuItem menuExportAll = new JMenuItem(translate("menu.file.export.all"));
        menuExportAll.setActionCommand(ACTION_EXPORT);
        menuExportAll.addActionListener(this);
        JMenuItem menuExportSel = new JMenuItem(translate("menu.file.export.selection"));
        menuExportSel.setActionCommand(ACTION_EXPORT_SEL);
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
        miClose.setActionCommand(ACTION_EXIT);
        miClose.addActionListener(this);
        menuFile.add(miClose);
        menuBar.add(menuFile);
        JMenu menuDeobfuscation = new JMenu(translate("menu.tools.deobfuscation"));
        menuDeobfuscation.setIcon(View.getIcon("deobfuscate16"));

        JMenuItem miDeobfuscation = new JMenuItem(translate("menu.tools.deobfuscation.pcode"));
        miDeobfuscation.setActionCommand(ACTION_DEOBFUSCATE);
        miDeobfuscation.addActionListener(this);

        miAutoDeobfuscation.setSelected(Configuration.autoDeobfuscate.get());
        miAutoDeobfuscation.addActionListener(this);
        miAutoDeobfuscation.setActionCommand(ACTION_AUTO_DEOBFUSCATE);

        JMenuItem miRenameOneIdentifier = new JMenuItem(translate("menu.tools.deobfuscation.globalrename"));
        miRenameOneIdentifier.setActionCommand(ACTION_RENAME_ONE_IDENTIFIER);
        miRenameOneIdentifier.addActionListener(this);

        JMenuItem miRenameIdentifiers = new JMenuItem(translate("menu.tools.deobfuscation.renameinvalid"));
        miRenameIdentifiers.setActionCommand(ACTION_RENAME_IDENTIFIERS);
        miRenameIdentifiers.addActionListener(this);


        menuDeobfuscation.add(miRenameOneIdentifier);
        menuDeobfuscation.add(miRenameIdentifiers);
        menuDeobfuscation.add(miDeobfuscation);
        JMenu menuTools = new JMenu(translate("menu.tools"));
        JMenuItem miProxy = new JMenuItem(translate("menu.tools.proxy"));
        miProxy.setActionCommand(ACTION_SHOW_PROXY);
        miProxy.setIcon(View.getIcon("proxy16"));
        miProxy.addActionListener(this);

        JMenuItem miSearchScript = new JMenuItem(translate("menu.tools.searchas"));
        miSearchScript.addActionListener(this);
        miSearchScript.setActionCommand(ACTION_SEARCH_AS);
        miSearchScript.setIcon(View.getIcon("search16"));

        menuTools.add(miSearchScript);

        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.setActionCommand(ACTION_INTERNAL_VIEWER_SWITCH);
        miInternalViewer.addActionListener(this);

        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.setActionCommand(ACTION_PARALLEL_SPEED_UP);
        miParallelSpeedUp.addActionListener(this);


        menuTools.add(miProxy);

        menuTools.add(menuDeobfuscation);

        JMenuItem miGotoDocumentClass = new JMenuItem(translate("menu.tools.gotodocumentclass"));
        miGotoDocumentClass.setActionCommand(ACTION_GOTO_DOCUMENT_CLASS);
        miGotoDocumentClass.addActionListener(this);
        menuBar.add(menuTools);

        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.setActionCommand(ACTION_DISABLE_DECOMPILATION);
        miDecompile.addActionListener(this);


        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.setActionCommand(ACTION_CACHE_ON_DISK);
        miCacheDisk.addActionListener(this);

        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.setActionCommand(ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP);
        miGotoMainClassOnStartup.addActionListener(this);

        /*JMenu menuSettings = new JMenu(translate("menu.settings"));
         menuSettings.add(autoDeobfuscateMenuItem);
         menuSettings.add(miInternalViewer);
         menuSettings.add(miParallelSpeedUp);
         menuSettings.add(miDecompile);
         menuSettings.add(miCacheDisk);
         menuSettings.add(miGotoMainClassOnStartup);*/

        miAssociate.setActionCommand(ACTION_ASSOCIATE);
        miAssociate.addActionListener(this);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());


        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.setActionCommand(ACTION_SET_LANGUAGE);
        miLanguage.addActionListener(this);

        /* if (Platform.isWindows()) {
         menuSettings.add(miAssociate);
         }
         menuSettings.add(miLanguage);

         menuBar.add(menuSettings);*/
        JMenu menuHelp = new JMenu(translate("menu.help"));
        JMenuItem miAbout = new JMenuItem(translate("menu.help.about"));
        miAbout.setIcon(View.getIcon("about16"));

        miAbout.setActionCommand(ACTION_ABOUT);
        miAbout.addActionListener(this);

        JMenuItem miCheckUpdates = new JMenuItem(translate("menu.help.checkupdates"));
        miCheckUpdates.setActionCommand(ACTION_CHECK_UPDATES);
        miCheckUpdates.setIcon(View.getIcon("update16"));
        miCheckUpdates.addActionListener(this);

        JMenuItem miHelpUs = new JMenuItem(translate("menu.help.helpus"));
        miHelpUs.setActionCommand(ACTION_HELP_US);
        miHelpUs.setIcon(View.getIcon("donate16"));
        miHelpUs.addActionListener(this);

        JMenuItem miHomepage = new JMenuItem(translate("menu.help.homepage"));
        miHomepage.setActionCommand(ACTION_HOMEPAGE);
        miHomepage.setIcon(View.getIcon("homepage16"));
        miHomepage.addActionListener(this);


        menuHelp.add(miCheckUpdates);
        menuHelp.add(miHelpUs);
        menuHelp.add(miHomepage);
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        //setJMenuBar(menuBar);
        
        if (hasAbc) {
            menuTools.add(miGotoDocumentClass);
        }
    }
    
    private RibbonApplicationMenu createMainMenu(boolean swfLoaded) {
        RibbonApplicationMenu mainMenu = new RibbonApplicationMenu();
        RibbonApplicationMenuEntryPrimary exportFlaMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportfla32"), translate("menu.file.export.fla"), new ActionRedirector(this, ACTION_EXPORT_FLA), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportAllMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("export32"), translate("menu.file.export.all"), new ActionRedirector(this, ACTION_EXPORT), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportSelMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportsel32"), translate("menu.file.export.selection"), new ActionRedirector(this, ACTION_EXPORT_SEL), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary checkUpdatesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("update32"), translate("menu.help.checkupdates"), new ActionRedirector(this, ACTION_CHECK_UPDATES), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary aboutMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("about32"), translate("menu.help.about"), new ActionRedirector(this, ACTION_ABOUT), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary openFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("open32"), translate("menu.file.open"), new ActionRedirector(this, ACTION_OPEN), JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
        openFileMenu.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
               targetPanel.removeAll();
               JCommandButtonPanel openHistoryPanel = new JCommandButtonPanel(CommandButtonDisplayState.MEDIUM);
               String groupName = translate("menu.recentFiles");
               openHistoryPanel.addButtonGroup(groupName);
               List<String> recentFiles = Configuration.getRecentFiles();
               int j = 0;
               for (int i = recentFiles.size() - 1; i >= 0; i--) {
                  String path = recentFiles.get(i); 
                  RecentFilesButton historyButton = new RecentFilesButton(j + "    " + path, null);
                  historyButton.fileName = path;
                  historyButton.addActionListener(new ActionListener() {

                      @Override
                      public void actionPerformed(ActionEvent ae) {
                          RecentFilesButton source = (RecentFilesButton) ae.getSource();
                          if (Main.openFile(source.fileName) == OpenFileResult.NOT_FOUND) {
                              if (View.showConfirmDialog(null, translate("message.confirm.recentFileNotFound"), translate("message.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                                  Configuration.removeRecentFile(source.fileName);
                              }
                          }
                      }
                  });
                  j++;
                  historyButton.setHorizontalAlignment(SwingUtilities.LEFT);
                  openHistoryPanel.addButtonToLastGroup(historyButton);
               }
               openHistoryPanel.setMaxButtonColumns(1);
               targetPanel.setLayout(new BorderLayout());
               targetPanel.add(openHistoryPanel, BorderLayout.CENTER);
            }
        });
        
        RibbonApplicationMenuEntryFooter exitMenu = new RibbonApplicationMenuEntryFooter(View.getResizableIcon("exit32"), translate("menu.file.exit"), new ActionRedirector(this, "EXIT"));

        mainMenu.addMenuEntry(openFileMenu);
        mainMenu.addMenuSeparator();
        mainMenu.addMenuEntry(exportFlaMenu);
        mainMenu.addMenuEntry(exportAllMenu);
        mainMenu.addMenuEntry(exportSelMenu);
        mainMenu.addMenuSeparator();
        mainMenu.addMenuEntry(checkUpdatesMenu);
        mainMenu.addMenuEntry(aboutMenu);
        mainMenu.addFooterEntry(exitMenu);
        mainMenu.addMenuSeparator();

        if (!swfLoaded) {
            exportAllMenu.setEnabled(false);
            exportFlaMenu.setEnabled(false);
            exportSelMenu.setEnabled(false);
        }

        return mainMenu;
    }

    private List<RibbonBandResizePolicy> getResizePolicies(JRibbonBand ribbonBand) {
        List<RibbonBandResizePolicy> resizePolicies = new ArrayList<>();
        resizePolicies.add(new CoreRibbonResizePolicies.Mirror(ribbonBand.getControlPanel()));
        resizePolicies.add(new IconRibbonBandResizePolicy(ribbonBand.getControlPanel()));
        return resizePolicies;
    }
    
    private RibbonTask createFileRibbonTask(boolean swfLoaded) {
        JRibbonBand editBand = new JRibbonBand(translate("menu.general"), null);
        editBand.setResizePolicies(getResizePolicies(editBand));
        JCommandButton openCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.open")), View.getResizableIcon("open32"));
        assignListener(openCommandButton, ACTION_OPEN);
        saveCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.save")), View.getResizableIcon("save32"));
        assignListener(saveCommandButton, ACTION_SAVE);
        JCommandButton saveasCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveas")), View.getResizableIcon("saveas16"));
        assignListener(saveasCommandButton, ACTION_SAVE_AS);

        JCommandButton reloadCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.reload")), View.getResizableIcon("reload16"));
        assignListener(reloadCommandButton, ACTION_RELOAD);

        editBand.addCommandButton(openCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveasCommandButton, RibbonElementPriority.MEDIUM);
        editBand.addCommandButton(reloadCommandButton, RibbonElementPriority.MEDIUM);
        saveCommandButton.setEnabled(!Main.readOnly);

        JRibbonBand exportBand = new JRibbonBand(translate("menu.export"), null);
        exportBand.setResizePolicies(getResizePolicies(exportBand));
        JCommandButton exportFlaCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.fla")), View.getResizableIcon("exportfla32"));
        assignListener(exportFlaCommandButton, ACTION_EXPORT_FLA);
        JCommandButton exportAllCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.all")), View.getResizableIcon("export16"));
        assignListener(exportAllCommandButton, ACTION_EXPORT);
        JCommandButton exportSelectionCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.selection")), View.getResizableIcon("exportsel16"));
        assignListener(exportSelectionCommandButton, ACTION_EXPORT_SEL);

        exportBand.addCommandButton(exportFlaCommandButton, RibbonElementPriority.TOP);
        exportBand.addCommandButton(exportAllCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportSelectionCommandButton, RibbonElementPriority.MEDIUM);

        if (!swfLoaded) {
            saveasCommandButton.setEnabled(false);
            exportAllCommandButton.setEnabled(false);
            exportFlaCommandButton.setEnabled(false);
            exportSelectionCommandButton.setEnabled(false);
            reloadCommandButton.setEnabled(false);
        }

        return new RibbonTask(translate("menu.file"), editBand, exportBand);
    }
    
    private RibbonTask createToolsRibbonTask(boolean swfLoaded, boolean hasAbc) {
        //----------------------------------------- TOOLS -----------------------------------
        
        JRibbonBand toolsBand = new JRibbonBand(translate("menu.tools"), null);
        toolsBand.setResizePolicies(getResizePolicies(toolsBand));

        JCommandButton searchCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchas")), View.getResizableIcon("search32"));
        assignListener(searchCommandButton, ACTION_SEARCH_AS);
        JCommandButton gotoDocumentClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.gotodocumentclass")), View.getResizableIcon("gotomainclass32"));
        assignListener(gotoDocumentClassCommandButton, ACTION_GOTO_DOCUMENT_CLASS);

        JCommandButton proxyCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.proxy")), View.getResizableIcon("proxy16"));
        assignListener(proxyCommandButton, ACTION_SHOW_PROXY);

        JCommandButton loadMemoryCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchmemory")), View.getResizableIcon("loadmemory16"));
        assignListener(loadMemoryCommandButton, ACTION_LOAD_MEMORY);

        JCommandButton loadCacheCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchcache")), View.getResizableIcon("loadcache16"));
        assignListener(loadCacheCommandButton, ACTION_LOAD_CACHE);

        toolsBand.addCommandButton(searchCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(gotoDocumentClassCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(proxyCommandButton, RibbonElementPriority.MEDIUM);
        toolsBand.addCommandButton(loadMemoryCommandButton, RibbonElementPriority.MEDIUM);
        toolsBand.addCommandButton(loadCacheCommandButton, RibbonElementPriority.MEDIUM);
        if (!ProcessTools.toolsAvailable()) {
            loadMemoryCommandButton.setEnabled(false);
        }
        JRibbonBand deobfuscationBand = new JRibbonBand(translate("menu.tools.deobfuscation"), null);
        deobfuscationBand.setResizePolicies(getResizePolicies(deobfuscationBand));

        JCommandButton deobfuscationCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.pcode")), View.getResizableIcon("deobfuscate32"));
        assignListener(deobfuscationCommandButton, ACTION_DEOBFUSCATE);
        JCommandButton globalrenameCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.globalrename")), View.getResizableIcon("rename16"));
        assignListener(globalrenameCommandButton, ACTION_RENAME_ONE_IDENTIFIER);
        JCommandButton renameinvalidCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.renameinvalid")), View.getResizableIcon("renameall16"));
        assignListener(renameinvalidCommandButton, ACTION_RENAME_IDENTIFIERS);

        deobfuscationBand.addCommandButton(deobfuscationCommandButton, RibbonElementPriority.TOP);
        deobfuscationBand.addCommandButton(globalrenameCommandButton, RibbonElementPriority.MEDIUM);
        deobfuscationBand.addCommandButton(renameinvalidCommandButton, RibbonElementPriority.MEDIUM);

        if (!swfLoaded) {
            renameinvalidCommandButton.setEnabled(false);
            globalrenameCommandButton.setEnabled(false);
            saveCommandButton.setEnabled(false);
            deobfuscationCommandButton.setEnabled(false);
            searchCommandButton.setEnabled(false);
        }

        if (!hasAbc) {
            gotoDocumentClassCommandButton.setEnabled(false);
            deobfuscationCommandButton.setEnabled(false);
            //miDeobfuscation.setEnabled(false);
        }
        
        return new RibbonTask(translate("menu.tools"), toolsBand, deobfuscationBand);
    }
    
    private RibbonTask createSettingsRibbonTask() {
        //----------------------------------------- SETTINGS -----------------------------------
        
        JRibbonBand settingsBand = new JRibbonBand(translate("menu.settings"), null);
        settingsBand.setResizePolicies(getResizePolicies(settingsBand));

        miAutoDeobfuscation = new JCheckBox(translate("menu.settings.autodeobfuscation"));

        miInternalViewer = new JCheckBox(translate("menu.settings.internalflashviewer"));
        miParallelSpeedUp = new JCheckBox(translate("menu.settings.parallelspeedup"));
        miDecompile = new JCheckBox(translate("menu.settings.disabledecompilation"));
        miAssociate = new JCheckBox(translate("menu.settings.addtocontextmenu"));
        miCacheDisk = new JCheckBox(translate("menu.settings.cacheOnDisk"));
        miGotoMainClassOnStartup = new JCheckBox(translate("menu.settings.gotoMainClassOnStartup"));

        settingsBand.addRibbonComponent(new JRibbonComponent(miAutoDeobfuscation));
        settingsBand.addRibbonComponent(new JRibbonComponent(miInternalViewer));
        settingsBand.addRibbonComponent(new JRibbonComponent(miParallelSpeedUp));
        settingsBand.addRibbonComponent(new JRibbonComponent(miDecompile));
        settingsBand.addRibbonComponent(new JRibbonComponent(miAssociate));
        settingsBand.addRibbonComponent(new JRibbonComponent(miCacheDisk));
        settingsBand.addRibbonComponent(new JRibbonComponent(miGotoMainClassOnStartup));

        JRibbonBand languageBand = new JRibbonBand(translate("menu.language"), null);
        List<RibbonBandResizePolicy> languageBandResizePolicies = new ArrayList<>();
        languageBandResizePolicies.add(new BaseRibbonBandResizePolicy<AbstractBandControlPanel>(languageBand.getControlPanel()) {
            @Override
            public int getPreferredWidth(int i, int i1) {
                return 105;
            }

            @Override
            public void install(int i, int i1) {
            }
        });
        languageBandResizePolicies.add(new IconRibbonBandResizePolicy(languageBand.getControlPanel()));
        languageBand.setResizePolicies(languageBandResizePolicies);
        JCommandButton setLanguageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.settings.language")), View.getResizableIcon("setlanguage32"));
        assignListener(setLanguageCommandButton, ACTION_SET_LANGUAGE);
        languageBand.addCommandButton(setLanguageCommandButton, RibbonElementPriority.TOP);

        JRibbonBand advancedSettingsBand = new JRibbonBand(translate("menu.advancedsettings.advancedsettings"), null);
        advancedSettingsBand.setResizePolicies(getResizePolicies(advancedSettingsBand));
        JCommandButton advancedSettingsCommandButton = new JCommandButton(fixCommandTitle(translate("menu.advancedsettings.advancedsettings")), View.getResizableIcon("settings16"));
        assignListener(advancedSettingsCommandButton, ACTION_ADVANCED_SETTINGS);

        advancedSettingsBand.addCommandButton(advancedSettingsCommandButton, RibbonElementPriority.MEDIUM);
        
        return new RibbonTask(translate("menu.settings"), settingsBand, languageBand, advancedSettingsBand);
    }
    
    private RibbonTask createHelpRibbonTask() {
        //----------------------------------------- HELP -----------------------------------

        JRibbonBand helpBand = new JRibbonBand(translate("menu.help"), null);
        helpBand.setResizePolicies(getResizePolicies(helpBand));

        JCommandButton checkForUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.checkupdates")), View.getResizableIcon("update16"));
        assignListener(checkForUpdatesCommandButton, ACTION_CHECK_UPDATES);
        JCommandButton helpUsUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.helpus")), View.getResizableIcon("donate32"));
        assignListener(helpUsUpdatesCommandButton, ACTION_HELP_US);
        JCommandButton homepageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.homepage")), View.getResizableIcon("homepage16"));
        assignListener(homepageCommandButton, ACTION_HOMEPAGE);
        JCommandButton aboutCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.about")), View.getResizableIcon("about32"));
        assignListener(aboutCommandButton, ACTION_ABOUT);

        helpBand.addCommandButton(aboutCommandButton, RibbonElementPriority.TOP);
        helpBand.addCommandButton(checkForUpdatesCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(homepageCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(helpUsUpdatesCommandButton, RibbonElementPriority.TOP);
        return new RibbonTask(translate("menu.help"), helpBand);
    }
    
    private RibbonTask createDebugRibbonTask() {
        //----------------------------------------- DEBUG -----------------------------------

        JRibbonBand debugBand = new JRibbonBand("Debug", null);
        debugBand.setResizePolicies(getResizePolicies(debugBand));

        JCommandButton removeNonScriptsCommandButton = new JCommandButton(fixCommandTitle("Remove non scripts"), View.getResizableIcon("update16"));
        assignListener(removeNonScriptsCommandButton, ACTION_REMOVE_NON_SCRIPTS);

        JCommandButton refreshDecompiledCommandButton = new JCommandButton(fixCommandTitle("Refresh decompiled script"), View.getResizableIcon("update16"));
        assignListener(refreshDecompiledCommandButton, ACTION_REFRESH_DECOMPILED);

        debugBand.addCommandButton(removeNonScriptsCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(refreshDecompiledCommandButton, RibbonElementPriority.MEDIUM);
        return new RibbonTask("Debug", debugBand);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_RELOAD:
                if (View.showConfirmDialog(null, translate("message.confirm.reload"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    Main.reloadSWF();
                }
                break;
            case ACTION_ADVANCED_SETTINGS:
                Main.advancedSettings();
                break;
            case ACTION_LOAD_MEMORY:
                Main.loadFromMemory();
                break;
            case ACTION_LOAD_CACHE:
                Main.loadFromCache();
                break;
            case ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP:
                Configuration.gotoMainClassOnStartup.set(miGotoMainClassOnStartup.isSelected());
                break;
            case ACTION_CACHE_ON_DISK:
                Configuration.cacheOnDisk.set(miCacheDisk.isSelected());
                if (miCacheDisk.isSelected()) {
                    Cache.setStorageType(Cache.STORAGE_FILES);
                } else {
                    Cache.setStorageType(Cache.STORAGE_MEMORY);
                }
                break;
            case ACTION_SET_LANGUAGE:
                new SelectLanguageDialog().display();
                break;
            case ACTION_DISABLE_DECOMPILATION:
                Configuration.decompile.set(!miDecompile.isSelected());
                mainFrame.disableDecompilationChanged();
                break;
            case ACTION_ASSOCIATE:
                if (miAssociate.isSelected() == ContextMenuTools.isAddedToContextMenu()) {
                    return;
                }
                ContextMenuTools.addToContextMenu(miAssociate.isSelected());

                //Update checkbox menuitem accordingly (User can cancel rights elevation)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());
                    }
                }, 1000); //It takes some time registry change to apply
                break;
            case ACTION_GOTO_DOCUMENT_CLASS:
                mainFrame.gotoDocumentClass();
                break;
            case ACTION_PARALLEL_SPEED_UP:
                String confStr = translate("message.confirm.parallel") + "\r\n";
                if (miParallelSpeedUp.isSelected()) {
                    confStr += " " + translate("message.confirm.on");
                } else {
                    confStr += " " + translate("message.confirm.off");
                }
                if (View.showConfirmDialog(null, confStr, translate("message.parallel"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.parallelSpeedUp.set((Boolean) miParallelSpeedUp.isSelected());
                } else {
                    miParallelSpeedUp.setSelected(!miParallelSpeedUp.isSelected());
                }
                break;
            case ACTION_INTERNAL_VIEWER_SWITCH:
                Configuration.internalFlashViewer.set(miInternalViewer.isSelected());
                mainFrame.reload(true);
                break;
            case ACTION_SEARCH_AS:
                mainFrame.searchAs();
                break;
            case ACTION_AUTO_DEOBFUSCATE:
                if (View.showConfirmDialog(mainFrame, translate("message.confirm.autodeobfuscate") + "\r\n" + (miAutoDeobfuscation.isSelected() ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.autoDeobfuscate.set(miAutoDeobfuscation.isSelected());
                    mainFrame.autoDeobfuscateChanged();
                } else {
                    miAutoDeobfuscation.setSelected(!miAutoDeobfuscation.isSelected());
                }
                break;
            case ACTION_EXIT:
                mainFrame.setVisible(false);
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
            case ACTION_RENAME_ONE_IDENTIFIER:
                mainFrame.renameOneIdentifier();
                break;
            case ACTION_ABOUT:
                Main.about();
                break;
            case ACTION_SHOW_PROXY:
                Main.showProxy();
                break;
            case ACTION_SUB_LIMITER:
                if (e.getSource() instanceof JCheckBoxMenuItem) {
                    Main.setSubLimiter(((JCheckBoxMenuItem) e.getSource()).getState());
                }
                break;
            case ACTION_SAVE:
                try {
                    Main.saveFile(Main.file);
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameRibbon.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ACTION_SAVE_AS:
                if (Main.saveFileDialog()) {
                    mainFrame.setTitle(ApplicationInfo.applicationVerName + (Configuration.displayFileName.get() ? " - " + Main.getFileTitle() : ""));
                    saveCommandButton.setEnabled(!Main.readOnly);
                }
                break;
            case ACTION_OPEN:
                Main.openFileDialog();
                break;
            case ACTION_EXPORT_FLA:
                mainFrame.exportFla();
                break;
            case ACTION_EXPORT_SEL:
            case ACTION_EXPORT:
                boolean onlySel = e.getActionCommand().endsWith("SEL");
                mainFrame.export(onlySel);
                break;
            case ACTION_CHECK_UPDATES:
                if (!Main.checkForUpdates()) {
                    View.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case ACTION_HELP_US:
                String helpUsURL = ApplicationInfo.PROJECT_PAGE + "/help_us.html";
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        java.net.URI uri = new java.net.URI(helpUsURL);
                        desktop.browse(uri);
                    } catch (URISyntaxException | IOException ex) {
                    }
                } else {
                    View.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
                }
                break;
            case ACTION_HOMEPAGE:
                String homePageURL = ApplicationInfo.PROJECT_PAGE;
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    try {
                        java.net.URI uri = new java.net.URI(homePageURL);
                        desktop.browse(uri);
                    } catch (URISyntaxException | IOException ex) {
                    }
                } else {
                    View.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
                }
                break;
            case ACTION_RESTORE_CONTROL_FLOW:
            case ACTION_RESTORE_CONTROL_FLOW_ALL:
                boolean all = e.getActionCommand().endsWith("ALL");
                mainFrame.restoreControlFlow(all);
                break;
            case ACTION_RENAME_IDENTIFIERS:
                mainFrame.renameIdentifiers();
                break;
            case ACTION_DEOBFUSCATE:
            case ACTION_DEOBFUSCATE_ALL:
                mainFrame.deobfuscate();
                break;
            case ACTION_REMOVE_NON_SCRIPTS:
                mainFrame.removeNonScripts();
                break;
            case ACTION_REFRESH_DECOMPILED:
                mainFrame.refreshDecompiled();
                break;
        }
    }

}
