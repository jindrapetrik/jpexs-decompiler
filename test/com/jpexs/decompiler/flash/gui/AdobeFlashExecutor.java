/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.fastactionlist.ActionItem;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.javactivex.ActiveX;
import com.jpexs.javactivex.ActiveXEvent;
import com.jpexs.javactivex.ActiveXEventListener;
import com.jpexs.javactivex.Reference;
import com.jpexs.javactivex.example.controls.flash.ShockwaveFlash;
import java.awt.Panel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AdobeFlashExecutor {

    private static final AtomicInteger id = new AtomicInteger();

    private final Object lockObj = new Object();

    private final Reference<String> resultRef = new Reference<>(null);

    private final File runFileAs2;

    private final File runFileAs3;

    private final ShockwaveFlash flash;

    public DoABC2Tag as3TestSwfAbcTag;

    public AdobeFlashExecutor() {
        flash = ActiveX.createObject(ShockwaveFlash.class, new Panel());
        flash.setAllowScriptAccess("always");
        flash.setAllowNetworking("all");
        flash.addFSCommandListener(new ActiveXEventListener() {
            @Override
            public void onEvent(ActiveXEvent axe) {
                resultRef.setVal((String) axe.args.get("args"));
                synchronized (lockObj) {
                    lockObj.notify();
                }
            }
        });

        runFileAs2 = new File("libsrc/ffdec_lib/testdata/run_as2/run_as2.swf");
        runFileAs3 = new File("libsrc/ffdec_lib/testdata/run_as3/run.swf");
    }

    public String executeActionList(List<Action> actionsToExecute) {
        try {
            File f2 = new File("run_test_" + new Date().getTime() + "_" + id.getAndIncrement() + ".swf");
            f2.deleteOnExit();

            SWF swf;
            try (InputStream is = new BufferedInputStream(new FileInputStream(runFileAs2))) {
                swf = new SWF(is, false);
            }

            Map<String, ASMSource> asms = swf.getASMs(true);
            ASMSource asm = asms.get("\\frame_1\\DoAction");
            ActionList actions = asm.getActions();
            actions.removeAction(2, 4);

            actions.addActions(2, actionsToExecute);

            asm.setActions(actions);
            asm.setModified();
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
                swf.saveTo(fos);
            }

            flash.setMovie(f2.getAbsolutePath());

            synchronized (lockObj) {
                lockObj.wait();
            }

            //String str = flash.GetVariable("myText.text");
            f2.delete();

            String flashResult = resultRef.getVal();
            return flashResult;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(AdobeFlashExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void executeActionLists(List<AS2ExecuteTask> tasks) {
        try {
            File f2 = new File("run_test_" + new Date().getTime() + "_" + id.getAndIncrement() + ".swf");
            f2.deleteOnExit();

            SWF swf;
            try (InputStream is = new BufferedInputStream(new FileInputStream(runFileAs2))) {
                swf = new SWF(is, false);
            }

            Map<String, ASMSource> asms = swf.getASMs(true);
            ASMSource asm = asms.get("\\frame_1\\DoAction");

            ActionList actionsList = asm.getActions();
            FastActionList actions = new FastActionList(actionsList);
            actions.removeItem(2, 4);

            int i = 0;
            ActionItem item = actions.get(1);
            for (AS2ExecuteTask task : tasks) {
                DoActionTag doaTag = new DoActionTag(swf);
                List<Action> actions2 = new ArrayList<>();
                int codeSize = 1; // 1 == size of return action
                for (Action actionsToExecute : task.actions) {
                    codeSize += actionsToExecute.getBytesLength();
                }

                actions2.add(new ActionDefineFunction("testRun" + i, new ArrayList<>(), codeSize, swf.version));
                actions2.addAll(task.actions);
                actions2.add(new ActionReturn());

                doaTag.setActions(actions2);
                swf.addTag(doaTag, asm);

                i++;
            }

            item = actions.insertItemAfter(item, new ActionPush(new Object[]{tasks.size(), 1, "runTests"}));
            actions.insertItemAfter(item, new ActionCallFunction());
            asm.setActions(actions.toActionList());
            asm.setModified();
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
                swf.saveTo(fos);
            }

            flash.setMovie(f2.getAbsolutePath());

            synchronized (lockObj) {
                lockObj.wait();
            }

            //String str = flash.GetVariable("myText.text");
            f2.delete();

            String flashResult = resultRef.getVal();
            String[] lines = flashResult.split("(\r\n|\r|\n)");
            if (lines.length == tasks.size()) {
                for (int j = 0; j < tasks.size(); j++) {
                    tasks.get(j).flashResult = lines[j];
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(AdobeFlashExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTestSwf() throws IOException, InterruptedException {
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream(runFileAs3)), false);
        swf.version = SWF.MAX_VERSION;
        DoABC2Tag abcTag = null;
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abcTag = ((DoABC2Tag) t);
                break;
            }
        }

        as3TestSwfAbcTag = abcTag;
    }

    public String executeAvm2(AVM2Code code) {
        try {
            File f2 = new File("run_test_" + new Date().getTime() + "_" + id.getAndIncrement() + ".swf");
            f2.deleteOnExit();

            if (as3TestSwfAbcTag == null) {
                loadTestSwf();
            }

            DoABC2Tag abcTag = as3TestSwfAbcTag;
            ABC abc = abcTag.getABC();
            MethodBody body = abc.findBodyByClassAndName("Run", "run");
            body.max_stack = 20;
            body.max_regs = 10;

            body.setCode(code);

            abcTag.setModified(true);

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
                abcTag.getSwf().saveTo(fos);
            }

            flash.setMovie(f2.getAbsolutePath());

            synchronized (lockObj) {
                lockObj.wait();
            }

            f2.delete();

            String flashResult = resultRef.getVal();
            return flashResult;

            /*int cnt = 0;
             while (flash.getReadyState() != 4) {
             Thread.sleep(50);
             if (cnt > 100) {
             fail("Flash init timeout");
             }

             cnt++;
             }*/
 /*try {
             String res = flash.CallFunction("<invoke name=\"testFunc\" returntype=\"xml\"><arguments><string>something</string></arguments></invoke>");
             //String str = flash.GetVariable("_root.myText.text");
             throw new Error(res + " " + body.getCode().toString() + "");
             } catch (Exception ex) {
             int a = 1;
             }*/
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(AdobeFlashExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void executeAvm2(List<AS3ExecuteTask> tasks) {
        try {
            File f2 = new File("run_test_" + new Date().getTime() + "_" + id.getAndIncrement() + ".swf");
            f2.deleteOnExit();

            if (as3TestSwfAbcTag == null) {
                loadTestSwf();
            }

            DoABC2Tag abcTag = as3TestSwfAbcTag;
            ABC abc = abcTag.getABC();
            int classId = abc.findClassByName("Run");
            MethodBody body = abc.findBodyByClassAndName("Run", "run");
            body.max_stack = 20;
            body.max_regs = 10;

            Multiname multiname = new Multiname();
            multiname.kind = Multiname.QNAME;
            multiname.name_index = abc.constants.getStringId("executeStaticMethod", true);
            multiname.namespace_index = abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true);
            int multinameId = abc.constants.getMultinameId(multiname, true);

            AVM2Code ccode = new AVM2Code();
            List<AVM2Instruction> code = ccode.code;
            code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
            code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
            code.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("", true)}));

            int i = 0;
            for (AS3ExecuteTask task : tasks) {
                String testName = "test_" + i;
                code.add(new AVM2Instruction(0, AVM2Instructions.FindPropertyStrict, new int[]{multinameId}));
                code.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId(testName, true)}));
                code.add(new AVM2Instruction(0, AVM2Instructions.CallProperty, new int[]{multinameId, 1}));
                code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));

                addMethod(abc, classId, testName, true, task.code);
                i++;
            }

            code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
            ccode.markOffsets();
            body.setCode(ccode);

            abcTag.setModified(true);

            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2))) {
                abcTag.getSwf().saveTo(fos);
            }

            flash.setMovie(f2.getAbsolutePath());

            synchronized (lockObj) {
                lockObj.wait();
            }

            f2.delete();

            String flashResult = resultRef.getVal();
            if (!flashResult.startsWith("Result:") || !flashResult.endsWith(" Type:string")) {
                return;
            }

            flashResult = flashResult.substring(7);
            flashResult = flashResult.substring(0, flashResult.length() - 12);
            String[] lines = flashResult.split("(\r\n|\r|\n)");
            if (lines.length == tasks.size()) {
                for (int j = 0; j < tasks.size(); j++) {
                    tasks.get(j).flashResult = lines[j];
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(AdobeFlashExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int addMethod(ABC abc, int classId, String name, boolean isStatic, AVM2Code code) {
        TraitMethodGetterSetter methodTrait = abc.addMethod(classId, name, isStatic);
        MethodInfo methodInfo = abc.method_info.get(methodTrait.method_info);
        MethodBody methodBody = abc.findBody(methodInfo);
        methodBody.max_stack = 10;
        methodBody.max_regs = 10;
        methodBody.init_scope_depth = 3;
        methodBody.max_scope_depth = 10;

        methodBody.setCode(code);
        methodBody.autoFillMaxRegs(abc);

        return methodTrait.name_index;
    }
}
