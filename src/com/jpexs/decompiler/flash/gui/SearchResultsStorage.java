/*
 * Copyright (C) 2021 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import com.jpexs.decompiler.flash.search.ActionSearchResult;
import com.jpexs.decompiler.flash.search.ScriptNotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class SearchResultsStorage {

    public static final String SEARCH_RESULTS_FILE = "search_results.bin";

    private static String getConfigFile() throws IOException {
        return Configuration.getFFDecHome() + SEARCH_RESULTS_FILE;
    }

    List<String> swfIds = new ArrayList<>();
    List<String> searchedValues = new ArrayList<>();
    List<Boolean> isRegExp = new ArrayList<>();
    List<Boolean> isIgnoreCase = new ArrayList<>();
    List<byte[]> data = new ArrayList<>();
    Map<Integer, Object> unpackedData = new HashMap<>();

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
        return searchedValues.get(index);
    }

    public boolean isIgnoreCaseAt(int index) {
        return isIgnoreCase.get(index);
    }

    public boolean isRegExpAt(int index) {
        return isRegExp.get(index);
    }

    public List<Integer> getIndicesForSwf(SWF swf) {
        String swfId = getSwfId(swf);
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < swfIds.size(); i++) {
            if (swfIds.get(i).equals(swfId)) {
                res.add(i);
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<ABCSearchResult> getAbcSearchResultsAt(SWF swf, int index) {
        if (unpackedData.containsKey(index)) {
            return (List<ABCSearchResult>) unpackedData.get(index);
        }
        List<ABCSearchResult> result = new ArrayList<>();
        byte[] itemData = data.get(index);

        try {
            DataInputStream dais = new DataInputStream(new ByteArrayInputStream(itemData));
            int cnt = dais.readInt();
            for (int i = 0; i < cnt; i++) {
                try {
                    result.add(new ABCSearchResult(swf, dais));
                } catch (ScriptNotFoundException ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        unpackedData.put(index, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ActionSearchResult> getActionSearchResultsAt(SWF swf, int index) {
        if (unpackedData.containsKey(index)) {
            return (List<ActionSearchResult>) unpackedData.get(index);
        }
        List<ActionSearchResult> result = new ArrayList<>();
        byte[] itemData = data.get(index);

        try {
            DataInputStream dais = new DataInputStream(new ByteArrayInputStream(itemData));
            int cnt = dais.readInt();
            for (int i = 0; i < cnt; i++) {
                try {
                    result.add(new ActionSearchResult(swf, dais));
                } catch (ScriptNotFoundException ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        unpackedData.put(index, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        String configFile = getConfigFile();
        if (new File(configFile).exists()) {
            try (FileInputStream fis = new FileInputStream(configFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                swfIds = (List<String>) ois.readObject();
                searchedValues = (List<String>) ois.readObject();
                isIgnoreCase = (List<Boolean>) ois.readObject();
                isRegExp = (List<Boolean>) ois.readObject();
                data = (List<byte[]>) ois.readObject();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SearchResultsStorage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void save() throws IOException {
        String configFile = getConfigFile();
        try (FileOutputStream fos = new FileOutputStream(configFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(swfIds);
            oos.writeObject(searchedValues);
            oos.writeObject(isIgnoreCase);
            oos.writeObject(isRegExp);
            oos.writeObject(data);
        }
    }

    public void addABCResults(SWF swf, String searchedString, boolean ignoreCase, boolean regExp, List<ABCSearchResult> results) {
        swfIds.add(getSwfId(swf));
        searchedValues.add(searchedString);
        isIgnoreCase.add(ignoreCase);
        isRegExp.add(regExp);
        unpackedData.put(data.size(), new ArrayList<>(results));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        try {
            daos.writeInt(results.size());
            for (ABCSearchResult res : results) {
                res.save(daos);
            }
            daos.flush();
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
        unpackedData.put(data.size(), new ArrayList<>(results));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        try {
            daos.writeInt(results.size());
            for (ActionSearchResult res : results) {
                res.save(daos);
            }
            daos.flush();
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
        unpackedData.clear();
    }
}
