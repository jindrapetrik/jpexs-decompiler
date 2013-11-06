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
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.ExportMode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    public int addTrait(Trait t) {
        traits = Arrays.copyOf(traits, traits.length + 1);
        traits[traits.length - 1] = t;
        return traits.length - 1;
    }

    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
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

    private class TraitConvertTask implements Callable<Void> {

        Trait trait;
        boolean makePackages;
        String path;
        List<ABCContainerTag> abcTags;
        ABC abc;
        boolean isStatic;
        ExportMode exportMode;
        int scriptIndex;
        int classIndex;
        NulWriter writer;
        List<String> fullyQualifiedNames;
        int traitIndex;
        boolean parallel;
        Trait parent;

        public TraitConvertTask(Trait trait, Trait parent, boolean makePackages, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, int traitIndex, boolean parallel) {
            this.trait = trait;
            this.parent = parent;
            this.makePackages = makePackages;
            this.path = path;
            this.abcTags = abcTags;
            this.abc = abc;
            this.isStatic = isStatic;
            this.exportMode = exportMode;
            this.scriptIndex = scriptIndex;
            this.classIndex = classIndex;
            this.writer = writer;
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.traitIndex = traitIndex;
            this.parallel = parallel;
        }

        @Override
        public Void call() {
            if (makePackages) {
                trait.convertPackaged(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            } else {
                trait.convert(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            }
            return null;
        }
    }

    public GraphTextWriter toString(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, GraphTextWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        for (int t = 0; t < traits.length; t++) {
            if (t > 0) {
                writer.newLine();
            }
            Trait trait = traits[t];
            int h = t;
            if (classIndex != -1) {
                if (!isStatic) {
                    h += abc.class_info[classIndex].static_traits.traits.length;
                }
            }
            if (trait instanceof TraitClass) {
                writer.startClass(((TraitClass) trait).class_info);
            } else {
                writer.startTrait(h);
            }
            if (makePackages) {
                trait.toStringPackaged(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            } else {
                trait.toString(parent, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            }
            if (trait instanceof TraitClass) {
                writer.endClass();
            } else {
                writer.endTrait();
            }
        }
        return writer;
    }

    public void convert(Trait parent, String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, ExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, NulWriter writer, List<String> fullyQualifiedNames, boolean parallel) {
        if (!parallel || traits.length < 2) {
            for (int t = 0; t < traits.length; t++) {
                TraitConvertTask task = new TraitConvertTask(traits[t], parent, makePackages, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, t, parallel);
                task.call();
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(20);
            List<Future<Void>> futureResults = null;

            futureResults = new ArrayList<>();
            for (int t = 0; t < traits.length; t++) {
                TraitConvertTask task = new TraitConvertTask(traits[t], parent, makePackages, path, abcTags, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, t, parallel);
                Future<Void> future = executor.submit(task);
                futureResults.add(future);
            }

            for (int f = 0; f < futureResults.size(); f++) {
                try {
                    futureResults.get(f).get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Traits.class.getName()).log(Level.SEVERE, "Error during traits converting", ex);
                }
            }
            executor.shutdown();
        }
    }
}
