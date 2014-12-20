/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.helpers.Cache;
import com.sun.jna.Platform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public class MainFrameClassicMenu extends MainFrameMenu implements ActionListener {

    private static final String ACTION_RELOAD = "RELOAD";
    private static final String ACTION_ADVANCED_SETTINGS = "ADVANCEDSETTINGS";
    private static final String ACTION_LOAD_MEMORY = "LOADMEMORY";
    private static final String ACTION_LOAD_CACHE = "LOADCACHE";
    private static final String ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP = "GOTODOCUMENTCLASSONSTARTUP";
    private static final String ACTION_AUTO_RENAME_IDENTIFIERS = "AUTORENAMEIDENTIFIERS";
    private static final String ACTION_CACHE_ON_DISK = "CACHEONDISK";
    private static final String ACTION_SET_LANGUAGE = "SETLANGUAGE";
    private static final String ACTION_DISABLE_DECOMPILATION = "DISABLEDECOMPILATION";
    private static final String ACTION_ASSOCIATE = "ASSOCIATE";
    private static final String ACTION_GOTO_DOCUMENT_CLASS = "GOTODOCUMENTCLASS";
    private static final String ACTION_PARALLEL_SPEED_UP = "PARALLELSPEEDUP";
    private static final String ACTION_INTERNAL_VIEWER_SWITCH = "INTERNALVIEWERSWITCH";
    private static final String ACTION_SEARCH = "SEARCH";
    private static final String ACTION_AUTO_DEOBFUSCATE = "AUTODEOBFUSCATE";
    private static final String ACTION_EXIT = "EXIT";

    private static final String ACTION_RENAME_ONE_IDENTIFIER = "RENAMEONEIDENTIFIER";
    private static final String ACTION_ABOUT = "ABOUT";
    private static final String ACTION_SHOW_PROXY = "SHOWPROXY";
    private static final String ACTION_SUB_LIMITER = "SUBLIMITER";
    private static final String ACTION_SAVE = "SAVE";
    private static final String ACTION_SAVE_AS = "SAVEAS";
    private static final String ACTION_SAVE_AS_EXE = "SAVEASEXE";
    private static final String ACTION_OPEN = "OPEN";
    private static final String ACTION_EXPORT_FLA = "EXPORTFLA";
    private static final String ACTION_EXPORT_SEL = "EXPORTSEL";
    private static final String ACTION_EXPORT = "EXPORT";
    private static final String ACTION_CHECK_UPDATES = "CHECKUPDATES";
    private static final String ACTION_HELP_US = "HELPUS";
    private static final String ACTION_HOMEPAGE = "HOMEPAGE";
    private static final String ACTION_RESTORE_CONTROL_FLOW = "RESTORECONTROLFLOW";
    private static final String ACTION_RESTORE_CONTROL_FLOW_ALL = "RESTORECONTROLFLOWALL";
    private static final String ACTION_RENAME_IDENTIFIERS = "RENAMEIDENTIFIERS";
    private static final String ACTION_DEOBFUSCATE = "DEOBFUSCATE";
    private static final String ACTION_DEOBFUSCATE_ALL = "DEOBFUSCATEALL";
    private static final String ACTION_REMOVE_NON_SCRIPTS = "REMOVENONSCRIPTS";
    private static final String ACTION_REFRESH_DECOMPILED = "REFRESHDECOMPILED";

    private final MainFrameClassic mainFrame;

    private JCheckBoxMenuItem miAutoDeobfuscation;
    private JCheckBoxMenuItem miInternalViewer;
    private JCheckBoxMenuItem miParallelSpeedUp;
    private JCheckBoxMenuItem miAssociate;
    private JCheckBoxMenuItem miDecompile;
    private JCheckBoxMenuItem miCacheDisk;
    private JCheckBoxMenuItem miGotoMainClassOnStartup;
    private JCheckBoxMenuItem miAutoRenameIdentifiers;
    private JMenuItem saveCommandButton;
    private JMenuItem saveasCommandButton;
    private JMenuItem saveasexeCommandButton;
    private JMenuItem exportAllCommandButton;
    private JMenuItem exportFlaCommandButton;
    private JMenuItem exportSelectionCommandButton;

    private JMenuItem reloadCommandButton;
    private JMenuItem renameInvalidCommandButton;
    private JMenuItem globalRenameCommandButton;
    private JMenuItem deobfuscationCommandButton;
    private JMenuItem searchCommandButton;
    private JMenuItem gotoDocumentClassCommandButton;

    public MainFrameClassicMenu(MainFrameClassic mainFrame, boolean externalFlashPlayerUnavailable) {
        super(mainFrame);
        this.mainFrame = mainFrame;

        createMenuBar(externalFlashPlayerUnavailable);
    }

    @Override
    public boolean isInternalFlashViewerSelected() {
        return miInternalViewer.isSelected();
    }

    private void assignListener(JMenuItem b, final String command) {
        final MainFrameClassicMenu t = this;
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

    private void createMenuBar(boolean externalFlashPlayerUnavailable) {
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
        JMenuItem miSaveAsExe = new JMenuItem(translate("menu.file.saveasexe"));
        miSaveAsExe.setIcon(View.getIcon("saveas16"));
        miSaveAsExe.setActionCommand(ACTION_SAVE_AS_EXE);
        miSaveAsExe.addActionListener(this);

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
        menuFile.add(miSaveAsExe);
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

        miAutoDeobfuscation = new JCheckBoxMenuItem(translate("menu.settings.autodeobfuscation"));
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
        miSearchScript.setActionCommand(ACTION_SEARCH);
        miSearchScript.setIcon(View.getIcon("search16"));

        menuTools.add(miSearchScript);

        miInternalViewer = new JCheckBoxMenuItem(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.setActionCommand(ACTION_INTERNAL_VIEWER_SWITCH);
        miInternalViewer.addActionListener(this);

        miParallelSpeedUp = new JCheckBoxMenuItem(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.setActionCommand(ACTION_PARALLEL_SPEED_UP);
        miParallelSpeedUp.addActionListener(this);

        menuTools.add(miProxy);

        menuTools.add(menuDeobfuscation);

        JMenuItem miGotoDocumentClass = new JMenuItem(translate("menu.tools.gotodocumentclass"));
        miGotoDocumentClass.setActionCommand(ACTION_GOTO_DOCUMENT_CLASS);
        miGotoDocumentClass.addActionListener(this);
        menuBar.add(menuTools);

        miDecompile = new JCheckBoxMenuItem(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.setActionCommand(ACTION_DISABLE_DECOMPILATION);
        miDecompile.addActionListener(this);

        miCacheDisk = new JCheckBoxMenuItem(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.setActionCommand(ACTION_CACHE_ON_DISK);
        miCacheDisk.addActionListener(this);

        miGotoMainClassOnStartup = new JCheckBoxMenuItem(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.setActionCommand(ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP);
        miGotoMainClassOnStartup.addActionListener(this);

        miAutoRenameIdentifiers = new JCheckBoxMenuItem(translate("menu.settings.autoRenameIdentifiers"));
        miAutoRenameIdentifiers.setSelected(Configuration.autoRenameIdentifiers.get());
        miAutoRenameIdentifiers.setActionCommand(ACTION_AUTO_RENAME_IDENTIFIERS);
        miAutoRenameIdentifiers.addActionListener(this);

        JMenu menuSettings = new JMenu(translate("menu.settings"));
        menuSettings.add(miAutoDeobfuscation);
        menuSettings.add(miInternalViewer);
        menuSettings.add(miParallelSpeedUp);
        menuSettings.add(miDecompile);
        menuSettings.add(miCacheDisk);
        menuSettings.add(miGotoMainClassOnStartup);
        menuSettings.add(miAutoRenameIdentifiers);

        miAssociate = new JCheckBoxMenuItem(translate("menu.settings.addtocontextmenu"));
        miAssociate.setActionCommand(ACTION_ASSOCIATE);
        miAssociate.addActionListener(this);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());

        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.setActionCommand(ACTION_SET_LANGUAGE);
        miLanguage.addActionListener(this);

        if (Platform.isWindows()) {
            menuSettings.add(miAssociate);
        }
        menuSettings.add(miLanguage);

        JMenuItem advancedSettingsCommandButton = new JMenuItem(translate("menu.advancedsettings.advancedsettings"));
        advancedSettingsCommandButton.setActionCommand(ACTION_ADVANCED_SETTINGS);
        advancedSettingsCommandButton.setIcon(View.getIcon("settings16"));
        advancedSettingsCommandButton.addActionListener(this);
        menuSettings.add(advancedSettingsCommandButton);

        menuBar.add(menuSettings);
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

        mainFrame.setJMenuBar(menuBar);

        //if (hasAbc) {
        menuTools.add(miGotoDocumentClass);
        //}
    }

    @Override
    public void updateComponents(SWF swf) {
        super.updateComponents(swf);
        boolean swfLoaded = swf != null;
        List<ABCContainerTag> abcList = swfLoaded ? swf.abcList : null;
        boolean hasAbc = swfLoaded && abcList != null && !abcList.isEmpty();

        /*saveCommandButton.setEnabled(swfLoaded);
         saveasCommandButton.setEnabled(swfLoaded);
         saveasexeCommandButton.setEnabled(swfLoaded);
         exportAllCommandButton.setEnabled(swfLoaded);
         exportFlaCommandButton.setEnabled(swfLoaded);
         exportSelectionCommandButton.setEnabled(swfLoaded);
         reloadCommandButton.setEnabled(swfLoaded);

         renameInvalidCommandButton.setEnabled(swfLoaded);
         globalRenameCommandButton.setEnabled(swfLoaded);
         deobfuscationCommandButton.setEnabled(swfLoaded);
         searchCommandButton.setEnabled(swfLoaded);

         gotoDocumentClassCommandButton.setEnabled(hasAbc);
         deobfuscationCommandButton.setEnabled(hasAbc);*/
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_RELOAD:
                reload();
                break;
            case ACTION_ADVANCED_SETTINGS:
                advancedSettings();
                break;
            case ACTION_LOAD_MEMORY:
                loadFromMemory();
                break;
            case ACTION_LOAD_CACHE:
                loadFromCache();
                break;
            case ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP:
                Configuration.gotoMainClassOnStartup.set(miGotoMainClassOnStartup.isSelected());
                break;
            case ACTION_AUTO_RENAME_IDENTIFIERS:
                Configuration.autoRenameIdentifiers.set(miAutoRenameIdentifiers.isSelected());
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
                setLanguage();
                break;
            case ACTION_DISABLE_DECOMPILATION:
                Configuration.decompile.set(!miDecompile.isSelected());
                mainFrame.getPanel().disableDecompilationChanged();
                break;
            case ACTION_ASSOCIATE:
                if (miAssociate.isSelected() == ContextMenuTools.isAddedToContextMenu()) {
                    return;
                }
                ContextMenuTools.addToContextMenu(miAssociate.isSelected(), false);

                // Update checkbox menuitem accordingly (User can cancel rights elevation)
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());
                    }
                }, 1000); // It takes some time registry change to apply
                break;
            case ACTION_GOTO_DOCUMENT_CLASS:
                mainFrame.getPanel().gotoDocumentClass(mainFrame.getPanel().getCurrentSwf());
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
                mainFrame.getPanel().reload(true);
                break;
            case ACTION_SEARCH:
                search(false);
                break;
            case ACTION_AUTO_DEOBFUSCATE:
                if (View.showConfirmDialog(mainFrame.getPanel(), translate("message.confirm.autodeobfuscate") + "\r\n" + (miAutoDeobfuscation.isSelected() ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.autoDeobfuscate.set(miAutoDeobfuscation.isSelected());
                    mainFrame.getPanel().autoDeobfuscateChanged();
                } else {
                    miAutoDeobfuscation.setSelected(!miAutoDeobfuscation.isSelected());
                }
                break;
            case ACTION_EXIT:
                exit();
                break;
        }

        if (Main.isWorking()) {
            return;
        }

        switch (e.getActionCommand()) {
            case ACTION_RENAME_ONE_IDENTIFIER:
                renameOneIdentifier();
                break;
            case ACTION_SHOW_PROXY:
                showProxy();
                break;
            case ACTION_SUB_LIMITER:
                setSubLimiter(((JCheckBoxMenuItem) e.getSource()).isSelected());
                break;
            case ACTION_SAVE:
                save();
                break;
            case ACTION_SAVE_AS:
                saveAs();
                break;
            case ACTION_SAVE_AS_EXE:
                saveAsExe();
                break;
            case ACTION_OPEN:
                open();
                break;
            case ACTION_EXPORT_SEL:
            case ACTION_EXPORT:
                boolean onlySel = e.getActionCommand().equals(ACTION_EXPORT_SEL);
                export(onlySel);
                break;
            case ACTION_EXPORT_FLA:
                exportFla();
                break;
            case ACTION_CHECK_UPDATES:
                checkUpdates();
                break;
            case ACTION_HELP_US:
                helpUs();
                break;
            case ACTION_HOMEPAGE:
                homePage();
                break;
            case ACTION_ABOUT:
                about();
                break;
            case ACTION_RESTORE_CONTROL_FLOW:
            case ACTION_RESTORE_CONTROL_FLOW_ALL:
                boolean all = e.getActionCommand().equals(ACTION_RESTORE_CONTROL_FLOW_ALL);
                restoreControlFlow(all);
                break;
            case ACTION_RENAME_IDENTIFIERS:
                renameIdentifiers();
                break;
            case ACTION_DEOBFUSCATE:
            case ACTION_DEOBFUSCATE_ALL:
                deobfuscate();
                break;
            case ACTION_REMOVE_NON_SCRIPTS:
                removeNonScripts();
                break;
            case ACTION_REFRESH_DECOMPILED:
                refreshDecompiled();
                break;
        }
    }

}
