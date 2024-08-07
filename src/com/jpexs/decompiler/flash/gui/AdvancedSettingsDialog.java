/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.configuration.ConfigurationDirectory;
import com.jpexs.decompiler.flash.configuration.ConfigurationFile;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.gui.helpers.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;
import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 * @author JPEXS
 */
public class AdvancedSettingsDialog extends AppDialog {

    private final Map<String, JLabel> labelsMap = new HashMap<>();
    private final Map<String, Component> componentsMap = new HashMap<>();
    private final Map<String, String> titleToNameMap = new HashMap<>();
    private final Map<String, Integer> categoryCounts = new HashMap<>();
    private final JTextField searchTextField = new JTextField(30);

    private final Map<String, Component> allComponentsMap = new HashMap<>();

    private Map<String, Component> tabs = new HashMap<>();

    private JButton cancelButton;

    private JButton okButton;

    private JButton resetButton;

    private JTabbedPane tabPane = new JTabbedPane();

    private String hilightBackgroundColorHex;
    private String hilightForegroundColorHex;

    public AdvancedSettingsDialog(Window owner, String selectedCategory) {
        super(owner);
        initComponents(selectedCategory);
        View.centerScreen(this);
        View.setWindowIcon(this, "settings");

        //configurationTable.setCellEditor(configurationTable.getDefaultEditor(null));
        pack();

        Color c;
        c = UIManager.getColor("List.selectionBackground");
        hilightBackgroundColorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());

        c = UIManager.getColor("List.selectionForeground");
        hilightForegroundColorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private DefaultTableModel getModel() {
        return new DefaultTableModel(
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

        private final String name;

        private final String className;

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

    private void initTabPane(String filter, String selectedCategory) {
        tabPane.removeAll();
        JComboBox<SkinSelect> skinComboBox = new JComboBox<>();
        skinComboBox.setRenderer(new SubstanceDefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                SubstanceDefaultListCellRenderer cmp = (SubstanceDefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                final SkinSelect ss = (SkinSelect) value;
                cmp.setIcon(new Icon() {
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        try {
                            Class<?> act = Class.forName(ss.getClassName());
                            SubstanceSkin skin = (SubstanceSkin) act.getDeclaredConstructor().newInstance();
                            Color fill = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
                            Color hilight = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getBackgroundFillColor();
                            Color border = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED).getDarkColor();
                            g2.setColor(fill);
                            g2.fillOval(0, 0, 16, 16);
                            g2.setColor(hilight);
                            g2.fillArc(0, 0, 16, 16, -45, 90);
                            g2.setColor(border);
                            g2.drawOval(0, 0, 16, 16);

                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
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
        Map<String, SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();
        for (String skinKey : skins.keySet()) {
            SkinInfo skin = skins.get(skinKey);
            skinComboBox.addItem(new SkinSelect(skin.getDisplayName(), skin.getClassName()));
            if (skin.getClassName().equals(Configuration.guiSkin.get())) {
                skinComboBox.setSelectedIndex(skinComboBox.getItemCount() - 1);
            }
        }

        tabs = new LinkedHashMap<>();

        getCategories(hilightBackgroundColorHex, hilightForegroundColorHex, allComponentsMap, categoryCounts, filter, titleToNameMap, labelsMap, componentsMap, tabs, skinComboBox, getResourceBundle());

        String[] catOrder = new String[]{"ui", "display", "decompilation", "script", "format", "export", "import", "paths", "limit", "update", "debug", "other"};

        for (String cat : catOrder) {
            if (!tabs.containsKey(cat)) {
                continue;
            }

            int count = categoryCounts.get(cat);
            String countAdd = "";
            if (!filter.trim().equals("")) {
                countAdd = " (" + count + ")";
            }

            if (count > 0) {
                tabPane.add(translate("config.group.name." + cat) + countAdd, tabs.get(cat));
                tabPane.setToolTipTextAt(tabPane.getTabCount() - 1, translate("config.group.description." + cat));
            } else {
                if (cat.equals(selectedCategory)) {
                    selectedCategory = null;
                }
            }
        }
        if (selectedCategory != null && tabs.containsKey(selectedCategory)) {
            tabPane.setSelectedComponent(tabs.get(selectedCategory));
        }
    }

    private void initComponents(String selectedCategory) {
        okButton = new JButton();
        cancelButton = new JButton();
        resetButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(translate("advancedSettings.dialog.title"));
        setModal(true);
        setPreferredSize(new Dimension(800, 500));

        okButton.setText(AppStrings.translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);

        cancelButton.setText(AppStrings.translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        resetButton.setText(AppStrings.translate("button.reset"));
        resetButton.addActionListener(this::resetButtonActionPerformed);

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        //cnt.add(new FasterScrollPane(configurationTable),BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new BorderLayout());

        JPanel buttonsLeftPanel = new JPanel(new FlowLayout());
        buttonsLeftPanel.add(resetButton, BorderLayout.WEST);

        buttonsPanel.add(buttonsLeftPanel, BorderLayout.WEST);

        JPanel buttonsRightPanel = new JPanel(new FlowLayout());
        buttonsRightPanel.add(okButton);
        buttonsRightPanel.add(cancelButton);
        buttonsPanel.add(buttonsRightPanel, BorderLayout.EAST);

        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String val = searchTextField.getText();
                Component selComponent = tabPane.getSelectedComponent();
                String selCategory = null;
                for (String cat : tabs.keySet()) {
                    if (tabs.get(cat) == selComponent) {
                        selCategory = cat;
                        break;
                    }
                }

                initTabPane(val, selCategory);
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel(translate("advancedSettings.search")));
        searchPanel.add(searchTextField);

        buttonsPanel.add(searchPanel, BorderLayout.CENTER);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        initTabPane("", selectedCategory);
        cnt.add(tabPane, BorderLayout.CENTER);
        pack();
        searchTextField.requestFocusInWindow();
    }

    public static void getCategories(String hilightBackgroundColorHex, String hilightForegroundColorHex, Map<String, Component> allComponentsMap, Map<String, Integer> categoryCounts, String filter, Map<String, String> titleToNameMap, Map<String, JLabel> labelsMap, Map<String, Component> componentsMap, Map<String, Component> tabs, JComboBox<?> skinComboBox, ResourceBundle resourceBundle) {
        Map<String, Map<String, Field>> categorized = new HashMap<>();

        Map<String, Field> fields = Configuration.getConfigurationFields();
        String[] keys = new String[fields.size()];
        keys = fields.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String name : keys) {
            Field field = fields.get(name);
            ConfigurationCategory cat = field.getAnnotation(ConfigurationCategory.class);
            String scat = (cat == null || cat.value().isEmpty()) ? "other" : cat.value();
            if (!categorized.containsKey(scat)) {
                categorized.put(scat, new HashMap<>());
            }

            categorized.get(scat).put(name, field);
        }

        for (String cat : categorized.keySet()) {
            JPanel configPanel = new JPanel(new SpringLayout());
            int itemCount = 0;
            List<String> names = new ArrayList<>(categorized.get(cat).keySet());

            final Map<String, String> locNames = new HashMap<>();
            for (String name : names) {
                String locName;

                if (resourceBundle.containsKey("config.name." + name)) {
                    locName = resourceBundle.getString("config.name." + name);
                } else { //if it is undocumented, then it must have ConfigurationInternal annotation
                    Field f = fields.get(name);
                    if (!ConfigurationItem.isInternal(f)) {
                        throw new RuntimeException("Missing configuration name: " + name);
                    }

                    locName = "(Internal) " + name;
                }

                locNames.put(name, locName);
            }

            Collections.sort(names, new Comparator<String>() {
                @Override
                public int compare(String name1, String name2) {
                    return locNames.get(name1).compareTo(locNames.get(name2));
                }
            });
            for (String name : names) {
                Field field = categorized.get(cat).get(name);

                String locName = locNames.get(name);

                String description = "";
                if (resourceBundle.containsKey("config.description." + name)) {
                    description = resourceBundle.getString("config.description." + name);
                }

                if (!"".equals(filter.trim())
                        && !(locName.toLowerCase().contains(filter.toLowerCase())
                        || description.toLowerCase().contains(filter.toLowerCase()))) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    ConfigurationItem item = (ConfigurationItem) field.get(null);

                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    java.lang.reflect.Type itemType2 = listType.getActualTypeArguments()[0];
                    if (!(itemType2 instanceof Class<?>)) {
                        continue;
                    }

                    Class itemType = (Class<?>) itemType2;

                    Object defaultValue = Configuration.getDefaultValue(field);
                    if (name.equals("gui.skin")) {
                        Class c;
                        try {
                            c = Class.forName((String) defaultValue);
                            defaultValue = c.getField("NAME").get(c);
                        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException ex) {
                            Logger.getLogger(AdvancedSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    String locNameHtml = locName;
                    if (!filter.trim().equals("")) {
                        locNameHtml = Pattern.compile(Pattern.quote(filter), Pattern.UNICODE_CASE + Pattern.CASE_INSENSITIVE).matcher(locNameHtml).replaceAll("{bold}$0{/bold}");
                    }
                    locNameHtml = "<html>" + locNameHtml.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>";
                    locNameHtml = locNameHtml.replace("{bold}", "<span style=\"background-color:" + hilightBackgroundColorHex + "; color: " + hilightForegroundColorHex + "\">").replace("{/bold}", "</span>");

                    if (defaultValue != null) {
                        description += " (" + resourceBundle.getString("default") + ": " + defaultValue + ")";
                    }

                    JLabel l = new JLabel(locNameHtml, JLabel.TRAILING);
                    titleToNameMap.put(locName, name);
                    l.setToolTipText(description);
                    configPanel.add(l);

                    Component c = null;

                    if (allComponentsMap.containsKey(name)) {
                        c = allComponentsMap.get(name);
                    } else {

                        if (name.equals("gui.skin")) {
                            skinComboBox.setToolTipText(description);
                            skinComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, skinComboBox.getPreferredSize().height));
                            c = skinComboBox;
                        } else if ((itemType == String.class) || (itemType == Integer.class) || (itemType == Long.class) || (itemType == Double.class) || (itemType == Float.class) || (itemType == Calendar.class)) {
                            ConfigurationFile confFile = field.getAnnotation(ConfigurationFile.class);
                            ConfigurationDirectory confDirectory = field.getAnnotation(ConfigurationDirectory.class);

                            JTextField tf = new JTextField();
                            Object val = item.get();
                            if (val == null) {
                                val = "";
                            }
                            if (itemType == Calendar.class) {
                                tf.setText(new SimpleDateFormat().format(((Calendar) val).getTime()));
                            } else {
                                tf.setText(val.toString());
                            }
                            tf.setToolTipText(description);
                            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, tf.getPreferredSize().height));

                            c = tf;
                            if (confFile != null) {
                                c = new ConfigurationFileSelection(item, confFile, val.toString(), description);
                            }
                            if (confDirectory != null) {
                                c = new ConfigurationDirectorySelection(item, val.toString(), description);
                            }
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

                        labelsMap.put(name, l);
                        componentsMap.put(name, c);
                        allComponentsMap.put(name, c);
                    }
                    Component toLabelComponent = c;
                    if (toLabelComponent instanceof ConfigurationFileSelection) {
                        toLabelComponent = ((ConfigurationFileSelection) toLabelComponent).getTextField();
                    }
                    if (toLabelComponent instanceof ConfigurationDirectorySelection) {
                        toLabelComponent = ((ConfigurationDirectorySelection) toLabelComponent).getTextField();
                    }
                    l.setLabelFor(toLabelComponent);
                    configPanel.add(c);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    // Reflection exceptions. This should never happen
                    throw new Error(ex.getMessage());
                }

                itemCount++;
            }

            categoryCounts.put(cat, itemCount);

            SpringUtilities.makeCompactGrid(configPanel,
                    itemCount, 2, //rows, cols
                    6, 6, //initX, initY
                    6, 6);       //xPad, yPad
            if (resourceBundle.containsKey("config.group.tip." + cat)) {
                String tip = resourceBundle.getString("config.group.tip." + cat);
                String[] urls = new String[0];
                if (resourceBundle.containsKey("config.group.link." + cat)) {
                    urls = resourceBundle.getString("config.group.link." + cat).split(" ");
                }
                for (int i = 0; i < urls.length; i++) {
                    tip = tip.replace("%link" + (i + 1) + "%", urls[i]);
                }
                JPanel p = new JPanel(new BorderLayout());
                p.add(configPanel, BorderLayout.CENTER);
                JPanel tipPanel = new JPanel(new FlowLayout());
                tipPanel.add(new HtmlLabel("<b>" + resourceBundle.getString("tip") + "</b>" + tip));
                p.add(tipPanel, BorderLayout.SOUTH);
                configPanel = p;
            }
            tabs.put(cat, new FasterScrollPane(configPanel));
        }
    }

    private void showRestartConfirmDialog() {
        if (ViewMessages.showConfirmDialog(this, translate("advancedSettings.restartConfirmation"), AppStrings.translate("message.warning"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                // todo: honfika: why?
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AdvancedSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
            }

            SelectLanguageDialog.reloadUi();
        }
    }

    @SuppressWarnings("unchecked")
    private void okButtonActionPerformed(ActionEvent evt) {
        boolean modified = false;
        Map<String, Field> fields = Configuration.getConfigurationFields();
        Map<String, Object> values = new HashMap<>();
        for (String name : fields.keySet()) {
            Component c = allComponentsMap.get(name);
            Object value = null;

            ParameterizedType listType = (ParameterizedType) fields.get(name).getGenericType();
            java.lang.reflect.Type itemType2 = listType.getActualTypeArguments()[0];
            if (!(itemType2 instanceof Class<?>)) {
                continue;
            }

            Class itemType = (Class<?>) itemType2;
            if (c instanceof ConfigurationFileSelection) {
                value = ((ConfigurationFileSelection) c).getValue();
            } else if (c instanceof ConfigurationDirectorySelection) {
                value = ((ConfigurationDirectorySelection) c).getValue();
            } else if (name.equals("gui.skin")) {
                value = ((SkinSelect) ((JComboBox<SkinSelect>) c).getSelectedItem()).className;
            } else if (itemType == String.class) {
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
                field.setAccessible(true);
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
            showRestartConfirmDialog();
        }
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    private void resetButtonActionPerformed(ActionEvent evt) {
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
        showRestartConfirmDialog();
    }
}
