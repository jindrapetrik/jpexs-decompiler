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
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.process.ProcessTools;
import com.sun.jna.Platform;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
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
public class MainFrameRibbonMenu extends MainFrameMenu implements ActionListener {

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

    private static final String ACTION_REPLACE = "REPLACE";

    private static final String ACTION_TIMELINE = "TIMELINE";

    private static final String ACTION_AUTO_DEOBFUSCATE = "AUTODEOBFUSCATE";

    private static final String ACTION_EXIT = "EXIT";

    private static final String ACTION_DEBUGGER_SWITCH = "DEBUGGER_SWITCH";

    private static final String ACTION_DEBUGGER_REPLACE_TRACE = "DEBUGGER_REPLACE_TRACE";

    private static final String ACTION_DEBUGGER_LOG = "DEBUGGER_LOG";

    private static final String ACTION_RENAME_ONE_IDENTIFIER = "RENAMEONEIDENTIFIER";

    private static final String ACTION_ABOUT = "ABOUT";

    private static final String ACTION_SHOW_PROXY = "SHOWPROXY";

    private static final String ACTION_SUB_LIMITER = "SUBLIMITER";

    private static final String ACTION_SAVE = "SAVE";

    private static final String ACTION_SAVE_AS = "SAVEAS";

    private static final String ACTION_SAVE_AS_EXE = "SAVEASEXE";

    private static final String ACTION_OPEN = "OPEN";

    private static final String ACTION_CLOSE = "CLOSE";

    private static final String ACTION_CLOSE_ALL = "CLOSEALL";

    private static final String ACTION_EXPORT_FLA = "EXPORTFLA";

    public static final String ACTION_EXPORT_SEL = "EXPORTSEL";

    public static final String ACTION_EXPORT_JAVA_SOURCE = "EXPORTJAVASOURCE";

    public static final String ACTION_EXPORT_SWF_XML = "EXPORTSWFXML";

    public static final String ACTION_IMPORT_SWF_XML = "IMPORTSWFXML";

    private static final String ACTION_EXPORT = "EXPORT";

    private static final String ACTION_IMPORT_TEXT = "IMPORTTEXT";

    private static final String ACTION_IMPORT_SCRIPT = "IMPORTSCRIPT";

    private static final String ACTION_IMPORT_SYMBOL_CLASS = "IMPORTSYMBOLCLASS";

    private static final String ACTION_CHECK_UPDATES = "CHECKUPDATES";

    private static final String ACTION_HELP_US = "HELPUS";

    private static final String ACTION_HOMEPAGE = "HOMEPAGE";

    private static final String ACTION_RESTORE_CONTROL_FLOW = "RESTORECONTROLFLOW";

    private static final String ACTION_RESTORE_CONTROL_FLOW_ALL = "RESTORECONTROLFLOWALL";

    private static final String ACTION_RENAME_IDENTIFIERS = "RENAMEIDENTIFIERS";

    private static final String ACTION_DEOBFUSCATE = "DEOBFUSCATE";

    private static final String ACTION_DEOBFUSCATE_ALL = "DEOBFUSCATEALL";

    private static final String ACTION_CLEAR_RECENT_FILES = "CLEARRECENTFILES";

    private static final String ACTION_VIEWMODE_RESOURCES = "VIEWMODERESOURCES";

    private static final String ACTION_VIEWMODE_HEX_DUMP = "VIEWMODEHEXDUMP";

    private static final String ACTION_DEOBFUSCATION_MODE_OLD = "DEOBFUSCATIONMODEOLD";

    private static final String ACTION_DEOBFUSCATION_MODE_NEW = "DEOBFUSCATIONMODENEW";

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

    private void assignListener(AbstractCommandButton b, final String command) {
        final MainFrameRibbonMenu t = this;
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

    private RibbonApplicationMenu createMainMenu() {
        RibbonApplicationMenu mainMenu = new RibbonApplicationMenu();
        exportFlaMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportfla32"), translate("menu.file.export.fla"), new ActionRedirector(this, ACTION_EXPORT_FLA), JCommandButton.CommandButtonKind.ACTION_ONLY);
        exportAllMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("export32"), translate("menu.file.export.all"), new ActionRedirector(this, ACTION_EXPORT), JCommandButton.CommandButtonKind.ACTION_ONLY);
        exportSelMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("exportsel32"), translate("menu.file.export.selection"), new ActionRedirector(this, ACTION_EXPORT_SEL), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary checkUpdatesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("update32"), translate("menu.help.checkupdates"), new ActionRedirector(this, ACTION_CHECK_UPDATES), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary aboutMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("about32"), translate("menu.help.about"), new ActionRedirector(this, ACTION_ABOUT), JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary openFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("open32"), translate("menu.file.open"), new ActionRedirector(this, ACTION_OPEN), JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
        saveFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("save32"), translate("menu.file.save"), new ActionRedirector(this, ACTION_SAVE), JCommandButton.CommandButtonKind.ACTION_ONLY);
        saveAsFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("saveas32"), translate("menu.file.saveas"), new ActionRedirector(this, ACTION_SAVE_AS), JCommandButton.CommandButtonKind.ACTION_ONLY);
        closeFileMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("close32"), translate("menu.file.close"), new ActionRedirector(this, ACTION_CLOSE), JCommandButton.CommandButtonKind.ACTION_ONLY);
        closeAllFilesMenu = new RibbonApplicationMenuEntryPrimary(View.getResizableIcon("close32"), translate("menu.file.closeAll"), new ActionRedirector(this, ACTION_CLOSE_ALL), JCommandButton.CommandButtonKind.ACTION_ONLY);
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

        RibbonApplicationMenuEntryFooter exitMenu = new RibbonApplicationMenuEntryFooter(View.getResizableIcon("exit32"), translate("menu.file.exit"), new ActionRedirector(this, "EXIT"));

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
        assignListener(openCommandButton, ACTION_OPEN);
        saveCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.save")), View.getResizableIcon("save32"));
        assignListener(saveCommandButton, ACTION_SAVE);
        saveasCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveas")), View.getResizableIcon("saveas16"));
        assignListener(saveasCommandButton, ACTION_SAVE_AS);

        reloadCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.reload")), View.getResizableIcon("reload16"));
        assignListener(reloadCommandButton, ACTION_RELOAD);

        saveasexeCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.saveasexe")), View.getResizableIcon("saveasexe16"));
        assignListener(saveasexeCommandButton, ACTION_SAVE_AS_EXE);

        editBand.addCommandButton(openCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveCommandButton, RibbonElementPriority.TOP);
        editBand.addCommandButton(saveasCommandButton, RibbonElementPriority.MEDIUM);
        editBand.addCommandButton(saveasexeCommandButton, RibbonElementPriority.MEDIUM);
        editBand.addCommandButton(reloadCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand exportBand = new JRibbonBand(translate("menu.export"), null);
        exportBand.setResizePolicies(getResizePolicies(exportBand));
        exportFlaCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.fla")), View.getResizableIcon("exportfla32"));
        assignListener(exportFlaCommandButton, ACTION_EXPORT_FLA);
        exportAllCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.all")), View.getResizableIcon("export16"));
        assignListener(exportAllCommandButton, ACTION_EXPORT);
        exportSelectionCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.selection")), View.getResizableIcon("exportsel16"));
        assignListener(exportSelectionCommandButton, ACTION_EXPORT_SEL);

        exportXmlCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.export.xml")), View.getResizableIcon("exportxml32"));
        assignListener(exportXmlCommandButton, ACTION_EXPORT_SWF_XML);

        exportBand.addCommandButton(exportFlaCommandButton, RibbonElementPriority.TOP);
        exportBand.addCommandButton(exportAllCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportSelectionCommandButton, RibbonElementPriority.MEDIUM);
        exportBand.addCommandButton(exportXmlCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand importBand = new JRibbonBand(translate("menu.import"), null);
        importBand.setResizePolicies(getResizePolicies(importBand));
        importTextCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.text")), View.getResizableIcon("importtext32"));
        assignListener(importTextCommandButton, ACTION_IMPORT_TEXT);

        // todo: icon
        importScriptCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.script")), View.getResizableIcon("importtext32"));
        assignListener(importScriptCommandButton, ACTION_IMPORT_SCRIPT);

        importSymbolClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.symbolClass")), View.getResizableIcon("importsymbolclass32"));
        assignListener(importSymbolClassCommandButton, ACTION_IMPORT_SYMBOL_CLASS);

        importXmlCommandButton = new JCommandButton(fixCommandTitle(translate("menu.file.import.xml")), View.getResizableIcon("importxml32"));
        assignListener(importXmlCommandButton, ACTION_IMPORT_SWF_XML);

        importBand.addCommandButton(importXmlCommandButton, RibbonElementPriority.TOP);
        importBand.addCommandButton(importTextCommandButton, RibbonElementPriority.MEDIUM);
        importBand.addCommandButton(importScriptCommandButton, RibbonElementPriority.MEDIUM);
        importBand.addCommandButton(importSymbolClassCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand viewBand = new JRibbonBand(translate("menu.view"), null);
        viewBand.setResizePolicies(getResizePolicies(viewBand));

        viewModeToggleGroup = new CommandToggleButtonGroup();

        viewModeResourcesToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.view.resources")), View.getResizableIcon("viewresources16"));
        assignListener(viewModeResourcesToggleButton, ACTION_VIEWMODE_RESOURCES);

        viewModeHexToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.view.hex")), View.getResizableIcon("viewhex16"));
        assignListener(viewModeHexToggleButton, ACTION_VIEWMODE_HEX_DUMP);

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
        assignListener(debuggerSwitchCommandButton, ACTION_DEBUGGER_SWITCH);

        //debuggerDetachCommandButton = new JCommandButton("Detach debugger",View.getResizableIcon("debuggerremove16"));
        //assignListener(debuggerDetachCommandButton, ACTION_DEBUGGER_DETACH);
        debuggerReplaceTraceCommandButton = new JCommandButton(translate("menu.debugger.replacetrace"), View.getResizableIcon("debuggerreplace16"));
        assignListener(debuggerReplaceTraceCommandButton, ACTION_DEBUGGER_REPLACE_TRACE);

        debuggerLogCommandButton = new JCommandButton(translate("menu.debugger.showlog"), View.getResizableIcon("debuggerlog16"));
        assignListener(debuggerLogCommandButton, ACTION_DEBUGGER_LOG);

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
        assignListener(searchCommandButton, ACTION_SEARCH);

        replaceCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.replace")), View.getResizableIcon("replace32"));
        assignListener(replaceCommandButton, ACTION_REPLACE);

        timeLineToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.tools.timeline")), View.getResizableIcon("timeline32"));
        assignListener(timeLineToggleButton, ACTION_TIMELINE);

        timeLineToggleGroup = new CommandToggleButtonGroup();
        timeLineToggleGroup.add(timeLineToggleButton);

        gotoDocumentClassCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.gotodocumentclass")), View.getResizableIcon("gotomainclass32"));
        assignListener(gotoDocumentClassCommandButton, ACTION_GOTO_DOCUMENT_CLASS);

        JCommandButton proxyCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.proxy")), View.getResizableIcon("proxy16"));
        assignListener(proxyCommandButton, ACTION_SHOW_PROXY);

        JCommandButton loadMemoryCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchmemory")), View.getResizableIcon("loadmemory16"));
        assignListener(loadMemoryCommandButton, ACTION_LOAD_MEMORY);

        JCommandButton loadCacheCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.searchcache")), View.getResizableIcon("loadcache16"));
        assignListener(loadCacheCommandButton, ACTION_LOAD_CACHE);

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
        assignListener(deobfuscationCommandButton, ACTION_DEOBFUSCATE);
        globalRenameCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.globalrename")), View.getResizableIcon("rename16"));
        assignListener(globalRenameCommandButton, ACTION_RENAME_ONE_IDENTIFIER);
        renameInvalidCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.deobfuscation.renameinvalid")), View.getResizableIcon("renameall16"));
        assignListener(renameInvalidCommandButton, ACTION_RENAME_IDENTIFIERS);

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
        miAutoDeobfuscation.addActionListener(this);
        miAutoDeobfuscation.setActionCommand(ACTION_AUTO_DEOBFUSCATE);

        miInternalViewer = new JCheckBox(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.setActionCommand(ACTION_INTERNAL_VIEWER_SWITCH);
        miInternalViewer.addActionListener(this);

        miParallelSpeedUp = new JCheckBox(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.setActionCommand(ACTION_PARALLEL_SPEED_UP);
        miParallelSpeedUp.addActionListener(this);

        miDecompile = new JCheckBox(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.setActionCommand(ACTION_DISABLE_DECOMPILATION);
        miDecompile.addActionListener(this);

        miAssociate = new JCheckBox(translate("menu.settings.addtocontextmenu"));
        miAssociate.setActionCommand(ACTION_ASSOCIATE);
        miAssociate.addActionListener(this);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());

        miCacheDisk = new JCheckBox(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.setActionCommand(ACTION_CACHE_ON_DISK);
        miCacheDisk.addActionListener(this);

        miGotoMainClassOnStartup = new JCheckBox(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.setActionCommand(ACTION_GOTO_DOCUMENT_CLASS_ON_STARTUP);
        miGotoMainClassOnStartup.addActionListener(this);

        miAutoRenameIdentifiers = new JCheckBox(translate("menu.settings.autoRenameIdentifiers"));
        miAutoRenameIdentifiers.setSelected(Configuration.autoRenameIdentifiers.get());
        miAutoRenameIdentifiers.setActionCommand(ACTION_AUTO_RENAME_IDENTIFIERS);
        miAutoRenameIdentifiers.addActionListener(this);

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
        assignListener(setLanguageCommandButton, ACTION_SET_LANGUAGE);
        languageBand.addCommandButton(setLanguageCommandButton, RibbonElementPriority.TOP);

        JRibbonBand advancedSettingsBand = new JRibbonBand(translate("menu.advancedsettings.advancedsettings"), null);
        advancedSettingsBand.setResizePolicies(getResizePolicies(advancedSettingsBand));
        JCommandButton advancedSettingsCommandButton = new JCommandButton(fixCommandTitle(translate("menu.advancedsettings.advancedsettings")), View.getResizableIcon("settings32"));
        assignListener(advancedSettingsCommandButton, ACTION_ADVANCED_SETTINGS);
        advancedSettingsBand.addCommandButton(advancedSettingsCommandButton, RibbonElementPriority.TOP);

        clearRecentFilesCommandButton = new JCommandButton(fixCommandTitle(translate("menu.tools.otherTools.clearRecentFiles")), View.getResizableIcon("clearrecent16"));
        assignListener(clearRecentFilesCommandButton, ACTION_CLEAR_RECENT_FILES);
        advancedSettingsBand.addCommandButton(clearRecentFilesCommandButton, RibbonElementPriority.MEDIUM);

        JRibbonBand deobfuscationBand = new JRibbonBand(translate("menu.deobfuscation"), null);
        deobfuscationBand.setResizePolicies(titleResizePolicies(deobfuscationBand));

        CommandToggleButtonGroup grpDeobfuscation = new CommandToggleButtonGroup();

        deobfuscationModeOldToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.deobfuscation.old")), View.getResizableIcon("deobfuscateold16"));
        assignListener(deobfuscationModeOldToggleButton, ACTION_DEOBFUSCATION_MODE_OLD);

        deobfuscationModeNewToggleButton = new JCommandToggleButton(fixCommandTitle(translate("menu.file.deobfuscation.new")), View.getResizableIcon("deobfuscatenew16"));
        assignListener(deobfuscationModeNewToggleButton, ACTION_DEOBFUSCATION_MODE_NEW);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_DEBUGGER_SWITCH:
                if (debuggerSwitchGroup.getSelected() == null || View.showConfirmDialog(mainFrame, translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
                    switchDebugger();
                    mainFrame.getPanel().refreshDecompiled();
                } else {
                    if (debuggerSwitchGroup.getSelected() == debuggerSwitchCommandButton) {
                        debuggerSwitchGroup.setSelected(debuggerSwitchCommandButton, false);
                    }
                }
                debuggerReplaceTraceCommandButton.setEnabled(debuggerSwitchGroup.getSelected() == debuggerSwitchCommandButton);
                break;
            case ACTION_DEBUGGER_LOG:
                debuggerShowLog();
                break;
            case ACTION_DEBUGGER_REPLACE_TRACE:
                ReplaceTraceDialog rtd = new ReplaceTraceDialog(mainFrame, Configuration.lastDebuggerReplaceFunction.get());
                rtd.setVisible(true);
                if (rtd.getValue() != null) {
                    replaceTraceCalls(rtd.getValue());
                    mainFrame.getPanel().refreshDecompiled();
                    Configuration.lastDebuggerReplaceFunction.set(rtd.getValue());
                }
                break;
            case ACTION_RELOAD:
                reload(null);
                break;
            case ACTION_ADVANCED_SETTINGS:
                advancedSettings(null);
                break;
            case ACTION_LOAD_MEMORY:
                loadFromMemory(null);
                break;
            case ACTION_LOAD_CACHE:
                loadFromCache(null);
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

            case ACTION_VIEWMODE_RESOURCES:
                Configuration.dumpView.set(false);
                mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
                timeLineToggleGroup.setSelected(timeLineToggleButton, false);
                viewModeToggleGroup.setSelected(viewModeResourcesToggleButton, true);
                break;
            case ACTION_VIEWMODE_HEX_DUMP:
                Configuration.dumpView.set(true);
                mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
                timeLineToggleGroup.setSelected(timeLineToggleButton, false);
                viewModeToggleGroup.setSelected(viewModeHexToggleButton, true);
                break;
            case ACTION_DEOBFUSCATION_MODE_OLD:
                Configuration.deobfuscationMode.set(0);
                mainFrame.getPanel().autoDeobfuscateChanged();
                break;
            case ACTION_DEOBFUSCATION_MODE_NEW:
                Configuration.deobfuscationMode.set(1);
                mainFrame.getPanel().autoDeobfuscateChanged();
                break;
            case ACTION_SEARCH:
                search(null);
                break;
            case ACTION_REPLACE:
                replace();
                break;
            case ACTION_TIMELINE:
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
                break;
            case ACTION_AUTO_DEOBFUSCATE:
                if (View.showConfirmDialog(mainFrame.getPanel(), translate("message.confirm.autodeobfuscate") + "\r\n" + (miAutoDeobfuscation.isSelected() ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Configuration.autoDeobfuscate.set(miAutoDeobfuscation.isSelected());
                    mainFrame.getPanel().autoDeobfuscateChanged();
                } else {
                    miAutoDeobfuscation.setSelected(!miAutoDeobfuscation.isSelected());
                }
                break;
            case ACTION_CLEAR_RECENT_FILES:
                Configuration.recentFiles.set(null);
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
                setSubLimiter(((JCheckBox) e.getSource()).isSelected());
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
            case ACTION_CLOSE:
                close();
                break;
            case ACTION_CLOSE_ALL:
                closeAll();
                break;
            case ACTION_IMPORT_TEXT:
                importText();
                break;
            case ACTION_IMPORT_SCRIPT:
                importScript();
                break;
            case ACTION_IMPORT_SYMBOL_CLASS:
                importSymbolClass();
                break;

            case ACTION_EXPORT_SWF_XML:
                mainFrame.getPanel().exportSwfXml();
                break;
            case ACTION_IMPORT_SWF_XML:
                mainFrame.getPanel().importSwfXml();
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
        }
    }
}
