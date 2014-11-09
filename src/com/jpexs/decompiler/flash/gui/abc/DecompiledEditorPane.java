/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperVoidIns;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.HilightedText;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.helpers.Cache;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public boolean ignoreCarret = false;
    private boolean reset = false;
    private final ABCPanel abcPanel;
    private int classIndex = -1;
    private boolean isStatic = false;
    private final Cache<ScriptPack, CachedDecompilation> cache = Cache.getInstance(true);

    private final List<Runnable> scriptListeners = new ArrayList<>();

    public void addScriptListener(Runnable l) {
        scriptListeners.add(l);
    }

    public ABCPanel getAbcPanel() {
        return abcPanel;
    }

    public void removeScriptListener(Runnable l) {
        scriptListeners.remove(l);
    }

    public void fireScript() {
        for (int i = 0; i < scriptListeners.size(); i++) {
            scriptListeners.get(i).run();
        }
    }

    public Trait getCurrentTrait() {
        return abc.findTraitByTraitId(classIndex, lastTraitIndex);
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
                if (h.startPos <= getDocument().getLength()) {
                    setCaretPosition(h.startPos);
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
                    if (h2.startPos <= getDocument().getLength()) {
                        setCaretPosition(h2.startPos);
                    }
                    getCaret().setVisible(true);
                    ignoreCarret = false;
                }

            }
        }
    }

    public synchronized void setClassIndex(int classIndex) {
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
        MethodCodePanel methodCodePanel = abcPanel.detailPanel.methodTraitPanel.methodCodePanel;
        if (reset || (methodCodePanel.getBodyIndex() != bi)) {
            methodCodePanel.setBodyIndex(bi, abc, name, trait);
            abcPanel.detailPanel.setEditMode(false);
            this.isStatic = isStatic;
        }
        boolean success = false;
        Highlighting h = Highlighting.search(highlights, pos);
        if (h != null) {
            methodCodePanel.hilighOffset(h.getPropertyLong("offset"));
            success = true;
        }
        Highlighting sh = Highlighting.search(specialHighlights, pos);
        if (sh != null) {
            methodCodePanel.hilighSpecial(sh.getPropertyString("subtype"), sh.getPropertyString("index"));
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

    public int getMultinameUnderMouseCursor(Point pt) {
        return getMultinameAtPos(viewToModel(pt));
    }

    public int getMultinameUnderCaret() {
        return getMultinameAtPos(getCaretPosition());
    }

    public int getLocalDeclarationOfPos(int pos) {
        Highlighting sh = Highlighting.search(specialHighlights, pos);
        Highlighting h = Highlighting.search(highlights, pos);

        List<Highlighting> tms = Highlighting.searchAll(methodHighlights, pos, null, null, -1, -1);
        if (tms.isEmpty()) {
            return -1;
        }
        for (Highlighting tm : tms) {

            List<Highlighting> tm_tms = Highlighting.searchAll(methodHighlights, -1, "index", tm.getPropertyString("index"), -1, -1);
            if (h == null) {
                return -1;
            }
            //is it already declaration?
            if ("true".equals(h.getPropertyString("declaration")) || (sh != null && "true".equals(sh.getPropertyString("declaration")))) {
                return -1; //no jump
            }

            Map<String, String> search = h.getProperties();
            search.remove("index");
            search.remove("subtype");
            search.remove("offset");
            if (search.isEmpty()) {
                return -1;
            }
            search.put("declaration", "true");

            for (Highlighting tm1 : tm_tms) {
                Highlighting rh = Highlighting.search(highlights, search, tm1.startPos, tm1.startPos + tm1.len);
                if (rh == null) {
                    rh = Highlighting.search(specialHighlights, search, tm1.startPos, tm1.startPos + tm1.len);
                }
                if (rh != null) {
                    return rh.startPos;
                }
            }
        }

        return -1;
    }

    public int getMultinameAtPos(int pos) {
        Highlighting tm = Highlighting.search(methodHighlights, pos);
        Trait currentTrait = null;
        int currentMethod = -1;
        if (tm != null) {

            int mi = (int) (long) tm.getPropertyLong("index");
            int bi = abc.findBodyIndex(mi);
            Highlighting h = Highlighting.search(highlights, pos);
            if (h != null) {
                List<AVM2Instruction> list = abc.bodies.get(bi).getCode().code;
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
                    if ((selIns.definition instanceof ConstructSuperIns) || (selIns.definition instanceof CallSuperIns)|| (selIns.definition instanceof CallSuperVoidIns)) {
                        Highlighting tc = Highlighting.search(classHighlights, pos);
                        if(tc!=null){
                            int cindex = (int)(long)tc.getPropertyLong("index");
                            if(cindex>-1){
                                return abc.instance_info.get(cindex).super_index;
                            }
                        }
                    } else {
                        for (int i = 0; i < selIns.definition.operands.length; i++) {
                            if (selIns.definition.operands[i] == AVM2Code.DAT_MULTINAME_INDEX) {
                                return selIns.operands[i];
                            }
                        }
                    }
                }
            }

            currentTrait = getCurrentTrait();
            if (currentTrait instanceof TraitMethodGetterSetter) {
                currentMethod = ((TraitMethodGetterSetter) currentTrait).method_info;
            }
            if (currentMethodHighlight != null) {
                currentMethod = (int) (long) currentMethodHighlight.getPropertyLong("index");
            }
        }
        Highlighting sh = Highlighting.search(specialHighlights, pos);
        if (sh != null) {
            switch (sh.getPropertyString("subtype")) {
                case "typename":
                    String typeName = sh.getPropertyString("index");
                    for (int i = 1; i < abc.constants.constant_multiname.size(); i++) {
                        Multiname m = abc.constants.constant_multiname.get(i);
                        if (m != null) {
                            if (typeName.equals(m.getNameWithNamespace(abc.constants, true))) {
                                return i;
                            }
                        }
                    }
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
                        return abc.method_info.get(currentMethod).ret_type;
                    }
                    break;
                case "param":
                    if (currentMethod > -1) {
                        return abc.method_info.get(currentMethod).param_types[(int) (long) sh.getPropertyLong("index")];
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
                        name = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, false);
                    }
                }
                Trait currentTrait = null;
                currentTraitHighlight = Highlighting.search(traitHighlights, pos);
                if (currentTraitHighlight != null) {
                    lastTraitIndex = (int) (long) currentTraitHighlight.getPropertyLong("index");
                    if ((abc != null) && (classIndex != -1)) {
                        currentTrait = getCurrentTrait();
                        isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                        if (currentTrait != null) {
                            name += ":" + currentTrait.getName(abc).getName(abc.constants, new ArrayList<String>(), false);
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
            Trait currentTrait = null;
            currentTraitHighlight = Highlighting.search(traitHighlights, pos);
            if (currentTraitHighlight != null) {
                lastTraitIndex = (int) (long) currentTraitHighlight.getPropertyLong("index");
                currentTrait = getCurrentTrait();
                if (currentTrait != null) {
                    if (currentTrait instanceof TraitSlotConst) {
                        abcPanel.detailPanel.slotConstTraitPanel.load((TraitSlotConst) currentTrait, abc,
                                abc.isStaticTraitId(classIndex, lastTraitIndex));
                        abcPanel.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD, currentTrait);
                        abcPanel.detailPanel.setEditMode(false);
                        currentMethodHighlight = null;
                        Highlighting spec = Highlighting.search(specialHighlights, pos, null, null, currentTraitHighlight.startPos, currentTraitHighlight.startPos + currentTraitHighlight.len);
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
                    name = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, false);
                    currentTrait = getCurrentTrait();
                    isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                    if (currentTrait != null) {
                        name += ":" + currentTrait.getName(abc).getName(abc.constants, new ArrayList<String>(), false);
                    }
                }

                displayMethod(pos, abc.findMethodIdByTraitId(classIndex, lastTraitIndex), name, currentTrait, isStatic);
                return;
            }
            setNoTrait();
        } finally {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(false);
        }
    }

    public void uncache(ScriptPack pack) {
        cache.remove(pack);
    }

    public boolean isCached(ScriptPack pack) {
        return cache.contains(pack);
    }

    private CachedDecompilation getCached(ScriptPack pack) throws InterruptedException {
        if (!cache.contains(pack)) {
            cacheScriptPack(pack, abcList);
        }
        return (CachedDecompilation) cache.get(pack);
    }

    public String getCachedText(ScriptPack pack) throws InterruptedException {
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
                ignoreCarret = true;
                int startPos = th.startPos + th.len - 1;
                if (startPos <= getDocument().getLength()) {
                    setCaretPosition(startPos);
                }
                ignoreCarret = false;
                final int pos = th.startPos;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (pos <= getDocument().getLength()) {
                            setCaretPosition(pos);
                        }
                    }
                }, 100);
                return;
            }
        }

        setCaretPosition(0);
    }

    public DecompiledEditorPane(ABCPanel abcPanel) {
        super();
        setEditable(false);
        getCaret().setVisible(true);
        addCaretListener(this);
        this.abcPanel = abcPanel;
    }
    private List<ABCContainerTag> abcList;

    public void clearScriptCache() {
        cache.clear();
    }

    public void cacheScriptPack(ScriptPack scriptLeaf, List<ABCContainerTag> abcList) throws InterruptedException {
        int maxCacheSize = 50;
        int scriptIndex = scriptLeaf.scriptIndex;
        ScriptInfo script = null;
        ABC abc = scriptLeaf.abc;
        if (scriptIndex > -1) {
            script = abc.script_info.get(scriptIndex);
        }
        if (!cache.contains(scriptLeaf)) {
            boolean parallel = Configuration.parallelSpeedUp.get();
            HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
            scriptLeaf.toSource(writer, abcList, script.traits.traits, ScriptExportMode.AS, parallel);
            HilightedText hilightedCode = new HilightedText(writer);
            cache.put(scriptLeaf, new CachedDecompilation(hilightedCode));
        }
    }

    public void setScript(ScriptPack scriptLeaf, List<ABCContainerTag> abcList) {
        abcPanel.scriptNameLabel.setText(scriptLeaf.getClassPath().toString());
        int scriptIndex = scriptLeaf.scriptIndex;
        ScriptInfo script = null;
        ABC abc = scriptLeaf.abc;
        if (scriptIndex > -1) {
            script = abc.script_info.get(scriptIndex);
        }
        if (script == null) {
            highlights = new ArrayList<>();
            specialHighlights = new ArrayList<>();
            traitHighlights = new ArrayList<>();
            methodHighlights = new ArrayList<>();
            this.script = scriptLeaf;
            return;
        }
        setText("//" + AppStrings.translate("pleasewait") + "...");

        this.abc = abc;
        this.abcList = abcList;
        this.script = scriptLeaf;
        CachedDecompilation cd = null;
        try {
            cacheScriptPack(scriptLeaf, abcList);
            cd = getCached(scriptLeaf);
        } catch (InterruptedException ex) {
        }
        if (cd != null) {
            final String hilightedCode = cd.text;
            highlights = cd.getInstructionHighlights();
            specialHighlights = cd.getSpecialHighligths();
            traitHighlights = cd.getTraitHighlights();
            methodHighlights = cd.getMethodHighlights();
            classHighlights = cd.getClassHighlights();
            setContentType("text/actionscript");
            setText(hilightedCode);
        }
        fireScript();
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
