/*
 * Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.console;

import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class CommandLineHelp {
    private static Map<String, List<Command>> commands = new LinkedHashMap<>();
    private static Map<String, Option> preOptions = new LinkedHashMap<>();
    
    private static class Command {
        String name;
        String customSynopsis;
        String arguments;
        String header;
        String description;

        public Command(String name, String customSynopsis, String arguments, String header, String description) {
            this.name = name;
            this.customSynopsis = customSynopsis;
            this.arguments = arguments;
            this.header = header;
            this.description = description;
        }

        @Override
        public String toString() {
            if (customSynopsis != null) {
                return customSynopsis + "\r\n" + indent(header) + "\r\n" + indent(description);
            } else {
                return name + " " + arguments + "\r\n" + indent(header) + "\r\n" + indent(description);
            }
        }                
    }
    
    private static String indent(String text) {
        String indent = "    ";
        return indent + rtrim(text.replace("\r\n", "\r\n" + indent));
    }
    
    private static String rtrim(String text) {
        return text.replaceAll("\\s+$","");
    }
    
    private static class Option {
        String name;
        String arguments;
        String appliesTo;
        String description;        

        public Option(String name, String arguments, String appliesTo, String description) {
            this.name = name;
            this.arguments = arguments;
            this.appliesTo = appliesTo;
            this.description = description;
        }                
    }
    
    private static void parse() {
        InputStream is = CommandLineHelp.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/console/help.txt");            
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is, Utf8Helper.charset))) {
            String s;
            boolean commandsSection = false;
            boolean optionsSection = false;
            boolean headerSection = false;
            String itemName = null;
            String arguments = null;
            String customSynopsis = null;
            StringBuffer descriptionBuffer = new StringBuffer();
            String header = null;
            while ((s = br.readLine()) != null) {
                s = rtrim(s);
                if (s.startsWith("Commands:")) {
                    commandsSection = true;
                    continue;
                }
                if (s.startsWith("Pre-options:")) {
                    optionsSection = true;
                    commandsSection = false;
                    continue;
                }
                
                if (commandsSection) {
                    if (s.isEmpty()) {
                        if (itemName != null) {
                            if (!commands.containsKey(itemName)) {
                                commands.put(itemName, new ArrayList<>());
                            }
                            commands.get(itemName).add(new Command(itemName, customSynopsis, arguments, header, descriptionBuffer.toString()));
                        }
                    } else if (s.startsWith("<")) {
                        itemName = "main";
                        arguments = null;
                        customSynopsis = s;
                        descriptionBuffer.setLength(0);
                        headerSection = true;
                        header = null;
                    } else if (s.startsWith("-help")) {
                        itemName = "-help";
                        arguments = null;
                        customSynopsis = s;
                        descriptionBuffer.setLength(0);
                        headerSection = true;
                        header = null;
                    } else if (s.startsWith("-")) {
                        if (s.contains(" ")) {
                            itemName = s.substring(0, s.indexOf(" "));
                            arguments = s.substring(s.indexOf(" ") + 1);
                        } else {
                            itemName = s;
                            arguments = "";
                        }
                        customSynopsis = null;
                        descriptionBuffer.setLength(0);
                        headerSection = true;
                        header = null;
                    } else if (s.startsWith("    ")) {
                        if (headerSection) {
                            headerSection = false;
                            header = s.substring(4);
                        } else {
                            descriptionBuffer.append(s.substring(4)).append("\r\n");
                        }
                    }
                }
                
                if (optionsSection) {
                    //TODO...
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineHelp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        parse();
        for (String name : commands.keySet()) {
            List<Command> list = commands.get(name);
            for (Command c : list) {
                System.out.println(c);
            }
        }
    }
}
