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
import com.jpexs.decompiler.flash.configuration.ConfigurationCategory;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author JPEXS
 */
public class AdvancedSettingsDialog extends AppDialog implements ActionListener {

    private Map<String, Component> componentsMap = new HashMap<>();

    /**
     * Creates new form AdvancedSettingsDialog
     */
    public AdvancedSettingsDialog() {
        initComponents();
        View.centerScreen(this);
        View.setWindowIcon(this);

        //configurationTable.setCellEditor(configurationTable.getDefaultEditor(null));
        pack();
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
        okButton = new JButton();
        cancelButton = new JButton();
        resetButton = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(translate("advancedSettings.dialog.title"));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(800, 500));

        okButton.setText(AppStrings.translate("button.ok"));
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");

        cancelButton.setText(AppStrings.translate("button.cancel"));
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("CANCEL");

        resetButton.setText(AppStrings.translate("button.reset"));
        resetButton.addActionListener(this);
        resetButton.setActionCommand("RESET");

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        //cnt.add(new JScrollPane(configurationTable),BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new BorderLayout());

        JPanel buttonsLeftPanel = new JPanel(new FlowLayout());
        buttonsLeftPanel.add(resetButton, BorderLayout.WEST);

        buttonsPanel.add(buttonsLeftPanel, BorderLayout.WEST);

        JPanel buttonsRightPanel = new JPanel(new FlowLayout());
        buttonsRightPanel.add(cancelButton);
        buttonsRightPanel.add(okButton);
        buttonsPanel.add(buttonsRightPanel, BorderLayout.EAST);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        Map<String, Field> fields = Configuration.getConfigurationFields();
        String[] keys = new String[fields.size()];
        keys = fields.keySet().toArray(keys);
        Arrays.sort(keys);

        Map<String, Map<String, Field>> categorized = new HashMap<>();

        for (String name : keys) {
            Field field = fields.get(name);
            ConfigurationCategory cat = field.getAnnotation(ConfigurationCategory.class);
            String scat = cat == null ? "other" : cat.value();
            if (!categorized.containsKey(scat)) {
                categorized.put(scat, new HashMap<String, Field>());
            }
            categorized.get(scat).put(name, field);
        }

        JTabbedPane tabPane = new JTabbedPane();
        Map<String, Component> tabs = new HashMap<>();
        for (String cat : categorized.keySet()) {
            JPanel configPanel = new JPanel(new SpringLayout());
            for (String name : categorized.get(cat).keySet()) {
                Field field = categorized.get(cat).get(name);

                String locName = translate("config.name." + name);

                try {

                    ConfigurationItem item = (ConfigurationItem) field.get(null);

                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class itemType = (Class<?>) listType.getActualTypeArguments()[0];
                    /*String description = Configuration.getDescription(field);
                     if (description == null) {
                     description = "";
                     }*/
                    String description = translate("config.description." + name);

                    Object defaultValue = Configuration.getDefaultValue(field);
                    if (defaultValue != null) {
                        description += " (" + translate("default") + ": " + defaultValue + ")";
                    }
                    //model.addRow(new Object[]{locName, item.get(), description});

                    JLabel l = new JLabel(locName, JLabel.TRAILING);
                    l.setToolTipText(description);
                    configPanel.add(l);
                    Component c = null;
                    if ((itemType == String.class) || (itemType == Integer.class) || (itemType == Long.class) || (itemType == Double.class) || (itemType == Float.class) || (itemType == Calendar.class)) {
                        JTextField tf = new JTextField();
                        Object val = item.get();
                        if (val == null) {
                            val = "";
                        }
                        if (itemType == Calendar.class) {

                            tf.setText(new SimpleDateFormat().format(((Calendar) item.get()).getTime()));
                        } else {
                            tf.setText(val.toString());
                        }
                        tf.setToolTipText(description);
                        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, tf.getPreferredSize().height));
                        c = tf;
                    }
                    if (itemType == Boolean.class) {
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected((Boolean) item.get());
                        cb.setToolTipText(description);
                        c = cb;
                    }
                    componentsMap.put(name, c);
                    l.setLabelFor(c);
                    configPanel.add(c);

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    // Reflection exceptions. This should never happen
                    throw new Error(ex.getMessage());
                }
            }
            SpringUtilities.makeCompactGrid(configPanel,
                    categorized.get(cat).size(), 2, //rows, cols
                    6, 6, //initX, initY
                    6, 6);       //xPad, yPad
            tabs.put(cat, new JScrollPane(configPanel));
        }

        String catOrder[] = new String[]{"ui", "display", "decompilation", "script", "format", "export", "limit", "update", "debug", "other"};

        for (String cat : catOrder) {
            if (!tabs.containsKey(cat)) {
                continue;
            }
            tabPane.add(translate("config.group.name." + cat), tabs.get(cat));
            tabPane.setToolTipTextAt(tabPane.getTabCount() - 1, translate("config.group.description." + cat));
        }

        cnt.add(tabPane, BorderLayout.CENTER);
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
    //private EachRowRendererEditor configurationTable;

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "OK":
                boolean modified = false;
                Map<String, Field> fields = Configuration.getConfigurationFields();
                Map<String, Object> values = new HashMap<>();
                for (String name : fields.keySet()) {
                    Component c = componentsMap.get(name);
                    Object value = null;

                    ParameterizedType listType = (ParameterizedType) fields.get(name).getGenericType();
                    Class itemType = (Class<?>) listType.getActualTypeArguments()[0];
                    if (itemType == String.class) {
                        value = ((JTextField) c).getText();
                    }
                    if (itemType == Boolean.class) {
                        value = ((JCheckBox) c).isSelected();
                    }

                    if (itemType == Calendar.class) {
                        Calendar cal = Calendar.getInstance();
                        try {
                            cal.setTime(new SimpleDateFormat().parse(((JTextField) c).getText()));
                        } catch (ParseException ex) {
                            c.requestFocusInWindow();
                            return;
                        }
                        value = cal;
                    }

                    try {
                        if (itemType == Integer.class) {
                            value = Integer.parseInt(((JTextField) c).getText());
                        }
                        if (itemType == Long.class) {
                            value = Long.parseLong(((JTextField) c).getText());
                        }
                        if (itemType == Double.class) {
                            value = Double.parseDouble(((JTextField) c).getText());
                        }
                        if (itemType == Float.class) {
                            value = Float.parseFloat(((JTextField) c).getText());
                        }
                    } catch (NumberFormatException nfe) {
                        if (!((JTextField) c).getText().isEmpty()) {
                            c.requestFocusInWindow();
                            return;
                        }//else null
                    }
                    values.put(name, value);
                }

                for (String name : fields.keySet()) {
                    Component c = componentsMap.get(name);
                    Object value = values.get(name);

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

                Map<String, Field> rfields = Configuration.getConfigurationFields();
                for (Entry<String, Field> entry : rfields.entrySet()) {
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
