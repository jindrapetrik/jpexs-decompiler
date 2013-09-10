package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LimitedInputStream;
import com.jpexs.helpers.PosMarkedInputStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.ReReadableInputStream;
import com.jpexs.process.ProcessTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author petrik
 */
public class LoadFromMemoryFrame extends AppFrame implements ActionListener {

    private List<com.jpexs.process.Process> processlist;
    private List<ReReadableInputStream> foundIs;
    private com.jpexs.process.Process selProcess;
    private JList<com.jpexs.process.Process> list;
    private DefaultListModel<com.jpexs.process.Process> model;
    private DefaultListModel<String> resModel;
    private final JList listRes;
    private JLabel stateLabel;
    private boolean processing = false;
    private JProgressBar progress;

    private class SelectProcessWorker extends SwingWorker<List<ReReadableInputStream>, Object> {

        private com.jpexs.process.Process proc;

        public SelectProcessWorker(com.jpexs.process.Process proc) {
            this.proc = proc;
        }

        @Override
        protected void process(List<Object> chunks) {
            for (Object s : chunks) {
                if (s instanceof String) {
                    resModel.addElement((String) s);
                }
            }
        }

        @Override
        protected List<ReReadableInputStream> doInBackground() throws Exception {
            Map<Long, InputStream> ret = new HashMap<>();
            ret = proc.search(new ProgressListener() {
                @Override
                public void progress(int p) {
                    setProgress(p);
                }
            }, "CWS".getBytes(), "FWS".getBytes(), "ZWS".getBytes());
            List<ReReadableInputStream> swfStreams = new ArrayList<>();
            int pos = 0;
            for (Long addr : ret.keySet()) {
                setProgress(pos * 100 / ret.size());
                pos++;
                try {
                    PosMarkedInputStream pmi=new PosMarkedInputStream(ret.get(addr));
                    ReReadableInputStream is = new ReReadableInputStream(pmi);
                    SWF swf = new SWF(is, null, false, true);
                    long limit = pmi.getPos();
                    is.setPos(0);                    
                    is = new ReReadableInputStream(new LimitedInputStream(is, limit));
                    if (swf.fileSize > 0 && swf.version > 0 && !swf.tags.isEmpty() && swf.version < 25/*Needs to be fixed when SWF versions reaches this value*/) {
                        String p=translate("swfitem").replace("%version%", "" + swf.version).replace("%size%", "" + swf.fileSize);
                        publish(p);
                        swfStreams.add(is);
                    }                    

                } catch (OutOfMemoryError ome) {
                    System.gc();
                } catch (Exception | Error ex) {
                }

            }
            setProgress(100);
            if (swfStreams.isEmpty()) {
                return null;
            }
            return swfStreams;
        }
    }

    private void refreshList() {
        model.clear();
        processlist = ProcessTools.listProcesses();
        Collections.sort(processlist);
        for (com.jpexs.process.Process p : processlist) {
            model.addElement(p);
        }
    }

    private void openSWF() {
        if (foundIs == null) {
            return;
        }
        int index = listRes.getSelectedIndex();
        if (index > -1) {
            ReReadableInputStream str = foundIs.get(index);
            try {
                str.setPos(0);
            } catch (IOException ex) {
                Logger.getLogger(LoadFromMemoryFrame.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            str.mark(Integer.MAX_VALUE);
            Main.openFile("" + selProcess + " [" + (index + 1) + "]", str);
        }
    }

    private void selectProcess() {
        if (processing) {
            return;
        }
        selProcess = (com.jpexs.process.Process) list.getSelectedValue();
        if (selProcess != null) {
            processing = true;
            stateLabel.setText(selProcess.toString());
            resModel.clear();
            foundIs = null;
            progress.setIndeterminate(true);
            progress.setString(translate("searching"));
            progress.setStringPainted(true);
            progress.setVisible(true);
            final SelectProcessWorker wrk = new SelectProcessWorker(selProcess);
            wrk.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
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
                                } catch (Exception ex) {
                                    Logger.getLogger(LoadFromMemoryFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if (foundIs == null) {
                                    resModel.addElement(translate("notfound"));
                                }
                                listRes.setEnabled(foundIs != null);
                                progress.setVisible(false);
                                processing = false;
                            }
                    }

                }
            });
            wrk.execute();
        }
    }

    @SuppressWarnings("unchecked")
    public LoadFromMemoryFrame() {
        setSize(800, 600);
        //setAlwaysOnTop(true);
        setTitle(translate("dialog.title"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!Main.mainFrame.isVisible()) {
                    Main.mainFrame.setVisible(true);
                }
            }
        });
        model = new DefaultListModel<>();

        resModel = new DefaultListModel<>();
        listRes = new JList(resModel);
        list = new JList(model);
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) { //Enter pressed
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
        listRes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    openSWF();
                }
            }
        });
        listRes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) { //Enter pressed
                    openSWF();
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
        selectButton.setActionCommand("SELECTPROCESS");
        selectButton.addActionListener(this);
        JButton refreshButton = new JButton(translate("button.refresh"));
        refreshButton.setActionCommand("REFRESHPROCESSLIST");
        refreshButton.addActionListener(this);
        leftButtonsPanel.add(selectButton);
        leftButtonsPanel.add(refreshButton);
        leftPanel.add(leftButtonsPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(listRes), BorderLayout.CENTER);
        JPanel rightButtonsPanel = new JPanel(new FlowLayout());
        JButton openButton = new JButton(translate("button.open"));
        openButton.setActionCommand("OPENSWF");
        openButton.addActionListener(this);
        
        JButton saveButton = new JButton(translate("button.save"));
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(this);
        
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "SELECTPROCESS":
                selectProcess();
                break;
            case "OPENSWF":
                openSWF();
                break;
            case "REFRESHPROCESSLIST":
                refreshList();
                break;
            case "SAVE":
                if (foundIs == null) {
                    return;
                }
                int[] selected = listRes.getSelectedIndices();
                if (selected.length>0) {
                    JFileChooser fc = new JFileChooser();
                    fc.setCurrentDirectory(new File(Configuration.getConfig("lastSaveDir", ".")));
                    if (selected.length > 1) {
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    } else {
                        fc.setSelectedFile(new File(Configuration.getConfig("lastSaveDir", "."), "movie.swf"));
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
                    int returnVal = fc.showSaveDialog(f);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = Helper.fixDialogFile(fc.getSelectedFile());
                        try {
                            if (selected.length == 1) {
                                ReReadableInputStream bis=foundIs.get(selected[0]);
                                bis.setPos(0);
                                Helper.saveStream(bis, file);
                            } else {
                                for (int sel : selected) {
                                    ReReadableInputStream bis=foundIs.get(sel);
                                    bis.setPos(0);
                                    Helper.saveStream(bis, new File(file, "movie"+sel+".swf"));
                                }
                            }
                            Configuration.setConfig("lastSaveDir", file.getParentFile().getAbsolutePath());
                        } catch (IOException ex) {
                            View.showMessageDialog(null, translate("error.file.write"));
                        }
                    }
                }
                break;
        }
    }
}
