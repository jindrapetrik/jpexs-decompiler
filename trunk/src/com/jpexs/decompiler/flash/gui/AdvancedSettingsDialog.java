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

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author JPEXS
 */
public class AdvancedSettingsDialog extends AppDialog implements ActionListener {

    /**
     * Creates new form AdvancedSettingsDialog
     */
    public AdvancedSettingsDialog() {
        initComponents();
        View.centerScreen(this);
        View.setWindowIcon(this);
        pack();

        configurationTable.setCellEditor(configurationTable.getDefaultEditor(null));

        Map<String, Field> fields = Configuration.getConfigurationFields();
        String[] keys = new String[fields.size()];
        keys = fields.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String name : keys) {
            Field field = fields.get(name);
            DefaultTableModel model = (DefaultTableModel) configurationTable.getModel();
            try {
                ConfigurationItem item = (ConfigurationItem) field.get(null);
                String description = Configuration.getDescription(field);
                if (description == null) {
                    description = "";
                }
                Object defaultValue = Configuration.getDefaultValue(field);
                if (defaultValue != null) {
                    description += " (" + translate("default") + ": " + defaultValue + ")";
                }
                model.addRow(new Object[]{name, item.get(), description});
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                // Reflection exceptions. This should never happen
                throw new Error(ex.getMessage());
            }
        }
    }

    private DefaultTableModel getModel() {
        return new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    translate("advancedSettings.columns.name"),
                    translate("advancedSettings.columns.value"),
                    translate("advancedSettings.columns.description")
                }
        ) {
            Class[] types = new Class[]{
                String.class, Object.class, String.class
            };
            boolean[] canEdit = new boolean[]{
                false, true, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }

    private void initComponents() {
        configurationTable = new EachRowRendererEditor();      
        okButton = new JButton();
        cancelButton = new JButton();
        resetButton = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(translate("advancedSettings.dialog.title"));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(800, 500));

        configurationTable.setModel(getModel());
              
        configurationTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        configurationTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        configurationTable.getColumnModel().getColumn(2).setPreferredWidth(600);
        
        okButton.setText(AppStrings.translate("button.ok"));
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");

        cancelButton.setText(AppStrings.translate("button.cancel"));
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("CANCEL");

        resetButton.setText(AppStrings.translate("button.reset"));
        resetButton.addActionListener(this);
        resetButton.setActionCommand("RESET");

        Container cnt=getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(new JScrollPane(configurationTable),BorderLayout.CENTER);
        
        
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        
        
        JPanel buttonsLeftPanel = new JPanel(new FlowLayout());
        buttonsLeftPanel.add(resetButton,BorderLayout.WEST);
        
        buttonsPanel.add(buttonsLeftPanel,BorderLayout.WEST);
        
        JPanel buttonsRightPanel = new JPanel(new FlowLayout());
        buttonsRightPanel.add(cancelButton); 
        buttonsRightPanel.add(okButton);                       
        buttonsPanel.add(buttonsRightPanel,BorderLayout.EAST);
        
        cnt.add(buttonsPanel,BorderLayout.SOUTH);        
        pack();
    }

    private void showRestartConfirmDialod() {
        if (View.showConfirmDialog(this, translate("advancedSettings.restartConfirmation"), AppStrings.translate("message.warning"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            View.execInEventDispatchLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AdvancedSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    SelectLanguageDialog.reloadUi();
                }
            });
            
        }
    }

    private JButton cancelButton;
    private JButton okButton;
    private JButton resetButton;
    private EachRowRendererEditor configurationTable;

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "OK":
                TableModel model = configurationTable.getModel();
                int count = model.getRowCount();
                boolean modified = false;
                for (int i = 0; i < count; i++) {
                    String name = (String) model.getValueAt(i, 0);
                    Object value = model.getValueAt(i, 1);
                    Map<String, Field> fields = Configuration.getConfigurationFields();
                    Field field = fields.get(name);
                    ConfigurationItem item = null;
                    try {
                        item = (ConfigurationItem) field.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        // Reflection exceptions. This should never happen
                        throw new Error(ex.getMessage());
                    }
                    if (item.get() != null && !item.get().equals(value)) {
                        item.set(value);
                        modified = true;
                    }
                }
                Configuration.saveConfig();
                setVisible(false);
                if (modified) {
                    showRestartConfirmDialod();
                }
                break;
            case "CANCEL":
                setVisible(false);
                break;
            case "RESET":
                
                Map<String, Field> fields = Configuration.getConfigurationFields();
                for (Entry<String, Field> entry : fields.entrySet()) {
                    String name = entry.getKey();
                    Field field = entry.getValue();
                    try {
                        ConfigurationItem item = (ConfigurationItem) field.get(null);
                        item.unset();
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        // Reflection exceptions. This should never happen
                        throw new Error(ex.getMessage());
                    }
                }
                Configuration.saveConfig();
                setVisible(false);
                showRestartConfirmDialod();
                break;
        }
    }
}
