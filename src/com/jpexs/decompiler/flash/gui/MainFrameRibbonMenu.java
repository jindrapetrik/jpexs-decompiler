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
import com.jpexs.helpers.Helper;
import com.jpexs.process.ProcessTools;
import com.sun.jna.Platform;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
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
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;

/**
 *
 * @author JPEXS
 */
public class MainFrameRibbonMenu extends MainFrameMenu {

    private final MainFrameRibbon mainFrame;

    private JCheckBox miAutoDeobfuscation;

    private JCheckBox miInternalViewer;

    private JCheckBox miParallelSpeedUp;

    private JCheckBox miAssociate;

    private JCheckBox miDecompile;

    private JCheckBox miCacheDisk;

    private JCheckBox miGotoMainClassOnStartup;

    private JCheckBox miAutoRenameIdentifiers;

    private JCommandButton saveCommandButton;

    private JCommandButton saveasCommandButton;

    private JCommandButton saveasexeCommandButton;

    private JCommandButton exportAllCommandButton;

    private JCommandButton exportFlaCommandButton;

    private JCommandButton exportSelectionCommandButton;

    private JCommandButton importTextCommandButton;

    private JCommandButton importScriptCommandButton;

    private JCommandButton importSymbolClassCommandButton;

    private JCommandButton importXmlCommandButton;

    private JCommandButton exportXmlCommandButton;

    private JCommandToggleButton viewModeResourcesToggleButton;

    private JCommandToggleButton viewModeHexToggleButton;

    private JCommandToggleButton deobfuscationModeOldToggleButton;

    private JCommandToggleButton deobfuscationModeNewToggleButton;

    private JCommandButton reloadCommandButton;

    private JCommandButton renameInvalidCommandButton;

    private JCommandButton globalRenameCommandButton;

    private JCommandButton deobfuscationCommandButton;

    private JCommandButton searchCommandButton;

    private JCommandButton replaceCommandButton;

    private JCommandToggleButton timeLineToggleButton;

    private CommandToggleButtonGroup timeLineToggleGroup;

    private JCommandButton gotoDocumentClassCommandButton;

    private JCommandButton clearRecentFilesCommandButton;

    private JCommandToggleButton debuggerSwitchCommandButton;

    private CommandToggleButtonGroup debuggerSwitchGroup;

    private JCommandButton debuggerReplaceTraceCommandButton;

    private JCommandButton debuggerLogCommandButton;

    private CommandToggleButtonGroup viewModeToggleGroup;

    RibbonApplicationMenuEntryPrimary exportFlaMenu;

    RibbonApplicationMenuEntryPrimary exportAllMenu;

    RibbonApplicationMenuEntryPrimary exportSelMenu;

    RibbonApplicationMenuEntryPrimary saveFileMenu;

    RibbonApplicationMenuEntryPrimary saveAsFileMenu;

    RibbonApplicationMenuEntryPrimary closeFileMenu;

    RibbonApplicationMenuEntryPrimary closeAllFilesMenu;

    public MainFrameRibbonMenu(MainFrameRibbon mainFrame, JRibbon ribbon, boolean externalFlashPlayerUnavailable) {
        super(mainFrame);
        this.mainFrame = mainFrame;

        ribbon.addTask(createFileRibbonTask());
        ribbon.addTask(createToolsRibbonTask());
        ribbon.addTask(createSettingsRibbonTask(externalFlashPlayerUnavailable));
        ribbon.addTask(createHelpRibbonTask());

        if (Configuration.showDebugMenu.get() || Configuration.debugMode.get()) {
            ribbon.addTask(createDebugRibbonTask());
        }

        ribbon.setApplicationMenu(createMainMenu());
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

    private RibbonApplicationMenu createMainMenu() {
        RibbonApplicationMenu mainMenu = new RibbonApplicationMenu();
        exportFlaMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportfla32"), translate("menu.file.export.fla"), this::exportFla, JCommandButton.CommandButtonKind.ACTION_ONLY);
        exportAllMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("export32"), translate("menu.file.export.all"), this::exportAll, JCommandButton.CommandButtonKind.ACTION_ONLY);
        exportSelMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportsel32"), translate("menu.file.export.selection"), this::exportSelected, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary checkUpdatesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("update32"), translate("menu.help.checkupdates"), this::checkUpdates, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary aboutMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("about32"), translate("menu.help.about"), this::about, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary openFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("open32"), translate("menu.file.open"), this::open, JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
        saveFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("save32"), translate("menu.file.save"), this::save, JCommandButton.CommandButtonKind.ACTION_ONLY);
        saveAsFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("saveas32"), translate("menu.file.saveas"), this::saveAs, JCommandButton.CommandButtonKind.ACTION_ONLY);
        closeFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("close32"), translate("menu.file.close"), this::close, JCommandButton.CommandButtonKind.ACTION_ONLY);
        closeAllFilesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("close32"), translate("menu.file.closeAll"), this::closeAll, JCommandButton.CommandButtonKind.ACTION_ONLY);
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
                            if (Main.openFile(source.fileName, null) == OpenFileResult.NOT_FOUND) {
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

        RibbonApplicationMenuEntryFooter exitMenu = new RibbonApplicationMenuEntryFooter(View.getResizableIcon("exit32"), translate("menu.file.exit"), this::exit);

        mainMenu.addMenuEntry(openFileMenu);
        mainMenu.addMenuEntry(saveFileMenu);
        mainMenu.addMenuEntry(saveAsFileMenu);
        mainMenu.addMenuEntry(closeFileMenu);
        mainMenu.addMenuEntry(closeAllFilesMenu);
        mainMenu.addMenuSeparator();
        mainMenu.addMenuEntry(exportFlaMenu);
        mainMenu.addMenuEntry(exportAllMenu);
        mainMenu.addMenuEntry(exportSelMenu);
        mainMenu.addMenuSeparator();
        mainMenu.addMenuEntry(checkUpdatesMenu);
        mainMenu.addMenuEntry(aboutMenu);
        mainMenu.addFooterEntry(exitMenu);
        mainMenu.addMenuSeparator();

        return mainMenu;
    }

    private List<RibbonBandResizePolicy> titleResizePolicies(final JRibbonBand ribbonBand) {
        List<RibbonBandResizePolicy> resizePolicies = new ArrayList<>();
        resizePolicies.add(new BaseRibbonBandResizePolicy<AbstractBandControlPanel>(ribbonBand.getControlPanel()) {
            @Override
            public int getPreferredWidth(int i, int i1) {
                return ribbonBand.getGraphics().getFontMetrics(ribbonBand.getFont()).stringWidth(ribbonBand.getTitle()) + 20;
            }

            @Override
            public void install(int i, int i1) {
            }
        });
        return resizePolicies;
    }

    private List<RibbonBandResizePolicy> getResizePolicies(JRibbonBand ribbonBand) {
        List<RibbonBandResizePolicy> resizePolicies = new ArrayList<>();
        resizePolicies.add(new CoreRibbonResizePolicies.Mirror(ribbonBand.getControlPanel()));
        resizePolicies.add(new IconRibbonBandResizePolicy(ribbonBand.getControlPanel()));
        return resizePolicies;
    }

    private List<RibbonBandResizePolicy> getIconBandResizePolicies(JRibbonBand ribbonBand) {
        List<RibbonBandResizePolicy> resizePolicies = new ArrayList<>();
        IconRibbonBandResizePolicy iconBandResizePolicy = new IconRibbonBandResizePolicy(ribbonBand.getControlPanel());
        final int width = Math.max(105, iconBandResizePolicy.getPreferredWidth(0, 0));
        resizePolicies.add(new BaseRibbonBandResizePolicy<AbstractBandControlPanel>(ribbonBand.getControlPanel()) {
            @Override
            public int getPreferredWidth(int i, int i1) {
                return width;
            }

            @Override
            public void install(int i, int i1) {
            }
        });
        resizePolicies.add(iconBandResizePolicy);
        return resizePolicies;
    }

    private RibbonTask createFileRibbonTask() {
        JRibbonBand editBand = new JRibbonBand(translate("menu.general"), null);
        editBand.setResizePolicies(getResizePolicies(editBand));
        JCommandButton openCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.open")), View.getResizableIcon("open32"));
        openCommandButton.addActionListener(this::open);
        saveCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.save")), View.getResizableIcon("save32"));
        saveCommandButton.addActionListener(this::save);
        saveasCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveas")), View.getResizableIcon("saveas16"));
        saveasCommandButton.addActionListener(this::saveAs);

        reloadCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.reload")), View.getResizableIcon("reload16"));
        reloadCommandButton.addActionListener(this::reload);

        saveasexeCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveasexe")), View.getResizableIcon("saveasexe16"));
        saveasexeCommandButton.addActionListener(this::saveAsExe);;

        editBand.addCommandButton(openCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveasCommandButton, RibbonElementPriority.MEDIUM);
        editBand.addCommandButton(saveasexeCommandButton, RibbonElementPriority.MEDIUM);
        editBand.addCommandButton(reloadCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand exportBand = new JRibbonBand(translate("menu.export"), null);
        exportBand.setResizePolicies(getResizePolicies(exportBand));
        exportFlaCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.fla")), View.getResizableIcon("exportfla32"));
        exportFlaCommandButton.addActionListener(this::exportFla);
        exportAllCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.all")), View.getResizableIcon("export16"));
        exportAllCommandButton.addActionListener(this::exportAll);
        exportSelectionCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.selection")), View.getResizableIcon("exportsel16"));
        exportSelectionCommandButton.addActionListener(this::exportSelected);

        exportXmlCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.xml")), View.getResizableIcon("exportxml32"));
        exportXmlCommandButton.addActionListener(this::exportSwfXml);

        exportBand.addCommandButton(exportFlaCommandButton, RibbonElementPriority.TOP);
        exportBand.addCommandButton(exportAllCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportSelectionCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportXmlCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand importBand = new JRibbonBand(translate("menu.import"), null);
        importBand.setResizePolicies(getResizePolicies(importBand));
        importTextCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.text")), View.getResizableIcon("importtext32"));
        importTextCommandButton.addActionListener(this::importText);

        // todo: icon
        importScriptCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.script")), View.getResizableIcon("importtext32"));
        importScriptCommandButton.addActionListener(this::importScript);

        importSymbolClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.symbolClass")), View.getResizableIcon("importsymbolclass32"));
        importSymbolClassCommandButton.addActionListener(this::importSymbolClass);

        importXmlCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.xml")), View.getResizableIcon("importxml32"));
        importXmlCommandButton.addActionListener(this::importSwfXml);

        importBand.addCommandButton(importXmlCommandButton, RibbonElementPriority.TOP);
        importBand.addCommandButton(importTextCommandButton, RibbonElementPriority.MEDIUM);
        importBand.addCommandButton(importScriptCommandButton, RibbonElementPriority.MEDIUM);
        importBand.addCommandButton(importSymbolClassCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand viewBand = new JRibbonBand(translate("menu.view"), null);
        viewBand.setResizePolicies(getResizePolicies(viewBand));

        viewModeToggleGroup = new CommandToggleButtonGroup();

        viewModeResourcesToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.view.resources")), View.getResizableIcon("viewresources16"));
        viewModeResourcesToggleButton.addActionListener(this::viewModeResouresButtonActionPerformed);

        viewModeHexToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.view.hex")), View.getResizableIcon("viewhex16"));
        viewModeHexToggleButton.addActionListener(this::viewModeHexDumpButtonActionPerformed);;

        viewModeToggleGroup.add(viewModeResourcesToggleButton);
        viewModeToggleGroup.add(viewModeHexToggleButton);

        if (Configuration.dumpView.get()) {
            viewModeToggleGroup.setSelected(viewModeHexToggleButton, true);
        } else {
            viewModeToggleGroup.setSelected(viewModeResourcesToggleButton, true);
        }

        viewBand.addCommandButton(viewModeResourcesToggleButton, RibbonElementPriority.MEDIUM);
        viewBand.addCommandButton(viewModeHexToggleButton, RibbonElementPriority.MEDIUM);

        return new RibbonTask(translate("menu.file"), editBand, exportBand, importBand, viewBand);
    }

    private RibbonTask createToolsRibbonTask() {

        JRibbonBand debuggerBand = new JRibbonBand(translate("menu.debugger"), null);
        debuggerBand.setResizePolicies(getResizePolicies(debuggerBand));

        debuggerSwitchCommandButton = new JCommandToggleButton(translate("menu.debugger.switch"), View.getResizableIcon("debugger32"));
        debuggerSwitchCommandButton.addActionListener(this::debuggerSwitchButtonActionPerformed);

        //debuggerDetachCommandButton = new JCommandButton("Detach debugger",View.getResizableIcon("debuggerremove16"));
        //debuggerDetachCommandButton.addActionListener(this::debuggerDetach);
        debuggerReplaceTraceCommandButton = new JCommandButton(translate("menu.debugger.replacetrace"), View.getResizableIcon("debuggerreplace16"));
        debuggerReplaceTraceCommandButton.addActionListener(this::debuggerReplaceTraceCalls);

        debuggerLogCommandButton = new JCommandButton(translate("menu.debugger.showlog"), View.getResizableIcon("debuggerlog16"));
        debuggerLogCommandButton.addActionListener(this::debuggerShowLog);

        debuggerSwitchGroup = new CommandToggleButtonGroup();
        debuggerSwitchGroup.add(debuggerSwitchCommandButton);

        debuggerSwitchCommandButton.setEnabled(false);

        debuggerReplaceTraceCommandButton.setEnabled(false);

        debuggerBand.addCommandButton(debuggerSwitchCommandButton, RibbonElementPriority.TOP);
        debuggerBand.addCommandButton(debuggerReplaceTraceCommandButton, RibbonElementPriority.MEDIUM);
        debuggerBand.addCommandButton(debuggerLogCommandButton, RibbonElementPriority.MEDIUM);

        // ----------------------------------------- TOOLS -----------------------------------
        JRibbonBand toolsBand = new JRibbonBand(translate("menu.tools"), null);
        toolsBand.setResizePolicies(getResizePolicies(toolsBand));

        searchCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.search")), View.getResizableIcon("search32"));
        searchCommandButton.addActionListener((ActionEvent e) -> {
            search(e, null);
        });

        replaceCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.replace")), View.getResizableIcon("replace32"));
        replaceCommandButton.addActionListener(this::replace);

        timeLineToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.tools.timeline")), View.getResizableIcon("timeline32"));
        timeLineToggleButton.addActionListener(this::timelineButtonActionPerformed);

        timeLineToggleGroup = new CommandToggleButtonGroup();
        timeLineToggleGroup.add(timeLineToggleButton);

        gotoDocumentClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.gotodocumentclass")), View.getResizableIcon("gotomainclass32"));
        gotoDocumentClassCommandButton.addActionListener(this::gotoDucumentClass);

        JCommandButton proxyCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.proxy")), View.getResizableIcon("proxy16"));
        proxyCommandButton.addActionListener(this::showProxy);

        JCommandButton loadMemoryCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchmemory")), View.getResizableIcon("loadmemory16"));
        loadMemoryCommandButton.addActionListener(this::loadFromMemory);

        JCommandButton loadCacheCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchcache")), View.getResizableIcon("loadcache16"));
        loadCacheCommandButton.addActionListener(this::loadFromCache);

        toolsBand.addCommandButton(searchCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(replaceCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(timeLineToggleButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(gotoDocumentClassCommandButton, RibbonElementPriority.TOP);
        toolsBand.addCommandButton(proxyCommandButton, RibbonElementPriority.MEDIUM);
        toolsBand.addCommandButton(loadMemoryCommandButton, RibbonElementPriority.MEDIUM);
        toolsBand.addCommandButton(loadCacheCommandButton, RibbonElementPriority.MEDIUM);
        if (!ProcessTools.toolsAvailable()) {
            loadMemoryCommandButton.setEnabled(false);
        }
        JRibbonBand deobfuscationBand = new JRibbonBand(translate("menu.tools.deobfuscation"), null);
        deobfuscationBand.setResizePolicies(getResizePolicies(deobfuscationBand));

        deobfuscationCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.pcode")), View.getResizableIcon("deobfuscate32"));
        deobfuscationCommandButton.addActionListener(this::deobfuscate);
        globalRenameCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.globalrename")), View.getResizableIcon("rename16"));
        globalRenameCommandButton.addActionListener(this::renameOneIdentifier);
        renameInvalidCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.renameinvalid")), View.getResizableIcon("renameall16"));
        renameInvalidCommandButton.addActionListener(this::renameIdentifiers);

        deobfuscationBand.addCommandButton(deobfuscationCommandButton, RibbonElementPriority.TOP);
        deobfuscationBand.addCommandButton(globalRenameCommandButton, RibbonElementPriority.MEDIUM);
        deobfuscationBand.addCommandButton(renameInvalidCommandButton, RibbonElementPriority.MEDIUM);

        //JRibbonBand otherToolsBand = new JRibbonBand(translate("menu.tools.otherTools"), null);
        //otherToolsBand.setResizePolicies(getResizePolicies(otherToolsBand));
        return new RibbonTask(translate("menu.tools"), toolsBand, deobfuscationBand, debuggerBand /*, otherToolsBand*/);
    }

    private RibbonTask createSettingsRibbonTask(boolean externalFlashPlayerUnavailable) {
        // ----------------------------------------- SETTINGS -----------------------------------

        JRibbonBand settingsBand = new JRibbonBand(translate("menu.settings"), null);
        settingsBand.setResizePolicies(getResizePolicies(settingsBand));

        miAutoDeobfuscation = new JCheckBox(translate("menu.settings.autodeobfuscation"));
        miAutoDeobfuscation.setSelected(Configuration.autoDeobfuscate.get());
        miAutoDeobfuscation.addActionListener(this::autoDeobfuscate);

        miInternalViewer = new JCheckBox(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.addActionListener(this::internalViewerSwitch);

        miParallelSpeedUp = new JCheckBox(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.addActionListener(this::parallelSpeedUp);

        miDecompile = new JCheckBox(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.addActionListener(this::disableDecompilation);

        miAssociate = new JCheckBox(translate("menu.settings.addtocontextmenu"));
        miAssociate.addActionListener(this::associate);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());

        miCacheDisk = new JCheckBox(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.addActionListener(this::cacheOnDisk);

        miGotoMainClassOnStartup = new JCheckBox(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.addActionListener(this::gotoDucumentClassOnStartup);

        miAutoRenameIdentifiers = new JCheckBox(translate("menu.settings.autoRenameIdentifiers"));
        miAutoRenameIdentifiers.setSelected(Configuration.autoRenameIdentifiers.get());
        miAutoRenameIdentifiers.addActionListener(this::autoRenameIdentifiers);

        settingsBand.addRibbonComponent(new JRibbonComponent(miAutoDeobfuscation));
        settingsBand.addRibbonComponent(new JRibbonComponent(miInternalViewer));
        settingsBand.addRibbonComponent(new JRibbonComponent(miParallelSpeedUp));
        settingsBand.addRibbonComponent(new JRibbonComponent(miDecompile));
        if (Platform.isWindows()) {
            settingsBand.addRibbonComponent(new JRibbonComponent(miAssociate));
        }
        settingsBand.addRibbonComponent(new JRibbonComponent(miCacheDisk));
        settingsBand.addRibbonComponent(new JRibbonComponent(miGotoMainClassOnStartup));
        settingsBand.addRibbonComponent(new JRibbonComponent(miAutoRenameIdentifiers));

        JRibbonBand languageBand = new JRibbonBand(translate("menu.language"), null);
        List<RibbonBandResizePolicy> languageBandResizePolicies = getIconBandResizePolicies(languageBand);
        languageBand.setResizePolicies(languageBandResizePolicies);
        JCommandButton setLanguageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.settings.language")), View.getResizableIcon("setlanguage32"));
        setLanguageCommandButton.addActionListener(this::setLanguage);
        languageBand.addCommandButton(setLanguageCommandButton, RibbonElementPriority.TOP);

        JRibbonBand advancedSettingsBand = new JRibbonBand(translate("menu.advancedsettings.advancedsettings"), null);
        advancedSettingsBand.setResizePolicies(getResizePolicies(advancedSettingsBand));
        JCommandButton advancedSettingsCommandButton = new JCommandButton(fixCommandTitle(translate("menu.advancedsettings.advancedsettings")), View.getResizableIcon("settings32"));
        advancedSettingsCommandButton.addActionListener(this::advancedSettings);
        advancedSettingsBand.addCommandButton(advancedSettingsCommandButton, RibbonElementPriority.TOP);

        clearRecentFilesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.otherTools.clearRecentFiles")), View.getResizableIcon("clearrecent16"));
        clearRecentFilesCommandButton.addActionListener(this::clearRecentFiles);
        advancedSettingsBand.addCommandButton(clearRecentFilesCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand deobfuscationBand = new JRibbonBand(translate("menu.deobfuscation"), null);
        deobfuscationBand.setResizePolicies(titleResizePolicies(deobfuscationBand));

        CommandToggleButtonGroup grpDeobfuscation = new CommandToggleButtonGroup();

        deobfuscationModeOldToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.deobfuscation.old")), View.getResizableIcon("deobfuscateold16"));
        deobfuscationModeOldToggleButton.addActionListener((ActionEvent e) -> {
            deobfuscationMode(e, 0);
        });

        deobfuscationModeNewToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.deobfuscation.new")), View.getResizableIcon("deobfuscatenew16"));
        deobfuscationModeNewToggleButton.addActionListener((ActionEvent e) -> {
            deobfuscationMode(e, 1);
        });

        grpDeobfuscation.add(deobfuscationModeOldToggleButton);
        grpDeobfuscation.add(deobfuscationModeNewToggleButton);

        int deobfuscationMode = Configuration.deobfuscationMode.get();
        switch (deobfuscationMode) {
            case 0:
                grpDeobfuscation.setSelected(deobfuscationModeOldToggleButton, true);
                break;
            case 1:
                grpDeobfuscation.setSelected(deobfuscationModeNewToggleButton, true);
                break;
        }

        deobfuscationBand.addCommandButton(deobfuscationModeOldToggleButton, RibbonElementPriority.MEDIUM);
        deobfuscationBand.addCommandButton(deobfuscationModeNewToggleButton, RibbonElementPriority.MEDIUM);

        return new RibbonTask(translate("menu.settings"), settingsBand, languageBand, advancedSettingsBand, deobfuscationBand);
    }

    private RibbonTask createHelpRibbonTask() {
        // ----------------------------------------- HELP -----------------------------------

        JRibbonBand helpBand = new JRibbonBand(translate("menu.help"), null);
        helpBand.setResizePolicies(getResizePolicies(helpBand));

        JCommandButton checkForUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.checkupdates")), View.getResizableIcon("update16"));
        checkForUpdatesCommandButton.addActionListener(this::checkUpdates);
        JCommandButton helpUsUpdatesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.helpus")), View.getResizableIcon("donate32"));
        helpUsUpdatesCommandButton.addActionListener(this::helpUs);
        JCommandButton homepageCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.homepage")), View.getResizableIcon("homepage16"));
        homepageCommandButton.addActionListener(this::homePage);
        JCommandButton aboutCommandButton = new JCommandButton(fixCommandTitle(translate("menu.help.about")), View.getResizableIcon("about32"));
        aboutCommandButton.addActionListener(this::about);

        helpBand.addCommandButton(aboutCommandButton, RibbonElementPriority.TOP);
        helpBand.addCommandButton(checkForUpdatesCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(homepageCommandButton, RibbonElementPriority.MEDIUM);
        helpBand.addCommandButton(helpUsUpdatesCommandButton, RibbonElementPriority.TOP);
        return new RibbonTask(translate("menu.help"), helpBand);
    }

    private RibbonTask createDebugRibbonTask() {
        // ----------------------------------------- DEBUG -----------------------------------

        ResizableIcon icon = View.getResizableIcon("update16");
        JRibbonBand debugBand = new JRibbonBand("Debug", null);
        debugBand.setResizePolicies(getResizePolicies(debugBand));

        JCommandButton removeNonScriptsCommandButton = new JCommandButton(fixCommandTitle("Remove non scripts"), icon);
        removeNonScriptsCommandButton.addActionListener(e -> removeNonScripts());

        JCommandButton refreshDecompiledCommandButton = new JCommandButton(fixCommandTitle("Refresh decompiled script"), icon);
        refreshDecompiledCommandButton.addActionListener(e -> refreshDecompiled());

        JCommandButton checkResourcesCommandButton = new JCommandButton(fixCommandTitle("Check resources"), icon);
        checkResourcesCommandButton.addActionListener(e -> checkResources());

        JCommandButton callGcCommandButton = new JCommandButton(fixCommandTitle("Call System.gc()"), icon);
        callGcCommandButton.addActionListener(e -> System.gc());

        JCommandButton emptyCacheCommandButton = new JCommandButton(fixCommandTitle("Empty cache"), icon);
        emptyCacheCommandButton.addActionListener(e -> {
            SWF swf = mainFrame.getPanel().getCurrentSwf();
            if (swf != null) {
                swf.clearAllCache();
            }
        });

        JCommandButton memoryInformationCommandButton = new JCommandButton(fixCommandTitle("Memory information"), icon);
        memoryInformationCommandButton.addActionListener(e -> {
            String architecture = System.getProperty("sun.arch.data.model");
            Runtime runtime = Runtime.getRuntime();
            String info = "Architecture: " + architecture + Helper.newLine
                    + "Max: " + (runtime.maxMemory() / 1024 / 1024) + "MB" + Helper.newLine
                    + "Used: " + (runtime.totalMemory() / 1024 / 1024) + "MB" + Helper.newLine
                    + "Free: " + (runtime.freeMemory() / 1024 / 1024) + "MB";
            View.showMessageDialog(null, info);
            SWF swf = mainFrame.getPanel().getCurrentSwf();
            if (swf != null) {
                swf.clearAllCache();
            }
        });

        JCommandButton fixAs3CodeCommandButton = new JCommandButton(fixCommandTitle("Fix AS3 code"), icon);
        fixAs3CodeCommandButton.addActionListener(e -> {
            SWF swf = mainFrame.getPanel().getCurrentSwf();
            if (swf != null) {
                swf.fixAS3Code();
            }
        });

        debugBand.addCommandButton(removeNonScriptsCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(refreshDecompiledCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(checkResourcesCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(callGcCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(emptyCacheCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(memoryInformationCommandButton, RibbonElementPriority.MEDIUM);
        debugBand.addCommandButton(fixAs3CodeCommandButton, RibbonElementPriority.MEDIUM);
        return new RibbonTask("Debug", debugBand);
    }

    @Override
    public void updateComponents(SWF swf) {
        super.updateComponents(swf);
        boolean swfLoaded = swf != null;
        List<ABCContainerTag> abcList = swfLoaded ? swf.getAbcList() : null;
        boolean hasAbc = swfLoaded && abcList != null && !abcList.isEmpty();
        boolean hasDebugger = hasAbc && DebuggerTools.hasDebugger(swf);

        exportAllMenu.setEnabled(swfLoaded);
        exportFlaMenu.setEnabled(swfLoaded);
        exportSelMenu.setEnabled(swfLoaded);
        saveFileMenu.setEnabled(swfLoaded);
        saveAsFileMenu.setEnabled(swfLoaded);
        closeFileMenu.setEnabled(swfLoaded);
        closeAllFilesMenu.setEnabled(swfLoaded);

        boolean isBundle = swfLoaded && (swf.swfList != null) && (swf.swfList.isBundle());
        saveCommandButton.setEnabled(swfLoaded && ((!isBundle) || (!swf.swfList.bundle.isReadOnly())));
        saveasCommandButton.setEnabled(swfLoaded);
        saveasexeCommandButton.setEnabled(swfLoaded);
        exportAllCommandButton.setEnabled(swfLoaded);
        exportFlaCommandButton.setEnabled(swfLoaded);
        exportXmlCommandButton.setEnabled(swfLoaded);
        exportSelectionCommandButton.setEnabled(swfLoaded);
        importTextCommandButton.setEnabled(swfLoaded);
        importScriptCommandButton.setEnabled(swfLoaded);
        importSymbolClassCommandButton.setEnabled(swfLoaded);
        importXmlCommandButton.setEnabled(swfLoaded);
        reloadCommandButton.setEnabled(swfLoaded);

        renameInvalidCommandButton.setEnabled(swfLoaded);
        globalRenameCommandButton.setEnabled(swfLoaded);
        deobfuscationCommandButton.setEnabled(swfLoaded);
        searchCommandButton.setEnabled(swfLoaded);
        replaceCommandButton.setEnabled(swfLoaded);
        timeLineToggleButton.setEnabled(swfLoaded);

        gotoDocumentClassCommandButton.setEnabled(hasAbc);
        deobfuscationCommandButton.setEnabled(hasAbc);
        debuggerSwitchCommandButton.setEnabled(hasAbc);
        debuggerSwitchGroup.setSelected(debuggerSwitchCommandButton, hasDebugger);
        //debuggerSwitchCommandButton.
        //debuggerDetachCommandButton.setEnabled(hasDebugger);
        debuggerReplaceTraceCommandButton.setEnabled(hasAbc && hasDebugger);
    }

    private void debuggerSwitchButtonActionPerformed(ActionEvent evt) {
        if (debuggerSwitchGroup.getSelected() == null || View.showConfirmDialog(mainFrame, translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            switchDebugger();
            mainFrame.getPanel().refreshDecompiled();
        } else {
            if (debuggerSwitchGroup.getSelected() == debuggerSwitchCommandButton) {
                debuggerSwitchGroup.setSelected(debuggerSwitchCommandButton, false);
            }
        }
        debuggerReplaceTraceCommandButton.setEnabled(debuggerSwitchGroup.getSelected() == debuggerSwitchCommandButton);
    }

    private void viewModeResouresButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(false);
        mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        timeLineToggleGroup.setSelected(timeLineToggleButton, false);
        viewModeToggleGroup.setSelected(viewModeResourcesToggleButton, true);
    }

    private void viewModeHexDumpButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(true);
        mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
        timeLineToggleGroup.setSelected(timeLineToggleButton, false);
        viewModeToggleGroup.setSelected(viewModeHexToggleButton, true);
    }

    private void timelineButtonActionPerformed(ActionEvent evt) {
        timeLineToggleGroup.setSelected(timeLineToggleButton, timeLineToggleGroup.getSelected() == timeLineToggleButton);
        if (timeLineToggleGroup.getSelected() == timeLineToggleButton) {
            if (!mainFrame.getPanel().showView(MainPanel.VIEW_TIMELINE)) {
                timeLineToggleGroup.setSelected(timeLineToggleButton, false);
            } else {
                viewModeToggleGroup.setSelected(viewModeHexToggleButton, false);
                viewModeToggleGroup.setSelected(viewModeResourcesToggleButton, false);
            }
        } else {
            if (Configuration.dumpView.get()) {
                viewModeToggleGroup.setSelected(viewModeHexToggleButton, true);
                mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
            } else {
                viewModeToggleGroup.setSelected(viewModeResourcesToggleButton, true);
                mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
            }
        }
    }
}
