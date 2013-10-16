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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.helpers.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class DecompiledEditorPane extends LineMarkedEditorPane implements CaretListener {

    private List<Highlighting> highlights = new ArrayList<>();
    private List<Highlighting> specialHighlights = new ArrayList<>();
    private List<Highlighting> traitHighlights = new ArrayList<>();
    private List<Highlighting> methodHighlights = new ArrayList<>();
    private List<Highlighting> classHighlights = new ArrayList<>();
    private Highlighting currentMethodHighlight;
    private Highlighting currentTraitHighlight;
    private ABC abc;
    private ScriptPack script;
    public int lastTraitIndex = 0;
    private boolean ignoreCarret = false;
    private boolean reset = false;
    private ABCPanel abcPanel;
    private int classIndex = -1;
    private boolean isStatic = false;
    private Cache cache = Cache.getInstance(true);
    private Trait currentTrait = null;

    public Trait getCurrentTrait() {
        return currentTrait;
    }

    public ScriptPack getScriptLeaf() {
        return script;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    public void setNoTrait() {
        abcPanel.detailPanel.showCard(DetailPanel.UNSUPPORTED_TRAIT_CARD, null);
    }

    public void hilightSpecial(String type, int index) {
        int startPos;
        int endPos;
        if (currentMethodHighlight == null) {
            if (currentTraitHighlight == null) {
                return;
            }
            startPos = currentTraitHighlight.startPos;
            endPos = currentTraitHighlight.startPos + currentTraitHighlight.len;
        } else {
            startPos = currentMethodHighlight.startPos;
            endPos = currentMethodHighlight.startPos + currentMethodHighlight.len;
        }

        List<Highlighting> allh = new ArrayList<>();
        for (Highlighting h : traitHighlights) {
            if (h.getPropertyString("index").equals("" + lastTraitIndex)) {
                for (Highlighting sh : specialHighlights) {
                    if (sh.startPos >= h.startPos && (sh.startPos + sh.len < h.startPos + h.len)) {
                        allh.add(sh);
                    }
                }
            }
        }
        if (currentMethodHighlight != null) {
            for (Highlighting h : specialHighlights) {
                if (h.startPos >= startPos && (h.startPos + h.len < endPos)) {
                    allh.add(h);
                }
            }
        }
        for (Highlighting h : allh) {
            if (h.getPropertyString("subtype").equals(type) && ((long) h.getPropertyLong("index") == index)) {
                ignoreCarret = true;
                try {
                    setCaretPosition(h.startPos);
                } catch (IllegalArgumentException ie) {
                    //ignored
                }
                getCaret().setVisible(true);
                ignoreCarret = false;
                break;
            }
        }
    }

    public void hilightOffset(long offset) {
        if (currentMethodHighlight == null) {
            return;
        }
        List<Highlighting> allh = new ArrayList<>();
        for (Highlighting h : traitHighlights) {
            if (h.getPropertyString("index").equals("" + lastTraitIndex)) {
                Highlighting h2 = Highlighting.search(highlights, "offset", "" + offset, h.startPos, h.startPos + h.len);
                if (h2 != null) {
                    ignoreCarret = true;
                    try {
                        setCaretPosition(h2.startPos);
                    } catch (IllegalArgumentException ie) {
                        //ignored
                    }
                    getCaret().setVisible(true);
                    ignoreCarret = false;
                }

            }
        }
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

    private boolean displayMethod(int pos, int methodIndex, String name, Trait trait, boolean isStatic) {
        if (abc == null) {
            return false;
        }
        int bi = abc.findBodyIndex(methodIndex);
        if (bi == -1) {
            return false;
        }
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
            }
        });
        //fix for inner functions:
        if (trait instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) trait;
            if (tm.method_info != methodIndex) {
                trait = null;
            }
        }
        if (trait instanceof TraitFunction) {
            TraitFunction tf = (TraitFunction) trait;
            if (tf.method_info != methodIndex) {
                trait = null;
            }
        }
        abcPanel.detailPanel.showCard(DetailPanel.METHOD_TRAIT_CARD, trait);
        if (reset || (abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex() != bi)) {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abc, name, trait);
            abcPanel.detailPanel.setEditMode(false);
            this.isStatic = isStatic;
        }
        boolean success = false;
        Highlighting h = Highlighting.search(highlights, pos);
        if (h != null) {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.hilighOffset(h.getPropertyLong("offset"));
            success = true;
        }
        Highlighting sh = Highlighting.search(specialHighlights, pos);
        if (sh != null) {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.hilighSpecial(sh.getPropertyString("subtype"), (int) (long) sh.getPropertyLong("index"));
            success = true;
        }
        return success;
    }

    public void displayClass(int classIndex, int scriptIndex) {
        if (abcPanel.navigator.getClassIndex() != classIndex) {
            abcPanel.navigator.setClassIndex(classIndex, scriptIndex);
        }
    }

    public void resetEditing() {
        reset = true;
        caretUpdate(null);
        reset = false;
    }

    public int getMultinameUnderCursor() {
        int pos = getCaretPosition();
        Highlighting h = Highlighting.search(highlights, pos);
        if (h != null) {
            List<AVM2Instruction> list = abc.bodies[abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex()].code.code;
            AVM2Instruction lastIns = null;
            long inspos = 0;
            AVM2Instruction selIns = null;
            for (AVM2Instruction ins : list) {
                if (h.getPropertyLong("offset") == ins.getOffset()) {
                    selIns = ins;
                    break;
                }
                if (ins.getOffset() > h.getPropertyLong("offset")) {
                    inspos = h.getPropertyLong("offset") - lastIns.offset;
                    selIns = lastIns;
                    break;
                }
                lastIns = ins;
            }
            if (selIns != null) {
                for (int i = 0; i < selIns.definition.operands.length; i++) {
                    if (selIns.definition.operands[i] == AVM2Code.DAT_MULTINAME_INDEX) {
                        return selIns.operands[i];
                    }
                }
            }
        }
        int currentMethod = -1;
        if (currentTrait instanceof TraitMethodGetterSetter) {
            currentMethod = ((TraitMethodGetterSetter) currentTrait).method_info;
        }
        if (currentMethodHighlight != null) {
            currentMethod = (int) (long) currentMethodHighlight.getPropertyLong("index");
        }
        Highlighting sh = Highlighting.search(specialHighlights, pos);
        if (sh != null) {
            switch (sh.getPropertyString("subtype")) {
                case "traittypename":
                    if (currentTrait instanceof TraitSlotConst) {
                        TraitSlotConst ts = (TraitSlotConst) currentTrait;
                        return ts.type_index;
                    }
                    break;
                case "traitname":
                    if (currentTrait != null) {
                        return currentTrait.name_index;
                    }
                    break;
                case "returns":
                    if (currentMethod > -1) {
                        return abc.method_info[currentMethod].ret_type;
                    }
                    break;
                case "param":
                    if (currentMethod > -1) {
                        return abc.method_info[currentMethod].param_types[(int) (long) sh.getPropertyLong("index")];
                    }
                    break;
            }
        }
        return -1;
    }

    @Override
    public void caretUpdate(final CaretEvent e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    caretUpdate(e);
                }
            });
            return;
        }
        if (abc == null) {
            return;
        }
        if (ignoreCarret) {
            return;
        }

        getCaret().setVisible(true);
        int pos = getCaretPosition();
        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(true);
        try {
            classIndex = -1;
            Highlighting cm = Highlighting.search(classHighlights, pos);
            if (cm != null) {
                classIndex = (int) (long) cm.getPropertyLong("index");
                displayClass(classIndex, script.scriptIndex);
            }
            Highlighting tm = Highlighting.search(methodHighlights, pos);
            if (tm != null) {
                String name = "";
                if (abc != null) {
                    if (classIndex > -1) {
                        name = abc.instance_info[classIndex].getName(abc.constants).getNameWithNamespace(abc.constants);
                    }
                }
                currentTrait = null;
                currentTraitHighlight = Highlighting.search(traitHighlights, pos);
                if (currentTraitHighlight != null) {
                    lastTraitIndex = (int) (long) currentTraitHighlight.getPropertyLong("index");
                    if ((abc != null) && (classIndex != -1)) {
                        currentTrait = abc.findTraitByTraitId(classIndex, lastTraitIndex);
                        isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                        if (currentTrait != null) {
                            name += ":" + currentTrait.getName(abc).getName(abc.constants, new ArrayList<String>());
                        }
                    }
                }

                displayMethod(pos, (int) (long) tm.getPropertyLong("index"), name, currentTrait, isStatic);
                currentMethodHighlight = tm;
                return;
            }

            if (classIndex == -1) {
                setNoTrait();
                return;
            }
            currentTrait = null;
            currentTraitHighlight = Highlighting.search(traitHighlights, pos);
            if (currentTraitHighlight != null) {
                lastTraitIndex = (int) (long) currentTraitHighlight.getPropertyLong("index");
                currentTrait = abc.findTraitByTraitId(classIndex, (int) (long) currentTraitHighlight.getPropertyLong("index"));
                if (currentTrait != null) {
                    if (currentTrait instanceof TraitSlotConst) {
                        abcPanel.detailPanel.slotConstTraitPanel.load((TraitSlotConst) currentTrait, abc,
                                abc.isStaticTraitId(classIndex, lastTraitIndex));
                        abcPanel.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD, currentTrait);
                        abcPanel.detailPanel.setEditMode(false);
                        currentMethodHighlight = null;
                        Highlighting spec = Highlighting.search(specialHighlights, pos, "type", "special", currentTraitHighlight.startPos, currentTraitHighlight.startPos + currentTraitHighlight.len);
                        if (spec != null) {
                            abcPanel.detailPanel.slotConstTraitPanel.hilightSpecial(spec);
                        }

                        return;
                    }
                }
                currentMethodHighlight = null;
                String name = "";
                currentTrait = null;
                if (abc != null) {
                    name = abc.instance_info[classIndex].getName(abc.constants).getNameWithNamespace(abc.constants);
                    currentTrait = abc.findTraitByTraitId(classIndex, lastTraitIndex);
                    isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                    if (currentTrait != null) {
                        name += ":" + currentTrait.getName(abc).getName(abc.constants, new ArrayList<String>());
                    }
                }

                displayMethod(pos, abc.findMethodIdByTraitId(classIndex, (int) (long) currentTraitHighlight.getPropertyLong("index")), name, currentTrait, isStatic);
                return;
            }
            setNoTrait();
        } finally {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(false);
        }
    }

    private void uncache(ScriptPack pack) {
        cache.remove(pack);
    }

    public boolean isCached(ScriptPack pack) {
        return cache.contains(pack);
    }

    private CachedDecompilation getCached(ScriptPack pack) {
        if (!cache.contains(pack)) {
            cacheScriptPack(pack, abcList);
        }
        return (CachedDecompilation) cache.get(pack);
    }

    public String getCachedText(ScriptPack pack) {
        return getCached(pack).text;
    }

    public void gotoLastTrait() {
        gotoTrait(lastTraitIndex);
    }

    public void gotoTrait(int traitId) {
        if (traitId == -1) {
            setCaretPosition(0);
            return;
        }

        Highlighting tc = Highlighting.search(classHighlights, "index", "" + classIndex);
        if (tc != null) {
            Highlighting th = Highlighting.search(traitHighlights, "index", "" + traitId, tc.startPos, tc.startPos + tc.len);
            if (th != null) {
                try {
                    ignoreCarret = true;
                    setCaretPosition(th.startPos + th.len - 1);
                    ignoreCarret = false;
                } catch (IllegalArgumentException iae) {
                }
                final int pos = th.startPos;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            setCaretPosition(pos);
                        } catch (IllegalArgumentException iae) {
                        }
                    }
                }, 100);
                return;
            }
        }



        setCaretPosition(0);
    }

    public DecompiledEditorPane(ABCPanel abcPanel) {
        setEditable(false);
        getCaret().setVisible(true);
        addCaretListener(this);
        this.abcPanel = abcPanel;
    }
    private List<ABCContainerTag> abcList;

    public void clearScriptCache() {
        cache.clear();
    }

    public void cacheScriptPack(ScriptPack scriptLeaf, List<ABCContainerTag> abcList) {
        int maxCacheSize = 50;
        int scriptIndex = scriptLeaf.scriptIndex;
        String hilightedCode = "";
        ScriptInfo script = null;
        ABC abc = scriptLeaf.abc;
        if (scriptIndex > -1) {
            script = abc.script_info[scriptIndex];
        }
        if (!cache.contains(scriptLeaf)) {
            HilightedTextWriter writer = new HilightedTextWriter(true);
            for (int scriptTraitIndex : scriptLeaf.traitIndices) {
                script.traits.traits[scriptTraitIndex].convertPackaged(null, scriptLeaf.getPath().toString(), abcList, abc, false, ExportMode.SOURCE, scriptIndex, -1, writer, new ArrayList<String>(), Configuration.getConfig("parallelSpeedUp", true));
            }
            String s = Graph.removeNonRefenrencedLoopLabels(writer.toString(), true);
            hilightedCode = s;
            cache.put(scriptLeaf, new CachedDecompilation(hilightedCode));
        }
    }

    public void setScript(ScriptPack scriptLeaf, List<ABCContainerTag> abcList) {
        abcPanel.scriptNameLabel.setText(scriptLeaf.getPath().toString());
        int scriptIndex = scriptLeaf.scriptIndex;
        ScriptInfo script = null;
        ABC abc = scriptLeaf.abc;
        if (scriptIndex > -1) {
            script = abc.script_info[scriptIndex];
        }
        if (script == null) {
            highlights = new ArrayList<>();
            specialHighlights = new ArrayList<>();
            traitHighlights = new ArrayList<>();
            methodHighlights = new ArrayList<>();
            this.script = scriptLeaf;
            return;
        }
        setText("//" + translate("pleasewait") + "...");

        this.abc = abc;
        this.abcList = abcList;
        this.script = scriptLeaf;

        cacheScriptPack(scriptLeaf, abcList);
        CachedDecompilation cd = getCached(scriptLeaf);
        final String hilightedCode = cd.text;
        highlights = cd.getHighlights();
        specialHighlights = cd.getSpecialHighligths();
        traitHighlights = cd.getTraitHighlights();
        methodHighlights = cd.getMethodHighlights();
        classHighlights = cd.getClassHighlights();
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (hilightedCode.length() > 1024 * 1024 * 2/*2MB*/) {
                    setContentType("text/plain");
                } else {
                    setContentType("text/actionscript");
                }
                setText(hilightedCode);
            }
        });

    }

    public void reloadClass() {
        int ci = classIndex;
        uncache(script);
        if ((script != null) && (abc != null)) {
            setScript(script, abcList);
        }
        setNoTrait();
        setClassIndex(ci);
    }

    public int getClassIndex() {
        return classIndex;
    }

    public void setABC(ABC abc) {
        this.abc = abc;
        cache.clear();
        setText("");
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        setCaretPosition(0);
    }
}
