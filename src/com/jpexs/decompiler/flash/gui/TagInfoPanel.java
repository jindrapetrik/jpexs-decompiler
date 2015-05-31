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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.TagInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author JPEXS
 */
public class TagInfoPanel extends JPanel {

    private final MainPanel mainPanel;

    private final JTable infoTable = new JTable();

    private TagInfo tagInfo = new TagInfo();

    public TagInfoPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        infoTable.setModel(new InfoTableModel("general"));
        setLayout(new BorderLayout());
        infoTable.setAutoCreateRowSorter(true);
        JLabel topLabel = new JLabel(AppStrings.translate("taginfo.header"), JLabel.CENTER);
        add(topLabel, BorderLayout.NORTH);
        add(new JScrollPane(infoTable), BorderLayout.CENTER);
    }

    public void setTagInfos(TagInfo tagInfo) {
        this.tagInfo = tagInfo;
        infoTable.setBackground(Color.WHITE);
        infoTable.setModel(new InfoTableModel("general"));
    }

    private class InfoTableModel implements TableModel {

        private final String categoryName;

        public InfoTableModel(String categoryName) {
            this.categoryName = categoryName;
        }

        @Override
        public int getRowCount() {
            List<TagInfo.TagInfoItem> category = tagInfo.getInfos().get(categoryName);
            if (category != null) {
                return category.size();
            }

            return 0;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return mainPanel.translate("tagInfo.header.name");
                case 1:
                    return mainPanel.translate("tagInfo.header.value");
            }

            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            List<TagInfo.TagInfoItem> category = tagInfo.getInfos().get(categoryName);
            if (category != null) {
                TagInfo.TagInfoItem item = category.get(rowIndex);

                switch (columnIndex) {
                    case 0:
                        String name = item.getName();
                        String key = "tagInfo." + name;
                        try {
                            name = mainPanel.translate(key);
                        } catch (MissingResourceException mes) {
                            if (Configuration.debugMode.get()) {
                                Logger.getLogger(TagInfoPanel.class.getName()).log(Level.WARNING, "Resource not found: {0}", key);
                            }
                        }

                        return name;
                    case 1:
                        Object value = item.getValue();
                        if (value instanceof Boolean) {
                            boolean boolValue = (boolean) value;
                            return boolValue ? AppStrings.translate("yes") : AppStrings.translate("no");
                        }

                        return "" + value;
                }
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }
    }
}
