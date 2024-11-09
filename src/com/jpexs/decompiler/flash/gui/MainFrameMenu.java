/*
 *  Copyright (C) 2010-2024 JPEXS
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

import com.jpexs.debugger.flash.DebuggerCommands;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItemChangeListener;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.easygui.EasyPanel;
import com.jpexs.decompiler.flash.exporters.swf.SwfFlashDevelopExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfIntelliJIdeaExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfVsCodeExporter;
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.gui.helpers.CheckResources;
import com.jpexs.decompiler.flash.search.ScriptSearchResult;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.sun.jna.Platform;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author JPEXS
 */
public abstract class MainFrameMenu implements MenuBuilder {

    private final MainFrame mainFrame;

    private KeyEventDispatcher keyEventDispatcher;

    private Openable openable;

    private ConfigurationItemChangeListener<Boolean> configListenerAutoDeobfuscate;

    private ConfigurationItemChangeListener<Boolean> configListenerFlattenASPackages;

    private ConfigurationItemChangeListener<Boolean> configListenerSimplifyExpressions;

    private ConfigurationItemChangeListener<Boolean> configListenerInternalFlashViewer;

    private ConfigurationItemChangeListener<Boolean> configListenerParallelSpeedUp;

    private ConfigurationItemChangeListener<Boolean> configListenerDecompile;

    //private ConfigurationItemChangeListener<Boolean> configListenerCacheOnDisk;
    private ConfigurationItemChangeListener<Boolean> configListenerGotoMainClassOnStartup;

    private ConfigurationItemChangeListener<Boolean> configListenerAutoRenameIdentifiers;

    private ConfigurationItemChangeListener<Boolean> configListenerAutoOpenLoadedSWFs;

    protected final Map<String, HotKey> menuHotkeys = new HashMap<>();

    @Override
    public HotKey getMenuHotkey(String path) {
        return menuHotkeys.get(path);
    }

    protected final Map<String, ActionListener> menuActions = new HashMap<>();

    /*public boolean isInternalFlashViewerSelected() {
        return isMenuChecked("/settings/internalViewer"); //miInternalViewer.isSelected();
    }*/
    private final boolean externalFlashPlayerUnavailable;

    public MainFrameMenu(MainFrame mainFrame, boolean externalFlashPlayerUnavailable) {
        registerHotKeys();
        this.mainFrame = mainFrame;
        this.externalFlashPlayerUnavailable = externalFlashPlayerUnavailable;
    }

    protected String translate(String key) {
        return mainFrame.translate(key);
    }

    protected boolean openActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }

        Main.openFileDialog();
        return true;
    }

    private boolean saveOpenable(Openable openable) {
        boolean saved = false;
        if (openable != null) {
            if (openable.getOpenableList() != null && openable.getOpenableList().isBundle()) {
                File savedFile = new File(openable.getOpenableList().sourceInfo.getFile());
                Main.startSaving(savedFile);
                Bundle bundle = openable.getOpenableList().bundle;
                if (!bundle.isReadOnly()) {

                    if (openable instanceof SWF) {
                        SWF swf = (SWF) openable;
                        swf.saveNestedDefineBinaryData();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        openable.saveTo(baos);
                        saved = bundle.putOpenable(openable.getFileTitle(), new ByteArrayInputStream(baos.toByteArray()));
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrameMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                    }
                } else {
                    ViewMessages.showMessageDialog(mainFrame.getWindow(), translate("error.readonly.cannotSave"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                Main.stopSaving(savedFile);
            } else if ((openable instanceof SWF) && ((SWF) openable).binaryData != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    SWF swf = (SWF) openable;
                    swf.saveNestedDefineBinaryData();
                    swf.saveTo(baos);
                    byte[] data = baos.toByteArray();
                    swf.binaryData.setDataBytes(new ByteArrayRange(data));
                    swf.binaryData.setModified(true);
                    swf.binaryData.getTopLevelBinaryData().pack();
                    saved = saveOpenable(swf.binaryData.getSwf()); //save parent swf                   
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                }
            } else if (openable.getFile() == null) {
                saved = saveAs(openable, SaveFileMode.SAVEAS);
            } else {
                try {
                    Main.saveFile(openable, openable.getFile());
                    saved = true;
                } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
                    Main.handleSaveError(ex);
                }
            }
            if (saved) {
                openable.clearModified();
                mainFrame.getPanel().refreshTree(openable);
            }
        }
        return saved;
    }

    protected void newActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        Main.newFile();
    }

    protected boolean saveActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }
        return saveOpenable(openable);
    }

    protected boolean saveAllActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }
        List<OpenableList> openableLists = mainFrame.getPanel().getSwfs();
        List<Openable> allOpenables = new ArrayList<>();
        for (OpenableList list : openableLists) {
            for (Openable openable : list.items) {
                allOpenables.add(openable);
            }
        }
        for (Openable openable : allOpenables) {
            if (openable.isModified()) {
                if (!saveOpenable(openable)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean saveAsActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }

        if (openable != null) {
            if (saveAs(openable, SaveFileMode.SAVEAS)) {
                if (openable.getOpenableList() != null) { //binarydata won't clear modified on saveas                    
                    if (!isSwfReadOnly(openable)) {
                        openable.clearModified();
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean isSwfReadOnly(Openable openable) {
        return openable.getOpenableList() != null && openable.getOpenableList().bundle != null && openable.getOpenableList().bundle.isReadOnly();
    }

    private boolean saveAs(Openable openable, SaveFileMode mode) {
        View.checkAccess();

        if (Main.saveFileDialog(openable, mode)) {
            updateComponents(openable);
            return true;
        }
        return false;
    }

    protected void saveAsExeActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        if (openable != null) {
            saveAs(openable, SaveFileMode.EXE);
        }
    }

    private void enumerateListsToClose(Set<OpenableList> listsToClose, Openable openable, List<SWF> binaryDataClosedSwfs) {
        if (openable instanceof SWF) {
            SWF swf = (SWF) openable;
            if (swf.binaryData != null) {
                // embedded swf
                swf.binaryData.setInnerSwf(null);
                swf.clearTagSwfs();
                binaryDataClosedSwfs.add(swf);
            } else {
                listsToClose.add(swf.openableList);
            }
        } else if (openable != null) {
            listsToClose.add(openable.getOpenableList());
        }
    }

    protected void closeActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        Set<OpenableList> listsToClose = new LinkedHashSet<>();
        List<SWF> binaryDataClosedSwfs = new ArrayList<>();

        for (TreeItem item : mainFrame.getPanel().getCurrentTree().getSelected()) {
            if (item instanceof OpenableList) {
                listsToClose.add((OpenableList) item);
            } else {
                Openable itemOpenable = item.getOpenable();
                enumerateListsToClose(listsToClose, itemOpenable, binaryDataClosedSwfs);
            }
        }
        if (openable != null && !binaryDataClosedSwfs.contains(openable)) {
            enumerateListsToClose(listsToClose, openable, binaryDataClosedSwfs);
        }
        for (OpenableList list : listsToClose) {
            Main.closeFile(list);
        }
        mainFrame.getPanel().refreshTree();
        openable = null;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.gc();
            }
        }, 1000);
    }

    protected boolean closeAllActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }

        boolean result = Main.closeAll(false);
        if (result) {
            openable = null;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.gc();
                }
            }, 1000);
        }
        return result;
    }

    protected void importTextActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().importText((SWF) openable);
    }

    protected void importScriptActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().importScript(openable);
    }

    protected void importImagesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importImage((SWF) openable);
    }

    protected void importShapesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importShape((SWF) openable, false);
    }

    protected void importShapesNoFillActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importShape((SWF) openable, true);
    }

    protected void importSpritesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importSprite((SWF) openable);
    }

    protected void importMoviesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importMovie((SWF) openable);
    }

    protected void importSoundsActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        mainFrame.getPanel().importSound((SWF) openable);
    }

    protected void importSymbolClassActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().importSymbolClass((SWF) openable);
    }

    protected boolean exportAllActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }

        return export(false);
    }

    protected boolean exportSelectedActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }

        return export(true);
    }

    protected boolean export(boolean onlySelected) {
        View.checkAccess();

        if (openable != null) {
            mainFrame.getPanel().export(onlySelected, getSelectedOrCurrentOpenable());
            return true;
        }

        return false;
    }

    protected void exportFlashDevelopActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().exportFlashDevelop((SWF) openable);
    }

    protected void exportIdeaActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().exportIdea((SWF) openable);
    }
    
    protected void exportVsCodeActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().exportVsCode((SWF) openable);
    }

    protected void exportFlaActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().exportFla((SWF) openable);
    }

    protected void importXmlActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().importSwfXml(getSelectedOrCurrentOpenable());
    }

    protected void exportXmlActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().exportSwfXml(getSelectedOrCurrentOpenable());
    }

    private List<TreeItem> getSelectedOrCurrentOpenable() {
        List<TreeItem> sel = mainFrame.getPanel().getCurrentTree().getSelected();
        if (sel.isEmpty() && openable != null) {
            sel.add(openable);
        }
        return sel;
    }

    protected boolean searchActionPerformed(ActionEvent evt) {
        return search(evt, null);
    }

    protected boolean searchInTextPerformed(ActionEvent evt) {
        return search(evt, true);
    }

    protected boolean searchInActionPerformed(ActionEvent evt) {
        return search(evt, false);
    }

    protected boolean search(ActionEvent evt, Boolean searchInText) {
        View.checkAccess();

        if (openable != null) {
            mainFrame.getPanel().searchInActionScriptOrText(searchInText, openable, false);
            return true;
        }

        return false;
    }

    protected boolean replaceActionPerformed(ActionEvent evt) {
        if (openable != null) {
            mainFrame.getPanel().replaceText();
            return true;
        }

        return false;
    }

    protected boolean clearLog(ActionEvent evt) {
        ErrorLogFrame.getInstance().clearLog();
        return true;
    }

    protected void renameOneIdentifier(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().renameOneIdentifier((SWF) openable);
    }

    protected void renameColliding(ActionEvent evt) {
        View.checkAccess();

        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().renameColliding(openable);
    }

    protected void renameInvalidIdentifiers(ActionEvent evt) {
        View.checkAccess();

        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().renameIdentifiers(openable);
    }

    protected void deobfuscationActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        mainFrame.getPanel().deobfuscate();
    }

    protected void setSubLimiter(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();
        Main.setSubLimiter(selected);
    }

    protected void switchDebugger() {
        DebuggerTools.switchDebugger((SWF) openable);
    }

    protected void debuggerShowLogActionPerformed(ActionEvent evt) {
        DebuggerTools.debuggerShowLog();
    }

    protected void debuggerInjectLoader(ActionEvent evt) {
        DebuggerTools.injectDebugLoader((SWF) openable);
        refreshDecompiled();
    }

    protected void debuggerReplaceTraceCallsActionPerformed(ActionEvent evt) {
        ReplaceTraceDialog rtd = new ReplaceTraceDialog(Main.getDefaultDialogsOwner(), Configuration.lastDebuggerReplaceFunction.get());
        rtd.setVisible(true);
        if (rtd.getValue() != null) {
            String fname = rtd.getValue();
            DebuggerTools.replaceTraceCalls((SWF) openable, fname);
            mainFrame.getPanel().refreshDecompiled();
            Configuration.lastDebuggerReplaceFunction.set(rtd.getValue());
        }
    }

    protected void clearRecentFilesActionPerformed(ActionEvent evt) {
        Configuration.recentFiles.set(null);
    }

    protected void clearRecentSearchesActionPerformed(ActionEvent evt) {
        Main.searchResultsStorage.clear();
    }

    protected void clearPinnedItemsActionPerformed(ActionEvent evt) {
        mainFrame.getPanel().destroyPins();
    }

    protected void removeNonScripts() {
        if (openable instanceof SWF) {
            return;
        }
        mainFrame.getPanel().removeNonScripts((SWF) openable);
    }

    protected void removeExceptSelected() {
        if (openable instanceof SWF) {
            return;
        }
        mainFrame.getPanel().removeExceptSelected((SWF) openable);
    }

    protected void refreshDecompiled() {
        mainFrame.getPanel().refreshDecompiled();
    }

    protected boolean previousTag(ActionEvent evt) {
        return mainFrame.getPanel().previousTag();
    }

    protected boolean nextTag(ActionEvent evt) {
        return mainFrame.getPanel().nextTag();
    }

    protected void checkResources() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(os);
        CheckResources.checkResources(stream, null);
        final String str = new String(os.toByteArray(), Utf8Helper.charset);
        JDialog dialog = new JDialog() {
            @Override
            public void setVisible(boolean bln) {
                setSize(new Dimension(800, 600));
                Container cnt = getContentPane();
                cnt.setLayout(new BorderLayout());
                String[] languages = SelectLanguageDialog.getAvailableLanguages().clone();
                languages[0] = "all";
                JComboBox<String> languagesComboBox = new JComboBox<>(languages);
                this.add(languagesComboBox, BorderLayout.NORTH);
                ScrollPane scrollPane = new ScrollPane();
                JEditorPane editor = new JEditorPane();
                editor.setEditable(false);
                editor.setText(str);
                scrollPane.add(editor);
                this.add(scrollPane, BorderLayout.CENTER);
                this.setModal(true);
                View.centerScreen(this);
                languagesComboBox.addActionListener((ActionEvent e) -> {
                    String lang = (String) languagesComboBox.getSelectedItem();
                    if (lang.equals("all")) {
                        lang = null;
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try (PrintStream stream = new PrintStream(os, false, "UTF-8")) {
                        CheckResources.checkResources(stream, lang);
                        String str = new String(os.toByteArray(), Utf8Helper.charset);
                        editor.setText(str);
                    } catch (UnsupportedEncodingException ex) {
                        // ignore
                    }
                });
                super.setVisible(bln);
            }
        };
        dialog.setVisible(true);
    }

    protected void checkUpdatesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        if (!Main.checkForUpdates()) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void helpUsActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        String helpUsURL = ApplicationInfo.PROJECT_PAGE;
        if (!View.navigateUrl(helpUsURL)) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), translate("message.helpus").replace("%url%", helpUsURL));
        }
    }

    protected void homePageActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        String homePageURL = ApplicationInfo.PROJECT_PAGE;
        if (!View.navigateUrl(homePageURL)) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), translate("message.homepage").replace("%url%", homePageURL));
        }
    }
    
    protected void wikiActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        String wikiURL = ApplicationInfo.WIKI_PAGE;
        if (!View.navigateUrl(wikiURL)) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), translate("message.wiki").replace("%url%", wikiURL));
        }
    }

    protected void aboutActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }

        Main.about();
    }

    protected boolean reloadActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }
        if (openable != null) {
            if (!Configuration.showCloseConfirmation.get() || ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.reload"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                if (openable.getOpenableList() == null) {
                    if (openable instanceof SWF) {
                        SWF swf = (SWF) openable;
                        if (swf.binaryData != null) {
                            mainFrame.getPanel().loadFromBinaryTag(swf.binaryData);
                        }
                    }
                } else {
                    Main.reloadFile(openable.getOpenableList());
                }
            }
        }
        return true;
    }

    protected boolean reloadAllActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            return false;
        }
        if (openable != null) {
            if (!Configuration.showCloseConfirmation.get() || ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.reloadAll"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                Main.reloadApp();
            }

            return true;
        }

        Main.reloadApp();
        return true;
    }

    protected void advancedSettingsActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        Main.advancedSettings();
    }
    
    protected void solEditorActionPerformed(ActionEvent evt) {
        Main.openSolEditor();
    }

    protected void searchMemoryActionPerformed(ActionEvent evt) {
        Main.loadFromMemory();
    }

    protected void gotoDocumentClassOnStartupActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.gotoMainClassOnStartup.set(selected);
    }

    protected void autoOpenLoadedSWFsActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.autoOpenLoadedSWFs.set(selected);
    }

    protected void flattenASPackagesActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.flattenASPackages.set(selected);
    }

    protected void autoRenameIdentifiersActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (!selected || ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.autoRenameIdentifiers") + "\r\n" + translate("message.confirm.on"), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningRenameIdentifiers, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoRenameIdentifiers.set(selected);
            mainFrame.getPanel().autoDeobfuscateChanged();
        } else {
            button.setSelected(Configuration.autoRenameIdentifiers.get());
        }
    }

    /*protected void cacheOnDiskActionPerformed(ActionEvent evt) {
     AbstractButton button = (AbstractButton) evt.getSource();
     boolean selected = button.isSelected();

     Configuration.cacheOnDisk.set(selected);
     if (selected) {
     Cache.setStorageType(Cache.STORAGE_FILES);
     } else {
     Cache.setStorageType(Cache.STORAGE_MEMORY);
     }
     }*/
    protected void setLanguageActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            return;
        }
        new SelectLanguageDialog(Main.getDefaultDialogsOwner()).display();
    }

    protected void disableDecompilationActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.decompile.set(!selected);
        mainFrame.getPanel().disableDecompilationChanged();
    }

    protected void associateActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (selected == ContextMenuTools.isAddedToContextMenu()) {
            return;
        }
        ContextMenuTools.addToContextMenu(selected, false);

        // Update checkbox menuitem accordingly (User can cancel rights elevation)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                button.setSelected(ContextMenuTools.isAddedToContextMenu());
            }
        }, 1000); // It takes some time registry change to apply
    }

    protected void gotoDocumentClassActionPerformed(ActionEvent evt) {
        mainFrame.getPanel().gotoDocumentClass(mainFrame.getPanel().getCurrentSwf());
    }

    protected void parallelSpeedUpActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        String confStr = translate("message.confirm.parallel") + "\r\n";
        if (selected) {
            confStr += " " + translate("message.confirm.on");
        } else {
            confStr += " " + translate("message.confirm.off");
        }
        if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), confStr, translate("message.parallel"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.parallelSpeedUp.set(selected);
        } else {
            button.setSelected(Configuration.parallelSpeedUp.get());
        }
    }

    /*protected void internalViewerSwitchActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.internalFlashViewer.set(selected);
        mainFrame.getPanel().reload(true);
    }
     */
    protected void simplifyExpressionsActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.simplifyExpressions.set(selected);
        mainFrame.getPanel().autoDeobfuscateChanged();
    }

    protected void autoDeobfuscationActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.autodeobfuscate") + "\r\n" + (selected ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningDeobfuscation, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoDeobfuscate.set(selected);
            mainFrame.getPanel().autoDeobfuscateChanged();
        } else {
            button.setSelected(Configuration.autoDeobfuscate.get());
        }
    }

    /*protected void deobfuscationMode(ActionEvent evt, int mode) {
     Configuration.deobfuscationMode.set(mode);
     mainFrame.getPanel().autoDeobfuscateChanged();
     }*/
    protected void exitActionPerformed(ActionEvent evt) {
        JFrame frame = (JFrame) mainFrame;
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void updateComponents() {
        updateComponents(openable);
    }

    public void updateComponents(Openable openable) {
        this.openable = openable;
        boolean isRunning = Main.isRunning();
        boolean isDebugRunning = Main.isDebugRunning();
        boolean isDebugPaused = Main.isDebugPaused();

        boolean isRunningOrDebugging = isRunning || isDebugRunning;

        SWF swf = (openable instanceof SWF) ? (SWF) openable : null;
        boolean openableSelected = openable != null;
        boolean swfSelected = openable instanceof SWF;
        boolean abcSelected = openable instanceof ABC;
        boolean isAs3 = false;
        boolean canExportFlashDevelop = false;
        boolean canExportIdea = false;
        boolean canExportVsCode = false;
        if (swf != null) {
            isAs3 = swf.isAS3();
            canExportFlashDevelop = SwfFlashDevelopExporter.canExportSwf(swf);
            canExportIdea = SwfIntelliJIdeaExporter.canExportSwf(swf);
            canExportVsCode = SwfVsCodeExporter.canExportSwf(swf);
        }
        if (abcSelected) {
            isAs3 = true;
        }
        boolean isWorking = Main.isWorking();
        List<ABCContainerTag> abcList = swf != null ? swf.getAbcList() : null;
        boolean hasAbc = openableSelected && abcList != null && !abcList.isEmpty();
        boolean hasDebugger = hasAbc && DebuggerTools.hasDebugger(swf);
        MainPanel mainPanel = mainFrame.getPanel();
        boolean swfLoaded = mainPanel != null ? !mainPanel.getSwfs().isEmpty() : false;
        boolean swfIsNew = openableSelected && openable.getOpenableList() != null && openable.getOpenableList().sourceInfo.isEmpty();
        boolean anythingModified = mainPanel != null ? mainPanel.isModified() : false;
        boolean swfModified = swf == null ? false : swf.isModified();

        boolean allSameSwf = true;
        boolean allSameOpenable = true;
        boolean openableOrListSelected = openable != null;
        if (mainPanel != null) {
            List<TreeItem> items = mainPanel.getCurrentTree().getSelected();
            SWF firstSwf = null;
            Openable firstOpenable = null;
            for (TreeItem item : items) {
                if (item instanceof OpenableList) {
                    if (items.size() == 1) {
                        openableOrListSelected = true;
                    }
                    allSameSwf = false;
                    break;
                }
                Openable fopenable = item.getOpenable();
                if (firstOpenable == null) {
                    firstOpenable = fopenable;
                } else {
                    if (fopenable != firstOpenable) {
                        allSameOpenable = false;
                    }
                }
                if (firstSwf == null) {
                    if (fopenable instanceof SWF) {
                        firstSwf = (SWF) fopenable;
                    } else {
                        allSameSwf = false;
                        break;
                    }
                } else {
                    if (item.getOpenable() != firstSwf) {
                        allSameSwf = false;
                        break;
                    }
                }
            }
        }

        setMenuEnabled("_/open", !isWorking);
        setMenuEnabled("/file/open", !isWorking);
        setMenuEnabled("_/save", openableSelected && !isWorking && swfModified);
        setMenuEnabled("/file/save", openableSelected && !isWorking && swfModified);
        setMenuEnabled("_/saveAs", openableSelected && !isWorking);
        setMenuEnabled("/file/saveAs", openableSelected && !isWorking);
        setMenuEnabled("_/saveAll", !isWorking && anythingModified);
        setMenuEnabled("/file/saveAll", !isWorking && anythingModified);
        setMenuEnabled("_/close", openableOrListSelected && !isWorking);
        setMenuEnabled("/file/close", openableOrListSelected && !isWorking);
        setMenuEnabled("_/closeAll", swfLoaded && !isWorking);
        setMenuEnabled("/file/closeAll", swfLoaded && !isWorking);
        setMenuEnabled("/file/reload", openableSelected && !swfIsNew && !isWorking);

        setMenuEnabled("/file/export", openableSelected);
        setMenuEnabled("_/exportAll", openableSelected && !isWorking);
        setMenuEnabled("/file/export/exportAll", openableSelected && !isWorking);
        setMenuEnabled("_/exportFla", swfSelected && !isWorking);
        setMenuEnabled("/file/export/exportFla", allSameSwf && openableSelected && !isWorking);
        setMenuEnabled("_/exportFlashDevelop", swfSelected && !isWorking && canExportFlashDevelop);
        setMenuEnabled("/file/export/exportFlashDevelop", allSameSwf && openableSelected && isAs3 && !isWorking && canExportFlashDevelop);
        setMenuEnabled("_/exportIdea", swfSelected && !isWorking && canExportIdea);
        setMenuEnabled("/file/export/exportIdea", allSameSwf && openableSelected && isAs3 && !isWorking && canExportIdea);
        setMenuEnabled("_/exportVsCode", swfSelected && !isWorking && canExportVsCode);
        setMenuEnabled("/file/export/exportVsCode", allSameSwf && openableSelected && isAs3 && !isWorking && canExportVsCode);
        setMenuEnabled("_/exportSelected", openableSelected && !isWorking);
        setMenuEnabled("/file/export/exportSelected", openableSelected && !isWorking);
        setMenuEnabled("/file/export/exportXml", swfSelected && !isWorking);

        setMenuEnabled("/import/importtab", openableSelected);
        setMenuEnabled("/import/importtab/importText", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importScript", allSameOpenable && openableSelected && !isWorking);
        setMenuEnabled("/import/importtab/importImages", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importSprites", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importShapes", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importShapesNoFill", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importMovies", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importSounds", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importSymbolClass", allSameSwf && swfSelected && !isWorking);
        setMenuEnabled("/import/importtab/importXml", allSameSwf && swfSelected && !isWorking);

        setMenuEnabled("/tools/deobfuscation", openableSelected);
        setMenuEnabled("/tools/deobfuscation/renameOneIdentifier", swfSelected && !isWorking);
        setMenuEnabled("/tools/deobfuscation/renameInvalidIdentifiers", swfSelected && !isWorking);
        setMenuEnabled("/tools/deobfuscation/renameColliding", swfSelected && !isWorking);
        setMenuEnabled("/tools/deobfuscation/deobfuscation", hasAbc);

        setMenuEnabled("/tools/search", openableSelected);
        setMenuEnabled("/tools/replace", swfSelected);
        //setMenuEnabled("/file/view/easy", swfSelected);
        setMenuEnabled("/tools/abcExplorer", isAs3);

        setMenuEnabled("/tools/gotoDocumentClass", hasAbc);
        setMenuEnabled("/tools/saveAsExe", swfSelected && !isWorking);

        /*setMenuEnabled("/tools/debugger/debuggerSwitch", hasAbc);
         setMenuChecked("/tools/debugger/debuggerSwitch", hasDebugger);
         setMenuEnabled("/tools/debugger/debuggerReplaceTrace", hasAbc && hasDebugger);*/
        //setMenuEnabled("/tools/debugger/debuggerInjectLoader", hasAbc && hasDebugger);
        setMenuEnabled("_/checkUpdates", !isWorking);
        setMenuEnabled("/help/checkUpdates", !isWorking);
        //setMenuEnabled("/help/helpUs", !isWorking);
        setMenuEnabled("/help/homePage", !isWorking);
        setMenuEnabled("/help/wiki", !isWorking);
        setMenuEnabled("_/about", !isWorking);
        setMenuEnabled("/help/about", !isWorking);

        setMenuEnabled("/file/start/run", swfSelected && !isRunningOrDebugging);
        setMenuEnabled("/file/start/debug", swfSelected && !isRunningOrDebugging);
        setMenuEnabled("/file/start/debugpcode", swfSelected && !isRunningOrDebugging);

        setMenuEnabled("/file/start/stop", isRunningOrDebugging);
        setMenuEnabled("/debugging/debug/stop", isRunningOrDebugging); //same as previous

        setPathVisible("/debugging", isDebugRunning);
        setPathVisible("/debugging/debug", isDebugRunning);
        //setMenuEnabled("/debugging/debug/pause", isDebugRunning);
        setMenuEnabled("/debugging/debug/stepOver", isDebugPaused);
        setMenuEnabled("/debugging/debug/stepInto", isDebugPaused);
        setMenuEnabled("/debugging/debug/stepOut", isDebugPaused);
        setMenuEnabled("/debugging/debug/continue", isDebugPaused);
        //setMenuEnabled("/debugging/debug/stack", isDebugPaused);
        //setMenuEnabled("/debugging/debug/watch", isDebugPaused);
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(ApplicationInfo.applicationVerName);

        if (Configuration.displayFileName.get() && openable != null) {
            titleBuilder.append(" - ");
            titleBuilder.append(openable.getFullPathTitle());
        }
        mainFrame.setTitle(titleBuilder.toString());

        if (mainPanel != null) {
            switch (mainPanel.getCurrentView()) {
                case MainPanel.VIEW_RESOURCES:
                    setGroupSelection("view", "/file/view/viewResources");
                    break;
                case MainPanel.VIEW_TAGLIST:
                    setGroupSelection("view", "/file/view/viewTagList");
                    break;
                case MainPanel.VIEW_DUMP:
                    setGroupSelection("view", "/file/view/viewHex");
                    break;
                case MainPanel.VIEW_EASY:
                    if (EasyPanel.EASY_AVAILABLE) {
                        setMenuChecked("/file/view/easy", true);
                    }
                    break;
            }
        }
    }

    private void registerHotKeys() {

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(keyEventDispatcher = this::dispatchKeyEvent);
    }

    public void createMenuBar() {
        initMenu();

        if (supportsAppMenu()) {
            addMenuItem("_", null, null, null, 0, null, false, null, false);
            addMenuItem("_/open", translate("menu.file.open"), "open32", this::openActionPerformed, PRIORITY_TOP, this::loadRecent, false, null, false);
            addMenuItem("_/save", translate("menu.file.save"), "save32", this::saveActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/saveAll", translate("menu.file.saveAll"), "saveall32", this::saveAllActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/saveAs", translate("menu.file.saveas"), "saveas32", this::saveAsActionPerformed, PRIORITY_TOP, null, true, null, false);
            addSeparator("_");
            addMenuItem("_/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFlaActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/exportFlashDevelop", translate("menu.file.export.flashDevelop"), "exportflashdevelop32", this::exportFlashDevelopActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/exportIdea", translate("menu.file.export.idea"), "exportidea32", this::exportIdeaActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/exportVsCode", translate("menu.file.export.vsCode"), "exportvscode32", this::exportVsCodeActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/exportAll", translate("menu.file.export.all"), "export32", this::exportAllActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/exportSelected", translate("menu.file.export.selection"), "exportsel32", this::exportSelectedActionPerformed, PRIORITY_TOP, null, true, null, false);
            addSeparator("_");
            addMenuItem("_/checkUpdates", translate("menu.help.checkupdates"), "update32", this::checkUpdatesActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/about", translate("menu.help.about"), "about32", this::aboutActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/close", translate("menu.file.close"), "close32", this::closeActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/closeAll", translate("menu.file.closeAll"), "closeall32", this::closeAllActionPerformed, PRIORITY_TOP, null, true, null, false);
            addMenuItem("_/$exit", translate("menu.file.exit"), "exit32", this::exitActionPerformed, PRIORITY_TOP, null, true, null, false);
            finishMenu("_");
        }

        addMenuItem("/file", translate("menu.file"), null, null, 0, null, false, null, false);
        addMenuItem("/file/open", translate("menu.file.open"), "open32", this::openActionPerformed, PRIORITY_TOP, this::loadRecent, !supportsMenuAction(), new HotKey("CTRL+SHIFT+O"), false);

        if (!supportsMenuAction()) {
            addMenuItem("/file/recent", translate("menu.recentFiles"), null, null, 0, this::loadRecent, false, null, false);
            finishMenu("/file/recent");
        } else {
            finishMenu("/file/open");
        }

        addMenuItem("/file/new", translate("menu.file.new"), "newswf32", this::newActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/file/save", translate("menu.file.save"), "save16", this::saveActionPerformed, PRIORITY_TOP, null, true, new HotKey("CTRL+SHIFT+S"), false);
        addMenuItem("/file/saveAll", translate("menu.file.saveAll"), "saveall32", this::saveAllActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/saveAs", translate("menu.file.saveas"), "saveas16", this::saveAsActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+SHIFT+A"), false);
        addMenuItem("/file/reload", translate("menu.file.reload"), "reload16", this::reloadActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+SHIFT+R"), false);
        addMenuItem("/file/reloadAll", translate("menu.file.reloadAll"), "reload16", this::reloadAllActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/close", translate("menu.file.close"), "close32", this::closeActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/closeAll", translate("menu.file.closeAll"), "closeall32", this::closeAllActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+SHIFT+X"), false);

        addSeparator("/file");

        addMenuItem("/file/export", translate("menu.export"), null, null, 0, null, false, null, false);
        addMenuItem("/file/export/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFlaActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/file/export/exportFlashDevelop", translate("menu.file.export.flashDevelop"), "exportflashdevelop32", this::exportFlashDevelopActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/export/exportIdea", translate("menu.file.export.idea"), "exportidea32", this::exportIdeaActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/export/exportVsCode", translate("menu.file.export.vsCode"), "exportvscode32", this::exportVsCodeActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/export/exportXml", translate("menu.file.export.xml"), "exportxml32", this::exportXmlActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/file/export/exportAll", translate("menu.file.export.all"), "export16", this::exportAllActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+SHIFT+E"), false);
        addMenuItem("/file/export/exportSelected", translate("menu.file.export.selection"), "exportsel16", this::exportSelectedActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        finishMenu("/file/export");

        addMenuItem("/import", translate("menu.import"), null, null, 0, null, false, null, false);
        addMenuItem("/import/importtab", translate("menu.import"), null, null, 0, null, false, null, false);
        addMenuItem("/import/importtab/importXml", translate("menu.file.import.xml"), "importxml32", this::importXmlActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/import/importtab/importText", translate("menu.file.import.text"), "importtext32", this::importTextActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importScript", translate("menu.file.import.script"), "importscript32", this::importScriptActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importImages", translate("menu.file.import.image"), "importimage32", this::importImagesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importShapes", translate("menu.file.import.shape"), "importshape32", this::importShapesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importShapesNoFill", translate("menu.file.import.shapeNoFill"), "importshape32", this::importShapesNoFillActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importSprites", translate("menu.file.import.sprite"), "importsprite32", this::importSpritesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importMovies", translate("menu.file.import.movie"), "importmovie32", this::importMoviesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importSounds", translate("menu.file.import.sound"), "importsound32", this::importSoundsActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/import/importtab/importSymbolClass", translate("menu.file.import.symbolClass"), "importsymbolclass32", this::importSymbolClassActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        finishMenu("/import/importtab");
        finishMenu("/import");

        addMenuItem("/file/start", translate("menu.file.start"), null, null, 0, null, false, null, false);
        addMenuItem("/file/start/run", translate("menu.file.start.run"), "play32", this::runActionPerformed, PRIORITY_TOP, null, true, new HotKey("F6"), false);
        addMenuItem("/file/start/stop", translate("menu.file.start.stop"), "stop32", this::stopActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/file/start/debug", translate("menu.file.start.debug"), "debug32", this::debugActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+F5"), false);
        addMenuItem("/file/start/debugpcode", translate("menu.file.start.debugpcode"), "debug32", this::debugPCodeActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        finishMenu("/file/start");

        addMenuItem("/file/view", translate("menu.view"), null, null, 0, null, false, null, false);
        addToggleMenuItem("/file/view/viewResources", translate("menu.file.view.resources"), "view", "viewresources16", this::viewResourcesActionPerformed, PRIORITY_MEDIUM, null);
        addToggleMenuItem("/file/view/viewTagList", translate("menu.file.view.tagList"), "view", "taglist16", this::viewTagListActionPerformed, PRIORITY_MEDIUM, null);
        addToggleMenuItem("/file/view/viewHex", translate("menu.file.view.hex"), "view", "viewhex16", this::viewHexActionPerformed, PRIORITY_MEDIUM, null);
        if (EasyPanel.EASY_AVAILABLE) {
            addToggleMenuItem("/file/view/easy", translate("menu.file.view.easy"), null, "easy32", this::easyActionPerformed, PRIORITY_TOP, null);
        }
        finishMenu("/file/view");

        addSeparator("/file");

        if (!supportsAppMenu()) {
            addSeparator("/file");
            addMenuItem("/file/exit", translate("menu.file.exit"), "exit32", this::exitActionPerformed, PRIORITY_TOP, null, true, null, false);
        }

        finishMenu("/file");

        /*
         menu.file.start = Start
         menu.file.start.run = Run
         menu.file.start.stop = Stop
         menu.file.start.debug = Debug
         menu.debugging = Debugging
         menu.debugging.debug = Debug
         menu.debugging.debug.stop = Stop
         menu.debugging.debug.pause = Pause
         menu.debugging.debug.stepOver = Step over
         menu.debugging.debug.stepInto = Step into
         menu.debugging.debug.stepOut = Step out
         menu.debugging.debug.continue = Continue
         menu.debugging.debug.stack = Stack...
         menu.debugging.debug.watch = New watch...
         */
        addMenuItem("/debugging", translate("menu.debugging"), null, null, 0, null, false, null, true);
        addMenuItem("/debugging/debug", translate("menu.debugging.debug"), null, null, 0, null, false, null, false);
        addMenuItem("/debugging/debug/stop", translate("menu.file.start.stop"), "stop32", this::stopActionPerformed, PRIORITY_TOP, null, true, null, false);
        //addMenuItem("/debugging/debug/pause", translate("menu.debugging.debug.pause"), "pause32", this::pauseActionPerformed, PRIORITY_TOP, null, true,false);
        addMenuItem("/debugging/debug/continue", translate("menu.debugging.debug.continue"), "continue32", this::continueActionPerformed, PRIORITY_TOP, null, true, new HotKey("F5"), false);
        addMenuItem("/debugging/debug/stepOver", translate("menu.debugging.debug.stepOver"), "stepover32", this::stepOverActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("F8"), false);
        addMenuItem("/debugging/debug/stepInto", translate("menu.debugging.debug.stepInto"), "stepinto32", this::stepIntoActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("F7"), false);
        addMenuItem("/debugging/debug/stepOut", translate("menu.debugging.debug.stepOut"), "stepout32", this::stepOutActionPerformed, PRIORITY_MEDIUM, null, true, new HotKey("CTRL+F7"), false);
        //addMenuItem("/debugging/debug/stack", translate("menu.debugging.debug.stack"), "stack32", this::stackActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        //addMenuItem("/debugging/debug/watch", translate("menu.debugging.debug.watch"), "watch32", this::watchActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        finishMenu("/debugging/debug");
        finishMenu("/debugging");

        addMenuItem("/tools", translate("menu.tools"), null, null, 0, null, false, null, false);
        addMenuItem("/tools/search", translate("menu.tools.search"), "search16", this::searchActionPerformed, PRIORITY_TOP, this::loadRecentSearches, !supportsMenuAction(), null, false);

        if (!supportsMenuAction()) {
            addMenuItem("/tools/recentsearch", translate("menu.recentSearches"), null, null, 0, this::loadRecentSearches, false, null, false);
            finishMenu("/tools/recentsearch");
        } else {
            finishMenu("/tools/search");
        }

        addMenuItem("/tools/replace", translate("menu.tools.replace"), "replace32", this::replaceActionPerformed, PRIORITY_TOP, null, true, null, false);
        
        addMenuItem("/tools/abcExplorer", translate("menu.tools.abcexplorer"), "abcexplorer32", this::abcExplorerActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/tools/gotoDocumentClass", translate("menu.tools.gotoDocumentClass"), "gotomainclass32", this::gotoDocumentClassActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/tools/solEditor", translate("menu.tools.solEditor"), "soleditor32", this::solEditorActionPerformed, PRIORITY_TOP, null, true, null, false);
        if (Platform.isWindows()) {
            addMenuItem("/tools/searchMemory", translate("menu.tools.searchMemory"), "loadmemory16", this::searchMemoryActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        }
        addMenuItem("/tools/saveAsExe", translate("menu.file.saveasexe"), "saveasexe16", this::saveAsExeActionPerformed, PRIORITY_MEDIUM, null, true, null, false);

        //addMenuItem("/tools/searchCache", translate("menu.tools.searchCache"), "loadcache16", this::searchCacheActionPerformed, PRIORITY_MEDIUM, null, true, null);
        addMenuItem("/tools/deobfuscation", translate("menu.tools.deobfuscation"), "deobfuscate16", null, 0, null, false, null, false);
        addMenuItem("/tools/deobfuscation/renameOneIdentifier", translate("menu.tools.deobfuscation.globalrename"), "rename16", this::renameOneIdentifier, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/tools/deobfuscation/renameInvalidIdentifiers", translate("menu.tools.deobfuscation.renameinvalid"), "renameall16", this::renameInvalidIdentifiers, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/tools/deobfuscation/renameColliding", translate("menu.tools.deobfuscation.renameColliding"), "renameall16", this::renameColliding, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/tools/deobfuscation/deobfuscation", translate("menu.tools.deobfuscation.pcode"), "deobfuscate32", this::deobfuscationActionPerformed, PRIORITY_TOP, null, true, null, false);
        finishMenu("/tools/deobfuscation");

        /*addMenuItem("/tools/debugger", translate("menu.debugger"), null, null, 0, null, false, null,false);
         addToggleMenuItem("/tools/debugger/debuggerSwitch", translate("menu.debugger.switch"), null, "debugger32", this::debuggerSwitchActionPerformed, PRIORITY_TOP, null,false);
         addMenuItem("/tools/debugger/debuggerReplaceTrace", translate("menu.debugger.replacetrace"), "debuggerreplace16", this::debuggerReplaceTraceCallsActionPerformed, PRIORITY_MEDIUM, null, true, null,false);
         //addMenuItem("/tools/debugger/debuggerInjectLoader", "Inject Loader", "debuggerreplace16", this::debuggerInjectLoader, PRIORITY_MEDIUM, null, true,false);
         addMenuItem("/tools/debugger/debuggerShowLog", translate("menu.debugger.showlog"), "debuggerlog16", this::debuggerShowLogActionPerformed, PRIORITY_MEDIUM, null, true, null,false);
         finishMenu("/tools/debugger");*/
        finishMenu("/tools");

        //Settings
        addMenuItem("/settings", translate("menu.settings"), null, null, 0, null, false, null, false);

        addToggleMenuItem("/settings/autoDeobfuscation", translate("menu.settings.autodeobfuscation"), null, null, this::autoDeobfuscationActionPerformed, 0, null);
        addToggleMenuItem("/settings/simplifyExpressions", translate("menu.settings.simplifyExpressions"), null, null, this::simplifyExpressionsActionPerformed, 0, null);
        //addToggleMenuItem("/settings/internalViewer", translate("menu.settings.internalflashviewer"), null, null, this::internalViewerSwitchActionPerformed, 0, null);
        addToggleMenuItem("/settings/parallelSpeedUp", translate("menu.settings.parallelspeedup"), null, null, this::parallelSpeedUpActionPerformed, 0, null);
        addToggleMenuItem("/settings/disableDecompilation", translate("menu.settings.disabledecompilation"), null, null, this::disableDecompilationActionPerformed, 0, null);
        //addToggleMenuItem("/settings/cacheOnDisk", translate("menu.settings.cacheOnDisk"), null, null, this::cacheOnDiskActionPerformed, 0, null);
        addToggleMenuItem("/settings/gotoMainClassOnStartup", translate("menu.settings.gotoMainClassOnStartup"), null, null, this::gotoDocumentClassOnStartupActionPerformed, 0, null);
        addToggleMenuItem("/settings/autoRenameIdentifiers", translate("menu.settings.autoRenameIdentifiers"), null, null, this::autoRenameIdentifiersActionPerformed, 0, null);
        addToggleMenuItem("/settings/autoOpenLoadedSWFs", translate("menu.settings.autoOpenLoadedSWFs"), null, null, this::autoOpenLoadedSWFsActionPerformed, 0, null);
        addToggleMenuItem("/settings/flattenASPackages", translate("menu.settings.flattenASPackages"), null, null, this::flattenASPackagesActionPerformed, 0, null);
        if (Platform.isWindows()) {
            addToggleMenuItem("/settings/associate", translate("menu.settings.addtocontextmenu"), null, null, this::associateActionPerformed, 0, null);
        }

        addMenuItem("/settings/language", translate("menu.language"), null, null, 0, null, false, null, false);
        addMenuItem("/settings/language/setLanguage", translate("menu.settings.language"), "setlanguage32", this::setLanguageActionPerformed, PRIORITY_TOP, null, true, null, false);
        finishMenu("/settings/language");

        /*addMenuItem("/settings/deobfuscation", translate("menu.deobfuscation"), null, null, 0, null, false,false);
         addToggleMenuItem("/settings/deobfuscation/old", translate("menu.file.deobfuscation.old"), "deobfuscation", "deobfuscateold16", (ActionEvent e) -> {
         deobfuscationMode(e, 0);
         }, 0);
         addToggleMenuItem("/settings/deobfuscation/new", translate("menu.file.deobfuscation.new"), "deobfuscation", "deobfuscatenew16", (ActionEvent e) -> {
         deobfuscationMode(e, 1);
         }, 0);

         finishMenu("/settings/deobfuscation");*/
        addMenuItem("/settings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), null, null, 0, null, false, null, false);
        addMenuItem("/settings/advancedSettings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), "settings32", this::advancedSettingsActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/settings/advancedSettings/clearRecentFiles", translate("menu.tools.otherTools.clearRecentFiles"), "clearrecent16", this::clearRecentFilesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/settings/advancedSettings/clearRecentSearches", translate("menu.tools.otherTools.clearRecentSearches"), "clearrecent16", this::clearRecentSearchesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/settings/advancedSettings/clearPinnedItems", translate("menu.tools.otherTools.clearPinnedItems"), "clearrecent16", this::clearPinnedItemsActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        finishMenu("/settings/advancedSettings");

        finishMenu("/settings");

        setMenuChecked("/settings/autoDeobfuscation", Configuration.autoDeobfuscate.get());
        Configuration.autoDeobfuscate.addListener(configListenerAutoDeobfuscate = (Boolean newValue) -> {
            setMenuChecked("/settings/autoDeobfuscation", newValue);
        });

        setMenuChecked("/settings/simplifyExpressions", Configuration.simplifyExpressions.get());
        Configuration.simplifyExpressions.addListener(configListenerSimplifyExpressions = (Boolean newValue) -> {
            setMenuChecked("/settings/simplifyExpressions", newValue);
        });

        /*setMenuChecked("/settings/internalViewer", Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        Configuration.internalFlashViewer.addListener(configListenerInternalFlashViewer = (Boolean newValue) -> {
            setMenuChecked("/settings/internalViewer", newValue || externalFlashPlayerUnavailable);
        });
         */
        setMenuChecked("/settings/parallelSpeedUp", Configuration.parallelSpeedUp.get());
        Configuration.parallelSpeedUp.addListener(configListenerParallelSpeedUp = (Boolean newValue) -> {
            setMenuChecked("/settings/parallelSpeedUp", newValue);
        });

        setMenuChecked("/settings/disableDecompilation", !Configuration.decompile.get());
        Configuration.decompile.addListener(configListenerDecompile = (Boolean newValue) -> {
            setMenuChecked("/settings/disableDecompilation", !newValue);
        });

        /*setMenuChecked("/settings/cacheOnDisk", Configuration.cacheOnDisk.get());
         Configuration.cacheOnDisk.addListener(configListenerCacheOnDisk = (Boolean newValue) -> {
         setMenuChecked("/settings/cacheOnDisk", newValue);
         });*/
        setMenuChecked("/settings/gotoMainClassOnStartup", Configuration.gotoMainClassOnStartup.get());
        Configuration.gotoMainClassOnStartup.addListener(configListenerGotoMainClassOnStartup = (Boolean newValue) -> {
            setMenuChecked("/settings/gotoMainClassOnStartup", newValue);
        });

        setMenuChecked("/settings/autoRenameIdentifiers", Configuration.autoRenameIdentifiers.get());
        Configuration.autoRenameIdentifiers.addListener(configListenerAutoRenameIdentifiers = (Boolean newValue) -> {
            setMenuChecked("/settings/autoRenameIdentifiers", newValue);
        });

        setMenuChecked("/settings/autoOpenLoadedSWFs", Configuration.autoOpenLoadedSWFs.get());
        Configuration.autoOpenLoadedSWFs.addListener(configListenerAutoOpenLoadedSWFs = (Boolean newValue) -> {
            setMenuChecked("/settings/autoOpenLoadedSWFs", newValue);
        });

        setMenuChecked("/settings/flattenASPackages", Configuration.flattenASPackages.get());
        Configuration.flattenASPackages.addListener(configListenerFlattenASPackages = (Boolean newValue) -> {
            setMenuChecked("/settings/flattenASPackages", newValue);
        });

        if (Platform.isWindows()) {
            setMenuChecked("/settings/associate", ContextMenuTools.isAddedToContextMenu());
        }

        //Help
        addMenuItem("/help", translate("menu.help"), null, null, 0, null, false, null, false);
        //addMenuItem("/help/helpUs", translate("menu.help.helpus"), "donate32", this::helpUsActionPerformed, PRIORITY_TOP, null, true, null, false);
        addMenuItem("/help/homePage", translate("menu.help.homepage"), "homepage16", this::homePageActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/help/wiki", translate("menu.help.wiki"), "wiki16", this::wikiActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addSeparator("/help");
        addMenuItem("/help/checkUpdates", translate("menu.help.checkupdates"), "update16", this::checkUpdatesActionPerformed, PRIORITY_MEDIUM, null, true, null, false);
        addMenuItem("/help/about", translate("menu.help.about"), "about32", this::aboutActionPerformed, PRIORITY_TOP, null, true, null, false);
        finishMenu("/help");

        if (Configuration._showDebugMenu.get() || Configuration._debugMode.get()) {

            addMenuItem("/debug", "# FFDec Debug #", null, null, 0, null, false, null, false);
            addMenuItem("/debug/removeNonScripts", "Remove non scripts", "continue16", e -> removeNonScripts(), PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/removeExceptSelected", "Remove except selected", "continue16", e -> removeExceptSelected(), PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/refreshDecompiled", "Refresh decompiled script", "continue16", e -> refreshDecompiled(), PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/checkResources", "Check resources", "continue16", e -> checkResources(), PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/callGc", "Call System.gc()", "continue16", e -> System.gc(), PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/emptyCache", "Empty cache", "continue16", e -> {
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.clearAllCache();
                }
            }, PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/emptyAllCache", "Empty all caches", "continue16", e -> {
                Cache.clearAll();
            }, PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/memoryInformation", "Memory information", "continue16", e -> {
                String architecture = System.getProperty("sun.arch.data.model");
                Runtime runtime = Runtime.getRuntime();
                String info = "Architecture: " + architecture + Helper.newLine
                        + "Jre 64bit: " + Helper.is64BitJre() + Helper.newLine
                        + "Os 64bit: " + Helper.is64BitOs() + Helper.newLine
                        + "Max: " + (runtime.maxMemory() / 1024 / 1024) + "MB" + Helper.newLine
                        + "Total: " + (runtime.totalMemory() / 1024 / 1024) + "MB" + Helper.newLine
                        + "Used: " + ((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024) + "MB" + Helper.newLine
                        + "Free: " + (runtime.freeMemory() / 1024 / 1024) + "MB";
                ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), info);
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.clearAllCache();
                }
            }, PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/fixAs3Code", "Fix AS3 code", "continue16", e -> {
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.fixAS3Code();
                }
            }, PRIORITY_MEDIUM, null, true, null, false);
            addMenuItem("/debug/openTestSwfs", "Open test SWFs", "continue16", e -> {
                String path;

                OpenableSourceInfo[] sourceInfos = new OpenableSourceInfo[2];
                String mainPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                path = mainPath + "\\..\\..\\libsrc\\ffdec_lib\\testdata\\as2\\as2.swf";
                sourceInfos[0] = new OpenableSourceInfo(null, path, null);
                path = mainPath + "\\..\\..\\libsrc\\ffdec_lib\\testdata\\as3\\as3.swf";
                sourceInfos[1] = new OpenableSourceInfo(null, path, null);
                Main.openFile(sourceInfos);
            }, PRIORITY_MEDIUM, null, true, null, false);
            finishMenu("/debug");
        }

        finishMenu("");
    }

    public void showResourcesView() {
        viewResourcesActionPerformed(null);
    }

    public void showTagListView() {
        viewTagListActionPerformed(null);
    }

    private void reselectView() {
        switch (mainFrame.getPanel().getCurrentView()) {
            case MainPanel.VIEW_RESOURCES:
                setMenuChecked("/file/view/viewResources", true);
                break;
            case MainPanel.VIEW_TAGLIST:
                setMenuChecked("/file/view/viewTagList", true);
                break;
            case MainPanel.VIEW_DUMP:
                setMenuChecked("/file/view/viewHex", true);
                break;
            case MainPanel.VIEW_EASY:
                setMenuChecked("/file/view/easy", true);
                break;
        }
    }

    private void viewResourcesActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            reselectView();
            return;
        }
        Configuration.dumpView.set(false);
        mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        setGroupSelection("view", "/file/view/viewResources");
        if (EasyPanel.EASY_AVAILABLE) {
            setMenuChecked("/file/view/easy", false);
        }
    }

    private void viewHexActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            reselectView();
            return;
        }
        Configuration.dumpView.set(true);
        MainPanel mainPanel = mainFrame.getPanel();
        if (mainPanel.isModified()) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), translate("message.warning.hexViewNotUpToDate"), translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningHexViewNotUpToDate);
        }

        mainPanel.showView(MainPanel.VIEW_DUMP);
        setGroupSelection("view", "/file/view/viewHex");
        if (EasyPanel.EASY_AVAILABLE) {
            setMenuChecked("/file/view/easy", false);
        }
    }

    private void viewTagListActionPerformed(ActionEvent evt) {
        if (mainFrame.getPanel().checkEdited()) {
            reselectView();
            return;
        }
        Configuration.dumpView.set(false);
        MainPanel mainPanel = mainFrame.getPanel();
        mainPanel.showView(MainPanel.VIEW_TAGLIST);
        setGroupSelection("view", "/file/view/viewTagList");
        if (EasyPanel.EASY_AVAILABLE) {
            setMenuChecked("/file/view/easy", false);
        }
    }

    private void debuggerSwitchActionPerformed(ActionEvent evt) {
        boolean debuggerOn = isMenuChecked("/tools/debugger/debuggerSwitch");
        if (!debuggerOn || ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            switchDebugger();
            mainFrame.getPanel().refreshDecompiled();
        } else {
            setMenuChecked("/tools/debugger/debuggerSwitch", false);
        }
        setMenuEnabled("/tools/debugger/debuggerReplaceTrace", isMenuChecked("/tools/debugger/debuggerSwitch"));
        //setMenuEnabled("/tools/debugger/debuggerInjectLoader", isMenuChecked("/tools/debugger/debuggerSwitch"));
    }

    private void easyActionPerformed(ActionEvent evt) {
        if (isMenuChecked("/file/view/easy")) {
            if (!mainFrame.getPanel().showView(MainPanel.VIEW_EASY)) {
                setMenuChecked("/file/view/easy", false);
            } else {
                setGroupSelection("view", null);
            }
        } else {
            setGroupSelection("view", "/file/view/viewResources");
            mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadRecentSearches(ActionEvent evt) {
        clearMenu("/tools/" + (supportsMenuAction() ? "search" : "recentsearch"));
        SWF swf = Main.getMainFrame().getPanel().getCurrentSwf();

        if (swf != null) {
            List<Integer> indices = Main.searchResultsStorage.getIndicesForOpenable(swf);
            int j = 0;
            for (int i = 0; i < indices.size(); i++) {
                final int fi = indices.get(i);
                String searched = Main.searchResultsStorage.getSearchedValueAt(fi);
                ActionListener a = (ActionEvent e) -> {
                    SearchResultsDialog sr;
                    List<SearchListener<ScriptSearchResult>> listeners = new ArrayList<>();
                    listeners.add(Main.getMainFrame().getPanel().getABCPanel());
                    listeners.add(Main.getMainFrame().getPanel().getActionPanel());

                    if (swf.isAS3()) {
                        sr = new SearchResultsDialog<>(Main.getDefaultDialogsOwner(), searched, Main.searchResultsStorage.isIgnoreCaseAt(fi), Main.searchResultsStorage.isRegExpAt(fi), listeners);

                    } else {
                        sr = new SearchResultsDialog<>(Main.getDefaultDialogsOwner(), searched, Main.searchResultsStorage.isIgnoreCaseAt(fi), Main.searchResultsStorage.isRegExpAt(fi), listeners);
                    }
                    sr.setResults(Main.searchResultsStorage.getSearchResultsAt(mainFrame.getPanel().getAllOpenablesAndSwfs(), fi));
                    sr.setVisible(true);
                    Main.getMainFrame().getPanel().searchResultsDialogs.add(sr);
                };
                addMenuItem("/tools/" + (supportsMenuAction() ? "search" : "recentsearch") + "/" + i, j + "    " + searched, null, a, 0, null, true, null, false);
                j++;
            }
            if (indices.size() > 0) {
                addMenuItem("/tools/" + (supportsMenuAction() ? "search" : "recentsearch") + "/clear", translate("menu.recentSearches.clear"), null, this::clearRecentSearchesForCurrentSwfActionPerformed, 0, null, true, null, false);
            }
        }

        finishMenu("/tools/" + (supportsMenuAction() ? "search" : "recentsearch"));
    }

    protected void clearRecentSearchesForCurrentSwfActionPerformed(ActionEvent evt) {
        if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.recentSearches.clear"), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Main.searchResultsStorage.clearForOpenable(openable);
        }
    }

    protected void loadRecent(ActionEvent evt) {
        List<String> recentFiles = Configuration.getRecentFiles();
        clearMenu("/file/" + (supportsMenuAction() ? "open" : "recent"));
        clearMenu("_/open");

        for (int i = recentFiles.size() - 1; i >= 0; i--) {
            final String f = recentFiles.get(i);
            ActionListener a = (ActionEvent e) -> {
                if (Main.openFile(f, null) == OpenFileResult.NOT_FOUND) {
                    if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), translate("message.confirm.recentFileNotFound"), translate("message.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                        Configuration.removeRecentFile(f);
                    }
                }
            };
            addMenuItem("/file/" + (supportsMenuAction() ? "open" : "recent") + "/" + i, f, null, a, 0, null, true, null, false);
            addMenuItem("_/open/" + i, f, null, a, 0, null, true, null, false);
        }

        finishMenu("/file/" + (supportsMenuAction() ? "open" : "recent"));
        finishMenu("_/open");
    }

    public void dispose() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.removeKeyEventDispatcher(keyEventDispatcher);

        Configuration.autoDeobfuscate.removeListener(configListenerAutoDeobfuscate);
        Configuration.simplifyExpressions.removeListener(configListenerSimplifyExpressions);
        //Configuration.internalFlashViewer.removeListener(configListenerInternalFlashViewer);
        Configuration.parallelSpeedUp.removeListener(configListenerParallelSpeedUp);
        Configuration.decompile.removeListener(configListenerDecompile);
        //Configuration.cacheOnDisk.removeListener(configListenerCacheOnDisk);
        Configuration.gotoMainClassOnStartup.removeListener(configListenerGotoMainClassOnStartup);
        Configuration.autoRenameIdentifiers.removeListener(configListenerAutoRenameIdentifiers);
        Configuration.autoOpenLoadedSWFs.removeListener(configListenerAutoOpenLoadedSWFs);

        Main.stopRun();
    }

    public boolean runActionPerformed(ActionEvent evt) {
        Main.run((SWF) openable);
        return true;
    }

    public boolean debugActionPerformed(ActionEvent evt) {
        Main.runDebug((SWF) openable, false);
        return true;
    }

    public boolean debugPCodeActionPerformed(ActionEvent evt) {
        Main.runDebug((SWF) openable, true);
        return true;
    }

    public boolean stopActionPerformed(ActionEvent evt) {
        Main.stopRun();
        return true;
    }

    public boolean pauseActionPerformed(ActionEvent evt) {
        try {
            DebuggerCommands cmd = Main.getDebugHandler().getCommands();
            //TODO

        } catch (IOException ex) {
            Main.getDebugHandler().disconnect();
            //ignore
        }
        return true;
    }

    public boolean stepOverActionPerformed(ActionEvent evt) {

        try {

            DebuggerCommands cmd = Main.getDebugHandler().getCommands();
            mainFrame.getPanel().clearDebuggerColors();
            Main.startWork(AppStrings.translate("work.debugging") + "...", null);

            cmd.stepOver();
        } catch (IOException ex) {
            Main.getDebugHandler().disconnect();
            //ignore
        }
        return true;
    }

    public boolean stepIntoActionPerformed(ActionEvent evt) {
        try {
            DebuggerCommands cmd = Main.getDebugHandler().getCommands();
            mainFrame.getPanel().clearDebuggerColors();
            Main.startWork(AppStrings.translate("work.debugging") + "...", null);

            cmd.stepInto();
        } catch (IOException ex) {
            Main.getDebugHandler().disconnect();
            //ignore
        }

        return true;
    }

    public boolean stepOutActionPerformed(ActionEvent evt) {
        try {
            DebuggerCommands cmd = Main.getDebugHandler().getCommands();
            mainFrame.getPanel().clearDebuggerColors();
            Main.startWork(AppStrings.translate("work.debugging") + "...", null);
            cmd.stepOut();
        } catch (IOException ex) {
            Main.getDebugHandler().disconnect();
            //ignore
        }

        return true;
    }

    public boolean continueActionPerformed(ActionEvent evt) {
        try {
            DebuggerCommands cmd = Main.getDebugHandler().getCommands();
            mainFrame.getPanel().clearDebuggerColors();
            Main.startWork(AppStrings.translate("work.debugging") + "...", null);
            cmd.sendContinue();
        } catch (IOException ex) {
            Main.getDebugHandler().disconnect();
            //ignore
        }

        return true;
    }

    protected void abcExplorerActionPerformed(ActionEvent evt) {

        if (openable == null) {
            return;
        }

        mainFrame.getPanel().showAbcExplorer(openable, null);
    }

    public boolean stackActionPerformed(ActionEvent evt) {
        //TODO
        return true;
    }

    public boolean watchActionPerformed(ActionEvent evt) {
        //TODO
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (((JFrame) mainFrame).isActive() && e.getID() == KeyEvent.KEY_PRESSED) {

            HotKey ek = new HotKey(e);
            for (String path : menuHotkeys.keySet()) {
                HotKey mk = menuHotkeys.get(path);
                if (ek.equals(mk)) {
                    if (menuActions.containsKey(path)) {
                        menuActions.get(path).actionPerformed(null);
                        return true;
                    }
                }
            }

            //other nonmenu actions
            int code = e.getKeyCode();
            if (e.isControlDown() && e.isShiftDown()) { //CTRL+SHIFT
                switch (code) {
                    case KeyEvent.VK_F:
                        return searchInActionPerformed(null);
                    case KeyEvent.VK_T:
                        return searchInTextPerformed(null);
                    case KeyEvent.VK_D:
                        return clearLog(null);
                }
            } else if (e.isControlDown() && !e.isShiftDown()) { //CTRL
                switch (code) {
                    case KeyEvent.VK_UP:
                        return previousTag(null);
                    case KeyEvent.VK_DOWN:
                        return nextTag(null);
                }
            }
        }

        return false;
    }

    public abstract void hilightPath(String path);

    public abstract void setPathVisible(String path, boolean val);
}
