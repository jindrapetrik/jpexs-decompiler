/*
 *  Copyright (C) 2010-2021 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperVoidIns;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.action.deobfuscation.BrokenScriptDetector;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.editor.DebuggableEditorPane;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 *
 * @author JPEXS
 */
public class DecompiledEditorPane extends DebuggableEditorPane implements CaretListener {

    private static final Logger logger = Logger.getLogger(DecompiledEditorPane.class.getName());

    private HighlightedText highlightedText = HighlightedText.EMPTY;

    private Highlighting currentMethodHighlight;

    private Highlighting currentTraitHighlight;

    private ScriptPack script;

    public int lastTraitIndex = GraphTextWriter.TRAIT_UNKNOWN;

    public boolean ignoreCarret = false;

    private boolean reset = false;

    private final ABCPanel abcPanel;

    private int classIndex = -1;

    private boolean isStatic = false;

    private CancellableWorker setSourceWorker;

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
        Runnable[] listeners = scriptListeners.toArray(new Runnable[scriptListeners.size()]);
        for (Runnable scriptListener : listeners) {
            scriptListener.run();
        }
    }

    public Trait getCurrentTrait() {
        return script.abc.findTraitByTraitId(classIndex, lastTraitIndex);
    }

    public ScriptPack getScriptLeaf() {
        return script;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    public void setNoTrait() {
        abcPanel.detailPanel.showCard(DetailPanel.UNSUPPORTED_TRAIT_CARD, null, 0);
    }

    public void hilightSpecial(HighlightSpecialType type, long index) {
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
        for (Highlighting h : highlightedText.getTraitHighlights()) {
            if (h.getProperties().index == lastTraitIndex) {
                for (Highlighting sh : highlightedText.getSpecialHighlights()) {
                    if (sh.startPos >= h.startPos && (sh.startPos + sh.len < h.startPos + h.len)) {
                        allh.add(sh);
                    }
                }
            }
        }
        if (currentMethodHighlight != null) {
            for (Highlighting h : highlightedText.getSpecialHighlights()) {
                if (h.startPos >= startPos && (h.startPos + h.len < endPos)) {
                    allh.add(h);
                }
            }
        }
        for (Highlighting h : allh) {
            if (h.getProperties().subtype.equals(type) && (h.getProperties().index == index)) {
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
        Highlighting h2 = Highlighting.searchOffset(highlightedText.getInstructionHighlights(), offset, currentMethodHighlight.startPos, currentMethodHighlight.startPos + currentMethodHighlight.len);
        if (h2 != null) {
            ignoreCarret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCarret = false;
        }
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

    private boolean displayMethod(int pos, int methodIndex, String name, Trait trait, int traitIndex, boolean isStatic) {
        ABC abc = getABC();
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
        abcPanel.detailPanel.showCard(DetailPanel.METHOD_GETTER_SETTER_TRAIT_CARD, trait, traitIndex);
        MethodCodePanel methodCodePanel = abcPanel.detailPanel.methodTraitPanel.methodCodePanel;
        if (reset || (methodCodePanel.getBodyIndex() != bi)) {
            methodCodePanel.setBodyIndex(scriptName, bi, abc, name, trait, script.scriptIndex);
            abcPanel.detailPanel.setEditMode(false);
            this.isStatic = isStatic;
        }
        boolean success = false;
        Highlighting h = Highlighting.searchPos(highlightedText.getInstructionHighlights(), pos);
        if (h != null) {
            methodCodePanel.hilighOffset(h.getProperties().offset);
            success = true;
        }
        Highlighting sh = Highlighting.searchPos(highlightedText.getSpecialHighlights(), pos);
        if (sh != null) {
            methodCodePanel.hilighSpecial(sh.getProperties().subtype, sh.getProperties().specialValue);
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

    public int getLocalDeclarationOfPos(int pos, Reference<DottedChain> type) {
        Highlighting sh = Highlighting.searchPos(highlightedText.getSpecialHighlights(), pos);
        Highlighting h = Highlighting.searchPos(highlightedText.getInstructionHighlights(), pos);

        if (h == null) {
            return -1;
        }

        List<Highlighting> tms = Highlighting.searchAllPos(highlightedText.getMethodHighlights(), pos);
        if (tms.isEmpty()) {
            return -1;
        }
        for (Highlighting tm : tms) {

            List<Highlighting> tm_tms = Highlighting.searchAllIndexes(highlightedText.getMethodHighlights(), tm.getProperties().index);
            //is it already declaration?
            if (h.getProperties().declaration || (sh != null && sh.getProperties().declaration)) {
                return -1; //no jump
            }

            String lname = h.getProperties().localName;
            if ("this".equals(lname)) {
                Highlighting ch = Highlighting.searchPos(highlightedText.getClassHighlights(), pos);
                int cindex = (int) ch.getProperties().index;
                ABC abc = getABC();
                type.setVal(abc.instance_info.get(cindex).getName(abc.constants).getNameWithNamespace(abc.constants, true));
                return ch.startPos;
            }

            HighlightData hData = h.getProperties();
            HighlightData search = new HighlightData();
            search.declaration = hData.declaration;
            search.declaredType = hData.declaredType;
            search.localName = hData.localName;
            search.specialValue = hData.specialValue;
            if (search.isEmpty()) {
                return -1;
            }
            search.declaration = true;

            for (Highlighting tm1 : tm_tms) {
                Highlighting rh = Highlighting.search(highlightedText.getInstructionHighlights(), search, tm1.startPos, tm1.startPos + tm1.len);
                if (rh == null) {
                    rh = Highlighting.search(highlightedText.getSpecialHighlights(), search, tm1.startPos, tm1.startPos + tm1.len);
                }
                if (rh != null) {
                    type.setVal(rh.getProperties().declaredType);
                    return rh.startPos;
                }
            }
        }

        return -1;
    }

    public boolean getPropertyTypeAtPos(int pos, Reference<Integer> abcIndex, Reference<Integer> classIndex, Reference<Integer> traitIndex, Reference<Boolean> classTrait, Reference<Integer> multinameIndex) {

        int m = getMultinameAtPos(pos, true);
        if (m <= 0) {
            return false;
        }
        SyntaxDocument sd = (SyntaxDocument) getDocument();
        Token t = sd.getTokenAt(pos + 1);
        Token lastToken = t;
        Token prev;
        while (t.type == TokenType.IDENTIFIER || t.type == TokenType.KEYWORD || t.type == TokenType.REGEX) {
            prev = sd.getPrevToken(t);
            if (prev != null) {
                if (!".".equals(prev.getString(sd))) {
                    break;
                }
                t = sd.getPrevToken(prev);
            } else {
                break;
            }
        }
        if (t.type != TokenType.IDENTIFIER && t.type != TokenType.KEYWORD && t.type != TokenType.REGEX) {
            return false;
        }
        Reference<DottedChain> locTypeRef = new Reference<>(DottedChain.EMPTY);
        getLocalDeclarationOfPos(t.start, locTypeRef);
        DottedChain currentType = locTypeRef.getVal();
        if (currentType.equals(DottedChain.ALL)) {
            return false;
        }
        boolean found;

        while (!currentType.equals(DottedChain.ALL)) {
            String ident = t.getString(sd);
            found = false;
            List<ABCContainerTag> abcList = getABC().getSwf().getAbcList();
            loopi:
            for (int i = 0; i < abcList.size(); i++) {
                ABC a = abcList.get(i).getABC();
                int cindex = a.findClassByName(currentType);
                if (cindex > -1) {
                    InstanceInfo ii = a.instance_info.get(cindex);
                    for (int j = 0; j < ii.instance_traits.traits.size(); j++) {
                        Trait tr = ii.instance_traits.traits.get(j);
                        if (ident.equals(tr.getName(a).getName(a.constants, null, false /*NOT RAW!*/, true))) {
                            classIndex.setVal(cindex);
                            abcIndex.setVal(i);
                            traitIndex.setVal(j);
                            classTrait.setVal(false);
                            multinameIndex.setVal(tr.name_index);
                            currentType = ii.getName(a.constants).getNameWithNamespace(a.constants, true);
                            found = true;
                            break loopi;
                        }
                    }

                    ClassInfo ci = a.class_info.get(cindex);
                    for (int j = 0; j < ci.static_traits.traits.size(); j++) {
                        Trait tr = ci.static_traits.traits.get(j);
                        if (ident.equals(tr.getName(a).getName(a.constants, null, false /*NOT RAW!*/, true))) {
                            classIndex.setVal(cindex);
                            abcIndex.setVal(i);
                            traitIndex.setVal(j);
                            classTrait.setVal(true);
                            multinameIndex.setVal(tr.name_index);
                            currentType = ii.getName(a.constants).getNameWithNamespace(a.constants, true);
                            found = true;
                            break loopi;
                        }
                    }
                }
            }
            if (!found) {
                return false;
            }

            if (t == lastToken) {
                break;
            }
            t = sd.getNextToken(t);
            if (!".".equals(t.getString(sd))) {
                break;
            }
            t = sd.getNextToken(t);
        }
        return true;
    }

    public int getMultinameAtPos(int pos) {
        return getMultinameAtPos(pos, false);
    }

    private int getMultinameAtPos(int pos, boolean codeOnly) {
        int multinameIndex = _getMultinameAtPos(pos, codeOnly);
        if (multinameIndex > -1) {
            ABC abc = getABC();
            multinameIndex = abc.constants.convertToQname(abc.constants, multinameIndex);
        }
        return multinameIndex;
    }

    public int _getMultinameAtPos(int pos, boolean codeOnly) {
        Highlighting tm = Highlighting.searchPos(highlightedText.getMethodHighlights(), pos);
        Trait currentTrait = null;
        int currentMethod = -1;
        ABC abc = getABC();
        if (tm != null) {

            int mi = (int) tm.getProperties().index;
            currentMethod = mi;
            int bi = abc.findBodyIndex(mi);
            Highlighting h = Highlighting.searchPos(highlightedText.getInstructionHighlights(), pos);
            if (h != null) {
                long highlightOffset = h.getProperties().offset;
                List<AVM2Instruction> list = abc.bodies.get(bi).getCode().code;
                AVM2Instruction lastIns = null;
                AVM2Instruction selIns = null;
                for (AVM2Instruction ins : list) {
                    if (highlightOffset == ins.getAddress()) {
                        selIns = ins;
                        break;
                    }
                    if (ins.getAddress() > highlightOffset) {
                        selIns = lastIns;
                        break;
                    }
                    lastIns = ins;
                }
                if (selIns != null) {
                    //long inspos = highlightOffset - selIns.offset;
                    if (!codeOnly && ((selIns.definition instanceof ConstructSuperIns) || (selIns.definition instanceof CallSuperIns) || (selIns.definition instanceof CallSuperVoidIns))) {
                        Highlighting tc = Highlighting.searchPos(highlightedText.getClassHighlights(), pos);
                        if (tc != null) {
                            int cindex = (int) tc.getProperties().index;
                            if (cindex > -1) {
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

        }
        if (codeOnly) {
            return -1;
        }

        Highlighting ch = Highlighting.searchPos(highlightedText.getClassHighlights(), pos);
        if (ch != null) {
            Highlighting th = Highlighting.searchPos(highlightedText.getTraitHighlights(), pos);
            if (th != null) {
                currentTrait = abc.findTraitByTraitId((int) ch.getProperties().index, (int) th.getProperties().index);
            }
        }

        if (currentTrait instanceof TraitMethodGetterSetter) {
            currentMethod = ((TraitMethodGetterSetter) currentTrait).method_info;
        }
        Highlighting sh = Highlighting.searchPos(highlightedText.getSpecialHighlights(), pos);
        if (sh != null) {
            switch (sh.getProperties().subtype) {
                case TYPE_NAME:
                    String typeName = sh.getProperties().specialValue;
                    for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
                        Multiname m = abc.constants.getMultiname(i);
                        if (m != null) {
                            if (typeName.equals(m.getNameWithNamespace(abc.constants, true).toRawString())) {
                                return i;
                            }
                        }
                    }
                case TRAIT_TYPE_NAME:
                    if (currentTrait instanceof TraitSlotConst) {
                        TraitSlotConst ts = (TraitSlotConst) currentTrait;
                        return ts.type_index;
                    }
                    break;
                case TRAIT_NAME:
                    if (currentTrait != null) {
                        //return currentTrait.name_index;
                    }
                    break;
                case RETURNS:
                    if (currentMethod > -1) {
                        return abc.method_info.get(currentMethod).ret_type;
                    }
                    break;
                case PARAM:
                    if (currentMethod > -1) {
                        return abc.method_info.get(currentMethod).param_types[(int) sh.getProperties().index];
                    }
                    break;
            }
        }
        return -1;
    }

    @Override
    public void caretUpdate(final CaretEvent e) {
        ABC abc = getABC();
        if (abc == null) {
            return;
        }
        if (ignoreCarret) {
            return;
        }

        getCaret().setVisible(true);
        int pos = getCaretPosition();
        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(true);
        lastTraitIndex = GraphTextWriter.TRAIT_UNKNOWN;
        try {
            classIndex = -1;
            Highlighting cm = Highlighting.searchPos(highlightedText.getClassHighlights(), pos);
            if (cm != null) {
                classIndex = (int) cm.getProperties().index;
            }
            displayClass(classIndex, script.scriptIndex);
            Highlighting tm = Highlighting.searchPos(highlightedText.getMethodHighlights(), pos);
            if (tm != null) {
                String name = "";
                if (classIndex > -1) {
                    name = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, true).toPrintableString(true);
                }

                Trait currentTrait = null;
                currentTraitHighlight = Highlighting.searchPos(highlightedText.getTraitHighlights(), pos);
                if (currentTraitHighlight != null) {
                    lastTraitIndex = (int) currentTraitHighlight.getProperties().index;
                    if (classIndex != -1) {
                        currentTrait = getCurrentTrait();
                        isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                        if (currentTrait != null) {
                            name += ":" + currentTrait.getName(abc).getName(abc.constants, null, false, true);
                        }
                    }
                }

                displayMethod(pos, (int) tm.getProperties().index, name, currentTrait, lastTraitIndex, isStatic);
                currentMethodHighlight = tm;
                return;
            }

            if (classIndex == -1) {
                abcPanel.navigator.setClassIndex(-1, script.scriptIndex);
                setNoTrait();
                return;
            }
            Trait currentTrait;
            currentTraitHighlight = Highlighting.searchPos(highlightedText.getTraitHighlights(), pos);
            if (currentTraitHighlight != null) {
                lastTraitIndex = (int) currentTraitHighlight.getProperties().index;
                currentTrait = getCurrentTrait();
                if (currentTrait != null) {
                    if (currentTrait instanceof TraitSlotConst) {
                        abcPanel.detailPanel.slotConstTraitPanel.load((TraitSlotConst) currentTrait, abc,
                                abc.isStaticTraitId(classIndex, lastTraitIndex));
                        final Trait ftrait = currentTrait;
                        final int ftraitIndex = lastTraitIndex;
                        View.execInEventDispatch(() -> {
                            abcPanel.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD, ftrait, ftraitIndex);
                        });
                        abcPanel.detailPanel.setEditMode(false);
                        currentMethodHighlight = null;
                        Highlighting spec = Highlighting.searchPos(highlightedText.getSpecialHighlights(), pos, currentTraitHighlight.startPos, currentTraitHighlight.startPos + currentTraitHighlight.len);
                        if (spec != null) {
                            abcPanel.detailPanel.slotConstTraitPanel.hilightSpecial(spec);
                        }

                        return;
                    }
                }
                currentMethodHighlight = null;
                //currentTrait = null;
                String name = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, true).toPrintableString(true);
                currentTrait = getCurrentTrait();
                isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                if (currentTrait != null) {
                    name += ":" + currentTrait.getName(abc).getName(abc.constants, null, false, true);
                }

                displayMethod(pos, abc.findMethodIdByTraitId(classIndex, lastTraitIndex), name, currentTrait, lastTraitIndex, isStatic);
                return;
            }
            setNoTrait();
        } finally {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(false);
        }
    }

    public void gotoLastTrait() {
        gotoTrait(lastTraitIndex);
    }

    public void gotoLastMethod() {
        if (currentMethodHighlight != null) {
            final int fpos = currentMethodHighlight.startPos;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (fpos <= getDocument().getLength()) {
                        setCaretPosition(fpos);
                    }
                }
            }, 100);
        }
    }

    public void gotoTrait(int traitId) {
        boolean isScriptInit = traitId == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER;

        Highlighting tc = Highlighting.searchIndex(highlightedText.getClassHighlights(), classIndex);
        if (tc != null || isScriptInit) {
            Highlighting th = Highlighting.searchIndex(highlightedText.getTraitHighlights(), traitId, isScriptInit ? 0 : tc.startPos, isScriptInit ? -1 : tc.startPos + tc.len);
            int pos = 0;
            if (th != null) {
                if (th.len > 1) {
                    ignoreCarret = true;
                    int startPos = th.startPos + th.len - 1;
                    if (startPos <= getDocument().getLength()) {
                        setCaretPosition(startPos);
                    }
                    ignoreCarret = false;
                }
                pos = th.startPos;
            } else if (tc != null) {
                pos = tc.startPos;
            }

            final int fpos = pos;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (fpos <= getDocument().getLength()) {
                        setCaretPosition(fpos);
                    }
                }
            }, 100);
        }
    }

    public DecompiledEditorPane(ABCPanel abcPanel) {
        super();
        setEditable(false);
        getCaret().setVisible(true);
        addCaretListener(this);
        this.abcPanel = abcPanel;
    }

    public void clearScript() {
        script = null;
    }

    public void setScript(ScriptPack scriptLeaf, boolean force) {
        View.checkAccess();

        if (setSourceWorker != null) {
            setSourceWorker.cancel(true);
            setSourceWorker = null;
        }

        if (!force && this.script == scriptLeaf) {
            fireScript();
            return;
        }

        String sn = scriptLeaf.getClassPath().toString();
        setScriptName(sn);
        abcPanel.scriptNameLabel.setText(sn);
        int scriptIndex = scriptLeaf.scriptIndex;
        ScriptInfo nscript = null;
        ABC abc = scriptLeaf.abc;
        if (scriptIndex > -1) {
            nscript = abc.script_info.get(scriptIndex);
        }

        if (nscript == null) {
            highlightedText = HighlightedText.EMPTY;
            return;
        }

        HighlightedText decompiledText = SWF.getFromCache(scriptLeaf);

        boolean decompileNeeded = decompiledText == null;

        if (decompileNeeded) {
            CancellableWorker worker = new CancellableWorker() {
                @Override
                protected Void doInBackground() throws Exception {

                    if (decompileNeeded) {
                        View.execInEventDispatch(() -> {
                            setText("// " + AppStrings.translate("work.decompiling") + "...");
                        });

                        HighlightedText htext = SWF.getCached(scriptLeaf);
                        View.execInEventDispatch(() -> {
                            setSourceCompleted(scriptLeaf, htext);
                        });
                    }

                    return null;
                }

                @Override
                protected void done() {
                    View.execInEventDispatch(() -> {
                        setSourceWorker = null;
                        if (!Main.isDebugging()) {
                            Main.stopWork();
                        }

                        try {
                            get();
                        } catch (CancellationException ex) {
                            setText("// " + AppStrings.translate("work.canceled"));
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error", ex);
                            setText("// " + AppStrings.translate("decompilationError") + ": " + ex);
                        }
                    });
                }
            };

            worker.execute();
            setSourceWorker = worker;
            if (!Main.isDebugging()) {
                Main.startWork(AppStrings.translate("work.decompiling") + "...", worker);
            }
        } else {
            setSourceCompleted(scriptLeaf, decompiledText);
        }
    }

    private void setSourceCompleted(ScriptPack scriptLeaf, HighlightedText decompiledText) {
        View.checkAccess();

        if (decompiledText == null) {
            decompiledText = HighlightedText.EMPTY;
        }

        script = scriptLeaf;
        highlightedText = decompiledText;
        if (decompiledText != null) {
            String hilightedCode = decompiledText.text;
            BrokenScriptDetector det = new BrokenScriptDetector();
            if (det.codeIsBroken(hilightedCode)) {
                abcPanel.brokenHintPanel.setVisible(true);
            } else {
                abcPanel.brokenHintPanel.setVisible(false);
            }

            setText(hilightedCode);

            if (highlightedText.getClassHighlights().size() > 0) {
                try {
                    setCaretPosition(highlightedText.getClassHighlights().get(0).startPos);
                } catch (Exception ex) { //sometimes happens
                    //ignore
                }
            }
        }

        fireScript();
    }

    public void reloadClass() {
        View.checkAccess();

        int ci = classIndex;
        SWF.uncache(script);
        if (script != null && getABC() != null) {
            setScript(script, true);
        }

        setNoTrait();
        setClassIndex(ci);
    }

    public int getClassIndex() {
        return classIndex;
    }

    private ABC getABC() {
        return script == null ? null : script.abc;
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        setCaretPosition(0);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        // not debugging: so return existing text
        if (abcPanel.getDebugPanel().localsTable == null) {
            return super.getToolTipText();
        }

        final Point point = new Point(e.getX(), e.getY());
        final int pos = abcPanel.decompiledTextArea.viewToModel(point);
        final String identifier = abcPanel.getMainPanel().getActionPanel().getStringUnderPosition(pos, abcPanel.decompiledTextArea);

        if (identifier != null && !identifier.isEmpty()) {
            String tooltipText = abcPanel.getDebugPanel().localsTable.tryGetDebugHoverToolTipText(identifier);
            return (tooltipText == null ? super.getToolTipText() : tooltipText);
        }

        // not found: so return existing text
        return super.getToolTipText();
    }
}
