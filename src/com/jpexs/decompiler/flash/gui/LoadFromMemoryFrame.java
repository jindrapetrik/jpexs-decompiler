/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.SWFSourceInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReReadableInputStream;
import com.jpexs.process.ProcessTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author JPEXS
 */
public class LoadFromMemoryFrame extends AppFrame {

    private MainFrame mainFrame;

    private List<com.jpexs.process.Process> processList;

    private List<SwfInMemory> foundIs;

    private List<com.jpexs.process.Process> selProcesses;

    private JList<com.jpexs.process.Process> list;

    private DefaultListModel<com.jpexs.process.Process> model;

    private DefaultTableModel resTableModel;

    private final JTable tableRes;

    private final JLabel stateLabel;

    private boolean processing = false;

    private final JProgressBar progress;

    private class SelectProcessWorker extends SwingWorker<List<SwfInMemory>, Object> {

        private final List<com.jpexs.process.Process> procs;

        public SelectProcessWorker(List<com.jpexs.process.Process> procs) {
            this.procs = procs;
        }

        @Override
        protected void process(List<Object> chunks) {
            for (Object s : chunks) {
                if (s instanceof com.jpexs.process.Process) {
                    stateLabel.setText(s.toString());
                }
                if (s instanceof SwfInMemory) {
                    SwfInMemory swf = (SwfInMemory) s;
                    addResultRow(swf);
                }
            }
        }

        @Override
        protected List<SwfInMemory> doInBackground() throws Exception {
            return new SearchInMemory(new SearchInMemoryListener() {
                @Override
                public void publish(Object... chunks) {
                    SelectProcessWorker.this.publish(chunks);
                }

                @Override
                public void setProgress(int progress) {
                    SelectProcessWorker.this.setProgress(progress);
                }
            }).search(procs);
        }
    }

    private void addResultRow(SwfInMemory swf) {
        if (swf != null) {
            com.jpexs.process.Process process = swf.process;
            resTableModel.addRow(new Object[]{swf.version, swf.fileSize, process.getPid(), process.getFileName(), swf.address});
        } else {
            String notFound = translate("notfound");
            resTableModel.addRow(new Object[]{notFound, 0, "", "", 0});
        }
    }

    private void refreshList() {
        model.clear();
        processList = ProcessTools.listProcesses();
        if (processList != null) {
            Collections.sort(processList);
            for (com.jpexs.process.Process p : processList) {
                model.addElement(p);
            }
        }
    }

    private void openSwf() {
        View.checkAccess();

        if (foundIs == null) {
            return;
        }
        int index = tableRes.getRowSorter().convertRowIndexToModel(tableRes.getSelectedRow());
        if (index > -1) {
            SwfInMemory swf = foundIs.get(index);
            ReReadableInputStream str = swf.is;
            try {
                str.seek(0);
            } catch (IOException ex) {
                Logger.getLogger(LoadFromMemoryFrame.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            str.mark(Integer.MAX_VALUE);
            SWFSourceInfo sourceInfo = new SWFSourceInfo(str, null, swf.process + " [" + (index + 1) + "]");
            Main.openFile(sourceInfo);
        }
    }

    private void selectProcess() {
        if (processing) {
            return;
        }

        selProcesses = list.getSelectedValuesList();
        if (!selProcesses.isEmpty()) {
            processing = true;
            tableRes.setEnabled(false);
            resTableModel.getDataVector().removeAllElements();
            resTableModel.fireTableDataChanged();
            foundIs = null;
            progress.setIndeterminate(true);
            progress.setString(translate("searching"));
            progress.setStringPainted(true);
            progress.setVisible(true);
            final SelectProcessWorker wrk = new SelectProcessWorker(selProcesses);
            wrk.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                switch (evt.getPropertyName()) {
                    case "progress":
                        progress.setIndeterminate(false);
                        progress.setStringPainted(false);
                        progress.setValue((Integer) evt.getNewValue());
                        break;
                    case "state":
                        if (((StateValue) evt.getNewValue()) == StateValue.DONE) {
                            try {
                                foundIs = wrk.get();
                            } catch (InterruptedException | ExecutionException ex) {
                                Logger.getLogger(LoadFromMemoryFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (foundIs == null) {
                                addResultRow(null);
                            }
                            tableRes.setEnabled(foundIs != null);
                            progress.setVisible(false);
                            processing = false;
                        }
                }
            });
            wrk.execute();
        }
    }

    public LoadFromMemoryFrame(final MainFrame mainFrame) {
        setSize(800, 600);
        //setAlwaysOnTop(true);
        setTitle(translate("dialog.title"));

        this.mainFrame = mainFrame;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!mainFrame.isVisible()) {
                    mainFrame.setVisible(true);
                }
            }
        });
        model = new DefaultListModel<>();

        resTableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return Integer.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Long.class;
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        resTableModel.addColumn(translate("column.version"));
        resTableModel.addColumn(translate("column.fileSize"));
        resTableModel.addColumn(translate("column.pid"));
        resTableModel.addColumn(translate("column.processName"));
        resTableModel.addColumn(translate("column.address"));
        tableRes = new JTable(resTableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(resTableModel);
        tableRes.setRowSorter(sorter);
        list = new JList<>(model);
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { //Enter pressed
                    selectProcess();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    selectProcess();
                }
            }
        });
        tableRes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    openSwf();
                }
            }
        });
        tableRes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { //Enter pressed
                    openSwf();
                }
            }
        });
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list,
                        value, index, isSelected, cellHasFocus);

                if (value instanceof com.jpexs.process.Process) {
                    if (((com.jpexs.process.Process) value).getIcon() != null) {
                        label.setIcon(new ImageIcon(((com.jpexs.process.Process) value).getIcon()));
                    }
                }
                if (!isSelected) {
                    label.setBackground(Color.white);
                }
                return label;
            }
        });
        refreshList();
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel leftButtonsPanel = new JPanel(new FlowLayout());
        JButton selectButton = new JButton(translate("button.select"));
        selectButton.addActionListener(this::selectProcessButtonActionPerformed);
        JButton refreshButton = new JButton(translate("button.refresh"));
        refreshButton.addActionListener(this::refreshProcessListButtonActionPerformed);
        leftButtonsPanel.add(selectButton);
        leftButtonsPanel.add(refreshButton);
        leftPanel.add(leftButtonsPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(tableRes), BorderLayout.CENTER);
        JPanel rightButtonsPanel = new JPanel(new FlowLayout());
        JButton openButton = new JButton(translate("button.open"));
        openButton.addActionListener(this::openSwfButtonActionPerformed);

        JButton saveButton = new JButton(translate("button.save"));
        saveButton.addActionListener(this::saveButtonActionPerformed);

        rightButtonsPanel.add(openButton);
        rightButtonsPanel.add(saveButton);
        rightPanel.add(rightButtonsPanel, BorderLayout.SOUTH);

        JPanel statePanel = new JPanel();
        statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.Y_AXIS));

        stateLabel = new JLabel(translate("noprocess"));
        statePanel.add(stateLabel);
        progress = new JProgressBar(0, 100);
        statePanel.add(progress);
        progress.setVisible(false);
        rightPanel.add(statePanel, BorderLayout.NORTH);

        cnt.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel), BorderLayout.CENTER);
        View.setWindowIcon(this);
        View.centerScreen(this);
        List<Image> images = new ArrayList<>();
        images.add(View.loadImage("loadmemory16"));
        images.add(View.loadImage("loadmemory32"));
        setIconImages(images);
    }

    private void selectProcessButtonActionPerformed(ActionEvent evt) {
        selectProcess();
    }

    private void openSwfButtonActionPerformed(ActionEvent evt) {
        openSwf();
    }

    private void refreshProcessListButtonActionPerformed(ActionEvent evt) {
        refreshList();
    }

    private void saveButtonActionPerformed(ActionEvent evt) {
        if (foundIs == null) {
            return;
        }

        int[] selected = tableRes.getSelectedRows();
        if (selected.length > 0) {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(Configuration.lastSaveDir.get()));
            if (selected.length > 1) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                fc.setSelectedFile(new File(Configuration.lastSaveDir.get(), "movie.swf"));
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return (f.getName().endsWith(".swf")) || (f.isDirectory());
                    }

                    @Override
                    public String getDescription() {
                        return AppStrings.translate("filter.swf");
                    }
                });
            }
            fc.setAcceptAllFileFilterUsed(false);
            JFrame f = new JFrame();
            View.setWindowIcon(f);
            if (fc.showSaveDialog(f) == JFileChooser.APPROVE_OPTION) {
                File file = Helper.fixDialogFile(fc.getSelectedFile());
                try {
                    if (selected.length == 1) {
                        SwfInMemory swf = foundIs.get(tableRes.getRowSorter().convertRowIndexToModel(selected[0]));
                        ReReadableInputStream bis = swf.is;
                        bis.seek(0);
                        Helper.saveStream(bis, file);
                    } else {
                        for (int sel : selected) {
                            SwfInMemory swf = foundIs.get(tableRes.getRowSorter().convertRowIndexToModel(sel));
                            ReReadableInputStream bis = swf.is;
                            bis.seek(0);
                            Helper.saveStream(bis, new File(file, "movie" + sel + ".swf"));
                        }
                    }
                    Configuration.lastSaveDir.set(file.getParentFile().getAbsolutePath());
                } catch (IOException ex) {
                    View.showMessageDialog(null, translate("error.file.write"));
                }
            }
        }
    }
}
