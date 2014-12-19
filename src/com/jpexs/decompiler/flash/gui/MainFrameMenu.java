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

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public abstract class MainFrameMenu {

    private final MainFrame mainFrame;
    private SWF swf;
    
    public abstract boolean isInternalFlashViewerSelected();

    public MainFrameMenu(MainFrame mainFrame) {
        registerHotKeys();
        this.mainFrame = mainFrame;
    }
    
    protected String translate(String key) {
        return mainFrame.translate(key);
    }

    protected void open() {
        Main.openFileDialog();
    }
    
    protected void save() {
        SWF swf = mainFrame.getPanel().getCurrentSwf();
        if (swf != null) {
            boolean saved = false;
            if (swf.binaryData != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    swf.saveTo(baos);
                    swf.binaryData.binaryData = new ByteArrayRange(baos.toByteArray());
                    swf.binaryData.setModified(true);
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameClassicMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                }
            } else if (swf.file == null) {
                saved = saveAs(swf, SaveFileMode.SAVEAS);
            } else {
                try {
                    Main.saveFile(swf, swf.file);
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameClassicMenu.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
            if (saved) {
                swf.clearModified();
            }
        }
    }
    
    protected boolean saveAs(SWF swf, SaveFileMode mode) {
        if (Main.saveFileDialog(swf, mode)) {
            mainFrame.setTitle(ApplicationInfo.applicationVerName + (Configuration.displayFileName.get() ? " - " + swf.getFileTitle() : ""));
            updateComponents(mainFrame.getPanel().getCurrentSwf());
            return true;
        }
        return false;
    }

    protected void search(boolean searhInText) {
        mainFrame.getPanel().searchInActionScriptOrText(searhInText);
    }
    
    public void updateComponents(SWF swf) {
        this.swf = swf;
    }
    
    private void registerHotKeys() {
        
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (((JFrame) mainFrame).isActive()) {
                    if (e.isControlDown() && e.isShiftDown()) {
                        int code = e.getKeyCode();
                        switch (code) {
                            case KeyEvent.VK_O:
                                open();
                                return true;
                            case KeyEvent.VK_S:
                                if (swf != null) {
                                    save();
                                    return true;
                                }
                                break;
                            case KeyEvent.VK_F:
                                if (swf != null) {
                                    search(false);
                                    return true;
                                }
                                break;
                            case KeyEvent.VK_T:
                                if (swf != null) {
                                    search(true);
                                    return true;
                                }
                                break;
                        }
                    }
                }

                return false;
            }
        });
    }
}
