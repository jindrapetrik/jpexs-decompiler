/*
 *  Copyright (C) 2010-2026 JPEXS
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates CHANGELOG.md file. Adds links to issue numbers - brackets, adds links
 * to versions
 *
 * @author JPEXS
 */
public class ChangelogUpdater {

    private static final String GITHUB_ADDRESS = "https://github.com/jindrapetrik/jpexs-decompiler/";
    private static final String ISSUE_TRACKER_ADDRESS = "https://www.free-decompiler.com/flash/issues/";

    private static final String PR_URL_PREFIX = GITHUB_ADDRESS + "pull/";

    private static final String CHANGELOG_FILENAME = "CHANGELOG.md";

    private static String readFile(String file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[4096];
            int cnt;
            while ((cnt = fis.read(buf)) > 0) {
                baos.write(buf, 0, cnt);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChangelogUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            return new String(baos.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ChangelogUpdater.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String changeLog = readFile(CHANGELOG_FILENAME);
        changeLog = changeLog.replaceAll("\\[[^\\]]+\\]: [^\\r\\n]+\\r\\n", "");

        changeLog = changeLog.replaceAll("\\[#([0-9]+)\\]", "#$1");
        changeLog = changeLog.replaceAll("#([0-9]+)", "[#$1]");

        changeLog = changeLog.replaceAll("\\[PR([0-9]+)\\]", "PR$1");
        changeLog = changeLog.replaceAll("PR([0-9]+)", "[PR$1]");

        Pattern issuePattern = Pattern.compile("\\[#([0-9]+)\\]");

        Matcher issueMatcher = issuePattern.matcher(changeLog);
        Set<Integer> issues = new LinkedHashSet<>();
        while (issueMatcher.find()) {
            issues.add(Integer.parseInt(issueMatcher.group(1)));
        }

        Pattern prPattern = Pattern.compile("PR([0-9]+)");
        Matcher prMatcher = prPattern.matcher(changeLog);
        Set<Integer> prs = new LinkedHashSet<>();
        while (prMatcher.find()) {
            prs.add(Integer.parseInt(prMatcher.group(1)));
        }

        Pattern headerPattern = Pattern.compile("## \\[([^\\]]+)\\]");

        Matcher headerMatcher = headerPattern.matcher(changeLog);
        List<String> versionNames = new ArrayList<>();
        List<String> tagNames = new ArrayList<>();
        while (headerMatcher.find()) {
            String versionName = headerMatcher.group(1);
            Pattern updatePattern = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+) update ([0-9]+)");
            Matcher updateMatcher = updatePattern.matcher(versionName);
            String tagName;
            if (versionName.matches("[0-9]+\\.[0-9]+\\.[0-9]+")) {
                tagName = "version" + versionName;
            } else if (updateMatcher.matches()) {
                tagName = "version" + updateMatcher.group(1) + "u" + updateMatcher.group(2);
            } else if (versionName.equals("Unreleased")) {
                tagName = "dev";
            } else {
                tagName = versionName.replace(" ", "");
            }
            versionNames.add(versionName);
            tagNames.add(tagName);
        }

        for (int i = 0; i < versionNames.size(); i++) {
            String versionName = versionNames.get(i);
            String tagName = tagNames.get(i);
            String nextTagName = i == versionNames.size() - 1 ? null : tagNames.get(i + 1);

            if (nextTagName == null) {
                changeLog += "[" + versionName + "]: " + GITHUB_ADDRESS + "releases/tag/" + tagName + "\r\n";
            } else {
                changeLog += "[" + versionName + "]: " + GITHUB_ADDRESS + "compare/" + nextTagName + "..." + tagName + "\r\n";
            }
        }

        for (int issue : issues) {
            changeLog += "[#" + issue + "]: " + ISSUE_TRACKER_ADDRESS + issue + "\r\n";
        }
        for (int pr : prs) {
            changeLog += "[PR" + pr + "]: " + PR_URL_PREFIX + pr + "\r\n";
        }

        try (FileOutputStream fos = new FileOutputStream(CHANGELOG_FILENAME)) {
            fos.write(changeLog.getBytes("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(ChangelogUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
