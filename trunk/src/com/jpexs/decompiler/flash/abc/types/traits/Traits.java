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
import com.jpexs.decompiler.flash.helpers.Highlighting;
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

    public Trait traits[] = new Trait[0];

    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc) {
        int ret = 0;
        for (Trait t : traits) {
            ret += t.removeTraps(scriptIndex, classIndex, isStatic, abc);
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
        boolean paralel;

        public TraitConvertTask(Trait trait, boolean makePackages, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlighting, List<String> fullyQualifiedNames, int traitIndex, boolean paralel) {
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
            this.paralel = paralel;
        }

        @Override
        public String call() throws Exception {
            String plus;
            if (makePackages) {
                plus = trait.convertPackaged(path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, paralel);
            } else {
                plus = trait.convert(path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, paralel);
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

    public String convert(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, boolean makePackages, int scriptIndex, int classIndex, boolean highlighting, List<String> fullyQualifiedNames, boolean paralel) {
        String s = "";
        ExecutorService executor = Executors.newFixedThreadPool(paralel ? 20 : 1);
        List<Future<String>> futureResults = new ArrayList<>();
        for (int t = 0; t < traits.length; t++) {
            Future<String> future = executor.submit(new TraitConvertTask(traits[t], makePackages, path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlighting, fullyQualifiedNames, t, paralel));
            futureResults.add(future);
        }

        for (int f = 0; f < futureResults.size(); f++) {
            if (f > 0) {
                s += "\r\n\r\n";
            }
            try {
                s += futureResults.get(f).get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Traits.class.getName()).log(Level.SEVERE, "Error during traits converting", ex);
            }
        }
        executor.shutdown();
        return s;
    }
}
