/*
 *  Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.console;

import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fusesource.jansi.AnsiConsole;

/**
 * @author JPEXS
 */
public class CommandLineHelp {

    private static Map<String, List<Command>> commands = new LinkedHashMap<>();
    private static Map<String, Option> preOptions = new LinkedHashMap<>();
    private static String preface = null;
    private static Map<String, String> aliasMap = new HashMap<>();

    static {
        parse();
    }

    private static String getPreface(String command, String arguments) {
        String ret = preface;
        if (command != null) {
            ret = ret.replace("[COMMAND]", command + "\\\r\n       " + arguments);
        }
        ret = ret.replace("Usage:", "@|bold,underline Usage|@:");
        ret = ret.replace("Executable:", "@|bold,underline Executable|@:");
        return ret;
    }

    private static String hilight(String str) {
        str = str.replaceAll("<[a-zA-Z_0-9]+>", "@|italic $0|@");
        str = str.replaceAll("(^|[ \\[\\(\\|\n])(-[a-zA-Z_0-9]+)", "$1@|yellow $2|@");
        str = str.replaceAll("@\\|yellow (-[a-zA-Z_0-9]+)\\|@ command", "@|bold $1|@ command");

        return str;
    }

    private static String indentArguments(String arguments, String commandName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandName.length(); i++) {
            sb.append(" ");
        }
        sb.append(" ");
        return arguments.replace("\r\n", "\r\n" + sb.toString());
    }

    private static class Command {

        String name;
        String customSynopsis;
        String arguments;
        String header;
        String description;
        List<String> aliases;

        List<Option> preOptions = new ArrayList<>();

        public Command(String name, String customSynopsis, String arguments, String header, String description, List<String> aliases) {
            this.name = name;
            this.customSynopsis = customSynopsis;
            this.arguments = arguments;
            this.header = header;
            this.description = description;
            this.aliases = aliases;
        }

        @Override
        public String toString() {
            return name + " " + header;
        }

        public String getHeader(int padWidth) {
            StringBuilder sb = new StringBuilder();
            StringBuilder sbRaw = new StringBuilder();
            sb.append("@|bold ").append(name).append("|@");
            sbRaw.append(name);
            if (!aliases.isEmpty()) {
                List<String> aliasesBold = new ArrayList<>();
                for (String alias : aliases) {
                    aliasesBold.add("@|bold " + alias + "|@");
                }
                sb.append(" | ").append(String.join(" | ", aliasesBold));
                sbRaw.append(" | ").append(String.join(" | ", aliases));
            }
            while (sbRaw.length() < padWidth - 2) {
                sb.append(" ");
                sbRaw.append(" ");
            }
            sb.append("  ");
            sbRaw.append("  ");

            if (sbRaw.length() > padWidth) {
                sb.append("\r\n");
                for (int i = 0; i < padWidth; i++) {
                    sb.append(" ");
                }
            }
            sb.append(header);

            return sb.toString();
        }

        public String getHelp(boolean includePreOptions, boolean includePreface) {
            StringBuilder sb = new StringBuilder();

            if (includePreface) {
                sb.append(header).append("\r\n");
            }

            String synopsis;
            String hilightedArguments = "";
            String hilightedName = "";
            if (customSynopsis != null) {
                synopsis = customSynopsis;
            } else {
                hilightedName = "@|bold " + name + "|@ ";
                hilightedArguments = hilight(indentArguments(arguments, name));
                synopsis = hilightedName + hilightedArguments;
            }
            if (includePreface) {
                sb.append(getPreface(hilightedName, hilight(indentArguments(arguments, "Usage:"))));
                if (!aliases.isEmpty()) {
                    sb.append("\r\n\r\n");
                    sb.append("@|bold,underline Aliases|@:");
                    for (String alias : aliases) {
                        sb.append("\r\n@|bold ").append(alias).append("|@");
                    }
                }
            } else {
                sb.append(synopsis);
                if (!aliases.isEmpty()) {
                    for (String alias : aliases) {
                        sb.append("\r\nalias @|bold ").append(alias).append("|@");
                    }
                }
            }
            if (!includePreface) {
                sb.append("\r\n");
                sb.append(indent(header));
            }
            if (!description.isEmpty()) {
                sb.append("\r\n");
                if (includePreface) {
                    sb.append("\r\n@|bold,underline Description|@:\r\n");
                }
                sb.append(indent(hilight(description)));
            }
            if (includePreOptions && !preOptions.isEmpty()) {
                sb.append("\r\n");
                sb.append("\r\n");
                sb.append("@|bold,underline Pre-options|@:");
                for (Option opt : preOptions) {
                    sb.append("\r\n");
                    sb.append(opt.getHelp(false)).append("\r\n");
                }
            }
            return sb.toString();
        }

        public void addPreOption(Option option) {
            preOptions.add(option);
        }
    }

    private static String indent(String text) {
        String indent = "    ";
        return indent + rtrim(text.replace("\r\n", "\r\n" + indent));
    }

    private static String rtrim(String text) {
        return text.replaceAll("\\s+$", "");
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

        public String getHelp(boolean getAppliesTos) {
            StringBuilder sb = new StringBuilder();
            sb.append("@|yellow ").append(name).append("|@").append(" ").append(hilight(arguments)).append("\r\n");
            if (getAppliesTos) {
                sb.append(indent("Applies to: " + appliesTo.replaceAll("-[a-zA-Z_0-9]+", "@|bold $0|@"))).append("\r\n");
            }
            sb.append(indent(hilight(description)));

            return sb.toString();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static void parse() {
        InputStream is = CommandLineHelp.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/console/help.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Utf8Helper.charset))) {
            String s;
            boolean commandsSection = false;
            boolean optionsSection = false;
            boolean headerSection = false;
            boolean appliesToSection = false;
            boolean prefaceSection = true;
            boolean argumentsContinuation = false;
            String itemName = null;
            String arguments = null;
            String customSynopsis = null;
            StringBuffer descriptionBuffer = new StringBuffer();
            StringBuilder prefaceBuilder = new StringBuilder();
            String header = null;
            String appliesTo = null;
            List<String> aliases = new ArrayList<>();
            while ((s = br.readLine()) != null) {
                s = rtrim(s);
                if (s.startsWith("Commands:")) {
                    commandsSection = true;
                    prefaceSection = false;
                    preface = prefaceBuilder.toString().trim();
                    continue;
                }
                if (s.startsWith("Pre-options:")) {
                    optionsSection = true;
                    commandsSection = false;
                    continue;
                }

                if (prefaceSection) {
                    prefaceBuilder.append(s).append("\r\n");
                }

                if (commandsSection) {
                    if (s.isEmpty()) {
                        if (itemName != null) {
                            if (!commands.containsKey(itemName.toLowerCase())) {
                                commands.put(itemName.toLowerCase(), new ArrayList<>());
                            }
                            commands.get(itemName.toLowerCase()).add(new Command(itemName, customSynopsis, arguments, header, descriptionBuffer.toString(), aliases));
                            for (String alias : aliases) {
                                aliasMap.put(alias.toLowerCase(), itemName.toLowerCase());
                            }
                            itemName = null;
                            argumentsContinuation = false;
                        }
                    } else if (s.startsWith("<")) {
                        itemName = "main";
                        arguments = null;
                        customSynopsis = s;
                        descriptionBuffer.setLength(0);
                        headerSection = true;
                        header = null;
                        aliases = new ArrayList<>();
                    } else if (s.startsWith("alias ")) {
                        aliases.add(s.substring("alias ".length()));
                    } else if (argumentsContinuation) {
                        arguments += "\r\n" + s.trim();
                        argumentsContinuation = arguments.endsWith("\\");
                    } else if (s.startsWith("-")) {
                        aliases = new ArrayList<>();
                        if (s.contains(" ")) {
                            itemName = s.substring(0, s.indexOf(" "));
                            arguments = s.substring(s.indexOf(" ") + 1);
                        } else {
                            itemName = s;
                            arguments = "";
                        }
                        argumentsContinuation = arguments.endsWith("\\");
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
                    if (s.isEmpty()) {
                        if (itemName != null) {
                            Option opt = new Option(itemName, arguments, appliesTo, descriptionBuffer.toString());
                            String[] appliesToParts = appliesTo.split(", ");
                            for (String a : appliesToParts) {
                                if ("*".equals(a)) {
                                    for (String ckey : commands.keySet()) {
                                        for (Command c : commands.get(ckey)) {
                                            c.addPreOption(opt);
                                        }
                                    }
                                } else {
                                    if (!commands.containsKey(a.toLowerCase())) {
                                        throw new RuntimeException("Invalid command in applies to: " + a);
                                    }
                                    for (Command c : commands.get(a.toLowerCase())) {
                                        c.addPreOption(opt);
                                    }
                                }
                            }
                            preOptions.put(itemName.toLowerCase(), opt);
                            itemName = null;
                        }
                    } else if (s.startsWith("-")) {
                        if (s.contains(" ")) {
                            itemName = s.substring(0, s.indexOf(" "));
                            arguments = s.substring(s.indexOf(" ") + 1);
                        } else {
                            itemName = s;
                            arguments = "";
                        }
                        descriptionBuffer.setLength(0);
                        appliesToSection = true;
                    } else if (s.startsWith("    ")) {
                        if (appliesToSection && s.startsWith("    Applies to: ")) {
                            appliesTo = s.substring("    Applies to: ".length());
                            appliesToSection = false;
                        } else {
                            descriptionBuffer.append(s.substring(4)).append("\r\n");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineHelp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printCmdLineUsage(PrintStream out, String filter) {
        AnsiConsole.systemInstall();
        if (filter == null) {
            out.println(getPreface(null, null));
            out.println();

            out.println("@|bold,underline Global pre-options|@:");
            for (Option opt : preOptions.values()) {
                if ("*".equals(opt.appliesTo)) {
                    out.println(opt.getHelp(false));
                }
            }
            out.println();

            out.println("@|bold,underline Commands:|@");
            for (String name : commands.keySet()) {
                if ("main".equals(name)) {
                    continue;
                }
                List<Command> list = commands.get(name);
                for (Command c : list) {
                    out.println(c.getHeader(20));
                }
            }
        } else if ("all".equals(filter)) {
            out.println(getPreface(null, null));
            out.println();

            out.println("@|bold,underline Commands:|@");
            for (String name : commands.keySet()) {
                if ("main".equals(name)) {
                    continue;
                }
                List<Command> list = commands.get(name);
                for (Command c : list) {
                    out.println(c.getHelp(false, false));
                    out.println();
                }
            }

            out.println("@|bold,underline Pre-options|@:");
            for (Option opt : preOptions.values()) {
                out.println(opt.getHelp(true));
            }
            out.println();
        } else {

            if (aliasMap.containsKey("-" + filter)) {
                filter = aliasMap.get("-" + filter).substring(1);
            } else if (aliasMap.containsKey(filter)) {
                filter = aliasMap.get(filter).substring(1);
            }

            if (commands.containsKey("-" + filter.toLowerCase())) {
                boolean first = true;
                for (Command c : commands.get("-" + filter.toLowerCase())) {
                    if (!first) {
                        out.println("@|bold,underline Alternative|@:\r\n");
                    }
                    out.println(c.getHelp(first, first));
                    first = false;
                }
            } else if (preOptions.containsKey("-" + filter.toLowerCase())) {
                out.println(getPreface(null, null));
                out.println();
                out.println("@|bold,underline Pre-option|@:");
                out.println(preOptions.get("-" + filter.toLowerCase()).getHelp(true));
            } else {
                out.println("@|red Command or option with name -" + filter + " NOT FOUND.|@");
            }
        }
        AnsiConsole.systemUninstall();
    }

    public static void main(String[] args) {
        printCmdLineUsage(System.out, "export");
    }
}
