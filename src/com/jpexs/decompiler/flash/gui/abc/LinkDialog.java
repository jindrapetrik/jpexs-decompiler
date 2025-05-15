/*
 *  Copyright (C) 2023-2024 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * @author JPEXS
 */
public class LinkDialog extends JDialog {

    private MainPanel mainPanel;

    private JList<LinkItem> linkList;

    private SWF swf;

    private boolean overLinkButton = false;

    private List<ActionListener> saveListeners = new ArrayList<>();

    public void addSaveListener(ActionListener listener) {
        saveListeners.add(listener);
    }

    public void removeSaveListener(ActionListener listener) {
        saveListeners.remove(listener);
    }

    private void fireSave() {
        for (ActionListener listener : saveListeners) {
            listener.actionPerformed(new ActionEvent(this, 0, "SAVE"));
        }
    }

    public LinkDialog(MainPanel mainPanel, JToggleButton linkButton) {
        this.mainPanel = mainPanel;
        linkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                overLinkButton = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                overLinkButton = false;
            }
        });
        setUndecorated(true);
        setResizable(false);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        setSize(400, 300);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                save(swf, false);
                swf = null;
                if (!overLinkButton) {
                    linkButton.setSelected(false);
                }
                dispose();
            }
        });
        JPanel customLibraryPanel = new JPanel(new BorderLayout());
        linkList = new JList<>();
        linkList.setCellRenderer(new CustomLibraryListCellRenderer());
        linkList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int index = linkList.locationToIndex(e.getPoint());
                    LinkItem item = linkList.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    linkList.repaint(linkList.getCellBounds(index, index));
                }
            }
        });
        customLibraryPanel.add(linkList, BorderLayout.CENTER);

        customLibraryPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        setContentPane(customLibraryPanel);
    }

    public void load(SWF swf) {
        this.swf = swf;
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
        List<SWF> selectedSWFs = new ArrayList<>();

        if (conf != null) {
            List<String> preselectedNames = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_ABC_DEPENDENCIES);
            if (!preselectedNames.isEmpty()) {
                selectedSWFs = Main.namesToSwfs(preselectedNames);
            }
        }

        populateSWFs(swf, selectedSWFs);
    }

    public void show(SWF swf) {
        load(swf);
        setVisible(true);
    }

    public void save(SWF swf, boolean force) {
        Map<String, SWF> map = getSelectedSwfs();
        SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf.getShortPathTitle());
        List<String> oldValue = conf.getCustomDataAsList(CustomConfigurationKeys.KEY_ABC_DEPENDENCIES);
        List<String> newValue = new ArrayList<>(map.keySet());
        conf.setCustomData(CustomConfigurationKeys.KEY_ABC_DEPENDENCIES, newValue);
        List<SWF> swfs = new ArrayList<>(map.values());
        if (!Objects.equals(oldValue, newValue) || force) {
            swf.setAbcIndexDependencies(swfs);
            fireSave();
        }
    }

    private Map<String, SWF> getSelectedSwfs() {
        ListModel<LinkItem> model = linkList.getModel();
        Map<String, SWF> ret = new LinkedHashMap<>();
        for (int i = 0; i < model.getSize(); i++) {
            LinkItem item = model.getElementAt(i);
            if (!item.isSelected()) {
                continue;
            }
            ret.put(item.getName(), item.getSwf());
        }
        return ret;
    }

    private void populateSWFs(SWF ignoreSWF, List<SWF> selectedSWFs) {
        Map<String, SWF> swfs = new LinkedHashMap<>();
        Main.populateAllSWFs(swfs);
        DefaultListModel<LinkItem> listModel = new DefaultListModel<>();

        for (String key : swfs.keySet()) {
            SWF swf = swfs.get(key);
            if (swf == ignoreSWF) {
                continue;
            }
            boolean selected = false;
            for (SWF s : selectedSWFs) {
                if (s == swf) {
                    selected = true;
                }
            }
            listModel.addElement(new LinkItem(key, swf, selected));
        }
        linkList.setModel(listModel);
        pack();
    }

    class LinkItem {

        private String name;
        private SWF swf;
        private boolean selected;

        public LinkItem(String name, SWF swf, boolean selected) {
            this.name = name;
            this.swf = swf;
            this.selected = selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getName() {
            return name;
        }

        public SWF getSwf() {
            return swf;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class CustomLibraryListCellRenderer extends JCheckBox implements ListCellRenderer<LinkItem> {

        @Override
        public Component getListCellRendererComponent(JList<? extends LinkItem> list, LinkItem value, int index, boolean isSelected, boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }
}
