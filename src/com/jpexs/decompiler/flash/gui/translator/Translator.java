/*
 *  Copyright (C) 2022-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.translator;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.properties.ParsedSymbol;
import com.jpexs.helpers.properties.PropertiesLexer;
import com.jpexs.helpers.properties.PropertiesParseException;
import com.jpexs.helpers.properties.SymbolType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author JPEXS
 */
public class Translator extends JFrame implements ItemListener {

    private TreeSet<String> locales = new TreeSet<>();
    private Map<String, TreeSet<String>> resourceLocales = new TreeMap<>();
    private Set<String> resourceKeys = new TreeSet<>();
    private Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> resourceValues = new TreeMap<>();

    private Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> comments = new TreeMap<>();

    private Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> newValues = new TreeMap<>();
    private Map<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> newLinesBeforeComment = new TreeMap<>();
    private Map<String, LinkedHashMap<String, LinkedHashMap<String, Integer>>> newLinesAfterComment = new TreeMap<>();

    private Map<String, Set<String>> hiddenKeys = new TreeMap<>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<LocaleItem> localeComboBox;
    private JComboBox<ResourceItem> resourcesComboBox;

    private String lastSaveDir = "";

    private static final String DO_NOT_EDIT = "!!!! FFDec translators - please do not edit anything below this line !!!";

    private List<String> ignoredResources = Arrays.asList(
            "project",
            "META-INF/services/jsyntaxpane/kitsfortypes",
            "META-INF/services/jsyntaxpane/syntaxkits/actionscript3syntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/actionscriptsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/flasm3methodinfosyntaxkit/abbreviations",
            "META-INF/services/jsyntaxpane/syntaxkits/bashsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/clojuresyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/dosbatchsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/flasmsyntaxkit/abbreviations",
            "META-INF/services/jsyntaxpane/syntaxkits/flasm3syntaxkit/abbreviations",
            "META-INF/services/jsyntaxpane/syntaxkits/groovysyntaxkit/abbreviations",
            "META-INF/services/jsyntaxpane/syntaxkits/groovysyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/javascriptsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/javasyntaxkit/abbreviations",
            "META-INF/services/jsyntaxpane/syntaxkits/javasyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/luasyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/plainsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/propertiessyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/pythonsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/rubysyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/scalasyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/sqlsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/talsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/xhtmlsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/xmlsyntaxkit/config",
            "META-INF/services/jsyntaxpane/syntaxkits/xpathsyntaxkit/config",
            "META-INF/maven/jsyntaxpane/jsyntaxpane/pom"
    );

    private String readStreamAsString(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        int cnt = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((cnt = is.read(buf)) > 0) {
            baos.write(buf, 0, cnt);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    private void loadJar(File file, String fileTitle) throws FileNotFoundException, IOException, URISyntaxException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zis.getNextEntry();
        Pattern pat = Pattern.compile("(?<resource>(.*/)?[^/_]+)(_(?<locale>[^/\\.]+))?\\.properties");

        while (zipEntry != null) {
            if (!zipEntry.isDirectory()) {
                String name = zipEntry.getName();
                Matcher m = pat.matcher(name);
                if (m.matches()) {
                    String resource = m.group("resource");
                    if (!ignoredResources.contains(resource)) {
                        resource = fileTitle + ": " + resource;
                        String locale = m.group("locale");
                        if (locale == null) {
                            locale = "en";
                        }
                        if (!resourceLocales.containsKey(resource)) {
                            resourceLocales.put(resource, new TreeSet<>());
                            resourceKeys.add(resource);
                            resourceValues.put(resource, new LinkedHashMap<>());
                            hiddenKeys.put(resource, new HashSet<>());
                        }
                        if (!resourceValues.get(resource).containsKey(locale)) {
                            resourceValues.get(resource).put(locale, new LinkedHashMap<>());
                        }
                        resourceLocales.get(resource).add(locale);
                        locales.add(locale);
                        String propertiesData = readStreamAsString(zis);
                        PropertiesLexer lexer = new PropertiesLexer(propertiesData);
                        try {
                            ParsedSymbol s = lexer.lex();
                            boolean hidden = false;
                            String comment = "";
                            int numEmptyLinesBeforeComment = 0;
                            int numEmptyLinesAfterComment = 0;
                            while (s.type != SymbolType.EOF) {
                                if (s.type == SymbolType.COMMENT) {
                                    if (((String) s.value).trim().equals(DO_NOT_EDIT)) {
                                        hidden = true;
                                    }
                                    if (comment.isEmpty()) {
                                        comment = (String) s.value;
                                    } else {
                                        comment = comment + "\r\n" + s.value;
                                    }

                                    s = lexer.lex();
                                    continue;
                                }
                                if (s.type == SymbolType.EMPTY_LINE) {
                                    if (comment.isEmpty()) {
                                        numEmptyLinesBeforeComment++;
                                    } else {
                                        numEmptyLinesAfterComment++;
                                    }
                                    s = lexer.lex();
                                    continue;
                                }
                                //System.out.println(s);
                                if (s.type == SymbolType.EOF) {
                                    break;
                                }
                                if (s.type != SymbolType.KEY) {
                                    throw new RuntimeException("KEY EXPECTED");
                                    //break;
                                }
                                String key = (String) s.value;
                                s = lexer.lex();

                                if (s.type != SymbolType.VALUE) {
                                    throw new RuntimeException("VALUE EXPECTED");
                                    //break;
                                }
                                String value = (String) s.value;

                                resourceValues.get(resource).get(locale).put(key, value);
                                if (hidden) {
                                    hiddenKeys.get(resource).add(key);
                                }

                                if (!comment.isEmpty()) {
                                    if (!comments.containsKey(resource)) {
                                        comments.put(resource, new LinkedHashMap<>());
                                    }
                                    if (!comments.get(resource).containsKey(locale)) {
                                        comments.get(resource).put(locale, new LinkedHashMap<>());
                                    }
                                    comments.get(resource).get(locale).put(key, comment);
                                }

                                if (numEmptyLinesAfterComment > 0) {
                                    if (!newLinesAfterComment.containsKey(resource)) {
                                        newLinesAfterComment.put(resource, new LinkedHashMap<>());
                                    }
                                    if (!newLinesAfterComment.get(resource).containsKey(locale)) {
                                        newLinesAfterComment.get(resource).put(locale, new LinkedHashMap<>());
                                    }
                                    newLinesAfterComment.get(resource).get(locale).put(key, numEmptyLinesAfterComment);
                                }

                                if (numEmptyLinesBeforeComment > 0) {
                                    if (!newLinesBeforeComment.containsKey(resource)) {
                                        newLinesBeforeComment.put(resource, new LinkedHashMap<>());
                                    }
                                    if (!newLinesBeforeComment.get(resource).containsKey(locale)) {
                                        newLinesBeforeComment.get(resource).put(locale, new LinkedHashMap<>());
                                    }
                                    newLinesBeforeComment.get(resource).get(locale).put(key, numEmptyLinesBeforeComment);
                                }
                                numEmptyLinesAfterComment = 0;
                                numEmptyLinesBeforeComment = 0;

                                comment = "";
                                //System.out.println(resource+": locale="+locale+" key="+key+" value="+value);
                                s = lexer.lex();
                            }
                            //System.exit(0);
                        } catch (PropertiesParseException ex) {
                            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private void loadJarTry(String path) throws FileNotFoundException, IOException, URISyntaxException {
        File file;
        file = new File("dist/" + path);
        if (!file.exists()) {
            file = new File(path);
        }
        loadJar(file, path);
    }

    private void loadItems() throws FileNotFoundException, IOException, URISyntaxException {
        loadJarTry("ffdec.jar");
        loadJarTry("lib/ffdec_lib.jar");
        loadJarTry("lib/jsyntaxpane-0.9.5.jar");
    }

    public Translator() throws IOException, FileNotFoundException, URISyntaxException {

        loadItems();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cnt = getContentPane();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    save();
                    saveWindow();
                } catch (IOException ex) {
                    Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        List<LocaleItem> localeItems = new ArrayList<LocaleItem>();
        for (String locale : locales) {
            localeItems.add(new LocaleItem(locale));
        }
        localeComboBox = new JComboBox<>(localeItems.toArray(new LocaleItem[localeItems.size()]));

        List<ResourceItem> resources = new ArrayList<>();
        for (String resource : resourceLocales.keySet()) {
            resources.add(new ResourceItem(resource));
        }

        resourcesComboBox = new JComboBox<>(resources.toArray(new ResourceItem[resources.size()]));
        resourcesComboBox.addItemListener(this);
        localeComboBox.addItemListener(this);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1;
            }

        };

        tableModel.addColumn("key");
        tableModel.addColumn("en");
        tableModel.addColumn("translated");

        tableModel.addRow(new Object[]{"menu.new", "New", "Nový"});
        tableModel.addRow(new Object[]{"error.missing", "Error: missing xy", "Chyba: chybí xy"});

        table.getColumn("key").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setVerticalAlignment(JLabel.TOP);
                String key = (String) value;
                String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
                String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;

                if (comments.containsKey(resource)
                        && comments.get(resource).containsKey("en")
                        && comments.get(resource).get("en").containsKey(key)) {

                    //"General Public License"
                    String comment = comments.get(resource).get("en").get(key);
                    if (comment.contains("General Public License")) {
                        label.setIcon(null);
                        label.setToolTipText(null);
                    } else {
                        label.setIcon(View.getIcon("about16"));
                        label.setToolTipText(comment);
                    }
                } else {
                    label.setIcon(null);
                    label.setToolTipText(null);
                }

                return label;
            }

        });

        table.getColumn("en").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String valueStr = "" + value;
                label.setText("<html>" + valueStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\r\n", "<br/>") + "</html>");
                label.setVerticalAlignment(JLabel.TOP);

                return label;
            }

        });
        table.getColumn("translated").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setVerticalAlignment(JLabel.TOP);
                String key = (String) table.getValueAt(row, 0);
                String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
                String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;
                String oldValue = null;
                if (resourceValues.containsKey(resource)
                        && resourceValues.get(resource).containsKey(locale)
                        && resourceValues.get(resource).get(locale).containsKey(key)) {
                    oldValue = resourceValues.get(resource).get(locale).get(key);
                }
                String enValue = resourceValues.get(resource).get("en").get(key);
                if (enValue == null) {
                    enValue = "";
                }

                if (resourceValues.containsKey(resource)
                        && resourceValues.get(resource).containsKey(locale)
                        && resourceValues.get(resource).get(locale).containsKey(key)) {
                    if (!Objects.equals(oldValue, value)) {
                        label.setBackground(new Color(0xaa, 0xaa, 0xff));
                    } else {
                        label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    }
                } else if (newValues.containsKey(resource)
                        && newValues.get(resource).containsKey(locale)
                        && newValues.get(resource).get(locale).containsKey(key)) {
                    label.setBackground(new Color(0xaa, 0xff, 0xaa));
                } else if ((!resourceValues.get(resource).containsKey(locale)
                        || !resourceValues.get(resource).get(locale).containsKey(key))
                        && !enValue.equals("")) {
                    label.setBackground(new Color(0xff, 0xaa, 0xaa));
                } else {
                    label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }

                String valueStr = "" + value;
                label.setText("<html>" + valueStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\r\n", "<br/>") + "</html>");

                return label;
            }

        });

        table.putClientProperty("terminateEditOnFocusLost", true);
        table.getColumn("translated").setCellEditor(new JTextAreaColumn(resourceValues, localeComboBox, resourcesComboBox));

        table.getColumn("en").setCellEditor(new JTextAreaColumn(resourceValues, localeComboBox, resourcesComboBox));

        TableCellEditor editor = table.getColumn("translated").getCellEditor();
        editor.addCellEditorListener(getEditorListener());
        editor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
                String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;
                String key = (String) table.getModel().getValueAt(table.getSelectedRow(), 0);
                String value = (String) editor.getCellEditorValue();
                String oldValue = null;
                if (resourceValues.containsKey(resource)
                        && resourceValues.get(resource).containsKey(locale)
                        && resourceValues.get(resource).get(locale).containsKey(key)) {
                    oldValue = resourceValues.get(resource).get(locale).get(key);
                }
                String enValue = null;
                if (resourceValues.containsKey(resource)
                        && resourceValues.get(resource).containsKey("en")
                        && resourceValues.get(resource).get("en").containsKey(key)) {
                    enValue = resourceValues.get(resource).get("en").get(key);
                }
                if (Objects.equals(oldValue, value) || ("".equals(value) && !enValue.equals(""))) {
                    if (newValues.containsKey(resource)
                            && newValues.get(resource).containsKey(locale)
                            && newValues.get(resource).get(locale).containsKey(key)) {
                        newValues.get(resource).get(locale).remove(key);
                        if (newValues.get(resource).get(locale).isEmpty()) {
                            newValues.get(resource).remove(locale);
                        }
                        if (newValues.get(resource).isEmpty()) {
                            newValues.remove(resource);
                        }
                    }
                } else {
                    if (!newValues.containsKey(resource)) {
                        newValues.put(resource, new LinkedHashMap<>());
                    }
                    if (!newValues.get(resource).containsKey(locale)) {
                        newValues.get(resource).put(locale, new LinkedHashMap<>());
                    }
                    newValues.get(resource).get(locale).put(key, value);
                }
                try {
                    save();
                } catch (IOException ex) {
                    Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                }
                updateCounts();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }
        });

        cnt.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        topPanel.add(new JLabel("Locale:"), c);

        c.gridx = 1;
        c.gridy = 0;
        topPanel.add(localeComboBox, c);

        c.gridx = 2;
        c.gridy = 0;

        JButton newLocaleButton = new JButton("New");
        newLocaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newLocale = ViewMessages.showInputDialog(Translator.this, "Enter new locale identifier", "New locale", "");
                if (newLocale == null) {
                    return;
                }
                newLocale = newLocale.trim();
                if (newLocale.equals("")) {
                    return;
                }

                for (int i = 0; i < localeComboBox.getItemCount(); i++) {
                    String locale = localeComboBox.getItemAt(i).locale;
                    if (locale.equals(newLocale)) {
                        ViewMessages.showMessageDialog(Translator.this, "Locale already exists", "Already exists", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                locales.add(newLocale);

                List<LocaleItem> localeItems = new ArrayList<LocaleItem>();
                int index = -1;
                int i = 0;
                for (String locale : locales) {
                    if (locale.equals(newLocale)) {
                        index = i;
                    }
                    i++;
                    localeItems.add(new LocaleItem(locale));
                }
                localeComboBox.setModel(new DefaultComboBoxModel<>(localeItems.toArray(new LocaleItem[localeItems.size()])));
                localeComboBox.setSelectedIndex(index);
            }
        });
        topPanel.add(newLocaleButton, c);

        c.gridx = 0;
        c.gridy = 1;
        topPanel.add(new JLabel("Resource:"), c);

        c.gridx = 1;
        c.gridy = 1;
        topPanel.add(resourcesComboBox, c);

        c.gridx = 2;
        c.gridy = 1;

        JButton nextMissingButton = new JButton("Next missing");
        nextMissingButton.addActionListener(new ActionListener() {
            private void selectFirstMissing() {
                String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
                String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;
                int row = 0;
                for (String key : resourceValues.get(resource).get("en").keySet()) {
                    if (!resourceValues.containsKey(resource)
                            || !resourceValues.get(resource).containsKey(locale)
                            || !resourceValues.get(resource).get(locale).containsKey(key)) {
                        if (!newValues.containsKey(resource)
                                || !newValues.get(resource).containsKey(locale)
                                || !newValues.get(resource).get(locale).containsKey(key)) {
                            table.scrollRectToVisible(table.getCellRect(row, 0, false));
                            break;
                        }
                    }
                    row++;
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = resourcesComboBox.getSelectedIndex() + 1; i < resourcesComboBox.getItemCount(); i++) {
                    ResourceItem item = resourcesComboBox.getItemAt(i);
                    if (item.missingCount > 0) {
                        resourcesComboBox.setSelectedIndex(i);
                        selectFirstMissing();
                        return;
                    }
                }
                for (int i = 0; i < resourcesComboBox.getItemCount(); i++) {
                    ResourceItem item = resourcesComboBox.getItemAt(i);
                    if (item.missingCount > 0) {
                        resourcesComboBox.setSelectedIndex(i);
                        selectFirstMissing();
                        return;
                    }
                }
            }
        });

        topPanel.add(nextMissingButton, c);

        cnt.add(topPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton exportButton = new JButton("Export JPT");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                FileFilter jptFilter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".jpt")) || (f.isDirectory());
                    }

                    @Override
                    public String getDescription() {
                        return "JPEXS translation files (*.jpt)";
                    }
                };
                fc.setFileFilter(jptFilter);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setSelectedFile(new File("translation.jpt"));
                if (!lastSaveDir.isEmpty()) {
                    fc.setCurrentDirectory(new File(lastSaveDir));
                }
                if (fc.showSaveDialog(Translator.this) == JFileChooser.APPROVE_OPTION) {
                    File file = Helper.fixDialogFile(fc.getSelectedFile());
                    String fileName = file.getAbsolutePath();
                    FileFilter selFilter = fc.getFileFilter();
                    if (selFilter == jptFilter) {
                        if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".jpt")) {
                            fileName += ".jpt";
                        }
                    }
                    File targetFile = new File(fileName);
                    if (targetFile.exists()) {
                        targetFile.delete();
                    }
                    try {
                        Files.copy(new File(getStorageFile()).toPath(), targetFile.toPath());
                    } catch (IOException ex) {
                        ViewMessages.showMessageDialog(Translator.this, "Error during saving: " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    lastSaveDir = file.getParentFile().getAbsolutePath();
                }
            }
        });

        JButton importButton = new JButton("Import JPT");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ViewMessages.showConfirmDialog(Translator.this, "WARNING: This will erase all your previous work and imports data from other JPT file", "WARNING", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

                    JFileChooser fc = new JFileChooser();
                    FileFilter jptFilter = new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".jpt")) || (f.isDirectory());
                        }

                        @Override
                        public String getDescription() {
                            return "JPEXS translation files (*.jpt)";
                        }
                    };
                    fc.setFileFilter(jptFilter);
                    fc.setAcceptAllFileFilterUsed(false);
                    fc.setSelectedFile(new File("translation.jpt"));
                    if (!lastSaveDir.isEmpty()) {
                        fc.setCurrentDirectory(new File(lastSaveDir));
                    }
                    if (fc.showOpenDialog(Translator.this) == JFileChooser.APPROVE_OPTION) {
                        File file = Helper.fixDialogFile(fc.getSelectedFile());
                        String fileName = file.getAbsolutePath();
                        File targetFile = new File(getStorageFile());
                        File tmpFile = new File(getStorageFile() + ".tmp");
                        try {
                            if (targetFile.exists()) {
                                targetFile.renameTo(tmpFile);
                            }
                            Files.copy(new File(fileName).toPath(), targetFile.toPath());
                            tmpFile.delete();
                            load();
                        } catch (IOException ex) {
                            tmpFile.renameTo(targetFile);
                            ViewMessages.showMessageDialog(Translator.this, "Error during importing: " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JButton startOverButton = new JButton("Start over");
        startOverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ViewMessages.showConfirmDialog(Translator.this, "Do you really want to lose all your work? This is unreversable!", "WARNING", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                    newValues.clear();
                    try {
                        save();
                        load();
                    } catch (IOException ex) {
                        Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });
        buttonsPanel.add(importButton);
        buttonsPanel.add(exportButton);
        buttonsPanel.add(startOverButton);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        cnt.add(new JScrollPane(table), BorderLayout.CENTER);
        resizeColumnWidth(table);
        setSize(800, 600);
        setTitle("JPEXS Free Flash Decompiler Translator");
        itemStateChanged(null);
        table.setRowHeight(table.getFont().getSize() + 10);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateRowHeights(table);
            }
        });
        View.centerScreen(this);

        load();
        loadWindow();
    }

    private void updateCounts() {
        String selectedLocale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
        for (int j = 0; j < localeComboBox.getItemCount(); j++) {
            LocaleItem localeItem = localeComboBox.getItemAt(j);
            String locale = localeItem.locale;
            int localeMissingCount = 0;
            int localeNewCount = 0;
            int localeModifiedCount = 0;

            for (int i = 0; i < resourcesComboBox.getItemCount(); i++) {
                ResourceItem resourceItem = resourcesComboBox.getItemAt(i);
                String resource = resourceItem.resource;

                int missingCount = 0;
                int newCount = 0;
                int modifiedCount = 0;
                for (String key : resourceValues.get(resource).get("en").keySet()) {
                    if (resourceValues.get(resource).containsKey(locale)
                            && resourceValues.get(resource).get(locale).containsKey(key)) {
                        if (newValues.containsKey(resource)
                                && newValues.get(resource).containsKey(locale)
                                && newValues.get(resource).get(locale).containsKey(key)) {
                            modifiedCount++;
                        }
                    } else {
                        if (newValues.containsKey(resource)
                                && newValues.get(resource).containsKey(locale)
                                && newValues.get(resource).get(locale).containsKey(key)) {
                            newCount++;
                        } else {
                            missingCount++;
                        }
                    }
                }
                if (locale.equals(selectedLocale)) {
                    resourceItem.missingCount = missingCount;
                    resourceItem.modifiedCount = modifiedCount;
                    resourceItem.newCount = newCount;
                }
                localeMissingCount += missingCount;
                localeModifiedCount += modifiedCount;
                localeNewCount += newCount;
            }

            localeItem.missingCount = localeMissingCount;
            localeItem.modifiedCount = localeModifiedCount;
            localeItem.newCount = localeNewCount;
        }
        repaint();

    }

    private CellEditorListener getEditorListener() {
        return new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                updateRowHeights(table);
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                updateRowHeights(table);
            }
        };
    }

    private void updateRowHeights(JTable table) {
        try {
            for (int row = 0; row < table.getRowCount(); row++) {
                int rowHeight = table.getRowHeight();

                for (int column = 0; column < table.getColumnCount(); column++) {
                    Component comp = table.prepareRenderer(
                            table.getCellRenderer(row, column), row, column);
                    int maxHeight = 0;
                    int columnWidth = table.getColumn(table.getColumnName(column)).getWidth();

                    if (comp instanceof JLabel) {
                        JLabel lab = (JLabel) comp;
                        javax.swing.text.View view = (javax.swing.text.View) lab.getClientProperty("html");
                        if (view != null) {
                            String text = lab.getText();
                            float textWidth = view.getPreferredSpan(javax.swing.text.View.X_AXIS);
                            float charHeight = view.getPreferredSpan(javax.swing.text.View.Y_AXIS);
                            double lines = Math.ceil(textWidth / (double) columnWidth);
                            double height = lines * charHeight;
                            int heightInt = (int) Math.ceil(height);
                            if (heightInt > maxHeight) {
                                maxHeight = heightInt;
                            }
                        }
                    }
                    if (maxHeight == 0) {
                        maxHeight = comp.getPreferredSize().height;
                    }

                    comp.setPreferredSize(new Dimension(columnWidth, maxHeight));

                    rowHeight = Math.max(rowHeight, maxHeight);
                }
                table.setRowHeight(row, rowHeight);
            }
        } catch (ClassCastException e) {
            //ignored
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
        String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;
        tableModel.setRowCount(0);
        for (String key : resourceValues.get(resource).get("en").keySet()) {
            String value = "";
            if (newValues.containsKey(resource) && newValues.get(resource).containsKey(locale) && newValues.get(resource).get(locale).containsKey(key)) {
                value = newValues.get(resource).get(locale).get(key);
            } else if (resourceValues.get(resource).containsKey(locale) && resourceValues.get(resource).get(locale).containsKey(key)) {
                value = resourceValues.get(resource).get(locale).get(key);
            }
            if (hiddenKeys.get(resource).contains(key)) {
                continue;
            }
            tableModel.addRow(new Object[]{key, resourceValues.get(resource).get("en").get(key), value});
        }
        updateRowHeights(table);
        updateCounts();
    }

    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300) {
                width = 300;
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private String getStorageFile() {
        return Configuration.getFFDecHome() + "translated.zip";
    }

    private String getWindowFile() {
        return Configuration.getFFDecHome() + "translated.ini";
    }

    private String escapeUnicode(String s) {
        StringBuilder b = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (c >= 128) {
                b.append("\\u").append(String.format("%04x", (int) c));
            } else {
                b.append(c);
            }
        }

        return b.toString();
    }

    private String escapeKey(String s) {
        if (s.startsWith("#") || s.startsWith("!")) {
            s = "\\" + s;
        }
        s = escapeUnicode(s);
        return s;
    }

    private String escapeValue(String s) {
        if (s.startsWith(" ")) {
            s = "\\" + s;
        }
        s = s.replace("\r\n", "\n");
        s = s.replace("\n", "\\\r\n ");
        s = escapeUnicode(s);
        return s;
    }

    private void load() throws IOException {
        newValues.clear();

        File storageFile = new File(getStorageFile());
        if (storageFile.exists()) {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(storageFile));
            ZipEntry zipEntry = zis.getNextEntry();
            Pattern pat = Pattern.compile("(?<resource>(.*/)?[^/_]+)(_(?<locale>[^/\\.]+))?\\.properties");

            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    String name = zipEntry.getName();
                    Matcher m = pat.matcher(name);
                    if (m.matches()) {
                        String resource = m.group("resource");
                        resource = resource.replace(".jar/", ".jar: ");
                        String locale = m.group("locale");
                        if (locale == null) {
                            locale = "en";
                        }
                        locales.add(locale);
                        if (!newValues.containsKey(resource)) {
                            newValues.put(resource, new LinkedHashMap<>());
                        }
                        if (!newValues.get(resource).containsKey(locale)) {
                            newValues.get(resource).put(locale, new LinkedHashMap<>());
                        }
                        resourceLocales.get(resource).add(locale);
                        locales.add(locale);
                        String propertiesData = readStreamAsString(zis);
                        PropertiesLexer lexer = new PropertiesLexer(propertiesData);
                        try {
                            ParsedSymbol s = lexer.lex();
                            boolean hidden = false;
                            while (s.type != SymbolType.EOF) {
                                if (s.type == SymbolType.COMMENT) {
                                    if (((String) s.value).trim().equals(DO_NOT_EDIT)) {
                                        hidden = true;
                                    }
                                    s = lexer.lex();
                                    continue;
                                }
                                if (s.type == SymbolType.EMPTY_LINE) {
                                    s = lexer.lex();
                                    continue;
                                }
                                //System.out.println(s);
                                if (s.type == SymbolType.EOF) {
                                    break;
                                }
                                if (s.type != SymbolType.KEY) {
                                    throw new RuntimeException("KEY EXPECTED");
                                    //break;
                                }
                                String key = (String) s.value;
                                s = lexer.lex();

                                if (s.type != SymbolType.VALUE) {
                                    throw new RuntimeException("VALUE EXPECTED");
                                    //break;
                                }
                                String value = (String) s.value;
                                if (!hidden) {
                                    if (resourceValues.containsKey(resource)
                                            && resourceValues.get(resource).containsKey(locale)
                                            && resourceValues.get(resource).get(locale).containsKey(key)
                                            && Objects.equals(resourceValues.get(resource).get(locale).get(key), value)) {
                                        //same, ignore
                                    } else {
                                        newValues.get(resource).get(locale).put(key, value);
                                    }
                                }
                                //System.out.println(resource+": locale="+locale+" key="+key+" value="+value);
                                s = lexer.lex();
                            }
                            //System.exit(0);
                        } catch (PropertiesParseException ex) {
                            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }

        List<LocaleItem> localeItems = new ArrayList<LocaleItem>();
        for (String locale : locales) {
            localeItems.add(new LocaleItem(locale));
        }
        localeComboBox.setModel(new DefaultComboBoxModel<>(localeItems.toArray(new LocaleItem[localeItems.size()])));

        itemStateChanged(null);
        repaint();
    }

    private void loadWindow() throws FileNotFoundException, IOException {
        if (!new File(getWindowFile()).exists()) {
            return;
        }

        FileReader fileReader = new FileReader(getWindowFile());
        BufferedReader reader = new BufferedReader(fileReader);
        String s = null;
        while ((s = reader.readLine()) != null) {
            if (!s.contains("=")) {
                continue;
            }
            String key = s.substring(0, s.indexOf("="));
            String value = s.substring(s.indexOf("=") + 1);

            int valueInt;
            switch (key) {
                case "locale":
                    for (int i = 0; i < localeComboBox.getItemCount(); i++) {
                        LocaleItem item = (LocaleItem) localeComboBox.getItemAt(i);
                        if (item.locale.equals(value)) {
                            localeComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                    break;
                case "resource":
                    for (int i = 0; i < resourcesComboBox.getItemCount(); i++) {
                        ResourceItem item = (ResourceItem) resourcesComboBox.getItemAt(i);
                        if (item.resource.equals(value)) {
                            resourcesComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                    break;
                case "window.x":
                    int x = Integer.parseInt(value);
                    setLocation(x, getLocation().y);
                    break;
                case "window.y":
                    int y = Integer.parseInt(value);
                    setLocation(getLocation().x, y);
                    break;
                case "window.width":
                    int width = Integer.parseInt(value);
                    setSize(width, getSize().height);
                    break;
                case "window.height":
                    int height = Integer.parseInt(value);
                    setSize(getSize().width, height);
                    break;
                case "window.maximized":
                    if (value.equals("true")) {
                        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
                    }
                    break;
                case "column.key.width":
                    valueInt = Integer.parseInt(value);
                    table.getColumn("key").setPreferredWidth(valueInt);
                    break;
                case "column.en.width":
                    valueInt = Integer.parseInt(value);
                    table.getColumn("en").setPreferredWidth(valueInt);
                    break;
                case "column.translated.width":
                    valueInt = Integer.parseInt(value);
                    table.getColumn("translated").setPreferredWidth(valueInt);
                    break;
                case "export.dir":
                    lastSaveDir = value;
                    break;
            }

        }
        fileReader.close();
    }

    private void saveWindow() throws IOException {

        FileWriter writer = new FileWriter(new File(getWindowFile()));
        PrintWriter pw = new PrintWriter(writer);
        pw.println("locale=" + ((LocaleItem) localeComboBox.getSelectedItem()).locale);
        pw.println("resource=" + ((ResourceItem) resourcesComboBox.getSelectedItem()).resource);
        pw.println("window.x=" + getLocation().x);
        pw.println("window.y=" + getLocation().y);
        pw.println("window.width=" + getSize().width);
        pw.println("window.height=" + getSize().height);
        if ((getExtendedState() & MAXIMIZED_BOTH) > 0) {
            pw.println("window.maximized=true");
        } else {
            pw.println("window.maximized=false");
        }
        pw.println("column.key.width=" + table.getColumn("key").getWidth());
        pw.println("column.en.width=" + table.getColumn("en").getWidth());
        pw.println("column.translated.width=" + table.getColumn("translated").getWidth());
        pw.println("export.dir=" + lastSaveDir);

        writer.close();
    }

    private void saveAll() throws FileNotFoundException, IOException {
        String storageFile = Configuration.getFFDecHome() + "alltranslated.zip";
        save(storageFile, resourceValues);
    }

    private void save() throws FileNotFoundException, IOException {
        save(getStorageFile(), newValues);
    }

    private void save(String storageFile, Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> values) throws FileNotFoundException, IOException {
        String storageFileTmp = storageFile + ".tmp";
        FileOutputStream fos = new FileOutputStream(storageFileTmp);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String resource : values.keySet()) {
            for (String locale : values.get(resource).keySet()) {
                String resourceZipName = resource.replace(": ", "/");
                String propertiesPath = resourceZipName + (locale.equals("en") ? "" : "_" + locale) + ".properties";
                ZipEntry zipEntry = new ZipEntry(propertiesPath);
                zipOut.putNextEntry(zipEntry);
                for (String key : resourceValues.get(resource).get("en").keySet()) {
                    String value;
                    if (values.containsKey(resource)
                            && values.get(resource).containsKey(locale)
                            && values.get(resource).get(locale).containsKey(key)) {
                        value = values.get(resource).get(locale).get(key);
                    } else if (resourceValues.containsKey(resource)
                            && resourceValues.get(resource).containsKey(locale)
                            && resourceValues.get(resource).get(locale).containsKey(key)) {
                        value = resourceValues.get(resource).get(locale).get(key);
                    } else {
                        continue;
                    }

                    if (newLinesBeforeComment.containsKey(resource)
                            && newLinesBeforeComment.get(resource).containsKey(locale)
                            && newLinesBeforeComment.get(resource).get(locale).containsKey(key)) {
                        int numLines = newLinesBeforeComment.get(resource).get(locale).get(key);
                        for (int i = 0; i < numLines; i++) {
                            zipOut.write("\r\n".getBytes("UTF-8"));
                        }
                    }
                    if (comments.containsKey(resource)
                            && comments.get(resource).containsKey("en")
                            && comments.get(resource).get("en").containsKey(key)) {
                        String comment = comments.get(resource).get("en").get(key);
                        comment = "#" + comment.replace("\r\n", "\r\n#") + "\r\n";
                        zipOut.write(comment.getBytes("UTF-8"));
                    }
                    if (newLinesAfterComment.containsKey(resource)
                            && newLinesAfterComment.get(resource).containsKey(locale)
                            && newLinesAfterComment.get(resource).get(locale).containsKey(key)) {
                        int numLines = newLinesAfterComment.get(resource).get(locale).get(key);
                        for (int i = 0; i < numLines; i++) {
                            zipOut.write("\r\n".getBytes("UTF-8"));
                        }
                    }
                    zipOut.write((escapeKey(key) + " = " + escapeValue(value) + "\r\n").getBytes("UTF-8"));
                }
            }
        }
        zipOut.close();
        fos.close();
        if (!new File(storageFile).exists() || new File(storageFile).delete()) {
            new File(storageFileTmp).renameTo(new File(storageFile));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ignored) {
            //ignored
        }
        try {
            Translator t = new Translator();
            t.setVisible(true);
            t.updateRowHeights(t.table);
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class LocaleItem {

        public String locale;
        public int missingCount = 0;
        public int modifiedCount = 0;
        public int newCount = 0;

        public LocaleItem(String locale) {
            this.locale = locale;
        }

        @Override
        public String toString() {
            String[] parts = locale.split("_");
            Locale loc;

            if (parts.length == 2) {
                loc = View.createLocale(parts[0], parts[1]);
            } else {
                loc = View.createLocale(parts[0]);
            }

            String ret = loc.getDisplayName() + " [" + locale + "]";
            if (missingCount > 0 || modifiedCount > 0 || newCount > 0) {
                ret += " (";
                List<String> parts2 = new ArrayList<>();
                if (missingCount > 0) {
                    parts2.add("" + missingCount + " missing");
                }
                if (modifiedCount > 0) {
                    parts2.add("" + modifiedCount + " modified");
                }
                if (newCount > 0) {
                    parts2.add("" + newCount + " new");
                }
                ret += String.join(", ", parts2);

                ret += ")";

            }

            return ret;
        }

    }

    class ResourceItem {

        public String resource;
        public int missingCount = 0;
        public int modifiedCount = 0;
        public int newCount = 0;

        public ResourceItem(String resource) {
            this.resource = resource;
        }

        @Override
        public String toString() {
            String ret = resource;
            if (missingCount > 0 || modifiedCount > 0 || newCount > 0) {
                ret += " (";
                List<String> parts = new ArrayList<>();
                if (missingCount > 0) {
                    parts.add("" + missingCount + " missing");
                }
                if (modifiedCount > 0) {
                    parts.add("" + modifiedCount + " modified");
                }
                if (newCount > 0) {
                    parts.add("" + newCount + " new");
                }
                ret += String.join(", ", parts);

                ret += ")";

            }

            return ret;
        }
    }

    class JTextAreaColumn extends AbstractCellEditor implements TableCellEditor {

        private JTextArea textArea = new JTextArea();
        private JTextField textField = new JTextField();
        private JScrollPane pane = new JScrollPane(textArea);
        private boolean areaMode = false;
        private Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> resourceValues;
        private JComboBox<LocaleItem> localeComboBox;
        private JComboBox<ResourceItem> resourcesComboBox;

        public JTextAreaColumn(
                Map<String, LinkedHashMap<String, LinkedHashMap<String, String>>> resourceValues,
                JComboBox<LocaleItem> localeComboBox,
                JComboBox<ResourceItem> resourcesComboBox
        ) {
            pane.setBorder(null);
            this.resourceValues = resourceValues;
            this.localeComboBox = localeComboBox;
            this.resourcesComboBox = resourcesComboBox;
        }

        @Override
        public Object getCellEditorValue() {
            String val;
            if (areaMode) {
                return textArea.getText().replace("\r\n", "\n").replace("\n", "\r\n");
            }
            return textField.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {

            String locale = ((LocaleItem) localeComboBox.getSelectedItem()).locale;
            String resource = ((ResourceItem) resourcesComboBox.getSelectedItem()).resource;
            String key = (String) table.getModel().getValueAt(row, 0);

            String enValue = "";
            if (resourceValues.containsKey(resource)
                    && resourceValues.get(resource).containsKey("en")
                    && resourceValues.get(resource).get("en").containsKey(key)) {
                enValue = resourceValues.get(resource).get("en").get(key);
            }

            String valueStr = value == null ? "" : value.toString();

            areaMode = true;
            textArea.setFont(new JLabel().getFont());
            textArea.setText(valueStr);
            textArea.setEditable(column == 2);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            return pane;

            /*
        String valueStr = value == null ? "" : value.toString();
        if (enValue.contains("\n") || valueStr.contains("\n")) {
            textArea.setText(valueStr);
            textArea.setEditable(column == 2);
            areaMode = true;
            return pane;
        }
        areaMode = false;
        textField.setText(valueStr);
        textField.setEditable(column == 2);        
        return textField;*/
        }
    }
}
