/*
 *  Copyright (C) 2021-2024 JPEXS
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
import com.jpexs.decompiler.flash.treeitems.Openable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    List<String> openableIds = new ArrayList<>();
    List<String> searchedValues = new ArrayList<>();
    List<Boolean> isRegExp = new ArrayList<>();
    List<Boolean> isIgnoreCase = new ArrayList<>();
    List<byte[]> data = new ArrayList<>();
    List<List<ScriptSearchResult>> unpackedData = new ArrayList<>();
    List<Integer> groups = new ArrayList<>();

    private int currentGroupId = 0;

    public synchronized void finishGroup() {
        currentGroupId++;
    }

    public static String getOpenableId(Openable swf) {

        Openable s = swf;
        String binaryDataSuffix = "";

        while ((s instanceof SWF) && ((SWF) s).binaryData != null) {
            binaryDataSuffix += "|" + ((SWF) s).binaryData.getStoragesPathIdentifier();
            s = ((SWF) s).binaryData.getSwf();
        }

        if (s.getOpenableList() != null) {
            String fileInsideTitle = s.getFile() == null ? s.getFileTitle() : "";
            if (fileInsideTitle != null && !"".equals(fileInsideTitle)) {
                fileInsideTitle = "|" + fileInsideTitle;
            }
            return s.getOpenableList().sourceInfo.getFile() + fileInsideTitle + binaryDataSuffix;
        }
        return "**NONE**";
    }

    public synchronized int getCount() {
        return openableIds.size();
    }

    public synchronized String getSearchedValueAt(int index) {
        for (int j = 0; j < groups.size(); j++) {
            if (groups.get(j) == index) {
                return searchedValues.get(j);
            }
        }
        return null;
    }

    public synchronized boolean isIgnoreCaseAt(int index) {
        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                return isIgnoreCase.get(j);
            }
        }
        return false;
    }

    public synchronized boolean isRegExpAt(int index) {
        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                return isRegExp.get(j);
            }
        }
        return false;
    }

    public synchronized List<Integer> getIndicesForOpenable(Openable swf) {
        String swfId = getOpenableId(swf);
        List<Integer> res = new ArrayList<>();
        Set<Integer> foundGroups = new LinkedHashSet<>();
        for (int i = 0; i < openableIds.size(); i++) {
            if (openableIds.get(i).equals(swfId)) {
                foundGroups.add(groups.get(i));
            }
        }
        return new ArrayList<>(foundGroups);
    }

    @SuppressWarnings("unchecked")
    public synchronized List<ScriptSearchResult> getSearchResultsAt(Set<Openable> allOpenables, int index) {
        List<ScriptSearchResult> result = new ArrayList<>();

        Map<String, Openable> openableIdToOpenable = new HashMap<>();
        for (Openable o : allOpenables) {
            openableIdToOpenable.put(getOpenableId(o), o);
        }

        for (int j = 0; j < data.size(); j++) {
            if (groups.get(j) == index) {
                if (!openableIdToOpenable.containsKey(openableIds.get(j))) {
                    continue;
                }
                if (unpackedData.get(j) != null) {
                    List<ScriptSearchResult> unpacked = unpackedData.get(j);
                    for (ScriptSearchResult sr : unpacked) {
                        if (allOpenables.contains(sr.getOpenable())) {
                            result.add(sr);
                        }
                    }
                    continue;
                }
                Openable openable = openableIdToOpenable.get(openableIds.get(j));
                byte[] itemData = data.get(j);
                List<ScriptSearchResult> currentResults = new ArrayList<>();
                try {
                    ByteArrayInputStream bais1 = new ByteArrayInputStream(itemData);
                    int kind = bais1.read();
                    ObjectInputStream ois = new ObjectInputStream(bais1);
                    List<byte[]> resultData = readByteList(ois);
                    for (int i = 0; i < resultData.size(); i++) {
                        try {
                            ByteArrayInputStream bais = new ByteArrayInputStream(resultData.get(i));

                            if (kind == DATA_ABC) {
                                currentResults.add(new ABCSearchResult(openable, bais));
                            }
                            if (kind == DATA_ACTION) {
                                currentResults.add(new ActionSearchResult((SWF) openable, bais));
                            }
                        } catch (ScriptNotFoundException | IOException ex) {
                            ex.printStackTrace();
                            //ignore
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                unpackedData.set(j, currentResults);
                result.addAll(currentResults);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() throws IOException {
        String configFile = getConfigFile();
        if (new File(configFile).exists()) {
            try (FileInputStream fis = new FileInputStream(configFile); ObjectInputStream ois = new ObjectInputStream(fis)) {
                int major = ois.read();
                ois.read(); // minor
                if (major != SERIAL_VERSION_MAJOR) { //incompatible version
                    return;
                }
                openableIds = (List<String>) ois.readObject();
                searchedValues = (List<String>) ois.readObject();
                isIgnoreCase = (List<Boolean>) ois.readObject();
                isRegExp = (List<Boolean>) ois.readObject();
                groups = (List<Integer>) ois.readObject();
                data = readByteList(ois);

                int size = openableIds.size();
                if (searchedValues.size() != size
                        || isIgnoreCase.size() != size
                        || isRegExp.size() != size
                        || groups.size() != size
                        || data.size() != size) {
                    //something wrong, do not load this state
                    openableIds.clear();
                    searchedValues.clear();
                    isIgnoreCase.clear();
                    isRegExp.clear();
                    groups.clear();
                    data.clear();
                }

                int maxgroup = -1;
                for (int g : groups) {
                    if (g > maxgroup) {
                        maxgroup = g;
                    }
                }
                currentGroupId = maxgroup + 1;

                unpackedData = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    unpackedData.add(null);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException iex) {
                openableIds.clear();
                searchedValues.clear();
                isIgnoreCase.clear();
                isRegExp.clear();
                groups.clear();
                data.clear();
                unpackedData.clear();
                currentGroupId = 0;
                throw iex;
            }
        }
    }

    public synchronized void save() throws IOException {
        int size = openableIds.size();
        if (searchedValues.size() != size
                || isIgnoreCase.size() != size
                || isRegExp.size() != size
                || groups.size() != size
                || data.size() != size) {
            //something wrong, do not save this state
            return;
        }
        String configFile = getConfigFile();
        try (FileOutputStream fos = new FileOutputStream(configFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.write(SERIAL_VERSION_MAJOR);
            oos.write(SERIAL_VERSION_MINOR);
            oos.writeObject(openableIds);
            oos.writeObject(searchedValues);
            oos.writeObject(isIgnoreCase);
            oos.writeObject(isRegExp);
            oos.writeObject(groups);
            writeByteList(oos, data);
        }
    }

    public synchronized void addABCResults(Openable openable, String searchedString, boolean ignoreCase, boolean regExp, List<ABCSearchResult> results) {
        openableIds.add(getOpenableId(openable));
        searchedValues.add(searchedString);
        isIgnoreCase.add(ignoreCase);
        isRegExp.add(regExp);
        groups.add(currentGroupId);
        unpackedData.add(new ArrayList<>(results));
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

    public synchronized void addActionResults(SWF swf, String searchedString, boolean ignoreCase, boolean regExp, List<ActionSearchResult> results) {
        openableIds.add(getOpenableId(swf));
        searchedValues.add(searchedString);
        isIgnoreCase.add(ignoreCase);
        isRegExp.add(regExp);
        groups.add(currentGroupId);
        unpackedData.add(new ArrayList<>(results));
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

    public synchronized void clear() {
        openableIds.clear();
        searchedValues.clear();
        isIgnoreCase.clear();
        isRegExp.clear();
        groups.clear();
        data.clear();
        unpackedData.clear();
    }

    public synchronized void clearForOpenable(Openable openable) {
        String swfId = getOpenableId(openable);
        for (int i = openableIds.size() - 1; i >= 0; i--) {
            if (openableIds.get(i).equals(swfId)) {
                openableIds.remove(i);
                searchedValues.remove(i);
                isIgnoreCase.remove(i);
                isRegExp.remove(i);
                groups.remove(i);
                data.remove(i);
                unpackedData.remove(i);
            }
        }
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
            byte[] buf = new byte[len];
            ois.readFully(buf);
            ret.add(buf);
        }
        return ret;
    }

    public synchronized void destroySwf(SWF swf) {
        String swfId = getOpenableId(swf);
        for (int i = 0; i < openableIds.size(); i++) {
            if (openableIds.get(i).equals(swfId) && unpackedData.size() > i) {
                unpackedData.set(i, null);
            }
        }
    }
}
