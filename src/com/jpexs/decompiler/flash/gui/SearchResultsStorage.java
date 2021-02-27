/*
 *  Copyright (C) 2021 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import com.jpexs.decompiler.flash.search.ActionSearchResult;
import com.jpexs.decompiler.flash.search.ScriptNotFoundException;
import com.jpexs.decompiler.flash.search.ScriptSearchResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class SearchResultsStorage {

    public static final String SEARCH_RESULTS_FILE = "searchresults.bin";

    private static final int SERIAL_VERSION_MAJOR = 2;
    private static final int SERIAL_VERSION_MINOR = 0;

    private static final int DATA_ABC = 1;
    private static final int DATA_ACTION = 2;

    private static String getConfigFile() throws IOException {
        return Configuration.getFFDecHome() + SEARCH_RESULTS_FILE;
    }

    List<String> swfIds = new ArrayList<>();
    List<String> searchedValues = new ArrayList<>();
    List<Boolean> isRegExp = new ArrayList<>();
    List<Boolean> isIgnoreCase = new ArrayList<>();
    List<byte[]> data = new ArrayList<>();
    Map<Integer, List<ScriptSearchResult>> unpackedData = new HashMap<>();
    List<Integer> groups = new ArrayList<>();

    private int currentGroupId = 0;

    public void finishGroup() {
        currentGroupId++;
    }

    public static String getSwfId(SWF swf) {

        SWF s = swf;
        String binaryDataSuffix = "";

        while (s.binaryData != null) {
            binaryDataSuffix += "|binaryData[" + s.binaryData.getCharacterId() + "]";
            s = s.binaryData.getSwf();
        }

        if (s.swfList != null) {
            String fileInsideTitle = s.getFile() == null ? s.getFileTitle() : "";
            if (fileInsideTitle != null && !"".equals(fileInsideTitle)) {
                fileInsideTitle = "|" + fileInsideTitle;
            }
            return s.swfList.sourceInfo.getFile() + fileInsideTitle + binaryDataSuffix;
        }
        return "**NONE**";
    }

    public int getCount() {
        return swfIds.size();
    }

    public String getSearchedValueAt(int index) {
        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                return searchedValues.get(j);
            }
        }
        return null;
    }

    public boolean isIgnoreCaseAt(int index) {
        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                return isIgnoreCase.get(j);
            }
        }
        return false;
    }

    public boolean isRegExpAt(int index) {
        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                return isRegExp.get(j);
            }
        }
        return false;
    }

    public List<Integer> getIndicesForSwf(SWF swf) {
        String swfId = getSwfId(swf);
        List<Integer> res = new ArrayList<>();
        Set<Integer> foundGroups = new LinkedHashSet<>();
        for (int i = 0; i < swfIds.size(); i++) {
            if (swfIds.get(i).equals(swfId)) {
                foundGroups.add(groups.get(i));
            }
        }
        return new ArrayList<>(foundGroups);
    }

    @SuppressWarnings("unchecked")
    public List<ScriptSearchResult> getSearchResultsAt(Set<SWF> allSwfs, int index) {
        if (unpackedData.containsKey(index)) {

            List<ScriptSearchResult> unpacked = unpackedData.get(index);
            List<ScriptSearchResult> res = new ArrayList<>();
            for (ScriptSearchResult sr : unpacked) {
                if (allSwfs.contains(sr.getSWF())) {
                    res.add(sr);
                }
            }

            return res;
        }
        List<ScriptSearchResult> result = new ArrayList<>();

        Map<String, SWF> swfIdToSwf = new HashMap<>();
        for (SWF s : allSwfs) {
            swfIdToSwf.put(getSwfId(s), s);
        }

        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                if (!swfIdToSwf.containsKey(swfIds.get(j))) {
                    continue;
                }
                SWF swf = swfIdToSwf.get(swfIds.get(j));
                byte[] itemData = data.get(j);
                try {
                    ByteArrayInputStream bais1 = new ByteArrayInputStream(itemData);
                    int kind = bais1.read();
                    ObjectInputStream ois = new ObjectInputStream(bais1);
                    List<byte[]> resultData = readByteList(ois);
                    for (int i = 0; i < resultData.size(); i++) {
                        try {
                            ByteArrayInputStream bais = new ByteArrayInputStream(resultData.get(i));

                            if (kind == DATA_ABC) {
                                result.add(new ABCSearchResult(swf, bais));
                            }
                            if (kind == DATA_ACTION) {
                                result.add(new ActionSearchResult(swf, bais));
                            }
                        } catch (ScriptNotFoundException | IOException ex) {
                            ex.printStackTrace();
                            //ignore
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                unpackedData.put(j, result);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        String configFile = getConfigFile();
        if (new File(configFile).exists()) {
            try (FileInputStream fis = new FileInputStream(configFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                int major = ois.read();
                ois.read(); // minor
                if (major != SERIAL_VERSION_MAJOR) { //incompatible version
                    return;
                }
                swfIds = (List<String>) ois.readObject();
                searchedValues = (List<String>) ois.readObject();
                isIgnoreCase = (List<Boolean>) ois.readObject();
                isRegExp = (List<Boolean>) ois.readObject();
                groups = (List<Integer>) ois.readObject();
                data = readByteList(ois);
                int maxgroup = -1;
                for (int g : groups) {
                    if (g > maxgroup) {
                        maxgroup = g;
                    }
                }
                currentGroupId = maxgroup + 1;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void save() throws IOException {
        String configFile = getConfigFile();
        try (FileOutputStream fos = new FileOutputStream(configFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.write(SERIAL_VERSION_MAJOR);
            oos.write(SERIAL_VERSION_MINOR);
            oos.writeObject(swfIds);
            oos.writeObject(searchedValues);
            oos.writeObject(isIgnoreCase);
            oos.writeObject(isRegExp);
            oos.writeObject(groups);
            writeByteList(oos, data);
        }
    }

    public void addABCResults(SWF swf, String searchedString, boolean ignoreCase, boolean regExp, List<ABCSearchResult> results) {
        swfIds.add(getSwfId(swf));
        searchedValues.add(searchedString);
        isIgnoreCase.add(ignoreCase);
        isRegExp.add(regExp);
        groups.add(currentGroupId);
        unpackedData.put(data.size(), new ArrayList<>(results));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DATA_ABC);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            List<byte[]> resultData = new ArrayList<>();
            for (ABCSearchResult res : results) {
                ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
                res.save(resultBaos);
                resultData.add(resultBaos.toByteArray());
            }
            writeByteList(oos, resultData);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
        }

        data.add(baos.toByteArray());

    }

    public void addActionResults(SWF swf, String searchedString, boolean ignoreCase, boolean regExp, List<ActionSearchResult> results) {
        swfIds.add(getSwfId(swf));
        searchedValues.add(searchedString);
        isIgnoreCase.add(ignoreCase);
        isRegExp.add(regExp);
        groups.add(currentGroupId);
        unpackedData.put(data.size(), new ArrayList<>(results));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DATA_ACTION);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            List<byte[]> resultData = new ArrayList<>();
            for (ActionSearchResult res : results) {
                ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
                res.save(resultBaos);
                resultData.add(resultBaos.toByteArray());
            }
            writeByteList(oos, resultData);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        data.add(baos.toByteArray());
    }

    public void clear() {
        swfIds.clear();
        searchedValues.clear();
        isIgnoreCase.clear();
        isRegExp.clear();
        groups.clear();
        data.clear();
        unpackedData.clear();
    }

    private static void writeByteList(ObjectOutputStream os, List<byte[]> data) throws IOException {
        os.writeInt(data.size());
        for (byte[] d : data) {
            os.writeInt(d.length);
            os.write(d);
        }
    }

    private static List<byte[]> readByteList(ObjectInputStream ois) throws IOException {
        List<byte[]> ret = new ArrayList<>();
        int cnt = ois.readInt();
        for (int i = 0; i < cnt; i++) {
            int len = ois.readInt();
            byte buf[] = new byte[len];
            ois.readFully(buf);
            ret.add(buf);
        }
        return ret;
    }

    public void destroySwf(SWF swf) {
        String swfId = getSwfId(swf);
        for (int i = 0; i < swfIds.size(); i++) {
            if (swfIds.get(i).equals(swfId)) {
                unpackedData.remove(i);
            }
        }
    }
}
