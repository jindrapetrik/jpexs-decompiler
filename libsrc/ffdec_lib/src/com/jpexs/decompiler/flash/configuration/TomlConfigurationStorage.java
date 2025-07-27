/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.configuration;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tomlj.Toml;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

/**
 * TOML file confiraration storage.
 * @author JPEXS
 */
public class TomlConfigurationStorage implements ConfigurationStorage {

    @Override
    public String getConfigName() {
        return "config.toml";
    }

    @Override
    public Map<String, Object> loadFromFile(String file) {
        Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.FINE, "Loading TOML file {0}", file);
            
        Map<String, Object> result = new LinkedHashMap<>();
        TomlParseResult tomlResult;
        try {
            tomlResult = Toml.parse(Paths.get(file));
        } catch (IOException ex) {
            Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.SEVERE, "Error reading TOML file", ex);
            return result;
        }

        if (!tomlResult.errors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error parsing configuration file:\r\n");
            for (TomlParseError error : tomlResult.errors()) {
                sb.append("- ").append(error).append("\r\n");
            }
            Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.SEVERE, sb.toString());
        }

        TomlTable configurationTable = tomlResult.getTable("configuration");
        if (configurationTable == null) {
            return result;
        }

        for (Map.Entry<String, Field> entry : Configuration.getConfigurationFields(false, true).entrySet()) {
            try {
                String name = entry.getKey();
                Field field = entry.getValue();
                ConfigurationItem item = (ConfigurationItem) field.get(null);
                ConfigurationName nameAnnotation = field.getAnnotation(ConfigurationName.class);
                if (nameAnnotation != null) {
                    name = nameAnnotation.value();
                }
                String key = name;
                if (key.contains(".")) {
                    key = "\"" + key + "\"";
                }

                Object value = null;

                switch (name) {
                    case "fontPairingMap":
                        TomlTable fontPairingTable = configurationTable.getTable(key);
                        if (fontPairingTable != null) {
                            Map<String, String> fontPairingMap = new LinkedHashMap<>();
                            for (Map.Entry<String, Object> fontEntry : fontPairingTable.entrySet()) {
                                if (fontEntry.getValue() instanceof String) {
                                    fontPairingMap.put(fontEntry.getKey(), (String) fontEntry.getValue());
                                }
                            }
                            value = fontPairingMap;
                        }
                        break;
                    case "swfSpecificConfigs":
                        TomlTable swfSpecificConfigsTable = configurationTable.getTable(key);
                        if (swfSpecificConfigsTable != null) {
                            Map<String, SwfSpecificConfiguration> swfSpecificConfigs = new LinkedHashMap<>();
                            for (Map.Entry<String, Object> swfEntry : swfSpecificConfigsTable.entrySet()) {
                                String swfKey = swfEntry.getKey();
                                if (swfEntry.getValue() instanceof TomlTable) {
                                    SwfSpecificConfiguration swfSpecificConfig = new SwfSpecificConfiguration();
                                    TomlTable configsTable = (TomlTable) swfEntry.getValue();
                                    TomlTable swfSpecificFontPairingTable = configsTable.getTable("fontPairingMap");
                                    Map<String, String> swfSpecificFontPairingMap = new LinkedHashMap<>();
                                    for (Map.Entry<String, Object> fontEntry : swfSpecificFontPairingTable.entrySet()) {
                                        if (fontEntry.getValue() instanceof String) {
                                            swfSpecificFontPairingMap.put(fontEntry.getKey(), (String) fontEntry.getValue());
                                        }
                                    }
                                    swfSpecificConfig.fontPairingMap = swfSpecificFontPairingMap;
                                    swfSpecificConfig.lastSelectedPath = configsTable.getString("lastSelectedPath");
                                    swfSpecificConfigs.put(swfKey, swfSpecificConfig);
                                }
                            }
                            value = swfSpecificConfigs;
                        }
                        break;
                    case "swfSpecificCustomConfigs":
                        TomlTable swfSpecificCustomConfigsTable = configurationTable.getTable(key);
                        if (swfSpecificCustomConfigsTable != null) {
                            Map<String, SwfSpecificCustomConfiguration> swfSpecificCustomConfigs = new LinkedHashMap<>();
                            for (Map.Entry<String, Object> swfEntry : swfSpecificCustomConfigsTable.entrySet()) {
                                String swfKey = swfEntry.getKey();
                                if (swfEntry.getValue() instanceof TomlTable) {
                                    SwfSpecificCustomConfiguration swfSpecificCustomConfig = new SwfSpecificCustomConfiguration();
                                    Map<String, String> swfSpecificCustomConfigMap = swfSpecificCustomConfig.getAllCustomData();
                                    TomlTable configsTable = (TomlTable) swfEntry.getValue();

                                    for (Map.Entry<String, Object> customDataEntry : configsTable.entrySet()) {
                                        if (customDataEntry.getValue() instanceof String) {
                                            swfSpecificCustomConfigMap.put(customDataEntry.getKey(), (String) customDataEntry.getValue());
                                        }
                                    }
                                    swfSpecificCustomConfigs.put(swfKey, swfSpecificCustomConfig);
                                }
                            }
                            value = swfSpecificCustomConfigs;
                        }
                        break;
                    default:
                        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        if (type instanceof Class) {
                            switch (((Class) type).getSimpleName()) {
                                case "Boolean":
                                    value = configurationTable.getBoolean(key);
                                    break;
                                case "Calendar":
                                    OffsetDateTime offsetDateTime = configurationTable.getOffsetDateTime(key);
                                    if (offsetDateTime != null) {
                                        Date date = Date.from(offsetDateTime.toInstant());
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);
                                        value = calendar;
                                    }
                                    break;
                                case "Color":
                                    String colorString = configurationTable.getString(key);
                                    if (colorString != null) {
                                        Pattern colorPattern = Pattern.compile("#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})$");
                                        Matcher m = colorPattern.matcher(colorString);
                                        if (m.matches()) {
                                            value = new Color(Integer.parseInt(m.group(1), 16), Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16));
                                        }
                                    }
                                    break;
                                case "Double":
                                    value = configurationTable.getDouble(key);
                                    break;
                                case "Integer":
                                    Long longValue = configurationTable.getLong(key);
                                    if (longValue != null) {
                                        value = (Integer) (int) (long) longValue;
                                    }
                                    break;
                                case "String":
                                    value = configurationTable.getString(key);
                                    break;
                            }
                        }
                }
                if (value != null) {
                    result.put(name, value);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Cannot load TOML configuration", ex);
            } catch (Exception ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Exception during loading TOML configuration", ex);
            }
        }
        Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.FINE, "TOML file loaded.");
        return result;
    }

    private static List<String> wordWrap(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() + 1 > maxLineLength) {
                lines.add(line.toString().trim());
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }

        if (!line.toString().isEmpty()) {
            lines.add(line.toString().trim());
        }

        return lines;
    }

    private static String stringOfChar(char c, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static String comment(String comment) {
        return "# " + String.join(System.lineSeparator() + "# ", wordWrap(comment, 80 - 2));
    }

    @Override
    public void saveToFile(String file) {
        saveToFile(file, null, null);
    }

    public void saveToFile(String file, Boolean showComments, Boolean modifiedOnly) {
        if (new File(file).exists() && (showComments == null || modifiedOnly == null)) {
            TomlParseResult tomlResult;
            try {
                tomlResult = Toml.parse(Paths.get(file));
                TomlTable metaTable = tomlResult.getTable("meta");
                if (metaTable != null) {
                    if (showComments == null) {
                        showComments = metaTable.getBoolean("showComments") == Boolean.TRUE;
                    }
                    if (modifiedOnly == null) {
                        modifiedOnly = metaTable.getBoolean("modifiedOnly") == Boolean.TRUE;
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.WARNING, "Cannot load showComments/modifiedOnly flags from previous TOML file", ex);                
            }
        }
        if (showComments == null) {
            showComments = false;
        }
        if (modifiedOnly == null) {
            modifiedOnly = true;
        }
        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            String header = AppResources.translate("configurationFile").replace("%app%", ApplicationInfo.APPLICATION_NAME);
            String splitter = stringOfChar('-', header.length());
            pw.println("# " + splitter);
            pw.println("# " + header);
            pw.println("# " + splitter);
            pw.println();
            pw.println(comment(AppResources.translate("configurationFile.comment")));
            pw.println();
            pw.println(comment(AppResources.translate("configurationFile.modify")));
            pw.println();
            pw.println(comment(AppResources.translate("configurationFile.meta")));
            pw.println("[meta]");
            pw.println();
            Calendar generatedCalendarValue = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
            sdf.setTimeZone(generatedCalendarValue.getTimeZone());
            String generatedValue = sdf.format(generatedCalendarValue.getTime());
            pw.println(comment(AppResources.translate("configurationFile.meta.saveDate")));
            pw.println("saveDate = " + generatedValue);
            pw.println();
            pw.println(comment(AppResources.translate("configurationFile.meta.appVersion")));
            pw.println("appVersion = \"" + ApplicationInfo.version + "\"");
            pw.println();

            pw.println(comment(AppResources.translate("configurationFile.meta.showComments")));
            pw.println("showComments = " + (showComments ? "true" : "false") + "");
            pw.println();

            pw.println(comment(AppResources.translate("configurationFile.meta.modifiedOnly")));
            pw.println("modifiedOnly = " + (modifiedOnly ? "true" : "false") + "");
            pw.println();

            pw.println(comment(AppResources.translate("configurationFile.configuration")));
            pw.println("[configuration]");
            pw.println();

            for (Map.Entry<String, Field> entry : Configuration.getConfigurationFields(false, true).entrySet()) {
                try {
                    String name = entry.getKey();
                    Field field = entry.getValue();
                    ConfigurationItem item = (ConfigurationItem) field.get(null);
                    ConfigurationName nameAnnotation = field.getAnnotation(ConfigurationName.class);
                    if (nameAnnotation != null) {
                        name = nameAnnotation.value();
                    }
                    String key = name;
                    if (key.contains(".")) {
                        key = "\"" + key + "\"";
                    }

                    if (!item.hasValue && modifiedOnly) {
                        continue;
                    }

                    String title = Configuration.getConfigurationTitle(name);
                    String description = Configuration.getConfigurationDescription(name);

                    if (showComments) {
                        if (title != null && !title.isEmpty()) {
                            pw.println("# " + title.replace("\n", "\n# "));
                        }
                        if (description != null && !description.isEmpty()) {
                            List<String> descriptionLines = wordWrap(description, 80 - 4);
                            boolean first = true;
                            for (String descriptionLine : descriptionLines) {
                                pw.println("#" + (first ? " - " : "   ") + descriptionLine);
                                first = false;
                            }
                        }
                        pw.println("#");
                    }
                    Object value = item.get();
                    Object defaultValue = Configuration.getDefaultValue(field);
                    String savedValue = null;
                    String savedDefaultValue = null;
                    StringBuilder sb = new StringBuilder();
                    boolean first = true;

                    String valueType = null;
                    if (value == null) {
                        savedValue = "";
                    } else {
                        switch (name) {
                            case "fontPairingMap":
                                @SuppressWarnings("unchecked") HashMap<String, String> fontPairingMap = (HashMap<String, String>) value;
                                sb.append("{");
                                for (String fontKey : fontPairingMap.keySet()) {
                                    if (!first) {
                                        sb.append(", ");
                                    }
                                    sb.append("\"");
                                    sb.append(Helper.escapeJavaString(fontKey));
                                    sb.append("\" = \"");
                                    sb.append(Helper.escapeJavaString(fontPairingMap.get(fontKey)));
                                    sb.append("\"");
                                    first = false;
                                }
                                sb.append("}");
                                break;
                            case "swfSpecificConfigs":
                                @SuppressWarnings("unchecked") HashMap<String, SwfSpecificConfiguration> swfSpecificConfigs = (HashMap<String, SwfSpecificConfiguration>) value;
                                sb.append("{");
                                for (String swfKey : swfSpecificConfigs.keySet()) {
                                    if (!first) {
                                        sb.append(", ");
                                    }
                                    sb.append("\"");
                                    sb.append(Helper.escapeJavaString(swfKey));
                                    sb.append("\" = {");
                                    SwfSpecificConfiguration swfSpecificConf = swfSpecificConfigs.get(swfKey);
                                    sb.append("fontPairingMap = {");
                                    boolean first2 = true;
                                    for (String fontKey : swfSpecificConf.fontPairingMap.keySet()) {
                                        if (!first2) {
                                            sb.append(", ");
                                        }
                                        sb.append("\"");
                                        sb.append(Helper.escapeJavaString(fontKey));
                                        sb.append("\" = \"");
                                        sb.append(Helper.escapeJavaString(swfSpecificConf.fontPairingMap.get(fontKey)));
                                        sb.append("\"");
                                        first2 = false;
                                    }
                                    sb.append("}, lastSelectedPath = \"");
                                    if (swfSpecificConf.lastSelectedPath != null) {
                                        sb.append(Helper.escapeJavaString(swfSpecificConf.lastSelectedPath));
                                    }
                                    sb.append("\"");
                                    sb.append("}");
                                    first = false;
                                }
                                sb.append("}");
                                savedValue = sb.toString();
                                break;
                            case "swfSpecificCustomConfigs":
                                @SuppressWarnings("unchecked") HashMap<String, SwfSpecificCustomConfiguration> swfSpecificCustomConfigs = (HashMap<String, SwfSpecificCustomConfiguration>) value;
                                sb.append("{");
                                for (String swfKey : swfSpecificCustomConfigs.keySet()) {
                                    if (!first) {
                                        sb.append(", ");
                                    }
                                    sb.append("\"");
                                    sb.append(Helper.escapeJavaString(swfKey));
                                    sb.append("\" = {");
                                    SwfSpecificCustomConfiguration swfSpecificCustomConf = swfSpecificCustomConfigs.get(swfKey);
                                    boolean first2 = true;
                                    for (String customKey : swfSpecificCustomConf.getAllCustomData().keySet()) {
                                        if (!first2) {
                                            sb.append(", ");
                                        }
                                        sb.append("\"");
                                        sb.append(Helper.escapeJavaString(customKey));
                                        sb.append("\" = \"");
                                        sb.append(Helper.escapeJavaString(swfSpecificCustomConf.getAllCustomData().get(customKey)));
                                        sb.append("\"");
                                        first2 = false;
                                    }
                                    sb.append("}");
                                    first = false;
                                }
                                sb.append("}");
                                savedValue = sb.toString();
                                break;
                            default:
                                Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                if (type instanceof Class) {
                                    switch (((Class) type).getSimpleName()) {
                                        case "Boolean":
                                            Boolean booleanValue = (Boolean) value;
                                            savedValue = booleanValue == Boolean.TRUE ? "true" : "false";
                                            savedDefaultValue = defaultValue == Boolean.TRUE ? "true" : "false";
                                            valueType = "Boolean";
                                            break;
                                        case "Calendar":
                                            Calendar calendarValue = (Calendar) value;
                                            sdf.setTimeZone(calendarValue.getTimeZone());
                                            savedValue = sdf.format(calendarValue.getTime());
                                            if (defaultValue != null) {
                                                savedDefaultValue = sdf.format(((Calendar) defaultValue).getTime());
                                            }
                                            valueType = "Calendar";
                                            break;
                                        case "Color":
                                            Color colorValue = (Color) value;
                                            savedValue = "\"" + new RGB(colorValue).toHexRGB() + "\"";
                                            if (defaultValue != null) {
                                                savedDefaultValue = "\"" + new RGB((Color) defaultValue).toHexRGB() + "\"";
                                            }
                                            valueType = "Color";
                                            break;
                                        case "Double":
                                            savedValue = "" + value;
                                            if (defaultValue != null) {
                                                savedDefaultValue = "" + defaultValue;
                                            }
                                            valueType = "Double";
                                            break;
                                        case "Integer":
                                            savedValue = "" + value;
                                            if (defaultValue != null) {
                                                savedDefaultValue = "" + defaultValue;
                                            }
                                            valueType = "Integer";
                                            break;
                                        case "String":
                                            String stringValue = value.toString();
                                            if (stringValue.contains("\\") && !stringValue.matches("^.*[\b\t\n\f\r\"'\\x00-\\x08\\x1A-\\x1F\\x7F].*$")) {
                                                savedValue = "'" + stringValue + "'";
                                            } else {
                                                savedValue = "\"" + Helper.escapeJavaString(stringValue) + "\"";
                                            }
                                            if (defaultValue != null) {
                                                stringValue = defaultValue.toString();
                                                if (stringValue.contains("\\") && !stringValue.matches("^.*[\b\t\n\f\r\"'\\x00-\\x08\\x1A-\\x1F\\x7F].*$")) {
                                                    savedDefaultValue = "'" + stringValue + "'";
                                                } else {
                                                    savedDefaultValue = "\"" + Helper.escapeJavaString(stringValue) + "\"";
                                                }
                                            }
                                            valueType = "String";
                                            break;
                                        default:
                                            String stringOtherValue = value.toString();
                                            savedValue = "\"" + Helper.escapeJavaString(stringOtherValue) + "\"";
                                            if (defaultValue != null) {
                                                savedDefaultValue = "" + "\"" + Helper.escapeJavaString(defaultValue.toString()) + "\"";
                                            }
                                            break;
                                    }
                                }

                        }
                    }

                    if (showComments && valueType != null) {
                        pw.println("#   " + AppResources.translate("valueType") + " " + AppResources.translate("valueType." + valueType));
                    }

                    Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (showComments && type instanceof Class) {
                        Class c = (Class) type;
                        if (c.isEnum()) {
                            @SuppressWarnings("unchecked")
                            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
                            Enum<?>[] values = enumType.getEnumConstants();
                            sb = new StringBuilder();
                            first = true;
                            for (Enum<?> enumValue : values) {
                                if (!first) {
                                    sb.append(", ");
                                }
                                sb.append("\"");
                                sb.append(enumValue.toString());
                                sb.append("\"");
                                first = false;
                            }
                            pw.println("#   " + AppResources.translate("valueType") + " " + enumType.getSimpleName());
                            pw.println("#   " + AppResources.translate("possibleValues") + " " + sb.toString());
                        }
                    }

                    if (showComments && savedDefaultValue != null) {
                        pw.println("#   " + AppResources.translate("defaultValue") + " " + savedDefaultValue);
                    }

                    ConfigurationRemoved removed = field.getAnnotation(ConfigurationRemoved.class);

                    if (showComments && removed != null) {
                        pw.println("# " + AppResources.translate("configuration.removed"));
                    }

                    if (showComments) {
                        pw.println();
                    }
                    if (!item.hasValue || savedValue == null || savedValue.isEmpty()) {
                        pw.print("# ");
                    }
                    pw.print(key + " = ");
                    if (item.hasValue) {
                        pw.println(savedValue);
                    } else {
                        pw.println(savedDefaultValue);
                    }
                    if (showComments) {
                        pw.println();
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Cannot get configuration field to save", ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.SEVERE, "Cannot write TOML configuration", ex);
        } catch (Exception ex) {
            Logger.getLogger(TomlConfigurationStorage.class.getName()).log(Level.SEVERE, "Exception during saving configuration", ex);
        }
    }
}
