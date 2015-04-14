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
import com.jpexs.decompiler.flash.configuration.ConfigurationCategory;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;
import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 *
 * @author JPEXS
 */
public class AdvancedSettingsDialog extends AppDialog implements ActionListener {

    private final Map<String, Component> componentsMap = new HashMap<>();

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
    
    private static class SkinSelect {
        private String name;
        private String className;

        public SkinSelect(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return name;
        }
                
        
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
                categorized.put(scat, new HashMap<>());
            }
            categorized.get(scat).put(name, field);
        }

        JTabbedPane tabPane = new JTabbedPane();
        Map<String, Component> tabs = new HashMap<>();
        
         JComboBox<SkinSelect> skinComboBox=new JComboBox<>();
                      skinComboBox.setRenderer(new SubstanceDefaultListCellRenderer(){

                          @Override
                          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                              SubstanceDefaultListCellRenderer cmp= (SubstanceDefaultListCellRenderer)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                              final SkinSelect ss=(SkinSelect)value;
                              cmp.setIcon(new Icon() {

                                  @Override
                                  public void paintIcon(Component c, Graphics g, int x, int y) {
                                      Graphics2D g2 = (Graphics2D) g;
                                      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                      try {
                                          Class<?> act = Class.forName(ss.getClassName());                                          
                                          SubstanceSkin skin = (SubstanceSkin)act.newInstance();
                                          Color fill = skin.getColorScheme(DecorationAreaType.GENERAL,ColorSchemeAssociationKind.FILL,ComponentState.ENABLED).getBackgroundFillColor();
                                          Color hilight=skin.getColorScheme(DecorationAreaType.GENERAL,ColorSchemeAssociationKind.FILL,ComponentState.ROLLOVER_SELECTED).getBackgroundFillColor();
                                          Color border = skin.getColorScheme(DecorationAreaType.GENERAL,ColorSchemeAssociationKind.BORDER,ComponentState.ENABLED).getDarkColor();
                                          g2.setColor(fill);
                                          g2.fillOval(0, 0, 16, 16);
                                          g2.setColor(hilight);
                                          g2.fillArc(0, 0, 16, 16, -45, 90);                                          
                                          g2.setColor(border);
                                          g2.drawOval(0, 0, 16, 16);
                                          
                                      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                                          //no icon
                                      }
                                      
                                  }

                                  @Override
                                  public int getIconWidth() {
                                      return 16;
                                  }

                                  @Override
                                  public int getIconHeight() {
                                      return 16;
                                  }
                              });
                              return cmp;
                          }
                      
                      });
                      skinComboBox.addItem(new SkinSelect(OceanicSkin.NAME, OceanicSkin.class.getName()));
                        Map<String,SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();
                        for(String skinKey:skins.keySet()){
                           SkinInfo skin=skins.get(skinKey);
                           skinComboBox.addItem(new SkinSelect(skin.getDisplayName(), skin.getClassName()));
                           if(skin.getClassName().equals(Configuration.guiSkin.get())){
                               skinComboBox.setSelectedIndex(skinComboBox.getItemCount()-1);
                           }
                        }
        
        for (String cat : categorized.keySet()) {
            JPanel configPanel = new JPanel(new SpringLayout());
            for (String name : categorized.get(cat).keySet()) {
                Field field = categorized.get(cat).get(name);

                String locName = translate("config.name." + name);

                try {

                    ConfigurationItem item = (ConfigurationItem) field.get(null);

                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class itemType = (Class<?>) listType.getActualTypeArguments()[0];
                    
                    String description = translate("config.description." + name);

                    Object defaultValue = Configuration.getDefaultValue(field);
                    if(name.equals("gui.skin")){
                        Class c;
                        try {
                            c = Class.forName((String)defaultValue);
                            defaultValue = c.getField("NAME").get(c);
                        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException ex) {
                            Logger.getLogger(AdvancedSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                    if (defaultValue != null) {
                        description += " (" + translate("default") + ": " + defaultValue + ")";
                    }
                    
                    JLabel l = new JLabel(locName, JLabel.TRAILING);
                    l.setToolTipText(description);
                    configPanel.add(l);
                    Component c = null;
                    if(name.equals("gui.skin")){                         
                        skinComboBox.setToolTipText(description);
                        skinComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, skinComboBox.getPreferredSize().height));
                        c = skinComboBox;
                    } else if ((itemType == String.class) || (itemType == Integer.class) || (itemType == Long.class) || (itemType == Double.class) || (itemType == Float.class) || (itemType == Calendar.class)) {                        
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
                    } else if (itemType == Boolean.class) {
                        JCheckBox cb = new JCheckBox();
                        cb.setSelected((Boolean) item.get());
                        cb.setToolTipText(description);
                        c = cb;
                    } else if (itemType.isEnum()) {
                        JComboBox<String> cb = new JComboBox<>();
                        @SuppressWarnings("unchecked")
                        EnumSet enumValues = EnumSet.allOf(itemType);
                        String stringValue = null;
                        for (Object enumValue : enumValues) {
                            String enumValueStr = enumValue.toString();
                            if (stringValue == null) {
                                stringValue = enumValueStr;
                            }
                            cb.addItem(enumValueStr);
                        }
                        if (item.get() != null) {
                            stringValue = item.get().toString();
                        }
                        cb.setToolTipText(description);
                        cb.setSelectedItem(stringValue);
                        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, cb.getPreferredSize().height));
                        c = cb;
                    } else {
                        throw new UnsupportedOperationException("Configuration ttem type '" + itemType.getName() + "' is not supported");
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

        String catOrder[] = new String[]{"ui", "display", "decompilation", "script", "format", "export", "import", "limit", "update", "debug", "other"};

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
                    if(name.equals("gui.skin")){
                        value = ((SkinSelect)((JComboBox<SkinSelect>)c).getSelectedItem()).className;
                    }
                    else if (itemType == String.class) {
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

                    if (itemType.isEnum()) {
                        String stringValue = (String) ((JComboBox<String>) c).getSelectedItem();
                        value = Enum.valueOf(itemType, stringValue);
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
                        } // else null
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
                    if (item.get() == null || !item.get().equals(value)) {
                        if (item.hasValue() || value != null) {
                            item.set(value);
                            modified = true;
                        }
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
