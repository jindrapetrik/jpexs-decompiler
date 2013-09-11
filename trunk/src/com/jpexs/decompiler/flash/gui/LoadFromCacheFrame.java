/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.browsers.cache.CacheEntry;
import com.jpexs.browsers.cache.CacheImplementation;
import com.jpexs.browsers.cache.CacheReader;
import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReReadableInputStream;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author JPEXS
 */
public class LoadFromCacheFrame extends AppFrame implements ActionListener {

    private JList<CacheEntry> list;
    private JTextField searchField;
    private List<CacheImplementation> caches;
    private List<CacheEntry> entries;

    public LoadFromCacheFrame() {
        setSize(900, 600);
        View.setWindowIcon(this);
        View.centerScreen(this);
        setTitle(translate("dialog.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        list = new JList<>();
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    openSWF();
                }
            }
        });
        searchField = new JTextField();
        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                filter();
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        cnt.setLayout(new BorderLayout());
        cnt.add(searchField, BorderLayout.NORTH);
        cnt.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        JButton openButton = new JButton(translate("button.open"));
        openButton.setActionCommand("OPEN");
        openButton.addActionListener(this);
        buttonsPanel.add(openButton);

        JButton saveButton = new JButton(translate("button.save"));
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(this);
        buttonsPanel.add(saveButton);

        JButton refreshButton = new JButton(translate("button.refresh"));
        refreshButton.setActionCommand("REFRESH");
        refreshButton.addActionListener(this);
        buttonsPanel.add(refreshButton);

        JPanel browsersPanel = new JPanel(new FlowLayout());
        browsersPanel.add(new JLabel(translate("supported.browsers")));
        JLabel chromeLabel = new JLabel("Google Chrome", View.getIcon("chrome16"), JLabel.CENTER);
        JLabel firefoxLabel = new JLabel("Mozilla Firefox*", View.getIcon("firefox16"), JLabel.CENTER);
        browsersPanel.add(chromeLabel);
        browsersPanel.add(firefoxLabel);
        buttonsPanel.setAlignmentX(0.5f);

        bottomPanel.add(buttonsPanel);
        browsersPanel.setAlignmentX(0.5f);
        bottomPanel.add(browsersPanel);
        JLabel infoLabel = new JLabel(translate("info.closed"));
        infoLabel.setAlignmentX(0.5f);
        bottomPanel.add(infoLabel);
        cnt.add(bottomPanel, BorderLayout.SOUTH);
        refresh();
    }

    private void refresh() {
        if (caches == null) {
            caches = new ArrayList<>();
            for (String b : CacheReader.availableBrowsers()) {
                caches.add(CacheReader.getBrowserCache(b));
            }
        } else {
            for (CacheImplementation c : caches) {
                c.refresh();
            }
        }
        entries = new ArrayList<>();
        for (CacheImplementation c : caches) {
            List<CacheEntry> list=c.getEntries();
            if(list!=null){
                for (CacheEntry en : c.getEntries()) {
                    String contentType = en.getHeader("Content-Type");
                    if ("application/x-shockwave-flash".equals(contentType)) {
                        entries.add(en);
                    }
                }
            }
        }
        filter();
    }

    @SuppressWarnings("unchecked")
    private void filter() {
        String search = searchField.getText();
        List<CacheEntry> filtered = new ArrayList<>();
        for (CacheEntry en : entries) {
            if (search.equals("") || en.getRequestURL().contains(search)) {
                filtered.add(en);
            }
        }
        list.setListData(new Vector(filtered));
    }

    private static String entryToFileName(CacheEntry en) {
        String ret = en.getRequestURL();
        //Strip parameters
        if (ret.contains("?")) {
            ret = ret.substring(0, ret.indexOf("?"));
        }
        //Strip path
        if (ret.contains("/")) {
            ret = ret.substring(ret.lastIndexOf("/") + 1);
        }
        return ret;
    }

    private void openSWF() {
        CacheEntry en = list.getSelectedValue();
        if (en != null) {
            ReReadableInputStream str = new ReReadableInputStream(en.getResponseDataStream());
            Main.openFile(entryToFileName(en), str);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "REFRESH":
                refresh();
                break;
            case "OPEN":
                openSWF();
                break;
            case "SAVE":
                List<CacheEntry> selected = list.getSelectedValuesList();
                if (!selected.isEmpty()) {
                    JFileChooser fc = new JFileChooser();
                    fc.setCurrentDirectory(new File(Configuration.getConfig("lastSaveDir", ".")));
                    if (selected.size() > 1) {
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    } else {
                        fc.setSelectedFile(new File(Configuration.getConfig("lastSaveDir", "."), entryToFileName(selected.get(0))));
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
                            if (selected.size() == 1) {
                                Helper.saveStream(selected.get(0).getResponseDataStream(), file);
                            } else {
                                for (CacheEntry sel : selected) {
                                    Helper.saveStream(sel.getResponseDataStream(), new File(file, entryToFileName(sel)));
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