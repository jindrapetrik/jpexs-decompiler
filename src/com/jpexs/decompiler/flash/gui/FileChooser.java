/*
 *  Copyright (C) 2010-2026 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 * File chooser abstraction backed by either a native {@link FileDialog} or a
 * Swing {@link JFileChooser}.
 *
 * @author JPEXS
 */
public class FileChooser {

    private final JFileChooser swingChooser;

    private File currentDirectory;

    private File selectedFile;

    private File[] selectedFiles = new File[0];

    private FileFilter fileFilter;

    private boolean multiSelectionEnabled;

    private int fileSelectionMode = JFileChooser.FILES_ONLY;

    private String dialogTitle;

    public FileChooser() {
        this(null);
    }

    public FileChooser(String iconName) {
        swingChooser = new JFileChooser() {
            @Override
            protected javax.swing.JDialog createDialog(Component parent) {
                javax.swing.JDialog dialog = super.createDialog(parent);
                View.setWindowIcon(dialog, iconName);
                dialog.getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.FRAME);
                return dialog;
            }
        };
    }

    public void setCurrentDirectory(File directory) {
        currentDirectory = directory;
        swingChooser.setCurrentDirectory(directory);
    }

    public File getCurrentDirectory() {
        return currentDirectory == null ? swingChooser.getCurrentDirectory() : currentDirectory;
    }

    public void setSelectedFile(File file) {
        selectedFile = file;
        swingChooser.setSelectedFile(file);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public File[] getSelectedFiles() {
        return selectedFiles.clone();
    }

    public void setFileFilter(FileFilter filter) {
        fileFilter = filter;
        swingChooser.setFileFilter(filter);
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public void addChoosableFileFilter(FileFilter filter) {
        swingChooser.addChoosableFileFilter(filter);
    }

    public void setAcceptAllFileFilterUsed(boolean accept) {
        swingChooser.setAcceptAllFileFilterUsed(accept);
    }

    public void setMultiSelectionEnabled(boolean enabled) {
        multiSelectionEnabled = enabled;
        swingChooser.setMultiSelectionEnabled(enabled);
    }

    public void setFileHidingEnabled(boolean enabled) {
        swingChooser.setFileHidingEnabled(enabled);
    }

    public void setFileSelectionMode(int mode) {
        fileSelectionMode = mode;
        swingChooser.setFileSelectionMode(mode);
    }

    public void setDialogTitle(String title) {
        dialogTitle = title;
        swingChooser.setDialogTitle(title);
    }

    public void setAccessory(JComponent component) {
        swingChooser.setAccessory(component);
    }

    public Dimension getPreferredSize() {
        return swingChooser.getPreferredSize();
    }

    public void setPreferredSize(Dimension preferredSize) {
        swingChooser.setPreferredSize(preferredSize);
    }

    JFileChooser getSwingChooser() {
        return swingChooser;
    }

    public int showOpenDialog(Component parent) {
        return showDialog(parent, FileDialog.LOAD);
    }

    public int showSaveDialog(Component parent) {
        return showDialog(parent, FileDialog.SAVE);
    }

    private int showDialog(Component parent, int mode) {
        if (Configuration.useNativeFileDialogs.get() && supportsNativeDialog()) {
            return showNativeDialog(parent, mode);
        }

        int result = mode == FileDialog.LOAD
                ? swingChooser.showOpenDialog(parent)
                : swingChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = swingChooser.getSelectedFile();
            selectedFiles = swingChooser.getSelectedFiles();
            if (selectedFiles.length == 0 && selectedFile != null) {
                selectedFiles = new File[]{selectedFile};
            }
            fileFilter = swingChooser.getFileFilter();
            currentDirectory = swingChooser.getCurrentDirectory();
        }
        return result;
    }

    private boolean supportsNativeDialog() {
        return fileSelectionMode == JFileChooser.FILES_ONLY;
    }

    private int showNativeDialog(Component parent, int mode) {
        FileDialog dialog = createNativeDialog(parent, mode);
        File initialDirectory = currentDirectory;
        if (selectedFile != null) {
            File parentDirectory = selectedFile.getParentFile();
            if (parentDirectory != null) {
                initialDirectory = parentDirectory;
            }
            dialog.setFile(selectedFile.getName());
        }
        if (initialDirectory != null) {
            dialog.setDirectory(initialDirectory.getAbsolutePath());
        }
        if (fileFilter != null) {
            dialog.setFilenameFilter((directory, name) -> fileFilter.accept(new File(directory, name)));
        }
        dialog.setMultipleMode(multiSelectionEnabled);
        dialog.setVisible(true);

        File[] files = dialog.getFiles();
        dialog.dispose();
        if (files == null || files.length == 0) {
            selectedFile = null;
            selectedFiles = new File[0];
            return JFileChooser.CANCEL_OPTION;
        }

        selectedFiles = files.clone();
        selectedFile = selectedFiles[0];
        currentDirectory = selectedFile.getParentFile();
        return JFileChooser.APPROVE_OPTION;
    }

    private FileDialog createNativeDialog(Component parent, int mode) {
        Window window = parent instanceof Window
                ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        if (window instanceof Dialog) {
            return new FileDialog((Dialog) window, dialogTitle, mode);
        }
        if (window instanceof Frame) {
            return new FileDialog((Frame) window, dialogTitle, mode);
        }
        return new FileDialog((Frame) null, dialogTitle, mode);
    }
}
