/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
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
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Reference;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 * @author JPEXS
 */
public class DecompiledEditorPane extends DebuggableEditorPane implements CaretListener {

    private static final Logger logger = Logger.getLogger(DecompiledEditorPane.class.getName());

    private HighlightedText highlightedText = HighlightedText.EMPTY;

    private Highlighting currentMethodHighlight;

    private Highlighting currentTraitHighlight;

    private ScriptPack script;

    public int lastTraitIndex = GraphTextWriter.TRAIT_UNKNOWN;

    public boolean ignoreCaret = false;

    private boolean reset = false;

    private final ABCPanel abcPanel;

    private int classIndex = -1;

    private boolean isStatic = false;

    private CancellableWorker setSourceWorker;

    private final List<Runnable> scriptListeners = new ArrayList<>();

    private boolean scriptLoaded = true;

    public void addScriptListener(Runnable l) {
        scriptListeners.add(l);
    }

    public void runWhenLoaded(Runnable l) {
        if (scriptLoaded) {
            l.run();
        } else {
            addScriptListener(new Runnable() {
                @Override
                public void run() {
                    l.run();
                    removeScriptListener(this);
                }
            });
        }
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
        if (lastTraitIndex < 0) {
            return null;
        }
        if (classIndex == -1) {
            return script.abc.script_info.get(script.scriptIndex).traits.traits.get(lastTraitIndex);
        }
        return script.abc.findTraitByTraitId(classIndex, lastTraitIndex);
    }

    public ScriptPack getScriptLeaf() {
        return script;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    public void setNoTrait() {
        abcPanel.detailPanel.showCard(DetailPanel.UNSUPPORTED_TRAIT_CARD, null, 0, null);
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
                ignoreCaret = true;
                if (h.startPos <= getDocument().getLength()) {
                    setCaretPosition(h.startPos);
                }
                getCaret().setVisible(true);
                ignoreCaret = false;
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
            ignoreCaret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCaret = false;
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

        //in interfaces, there is no body
        /*if (bi == -1) {
            return false;
        }*/
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
        abcPanel.detailPanel.showCard(DetailPanel.METHOD_GETTER_SETTER_TRAIT_CARD, trait, traitIndex, abc);
        MethodCodePanel methodCodePanel = abcPanel.detailPanel.methodTraitPanel.methodCodePanel;
        if (reset || (methodCodePanel.getMethodIndex() != methodIndex) || (methodCodePanel.getABC() != abc)) {
            methodCodePanel.setMethod(scriptName, methodIndex, bi, abc, name, trait, script.scriptIndex);
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

    public void displayClass(int classIndex, int scriptIndex, boolean hasScriptInitializer) {
        if (abcPanel.navigator.getClassIndex() != classIndex) {
            abcPanel.navigator.setClassIndex(classIndex, scriptIndex, hasScriptInitializer);
        }
    }

    public void resetEditing() {
        reset = true;
        caretUpdate(null);
        reset = false;
    }

    public int getMultinameUnderMouseCursor(Point pt, Reference<ABC> abcUsed) {
        return getMultinameAtPos(View.textComponentViewToModel(this, pt), abcUsed);
    }

    public int getMultinameUnderCaret(Reference<ABC> abcUsed) {
        return getMultinameAtPos(getCaretPosition(), abcUsed);
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

    public boolean getPropertyTypeAtPos(AbcIndexing indexing, int pos, Reference<Integer> abcIndex, Reference<Integer> classIndex, Reference<Integer> traitIndex, Reference<Boolean> classTrait, Reference<Integer> multinameIndex, Reference<ABC> abcUsed) {

        int m = getMultinameAtPos(pos, true, abcUsed);

        if (indexing == null) {
            return false;
        }
        /*int m = getMultinameAtPos(pos, true, abcUsed);
        if (m <= 0) {
            return false;
        }*/
        SyntaxDocument sd = (SyntaxDocument) getDocument();
        Token t = sd.getTokenAt(pos + 1);
        Token lastToken = t;
        Token prev;
        String propName = t.getString(sd);
        if (!(t.type == TokenType.IDENTIFIER || t.type == TokenType.KEYWORD || t.type == TokenType.REGEX)) {
            return false;
        }
        prev = sd.getPrevToken(t);
        if (prev == null) {
            return false;
        }
        if (!".".equals(prev.getString(sd))) {
            return false;
        }
        Highlighting sh = Highlighting.search(highlightedText.getSpecialHighlights(), new HighlightData(), prev.start, prev.start);
        if (sh == null) {
            return false;
        }

        HighlightData data = sh.getProperties();

        String parentType = data.propertyType;
        if (parentType.equals("*")) {
            return false;
        }
        AbcIndexing.TraitIndex propertyTraitIndex = indexing.findProperty(new AbcIndexing.PropertyDef(propName, new TypeItem(parentType), getABC(), data.namespaceIndex), data.isStatic, !data.isStatic, true);
        if (propertyTraitIndex == null) {
            return false;
        }

        List<ABCContainerTag> abcs = getABC().getSwf().getAbcList();
        int index = 0;
        boolean found = false;
        for (ABCContainerTag cnt : abcs) {
            if (cnt.getABC() == propertyTraitIndex.abc) {
                abcIndex.setVal(index);
                found = true;
                break;
            }
            index++;
        }
        if (!found) {
            return false;
        }

        abcUsed.setVal(propertyTraitIndex.abc);

        index = propertyTraitIndex.abc.findClassByName(propertyTraitIndex.objType.toString());
        if (index == -1) {
            return false;
        }
        classIndex.setVal(index);

        classTrait.setVal(data.isStatic);

        Traits ts;
        if (data.isStatic) {
            ts = propertyTraitIndex.abc.class_info.get(index).static_traits;
        } else {
            ts = propertyTraitIndex.abc.instance_info.get(index).instance_traits;
        }

        found = false;
        for (int i = 0; i < ts.traits.size(); i++) {
            if (ts.traits.get(i) == propertyTraitIndex.trait) {
                traitIndex.setVal(i);
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }

        multinameIndex.setVal(propertyTraitIndex.trait.name_index);

        return true;
        /*
        if (t.type != TokenType.IDENTIFIER && t.type != TokenType.KEYWORD && t.type != TokenType.REGEX) {
            return false;
        }
        Reference<DottedChain> locTypeRef = new Reference<>(DottedChain.EMPTY);
        getLocalDeclarationOfPos(t.start, locTypeRef);
        DottedChain currentType = locTypeRef.getVal();
        if (currentType.equals(DottedChain.ALL)) {
            return false;
        }     
         
        while (!currentType.equals(DottedChain.ALL)) {
            String ident = t.getString(sd);
            found = false;
            List<ABCContainerTag> abcList = abcUsed.getVal().getSwf().getAbcList();
            loopi:
            for (int i = 0; i < abcList.size(); i++) {
                ABC a = abcList.get(i).getABC();
                int cindex = a.findClassByName(currentType);
                if (cindex > -1) {
                    InstanceInfo ii = a.instance_info.get(cindex);
                    for (int j = 0; j < ii.instance_traits.traits.size(); j++) {
                        Trait tr = ii.instance_traits.traits.get(j);
                        if (ident.equals(tr.getName(a).getName(a.constants, null, false --not raw--, true))) { 
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
                        if (ident.equals(tr.getName(a).getName(a.constants, null, false --not raw--, true))) {
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
        return true;*/
    }

    public int getMultinameAtPos(int pos, Reference<ABC> abcUsed) {
        return getMultinameAtPos(pos, false, abcUsed);
    }

    private int getMultinameAtPos(int pos, boolean codeOnly, Reference<ABC> abcUsed) {
        int multinameIndex = _getMultinameAtPos(pos, codeOnly, abcUsed);
        if (multinameIndex > -1) {
            ABC abc = abcUsed.getVal();
            multinameIndex = abc.constants.convertToQname(abc.constants, multinameIndex);
        }
        return multinameIndex;
    }

    public int _getMultinameAtPos(int pos, boolean codeOnly, Reference<ABC> abcUsed) {
        Highlighting tm = Highlighting.searchPos(highlightedText.getMethodHighlights(), pos);
        Trait currentTrait = null;
        int currentMethod = -1;
        ABC abc = getABC();
        abcUsed.setVal(abc);
        if (abc == null) {
            return -1;
        }

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
                case CLASS_NAME:
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
                    break;
                case TRAIT_TYPE_NAME:
                    if (currentTrait instanceof TraitSlotConst) {
                        TraitSlotConst ts = (TraitSlotConst) currentTrait;
                        return ts.type_index;
                    }
                    break;
                case TRAIT_NAME:
                    if (currentTrait != null) {
                        //TODO: this should be handled better = to match method usages on the same class, not all matching classes. But that requires decompiling target usages.
                        return currentTrait.name_index;
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
        if (ignoreCaret) {
            return;
        }

        getCaret().setVisible(true);
        int pos = getCaretPosition();
        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCaret(true);
        lastTraitIndex = GraphTextWriter.TRAIT_UNKNOWN;
        try {
            classIndex = -1;
            Highlighting cm = Highlighting.searchPos(highlightedText.getClassHighlights(), pos);
            if (cm != null) {
                classIndex = (int) cm.getProperties().index;
            }
            displayClass(classIndex, script.scriptIndex, script.isSimple || script.traitIndices.isEmpty());
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

                    currentTrait = getCurrentTrait();
                    isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                    if (currentTrait != null) {
                        name += ":" + currentTrait.getName(abc).getName(abc.constants, null, false, true);
                    }

                    if (currentTrait instanceof TraitSlotConst) {
                        currentTrait = null;
                    }
                }

                displayMethod(pos, (int) tm.getProperties().index, name, currentTrait, lastTraitIndex, isStatic);
                currentMethodHighlight = tm;
                return;
            }

            if (classIndex == -1) {
                abcPanel.navigator.setClassIndex(-1, script.scriptIndex, script.isSimple || script.traitIndices.isEmpty());
                //setNoTrait();
                //return;
            }
            Trait currentTrait;
            currentTraitHighlight = Highlighting.searchPos(highlightedText.getTraitHighlights(), pos);
            if (currentTraitHighlight != null) {
                lastTraitIndex = (int) currentTraitHighlight.getProperties().index;
                currentTrait = getCurrentTrait();
                if (currentTrait != null) {
                    if (currentTrait instanceof TraitClass) {
                        abcPanel.detailPanel.classTraitPanel.load((TraitClass) currentTrait, abc, true);
                        final Trait ftrait = currentTrait;
                        final int ftraitIndex = lastTraitIndex;
                        View.execInEventDispatch(() -> {
                            abcPanel.detailPanel.showCard(DetailPanel.CLASS_TRAIT_CARD, ftrait, ftraitIndex, abc);
                        });
                        abcPanel.detailPanel.setEditMode(false);
                        currentMethodHighlight = null;
                        Highlighting spec = Highlighting.searchPos(highlightedText.getSpecialHighlights(), pos, currentTraitHighlight.startPos, currentTraitHighlight.startPos + currentTraitHighlight.len);
                        if (spec != null) {
                            abcPanel.detailPanel.classTraitPanel.hilightSpecial(spec);
                        }
                        return;
                    } else if (currentTrait instanceof TraitSlotConst) {
                        abcPanel.detailPanel.slotConstTraitPanel.load((TraitSlotConst) currentTrait, abc,
                                abc.isStaticTraitId(classIndex, lastTraitIndex));
                        final Trait ftrait = currentTrait;
                        final int ftraitIndex = lastTraitIndex;
                        View.execInEventDispatch(() -> {
                            abcPanel.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD, ftrait, ftraitIndex, abc);
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
                String name = classIndex == -1 ? "" : abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, true).toPrintableString(true);
                currentTrait = getCurrentTrait();
                isStatic = abc.isStaticTraitId(classIndex, lastTraitIndex);
                if (currentTrait != null) {
                    if (!name.isEmpty()) {
                        name += ":";
                    }
                    name += currentTrait.getName(abc).getName(abc.constants, null, false, true);
                }

                int methodId;
                if (classIndex > -1) {
                    methodId = abc.findMethodIdByTraitId(classIndex, lastTraitIndex);
                } else {
                    methodId = ((TraitMethodGetterSetter) abc.script_info.get(script.scriptIndex).traits.traits.get(lastTraitIndex)).method_info;
                }
                displayMethod(pos, methodId, name, currentTrait, lastTraitIndex, isStatic);
                return;
            }
            setNoTrait();
        } finally {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCaret(false);
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

    /**
     * WARNING: This won't change script. This only hilights class in current
     * script
     */
    public void gotoClassHeader() {
        Highlighting tc = Highlighting.searchIndex(highlightedText.getClassHighlights(), classIndex);
        if (tc != null) {
            final int fpos = tc.startPos;
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

    public void gotoMethod(int methodId) {
        gotoMethod(methodId, null);
    }

    public void gotoMethod(int methodId, Runnable afterHandler) {
        Highlighting tm = Highlighting.searchIndex(highlightedText.getMethodHighlights(), methodId);
        if (tm != null) {
            int pos = 0;
            if (tm.len > 1) {
                ignoreCaret = true;
                int startPos = tm.startPos + tm.len - 1;
                if (startPos <= getDocument().getLength()) {
                    setCaretPosition(startPos);
                }
                ignoreCaret = false;
            }
            pos = tm.startPos;

            final int fpos = pos;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (fpos <= getDocument().getLength()) {
                        setCaretPosition(fpos);
                    }
                    if (afterHandler != null) {
                        afterHandler.run();
                    }
                }
            }, 100);
        }
    }

    public void gotoTrait(int traitId) {
        boolean isScriptInit = traitId == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER;

        Highlighting tc = Highlighting.searchIndex(highlightedText.getClassHighlights(), classIndex);

        if (!isScriptInit && tc == null) {
            List<Highlighting> traitHighlights = highlightedText.getTraitHighlights();
            List<Highlighting> classHighlights = highlightedText.getClassHighlights();
            looph:
            for (Highlighting th : traitHighlights) {
                if (th.getProperties().index == traitId) {
                    for (Highlighting tc2 : classHighlights) {
                        if (tc2.startPos <= th.startPos && tc2.startPos + tc2.len >= th.startPos + th.len) {
                            continue looph;
                        }
                    }
                    final int fpos = th.startPos + 1; //+1 to skip script initializer which is on first character
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (fpos <= getDocument().getLength()) {
                                setCaretPosition(fpos);
                            }
                        }
                    }, 100);
                    break;
                }
            }
        }

        if (tc != null || isScriptInit) {
            Highlighting th = Highlighting.searchIndex(highlightedText.getTraitHighlights(), traitId, isScriptInit ? 0 : tc.startPos, isScriptInit ? -1 : tc.startPos + tc.len);
            int pos = 0;
            if (th != null) {
                if (th.len > 1) {
                    ignoreCaret = true;
                    int startPos = th.startPos + th.len - 1;
                    if (startPos <= getDocument().getLength()) {
                        setCaretPosition(startPos);
                    }
                    ignoreCaret = false;
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
        scriptLoaded = false;

        if (!force && this.script == scriptLeaf) {
            scriptLoaded = true;
            fireScript();
            return;
        }

        String sn = scriptLeaf.getClassPath().toString();
        setScriptName(sn, sn);
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
            CancellableWorker worker = new CancellableWorker("abc.decompiledEditorPane") {
                @Override
                protected Void doInBackground() throws Exception {

                    if (decompileNeeded) {
                        //long timeBefore = System.currentTimeMillis();
                        setShowMarkers(false);
                        View.execInEventDispatch(() -> {
                            setText("// " + AppStrings.translate("work.decompiling") + "...");
                        });

                        HighlightedText htext = SWF.getCached(scriptLeaf);
                        //long timeAfter = System.currentTimeMillis();
                        //long delta = timeAfter - timeBefore;
                        //System.err.println("Finished in " + Helper.formatTimeSec(delta));
                        setShowMarkers(true);
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
                            setShowMarkers(false);
                            setText("// " + AppStrings.translate("work.canceled"));
                        } catch (Exception ex) {
                            Throwable cause = ex;
                            if (ex instanceof ExecutionException) {
                                cause = ex.getCause();
                            }
                            setShowMarkers(false);
                            if (cause instanceof CancellationException) {
                                setText("// " + AppStrings.translate("work.canceled"));
                            } else {
                                logger.log(Level.SEVERE, "Error", cause);
                                setText("// " + AppStrings.translate("decompilationError") + ": " + cause);
                            }
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
            setShowMarkers(true);
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

        scriptLoaded = true;

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
        final int pos = View.textComponentViewToModel(abcPanel.decompiledTextArea, point);
        final String identifier = abcPanel.getMainPanel().getActionPanel().getStringUnderPosition(pos, abcPanel.decompiledTextArea);

        if (identifier != null && !identifier.isEmpty()) {
            String tooltipText = abcPanel.getDebugPanel().localsTable.tryGetDebugHoverToolTipText(identifier);
            return (tooltipText == null ? super.getToolTipText() : tooltipText);
        }

        // not found: so return existing text
        return super.getToolTipText();
    }
}
