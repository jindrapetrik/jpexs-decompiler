/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Traits implements Serializable {

    public Trait[] traits = new Trait[0];

    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) {
        int ret = 0;
        for (Trait t : traits) {
            ret += t.removeTraps(scriptIndex, classIndex, isStatic, abc, path);
        }
        return ret;
    }

    @Override
    public String toString() {
        String s = "";
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) {
                s += "\r\n";
            }
            s += traits[t].toString();
        }
        return s;
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        String s = "";
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) {
                s += "\r\n";
            }
            s += traits[t].toString(abc, fullyQualifiedNames);
        }
        return s;
    }

    private class TraitConvertTask implements Callable<String> {

        Trait trait;
        boolean makePackages;
        String path;
        List<ABCContainerTag> abcTags;
        ABC abc;
        boolean isStatic;
        boolean pcode;
        int scriptIndex;
        int classIndex;
        boolean highlighting;
        List<String> fullyQualifiedNames;
        int traitIndex;
        boolean parallel;

        public TraitConvertTask(Trait trait, boolean makePackages, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlighting, List<String> fullyQualifiedNames, int traitIndex, boolean parallel) {
            this.trait = trait;
            this.makePackages = makePackages;
            this.path = path;
            this.abcTags = abcTags;
            this.abc = abc;
            this.isStatic = isStatic;
            this.pcode = pcode;
            this.scriptIndex = scriptIndex;
            this.classIndex = classIndex;
            this.highlighting = highlighting;
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.traitIndex = traitIndex;
            this.parallel = parallel;
        }

        @Override
        public String call() {
            String plus;
            if (makePackages) {
                plus = trait.convertPackaged(path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, parallel);
            } else {
                plus = trait.convert(path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, parallel);
            }
            if (highlighting) {
                int h = traitIndex;
                if (classIndex != -1) {
                    if (!isStatic) {
                        h = h + abc.class_info[classIndex].static_traits.traits.length;
                    }
                }
                if (trait instanceof TraitClass) {
                    plus = Highlighting.hilighClass(plus, ((TraitClass) trait).class_info);
                } else {
                    plus = Highlighting.hilighTrait(plus, h);
                }
            }
            return plus;
        }
    }

    public String convert(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, boolean makePackages, int scriptIndex, int classIndex, boolean highlighting, List<String> fullyQualifiedNames, boolean parallel) {
        StringBuilder sb = new StringBuilder();
        ExecutorService executor = null;
        List<Future<String>> futureResults = null;
        List<TraitConvertTask> traitConvertTasks = null;

        if (parallel) {
            executor = Executors.newFixedThreadPool(20);
            futureResults = new ArrayList<>();
        } else {
            traitConvertTasks = new ArrayList<>();
        }
        for (int t = 0; t < traits.length; t++) {
            TraitConvertTask task = new TraitConvertTask(traits[t], makePackages, path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, t, parallel);
            if (parallel) {
                Future<String> future = executor.submit(task);
                futureResults.add(future);
            } else {
                traitConvertTasks.add(task);
            }
        }

        int taskCount = parallel ? futureResults.size() : traitConvertTasks.size();
        for (int f = 0; f < taskCount; f++) {
            if (f > 0) {
                sb.append("\r\n\r\n");
            }
            try {
                String taskResult = parallel ? futureResults.get(f).get() : traitConvertTasks.get(f).call();
                sb.append(taskResult);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Traits.class.getName()).log(Level.SEVERE, "Error during traits converting", ex);
            }
        }
        if (parallel) {
            executor.shutdown();
        }
        return sb.toString();
    }
}
