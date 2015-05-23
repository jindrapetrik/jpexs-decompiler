/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.sun.jna.Platform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author JPEXS
 */
public class MainFrameClassicMenu extends MainFrameMenu {

    private final MainFrameClassic mainFrame;

    private JCheckBoxMenuItem miAutoDeobfuscation;

    private JCheckBoxMenuItem miInternalViewer;

    private JCheckBoxMenuItem miParallelSpeedUp;

    private JCheckBoxMenuItem miAssociate;

    private JCheckBoxMenuItem miDecompile;

    private JCheckBoxMenuItem miCacheDisk;

    private JCheckBoxMenuItem miGotoMainClassOnStartup;

    private JCheckBoxMenuItem miAutoRenameIdentifiers;
    private ButtonGroup viewGroup;

    private JRadioButtonMenuItem miViewResources;
    private JRadioButtonMenuItem miViewHex;

    private JCheckBoxMenuItem miDebuggerSwitch;
    private JMenuItem miDebuggerReplaceTrace;

    private JCheckBoxMenuItem miViewTimeline;

    private ButtonGroup grpDeobfuscation;
    private JRadioButtonMenuItem miDeobfuscationModeOld;
    private JRadioButtonMenuItem miDeobfuscationModeNew;
    private JMenu menuRecent;

    private JMenuItem miReload;
    private JMenuItem miExportAll;
    private JMenuItem miExportXml;
    private JMenuItem miExportSel;
    private JMenuItem miExportFla;
    private JMenuItem miSave;
    private JMenuItem miSaveAs;
    private JMenuItem miSaveAsExe;
    private JMenuItem miClose;
    private JMenuItem miCloseAll;

    private JMenu menuExport;
    private JMenuItem miImportText;
    private JMenuItem miImportScript;
    private JMenuItem miImportSymbolClass;
    private JMenuItem miImportXml;
    private JMenu menuImport;
    private JMenuItem miRenameIdentifiers;
    private JMenuItem miRenameOneIdentifier;

    private JMenuItem miReplace;
    private JMenuItem miSearch;
    private JMenuItem miDeobfuscation;
    private JMenuItem miGotoDocumentClass;

    public MainFrameClassicMenu(MainFrameClassic mainFrame, boolean externalFlashPlayerUnavailable) {
        super(mainFrame);
        this.mainFrame = mainFrame;

        createMenuBar(externalFlashPlayerUnavailable);
    }

    @Override
    public boolean isInternalFlashViewerSelected() {
        return miInternalViewer.isSelected();
    }

    private String fixCommandTitle(String title) {
        if (title.length() > 2) {
            if (title.charAt(1) == ' ') {
                title = title.charAt(0) + "\u00A0" + title.substring(2);
            }
        }
        return title;
    }

    private void loadRecent() {
        List<String> recentFiles = Configuration.getRecentFiles();
        menuRecent.removeAll();
        for (int i = recentFiles.size() - 1; i >= 0; i--) {
            JMenuItem miRecent = new JMenuItem(recentFiles.get(i));
            final String f = recentFiles.get(i);
            miRecent.addActionListener((ActionEvent e) -> {
                if (Main.openFile(f, null) == OpenFileResult.NOT_FOUND) {
                    if (View.showConfirmDialog(null, translate("message.confirm.recentFileNotFound"), translate("message.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                        Configuration.removeRecentFile(f);
                    }
                }
            });
            menuRecent.add(miRecent);
        }
    }

    private void createMenuBar(boolean externalFlashPlayerUnavailable) {
        JMenuBar menuBar = new JMenuBar();

        JMenuItem miOpen = new JMenuItem(translate("menu.file.open"));
        miOpen.setIcon(View.getIcon("open16"));
        miOpen.addActionListener(this::open);

        menuRecent = new JMenu(translate("menu.recentFiles"));

        miSave = new JMenuItem(translate("menu.file.save"));
        miSave.setIcon(View.getIcon("save16"));
        miSave.addActionListener(this::save);
        miSaveAs = new JMenuItem(translate("menu.file.saveas"));
        miSaveAs.setIcon(View.getIcon("saveas16"));
        miSaveAs.addActionListener(this::saveAs);
        miSaveAsExe = new JMenuItem(translate("menu.file.saveasexe"));
        miSaveAsExe.setIcon(View.getIcon("saveasexe16"));
        miSaveAsExe.addActionListener(this::saveAsExe);

        miReload = new JMenuItem(translate("menu.file.reload"));
        miReload.setIcon(View.getIcon("reload16"));
        miReload.addActionListener(this::reload);

        miExportFla = new JMenuItem(translate("menu.file.export.fla"));
        miExportFla.addActionListener(this::exportFla);
        miExportFla.setIcon(View.getIcon("flash16"));

        miExportAll = new JMenuItem(translate("menu.file.export.all"));
        miExportAll.addActionListener(this::exportAll);
        miExportAll.setIcon(View.getIcon("export16"));

        miExportSel = new JMenuItem(translate("menu.file.export.selection"));
        miExportSel.addActionListener(this::exportSelected);
        miExportSel.setIcon(View.getIcon("exportsel16"));

        miExportXml = new JMenuItem(translate("menu.file.export.xml"));
        miExportXml.addActionListener(this::exportSwfXml);
        miExportXml.setIcon(View.getIcon("exportxml32", 16));

        menuExport = new JMenu(translate("menu.export"));
        menuExport.add(miExportFla);
        menuExport.add(miExportXml);
        menuExport.addSeparator();
        menuExport.add(miExportAll);
        menuExport.add(miExportSel);

        miImportText = new JMenuItem(translate("menu.file.import.text"));
        miImportText.addActionListener(this::importText);
        miImportText.setIcon(View.getIcon("importtext32", 16));

        miImportScript = new JMenuItem(translate("menu.file.import.script"));
        miImportScript.addActionListener(this::importScript);
        miImportScript.setIcon(View.getIcon("importtext32", 16));

        miImportSymbolClass = new JMenuItem(translate("menu.file.import.symbolClass"));
        miImportSymbolClass.addActionListener(this::importSymbolClass);
        miImportSymbolClass.setIcon(View.getIcon("importsymbolclass32", 16));

        miImportXml = new JMenuItem(translate("menu.file.import.xml"));
        miImportXml.addActionListener(this::importSwfXml);
        miImportXml.setIcon(View.getIcon("importxml32", 16));
        menuImport = new JMenu(translate("menu.import"));
        menuImport.add(miImportXml);
        menuImport.add(miImportText);
        menuImport.add(miImportScript);
        menuImport.add(miImportSymbolClass);

        /*
         JMenuItem menuX;
         menuX = new JMenuItem(translate("menu.file.export.all"));
         menuX.addActionListener(this::exportAll);
         menuX.setIcon(View.getIcon("export16"));
         menu.file.close
         */
        miClose = new JMenuItem(translate("menu.file.close"));
        miClose.addActionListener(this::close);
        miClose.setIcon(View.getIcon("close32", 16));

        miCloseAll = new JMenuItem(translate("menu.file.closeAll"));
        miCloseAll.addActionListener(this::closeAll);
        miCloseAll.setIcon(View.getIcon("close32", 16));

        JMenuItem miExit = new JMenuItem(translate("menu.file.exit"));
        miExit.setIcon(View.getIcon("exit16"));
        miExit.addActionListener(this::exit);

        JMenu menuFile = new JMenu(translate("menu.file"));

        menuFile.addMenuListener(new MenuListener() {

            @Override
            public void menuSelected(MenuEvent e) {
                loadRecent();
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });

        menuFile.add(miOpen);
        menuFile.add(menuRecent);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(miSaveAsExe);
        menuFile.add(miReload);
        menuFile.addSeparator();

        menuFile.add(menuExport);
        menuFile.add(menuImport);
        menuFile.addSeparator();

        menuFile.add(miClose);
        menuFile.add(miCloseAll);
        menuFile.addSeparator();

        menuFile.add(miExit);

        menuBar.add(menuFile);

        miViewResources = new JRadioButtonMenuItem(translate("menu.file.view.resources"));
        miViewResources.addActionListener(this::viewModeResourcesButtonActionPerformed);
        miViewResources.setIcon(View.getIcon("viewresources16"));

        miViewHex = new JRadioButtonMenuItem(translate("menu.file.view.hex"));
        miViewHex.addActionListener(this::viewModeHexDumpButtonActionPerformed);
        miViewHex.setIcon(View.getIcon("viewhex16"));

        viewGroup = new ButtonGroup();
        viewGroup.add(miViewResources);
        viewGroup.add(miViewHex);

        if (Configuration.dumpView.get()) {
            miViewHex.setSelected(true);
        } else {
            miViewResources.setSelected(true);
        }

        JMenu menuView = new JMenu(translate("menu.view"));
        menuView.add(miViewResources);
        menuView.add(miViewHex);

        menuBar.add(menuView);
        miDebuggerSwitch = new JCheckBoxMenuItem(translate("menu.debugger.switch"));
        miDebuggerSwitch.addActionListener(this::debuggerSwitchButtonActionPerformed);
        miDebuggerSwitch.setIcon(View.getIcon("debugger32", 16));

        miDebuggerReplaceTrace = new JMenuItem(translate("menu.debugger.replacetrace"));
        miDebuggerReplaceTrace.addActionListener(this::debuggerReplaceTraceCalls);
        miDebuggerReplaceTrace.setIcon(View.getIcon("debuggerreplace16"));

        JMenuItem miDebuggerShowLog = new JMenuItem(translate("menu.debugger.showlog"));
        miDebuggerShowLog.addActionListener(this::debuggerShowLog);
        miDebuggerShowLog.setIcon(View.getIcon("debuggerlog16"));

        JMenu menuDebugger = new JMenu(translate("menu.debugger"));
        menuDebugger.add(miDebuggerSwitch);
        menuDebugger.add(miDebuggerReplaceTrace);
        menuDebugger.add(miDebuggerShowLog);

        JMenu menuDeobfuscation = new JMenu(translate("menu.tools.deobfuscation"));
        menuDeobfuscation.setIcon(View.getIcon("deobfuscate16"));

        miDeobfuscation = new JMenuItem(translate("menu.tools.deobfuscation.pcode"));
        miDeobfuscation.addActionListener(this::deobfuscate);

        miAutoDeobfuscation = new JCheckBoxMenuItem(translate("menu.settings.autodeobfuscation"));
        miAutoDeobfuscation.setSelected(Configuration.autoDeobfuscate.get());
        miAutoDeobfuscation.addActionListener(this::autoDeobfuscate);

        miRenameOneIdentifier = new JMenuItem(translate("menu.tools.deobfuscation.globalrename"));
        miRenameOneIdentifier.addActionListener(this::renameOneIdentifier);

        miRenameIdentifiers = new JMenuItem(translate("menu.tools.deobfuscation.renameinvalid"));
        miRenameIdentifiers.addActionListener(this::renameIdentifiers);

        menuDeobfuscation.add(miRenameOneIdentifier);
        menuDeobfuscation.add(miRenameIdentifiers);
        menuDeobfuscation.add(miDeobfuscation);
        JMenuItem miProxy = new JMenuItem(translate("menu.tools.proxy"));
        miProxy.setIcon(View.getIcon("proxy16"));
        miProxy.addActionListener(this::showProxy);

        JMenuItem miSearchMemory = new JMenuItem(translate("menu.tools.searchmemory"));
        miSearchMemory.addActionListener(this::loadFromMemory);
        miSearchMemory.setIcon(View.getIcon("loadmemory16"));

        JMenuItem miSearchCache = new JMenuItem(translate("menu.tools.searchcache"));
        miSearchCache.addActionListener(this::loadFromCache);
        miSearchCache.setIcon(View.getIcon("loadcache16"));

        miSearch = new JMenuItem(translate("menu.tools.search"));
        miSearch.addActionListener((ActionEvent e) -> {
            search(e, null);
        });
        miSearch.setIcon(View.getIcon("search16"));

        miReplace = new JMenuItem(translate("menu.tools.replace"));
        miReplace.addActionListener(this::replace);
        miReplace.setIcon(View.getIcon("replace32", 16));

        miViewTimeline = new JCheckBoxMenuItem(translate("menu.tools.timeline"));
        miViewTimeline.addActionListener(this::timelineButtonActionPerformed);
        miViewTimeline.setIcon(View.getIcon("timeline32", 16));

        miGotoDocumentClass = new JMenuItem(translate("menu.tools.gotodocumentclass"));
        miGotoDocumentClass.addActionListener(this::gotoDucumentClass);

        JMenu menuTools = new JMenu(translate("menu.tools"));
        menuTools.add(miSearch);
        menuTools.add(miReplace);
        menuTools.addSeparator();
        menuTools.add(miViewTimeline);
        menuTools.add(miProxy);
        menuTools.add(miSearchMemory);
        menuTools.add(miSearchCache);
        menuTools.addSeparator();
        menuTools.add(menuDeobfuscation);
        menuTools.add(menuDebugger);
        menuTools.add(miGotoDocumentClass);
        menuBar.add(menuTools);

        //Settings
        miInternalViewer = new JCheckBoxMenuItem(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.addActionListener(this::internalViewerSwitch);

        miParallelSpeedUp = new JCheckBoxMenuItem(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.addActionListener(this::parallelSpeedUp);

        miDecompile = new JCheckBoxMenuItem(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.addActionListener(this::disableDecompilation);

        miCacheDisk = new JCheckBoxMenuItem(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.addActionListener(this::cacheOnDisk);

        miGotoMainClassOnStartup = new JCheckBoxMenuItem(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.addActionListener(this::gotoDucumentClassOnStartup);

        miAutoRenameIdentifiers = new JCheckBoxMenuItem(translate("menu.settings.autoRenameIdentifiers"));
        miAutoRenameIdentifiers.setSelected(Configuration.autoRenameIdentifiers.get());
        miAutoRenameIdentifiers.addActionListener(this::autoRenameIdentifiers);

        JMenu menuSettings = new JMenu(translate("menu.settings"));
        menuSettings.add(miAutoDeobfuscation);
        menuSettings.add(miInternalViewer);
        menuSettings.add(miParallelSpeedUp);
        menuSettings.add(miDecompile);
        menuSettings.add(miCacheDisk);
        menuSettings.add(miGotoMainClassOnStartup);
        menuSettings.add(miAutoRenameIdentifiers);

        miAssociate = new JCheckBoxMenuItem(translate("menu.settings.addtocontextmenu"));
        miAssociate.addActionListener(this::associate);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());

        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.addActionListener(this::setLanguage);
        miLanguage.setIcon(View.getIcon("setlanguage32", 16));

        if (Platform.isWindows()) {
            menuSettings.add(miAssociate);
        }
        menuSettings.add(miLanguage);

        miDeobfuscationModeOld = new JRadioButtonMenuItem(translate("menu.file.deobfuscation.old"));
        miDeobfuscationModeOld.addActionListener((ActionEvent e) -> {
            deobfuscationMode(e, 0);
        });
        miDeobfuscationModeOld.setIcon(View.getIcon("deobfuscateold16"));

        miDeobfuscationModeNew = new JRadioButtonMenuItem(translate("menu.file.deobfuscation.new"));
        miDeobfuscationModeNew.addActionListener((ActionEvent e) -> {
            deobfuscationMode(e, 1);
        });
        miDeobfuscationModeNew.setIcon(View.getIcon("deobfuscatenew16"));

        grpDeobfuscation = new ButtonGroup();
        grpDeobfuscation.add(miDeobfuscationModeOld);
        grpDeobfuscation.add(miDeobfuscationModeNew);

        int deobfuscationMode = Configuration.deobfuscationMode.get();
        switch (deobfuscationMode) {
            case 0:
                grpDeobfuscation.clearSelection();
                miDeobfuscationModeOld.setSelected(true);
                break;
            case 1:
                grpDeobfuscation.clearSelection();
                miDeobfuscationModeNew.setSelected(true);
                break;
        }

        JMenu deobfuscationMenu = new JMenu(translate("menu.deobfuscation"));
        deobfuscationMenu.add(miDeobfuscationModeOld);
        deobfuscationMenu.add(miDeobfuscationModeNew);
        menuSettings.add(deobfuscationMenu);

        JMenuItem advancedSettingsCommandButton = new JMenuItem(translate("menu.advancedsettings.advancedsettings"));
        advancedSettingsCommandButton.setIcon(View.getIcon("settings16"));
        advancedSettingsCommandButton.addActionListener(this::advancedSettings);
        menuSettings.add(advancedSettingsCommandButton);

        menuBar.add(menuSettings);
        JMenu menuHelp = new JMenu(translate("menu.help"));
        JMenuItem miAbout = new JMenuItem(translate("menu.help.about"));
        miAbout.setIcon(View.getIcon("about16"));
        miAbout.addActionListener(this::about);

        JMenuItem miCheckUpdates = new JMenuItem(translate("menu.help.checkupdates"));
        miCheckUpdates.setIcon(View.getIcon("update16"));
        miCheckUpdates.addActionListener(this::checkUpdates);

        JMenuItem miHelpUs = new JMenuItem(translate("menu.help.helpus"));
        miHelpUs.setIcon(View.getIcon("donate16"));
        miHelpUs.addActionListener(this::helpUs);

        JMenuItem miHomepage = new JMenuItem(translate("menu.help.homepage"));
        miHomepage.setIcon(View.getIcon("homepage16"));
        miHomepage.addActionListener(this::homePage);

        menuHelp.add(miCheckUpdates);
        menuHelp.add(miHelpUs);
        menuHelp.add(miHomepage);
        menuHelp.addSeparator();
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        mainFrame.setJMenuBar(menuBar);

    }

    @Override
    public void updateComponents(SWF swf) {
        super.updateComponents(swf);
        boolean swfLoaded = swf != null;
        List<ABCContainerTag> abcList = swfLoaded ? swf.getAbcList() : null;
        boolean hasAbc = swfLoaded && abcList != null && !abcList.isEmpty();
        boolean hasDebugger = hasAbc && DebuggerTools.hasDebugger(swf);

        miSave.setEnabled(swfLoaded);
        miSaveAs.setEnabled(swfLoaded);
        miSaveAsExe.setEnabled(swfLoaded);
        miClose.setEnabled(swfLoaded);
        miCloseAll.setEnabled(swfLoaded);

        menuExport.setEnabled(swfLoaded);
        miExportAll.setEnabled(swfLoaded);
        miExportFla.setEnabled(swfLoaded);
        miExportSel.setEnabled(swfLoaded);
        miExportXml.setEnabled(swfLoaded);

        menuImport.setEnabled(swfLoaded);
        miImportText.setEnabled(swfLoaded);
        miImportScript.setEnabled(swfLoaded);
        miImportSymbolClass.setEnabled(swfLoaded);
        miImportXml.setEnabled(swfLoaded);

        miReload.setEnabled(swfLoaded);

        miRenameIdentifiers.setEnabled(swfLoaded);
        miRenameOneIdentifier.setEnabled(swfLoaded);
        miSearch.setEnabled(swfLoaded);
        miReplace.setEnabled(swfLoaded);
        miViewTimeline.setEnabled(swfLoaded);

        miGotoDocumentClass.setEnabled(hasAbc);
        miDeobfuscation.setEnabled(hasAbc);
        miDebuggerSwitch.setEnabled(hasAbc);
        miDebuggerSwitch.setSelected(hasDebugger);
        miDebuggerReplaceTrace.setEnabled(hasAbc && hasDebugger);
    }

    private void viewModeResourcesButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(false);
        mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        miViewTimeline.setSelected(false);
        viewGroup.clearSelection();
        miViewResources.setSelected(true);
    }

    private void viewModeHexDumpButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(true);
        mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
        miViewTimeline.setSelected(false);
        viewGroup.clearSelection();
        miViewHex.setSelected(true);
    }

    private void debuggerSwitchButtonActionPerformed(ActionEvent evt) {
        if (!miDebuggerSwitch.isSelected() || View.showConfirmDialog(mainFrame, translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            switchDebugger();
            mainFrame.getPanel().refreshDecompiled();
        } else {
            if (miDebuggerSwitch.isSelected()) {
                miDebuggerSwitch.setSelected(false);
            }
        }
        miDebuggerReplaceTrace.setEnabled(miDebuggerSwitch.isSelected());
    }

    private void timelineButtonActionPerformed(ActionEvent evt) {
        if (miViewTimeline.isSelected()) {
            if (!mainFrame.getPanel().showView(MainPanel.VIEW_TIMELINE)) {
                miViewTimeline.setSelected(false);
            } else {
                viewGroup.clearSelection();
            }
        } else {
            viewGroup.clearSelection();
            if (Configuration.dumpView.get()) {
                miViewHex.setSelected(true);
                mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
            } else {
                miViewResources.setSelected(true);
                mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
            }
        }
    }
}
