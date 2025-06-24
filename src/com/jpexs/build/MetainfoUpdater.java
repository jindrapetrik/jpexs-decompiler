/*
 *  Copyright (C) 2023-2025 JPEXS
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
package com.jpexs.build;

import com.jpexs.helpers.Helper;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Update Flatpak metainfo XML file. Generates metainfo from CHANGELOG.md file.
 * Appends new versions that are not present in metainfo file.
 *
 * @author JPEXS
 */
public class MetainfoUpdater {

    public static final String METAINFO_FILENAME = "resources/com.jpexs.decompiler.flash.metainfo.xml";
    private static final String CHANGELOG_FILENAME = "CHANGELOG.md";

    public static void main(String[] args) throws UnsupportedEncodingException {
        String metainfo = Helper.readTextFile(METAINFO_FILENAME);

        String changeLog = Helper.readTextFile(CHANGELOG_FILENAME);
        String newline = System.lineSeparator();

        Pattern metainfoVersionPattern = Pattern.compile("<release version=\"(?<version>[0-9]+\\.[0-9]+\\.[0-9]+)\" date=\"(?<date>[0-9]+-[0-9]+-[0-9]+)\">");
        Matcher metainfoVersionMatcher = metainfoVersionPattern.matcher(metainfo);
        if (!metainfoVersionMatcher.find()) {
            throw new IllegalArgumentException("No last metainfo version found");
        }
        String latestMetainfoVersion = metainfoVersionMatcher.group("version");

        Pattern changelogVersionPattern = Pattern.compile("## \\[(?<version>[0-9]+\\.[0-9]+\\.[0-9]+)\\] - (?<date>[0-9]+-[0-9]+-[0-9]+)");
        Matcher changelogVersionMatcher = changelogVersionPattern.matcher(changeLog);
        int prevMatchEnd = -1;
        String prevVersion = null;
        String prevDate = null;
        String releases = "";
        while (changelogVersionMatcher.find()) {
            if (prevMatchEnd != -1) {
                if (prevVersion.equals(latestMetainfoVersion)) {
                    break;
                }
                String versionChangelog = changeLog.substring(prevMatchEnd, changelogVersionMatcher.start()).trim();
                String[] parts = (versionChangelog + "\r\n").split("\r?\n");
                String prev = null;
                String description = "";
                String li = "";
                for (String s : parts) {
                    if (s.startsWith("### ")) {
                        if (!li.isEmpty()) {
                            description += "\t\t\t\t\t<li>" + filterLiText(li) + "</li>" + newline;
                            li = "";
                        }
                        if (!description.isEmpty()) {
                            description += "\t\t\t\t</ul>" + newline;
                        }
                        description += "\t\t\t\t<p>" + s.substring(4).trim() + "</p>" + newline;
                        description += "\t\t\t\t<ul>" + newline;
                        continue;
                    }

                    if (s.startsWith("- ")) {
                        if (!li.isEmpty()) {
                            description += "\t\t\t\t\t<li>" + filterLiText(li) + "</li>" + newline;
                        }

                        li = s.trim().substring(2);
                    } else {
                        if (!s.trim().isEmpty()) {
                            li = li + " " + s.trim();
                        }
                    }
                }
                if (!li.isEmpty()) {
                    description += "\t\t\t\t\t<li>" + filterLiText(li) + "</li>" + newline;
                }
                if (!description.isEmpty()) {
                    description += "\t\t\t\t</ul>" + newline;
                }

                String release = "\t\t<release version=\"" + prevVersion + "\" date=\"" + prevDate + "\">" + newline
                        + "\t\t\t<description>" + newline
                        + description
                        + "\t\t\t</description>" + newline
                        + "\t\t</release>" + newline;
                releases += release;
            }

            prevVersion = changelogVersionMatcher.group("version");
            prevDate = changelogVersionMatcher.group("date");
            prevMatchEnd = changelogVersionMatcher.end();
        }

        metainfo = metainfo.replaceAll("<releases>", ("<releases>" + newline + releases).trim());

        Helper.writeFile(METAINFO_FILENAME, metainfo.getBytes("UTF-8"));
    }

    private static String filterLiText(String li) {
        li = li.replaceAll("\\[(#[0-9]+)\\]", "$1");
        li = li.replaceAll("\\[(PR[0-9]+)\\]", "$1");
        li = li.replace("&", "&amp;");
        return li;
    }
}
